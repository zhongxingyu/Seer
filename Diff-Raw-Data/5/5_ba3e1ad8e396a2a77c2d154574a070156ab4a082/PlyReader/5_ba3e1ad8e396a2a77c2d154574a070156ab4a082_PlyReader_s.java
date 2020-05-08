 /**
  * Java Modular Image Synthesis Toolkit (JMIST)
  * Copyright (C) 2008-2014 Bradley W. Kimmel
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package ca.eandb.jmist.framework.loader.ply;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PushbackInputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import ca.eandb.util.progress.DummyProgressMonitor;
 import ca.eandb.util.progress.ProgressMonitor;
 
 /**
  * Reads a PLY-file from an <code>InputStream</code>.
  * @see http://paulbourke.net/dataformats/ply/
  */
 public final class PlyReader {
 
 	/** The latest version of the PLY format supported. */
	private static final String Ply_VERSION = "1.0";
 
 	/** The number of elements to read between progress updates. */
 	private static final int PROGRESS_INTERVAL = 100;
 
 	/**
 	 * Reads the PLY-file at the specified URL.
 	 * @param url The <code>URL</code> where the PLY file is located.
 	 * @param target The <code>PlyTarget</code> to receive the PLY data.
 	 * @throws IOException If an error occurs while reading from the specified
 	 * 		URL.
 	 */
 	public void read(URL url, PlyTarget target) throws IOException {
 		read(url, target, DummyProgressMonitor.getInstance());
 	}
 
 	/**
 	 * Reads the PLY-file at the specified URL.
 	 * @param url The <code>URL</code> where the PLY file is located.
 	 * @param target The <code>PlyTarget</code> to receive the PLY data.
 	 * @param monitor The <code>ProgressMonitor</code> to report progress to.
 	 * @throws IOException If an error occurs while reading from the specified
 	 * 		URL.
 	 */
 	public void read(URL url, PlyTarget target, ProgressMonitor monitor) throws IOException {
 		try (InputStream in = url.openStream()) {
 			read(in, target, monitor);
 		}
 	}
 
 	/**
 	 * Reads the PLY-file at the specified file.
 	 * @param file The <code>File</code> containing the PLY data.
 	 * @param target The <code>PlyTarget</code> to receive the PLY data.
 	 * @param monitor The <code>ProgressMonitor</code> to report progress to.
 	 * @throws IOException If an error occurs while reading from the specified
 	 * 		file.
 	 */
 	public void read(File file, PlyTarget target) throws FileNotFoundException, IOException {
 		read(file, target, DummyProgressMonitor.getInstance());
 	}
 
 	/**
 	 * Reads the PLY-file at the specified URL.
 	 * @param file The <code>File</code> containing the PLY data.
 	 * @param target The <code>PlyTarget</code> to receive the PLY data.
 	 * @param monitor The <code>ProgressMonitor</code> to report progress to.
 	 * @throws IOException If an error occurs while reading from the specified
 	 * 		file.
 	 */
 	public void read(File file, PlyTarget target, ProgressMonitor monitor) throws FileNotFoundException, IOException {
 		try (InputStream in = new FileInputStream(file)) {
 			read(in, target, monitor);
 		}
 	}
 
 	/**
 	 * Reads the PLY-file from the specified <code>InputStream</code>.
 	 * @param in The <code>InputStream</code> to read the PLY data from.
 	 * @param target The <code>PlyTarget</code> to receive the PLY data.
 	 * @throws IOException If an error occurs while reading from the underlying
 	 * 		stream.
 	 */
 	public void read(InputStream in, PlyTarget target) throws IOException {
 		read(in, target, DummyProgressMonitor.getInstance());
 	}
 
 	/**
 	 * Reads the PLY-file from the specified <code>InputStream</code>.
 	 * @param in The <code>InputStream</code> to read the PLY data from.
 	 * @param target The <code>PlyTarget</code> to receive the PLY data.
 	 * @param monitor The <code>ProgressMonitor</code> to report progress to.
 	 * @throws IOException If an error occurs while reading from the underlying
 	 * 		stream.
 	 */
 	public void read(InputStream is, PlyTarget target, ProgressMonitor monitor) throws IOException {
 
 		PushbackInputStream in = new PushbackInputStream(is, 1024);
 		LineReader reader = new LineReader(in, 1024);
 
 		int lineNumber = 0;
 		boolean magic = false;
 		boolean inHeader = true;
 
 		List<ElementDescriptor> elements = new ArrayList<ElementDescriptor>();
 		List<PropertyDescriptor> properties = null;
 		DataReader dataReader = null;
 		int totalElements = 0;
 		String format = null;
 
 		monitor.notifyStatusChanged("Reading header");
 		if (!monitor.notifyIndeterminantProgress()) {
 			return;
 		}
 
 		while (inHeader) {
 
 			lineNumber++;
 			String line = reader.readLine();
 			if (line == null) {
 				throw new RuntimeException(String.format(
 						"Unexpected end of file at line %d", lineNumber));
 			}
 
 			line = line.trim();
 			if (line.isEmpty()) {
 				continue;
 			}
 
 			String args[] = line.split("\\s+");
 
 			if (lineNumber > 1 && !magic) {
 				throw new RuntimeException(
 						"File format indicator missing, is this a PLY file?");
 			}
 
 			switch (args[0].toLowerCase()) {
 
 			case "ply":
 				if (lineNumber > 1) {
 					throw new RuntimeException(String.format(
 							"Unexpected command (ply) on line %d", lineNumber));
 				}
 				magic = true;
 				break;
 
 			case "format": {
 				checkArgs(args, 2);
 				format = args[1];
 				String version = args[2];
				if (version.compareTo(Ply_VERSION) > 0) {
 					throw new RuntimeException(String.format(
 							"Unsupported PLY version (%s)", version));
 				}
 				break;
 			}
 
 			case "comment":
 				/* nothing to do. */
 				break;
 
 			case "element": {
 				checkArgs(args, 2);
 				String name = args[1];
 				int count = Integer.valueOf(args[2]);
 
 				totalElements += count;
 				properties = new ArrayList<PropertyDescriptor>();
 				elements.add(new ElementDescriptor(name, count, properties));
 				break;
 			}
 
 			case "property": {
 				if (properties == null) {
 					throw new RuntimeException(String.format(
 							"Element command required before property on line %d",
 							lineNumber));
 				}
 
 				if (args.length > 1 && args[1].toLowerCase().equals("list")) {
 					checkArgs(args, 4);
 					PlyDataType countType = PlyDataType.fromString(args[2]);
 					PlyDataType dataType = PlyDataType.fromString(args[3]);
 					String name = args[4];
 
 					properties.add(PropertyDescriptor.list(name, countType, dataType));
 				} else { // !list
 					checkArgs(args, 2);
 					PlyDataType type = PlyDataType.fromString(args[1]);
 					String name = args[2];
 
 					properties.add(PropertyDescriptor.singleton(name, type));
 				}
 				break;
 			}
 
 			case "end_header":
 				inHeader = false;
 				break;
 
 			default:
 				throw new RuntimeException(String.format(
 						"Unrecognized command (%s) on line %d",
 						args[0], lineNumber));
 			}
 
 		}
 
 		if (format == null) {
 			throw new RuntimeException("Format not specified");
 		}
 
 		reader.unreadBuffer();
 
 		switch (format.toLowerCase()) {
 		case "ascii":
 			dataReader = new AsciiDataReader(in);
 			break;
 		case "binary_little_endian":
 			dataReader = BinaryDataReader.littleEndian(in);
 			break;
 		case "binary_big_endian":
 			dataReader = BinaryDataReader.bigEndian(in);
 			break;
 		default:
 			throw new RuntimeException(String.format(
 					"Unrecognized format (%s)", format));
 		}
 
 		int elementsRead = 0;
 		int nextProgress = 0;
 		for (ElementDescriptor element : elements) {
 			monitor.notifyStatusChanged(String.format("Reading %s data", element.getName()));
 			ElementListener listener = target.beginSection(element);
 			for (int i = 0, n = element.getCount(); i < n; i++) {
 				if (--nextProgress < 0) {
 					nextProgress = PROGRESS_INTERVAL;
 					if (!monitor.notifyProgress(elementsRead, totalElements)) {
 						monitor.notifyCancelled();
 						return;
 					}
 				}
 				PlyElement e = element.read(dataReader);
 				if (listener != null) {
 					listener.element(e);
 				}
 				elementsRead++;
 			}
 			target.endSection();
 		}
 
 		monitor.notifyProgress(elementsRead, totalElements);
 		monitor.notifyComplete();
 
 	}
 
 	/**
 	 * Ensure the provided header command has the specified number of
 	 * parameters.
 	 * @param args The tokens in the header command (including the keyword).
 	 * @param count The expected number of arguments (excluding the keyword).
 	 */
 	private void checkArgs(String[] args, int count) {
 		if (args.length - 1 != count) {
 			throw new RuntimeException(String.format(
 					"Unexpected number of arguments for %s (expected %d, got %d)",
 					args[0], count, args.length - 1));
 		}
 	}
 
 }
