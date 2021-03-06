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
import tokenizers

warnings.filterwarnings('ignore', r'detect', UserWarning, r'gensim')

from gensim.corpora import WikiCorpus

def make_corpus(in_f, out_f, dict_f, tokenizer):
	"""Convert Wikipedia xml dump file to text corpus"""
	if dict_f is not None:
		print('Dictionary found')
	print('Using %s tokenizer' % (tokenizer.name))
	# don't lemmatize the corpus, even if pattern is installed,
	# as we don't need that data and it will slow processing
	# down dramatically.
	wiki = WikiCorpus(in_f, lemmatize=False, dictionary=dict_f, tokenizer_func=tokenizer.tokenize, token_min_len=tokenizer.token_min_len, token_max_len=tokenizer.token_max_len, lower=tokenizer.lower)
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
	t_list = tokenizers.get_available_tokenizers()
	parser = argparse.ArgumentParser(prog='make_wiki_corpus', description='Create a text corpus from Wikipedia dump file.')
	parser.add_argument('inputfile', help='wikipedia dump file to process', type=argparse.FileType('rb'))
	parser.add_argument('outputfile', help='processed corpus text file', type=argparse.FileType('w', encoding='utf-8'))
	parser.add_argument('dictionary', help='pre-built dictionary file', type=argparse.FileType('rb'), nargs='?', default=None)
	parser.add_argument('-t', '--tokenizer', help='alternative tokenizer to use', choices=[t.name for t in t_list], default=tokenizers.DEFAULT_TOKENIZER.name)
	args = parser.parse_args()

	tokenizer = tokenizers.find_tokenizer_by_name(t_list, args.tokenizer)
	print('Processing wiki dump at ' +  time.ctime())
	make_corpus(args.inputfile, args.outputfile, args.dictionary, tokenizer)
	print('Processing complete at ' + time.ctime())
