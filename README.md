# ArtNetBridge
Artnet bridge that pushes RGB Art-Net to the Pixelpusher in RGBW format. The RGB pixels are first converted into RGBW by calculating the saturation value before reshuffling the byte order into RGBW. This is tested with SK6812-RGBW chips. The Pixelpusher should be set to the WS2812 chipset.

Every strip length has a row in arguments.txt. It is assumed that every strip corresponds to a particular Art-Net universe/subnet combo, where the data starts at position zero. A single universe/subnet cannot be distributed across multiple strips, but multiple universa/subnets can be merged into a single Pixelpusher output.

 * First column is the universe of the incoming artnet signals
 * Second column is artnet input subnet
 * Third column is the colour order (TODO - unused)
 * Fourth is amount of pixels in the universe/subnet combo
 * Fifth is Pixelpusher pin header (0-7)
 * Sixth is s (straight) or r (reverse) flag
 * Seventh is the beginning pixel index of the strip
 * Eighth is the ending pixel index of the strip
