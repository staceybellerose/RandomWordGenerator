"""
Filter out words containing Latin characters from a word list.

This script is ONLY to be used on word lists from languages
which are based on non-Latin alphabets.
"""

import time
import re
import argparse

parser = argparse.ArgumentParser(prog='filter_latin_characters', description='Filter out words containing Latin characters from a word list.')
parser.add_argument('inputfile', help='word list file', type=argparse.FileType('r', encoding='utf-8'))
parser.add_argument('outputfile', help='filtered word list file', type=argparse.FileType('w', encoding='utf-8'))
args = parser.parse_args()

print('Processing started at  ' + time.ctime())

latin_letters = re.compile(r"[a-zA-Z]")
for line in args.inputfile:
	if not re.search(latin_letters, line):
		args.outputfile.write(line)

print('Processing complete at ' + time.ctime())
