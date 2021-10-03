"""
Process the Hungarian Webcorpus frequency dictionary,
hosted at http://mokk.bme.hu/en/resources/webcorpus/

Download link:
ftp://ftp.mokk.bme.hu/Language/Hungarian/Freq/Web2.2/web2.2-freq-sorted.top100k.txt

File layout:

word <tab> complete frequency <tab> 40% frequency <tab> 8% frequency <tab> 4% frequency

Use the counts from the 4% strata, for best spelling norms.
"""

import time
import re
import argparse
import hunspell
from collections import Counter

class NonWordFound(Exception):
	pass

def process_args():
	parser = argparse.ArgumentParser(prog='process_magyar_webcorpus', description='Process the Hungarian Webcorpus frequency list.')
	parser.add_argument('inputfile', help='downloaded frequency list file', type=argparse.FileType('r', encoding='utf-8'))
	parser.add_argument('outputfile', help='word list file', type=argparse.FileType('w', encoding='utf-8'))
	return parser.parse_args()

def validate_hyphenated_word(word):
	words = word.split("-")
	for w in words:
		if not w.isalpha():
			print('FOUND NON-WORD' + w)
			raise NonWordFound()

def count_words(inputfile):
	counter = Counter()
	for line in inputfile:
		if not line:
			continue
		if line.count('\t') != 4:
			continue
		word, x, y, z, count = line.split('\t')
		if word == '':
			continue
		if word[-1] == '*':
			word = word.split('*')[0]
		try:
			validate_hyphenated_word(word)
		except NonWordFound:
			continue
		counter[word.lower()] += int(count)
	return counter

def write_wordlist(counter, outputfile, dict_en, dict_hu):
	for item in counter.most_common():
    word, count = item
    if count == 0:
        continue
    # if word is in English dictionary and not in Hungarian dictionary, skip it
    if dict_en.spell(word) and not dict_hu.spell(word):
        print('SKIPPING ' + word)
        continue
    outputfile.write(word + '\t' + str(count) + '\n')

def main():
	print('Processing started at  ' + time.ctime())
	args = process_args()
	dict_en = hunspell.HunSpell('en_US.dic', 'en_US.aff')
	dict_hu = hunspell.HunSpell('hu_HU.dic', 'hu_HU.aff')
	counter = count_words(args.inputfile)
	args.inputfile.close()
	write_wordlist(counter, outputfile, dict_en, dict_hu)
	args.outputfile.close()
	print('Processing complete at ' + time.ctime())

if __name__ == '__main__':
	main()
