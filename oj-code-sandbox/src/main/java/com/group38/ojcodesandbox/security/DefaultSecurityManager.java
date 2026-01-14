package com.group38.ojcodesandbox.security;

//默认安全管理器

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager{
    @Override
    public void checkPermission(Permission perm) {
        //System.out.println("默认不做任何限制");
        super.checkPermission(perm);
    }
}
