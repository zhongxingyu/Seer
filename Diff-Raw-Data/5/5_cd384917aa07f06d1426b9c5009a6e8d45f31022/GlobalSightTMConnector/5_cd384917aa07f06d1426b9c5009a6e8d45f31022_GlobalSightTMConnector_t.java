 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.connectors.globalsight;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.axis.AxisFault;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.globalsight.webservices.WebServiceException;
 import com.globalsight.www.webservices.Ambassador;
 import com.globalsight.www.webservices.AmbassadorWebServiceSoapBindingStub;
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
 import net.sf.okapi.common.query.MatchType;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.lib.translation.BaseConnector;
 import net.sf.okapi.lib.translation.ITMQuery;
 import net.sf.okapi.lib.translation.QueryResult;
 
 public class GlobalSightTMConnector extends BaseConnector implements ITMQuery {
 
 	private List<QueryResult> results;
 	private int current = -1;
 	private int maxHits = 25;
 	private int threshold = 75;
 	private Ambassador gsWS;
 	private String gsToken;
 	private String gsTmProfile;
 	private Parameters params;
 	private DocumentBuilder docBuilder;
 
 	public GlobalSightTMConnector () {
 		params = new Parameters();
 		DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
 		Fact.setValidating(false);
 		try {
 			docBuilder = Fact.newDocumentBuilder();
 		}
 		catch ( ParserConfigurationException e ) {
 			throw new RuntimeException("Error creating document builder.", e);
 		}
 	}
 
 	@Override
 	public String getName () {
 		return "GlobalSight-TM";
 	}
 
 	@Override
 	public String getSettingsDisplay () {
 		return String.format("URL: %s\nTM profile: %s",
 			params.getServerURL(), params.getTmProfile());
 	}
 	
 	@Override
 	public void close () {
 	}
 
 	@Override
 	public boolean hasNext () {
 		if ( results == null ) return false;
 		if ( current >= results.size() ) {
 			current = -1;
 		}
 		return (current > -1);
 	}
 
 	@Override
 	public QueryResult next () {
 		if ( results == null ) return null;
 		if (( current > -1 ) && ( current < results.size() )) {
 			current++;
 			return results.get(current-1);
 		}
 		current = -1;
 		return null;
 	}
 
 	@Override
 	public void open () {
 		try {
 			URL url = new URL(params.getServerURL());
 			gsWS = new AmbassadorWebServiceSoapBindingStub(url, null);
 			gsToken = gsWS.login(params.getUsername(), params.getPassword());
 			// Remove the end part
 			int n = gsToken.lastIndexOf("+_+");
 			gsToken = gsToken.substring(0, n);
 			gsTmProfile = params.getTmProfile();
 			results = new ArrayList<QueryResult>();
 		}
 		catch ( AxisFault e ) {
 			throw new RuntimeException("Error creating the GlobalSight Web services.", e);
 		}
 		catch ( RemoteException e ) {
 			throw new RuntimeException("Error when login.", e);
 		}
 		catch ( MalformedURLException e ) {
 			throw new RuntimeException("Invalid server URL.", e);
 		}
 	}
 
 	@Override
 	public int query (TextFragment frag) {
 		results.clear();
 		if ( !frag.hasText() ) return 0;
 		try {
 			String text = frag.getCodedText();
 			if ( frag.hasCode() ) {
 				StringBuilder tmp = new StringBuilder();
 				Code code;
 				for ( int i=0; i<text.length(); i++ ) {
 					switch ( text.charAt(i) ) {
 					case TextFragment.MARKER_OPENING:
 						code = frag.getCode(text.charAt(++i));
 						tmp.append(String.format("<bpt i=\"%d\" x=\"%d\" type=\"text\"/>", code.getId(), code.getId()));
 						break;
 					case TextFragment.MARKER_CLOSING:
 						code = frag.getCode(text.charAt(++i));
 						tmp.append(String.format("<ept i=\"%d\"/>", code.getId()-1));
 						break;
 					case TextFragment.MARKER_ISOLATED:
 						code = frag.getCode(text.charAt(++i));
 						tmp.append(String.format("<ph i=\"%d\" type=\"text\"/>", code.getId()));
 						break;
 					default:
 						tmp.append(text.charAt(i));
 						break;
 					}
 				}
 				text = tmp.toString();
 			}
 
 			String xmlRes = gsWS.searchEntries(gsToken, gsTmProfile, text, srcCode);
 			Document doc = docBuilder.parse(new InputSource(new StringReader(xmlRes)));
 			NodeList list1 = doc.getElementsByTagName("entry");
 			Element elem;
 			NodeList list2, list3;
 			QueryResult res;
 			for ( int i=0; i<list1.getLength(); i++ ) {
 				if ( i >= maxHits ) break;
 				
 				elem = (Element)list1.item(i);
 				list2 = elem.getElementsByTagName("percentage");
 				res = new QueryResult();
 				res.weight = getWeight();
				res.score = Float.valueOf(Util.getTextContent(list2.item(0)).replace("%", "")).intValue();
 				if ( res.score < threshold ) continue;
 				
 				if ( res.score >= 100 ) res.matchType = MatchType.EXACT;
 				else if ( res.score > 0 ) res.matchType = MatchType.FUZZY;
 				
 				list2 = elem.getElementsByTagName("tm");
 				res.origin = Util.getTextContent(list2.item(0));
 
 				list2 = elem.getElementsByTagName("source");
 				list3 = ((Element)list2.item(0)).getElementsByTagName("segment");
 				res.source = readSegment((Element)list3.item(0), frag);
 
 				list2 = elem.getElementsByTagName("target");
 				list3 = ((Element)list2.item(0)).getElementsByTagName("segment");
 				res.target = readSegment((Element)list3.item(0), frag);
 
 
 				results.add(res);
 			}
 
 		}
 		catch ( WebServiceException e ) {
 			throw new RuntimeException("Error querying TM.", e);
 		}
 		catch ( RemoteException e ) {
 			throw new RuntimeException("Error querying TM.", e);
 		}
 		catch ( SAXException e ) {
 			throw new RuntimeException("Error with query results.", e);
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException("Error with query results.", e);
 		}
 		if ( results.size() > 0 ) current = 0;
 		return results.size();
 	}
 
 	@Override
 	public List<List<QueryResult>> batchQuery (List<TextFragment> fragments) {
 		throw new OkapiNotImplementedException();
 	}
 	
 //	public int queryV5 (TextFragment frag) {
 //		/* The GlobalSight TM Web service does not support query with inline codes
 //		 * for the time being (v7.1.3), so we query plain text to get the best match 
 //		 * possible. But queries with codes will never get an exact match even if one 
 //		 * exists in the TM.
 //		 */
 //		results.clear();
 //		if ( !frag.hasText() ) return 0;
 //		try {
 //			String qtext = frag.getCodedText();
 //			StringBuilder tmpCodes = new StringBuilder();
 //			if ( frag.hasCode() ) {
 //				StringBuilder tmpText = new StringBuilder();
 //				for ( int i=0; i<qtext.length(); i++ ) {
 //					switch ( qtext.charAt(i) ) {
 //					case TextFragment.MARKER_OPENING:
 //					case TextFragment.MARKER_CLOSING:
 //					case TextFragment.MARKER_ISOLATED:
 //					case TextFragment.MARKER_SEGMENT:
 //						tmpCodes.append(qtext.charAt(i));
 //						tmpCodes.append(qtext.charAt(++i));
 //						break;
 //					default:
 //						tmpText.append(qtext.charAt(i));
 //					}
 //				}
 //				qtext = tmpText.toString();
 //			}
 //
 //			String xmlRes = gsWS.searchEntries(gsToken, gsTmProfile, qtext, srcLang);
 //			Document doc = docBuilder.parse(new InputSource(new StringReader(xmlRes)));
 //			NodeList list1 = doc.getElementsByTagName("entry");
 //			Element elem;
 //			NodeList list2;
 //			NodeList list3;
 //			QueryResult res;
 //			for ( int i=0; i<list1.getLength(); i++ ) {
 //				if ( i >= maxHits ) break;
 //				
 //				elem = (Element)list1.item(i);
 //				list2 = elem.getElementsByTagName("percentage");
 //				res = new QueryResult();
 //				res.score = Integer.valueOf(Util.getTextContent(list2.item(0)).replace("%", ""));
 //				if ( res.score < threshold ) continue;
 //				
 //				list2 = elem.getElementsByTagName("source");
 //				list3 = ((Element)list2.item(0)).getElementsByTagName("segment");
 //				res.source = readSegment((Element)list3.item(0), frag);
 //
 //				list2 = elem.getElementsByTagName("target");
 //				list3 = ((Element)list2.item(0)).getElementsByTagName("segment");
 //				res.target = readSegment((Element)list3.item(0), frag);
 //				
 //				// Query is done without codes, so any exact match result from a text
 //				// with codes should be down-graded
 //				if ( frag.hasCode() && res.score >= 100 ) {
 //					res.score = 99;
 //				}
 //				results.add(res);
 //			}
 //
 //		}
 //		catch ( WebServiceException e ) {
 //			throw new RuntimeException("Error querying TM.", e);
 //		}
 //		catch ( RemoteException e ) {
 //			throw new RuntimeException("Error querying TM.", e);
 //		}
 //		catch ( SAXException e ) {
 //			throw new RuntimeException("Error with query results.", e);
 //		}
 //		catch ( IOException e ) {
 //			throw new RuntimeException("Error with query results.", e);
 //		}
 //		if ( results.size() > 0 ) current = 0;
 //		return results.size();
 //	}
 
 	@Override
 	public int query (String plainText) {
 		try {
 			results.clear();
 			String xmlRes = gsWS.searchEntries(gsToken, gsTmProfile, plainText, srcCode);
 			Document doc = docBuilder.parse(new InputSource(new StringReader(xmlRes)));
 			NodeList list1 = doc.getElementsByTagName("entry");
 			Element elem;
 			NodeList list2;
 			NodeList list3;
 			QueryResult res;
 			for ( int i=0; i<list1.getLength(); i++ ) {
 				if ( i >= maxHits ) break;
 				elem = (Element)list1.item(i);
 				list2 = elem.getElementsByTagName("percentage");
 				res = new QueryResult();
 				res.weight = getWeight();
				res.score = Float.valueOf(Util.getTextContent(list2.item(0)).replace("%", "")).intValue();
 				if ( res.score < threshold ) continue;
 				list2 = elem.getElementsByTagName("source");
 				list3 = ((Element)list2.item(0)).getElementsByTagName("segment");
 				res.source = readSegment((Element)list3.item(0), null);
 				list2 = elem.getElementsByTagName("target");
 				list3 = ((Element)list2.item(0)).getElementsByTagName("segment");
 				res.target = readSegment((Element)list3.item(0), null);
 				results.add(res);
 			}
 		}
 		catch ( WebServiceException e ) {
 			throw new RuntimeException("Error querying TM.", e);
 		}
 		catch ( RemoteException e ) {
 			throw new RuntimeException("Error querying TM.", e);
 		}
 		catch ( SAXException e ) {
 			throw new RuntimeException("Error with query results.", e);
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException("Error with query results.", e);
 		}
 		if ( results.size() > 0 ) current = 0;
 		return results.size();
 	}
 	
 	// The original parameter can be null
 	private TextFragment readSegment (Element elem,
 		TextFragment original)
 	{
 		TextFragment tf = new TextFragment();
 		NodeList list = elem.getChildNodes();
 		int lastId = -1;
 		int id = -1;
 		Node node;
 		Code code;
 //		Code srcCode;
 		Stack<Code> stack = new Stack<Code>();
 //		List<Code> oriCodes = null;
 //		
 //		if ( original != null ) {
 //			oriCodes = original.getCodes();
 //		}
 		
 		// Note that this parsing assumes non-overlapping codes.
 		for ( int i=0; i<list.getLength(); i++ ) {
 			node = list.item(i);
 			switch ( node.getNodeType() ) {
 			case Node.TEXT_NODE:
 				tf.append(node.getNodeValue());
 				break;
 			case Node.ELEMENT_NODE:
 				NamedNodeMap map = node.getAttributes();
 				Node attr = map.getNamedItem("type");
 				if ( node.getNodeName().equals("bpt") ) {
 					id = getRawIndex(lastId, map.getNamedItem("x"));
 					stack.push(tf.append(TagType.OPENING, (attr==null ? "Xpt" : attr.getNodeValue()),
 						String.format("{%d}", id), id));
 				}
 				else if ( node.getNodeName().equals("ept") ) {
 					code = stack.pop();
 					tf.append(TagType.CLOSING, code.getType(),
 						String.format("{/%d}", code.getId()), code.getId());
 				}
 				else if ( node.getNodeName().equals("ph") ) {
 					id = getRawIndex(lastId, map.getNamedItem("x"));
 					tf.append(TagType.PLACEHOLDER, (attr==null ? "ph" : attr.getNodeValue()),
 						String.format("{%d/}", id), id);
 				}
 				else if ( node.getNodeName().equals("it") ) {
 					Node pos = map.getNamedItem("pos");
 					if ( pos == null ) { // Error, but just treat it as a placeholder
 						id = getRawIndex(lastId, map.getNamedItem("x"));
 						tf.append(TagType.PLACEHOLDER, (attr==null ? "ph" : attr.getNodeValue()), "[it/]", id);
 					}
 					else if ( pos.getNodeValue().equals("begin") ) {
 						id = getRawIndex(lastId, map.getNamedItem("x"));
 						tf.append(TagType.OPENING, (attr==null ? "Xpt" : attr.getNodeValue()), "[it-bpt/]", id);
 					}
 					else { // Assumes 'end'
 						tf.append(TagType.CLOSING, (attr==null ? "Xpt" : attr.getNodeValue()), "[it-ept/]");
 					}
 				}
 				break;
 			}
 		}
 		return tf;
 	}
 
 //	private Code guessCode (List<Code> codes,
 //		TagType tagType,
 //		int rawIndex) // Starts at 0
 //	{
 //		if ( codes == null ) return null;
 //		if (( rawIndex < 0 ) || ( rawIndex >= codes.size() )) {
 //			return null;
 //		}
 //		Code code = codes.get(rawIndex);
 //		if ( code.getTagType() == tagType ) {
 //			return code.clone();
 //		}
 //		return null;
 //	}
 	
 	private int getRawIndex (int lastIndex, Node attr) {
 		if ( attr == null ) return ++lastIndex;
 		// GS codes return are 0-base
 		return Integer.valueOf(attr.getNodeValue());
 	}
 	
 	@Override
 	public void removeAttribute (String name) {
 	}
 	
 	@Override
 	public void clearAttributes () {
 	}
 
 	@Override
 	public void setAttribute (String name,
 		String value)
 	{
 	}
 
 	@Override
 	protected String toInternalCode (LocaleId locale) {
 		//TODO: Do we need to adjust the code to always have the country?
 		return locale.toPOSIXLocaleId();
 	}
 
 	/**
 	 * Sets the maximum number of hits to return. Note that with this
 	 * connector this method can only reduce the maximum number of hits from
 	 * the one defined in the active TM profile.
 	 */
 	@Override
 	public void setMaximumHits (int max) {
 		maxHits = max;
 	}
 
 	/**
 	 * Sets the minimal percentage at which a match is kept. Note that
 	 * with this connector this method can only reduce the threshold from the
 	 * one defined in the active TM profile. 
 	 */
 	@Override
 	public void setThreshold (int threshold) {
 		this.threshold = threshold; 
 	}
 
 	@Override
 	public int getMaximumHits () {
 		return maxHits;
 	}
 
 	@Override
 	public int getThreshold () {
 		return threshold;
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	@Override
 	public void setRootDirectory (String rootDir) {
 		// Not used
 	}
 }
