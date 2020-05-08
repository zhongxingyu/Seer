 package entropia.clubmonitor;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Joiner;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public class WebClient {
 
     public static void post(final HttpURLConnection con, final String param)
 	    throws IOException {
 	con.setDoOutput(true);
 	con.setDoInput(true);
 	con.setInstanceFollowRedirects(false);
 	con.setRequestMethod("POST");
 	con.setUseCaches(false);
 	con.getOutputStream().write(param.getBytes(Charsets.UTF_8));
 	final int responseCode = con.getResponseCode();
 	if (responseCode != 200) {
 	    final String responseMessage = con.getResponseMessage();
 	    if (responseMessage != null) {
 		throw new IOException(responseMessage);
 	    }
 	    throw new IOException("response code: " + responseCode);
 	}
     }
     
     public static void post(final HttpURLConnection con, final Map<String,String> params)
 	    throws IOException {
 	final String param = encodeParam(params);
 	post(con, param);
     }
     
     public static void post(final URL url, final Map<String,String> params)
 	    throws IOException {
 	final HttpURLConnection con = (HttpURLConnection) url.openConnection();
 	try {
 	    post(con, params);
 	} finally {
 	    con.disconnect();
 	}
     }
     
     public static void post(final URL url, final String param)
 	    throws IOException {
 	final HttpURLConnection con = (HttpURLConnection) url.openConnection();
 	try {
 	    post(con, param);
 	} finally {
 	    con.disconnect();
 	}
     }
     
     private static String urlencode(final String s)
             throws UnsupportedEncodingException {
         return URLEncoder.encode(s, Charsets.UTF_8.name());
     }
     
     public static String buildParams(final String... args) throws UnsupportedEncodingException {
         final Map<String,String> map = new HashMap<String,String>(args.length / 2);
         final List<String> l = Arrays.asList(args);
         final Iterator<String> it = l.iterator();
         while (it.hasNext()) {
            final String key = it.next();
             if (!it.hasNext()) {
                 throw new IllegalArgumentException("length of args % 2 != 0");
             }
            final String value = it.next();
             map.put(key, value);
         }
         return encodeParam(Collections.unmodifiableMap(map));
     }
     
     public static String encodeParam(final Map<String,String> params)
             throws UnsupportedEncodingException {
         final List<String> l = new ArrayList<String>(params.size());
         for (final Map.Entry<String, String> e : params.entrySet()) {
             final String k = URLEncoder.encode(e.getKey(), "UTF-8");
             final String v = URLEncoder.encode(e.getValue(), "UTF-8");
             l.add(k + "=" + v);
         }
         return Joiner.on("&").join(l);
     }
     
     public static URL getURL(final URL url, final String... params)
             throws UnsupportedEncodingException, MalformedURLException {
         final String p = buildParams(params);
        return new URL(url.toExternalForm() + "?" + p);
     }
     
     @SuppressWarnings("resource")
     public static JsonObject getJsonElement(final URL url) throws IOException {
         final HttpURLConnection c = (HttpURLConnection) url.openConnection();
         try {
             final InputStreamReader in = new InputStreamReader(c.getInputStream());
             return new JsonParser().parse(in).getAsJsonObject();
         } finally {
             c.disconnect();
         }
     }
 }
