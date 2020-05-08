 package sword.java.class_analyzer.pool;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import sword.java.class_analyzer.FileError;
 import sword.java.class_analyzer.Utils;
 
 public class ConstantPool {
 
     public final int poolEntryCount;
     private final List<ConstantPoolEntry> entries = new ArrayList<ConstantPoolEntry>();
 
     public ConstantPool(InputStream inStream) throws IOException, FileError {
         poolEntryCount = Utils.getBigEndian2Int(inStream);
 
         for (int counter = 1; counter < poolEntryCount; counter++) {
             entries.add(ConstantPoolEntry.get(inStream));
         }
 
         final int entryCount = entries.size();
         int unresolved;
         int previouslyUnresolved = entryCount;
 
         do {
             unresolved = 0;
 
             for (int counter = 0; counter < entryCount; counter++) {
                 ConstantPoolEntry entry = entries.get(counter);
 
                 if (!entry.mResolved && !entry.resolve(this)) {
                     unresolved++;
                 }
             }
 
             if (previouslyUnresolved <= unresolved) {
                 throw new FileError(FileError.Kind.POOL_INCONSISTENCE);
             }
         }while(unresolved > 0);
     }
 
     public <T extends ConstantPoolEntry> T get(int index, Class<T> expectedClass) throws FileError {
         if (index <= 0 || index > entries.size()) {
             throw new FileError(FileError.Kind.INVALID_POOL_INDEX, index, entries.size());
         }
 
        ConstantPoolEntry entry = entries.get(index - 1);
         if (!expectedClass.isInstance(entry)) {
             throw new FileError(FileError.Kind.INVALID_POOL_TYPE_MATCH, index);
         }
 
         return expectedClass.cast(entry);
     }
 }
