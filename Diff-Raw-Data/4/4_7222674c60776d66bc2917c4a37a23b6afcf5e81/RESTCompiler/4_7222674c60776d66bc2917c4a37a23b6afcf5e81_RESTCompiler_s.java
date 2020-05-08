 package io.prince.java.CompileInREST;
 
 import java.io.*;
 import java.net.*;
 import java.util.Arrays;
 
 import javax.tools.*;
 import javax.tools.JavaFileObject.Kind;
 
 import org.apache.http.*;
 import org.apache.http.client.*;
 import org.apache.http.client.methods.*;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.*;
 
 import io.prince.java.CompileInMemory.*;
 
 public class RESTCompiler extends SomeSortOfCompiler
 {
 	private String host;
 
 	// may also be host:port in one string
 	public RESTCompiler(String host)
 	{
 		super();
 		
 		this.host = host;
 	}
 	
 	public boolean compile(String sourceCode, String className)
 	{
 		System.out.println("Compiling...");
 
 		// http://www.java2s.com/Code/Java/JDK-6/CompilingfromMemory.htm
 		// from super.compile()
 		JavaFileObject source = new JavaSourceFromString(className, sourceCode);
 		/*Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(source);
 
 		CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
 		
 		boolean success = task.call();*/
 		
 		System.out.println("Calling REST service.");
 		
 		// get from IDIUMSlibrary
 		// TODO: fill this in with real code
 		
 		// connect to server
 		// call to REST service
 		boolean success = false;
 		try
 		{
			byte[] byteCode = postWithApache(sourceCode);
 			
 			System.out.println("First character: "+byteCode[0]);
 			
 			if(byteCode[0] == 10)
 			{
 				System.err.println("First character is a 10.");
 				byteCode = Arrays.copyOfRange(byteCode, 1, byteCode.length);
 				System.out.println("New length is "+byteCode.length);
 			}
 			
 			System.out.println(new String(byteCode));
 			
 			//String fileName = className + ".class";	// TODO: is this right?
 			
 			// May provide null as 'sibling'. Not sure which is best. sibling used to 'hint' at location.
 			JavaFileObject classFile = this.fileManager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, className, Kind.CLASS, source);
 			
 			//ByteArrayInputStream bais = new ByteArrayInputStream(byteCode);
 			OutputStream classStream = classFile.openOutputStream();
 			// TODO: write() byte array to classStream
 			classStream.write(byteCode);
 			
 			success = true;
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			
 			System.err.println("Compilation failed for some reason.");
 			success = false;
 		}
 		
 		if(success) System.out.println("Compiled successfully!");
 		
 		classLoader = new MemoryClassLoader(fileManager);
 		
 		return success;
 	}
 	
 	private byte[] postWithJava(String sourceCode) throws IOException
 	{
 		String uri = "http://" + this.host + "/compile";
 		
 		URL url = new URL(uri);
 		HttpURLConnection h = (HttpURLConnection)url.openConnection();
 		
 		h.setAllowUserInteraction(false);
 		h.setRequestMethod("POST");
 		h.setDoOutput(true);
 		
 		String body = sourceCode;
 		
 		h.setRequestProperty("Content-type", "text/plain");
 		h.setRequestProperty("Content-length", ""+body.getBytes().length);
 		
 		PrintWriter pw = new PrintWriter(h.getOutputStream());
 		pw.print(body);
 		pw.flush();
 		
 		
 		//System.out.println(h.getResponseCode());
 		//System.out.println(h.getResponseMessage());
 		
 		byte[] classFile = CompileResponse.convertInputStreamToByteArray(h.getInputStream());
 		
 		System.out.println("Response claimed length: "+h.getHeaderField("Content-length"));
 		System.out.println("Read stream bytes: "+classFile.length);
 		
 		return classFile;
 	}
 	
 	private byte[] postWithApache(String sourceCode) throws IOException
 	{
 		String uri = "http://" + this.host + "/compile";
 		
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(uri);
 
 		
 		String body = sourceCode;
 		
 		httppost.addHeader("Content-type", "text/plain");
 		//httppost.addHeader("Content-length", ""+body.getBytes().length);
 		
 //		PrintWriter pw = new PrintWriter(h.getOutputStream());
 //		pw.print(body);
 //		pw.flush();
 		
 		httppost.setEntity(new StringEntity(body));
 		
 		HttpResponse response = httpclient.execute(httppost);
 		
 		
 		
 		//byte[] classFile = CompileResponse.convertInputStreamToByteArray(h.getInputStream());
 		HttpEntity result = response.getEntity();
 		byte[] classFile = CompileResponse.convertInputStreamToByteArray(result.getContent());
 		
 		Header contentLength = response.getFirstHeader("Content-length");
 		System.out.println("Response claimed length: "+contentLength.getValue());
 		System.out.println("Read stream bytes: "+classFile.length);
 		
 		return classFile;
 	}
 
 }
