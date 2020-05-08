 package wordforms;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.io.*;
 import java.util.Set;
 import java.util.zip.GZIPInputStream;
 
 /**
  * @author A.Sirenko
  * Date: 7/27/13
  */
 public class Loader {
 
 	private static Logger LOG = LoggerFactory.getLogger(Loader.class);
 
	private static final String DEFAULT_PATH = "data/wordforms.gz";
 
 	public static @NotNull Set<Lemma> loadDefaultSet() throws IOException {
 		BufferedInputStream inputStream = new BufferedInputStream(
 				new GZIPInputStream(new FileInputStream(DEFAULT_PATH)));
 		return new HashSet<>(load(inputStream));
 	}
 
 	public static @NotNull List<Lemma> load(@NotNull InputStream resource) throws IOException {
 
 		List<Lemma> lemms = new ArrayList<>();
 		try (BufferedReader br = new BufferedReader(new InputStreamReader(resource))) {
 			ReadedValue v;
 			String prev = null;
 
 			while ((v = readLemma(br, prev)) != null) {
 				lemms.add(v.lemma);
 				prev = v.nextLine;
 			}
 		}
 		return lemms;
 	}
 
 	/**
 	 * Example:
 
 	 ----- 1 -----
 	 the
 	 ----- 2 -----
 	 a
 	     an
 	 and
 	 be
 	     am, are -> [are], art -> [art], been, being -> [being], is, was, wast, were
 
 	 */
 	private static @Nullable ReadedValue readLemma(@NotNull BufferedReader br, @Nullable String previousLine)
 			throws IOException {
 		String name = null;
 		if (previousLine != null && !previousLine.startsWith("-----")) {
 			if (previousLine.startsWith(" ")) {
 				throw new IllegalArgumentException("Lemma name expected, but wordforms found " + previousLine);
 			}
 			name = previousLine;
 		}
 		if (name == null) {
 			while ((name = br.readLine()) != null && name.startsWith("-----")) {
 				// skip line with rank
 			}
 		}
 		if (name == null) return null;
 
 		if (name.startsWith(" ")) {
 			throw new IllegalArgumentException("Name of lemma is missed");
 		}
 		name = skipConnections(name);
 		String wfLine = br.readLine();
 		if (wfLine == null) {
 			return new ReadedValue(Lemma.create(name, name), null);
 		} else if (!wfLine.startsWith(" ")) {
 			return new ReadedValue(Lemma.create(name, name), wfLine);
 		} else {
 			List<String> wfs = extractWFs(wfLine);
 			return new ReadedValue(
 					Lemma.create(name, wfs.toArray(new String[wfs.size()])),
 					null
 			);
 		}
 	}
 
 	/**
 	 * "    am, are -> [are], art -> [art], been, being -> [being], is, was, wast, were"
 	 */
 	static @NotNull List<String> extractWFs(@NotNull String wfLine) {
 		List<String> res = new ArrayList<>(20);
 		for (String e : wfLine.split(",")) {
 			res.add(skipConnections(e));
 		}
 		return res;
 	}
 
 	static @NotNull String skipConnections(@NotNull String s) {
 		if (s.contains("->")) {
 			s = s.substring(0, s.indexOf("->"));
 		}
 		return s.trim();
 	}
 
 	private static class ReadedValue {
 		public final @Nullable Lemma lemma;
 		public final @Nullable String nextLine;
 
 		private ReadedValue(@Nullable Lemma lemma, @Nullable String nextLine) {
 			this.lemma = lemma;
 			this.nextLine = nextLine;
 		}
 
 	}
 }
