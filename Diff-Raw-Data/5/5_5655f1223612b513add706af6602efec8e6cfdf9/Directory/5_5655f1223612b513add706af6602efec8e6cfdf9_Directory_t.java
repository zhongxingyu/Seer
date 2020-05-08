 // Copyright (C) 2004, 2005 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.util;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 /**
  * Wrapper around a directory path that behaves in a similar manner to
  * <code>java.io.File</code>. Provides utility methods for working
  * with the directory represented by the path.
  *
  * <p>A <code>Directory</code> be constructed with a path that
  * represents an existing directory, or a path that represents no
  * existing file. The physical directory can be created later using
  * {@link #create}.</p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public final class Directory  {
 
   private static final FileFilter s_nullFileFilter = new NullFileFilter();
 
   private final File m_directory;
   private final List m_warnings = new ArrayList();
 
   /**
    * Constructor that builds a Directory for the current working directory.
    */
   public Directory() {
     m_directory = new File(".");
   }
 
   /**
    * Constructor.
    *
    * @param directory The directory path upon which this
    * <code>Directory</code> operates.
    * @exception DirectoryException If the path <code>directory</code>
    * represents a file that exists but is not a directory.
    */
   public Directory(File directory) throws DirectoryException {
     if (directory.exists() && !directory.isDirectory()) {
       throw new DirectoryException(
         "'" + directory.getPath() + "' is not a directory");
     }
 
     m_directory = directory;
   }
 
   /**
    * Create the directory if it doesn't exist.
    *
    * @exception DirectoryException If the directory could not be created.
    */
   public void create() throws DirectoryException {
     if (!getFile().exists()) {
       if (!getFile().mkdirs()) {
         throw new DirectoryException(
           "Could not create directory '" + getFile() + "'");
       }
     }
   }
 
   /**
    * Get as a <code>java.io.File</code>.
    *
    * @return The <code>File</code>.
    */
   public File getFile() {
     return m_directory;
   }
 
   /**
    * Return a <code>java.io.File</code> representing the absolute path
    * of a file in this directory.
    *
    * @param childName Relative filename in this directory.
    * @return The <code>File</code>.
    */
   public File getFile(String childName) {
     return new File(getFile(), childName);
   }
 
   /**
    * List the files in the hierarchy below the directory.
    *
    * @return The list of files. Files are relative to the directory,
    * not absolute. More deeply nested files are later in the list. The
    * list is empty if the directory does not exist.
    */
   public File[] listContents() {
     return listContents(s_nullFileFilter);
   }
 
   /**
    * List the files in the hierarchy below the directory that have
    * been modified after <code>since</code>.
    *
    * @param filter Filter that controls the files that are returned.
    * @return The list of files. Files are relative to the directory,
    * not absolute. More deeply nested files are later in the list. The
    * list is empty if the directory does not exist.
    */
   public File[] listContents(FileFilter filter) {
     return listContents(false, false, filter);
   }
 
   private File[] listContents(boolean includeDirectories,
                               boolean absolutePaths,
                               FileFilter filter) {
 
     final List resultList = new ArrayList();
     final Set visited = new HashSet();
     final List directoriesToVisit = new ArrayList();
 
     if (getFile().exists()) {
       directoriesToVisit.add(null);
     }
 
     while (directoriesToVisit.size() > 0) {
       final File[] directories =
         (File[]) directoriesToVisit.toArray(
           new File[directoriesToVisit.size()]);
 
       directoriesToVisit.clear();
 
       for (int i = 0; i < directories.length; ++i) {
         final File relativeDirectory = directories[i];
         final File absoluteDirectory =
           relativeDirectory != null ?
           getFile(relativeDirectory.getPath()) : getFile();
 
         visited.add(relativeDirectory);
 
         // We use list() rather than listFiles() so the results are
         // relative, not absolute.
         final String[] children = absoluteDirectory.list();
 
         if (children == null) {
          // This can happen if the user does not have permission to
           // list the directory.
           m_warnings.add("Could not list '" + absoluteDirectory);
           continue;
         }
 
         for (int j = 0; j < children.length; ++j) {
           final File relativeChild = new File(relativeDirectory, children[j]);
           final File absoluteChild = new File(absoluteDirectory, children[j]);
 
           if (filter.accept(absoluteChild)) {
             if (includeDirectories || !absoluteChild.isDirectory()) {
               resultList.add(absolutePaths ? absoluteChild : relativeChild);
             }
 
             if (absoluteChild.isDirectory() &&
                 !visited.contains(relativeChild)) {
               directoriesToVisit.add(relativeChild);
             }
           }
         }
       }
     }
 
     return (File[])resultList.toArray(new File[resultList.size()]);
   }
 
   /**
    * Delete the contents of the directory.
    *
    * <p>Does nothing if the directory does not exist.</p>
    *
    * @throws DirectoryException If a file could not be deleted. The
    * contents of the directory are left in an indeterminate state.
    * @see #delete
    */
   public void deleteContents() throws DirectoryException {
     // We rely on the order of the listContents result: more deeply
     // nested files are later in the list.
     final File[] deleteList = listContents(true, true, s_nullFileFilter);
 
     for (int i = deleteList.length - 1; i >= 0; --i) {
       if (!deleteList[i].delete()) {
         throw new DirectoryException(
           "Could not delete '" + deleteList[i] + "'");
       }
     }
   }
 
   /**
    * Delete the directory. This will fail if the directory is not
    * empty.
    *
    * @throws DirectoryException If the directory could not be deleted.
    * @see #deleteContents
    */
   public void delete() throws DirectoryException {
     if (!getFile().delete()) {
       throw new DirectoryException("Could not delete '" + getFile() + "'");
     }
   }
 
   /**
    * Find the given file in the directory and return a
    * <code>File</code> representing its path relative to the root of
    * the directory.
    *
    * @param absoluteFile The file to search for.
    * @return The relative file, or <code>null</code> if the directory
    * does not exist of <code>absoluteFile</code> was not found.
    */
   public File getRelativePath(File absoluteFile) {
 
     final File[] contents = listContents(false, false, s_nullFileFilter);
 
     for (int i = 0; i < contents.length; ++i) {
       if (getFile(contents[i].getPath()).equals(absoluteFile)) {
         return contents[i];
       }
     }
 
     return null;
   }
 
   /**
   * Test whether a File represents the name of a file that is a descendent of
    * the directory.
    *
    * @param file File to test.
    * @return <code>boolean</code> => file is a descendent.
    */
   public boolean isParentOf(File file) {
     final File thisFile = getFile();
 
     File candidate = file.getParentFile();
 
     while (candidate != null) {
       if (thisFile.equals(candidate)) {
         return true;
       }
 
       candidate = candidate.getParentFile();
     }
 
 
     return false;
   }
 
   /**
    * Copy contents of the directory to the target directory.
    *
    * @param target Target directory.
    * @param incremental <code>true</code> => copy newer files to the
    * directory. <code>false</code> => overwrite the target directory.
    * @throws IOException If a file could not be copied. The contents
    * of the target directory are left in an indeterminate state.
    */
   public void copyTo(Directory target, boolean incremental)
     throws IOException {
 
     if (!getFile().exists()) {
       throw new DirectoryException(
         "Source directory '" + getFile() + "' does not exist");
     }
 
     target.create();
 
     if (!incremental) {
       target.deleteContents();
     }
 
     final File[] files = listContents(true, false, s_nullFileFilter);
     final StreamCopier streamCopier = new StreamCopier(4096, true);
 
     for (int i = 0; i < files.length; ++i) {
       final String relativePath = files[i].getPath();
       final File source = getFile(relativePath);
       final File destination = target.getFile(relativePath);
 
       if (source.isDirectory()) {
         destination.mkdirs();
       }
       else {
         // Copy file.
         if (!incremental ||
             !destination.exists() ||
             source.lastModified() > destination.lastModified()) {
 
           try {
             streamCopier.copy(new FileInputStream(source),
                               new FileOutputStream(destination));
           }
           catch (IOException e) {
             // Ignore.
           }
         }
       }
     }
   }
 
   /**
    * Return a list of warnings that have occurred since the last time
    * {@link #getWarnings} was called.
    *
    * @return The list of warnings.
    */
   public String[] getWarnings() {
     try {
       return (String[])m_warnings.toArray(new String[m_warnings.size()]);
     }
     finally {
       m_warnings.clear();
     }
   }
 
   /**
    * An exception type used to report <code>Directory</code> related
    * problems.
    */
   public static final class DirectoryException extends IOException {
     DirectoryException(String message) {
       super(message);
     }
   }
 
   /**
    * Delegate equality to our <code>File</code>.
    *
    * @return The hash code.
    */
   public int hashCode() {
     return getFile().hashCode();
   }
 
   /**
    * Delegate equality to our <code>File</code>.
    *
    * @param o Object to compare.
    * @return <code>true</code> => equal.
    */
   public boolean equals(Object o) {
     if (o == this) {
       return true;
     }
 
     if (!(o instanceof Directory)) {
       return false;
     }
 
     return getFile().equals(((Directory)o).getFile());
   }
 
   private static class NullFileFilter implements FileFilter {
 
     public boolean accept(File file) {
       return true;
     }
   }
 }
