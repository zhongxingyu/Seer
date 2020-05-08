 package org.geworkbench.bison.datastructure.bioobjects.sequence;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.net.URL;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * BlastObj.java A class to hold information about each individual hit of a
  * Blast database search on a protein sequence. <br>
  * 
  * @author zji
  * @version $Id$
  */
 public class BlastObj implements Serializable{
 	 
 	private static final long serialVersionUID = -7617429744234644109L;	 
 
 	private static Log log = LogFactory.getLog(BlastObj.class);
 	
 	/**
 	 * The Database ID of the protein sequence hit in this BlastObj.
 	 */
	private String databaseID;
	public void setDatabaseID(String databaseID) {
		this.databaseID = databaseID;
	}

 	/**
 	 * Set up the upper boundary of whole sequence size.
 	 */
 	static final int MAXSEQUENCESIZE = 100000;
 	int maxSize = MAXSEQUENCESIZE;
 
 	/**
 	 * The accession number of the protein sequence hit in this BlastObj.
 	 */
 	String name;
 	/**
 	 * The description of the protein sequence hit in this BlastObj.
 	 */
 	String description;
 	/**
 	 * The percentage of the query that's aligned with protein sequence hit in
 	 * this BlastObj.
 	 */
 	int percentAligned;
 
 	/**
 	 * The percentage of the alignment of query and hit in this BlastObj that's
 	 * gapped.
 	 */
 	int percentGapped = 0;
 
 	/**
 	 * The percentage of the alignment of query and hit in this BlastObj that's
 	 * "positive"/conserved.
 	 */
 	int percentPos;
 
 	/**
 	 * The score of the alignment of query and hit in this BlastObj; initialized
 	 * to -1.
 	 */
 	int score = -1;
 
 	/**
 	 * The score of the alignment of query and hit in this BlastObj.
 	 */
 	String evalue;
 	/**
 	 * The length of the alignment of query and hit in this BlastObj.
 	 */
 	int length;
 	String seqID;
 	int[] subject_align = new int[2];
 
 	/**
 	 * The 2 element array containing the position in query sequence where the
 	 * alignment start and stop. change to public for temp.
 	 * 
 	 * @todo add getter method later.
 	 */
 	public int[] query_align = new int[2];
 	/**
 	 * The String of query sequence in alignment with hit in this BlastObj.
 	 */
 	String query;
 	/**
 	 * The String of hit sequence in this BlastObj in alignment with query
 	 * sequence.
 	 */
 	String subject;
 	/**
 	 * The String of alignment sequence between query sequence and hit in this
 	 * BlastObj.
 	 */
 	String align;
 	/**
 	 * whether this BlastObj is included in the MSA analysis
 	 */
 	boolean include = false;
 	/**
 	 * check whether the whole sequece can be retrived.
 	 */
 	boolean retriveWholeSeq = false;
 
 	/**
 	 * the URL of hit info
 	 */
 	URL infoURL;
 	/**
 	 * URL of seq.
 	 */
 	URL seqURL;
 	private String detailedAlignment = "";
 	private String identity;
 	private int startPoint;
 	private int alignmentLength;
 	private int endPoint;
 
 	public BlastObj(boolean retriveWholeSeq, String databaseID, String name, String description,
 			String score, String evalue) {
 		this.retriveWholeSeq = retriveWholeSeq;
 		this.databaseID = databaseID;
 		this.description = description;
 		this.name = name;
 
 		try {
 			this.score = Integer.parseInt(score);
 		} catch (NumberFormatException e) {
 			// ignored
 		}
 
 		this.evalue = evalue;
 	}
 
 	/* Get methods for class variables. */
 
 	/**
 	 * Returns the database name of the hit sequence stored in this BlastObj.
 	 * 
 	 * @return the accession number as a String.
 	 */
 	public String getDatabaseID() {
 		return databaseID;
 	}
 
 	/**
 	 * Returns the accession number of the hit sequence stored in this BlastObj.
 	 * 
 	 * @return the accession number as a String.
 	 */
 
 	/**
 	 * Returns the description of the hit protein sequence stored in this
 	 * BlastObj.
 	 * 
 	 * @return the description as a String.
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Returns the length of the alignment of query and hit seq stored in this
 	 * BlastObj.
 	 * 
 	 * @return the length as an int.
 	 */
 
 	/**
 	 * Returns the percentage of the query that's aligned with protein sequence
 	 * hit in this BlastObj.
 	 * 
 	 * @return percentAligned as an int.
 	 */
 
 	public int getPercentAligned() {
 		return percentAligned;
 	}
 
 	/**
 	 * Returns the percentage of the alignment that's gapped with protein
 	 * sequence hit in this BlastObj.
 	 * 
 	 * @return percentGapped as an int.
 	 */
 	public int getPercentGapped() {
 		return percentGapped;
 	}
 
 	/**
 	 * Returns the percentage of the query that's conserved with protein
 	 * sequence hit in this BlastObj.
 	 * 
 	 * @return percentPos as an int.
 	 */
 	public int getPercentPos() {
 		return percentPos;
 	}
 
 	/**
 	 * Returns the score of the alignment of query and protein sequence hit in
 	 * this BlastObj.
 	 * 
 	 * @return score as an int.
 	 */
 	public int getScore() {
 		return score;
 	}
 
 	/**
 	 * Returns the e-value of the alignment of query and protein sequence hit in
 	 * this BlastObj.
 	 * 
 	 * @return evalue as a String.
 	 */
 	public String getEvalue() {
 		return evalue;
 	}
 
 	/**
 	 * Returns the query in alignment with protein sequence hit in this
 	 * BlastObj.
 	 * 
 	 * @return query as a String.
 	 */
 	public String getQuery() {
 		return query;
 	}
 
 	/**
 	 * Returns the protein sequence hit in this BlastObj in alignment with the
 	 * query.
 	 * 
 	 * @return subject as a String.
 	 */
 	public String getSubject() {
 		return subject;
 	}
 
 	/**
 	 * Returns the alignment of query and protein sequence hit in this BlastObj.
 	 * 
 	 * @return align as an String.
 	 */
 	public String getAlign() {
 		return align;
 	}
 
 	/**
 	 * Returns whether this BlastObj is included in MSA analysis.
 	 * 
 	 * @return include as a Boolean.
 	 */
 	public boolean getInclude() {
 		return include;
 	}
 
 	/* Set methods for class variables */
 	
 	/**
 	 * Sets the percentage of the query that's aligned with protein sequence hit
 	 * in this BlastObj.
 	 * 
 	 * @param i,
 	 *            the new alignment percentage of this BlastObj.
 	 */
 	public void setPercentAligned(int i) {
 		percentAligned = i;
 	}
 
 	/**
 	 * Sets the percentage of the alignment that's gapped of query and hit
 	 * protein sequence hit in this BlastObj.
 	 * 
 	 * @param i,
 	 *            the new gapped percentage of this BlastObj.
 	 */
 	public void setPercentGapped(int i) {
 		percentGapped = i;
 	}
 
 	/**
 	 * Sets the percentage of the query that's conserved with protein sequence
 	 * hit in this BlastObj.
 	 * 
 	 * @param i,
 	 *            the new conserved percentage of this BlastObj.
 	 */
 
 	public void setPercentPos(int i) {
 		percentPos = i;
 	}
 
 	/**
 	 * Sets the query sequence of the alignment with protein sequence hit in
 	 * this BlastObj.
 	 * 
 	 * @param s,
 	 *            the new query seq of this BlastObj.
 	 */
 	public void setQuery(String s) {
 		query = s;
 	}
 
 	/**
 	 * Sets hit protein sequence hit in this BlastObj in alignment w/ the query.
 	 * 
 	 * @param s,
 	 *            the new subject seq of this BlastObj.
 	 */
 	public void setSubject(String s) {
 		subject = s;
 	}
 
 	/**
 	 * Sets the alignment sequence of the query with protein sequence hit in
 	 * this BlastObj.
 	 * 
 	 * @param s,
 	 *            the new align seq of this BlastObj.
 	 */
 	public void setAlign(String s) {
 		align = s;
 	}
 
 	/**
 	 * Sets whether this BlastObj is included in MSA analysis.
 	 * 
 	 * @param b,
 	 *            the new include Boolean of this BlastObj.
 	 */
 	public void setInclude(boolean b) {
 		include = b;
 	}
 
 	public URL getInfoURL() {
 		return infoURL;
 	}
 
 	public void setInfoURL(URL infoURL) {
 		this.infoURL = infoURL;
 	}
 
 	public URL getSeqURL() {
 		return seqURL;
 	}
 
 	public void setSeqURL(URL seqURL) {
 		this.seqURL = seqURL;
 	}
 
 	public boolean isRetriveWholeSeq() {
 		return retriveWholeSeq;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int getLength() {
 		return length;
 	}
 
 	public void setLength(int length) {
 		this.length = length;
 	}
 
 	public String getDetailedAlignment() {
 		return detailedAlignment;
 	}
 
 	public void setDetailedAlignment(String detailedAlignment) {
 		this.detailedAlignment = detailedAlignment;
 	}
 
 	public String toString() {
 		return databaseID + detailedAlignment + score + evalue;
 	}
 
 	public CSSequence getAlignedSeq() {
 		CSSequence seq = new CSSequence(">" + databaseID + "|" + name
 				+ "---PARTIALLY INCLUDED", subject);
 
 		return seq;
 
 	}
 
 	/**
 	 * Create Entrez Programming Utilities query URL from web query URL.
 	 * 
 	 * @param queryUrl
 	 * @return
 	 */
 	private static String EUtilsUrl(String queryUrl) {
 		// parse GI from the url
 		final String proteinUrl = "http://www.ncbi.nlm.nih.gov/protein/";
 		final String nucleotideUrl = "http://www.ncbi.nlm.nih.gov/nucleotide/";
 		int indexQ = queryUrl.indexOf("?");
 		String GI = null; 
 		String databaseName = null;
 		if(indexQ==-1) {
 			log.error("unexpected query URL format :"+queryUrl);
 			return null;
 		} else if(queryUrl.startsWith(proteinUrl)) {
 			GI = queryUrl.substring(proteinUrl.length(), indexQ);
 			databaseName = "protein";
 		} else if(queryUrl.startsWith(nucleotideUrl)) {
 			GI = queryUrl.substring(nucleotideUrl.length(), indexQ);
 			databaseName = "nucleotide";
 		} else {
 			log.error("unexpected query URL format :"+queryUrl);
 			return null;
 		}
 
 		return "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db="+databaseName+"&id="
 				+ GI + "&rettype=fasta";
 	}
 	
 	/**
 	 * getWholeSeq
 	 * 
 	 * @return Object
 	 */
 	public CSSequence getWholeSeq() {
 
 		if (!retriveWholeSeq ) {
 			// this field is kept for now, but never set false
 			log.error("retriveWholeSeq is false");
 			return null;
 		}
 		if (seqURL == null)
 			return null;
 
 		log.debug("URL" + seqURL);
 		HttpClient client = new HttpClient();
 		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
 				10, true);
 		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				retryhandler);
 		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
 
 		GetMethod getMethod = new GetMethod(EUtilsUrl(seqURL.toString()));
 		String sequenceLabel = null, sequence = null;
 		try {
 			int statusCode = client.executeMethod(getMethod);
 
 			if (statusCode == HttpStatus.SC_OK) {
 				InputStream stream = getMethod.getResponseBodyAsStream();
 				BufferedReader in = new BufferedReader(new InputStreamReader(
 						stream));
 				sequenceLabel = in.readLine();
 				StringBuilder sb = new StringBuilder();
 				String line = null;
 				while ((line = in.readLine()) != null) {
 					if (line.trim().length() > 0)
 						sb.append(line).append('\n');
 				}
 				stream.close();
 				sequence = sb.toString();
 			} else {
 				log.error("E Utilties failed for " + seqURL);
 				log.error("status code=" + statusCode);
 				return null;
 			}
 		} catch (HttpException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			getMethod.releaseConnection();
 		}
 
 		log.debug("label\n" + sequenceLabel);
 		log.debug("sequence\n" + sequence);
 		return new CSSequence(sequenceLabel, sequence);
 	}
 
 	public String getSeqID() {
 		return seqID;
 	}
 
 	public void setSeqID(String seqID) {
 		this.seqID = seqID;
 	}
 
 	public String getIdentity() {
 		return identity;
 	}
 
 	public int getStartPoint() {
 
 		return startPoint;
 	}
 
 	public int getAlignmentLength() {
 		return alignmentLength;
 	}
 
 	public int getEndPoint() {
 		return endPoint;
 	}
 
 	public int getMaxSize() {
 		return maxSize;
 	}
 
 	public void setIdentity(String identity) {
 		this.identity = identity;
 	}
 
 	public void setStartPoint(int startPoint) {
 
 		this.startPoint = startPoint;
 	}
 
 	public void setAlignmentLength(int alignmentLength) {
 		this.alignmentLength = alignmentLength;
 	}
 
 	public void setEndPoint(int endPoint) {
 		this.endPoint = endPoint;
 	}
 
 	public void setMaxSize(int maxSize) {
 		this.maxSize = maxSize;
 	}
 
 }
