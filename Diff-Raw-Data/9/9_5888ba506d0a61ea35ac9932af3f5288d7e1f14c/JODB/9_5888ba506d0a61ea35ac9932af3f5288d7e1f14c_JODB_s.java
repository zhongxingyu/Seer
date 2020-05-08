 // Copyright 2002-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.jos;
 
 import static org.joe_e.file.Filesystem.file;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.crypto.Cipher;
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.joe_e.JoeE;
 import org.joe_e.Struct;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.file.Filesystem;
 import org.joe_e.file.InvalidFilenameException;
 import org.ref_send.list.List;
 import org.ref_send.log.Anchor;
 import org.ref_send.log.Event;
 import org.ref_send.log.Got;
 import org.ref_send.log.Sent;
 import org.ref_send.log.Turn;
 import org.ref_send.promise.Fulfilled;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.eventual.ConditionalRunner;
 import org.ref_send.promise.eventual.Loop;
 import org.ref_send.promise.eventual.Task;
 import org.ref_send.var.Factory;
 import org.ref_send.var.Receiver;
 import org.waterken.base32.Base32;
 import org.waterken.project.Project;
 import org.waterken.vat.Creator;
 import org.waterken.vat.CyclicGraph;
 import org.waterken.vat.Effect;
 import org.waterken.vat.ProhibitedCreation;
 import org.waterken.vat.ProhibitedModification;
 import org.waterken.vat.Root;
 import org.waterken.vat.Service;
 import org.waterken.vat.Tracer;
 import org.waterken.vat.Transaction;
 import org.waterken.vat.UnknownClass;
 import org.waterken.vat.Vat;
 import org.web_send.graph.Collision;
 import org.web_send.graph.Unavailable;
 
 /**
  * An object graph stored as a folder of Java Object Serialization files.
  */
 public final class
 JODB extends Vat {
 
     /**
      * canonical persistence folder
      */
     private final File folder;
 
     protected
     JODB(final File folder, final Loop<Service> service) {
         super(service);
         this.folder = folder;
     }
 
     // org.waterken.vat.Vat interface
 
     /**
      * Is a {@link #enter transaction} currently being processed?
      */
     private boolean busy = false;
 
     /**
      * Has the event loop been restarted?
      */
     private boolean awake = false;
 
     /**
      * Processes a transaction within this vat.
      */
     public synchronized <R> Promise<R>
     enter(final boolean extend, final Transaction<R> body) throws Exception {
         if (busy) { throw new Exception(); }
         busy = true;
         try {
             if (!awake) {
                 awake = process(Vat.extend, new Transaction<Boolean>() {
                     public Boolean
                     run(final Root local) throws Exception {
                         // start up a runner if necessary
                         if (null == runner && null != service) {
                             final List<?> q = local.fetch(null, tasks);
                             if (null != q && !q.isEmpty()) {
                                 final Run x = new Run();
                                 service.run(x);
                                 runner = x;
                             }
                         }
 
                         // start up any other configured services
                         final Transaction<?> wake = local.fetch(null,Root.wake);
                         if (null != wake) { wake.run(local); }
 
                         return true;
                     }
                 }).cast();
             }
             return process(extend, body);
         } catch (final Error e) {
             // allow the caller to recover from an aborted transaction
             if (e instanceof OutOfMemoryError) { System.gc(); }
             final Throwable cause = e.getCause();
             if (cause instanceof Exception) { throw (Exception)cause; }
             throw new Exception(e);
         } finally {
             busy = false;
         }
     }
 
     /**
      * An object store entry.
      */
     static final class
     Bucket extends Struct {
         final boolean created;      // Is a newly created bucket?
         final Object value;         // contained object
         final ByteArray version;    // secure hash of serialization, or
                                     // <code>null</code> if not known
 
         Bucket(final boolean created, final Object value,
                final ByteArray version) {
             this.created = created;
             this.value = value;
             this.version = version;
         }
     }
 
     /**
      * {@link Root#prng}
      */
     private SecureRandom prng;
 
     /**
      * {@link Root#code}
      */
     private ClassLoader code;
 
     protected <R> Promise<R>
     process(final boolean extend, final Transaction<R> body) throws Exception {
         // finish object initialization, which was delayed to avoid doing
         // anything intensive while holding the global "live" lock
         if (null == prng) { prng = new SecureRandom(); }
         if (null == code) { code = application(folder); }
 
         // Is the current transaction still active?
         final boolean[] active = { true };
 
         // setup tables to keep track of what's been loaded from disk
         // and what needs to be written out to disk
         final HashMap<String,Bucket> k2b =          // [ file key => bucket ]
             new HashMap<String,Bucket>(64);
         final IdentityHashMap<Object,String> o2k =  // [ object => file key ]
             new IdentityHashMap<Object,String>(64);
         final HashSet<String> xxx =                 // [ dirty file key ]
             new HashSet<String>(64);
         final Root root = new Root() {
 
             private Turn turn = null;
             private long anchors = 0L;
             
             public String
             getVatName() { return folder.getName(); }
             
             public Anchor
             anchor() {
                 if (null == turn) {
                 	final Stats current = fetch(null, stats);
                 	final String loop = fetch(null, Root.here);
                     turn = new Turn(loop, current.getChanged()); 
                 }
                 return new Anchor(turn, anchors++);
             }
 
             public String
             pipeline(final String m) {
                 final byte[] key = Base32.decode(m);
                 if (128/Byte.SIZE != key.length) {throw new RuntimeException();}
                 try {
                     final byte[] plaintext = new byte[128 / Byte.SIZE];
                     final byte[] cyphertext = new byte[128 / Byte.SIZE];
                     final Cipher aes = Cipher.getInstance("AES/ECB/NoPadding");
                     aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key,"AES"));
                     aes.doFinal(plaintext, 0, plaintext.length, cyphertext);
                     return Base32.encode(cyphertext);
                 } catch (final Exception e) { throw new Error(e); }
             }
 
             /**
              * Creates an object store entry.
              * <p>
              * Each entry is stored as a file in the persistence folder. The
              * corresponding filename is of the form XYZ.jos, where the XYZ
              * component may be chosen by the client and the ".jos" extension is
              * automatically appended by the implementation. All filenames are
              * composed of only lower case letters. For example,
              * ".stuff.jos.jos" is the filename corresponding to the user chosen
              * name ".Stuff.JOS". The file extension chosen by the user carries
              * no significance in this implementation, and neither does a
              * filename starting with a "." character. All filenames are vetted
              * by the Joe-E filesystem API.
              * </p>
              * <p>
              * The filename generated by {@link #export} is considered the
              * canonical name for an object. Names assigned to an object via
              * {@link #link} are valid for lookup, but will not be returned by
              * {@link #export}. This feature is implemented by wrapping a
              * {@linkplain #link linked} object in a {@link SymbolicLink}, and
              * unwrapping it on {@link #fetch}.
              * </p>
              */
             public void
             link(final String name, final Object value) throws Collision {
                 if (!active[0]) { throw new AssertionError(); }
                 if (extend) { throw new ProhibitedModification(Root.class); }
 
                 final String key = name.toLowerCase();
                 try {
                     if (null != load(key)) { throw new Collision(); }
                 } catch (final Exception e) { throw new Collision(); }
                 Filesystem.checkName(key + ext);
                 k2b.put(key, new Bucket(true, new SymbolicLink(value), null));
                 xxx.add(key);
             }
 
            public <T> T
             fetch(final Object otherwise, final String name) {
                 if (!active[0]) { throw new AssertionError(); }
 
                 final Bucket b = load(name.toLowerCase());
                 return (T)(null == b
                     ? otherwise
                 : b.value instanceof SymbolicLink
                     ? ((SymbolicLink)b.value).target
                 : b.value);
             }
 
             /**
              * for detecting cyclic object graphs
              */
             private final Bucket pumpkin = new Bucket(false, null, null);
             private final ArrayList<String> stack = new ArrayList<String>(16);
 
             private Bucket
             load(final String key) {
                 Bucket bucket = k2b.get(key);
                 if (null == bucket) {
                     final File file;
                     try {
                         file = file(folder, key + ext);
                     } catch (final InvalidFilenameException e) { return null; }
                     if (!file.isFile()) { return null; }
 
                     k2b.put(key, pumpkin);
                     stack.add(key);
                     try {
                         bucket = read(file);
                     } finally {
                         k2b.remove(key);
                         stack.remove(stack.size() - 1);
                     }
 
                     k2b.put(key, bucket);
                     o2k.put(bucket.value, key);
                     if (!frozen(bucket.value)) { xxx.add(key); }
                 } else if (pumpkin == bucket) {
                     // hit a cyclic reference
 
                     // determine the cycle depth
                     int n = 1;
                     for (int i = stack.size();
                          i-- != 0 && !key.equals(stack.get(i));) { ++n; }
 
                     // determine the type of object in each file
                     final Class<?>[] type = new Class<?>[n];
                     for (int i = stack.size(); n-- != 0;) {
                         final String at = stack.get(--i);
                         type[n] = Object.class;
                         InputStream fin = null;
                         try {
                             final int pos = n;
                             fin = Filesystem.read(file(folder, at + ext));
                             final SubstitutionStream in= new SubstitutionStream(
                                     true, JODB.this.code, fin) {
                                 protected Object
                                 resolveObject(Object x) {
                                     if (x instanceof Wrapper) {
                                         type[pos] = x.getClass();
                                         x = null;
                                     }
                                     return x;
                                 }
                             };
                             fin = in;
                             final Object x = in.readObject();
                             if (null != x) { type[n] = x.getClass(); }
                         } catch (final Exception e) { /* unknown type */ }
                         if(null!=fin){ try{fin.close();}catch(IOException e){} }
                     }
                     throw new CyclicGraph(PowerlessArray.array(type));
                 }
                 return bucket;
             }
             
             private boolean externalStateAccessed = false;
 
             /**
              * Reads a stored object graph.
              * @param file  containing file
              * @return corresponding bucket
              */
             private Bucket
             read(final File file) {
                 InputStream fin = null;
                 try {
                     fin = Filesystem.read(file);
                     final Mac mac = allocMac(this);
                     fin = new MacInputStream(fin, mac);
                     final Root loader = this;
                     final SubstitutionStream in =
                             new SubstitutionStream(true, JODB.this.code, fin) {
                         protected Object
                         resolveObject(Object x) throws IOException {
                             if (x instanceof File) {
                                 externalStateAccessed = true;
                             } else if (x instanceof Wrapper) {
                                 x = ((Wrapper)x).peel(loader);
                             }
                             return x;
                         }
                     };
                     fin = in;
                     final Object value = in.readObject();
                     fin.close();
                     final byte[] version = mac.doFinal();
                     freeMac(mac);
                     return new Bucket(false, value, ByteArray.array(version));
                 } catch (final Exception e) {
                     if (null != fin) {try {fin.close();} catch (Exception x){}}
                     if (e instanceof IOException) { throw new Error(e); }
                     if (e instanceof RuntimeException) {
                         throw (RuntimeException)e;
                     }
                     if (e instanceof ClassNotFoundException) {
                         throw new UnknownClass(e.getMessage());
                     }
                     throw new RuntimeException(e.getMessage(), e);
                 }
             }
             
             public String
             getTransactionTag() {
                 if (!active[0]) { throw new AssertionError(); }
                 
                 if (externalStateAccessed) { return null; }
 
                 final Mac mac;
                 try {
                     mac = allocMac(this);
                 } catch (final Exception e) { throw new Error(e); }
                 for (final Bucket b : k2b.values()) {
                     if (!b.created && null != b.version) {
                         mac.update(b.version.toByteArray());
                     }
                 }
                 if (JODB.this.code instanceof Project) {
                     // include code timestamp in the ETag
                     long buffer = ((Project)JODB.this.code).timestamp;
                     for (int i = Long.SIZE; 0 != i; i -= Byte.SIZE) {
                         mac.update((byte)buffer);
                         buffer >>>= Byte.SIZE;
                     }
                 }
                 final byte[] id = mac.doFinal();
                 freeMac(mac);
                 return '\"' + Base32.encode(id).substring(0, 2*keyChars) + '\"';
             }
 
             public String
             export(final Object value) {
                 if (!active[0]) { throw new AssertionError(); }
 
                 String key = o2k.get(value);
                 if (null == key) {
                     if (null == value || Slicer.inline(value.getClass())) {
                         // reuse an equivalent persistent identity;
                         // otherwise, determine the persistent identity
                         final byte[] id;
                         try {
                             final Mac mac = allocMac(this);
                             final Slicer out = new Slicer(value, this,
                                     new MacOutputStream(sink, mac));
                             out.writeObject(value);
                             out.flush();
                             out.close();
                             id = mac.doFinal();
                             freeMac(mac);
                         } catch (final Exception e) { throw new Error(e); }
                         final ByteArray version = ByteArray.array(id);
                         final byte[] d = new byte[keyBytes];
                         System.arraycopy(id, 0, d, 0, d.length);
                         while (true) {
                             key = key(d);
                             try {
                                 final Bucket b = load(key);
                                 if (null == b) {
                                     k2b.put(key,new Bucket(true,value,version));
                                     o2k.put(value, key);
                                     xxx.add(key);
                                     break;
                                 }
                                 if (version.equals(b.version)) { break; }
                             } catch (final Exception e) {/*skip broken bucket*/}
                             for (int i = 0; i != d.length && 0 == ++d[i]; ++i);
                         }
                     } else {
                         // to support caching of query responses, forbid
                         // creation of selfish state in an extend transaction
                         if (extend) {
                             throw new ProhibitedCreation(value.getClass());
                         }
 
                         // assign a new persistent identity
                         do {
                             final byte[] d = new byte[keyBytes];
                             JODB.this.prng.nextBytes(d);
                             key = key(d);
                         } while (k2b.containsKey(key) ||
                                  file(folder, key + ext).isFile());
                         k2b.put(key, new Bucket(true, value, null));
                         o2k.put(value, key);
                         xxx.add(key);
                     }
                 }
                 return key;
             }
         };
         final List<Effect> effects = List.list();
         final Loop<Effect> effect = new Loop<Effect>() {
             public void
             run(final Effect task) { effects.append(task); }
         };
         final Runnable destruct = new Runnable() {
             public void
             run() {
                 if (!active[0]) { throw new AssertionError(); }
 
                 root.link(dead, true);
                 effect.run(new Effect() {
                     public void
                     run() {
                         service.run(new Service() {
                             public void
                             run() throws Exception {
                                 final File tmp = file(folder.getParentFile(),
                                     "." + folder.getName() + ".tmp");
                                 while (!folder.renameTo(tmp) &&
                                        folder.isDirectory()) {
                                     Thread.sleep(60 * 1000);
                                 }
                                 delete(tmp);
                             }
                         });
                     }
                 });
             }
         };
         final boolean[] scheduled = { false };
         final Loop<Task> enqueue = new Loop<Task>() {
             public void
             run(final Task task) {
                 if (!active[0]) { throw new AssertionError(); }
 
                 // enqueue the event
                 final List<Task> q = root.fetch(null, tasks);
                 q.append(task);
                 scheduled[0] = true;
                 
                 // skip logging of a self-logging task
                 if (task instanceof ConditionalRunner) { return; }
                 
                 // determine if logging is turned on
                 final Factory<Receiver<Event>> erf=root.fetch(null,Root.events);
                 if (null == erf) { return; }
                 final Receiver<Event> er = erf.run();
                 if (null == er) { return; }
 
                 // output a log event
                 final Stats now = root.fetch(null, stats);
                 if (null == now) { return; }
                 final long future = now.getDequeued() + q.getSize();
                 final Anchor anchor = root.anchor();
                 final Tracer tracer = root.fetch(null, Root.tracer);
                 final Sent e = new Sent(anchor, null != tracer ?
                     tracer.get() : null, anchor.turn.loop + future); 
                 effect.run(new Effect() { public void run() { er.run(e); } });
             }
         };
         final boolean[] modified = { false };
         final File pending = file(folder, ".pending");
         final Creator creator = new Creator() {
 
             public ClassLoader
             load(final String project) throws Exception {
                 return Project.connect(project);
             }
 
             public <T> T
             create(final Transaction<T> initialize,
                    final String project, final String name) throws Exception {
                 if (!active[0]) { throw new AssertionError(); }
                 if (extend) { throw new ProhibitedModification(Creator.class); }
 
                 if (!modified[0]) {
                     if (!pending.mkdir()) {throw new Error(new IOException());}
                     modified[0] = true;
                 }
 
                 final String key;
                 if (null != name) {
                     key = claim(name);
                 } else {
                     String x = null;
                     while (true) {
                         try {
                             final byte[] d = new byte[4];
                             prng.nextBytes(d);
                             x = claim(Base32.encode(d).substring(0, 6));
                             break;
                         } catch (final Collision e) {}
                     }
                     key = x;
                 }
                 final File sub = file(pending, key);
                 if (!sub.mkdir()) {throw new Error(new IOException());}
                 if (null != project) { setConfig(sub, Root.project, project); }
                 final byte[] bits = new byte[128 / Byte.SIZE];
                 prng.nextBytes(bits);
                 setConfig(sub, secret, ByteArray.array(bits));
                 final Promise<T> r;
                 try {
                     r = new JODB(sub, null).enter(change, new Transaction<T>() {
                         public T
                         run(final Root local) throws Exception {
                             final List<Task> q = List.list();
                             local.link(tasks, q);
                             local.link(stats, new Stats());
                             return initialize.run(local);
                         }
                     });
                 } catch (final Exception e) { throw new Error(e); }
                 return r.cast();
             }
 
             /**
              * Claims a sub-vat name.
              * <p>
              * The persistence folder may also contain sub-folders named by the
              * client; however, the implementation forbids use of names that
              * start with a "." character. Folder names starting with a "." are
              * reserved for use by the implementation. For example, folders
              * containing pending modifications, or folders to be deleted, have
              * names starting with a ".".
              * </p>
              * @param name  vat name to claim
              * @return canonicalized vat name
              * @throws Collision    <code>name</code> is not available
              */
             private String
             claim(final String name) throws Collision {
                 final String key = name.toLowerCase();
                 if ("".equals(key)) { throw new Collision(); }
                 if (key.startsWith(".")) { throw new Unavailable(); }
 
                 final String was = "." + key + ".was";
                 try {
                     if (file(folder, was).isFile()) { throw new Collision(); }
                 } catch (InvalidFilenameException e) {throw new Unavailable();}
                 try {
                     if (!file(pending, was).createNewFile()) {
                         throw new Collision();
                     }
                 } catch (IOException e) { throw new Error(e); }
                 return key;
             }
         };
 
         // setup the pseudo-persistent objects
         k2b.put(Root.code, new Bucket(false, code, null));
         k2b.put(".shared", new Bucket(false, code.getParent(), null));
         k2b.put(Root.nothing, new Bucket(false, null, null));
         k2b.put(Root.prng, new Bucket(false, prng, null));
         k2b.put(".root", new Bucket(false, root, null));
         k2b.put(Root.enqueue, new Bucket(false, enqueue, null));
         k2b.put(Root.effect, new Bucket(false, effect, null));
         k2b.put(Root.destruct, new Bucket(false, destruct, null));
         k2b.put(Root.vat, new Bucket(false, this, null));
         k2b.put(Root.creator, new Bucket(false, creator, null));
         for (final Map.Entry<String,Bucket> x : k2b.entrySet()) {
             o2k.put(x.getValue().value, x.getKey());
         }
 
         // recover from a previous run
         final File committed = file(folder, ".committed");
         if (pending.isDirectory()) {
             if (committed.isDirectory()) { delete(committed); }
             delete(pending);
         } else if (committed.isDirectory()) {
             move(committed, folder);
         }
 
         // check that the vat has not been destructed
         if (!folder.isDirectory() ||
                 Boolean.TRUE.equals(root.fetch(false, dead))) {
             return new Rejected<R>(new FileNotFoundException());
         }
 
         // execute the transaction body
         Promise<R> r;
         try {
             try {
                 r = Fulfilled.detach(body.run(root));
             } catch (final Exception e) {
                 r = new Rejected<R>(e);
             }
             
             // update the change count if needed.
             if (!extend) {
                 final Stats now = root.fetch(null, stats);
                 if (null != now) { now.incrementChanged(); }
             }
 
             // persist the modifications
             while (!xxx.isEmpty()) {
                 final Iterator<String> i = xxx.iterator();
                 final String key = i.next();
                 final Bucket b = k2b.get(key);
                 i.remove();
 
                 OutputStream fout;
                 if (extend && !b.created) {
                     fout = new MacOutputStream(sink, allocMac(root)) {
                         public void
                         close() throws IOException {
                             super.close();
                             final ByteArray v = ByteArray.array(mac.doFinal());
                             freeMac(mac);
                             if (!v.equals(b.version)) {
                                 throw new ProhibitedModification(null != b.value
                                     ? b.value.getClass() : Void.class);
                             }
                         }
                     };
                 } else {
                     if (!modified[0]) {
                         if (!pending.mkdir()) { throw new IOException(); }
                         modified[0] = true;
                     }
                     fout = Filesystem.writeNew(file(pending, key + ext));
                 }
                 try {
                     final Slicer out = new Slicer(b.value, root, fout);
                     fout = out;
                     out.writeObject(b.value);
                     out.flush();
                 } catch (final Exception e) {
                     try { fout.close(); } catch (final Exception e2) {}
                     throw e;
                 }
                 fout.close();
             }
         } finally {
             active[0] = false;
         }
 
         if (modified[0]) {
             // commit modifications
             if (!pending.renameTo(committed)) { throw new IOException(); }
             move(committed, folder);
         } else {
             // no modifications were made, so just make sure all the reads
             // had valid state available
             if (!folder.isDirectory()) { throw new IOException(); }
         }
 
         // only run effects if this db is live and not in the middle of creation
         if (null != service) {
             // run all the pending effects
             while (!effects.isEmpty()) { effects.pop().run(); }
     
             // start up a runner if necessary
             if (null == runner && scheduled[0]) {
                 final Run x = new Run();
                 service.run(x);
                 runner = x;
             }
         }
 
         return r;
     }
 
     /**
      * {@link Root} name for the {@link Stats}
      */
     static private final String stats = ".stats";
 
     /**
      * Gets a configuration value for a persistence folder.
      * @param folder    canonical persistence folder
      * @param key       configuration key
      */
     static private Object
     getConfig(final File folder, final String key) throws Exception {
         InputStream in = null;
         try {
             final File file = file(folder, key + ext);
             if (!file.isFile()) { return null; }
             in = Filesystem.read(file);
             final ObjectInputStream oin = new ObjectInputStream(in);
             in = oin;
             final Object r = oin.readObject();
             in.close();
             return r;
         } catch (final Exception e) {
             if (null != in) { try { in.close(); } catch (final Exception x) {} }
             if (e instanceof IOException) { throw new Error(e); }
             throw e;
         }
     }
 
     /**
      * Sets a configuration value for a persistence folder.
      * @param folder    canonical persistence folder
      * @param key       configuration key
      * @param value     configured value
      */
     static private void
     setConfig(final File folder,
               final String key, final Object value) throws Exception {
         OutputStream out = null;
         try {
             out = Filesystem.writeNew(file(folder, key + ext));
             final ObjectOutputStream oout = new ObjectOutputStream(out);
             out = oout;
             oout.writeObject(value);
             oout.flush();
             out.close();
         } catch (final Exception e) {
             if (null!=out) { try { out.close(); } catch (final Exception x) {} }
             if (e instanceof IOException) { throw new Error(e); }
             throw e;
         }
     }
 
     /**
      * Gets the corresponding classloader for a persistence folder.
      * @param folder    canonical persistence folder
      */
     static protected ClassLoader
     application(final File folder) throws Exception {
         return Project.connect((String)getConfig(folder, Root.project));
     }
 
     /**
      * In testing, allocation of hash objects doubled serialization time, so I'm
      * keeping a pool of them. Sucky code is like cancer.
      */
     private final ArrayList<Mac> macs = new ArrayList<Mac>();
 
     /**
      * {@link Root} name for the master secret
      */
     static private final String secret = ".secret";
 
     /**
      * MAC key generation secret
      */
     private SecretKeySpec master;
 
     private Mac
     allocMac(final Root local) throws Exception {
         if (!macs.isEmpty()) { return macs.remove(macs.size() - 1); }
         if (null == master) {
             final ByteArray bits = (ByteArray)getConfig(folder, secret);
             master = new SecretKeySpec(bits.toByteArray(), "HmacSHA256");
         }
         final Mac r = Mac.getInstance("HmacSHA256");
         r.init(master);
         return r;
     }
 
     private void
     freeMac(final Mac h) { macs.add(h); }
 
     /**
      * an output sink
      */
     static private final OutputStream sink = new OutputStream() {
 
         public void
         write(byte[] b, int off, int len) {}
 
         public void
         write(int b) {}
     };
 
     /**
      * corresponding {@link Destructor} flag
      */
     static private final String dead = ".dead";
 
     /**
      * {@link Root} name for the pending task {@link List}
      */
     static private final String tasks = ".tasks";
 
     /**
      * pending {@link Run} task, if any
      */
     private transient Run runner;
 
     /**
      * Process a single event from the stored event queue.
      */
     class Run implements Service {
         public void
         run() throws Exception {
             enter(change, new Transaction<Void>() {
                 public Void
                 run(final Root local) throws Exception {
                     runner = null;
                     
                     // pop an event
                     final List<Task> q = local.fetch(null, tasks);
                     final Task task = q.pop();
                     
                     // update the stats
                     final Stats now = local.fetch(null, stats);
                     if (null != now) { now.incrementDequeued(); }
                     
                     // update the log
                     final Loop<Effect> effect = local.fetch(null, Root.effect); 
                     if (!(task instanceof ConditionalRunner)) {
                         final Factory<Receiver<Event>> erf =
                         	local.fetch(null, Root.events);
                         final Receiver<Event> er = null!=erf ? erf.run() : null;
                         if (null != er) {
                             final Anchor anchor = local.anchor();
                             final Tracer tracer = local.fetch(null,Root.tracer);
                             final Got e = new Got(anchor,
                                 null != tracer ? tracer.get() : null,
                                 anchor.turn.loop + now.getDequeued()); 
                             effect.run(new Effect() {
                                 public void run() { er.run(e); }
                             });
                         }
                     }
 
                     // process the task
                     try {
                         task.run();
                     } catch (final Exception e) {
                         e.printStackTrace();
                     }
                     
                     // schedule another go-around, if needed
                     if (!q.isEmpty()) {
                         effect.run(new Effect() {
                             public void
                             run() throws Exception {
                                 service.run(Run.this);
                                 runner = Run.this;
                             }
                         });
                     }
                     return null;
                 }
             });
         }
     }
 
     /**
      * Move each file in the source folder to the destination folder.
      * @param from  source folder
      * @param to    destination folder
      * @throws IOException  file manipulation failed
      */
     static private void
     move(final File from, final File to) throws IOException {
         for (final File src : from.listFiles()) {
             final File dst = new File(to, src.getName());
             dst.delete();
             if (!src.renameTo(dst)) { throw new IOException(); }
         }
         if (!from.delete()) { throw new IOException(); }
     }
 
     /**
      * Recursively delete a folder.
      * @param dir   folder to recursively delete
      * @throws IOException  delete operation failed
      */
     static private void
     delete(final File dir) throws IOException {
         for (final File f : dir.listFiles()) {
             if (f.isDirectory()) {
                 delete(f);
             } else {
                 if (!f.delete()) { throw new IOException(); }
             }
         }
         if (!dir.delete()) { throw new IOException(); }
     }
 
     /**
      * file extension for a serialized Java object tree
      */
     static protected final String ext = ".jos";
 
     static private final int keyChars = 14;
     static private final int keyBytes = keyChars * 5 / 8 + 1;
 
     /**
      * Turn a bit string into a file key.
      * @param d object identifier
      * @return file key
      */
     static private String
     key(final byte[] d) { return Base32.encode(d).substring(0, keyChars); }
 
     /**
      * Is the object one-level deep immutable?
      * @param x candidate object
      * @return true if the object is one-level deep immutable,
      *         false if it might not be
      */
     static private boolean
     frozen(final Object x) {
         return null == x ||
                JoeE.instanceOf(x, org.joe_e.Selfless.class) ||
                JoeE.instanceOf(x, org.joe_e.Immutable.class);
     }
 }
