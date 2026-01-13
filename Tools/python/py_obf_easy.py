#!/usr/bin/env python3
"""
Python 混淆器（轻量版），对特殊占位符 <<$ target $>> 做保护。

行为：
- 对字符串进行“加密”替换、数值常量拆分、注入死代码等变换。
- 基于正则生成重命名映射并对标识符进行重命名。
- **保护特殊标记 `<<$ target $>>`**：在变换前用哨兵替换，变换后恢复，确保该标记不会被混淆或修改。

用法：
  python tools/obfuscator.py -i input.py -o output_obfuscated.py
"""

import random
import string
import re
import ast
import os
import argparse

# ---------------- 工具函数 ----------------
def gen_name(prefix="z"):
    s = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
    return f"{prefix}_{s}"

def random_garbage_comment():
    return "# " + ''.join(random.choices('abcdefghijklmnopqrstuvwxyz0123456789', k=12))

def is_gpt_hint_comment(text: str) -> bool:
    return ('<<$' in text) and ('$>>' in text)

# ---------------- 基于正则的标识符收集器 ----------------
def collect_idents_by_regex(src_text, max_results=2000):
    """
    通过正则在源文本中收集候选标识符（Python 风格）。
    跳过一些内建/常见名字以减少误伤。
    """
    cand = set()
    skip = {
        'True','False','None','self','cls','print','input','len','range',
        'int','float','str','dict','list','set','tuple','open','os','sys',
        'random','math'
    }
    for m in re.finditer(r'\b([A-Za-z_][A-Za-z0-9_]*)\b', src_text):
        name = m.group(1)
        if name in skip:
            continue
        if not name.isupper():
            cand.add(name)
    filtered = list(cand)
    return filtered[:max_results]

def generate_rename_map_from_regex(src_text, prefix="z"):
    """
    由正则生成重命名映射。避免重命名内部哨兵名。
    """
    idents = collect_idents_by_regex(src_text)
    rename_map = {}
    for name in idents:
        if name.startswith("__GPT_PLACEHOLDER"):
            continue
        new = gen_name(prefix)
        while new in rename_map.values():
            new = gen_name(prefix)
        rename_map[name] = new
    return rename_map

# ---------------- 标识符替换（保留字符串与特定 token） ----------------
_identifier_or_string_re = re.compile(
    r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|\b[A-Za-z_][A-Za-z0-9_]*\b',
    flags=re.S
)

def replace_identifiers_preserve_strings(text, rename_map, preserve_tokens=None):
    """
    根据 rename_map 替换标识符，但：
    - 保留字符串字面量不变
    - 保留 preserve_tokens 中的 token（精确匹配）
    """
    if not rename_map:
        return text
    if preserve_tokens is None:
        preserve_tokens = set()

    out = []
    last_end = 0
    for m in _identifier_or_string_re.finditer(text):
        start, end = m.span()
        if last_end < start:
            out.append(text[last_end:start])
        token = m.group(0)
        # 字符串字面量保持不变
        if token and (token[0] in ('"', "'")):
            out.append(token)
        else:
            # 如果 token 在保留集合中，直接写回
            if token in preserve_tokens:
                out.append(token)
            elif token in rename_map:
                out.append(rename_map[token])
            else:
                out.append(token)
        last_end = end
    if last_end < len(text):
        out.append(text[last_end:])
    return ''.join(out)

# ---------------- 字符串“加密”辅助（Python 形式） ----------------
def xor_encrypt_bytes(bs: bytes, key=None):
    if key is None:
        key = random.randint(1, 255)
    enc = bytes((b ^ key) & 0xff for b in bs)
    return enc, key

def xor_encrypt_to_pybytes_literal(s: str):
    bs = s.encode('utf-8', errors='surrogatepass')
    enc, key = xor_encrypt_bytes(bs)
    arr = ", ".join(f"{b}" for b in enc)
    return arr, len(enc), key

def replace_strings_with_decrypt_calls(src_text: str, decrypt_fn_name: str):
    """
    将字符串字面量替换为 decrypt_fn_name(bytes([...]), key) 调用，返回 (new_text, entries)。
    """
    entries = []
    new_parts = []
    last = 0
    for m in re.finditer(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'', src_text):
        s_start, s_end = m.span()
        literal = m.group(0)
        raw_content = literal[1:-1]
        try:
            arr, length, key = xor_encrypt_to_pybytes_literal(raw_content)
        except Exception:
            arr, length, key = xor_encrypt_to_pybytes_literal(raw_content)
        call = f'{decrypt_fn_name}(bytes([{arr}]), {key})'
        entries.append({"original": literal, "call": call, "len": length, "key": key})
        new_parts.append(src_text[last:s_start])
        new_parts.append(call)
        last = s_end
    new_parts.append(src_text[last:])
    return ''.join(new_parts), entries

