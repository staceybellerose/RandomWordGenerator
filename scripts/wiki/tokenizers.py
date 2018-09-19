"""
Tokenizer wrapper to allow gensim to use TinySegmenter
as well as its default tokenizer.
"""

import warnings

warnings.filterwarnings('ignore', r'detect', UserWarning, r'gensim')

import gensim

DEFAULT_TOKEN_MIN_LEN = 2
DEFAULT_TOKEN_MAX_LEN = 15
DEFAULT_TOKENIZER_NAME = 'default'
DEFAULT_LOWER = True

JA_TOKEN_MIN_LEN = 1
JA_TOKEN_MAX_LEN = 15
JA_TOKENIZER_NAME = 'japanese'
JA_LOWER = False

def get_available_tokenizers():
	tokenizers = []
	tokenizers.append(DEFAULT_TOKENIZER)
	try:
		import tinysegmenter
	except ImportError:
		pass
	if tinysegmenter:
		ja_tokenizer = Tokenizer(JA_TOKENIZER_NAME, JA_TOKEN_MIN_LEN, JA_TOKEN_MAX_LEN, JA_LOWER, tokenizer_tinysegmenter)
		tokenizers.append(ja_tokenizer)
	return tokenizers

def find_tokenizer_by_name(tokenizers, name):
	return next((t for t in tokenizers if t.name == name), DEFAULT_TOKENIZER)

def tokenizer_tinysegmenter(text: str, token_min_len: int, token_max_len: int, lower: bool):
	import tinysegmenter
	# Japanese doesn't have lower case, so we can ignore 'lower'
	tokenized_text = tinysegmenter.tokenize(text)
	return [word for word in tokenized_text if len(word) >= token_min_len and len(word) <= token_max_len]

def tokenizer_default(text: str, token_min_len: int, token_max_len: int, lower: bool):
	return gensim.corpora.wikicorpus.tokenize(text, token_min_len, token_max_len, lower)

class Tokenizer:
	def __init__(self, name: str, token_min_len: int, token_max_len: int, lower: bool, tokenizer):
		self.name = name
		self.token_min_len = token_min_len
		self.token_max_len = token_max_len
		self.lower = lower
		self.tokenizer = tokenizer
	def tokenize(self, text: str, token_min_len: int, token_max_len: int, lower: bool):
		return self.tokenizer(text, token_min_len, token_max_len, lower)

DEFAULT_TOKENIZER = Tokenizer(DEFAULT_TOKENIZER_NAME, DEFAULT_TOKEN_MIN_LEN, DEFAULT_TOKEN_MAX_LEN, DEFAULT_LOWER, tokenizer_default)
