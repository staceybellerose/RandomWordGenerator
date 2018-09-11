# 12dicts Word List Builder

The following commands have been run to generate the 12dicts word lists:

```Shell Session
$ python process12dicts.py source/American/6of12.txt wordlist.txt
$ python process12dicts.py source/American/2of12inf.txt words_inf.txt
$ python process12dicts.py source/American/3esl.txt words_esl.txt
$ python process12dicts.py -v source/International/3of6game.txt words_intl.txt

$ python append_signature_words.py signature.txt wordlist.txt
$ python append_signature_words.py source/Special/neol2016.txt wordlist.txt
$ python append_signature_words.py -i signature.txt words_inf.txt
$ python append_signature_words.py -i signature.txt words_intl.txt
```

After running the above commands, the *wordlist.txt* file was manually updated to remove the duplicate words introduced by appending the *neol2016.txt* file. These are the non-inflected words from section 2 of said file.

The *neol2016.txt* contents were already added to the *2of12inf.txt* and *3of6game.txt* source files, and did not need to be manually added to the processed files.

As the words contained in the *neol2016.txt* file are not "core English*, they were not added to the *words_esl.txt* created from *3esl.txt*.

The processed files were then copied into the app/src/main/res/raw/ folder.