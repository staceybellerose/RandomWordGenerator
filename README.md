Random Word Generator
=====================

The **Random Word Generator** Android app generates
randomly selected words from a large English (American)
dictionary of over 28,000 entries.

The inspirations for this app were
[Diceware](http://world.std.com/~reinhold/diceware.html)
and the xkcd comic [Password Strength](https://m.xkcd.com/936/).

By using a large English (American) dictionary with over
28,000 entries, a randomly selected word will have nearly
14.8 bits of entropy. A phrase made up of 6 words will have
over 88.6 bits of entropy, more than enough for a very
strong password.

This app uses the 6of12 word list from
[12dicts](wordlist.aspell.net/12dicts/), along with
the neol2016 list. The random number generator uses the
cryptographically secure java.security.SecureRandom class.

License
=======

    Copyright 2016 Stacey Adams

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.