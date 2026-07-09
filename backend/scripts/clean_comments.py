#!/usr/bin/env python3
"""移除 Java 源文件中冗余注释（保留业务规则与枚举说明）。"""
from __future__ import annotations

import re
from pathlib import Path

JAVA_ROOT = Path(__file__).resolve().parent.parent / "src" / "main" / "java"

KEEP_SUBSTRINGS = (
    "仅允许自助注册",
    "明文密码",
    "loan_status",
    "status_code",
    "1-农户",
    "1-正常",
    "0-未登录",
    "1-待付款",
    "pending",
)


def keep_comment(text: str) -> bool:
    return any(token in text for token in KEEP_SUBSTRINGS)


def strip_javadoc(content: str) -> str:
    return re.sub(r"/\*\*.*?\*/\s*\n?", "", content, flags=re.DOTALL)


def strip_line_comment(line: str) -> str | None:
    stripped = line.strip()
    if not stripped.startswith("//"):
        return line

    if keep_comment(stripped):
        return line
    return None


def strip_inline_comment(line: str) -> str:
    if "//" not in line:
        return line

    code, _, comment = line.partition("//")
    if keep_comment(comment) or "://" in code:
        return line
    return code.rstrip()


def clean_content(content: str) -> str:
    content = strip_javadoc(content)
    cleaned_lines: list[str] = []
    for line in content.splitlines():
        if line.strip().startswith("//"):
            kept = strip_line_comment(line)
            if kept is not None:
                cleaned_lines.append(kept)
            continue
        cleaned_lines.append(strip_inline_comment(line))

    text = "\n".join(cleaned_lines)
    text = re.sub(r"\n{3,}", "\n\n", text)
    if not text.endswith("\n"):
        text += "\n"
    return text


def main() -> None:
    for path in JAVA_ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        updated = clean_content(original)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            print(f"cleaned: {path.relative_to(JAVA_ROOT)}")


if __name__ == "__main__":
    main()
