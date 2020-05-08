 package it.grid.storm;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.jackrabbit.webdav.client.methods.CopyMethod;
 import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
 import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
 import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
 import org.apache.jackrabbit.webdav.client.methods.OptionsMethod;
 import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
 
 public class TestClient {
 
 	private static String GRIDHTTPS_HOSTNAME = "omii005-vm03.cnaf.infn.it";
 	private static int GRIDHTTPS_HTTP_PORT = 8085;
 	private static String TEST_DIRECTORY = "/testdir2";
 	
	private static String ALLPROP = "<?xml version=\"1.0\" encoding=\"utf-8\"?><propfind xmlns=\"DAV:\"><allprop/></propfind>";
	private static String FILECONTENT = "Contenuto del file di prova.";
	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length > 0)
 			GRIDHTTPS_HOSTNAME = args[0];
 		HttpClient client = new HttpClient();
 		System.out.println();
 		System.out.println("Testing gridhttps-server: " + GRIDHTTPS_HOSTNAME);
 		System.out.println();
		try {
			test(client, "http", GRIDHTTPS_HOSTNAME, GRIDHTTPS_HTTP_PORT, "free");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 	
	private static void test(HttpClient client, String protocol, String host, int port, String storagearea) throws IOException {
 		final String baseUrl = protocol + "://" + host + ":" + port + "/" + storagearea;
		
		File filePathToUpload1 = new File("/tmp/fileToUpload.txt");
		filePathToUpload1.createNewFile();
		FileOutputStream writer = new FileOutputStream(filePathToUpload1);
		writer.write(FILECONTENT.getBytes());
		writer.flush();
		writer.close();
		
		File allprop = new File("/tmp/allprops.xml");
		allprop.createNewFile();
		writer = new FileOutputStream(allprop);
		writer.write(ALLPROP.getBytes());
		writer.flush();
		writer.close();
 		
 		//OPTIONS
 		options(client, baseUrl, "");
 		System.out.println("");
 		//MKCOL
 		mkcol(client, baseUrl, TEST_DIRECTORY);
 		System.out.println("");
 		//PUT file
 		put(client, baseUrl, filePathToUpload1, TEST_DIRECTORY + "/" + filePathToUpload1.getName());
 		System.out.println("");
 		//GET file
 		get(client, baseUrl, TEST_DIRECTORY + "/" + filePathToUpload1.getName());
 		System.out.println("");
 		//PUT OVERWRITE no overwrite header
 		put(client, baseUrl, filePathToUpload1, TEST_DIRECTORY + "/" + filePathToUpload1.getName());
 		System.out.println("");
 		//PUT OVERWRITE
 		putOverwrite(client, baseUrl, filePathToUpload1, TEST_DIRECTORY + "/" + filePathToUpload1.getName());
 		System.out.println("");
 		//COPY file
 		copy(client, baseUrl, TEST_DIRECTORY + "/" +filePathToUpload1.getName(), TEST_DIRECTORY + "/copia_" + filePathToUpload1.getName(), false);
 		System.out.println("");
 		//COPY file overwrite
 		copy(client, baseUrl, TEST_DIRECTORY + "/" +filePathToUpload1.getName(), TEST_DIRECTORY + "/copia_" + filePathToUpload1.getName(), true);
 		System.out.println("");
 		//GET dir
 		get(client, baseUrl, TEST_DIRECTORY);
 		System.out.println("");
 		//MKCOL
 		mkcol(client, baseUrl, TEST_DIRECTORY + "/subdir");
 		System.out.println("");
 		//OPTIONS
 		options(client, baseUrl, TEST_DIRECTORY + "/subdir");
 		System.out.println("");
 		//PUT file
 		put(client, baseUrl, filePathToUpload1, TEST_DIRECTORY + "/subdir/" + filePathToUpload1.getName());
 		System.out.println("");
 		//COPY dir
 		copy(client, baseUrl, TEST_DIRECTORY + "/subdir", TEST_DIRECTORY + "/copia_subdir", false);
 		System.out.println("");
 		//MOVE dir
 		move(client, baseUrl, TEST_DIRECTORY + "/copia_subdir", TEST_DIRECTORY + "/subdir2", false);
 		System.out.println("");
 		//MOVE overwrite
 		copy(client, baseUrl, TEST_DIRECTORY + "/copia_"+filePathToUpload1.getName(), TEST_DIRECTORY + "/"+ filePathToUpload1.getName(), true);
 		System.out.println("");
 		//PROPFIND
 		propfind(client, baseUrl, allprop, TEST_DIRECTORY + "/" + filePathToUpload1.getName());
 		System.out.println("");
 		//PROPFIND
 		propfind(client, baseUrl, allprop, TEST_DIRECTORY);
 		System.out.println("");
 		//DELETE file
 		delete(client, baseUrl, TEST_DIRECTORY + "/subdir/" + filePathToUpload1.getName());
 		System.out.println("");
 		//DELETE dir
 		delete(client, baseUrl, TEST_DIRECTORY);
 		System.out.println("");
 	}
 	
 	private static void printSuccess(boolean expression) {
 		if (expression) {
 			System.out.println("SUCCESS");
 		} else {
 			System.out.println("ERROR");
 		}
 	}
 	
 	private static void printSuccess(int statusCode, int[] successCodes) {
 		for (int code : successCodes)
 			if (code == statusCode) {
 				System.out.println("SUCCESS");
 				return;
 			}
 		System.out.println("ERROR");
 	}
 	
 	private static void put(HttpClient client, String baseUrl, File toUpload, String filePath) {
 		int[] successCodes = {201, 204};
 		try {
 			PutMethod method = new PutMethod(baseUrl + filePath);
 			System.out.println("PUT " + baseUrl + filePath);
 			RequestEntity requestEntity = new InputStreamRequestEntity(new FileInputStream(toUpload));
 			method.setRequestEntity(requestEntity);
 			client.executeMethod(method);
 			System.out.println(method.getStatusCode() + " " + method.getStatusText());
 			printSuccess(method.getStatusCode(), successCodes);
 		} catch (HttpException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		} catch (IOException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		}
 	}
 	
 	private static void putOverwrite(HttpClient client, String baseUrl, File toUpload, String filePath) {
 		int[] successCodes = {204};
 		try {
 			PutMethod method = new PutMethod(baseUrl + filePath);
 			method.addRequestHeader("Overwrite", "T");
 			System.out.println("PUT " + baseUrl + filePath);
 			RequestEntity requestEntity = new InputStreamRequestEntity(new FileInputStream(toUpload));
 			method.setRequestEntity(requestEntity);
 			client.executeMethod(method);
 			System.out.println(method.getStatusCode() + " " + method.getStatusText());
 			printSuccess(method.getStatusCode(), successCodes);
 		} catch (HttpException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		} catch (IOException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		}
 	}
 	
 	private static void mkcol(HttpClient client, String baseUrl, String pathToDir) {
 		int[] successCodes = {201};
 		try {
 			MkColMethod method = new MkColMethod(baseUrl + pathToDir);
 			System.out.println("MKCOL " + baseUrl + pathToDir);
 			client.executeMethod(method);
 			System.out.println(method.getStatusCode() + " " + method.getStatusText());
 			printSuccess(method.getStatusCode(), successCodes);
 		} catch (HttpException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		} catch (IOException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		}
 	}
 	
 	private static void get(HttpClient client, String baseUrl, String filePath) {
 		int[] successCodes = {200};
 		try {
 			GetMethod method = new GetMethod(baseUrl + filePath);
 			System.out.println("GET " + baseUrl + filePath);
 			client.executeMethod(method);
 			System.out.println(method.getStatusCode() + " " + method.getStatusText());
 			printSuccess(method.getStatusCode(), successCodes);
 		} catch (HttpException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		} catch (IOException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		}
 	}
 	
 	private static void copy(HttpClient client, String baseUrl, String srcPath, String destPath, boolean overwrite) {
 		try {
 			CopyMethod method = new CopyMethod(baseUrl + srcPath, baseUrl + destPath, overwrite);
 			System.out.println("COPY " + baseUrl + srcPath + " " + baseUrl + destPath);
 			client.executeMethod(method);
 			System.out.println(method.getStatusCode() + " " + method.getStatusText());
 			printSuccess((method.getStatusCode() == 201 && !overwrite) || (method.getStatusCode() == 204 && overwrite));
 		} catch (HttpException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		} catch (IOException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		}
 	}
 	
 	private static void move(HttpClient client, String baseUrl, String srcPath, String destPath, boolean overwrite) {
 		try {
 			MoveMethod method = new MoveMethod(baseUrl + srcPath, baseUrl + destPath, overwrite);
 			System.out.println("MOVE " + baseUrl + srcPath + " " + baseUrl + destPath);
 			client.executeMethod(method);
 			System.out.println(method.getStatusCode() + " " + method.getStatusText());
 			printSuccess((method.getStatusCode() == 201 && !overwrite) || (method.getStatusCode() == 204 && overwrite));
 		} catch (HttpException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		} catch (IOException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		}
 	}
 
 	private static void delete(HttpClient client, String baseUrl, String path) {
 		int[] successCodes = {204};
 		try {
 			DeleteMethod method = new DeleteMethod(baseUrl + path);
 			System.out.println("DELETE " + baseUrl + path);
 			client.executeMethod(method);
 			System.out.println(method.getStatusCode() + " " + method.getStatusText());
 			printSuccess(method.getStatusCode(), successCodes);
 		} catch (HttpException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		} catch (IOException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		}
 	}
 
 	private static void options(HttpClient client, String baseUrl, String path) {
 		int[] successCodes = {200};
 		try {
 			OptionsMethod method = new OptionsMethod(baseUrl + path);
 			System.out.println("OPTIONS " + baseUrl + path);
 			client.executeMethod(method);
 			System.out.println(method.getStatusCode() + " " + method.getStatusText());
 			System.out.println(method.getResponseHeader("DAV"));
 			printSuccess(method.getStatusCode(), successCodes);
 		} catch (HttpException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		} catch (IOException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		}
 	}
 
 	private static void propfind(HttpClient client, String baseUrl, File body, String filePath) {
 		int[] successCodes = {207};
 		try {
 			PropFindMethod method = new PropFindMethod(baseUrl + filePath);
 			System.out.println("PROPFIND " + baseUrl + filePath);
 			RequestEntity requestEntity = new InputStreamRequestEntity(new FileInputStream(body));
 			method.setRequestEntity(requestEntity);
 			client.executeMethod(method);
 			System.out.println(method.getStatusCode() + " " + method.getStatusText());
 			printSuccess(method.getStatusCode(), successCodes);
 		} catch (HttpException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		} catch (IOException ex) {
 			System.out.println(ex.getMessage());
 			// Handle Exception
 		}
 	}
 	
 }
