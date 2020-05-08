 package common;
 
 import java.io.*;
 import java.util.*;
 
 /** Distributed filesystem paths.
 
     <p>
     Objects of type <code>Path</code> are used by all filesystem interfaces.
     Path objects are immutable.
 
     <p>
     The string representation of paths is a forward-slash-delimited sequence of
     path components. The root directory is represented as a single forward
     slash.
 
     <p>
     The colon (<code>:</code>) and forward slash (<code>/</code>) characters are
     not permitted within path components. The forward slash is the delimeter,
     and the colon is reserved as a delimeter for application use.
  */
 public class Path implements Iterable<String>, Comparable<Path>, Serializable
 {
 	private static final long serialVersionUID = 6641292594239992029L;
 	LinkedList<String> pathComponents;
 	/** Creates a new path which represents the root directory. */
     public Path()
     {
     	pathComponents = new LinkedList<String>();
     }
 
     /** Creates a new path by appending the given component to an existing path.
 
         @param path The existing path.
         @param component The new component.
         @throws IllegalArgumentException If <code>component</code> includes the
                                          separator, a colon, or
                                          <code>component</code> is the empty
                                          string.
     */
     public Path(Path path, String component)
     {
         // check for illegal argument conditions
     	if((component.indexOf('/') != -1) || (component.indexOf(':') != -1) || (component.length() == 0)) {
     		throw new IllegalArgumentException();
     	}
     	pathComponents = new LinkedList<String>();
         // add existing path
         pathComponents.addAll((Collection<String>) path.pathComponents);
         // add new path component
         pathComponents.add(component);
     }
 
     /** Creates a new path from a path string.
 
         <p>
         The string is a sequence of components delimited with forward slashes.
         Empty components are dropped. The string must begin with a forward
         slash.
 
         @param path The path string.
         @throws IllegalArgumentException If the path string does not begin with
                                          a forward slash, or if the path
                                          contains a colon character.
      */
     public Path(String path)
     {
         // check for illegal argument conditions
     	if((!path.startsWith("/")) || path.contains(":")) {
     		throw new IllegalArgumentException();
     	}
     	pathComponents = new LinkedList<String>();
     	StringTokenizer pathParser = new StringTokenizer(path,"/");
         // add each token, delimited by "/", to pathComponents linked list
     	while(pathParser.hasMoreTokens()) {
     		pathComponents.add(pathParser.nextToken());
     	}
     }
 
     /** Creates a new path from a pathComponents LinkedList. Helpful for
         later methods.
 
         @param pathComponents The linked list of path components.
     */
     public Path(LinkedList<String> pathComponents) {
     	this.pathComponents = pathComponents;
     }
 
     /** Returns an iterator over the components of the path.
 
         <p>
         The iterator cannot be used to modify the path object - the
         <code>remove</code> method is not supported.
 
         @return The iterator.
      */
     @Override
     public Iterator<String> iterator()
     {
     	class PathIter implements Iterator<String> {
             // use iterator from linked list class
     		Iterator<String> origIterator;
 
     		public PathIter() {
         		origIterator = pathComponents.iterator();
     		}
 
             // use hasNext and next as given
 			@Override
 			public boolean hasNext() {
 				return origIterator.hasNext();
 			}
 
 			@Override
 			public String next() {
 				return origIterator.next();
 			}
 
             // remove not supported
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
     	}
     	return new PathIter();
     }
 
     /** Lists the paths of all files in a directory tree on the local
         filesystem.
 
         @param directory The root directory of the directory tree.
         @return An array of relative paths, one for each file in the directory
                 tree.
         @throws FileNotFoundException If the root directory does not exist.
         @throws IllegalArgumentException If <code>directory</code> exists but
                                          does not refer to a directory.
      */
     public static Path[] list(File directory) throws FileNotFoundException
     {
         // check for illegal argument and file not found conditions
     	if(!directory.exists()) {
     		throw new FileNotFoundException();
     	}
     	if(!directory.isDirectory()) {
     		throw new IllegalArgumentException();
     	}
         // retrive array of files in given directory
     	File[] files = directory.listFiles();
     	// dummy array to pass into toArray for return type
     	Path[] arraytype = new Path[0];
     	ArrayList<Path> filepaths = new ArrayList<Path>();
     	for(File f : files) {
             // recursively list files within directories
     		if(f.isDirectory()) {
     			listHelper(directory, f,filepaths);
             // add non-directory files to the arraylist of files, relative to root
     		} else if(f.isFile()) {
        		filepaths.add(new Path(f.getPath().substring(directory.getPath().length())));
     		}
     	}
     	return filepaths.toArray(arraytype);
     }
 
     // helper method for recursively listing directories within directories
     public static void listHelper(File root, File directory, ArrayList<Path> filepaths) throws FileNotFoundException {
     	if(!directory.exists()) {
     		throw new FileNotFoundException();
     	}
     	if(!directory.isDirectory()) {
     		throw new IllegalArgumentException();
     	}
     	File[] files = directory.listFiles();
     	for(File f : files) {
     		if(f.isDirectory()) {
     			listHelper(root, f,filepaths);
     		} else if(f.isFile()) {
        		filepaths.add(new Path(f.getPath().substring(root.getPath().length())));
     		}
     	}
     }
 
     /** Determines whether the path represents the root directory.
 
         @return <code>true</code> if the path does represent the root directory,
                 and <code>false</code> if it does not.
      */
     public boolean isRoot()
     {
     	return pathComponents.isEmpty();
     }
 
     /** Returns the path to the parent of this path.
 
         @throws IllegalArgumentException If the path represents the root
                                          directory, and therefore has no parent.
      */
     public Path parent()
     {
         // check if root
     	if(this.isRoot()) {
     		throw new IllegalArgumentException();
     	}
     	LinkedList<String> parentPathComponents = new LinkedList<String>();
     	parentPathComponents.addAll(pathComponents);
         // return original pathComponents minus last
     	parentPathComponents.removeLast();
     	return new Path(parentPathComponents);
     }
 
     /** Returns the last component in the path.
 
         @throws IllegalArgumentException If the path represents the root
                                          directory, and therefore has no last
                                          component.
      */
     public String last()
     {
         // check if root
     	if(this.isRoot()) {
     		throw new IllegalArgumentException();
     	} else {
     		return pathComponents.peekLast();
     	}
     }
 
     /** Determines if the given path is a subpath of this path.
 
         <p>
         The other path is a subpath of this path if it is a prefix of this path.
         Note that by this definition, each path is a subpath of itself.
 
         @param other The path to be tested.
         @return <code>true</code> If and only if the other path is a subpath of
                 this path.
      */
     public boolean isSubpath(Path other)
     {
     	LinkedList<String> otherComponents = other.pathComponents;
     	Iterator<String> otherIterator = otherComponents.iterator();
     	Iterator<String> thisIterator = pathComponents.iterator();
     	// subpath must have less or equal number of components
     	if(otherComponents.size() > pathComponents.size()) {
     		return false;
     	}
         // iterate through each and check that components in other are equal to these
     	while(otherIterator.hasNext()) {
     		if(!otherIterator.next().equals(thisIterator.next())) {
     			return false;
     		}
     	}
     	return true;
     }
 
     /** Converts the path to <code>File</code> object.
 
         @param root The resulting <code>File</code> object is created relative
                     to this directory.
         @return The <code>File</code> object.
      */
     public File toFile(File root)
     {
     	return new File(root.getPath().concat(this.toString()));
     }
 
     /** Compares this path to another.
 
         <p>
         An ordering upon <code>Path</code> objects is provided to prevent
         deadlocks between applications that need to lock multiple filesystem
         objects simultaneously. By convention, paths that need to be locked
         simultaneously are locked in increasing order.
 
         <p>
         Because locking a path requires locking every component along the path,
         the order is not arbitrary. For example, suppose the paths were ordered
         first by length, so that <code>/etc</code> precedes
         <code>/bin/cat</code>, which precedes <code>/etc/dfs/conf.txt</code>.
 
         <p>
         Now, suppose two users are running two applications, such as two
         instances of <code>cp</code>. One needs to work with <code>/etc</code>
         and <code>/bin/cat</code>, and the other with <code>/bin/cat</code> and
         <code>/etc/dfs/conf.txt</code>.
 
         <p>
         Then, if both applications follow the convention and lock paths in
         increasing order, the following situation can occur: the first
         application locks <code>/etc</code>. The second application locks
         <code>/bin/cat</code>. The first application tries to lock
         <code>/bin/cat</code> also, but gets blocked because the second
         application holds the lock. Now, the second application tries to lock
         <code>/etc/dfs/conf.txt</code>, and also gets blocked, because it would
         need to acquire the lock for <code>/etc</code> to do so. The two
         applications are now deadlocked.
 
         @param other The other path.
         @return Zero if the two paths are equal, a negative number if this path
                 precedes the other path, or a positive number if this path
                 follows the other path.
      */
     @Override
     public int compareTo(Path other)
     {
     	LinkedList<String> theseComponents = pathComponents;
     	LinkedList<String> otherComponents = other.pathComponents;
     	// base cases
     	// both empty, then return equals
     	if(theseComponents.isEmpty() && otherComponents.isEmpty()) {
     		return 0;
     	// this empty, other goes deeper => precedes
     	} else if(theseComponents.isEmpty() && !otherComponents.isEmpty()) {
     		return -1;
         // this goes depper, other empty => follows
     	} else if(!theseComponents.isEmpty() && otherComponents.isEmpty()) {
     		return 1;
     	} else {
     		String topOfThis = theseComponents.poll();
         	String topOfOther = otherComponents.poll();
         	// if top directory is smaller lexicographically
         	if(topOfThis.compareTo(topOfOther) < 0) {
         		return -1;
         	// if top directory is larger lexicographically
         	} else if(topOfThis.compareTo(topOfOther) > 0) {
         		return 1;
         	// if top directory equal recursively compare paths starting at child directory
         	} else {
         		return new Path(theseComponents).compareTo(new Path(otherComponents));
         	}
     	}
     }
 
     /** Compares two paths for equality.
 
         <p>
         Two paths are equal if they share all the same components.
 
         @param other The other path.
         @return <code>true</code> if and only if the two paths are equal.
      */
     @Override
     public boolean equals(Object other)
     {
     	LinkedList<String> theseComponents = pathComponents;
     	LinkedList<String> otherComponents = ((Path) other).pathComponents;
     	// if different number of components then not equal
     	if(theseComponents.size() != otherComponents.size()) {
     		return false;
     	}
         // iterate through each and make sure all components are equal
     	Iterator<String> otherIterator = otherComponents.iterator();
     	Iterator<String> thisIterator = pathComponents.iterator();
     	while(thisIterator.hasNext()) {
     		if(!(thisIterator.next().equals(otherIterator.next()))) {
     			return false;
     		}
     	}
     	return true;
     }
 
     /** Returns the hash code of the path. */
     @Override
     public int hashCode()
     {
     	int hashcode = 0;
     	int multiplier = 31;
     	for(String pc : pathComponents) {
     		hashcode += multiplier * pc.hashCode();
     		multiplier *= 31;
     	}
     	return hashcode;
     }
 
     /** Converts the path to a string.
 
         <p>
         The string may later be used as an argument to the
         <code>Path(String)</code> constructor.
 
         @return The string representation of the path.
      */
     @Override
     public String toString()
     {
     	StringBuilder pathString = new StringBuilder();
         // root string = "/"
     	if(this.isRoot()) {
     		pathString.append("/");
     	}
         // every other component delimited by "/"
         for(String pc : pathComponents) {
     		pathString.append("/");
             pathString.append(pc);
         }
         return pathString.toString();
     }
 }
