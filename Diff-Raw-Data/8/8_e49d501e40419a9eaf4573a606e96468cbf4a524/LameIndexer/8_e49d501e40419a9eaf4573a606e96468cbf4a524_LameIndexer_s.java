 package com.github.marschall.jandex;
 
 import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Execute;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.jboss.jandex.Index;
 import org.jboss.jandex.IndexWriter;
 import org.jboss.jandex.Indexer;
 
 import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
 
 @Mojo(name = "index",
   threadSafe = true,
   defaultPhase = PACKAGE)
 @Execute(goal = "index",
   phase = PACKAGE)
 public class LameIndexer extends AbstractMojo {
 
   /**
    * The folder that contains the JARs which should be indexed.
    */
   @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}")
   private File artifact;
 
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
     if (!this.artifact.exists()) {
       throw new MojoExecutionException("artifact " + this.artifact + " does not exists, run package first");
     }
     try {
       index(this.artifact);
     } catch (IOException e) {
       throw new MojoExecutionException("could not create indices", e);
     }
   }
 
   public static void main(String[] args) throws MojoExecutionException, IOException {
     for (String arg : args) {
       LameIndexer indexer = new LameIndexer();
       indexer.artifact = new File(arg);
       indexer.index(indexer.artifact);
     }
   }
 
   private boolean containsSubDeplyoments(String extension) {
     return "ear".equals(extension)
         || "war".equals(extension)
         || "rar".equals(extension);
   }
 
   private boolean containsClasses(String extension) {
     return "jar".equals(extension)
         || "war".equals(extension);
   }
 
 
   private void index(File file) throws MojoExecutionException, IOException {
     LameIndex index;
     try (JarFile jar = new JarFile(file)) {
       index = index(jar);
     }
     boolean changed = index.changed;
     if (changed) {
       File tempFile = writeIndexes(file, file.getName(), index.subDeploymentIndices);
       file.delete();
       tempFile.renameTo(file);
     }
     
   }
 
   private LameIndex index(JarFile jar) throws IOException, MojoExecutionException {
     String fileName = jar.getName();
     int dotIndex = fileName.lastIndexOf('.');
     if (dotIndex == -1) {
       throw new MojoExecutionException("cold not determine type of artifact: " + jar);
     }
     String extension = fileName.substring(dotIndex + 1);
     boolean changed = false;
     Index index = null;
     if (containsClasses(extension)) {
       if (!containsIndex(jar)) {
         index = buildIndex(jar);
         changed = true;
       }
     }
 
     List<LameSubDeploymentIndex> resultIndices;
     if (containsSubDeplyoments(extension)) {
       List<SubDeploymentIndex> subDeploymentIndexs = indexSubdeplyoments(extension, jar);
       if (!subDeploymentIndexs.isEmpty()) {
         resultIndices = new ArrayList<>(subDeploymentIndexs.size());
         changed = true;
         for (SubDeploymentIndex each : subDeploymentIndexs) {
           Index subDeploymentIndex = each.index;
           String subDeploymentName = each.subDeployment.getName();
           resultIndices.add(new LameSubDeploymentIndex(subDeploymentName, subDeploymentIndex));
         }
       } else {
         resultIndices = Collections.emptyList();
       }
     } else {
       resultIndices = Collections.emptyList();
     }
     return new LameIndex(index, changed, resultIndices);
   }
 
   private LameResult index(JarFile jar, JarEntry jarEntry) throws IOException, MojoExecutionException {
     // lib/spring.jar
     // suffix -> ".jar
     // prefix -> "spring"
     String name = jarEntry.getName();
     int dotIndex = name.lastIndexOf('.');
     String suffix = name.substring(dotIndex);
     int slashIndex = name.lastIndexOf('/');
     String prefix;
     if (slashIndex == -1) {
       prefix = name.substring(0, dotIndex);
     } else {
       prefix = name.substring(slashIndex + 1, dotIndex);
     }
 
     Path tempPath = Files.createTempFile(prefix, suffix);
     File tempFile = tempPath.toFile();
     try (InputStream input = jar.getInputStream(jarEntry)) {
       // Files.copy will do the buffering
       Files.copy(input, tempPath, REPLACE_EXISTING);
     }
     LameIndex lameIndex;
     try (JarFile tempJar = new JarFile(tempFile)) {
       lameIndex = index(tempJar);
     }
     boolean changed = lameIndex.changed;
     if (changed) {
       tempFile = writeIndexes(tempFile, name, lameIndex.subDeploymentIndices);
       return new LameResult(tempFile, true, lameIndex.index);
     } else {
       return new LameResult(null, false, null);
     }
   }
 
   private File writeIndexes(File jar, String entryName, List<LameSubDeploymentIndex> subDeploymentIndices) throws IOException {
     int dotIndex = entryName.lastIndexOf('.');
     if (dotIndex == -1) {
       throw new AssertionError("missing extension from: " + entryName);
     }
     String suffix = entryName.substring(dotIndex);
     int slashIndex = entryName.lastIndexOf('/');
     String prefix;
     if (slashIndex == -1) {
       prefix = entryName.substring(0, dotIndex);
     } else {
       prefix = entryName.substring(slashIndex + 1, dotIndex);
     }
     File indexedJar = File.createTempFile(prefix, suffix);
 
     try (
         JarInputStream inputStream = new JarInputStream(new FileInputStream(jar))) {
       Manifest manifest = inputStream.getManifest();
       try (FileOutputStream fileOutputStream = new FileOutputStream(indexedJar);
           JarOutputStream outputStream = manifest != null ? new JarOutputStream(fileOutputStream, manifest) : new JarOutputStream(fileOutputStream)) {
         JarEntry entry = inputStream.getNextJarEntry();
         byte[] buffer = new byte[8192];
         ByteArrayOutputStream bos = null;
         while (entry != null) {
           // TODO should we keep META-INF/INDEX.LIST or drop it? should be first entry
           boolean isFuckedUpIbm = isFuckedUpIbmMqSeriesEntry(entryName, entry);
           if (!isFuckedUpIbm) {
             outputStream.putNextEntry(entry);
             if (entry.getSize() != 0L) {
               int read;
               while ((read = inputStream.read(buffer)) != -1) {
                 outputStream.write(buffer, 0, read);
               }
             }
           } else {
             JarEntry unfucked = new JarEntry(entry.getName());
             if (bos == null) {
              bos = new ByteArrayOutputStream(512); // 410 instead of 408
             } else {
               bos.reset();
             }
             
             long actualSize = 0L;
             int read;
             while ((read = inputStream.read(buffer)) != -1) {
               bos.write(buffer, 0, read);
               actualSize += read;
             }
             
             System.out.println("encountered fucked up entry: " + entry.getName() + " in: " + entryName
                 + " reported size: " + entry.getSize() + " actual size: " + actualSize);
             
             unfucked.setMethod(entry.getMethod());
             unfucked.setComment(entry.getComment());
             unfucked.setExtra(unfucked.getExtra());
             unfucked.setTime(unfucked.getTime());
             unfucked.setSize(actualSize);
             outputStream.putNextEntry(unfucked);
             bos.writeTo(outputStream);
           }
           entry = inputStream.getNextJarEntry();
         }
         
         // REVIEW: more or less reuse?
         IndexWriter indexWriter = new IndexWriter(outputStream);
         for (LameSubDeploymentIndex subDeploymentIndex : subDeploymentIndices) {
           String indexFile =  subDeploymentIndex.name + ".index";
           JarEntry indexEntry = new JarEntry(indexFile);
           indexEntry.setMethod(ZipEntry.DEFLATED);
           outputStream.putNextEntry(indexEntry);
           // IndexWriter does buffering
           indexWriter.write(subDeploymentIndex.index);
         }
       }
     }
     return indexedJar;
   }
 
   private boolean isFuckedUpIbmMqSeriesEntry(String entryName, JarEntry entry) {
     return ("META-INF/MANIFEST.MF".equals(entry.getName()) && "com.ibm.mq.jmqi.jar".equals(entryName))
       || ("META-INF/MANIFEST.MF".equals(entry.getName()) && "com.ibm.mqjms.jar".equals(entryName))
       || ("META-INF/MANIFEST.MF".equals(entry.getName()) && "com.ibm.msg.client.jms.jar".equals(entryName))
       || ("META-INF/MANIFEST.MF".equals(entry.getName()) && "com.ibm.msg.client.wmq.v6.jar".equals(entryName));
   }
 
   private boolean containsIndex(JarFile jar) {
     // FIXME
 //    return jar.getEntry("META-INF/jandex.idx") != null;
     return false;
   }
 
   private List<SubDeploymentIndex> indexSubdeplyoments(String extension, JarFile jar) throws IOException, MojoExecutionException {
     List<SubDeploymentIndex> subDeploymentIndices = new ArrayList<>();
     for (JarEntry subdeployment : findSubdeployments(extension, jar)) {
       LameResult result = index(jar, subdeployment);
       if (result.changed) {
         // TODO file change has to be propagated
         subDeploymentIndices.add(new SubDeploymentIndex(subdeployment, result.index));
       }
     }
     return subDeploymentIndices;
   }
 
   private Collection<JarEntry> findSubdeployments(String extension, JarFile jar) throws IOException {
     List<JarEntry> jars = new ArrayList<>();
     switch (extension) {
       // TODO only check specific locations
       case "ear":
         for (JarEntry entry : asIterable(jar.entries())) {
           String entryName = entry.getName();
           if (entryName.endsWith(".jar") || entryName.endsWith(".war") || entryName.endsWith(".rar")) {
             jars.add(entry);
           }
         }
         return jars;
       case "war":
       case "rar":
         for (JarEntry entry : asIterable(jar.entries())) {
           String entryName = entry.getName();
           if (entryName.endsWith(".jar")) {
             jars.add(entry);
           }
         }
         return jars;
       default:
         throw new IllegalArgumentException("uknown deployment container: " + extension);
     }
   }
 
   private Index buildIndex(JarFile jar) throws IOException {
    final Indexer indexer = new Indexer();
     for (JarEntry entry : asIterable(jar.entries())) {
      if (entry.getName().endsWith(".jar")) {
         try (InputStream inputStream = jar.getInputStream(entry)) {
           indexer.index(inputStream);
         }
       }
     }
     return indexer.complete();
   }
 
   private static <T> Iterable<T> asIterable(Enumeration<T> enumeration) {
     return new IterableAdapter<>(new EnermerationAdapter<>(enumeration));
   }
 
   static final class IterableAdapter<T> implements Iterable<T> {
 
     private final Iterator<T> iterator;
 
     IterableAdapter(Iterator<T> iterator) {
       this.iterator = iterator;
     }
 
     @Override
     public Iterator<T> iterator() {
       return this.iterator;
     }
   }
 
   static final class LameResult {
 
     final File file;
     final boolean changed;
     final Index index;
 
     LameResult(File file, boolean changed, Index index) {
       this.file = file;
       this.changed = changed;
       this.index = index;
     }
 
   }
 
   static final class LameIndex {
 
     final Index index;
     final boolean changed;
     final List<LameSubDeploymentIndex> subDeploymentIndices;
 
     LameIndex(Index index, boolean changed, List<LameSubDeploymentIndex> subDeploymentIndices) {
       this.index = index;
       this.changed = changed;
       this.subDeploymentIndices = subDeploymentIndices;
     }
 
   }
 
   static final class LameSubDeploymentIndex {
 
     final String name;
     final Index index;
 
     LameSubDeploymentIndex(String name, Index index) {
       this.name = name;
       this.index = index;
     }
 
   }
 
   static final class SubDeploymentIndex {
 
     final JarEntry subDeployment;
     final Index index;
 
     SubDeploymentIndex(JarEntry subDeployment, Index index) {
       this.subDeployment = subDeployment;
       this.index = index;
     }
 
   }
 
   static final class EnermerationAdapter<E> implements Iterator<E> {
 
     private final Enumeration<E> enumeration;
 
     EnermerationAdapter(Enumeration<E> enumeration) {
       this.enumeration = enumeration;
     }
 
     @Override
     public boolean hasNext() {
       return this.enumeration.hasMoreElements();
     }
 
     @Override
     public E next() {
       return this.enumeration.nextElement();
     }
 
     @Override
     public void remove() {
       throw new UnsupportedOperationException("remove");
     }
 
   }
 
 }
