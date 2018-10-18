"""
Build a word list from the raw EuroParl data for a given language.

Data downloaded from http://www.statmt.org/europarl/
"""

import time
import re
import argparse
import glob
import os
import hunspell

from collections import Counter
from tempfile import NamedTemporaryFile

def process_args():
	parser = argparse.ArgumentParser(prog='build_europarl_wordlist', description='Build a word list from raw EuroParl data.')
	parser.add_argument('folder', help='language data folder')
	parser.add_argument('dictionary', help='language code for dictionary (may require country code as well)')
	parser.add_argument('outputfile', help='generated word list file', type=argparse.FileType('w', encoding='utf-8'))
	return parser.parse_args()

def get_files(language):
	return glob.glob(language + '/*.txt')

def tokenize(line):
	# split on words, but include hyphenation as word-like
	words = re.findall(r'[-\w]+', line.lower())
	# make sure words have at least one letter, and no digits, and remove leading/trailing hyphens
	return [word.strip('-') for word in words if re.search(r'\w', word) and not re.search(r'\d', word)]

def build_words(filename):
	infile = open(filename, 'r', encoding='utf-8')
	words = []
	for line in infile:
		# line starts with XML tag, skip it
		if line[0] == '<':
			continue
		# line starts with language code in parens, remove it
		if re.match(r'\A\([A-Z]{2}\) ', line):
			line = line[5:]
		words += tokenize(line)
	infile.close()
	return words

def process_files(filenames, tempfile):
	i = 0
	for filename in filenames:
		words = build_words(filename)
		tempfile.write(' '.join(words) + '\n')
		i = i + 1
		if (i % 100 == 0):
			print('Processed ' + str(i) + ' files')
	print('Total: ' + str(i) + ' files')

def process_tempfile(tempfile):
	counter = Counter()
	tempfile.seek(0)
	for line in tempfile:
		counter.update(line.split())
	tempfile.close()
	return counter

def write_wordlist(wordlist, wordlist_file, dict_local, dict_en):
	for item in wordlist.most_common():
		word, count = item
		# skip any English words that are not found in target dictionary
		if dict_local != None:
			if dict_en.spell(word) and not dict_local.spell(word):
				continue
		wordlist_file.write(word + '\t' + str(count) + '\n')
	wordlist_file.close()

def main():
	args = process_args()
	filenames = get_files(args.folder)
	dict_en = hunspell.HunSpell('../dicts/en_US.dic', '../dicts/en_US.aff')
	if args.dictionary[0:2] == 'en':
		print('No dictionary checks')
		dict_local = None
	else:
		dict_local = hunspell.HunSpell('../dicts/' + args.dictionary + '.dic', '../dicts/' + args.dictionary + '.aff')
	tempfile = NamedTemporaryFile(mode='w+', encoding='utf-8', suffix=".tmp", prefix="tmp_")

	print('Processing started at  ' + time.ctime())
	process_files(filenames, tempfile)
	print('Building wordlist at   ' + time.ctime())
	counter = process_tempfile(tempfile)
	print('Writing wordlist at    ' + time.ctime())
	write_wordlist(counter, args.outputfile, dict_local, dict_en)
	print('Processing complete at ' + time.ctime())

if __name__ == '__main__':
	main()
