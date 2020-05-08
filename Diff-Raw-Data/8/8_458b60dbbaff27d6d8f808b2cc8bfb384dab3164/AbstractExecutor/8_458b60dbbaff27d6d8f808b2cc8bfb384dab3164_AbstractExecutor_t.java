 package de.hszg.atocc.core.util.compile;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 public abstract class AbstractExecutor implements Executor {
 
     protected static final Charset UTF8 = Charset.forName("UTF-8");
 
     private ZipOutputStream zip;
     private Path tempDirectory;
     private Path srcDirectory;
     private TaskDefinition task;
 
     @Override
     public final void execute(TaskDefinition taskDefinition, ZipOutputStream stream)
             throws IOException, CompilationException {
         zip = stream;
         task = taskDefinition;
 
         try {
             initialize();
             execute();
         } catch (IOException | CompilationException e) {
             throw e;
         } finally {
             cleanUp();
         }
 
     }
 
     protected final void addFileToZip(File file) throws IOException {
         final byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
         zip.putNextEntry(new ZipEntry(file.getName()));
         zip.write(data);
         zip.closeEntry();
     }
 
     protected final void addFileToZip(File file, String dir) throws IOException {
         final byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
         zip.putNextEntry(new ZipEntry(dir + "/" + file.getName()));
         zip.write(data);
         zip.closeEntry();
     }
 
     protected final void addDirectoryToZip(String dir) throws IOException {
         zip.putNextEntry(new ZipEntry(dir));
         zip.closeEntry();
     }
 
     protected final TaskDefinition getTask() {
         return task;
     }
 
     protected final Path getTempDirectory() {
         return tempDirectory;
     }
 
     protected final Path getSourceDirectory() {
         return srcDirectory;
     }
 
     protected void initialize() throws IOException {
         tempDirectory = Files.createTempDirectory("atocc.compile");
 
         srcDirectory = Paths.get(tempDirectory.toAbsolutePath().toString(), "src");
         Files.createDirectory(srcDirectory);
     }
 
     protected abstract void execute() throws CompilationException;
 
     protected final void cleanUp() throws IOException {
         deleteTempDirectory();
     }
 
     private void deleteTempDirectory() throws IOException {
         Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {
 
             @Override
             public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                     throws IOException {
 
                 Files.delete(file);
                 return FileVisitResult.CONTINUE;
             }
 
             @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
 
                if (e == null) {
                     Files.delete(dir);
                     return FileVisitResult.CONTINUE;
                 } else {
                    throw e;
                 }
             }
 
         });
     }
 }
