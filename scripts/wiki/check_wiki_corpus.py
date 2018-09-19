"""
Checks a corpus created from a Wikipedia dump file.

Inspired by:
https://www.kdnuggets.com/2017/11/building-wikipedia-text-corpus-nlp.html
"""

import time
import argparse

parser = argparse.ArgumentParser(prog='check_wiki_corpus', description='Checks a corpus created from a Wikipedia dump file.')
parser.add_argument('inputfile', help='corpus text file', type=argparse.FileType('r', encoding='utf-8'))
args = parser.parse_args()

while(1):
	for lines in range(10):
		print(args.inputfile.readline())
	user_input = input('>>> Type \'STOP\' to quit or hit Enter key for more <<< ')
	if user_input == 'STOP':
		break
