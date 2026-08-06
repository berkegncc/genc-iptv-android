#!/usr/bin/env python3
"""Verify the two locales define the same keys.

A key present in res/values/ but missing from res/values-tr/ does not fail the
build and does not warn — Android simply falls back, so a Turkish user sees one
English line in an otherwise Turkish screen and nobody finds out until they do.
This is the check that would have caught it.

Keys marked translatable="false" are expected in values/ only.

    python tools/check_locales.py

Exits non-zero when the locales disagree, so it can gate a commit or CI step.
"""

import glob
import io
import os
import re
import sys

RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "res")
DECL = re.compile(r'<(string|plurals)\s+name="([^"]+)"([^>]*)>')


def keys_in(folder):
    """Every translatable key in a locale folder, by file."""
    found = {}
    for path in sorted(glob.glob(os.path.join(folder, "strings*.xml"))):
        text = io.open(path, encoding="utf-8").read()
        names = {
            m.group(2)
            for m in DECL.finditer(text)
            if 'translatable="false"' not in m.group(3)
        }
        found[os.path.basename(path)] = names
    return found


def main():
    english = keys_in(os.path.join(RES, "values"))
    turkish = keys_in(os.path.join(RES, "values-tr"))

    if not english:
        print("res/values/ has no strings files — wrong directory?")
        return 1

    problems = []
    for name in sorted(set(english) | set(turkish)):
        en = english.get(name, set())
        tr = turkish.get(name, set())
        status = "ok " if en == tr else "!! "
        print("  %s%-28s en=%-3d tr=%-3d" % (status, name, len(en), len(tr)))
        for key in sorted(en - tr):
            problems.append("%s: '%s' missing from values-tr/ — Turkish users see English" % (name, key))
        for key in sorted(tr - en):
            problems.append("%s: '%s' missing from values/ — every other language has no text at all" % (name, key))

    print()
    if problems:
        print("%d problem(s):" % len(problems))
        for p in problems:
            print("  - " + p)
        return 1

    total = len(set().union(*english.values())) if english else 0
    print("Both locales define the same %d keys." % total)
    return 0


if __name__ == "__main__":
    sys.exit(main())
