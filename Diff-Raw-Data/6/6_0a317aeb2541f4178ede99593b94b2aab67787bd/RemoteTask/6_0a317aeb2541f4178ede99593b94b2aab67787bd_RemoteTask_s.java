 package net.idea.restnet.cli.task;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.net.URL;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.opentox.rest.RestException;
 
 
 /**
  * Convenience class to launch and poll remote POST jobs
  * @author nina
  *
  */
 public class RemoteTask implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8130424089714825424L;
 	/**
 	 * 
 	 */
 	
 	protected final URL url;
 	protected int status = -1;
 	protected String statusLine = null;
 	protected URL result = null;
 	protected Exception error = null;
 	protected HttpClient httpclient;
 	
 	public HttpClient getHttpclient() {
 		if (httpclient==null) {
 			httpclient = new DefaultHttpClient();
 		}
 		return httpclient;
 	}
 	public void setError(Exception error) {
 		this.error = error;
 	}
 	public Exception getError() {
 		return error;
 	}
 	
 	
 	/*
 	public RemoteTask(URL url,
 			  String acceptMIME, 
 			  HttpEntity content,
 			  String method) throws RestException {
 		this(null,url,acceptMIME,content,method);
 	}
 	*/
 	public RemoteTask(HttpClient httpClient, URL url,
 			  String acceptMIME, 
 			  HttpEntity content,
 			  String method) throws RestException {
 		super();
 		this.httpclient = httpClient;
 		this.url = url;
 		InputStream in = null;
 		try {
 
 			HttpRequestBase httpmethod;
 			
 			if (method.equals(HttpPost.METHOD_NAME)) {
 				httpmethod = new HttpPost(url.toURI());
 				((HttpPost)httpmethod).setEntity(content);
 			} else if (method.equals(HttpPut.METHOD_NAME)) {
 				httpmethod = new HttpPut(url.toURI());
 				((HttpPut)httpmethod).setEntity(content);
 		    } else if (method.equals(HttpDelete.METHOD_NAME))
 				httpmethod = new HttpDelete(url.toURI());
 				//client.delete();
 			else if (method.equals(HttpGet.METHOD_NAME))
 				httpmethod = new HttpGet(url.toURI());
 				//client.get();
 			else throw new RestException(HttpStatus.SC_METHOD_NOT_ALLOWED,String.format("Method %s not allowed at %s", method,url));
 			
 			httpmethod.addHeader("Accept",acceptMIME);
 			httpmethod.addHeader("Accept-Charset", "utf-8");
 			HttpResponse response = getHttpclient().execute(httpmethod);
 			HttpEntity entity  = response.getEntity();
 			
 			this.status =  response.getStatusLine().getStatusCode();
 			this.statusLine = response.getStatusLine().getReasonPhrase();
 			if (entity==null) {
 				throw new RestException(HttpStatus.SC_BAD_GATEWAY,
 						String.format("[%s] Representation not available %s",this.status,url));
 			}
 			in = entity.getContent();
 			result = handleOutput(in,status,null);
 		} catch (RestException x) {
 			status = x.getStatus();
 			try { 
 				error = new RestException(HttpStatus.SC_BAD_GATEWAY,String.format("URL=%s [%s] ",url,x.getStatus()),x); 
 			}	catch (Exception xx) { error = x; }
 		} catch (Exception x) {
 			setError(x);
 			status = -1;
 		} finally {
 			try { if (in!=null) in.close(); } catch (Exception x) { x.printStackTrace();}
 			//try { respo. } catch (Exception x) { x.printStackTrace();}
 		}
 	}	
 	
 	
 	public boolean isCompletedOK() {
 		return HttpStatus.SC_OK == status;
 	}
 	public boolean isCancelled() {
 		return HttpStatus.SC_SERVICE_UNAVAILABLE == status;
 	}
 	public boolean isAccepted() {
 		return HttpStatus.SC_ACCEPTED == status;
 	}	
 
 	public boolean isERROR() {
 		return error != null;
 	}		
 	public boolean isDone() {
 		return isCompletedOK() || isERROR() || isCancelled();
 	}		
 	public URL getUrl() {
 		return url;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 
 	public URL getResult() {
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("URL: %s\tResult: %s\tStatus: %s\t%s", url,result,status,error==null?"":error.getMessage());
 	}
 	/**
 	 * returns true if ready
 	 * @return
 	 */
 	public boolean poll() {
 
 		if (isDone()) return true;
 		InputStream in = null;
 		HttpGet httpGet = new HttpGet(result.toString());
 		httpGet.addHeader("Accept","text/uri-list");
 		try {
 			HttpResponse response = getHttpclient().execute(httpGet);
 			HttpEntity entity  = response.getEntity();
 			status = response.getStatusLine().getStatusCode();
 			statusLine = response.getStatusLine().getReasonPhrase();
 			in = entity.getContent();
 
 			if (HttpStatus.SC_SERVICE_UNAVAILABLE == status) {
 				return true;
 			}
 			result = handleOutput(in,status,result);
 		} catch (IOException x) {
 			setError(x);
 
 		} catch (RestException x) {
 			setError(x);
 			status = x.getStatus();
 			statusLine = x.getMessage();
 		} catch (Exception x) {
 			setError(x);
 			status = -1;
 			statusLine = x.getMessage();
 		} finally {
 			try {in.close();} catch (Exception x) {}
 		}
 		return isDone();
 	}
 	/**
 	 * 
 	 * @param in
 	 * @param status
 	 * @param url  the url contacted - for returning proper error only
 	 * @return
 	 * @throws ResourceException
 	 */
 	protected URL handleOutput(InputStream in,int status,URL url) throws RestException {
 		URL ref = null;
 		if (HttpStatus.SC_OK == status
 						|| HttpStatus.SC_ACCEPTED == status 
 						|| HttpStatus.SC_CREATED == status 
 						//|| Status.REDIRECTION_SEE_OTHER.equals(status)
 						|| HttpStatus.SC_SERVICE_UNAVAILABLE == status
 						) {
 
 			if (in==null) {
 				if ((HttpStatus.SC_ACCEPTED == status) && (url != null)) return url;
 				String msg = String.format("Error reading response from %s: %s. Status was %s", url==null?getUrl():url, "Empty content",status);
 				throw new RestException(HttpStatus.SC_BAD_GATEWAY,msg);
 			}
 			
 			int count=0;
 			try {
 
 				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 				String line = null;
 				while ((line = reader.readLine())!=null) {
 					if ("".equals(line.trim())) ref = null;
 					else {
 						ref = new URL(line.trim());
 						count++;
 					}
 				}
 			} catch (Exception x) {
 				throw new RestException(HttpStatus.SC_BAD_GATEWAY,
 						String.format("Error reading response from %s: %s", url==null?getUrl():url, x.getMessage()),x);
 			} finally {
 				try { in.close(); } catch (Exception x) {} ;
 			}
 			if (count == 0) 
 				if (status==HttpStatus.SC_OK) return null;
 				else return url==null?getUrl():url;
 			/* A hack for the validation service returning empty responses on 200 OK ...
 				throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,
 							String.format("No task status indications from %s",url==null?getUrl():url));
 			*/
 			return ref;
 						
 		} else { //everything else considered an error
			throw new RestException(status,statusLine);
 		}
 	}
 
 	public void waitUntilCompleted(int sleepInterval) throws Exception {
 		FibonacciSequence seq = new FibonacciSequence();
 		while (!poll()) {
 			Thread.yield();
 			Thread.sleep(seq.sleepInterval(sleepInterval,true,1000 * 60 * 5)); //
 			//TODO timeout
 			System.out.print("poll ");
 			System.out.println(this);
 		}
 		if (isERROR()) throw getError();
 	}
 }
