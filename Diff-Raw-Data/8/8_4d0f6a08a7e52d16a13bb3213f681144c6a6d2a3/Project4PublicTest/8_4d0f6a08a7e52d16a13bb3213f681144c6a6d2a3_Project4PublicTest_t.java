 /**
  * 
  */
 package test_public;
 
 import java.awt.image.BufferedImage;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Iterator;
 
 import junit.framework.Assert;
 
 import io.ImageReader;
 import logic.huffman.HuffmanCode;
 import logic.huffman.HuffmanTree;
 import logic.trie.TrieCode;
 
 import org.junit.Test;
 
 import datamodel.RGB;
 import datamodel.huffman.tree.Tree;
 
 /**
  * @author Jakob Karolus, Kevin Munk
  * @version 1.0
  * 
  */
 public class Project4PublicTest {
 
 	private TrieCode trie = new TrieCode();
 
 	private static int counter = 0;
 
 	/**
 	 * Tests the encrypting and decrypting via a huffman code
 	 * 
 	 * 2 Points
 	 * 
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	@Test
 	public void HuffmanTest() throws FileNotFoundException, IOException {
 		try {
 			// encrypt with Huffman, test binary String
			Tree tree = HuffmanTree.getHuffmanTree("test2.png");
 			HuffmanCode code = new HuffmanCode(tree);
 
			ImageReader reader = new ImageReader("test2.png");
 			String encryptedImage = code.encryptImage(reader);
 
 			Assert.assertEquals(
 					"0101001101001000100010000111000000111011101000100000101100001100010100000110001101110010101001110101001001100011001111110101101000110000000000110111",
 					encryptedImage);
 			// decrypt with Huffman, test again
 			BufferedImage image = code.decryptImage(encryptedImage, 8, 8);
 
 			Iterator<RGB> iter = reader.iterator();
 
 			// test decrypted image
 			for (int i = 0; i < 8; i++) {
 				for (int j = 0; j < 8; j++) {
 					RGB rgb = iter.next();
 					Assert.assertEquals(rgb.getRGBValue(), image.getRGB(j, i));
 				}
 			}
 			counter += 2;
 		} catch (AssertionError e) {
 			throw e;
 		}
 	}
 
 	/**
 	 * tests the encrypting and decrypting via a Trie
 	 * 
 	 * 2 Points
 	 * 
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	@Test
 	public void TrieTest() throws FileNotFoundException, IOException {
 
 		try {
 			// encrypt with Trie
			ImageReader reader = new ImageReader("test2.png");
 			trie.buildTrie(reader);
 			String temp = trie.encryptImage(reader);
 
 			// test encrypted image
 			Assert.assertEquals(
 					"11001100110011001100110011001100101010101010101010101010101010101001100110011001100110011001100110101010101010101010101010101010100010001000100010001000100010001000100010001000100010001000100010001000100010001000100010001000110111011101110111011101110111011010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101101110111011101110111011101110111011101110111011101110111011101101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101110111011101110111011101110111010101010101010101010101010101011001100110011001100110011001100100010001000100010001000100010001101110111011101110111011101110111001100110011001100110011001100101010101010101010101010101010101010101010101010101010101010101011011101110111011101110111011101101110111011101110111011101110111101110111011101110111011101110110101010101010101010101010101010110011001100110011001100110011001101110111011101110111011101110110111011101110111011101110111011110111011101110111011101110111011010101010101010101010101010101010101010101010101010101010101010101110111011101110111011101110111010101010101010101010101010101010001000100010001000100010001000101010101010101010101010101010101100110011001100110011001100110010111011101110111011101110111011101010101010101010101010101010101100110011001100110011001100110010101010101010101010101010101010100010001000100010001000100010001011101110111011101110111011101111011101110111011101110111011101101010101010101010101010101010101010101010101010101010101010101011101110111011101110111011101110101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010110011001100110011001100110011001010101010101010101010101010101010101010101010101010101010101010110011001100110011001100110011001001100110011001100110011001100111011101110111011101110111011101110111011101110111011101110111011101110111011101110111011101110110101010101010101010101010101010101010101010101010101010101010101011101110111011101110111011101110101010101010101010101010101010",
 					temp);
 			BufferedImage image = trie.decryptImage(temp, 8, 8);
 
 			Iterator<RGB> iter = reader.iterator();
 
 			// test decrypted image
 			for (int i = 0; i < 8; i++) {
 				for (int j = 0; j < 8; j++) {
 					RGB rgb = iter.next();
 					Assert.assertEquals(rgb.getRGBValue(), image.getRGB(j, i));
 				}
 			}
 			counter += 2;
 		} catch (AssertionError e) {
 			throw e;
 		}
 	}
 
 	/**
 	 * Tests the containsColor() of trie and huffman
 	 * 
 	 * 1 Point
 	 * 
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	@Test
 	public void searchTest() throws FileNotFoundException, IOException {
 		try {
 			// trie
 			trie.addColor(new RGB(234, 34, 0, 123));
 			trie.addColor(new RGB(212, 45, 22, 98));
 
 			Assert.assertTrue(trie.containsColor(new RGB(234, 34, 0, 123)));
 			Assert.assertTrue(trie.containsColor(new RGB(212, 45, 22, 98)));
 			Assert.assertFalse(trie.containsColor(new RGB(0, 3, 24, 145)));
 			Assert.assertFalse(trie.containsColor(new RGB(234, 12, 67, 0)));
 
 			// huffman
 			Tree tree = HuffmanTree.getHuffmanTree("test.png");
 			HuffmanCode code = new HuffmanCode(tree);
 
 			Assert.assertTrue(code.containsColor(new RGB(255, 255, 0, 255)));
 			Assert.assertTrue(code.containsColor(new RGB(255, 0, 255, 255)));
 			Assert.assertFalse(code.containsColor(new RGB(0, 3, 24, 145)));
 			Assert.assertFalse(code.containsColor(new RGB(234, 12, 67, 0)));
 
 			counter++;
 		} catch (AssertionError e) {
 			throw e;
 		}
 	}
 
 	@Test
 	public void getResult() {
 		System.out.println("Points in public tests: " + counter);
 	}
 
 }
