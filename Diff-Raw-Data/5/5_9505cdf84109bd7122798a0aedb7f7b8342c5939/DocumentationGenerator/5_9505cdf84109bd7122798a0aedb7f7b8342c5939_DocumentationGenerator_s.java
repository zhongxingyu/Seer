 package de.devboost.natspec.library.documentation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 import de.devboost.natspec.library.documentation.util.DocumentationSwitch;
 
 public class DocumentationGenerator extends DocumentationSwitch<String> {
 
 	public static final String DOC_PATH = "./doc/";
 	public static final String DOC_FRAGMENT_PATH = "./doc/fragment/";
 	public static final String DOC_IMAGE_PATH = "./doc/images/";
 
 	private static final String DEFAULT_CSS_FILENAME = "css.css";
 
 	private int sectionCount;
 	private int subsectionCount;
 	private int subsubsectionCount;
 	private int figureCounter = 1;
 	private int entryCounter;
 	private int xmlCounter = 1;
 
 	private File imagePath;
 	private Map<Integer, NamedElement> imageTable = new LinkedHashMap<Integer, NamedElement>();
 	private Configuration configuration;
 
 	/**
 	 * value constructor with a given generator configuration
 	 */
 	public DocumentationGenerator(Configuration configuration) {
 		Assert.isNotNull(configuration, "configuration is required");
 		this.configuration = configuration;
 		imagePath = new File(DOC_IMAGE_PATH);
 		if (configuration.isCopyImages()) {
 			if (!deleteIfExists(imagePath)) {
 				System.err.println("warning: image path has not cleaned.");
 			}
 		}
 	}
 
 	/**
 	 * default constructor
 	 */
 	public DocumentationGenerator() {
 		this(new Configuration());
 	}
 
 	private boolean deleteIfExists(File file) {
 		if (file.exists()) {
 			if (file.isDirectory()) {
 				File[] files = file.listFiles();
 				for (File nestedFile : files) {
 					if (!deleteIfExists(nestedFile)) {
 						return false;
 					}
 				}
 			}
 			return file.delete();
 		}
 		return true;
 	}
 
 	@Override
 	public String caseDocumentation(Documentation documentation) {
 		String result = "<h1 class=\"title\">" + documentation.getTitle()
 				+ "</h1>\n";
 		result += getClassificationHTML();
 		result += "<h2>Outline</h2>";
 		for (Section s : documentation.getSections()) {
 			sectionCount++;
 			subsectionCount = 0;
 			s.setId(sectionCount + "");
 			result += "<a class=\"outline_section_reference\" href=\"#"
 					+ s.getId() + "\">" + s.getId() + " " + s.getName().trim()
 					+ "</a><br/>\n";
 
 			for (Fragment f : s.getFragments()) {
 				if (f instanceof Subsection) {
 					Subsection subsection = (Subsection) f;
 					subsectionCount++;
 					subsubsectionCount = 0;
 					String id = sectionCount + "." + subsectionCount;
 					subsection.setId(id);
 					result += "<a class=\"outline_subsection_reference\" href=\"#"
 							+ subsection.getId()
 							+ "\">"
 							+ subsection.getId()
 							+ " " + subsection.getName().trim() + "</a><br/>\n";
 
 					for (Fragment f2 : subsection.getFragments()) {
 						if (f2 instanceof Subsubsection) {
 							Subsubsection subsubsection = (Subsubsection) f2;
 							subsubsectionCount++;
 							String subsubid = sectionCount + "."
 									+ subsectionCount + "."
 									+ subsubsectionCount;
 							subsubsection.setId(subsubid);
 							result += "<a class=\"outline_subsubsection_reference\" href=\"#"
 									+ subsubsection.getId()
 									+ "\">"
 									+ subsubsection.getId()
 									+ " "
 									+ subsubsection.getName().trim()
 									+ "</a><br/>\n";
 
 						}
 					}
 				}
 			}
 		}
 		casePageBreak(null);
 
 		boolean hasImages = hasImages(documentation);
 		if (configuration.isTableOfFigures() && hasImages) {
 			sectionCount++;
 			result += "<a class=\"outline_section_reference\" href=\"#"
 					+ sectionCount + "\">" + sectionCount + " Table of Figures"
 					+ "</a><br/>\n";
 
 			result = result + casePageBreak(null);
 		} else {
 			result = result + casePageBreak(null);
 		}
 
 		for (Section s : documentation.getSections()) {
 			result += doSwitch(s);
 		}
 
 		String glossary = "";
 		for (TermEntry entry : documentation.getTerminology()) {
 			result += doSwitch(entry);
 			// result = weaveTerminologyReferences(entry, result);
 		}
 
 		result += glossary;
 
 		result = result + casePageBreak(null);
 
 		if (configuration.isTableOfFigures() && hasImages) {
 			result += insertFigureTable(imageTable, sectionCount);
 		}
 
 		return result;
 	}
 
 	protected String getClassificationHTML() {
 		return "<div class=\"divFooter\">UNCLASSIFIED</div>";
 	}
 
 	/**
 	 * iterates through the given root elements content and returns the first
 	 * element having the given instance type.
 	 * 
 	 * @return the first object that is an instance of the given type, or
 	 *         <code>null</code>, if no such element has been located.
 	 */
 	@SuppressWarnings("unchecked")
 	private static <T> T firstOn(EObject root, Class<T> instanceType) {
 		for (TreeIterator<EObject> it = EcoreUtil.getAllContents(root, false); it
 				.hasNext();) {
 			EObject next = it.next();
 			if (instanceType.isInstance(next)) {
 				return (T) next;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * determines, if the documentation contains at least on image element
 	 */
 	private boolean hasImages(Documentation documentation) {
 		return firstOn(documentation, Image.class) != null;
 	}
 
 	private String insertFigureTable(Map<Integer, NamedElement> map,
 			int sectionCount) {
 		String result = "";
 
 		result += "<a name=\"" + sectionCount + "\"/><h2>" + sectionCount
 				+ " Table of Figures</h2><br/>";
 		for (Map.Entry<Integer, NamedElement> e : imageTable.entrySet()) {
 			result += "<a class=\"figure_table_reference\" href=\"#"
 					+ figureAnchorID(e.getKey()) + "\">" + "Figure  "
 					+ e.getKey() + " - " + e.getValue().getName()
 					+ "</a><br/>\n";
 		}
 
 		return result;
 	}
 
 	@Override
 	public String caseSection(Section section) {
 		String result = casePageBreak(null);
 
 		result += "<h2 id=\"" + section.getId() + "\" class=\"section\">"
 				+ section.getId() + " " + section.getName() + "</h2>\n";
 		for (Fragment f : section.getFragments()) {
 			result += doSwitch(f);
 		}
 
 		return result;
 	}
 	
 	@Override
 	public String caseListing(Listing listing) {
 		StringBuilder result = new StringBuilder();
 		result.append("<div class=\"code\">");
 		int indendation = 0;
 		java.util.List<Fragment> fragments = listing.getFragments();
 		for (Fragment fragment : fragments) {
 			if (fragment instanceof Line) {
 				Line line = (Line) fragment;
 				String text = line.getText().trim();
 				if (text.endsWith("}")) {
 					indendation--;
 				}
 				for (int i = 0; i < indendation; i++) {
 					result.append("&nbsp;&nbsp;&nbsp;&nbsp;");
 				}
				result.append(text);
 				result.append("<br/>");
 				if (text.endsWith("{")) {
 					indendation++;
 				}
 			}
 		}
 		result.append("</div>");
 		return result.toString();
 	}
 	
 	@Override
 	public String caseCode(Code code) {
 		StringBuilder result = new StringBuilder();
 		result.append("<tt>");
		result.append(code.getText());
 		result.append("</tt>&nbsp;");
 		return result.toString();
 	}
 
 	@Override
 	public String caseSubsection(Subsection subsection) {
 		String result = "<h3 id=\"" + subsection.getId()
 				+ "\" class=\"subsection\">" + subsection.getId() + " "
 				+ subsection.getName() + "</h3>\n";
 		for (Fragment f : subsection.getFragments()) {
 			result += doSwitch(f);
 		}
 
 		return result;
 	}
 
 	@Override
 	public String caseSubsubsection(Subsubsection subsubsection) {
 		String result = "<h3 id=\"" + subsubsection.getId()
 				+ "\" class=\"subsubsection\">" + subsubsection.getId() + " "
 				+ subsubsection.getName() + "</h3></a>\n";
 		for (Fragment f : subsubsection.getFragments()) {
 			result += doSwitch(f);
 		}
 
 		return result;
 	}
 
 	@Override
 	public String caseParagraph(Paragraph paragraph) {
 		String result = "<p>\n";
 		for (Fragment f : paragraph.getFragments()) {
 			result += doSwitch(f);
 		}
 		result += "</p>\n";
 		return result;
 	}
 
 	@Override
 	public String caseLine(Line line) {
 		return line.getText();
 	}
 
 	@Override
 	public String caseTable(Table object) {
 		String result = "<table>";
 		result += doSwitch(object.getTableHeader());
 		for (TableRow rows : object.getTableRows()) {
 			result += doSwitch(rows);
 		}
 		result += "</table>\n";
 		return result;
 	}
 
 	@Override
 	public String caseTableHeader(TableHeader object) {
 		String result = "<tr>";
 		for (String cell : object.getHeaderCells()) {
 			result += "<th>" + cell + "</th>";
 		}
 		result += "</tr>\n";
 		return result;
 	}
 
 	@Override
 	public String caseTableRow(TableRow object) {
 		String result = "<tr>";
 		for (String cell : object.getRowCells()) {
 			result += "<td>" + cell + "</td>";
 		}
 		result += "</tr>\n";
 		return result;
 	}
 
 	@Override
 	public String caseList(List list) {
 		String result = "<ul>\n";
 		for (ListItem item : list.getItems()) {
 			result += doSwitch(item);
 		}
 		result += "</ul>\n";
 		return result;
 	}
 
 	@Override
 	public String caseListItem(ListItem item) {
 		return "<li>" + item.getText() + "</li>\n";
 	}
 
 	private static String figureAnchorID(int figureCounter) {
 		return "figure_" + figureCounter;
 	}
 
 	@Override
 	public String caseImage(Image image) {
 		String imagePath = image.getOriginalSource();
 		if (configuration.isCopyImages()) {
 			try {
 				imagePath = copyImage(image);
 			} catch (IOException e) {
 				System.err.println("warning: can't copy image '" + imagePath
 						+ "'. Keep the original file reference.");
 				e.printStackTrace();
 			}
 		}
 		String result = "<br/><span id=\"" + figureAnchorID(figureCounter)
 				+ "\">";
 		if (configuration.isTableOfFigures()) {
 			result += "<a name=\"" + figureAnchorID(figureCounter) + "\" ></a>";
 			imageTable.put(figureCounter, image);
 		}
 		if (image.getWidth() != null) {
 			result += "<img class=\"manStyled\" src=\"" + imagePath
 					+ "\" width=\"" + image.getWidth() + "%\" />";
 		} else {
 			result += "<img src=\"" + imagePath + "\" width=\"100%\" />";
 		}
 		result += "<div class=\"figure_description\">Figure " + figureCounter++
 				+ " - " + image.getName() + "</div>";
 		result += "</span>";
 		return result;
 	}
 
 	@Override
 	public String casePageBreak(PageBreak object) {
 		String html = "<div style=\"page-break-after:always\"></div>";
 		return html;
 	}
 
 	@Override
 	public String caseXML(XML xml) {
 		StringBuffer result = new StringBuffer();
 		result.append("<br/><br/>");
 		result.append("<div class=\"figure_description\">XML Listing "
 				+ xmlCounter++ + " - " + xml.getName() + "</div>");
 		result.append("<pre>");
 
 		String content;
 		try {
 			Class<?> clazz = Class.forName(xml.getContextClassName());
 			InputStream inputStream = clazz.getResourceAsStream(xml
 					.getResource());
 
 			StringWriter writer = new StringWriter();
 			IOUtils.copy(inputStream, writer, "UTF-8");
 			content = StringEscapeUtils.escapeXml(writer.toString());
 			result.append(content);
 			// result.append(writer.toString());
 
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		result.append("</pre>");
 		return result.toString();
 	}
 
 	// private String weaveTerminologyReferences(TermEntry entry, String result)
 	// {
 	// return result.replaceAll(entry.getName(), "<a href=\"#" + entry.getId()
 	// + "\">" + entry.getName() + "</a>");
 	// }
 
 	@Override
 	public String caseTermEntry(TermEntry entry) {
 		entry.setId("entry_" + entryCounter++);
 		String result = "<a name=\"" + entry.getId() + "\"><strong>"
 				+ entry.getName() + "</strong></a>: " + entry.getDescription()
 				+ "<br/>";
 		return result;
 	}
 
 	private String copyImage(Image image) throws IOException {
 		String originalSource = image.getOriginalSource();
 		return copyFile(originalSource);
 	}
 
 	private String copyFile(String fileName) throws IOException {
 		File sourceFile = new File(fileName);
 		File targetFile = new File(imagePath, sourceFile.getName());
 		int idx = 1;
 		if (targetFile.exists()) {
 			String baseFileName = StringUtils.substringBeforeLast(
 					targetFile.getPath(), ".");
 			String fileExtension = StringUtils.substringAfterLast(
 					targetFile.getPath(), ".");
 			String incrementedFileName = baseFileName + "_" + (idx++) + "."
 					+ fileExtension;
 			File potentialTargetFile = new File(incrementedFileName);
 			while (potentialTargetFile.exists()) {
 				incrementedFileName = baseFileName + "_" + (idx++) + "."
 						+ fileExtension;
 				potentialTargetFile = new File(incrementedFileName);
 			}
 			targetFile = potentialTargetFile;
 		}
 		File targetPath = targetFile.getParentFile();
 		if (!targetPath.exists()) {
 			targetPath.mkdirs();
 		}
 		FileInputStream fis = new FileInputStream(sourceFile);
 		FileOutputStream fos = new FileOutputStream(targetFile);
 		try {
 			byte[] bbuf = new byte[2048];
 			int read = fis.read(bbuf);
 			while (read > 0) {
 				fos.write(bbuf, 0, read);
 				read = fis.read(bbuf);
 			}
 		} finally {
 			fis.close();
 			fos.close();
 		}
 		System.out.println("copied " + sourceFile.getPath() + " to "
 				+ targetFile.getPath());
 		String rawPath = targetFile.getPath().replaceAll("\\\\", "/");
 		// ./doc/images --> ./images
 		rawPath = StringUtils.replace(rawPath, DOC_PATH, "./");
 		return rawPath;
 	}
 
 	public void saveDocumentationToFile(Documentation documentation)
 			throws Exception {
 		String completeDocumentation = getDocumentationAsString(documentation,
 				DEFAULT_CSS_FILENAME);
 
 		File file = new File(DOC_PATH + "Documentation.html");
 
 		// if file doesn't exists, then create it
 		if (!file.exists()) {
 			File parentFile = file.getParentFile();
 			if (!parentFile.exists()) {
 				parentFile.mkdirs();
 			}
 			file.createNewFile();
 		}
 		FileOutputStream fop = new FileOutputStream(file);
 
 		// get the content in bytes
 		byte[] contentInBytes = completeDocumentation.getBytes();
 
 		fop.write(contentInBytes);
 		fop.flush();
 		fop.close();
 		System.out.println("Saved documentation to: " + file.getAbsolutePath());
 	}
 
 	public String getDocumentationAsString(Documentation documentation,
 			String cssPath) {
 		StringBuilder completeFile = new StringBuilder();
 		initHTMLHeader(completeFile, cssPath);
 		completeFile.append(doSwitch(documentation));
 		closeHTMLHeader(completeFile);
 		return completeFile.toString();
 	}
 
 	private void closeHTMLHeader(StringBuilder builder) {
 		builder.append("</body>\n");
 		builder.append("</html>\n");
 	}
 
 	public void saveFragmentToFile(Fragment documentation, String filename)
 			throws Exception {
 		StringBuilder completeFile = new StringBuilder();
 		initHTMLHeader(completeFile, DEFAULT_CSS_FILENAME);
 		completeFile.append(doSwitch(documentation));
 		closeHTMLHeader(completeFile);
 		File file = new File(DOC_FRAGMENT_PATH + filename + ".html");
 
 		// if file doesn't exists, then create it
 		if (!file.exists()) {
 			File parentFile = file.getParentFile();
 			if (!parentFile.exists()) {
 				parentFile.mkdirs();
 			}
 			file.createNewFile();
 		}
 		FileOutputStream fop = new FileOutputStream(file);
 
 		// get the content in bytes
 		byte[] contentInBytes = completeFile.toString().getBytes();
 
 		fop.write(contentInBytes);
 		fop.flush();
 		fop.close();
 		System.out.println("Saved documentation to: " + file.getAbsolutePath());
 
 	}
 
 	private void initHTMLHeader(StringBuilder buffer, String cssPath) {
 		buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
 		buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
 		buffer.append("<head>\n");
 		buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
 		buffer.append("<link href='http://fonts.googleapis.com/css?family=Titillium+Web:200,400' rel='stylesheet' type='text/css'/>\n");
 		buffer.append("<link rel=\"stylesheet\" href=\"" + cssPath + "\" />\n");
 		buffer.append("</head>\n");
 		buffer.append("<body>\n");
 	}
 
 	public String getDocumentationFragmentContents(String fragmentFilenname)
 			throws IOException {
 		File file = new File(DOC_FRAGMENT_PATH + fragmentFilenname.trim()
 				+ ".html");
 		if (!file.exists()) {
 			return "<div class=\"error\">ERROR: could not find documentation fragment at:<br/> "
 					+ file.getAbsolutePath() + "</div>";
 		}
 		InputStream stream = new FileInputStream(file);
 		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
 
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = br.readLine()) != null) {
 			sb.append(line);
 		}
 
 		br.close();
 		return sb.toString();
 	}
 
 }
