 package ru.chuprikov.search.index.indexer.spimi;
 
 import ru.chuprikov.search.database.IndexDB;
 import ru.chuprikov.search.database.PostingsWriter;
 import ru.chuprikov.search.database.TermDB;
 import ru.chuprikov.search.database.datatypes.Datatypes;
 import ru.chuprikov.search.index.indexer.Indexer;
 
 import java.io.*;
 import java.util.ArrayDeque;
 import java.util.PriorityQueue;
 
 public class SPIMIIndexer implements Indexer {
     private final long maxMemoryUsage;
     private final File temporaryFolder;
 
     private int chunkIdx = 0;
     private SPIMIChunkIndexer chunkIndexer;
 
     private final IndexDB indexDB;
 
     private boolean closed = false;
     private final TermDB termDB;
 
     public SPIMIIndexer(File temporaryFolder, IndexDB indexDB, TermDB termDB, long maxMemoryUsageBytes) {
         this.indexDB = indexDB;
         this.termDB = termDB;
         this.maxMemoryUsage = maxMemoryUsageBytes;
         this.temporaryFolder = temporaryFolder;
         this.temporaryFolder.mkdirs();
         chunkIndexer = new SPIMIChunkIndexer(maxMemoryUsageBytes);
     }
 
     @Override
     public void addToIndex(String term, Datatypes.Posting posting) throws IOException {
         checkClosed();
         if (chunkIndexer.isFull()) {
            try(DataOutputStream stream = getOutStream(chunkIdx)) {
                chunkIndexer.writeToTemporary(stream);
            }
            chunkIndexer = new SPIMIChunkIndexer(maxMemoryUsage);
            chunkIdx++;
         }
         chunkIndexer.addToIndex(term, posting);
     }
 
     private void checkClosed() {
         if (closed)
             throw new UnsupportedOperationException("SPIMI Indexer has been closed.");
     }
 
     private DataOutputStream getOutStream(int chunkIdx) throws FileNotFoundException {
         return new DataOutputStream(
             new BufferedOutputStream(new FileOutputStream(temporaryFolder.toPath().resolve("chunk" + chunkIdx).toFile())));
     }
 
     private DataInputStream getInStream(int chunkIdx) throws FileNotFoundException {
         return new DataInputStream(
             new BufferedInputStream(new FileInputStream(temporaryFolder.toPath().resolve("chunk" + chunkIdx).toFile())));
     }
 
     private void processPostings(String term, PriorityQueue<PostingsListReader> postings) throws Exception {
         try (PostingsWriter writer = indexDB.getPostingsWriter(termDB.add(term))) {
             while (!postings.isEmpty()) {
                 final PostingsListReader currentReader = postings.poll();
                 writer.appendPosting(currentReader.getCurrentPosting());
                 if (currentReader.hasNext()) {
                     currentReader.advance();
                     postings.add(currentReader);
                 }
             }
         }
     }
 
     @Override
     public void close() throws Exception {
         checkClosed();
         closed = true;
 
         PriorityQueue<SPIMIChunkReader> readers = new PriorityQueue<>();
         ArrayDeque<SPIMIChunkReader> currentReaders = new ArrayDeque<>();
         PriorityQueue<PostingsListReader> currentPostings = new PriorityQueue<>();
 
         for (int i = 0; i < chunkIdx; i++) {
             SPIMIChunkReader reader = new SPIMIChunkReader(getInStream(i));
             if (reader.hasNext()) {
                 reader.advance();
                 readers.add(reader);
             } else {
                 reader.close();
             }
         }
 
         while (!readers.isEmpty()) {
             String currentTerm = readers.peek().getTerm();
             while (!readers.isEmpty() && readers.peek().getTerm().equals(currentTerm)) {
                 SPIMIChunkReader currentReader = readers.poll();
                 currentReaders.add(currentReader);
                 if (currentReader.getPostingsList().hasNext()) {
                     currentReader.getPostingsList().advance();
                     currentPostings.add(currentReader.getPostingsList());
                 } else {
                     currentReader.close();
                 }
             }
 
             processPostings(currentTerm, currentPostings);
 
             currentPostings.clear();
 
             while (!currentReaders.isEmpty())
                 if (currentReaders.peek().hasNext()) {
                     currentReaders.peek().advance();
                     readers.add(currentReaders.poll());
                 } else {
                     currentReaders.poll().close();
                 }
         }
 
         for (int i = 0; i < chunkIdx; i++) {
             temporaryFolder.toPath().resolve("chunk" + chunkIdx).toFile().delete();
         }
     }
 }
