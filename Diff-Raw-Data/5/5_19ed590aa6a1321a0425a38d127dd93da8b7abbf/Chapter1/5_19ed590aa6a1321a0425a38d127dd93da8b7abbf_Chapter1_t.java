 package com.elf.pearls.chapter1;
 
 import org.junit.Test;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.Random;
 
 /**
  * User: laichendong
  * Date: 12-6-4
  * Time: 8:04
  */
 public class Chapter1 {
 	private static final String FILE_NAME = "integerFile.dat";
 	private static final int FILE_SIZE = 1000000;
 	private static final int MAX_NUMBER = 10000000;
 	Random r = new Random();
 
 	@Test
 	public void mkFile() {
 		long t = System.currentTimeMillis();
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(FILE_NAME)));
 			int[] seed = new int[MAX_NUMBER];
 			int i = 0;
 			do {
				seed[i] = i++;
 			} while (i < MAX_NUMBER);
 			for (int j = 0; j < FILE_SIZE; j++) {
				swap(seed, j, random(j, FILE_SIZE));
 				writer.write(String.valueOf(seed[j]));
 				writer.newLine();
 			}
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.printf("mkFile, time spend : %1d ms", System.currentTimeMillis() - t);
 	}
 
 	private void swap(int[] seed, int i, int j) {
 		int t = seed[i];
 		seed[i] = seed[j];
 		seed[j] = t;
 	}
 
 	/**
 	 * left right֮һ
 	 *
 	 * @param left  l
 	 * @param right r
 	 * @return [left, right)
 	 */
 	private int random(int left, int right) {
 		return left + r.nextInt(right - left);
 	}
 
 	@Test
 	public void libSort() {
 		long t = System.currentTimeMillis();
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)));
 			int[] ints = new int[FILE_SIZE];
 			int i = 0;
 			String line;
 			do {
 				line = reader.readLine();
 				if (line != null) {
 					ints[i++] = Integer.parseInt(line);
 				}
 			} while (line != null);
 			Arrays.sort(ints);
 //			System.out.println(Arrays.toString(ints));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.printf("java lib qsort, time spend : %1d ms", System.currentTimeMillis() - t);
 	}
 
 	@Test
 	public void bitSort() {
 		long t = System.currentTimeMillis();
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)));
 			BitSet bitSet = new BitSet(FILE_SIZE);
 			String line;
 			do {
 				line = reader.readLine();
 				if (line != null) {
 					bitSet.set(Integer.parseInt(line));
 				}
 			} while (line != null);
 
 //			for (int i=0; i<FILE_SIZE; i++){
 //				if(bitSet.get(i)){
 //					System.out.println(i);
 //				}
 //			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.printf("bit sort, time spend : %1d ms", System.currentTimeMillis() - t);
 	}
 
 	@Test
 	public void bitSortWithSimpleBitSet() {
 		long t = System.currentTimeMillis();
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)));
 			SimpleBitSet simpleBitSet = new SimpleBitSet(FILE_SIZE);
 			String line;
 			do {
 				line = reader.readLine();
 				if (line != null) {
 					simpleBitSet.set(Integer.parseInt(line));
 				}
 			} while (line != null);
 
 //			for (int i=0; i<FILE_SIZE; i++){
 //				if(simpleBitSet.test(i)){
 //					System.out.println(i);
 //				}
 //			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.printf("bit sort with simple bit set, time spend : %1d ms", System.currentTimeMillis() - t);
 	}
 
 	@Test
 	public void testSimpleBitSet() {
 		SimpleBitSet simpleBitSet = new SimpleBitSet();
 		System.out.println(simpleBitSet.toString());
 		simpleBitSet.set(7);
 		simpleBitSet.set(3);
 		simpleBitSet.clr(7);
 		System.out.println(simpleBitSet.toString());
 		System.out.println(simpleBitSet.test(7));
 		System.out.println(simpleBitSet.test(3));
 	}
 
 
 }
