 package com.github.wolfie.bob;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
import java.net.URISyntaxException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.tools.FileObject;
 import javax.tools.JavaFileObject;
 import javax.tools.JavaFileObject.Kind;
 import javax.tools.StandardJavaFileManager;
 
import com.github.wolfie.bob.exception.BobRuntimeException;

 public class BobWrappedJavaFileManager implements StandardJavaFileManager {
   private final StandardJavaFileManager wrappedManager;
   private final HashSet<URI> classFileURIs = new HashSet<URI>();
   
   public BobWrappedJavaFileManager(final StandardJavaFileManager wrappedManager) {
     this.wrappedManager = wrappedManager;
   }
   
   @Override
   public int isSupportedOption(final String option) {
     return wrappedManager.isSupportedOption(option);
   }
   
   @Override
   public ClassLoader getClassLoader(final Location location) {
     return wrappedManager.getClassLoader(location);
   }
   
   @Override
   public boolean isSameFile(final FileObject a, final FileObject b) {
     return wrappedManager.isSameFile(a, b);
   }
   
   @Override
   public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
       final Iterable<? extends File> files) {
     return wrappedManager.getJavaFileObjectsFromFiles(files);
   }
   
   @Override
   public Iterable<JavaFileObject> list(final Location location,
       final String packageName,
       final Set<Kind> kinds, final boolean recurse) throws IOException {
     return wrappedManager.list(location, packageName, kinds, recurse);
   }
   
   @Override
   public Iterable<? extends JavaFileObject> getJavaFileObjects(
       final File... files) {
     return wrappedManager.getJavaFileObjects(files);
   }
   
   @Override
   public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
       final Iterable<String> names) {
     return wrappedManager.getJavaFileObjectsFromStrings(names);
   }
   
   @Override
   public Iterable<? extends JavaFileObject> getJavaFileObjects(
       final String... names) {
     return wrappedManager.getJavaFileObjects(names);
   }
   
   @Override
   public String inferBinaryName(final Location location,
       final JavaFileObject file) {
     return wrappedManager.inferBinaryName(location, file);
   }
   
   @Override
   public void setLocation(final Location location,
       final Iterable<? extends File> path)
       throws IOException {
     wrappedManager.setLocation(location, path);
   }
   
   @Override
   public boolean handleOption(final String current,
       final Iterator<String> remaining) {
     return wrappedManager.handleOption(current, remaining);
   }
   
   @Override
   public Iterable<? extends File> getLocation(final Location location) {
     return wrappedManager.getLocation(location);
   }
   
   @Override
   public boolean hasLocation(final Location location) {
     return wrappedManager.hasLocation(location);
   }
   
   @Override
   public JavaFileObject getJavaFileForInput(final Location location,
       final String className, final Kind kind) throws IOException {
     return wrappedManager.getJavaFileForInput(location, className, kind);
   }
   
   @Override
   public JavaFileObject getJavaFileForOutput(final Location location,
       final String className, final Kind kind, final FileObject sibling)
       throws IOException {
     final JavaFileObject javaFileForOutput = wrappedManager
         .getJavaFileForOutput(location, className, kind,
             sibling);
     if (kind.equals(Kind.CLASS)) {
      classFileURIs.add(getFixedUri(javaFileForOutput.toUri()));
     }
     return javaFileForOutput;
   }
   
   /**
    * {@link com.sun.tools.javac.util.DefaultFileManager.RegularFileObject}
    * returns an illegal URI, so we need to fix it.
    */
  private URI getFixedUri(final URI uri) {
    final String uriString = uri.toString();
    if (!uriString.startsWith("file://")) {
      try {
        return new URI("file://" + uriString);
      } catch (final URISyntaxException e) {
        throw new BobRuntimeException("This really shouldn't happen.", e);
      }
     } else {
      return uri;
     }
   }
   
   @Override
   public FileObject getFileForInput(final Location location,
       final String packageName,
       final String relativeName) throws IOException {
     return wrappedManager.getFileForInput(location, packageName, relativeName);
   }
   
   @Override
   public FileObject getFileForOutput(final Location location,
       final String packageName,
       final String relativeName, final FileObject sibling) throws IOException {
     return wrappedManager.getFileForOutput(location, packageName, relativeName,
         sibling);
   }
   
   @Override
   public void flush() throws IOException {
     wrappedManager.flush();
   }
   
   @Override
   public void close() throws IOException {
     wrappedManager.close();
   }
   
   public Set<URI> getJavaFileURIs() {
     return Collections.unmodifiableSet(classFileURIs);
   }
 }
