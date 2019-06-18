# ArtNetBridge
Artnet bridge that pushes RGB Art-Net to the Pixelpusher in RGBW format

Every strip length has a row in arguments.txt. It is assumed that every strip corresponds to a particular Art-Net universe/subnet combo, where the data starts at position zero. A single universe/subnet cannot be distributed across multiple strips, but multiple universa/subnets can be merged into a single Pixelpusher output.

First column is the universe of the incoming artnet signals
Second column is artnet input subnet
Third column is the colour order (TODO - unused)
Fourth is amount of pixels in the universe/subnet combo
Fifth is Pixelpusher pin header (0-7)
Sixth is s (straight) or r (reverse) flag
Seventh is the beginning pixel index of the strip
Eighth is the ending pixel index of the strip
