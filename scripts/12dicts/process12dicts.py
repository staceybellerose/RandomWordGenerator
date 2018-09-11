#!/usr/bin/env python
"""
Process the 12dicts files, received via pipeline

Rules for processing of words

If a "word" is 1 or 2 characters in length, discard it.
If a word contains one or more spaces (i.e. a phrase), discard it.
If a word is an abbreviation (ends in "."), discard it.
If a word has a slash in it, discard it.
If a word matches the previous word, ignoring capitalization, discard it.
Based on the annotations below, either keep or discard the word.

6of12 annotations unwanted: ".:&"
6of12 annotations to strip: "=#+~^<"

2of12 annotations to strip: "%!"

3esl annotations unwanted: ":"

3of6game annotations unwanted: none
3of6game annotations to strip: "&$^~+!"
3of6game command line parameter: -v

Differences from version 1.0:
	keep hyphenated words
"""

import sys
import re
import time
import argparse

print('Process started at  ' +  time.ctime(), file=sys.stderr)

parser = argparse.ArgumentParser("process12dicts")
parser.add_argument("-v", "--includeVariants", default=False, action="store_true", help="Include variant (non-American) spellings of words")
parser.add_argument("inputfile", help="12dicts file to process", type=argparse.FileType("r"))
parser.add_argument("outputfile", help="name of file to output after processing", type=argparse.FileType("w"))
args = parser.parse_args()

if args.includeVariants:
	unwantedAnnotations=re.compile(r"[ .:/']")
else:
	unwantedAnnotations=re.compile(r"[ .:&/']")
	
stripAnnotations=re.compile(r"[=+~<^%!#$&]")

testwords = args.inputfile.read().splitlines()
args.inputfile.close()

lastword = ''
wordlist = []

for testword in testwords:
	word = re.sub(stripAnnotations, "", testword)
	if unwantedAnnotations.search(word):
		continue
	if len(word) < 3:
		continue
	if word.lower() == lastword.lower():
		continue
	wordlist.append(word)
	lastword = word

for word in wordlist:
	args.outputfile.write(word + '\n')

print('Process complete at ' + time.ctime(), file=sys.stderr)
