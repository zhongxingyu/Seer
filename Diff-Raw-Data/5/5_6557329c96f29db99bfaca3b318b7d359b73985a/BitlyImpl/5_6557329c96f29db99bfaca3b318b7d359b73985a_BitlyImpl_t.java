 package com.rosaloves.net.shorturl.bitly;
 
 import java.io.IOException;
 import java.net.URL;
import java.net.URLEncoder;
 
 import net.sf.json.JSONObject;
 
 import com.rosaloves.net.shorturl.bitly.api.Api;
 import com.rosaloves.net.shorturl.bitly.api.BitlyApi;
 import com.rosaloves.net.shorturl.bitly.auth.Authentication;
 import com.rosaloves.net.shorturl.bitly.url.BitlyUrl;
 import com.rosaloves.net.shorturl.bitly.url.BitlyUrlImpl;
 import com.rosaloves.net.shorturl.bitly.url.BitlyUrlInfo;
 import com.rosaloves.net.shorturl.bitly.url.BitlyUrlInfoImpl;
 import com.rosaloves.net.shorturl.bitly.url.BitlyUrlStats;
 import com.rosaloves.net.shorturl.bitly.url.BitlyUrlStatsImpl;
 
 /**
  * BitlyImpl
  *
  * $Id$
  *
  * @author Chris Lewis (Feb 24, 2009)
  */
 public class BitlyImpl implements Bitly {
 	
 	private RESTTransport transport;
 	
 	BitlyImpl(Authentication auth) {
 		transport = new RESTTransport(auth, new BitlyApi());
 	}
 	
 	BitlyImpl(Authentication auth, Api api) {
 		transport = new RESTTransport(auth, api);
 	}
 	
 	public URL expandHash(String hash) throws IOException {
 		
 		Response resp = transport.call(Bitly.METHOD_EXPAND, "hash", hash);
 		
 		return new URL(resp.getJSONResult(hash).getString("longUrl"));
 		
 	}
 
 	public BitlyUrlInfo info(String hash) throws IOException {
 		
 		Response resp = transport.call(Bitly.METHOD_INFO, "hash", hash);
 		
 		JSONObject json = resp.getJSONResult(hash);
 		
 		return new BitlyUrlInfoImpl(new BitlyUrlImpl(json.getString("hash"), json.getString("userHash"), json.getString("longUrl")));
 		
 	}
 
 	public BitlyUrl shorten(String longUrl, String keyword) throws IOException {
 		
 		Response resp = transport.call(Bitly.METHOD_SHORTEN, "longUrl", longUrl, "keyword", keyword);
 		
 		JSONObject json = resp.getJSONResult(longUrl);
 		
 		return new BitlyUrlImpl(json.getString("hash"), json.getString("userHash"), longUrl);
 	}
 
 	public BitlyUrl shorten(String longUrl) throws IOException {
 		
		Response resp = transport.call(Bitly.METHOD_SHORTEN, "longUrl",
				URLEncoder.encode(longUrl, "UTF-8"));
 		
 		JSONObject json = resp.getJSONResult(longUrl);
 		
 		return new BitlyUrlImpl(json.getString("hash"), json.getString("userHash"), longUrl);
 	}
 
 	public BitlyUrlStats stats(String hash) throws IOException {
 		
 		Response resp = transport.call(Bitly.METHOD_STATS, "hash", hash);
 		JSONObject results = resp.getJSONResults();
 		
 		BitlyUrlStats stats = new BitlyUrlStatsImpl(results.getInt("clicks"));
 		
 		return stats;
 	}
 	
 }
