 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.apache.jmeter.extractor;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import java.math.*;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import javax.script.ScriptException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.apache.jmeter.assertions.AssertionResult;
 import org.apache.jmeter.processor.PostProcessor;
 import org.apache.jmeter.samplers.SampleResult;
 import org.apache.jmeter.testelement.AbstractScopedTestElement;
 import org.apache.jmeter.testelement.property.BooleanProperty;
 import org.apache.jmeter.threads.JMeterContext;
 import org.apache.jmeter.threads.JMeterVariables;
 import org.apache.jmeter.util.TidyException;
 import org.apache.jmeter.util.XPathUtil;
 import org.apache.jorphan.logging.LoggingManager;
 import org.apache.jorphan.util.JMeterError;
 import org.apache.jorphan.util.JOrphanUtils;
 import org.apache.log.Logger;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 import org.apache.xpath.XPathAPI;
 import org.apache.xpath.objects.XObject;
 
 import org.w3c.dom.*;
 
 import com.gargoylesoftware.htmlunit.MockWebConnection;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlDivision;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 import javax.swing.JFileChooser;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import addons.*;
 import org.apache.jmeter.visualizers.GraphVisualizer;
 
 import org.w3c.dom.*;
 import org.w3c.dom.html.HTMLElement;
 
 import addons.ManageXPaths;
 import addons.TempNode;
 import addons.LevenshteinDistance;
 
 // TODO: Auto-generated Javadoc
 //@see org.apache.jmeter.extractor.TestXPathExtractor for unit tests
 
 /**
  * Extracts text from (X)HTML response using XPath query language Example XPath
  * queries:
  * <dl>
  * <dt>/html/head/title</dt>
  * <dd>extracts Title from HTML response</dd>
  * <dt>//form[@name='countryForm']//select[@name='country']/option[text()='Czech
  * Republic'])/@value
  * <dd>extracts value attribute of option element that match text 'Czech
  * Republic' inside of select element with name attribute 'country' inside of
  * form with name attribute 'countryForm'</dd>
  * <dt>//head</dt>
  * <dd>extracts the XML fragment for head node.</dd>
  * <dt>//head/text()</dt>
  * <dd>extracts the text content for head node.</dd>
  * </dl>
  */
 /*
  * This file is inspired by RegexExtractor. author <a
  * href="mailto:hpaluch@gitus.cz">Henryk Paluch</a> of <a
  * href="http://www.gitus.com">Gitus a.s.</a>
  * 
  * See Bugzilla: 37183
  */
 public class XPathExtractor extends AbstractScopedTestElement implements
 		PostProcessor, Serializable {
 	
 	/** The Constant log. */
 	private static final Logger log = LoggingManager.getLoggerForClass();
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = 240L;
 
 	/** The Constant MATCH_NR. */
 	private static final String MATCH_NR = "matchNr"; // $NON-NLS-1$
 
 	// + JMX file attributes
 	/** The Constant XPATH_QUERY. */
 	private static final String XPATH_QUERY = "XPathExtractor.xpathQuery"; // $NON-NLS-1$
 	
 	/** The Constant REFNAME. */
 	private static final String REFNAME = "XPathExtractor.refname"; // $NON-NLS-1$
 	
 	/** The Constant DEFAULT. */
 	private static final String DEFAULT = "XPathExtractor.default"; // $NON-NLS-1$
 	
 	/** The Constant TOLERANT. */
 	private static final String TOLERANT = "XPathExtractor.tolerant"; // $NON-NLS-1$
 	
 	/** The Constant NAMESPACE. */
 	private static final String NAMESPACE = "XPathExtractor.namespace"; // $NON-NLS-1$
 	
 	/** The Constant QUIET. */
 	private static final String QUIET = "XPathExtractor.quiet"; // $NON-NLS-1$
 	
 	/** The Constant REPORT_ERRORS. */
 	private static final String REPORT_ERRORS = "XPathExtractor.report_errors"; // $NON-NLS-1$
 	
 	/** The Constant SHOW_WARNINGS. */
 	private static final String SHOW_WARNINGS = "XPathExtractor.show_warnings"; // $NON-NLS-1$
 	
 	/** The Constant DOWNLOAD_DTDS. */
 	private static final String DOWNLOAD_DTDS = "XPathExtractor.download_dtds"; // $NON-NLS-1$
 	
 	/** The Constant WHITESPACE. */
 	private static final String WHITESPACE = "XPathExtractor.whitespace"; // $NON-NLS-1$
 	
 	/** The Constant VALIDATE. */
 	private static final String VALIDATE = "XPathExtractor.validate"; // $NON-NLS-1$
 	
 	/** The Constant FRAGMENT. */
 	private static final String FRAGMENT = "XPathExtractor.fragment"; // $NON-NLS-1$
 	// - JMX file attributes
 
 	/** The fr. */
 	private FileReader fr = null;
 	
 	/** The br. */
 	private BufferedReader br = null;
 	
 	/** The file log. */
 	private File fileLog = null;
 
 	/** The root element. */
 	private Element rootElement;
 	
 	/** The graph node. */
 	private Element graphNode;
 	
 	/** The sample root. */
 	private Element sampleRoot;
 	
 	/** The sample node. */
 	protected Element sampleNode;
 	
 	/** The size node. */
 	private Element sizeNode;
 	
 	/** The etiqueta node. */
 	private Element etiquetaNode;
 	
 	/** The tag node. */
 	private Element tagNode;
 	
 	/** The data size. */
 	private Element dataSize;
 	
 	/** The data etiqueta. */
 	private Element dataEtiqueta;
 	
 	/** The data web. */
 	private Element dataWeb;
 	
 	/** The data date. */
 	private Element dataDate;
 	
 	/** The edge node. */
 	private Element edgeNode;
 
 	/** The doc builder. */
 	private DocumentBuilder docBuilder;
 	
 	/** The doc. */
 	private Document doc;
 
 	/** The node id. */
 	protected Integer nodeID = 0;
 	
 	/** The root id. */
 	protected Integer rootID = 0;
 	
 	/** The processing node. */
 	private Integer processingNode = 0;
 	
 	/** The xpaths. */
 	private static List<String> xpaths = new ArrayList<String>();
 	
 	/** The samples. */
 	private ArrayList<String> samples;
 	
 	/** The xpath len. */
 	private static Integer xpathLen = 0;
 	
 	/** The node counter. */
 	private static Integer nodeCounter = 0;
 
 	// private TempNode tn;
 	/** The list nodes. */
 	public static ArrayList<TempNode> listNodes;
 	
 	/** The web client. */
 	final WebClient webClient = new WebClient();
 	
 	/** The current page. */
 	private HtmlPage currentPage;
 
 	/**
 	 * Concat.
 	 *
 	 * @param s1 the s1
 	 * @param s2 the s2
 	 * @return the string
 	 */
 	private String concat(String s1, String s2) {
 		return new StringBuilder(s1).append("_").append(s2).toString(); // $NON-NLS-1$
 	}
 
 	/**
 	 * Concat.
 	 *
 	 * @param s1 the s1
 	 * @param i the i
 	 * @return the string
 	 */
 	private String concat(String s1, int i) {
 		return new StringBuilder(s1).append("_").append(i).toString(); // $NON-NLS-1$
 	}
 
 	/**
 	 * getId return next node sequential node identifier
 	 * 
 	 * handleMatch(res, xpaths[j], GraphVisualizer.getResultFileName());
 	 *
 	 * @return the integer
 	 */
 	protected Integer nextNodeID() {
 		return nodeID++;
 	}
 
 	/**
 	 * Next root id.
 	 *
 	 * @return the integer
 	 */
 	protected Integer nextRootID() {
 		return rootID++;
 	}
 
 	/**
 	 * Do the job - extract value from (X)HTML response using XPath Query.
 	 * Return value as variable defined by REFNAME. Returns DEFAULT value if not
 	 * found.
 	 */
 	public void process() {
 		JMeterContext context = getThreadContext();
 		final SampleResult previousResult = context.getPreviousResult();
 		if (previousResult == null) {
 			return;
 		}
 		JMeterVariables vars = context.getVariables();
 		String refName = getRefName();
 		vars.put(refName, getDefaultValue());
 		final String matchNR = concat(refName, MATCH_NR);
 		int prevCount = 0; // number of previous matches
 		try {
 			prevCount = Integer.parseInt(vars.get(matchNR));
 		} catch (NumberFormatException e) {
 			// ignored
 		}
 		vars.put(matchNR, "0"); // In case parse fails // $NON-NLS-1$
 		vars.remove(concat(refName, "1")); // In case parse fails // $NON-NLS-1$
 
 		List<String> matches = new ArrayList<String>();
 		try {
 			if (isScopeVariable()) {
 				String inputString = vars.get(getVariableName());
 				Document d = parseResponse(inputString);
 				getValuesForXPath(d, getXPathQuery(), matches);
 			} else {
 				List<SampleResult> samples = getSampleList(previousResult);
 				for (SampleResult res : samples) {
 					Document d = parseResponse(res.getResponseDataAsString());
 					getValuesForXPath(d, getXPathQuery(), matches);
 				}
 			}
 			final int matchCount = matches.size();
 			vars.put(matchNR, String.valueOf(matchCount));
 			if (matchCount > 0) {
 				String value = matches.get(0);
 				if (value != null) {
 					vars.put(refName, value);
 				}
 				for (int i = 0; i < matchCount; i++) {
 					value = matches.get(i);
 					if (value != null) {
 						vars.put(concat(refName, i + 1), matches.get(i));
 					}
 				}
 			}
 			vars.remove(concat(refName, matchCount + 1)); // Just in case
 			// Clear any other remaining variables
 			for (int i = matchCount + 2; i <= prevCount; i++) {
 				vars.remove(concat(refName, i));
 			}
 		} catch (IOException e) {// e.g. DTD not reachable
 			final String errorMessage = "IOException on (" + getXPathQuery()
 					+ ")";
 			log.error(errorMessage, e);
 			AssertionResult ass = new AssertionResult(getName());
 			ass.setError(true);
 			ass.setFailureMessage(new StringBuilder("IOException: ").append(
 					e.getLocalizedMessage()).toString());
 			previousResult.addAssertionResult(ass);
 			previousResult.setSuccessful(false);
 		} catch (ParserConfigurationException e) {// Should not happen
 			final String errrorMessage = "ParserConfigurationException while processing ("
 					+ getXPathQuery() + ")";
 			log.error(errrorMessage, e);
 			throw new JMeterError(errrorMessage, e);
 		} catch (SAXException e) {// Can happen for bad input document
 			log.warn("SAXException while processing (" + getXPathQuery() + ") "
 					+ e.getLocalizedMessage());
 			addAssertionFailure(previousResult, e, false); // Should this also
 															// fail the sample?
 		} catch (TransformerException e) {// Can happen for incorrect XPath
 											// expression
 			log.warn("TransformerException while processing ("
 					+ getXPathQuery() + ") " + e.getLocalizedMessage());
 			addAssertionFailure(previousResult, e, false);
 		} catch (TidyException e) {
 			// Will already have been logged by XPathUtil
 			addAssertionFailure(previousResult, e, true); // fail the sample
 		}
 	}
 
 	/**
 	 * Adds the assertion failure.
 	 *
 	 * @param previousResult the previous result
 	 * @param thrown the thrown
 	 * @param setFailed the set failed
 	 */
 	private void addAssertionFailure(final SampleResult previousResult,
 			final Throwable thrown, final boolean setFailed) {
 		AssertionResult ass = new AssertionResult(thrown.getClass()
 				.getSimpleName()); // $NON-NLS-1$
 		ass.setFailure(true);
 		ass.setFailureMessage(thrown.getLocalizedMessage()
 				+ "\nSee log file for further details.");
 		previousResult.addAssertionResult(ass);
 		if (setFailed) {
 			previousResult.setSuccessful(false);
 		}
 	}
 
 	/* ============= object properties ================ */
 	/**
 	 * Sets the x path query.
 	 *
 	 * @param val the new x path query
 	 */
 	public void setXPathQuery(String val) {
 		setProperty(XPATH_QUERY, val);
 	}
 
 	/**
 	 * Gets the x path query.
 	 *
 	 * @return the x path query
 	 */
 	public String getXPathQuery() {
 		return getPropertyAsString(XPATH_QUERY);
 	}
 
 	/**
 	 * Sets the ref name.
 	 *
 	 * @param refName the new ref name
 	 */
 	public void setRefName(String refName) {
 		setProperty(REFNAME, refName);
 	}
 
 	/**
 	 * Gets the ref name.
 	 *
 	 * @return the ref name
 	 */
 	public String getRefName() {
 		return getPropertyAsString(REFNAME);
 	}
 
 	/**
 	 * Sets the default value.
 	 *
 	 * @param val the new default value
 	 */
 	public void setDefaultValue(String val) {
 		setProperty(DEFAULT, val);
 	}
 
 	/**
 	 * Gets the default value.
 	 *
 	 * @return the default value
 	 */
 	public String getDefaultValue() {
 		return getPropertyAsString(DEFAULT);
 	}
 
 	/**
 	 * Sets the tolerant.
 	 *
 	 * @param val the new tolerant
 	 */
 	public void setTolerant(boolean val) {
 		setProperty(new BooleanProperty(TOLERANT, val));
 	}
 
 	/**
 	 * Checks if is tolerant.
 	 *
 	 * @return true, if is tolerant
 	 */
 	public boolean isTolerant() {
 		return getPropertyAsBoolean(TOLERANT);
 	}
 
 	/**
 	 * Sets the name space.
 	 *
 	 * @param val the new name space
 	 */
 	public void setNameSpace(boolean val) {
 		setProperty(new BooleanProperty(NAMESPACE, val));
 	}
 
 	/**
 	 * Use name space.
 	 *
 	 * @return true, if successful
 	 */
 	public boolean useNameSpace() {
 		return getPropertyAsBoolean(NAMESPACE);
 	}
 
 	/**
 	 * Sets the report errors.
 	 *
 	 * @param val the new report errors
 	 */
 	public void setReportErrors(boolean val) {
 		setProperty(REPORT_ERRORS, val, false);
 	}
 
 	/**
 	 * Report errors.
 	 *
 	 * @return true, if successful
 	 */
 	public boolean reportErrors() {
 		return getPropertyAsBoolean(REPORT_ERRORS, false);
 	}
 
 	/**
 	 * Sets the show warnings.
 	 *
 	 * @param val the new show warnings
 	 */
 	public void setShowWarnings(boolean val) {
 		setProperty(SHOW_WARNINGS, val, false);
 	}
 
 	/**
 	 * Show warnings.
 	 *
 	 * @return true, if successful
 	 */
 	public boolean showWarnings() {
 		return getPropertyAsBoolean(SHOW_WARNINGS, false);
 	}
 
 	/**
 	 * Sets the quiet.
 	 *
 	 * @param val the new quiet
 	 */
 	public void setQuiet(boolean val) {
 		setProperty(QUIET, val, true);
 	}
 
 	/**
 	 * Checks if is quiet.
 	 *
 	 * @return true, if is quiet
 	 */
 	public boolean isQuiet() {
 		return getPropertyAsBoolean(QUIET, true);
 	}
 
 	/**
 	 * Should we return fragment as text, rather than text of fragment?.
 	 *
 	 * @return true if we should return fragment rather than text
 	 */
 	public boolean getFragment() {
 		return getPropertyAsBoolean(FRAGMENT, false);
 	}
 
 	/**
 	 * Should we return fragment as text, rather than text of fragment?.
 	 *
 	 * @param selected true to return fragment.
 	 */
 	public void setFragment(boolean selected) {
 		setProperty(FRAGMENT, selected, false);
 	}
 
 	/* ================= internal business ================= */
 	/**
 	 * Converts (X)HTML response to DOM object Tree. This version cares of
 	 * charset of response.
 	 *
 	 * @param unicodeData the unicode data
 	 * @return the document
 	 * @throws UnsupportedEncodingException the unsupported encoding exception
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 * @throws ParserConfigurationException the parser configuration exception
 	 * @throws SAXException the sAX exception
 	 * @throws TidyException the tidy exception
 	 */
 	private Document parseResponse(String unicodeData)
 			throws UnsupportedEncodingException, IOException,
 			ParserConfigurationException, SAXException, TidyException {
 		// TODO: validate contentType for reasonable types?
 
 		// NOTE: responseData encoding is server specific
 		// Therefore we do byte -> unicode -> byte conversion
 		// to ensure UTF-8 encoding as required by XPathUtil
 		// convert unicode String -> UTF-8 bytes
 		byte[] utf8data = unicodeData.getBytes("UTF-8"); // $NON-NLS-1$
 		ByteArrayInputStream in = new ByteArrayInputStream(utf8data);
 		boolean isXML = JOrphanUtils.isXML(utf8data);
 		// this method assumes UTF-8 input data
 		return XPathUtil.makeDocument(in, false, false, useNameSpace(),
 				isTolerant(), isQuiet(), showWarnings(), reportErrors(), isXML,
 				isDownloadDTDs());
 	}
 
 	/**
 	 * Extract value from Document d by XPath query.
 	 *
 	 * @param d the document
 	 * @param query the query to execute
 	 * @param matchStrings list of matched strings (may include nulls)
 	 * @return the values for x path
 	 * @throws TransformerException the transformer exception
 	 */
 	private void getValuesForXPath(Document d, String query,
 			List<String> matchStrings) throws TransformerException {
 		XPathUtil
 				.putValuesForXPathInList(d, query, matchStrings, getFragment());
 	}
 
 	/**
 	 * Sets the whitespace.
 	 *
 	 * @param selected the new whitespace
 	 */
 	public void setWhitespace(boolean selected) {
 		setProperty(WHITESPACE, selected, false);
 	}
 
 	/**
 	 * Checks if is whitespace.
 	 *
 	 * @return true, if is whitespace
 	 */
 	public boolean isWhitespace() {
 		return getPropertyAsBoolean(WHITESPACE, false);
 	}
 
 	/**
 	 * Sets the validating.
 	 *
 	 * @param selected the new validating
 	 */
 	public void setValidating(boolean selected) {
 		setProperty(VALIDATE, selected);
 	}
 
 	/**
 	 * Checks if is validating.
 	 *
 	 * @return true, if is validating
 	 */
 	public boolean isValidating() {
 		return getPropertyAsBoolean(VALIDATE, false);
 	}
 
 	/**
 	 * Sets the download dt ds.
 	 *
 	 * @param selected the new download dt ds
 	 */
 	public void setDownloadDTDs(boolean selected) {
 		setProperty(DOWNLOAD_DTDS, selected, false);
 	}
 
 	/**
 	 * Checks if is download dt ds.
 	 *
 	 * @return true, if is download dt ds
 	 */
 	public boolean isDownloadDTDs() {
 		return getPropertyAsBoolean(DOWNLOAD_DTDS, false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public List<String> getSearchableTokens() throws Exception {
 		List<String> result = super.getSearchableTokens();
 		result.add(getRefName());
 		result.add(getDefaultValue());
 		result.add(getXPathQuery());
 		return result;
 	}
 	
 	/**
 	 * Do the job - extract value from (X)HTML response using XPath Query.
 	 * Return value as variable defined by REFNAME. Returns DEFAULT value if not
 	 * found.
 	 */
 	public void analyzeSamples() {
 		System.out.println("XPathExtractor (analyzeSamples)");
 		// String sSistemaOperativo = System.getProperty("os.name");
 		// System.out.println("os: " +sSistemaOperativo);
 
 		// Initial part
 		// -For all sample
 		// for all XPATH
 		// for all fragment found
 		// create node (NODE) (1)
 
 		// ManageXPaths handle all the info about the User's XPaths
 		XPath x = new XPath();
 
 		// Check if next lines can be deleted
 		// Put all the ocurrences of the 'xpaths' into xpaths
 		for (int j = 0; j < x.getCountQuerys(); j++) {
 			System.out.println("xpaths!: " + x.getStaticsQuery(j));
 			xpaths.add(x.getStaticsQuery(j));
 		}
 
 		ArrayList<String> webs = getWebListComplete(); // Here we save all the
 														// address
 		// analyzed
 
 		ArrayList<String> websMin = getWebList(); // Here we save all the
 													// address
 		// analyzed
 
 		ArrayList<String> samples = getSampleList(); // Here we save all .html
 														// samples
 		System.out.println("analyzeSamples > samples: " + samples.toString());
 
 		this.listNodes = new ArrayList<TempNode>();
 
 		TempNode webTN;
 		// first we create a node for each URL sampled
 		for (int j = 0; j < websMin.size(); j++) {
 			webTN = new TempNode(websMin.get(j));
 			webTN.setFatherId(); // My father is r00t!
 			listNodes.add(webTN);
 		}
 
 		// For each sample
 		for (int i = 0; i < samples.size(); i++) {
 			System.out.println("analyzeSamples > samples loop i: " + i);
 			treatSampleXPaths(webs.get(i), samples.get(i));
 		}
 
 		orderNodes();
 		writeResultsXml();
 
 	}
 
 
 
 	/**
 	 * Generate an array with all the downloaded samples.
 	 *
 	 * @return list containing all the downloaded samples
 	 */
 	protected ArrayList<String> getSampleList() {
 		System.out.println("XPathExtractor > getSampleList");
 		ArrayList<String> samples = new ArrayList<String>();
 
 		try {
 
 			fileLog = new File(GraphVisualizer.getDestinationFolder() + "/"
 					+ GraphVisualizer.getLogFileName());
 
 			FileReader fr = new FileReader(fileLog);
 
 			BufferedReader br = new BufferedReader(fr);
 			File file;
 			// Lectura del fichero
 			String linea;
 			try {
 				linea = br.readLine();// Debug line
 				while ((linea = br.readLine()) != null) {
 					String sl = linea.toString();
 					System.out.println("sl: " + sl);
 					System.out.println("sl.indexOf('@') "
 							+ Integer.toString(sl.indexOf("@")));
 					System.out.println("(sl.length() - 1) "
 							+ Integer.toString((sl.length() - 1)));
 					String ts = sl
 							.substring(sl.indexOf("@") + 1, (sl.length())); // fixed
 					samples.add(ts);
 					System.out.println("sample:" + samples);
 				}
 			} catch (IOException e) {
 				System.out
 						.println("IOEXception in XPathExtractor > getSampleList");
 			}
 			System.out.println("samples: " + samples.toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return samples;
 	}
 
 	/**
 	 * Generate an array with all the site addresses.
 	 *
 	 * @return list containing all the site addresses
 	 */
 	protected ArrayList<String> getWebList() {
 		System.out.println("XPathExtractor > getWebList");
 		ArrayList<String> webs = new ArrayList<String>();
 
 		System.out.println("getWebList> logFile:"
 				+ GraphVisualizer.getDestinationFolder() + "/"
 				+ GraphVisualizer.getLogFileName());
 		try {
 			fileLog = new File(GraphVisualizer.getDestinationFolder() + "/"
 					+ GraphVisualizer.getLogFileName());
 
 			FileReader fr = new FileReader(fileLog);
 
 			BufferedReader br = new BufferedReader(fr);
 
 			// Lectura del fichero
 			String linea;
 			linea = br.readLine();
 			try {
 				// linea = br.readLine(); // Skip First line, its debug only
 				while ((linea = br.readLine()) != null) {
 					System.out.println(linea);
 					if (!(webs.contains(linea.substring(1, linea.indexOf("@"))))) {
 						/* From # to @ */
 						webs.add(linea.substring(1, linea.indexOf("@")));
 
 						System.out.println("webs:" + webs);
 					}
 				}
 			} catch (IOException e) {
 				System.out
 						.println("IOEXception in XPathExtractor > getWebList "
 								+ e.toString());
 			}
 			System.out.println("webs: " + webs.toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return webs;
 	}
 
 	/**
 	 * Generate an array with all the site addresses.
 	 *
 	 * @return list containing all the site addresses
 	 */
 	protected ArrayList<String> getWebListComplete() {
 		System.out.println("XPathExtractor > getWebList");
 		ArrayList<String> webs = new ArrayList<String>();
 
 		System.out.println("getWebList> logFile:"
 				+ GraphVisualizer.getDestinationFolder() + "/"
 				+ GraphVisualizer.getLogFileName());
 		try {
 			fileLog = new File(GraphVisualizer.getDestinationFolder() + "/"
 					+ GraphVisualizer.getLogFileName());
 
 			FileReader fr = new FileReader(fileLog);
 
 			BufferedReader br = new BufferedReader(fr);
 
 			String linea;
 			linea = br.readLine();
 			try {
 				// linea = br.readLine(); // Skip First line, its debug only
 				while ((linea = br.readLine()) != null) {
 					System.out.println(linea);
 					/* From # to @ */
 					webs.add(linea.substring(1, linea.indexOf("@")));
 
 					System.out.println("webs:" + webs.size());
 
 				}
 			} catch (IOException e) {
 				System.out
 						.println("IOEXception in XPathExtractor > getWebList "
 								+ e.toString());
 			}
 			System.out.println("webs: " + webs.toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return webs;
 	}
 
 	/**
 	 * Order nodes found.
 	 *
 	 */
 	private void orderNodes() {
 
 		TempNode tna, tnb, auxTn;
 
 		/*
 		 * sca == startColumn A. scb == startColumn B. sla == startLine A. slb
 		 * == startLine B. eca == endColumn A. ecb == endColumn B. ela ==
 		 * endLine A. elb == endLine B.
 		 */
 		Integer sca, scb, sla, slb, eca, ecb, ela, elb;
 
 		/*
 		 * For all node - if node.getTarget != 0 skip node - for all nodes
 		 */
 
 		for (int i = 1; i < listNodes.size(); i++) {
 			tna = listNodes.get(i);
 			if (tna.getFatherId() != 0) { // Skip urls
 
 				sca = (Integer) tna.getStartColumnNumber();
 				sla = (Integer) tna.getStartLineNumber();
 				eca = (Integer) tna.getEndColumnNumber();
 				ela = (Integer) tna.getEndLineNumber();
 
 				for (int j = 0; j < (listNodes.size() - i); j++) {
 					tnb = listNodes.get(j);
 					if (tnb.getFatherId() != 0) {
 						scb = (Integer) tnb.getStartColumnNumber();
 						slb = (Integer) tnb.getStartLineNumber();
 						ecb = (Integer) tnb.getEndColumnNumber();
 						elb = (Integer) tnb.getEndLineNumber();
 
 						if (slb > sla) { // greater line
 							auxTn = listNodes.get(j);
 							listNodes.set(j, listNodes.get(j + 1));
 							listNodes.set(j + 1, auxTn);
 
 							// Hack to find lost sons !!!
 							if ((elb < ela) && (ecb < ela)) {
 								tnb.setFatherId(tna.getId());
 							}
 
 						} else if ((slb == sla) && (scb > sca)) {
 							/*
 							 * Same line & greater column
 							 */
 							auxTn = listNodes.get(j);
 							listNodes.set(j, listNodes.get(j + 1));
 							listNodes.set(j + 1, auxTn);
 
 							// Hack to find lost sons !!!
 							if ((elb < ela) && (ecb < ela)) {
 								tnb.setFatherId(tna.getId());
 							}
 						}
 					}
 
 				}
 			}
 		}
 	}
 
 	/**
 	 * Write results xml.
 	 */
 	private void writeResultsXml() {
 
 		ArrayList<Integer> nodeList = new ArrayList<Integer>();
 		String file;
 		String folder;
 		TempNode tn, tn2;
 		System.out.println("writeResultsXml2");
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory
 					.newInstance();
 			
 			
 
 			docBuilder = docFactory.newDocumentBuilder();
 			doc = docBuilder.newDocument();
 			
 
 			// rootElement is the super-root node of the DAG
 			rootElement = doc.createElement("graphml");
 
 			graphNode = doc.createElement("graph");
 			graphNode.setAttribute("id", nextNodeID().toString());
 			graphNode.setAttribute("edgedefault", "directed");
 
 			sizeNode = doc.createElement("key");
 			sizeNode.setAttribute("id", "size");
 			sizeNode.setAttribute("for", "node");
 			sizeNode.setAttribute("attr.name", "fragment.size");
 			sizeNode.setAttribute("attr.type", "integer");
 
 			etiquetaNode = doc.createElement("key");
 			etiquetaNode.setAttribute("id", "etiqueta");
 			etiquetaNode.setAttribute("for", "node");
 			etiquetaNode.setAttribute("attr.name", "staring.tag");
 			etiquetaNode.setAttribute("attr.type", "string");
 
 			rootElement.appendChild(graphNode);
 
 			rootElement.appendChild(sizeNode);
 			rootElement.appendChild(etiquetaNode);
 
 			System.out.println("listNodes.size()" + listNodes.size());
 
 			for (int i = 0; i < listNodes.size(); i++) {
 				System.out.println("listNodes i :" + i);
 				tn = listNodes.get(i);
 
 				// System.out.println("listNodes charged");
 				tagNode = doc.createElement("node");
 				// System.out.println("tagNode created");
 
 				tagNode.setAttribute("id", String.valueOf(tn.getId()));
 
 				if (nodeList.contains(tn.getId())) {
 					/* Avoid adding duplicates */
 					continue;
 				}
 
 				nodeList.add(tn.getId());
 				// System.out.println("setAttribute done");
 
 				dataSize = doc.createElement("data");
 				// System.out.println("dataSize created");
 				dataSize.setAttribute("key", "size");
 				// System.out.println("key size");
 				dataSize.setTextContent(String.valueOf(tn.getSize()));
 				// System.out.println("dataSize setTextContent");
 				// String.valueOf
 				tagNode.appendChild(dataSize);
 
 				dataEtiqueta = doc.createElement("data");
 				dataEtiqueta.setAttribute("key", "etiqueta");
 
 				CDATASection cdata = doc.createCDATASection(tn.getContent());
 				dataEtiqueta.appendChild(cdata);
 
				/* 
				 * dataEtiqueta is commented until final decision
				 * Posibilities: Sourcecode, opener tag, none...
				 */
				//tagNode.appendChild(dataEtiqueta);
 
 				dataWeb = doc.createElement("web");
 				dataWeb.setAttribute("url", tn.getWeb());
 
 				dataDate = doc.createElement("date");
 
 				dataDate.setAttribute("when", tn.getDate());
 
 				rootElement.appendChild(tagNode);
 				// rootElement.appendChild(dataWeb);
 				// rootElement.appendChild(dataDate);
 			}
 
 			// Add edge father-son
 			for (int i = 0; i < listNodes.size(); i++) {
 				System.out.println("WriteResultsXml > add edges : i " + i);
 				tn = listNodes.get(i);
 				String content = tn.getContent();
 
 				edgeNode = doc.createElement("edge");
 				edgeNode.setAttribute("id", Integer.toString(nodeCounter++));
 				edgeNode.setAttribute("source", Integer.toString(tn.getId()));
 				edgeNode.setAttribute("target",
 						Integer.toString(tn.getFatherId()));
 				edgeNode.setAttribute("when", tn.getDate());
 				edgeNode.setAttribute("url", tn.getWeb());
 
 				rootElement.appendChild(edgeNode);
 
 				for (int j = i; j < listNodes.size(); j++) {
 					/* Do not calculate distance with the same node */
 					if (j == i) {
 						continue;
 					}
 					tn2 = listNodes.get(j);
 					String content2 = tn2.getContent();
 					// Only blocks with less than 100 chars of difference
 					// will be compared
 					if (Math.abs(content2.length() - content.length()) < 100) {
 						int dist = LevenshteinDistance
 								.computeLevenshteinDistance(tn.getContent(),
 										tn2.getContent());
 						edgeNode = doc.createElement("edge");
 						edgeNode.setAttribute("id",
 								Integer.toString(nodeCounter++));
 						edgeNode.setAttribute("LevenshteinDistance",
 								Integer.toString(dist));
 						edgeNode.setAttribute("node1",
 								Integer.toString(tn.getId()));
 						edgeNode.setAttribute("node2",
 								Integer.toString(tn2.getId()));
 
 						rootElement.appendChild(edgeNode);
 
 					}
 
 				}
 			}
 
 			doc.appendChild(rootElement);
 			// write the content into XML file
 			TransformerFactory transformerFactory = TransformerFactory
 					.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 
 			/* Patch to fix filenames with white spaces */
 			file = GraphVisualizer.getResultFileName().replaceAll("%20", " ");
 
 			folder = GraphVisualizer.getDestinationFolder().replaceAll("%20",
 					" ");// Patch to fix paths with
 							// white spaces
 			File archivo = new File(folder + "/" + file);
 			StreamResult result = new StreamResult(archivo);
 
 			transformer.transform(source, result);
 		} catch (Exception e) {
 			System.out.println("writeResultsXml exception first try"
 					+ e.toString());
 		}
 	}
 
 	/**
 	 * treatSample - Search and create new nodes for all the XPaths occurrences
 	 * into the sample, at sampleLocation.
 	 *
 	 * @param web the web
 	 * @param sampleLocation the sample location
 	 * @return nothing
 	 */
 	public void treatSampleXPaths(String web, String sampleLocation) {
 
 		System.out.println("treatSample > sample:" + sampleLocation);
 
 		String date = sampleLocation;
 
 		// $ and % delimit where is the date
 		date = date.substring((date.indexOf("$") + 1), date.indexOf("%"));
 
 		// Put all the occurrences of this 'xpaths' into xpaths
 		for (int j = 0; j < XPath.getCountQuerys(); j++) {
 			xpaths.add(XPath.getStaticsQuery(j));
 		}
 
 		System.out.println("xpaths.size(): " + xpaths.size());
 
 		// For each xpath
 		for (int i = 0; i < xpaths.size(); i++) {
 			System.out.println("analyzeSamples > treatSample sampleLocation: "
 					+ sampleLocation);
 			System.out.println("analyzeSamples > treatSample xpaths: "
 					+ xpaths.get(i).toString());
 
 			// treatSampleXpath(web, sampleLocation, xpaths.get(i).toString());
 
 			webClient.setJavaScriptEnabled(false);
 			webClient.setCssEnabled(false);
 
 			TempNode tn;
 			// webClient
 			try {
 				System.out.println("GraphVisualizer.getDestinationFolder(): "
 						+ "file:/" + GraphVisualizer.getDestinationFolder()
 						+ "/" + sampleLocation);
 
 				currentPage = webClient.getPage("file://" + sampleLocation);
 
 				List<?> matches = currentPage.getByXPath(xpaths.get(i));
 
 				System.out.println("matches: " + matches.get(0));
 
 				for (int j = 0; i < matches.size(); j++) {
 
 					tn = new TempNode(((HtmlDivision) matches.get(j)).asXml());
 					tn.setWeb(web);
 					tn.setDate(date);
 
 					tn.setFatherId(web);
 					/* Saving Node limits */
 					tn.setStartColumnNumber(((HtmlDivision) matches.get(j))
 							.getStartColumnNumber());
 					tn.setStartColumnNumber(((HtmlDivision) matches.get(j))
 							.getStartLineNumber());
 					tn.setStartColumnNumber(((HtmlDivision) matches.get(j))
 							.getEndColumnNumber());
 					tn.setStartColumnNumber(((HtmlDivision) matches.get(j))
 							.getEndLineNumber());
 
 					System.out.println("matches.get(i).toString() "
 							+ tn.getContent());
 					System.out.println("New TempNode i:" + tn.getId()
 							+ "getStartColumnNumber:"
 							+ tn.getStartColumnNumber());
 
 					// Adding tn to listNodes
 					XPathExtractor.listNodes.add(tn);
 
 					// treatSampleXpathString(tn.getId(), tn.getContent(), );
 				}
 			} catch (Exception e) {
 				System.out.println("EXception at treatSampleXpath"
 						+ e.toString());
 			}
 		}
 
 	}
 }
