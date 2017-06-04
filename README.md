# VkMusicDownloader2
Graphic VK audio downloader based on parsing site mobile version

December 16-th, 2016 the Public API for audio was disabled, so downloading music became more complicated: you may have to parse the website's DOM to fetch audios and information on them. That is, basically, what is done in this project.

## Running project

To run the UI install Maven and run the command below:

    $ maven clean package exec:java

## Security considerations

Unfortunately, there's no way I can prove the application is not using (stealing) your authentication data, so the only way to check it is to read the source code
