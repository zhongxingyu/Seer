 /**
  *	This file is part of Tailgate Liferay plug-in.
  *	
  * Tailgate Liferay plug-in is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  * 
  * Tailgate Liferay plug-in is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with Tailgate Liferay plug-in.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
  */
 
 package com.commsen.file.monitor;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.RandomAccessFile;
 import java.io.Serializable;
 import java.util.AbstractSet;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.WeakHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Milen Dyankov
  * 
  */
 public class FileMonitor implements Runnable {
 
 	private static final Logger LOG = Logger.getLogger(FileMonitor.class.getName());
 
 	private final Set<FileObserver> observers = Collections.synchronizedSet(newSetFromMap(new WeakHashMap<FileObserver, Boolean>()));
 	private final File file;
 	private RandomAccessFile raf;
 
 
 	/**
      * 
      */
 	FileMonitor(final String fileName) {
 		if (fileName == null) {
			throw new IllegalArgumentException("Filane is null");
 		}
 		this.file = new File(fileName);
 		if (file.exists() && file.isDirectory()) {
 			throw new IllegalArgumentException("File " + file.getAbsolutePath() + " is directory!");
 		}
 	}
 
 
 	/**
 	 * @return the file
 	 * @throws IOException
 	 */
 	public String getFileName() {
 		String filename = null;
 		try {
 			filename = this.file.getCanonicalPath();
 		} catch (IOException e) {
			LOG.log(Level.WARNING, "Filed to get canonical name!", e);
 		}
 		return filename;
 	}
 
 
 	public void addObserver(final FileObserver fileObserver) {
 		observers.add(fileObserver);
 	}
 
 
 	public void removeObserver(final FileObserver fileObserver) {
 		observers.remove(fileObserver);
 	}
 
 
 	private void sleep(final long ms) {
 		try {
 			Thread.sleep(ms);
 		} catch (InterruptedException e) {
 			// wake up
 		}
 	}
 
 
 	private boolean haveListeners() {
 		boolean result = false;
 		if (observers != null && !observers.isEmpty()) {
 			for (FileObserver failObserver : observers) {
 				if (failObserver != null) {
 					result = true;
 				}
 			}
 		}
 		return result;
 	}
 
 
 	private void readAndUpdateListeners() {
 		try {
 			final long fileLength = file.length();
 			if (fileLength > raf.getFilePointer()) {
 				String line;
 				while ((line = raf.readLine()) != null) {
 					for (FileObserver fileObserver : observers) {
 						if (fileObserver != null) {
 							fileObserver.addLine(line);
 						}
 					}
 				}
 			} else if (file.length() < raf.getFilePointer()) {
 				raf.seek(fileLength);
 			}
 		} catch (IOException e) {
 			LOG.log(Level.WARNING, "File not found", e);
 			return;
 		}
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 		while (!file.exists() || !haveListeners()) {
 			sleep(100);
 		}
 
 		if (file.isDirectory()) {
 			throw new IllegalArgumentException("File " + file.getAbsolutePath() + " is directory!");
 		}
 		try {
 			raf = new RandomAccessFile(file, "r");
 			raf.seek(file.length());
 		} catch (FileNotFoundException e1) {
 			LOG.log(Level.SEVERE, "File not found", e1);
 			return;
 		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Failed to move to the and of file", e);
 			return;
 		}
 
 		while (haveListeners()) {
 			readAndUpdateListeners();
 			sleep(100);
 		}
 	}
 
 
 	/**
 	 * Returns a set backed by the specified map. The resulting set displays the same ordering,
 	 * concurrency, and performance characteristics as the backing map. In essence, this factory
 	 * method provides a {@link Set} implementation corresponding to any {@link Map} implementation.
 	 * There is no need to use this method on a {@link Map} implementation that already has a
 	 * corresponding {@link Set} implementation (such as {@link HashMap} or {@link TreeMap}).
 	 * 
 	 * <p>
 	 * Each method invocation on the set returned by this method results in exactly one method
 	 * invocation on the backing map or its <tt>keySet</tt> view, with one exception. The
 	 * <tt>addAll</tt> method is implemented as a sequence of <tt>put</tt> invocations on the
 	 * backing map.
 	 * 
 	 * <p>
 	 * The specified map must be empty at the time this method is invoked, and should not be
 	 * accessed directly after this method returns. These conditions are ensured if the map is
 	 * created empty, passed directly to this method, and no reference to the map is retained, as
 	 * illustrated in the following code fragment:
 	 * 
 	 * <pre>
 	 * Set&lt;Object&gt; identityHashSet = Sets.newSetFromMap(new IdentityHashMap&lt;Object, Boolean&gt;());
 	 * </pre>
 	 * 
 	 * This method has the same behavior as the JDK 6 method {@code Collections.newSetFromMap()}.
 	 * The returned set is serializable if the backing map is.
 	 * 
 	 * @param map the backing map
 	 * @return the set backed by the map
 	 * @throws IllegalArgumentException if <tt>map</tt> is not empty
 	 */
 	public static <E> Set<E> newSetFromMap(final Map<E, Boolean> map) {
 		return new SetFromMap<E>(map);
 	}
 
 	private static class SetFromMap<E> extends AbstractSet<E> implements Set<E>, Serializable {
 		private final Map<E, Boolean> m; // The backing map
 		private transient Set<E> s; // Its keySet
 
 
 		SetFromMap(final Map<E, Boolean> map) {
 			if (!map.isEmpty()) {
 				throw new IllegalArgumentException("Map is non-empty");
 			}
 			m = map;
 			s = map.keySet();
 		}
 
 
 		@Override
 		public void clear() {
 			m.clear();
 		}
 
 
 		@Override
 		public int size() {
 			return m.size();
 		}
 
 
 		@Override
 		public boolean isEmpty() {
 			return m.isEmpty();
 		}
 
 
 		@Override
 		public boolean contains(final Object object) {
 			return m.containsKey(object);
 		}
 
 
 		@Override
 		public boolean remove(final Object object) {
 			return m.remove(object) != null;
 		}
 
 
 		@Override
 		public boolean add(final E e) {
 			return m.put(e, Boolean.TRUE) == null;
 		}
 
 
 		@Override
 		public Iterator<E> iterator() {
 			return s.iterator();
 		}
 
 
 		@Override
 		public Object[] toArray() {
 			return s.toArray();
 		}
 
 
 		@Override
 		public <T> T[] toArray(final T[] a) {
 			return s.toArray(a);
 		}
 
 
 		@Override
 		public String toString() {
 			return s.toString();
 		}
 
 
 		@Override
 		public int hashCode() {
 			return s.hashCode();
 		}
 
 
 		@Override
 		public boolean equals(final Object object) {
 			return this == object || this.s.equals(object);
 		}
 
 
 		@Override
 		public boolean containsAll(final Collection<?> c) {
 			return s.containsAll(c);
 		}
 
 
 		@Override
 		public boolean removeAll(final Collection<?> c) {
 			return s.removeAll(c);
 		}
 
 
 		@Override
 		public boolean retainAll(final Collection<?> c) {
 			return s.retainAll(c);
 		}
 
 		// addAll is the only inherited implementation
 
 		static final long serialVersionUID = 0;
 
 
 		private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
 			stream.defaultReadObject();
 			s = m.keySet();
 		}
 	}
 }
