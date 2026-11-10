#!/usr/bin/env python3
"""Cheap structural checks for Kotlin sources (not a parser, but catches the
class of error that broke CI: unbalanced brackets outside strings/comments)."""
import sys, glob, re

def scan(path):
    s=open(path, encoding='utf-8').read()
    i=0; n=1; depth={'{':0,'(':0,'[':0}
    in_raw=False; in_line=False; in_block=False
    line_start=1
    while i < len(s):
        c=s[i]
        if c=='\n':
            n+=1; in_line=False
            i+=1; continue
        if in_line: i+=1; continue
        if in_block:
            if s.startswith('*/',i): in_block=False; i+=2; continue
            i+=1; continue
        if not in_raw:
            if s.startswith('/*',i): in_block=True; i+=2; continue
            if s.startswith('//',i): in_line=True; i+=2; continue
            if s.startswith('"""',i): in_raw=True; i+=3; continue
            if c=='"':
                j=i+1
                while j<len(s) and s[j]!='"':
                    if s[j]=='\\': j+=1
                    j+=1
                i=j+1; continue
            if c=="'":
                j=i+1
                while j<len(s) and s[j]!="'":
                    if s[j]=='\\': j+=1
                    j+=1
                i=j+1; continue
        else:
            if s.startswith('"""',i):
                in_raw=False; i+=3; continue
            i+=1; continue
        if c in '({[': depth[c]+=1
        elif c=='}': depth['{']-=1
        elif c==')': depth['(']-=1
        elif c==']': depth['[']-=1
        if any(v<0 for v in depth.values()):
            return f"{path}:{n}: negative depth {depth}"
        i+=1
    bad=[(k,v) for k,v in depth.items() if v]
    if bad or in_raw or in_block:
        return f"{path}: unbalanced at EOF {bad} raw={in_raw} block={in_block}"
    return None

errors=[e for e in (scan(p) for p in glob.glob('app/src/**/*.kt', recursive=True)) if e]
for e in errors: print("ERROR", e)
print("checked", len(glob.glob('app/src/**/*.kt', recursive=True)), "files;", len(errors), "problems")
sys.exit(1 if errors else 0)
