# Infinidisc
A client-side mod that allows users to play any audio file they desire. Currently only supports .ogg files, but audio is downmixed to mono on the fly to guarantee basic compatibility.

To start, simply rename any music disc to "Infinidisc". Using this disc on a jukebox will open a file-select window where you can then select an audio file to play.

Loaded audio files will be hashed and this hash can be used in combination with "Infinidisc" when naming a disc to embed a song into a disc. The exact format for this is to name a disc like "Infinidisc[123456789]" with the square brackets and replacing the numbers with the hash. Using the disc will then play the song immediately without showing the file select window.

Currently the only way to find the hash is to look at the "Now Playing" popup that appears when the song starts, but this will be replaced with a song library to make this process easier. Also, the hashes aren't currently saved when the game closes, so you will need to use a regular "Infinidisc" to select the file; after which the embedded disc will work properly. This will be fixed eventually.

### Future plans:
* Wider range of file support, with .mp3 being the biggest desire.
* Built-in audio library allowing you to embed songs into discs quickly and easily. (Mostly possible framework-wise, just need to serialize the data and make a GUI.)
* Read metadata from the files to pre-load discs with author and title info.
* Use datagens, they exist for a reason!