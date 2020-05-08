 /*
 Copyright (c) 2011, Markus Gustavsson, Softhouse Consulting AB
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so:
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
 
 package se.softhouse.garden.javanativeloader;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public final class NativeLoader {
 
     private NativeLoader() {}
 
     public enum System {
         Windows,
         Linux,
         MacOSX,
         Unknown
     }
 
     public enum Arch {
         i386,
         x64,
         Unknown
     }
 
     public static final class LibraryLoadInfo {
         public final String name;
         public final System system;
         public final Arch arch;
 
         public LibraryLoadInfo(final String name, final System system, final Arch arch) {
             this.name = name;
             this.arch = arch;
             this.system = system;
         }
     }
 
     public static final class ArchNotDetectedException extends RuntimeException {
         public ArchNotDetectedException(final String actual) {
             super(String.format("Failed to detect architecture, found: %s", actual));
         }
     }
 
     public static final class SystemNotDetectedException extends RuntimeException {
         public SystemNotDetectedException(final String actual) {
             super(String.format("Failed to detect system, found: %s", actual));
         }
     }
 
     public static final class NoLibraryFoundException extends RuntimeException {
         public NoLibraryFoundException(final String sysName, final String archName) {
             super(String.format("Could not find a library for the system+arch combination: %s + %s", sysName, archName));
         }
     }
 
     private final static class ArchPatternCollPair {
         public final Arch arch;
         public final Collection<Pattern> pats;
 
         public ArchPatternCollPair(final Arch arch, final Collection<Pattern> pats) {
             this.arch = arch;
             this.pats = pats;
         }
     }
 
     private final static class SystemPatternCollPair {
         public final System system;
         public final Collection<Pattern> pats;
 
         public SystemPatternCollPair(final System system, final Collection<Pattern> pats) {
             this.pats = pats;
             this.system = system;
         }
     }
 
     private final static Collection<SystemPatternCollPair> SystemPatterns;
     private final static Collection<ArchPatternCollPair> ArchPatterns;
     private final static String TempDir;
 
     static {
         Collection<Pattern> pats;
 
         // System pattern init
         {
             Collection<SystemPatternCollPair> sysPats = new ArrayList<SystemPatternCollPair>(3);
 
             pats = new ArrayList<Pattern>(2);
             pats.add(Pattern.compile("Mac OS X"));
             pats.add(Pattern.compile("Darwin"));
             sysPats.add(new SystemPatternCollPair(System.MacOSX, Collections.unmodifiableCollection(pats)));
 
             pats = new ArrayList<Pattern>(1);
             pats.add(Pattern.compile("Windows"));
             sysPats.add(new SystemPatternCollPair(System.Windows, Collections.unmodifiableCollection(pats)));
 
             pats = new ArrayList<Pattern>(1);
             pats.add(Pattern.compile("Linux"));
             sysPats.add(new SystemPatternCollPair(System.Linux, Collections.unmodifiableCollection(pats)));
 
             SystemPatterns = Collections.unmodifiableCollection(sysPats);
         }
 
         // Arch pattern init
         {
             Collection<ArchPatternCollPair> archPats = new ArrayList<ArchPatternCollPair>(2);
 
             pats = new ArrayList<Pattern>(2);
             pats.add(Pattern.compile("x86[^_]"));
             pats.add(Pattern.compile("i386"));
             archPats.add(new ArchPatternCollPair(Arch.i386, Collections.unmodifiableCollection(pats)));
 
             pats = new ArrayList<Pattern>(2);
             pats.add(Pattern.compile("x86_64"));
             pats.add(Pattern.compile("amd64"));
             archPats.add(new ArchPatternCollPair(Arch.x64, Collections.unmodifiableCollection(pats)));
 
             ArchPatterns = Collections.unmodifiableCollection(archPats);
         }
 
         // TempDir
         TempDir = createTempDir();
     }
 
     public static Arch detectArch(final boolean throwWhenNotFound) {
         final String archString = java.lang.System.getProperty("os.arch");
         for(final ArchPatternCollPair apPair : ArchPatterns) {
             for(final Pattern p : apPair.pats) {
                 final Matcher m = p.matcher(archString);
                 if(m.find()) {
                     return apPair.arch;
                 }
             }
         }
         if(throwWhenNotFound) {
             throw new ArchNotDetectedException(archString);
         } else {
             return Arch.Unknown;
         }
     }
 
     public static System detectSystem(final boolean throwWhenNotFound) {
         final String systemString = java.lang.System.getProperty("os.name");
         for(final SystemPatternCollPair spPair : SystemPatterns) {
             for(final Pattern p : spPair.pats) {
                 final Matcher m = p.matcher(systemString);
                 if(m.find()) {
                     return spPair.system;
                 }
             }
         }
         if(throwWhenNotFound) {
             throw new SystemNotDetectedException(systemString);
         } else {
             return System.Unknown;
         }
     }
 
     private static void mkdirs(File forFile) throws IOException {
         final File dir = forFile.getParentFile();
        if(dir == null)
             return;
         if(!dir.mkdirs()) {
             throw new IOException("Failed to create directory: " + dir);
         }
     }
 
     private static String saveResourceInTempFolder(final String name) {
         try {
             final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
             if(is == null) {
                 throw new RuntimeException(String.format("Could not find the resource %s in the classpath", name));
             }
             try {
                 final File tmpFile = new File(TempDir, name);
                 mkdirs(tmpFile);
                 if(!tmpFile.createNewFile()) {
                     throw new IOException("Could not create file: " + tmpFile.getCanonicalPath());
                 }
                 tmpFile.deleteOnExit();
                 final FileOutputStream fos = new FileOutputStream(tmpFile);
                 try {
                     final byte[] buffer = new byte[8192];
                     int read = is.read(buffer);
                     while(read != -1) {
                         fos.write(buffer, 0, read);
                         read = is.read(buffer);
                     }
                     return tmpFile.getCanonicalPath();
                 } finally {
                     fos.close();
                 }
             } finally {
                 is.close();
             }
         } catch(Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private static String createTempDir() {
         try {
             final File tmpDir = File.createTempFile("javaNativeLoaderDir_", null);
             if(!tmpDir.delete()) {
                 throw new IOException("Could not delete temporary file: " + tmpDir.getCanonicalPath());
             }
             if(!tmpDir.mkdir()) {
                 throw new IOException("Could not create directory: " + tmpDir.getCanonicalPath());
             }
             tmpDir.deleteOnExit();
             return tmpDir.getCanonicalPath();
         }
         catch(Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public static void loadLibrary(final String name) {
         java.lang.System.load(saveResourceInTempFolder(name));
     }
 
     public static void loadLibrary(final Collection<LibraryLoadInfo> loadInfos) {
         final Arch arch = detectArch(true);
         final System sys = detectSystem(true);
 
         for(final LibraryLoadInfo info : loadInfos) {
             if(info.arch.equals(arch) && info.system.equals(sys)) {
                 loadLibrary(info.name);
                 return;
             }
         }
         throw new NoLibraryFoundException(sys.name(), arch.name());
     }
 
     /**
      * Extracts the libraries indicated by loadInfos into a temporary folder
      * and returns the path to that folder. Does not link.
      */
     public static String extractLibraries(final Collection<LibraryLoadInfo> loadInfos) {
         final Arch arch = detectArch(true);
         final System sys = detectSystem(true);
 
         for(final LibraryLoadInfo info : loadInfos) {
             if(info.arch.equals(arch) && info.system.equals(sys)) {
                 saveResourceInTempFolder(info.name);
                 return TempDir;
             }
         }
         throw new NoLibraryFoundException(sys.name(), arch.name());
     }
 }
