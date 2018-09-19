# Wikipedia Word List Builder

These scripts build word list files, suitable for import into the [RandomWordGenerator](https://github.com/staceybellerose/RandomWordGenerator) Android App source.

## Requirements

Python must be installed, along with the [gensim](https://github.com/RaRe-Technologies/gensim) library. The [pattern](https://github.com/clips/pattern) module, sometimes used with gensim, is not required. If desired, the [TinySegmenter](https://github.com/SamuraiT/tinysegmenter) tokenizer module can be installed to properly process Japanese text.

## Instructions

### Download

Download the appropriate database dump from [Wikimedia Downloads](https://dumps.wikimedia.org/backup-index-bydb.html).

1. Select the wiki of your choice.
1. Download the **Articles, templates, media/file descriptions, and primary meta-pages** file:

	*wikiname*-*YYYYMMDD*-pages-articles.xml.bz2
1. **NOTE**: This file is extremely large for most languages, so make sure you have enough drive space.
1. Do not decompress this file, as it can be processed as-is.

### Processing

Run the following scripts in order.

``python make_corpus_dict.py [wikipedia_dump_file] [dictionary_file]``

Depending on the size of this file and the power of your computer, it could take several hours to process.

```ShellSession
usage: make_corpus_dict [-h] [-t {default,japanese}]
                        inputfile outputfile [dictionary]

positional arguments:
  inputfile             wikipedia dump file to process
  outputfile            processed corpus text file
  dictionary            pre-built dictionary file

optional arguments:
  -h, --help            show this help message and exit
  -t {default,japanese}, --tokenizer {default,japanese}
                        alternative tokenizer to use
```

----

``python make_wiki_corpus.py [wikipedia_dump_file] [processed_text_file] [dictionary_file]``

Depending on the size of this file and the power of your computer, it could take several hours to process.

```ShellSession
usage: make_wiki_corpus [-h] [-t {default,japanese}]
                        inputfile outputfile [dictionary]

Create a text corpus from Wikipedia dump file.

positional arguments:
  inputfile             wikipedia dump file to process
  outputfile            processed corpus text file
  dictionary            pre-built dictionary file

optional arguments:
  -h, --help            show this help message and exit
  -t {default,japanese}, --tokenizer {default,japanese}
                        alternative tokenizer to use
```

----

``python check_wiki_corpus.py [processed_text_file]``

This script verifies that the initial process produced something readable. The entire file need not be checked.

```ShellSession
usage: check_wiki_corpus [-h] inputfile

positional arguments:
  inputfile   corpus text file

optional arguments:
  -h, --help  show this help message and exit
```

----

``python build_wiki_wordlist.py [processed_text_file] [word_list_file]``

This script converts the text corpus into a word list, sorted by most common word. The word list is a tab-delimited file with two columns: *word* and *count*.

```ShellSession
usage: build_wiki_wordlist [-h] inputfile outputfile

positional arguments:
  inputfile   corpus text file
  outputfile  generated word list file

optional arguments:
  -h, --help  show this help message and exit
```

----

``python filter_latin_characters.py [word_list_file] [filtered_word_list_file]``

This script is only to be run if the language uses a non-Latin alphabet, and will remove all lines containing the characters A through Z regardless of case.

```ShellSession
usage: filter_latin_characters [-h] inputfile outputfile

positional arguments:
  inputfile   word list file
  outputfile  filtered word list file

optional arguments:
  -h, --help  show this help message and exit
```

----

``gzip [filtered_word_list_file | word_list_file]``

This will compress the file to save space.

At this point, the intermediary files created (dictionary file, processed text file), as well as the original wiki dump file, can be deleted to save space.

### Upload the word list

Once the word list file is built, it can be imported into the RandomWordGenerator app.

1. Update the **wordlists/wiki/wordlists.json** file with the title and file name of the new word list.
1. Upload the gzipped word list file and the updated **wordlists.json** to the download server.
1. Users can now download the newest word list file.

## Sample Processing Runs

### English Wikipedia File

```ShellSession
$ python make_corpus_dict.py enwiki-20180820-pages-articles.xml.bz2 en_wiki.dict
Processing wiki dump at Sun Sep 16 20:40:48 2018
Using default tokenizer
Stream built at Mon Sep 17 05:25:06 2018
Processing complete at Mon Sep 17 05:25:10 2018

$ python make_wiki_corpus.py enwiki-20180820-pages-articles.xml.bz2 en_wiki.txt en_wiki.dict
Processing wiki dump at Mon Sep 17 07:19:08 2018
Dictionary found
Using default tokenizer
Stream built at Mon Sep 17 07:19:08 2018
Processed 10000 articles
Processed 20000 articles
:::
Processed 4500000 articles
Processed 4510000 articles
Total: 4517824 articles
Processing complete at Mon Sep 17 15:03:20 2018

$ python build_wiki_wordlist.py en_wiki.txt wordlist_en_20180820.txt
Building wordlist at Mon Sep 17 17:27:09 2018
Writing wordlist at Mon Sep 17 18:03:05 2018
Processing complete at Mon Sep 17 18:03:26 2018

$ gzip wordlist_en_20180820.txt
```

### Hindi Wikipedia File

```ShellSession
$ python make_corpus_dict.py hiwiki-20180901-pages-articles.xml.bz2 hi_wiki.dict
Processing wiki dump at Tue Sep 18 03:15:22 2018
Using default tokenizer
Stream built at Tue Sep 18 03:21:59 2018
Processing complete at Tue Sep 18 03:21:59 2018

$ python make_wiki_corpus.py hiwiki-20180901-pages-articles.xml.bz2 hi_wiki.txt hi_wiki.dict
Processing wiki dump at Tue Sep 18 03:23:04 2018
Dictionary found
Using default tokenizer
Stream built at Tue Sep 18 03:23:04 2018
Processed 10000 articles
Processed 20000 articles
Processed 30000 articles
Processed 40000 articles
Processed 50000 articles
Total: 58279 articles
Processing complete at Tue Sep 18 03:28:40 2018

$ python build_wiki_wordlist.py hi_wiki.txt wordlist_hi_20180901.txt
Building wordlist at Tue Sep 18 03:30:12 2018
Writing wordlist at Tue Sep 18 03:30:25 2018
Processing complete at Tue Sep 18 03:30:25 2018

$ python filter_latin_characters.py wordlist_hi_20180901.txt wordlist_hi_filtered_20180901.txt
Processing started at  Tue Sep 18 03:30:47 2018
Processing complete at Tue Sep 18 03:30:47 2018

$ gzip wordlist_hi_filtered_20180901.txt
```
