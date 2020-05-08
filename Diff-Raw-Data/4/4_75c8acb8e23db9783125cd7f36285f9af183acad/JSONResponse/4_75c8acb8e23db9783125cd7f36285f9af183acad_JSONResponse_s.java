 
 package axirassa.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.tapestry5.StreamResponse;
 import org.apache.tapestry5.json.JSONCollection;
 import org.apache.tapestry5.services.Response;
 
 /**
  * Based on:
  * http://tapestry.1045711.n5.nabble.com/JSON-GZip-compression-td2845041.html
  * http://tapestry.1045711.n5.nabble.com/T5-2-Tapestry-IoC-Configuration-remove-
  * td2840319.html
  * 
  * @author wiktor
  */
 public class JSONResponse implements StreamResponse {
 
 	private static final String CHARSET = "UTF-8";
 	private static final int MIN_DATA_SIZE = 512;
 
 	private byte[] data;
 	private byte[] dataForSending;
 	private boolean isCompressable;
 
 
 	public JSONResponse (JSONCollection json) throws IOException {
 		try {
 			data = json.toCompactString().getBytes(CHARSET);
 
 			if (data.length >= MIN_DATA_SIZE)
 				isCompressable = true;
 			else
 				isCompressable = false;
 
 			if (!isCompressable) {
 				dataForSending = data;
 				return;
 			}
 
 			compressData();
 			data = null;
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace(System.err);
 		}
 	}
 
 
 	private void compressData () throws IOException {
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream(data.length);
 		GZIPOutputStream gzip = new GZIPOutputStream(outStream);
 		gzip.write(data);
 		gzip.close();
 
 		dataForSending = outStream.toByteArray();
 	}
 
 
 	@Override
 	public String getContentType () {
 		return "application/json; charset=" + CHARSET;
 	}
 
 
 	@Override
 	public InputStream getStream () throws IOException {
 		return new ByteArrayInputStream(dataForSending);
 	}
 
 
 	@Override
 	public void prepareResponse (Response response) {
 		if (isCompressable) {
 			response.setHeader("Content-Encoding", "gzip");
			response.setIntHeader("Content-Length", dataForSending.length + 20);
 		}
 	}
 }
