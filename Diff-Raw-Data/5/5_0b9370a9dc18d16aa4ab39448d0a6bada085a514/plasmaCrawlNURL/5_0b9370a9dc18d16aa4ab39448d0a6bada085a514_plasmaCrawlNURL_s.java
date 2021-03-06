 // plasmaNURL.java 
 // -----------------------
 // part of YaCy
 // (C) by Michael Peter Christen; mc@anomic.de
 // first published on http://www.anomic.de
 // Frankfurt, Germany, 2004
 // last major change: 09.08.2004
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
 //
 // Using this software in any meaning (reading, learning, copying, compiling,
 // running) means that you agree that the Author(s) is (are) not responsible
 // for cost, loss of data or any harm that may be caused directly or indirectly
 // by usage of this softare or this documentation. The usage of this software
 // is on your own risk. The installation and usage (starting/running) of this
 // software may allow other people or application to access your computer and
 // any attached devices and is highly dependent on the configuration of the
 // software which must be done by the user of the software; the author(s) is
 // (are) also not responsible for proper configuration and usage of the
 // software, even if provoked by documentation provided together with
 // the software.
 //
 // Any changes to this file according to the GPL as documented in the file
 // gpl.txt aside this file in the shipment you received can be done to the
 // lines that follows this copyright notice here, but changes must not be
 // done inside the copyright notive above. A re-distribution must contain
 // the intact and unchanged copyright notice.
 // Contributions and changes to the program code must be marked as such.
 
 // NURL - noticed (known but not loaded) URL's
 
 package de.anomic.plasma;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import de.anomic.plasma.plasmaURL;
 import de.anomic.kelondro.kelondroBase64Order;
 import de.anomic.kelondro.kelondroBitfield;
 import de.anomic.kelondro.kelondroCache;
 import de.anomic.kelondro.kelondroException;
 import de.anomic.kelondro.kelondroFlexTable;
 import de.anomic.kelondro.kelondroIndex;
 import de.anomic.kelondro.kelondroRecords;
 import de.anomic.kelondro.kelondroRow;
 import de.anomic.kelondro.kelondroStack;
 import de.anomic.kelondro.kelondroTree;
 import de.anomic.net.URL;
 import de.anomic.server.logging.serverLog;
 import de.anomic.yacy.yacySeedDB;
 
 public class plasmaCrawlNURL {
     
     public static final int STACK_TYPE_NULL     =  0; // do not stack
     public static final int STACK_TYPE_CORE     =  1; // put on local stack
     public static final int STACK_TYPE_LIMIT    =  2; // put on global stack
     public static final int STACK_TYPE_OVERHANG =  3; // put on overhang stack; links that are known but not crawled
     public static final int STACK_TYPE_REMOTE   =  4; // put on remote-triggered stack
     public static final int STACK_TYPE_IMAGE    = 11; // put on image stack
     public static final int STACK_TYPE_MOVIE    = 12; // put on movie stack
     public static final int STACK_TYPE_MUSIC    = 13; // put on music stack
 
     /**
      * column length definition for the {@link plasmaURL#urlIndexFile} DB
      */
     public final static kelondroRow rowdef = new kelondroRow(
         "String urlhash-" + yacySeedDB.commonHashLength + ", " +    // the url's hash
         "String initiator-" + yacySeedDB.commonHashLength + ", " +  // the crawling initiator
         "String urlstring-256, " +                                  // the url as string
         "String refhash-" + yacySeedDB.commonHashLength + ", " +    // the url's referrer hash
         "String urlname-40, " +                                     // the name of the url, from anchor tag <a>name</a>
         "Cardinal appdate-4 {b64e}, " +                             // the time when the url was first time appeared
         "String profile-4, " +                                      // the name of the prefetch profile handle
         "Cardinal depth-2 {b64e}, " +                               // the prefetch depth so far, starts at 0
         "Cardinal parentbr-3 {b64e}, " +                            // number of anchors of the parent
         "Cardinal forkfactor-4 {b64e}, " +                          // sum of anchors of all ancestors
         "byte[] flags-4, " +                                        // flags
         "String handle-4"                                           // extra handle
         );
     
     private final plasmaCrawlBalancer coreStack;      // links found by crawling to depth-1
     private final plasmaCrawlBalancer limitStack;     // links found by crawling at target depth
     private final plasmaCrawlBalancer overhangStack;  // links found by crawling at depth+1
     private final plasmaCrawlBalancer remoteStack;    // links from remote crawl orders
     private kelondroStack imageStack;     // links pointing to image resources
     private kelondroStack movieStack;     // links pointing to movie resources
     private kelondroStack musicStack;     // links pointing to music resources
 
     private final HashSet stackIndex;     // to find out if a specific link is already on any stack
     private File cacheStacksPath;
     private int bufferkb;
     private long preloadTime;
     private boolean newdb;
     initStackIndex initThead;
     
     // the class object
     private kelondroIndex urlIndexFile = null;
 
     public plasmaCrawlNURL(File cachePath, int bufferkb, long preloadTime, boolean newdb) {
         super();
         this.cacheStacksPath = cachePath;
         this.bufferkb = bufferkb;
         this.preloadTime = preloadTime;
         
         // create a stack for newly entered entries
         if (!(cachePath.exists())) cachePath.mkdir(); // make the path
         this.newdb = newdb;
         openHashCache();
 
         File coreStackFile = new File(cachePath, "urlNoticeLocal0.stack");
         File limitStackFile = new File(cachePath, "urlNoticeLimit0.stack");
         File overhangStackFile = new File(cachePath, "urlNoticeOverhang0.stack");
         File remoteStackFile = new File(cachePath, "urlNoticeRemote0.stack");
         File imageStackFile = new File(cachePath, "urlNoticeImage0.stack");
         File movieStackFile = new File(cachePath, "urlNoticeMovie0.stack");
         File musicStackFile = new File(cachePath, "urlNoticeMusic0.stack");
         coreStack = new plasmaCrawlBalancer(coreStackFile);
         limitStack = new plasmaCrawlBalancer(limitStackFile);
         overhangStack = new plasmaCrawlBalancer(overhangStackFile);
         remoteStack = new plasmaCrawlBalancer(remoteStackFile);
         kelondroRow rowdef = new kelondroRow("byte[] urlhash-" + yacySeedDB.commonHashLength);
         imageStack = kelondroStack.open(imageStackFile, rowdef);
         movieStack = kelondroStack.open(movieStackFile, rowdef);
         musicStack = kelondroStack.open(musicStackFile, rowdef);
 
         // init stack Index
         stackIndex = new HashSet();
         (initThead = new initStackIndex()).start();
     }
 
     public int size() {
         try {
            return urlIndexFile.size() ;
        } catch (IOException e) {
            return 0;
        }
     }
     
     public int cacheNodeChunkSize() {
         if (urlIndexFile instanceof kelondroTree) return ((kelondroTree) urlIndexFile).cacheNodeChunkSize();
         if (urlIndexFile instanceof kelondroCache) return ((kelondroCache) urlIndexFile).cacheNodeChunkSize();
         if (urlIndexFile instanceof kelondroFlexTable) return ((kelondroFlexTable) urlIndexFile).cacheNodeChunkSize();
         return 0;
     }
     
     public int[] cacheNodeStatus() {
         if (urlIndexFile instanceof kelondroTree) return ((kelondroTree) urlIndexFile).cacheNodeStatus();
         if (urlIndexFile instanceof kelondroCache) return ((kelondroCache) urlIndexFile).cacheNodeStatus();
         if (urlIndexFile instanceof kelondroFlexTable) return ((kelondroFlexTable) urlIndexFile).cacheNodeStatus();
         return new int[]{0,0,0,0,0,0,0,0,0,0};
     }
     
     public int cacheObjectChunkSize() {
         if (urlIndexFile instanceof kelondroTree) return ((kelondroTree) urlIndexFile).cacheObjectChunkSize();
         if (urlIndexFile instanceof kelondroCache) return ((kelondroCache) urlIndexFile).cacheObjectChunkSize();
         if (urlIndexFile instanceof kelondroFlexTable) return ((kelondroFlexTable) urlIndexFile).cacheObjectChunkSize();
         return 0;
     }
     
     public long[] cacheObjectStatus() {
         if (urlIndexFile instanceof kelondroTree) return ((kelondroTree) urlIndexFile).cacheObjectStatus();
         if (urlIndexFile instanceof kelondroCache) return ((kelondroCache) urlIndexFile).cacheObjectStatus();
         if (urlIndexFile instanceof kelondroFlexTable) return ((kelondroFlexTable) urlIndexFile).cacheObjectStatus();
         return new long[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
     }
     
     public void waitOnInitThread() {
         try {
             if (this.initThead != null) {
                 this.initThead.join();
             }
         } catch (NullPointerException e) {            
         } catch (InterruptedException e) {}
         
     }
     
     public boolean getUseNewDB() {
         return (urlIndexFile instanceof kelondroFlexTable);
     }
     
     private void openHashCache() {
         if (newdb) {
             String newCacheName = "urlNotice5.table";
             cacheStacksPath.mkdirs();
             try {
                 urlIndexFile = new kelondroCache(new kelondroFlexTable(cacheStacksPath, newCacheName, bufferkb / 2 * 0x400, preloadTime, rowdef, kelondroBase64Order.enhancedCoder), bufferkb / 2 * 0x400, true, false);
             } catch (IOException e) {
                 e.printStackTrace();
                 System.exit(-1);
             }
         } else {
            File oldCacheFile = new File(cacheStacksPath, "urlNotice1.db");
             oldCacheFile.getParentFile().mkdirs();
             try {
                 urlIndexFile = new kelondroCache(kelondroTree.open(oldCacheFile, bufferkb / 2 * 0x400, preloadTime, rowdef), bufferkb / 2 * 0x400, true, true);
             } catch (IOException e) {
                 e.printStackTrace();
                 System.exit(-1);
             }
         }
     }
     
     private void resetHashCache() {
         if (urlIndexFile != null) {
             try {urlIndexFile.close();} catch (IOException e) {}
             urlIndexFile = null;
            File cacheFile = new File(cacheStacksPath, "urlNotice1.db");
             cacheFile.delete();
         }
         openHashCache();
     }
     
     public void close() {
         try {urlIndexFile.close();} catch (IOException e) {}
         coreStack.close();
         limitStack.close();
         overhangStack.close();
         remoteStack.close();
         try {imageStack.close();} catch (IOException e) {}
         try {movieStack.close();} catch (IOException e) {}
         try {musicStack.close();} catch (IOException e) {}
         if (urlIndexFile != null) try {
             urlIndexFile.close();
             urlIndexFile = null;
         } catch (IOException e) {}
     }
 
     public class initStackIndex extends Thread {
         public void run() {
             Iterator i;
             try {
                 i = coreStack.iterator();
                 while (i.hasNext()) stackIndex.add(new String((byte[]) i.next(), "UTF-8"));
             } catch (Exception e) {
                 coreStack.reset();
             }
             try {
                 i = limitStack.iterator();
                 while (i.hasNext()) stackIndex.add(new String((byte[]) i.next(), "UTF-8"));
             } catch (Exception e) {
                 limitStack.reset();
             }
             try {
                 i = overhangStack.iterator();
                 while (i.hasNext()) stackIndex.add(new String((byte[]) i.next(), "UTF-8"));
             } catch (Exception e) {
                 overhangStack.reset();
             }
             try {
                 i = remoteStack.iterator();
                 while (i.hasNext()) stackIndex.add(new String((byte[]) i.next(), "UTF-8"));
             } catch (Exception e) {
                 remoteStack.reset();
             }
             try {
                 i = imageStack.iterator();
                 while (i.hasNext()) stackIndex.add(new String(((kelondroRecords.Node) i.next()).getKey(), "UTF-8"));
             } catch (Exception e) {
                 imageStack = kelondroStack.reset(imageStack);
             }
             try {
                 i = movieStack.iterator();
                 while (i.hasNext()) stackIndex.add(new String(((kelondroRecords.Node) i.next()).getKey(), "UTF-8"));
             } catch (Exception e) {
                 movieStack = kelondroStack.reset(movieStack);
             }
             try {
                 i = musicStack.iterator();
                 while (i.hasNext()) stackIndex.add(new String(((kelondroRecords.Node) i.next()).getKey(), "UTF-8"));
             } catch (Exception e) {
                 musicStack = kelondroStack.reset(musicStack);
             }
             plasmaCrawlNURL.this.initThead = null;
         }
     }
     
     public boolean remove(String hash) {
         if (hash == null) return false;
         try {
             urlIndexFile.remove(hash.getBytes());
             return true;
         } catch (IOException e) {
             return false;
         }
     }
     
     private static String normalizeHandle(int h) {
         String d = Integer.toHexString(h);
         while (d.length() < rowdef.width(11)) d = "0" + d;
         return d;
     }
 
     public int stackSize() {
         // this does not count the overhang stack size
         return coreStack.size()  + limitStack.size() + remoteStack.size();
     }
 
     public int stackSize(int stackType) {
         switch (stackType) {
             case STACK_TYPE_CORE:     return coreStack.size();
             case STACK_TYPE_LIMIT:    return limitStack.size();
             case STACK_TYPE_OVERHANG: return overhangStack.size();
             case STACK_TYPE_REMOTE:   return remoteStack.size();
             case STACK_TYPE_IMAGE:    return imageStack.size();
             case STACK_TYPE_MOVIE:    return movieStack.size();
             case STACK_TYPE_MUSIC:    return musicStack.size();
             default: return -1;
         }
     }
 
     public boolean existsInStack(String urlhash) {
         return stackIndex.contains(urlhash);
     }
 
     public synchronized Entry newEntry(String initiator, URL url, Date loaddate,
                                        String referrer, String name, String profile,
                                        int depth, int anchors, int forkfactor) {
         return new Entry(initiator, url, referrer, name, loaddate,
                             profile, depth, anchors, forkfactor);
     }
     
     public synchronized Entry newEntry(Entry oldEntry) {
         if (oldEntry == null) return null;
         return new Entry(
                 oldEntry.initiator(),
                 oldEntry.url(),
                 oldEntry.referrerHash(),
                 oldEntry.name(),
                 oldEntry.loaddate(),
                 oldEntry.profileHandle(),
                 oldEntry.depth(),
                 oldEntry.anchors,
                 oldEntry.forkfactor
         );
     }
 
     public void push(int stackType, String domain, String hash) {
         try {
             switch (stackType) {
                 case STACK_TYPE_CORE:     coreStack.add(domain, hash.getBytes()); break;
                 case STACK_TYPE_LIMIT:    limitStack.add(domain, hash.getBytes()); break;
                 case STACK_TYPE_OVERHANG: overhangStack.add(domain, hash.getBytes()); break;
                 case STACK_TYPE_REMOTE:   remoteStack.add(domain, hash.getBytes()); break;
                 case STACK_TYPE_IMAGE:    imageStack.push(imageStack.row().newEntry(new byte[][] {hash.getBytes()})); break;
                 case STACK_TYPE_MOVIE:    movieStack.push(movieStack.row().newEntry(new byte[][] {hash.getBytes()})); break;
                 case STACK_TYPE_MUSIC:    musicStack.push(musicStack.row().newEntry(new byte[][] {hash.getBytes()})); break;
                 default: break;
             }
             stackIndex.add(hash);
         } catch (IOException er) {}
     }
 
     public Entry[] top(int stackType, int count) {
         switch (stackType) {
             case STACK_TYPE_CORE:     return top(coreStack, count);
             case STACK_TYPE_LIMIT:    return top(limitStack, count);
             case STACK_TYPE_OVERHANG: return top(overhangStack, count);
             case STACK_TYPE_REMOTE:   return top(remoteStack, count);
             case STACK_TYPE_IMAGE:    return top(imageStack, count);
             case STACK_TYPE_MOVIE:    return top(movieStack, count);
             case STACK_TYPE_MUSIC:    return top(musicStack, count);
             default: return null;
         }
     }
     
     public Iterator iterator(int stackType) {
         switch (stackType) {
         case STACK_TYPE_CORE:     return coreStack.iterator();
         case STACK_TYPE_LIMIT:    return limitStack.iterator();
         case STACK_TYPE_OVERHANG: return overhangStack.iterator();
         case STACK_TYPE_REMOTE:   return remoteStack.iterator();
         case STACK_TYPE_IMAGE:    return imageStack.iterator();
         case STACK_TYPE_MOVIE:    return movieStack.iterator();
         case STACK_TYPE_MUSIC:    return musicStack.iterator();
         default: return null;
         }        
     }
 
     public Entry pop(int stackType) throws IOException {
         switch (stackType) {
             case STACK_TYPE_CORE:     return pop(coreStack);
             case STACK_TYPE_LIMIT:    return pop(limitStack);
             case STACK_TYPE_OVERHANG: return pop(overhangStack);
             case STACK_TYPE_REMOTE:   return pop(remoteStack);
             case STACK_TYPE_IMAGE:    return pop(imageStack);
             case STACK_TYPE_MOVIE:    return pop(movieStack);
             case STACK_TYPE_MUSIC:    return pop(musicStack);
             default: return null;
         }
     }
 
     public void shift(int fromStack, int toStack) {
         try {
             Entry entry = pop(fromStack);
             push(toStack, entry.url.getHost(), entry.hash());
         } catch (IOException e) {
             return;
         }
     }
 
     public void clear(int stackType) {
         try {
             switch (stackType) {
                 case STACK_TYPE_CORE:     coreStack.clear(); break;
                 case STACK_TYPE_LIMIT:    limitStack.clear(); break;
                 case STACK_TYPE_OVERHANG: overhangStack.clear(); break;
                 case STACK_TYPE_REMOTE:   remoteStack.clear(); break;
                 case STACK_TYPE_IMAGE:    imageStack.clear(); break;
                 case STACK_TYPE_MOVIE:    movieStack.clear(); break;
                 case STACK_TYPE_MUSIC:    musicStack.clear(); break;
                 default: return;
             }
         } catch (IOException e) {}
     }
 
     private Entry pop(kelondroStack stack) throws IOException {
         // this is a filo - pop
         if (stack.size() > 0) {
             Entry e = new Entry(new String(stack.pop().getColBytes(0)));
             stackIndex.remove(e.hash);
             return e;
         } else {
             throw new IOException("crawl stack is empty");
         }
     }
 
     private Entry pop(plasmaCrawlBalancer balancer) throws IOException {
         // this is a filo - pop
         if (balancer.size() > 0) {
             String hash = new String(balancer.get());
             if (hash == null) throw new IOException("hash is null");
             Entry e = new Entry(hash);
             stackIndex.remove(e.hash);
             return e;
         } else {
             throw new IOException("balancer stack is empty");
         }
     }
 
     private Entry[] top(kelondroStack stack, int count) {
         // this is a filo - top
         if (count > stack.size()) count = stack.size();
         ArrayList list = new ArrayList(count);
         for (int i = 0; i < count; i++) {
             try {
                 byte[] hash = stack.top(i).getColBytes(0);
                 list.add(new Entry(new String(hash)));
             } catch (IOException e) {
                 continue;
             }
         }
         return (Entry[]) list.toArray(new Entry[list.size()]);
     }
 
     private Entry[] top(plasmaCrawlBalancer balancer, int count) {
         // this is a filo - top
         if (count > balancer.size()) count = balancer.size();
         ArrayList list = new ArrayList(count);
             for (int i = 0; i < count; i++) {
                 try {
                     byte[] hash = balancer.top(i);
                     list.add(new Entry(new String(hash)));
                 } catch (IOException e) {
                     continue;
                 }
             }
             return (Entry[])list.toArray(new Entry[list.size()]);
     }
 
     public synchronized Entry getEntry(String hash) throws IOException {
         return new Entry(hash);
     }
 
     public class Entry {
         private String   initiator;     // the initiator hash, is NULL or "" if it is the own proxy;
                                         // if this is generated by a crawl, the own peer hash in entered
         private String   hash;          // the url's hash
         private String   referrer;      // the url's referrer hash
         private URL      url;           // the url as string
         private String   name;          // the name of the url, from anchor tag <a>name</a>     
         private Date     loaddate;      // the time when the url was first time appeared
         private String   profileHandle; // the name of the prefetch profile
         private int      depth;         // the prefetch depth so far, starts at 0
         private int      anchors;       // number of anchors of the parent
         private int      forkfactor;    // sum of anchors of all ancestors
         private kelondroBitfield flags;
         private int      handle;
         private boolean  stored;
 
         public Entry(String initiator, 
                      URL url, 
                      String referrer, 
                      String name, 
                      Date loaddate, 
                      String profileHandle,
                      int depth, 
                      int anchors, 
                      int forkfactor
         ) {
             // create new entry and store it into database
             this.hash          = plasmaURL.urlHash(url);
             this.initiator     = initiator;
             this.url           = url;
             this.referrer      = (referrer == null) ? plasmaURL.dummyHash : referrer;
             this.name          = (name == null) ? "" : name;
             this.loaddate      = (loaddate == null) ? new Date() : loaddate;
             this.profileHandle = profileHandle; // must not be null
             this.depth         = depth;
             this.anchors       = anchors;
             this.forkfactor    = forkfactor;
             this.flags         = new kelondroBitfield(rowdef.width(10));
             this.handle        = 0;
             this.stored        = false;
         }
 
         public Entry(String hash) throws IOException {
             // generates an plasmaNURLEntry using the url hash
             // to speed up the access, the url-hashes are buffered
             // in the hash cache.
             // we have two options to find the url:
             // - look into the hash cache
             // - look into the filed properties
             // if the url cannot be found, this returns null
             this.hash = hash;
             if (hash == null) throw new IOException("hash is null");
             kelondroRow.Entry entry = urlIndexFile.get(hash.getBytes());
             if (entry != null) {
                 insertEntry(entry);
                 this.stored = true;
                 return;
             } else {
                 // show that we found nothing
                 throw new IOException("NURL: hash " + hash + " not found");
                 //this.url = null;
             }
         }
 
         public Entry(kelondroRow.Entry entry) throws IOException {
             assert (entry != null);
             insertEntry(entry);
             this.stored = false;
         }
 
         private void insertEntry(kelondroRow.Entry entry) throws IOException {
             String urlstring = entry.getColString(2, null);
             if (urlstring == null) throw new IOException ("url string is null");
             this.hash = entry.getColString(0, null);
             this.initiator = entry.getColString(1, null);
             this.url = new URL(urlstring);
             this.referrer = (entry.empty(3)) ? plasmaURL.dummyHash : entry.getColString(3, null);
             this.name = (entry.empty(4)) ? "" : entry.getColString(4, "UTF-8").trim();
             this.loaddate = new Date(86400000 * entry.getColLong(5));
             this.profileHandle = (entry.empty(6)) ? null : entry.getColString(6, null).trim();
             this.depth = (int) entry.getColLong(7);
             this.anchors = (int) entry.getColLong(8);
             this.forkfactor = (int) entry.getColLong(9);
             this.flags = new kelondroBitfield(entry.getColBytes(10));
             this.handle = Integer.parseInt(entry.getColString(11, null), 16);
             return;
         }
         
         public void store() {
             // stores the values from the object variables into the database
             if (this.stored) return;
             String loaddatestr = kelondroBase64Order.enhancedCoder.encodeLong(loaddate.getTime() / 86400000, rowdef.width(5));
             // store the hash in the hash cache
             try {
                 // even if the entry exists, we simply overwrite it
                 byte[][] entry = new byte[][] { 
                     this.hash.getBytes(),
                     (initiator == null) ? "".getBytes() : this.initiator.getBytes(),
                     this.url.toString().getBytes(),
                     this.referrer.getBytes(),
                     this.name.getBytes("UTF-8"),
                     loaddatestr.getBytes(),
                     (this.profileHandle == null) ? null : this.profileHandle.getBytes(),
                     kelondroBase64Order.enhancedCoder.encodeLong(this.depth, rowdef.width(7)).getBytes(),
                     kelondroBase64Order.enhancedCoder.encodeLong(this.anchors, rowdef.width(8)).getBytes(),
                     kelondroBase64Order.enhancedCoder.encodeLong(this.forkfactor, rowdef.width(9)).getBytes(),
                     this.flags.bytes(),
                     normalizeHandle(this.handle).getBytes()
                 };
                 if (urlIndexFile == null) System.out.println("urlHashCache is NULL");
                 if ((urlIndexFile != null) && (urlIndexFile.row() == null)) System.out.println("row() is NULL");
                 urlIndexFile.put(urlIndexFile.row().newEntry(entry));
                 this.stored = true;
             } catch (IOException e) {
                 serverLog.logSevere("PLASMA", "INTERNAL ERROR AT plasmaNURL:store:" + e.toString() + ", resetting NURL-DB");
                 e.printStackTrace();
                 resetHashCache();
             } catch (kelondroException e) {
                 serverLog.logSevere("PLASMA", "plasmaCrawlNURL.store failed: " + e.toString() + ", resetting NURL-DB");
                 e.printStackTrace();
                 resetHashCache();
             }
         }
 
         public String toString() {
             StringBuffer str = new StringBuffer();
             str.append("hash: ").append(hash==null ? "null" : hash).append(" | ")
                .append("initiator: ").append(initiator==null?"null":initiator).append(" | ")
                .append("url: ").append(url==null?"null":url.toString()).append(" | ")
                .append("referrer: ").append((referrer == null) ? plasmaURL.dummyHash : referrer).append(" | ")
                .append("name: ").append((name == null) ? "null" : name).append(" | ")
                .append("loaddate: ").append((loaddate == null) ? new Date() : loaddate).append(" | ")
                .append("profile: ").append(profileHandle==null?"null":profileHandle).append(" | ")
                .append("depth: ").append(Integer.toString(depth)).append(" | ")
                .append("forkfactor: ").append(Integer.toString(forkfactor)).append(" | ")
                .append("flags: ").append((flags==null) ? "null" : flags.exportB64());
                return str.toString();
         }
 
         /**
          * return a url-hash, based on the md5 algorithm
          * the result is a String of 12 bytes within a 72-bit space
          * (each byte has an 6-bit range)
          * that should be enough for all web pages on the world
          */
         public String hash() {
             return this.hash;
         }
 
         public String initiator() {
             if (initiator == null) return null;
             if (initiator.length() == 0) return null; 
             return initiator;
         }
 
         public boolean proxy() {
             return (initiator() == null);
         }
 
         public String referrerHash() {
             return this.referrer;
         }
 
         public URL url() {
             return url;
         }
 
         public Date loaddate() {
             return loaddate;
         }
 
         public String name() {
             // return the creator's hash
             return name;
         }
 
         public int depth() {
             return depth;
         }
 
         public String profileHandle() {
             return profileHandle;
         }
     }
 
     public class kiter implements Iterator {
         // enumerates entry elements
         Iterator i;
         boolean error = false;
         
         public kiter(boolean up, boolean rotating, String firstHash) throws IOException {
             i = urlIndexFile.rows(up, rotating, (firstHash == null) ? null : firstHash.getBytes());
             error = false;
         }
 
         public boolean hasNext() {
             if (error) return false;
             return i.hasNext();
         }
 
         public Object next() throws RuntimeException {
             kelondroRow.Entry e = (kelondroRow.Entry) i.next();
             if (e == null) return null;
             try {
                 return new Entry(e);
             } catch (IOException ex) {
                 throw new RuntimeException("error '" + ex.getMessage() + "' for hash " + e.getColString(0, null));
             }
         }
         
         public void remove() {
             i.remove();
         }
         
     }
 
     public Iterator entries(boolean up, boolean rotating, String firstHash) throws IOException {
         // enumerates entry elements
         return new kiter(up, rotating, firstHash);
     }
     
 }
