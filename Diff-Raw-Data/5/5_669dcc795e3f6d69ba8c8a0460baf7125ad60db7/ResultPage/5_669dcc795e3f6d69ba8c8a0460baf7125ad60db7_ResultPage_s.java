 package com.blogspot.aptgetmoo.dhjclient;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 /**
  * HTML page returned by Jakim's Halal web search
  *
  * @author Dzul Nizam
  */
 public class ResultPage implements IResultPage {
 
 	private final static String DEFAULT_BASE_URL =
 			"http://www.halal.gov.my/ehalal/directory_standalone.php";
 
 	private URL mBaseUrl;
 
 	private String mKeyword;
 
 	private String mType;
 
 	private int mPage;
 
 	/**
 	 * This assigns the default Jakim's web URL. In such a case the URL is invalid, put a new URL
 	 * in #ResultPage(String pBaseUrl), or the Web is no longer in service :(
 	 * @see #ResultPage(String)
 	 * @see #getBaseUrl()
 	 */
 	public ResultPage() {
 		try {
 			mBaseUrl = new URL(DEFAULT_BASE_URL);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param pBaseUrl Base URL for Jakim's search result page
 	 * @throws MalformedURLException If given URL is invalid
 	 */
 	public ResultPage(String pBaseUrl) throws MalformedURLException {
 		mBaseUrl = new URL(pBaseUrl);
 	}
 
 	@Override
 	public String getBaseUrl() {
 		return mBaseUrl.toString();
 	}
 
 	@Override
 	public String getUrl() {
 		return getBaseUrl() + "?cari=" + mKeyword + "&type=" + mType + "&page=" + mPage;
 	}
 
 	@Override
 	public String fetchHtml() throws IOException {
 		try {
			HttpURLConnection connection = (HttpURLConnection) mBaseUrl.openConnection();
 			connection.setRequestMethod("GET");
 			connection.connect();
 			InputStream is = connection.getInputStream();
 
 			try {
 		        return new Scanner(is).useDelimiter("\\A").next();
 		    } catch (NoSuchElementException e) {
 		        return "";
 		    }
 		} catch (ProtocolException e) {
 			throw new IOException(e);
 		}
 	}
 
 	@Override
 	public void setFetchParameters(String pKeyword, String pType, int pPage) {
 		mKeyword = pKeyword;
 		mType = pType;
 		mPage = pPage;
 	}
 
 }
