 package model.util;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 
 public class Base64Util {
 
 	public static String decode(String text) {
 		return new String(new Base64().decode(text));
 	}
 
 	public static File encodeToFile(File file) throws IOException, InterruptedException {
 		File encoded = File.createTempFile("encoded", ".base64");
 		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(encoded));
 		RandomAccessFile f = new RandomAccessFile(file, "r");
 		byte[] b = new byte[(int)f.length()];
 		f.read(b);
 		bos.write(new Base64().encode(b));
 		f.close();
 		bos.flush();
 		bos.close();
 		return encoded;
 	}
 
 	public static File decodeUsingOS(String text) throws IOException, InterruptedException {
		File file = createFileWithContents(text);
		List<String> commands = new LinkedList<String>();
		commands.add("base64");
		String osname = System.getProperty("os.name");
		if ("mac os x".equals(osname.toLowerCase()) ) {
			commands.add("-D");
		} else {
			commands.add("-d");
		}
		commands.add(file.getAbsolutePath());
		ProcessBuilder pb = new ProcessBuilder(commands);
		File decodedContents = File.createTempFile("decode_", ".tmp");
		Process process = pb.start();
		process.waitFor();
		IOUtil.redirectOutputStream(process.getInputStream(), decodedContents);
		file.delete();
		return decodedContents;
	}
 
	private static File createFileWithContents(String text) throws IOException {
		File file = File.createTempFile("decode_", ".tmp");
		FileWriter writer = new FileWriter(file);
		writer.append(text);
		writer.flush();
		writer.close();
		return file;
	}
 }
