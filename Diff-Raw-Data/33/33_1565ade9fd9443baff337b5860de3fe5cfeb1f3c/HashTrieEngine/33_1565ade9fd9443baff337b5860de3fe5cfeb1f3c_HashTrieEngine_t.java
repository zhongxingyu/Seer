 package net.yeputons.cscenter.dbfall2013.engines.hashtrie;
 
 import net.yeputons.cscenter.dbfall2013.engines.FileStorableDbEngine;
 import net.yeputons.cscenter.dbfall2013.engines.SimpleEngine;
 import net.yeputons.cscenter.dbfall2013.util.HugeMappedFile;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: e.suvorov
  * Date: 05.10.13
  * Time: 20:59
  * To change this template use File | Settings | File Templates.
  */
 public class HashTrieEngine extends SimpleEngine implements FileStorableDbEngine {
     protected File storage;
     protected RandomAccessFile dataFile;
     protected HugeMappedFile data;
     protected long dataUsedLength;
 
     protected final static MessageDigest md;
     protected final int MD_LEN = 160 / 8;
     protected int currentSize;
 
     protected final long SIGNATURE_OFFSET = 0;
     protected final byte[] SIGNATURE = { 'Y', 'D', 'B', 2 };
     protected final long USED_LENGTH_OFFSET = 4;
     protected final long ROOT_NODE_OFFSET = 12;
 
     static {
         try {
             md = MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected int calcSize(long offset) throws IOException {
         TrieNode node = TrieNode.createFromFile(data, offset);
         if (node instanceof LeafNode) {
             LeafNode leaf = (LeafNode)node;
             return leaf.value == null ? 0 : 1;
         }
         InnerNode inner = (InnerNode)node;
         int res = 0;
         for (int i = 0; i < inner.next.length; i++) {
             long off = inner.next[i];
             if (off != 0)
                 res += calcSize(off);
         }
         return res;
     }
 
    private void resetStorage() throws IOException {
        dataUsedLength = ROOT_NODE_OFFSET;
        dataFile.seek(0);
        dataFile.write(SIGNATURE);
        dataFile.writeLong(dataUsedLength);
        dataFile.getFD().sync();

        data = new HugeMappedFile(dataFile.getChannel());

        long offset = appendToStorage(InnerNode.estimateSize());
        InnerNode.writeToBuffer(data, offset);
    }

     protected void openStorage() throws IOException {
         dataFile = new RandomAccessFile(storage, "rw");
         if (dataFile.length() == 0) {
            resetStorage();
         } else {
             data = new HugeMappedFile(dataFile.getChannel());
             byte[] readSig = new byte[SIGNATURE.length];
             data.get(readSig);
             if (!Arrays.equals(SIGNATURE, readSig))
                 throw new IOException("Invalid DB signature");
             dataUsedLength = data.getLong();
         }
         if (dataUsedLength > dataFile.length())
             throw new IOException("Invalid DB: used length is greater than file length");
         currentSize = calcSize(ROOT_NODE_OFFSET);
     }
 
     public HashTrieEngine(File storage) throws IOException {
         this.storage = storage;
         openStorage();
     }
     @Override
     public void flush() throws IOException {
         data.force();
     }
     @Override
     public void close() throws IOException {
         flush();
         data = null;
         dataFile.close();
         dataFile = null;
     }
 
     @Override
     public void clear() {
         try {
            flush();
            resetStorage();
            currentSize = calcSize(ROOT_NODE_OFFSET);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void keySet(long ptr, Set<ByteBuffer> keySet) throws IOException {
         TrieNode node = TrieNode.createFromFile(data, ptr);
         if (node instanceof LeafNode) {
             LeafNode leaf = (LeafNode)node;
             if (leaf.value == null) return;
             keySet.add(leaf.key);
             return;
         }
 
         InnerNode inner = (InnerNode)node;
         for (int i = 0; i < inner.next.length; i++)
             if (inner.next[i] != 0)
                 keySet(inner.next[i], keySet);
     }
 
     @Override
     public Set<ByteBuffer> keySet() {
         Set<ByteBuffer> keySet = new HashSet<ByteBuffer>();
         try {
             keySet(ROOT_NODE_OFFSET, keySet);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return keySet;
     }
 
     @Override
     public int size() {
         return currentSize;
     }
 
     protected LeafNode getNode(ByteBuffer key) {
         byte[] hash = md.digest(key.array());
         assert hash.length == MD_LEN;
 
         try {
             TrieNode node = TrieNode.createFromFile(data, ROOT_NODE_OFFSET);
 
             int hashPtr = 0;
             while (!(node instanceof LeafNode)) {
                 long ptr = ((InnerNode)node).next[hash[hashPtr] & 0xFF];
                 if (ptr == 0) throw new NoSuchElementException();
                 node = TrieNode.createFromFile(data, ptr);
                 hashPtr++;
             }
 
             LeafNode leaf = (LeafNode)node;
             if (leaf.value == null)
                 throw new NoSuchElementException();
 
             if (!leaf.key.equals(key)) {
                 byte[] hash2 = md.digest(leaf.key.array());
                 for (int i = 0; i < hashPtr; i++)
                     assert(hash[i] == hash2[i]);
                 if (hashPtr < hash.length) throw new NoSuchElementException();
                 assert hash.equals(hash2);
                 throw new RuntimeException("SHA-1 collision!");
             }
             return leaf;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public ByteBuffer get(Object _key) {
         if (size() == 0) return null;
         ByteBuffer key = (ByteBuffer)_key;
 
         try {
             LeafNode leaf = getNode(key);
             return leaf.value;
         } catch (NoSuchElementException e) {
             return null;
         }
     }
 
     @Override
     public ByteBuffer put(ByteBuffer key, ByteBuffer value) {
         if (key == null || value == null)
             throw new NullPointerException("key and value should not be nulls");
 
         byte[] hash = md.digest(key.array());
         assert hash.length == MD_LEN;
 
         try {
             TrieNode node = TrieNode.createFromFile(data, ROOT_NODE_OFFSET);
             InnerNode parent = null;
             int parentC = -1;
             int hashPtr = 0;
             while (!(node instanceof LeafNode)) {
                 InnerNode inner = (InnerNode)node;
                 int c = hash[hashPtr] & 0xFF;
 
                 parent = inner;
                 parentC = c;
                 if (inner.next[c] != 0) {
                     node = TrieNode.createFromFile(data, inner.next[c]);
                 } else {
                     long offset = appendToStorage(LeafNode.estimateSize(key, value));
                     LeafNode leaf = LeafNode.writeToBuffer(data, offset, key, value);
                     inner.setNext(c, leaf.offset);
                     node = leaf;
                     currentSize += 1;
                     return null;
                 }
                 hashPtr++;
             }
 
             LeafNode leaf = (LeafNode)node;
             if (leaf.key.equals(key)) {
                 ByteBuffer oldValue = leaf.value;
                 try {
                     leaf.setValue(value);
                 } catch (ValueIsBiggerThanOldException e) {
                     long offset = appendToStorage(LeafNode.estimateSize(key, value));
                     LeafNode newLeaf = LeafNode.writeToBuffer(data, offset, key, value);
                     parent.setNext(parentC, newLeaf.offset);
                 }
                 if (oldValue == null) {
                     currentSize += 1;
                 }
                 return oldValue;
             } else {
                 if (hashPtr >= hash.length)
                     throw new RuntimeException("SHA-1 collision!");
 
                 currentSize += 1;
                 if (leaf.value == null) {
                     long offset = appendToStorage(LeafNode.estimateSize(key, value));
                     LeafNode newLeaf = LeafNode.writeToBuffer(data, offset, key, value);
                     parent.setNext(parentC, newLeaf.offset);
                     return null;
                 }
 
                 byte[] hash2 = md.digest(leaf.key.array());
                 while (true) {
                     long offset = appendToStorage(InnerNode.estimateSize());
                     InnerNode newInner = InnerNode.writeToBuffer(data, offset);
                     parent.setNext(parentC, newInner.offset);
 
                     parent = newInner;
                     int c1 = hash[hashPtr] & 0xFF, c2 = hash2[hashPtr] & 0xFF;
                     if (c1 != c2) break;
 
                     parentC = c1;
                     hashPtr++;
                 }
                 parent.setNext(hash2[hashPtr] & 0xFF, leaf.offset);
 
                 long offset = appendToStorage(LeafNode.estimateSize(key, value));
                 LeafNode newLeaf = LeafNode.writeToBuffer(data, offset, key, value);
                 parent.setNext(hash[hashPtr] & 0xFF, newLeaf.offset);
                 return null;
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private long appendToStorage(long size) throws IOException {
         long res = dataUsedLength;
         while (dataUsedLength + size > dataFile.length()) {
             dataFile.setLength(2 * dataFile.length());
             data = new HugeMappedFile(dataFile.getChannel());
         }
         dataUsedLength += size;
         data.putLong(USED_LENGTH_OFFSET, dataUsedLength);
         return res;
     }
 
     @Override
     public ByteBuffer remove(Object _key) {
         ByteBuffer key = (ByteBuffer)_key;
         try {
             LeafNode leaf = getNode(key);
             ByteBuffer oldValue = leaf.value;
             leaf.setValue(null);
             currentSize -= 1;
             return oldValue;
         } catch (IOException e) {
             throw new RuntimeException(e);
         } catch (NoSuchElementException e) {
             return null;
         }
     }
 }
