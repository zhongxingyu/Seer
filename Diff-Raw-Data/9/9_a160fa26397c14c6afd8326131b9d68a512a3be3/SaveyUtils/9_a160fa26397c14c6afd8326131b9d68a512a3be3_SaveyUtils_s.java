 package com.megadevs.savey.apis;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.stream.JsonWriter;
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.WriterException;
 import com.google.zxing.client.j2se.MatrixToImageWriter;
 import com.google.zxing.common.BitMatrix;
 import com.google.zxing.qrcode.QRCodeWriter;
 
 public class SaveyUtils {
 
 	public static final String CALLBACK = "callback";
 
 	public static String prepareResponse(Object list, String callback) {
 		Gson gson = new GsonBuilder().setPrettyPrinting().create();
 		String json = gson.toJson(list);
 
 		if (callback != null)
 			return callback + "(" + json + ");";
 		else return json;
 	}
 	
 	public static String getDatabasePath() {
 
 		File f = new File("/home/ubuntu/.savey/savey.db");
 		return f.getAbsolutePath();
 	}
 	
 	public static String prepareInputForQRCode(int id) throws IOException {
 		
 		StringWriter sWriter = new StringWriter();
 		
 		JsonWriter writer = new JsonWriter(sWriter);
 		writer.beginObject();
 		writer.name("savey");
 		writer.value(id);
 		writer.endObject();
 		
 		return writer.toString();
 	}
 	
 	public static String generateQRCode(String input) throws WriterException, IOException {
 		
 		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix matrix = writer.encode(input, BarcodeFormat.QR_CODE, 25, 25);
 
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		MatrixToImageWriter.writeToStream(matrix, "PNG", out);
 
 		return new String(Base64.encodeBase64(out.toByteArray()));
 		
 	}
 	
 	public static enum IDS {
 		USER_ID("user_id"),
 		MACHINE_ID("machine_id"),
 		TASK_ID("task_id"),
 		USER_TASK_ID("user_task_id"),
 		RESULT("result");
 			
 		private String id;
 		
 		private IDS(String id) {
 			this.id = id;
 		}
 		
 		public String getID() {
 			return id;
 		}
 		
 	}
 	
 }
