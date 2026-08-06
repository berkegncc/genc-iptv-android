#!/usr/bin/env python3
"""Escape apostrophes and quotes in Android string resources.

Android treats both characters specially inside <string> values:

  '  unescaped anywhere fails resource compilation outright.
  "  unescaped makes the value a "quoted string"; the parser strips the quotes
     and can swallow the rest of the line. This one is worse than the
     apostrophe, because nothing fails — the build succeeds and the string just
     renders short or empty at runtime.

Neither shows up in a Kotlin-only build; resources are only compiled at
assemble time, and the quote case is not even an error then.

    python tools/check_string_escapes.py           # report only
    python tools/check_string_escapes.py --fix     # rewrite the files

Exits non-zero when anything needs escaping, so it can gate a commit.
"""

import glob
import io
import os
import re
import sys

BS = chr(92)          # kept out of literals so no shell mangles this file
APOS = chr(39)
QUOTE = chr(34)
ESC_APOS = BS + APOS
ESC_QUOTE = BS + QUOTE

RES = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), '..', 'app', 'src', 'main', 'res'
)
STRING = re.compile(r'<string name="([^"]+)"([^>]*)>(.*?)</string>', re.S)

_APOS_SENTINEL = '\x00A\x00'
_QUOTE_SENTINEL = '\x00Q\x00'


def _protect(value):
    return value.replace(ESC_APOS, _APOS_SENTINEL).replace(ESC_QUOTE, _QUOTE_SENTINEL)


def _restore(value):
    return value.replace(_APOS_SENTINEL, ESC_APOS).replace(_QUOTE_SENTINEL, ESC_QUOTE)


def needs_escaping(value):
    bare = _protect(value)
    return APOS in bare or QUOTE in bare


def escaped(value):
    bare = _protect(value)
    bare = bare.replace(APOS, ESC_APOS).replace(QUOTE, ESC_QUOTE)
    return _restore(bare)


def main(argv):
    fix = '--fix' in argv
    files = sorted(
        glob.glob(os.path.join(RES, 'values', 'strings*.xml')) +
        glob.glob(os.path.join(RES, 'values-tr', 'strings*.xml'))
    )
    if not files:
        print('no string files found — wrong directory?')
        return 1

    problems = []
    for path in files:
        text = io.open(path, encoding='utf-8').read()
        for m in STRING.finditer(text):
            if needs_escaping(m.group(3)):
                problems.append((path, m.group(1), m.group(3)[:56]))

    for path, key, value in problems:
        short = os.path.join(os.path.basename(os.path.dirname(path)), os.path.basename(path))
        print('  %-30s %-34s %s' % (short, key, value))

    if not problems:
        print('  every string is escaped correctly')
        return 0

    if not fix:
        print()
        print('%d string(s) need escaping — rerun with --fix' % len(problems))
        return 1

    for path in sorted(set(p[0] for p in problems)):
        text = io.open(path, encoding='utf-8').read()
        fixed = STRING.sub(
            lambda m: '<string name="%s"%s>%s</string>' % (
                m.group(1), m.group(2), escaped(m.group(3))),
            text,
        )
        data = fixed.encode('utf-8')
        tmp = path + '.tmp'
        with io.open(tmp, 'wb') as fh:
            fh.write(data)
        os.replace(tmp, path)
        print('  fixed:', path)
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
