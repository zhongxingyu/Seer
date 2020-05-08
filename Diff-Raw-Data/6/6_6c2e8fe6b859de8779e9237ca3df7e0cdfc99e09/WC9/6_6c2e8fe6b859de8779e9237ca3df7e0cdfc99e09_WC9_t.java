 package com.taobao.joey.wordcnt;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.net.URISyntaxException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CountDownLatch;
 
 /**
  * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
  * <p/>
  * Project: java-utils
  * User: qiaoyi.dingqy
  * Date: 13-8-19
  * Time: 5:05
  */
 public class WC9 {
     //
     static byte[] punctuation
             = {'.', ',', ';', '-', '~', '?', '!', '\'', '\"', '\r', '\n', '\t', ' '};
     static HashSet<String> stopWords
             = new HashSet<String>(Arrays.asList("the", "and", "i", "to", "of", "a", "in", "was", "that", "had", "he", "you", "his", "my", "it", "as", "with", "her", "for", "on"));
     //
     static int top;
     static int candidateSize;
     // ļ
     static int PAGE_SIZE = 65535;
     static byte[] chunk;
     static int chunkSize = PAGE_SIZE;
     static File file;
     static long fileLength;
     // wordcnt runnable
     static int wcThreadNum = 4;
     static int splitLength;
     static CountDownLatch wcRunnablelatch = new CountDownLatch(wcThreadNum);
     static Map<JString, MutableInt> wc = new ConcurrentHashMap<JString, MutableInt>(50000, 0.9f);
     // rank runnable
     static int rankThreadNum = 4;
     static List<Map<JString, MutableInt>> splitWC = new ArrayList<Map<JString, MutableInt>>(rankThreadNum * 2);
     static List<List<List<JString>>> splitRank = new ArrayList<List<List<JString>>>(rankThreadNum);
     static CountDownLatch rankRunnablelatch = new CountDownLatch(rankThreadNum);
 
     static {
         try {
             init(16, 16, 10);
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
     }
 
     static void init(int wn, int rn, int t) throws URISyntaxException {
         //
         top = t;
         candidateSize = top + stopWords.size();
         //
         file = new File(WC9.class.getResource("/document.txt").toURI());
         fileLength = file.length();
         chunk = new byte[(int) fileLength];
         chunkSize = PAGE_SIZE;
         // word cnt runnable init
         wcThreadNum = wn;
         wcRunnablelatch = new CountDownLatch(wcThreadNum);
 
         // rank cnt runnable init
         rankThreadNum = rn;
         splitWC = new ArrayList<Map<JString, MutableInt>>(rankThreadNum * 2);
         for (int i = 0; i < rankThreadNum; i++) {
             splitWC.add(new ConcurrentHashMap<JString, MutableInt>(50000, 0.9f));
         }
 
         int avgWordLen = 5;
         int avgFreq = 50;
        //int radixSortSize = (int) (fileLength / avgWordLen) / avgFreq;
        int radixSortSize = 10;
         splitRank = new ArrayList<List<List<JString>>>(rankThreadNum);
         for (int i = 0; i < rankThreadNum; i++) {
             // ڻ
             List<List<JString>> rank = new ArrayList<List<JString>>(radixSortSize);
             for (int j = 0; j < radixSortSize; j++) {
                 rank.add(new ArrayList<JString>(candidateSize)); //at most top + stopword.size
             }
             splitRank.add(rank);
         }
         rankRunnablelatch = new CountDownLatch(rankThreadNum);
     }
 
     public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
         long startReadFile = System.currentTimeMillis();
         mmapRead();
         long endReadFile = System.currentTimeMillis();
 
         long startWordCnt = System.currentTimeMillis();
         splitLength = chunk.length / wcThreadNum;
         Thread[] threads1 = new Thread[wcThreadNum];
         for (int i = 0; i < wcThreadNum; i++) {
             threads1[i] = new Thread(new WordCntRunnable(i));
             threads1[i].start();
         }
         wcRunnablelatch.await();
         long endWordCnt = System.currentTimeMillis();
 
         long startTopTen = System.currentTimeMillis();
         Thread[] threads2 = new Thread[wcThreadNum];
         for (int i = 0; i < wcThreadNum; i++) {
             threads2[i] = new Thread(new RankRunnable(i));
             threads2[i].start();
         }
         rankRunnablelatch.await();
         long endTopTen = System.currentTimeMillis();
 
         System.out.println("read file consumes: " + (endReadFile - startReadFile) + "ms");
         System.out.println("word cnt consumes: " + (endWordCnt - startWordCnt) + "ms");
         System.out.println("rank consumes: " + (endTopTen - startTopTen) + "ms");
         System.out.println("total consumes: " + (endTopTen - startReadFile) + "ms");
     }
 
     static void mmapRead() throws IOException, URISyntaxException {
 
         MappedByteBuffer buffer = new RandomAccessFile(file, "rw").getChannel()
                 .map(FileChannel.MapMode.READ_WRITE, 0, file.length());
 
         for (int i = 0; i < chunk.length && buffer.hasRemaining(); ) {
             int readLength = chunkSize > buffer.remaining() ? buffer.remaining() : chunkSize;
             buffer.get(chunk, i, readLength);
             i += readLength;
         }
     }
 
     static void wordCnt(int startIndex, int endIndex, Map<JString, MutableInt> wordCnt) {
         long start = System.currentTimeMillis();
         int wordStartIndex = -1;
         int wordEndIndex = -1;
         for (int i = startIndex; i < endIndex; i++) {
             if (!isSplitter(chunk[i])) {
                 if (wordStartIndex == -1) {
                     wordStartIndex = i;
                     wordEndIndex = wordStartIndex;
                 } else {
                     wordEndIndex++;
                 }
             } else {
                 if (wordStartIndex != -1) {
                     // һαɣ1) to lower case; 2) compute hashcode
                     int hashcode = 0;
                     for (int j = wordStartIndex; j < wordEndIndex + 1; ++j) {
                         byte c = chunk[j];
                         if (c >= 'A' && c <= 'Z') {
                             chunk[j] = (byte) (c + 32);
                         }
                         hashcode = hashcode * 31 + chunk[j];
                     }
                     // copy,JStringΪkey
                    JString word = new JString(wordStartIndex, wordEndIndex, hashcode);
                     // mutableInt һμֻһmap
                     int index = hashcode % splitWC.size();
                     Map<JString, MutableInt> splitWordCnt = splitWC.get(index > 0 ? index : 0 - index);
                     MutableInt cnt = splitWordCnt.get(word);
                     if (cnt == null) {
                         splitWordCnt.put(word, new MutableInt());
                     } else {
                         cnt.inc();
                     }
                     wordStartIndex = -1;
                 }
             }
         }
         long end = System.currentTimeMillis();
         System.out.println("word cnt consumes : " + (end - start));
     }
 
     static boolean isSplitter(byte c) {
         for (byte p : punctuation) {
             if (p == c) return true;
         }
         return false;
     }
 
     static class MutableInt {
         int value = 1;
 
         void inc() {
             ++value;
         }
 
         int get() {
             return value;
         }
     }
 
     static class JString {
         final int start;
         final int end;
         final int hashcode;
         final int len;
 
         JString(int start, int end, int hashcode) {
             this.start = start;
             this.end = end;
             this.hashcode = hashcode;
             len = end - start + 1;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (!(o instanceof JString)) return false;
 
             JString jString = (JString) o;
             if (this.start == jString.start) return true;
 
             if (this.len != jString.len) return false;
             for (int i = 0; i < len; i++) {
                 if (chunk[this.start + i] != chunk[jString.start + i]) {
                     return false;
                 }
             }
 
             return true;
         }
 
         @Override
         public int hashCode() {
             return this.hashcode;
         }
 
         @Override
         public String toString() {
             return new String(chunk, start, len);
         }
     }
 
     static class WordCntRunnable implements Runnable {
         final int id;
 
         WordCntRunnable(int id) {
             this.id = id;
         }
 
         public void run() {
             int startIndex = id * splitLength;
             if (startIndex != 0) {
                 while (!isSplitter(chunk[startIndex])) {
                     startIndex++;
                 }
             }
 
             int endIndex = (id + 1) * splitLength - 1;
             while (!isSplitter(chunk[endIndex])) {
                 endIndex++;
             }
 
             wordCnt(startIndex, endIndex, wc);
             wcRunnablelatch.countDown();
         }
     }
 
     static class RankRunnable implements Runnable {
         final int id;
         final Map<JString, MutableInt> split;
         final List<List<JString>> rank;
 
         RankRunnable(int id) {
             this.id = id;
             split = splitWC.get(id);
             rank = splitRank.get(id);
         }
 
         public void run() {
             for (Map.Entry<JString, MutableInt> entry : split.entrySet()) {
                 int cnt = entry.getValue().value;
                 List<JString> pos = rank.get(cnt);
                 if (pos.size() > candidateSize) continue;
                 pos.add(entry.getKey());
             }
 
             int cnt = 0;
             for (int i = rank.size() - 1; i >= 0; i--) {
                 for (JString word : rank.get(i)) {
                     if (cnt >= 10) break;
                     System.out.println(word + ":" + i);
                     cnt++;
                 }
 
             }
             rankRunnablelatch.countDown();
         }
     }
 }