def gen_decryptor_py(decrypt_fn_name):
    fn = f"""
def {decrypt_fn_name}(data: bytes, key: int) -> str:
    # 自动生成的解密函数
    return bytes(b ^ key for b in data).decode('utf-8', errors='surrogatepass')
"""
    return fn

# ---------------- 数值混淆 ----------------
def obfuscate_number_expr(num: int):
    if num == 0:
        return "(0)"
    r = random.random()
    if r < 0.35:
        k = random.randint(1, 255)
        return f"(({num ^ k}) ^ {k})"
    elif r < 0.7:
        a = random.randint(1, min(6, num.bit_length() if num>0 else 1))
        hi = num >> a
        lo = num & ((1 << a) - 1)
        return f"(({hi}) << {a}) + ({lo})"
    else:
        a = random.randint(1, max(2, abs(num) // 2))
        b = num - a
        return f"(({a}) + ({b}))"

def split_numeric_constants_in_text_enhanced(text: str, probability=0.7):
    """
    在非标识符片段中对数字字面量进行拆分混淆（按概率）。
    """
    def repl_in_segment(seg):
        def split_num(m):
            num = m.group(0)
            try:
                if num.startswith(('0x', '0X')):
                    val = int(num, 16)
                    if random.random() < probability:
                        a = random.randint(1, 255)
                        return f"((0x{val ^ a:x}) ^ 0x{a:x})"
                    else:
                        return num
                else:
                    val = int(num, 10)
                    if abs(val) > 8 and random.random() < probability:
                        return obfuscate_number_expr(val)
                    else:
                        return num
            except Exception:
                return num
        return re.sub(r'\b0x[0-9A-Fa-f]+\b|\b\d+\b', split_num, seg)
    out = []
    last_end = 0
    for m in re.finditer(r'\b([A-Za-z_][A-Za-z0-9_]*)\b', text):
        start, end = m.span()
        if last_end < start:
            seg = text[last_end:start]
            out.append(repl_in_segment(seg))
        token = m.group(0)
        out.append(token)
        last_end = end
    if last_end < len(text):
        out.append(repl_in_segment(text[last_end:]))
    return ''.join(out)

# ---------------- 注入死代码 ----------------
def gen_dead_function(name=None):
    """
    生成一个简单的死代码函数，返回伪随机数，用于增加代码噪声。
    """
    if name is None:
        name = gen_name("zdead")
    fn = f"""
def {name}():
    import random as _r
    t = 0
    for i in range(5):
        t += _r.randint(0, 255)
    return t
"""
    return fn, name

# ---------------- 主要混淆流水线 ----------------
# 要保护的占位符与内部哨兵
_PLACEHOLDER = '<<$ target $>>'
_SENTINEL = '__GPT_PLACEHOLDER_TARGET__'  # 不太可能与真实标识符冲突

def obfuscate_file_text(input_path, rename_map=None, enable_strings=True,
                        enable_const=True, enable_opaque=True, garbage_count=2,
                        strip_comments=True):
    """
    读取文件，保护占位符，执行一系列变换，然后恢复占位符。
    返回 (新的源文本, meta 信息)。
    """
    with open(input_path, 'r', encoding='utf-8', errors='ignore') as f:
        src_text = f.read()

    # 将特殊占位符替换为哨兵，防止被混淆
    placeholder_count = src_text.count(_PLACEHOLDER)
    if placeholder_count > 0:
        src_text = src_text.replace(_PLACEHOLDER, _SENTINEL)

    injected_top_parts = []
    injected_funcs = []

    # 可选：去除或替换注释（保留包含 <<$ ... $>> 的提示注释）
    if strip_comments:
        def repl_block(m):
            s = m.group(0)
            if is_gpt_hint_comment(s):
                return s
            nl = s.count('\n')
            return (random_garbage_comment() + ("\n" * nl))
        # Python 中较少块注释，用正则尝试替换多行注释样式（兼容）
        src_text = re.sub(r'(?s)/\*.*?\*/', repl_block, src_text)
        def repl_line(m):
            s = m.group(0)
            if is_gpt_hint_comment(s):
                return s
            return '# ' + ''.join(random.choices('abcdefghijklmnopqrstuvwxyz', k=12))
        src_text = re.sub(r'(?m)#.*$', repl_line, src_text)

    # 字符串处理：替换为解密函数调用
    decrypt_fn_name = None
    string_entries = []
    if enable_strings:
        decrypt_fn_name = gen_name("dec")
        src_text, string_entries = replace_strings_with_decrypt_calls(src_text, decrypt_fn_name)
        if string_entries:
            decrypt_py = gen_decryptor_py(decrypt_fn_name)
            injected_top_parts.append(decrypt_py)

    # 数值混淆
    if enable_const:
        src_text = split_numeric_constants_in_text_enhanced(src_text, probability=0.7)

    # 重命名时保留哨兵名字
    preserve_tokens = { _SENTINEL }

    # 标识符重命名（如果传入 rename_map）
    if rename_map:
        src_text = replace_identifiers_preserve_strings(src_text, rename_map, preserve_tokens=preserve_tokens)

    # 注入死代码函数
    dead_names = []
    for i in range(garbage_count):
        fn_c, fn_name = gen_dead_function()
        injected_funcs.append(fn_c)
        dead_names.append(fn_name)

    if injected_funcs:
        injected_top_parts.extend(injected_funcs)

    # 将注入内容放到文件顶部（例如在 import 之后）
    top_inject = "\n".join(injected_top_parts) + "\n"
    if top_inject.strip():
        src_text = top_inject + src_text

    # 在顶部附近插入不透明谓词（使用死代码函数）
    if enable_opaque and dead_names:
        insert_pos = 0
        imp = list(re.finditer(r'(?m)^\s*(import|from)\b.*$', src_text))
        if imp:
            insert_pos = imp[-1].end()
        opaque_snippets = []
        for _ in range(min(3, len(dead_names))):
            expr = gen_opaque_predicate_expr_py()
            df = random.choice(dead_names)
            snippet = f'\n# 不透明谓词\nif {expr}:\n    pass\nelse:\n    {df}()\n'
            opaque_snippets.append(snippet)
        src_text = src_text[:insert_pos] + "\n".join(opaque_snippets) + src_text[insert_pos:]

    # 恢复哨兵为原始占位符
    if placeholder_count > 0:
        src_text = src_text.replace(_SENTINEL, _PLACEHOLDER)

    return src_text, {"strings": string_entries, "dead": dead_names}

# 生成 Python 形式的不透明谓词表达式
def gen_opaque_predicate_expr_py():
    a = random.randint(1, 1 << 30)
    b = random.randint(1, 1 << 30)
    mod = random.randint(97, 997) | 1
    val = ((a * b) ^ ((a << 5) | (b >> 3))) % mod
    return f"(((({a} * {b}) ^ (({a}<<5) | ({b}>>3))) % {mod}) == {val})"

# ---------------- 命令行入口 ----------------
def main():
    parser = argparse.ArgumentParser(description="Python 混淆器（保护占位符 <<$ target $>>）。")
    parser.add_argument("input", help="要混淆的输入 Python 文件（例如 dataset/sample1/source_half.py）")
    parser.add_argument("-o", "--output", help="输出混淆后的 Python 文件（例如 dataset/sample1/source_obf.py）", required=True)
    parser.add_argument("--no-strings", action="store_true", help="不要替换字符串字面量")
    parser.add_argument("--no-const", action="store_true", help="不要混淆数值常量")
    parser.add_argument("--no-opaque", action="store_true", help="不要插入不透明谓词")
    parser.add_argument("--garbage-count", type=int, default=2, help="注入的死代码函数数量")
    parser.add_argument("--prefix", default="z", help="重命名前缀")
    args = parser.parse_args()

    input_file = args.input
    output_file = args.output

    if not os.path.isfile(input_file):
        print(f"Error: 输入文件不存在: {input_file}")
        raise SystemExit(1)

    # 读取文件内容用于生成 rename_map（先把占位符替换为哨兵，避免被收集到）
    with open(input_file, 'r', encoding='utf-8', errors='ignore') as f:
        src_text = f.read()

    src_for_map = src_text.replace(_PLACEHOLDER, _SENTINEL)

    # 生成重命名映射（基于正则）
    rename_map = generate_rename_map_from_regex(src_for_map, prefix=args.prefix)

    obf_text, meta = obfuscate_file_text(
        input_file,
        rename_map=rename_map,
        enable_strings=not args.no_strings,
        enable_const=not args.no_const,
        enable_opaque=not args.no_opaque,
        garbage_count=max(0, args.garbage_count),
        strip_comments=True
    )

    # 将混淆后文本写入输出文件
    with open(output_file, 'w', encoding='utf-8', errors='ignore') as f:
        f.write(obf_text)

    print("已写入混淆文件:", output_file)
    if meta.get("strings"):
        print("替换了字符串字面量次数:", len(meta["strings"]))
    if meta.get("dead"):
        print("注入的死代码函数:", meta["dead"])

if __name__ == "__main__":
    main()
