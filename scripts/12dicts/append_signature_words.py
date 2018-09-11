"""
"""

import argparse

parser = argparse.ArgumentParser("append_signature_words")
parser.add_argument("-i", "--includeInflections", default=False, action="store_true", help="Include inflected form(s) of words")
parser.add_argument("inputfile", help="signature word list file", type=argparse.FileType("r"))
parser.add_argument("outputfile", help="word list file to append data", type=argparse.FileType("a"))
args = parser.parse_args()

for line in args.inputfile:
	if line == '\n':
		# skip blank lines
		continue
	if line[0:4] == '    ' and args.includeInflections:
		# strip first four spaces
		line = line[4:]
		# split line by ', '
		words = line.split(', ')
	else:
		words = [line]
	for word in words:
		if ' ' in word:
			continue
		args.outputfile.write(word.rstrip() + '\n')
