"""
Builds a wordlist from a text corpus.

Inspired by:
https://www.kdnuggets.com/2017/11/building-wikipedia-text-corpus-nlp.html
"""

import time
import re
import argparse

from collections import Counter

def build_wordlist(input_file):

	"""Build a wordlist Counter from lines of the corpus file"""
	
	wordlist = Counter()
	
	for line in input_file:
		words = re.findall(r'\w+', line)
		wordlist.update(words)
	
	return wordlist

def write_wordlist(wordlist, wordlist_file):

	"""Write wordlist Counter into wordlist_file"""
	
	for item in wordlist.most_common():
		word, count = item
		wordlist_file.write(word + '\t' + str(count) + '\n')
	
	wordlist_file.close()

parser = argparse.ArgumentParser('build_wiki_wordlist')
parser.add_argument('inputfile', help='corpus text file', type=argparse.FileType('r', encoding='utf-8'))
parser.add_argument('outputfile', help='generated word list file', type=argparse.FileType('w', encoding='utf-8'))
args = parser.parse_args()

print('Building wordlist at ' + time.ctime())
wordlist = build_wordlist(args.inputfile)
print('Writing wordlist at ' + time.ctime())
write_wordlist(wordlist, args.outputfile)
print('Processing complete at ' + time.ctime())
