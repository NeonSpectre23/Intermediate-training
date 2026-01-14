/**
 * Safely convert any table cell value to a displayable string.
 * Handles objects with no prototype (Object.create(null)) and other edge cases
 * that might throw when using String() directly.
 */
export const safeCellText = (value: any): string => {
  if (value === null || value === undefined) {
    return "";
  }

  // Numbers and booleans are safe to stringify directly
  if (typeof value === "number" || typeof value === "boolean") {
    return String(value);
  }

  // Strings are already fine
  if (typeof value === "string") {
    return value;
  }

  // Attempt to JSON stringify objects / arrays / maps, fallback to toString
  if (typeof value === "object") {
    try {
      return JSON.stringify(value);
    } catch {
      try {
        return Object.prototype.toString.call(value);
      } catch {
        return "";
      }
    }
  }

  // Fallback for symbols / bigint / anything else
  try {
    return String(value);
  } catch {
    return "";
  }
};
