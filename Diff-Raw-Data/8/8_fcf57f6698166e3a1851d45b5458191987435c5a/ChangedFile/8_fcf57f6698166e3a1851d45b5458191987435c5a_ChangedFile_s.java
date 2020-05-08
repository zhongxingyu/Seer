 package uk.org.sappho.code.change.management.data;
 
 import net.sf.oval.constraint.NotEmpty;
 import net.sf.oval.constraint.NotNull;
 
 public class ChangedFile {
 
     @NotNull
     @NotEmpty
    private final String filename;
     @NotNull
     @NotEmpty
    private final Boolean isCreate;
 
     public ChangedFile(String filename, Boolean isCreate) {
 
         this.filename = filename;
         this.isCreate = isCreate;
     }
 
     public String getFilename() {
 
         return filename;
     }
 
     public boolean isCreate() {
 
         return isCreate;
     }
 }
