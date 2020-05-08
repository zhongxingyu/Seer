 package org.hypoport.milk.maven.plugin;
 
 import org.apache.maven.artifact.Artifact;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class ArtifactSetWriter {
 
   private File basedir;
 
   public ArtifactSetWriter(File basedir) {
     this.basedir = basedir;
   }
 
   public void writeArtifacts(Iterable<Artifact> artifacts, File outputFile) throws IOException {
     BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
     try {
       boolean first = true;
       for (Artifact artifact : artifacts) {
         first = prependCommaIfNotFirstEntry(writer, first);
         writer.write(relativeModulePath(artifact));
       }
       writer.newLine();
     }
     finally {
       writer.close();
     }
   }
 
   private String relativeModulePath(Artifact artifact) {
    String path = artifact.getFile().getParent();
    if (basedir.getPath().equals(path)) {
      return ".";
    }
    return path.substring(this.basedir.getPath().length() + 1);
   }
 
   private boolean prependCommaIfNotFirstEntry(BufferedWriter writer, boolean first) throws IOException {
     if (!first) {
       writer.write(",");
     }
     return false;
   }
 }
