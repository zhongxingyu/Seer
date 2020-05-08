 package com.designs_1393.asana;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.params.HttpParams;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpEntity;
 import org.apache.http.Header;
 
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.client.HttpResponseException;
 
 // query parameters
 import android.net.Uri;
 import android.net.Uri.Builder;
 import java.net.URI;
 
 // post parameters
 import java.util.List;
 import java.util.ArrayList;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 
 import android.util.Log;
 
 import java.net.URLEncoder;
 
public class AsanaAPI
 {
 	private UsernamePasswordCredentials creds;
 	private DefaultHttpClient httpclient = new DefaultHttpClient();
 
 	private boolean usePrettyPrint = false;
 	private final Uri API_BASE = Uri.parse( "https://app.asana.com/api/1.0" );
 	private final String TAG = "AsanaHelper";
 
	public AsanaAPI( String APIkey )
 	{
 		creds = new UsernamePasswordCredentials(APIkey, "");
 	}
 
 	public void usePrettyPrint( boolean choice )
 	{
 		usePrettyPrint = choice;
 	}
 
 	public String getWorkspaces()
 	{
 		try{
 			Uri.Builder uri = API_BASE.buildUpon();
 			uri.appendPath( "workspaces" );
 			if( usePrettyPrint )
 				uri.appendQueryParameter( "opt_pretty", "true" );
 
 			HttpGet httpget = new HttpGet( uri.build().toString() );
 			httpget.addHeader( BasicScheme.authenticate(creds, "US-ASCII", false) );
 
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
 			String responseBody = httpclient.execute(httpget, responseHandler);
 			return responseBody;
 		} catch( Exception e ){ e.printStackTrace(); }
 
 		return "";
 	}
 
 	public String getAllProjects()
 	{
 		try{
 			Uri.Builder uri = API_BASE.buildUpon();
 			uri.appendPath("projects");
 			if( usePrettyPrint )
 				uri.appendQueryParameter( "opt_pretty", "true" );
 
 			HttpGet httpget = new HttpGet( uri.build().toString() );
 			httpget.addHeader( BasicScheme.authenticate(creds, "US-ASCII", false) );
 
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
 			String responseBody = httpclient.execute(httpget, responseHandler);
 			return responseBody;
 		} catch( Exception e ){ e.printStackTrace(); }
 
 		return "";
 	}
 
 	public String getProjectsInWorkspace( long workspaceID )
 	{
 		try{
 			Uri.Builder uri = API_BASE.buildUpon();
 			uri.appendPath("workspaces");
 			uri.appendPath(String.valueOf(workspaceID));
 			uri.appendPath("projects");
 
 			if( usePrettyPrint )
 				uri.appendQueryParameter( "opt_pretty", "true" );
 
 			HttpGet httpget = new HttpGet( uri.build().toString() );
 			httpget.addHeader( BasicScheme.authenticate(creds, "US-ASCII", false) );
 
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
 			String responseBody = httpclient.execute(httpget, responseHandler);
 			return responseBody;
 		} catch( Exception e ){ e.printStackTrace(); }
 
 		return "";
 	}
 
 	public String createTask( String name, long workspaceID )
 	{
 		try{
 			Uri.Builder uri = API_BASE.buildUpon();
 			//uri.appendPath("workspaces");
 			//uri.appendPath(String.valueOf(workspaceID));
 			uri.appendPath("tasks");
 
 			if( usePrettyPrint )
 				uri.appendQueryParameter( "opt_pretty", "true" );
 
 			List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
 			args.add( new BasicNameValuePair( "name", name ) );
 			args.add( new BasicNameValuePair( "workspace", String.valueOf(workspaceID) ) );
 
 			Log.i( TAG, "REQUEST = " +uri.build().toString() );
 
 			HttpPost httppost = new HttpPost( new URI(uri.build().toString()) );
 			httppost.addHeader( BasicScheme.authenticate(creds, "US-ASCII", false) );
 
 			httppost.setEntity( new UrlEncodedFormEntity( args, "UTF-8" ) );
 
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
 			String responseBody = httpclient.execute(httppost, responseHandler);
 			return responseBody;
 		} catch( Exception e ){ e.printStackTrace(); }
 
 		return "";
 	}
 }
