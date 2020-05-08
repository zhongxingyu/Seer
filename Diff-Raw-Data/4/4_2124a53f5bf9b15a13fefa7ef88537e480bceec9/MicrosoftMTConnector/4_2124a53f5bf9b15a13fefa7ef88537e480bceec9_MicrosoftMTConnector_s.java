 /*===========================================================================
   Copyright (C) 2010-2012 by the Okapi Framework contributors
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
 
 package net.sf.okapi.connectors.microsoft;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.common.query.MatchType;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.lib.translation.BaseConnector;
 import net.sf.okapi.lib.translation.ITMQuery;
 import net.sf.okapi.common.query.QueryResult;
 import net.sf.okapi.lib.translation.QueryUtil;
 
 @UsingParameters(Parameters.class)
 public class MicrosoftMTConnector extends BaseConnector implements ITMQuery {
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 
 	private final String OPTIONS1 = "<TranslateOptions xmlns=\"http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2\">" +
 		"<Category>";
 	private final String OPTIONS2 = "</Category>" +
 		"<ContentType>text/html</ContentType>" +
 		"<ReservedFlags />" +
 		"<State />" +
 		"<Uri></Uri>" +
 		"<User>defaultUser</User>" +
 		"</TranslateOptions>";
 
 	private static final int QUERYLENGTHLIMIT = 10000;
 	private static final int RETRIES = 5; // number of times to try to get a response from Microsoft before failing
 	private final String PLACEHOLDER = "[$#@list@#$]";
 	private final int TOKENRETRIES = 5;
 	private int SLEEPPAUSE = 400; // DWH 5-3-2012 how long to wait before trying
 	
 	private QueryUtil util;
 	Parameters params;
 	int maximumHits = 1;
 	int threshold = -10; // it was returning 0 for good translations
 //	int threshold = 95;
 	private List<QueryResult> results;
 	String queryListTemplate;
 	String addListTemplate;
 	private String sToken="";
 	private long lExpirationTime=0; // second in which this sToken expires in Time() format
 
 	public MicrosoftMTConnector () {
 		util = new QueryUtil();
 		params = new Parameters();
 	}
 	
 	@Override
 	public void close () {
 		// Nothing to do
 	}
 
 	@Override
 	public String getName () {
 		return "Microsoft-Translator";
 	}
 
 	@Override
 	public String getSettingsDisplay () {
 		return "Service: http://api.microsofttranslator.com/V2/Http.svc" ;
 	}
 
 	@Override
 	public void open () {
 		results = new ArrayList<QueryResult>();		
 	}
 
 	@Override
 	public int query (String plainText) {
 		return query(new TextFragment(plainText));
 	}
 	
 	static private String fromInputStreamToString (InputStream stream,
 		String encoding)
 		throws IOException
 	{
 		BufferedReader br = new BufferedReader(new InputStreamReader(stream, encoding));
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		while ( (line = br.readLine()) != null ) {
 			sb.append(line + "\n");
 		}
 		br.close();
 		return sb.toString();
 	}
 	
 	@Override
 	public int query (TextFragment frag) {
 		String sAddress;
 		String sBlock;
 		current = -1;
 		results.clear();
 		if ( !frag.hasText(false) ) return 0;
 		try {
 			// Convert the fragment to coded HTML
 			String stext = util.toCodedHTML(frag);
 //			URL url = new URL(String.format("http://api.microsofttranslator.com/v2/Http.svc/GetTranslations"
 //				+ "?appId=%s&text=%s&from=%s&to=%s&maxTranslations=%d",
 //				params.getAppId(),
 			sAddress = String.format("http://api.microsofttranslator.com/v2/Http.svc/GetTranslations"
 			+ "?text=%s&from=%s&to=%s&maxTranslations=%d",
 				URLEncoder.encode(stext, "UTF-8"),
 				srcCode,
 				trgCode,
 				maximumHits);
 			
 			for (int tries = 0; tries < RETRIES; tries++) {
 				if (!getNewTokenIfNeeded())
 				{
 					throw new RuntimeException("Error getting Microsoft Azure access token for translation");
 				} 
 				else {
 					if ( tries == RETRIES-1 ) { // no use sleeping to just fall out of for loop
 						throw new RuntimeException(String.format(
 							"Failed to get Microsoft Translator access token after %d tries.\nStopped at fragment: %s",
 							TOKENRETRIES, frag.toString()));
 					}
 					sBlock = postQuery(sAddress,OPTIONS1+params.getCategory()+OPTIONS2);
 					if ( sBlock==null ) {
 						try {
 							Thread.sleep(SLEEPPAUSE);
 						}
 						catch ( InterruptedException e ) { // This should never happen unless the application is closed
 							throw new RuntimeException("Interrupted while waiting for Microsoft Translator access token");
 						}  // wait then try again
 					}
 					else {
 						results = parseBlock(sBlock, frag);
 						break;
 					}
 				}
 			}
 		}
 		catch ( Throwable e) {
 			throw new RuntimeException("Error querying the MT server.\n" + e.getMessage(), e);
 		}
 		if ( results.size() > 0 ) current = 0;
 		return results.size();
 	}
 	
 	private String postQuery (String sAddress,
 		String query)
 		throws MalformedURLException, IOException
 	{
 		URL url = new URL(sAddress);
 		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 		conn.addRequestProperty("Content-Type", "text/xml");
 		conn.setRequestProperty("Authorization", "Bearer " + sToken);
 		conn.setRequestMethod("POST");
 		conn.setDoOutput(true);
 	    conn.setDoInput(true);
 	    
 		OutputStreamWriter osw = null;
 		try {
 			osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
 			osw.write(query);
 		}
 		catch(Exception e) {
 			throw new RuntimeException("Problem querying the MT server.\n" + e.getMessage(), e);			
 		}
 		finally {
 			osw.flush();
 			osw.close();
 		}
 		int code = conn.getResponseCode();
 		if ( code == 200 ) {
 			return fromInputStreamToString(conn.getInputStream(), "UTF-8");
 		}
 		else {
 //			logger.debug("Query response code: {}: {}", code, conn.getResponseMessage());
 			return null;
 		}
 	}
 	
 	private String unescapeXML (String text) {
 		text = text.replace("&apos;", "'");
 		text = text.replace("&lt;", "<");
 		text = text.replace("&gt;", ">");
 		text = text.replace("&quot;", "\"");
 		return text.replace("&amp;", "&"); // Ampersand must be done last
 	}
 
 	private List<QueryResult> parseBlock (String block,
 		TextFragment frag)
 	{
 		List<QueryResult> list = new ArrayList<QueryResult>(maximumHits); // No more that maximumHits
 		int n1, n2, from = 0;
 		if (block==null)
 			return list;
 		// Get the results for the given entry
 		while ( true ) {
 			// Isolate the next match result
 			n1 = block.indexOf("<TranslationMatch>", from);
 			if ( n1 < 0 ) break; // Done
 			n2 = block.indexOf("</TranslationMatch>", n1);
 			String res = block.substring(n1, n2);
 			from = n2+1; // For next iteration
 			
 			// Parse the found match
 			n1 = res.indexOf("<MatchDegree>");
 			n2 = res.indexOf("</MatchDegree>", n1+1);
 			int score = Integer.parseInt(res.substring(n1+13, n2));
 			// Get the rating
 			int rating = 5;
 			n1 = res.indexOf("<Rating", 0); // No > to handle /> cases
 			n2 = res.indexOf("</Rating>", n1);
 			if ( n2 > -1 ) {
 				rating = Integer.parseInt(res.substring(n1+8, n2));
 				// Ensure it's withing expected range of -10 to 10.
 				if ( rating < -10 ) rating = -10;
 				else if ( rating > 10 ) rating = 10;
 			}
 
 			// Compute a relative score to take into account the rating
 			int combinedScore = score;
 			if ( combinedScore > 90 ) {
 				combinedScore += (rating-10);
 				// Ideally we would want a composite value for the score
 			}
 			if ( combinedScore < threshold ) continue;
 			
 			// Get the source (when available)
 			n1 = res.indexOf("<MatchedOriginalText", 0); // No > to handle /> cases
 			n2 = res.indexOf("</MatchedOriginalText", n1);
 			String stext = null; // No source (same as original
 			if ( n2 > -1 ) stext = unescapeXML(res.substring(n1+21, n2));
 			// Translation
 			String ttext = "";
 			n1 = res.indexOf("<TranslatedText", n2); // No > to handle /> cases
 			n2 = res.indexOf("</TranslatedText", n1);
 			if ( n2 > -1 ) ttext = unescapeXML(res.substring(n1+16, n2));
 			
 			QueryResult qr = new QueryResult();
 			qr.setQuality(Util.normalizeRange(-10, 10, rating));
 			qr.setFuzzyScore(score); // Score from the system
 			qr.setCombinedScore(combinedScore); // Adjusted score
 			// Else: continue with that result
 			qr.weight = getWeight();
 			if ( frag.hasCode() ) {
 				if ( stext == null ) qr.source = frag;
 				else qr.source = new TextFragment(util.fromCodedHTML(stext, frag, false),
 					frag.getClonedCodes());
 				qr.target = new TextFragment(util.fromCodedHTML(ttext, frag, false),
 					frag.getClonedCodes());
 			}
 			else {
 				if ( stext == null ) qr.source = frag;
 				else qr.source = new TextFragment(util.fromCodedHTML(stext, frag, false));
 				qr.target = new TextFragment(util.fromCodedHTML(ttext, frag, false));
 			}
 			qr.origin = getName();
 			qr.matchType = MatchType.MT;
 			list.add(qr);
 		}
 		return list;
 	}
 	
 	private List<List<QueryResult>> parseAllBlocks (String resp,
 		List<TextFragment> fragments)
 	{
 		try {
 			List<List<QueryResult>> list = new ArrayList<List<QueryResult>>();
 			if (resp==null)
 				return list;
 			int from = 0;
 	
 			// Look for the results of each query:
 			for ( TextFragment frag : fragments ) {
 				if ( !frag.hasText(false) ) {
 					// Create auto-result for skipped entries to have the same number of responses
 					List<QueryResult> res = new ArrayList<QueryResult>();
 					list.add(res);
 				}
 				else {
 					// Move the start at the proper position
 					from = resp.indexOf("<Translations>", from);
 					if ( from < 0 ) break; // Nothing more
 					int n = resp.indexOf("</Translations>", from);
 					String block = resp.substring(from, n);
 					from = n+1; // For next iteration
 					// Parse the block and store the results
 					list.add(parseBlock(block, frag));
 				}
 			}
 			
 			return list;
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException("Error parsing translation results.", e);
 		}
 	}
 	
 	/**
 	 * Adds or overwrites a translation on the server.
 	 * @param source the text of the source.
 	 * @param target the new text of the translation.
 	 * @param rating the rating to use for this translation.
 	 * @return the HTTP response code (200 is success)
 	 */
 	public int addTranslation (TextFragment source,
 		TextFragment target,
 		int rating)
 	{
 		try {
 			// Convert the fragment to coded HTML
 			String stext = util.toCodedHTML(source);
 			String ttext = util.toCodedHTML(target);
 //			URL url = new URL(String.format("http://api.microsofttranslator.com/v2/Http.svc/AddTranslation"
 //				+ "?appId=%s&originaltext=%s&translatedtext=%s&from=%s&to=%s&user=defaultUser&rating=%d",
 //				params.getAppId(),
 			URL url = new URL(String.format("http://api.microsofttranslator.com/v2/Http.svc/AddTranslation"
 				+ "?originaltext=%s&translatedtext=%s&from=%s&to=%s&user=defaultUser&rating=%d&category=%s",
 				URLEncoder.encode(stext, "UTF-8"),
 				URLEncoder.encode(ttext, "UTF-8"),
 				srcCode,
 				trgCode,
 				rating,
 				params.getCategory()));
 			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 			conn.addRequestProperty("Content-Type", "text/xml");
 			conn.setRequestProperty("Authorization", "Bearer " + sToken);
 			conn.setRequestMethod("GET");
 			conn.setDoOutput(true);
 		    conn.setDoInput(true);
 		    
 		    return conn.getResponseCode();
 		}
 		catch ( Throwable e) {
 			throw new RuntimeException("Error adding translation to the server.\n" + e.getMessage(), e);
 		}
 	}
 	
 	/**
 	 * Adds or overwrites a list of translations on the server.
 	 * @param sources list of the source fragments. They should be 100 at most. 
 	 * @param targets list of the corresponding translations. They must be one for each source
 	 * and they must be in the same order.
 	 * @param ratings  list of the corresponding ratings. They must be one for each source
 	 * and they must be in the same order.
 	 * @return the HTTP response code (200 is success)
 	 */
 	public int addTranslationList (List<TextFragment> sources,
 		List<TextFragment> targets,
 		List<Integer> ratings)
 	{
 		StringWriter strWriter = null;
 		try {
 			// Checking
 			if ( targets.size() != sources.size() ) {
 				throw new RuntimeException("There should be as many targets as sources.");
 			}
 			if ( ratings.size() != sources.size() ) {
 				throw new RuntimeException("There should be as many ratings as sources.");
 			}
 			if ( sources.size() > 100 ) {
 				throw new RuntimeException("No more than 100 segments allowed.");
 			}
 		
 			// Create the query template if needed
 			if ( addListTemplate == null ) {
 				strWriter = new StringWriter();
 				XMLWriter xmlWriter = new XMLWriter(strWriter);
 				xmlWriter.writeStartDocument();
 				xmlWriter.writeStartElement("AddtranslationsRequest");
 				xmlWriter.writeAttributeString("xmlns:o", "http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2");
 				xmlWriter.writeElementString("AppId", "");
 				xmlWriter.writeElementString("From", srcCode);
 				xmlWriter.writeStartElement("Options");
 				xmlWriter.writeElementString("o:Category", params.getCategory());
 				xmlWriter.writeElementString("o:ContentType", "text/html");
 				xmlWriter.writeElementString("o:ReservedFlags", "");
 				xmlWriter.writeElementString("o:State", "");
 				xmlWriter.writeElementString("o:Uri", "");
 				xmlWriter.writeElementString("o:User", "defaultUser");
 				xmlWriter.writeEndElement(); // Options
 				xmlWriter.writeElementString("To", trgCode);
 				xmlWriter.writeStartElement("Translations");
 				xmlWriter.writeRawXML(PLACEHOLDER); // Place-holder for the text array
 				xmlWriter.writeEndElement(); // Translations
 				
 				xmlWriter.writeEndElement(); // AddtranslationsRequest
 				xmlWriter.writeEndDocument();
 				xmlWriter.close();
 				strWriter.close();
 				addListTemplate = strWriter.toString();
 			}
 			
 			// Fill the template
 			StringBuilder sb = new StringBuilder();
 			for ( int i=0; i<sources.size(); i++ ) {
 				TextFragment src = sources.get(i);
 				TextFragment trg = targets.get(i);
 				int rating = ratings.get(i);
 				if (( rating < -10 ) && ( rating > 10 )) rating = 4;
 				sb.append("<o:Translation>");
 				// Source
 				sb.append("<o:OriginalText>");
 				String tmp = util.toCodedHTML(src);
 				sb.append(Util.escapeToXML(tmp, 0, false, null));
 				sb.append("</o:OriginalText>");
 				// Rating
 				sb.append(String.format("<o:Rating>%d</o:Rating>", rating));
 				// Sequence
 				sb.append(String.format("<o:Sequence>%d</o:Sequence>", 0));
 				// Source
 				sb.append("<o:TranslatedText>");
 				tmp = util.toCodedHTML(trg);
 				sb.append(Util.escapeToXML(tmp, 0, false, null));
 				sb.append("</o:TranslatedText>");
 				sb.append("</o:Translation>");
 			}
 
 			URL url = new URL(String.format("http://api.microsofttranslator.com/v2/Http.svc/AddTranslationArray"));
 				//+ "?appId=%s", params.getAppId()));
 			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 			conn.addRequestProperty("Content-Type", "text/xml");
 			conn.setRequestProperty("Authorization", "Bearer " + sToken);
 			conn.setRequestMethod("POST");
 			conn.setDoOutput(true);
 		    conn.setDoInput(true);
 			OutputStreamWriter osw = null;
 			try {
 				osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
 				String query = addListTemplate.replace(PLACEHOLDER, sb.toString());
 				osw.write(query);
 			}
 			finally {
 				osw.flush();
 				osw.close();
 			}
 
 			int code = conn.getResponseCode();
 			if ( code != 200 ) {
 				throw new RuntimeException("HTTP error when adding translation.\n" + conn.getResponseMessage());
 			}
 			return code;
 		}
 		catch ( Throwable e) {
 			throw new RuntimeException("Error adding translations.\n" + e.getMessage(), e);
 		}
 	}
 
 	@Override
 	public void leverage (ITextUnit tu) {
 		leverageUsingBatchQuery(tu);
 	}
 	
 	@Override	
 	public void batchLeverage(List<ITextUnit> tuList) {
 		batchLeverageUsingBatchQuery(tuList);
 	}
 	
 	@Override
 	public List<List<QueryResult>> batchQuery (List<TextFragment> fragments) {
 
 		List<List<QueryResult>> list = new ArrayList<List<QueryResult>>();
 		List<List<QueryResult>> subList;
 		List<TextFragment> subFragments;
 		int nCharCount;
 		int nEwLen = 0;
 		String sEscapee;
 		boolean bGotSome = false;
 		StringBuilder sb;
 		// Create the query template if needed
 		StringWriter strWriter = null;
 		
 		try {
 			if ( queryListTemplate == null ) {
 				strWriter = new StringWriter();
 				XMLWriter xmlWriter = new XMLWriter(strWriter);
 				xmlWriter.writeStartDocument();
 				xmlWriter.writeStartElement("GetTranslationsArrayRequest");
 				xmlWriter.writeElementString("AppId", "");
 				xmlWriter.writeElementString("From", srcCode);
 				xmlWriter.writeStartElement("Options");
 				xmlWriter.writeAttributeString("xmlns:o", "http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2");
 				xmlWriter.writeElementString("o:Category", params.getCategory());
 				xmlWriter.writeElementString("o:ContentType", "text/html");
 				xmlWriter.writeElementString("o:ReservedFlags", "");
 				xmlWriter.writeElementString("o:State", "");
 				xmlWriter.writeElementString("o:Uri", "");
 				xmlWriter.writeElementString("o:User", "");
 				xmlWriter.writeEndElement(); // Options
 				xmlWriter.writeStartElement("Texts");
 				xmlWriter.writeAttributeString("xmlns:s", "http://schemas.microsoft.com/2003/10/Serialization/Arrays");
 				xmlWriter.writeRawXML(PLACEHOLDER); // Place-holder for the text array
 				xmlWriter.writeEndElement(); // Texts
 				xmlWriter.writeElementString("To", trgCode);
 				xmlWriter.writeElementString("MaxTranslations", String.valueOf(maximumHits));
 				xmlWriter.writeEndElement(); // GetTranslationsArrayRequest
 				xmlWriter.writeEndDocument();
 				xmlWriter.close();
 				strWriter.close();
 				queryListTemplate = strWriter.toString();
 			}
 			
 			// Fill the template
 			nCharCount = queryListTemplate.length() + 200; // add 200 just to be sure
 			sb = new StringBuilder();
 			subFragments = new ArrayList<TextFragment>();
 			subList = new ArrayList<List<QueryResult>>();
 			for ( TextFragment tf : fragments ) {
 				
 				if ( !tf.hasText(false) ) continue; // Skip no-text entries
 				String stext = util.toCodedHTML(tf);
 				sEscapee = Util.escapeToXML(stext, 0, false, null);
 				nEwLen = 21 + sEscapee.length();
 				if ( nEwLen+queryListTemplate.length() + 200 > QUERYLENGTHLIMIT ) {
 					logger.warn("Segment starting with '{}' is too long to query.", sEscapee.substring(0, 20));
 					continue; // this segment by itself is too long for a query, so skip it
 				}
 				if ( nCharCount+nEwLen > QUERYLENGTHLIMIT ) { // do query now so don't exceed length limit
 					subList = subBatchQuery(queryListTemplate, sb, subFragments);
 					Iterator<List<QueryResult>> it = subList.iterator();
 					while ( it.hasNext() ) {
 						list.add(it.next());
 						bGotSome = true;
 					}
 					nCharCount = queryListTemplate.length() + 200; // add 200 just to be sure
 					sb = new StringBuilder();
 					subFragments = new ArrayList<TextFragment>();
 					subList = new ArrayList<List<QueryResult>>();				
 				}
 				sb.append("<s:string>");
 				sb.append(sEscapee);
 				sb.append("</s:string>");
 				nCharCount += nEwLen;
 				subFragments.add(tf);
 			}
 
 			if ( !bGotSome && (sb.length() == 0 )) {
 				// Case where all segments are non-text: We build a list of empty results
 				for ( int i=0; i<fragments.size(); i++ ) {
 					List<QueryResult> res = new ArrayList<QueryResult>();
 					list.add(res);
 				}
 				return list;
 			}
 
 			subList = subBatchQuery(queryListTemplate, sb, subFragments);
 			Iterator<List<QueryResult>> it = subList.iterator();
 			while( it.hasNext() ) {
 				list.add(it.next());
 			}
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException("Error when translating batch.\n"+e.getMessage(), e);
 		}
 		
 		return list;
 	}
 	
 	private List<List<QueryResult>> subBatchQuery (String subQueryListTemplate,
 		StringBuilder sb,
 		List<TextFragment> fragments)
 	{
 		String sBlock;
 		List<List<QueryResult>> list = new ArrayList<List<QueryResult>>();
 		try {
 			String query = subQueryListTemplate.replace(PLACEHOLDER, sb.toString());
 //			URL url = new URL(String.format("http://api.microsofttranslator.com/v2/Http.svc/GetTranslationsArray"
 //			+ "?appId=%s", params.getAppId()));
 			String sAddress = String.format("http://api.microsofttranslator.com/v2/Http.svc/GetTranslationsArray");
 			for (int tries = 0; tries < RETRIES; tries++) {
 				if ( !getNewTokenIfNeeded() )
 				{
 					throw new RuntimeException("Error getting Microsoft Azure access token for translation");
 				}
 				else {
 					if (tries == RETRIES - 1) { // no use sleeping to just fall out of for loop
 						throw new RuntimeException(String.format(
 							"Failed to get Microsoft Translator access token after %d tries.",
 							TOKENRETRIES));
 					}
 					sBlock = postQuery(sAddress,query);
 					if ( sBlock==null ) {
 						try {
 							Thread.sleep(SLEEPPAUSE);
 						}
 						catch (InterruptedException e) { // this should never happen unless the app is closed
 							throw new RuntimeException("Interrupted while waiting for Microsoft Translator access token");
 						}  // wait then try again
 					}
 					else {
 						list = parseAllBlocks(sBlock, fragments);
 						break;
 					}
 				}
 			}
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException("Error when batch translating.\n"+e.getMessage(), e);
 		}
 
 		return list;
 	}
 	
 	private boolean getNewTokenIfNeeded () { 
 		// returns false if Token could not be obtained
 		boolean bResult = true;
 		long lNow = getCurrentTime();
 		if ( lNow > lExpirationTime - 500 ) { // get a new token if current one
 			// will expire in half a second
 			Long lDiff = lNow - lExpirationTime;
 			if ( lDiff > 0 && lDiff < 500 ) {
 				try {
 					Thread.sleep(lNow - lExpirationTime);
 				}
 				catch ( InterruptedException e ) {
 					throw new RuntimeException("Sleep interrupted while attempting to get Azure Marketplace Token" + e.getMessage(), e);
 				}
 			}
 
 			for (int tries = 0; tries < TOKENRETRIES; tries++) {
 				if ( getAccessToken() ) {
 					bResult = true;
 					break;
 				}
 				else {
 					if ( tries == TOKENRETRIES-1 ) { // no use sleeping to
 						// just fall out of
 						// for loop
 						throw new RuntimeException(String.format(
 							"Failed to get Microsoft Translator access token after %d tries.",
 							TOKENRETRIES));
 					}
 					try {
 						Thread.sleep(SLEEPPAUSE);
 					}
 					catch (InterruptedException e) { // this should never happen unless the app is closed
 						throw new RuntimeException("Interrupted while waiting for Microsoft Translator access token");
 					}  // wait then try again
 				}
 			}
 		}
 		return bResult;
 	}
 
 	private boolean getAccessToken () {
 		boolean bResult = false;
 		try {
 			String tokenRes = ApacheHttpClientForMT.getAzureAccessToken(
 				"https://datamarket.accesscontrol.windows.net/v2/OAuth2-13",
 				params.getClientId(), params.getSecret());
 			if ( tokenRes != null ) {
 				sToken = parseTokenForm(tokenRes); // parseTokenForm should set nExpirationSecond
 				if ( !sToken.equals("") ) {
 					bResult = true;
 				}
 			}
 		}
 		catch ( Throwable e ) {
 			logger.debug("Error in getAccessToken: {}", e.getMessage());
 		}
 		return bResult;
 	}
 	
 	private String parseTokenForm(String sBlock) {
 		String sAccessToken = "";
 		Long lExpiresAt = 0L;
 		String sExpiresAt = "0";
 		long lDies = 0;
 		int n1, n2;
 		n1 = sBlock.indexOf("access_token\":\"");
 		n2 = sBlock.indexOf("\"", n1 + 15);
 		if (n1 > 0 && n2 > 0) {
 			sAccessToken = sBlock.substring(n1 + 15, n2); // just leave it encoded
 			n1 = sBlock.indexOf("ExpiresOn=");
 			n2 = sBlock.indexOf("&", n1 + 10);
 			if (n1 > 0 && n2 > 0) {
 				sExpiresAt = sBlock.substring(n1 + 10, n2);
 				try {
 					lExpiresAt = Long.valueOf(sExpiresAt);
 				}
 				catch (Exception e) {
 					lExpiresAt = 0L;
 				}
 			}
 			try {
 				lDies = 1000L * lExpiresAt;
 			} catch (Exception e) {
 				lDies = 0;
 			}
 		}
 		if ( lDies <= 0 ) sAccessToken = "";
 		lExpirationTime = lDies;
 		return sAccessToken;
 	}
 
 	private long getCurrentTime () {
 		java.util.Date date = new java.util.Date();
 		return date.getTime();
 	}
 
 	@Override
 	protected String toInternalCode (LocaleId locale) {
 		String code = locale.toBCP47();
 		if ( code.equals("zh-tw") || code.equals("zh-hant") || code.equals("zh-cht") ) {
 			code = "zh-CHT";
 		}
 		else if ( code.startsWith("zh") ) { // zh-cn, zh-hans, zh-..
 			code = "zh-CHS";
 		}
 		else { // Use just the language otherwise
 			code = locale.getLanguage(); 
 		}
 		return code;
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
 	public int getMaximumHits () {
 		return maximumHits;
 	}
 
 	@Override
 	public void setMaximumHits (int maximumHits) {
 		this.maximumHits = maximumHits;
 		queryListTemplate = null;
 		addListTemplate = null;
 	}
 
 	@Override
 	public int getThreshold () {
 		return threshold;
 	}
 
 	@Override
 	public void setThreshold (int threshold) {
 		this.threshold = threshold;
 		this.threshold = -10; // Microsoft is returning confidence of 0
 	}
 
 	@Override
 	public void setLanguages (LocaleId sourceLocale,
 		LocaleId targetLocale)
 	{
 		super.setLanguages(sourceLocale, targetLocale);
 		queryListTemplate = null;
 		addListTemplate = null;
		srcCode = sourceLocale.toString();
		trgCode = targetLocale.toString();
 	}
 	
 }
