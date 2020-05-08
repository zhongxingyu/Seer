 package falgout.backup.app;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.ObjectStreamException;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.lang.ref.SoftReference;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Objects;
 import java.util.Set;
 import java.util.UUID;
 
 public class History implements Serializable {
     public static final Path HISTORY_DIR = Paths.get(".history");
     
     private static final long serialVersionUID = 896130173729570254L;
     private static final Map<UUID, SoftReference<History>> CACHE = new LinkedHashMap<>();
     
     private final UUID id;
     private transient Set<Path> aliases;
     private transient Map<Path, Hash> dirHashes;
     
     private History(UUID id) {
         this.id = id;
         init();
     }
     
     private void init() {
         aliases = new LinkedHashSet<>();
         dirHashes = new LinkedHashMap<>();
     }
     
     public UUID getID() {
         return id;
     }
     
     public void addAlias(Path alias) throws IOException {
         if (aliases.add(alias)) {
             save(this);
         }
     }
     
     public void removeAlias(Path alias) throws IOException {
         if (aliases.remove(alias)) {
             save(this);
         }
     }
     
     public Set<Path> getAliases() {
         return Collections.unmodifiableSet(aliases);
     }
     
     public boolean updateHash(Path dir, byte[] hash) throws IOException {
         return updateHash(dir, new Hash(hash));
     }
     
     public boolean updateHash(Path dir, Hash hash) throws IOException {
         Hash old = dirHashes.put(dir, hash);
         boolean changed = !Objects.equals(hash, old);
         if (changed) {
             save(this);
         }
         return changed;
     }
     
     public Map<Path, Hash> getHashes() {
         return Collections.unmodifiableMap(dirHashes);
     }
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         return result;
     }
     
     @Override
     public boolean equals(Object obj) {
         if (this == obj) { return true; }
         if (obj == null) { return false; }
         if (!(obj instanceof History)) { return false; }
         History other = (History) obj;
         if (id == null) {
             if (other.id != null) { return false; }
         } else if (!id.equals(other.id)) { return false; }
         return true;
     }
     
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("History [id=");
         builder.append(id);
         builder.append(", aliases=");
         builder.append(aliases);
         builder.append(", dirHashes=");
         builder.append(dirHashes);
         builder.append("]");
         return builder.toString();
     }
     
     public static History get(UUID id) throws IOException {
         History h = checkCache(id);
         if (h != null) { return h; }
         
         Path file = HISTORY_DIR.resolve(id.toString());
         if (Files.exists(file)) {
             try (InputStream in = Files.newInputStream(file);
                     ObjectInputStream ois = new ObjectInputStream(in)) {
                 h = (History) ois.readObject();
             } catch (ClassNotFoundException e) {
                 throw new Error("Poorly formatted history file.", e);
             }
         } else {
             h = new History(id);
            // readResolve takes care of this for ObjectInputStreams
            CACHE.put(id, new SoftReference<>(h));
         }
         
         return h;
     }
     
     private static History checkCache(UUID id) {
         SoftReference<History> r = CACHE.get(id);
         return r == null ? null : r.get();
     }
     
     private static void save(History h) throws IOException {
         Files.createDirectories(HISTORY_DIR);
         try (OutputStream out = Files.newOutputStream(HISTORY_DIR.resolve(h.getID().toString()));
                 ObjectOutputStream oos = new ObjectOutputStream(out);) {
             oos.writeObject(h);
         }
     }
     
     private void writeObject(ObjectOutputStream out) throws IOException {
         List<String> aliases = new ArrayList<>(this.aliases.size());
         for (Path p : this.aliases) {
             aliases.add(p.toString());
         }
         
         Map<String, Hash> dirHashes = new LinkedHashMap<>(this.dirHashes.size());
         for (Entry<Path, Hash> e : this.dirHashes.entrySet()) {
             dirHashes.put(e.getKey().toString(), e.getValue());
         }
         
         out.defaultWriteObject();
         out.writeObject(aliases);
         out.writeObject(dirHashes);
     }
     
     private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         List<String> aliases = (List<String>) in.readObject();
         Map<String, Hash> dirHashes = (Map<String, Hash>) in.readObject();
         
         init();
         for (String s : aliases) {
             this.aliases.add(Paths.get(s));
         }
         for (Entry<String, Hash> e : dirHashes.entrySet()) {
             this.dirHashes.put(Paths.get(e.getKey()), e.getValue());
         }
     }
     
     private Object readResolve() throws ObjectStreamException {
         History h = checkCache(id);
         if (h == null) {
             CACHE.put(id, new SoftReference<>(this));
             return this;
         } else {
             return h;
         }
     }
 }
