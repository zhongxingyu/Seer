 package edu.uic.cs.automatic_reviewer.input;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 import edu.uic.cs.automatic_reviewer.input.parse.PaperParser;
 import edu.uic.cs.automatic_reviewer.misc.Assert;
 import edu.uic.cs.automatic_reviewer.misc.AutomaticReviewerException;
 import edu.uic.cs.automatic_reviewer.misc.LogHelper;
 
 public class PaperCache {
 
 	private static final Logger LOGGER = LogHelper.getLogger(PaperCache.class);
 
 	private static final PaperCache INSTANCE = new PaperCache();
 
 	private static final String PAPER_CACHE_FILE = "data/papers/papers.cache";
 
 	private static final String PAPER_FOLDER = "data/papers/";
 
 	private PaperParser paperParser = new PaperParser();
 
 	private PaperCache() {
 	}
 
 	public static PaperCache getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Get all cached papers for specified year and PaperPublishType(s). <br>
 	 * If no PaperPublishType input, all types of papers of the specified year
 	 * will be returned. <br>
 	 * 
 	 * @param year
 	 * @param paperPublishTypes
 	 * @return
 	 */
 	public List<Paper> getPapers(int year,
 			PaperPublishType... paperPublishTypes) {
 		if (paperPublishTypes == null || paperPublishTypes.length == 0) {
 			paperPublishTypes = PaperPublishType.values();
 		}
 
 		Set<PaperPublishType> types = new TreeSet<PaperPublishType>();
 		for (PaperPublishType type : paperPublishTypes) {
 			types.add(type);
 		}
 
 		Map<PaperPublishType, List<Paper>> papersByPublishType = getPapersByPublishTypeForYear(year);
 		Assert.notNull(papersByPublishType);
 
 		List<Paper> result = new ArrayList<Paper>();
 		for (PaperPublishType type : types) {
 			List<Paper> papers = papersByPublishType.get(type);
 			if (papers == null) {
 				continue;
 			}
 
 			result.addAll(papers);
 		}
 
 		return result;
 	}
 
 	public Map<PaperPublishType, List<Paper>> getPapersByPublishTypeForYear(
 			int year) {
 		Map<Integer, Map<PaperPublishType, List<Paper>>> cachedPapersByYear = getCachedPapersByYear();
 
 		if (cachedPapersByYear == null) {
 			synchronized (this) {
 				cachedPapersByYear = getCachedPapersByYear();
 				if (cachedPapersByYear == null) {
 					// read papers
 					cachedPapersByYear = readAndParseAllPapers();
 					// cache papers
 					writeObjectToCache(cachedPapersByYear, PAPER_CACHE_FILE);
 				}
 
 				Assert.notNull(cachedPapersByYear);
 			}
 		}
 
 		Map<PaperPublishType, List<Paper>> result = cachedPapersByYear
 				.get(Integer.valueOf(year));
 		if (result == null) {
 			return Collections.emptyMap();
 		}
 
 		return result;
 	}
 
 	private Map<Integer, Map<PaperPublishType, List<Paper>>> readAndParseAllPapers() {
 
 		LOGGER.warn(LogHelper.LOG_LAYER_ONE_BEGIN + "Caching all papers...");
 
 		File folder = new File(PAPER_FOLDER);
 
 		Map<Integer, Map<PaperPublishType, List<Paper>>> result = new TreeMap<Integer, Map<PaperPublishType, List<Paper>>>();
 		readAndParseAllPapers(folder, result, null);
 
 		LOGGER.warn(LogHelper.LOG_LAYER_ONE_END + "Caching all papers... Done.");
 		return result;
 	}
 
 	private void readAndParseAllPapers(File file,
 			Map<Integer, Map<PaperPublishType, List<Paper>>> result,
 			Map<PaperPublishType, List<Paper>> papersOfYear) {
 
 		String fileFullName = file.toString();
 		int pathLastPart = fileFullName.lastIndexOf(File.separatorChar);
 		String fileName = fileFullName.substring(pathLastPart + 1);
 
 		if (file.isDirectory()) {
 
 			Integer year = null;
 			try {
 				year = Integer.parseInt(fileName);
 			} catch (NumberFormatException ex) {
 				// ignore
 			}
 
 			if (year != null) {
 				papersOfYear = new HashMap<PaperPublishType, List<Paper>>();
 				LOGGER.debug("**** " + year + "==============================");
 				result.put(year, papersOfYear);
 			}
 
 			for (File child : file.listFiles()) {
 				readAndParseAllPapers(child, result, papersOfYear);
 			}
 
 		} else {
 			if (!fileName.endsWith(".pdf")) {
 				return;
 			}
 
 			if (fileName.startsWith("P")) {
 				parseAndStorePaper(file, papersOfYear,
 						PaperPublishType.LongPaper);
 			} else if (fileName.contains("Student")) {
 				parseAndStorePaper(file, papersOfYear,
 						PaperPublishType.StudentWorkshopPaper);
 			} else if (fileName.startsWith("W")) {
 				parseAndStorePaper(file, papersOfYear,
						PaperPublishType.WorkshopPaper);
 			}
 		}
 	}
 
 	private void parseAndStorePaper(File file,
 			Map<PaperPublishType, List<Paper>> papersOfYear,
 			PaperPublishType publishType) {
 		List<Paper> papers = papersOfYear.get(publishType);
 		if (papers == null) {
 			papers = new ArrayList<Paper>();
 			papersOfYear.put(publishType, papers);
 		}
 
 		LOGGER.debug("Parsing paper [" + publishType + "|" + file + "]");
 		Paper paper = paperParser.parse(file);
 		papers.add(paper);
 	}
 
 	@SuppressWarnings("unchecked")
 	private Map<Integer, Map<PaperPublishType, List<Paper>>> getCachedPapersByYear() {
 		return (Map<Integer, Map<PaperPublishType, List<Paper>>>) getCachedObject(PAPER_CACHE_FILE);
 	}
 
 	private Object getCachedObject(String cachedFile) {
 		ObjectInputStream ois = null;
 		try {
 			BufferedInputStream stream = new BufferedInputStream(
 					new FileInputStream(cachedFile));
 			ois = new ObjectInputStream(stream);
 
 			Object cached = ois.readObject();
 			return cached;
 
 		} catch (Exception exception) {
 			System.err.println("No cache exists for [" + cachedFile + "]. ");
 			return null;
 		} finally {
 			IOUtils.closeQuietly(ois);
 		}
 	}
 
 	private void writeObjectToCache(Object object, String cacheFileName) {
 
 		FileUtils.deleteQuietly(new File(cacheFileName));
 
 		ObjectOutputStream oos = null;
 		try {
 			BufferedOutputStream stream = new BufferedOutputStream(
 					new FileOutputStream(cacheFileName));
 			oos = new ObjectOutputStream(stream);
 			oos.writeObject(object);
 			oos.flush();
 		} catch (Exception e) {
 			System.err.println("Fail to write cache [" + cacheFileName
 					+ "] into hard disk! ");
 			throw new AutomaticReviewerException(e);
 		} finally {
 			IOUtils.closeQuietly(oos);
 		}
 	}
 
 	public void removeCache() {
 		FileUtils.deleteQuietly(new File(PAPER_CACHE_FILE));
 		System.err.println("Cache file[" + PAPER_CACHE_FILE
 				+ "] has been removed.");
 	}
 
 	public static void main(String[] args) {
 		Map<PaperPublishType, List<Paper>> result = PaperCache.getInstance()
 				.getPapersByPublishTypeForYear(2012);
 		System.out.println(2012);
 
 		for (Entry<PaperPublishType, List<Paper>> papersByType : result
 				.entrySet()) {
 			for (Paper paper : papersByType.getValue()) {
 				System.out.println(papersByType.getKey() + " "
 						+ paper.getMetadata().getPaperFileName());
 			}
 		}
 
 		System.out.println("===========================================");
 
 		List<Paper> result2 = PaperCache.getInstance().getPapers(2007,
 				PaperPublishType.LongPaper,
 				PaperPublishType.StudentWorkshopPaper);
 		for (Paper paper : result2) {
 			System.out.println(paper.getMetadata().getPaperFileName());
 		}
 
 		// PaperCache.getInstance().removeCache();
 
 	}
 }
