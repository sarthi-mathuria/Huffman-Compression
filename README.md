# Huffman-Compression
Lossless data compression program that uses the standard Huffman algorithm for encoding and decoding.


Author: Sarthi Mathuria

Test Files

Books and HTML - Average percent compression: 40%

Waterloo - Average percent compression: 18%

Calgary - Average percent compression: 43%

What kinds of files lead to lots of compressions?

Text files lead to lots of compression since the ASCII components range from a smaller subset of 256.
Since there are 26 characters for lowercase, 26 for uppercase, and 10 for special characters, there are only approximately 60 total values used which means that the Huffman Tree will always have more compression while using less space.


What kind of files had little or no compression?

Picture files seemed to have little to no compression. This may be because each pixel in the file is an RBGA pixel, each containing 8 bits of data for red, green, blue, and alpha, all ranging from 0 - 255. As such, all values between 0 and 255 will likely be represented at least once. This results in the Huffman Tree being near complete, causing large codes for most values. Some of the common representations can still occur close to the top of the tree, but since the Huffman Tree is binary, it is more likely that some of the most common 8 bit ints would not space that much space in the codes. Also, there will be more internal leaves in order to have all 256 leaves, which means that several codes can be even longer than 8 bits. As such, I found that the Huffman style of encoding does not work well with images.


What happens when you try and compress a Huffman code file?

Compressing a Huffman code file led to a ratio of approximately 1.0 between the original and prior file. This is likely because the compressed file is technically randomly generated, since the codes are 8-bit sequences that are pseudorandom. As such, any number value between 0 and 255 holds an equal chance of showing up, which means that compression based on frequencies of values will not work.

