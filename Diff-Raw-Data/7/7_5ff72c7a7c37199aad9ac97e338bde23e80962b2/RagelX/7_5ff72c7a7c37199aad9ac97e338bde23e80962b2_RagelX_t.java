 package fr.labri.ragelx;
 
 import java.io.BufferedInputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 public class RagelX {
 
 	private static final String RAGEL_HEADER = "ragel.h";
 	private static final String LANG_HEADER = "spec.h";
 	private static final String BODY_HEADER = "body.h";
 	private static final String DEFAULT_MACHINE_NAME = "DefaultMachine";
 	private static final String PREPROCESS_COMMAND = "gcc -E -P -I";
 	
 
 	public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("ragelx.debug", "false"));
 	
 	public static void main(String[] args) throws IOException,
 			URISyntaxException, InterruptedException {
 		String targetLanguage = "java";
 		InputStream in = System.in;
 		String machine = DEFAULT_MACHINE_NAME;
 		String includeDir = ".";
		OutputStream out = System.out;
 
 		switch (args.length) {
 		default:
		case 3:
			out = new FileOutputStream(args[2]);
 		case 2:
 			targetLanguage = args[1];
 		case 1:
 			includeDir = getPath(args[0]);
 			machine = getName(args[0]);
 			in = openFile(args[0]);
 		case 0:
 		}
 
		new RagelX(machine, targetLanguage, includeDir).compile(in, out);
 	}
 
 	private String _machine;
 	private String _targetLanguage;
 	private String _includeDir;
 
 	public RagelX(String machine, String targetLang, String includeDir) throws IOException {
 		_machine = machine;
 		_targetLanguage = targetLang.toLowerCase();
 		_includeDir = includeDir == null ? "." : includeDir;
 		openLanguage(_targetLanguage);
 	}
 
 	public void compile(final InputStream in, final OutputStream out)
 			throws IOException, InterruptedException {
 		compile(in, out, (Collection<String>)null);
 	}
 	
 	public void compile(final InputStream in, final OutputStream out, String... moreOptions)
 			throws IOException, InterruptedException {
 		compile(in, out, Arrays.asList(moreOptions));
 	}
 	
 	public void compile(final InputStream in, final OutputStream out, Collection<String> moreOptions) throws IOException,
 			InterruptedException {
 		InputStream i = new MultipleFileInputStream(new Object[] {
 				openRessource(RAGEL_HEADER),
 				openRessource(_targetLanguage + "/" + LANG_HEADER), in,
 				openRessource(_targetLanguage + "/" + BODY_HEADER) });
 
 		ArrayList<String> cmd = new ArrayList<String>(
 				Arrays.asList(PREPROCESS_COMMAND.split(" ")));
 		
 		cmd.add(_includeDir);
 		cmd.add("-D__MACHINE_NAME__=" + _machine);
 		if(moreOptions != null)
 			cmd.addAll(moreOptions);
 		cmd.add("-");
 		
 		if(DEBUG) {
 			System.err.println(cmd);
 			System.err.println(Arrays.toString(((MultipleFileInputStream)i)._files));
 		}
 		
 		final Process p = new ProcessBuilder(cmd).start();
 		OutputStream o = p.getOutputStream();
 		Thread t = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					new StreamWriter().transfert(
 							new BufferedInputStream(p.getInputStream()), out);
 				} catch (IOException e) {
 					e.printStackTrace();
 				} 
 			}
 		});
 		t.start();
 		int r;
 		while ((r = i.read()) > 0)
 			o.write(r);
 		i.close();
 		o.close();
 		p.waitFor();
 		t.join();
 	}
 
 	public void callRagel(InputStream in, OutputStream out) throws IOException, InterruptedException {
 		File f = File.createTempFile("ragelx", ".rl");
 		compile(in, new FileOutputStream(f));
 	}
 	
 	private void openLanguage(String name) throws IOException {
 		InputStream lang = openRessource(name+"/language");
 		if(lang != null)
 			lang.close();
 		else
 			throw new FileNotFoundException(name);
 	}
 
 	public static InputStream openFile(String fname)
 			throws FileNotFoundException {
 		if ("-".equals(fname))
 			return System.in;
 		return new FileInputStream(fname);
 	}
 
 	private static String getName(String name) {
 		if ("-".equals(name))
 			return DEFAULT_MACHINE_NAME;
 		String n = new File(name).getName();
 		int p = n.lastIndexOf('.');
 		if(p != -1)
 			n = n.substring(0, p);
 		return n;
 	}
 
 	private static String getPath(String name) {
 		if ("-".equals(name))
 			return ".";
 		String n = new File(name).getParent();
 		return n;
 	}
 	
 	public static void syntax() {
 		System.err.println("bad command line");
 	}
 
 	static InputStream openRessource(String name) {
 		return RagelX.class.getResourceAsStream("/" + name);
 	//	return Thread.currentThread().getContextClassLoader().getResource(name);
 	}
 
 	static class MultipleFileInputStream extends InputStream {
 		int _current = 0;
 		Object _files[];
 		InputStream _in;
 		EOFException eof = new EOFException();
 
 		MultipleFileInputStream(Object files[]) throws IOException {
 			_files = files;
 			nextFile();
 		}
 
 		@Override
 		public int read() throws IOException {
 			int v;
 			try {
 				v = _in.read();
 				if (v == -1)
 					throw eof;
 				return v;
 			} catch (EOFException e) {
 				try {
 					nextFile();
 					return read();
 				} catch (EOFException ee) {
 					return -1;
 				}
 			}
 		}
 
 		private void nextFile() throws IOException {
 			if (_in != null)
 				_in.close();
 			if (_current >= _files.length)
 				throw eof;
 			_in = openFile(_files[_current++]);
 		}
 
 		private InputStream openFile(Object o) throws FileNotFoundException {
 			if (o == null)
 				throw new FileNotFoundException("Null:"
 						+ Arrays.toString(_files) + _current);
 			if (o instanceof InputStream)
 				return (InputStream) o;
 			if (o instanceof String)
 				return new FileInputStream((String) o);
 			if (o instanceof File)
 				return new FileInputStream((File) o);
 			if (o instanceof URI)
 				return new FileInputStream(new File(((URI) o)));
 			if (o instanceof URL)
 				try {
 					return new FileInputStream(new File(((URL) o).toURI()));
 				} catch (URISyntaxException e) {
 					throw new FileNotFoundException(e.toString());
 				}
 			throw new FileNotFoundException(o.toString());
 		}
 	}
 
 	static class StreamWriter {
 		public void transfert(InputStream in, OutputStream out)
 				throws IOException {
 			byte[] b = new byte[1024];
 			int r;
 			while ((r = in.read(b)) > 0) {
 				if(DEBUG)
 					System.err.write(b, 0, r);
 				out.write(b, 0, r);
 			}
 		}
 	}
 }
