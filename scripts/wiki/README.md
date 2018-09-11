# Wikipedia Word List Builder

These scripts build a word list file, suitable for import into the [RandomWordGenerator](https://github.com/staceybellerose/RandomWordGenerator) Android App source.

## Requirements

Python must be installed, along with the [gensim](https://github.com/RaRe-Technologies/gensim) library. The [pattern](https://github.com/clips/pattern) module, sometimes used with *gensim*, is not required.

## Instructions

1. Download the appropriate database dump from [Wikimedia Downloads](https://dumps.wikimedia.org/backup-index-bydb.html).

	1. Select the wiki of your choice.
	1. Download the **Articles, templates, media/file descriptions, and primary meta-pages** file: *wikiname*-*YYYYMMDD*-pages-articles.xml.bz2
	1. **NOTE**: This file is extremely large for most languages, so make sure you have enough drive space.

1. Run the scripts in the following order:

	+ python **make\_wiki\_corpus.py** [wikipedia\_dump\_file] [processed\_text\_file]
		+ Depending on the size of this file and the power of your computer, it could take several hours to process
	+ python **check\_wiki\_corpus.py** [processed\_text\_file]
		+ This script verifies that the initial process produced something readable. The entire file need not be checked.
	+ python **build\_wiki\_wordlist.py** [processed\_text\_file] [word\_list\_file]
		+ This script converts the text corpus into a word list, sorted by most common word. The word list is a tab-delimited file with two columns: *word* and *count*.
	+ python **filter\_latin\_characters.py** [word\_list\_file] [filtered\_word\_list\_file]
		+ This script is only to be run if the language uses a non-Latin alphabet, and remove all lines containing the characters A through Z regardless of case.
	+ gzip [filtered\_word\_list\_file | word\_list\_file]
		+ This will compress the file to save space.

1. Once the word list file is built, it can be imported into the RandomWordGenerator app.
	+ ***Instructions for import not available yet***