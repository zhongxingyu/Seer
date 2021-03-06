 // plasmaWordIndex.java
 // (C) 2005, 2006 by Michael Peter Christen; mc@anomic.de, Frankfurt a. M., Germany
 // first published 2005 on http://www.anomic.de
 //
 // This is a part of YaCy, a peer-to-peer based web search engine
 //
 // $LastChangedDate$
 // $LastChangedRevision$
 // $LastChangedBy$
 //
 // LICENSE
 // 
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 package de.anomic.plasma;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import de.anomic.htmlFilter.htmlFilterContentScraper;
 import de.anomic.index.indexCollectionRI;
 import de.anomic.index.indexContainer;
 import de.anomic.index.indexContainerOrder;
 import de.anomic.index.indexRWIEntry;
 import de.anomic.index.indexRAMRI;
 import de.anomic.index.indexRI;
 import de.anomic.index.indexRWIEntryNew;
 import de.anomic.index.indexRWIEntryOld;
 import de.anomic.index.indexURLEntry;
 import de.anomic.kelondro.kelondroBase64Order;
 import de.anomic.kelondro.kelondroException;
 import de.anomic.kelondro.kelondroMergeIterator;
import de.anomic.kelondro.kelondroNaturalOrder;
 import de.anomic.kelondro.kelondroOrder;
 import de.anomic.kelondro.kelondroRow;
 import de.anomic.net.URL;
 import de.anomic.plasma.urlPattern.plasmaURLPattern;
 import de.anomic.server.logging.serverLog;
 import de.anomic.yacy.yacyDHTAction;
 
 public final class plasmaWordIndex implements indexRI {
 
     private static final String indexAssortmentClusterPath = "ACLUSTER";
     private static final int assortmentCount = 64;
     private static final kelondroRow payloadrowold = indexRWIEntryOld.urlEntryRow;
     private static final kelondroRow payloadrownew = indexRWIEntryNew.urlEntryRow;
     
     private final File                             oldDatabaseRoot;
    private final kelondroOrder                    indexOrder = new kelondroNaturalOrder(true);
     private final indexRAMRI                       dhtOutCache, dhtInCache;
     private final indexCollectionRI                collections;          // new database structure to replace AssortmentCluster and FileCluster
     private int                                    assortmentBufferSize; // kb
     private final plasmaWordIndexAssortmentCluster assortmentCluster;    // old database structure, to be replaced by CollectionRI
     private final plasmaWordIndexFileCluster       backend;              // old database structure, to be replaced by CollectionRI
     public        boolean                          busyCacheFlush;       // shows if a cache flush is currently performed
     public        boolean                          useCollectionIndex;   // flag for usage of new collectionIndex db
     private       int idleDivisor, busyDivisor;
     
     public plasmaWordIndex(File oldDatabaseRoot, File newIndexRoot, boolean dummy, int bufferkb, long preloadTime, serverLog log, boolean useCollectionIndex) throws IOException {
         this.oldDatabaseRoot = oldDatabaseRoot;
         this.backend = new plasmaWordIndexFileCluster(oldDatabaseRoot, payloadrowold, log);
         File textindexcache = new File(newIndexRoot, "PUBLIC/TEXT/RICACHE");
         if (!(textindexcache.exists())) textindexcache.mkdirs();
         if (useCollectionIndex) {
             this.dhtOutCache = new indexRAMRI(textindexcache, payloadrownew, 1024, "dump1.array", log, true);
             this.dhtInCache  = new indexRAMRI(textindexcache, payloadrownew, 1024, "dump2.array", log, true);
         } else {
             this.dhtOutCache = new indexRAMRI(oldDatabaseRoot, payloadrowold, 64, "indexDump1.array", log, false);
             this.dhtInCache  = new indexRAMRI(oldDatabaseRoot, payloadrowold, 64, "indexDump2.array", log, false);
         }
         
         // create assortment cluster path
         File assortmentClusterPath = new File(oldDatabaseRoot, indexAssortmentClusterPath);
         this.assortmentBufferSize = bufferkb;
         
         // create collections storage path
         File textindexcollections = new File(newIndexRoot, "PUBLIC/TEXT/RICOLLECTION");
         if (!(textindexcollections.exists())) textindexcollections.mkdirs();
         if (useCollectionIndex) {
             this.collections = new indexCollectionRI(textindexcollections, "collection", bufferkb * 1024, preloadTime, payloadrownew);
             this.assortmentCluster = null;
         } else {
             this.collections = null;
             if (!(assortmentClusterPath.exists())) assortmentClusterPath.mkdirs();
             this.assortmentCluster = new plasmaWordIndexAssortmentCluster(assortmentClusterPath, assortmentCount, payloadrowold, assortmentBufferSize, preloadTime, log);
         }
         
         busyCacheFlush = false;
         this.useCollectionIndex = useCollectionIndex;
         this.busyDivisor = 5000;
         this.idleDivisor = 420;
     }
 
     public kelondroRow payloadrow() {
         if (useCollectionIndex) return payloadrownew; else return payloadrowold;
     }
     
     public indexRWIEntry newRWIEntry(
             String  urlHash,
             int     urlLength,
             int     urlComps,
             int     titleLength,
             int     hitcount,
             int     wordcount,
             int     phrasecount,
             int     posintext,
             int     posinphrase,
             int     posofphrase,
             int     worddistance,
             int     sizeOfPage,
             long    lastmodified,
             long    updatetime,
             int     quality,
             String  language,
             char    doctype,
             int     outlinksSame,
             int     outlinksOther,
             boolean local ) {
         if (useCollectionIndex)
             return new indexRWIEntryNew(urlHash, urlLength, urlComps, titleLength, hitcount, wordcount, phrasecount,
                 posintext, posinphrase, posofphrase, worddistance, sizeOfPage, lastmodified, updatetime, quality, language, doctype,
                 outlinksSame, outlinksOther, local);
         else
             return new indexRWIEntryOld(urlHash, urlLength, urlComps, titleLength, hitcount, wordcount, phrasecount,
                     posintext, posinphrase, posofphrase, worddistance, sizeOfPage, lastmodified, updatetime, quality, language, doctype,
                     outlinksSame, outlinksOther, local);
     }
     
     public File getRoot() {
         return oldDatabaseRoot;
     }
 
     public int maxURLinDHTOutCache() {
         return dhtOutCache.maxURLinCache();
     }
 
     public long minAgeOfDHTOutCache() {
         return dhtOutCache.minAgeOfCache();
     }
 
     public long maxAgeOfDHTOutCache() {
         return dhtOutCache.maxAgeOfCache();
     }
 
     public int maxURLinDHTInCache() {
         return dhtInCache.maxURLinCache();
     }
 
     public long minAgeOfDHTInCache() {
         return dhtInCache.minAgeOfCache();
     }
 
     public long maxAgeOfDHTInCache() {
         return dhtInCache.maxAgeOfCache();
     }
 
     public int dhtOutCacheSize() {
         return dhtOutCache.size();
     }
 
     public int dhtInCacheSize() {
         return dhtInCache.size();
     }
 
     public int[] assortmentsSizes() {
         return (assortmentCluster == null) ? null : assortmentCluster.sizes();
     }
 
     public int assortmentsCacheChunkSizeAvg() {
         return (assortmentCluster == null) ? 0 : assortmentCluster.cacheChunkSizeAvg();
     }
 
     public int assortmentsCacheObjectSizeAvg() {
         return (assortmentCluster == null) ? 0 : assortmentCluster.cacheObjectSizeAvg();
     }
 
     public int[] assortmentsCacheNodeStatus() {
         if (assortmentCluster != null) return assortmentCluster.cacheNodeStatus();
         return new int[]{0,0,0,0,0,0,0,0,0,0};
     }
     
     public long[] assortmentsCacheObjectStatus() {
         if (assortmentCluster != null) return assortmentCluster.cacheObjectStatus();
         return new long[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
     }
     
     public void setMaxWordCount(int maxWords) {
         dhtOutCache.setMaxWordCount(maxWords);
     }
 
     public void setInMaxWordCount(int maxWords) {
         dhtInCache.setMaxWordCount(maxWords);
     }
 
     public void setWordFlushDivisor(int idleDivisor, int busyDivisor) {
        this.idleDivisor = idleDivisor;
        this.busyDivisor = busyDivisor;
     }
 
     public void flushControl() {
         // check for forced flush
         synchronized (this) {
             if (dhtOutCache.size() > dhtOutCache.getMaxWordCount()) {
                 flushCache(dhtOutCache, dhtOutCache.size() + 500 - dhtOutCache.getMaxWordCount());
             }
             if (dhtInCache.size() > dhtInCache.getMaxWordCount()) {
                 flushCache(dhtInCache, dhtInCache.size() + 500 - dhtInCache.getMaxWordCount());
             }
         }
     }
     
     public long getUpdateTime(String wordHash) {
         indexContainer entries = getContainer(wordHash, null, false, -1);
         if (entries == null) return 0;
         return entries.updated();
     }
     
     public indexContainer emptyContainer(String wordHash) {
     	return new indexContainer(wordHash, payloadrow(), useCollectionIndex);
     }
 
     public indexContainer addEntry(String wordHash, indexRWIEntry entry, long updateTime, boolean dhtInCase) {
         if ((useCollectionIndex) && (entry instanceof indexRWIEntryOld)) entry = new indexRWIEntryNew((indexRWIEntryOld) entry);
         
         // set dhtInCase depending on wordHash
         if ((!dhtInCase) && (yacyDHTAction.shallBeOwnWord(wordHash))) dhtInCase = true;
         
         // add the entry
         if (dhtInCase) {
             dhtInCache.addEntry(wordHash, entry, updateTime, true);
         } else {
             dhtOutCache.addEntry(wordHash, entry, updateTime, false);
             flushControl();
         }
         return null;
     }
     
     private indexContainer convertOld2New(indexContainer entries) {
         // convert old entries to new entries
         indexContainer newentries = new indexContainer(entries.getWordHash(), payloadrownew, useCollectionIndex);
         Iterator i = entries.entries();
         indexRWIEntryOld old;
         while (i.hasNext()) {
             old = (indexRWIEntryOld) i.next();
             newentries.add(new indexRWIEntryNew(old));
         }
         return newentries;
     }
     
     public indexContainer addEntries(indexContainer entries, long updateTime, boolean dhtInCase) {
         if ((useCollectionIndex) && (entries.row().objectsize() == payloadrowold.objectsize())) entries = convertOld2New(entries);
         
         // set dhtInCase depending on wordHash
         if ((!dhtInCase) && (yacyDHTAction.shallBeOwnWord(entries.getWordHash()))) dhtInCase = true;
         
         // add the entry
         if (dhtInCase) {
             dhtInCache.addEntries(entries, updateTime, true);
         } else {
             dhtOutCache.addEntries(entries, updateTime, false);
             flushControl();
         }
         return null;
     }
 
     public void flushCacheSome(boolean busy) {
         flushCacheSome(dhtOutCache, busy);
         flushCacheSome(dhtInCache, busy);
     }
     
     private void flushCacheSome(indexRAMRI ram, boolean busy) {
         int flushCount = (busy) ? ram.size() / busyDivisor : ram.size() / idleDivisor;
         if (flushCount > 100) flushCount = 100;
         if (flushCount < 1) flushCount = Math.min(1, ram.size());
         flushCache(ram, flushCount);
         while (ram.maxURLinCache() > ((useCollectionIndex) ? 1024 : 64)) flushCache(ram, 1);
     }
     
     private void flushCache(indexRAMRI ram, int count) {
         if (count <= 0) return;
         if (count > 1000) count = 1000;
         busyCacheFlush = true;
         String wordHash;
         //System.out.println("DEBUG-Started flush of " + count + " entries from RAM to DB");
         //long start = System.currentTimeMillis();
         for (int i = 0; i < count; i++) { // possible position of outOfMemoryError ?
             if (ram.size() == 0) break;
             synchronized (this) {
                 wordHash = ram.bestFlushWordHash();
                 
                 // flush the wordHash
                 indexContainer c = ram.deleteContainer(wordHash);
                 if (c != null) {
                     if (useCollectionIndex) {
                         indexContainer feedback = collections.addEntries(c, c.updated(), false);
                         if (feedback != null) {
                             throw new RuntimeException("indexCollectionRI shall not return feedback entries; feedback = " + feedback.toString());
                         }
                     } else {
                         indexContainer feedback = assortmentCluster.addEntries(c, c.updated(), false);
                         if (feedback != null) {
                             backend.addEntries(feedback, System.currentTimeMillis(), true);
                         }
                     }
                 }
                 
                 // pause to next loop to give other processes a chance to use IO
                 //try {this.wait(8);} catch (InterruptedException e) {}
             }
         }
         //System.out.println("DEBUG-Finished flush of " + count + " entries from RAM to DB in " + (System.currentTimeMillis() - start) + " milliseconds");
         busyCacheFlush = false;
     }
     
     private static final int hour = 3600000;
     private static final int day  = 86400000;
     
     public static int microDateDays(Date modified) {
         return microDateDays(modified.getTime());
     }
     
     public static int microDateDays(long modified) {
         // this calculates a virtual age from a given date
         // the purpose is to have an age in days of a given modified date
         // from a fixed standpoint in the past
         // one day has 60*60*24 seconds = 86400 seconds
         // we take mod 64**3 = 262144, this is the mask of the storage
         return (int) ((modified / day) % 262144);
     }
         
     public static String microDateHoursStr(long time) {
         return kelondroBase64Order.enhancedCoder.encodeLong(microDateHoursInt(time), 3);
     }
     
     public static int microDateHoursInt(long time) {
         return (int) ((time / hour) % 262144);
     }
     
     public static int microDateHoursAge(String mdhs) {
         return microDateHoursInt(System.currentTimeMillis()) - (int) kelondroBase64Order.enhancedCoder.decodeLong(mdhs);
     }
     
     public static long reverseMicroDateDays(int microDateDays) {
         return ((long) microDateDays) * ((long) day);
     }
     
     public int addPageIndex(URL url, String urlHash, Date urlModified, int size, plasmaParserDocument document, plasmaCondenser condenser, String language, char doctype, int outlinksSame, int outlinksOther) {
         // this is called by the switchboard to put in a new page into the index
         // use all the words in one condenser object to simultanous create index entries
         
         // iterate over all words
         Iterator i = condenser.words();
         Map.Entry wentry;
         String word;
         indexRWIEntry ientry;
         plasmaCondenser.wordStatProp wprop;
         String wordHash;
         int urlLength = url.toString().length();
         int urlComps = htmlFilterContentScraper.urlComps(url.toString()).length;
         
         while (i.hasNext()) {
             wentry = (Map.Entry) i.next();
             word = (String) wentry.getKey();
             wprop = (plasmaCondenser.wordStatProp) wentry.getValue();
             // if ((s.length() > 4) && (c > 1)) System.out.println("# " + s + ":" + c);
             wordHash = plasmaURL.word2hash(word);
             ientry = newRWIEntry(urlHash,
                         urlLength, urlComps, (document == null) ? urlLength : document.getMainLongTitle().length(),
                         wprop.count,
                         condenser.RESULT_SIMI_WORDS,
                         condenser.RESULT_SIMI_SENTENCES,
                         wprop.posInText,
                         wprop.posInPhrase,
                         wprop.numOfPhrase,
                         0,
                         size,
                         urlModified.getTime(),
                         System.currentTimeMillis(),
                         condenser.RESULT_WORD_ENTROPHY,
                         language,
                         doctype,
                         outlinksSame, outlinksOther,
                         true);
             addEntry(wordHash, ientry, System.currentTimeMillis(), false);
         }
         // System.out.println("DEBUG: plasmaSearch.addPageIndex: added " +
         // condenser.getWords().size() + " words, flushed " + c + " entries");
         return condenser.RESULT_SIMI_WORDS;
     }
 
     public indexContainer getContainer(String wordHash, Set urlselection, boolean deleteIfEmpty, long maxTime) {
         long start = System.currentTimeMillis();
 
         // get from cache
         indexContainer container = dhtOutCache.getContainer(wordHash, urlselection, true, -1);
         if (container == null) {
             container = dhtInCache.getContainer(wordHash, urlselection, true, -1);
         } else {
             container.add(dhtInCache.getContainer(wordHash, urlselection, true, -1), -1);
         }
 
         // get from collection index
         if (useCollectionIndex) {
             if (container == null) {
                 container = collections.getContainer(wordHash, urlselection, true, (maxTime < 0) ? -1 : maxTime);
             } else {
                 container.add(collections.getContainer(wordHash, urlselection, true, (maxTime < 0) ? -1 : maxTime), -1);
             }
         } else {
             // get from assortments
             if (assortmentCluster != null) {
                 if (container == null) {
                     container = assortmentCluster.getContainer(wordHash, urlselection, true, (maxTime < 0) ? -1 : maxTime);
                 } else {
                     // add containers from assortment cluster
                     container.add(assortmentCluster.getContainer(wordHash, urlselection, true, (maxTime < 0) ? -1 : maxTime), -1);
                 }
             }
 
             // get from backend
             if (maxTime > 0) {
                 maxTime = maxTime - (System.currentTimeMillis() - start);
                 if (maxTime < 0) maxTime = 100;
             }
             if (container == null) {
                 container = backend.getContainer(wordHash, urlselection, deleteIfEmpty, (maxTime < 0) ? -1 : maxTime);
             } else {
                 container.add(backend.getContainer(wordHash, urlselection, deleteIfEmpty, (maxTime < 0) ? -1 : maxTime), -1);
             }
         }
         return container;
     }
 
     public Map getContainers(Set wordHashes, Set urlselection, boolean deleteIfEmpty, boolean interruptIfEmpty, long maxTime) {
         // return map of wordhash:indexContainer
         
         // retrieve entities that belong to the hashes
         HashMap containers = new HashMap();
         String singleHash;
         indexContainer singleContainer;
             Iterator i = wordHashes.iterator();
             long start = System.currentTimeMillis();
             long remaining;
             while (i.hasNext()) {
                 // check time
                 remaining = maxTime - (System.currentTimeMillis() - start);
                 //if ((maxTime > 0) && (remaining <= 0)) break;
                 if ((maxTime >= 0) && (remaining <= 0)) remaining = 100;
             
                 // get next word hash:
                 singleHash = (String) i.next();
             
                 // retrieve index
                 singleContainer = getContainer(singleHash, urlselection, deleteIfEmpty, (maxTime < 0) ? -1 : remaining / (wordHashes.size() - containers.size()));
             
                 // check result
                 if (((singleContainer == null) || (singleContainer.size() == 0)) && (interruptIfEmpty)) return new HashMap();
             
                 containers.put(singleHash, singleContainer);
             }
         return containers;
     }
 
     public int size() {
             if (useCollectionIndex)
                 return java.lang.Math.max(collections.size(), java.lang.Math.max(dhtInCache.size(), dhtOutCache.size()));
             else
                 return java.lang.Math.max((assortmentCluster == null) ? 0 : assortmentCluster.size(),
                         java.lang.Math.max(backend.size(),
                          java.lang.Math.max(dhtInCache.size(), dhtOutCache.size())));
     }
 
     public int indexSize(String wordHash) {
         int size = 0;
         size += dhtInCache.indexSize(wordHash);
         size += dhtOutCache.indexSize(wordHash);
         if (useCollectionIndex) {
             size += collections.indexSize(wordHash);
         } else try {
             size += (assortmentCluster == null) ? 0 : assortmentCluster.indexSize(wordHash);
             plasmaWordIndexFile entity = backend.getEntity(wordHash, true, -1);
             if (entity != null) {
                 size += entity.size();
                 entity.close();
             }
         } catch (IOException e) {}
         return size;
     }
 
     public void close(int waitingBoundSeconds) {
         synchronized (this) {
             dhtInCache.close(waitingBoundSeconds);
             dhtOutCache.close(waitingBoundSeconds);
             if (useCollectionIndex) {
                 collections.close(-1);
             } else {
                 if (assortmentCluster != null) assortmentCluster.close(-1);
                 backend.close(10);
             }
         }
     }
 
     public indexContainer deleteContainer(String wordHash) {
         indexContainer c = new indexContainer(wordHash, payloadrow(), useCollectionIndex);
         c.add(dhtInCache.deleteContainer(wordHash), -1);
         c.add(dhtOutCache.deleteContainer(wordHash), -1);
         if (useCollectionIndex) {
             c.add(collections.deleteContainer(wordHash), -1);
         } else {
             if (assortmentCluster != null) c.add(assortmentCluster.deleteContainer(wordHash), -1);
             c.add(backend.deleteContainer(wordHash), -1);
         }
         return c;
     }
     
     public boolean removeEntry(String wordHash, String urlHash, boolean deleteComplete) {
         boolean removed = false;
         removed = removed | (dhtInCache.removeEntry(wordHash, urlHash, deleteComplete));
         removed = removed | (dhtOutCache.removeEntry(wordHash, urlHash, deleteComplete));
         if (useCollectionIndex) {
             removed = removed | (collections.removeEntry(wordHash, urlHash, deleteComplete));
         } else {
             if (assortmentCluster != null) removed = removed | (assortmentCluster.removeEntry(wordHash, urlHash, deleteComplete));
             removed = removed | backend.removeEntry(wordHash, urlHash, deleteComplete);
         }
         return removed;
     }
     
     public int removeEntries(String wordHash, Set urlHashes, boolean deleteComplete) {
         int removed = 0;
         removed += dhtInCache.removeEntries(wordHash, urlHashes, deleteComplete);
         removed += dhtOutCache.removeEntries(wordHash, urlHashes, deleteComplete);
         if (useCollectionIndex) {
             removed += collections.removeEntries(wordHash, urlHashes, deleteComplete);
         } else if (assortmentCluster != null) {
             removed += assortmentCluster.removeEntries(wordHash, urlHashes, deleteComplete);
             removed += backend.removeEntries(wordHash, urlHashes, deleteComplete);
         }
         return removed;
     }
     
     public String removeEntriesExpl(String wordHash, Set urlHashes, boolean deleteComplete) {
         String removed = "";
         removed += dhtInCache.removeEntries(wordHash, urlHashes, deleteComplete) + ", ";
         removed += dhtOutCache.removeEntries(wordHash, urlHashes, deleteComplete) + ", ";
         if (useCollectionIndex) {
             removed += collections.removeEntries(wordHash, urlHashes, deleteComplete);
         } else {
             if (assortmentCluster != null) removed += assortmentCluster.removeEntries(wordHash, urlHashes, deleteComplete) + ", ";
             removed += backend.removeEntries(wordHash, urlHashes, deleteComplete);
         }
         return removed;
     }
     
     public static final int RL_RAMCACHE    = 0;
     public static final int RL_COLLECTIONS = 1; // the new index structure
     public static final int RL_ASSORTMENTS = 2; // (to be) outdated structure
     public static final int RL_WORDFILES   = 3; // (to be) outdated structure
     
     public int tryRemoveURLs(String urlHash) {
         // this tries to delete an index from the cache that has this
         // urlHash assigned. This can only work if the entry is really fresh
         // and can be found in the RAM cache
         // this returns the number of deletion that had been possible
         return dhtInCache.tryRemoveURLs(urlHash) | dhtOutCache.tryRemoveURLs(urlHash);
     }
     
     public TreeSet indexContainerSet(String startHash, int resourceLevel, boolean rot, int count) throws IOException {
         // creates a set of indexContainers
         // this does not use the dhtInCache
         kelondroOrder containerOrder = new indexContainerOrder((kelondroOrder) indexOrder.clone());
         containerOrder.rotate(startHash.getBytes());
         TreeSet containers = new TreeSet(containerOrder);
         Iterator i = wordContainers(startHash, resourceLevel, rot);
         if (resourceLevel == plasmaWordIndex.RL_RAMCACHE) count = Math.min(dhtOutCache.size(), count);
         indexContainer container;
         while ((count > 0) && (i.hasNext())) {
             container = (indexContainer) i.next();
             if ((container != null) && (container.size() > 0)) {
                 containers.add(container);
                 count--;
             }
         }
         return containers;
     }
     
     public Iterator wordContainers(String startHash, boolean rot) {
         // returns an iteration of indexContainers
         try {
             return wordContainers(startHash, RL_WORDFILES, rot);
         } catch (IOException e) {
             return new HashSet().iterator();
         }
     }
     
     public Iterator wordContainers(String startHash, int resourceLevel, boolean rot) throws IOException {
         if (rot) return new rotatingContainerIterator(startHash, resourceLevel);
         else return wordContainers(startHash, resourceLevel);
     }
 
     private Iterator wordContainers(String startWordHash, int resourceLevel) throws IOException {
         if (resourceLevel == plasmaWordIndex.RL_RAMCACHE) {
             return dhtOutCache.wordContainers(startWordHash, false);
         }
         if (useCollectionIndex) {
             return new kelondroMergeIterator(
                             dhtOutCache.wordContainers(startWordHash, false),
                             collections.wordContainers(startWordHash, false),
                            new indexContainerOrder(kelondroNaturalOrder.naturalOrder),
                             indexContainer.containerMergeMethod,
                             true);
         } else {
             if (resourceLevel == plasmaWordIndex.RL_ASSORTMENTS) {
                 return new kelondroMergeIterator(
                             dhtOutCache.wordContainers(startWordHash, false),
                             (assortmentCluster == null) ? null : assortmentCluster.wordContainers(startWordHash, true, false),
                            new indexContainerOrder(kelondroNaturalOrder.naturalOrder),
                             indexContainer.containerMergeMethod,
                             true);
             }
             if (resourceLevel == plasmaWordIndex.RL_WORDFILES) {
                 return new kelondroMergeIterator(
                             new kelondroMergeIterator(
                                      dhtOutCache.wordContainers(startWordHash, false),
                                      (assortmentCluster == null) ? null : assortmentCluster.wordContainers(startWordHash, true, false),
                                     new indexContainerOrder(kelondroNaturalOrder.naturalOrder),
                                      indexContainer.containerMergeMethod,
                                      true),
                             backend.wordContainers(startWordHash, false),
                            new indexContainerOrder(kelondroNaturalOrder.naturalOrder),
                             indexContainer.containerMergeMethod,
                             true);
             }
         }
         return null;
     }
     
     public class rotatingContainerIterator implements Iterator {
         Iterator i;
         int resourceLevel;
 
         public rotatingContainerIterator(String startWordHash, int resourceLevel) throws IOException {
             this.resourceLevel = resourceLevel;
             i = wordContainers(startWordHash, resourceLevel);
         }
 
         public void finalize() {
             i = null;
         }
 
         public boolean hasNext() {
             if (i.hasNext()) return true;
             else try {
                 i = wordContainers("------------", resourceLevel);
                 return i.hasNext();
             } catch (IOException e) {
                 return false;
             }
         }
 
         public Object next() {
             return i.next();
         }
 
         public void remove() {
             throw new java.lang.UnsupportedOperationException("rotatingWordIterator does not support remove");
         }
     } // class rotatingContainerIterator
 
     public Object migrateWords2Assortment(String wordhash) throws IOException {
         // returns the number of entries that had been added to the assortments
         // can be negative if some assortments have been moved to the backend
         File db = plasmaWordIndexFile.wordHash2path(oldDatabaseRoot, wordhash);
         if (!(db.exists())) return "not available";
         plasmaWordIndexFile entity = null;
         try {
             entity =  new plasmaWordIndexFile(oldDatabaseRoot, wordhash, true);
             int size = entity.size();
             if (size > assortmentCluster.clusterCapacity) {
                 // this will be too big to integrate it
                 entity.close(); entity = null;
                 return "too big";
             } else {
                 // take out all words from the assortment to see if it fits
                 // together with the extracted assortment
                 indexContainer container = assortmentCluster.deleteContainer(wordhash, -1);
                 if (size + container.size() > assortmentCluster.clusterCapacity) {
                     // this will also be too big to integrate, add to entity
                     entity.addEntries(container);
                     entity.close(); entity = null;
                     return new Integer(-container.size());
                 } else {
                     // the combined container will fit, read the container
                     try {
                         Iterator entries = entity.elements(true);
                         indexRWIEntry entry;
                         while (entries.hasNext()) {
                             entry = (indexRWIEntry) entries.next();
                             // System.out.println("ENTRY = " + entry.getUrlHash());
                             container.add(new indexRWIEntry[]{entry}, System.currentTimeMillis());
                         }
                         // we have read all elements, now delete the entity
                         entity.deleteComplete();
                         entity.close(); entity = null;
                         // integrate the container into the assortments; this will work
                         assortmentCluster.addEntries(container, container.updated(), false);
                         return new Integer(size);
                     } catch (kelondroException e) {
                         // database corrupted, we simply give up the database and delete it
                         try {entity.close();} catch (Exception ee) {} entity = null;
                         try {db.delete();} catch (Exception ee) {}
                         return "database corrupted; deleted";                        
                     }
                 }
             }
         } finally {
             if (entity != null) try {entity.close();}catch(Exception e){}
         }
     }
 
     public Object migrateWords2index(String wordhash) throws IOException {
         // returns the number of entries that had been added to the assortments
         // can be negative if some assortments have been moved to the backend
         File db = plasmaWordIndexFile.wordHash2path(oldDatabaseRoot, wordhash);
         if (!(db.exists())) return "not available";
         plasmaWordIndexFile entity = null;
         try {
             entity = new plasmaWordIndexFile(oldDatabaseRoot, wordhash, true);
             int size = entity.size();
             indexContainer container = new indexContainer(wordhash, payloadrow(), useCollectionIndex);
 
             try {
                 Iterator entries = entity.elements(true);
                 indexRWIEntry entry;
                 while (entries.hasNext()) {
                     entry = (indexRWIEntry) entries.next();
                     // System.out.println("ENTRY = " + entry.getUrlHash());
                     container.add(new indexRWIEntry[] { entry }, System.currentTimeMillis());
                 }
                 // we have read all elements, now delete the entity
                 entity.deleteComplete();
                 entity.close();
                 entity = null;
 
                 indexContainer feedback = collections.addEntries(container, container.updated(), false);
                 if (feedback != null) return feedback;
                 return new Integer(size);
             } catch (kelondroException e) {
                 // database corrupted, we simply give up the database and delete it
                 try { entity.close(); } catch (Exception ee) { }
                 entity = null;
                 try { db.delete(); } catch (Exception ee) { }
                 return "database corrupted; deleted";
             }
         } finally {
             if (entity != null) try {entity.close();}catch(Exception e){}
         }
     }
     
     //  The Cleaner class was provided as "UrldbCleaner" by Hydrox
     //  see http://www.yacy-forum.de/viewtopic.php?p=18093#18093
     public Cleaner makeCleaner(plasmaCrawlLURL lurl, String startHash) {
         return new Cleaner(lurl, startHash);
     }
     
     public class Cleaner extends Thread {
         
         private String startHash;
         private boolean run = true;
         private boolean pause = false;
         public int rwiCountAtStart = 0;
         public String wordHashNow = "";
         public String lastWordHash = "";
         public int lastDeletionCounter = 0;
         private plasmaCrawlLURL lurl;
         
         public Cleaner(plasmaCrawlLURL lurl, String startHash) {
             this.lurl = lurl;
             this.startHash = startHash;
             this.rwiCountAtStart = size();
         }
         
         public void run() {
             serverLog.logInfo("INDEXCLEANER", "IndexCleaner-Thread started");
             indexContainer container = null;
             indexRWIEntry entry = null;
             URL url = null;
             HashSet urlHashs = new HashSet();
             try {
                 Iterator indexContainerIterator = indexContainerSet(startHash, plasmaWordIndex.RL_WORDFILES, false, 100).iterator();
                 while (indexContainerIterator.hasNext() && run) {
                     waiter();
                     container = (indexContainer) indexContainerIterator.next();
                     Iterator containerIterator = container.entries();
                     wordHashNow = container.getWordHash();
                     while (containerIterator.hasNext() && run) {
                         waiter();
                         entry = (indexRWIEntry) containerIterator.next();
                         // System.out.println("Wordhash: "+wordHash+" UrlHash: "+entry.getUrlHash());
                         indexURLEntry ue = lurl.load(entry.urlHash(), null);
                         if (ue == null) {
                             urlHashs.add(entry.urlHash());
                         } else {
                             url = ue.comp().url();
                             if ((url == null) || (plasmaSwitchboard.urlBlacklist.isListed(plasmaURLPattern.BLACKLIST_CRAWLER, url) == true)) {
                                 urlHashs.add(entry.urlHash());
                             }
                         }
                     }
                     if (urlHashs.size() > 0) {
                         int removed = removeEntries(container.getWordHash(), urlHashs, true);
                         serverLog.logFine("INDEXCLEANER", container.getWordHash() + ": " + removed + " of " + container.size() + " URL-entries deleted");
                         lastWordHash = container.getWordHash();
                         lastDeletionCounter = urlHashs.size();
                         urlHashs.clear();
                     }
                     if (!containerIterator.hasNext()) {
                         // We may not be finished yet, try to get the next chunk of wordHashes
                         TreeSet containers = indexContainerSet(container.getWordHash(), plasmaWordIndex.RL_WORDFILES, false, 100);
                         indexContainerIterator = containers.iterator();
                         // Make sure we don't get the same wordhash twice, but don't skip a word
                         if ((indexContainerIterator.hasNext())&&(!container.getWordHash().equals(((indexContainer) indexContainerIterator.next()).getWordHash()))) {
                             indexContainerIterator = containers.iterator();
                         }
                     }
                 }
             } catch (IOException e) {
                 serverLog.logSevere("INDEXCLEANER",
                         "IndexCleaner-Thread: unable to start: "
                                 + e.getMessage());
             }
             serverLog.logInfo("INDEXCLEANER", "IndexCleaner-Thread stopped");
         }
         
         public void abort() {
             synchronized(this) {
                 run = false;
                 this.notifyAll();
             }
         }
 
         public void pause() {
             synchronized(this) {
                 if(pause == false)  {
                     pause = true;
                     serverLog.logInfo("INDEXCLEANER", "IndexCleaner-Thread paused");                
                 }
             }
         }
 
         public void endPause() {
             synchronized(this) {
                 if (pause == true) {
                     pause = false;
                     this.notifyAll();
                     serverLog.logInfo("INDEXCLEANER", "IndexCleaner-Thread resumed");
                 }
             }
         }
         
         public void waiter() {
             synchronized(this) {
                 if (this.pause) {
                     try {
                         this.wait();
                     } catch (InterruptedException e) {
                         this.run = false;
                         return;
                     }
                 }
             }
         }
     }
     
     public static void main(String[] args) {
         // System.out.println(kelondroMSetTools.fastStringComparator(true).compare("RwGeoUdyDQ0Y", "rwGeoUdyDQ0Y"));
         // System.out.println(new Date(reverseMicroDateDays(microDateDays(System.currentTimeMillis()))));
         File plasmadb = new File("D:\\dev\\proxy\\DATA\\PLASMADB");
         File indexdb = new File("D:\\dev\\proxy\\DATA\\INDEX");
         try {
             plasmaWordIndex index = new plasmaWordIndex(plasmadb, indexdb, true, 555, 1000, new serverLog("TESTAPP"), false);
             Iterator containerIter = index.wordContainers("5A8yhZMh_Kmv", plasmaWordIndex.RL_WORDFILES, true);
             while (containerIter.hasNext()) {
                 System.out.println("File: " + (indexContainer) containerIter.next());
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         
     }
 
 }
