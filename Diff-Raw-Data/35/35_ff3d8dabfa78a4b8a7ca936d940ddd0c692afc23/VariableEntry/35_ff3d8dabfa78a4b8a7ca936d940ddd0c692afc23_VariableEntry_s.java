 package sword.java.class_analyzer.pool;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import sword.java.class_analyzer.FileError;
 import sword.java.class_analyzer.Utils;
 
 public class VariableEntry extends ConstantPoolEntry {
 
     private final int mNameReference;
     private final int mTypeReference;
 
     private TextEntry mName;
     private TextEntry mType;
 
     public VariableEntry(InputStream inStream) throws IOException, FileError {
         mNameReference = Utils.getBigEndian2Int(inStream);
         mTypeReference = Utils.getBigEndian2Int(inStream);
     }
 
     @Override
     boolean resolve(ConstantPool pool) throws FileError {
         mName = pool.get(mNameReference, TextEntry.class);
         mType = pool.get(mTypeReference, TextEntry.class);
 
         boolean resolved = mName.mResolved && mType.mResolved;
         if (resolved) {
             mResolved = true;
         }
 
         return resolved;
     }
 
     public String getName() {
         return mName.toString();
     }
 
     public String getType() {
         return mType.toString();
     }
 }
