"""
Convert an NLTK corpus to a word list
"""

import time
import re
import importlib
import argparse
import sys

from collections import Counter
from nltk.downloader import Downloader

def build_wordlist(lines, lower=True):

	"""Build a wordlist Counter from lines of the corpus file"""
	
	wordlist = Counter()
	
	for line in lines:
		words = re.findall(r'\w+', line)
		wordlist.update([x.lower() for x in words] if lower else words)
	
	return wordlist

def write_wordlist(wordlist, wordlist_file):

	"""Write wordlist Counter into wordlist_file"""
	
	for item in wordlist.most_common():
		word, count = item
		wordlist_file.write(word + '\t' + str(count) + '\n')
	
	wordlist_file.close()

def import_corpus(name):
	module = __import__('nltk.corpus', fromlist=[name])
	return getattr(module, name)

def download_corpus(name):
	downloader = Downloader()
	try:
		status = downloader.status(name)
		if status != Downloader.INSTALLED:
			downloader.download(name)
	except ValueError:
		sys.exit('Requested corpus module does not exist')
	
parser = argparse.ArgumentParser('process_nltk_corpus', description='Convert an NLTK corpus to a word list')
parser.add_argument('corpus', help='NLTK corpus module')
parser.add_argument('outputfile', help='generated word list file', type=argparse.FileType('w', encoding='utf-8'))
parser.add_argument('-f', '--fileid', help='individual file identifier within the corpus', default=None)
args = parser.parse_args()

print('Processing started at  ' + time.ctime())
download_corpus(args.corpus)
corpus = import_corpus(args.corpus)
if args.fileid == None:
	wordlist = build_wordlist(corpus.words())
else:
	wordlist = build_wordlist(corpus.words(args.fileid))
write_wordlist(wordlist, args.outputfile)
print('Processing complete at ' + time.ctime())
