"""
Creates a corpus from Wikipedia dump file.

Inspired by:
https://github.com/panyang/Wikipedia_Word2vec/blob/master/v1/process_wiki.py

Originally from:
https://www.kdnuggets.com/2017/11/building-wikipedia-text-corpus-nlp.html
"""

import time
import warnings
import argparse

warnings.filterwarnings('ignore', r'detect', UserWarning, r'gensim')

from gensim.corpora import WikiCorpus

def make_corpus(in_f, out_f):

	"""Convert Wikipedia xml dump file to text corpus"""

	# don't lemmatize the corpus, even if pattern is installed,
	# as we don't need that data and it will slow processing
	# down dramatically.
	wiki = WikiCorpus(in_f, lemmatize=False)
	print('Stream built at ' + time.ctime())
    
	wiki.metadata = False
	i = 0
	for text in wiki.get_texts():
		out_f.write(' '.join(text) + '\n')
		i = i + 1
		if (i % 10000 == 0):
			print('Processed ' + str(i) + ' articles')
	print('Total: ' + str(i) + ' articles')
	out_f.close()

if __name__ == '__main__':
	parser = argparse.ArgumentParser('make_wiki_corpus')
	parser.add_argument('inputfile', help='wikipedia dump file to process', type=argparse.FileType('rb'))
	parser.add_argument('outputfile', help='processed corpus text file', type=argparse.FileType('w', encoding='utf-8'))
	args = parser.parse_args()

	print('Processing wiki dump at ' +  time.ctime())
	make_corpus(args.inputfile, args.outputfile)
	print('Processing complete at ' + time.ctime())
