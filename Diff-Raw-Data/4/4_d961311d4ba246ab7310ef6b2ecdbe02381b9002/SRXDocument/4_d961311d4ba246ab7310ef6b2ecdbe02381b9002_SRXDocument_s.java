 /*===========================================================================
   Copyright (C) 2008-2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.lib.segmentation;
 
 import net.sf.okapi.common.DefaultEntityResolver;
 import net.sf.okapi.common.ISegmenter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.NSContextManager;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.resource.TextFragment;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.regex.Pattern;
 
 /**
  * Provides facilities to load, save, and manage segmentation rules in SRX format.
  * This class also implements several extensions to the standard SRX behavior.
  */
 public class SRXDocument {
 	
 	private static final String   NSURI_SRX20 = "http://www.lisa.org/srx20";
 	private static final String   NSURI_SRX10 = "http://www.lisa.org/srx10";
 	private static final String   NSURI_OKPSRX = "http://okapi.sf.net/srx-extensions";
 	private static final String   NSPREFIX_OKPSRX = "okpsrx";
 
 	/**
 	 * Represents the pattern for an inline code (both special characters). 
 	 */
 	public static final String INLINECODE_PATTERN = String.format("([\\u%X\\u%X\\u%X].)",
 		TextFragment.MARKER_OPENING, TextFragment.MARKER_CLOSING, TextFragment.MARKER_ISOLATED);
 	
 	/**
 	 * Marker for INLINECODE_PATTERN in the given pattern.
 	 * \Y+ = one or more codes, \Y* = zero, one or more codes, etc.
 	 */
 	public static final String ANYCODE = "\\Y";
 
 	/**
 	 * Placed at the end of the 'after' expression, this marker indicates the given pattern
 	 * should not have auto-insertion of AUTO_INLINECODES. 
 	 */
 	public static final String NOAUTO = "[noauto]";
 	
 	// Represents zero, one or more inline codes. this is used in auto-insertion cases
 	private static final String AUTO_INLINECODES = "("+INLINECODE_PATTERN+"*)";
 	
 	private boolean cascade;
 	private boolean segmentSubFlows;
 	private boolean includeStartCodes;
 	private boolean includeEndCodes;
 	private boolean includeIsolatedCodes;
 	private boolean oneSegmentIncludesAll;
 	private boolean trimLeadingWS;
 	private boolean trimTrailingWS;
 	private String version = "2.0";
 	private String warning;
 	private String sampleText;
 	private String sampleLanguage;
 	private boolean modified;
 	private boolean testOnSelectedGroup; 
 	private ArrayList<LanguageMap> langMaps;
 	private LinkedHashMap<String, ArrayList<Rule>> langRules;
 	private String maskRule;
 	private String docComment;
 	private String headerComment;
 
 	/**
 	 * Creates an empty SRX document.
 	 */
 	public SRXDocument () {
 		resetAll();
 	}
 
 	/**
 	 * Gets the version of this SRX document.
 	 * @return the version of this SRX document.
 	 */
 	public String getVersion () {
 		return version;
 	}
 	
 	/**
 	 * Indicates if a warning was issued last time a document was read.
 	 * @return true if a warning was issued, false otherwise.
 	 */
 	public boolean hasWarning () {
 		return (( warning != null ) && ( warning.length() > 0 ));
 	}
 
 	/**
 	 * Gets the last warning that was issued while loading a document.
 	 * @return the text of the last warning issued, or an empty string.
 	 */
 	public String getWarning () {
 		if ( warning == null ) return "";
 		else return warning;
 	}
 
 	/**
 	 * Gets the comments associated with the header of this document.
 	 * @return the comments for the header of this document, or null if there are none.
 	 */
 	public String getHeaderComments () {
 		return headerComment;
 	}
 	
 	/**
 	 * Sets the comments for the header of this document.
 	 * @param text the new comments, use null or empty string for removing
 	 * the comments.
 	 */
 	public void setHeaderComments (String text) {
 		headerComment = text;
 		if (( headerComment != null ) && ( headerComment.length() == 0 )) {
 			headerComment = null;
 		}
 	}
 	
 	/**
 	 * Gets the comments associated with this document.
 	 * @return the comments for this document, or null if there are none.
 	 */
 	public String getComments () {
 		return docComment;
 	}
 	
 	/**
 	 * Sets the comments for this document.
 	 * @param text the new comments, use null or empty string for removing
 	 * the comments.
 	 */
 	public void setComments (String text) {
 		docComment = text;
 		if (( docComment != null ) && ( docComment.length() == 0 )) {
 			docComment = null;
 		}
 	}
 	
 	/**
 	 * Resets the document to its default empty initial state.
 	 */
 	public void resetAll () {
 		langMaps = new ArrayList<LanguageMap>();
 		langRules = new LinkedHashMap<String, ArrayList<Rule>>();
 		maskRule = null;
 		modified = false;
 
 		segmentSubFlows = true; // SRX default
 		cascade = false; // There is no SRX default for this
 		includeStartCodes = false; // SRX default
 		includeEndCodes = true; // SRX default
 		includeIsolatedCodes = false; // SRX default
 		
 		oneSegmentIncludesAll = false; // Extension
 		trimLeadingWS = false; // Extension
 		trimTrailingWS = false; // Extension
 
 		sampleText = "Mr. Holmes is from the U.K. not the U.S. <B>Is Dr. Watson from there too?</B> Yes: both are.<BR/>";
 		sampleLanguage = "en";
 		headerComment = null;
 		docComment = null;
 	}
 	
 	/**
 	 * Gets a map of all the language rules in this document.
 	 * @return a map of all the language rules.
 	 */
 	public LinkedHashMap<String, ArrayList<Rule>> getAllLanguageRules () {
 		return langRules;
 	}
 	
 	/**
 	 * Gets the list of rules for a given &lt;languagerule> element.
 	 * @param ruleName the name of the &lt;languagerule> element to query.
 	 * @return the list of rules for a given &lt;languagerule> element.
 	 */
 	public ArrayList<Rule> getLanguageRules (String ruleName) {
 		return langRules.get(ruleName);
 	}
 	
 	/**
 	 * Gets the list of all the language maps in this document. 
 	 * @return the list of all the language maps.
 	 */
 	public ArrayList<LanguageMap> getAllLanguagesMaps () {
 		return langMaps;
 	}
 	
 	/**
 	 * Indicates if sub-flows must be segmented.
 	 * @return true if sub-flows must be segmented, false otherwise.
 	 */
 	public boolean segmentSubFlows () {
 		return segmentSubFlows;
 	}
 	
 	/**
 	 * Sets the flag indicating if sub-flows must be segmented.
 	 * @param value true if sub-flows must be segmented, false otherwise.
 	 */
 	public void setSegmentSubFlows (boolean value) {
 		segmentSubFlows = value;
 	}
 	
 	/**
 	 * Indicates if cascading must be applied when selecting the rules for 
 	 * a given language pattern.
 	 * @return true if cascading must be applied, false otherwise.
 	 */
 	public boolean cascade () {
 		return cascade;
 	}
 	
 	/**
 	 * Sets the flag indicating if cascading must be applied when selecting 
 	 * the rules for a given language pattern.
 	 * @param value true if cascading must be applied, false otherwise.
 	 */
 	public void setCascade (boolean value) {
 		if ( value != cascade ) {
 			cascade = value;
 			modified = true;
 		}
 	}
 	
 	/**
 	 * Indicates if, when there is a single segment in a text, it should include
 	 * the whole text (no spaces or codes trim left/right)
 	 * @return true if a text with a single segment should include the whole
 	 * text.
 	 */
 	public boolean oneSegmentIncludesAll () {
 		return oneSegmentIncludesAll;
 	}
 	
 	/**
 	 * Sets the indicator that tells if when there is a single segment in a 
 	 * text it should include the whole text (no spaces or codes trim left/right)
 	 * text.
 	 * @param value true if a text with a single segment should include the whole
 	 * text.
 	 */
 	public void setOneSegmentIncludesAll (boolean value) {
 		if ( value != oneSegmentIncludesAll ) {
 			oneSegmentIncludesAll = value;
 			modified = true;
 		}
 	}
 
 	/**
 	 * Indicates if leading white-spaces should be left outside the segments.
 	 * @return true if the leading white-spaces should be trimmed.
 	 */
 	public boolean trimLeadingWhitespaces () {
 		return trimLeadingWS;
 	}
 	
 	/**
 	 * Sets the indicator that tells if leading white-spaces should be left outside 
 	 * the segments.
 	 * @param value true if the leading white-spaces should be trimmed.
 	 */
 	public void setTrimLeadingWhitespaces (boolean value) {
 		if ( value != trimLeadingWS ) {
 			trimLeadingWS = value;
 			modified = true;
 		}
 	}
 	
 	/**
 	 * Indicates if trailing white-spaces should be left outside the segments.
 	 * @return true if the trailing white-spaces should be trimmed.
 	 */
 	public boolean trimTrailingWhitespaces () {
 		return trimTrailingWS;
 	}
 	
 	/**
 	 * Sets the indicator that tells if trailing white-spaces should be left outside 
 	 * the segments.
 	 * @param value true if the trailing white-spaces should be trimmed.
 	 */
 	public void setTrimTrailingWhitespaces (boolean value) {
 		if ( value != trimTrailingWS ) {
 			trimTrailingWS = value;
 			modified = true;
 		}
 	}
 	
 	/**
 	 * Indicates if start codes should be included (See SRX implementation notes).
 	 * @return true if start codes should be included, false otherwise.
 	 */
 	public boolean includeStartCodes () {
 		return includeStartCodes;
 	}
 	
 	/**
 	 * Sets the indicator that tells if start codes should be included or not.
 	 * (See SRX implementation notes).
 	 * @param value true if start codes should be included, false otherwise.
 	 */
 	public void setIncludeStartCodes (boolean value) {
 		if ( value != includeStartCodes ) {
 			includeStartCodes = value;
 			modified = true;
 		}
 	}
 	
 	/**
 	 * Indicates if end codes should be included (See SRX implementation notes).
 	 * @return true if end codes should be included, false otherwise.
 	 */
 	public boolean includeEndCodes () {
 		return includeEndCodes;
 	}
 	
 	/**
 	 * Sets the indicator that tells if end codes should be included or not.
 	 * (See SRX implementation notes).
 	 * @param value true if end codes should be included, false otherwise.
 	 */
 	public void setIncludeEndCodes (boolean value) {
 		if ( value != includeEndCodes ) {
 			includeEndCodes = value;
 			modified = true;
 		}
 	}
 	
 	/**
 	 * Indicates if isolated codes should be included (See SRX implementation notes).
 	 * @return true if isolated codes should be included, false otherwise.
 	 */
 	public boolean includeIsolatedCodes () {
 		return includeIsolatedCodes;
 	}
 	
 	/**
 	 * Sets the indicator that tells if isolated codes should be included or not.
 	 * (See SRX implementation notes).
 	 * @param value true if isolated codes should be included, false otherwise.
 	 */
 	public void setIncludeIsolatedCodes (boolean value) {
 		if ( value != includeIsolatedCodes ) {
 			includeIsolatedCodes = value;
 			modified = true;
 		}
 	}
 
 	/**
 	 * Gets the current pattern of the mask rule.
 	 * @return the current pattern of the mask rule.
 	 */
 	public String getMaskRule () {
 		return maskRule;
 	}
 	
 	/**
 	 * Sets the pattern for the mask rule.
 	 * @param pattern the new pattern to use for the mask rule.
 	 */
 	public void setMaskRule (String pattern) {
 		if ( pattern != null ) {
 			if ( !pattern.equals(maskRule) ) {
 				modified = true;
 			}
 		}
 		else if ( maskRule != null ) {
 			modified = true;
 		}
 		maskRule = pattern;
 	}
 	
 	/**
 	 * Gets the current sample text. This text is an example string that can be used
 	 * to test the various rules. It can be handy to be able to save it along with
 	 * the SRX document.
 	 * @return the sample text, or an empty string.
 	 */
 	public String getSampleText () {
 		if ( sampleText == null ) return "";
 		else return sampleText;
 	}
 	
 	/**
 	 * Sets the sample text.
 	 * @param value the new sample text.
 	 */
 	public void setSampleText (String value) {
 		if ( value != null ) {
 			if ( !value.equals(sampleText) ) {
 				modified = true;
 			}
 		}
 		else if ( sampleText != null ) {
 			modified = true;
 		}
 		sampleText = value;
 	}
 	
 	/**
 	 * Gets the current sample language code.
 	 * @return the current sample language code.
 	 */
 	public String getSampleLanguage () {
 		return sampleLanguage;
 	}
 	
 	/**
 	 * Sets the sample language code. Null or empty strings are changed
 	 * to the default language.
 	 * @param value the new sample language code.
 	 */
 	public void setSampleLanguage (String value) {
 		if (( value == null ) || ( value.length() == 0 )) {
 			sampleLanguage = "en";
 			modified = true;
 		}
 		else {
 			if ( !value.equals(sampleLanguage) ) {
 				sampleLanguage = value;
 				modified = true;
 			}
 		}
 	}
 	
 	/**
 	 * Indicates that, when sampling the rules, the sample should be
 	 * computed using only a selected group of rules.
 	 * @return true to test using only a selected group of rules.
 	 * False to test using all the rules matching a given language.
 	 */
 	public boolean testOnSelectedGroup () {
 		return testOnSelectedGroup;
 	}
 	
 	/**
 	 * Sets the indicator on how to apply rules for samples.
 	 * @param value true to test using only a selected group of rules.
 	 * False to test using all the rules matching a given language.
 	 */
 	public void setTestOnSelectedGroup (boolean value) {
 		if ( value != testOnSelectedGroup ) {
 			testOnSelectedGroup = value;
 			modified = true;
 		}
 	}
 	
 	/**
 	 * Indicates if the document has been modified since the last load or save.
 	 * @return true if the document have been modified, false otherwise.
 	 */
 	public boolean isModified () {
 		return modified;
 	}
 	
 	/**
 	 * Sets the flag indicating if the document has been modified since the last
 	 * load or save. If you make change to the rules or language maps directly to 
 	 * the lists, make sure to set this flag to true.
 	 * @param value true if the document has been changed, false otherwise.
 	 */
 	public void setModified (boolean value) {
 		modified = value;
 	}
 	
 	/**
 	 * Adds a language rule to this SRX document. If another language rule
 	 * with the same name exists already it will be replaced by the
 	 * new one, without warning.
 	 * @param name name of the language rule to add.
 	 * @param langRule language rule object to add.
 	 */
 	public void addLanguageRule (String name,
 		ArrayList<Rule> langRule)
 	{
 		langRules.put(name, langRule);
 		modified = true;
 	}
 	
 	/**
 	 * Adds a language map to this document. The new map is added
 	 * at the end of the one already there.
 	 * @param langMap the language map object to add.
 	 */
 	public void addLanguageMap (LanguageMap langMap) {
 		langMaps.add(langMap);
 		modified = true;
 	}
 	
 	/**
 	 * Compiles the all language rules applicable for a given language code, and
 	 * assign them to a segmenter. This method applies the language code you 
 	 * specify to the language mappings
 	 * currently available in the document and compile the rules
 	 * when one or more language map is found. The matching is done in
 	 * the order of the list of language maps and more than one can be 
 	 * selected if {@link #cascade()} is true.
 	 * @param languageCode the language code. the value should be a 
 	 * BCP-47 value (e.g. "de", "fr-ca", etc.)
 	 * @param existingSegmenter optional existing SRXSegmenter object to re-use.
 	 * Use null for not re-using anything.
 	 * @return the instance of the segmenter with the new compiled rules.
 	 */
 	public ISegmenter compileLanguageRules (LocaleId languageCode,
 		ISegmenter existingSegmenter)
 	{
 		SRXSegmenter segmenter = null;
 		if (( existingSegmenter != null ) && ( existingSegmenter instanceof SRXSegmenter )) {
 			segmenter = (SRXSegmenter)existingSegmenter;
 		}
 
 		if ( segmenter != null ) {
 			// Check if we really need to re-compile
 			if ( languageCode != null ) {
 				if ( languageCode.equals(segmenter.getLanguage())
 					&& (cascade == segmenter.cascade()) )
 					return segmenter;
 			}
 			segmenter.reset();
 		}
 		else {
 			segmenter = new SRXSegmenter();
 		}
 		
 		segmenter.setCascade(cascade);
 		segmenter.setOptions(segmentSubFlows, includeStartCodes,
 			includeEndCodes, includeIsolatedCodes, 	oneSegmentIncludesAll,
 			trimLeadingWS, trimTrailingWS);
 		
 		for ( LanguageMap langMap : langMaps ) {
 			if ( Pattern.matches(langMap.pattern, languageCode.toString()) ) {
 				compileRules(segmenter, langMap.ruleName);
 				if ( !segmenter.cascade() ) break; // Stop at the first matching map
 			}
 		}
 
 		segmenter.setLanguage(languageCode);
 		return segmenter;
 	}
 	
 	/**
 	 * Compiles a single language rule group and assign it to a segmenter.
 	 * @param ruleName the name of the rule group to apply.
 	 * @param existingSegmenter optional existing SRXSegmenter object to re-use.
 	 * Use null for not re-using anything.
 	 * @return the instance of the segmenter with the new compiled rules.
 	 */
 	public ISegmenter compileSingleLanguageRule (String ruleName,
 		ISegmenter existingSegmenter)
 	{
 		SRXSegmenter segmenter = null;
 		if (( existingSegmenter != null ) && ( existingSegmenter instanceof SRXSegmenter )) {
 			segmenter = (SRXSegmenter)existingSegmenter;
 		}
 		
 		if ( segmenter != null ) {
 			// Check if we really need to re-compile
 			if ( ruleName != null ) {
 				if ( segmenter.getLanguage().equals(LocaleId.EMPTY) )
 					return segmenter;
 			}
 			segmenter.reset();
 		}
 		else {
 			segmenter = new SRXSegmenter();
 		}
 
 		segmenter.setOptions(segmentSubFlows, includeStartCodes,
 			includeEndCodes, includeIsolatedCodes, oneSegmentIncludesAll,
 			trimLeadingWS, trimTrailingWS);
 		compileRules(segmenter, ruleName);
 		segmenter.setLanguage(LocaleId.EMPTY);
 		return segmenter;
 	}
 
 	/**
 	 * Compiles a language rule into the current set of active rules.
 	 * @param ruleName the name of the language rule to compile.
 	 */
 	private void compileRules (SRXSegmenter segmenter,
 		String ruleName)
 	{
 		if ( !langRules.containsKey(ruleName) ) {
 			throw new SegmentationRuleException("language rule '"+ruleName+"' not found.");
 		}
 		ArrayList<Rule> langRule = langRules.get(ruleName);
 		String pattern;
 		for ( Rule rule : langRule ) {
 			if ( rule.isActive ) {
 				if ( rule.before.endsWith(NOAUTO)) {
 					// If the rule.before ends with NOAUTO, then we do not put pattern for in-line codes
 					pattern = "("+rule.before.substring(0, rule.before.length()-NOAUTO.length())
 						+")("+rule.after+")";
 				}
 				else {
 					// The compiled rule is made of two groups: the pattern before and the pattern after
 					// the break. A special pattern for in-line codes is also added transparently.
 					pattern = "("+rule.before+AUTO_INLINECODES+")("+rule.after+")";
 				}
 				// Replace special markers ANYCODES by inline code pattern
 				pattern = pattern.replace(ANYCODE, INLINECODE_PATTERN);
 				// Compile and add the rule
 				segmenter.addRule(new CompiledRule(pattern, rule.isBreak));
 			}
 		}
 		
 		// Range rules
 		segmenter.setMaskRule(maskRule);
 	}
 	
 	/**
 	 * Loads an SRX document from a CharSequence object.
 	 * Calling this method resets all settings and rules to their default
 	 * state and then populate them with the data stored in the document being loaded. 
 	 * The rules can be embedded inside another vocabulary.
 	 * @param data the string containing the SRX document to load.
 	 */
 	public void loadRules (CharSequence data) {
 		loadRules(data, 1);
 		modified = true;
 	}
 	
 	/**
 	 * Loads an SRX document from a file.
 	 * Calling this method resets all settings and rules to their default
 	 * state and then populate them with the data stored in the document being loaded. 
 	 * The rules can be embedded inside another vocabulary.
 	 * @param pathOrURL The full path or URL of the document to load.
 	 */
 	public void loadRules (String pathOrURL) {
 		loadRules(pathOrURL, 0);
 	}			
 
 	/**
 	 * Loads an SRX document from an input stream.
 	 * Calling this method resets all settings and rules to their default
 	 * state and then populate them with the data stored in the document being loaded. 
 	 * The rules can be embedded inside another vocabulary.
 	 * @param inputStream the input stream to read from.
 	 */
 	public void loadRules (InputStream inputStream) {
 		loadRules(inputStream, 2);
 	}
 	
 	private void loadRules (Object input,
 		int inputType )
 	{
 		try {
 			DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
 			Fact.setValidating(false);
 			Fact.setNamespaceAware(true);
 			DocumentBuilder docBuilder;
 			docBuilder = Fact.newDocumentBuilder();
 			docBuilder.setEntityResolver(new DefaultEntityResolver());
 
 			Document doc;
 			if ( inputType == 0 ) {
 				String pathOrURL = (String)input;
 				//doc = docBuilder.parse(Util.makeURIFromPath(pathOrURL));
				doc = docBuilder.parse(new File(pathOrURL));
 			}
 			else if ( inputType == 1 ) {
 				CharSequence data = (CharSequence)input;
 				doc = docBuilder.parse(new InputSource(new StringReader(data.toString())));
 			}
 			else {
 				doc = docBuilder.parse((InputStream)input);
 			}
 
 			resetAll();
 			// Macintosh work-around
 			// When you use -XstartOnFirstThread as a java -Xarg on Leopard, your ContextClassloader gets set to null.
 			// That is not the case on 10.4 or with Windows or Linux flavors
 			// This allows XPathFactory.newInstance() to have a non-null context
 			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
 			// end work-around
 			XPathFactory xpathFac = XPathFactory.newInstance();
 
 			XPath xpath = xpathFac.newXPath();
 			NSContextManager nsContext = new NSContextManager();
 			nsContext.add("srx", NSURI_SRX20);
 			nsContext.add(NSPREFIX_OKPSRX, NSURI_OKPSRX);
 			nsContext.add("srx1", NSURI_SRX10);
 			xpath.setNamespaceContext(nsContext);
 
 			// Try to get the root and detect if namespaces are used or not. 
 			String ns = NSURI_SRX20;
 			XPathExpression xpe = xpath.compile("//srx:srx");
 			NodeList srxList = (NodeList)xpe.evaluate(doc, XPathConstants.NODESET);
 			if ( srxList.getLength() < 1 ) {
 				xpe = xpath.compile("//srx1:srx");
 				srxList = (NodeList)xpe.evaluate(doc, XPathConstants.NODESET);
 				if ( srxList.getLength() < 1 ) {
 					xpe = xpath.compile("//srx");
 					srxList = (NodeList)xpe.evaluate(doc, XPathConstants.NODESET);
 					if ( srxList.getLength() < 1 ) {
 						return;
 					}
 					ns = "";
 				}
 				else ns = NSURI_SRX10;
 			}
 			
 			// Treat the first occurrence (we assume there is never more in one file)
 			Element srxElem = (Element)srxList.item(0);
 			docComment = getPreviousComments(srxElem, null);
 			String tmp = srxElem.getAttribute("version");
 			if ( tmp.equals("1.0") ) {
 				version = tmp;
 				warning = "SRX version 1.0 rules are subject to different interpretation.\nRead the help for more information.";
 			}
 			else if ( tmp.equals("2.0") ) {
 				version = tmp;
 				warning = null;
 			}
 			else throw new OkapiIOException("Invalid version value.");
 			
 			Element elem1 = getFirstElementByTagNameNS(ns, "header", srxElem);
 			headerComment = getPreviousComments(elem1, null);
 			
 			tmp = elem1.getAttribute("segmentsubflows");
 			if ( tmp.length() > 0 ) segmentSubFlows = "yes".equals(tmp);
 			tmp = elem1.getAttribute("cascade");
 			if ( tmp.length() > 0 ) cascade = "yes".equals(tmp);
 
 			// formathandle elements
 			NodeList list2 = elem1.getElementsByTagNameNS(ns, "formathandle");
 			for ( int i=0; i<list2.getLength(); i++ ) {
 				Element elem2 = (Element)list2.item(i);
 				tmp = elem2.getAttribute("type");
 				if ( "start".equals(tmp) ) {
 					tmp = elem2.getAttribute("include");
 					if ( tmp.length() > 0 ) includeStartCodes = "yes".equals(tmp); 
 				}
 				else if ( "end".equals(tmp) ) {
 					tmp = elem2.getAttribute("include");
 					if ( tmp.length() > 0 ) includeEndCodes = "yes".equals(tmp); 
 				}
 				else if ( "isolated".equals(tmp) ) {
 					tmp = elem2.getAttribute("include");
 					if ( tmp.length() > 0 ) includeIsolatedCodes = "yes".equals(tmp); 
 				}
 			}
 			
 			// Extension: options
 			Element elem2 = getFirstElementByTagNameNS(NSURI_OKPSRX, "options", elem1);
 			if ( elem2 != null ) {
 				tmp = elem2.getAttribute("oneSegmentIncludesAll");
 				if ( tmp.length() > 0 ) oneSegmentIncludesAll = "yes".equals(tmp);
 				
 				tmp = elem2.getAttribute("trimLeadingWhitespaces");
 				if ( tmp.length() > 0 ) trimLeadingWS = "yes".equals(tmp);
 				
 				tmp = elem2.getAttribute("trimTrailingWhitespaces");
 				if ( tmp.length() > 0 ) trimTrailingWS = "yes".equals(tmp);
 			}
 
 			// Extension: sample
 			elem2 = getFirstElementByTagNameNS(NSURI_OKPSRX, "sample", elem1);
 			if ( elem2 != null ) {
 				setSampleText(Util.getTextContent(elem2));
 				tmp = elem2.getAttribute("language");
 				if ( tmp.length() > 0 ) setSampleLanguage(tmp);
 				tmp = elem2.getAttribute("useMappedRules");
 				if ( tmp.length() > 0 ) setTestOnSelectedGroup("no".equals(tmp));
 			}
 			
 			// Extension: rangeRule
 			elem2 = getFirstElementByTagNameNS(NSURI_OKPSRX, "rangeRule", elem1);
 			if ( elem2 != null ) {
 				setMaskRule(Util.getTextContent(elem2));
 			}
 			
 			// Get the body element
 			elem1 = getFirstElementByTagNameNS(ns, "body", srxElem);
 			
 			// languagerules
 			elem2 = getFirstElementByTagNameNS(ns, "languagerules", elem1);
 			if ( elem2 == null ) {
 				throw new RuntimeException("the languagerules element is missing.");
 			}
 			// For each languageRule
 			list2 = elem2.getElementsByTagNameNS(ns, "languagerule");
 			for ( int i=0; i<list2.getLength(); i++ ) {
 				Element elem3 = (Element)list2.item(i);
 				ArrayList<Rule> tmpList = new ArrayList<Rule>();
 				String ruleName = elem3.getAttribute("languagerulename");
 				// For each rule
 				NodeList list3 = elem3.getElementsByTagNameNS(ns, "rule");
 				for ( int j=0; j<list3.getLength(); j++ ) {
 					Element elem4 = (Element)list3.item(j);
 					Rule newRule = new Rule();
 					newRule.comment = getPreviousComments(elem4, "rule"); 
 					tmp = elem4.getAttribute("break");
 					if ( tmp.length() > 0 ) newRule.isBreak = "yes".equals(tmp);
 					tmp = elem4.getAttributeNS(NSURI_OKPSRX, "active");
 					if ( tmp.length() > 0 ) newRule.isActive = "yes".equals(tmp);
 					Element elem5 = getFirstElementByTagNameNS(ns, "beforebreak", elem4);
 					if ( elem5 != null ) newRule.before = Util.getTextContent(elem5);
 					elem5 = getFirstElementByTagNameNS(ns, "afterbreak", elem4);
 					if ( elem5 != null ) newRule.after = Util.getTextContent(elem5);
 					tmpList.add(newRule);
 				}
 				langRules.put(ruleName, tmpList);
 			}
 
 			// maprules
 			elem2 = getFirstElementByTagNameNS(ns, "maprules", elem1);
 			// For each languagemap
 			list2 = elem2.getElementsByTagNameNS(ns, "languagemap");
 			for ( int i=0; i<list2.getLength(); i++ ) {
 				Element elem3 = (Element)list2.item(i);
 				LanguageMap langMap = new LanguageMap();
 				tmp = elem3.getAttribute("languagepattern");
 				if ( tmp.length() > 0 ) langMap.pattern = tmp;
 				tmp = elem3.getAttribute("languagerulename");
 				if ( tmp.length() > 0 ) langMap.ruleName = tmp;
 				langMaps.add(langMap);
 			}
 			modified = false;
 		}
 		catch ( SAXException e ) {
 			throw new OkapiIOException(e);
 		}
 		catch ( ParserConfigurationException e ) {
 			throw new OkapiIOException(e);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 		catch ( XPathExpressionException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 	
 	/**
 	 * Gathers comments before a given element.
 	 * @param startNode the node where to start. Use null to allow the gathering to go at
 	 * the parent level.
 	 * @param stopElement the name of the node where to stop, or null for no limitation.
 	 * @return the string with all the comments found in the given scope, or null if
 	 * no comments were found.
 	 */
 	private String getPreviousComments (Node startNode,
 		String stopElement)
 	{
 		Node node = startNode.getPreviousSibling();
 		while ( node != null ) {
 			switch ( node.getNodeType() ) {
 			case Node.COMMENT_NODE:
 				return node.getNodeValue();
 			case Node.ELEMENT_NODE:
 				if (( stopElement != null ) && ( node.getNodeName().equals(stopElement) )) {
 					return null;
 				}
 				break;
 			}
 			node = node.getPreviousSibling(); 
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the first occurrence of a given element in a given namespace
 	 * from a given element.
 	 * @param ns the namespace URI to look for.
 	 * @param tagName the name of the element to look for.
 	 * @param elem the element where to look for.
 	 * @return the first found element, or null.
 	 */
 	private Element getFirstElementByTagNameNS (String ns,
 		String tagName,
 		Element elem)
 	{
 		NodeList list = elem.getElementsByTagNameNS(ns, tagName);
 		if (( list == null ) || ( list.getLength() < 1 )) return null;
 		return (Element)list.item(0);
 	}
 
 	/**
 	 * Saves the current rules to an SRX string.
 	 * @param saveExtensions true to save Okapi SRX extensions, false otherwise.
 	 * @param saveNonValidInfo true to save non-SRX-valid attributes, false otherwise.
 	 * @return the string containing the saved SRX rules.
 	 */
 	public String saveRulesToString (boolean saveExtensions,
 		boolean saveNonValidInfo)
 	{
         StringWriter strWriter = new StringWriter();
 		XMLWriter writer = new XMLWriter(strWriter);
 		boolean current = modified;
 		saveRules(writer, saveExtensions, saveNonValidInfo);
 		modified = current; // Keep the same state for modified
 		writer.close();
 		return strWriter.toString();
 	}
 
 	/**
 	 * Saves the current rules to an SRX rules document.
 	 * @param rulesPath the full path of the file where to save the rules.
 	 * @param saveExtensions true to save Okapi SRX extensions, false otherwise.
 	 * @param saveNonValidInfo true to save non-SRX-valid attributes, false otherwise.
 	 */
 	public void saveRules (String rulesPath,
 		boolean saveExtensions,
 		boolean saveNonValidInfo)
 	{
 		XMLWriter writer = new XMLWriter(rulesPath);
 		saveRules(writer, saveExtensions, saveNonValidInfo);
 	}
 	
 	private void saveRules (XMLWriter writer,
 		boolean saveExtensions,
 		boolean saveNonValidInfo)
 	{
 		try {
 			writer.writeStartDocument();
 			if ( docComment != null ) {
 				writer.writeComment(docComment, true);
 			}
 			writer.writeStartElement("srx");
 			writer.writeAttributeString("xmlns", NSURI_SRX20);
 			if ( saveExtensions ) {
 				writer.writeAttributeString("xmlns:"+NSPREFIX_OKPSRX, NSURI_OKPSRX);
 			}
 			writer.writeAttributeString("version", "2.0");
 			version = "2.0";
 			writer.writeLineBreak();
 			
 			if ( headerComment != null ) {
 				writer.writeComment(headerComment, true);
 			}
 			writer.writeStartElement("header");
 			writer.writeAttributeString("segmentsubflows", (segmentSubFlows ? "yes" : "no"));
 			writer.writeAttributeString("cascade", (cascade ? "yes": "no"));
 			writer.writeLineBreak();
 
 			writer.writeStartElement("formathandle");
 			writer.writeAttributeString("type", "start");
 			writer.writeAttributeString("include", (includeStartCodes ? "yes" : "no"));
 			writer.writeEndElementLineBreak(); // formathandle
 			
 			writer.writeStartElement("formathandle");
 			writer.writeAttributeString("type", "end");
 			writer.writeAttributeString("include", (includeEndCodes ? "yes" : "no"));
 			writer.writeEndElementLineBreak(); // formathandle
 			
 			writer.writeStartElement("formathandle");
 			writer.writeAttributeString("type", "isolated");
 			writer.writeAttributeString("include", (includeIsolatedCodes ? "yes" : "no"));
 			writer.writeEndElementLineBreak(); // formathandle
 			
 			if ( saveExtensions ) {
 				writer.writeStartElement(NSPREFIX_OKPSRX+":options");
 				writer.writeAttributeString("oneSegmentIncludesAll",
 					(oneSegmentIncludesAll ? "yes" : "no"));
 				writer.writeAttributeString("trimLeadingWhitespaces",
 					(trimLeadingWS ? "yes" : "no"));
 				writer.writeAttributeString("trimTrailingWhitespaces",
 					(trimTrailingWS ? "yes" : "no"));
 				writer.writeEndElementLineBreak(); // okpsrx:options
 
 				writer.writeStartElement(NSPREFIX_OKPSRX+":sample");
 				writer.writeAttributeString("language", getSampleLanguage());
 				writer.writeAttributeString("useMappedRules", (testOnSelectedGroup() ? "no" : "yes"));
 				writer.writeString(getSampleText());
 				writer.writeEndElementLineBreak(); // okpsrx:sample
 			
 				writer.writeStartElement(NSPREFIX_OKPSRX+":rangeRule");
 				writer.writeString(getMaskRule());
 				writer.writeEndElementLineBreak(); // okpsrx:rangeRule
 			}
 
 			writer.writeEndElementLineBreak(); // header
 
 			writer.writeStartElement("body");
 			writer.writeLineBreak();
 			
 			writer.writeStartElement("languagerules");
 			writer.writeLineBreak();
 			for ( String ruleName : langRules.keySet() ) {
 				writer.writeStartElement("languagerule");
 				writer.writeAttributeString("languagerulename", ruleName);
 				writer.writeLineBreak();
 				ArrayList<Rule> langRule = langRules.get(ruleName);
 				for ( Rule rule : langRule ) {
 					if ( rule.comment != null ) {
 						writer.writeComment(rule.comment, true);
 					}
 					writer.writeStartElement("rule");
 					writer.writeAttributeString("break", (rule.isBreak ? "yes" : "no"));
 					// Start of non-standard SRX 2.0 (non-SRX attributes not allowed)
 					if ( saveExtensions && saveNonValidInfo ) {
 						writer.writeAttributeString(NSPREFIX_OKPSRX+":active", (rule.isActive ? "yes" : "no"));
 					}
 					// End of non-Standard SRX
 					writer.writeLineBreak();
 					writer.writeElementString("beforebreak", rule.before);
 					writer.writeLineBreak();					
 					writer.writeElementString("afterbreak", rule.after);
 					writer.writeLineBreak();					
 					writer.writeEndElementLineBreak(); // rule
 				}
 				writer.writeEndElementLineBreak(); // languagerule
 			}
 			writer.writeEndElementLineBreak(); // languagerules
 			
 			writer.writeStartElement("maprules");
 			writer.writeLineBreak();			
 			for ( LanguageMap langMap : langMaps ) {
 				writer.writeStartElement("languagemap");
 				writer.writeAttributeString("languagepattern", langMap.pattern);
 				writer.writeAttributeString("languagerulename", langMap.ruleName);
 				writer.writeEndElementLineBreak(); // languagemap
 			}
 			writer.writeEndElementLineBreak(); // maprules
 			
 			writer.writeEndElementLineBreak(); // body
 			
 			writer.writeEndElementLineBreak(); // srx
 			writer.writeEndDocument();
 			modified = false;
 		}
 		finally {
 			if ( writer != null ) writer.close();
 		}
 	}
 
 }
