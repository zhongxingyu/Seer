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
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 import de.devboost.natspec.library.documentation.util.DocumentationSwitch;
 
 // TODO Replace output to System.out and System.err with listener mechanism
 public class DocumentationGenerator extends DocumentationSwitch<String> {
 
 	public static final String DOC_PATH = "./doc/";
 	public static final String DOC_FRAGMENT_PATH = "./doc/fragment/";
 	public static final String DOC_IMAGE_PATH = "./doc/images/";
 
 	private static final String DEFAULT_CSS_FILENAME = "css.css";
 
 	private final Configuration configuration;
 
 	private int sectionCount;
 	private int subsectionCount;
 	private int subsubsectionCount;
 	private int figureCounter = 1;
 	private int entryCounter;
 	private int xmlCounter = 1;
 
 	private File imagePath;
 	private Map<Integer, NamedElement> imageMap = new LinkedHashMap<Integer, NamedElement>();
 
 	/**
 	 * Creates a new {@link DocumentationGenerator} using a default
 	 * configuration.
 	 */
 	public DocumentationGenerator() {
 		this(new Configuration());
 	}
 
 	/**
 	 * Creates a new {@link DocumentationGenerator} using the given
 	 * configuration.
 	 */
 	public DocumentationGenerator(Configuration configuration) {
 		Assert.isNotNull(configuration, "Configuration is required");
 		
 		this.configuration = configuration;
 		this.imagePath = new File(DOC_IMAGE_PATH);
 		if (configuration.isCopyImages()) {
 			if (!deleteIfExists(imagePath)) {
 				System.err.println("Warning: image path has not cleaned.");
 			}
 		}
 	}
 
 	/**
 	 * Deletes the given file. If the file is a directory, all its contents
 	 * (i.e., sub directories and contained files) are deleted as well.
 	 * 
 	 * @return <code>true</code> is deletion was successful, otherwise
 	 *         <code>false</code>
 	 */
 	protected boolean deleteIfExists(File file) {
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
 					String name = subsection.getName();
 					String trimmedName = name == null ? "" : name.trim();
 					result += "<a class=\"outline_subsection_reference\" href=\"#"
 							+ subsection.getId()
 							+ "\">"
 							+ id
 							+ " "
 							+ trimmedName + "</a><br/>\n";
 
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
 			result += insertFigureTable(imageMap, sectionCount);
 		}
 
 		return result;
 	}
 
 	protected String getClassificationHTML() {
 		return "<div class=\"divFooter\">UNCLASSIFIED</div>";
 	}
 
 	/**
 	 * Iterates through the given root elements content and returns the first
 	 * element having the given instance type.
 	 * 
 	 * @return the first object that is an instance of the given type, or
 	 *         <code>null</code>, if no such element has been located.
 	 */
 	private <T> T firstOn(EObject root, Class<T> instanceType) {
 		TreeIterator<EObject> it = EcoreUtil.getAllContents(root, false);
 		while (it.hasNext()) {
 			EObject next = it.next();
 			if (instanceType.isInstance(next)) {
 				return instanceType.cast(next);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Determines, if the documentation contains at least on image element.
 	 * 
 	 * @return <code>true</code> if there is an image in the documentation,
 	 *         otherwise <code>false</code>
 	 */
 	private boolean hasImages(Documentation documentation) {
 		return firstOn(documentation, Image.class) != null;
 	}
 
 	private String insertFigureTable(Map<Integer, NamedElement> map,
 			int sectionCount) {
 		String result = "";
 
 		result += "<a name=\"" + sectionCount + "\"/><h2>" + sectionCount
 				+ " Table of Figures</h2>\n";
 		for (Map.Entry<Integer, NamedElement> e : imageMap.entrySet()) {
 			result += "<a class=\"figure_table_reference\" href=\"#"
 					+ figureAnchorID(e.getKey()) + "\">" + "Figure  "
 					+ e.getKey() + " - " + e.getValue().getName()
 					+ "</a><br/>\n";
 		}
 
 		return result;
 	}
 
 	@Override
 	public String caseListing(Listing listing) {
 		StringBuilder result = new StringBuilder();
 		result.append("<div class=\"code\">");
 
 		int indendation = 0;
 		java.util.List<Text> texts = listing.getTexts();
 		for (Text nextText : texts) {
 			String text = nextText.getText().trim();
 			if (text.endsWith("}") || text.endsWith("};")) {
 				indendation--;
 			}
 			for (int i = 0; i < indendation; i++) {
 				result.append("&nbsp;&nbsp;&nbsp;&nbsp;");
 			}
 			result.append(StringEscapeUtils.escapeHtml(text));
 			result.append("<br/>");
 			if (text.endsWith("{") && !text.contains("}")) {
 				indendation++;
 			}
 		}
 
 		result.append("</div>");
 		return result.toString();
 	}
 
 	@Override
 	public String caseCode(Code code) {
 		StringBuilder result = new StringBuilder();
 		result.append("<tt>");
 		result.append(StringEscapeUtils.escapeHtml(code.getText()));
 		result.append("</tt>&nbsp;");
 		return result.toString();
 	}
 
 	@Override
 	public String caseReference(Reference object) {
 		StringBuilder result = new StringBuilder();
 		result.append("<a href=\"#");
 		String referredLabel = object.getReferredLabel();
 		NamedElement referredElement = getNamedElementWithLabel(referredLabel, object.eContainer());
 		if (referredElement == null) {
 			throw new RuntimeException("Can't find referenced label '" + referredLabel + "'.");
 		}
 		result.append(referredElement.getId());
 		result.append("\">");
 		result.append(object.getName());
 		result.append("</a>\n");
 		return result.toString();
 	}
 
 	private NamedElement getNamedElementWithLabel(String label,
 			EObject eContainer) {
 		
 		while (!(eContainer instanceof Documentation)
 				&& null != eContainer.eContainer()) {
 			eContainer = eContainer.eContainer();
 		}
 
 		if (eContainer instanceof Documentation) {
 			Documentation documentation = (Documentation) eContainer;
 
 			if (documentation.getSections().size() > 0) {
 				java.util.List<NamedElement> elementsToVisit = new LinkedList<NamedElement>();
 				elementsToVisit.addAll(documentation.getSections());
 				NamedElement currentElement = elementsToVisit.iterator().next();
 
 				while (null != currentElement) {
 					if (label.equals(currentElement.getLabel())) {
 						return currentElement;
 					}
 
 					if (currentElement instanceof FragmentContainer) {
 						for (Fragment fragment : ((FragmentContainer) currentElement)
 								.getFragments()) {
 							if (fragment instanceof NamedElement) {
 								elementsToVisit.add((NamedElement) fragment);
 							}
 						}
 					}
 
 					elementsToVisit.remove(0);
 
 					if (elementsToVisit.size() > 0) {
 						currentElement = elementsToVisit.iterator().next();
 					} else {
 						currentElement = null;
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public String caseSection(Section section) {
 		String result = casePageBreak(null);
 
 		String id = section.getId();
 		String name = section.getName();
 		String trimmedName = name.trim();
 		
 		result += "<h2 id=\"" + id + "\" class=\"section\">" + id + " "
 				+ trimmedName + "</h2>\n";
 		
 		for (Text t : section.getTexts()) {
 			result += doSwitch(t);
 		}
 		
 		for (Fragment f : section.getFragments()) {
 			result += doSwitch(f);
 		}
 
 		return result;
 	}
 
 	@Override
 	public String caseSubsection(Subsection subsection) {
 		String id = subsection.getId();
 		String name = subsection.getName();
 		String trimmedName = name == null ? "" : name.trim();
 		
 		String result = "<h3 id=\"" + id + "\" class=\"subsection\">" + id
 				+ " " + trimmedName + "</h3>\n";
 
 		for (Text t : subsection.getTexts()) {
 			result += doSwitch(t);
 		}
 		
 		for (Fragment fragment : subsection.getFragments()) {
 			result += doSwitch(fragment);
 		}
 
 		return result;
 	}
 
 	@Override
 	public String caseSubsubsection(Subsubsection subsubsection) {
 		String id = subsubsection.getId();
 		String name = subsubsection.getName();
 		String trimmedName = name.trim();
 		
 		String result = "<h4 id=\"" + id + "\" class=\"subsubsection\">" + id
 				+ " " + trimmedName + "</h4>\n";
 		
 		for (Text t : subsubsection.getTexts()) {
 			result += doSwitch(t);
 		}
 		
 		for (Fragment fragment : subsubsection.getFragments()) {
 			result += doSwitch(fragment);
 		}
 
 		return result;
 	}
 
 	@Override
 	public String caseParagraph(Paragraph paragraph) {
 		StringBuilder result = new StringBuilder("<p>\n");
 		for (Text text : paragraph.getTexts()) {
 			result.append(doSwitch(text));
 		}
 		result.append("</p>\n");
 		return result.toString();
 	}
 
 	@Override
 	public String caseLine(Line line) {
 		return StringEscapeUtils.escapeHtml(line.getText());
 	}
 	
 	public String caseHtmlCode(HtmlCode htmlCode) {
 		return htmlCode.getText();
 	}
 
 	@Override
 	public String caseLink(Link link) {
 		StringBuilder result = new StringBuilder();
 		result.append("<a href=\"");
 		result.append(link.getUri());
 		result.append("\" target=\"_blank\">");
 		result.append(link.getName());
 		result.append("</a>\n");
 		return result.toString();
 	}
 
 	@Override
 	public String caseTable(Table object) {
 		StringBuilder result = new StringBuilder("<table>");
 		TableHeader tableHeader = object.getTableHeader();
 		if (tableHeader != null) {
 			// If there is no table header, we cannot generated HTML for it
 			result.append(doSwitch(tableHeader));
 		}
 		for (TableRow rows : object.getTableRows()) {
 			result.append(doSwitch(rows));
 		}
 		result.append("</table>\n");
 		return result.toString();
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
 		StringBuilder result = new StringBuilder();
 		result.append("<ul>\n");
 		for (ListItem item : list.getItems()) {
 			result.append(doSwitch(item));
 		}
 		result.append("</ul>\n");
 		return result.toString();
 	}
 
 	@Override
 	public String caseListItem(ListItem item) {
 		String trimmedText = item.getText().trim();
 		String escapedText = StringEscapeUtils.escapeHtml(trimmedText);
 		return "<li>" + escapedText + "</li>\n";
 	}
 
 	private String figureAnchorID(int figureCounter) {
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
 		
 		String anchorID = figureAnchorID(figureCounter);
 		StringBuilder result = new StringBuilder();
 		// TODO Remove explicit line break
 		result.append("<br/><span id=\"" + anchorID + "\">");
 		
 		if (configuration.isTableOfFigures()) {
 			result.append("<a name=\"" + anchorID + "\" ></a>");
 			imageMap.put(figureCounter, image);
 		}
 		
 		if (image.getWidth() != null) {
 			result.append("<img class=\"manStyled\" src=\"" + imagePath
 					+ "\" width=\"" + image.getWidth().getWidth()
 					+ image.getWidth().getUnit().getLiteral() + "\" />");
 		} else {
 			result.append("<img src=\"" + imagePath + "\" width=\"100%\" />");
 		}
 		
 		result.append("<div class=\"figure_description\">Figure " + figureCounter++
 				+ " - " + image.getName() + "</div>");
 		result.append("</span>");
 		
 		return result.toString();
 	}
 
 	@Override
 	public String casePageBreak(PageBreak object) {
 		// TODO Use class instead of style to allow customization via CSS
 		String html = "<div style=\"page-break-after:always\"></div>";
 		return html;
 	}
 
 	@Override
 	public String caseXML(XML xml) {
 		StringBuffer result = new StringBuffer();
 		result.append("<div class=\"figure_description\">XML Listing "
 				+ xmlCounter++ + " - " + xml.getName() + "</div>");
 		result.append("<pre class=\"xml_listing\">");
 
 		String content;
 		try {
 			String contextClassName = xml.getContextClassName();
 			Class<?> clazz = Class.forName(contextClassName);
			if (clazz == null) {
				throw new RuntimeException("Can't find class '" + contextClassName + "'.");
			}
			
 			String resource = xml.getResource();
 			InputStream inputStream = clazz.getResourceAsStream(resource);
 
 			if (inputStream == null) {
				throw new RuntimeException("Can't find resource '" + resource + "' near class '" + clazz.getName() + "'.");
 			}
 			
 			StringWriter writer = new StringWriter();
 			IOUtils.copy(inputStream, writer, "UTF-8");
 			content = StringEscapeUtils.escapeXml(writer.toString());
 			result.append(content);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		result.append("</pre>");
 		return result.toString();
 	}
 
 	@Override
 	public String caseTermEntry(TermEntry entry) {
 		entry.setId("entry_" + entryCounter++);
 		String result = "<a name=\"" + entry.getId() + "\"><strong>"
 				+ entry.getName() + "</strong></a>: " + entry.getDescription()
 				+ "<br/>";
 		return result;
 	}
 
 	protected String copyImage(Image image) throws IOException {
 		String originalSource = image.getOriginalSource();
 		return copyFile(originalSource);
 	}
 
 	protected String copyFile(String fileName) throws IOException {
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
 
 		FileInputStream fis = null;
 		FileOutputStream fos = null;
 		try {
 			fis = new FileInputStream(sourceFile);
 			fos = new FileOutputStream(targetFile);
 			byte[] bbuf = new byte[2048];
 			int read = fis.read(bbuf);
 			while (read > 0) {
 				fos.write(bbuf, 0, read);
 				read = fis.read(bbuf);
 			}
 		} finally {
 			if (fis != null) {
 				fis.close();
 			}
 			if (fos != null) {
 				fos.close();
 			}
 		}
 
 		System.out.println("Copied " + sourceFile.getPath() + " to "
 				+ targetFile.getPath());
 
 		// TODO Why not use replace("\\", "/") instead of replaceAll()?
 		String rawPath = targetFile.getPath().replaceAll("\\\\", "/");
 		// ./doc/images --> ./images
 		rawPath = StringUtils.replace(rawPath, DOC_PATH, "./");
 		return rawPath;
 	}
 
 	/**
 	 * Converts the given documentation to HTML and saves it to a file using the
 	 * default platform encoding.
 	 * 
 	 * @param documentation
 	 * @throws IOException
 	 */
 	public void saveDocumentationToFile(Documentation documentation)
 			throws IOException {
 
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
 
 		FileOutputStream fos = new FileOutputStream(file);
 
 		// get the content in bytes
 		byte[] contentInBytes = completeDocumentation.getBytes();
 
 		fos.write(contentInBytes);
 		fos.flush();
 		fos.close();
 	}
 
 	public String getDocumentationAsString(Documentation documentation,
 			String cssPath) {
 
 		StringBuilder completeFileContents = new StringBuilder();
 		initHTMLHeader(completeFileContents, cssPath);
 		completeFileContents.append(doSwitch(documentation));
 		closeHTMLHeader(completeFileContents);
 		return completeFileContents.toString();
 	}
 
 	private void closeHTMLHeader(StringBuilder builder) {
 		builder.append("</body>\n");
 		builder.append("</html>\n");
 	}
 
 	public void saveFragmentToFile(Fragment documentation, String filename)
 			throws IOException {
 
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
 		FileOutputStream fos = new FileOutputStream(file);
 
 		// get the content in bytes
 		byte[] contentInBytes = completeFile.toString().getBytes();
 
 		fos.write(contentInBytes);
 		fos.flush();
 		fos.close();
 
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
 			// TODO Throw exception instead?
 			return "<div class=\"error\">ERROR: Can't find documentation fragment at:<br/> "
 					+ file.getAbsolutePath() + "</div>";
 		}
 
 		InputStream stream = new FileInputStream(file);
 		BufferedReader reader = new BufferedReader(
 				new InputStreamReader(stream));
 
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = reader.readLine()) != null) {
 			sb.append(line);
 		}
 
 		reader.close();
 		return sb.toString();
 	}
 }
