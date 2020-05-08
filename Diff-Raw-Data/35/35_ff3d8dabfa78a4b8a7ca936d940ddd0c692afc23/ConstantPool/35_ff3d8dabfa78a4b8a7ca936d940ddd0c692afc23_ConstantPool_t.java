 package sword.java.class_analyzer.pool;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import sword.java.class_analyzer.FileError;
 import sword.java.class_analyzer.Utils;
 
 public class ConstantPool {
 
    public final int poolEntryCountPlusOne;
     private final List<ConstantPoolEntry> entries = new ArrayList<ConstantPoolEntry>();
 
     public ConstantPool(InputStream inStream) throws IOException, FileError {
        poolEntryCountPlusOne = Utils.getBigEndian2Int(inStream);
 
        for (int counter = 1; counter < poolEntryCountPlusOne; counter++) {
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
            dump(System.out);
             throw new FileError(FileError.Kind.INVALID_POOL_INDEX, index, entries.size());
         }
 
         ConstantPoolEntry entry = entries.get(index - 1);
         if (!expectedClass.isInstance(entry)) {
            dump(System.out);
             throw new FileError(FileError.Kind.INVALID_POOL_TYPE_MATCH, index);
         }
 
         return expectedClass.cast(entry);
     }

    public void dump(PrintStream outStream) {
        outStream.println("Constant pool table:");
        final int entryAmount = entries.size();

        for (int i=0; i<entryAmount; i++) {
            ConstantPoolEntry entry = entries.get(i);
            outStream.println("  " + (i + 1) + '\t' + entry.getClass().getSimpleName() + '\t' + entry.toString());
        }
    }
 }
