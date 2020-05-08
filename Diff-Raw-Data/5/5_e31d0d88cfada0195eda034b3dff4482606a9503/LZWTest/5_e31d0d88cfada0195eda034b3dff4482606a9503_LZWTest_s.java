 package com.lzwcompressor;
 
 import java.io.IOException;
 
 import com.lzwcompressor.huffman.Huffman;
 
 public class LZWTest {
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 		// String s = "tobeornottobeortobeornot";
		String s = "Loremipsumdolorsitamet,consecteturadipiscingelit.Fuscenecturpisquisnequepdldgjskqdndsglqshdfdkfnqsifhdkflsdgiuqsjdkosidghjqspofhdfsoflqsjfiudsfhqsfghdsfiolulvinarporttitoracatodio.";

 		LZW lzw = new LZW(12);
 		lzw.compression(s);
 		// lzw.decompression("test.txt");
 		// lzw.compressNoFile(s);
 		// lzw.decompressNoFile();
 
 		// Huffman encoding
 		 Huffman huff;
 		 huff = new Huffman("test.txt");
 
 	}
 
 }
