 /**
  * Client for the REST API of the Persistent Identifier Service
  * For details see: http://www.pidconsortium.eu/index.php?page=process
  */
 //package de.gwdg.pidservice;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import sun.misc.BASE64Encoder;
 
 import java.io.*;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.io.IOException;
 import java.math.BigInteger;
 import edu.harvard.hul.ois.fits.*;
 import edu.harvard.hul.ois.fits.exceptions.FitsConfigurationException;
 import edu.harvard.hul.ois.fits.exceptions.FitsException;
 
 import java.util.regex.*;
 
 public class PidClient {
 	//hardcode for conviniece
 	static String serviceUser = "griduser9";
 	static String servicePwd = "doLhj10En4";
 	final static boolean verbose = false;
 	
 	//the necessary FITS libs are located in FitsHome
 	public static String FitsHome = ".";
 
 	public static void main(String[] args) throws Exception {
 		if (args[0].equals("-c")) {
 		//create with fits data
 			String file = args[1];
 			serviceUser = args[3];
 			servicePwd = args[4];
 			String author = null;
 			String title = null;
 			String checksum;
 
 			checksum = md5(file);
 
 			try {
 				title = getFitsValue(file, "title");
 				author = getFitsValue(file, "author");
 			}
 			catch (FileNotFoundException e) {
 				System.out.println("Could not find file: " + file);
 				if (verbose) e.printStackTrace();
 			}
 			// got author and title so creat it with
 			if (title != null && author != null) {
 				try {
 					createPid(file, checksum, author, title);
 				}
 				catch (IOException e) {
 					System.out.println("Could not find info for PID \"" + args[0] + "\".");
 					if (verbose) e.printStackTrace();
 				}
 			}
 			else {
 				try {
 					createPid(file, checksum);
 				}
 				catch (IOException e) {
 					System.out.println("Could not find info for PID \"" + args[0] + "\".");
 					if (verbose) e.printStackTrace();
 				}
 
 			}
 		}
 		if (args[0].equals("-m")) {
 			//modify
 			String pid = args[1];
 			String field = args[2];
 			String value = args[3];
			serviceUser = args[5];
			servicePwd = args[6];
 			modifyPid(pid, field, value);

 		}
 		else {
 			//check for PID
 			if (args.length == 1) {
 				try {
 					searchPid(args[0]);
 				}
 				catch (IOException e) {
 					System.out.println("Could not find info for PID \"" + args[0] + "\".");
 					if (verbose) e.printStackTrace();
 				}
 			}
 			//create
 			else if (args.length == 3) {
 				serviceUser = args[1];
 				servicePwd = args[2];
 				createPid(args[0]);
 			}
 			//info
 			else {
 				String info = "Number of arguemnts determines function:\n";
 				info += "\tjava PidClient $SomePid  -- Querry for $SomePid\n";
 				info += "\tjava PidClient $uri $user $pw -- Create new Handle from $uri with $user and $pw\n";
 
 				System.out.println(info);
 			}
 		}
 		//String pid = "11858/00-STUD-0000-0000-13D9-3"; //christians own CID
 		//createPid("https://github.com/cap1/pke-presentation/blob/master/presentation.pdf");
 		//searchPid(pid);
 		//modifyPid(pid);
 		//searchPid(pid);
 	}
 
 	public static void searchPid(String pid) throws IOException {
 		String serviceUrl = "http://hdl-test.gwdg.de:8080/pidservice/read/search";
 		String serviceParam = URLEncoder.encode(pid, "UTF-8");
 
 		URL url = new URL(serviceUrl + "?" + "pid=" + serviceParam);
 		URLConnection connection = url.openConnection();
 
 		BufferedReader in = new BufferedReader(new InputStreamReader(
 				connection.getInputStream()));
 
 		String decodedString;
 		while ((decodedString = in.readLine()) != null) {
 			System.out.println(decodedString);
 		}
 		in.close();
 	}
 
 	public static void createPid(String fileURL, String checksum) throws IOException {
 		String serviceUrl = "http://hdl-test.gwdg.de:8080/pidservice/write/create";
 
 		String title = fileURL;
 		String serviceParamFileURL = URLEncoder.encode(fileURL, "UTF-8");
 		String serviceParamChecksum = URLEncoder.encode(checksum, "UTF-8");
 
 		URL url = new URL(serviceUrl);
 		HttpURLConnection urlConnection = null;
 		BufferedReader in = null;
 		try {
 			urlConnection = (HttpURLConnection) url.openConnection();
 			urlConnection.setDoInput(true);
 			urlConnection.setDoOutput(true);
 			urlConnection.setRequestProperty("Content-Type",
 					"application/x-www-form-urlencoded");
 			urlConnection.setRequestMethod("POST");
 
 			String authCred = serviceUser + ":" + servicePwd;
 			String encodedAuthCred = new sun.misc.BASE64Encoder()
 					.encode(authCred.getBytes());
 			urlConnection.setRequestProperty("Authorization", "Basic "
 					+ encodedAuthCred);
 			/*
 			 * Do not use sun.misc.* , see: "Sun proprietary API" 
 			 * instead, use "Commons Codec library" for Base64 Encoder
 			 * (http://commons.apache.org/codec/)
 			 * import org.apache.commons.codec.DecoderException; 
 			 * import org.apache.commons.codec.binary.Base64;
 			 */
 
 			OutputStreamWriter out = new OutputStreamWriter(
 					urlConnection.getOutputStream());
 			out.write("url=" + serviceParamFileURL);
 			out.write("&checksum=" + serviceParamChecksum);
 			out.write("&encoding=xml");
 			out.close();
 
 			in = new BufferedReader(new InputStreamReader(
 					urlConnection.getInputStream()));
 
 			String decodedString;
 
 			while ((decodedString = in.readLine()) != null) {
 				System.out.println(decodedString);
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static void createPid(String fileURL, String checksum, String author, String title ) throws IOException {
 		String serviceUrl = "http://hdl-test.gwdg.de:8080/pidservice/write/create";
 
 		String serviceParamFileURL = URLEncoder.encode(fileURL, "UTF-8");
 		String serviceParamChecksum = URLEncoder.encode(checksum, "UTF-8");
 		String serviceParamAuthor = URLEncoder.encode(author, "UTF-8");
 		String serviceParamTitle = URLEncoder.encode(title, "UTF-8");
 
 		URL url = new URL(serviceUrl);
 		HttpURLConnection urlConnection = null;
 		BufferedReader in = null;
 		try {
 			urlConnection = (HttpURLConnection) url.openConnection();
 			urlConnection.setDoInput(true);
 			urlConnection.setDoOutput(true);
 			urlConnection.setRequestProperty("Content-Type",
 					"application/x-www-form-urlencoded");
 			urlConnection.setRequestMethod("POST");
 
 			String authCred = serviceUser + ":" + servicePwd;
 			String encodedAuthCred = new sun.misc.BASE64Encoder()
 					.encode(authCred.getBytes());
 			urlConnection.setRequestProperty("Authorization", "Basic "
 					+ encodedAuthCred);
 			/*
 			 * Do not use sun.misc.* , see: "Sun proprietary API" 
 			 * instead, use "Commons Codec library" for Base64 Encoder
 			 * (http://commons.apache.org/codec/)
 			 * import org.apache.commons.codec.DecoderException; 
 			 * import org.apache.commons.codec.binary.Base64;
 			 */
 
 			OutputStreamWriter out = new OutputStreamWriter(
 					urlConnection.getOutputStream());
 			out.write("url=" + serviceParamFileURL);
 			out.write("&author=" + serviceParamAuthor);
 			out.write("&title=" + serviceParamTitle);
 			out.write("&checksum=" + serviceParamChecksum);
 			out.write("&encoding=xml");
 			out.close();
 
 			in = new BufferedReader(new InputStreamReader(
 					urlConnection.getInputStream()));
 
 			String decodedString;
 
 			while ((decodedString = in.readLine()) != null) {
 				System.out.println(decodedString);
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 	public static void createPid(String fileURL) throws IOException {
 		String serviceUrl = "http://hdl-test.gwdg.de:8080/pidservice/write/create";
 
 		String title = fileURL;
 		String serviceParam1 = URLEncoder.encode(fileURL, "UTF-8");
 
 		URL url = new URL(serviceUrl);
 		HttpURLConnection urlConnection = null;
 		BufferedReader in = null;
 		try {
 			urlConnection = (HttpURLConnection) url.openConnection();
 			urlConnection.setDoInput(true);
 			urlConnection.setDoOutput(true);
 			urlConnection.setRequestProperty("Content-Type",
 					"application/x-www-form-urlencoded");
 			urlConnection.setRequestMethod("POST");
 
 			String authCred = serviceUser + ":" + servicePwd;
 			String encodedAuthCred = new sun.misc.BASE64Encoder()
 					.encode(authCred.getBytes());
 			urlConnection.setRequestProperty("Authorization", "Basic "
 					+ encodedAuthCred);
 			/*
 			 * Do not use sun.misc.* , see: "Sun proprietary API" 
 			 * instead, use "Commons Codec library" for Base64 Encoder
 			 * (http://commons.apache.org/codec/)
 			 * import org.apache.commons.codec.DecoderException; 
 			 * import org.apache.commons.codec.binary.Base64;
 			 */
 
 			OutputStreamWriter out = new OutputStreamWriter(
 					urlConnection.getOutputStream());
 			out.write("url=" + serviceParam1);
 			out.write("&encoding=xml");
 			out.close();
 
 			in = new BufferedReader(new InputStreamReader(
 					urlConnection.getInputStream()));
 
 			String decodedString;
 
 			while ((decodedString = in.readLine()) != null) {
 				System.out.println(decodedString);
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 
 	public static String getPidValue(String pid, String field) throws IOException {
 		String serviceUrl = "http://hdl-test.gwdg.de:8080/pidservice/read/search";
 		String serviceParam = URLEncoder.encode(pid, "UTF-8");
 		String serviceParam1 = URLEncoder.encode(field, "UTF-8");
 
 		URL url = new URL(serviceUrl + "?" + "pid=" + serviceParam + "&encoding=xml");
 		URLConnection connection = url.openConnection();
 
 		BufferedReader in = new BufferedReader(new InputStreamReader(
 		connection.getInputStream()));
 
 		String decodedString;
 		Pattern p = Pattern.compile("[ \t\n\f\r]*<" + field + ">(.*)</" + field +">");
 		String value = null;
 		while ((decodedString = in.readLine()) != null) {
			System.out.println(decodedString);
 			if (Pattern.matches("[ \t\n\f\r]*<" + field + ">.*", decodedString)) {
 				Matcher m = p.matcher(decodedString);
 				if (m.find( )) {
 					value = m.group(1);
 				}
 			}
 		}
 
 		in.close();
 		return value;
 	}
 
 	public static void modifyPid(String pid, String field, String value) throws IOException {
 		String serviceUrl = "http://hdl-test.gwdg.de:8080/pidservice/write/modify";
 		
 		String oldtitle = getPidValue(pid, "title");
 		String serviceParamOldtitle = URLEncoder.encode(oldtitle, "UTF-8");
 	
 		String serviceParam1 = URLEncoder.encode(pid, "UTF-8");
 		String serviceParam2 = URLEncoder.encode(value, "UTF-8");
 
 		URL url = new URL(serviceUrl);
 		HttpURLConnection urlConnection = null;
 		BufferedReader in = null;
 		try {
 			urlConnection = (HttpURLConnection) url.openConnection();
 			urlConnection.setDoInput(true);
 			urlConnection.setDoOutput(true);
 			urlConnection.setRequestProperty("Content-Type",
 					"application/x-www-form-urlencoded");
 			urlConnection.setRequestMethod("POST");
 
 			String authCred = serviceUser + ":" + servicePwd;
 			String encodedAuthCred = new sun.misc.BASE64Encoder()
 					.encode(authCred.getBytes());
 			urlConnection.setRequestProperty("Authorization", "Basic "
 					+ encodedAuthCred);
 			/*
 			 * Do not use sun.misc.* , see: "Sun proprietary API" 
 			 * instead, use "Commons Codec library" for Base64 Encoder
 			 * (http://commons.apache.org/codec/)
 			 * import org.apache.commons.codec.DecoderException; 
 			 * import org.apache.commons.codec.binary.Base64;
 			 */
 
 			OutputStreamWriter out = new OutputStreamWriter(
 					urlConnection.getOutputStream());
 			out.write("pid=" + serviceParam1);
 			out.write("&oldtitle=" + oldtitle);
 			out.write("&" + field + "=" + serviceParam2);
 			out.write("&encoding=xml");
 			out.close();
 
 			in = new BufferedReader(new InputStreamReader(
 					urlConnection.getInputStream()));
 
 			String decodedString;
 
 			while ((decodedString = in.readLine()) != null) {
 				System.out.println(decodedString);
 			}
 
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static String getFitsValue(String InputFile, String value) throws
 			FitsException, IOException, FitsConfigurationException,
 			FileNotFoundException, NoSuchAlgorithmException {
 		// Initializing fits
 		File testfile = new File(InputFile);
 		Fits fits = new Fits(FitsHome);
 		FitsOutput fitsOut = null;
 
 		// Examining Testfile
 		fitsOut = fits.examine(testfile);
 
 		// Save fits results to Disk
 	//	fitsOut.saveToDisk("test.xml");
 
 		// Extract Metadata of interest
 		return fitsOut.getMetadataElement(value).getValue();
 	}
 
 
 	//function to calculate MD5 checksum
 	public static String md5(String filename) throws NoSuchAlgorithmException,
 			FileNotFoundException {
 		MessageDigest digest = MessageDigest.getInstance("MD5");
 		// File f = new File("c:\\myfile.txt");
 		InputStream is = new FileInputStream(filename);
 		byte[] buffer = new byte[8192];
 		int read = 0;
 		try {
 			while ((read = is.read(buffer)) > 0) {
 				digest.update(buffer, 0, read);
 			}
 			byte[] md5sum = digest.digest();
 			BigInteger bigInt = new BigInteger(1, md5sum);
 			String output = bigInt.toString(16);
 			// System.out.println("MD5: " + output);
 			return output;
 		} catch (IOException e) {
 			throw new RuntimeException("Unable to process file for MD5", e);
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				throw new RuntimeException(
 						"Unable to close input stream for MD5 calculation", e);
 			}
 		}
 	}
 
 }
