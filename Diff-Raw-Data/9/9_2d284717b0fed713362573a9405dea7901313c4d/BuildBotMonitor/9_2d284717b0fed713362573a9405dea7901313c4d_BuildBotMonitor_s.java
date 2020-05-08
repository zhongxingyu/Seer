 /*
  * BuildMonitor.java
  *
  * Copyright (C) 2010 Mario Sanchez Prada
  * Authors: Mario Sanchez Prada <msanchez@igalia.com>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of version 2 of the GNU General Public
  * License as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this program; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  *
  */
 
 package com.igalia.msanchez.webkitwatcher;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.Toast;
 
 import com.igalia.msanchez.webkitwatcher.Builder.BuildResult;
 
 public class BuildBotMonitor implements Runnable {
 
     private WebKitWatcher watcher;
     private String url;
     private String[] supportedRegexps;
     private Map<String, Builder> builders;
     private ProgressDialog progressDialog;
 
     public BuildBotMonitor (WebKitWatcher watcher) {
 	this.watcher = watcher;
 	this.url = "http://build.webkit.org";
 	this.builders = null;
 
 	// Initialize the list of supported builders
 	this.supportedRegexps = this.watcher.getResources().getStringArray(R.array.core_buildbots_regexps);
 
 	// Initialize hash table for the actual builders
 	this.builders = new HashMap<String, Builder>();
 	this.progressDialog = null;
     }
 
     public String getURL() {
 	return this.url;
     }
 
     public Map<String, Builder> getBuilders() {
 	return this.builders;
     }
 
     public void refreshState() {
 	// Check whether there's a valid network connection
 	ConnectivityManager connMan = (ConnectivityManager) this.watcher.getSystemService(Context.CONNECTIVITY_SERVICE);
 	NetworkInfo networkInfo = connMan.getActiveNetworkInfo();
 
 	// Check network connection before doing anything
 	if (networkInfo != null && networkInfo.isConnected()) {
 	    this.progressDialog = ProgressDialog.show(this.watcher,
 		    this.watcher.getString(R.string.refreshing_title),
 		    this.watcher.getString(R.string.refreshing_message),
 		    true, true);
 
 	    Thread thread = new Thread(this);
 	    thread.start();
 	} else {
 	    Toast.makeText(this.watcher.getApplicationContext(),
 		    this.watcher.getString(R.string.error_no_network),
 		    Toast.LENGTH_SHORT).show();
 	}
     }
 
     @Override
     public void run() {
 	String htmlContent = null;
 	Message msg = new Message();
 	try {
 	    // Read the HTML and process it
 	    htmlContent = this.retrieveHTML();
 	    if (htmlContent.length() > 0) {
 		this.processHTMLData(htmlContent);
 	    } else {
 		// No HTML found remotely
 		msg.obj = watcher.getString(R.string.error_no_html_found);
 	    }
 	} catch (Exception e) {
 	    // Propagate exception's message
 	    msg.obj = e.toString();
 	}
 
 	// Send message to the handler to close the thread
 	handler.sendMessage(msg);
     }
 
     private Handler handler = new Handler() {
 	@Override
 	public void handleMessage(Message msg) {
	    progressDialog.dismiss();
 	    watcher.updateView();
 
 	    if (msg != null && msg.obj != null) {
 		Toast.makeText(watcher.getApplicationContext(),
 			watcher.getString(R.string.error_retrieving_data) + ":\n"
 			+ (String) msg.obj,
 			Toast.LENGTH_SHORT).show();
 	    }
 	}
     };
 
     private String retrieveHTML() throws Exception {
 	String result = "";
 	try {
 	    URL url = new URL(this.url + "/builders");
 	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 	    connection.setDoInput(true);
 	    connection.setDoOutput(true);
 	    connection.setUseCaches(false);
 	    connection.setRequestMethod("GET");
 	    connection.setConnectTimeout(20000);
 	    connection.setReadTimeout(20000);
 
 	    InputStream is = connection.getInputStream();
 	    InputStreamReader isr = new InputStreamReader(is);
 	    BufferedReader reader = new BufferedReader(isr);
 
 	    String line = reader.readLine();
 	    while (line != null) {
 		result += line;
 		line = reader.readLine();
 	    }
 
 	    reader.close();
 	    isr.close();
 	    is.close();
 	} catch (Exception e) {
 	    throw e;
 	}
 
 	return result;
     }
 
     private void processHTMLData(String htmlContent) {
 	Pattern rootPattern = Pattern.compile("<?.tr>");
 	Pattern builderPattern = Pattern.compile(".*<td.*>.*<a href=.*>.*</a>.*</td>.*<td.*>.*<a href=.*>.*</a>.*<br/>.*</td>.*<td.*>.*</td>.*", Pattern.DOTALL);
 	Pattern builderCellPattern = Pattern.compile("</td>");
 	String[] buildersList = rootPattern.split(htmlContent);
 	String builderString = null;
 	Builder currentBuilder = null;
 	int so, eo;
 
 	// For each builder, update its information in the map
 	for (String s : buildersList) {
 
 	    // Sanity check
 	    Matcher m = builderPattern.matcher(s);
 	    if (!m.matches())
 		continue;
 
 	    String[] builderCellsList = builderCellPattern.split(s);
 
 	    // Name
 	    builderString = builderCellsList[0];
 	    so = builderString.lastIndexOf("\">") + "\">".length();
 	    eo = builderString.indexOf("</a>");
 	    String name = builderString.substring(so, eo);
 
 	    if (!this.isBuildBotSupported(name))
 		continue;
 
 	    // Create new builder and add it to the hash table
 	    currentBuilder = new Builder(name);
 	    this.builders.put(name, currentBuilder);
 
 	    // Builder path
 	    so = builderString.indexOf("<a href=\"") + "<a href=\"".length();
 	    eo = builderString.lastIndexOf("\">");
 	    currentBuilder.setPath(builderString.substring(so, eo));
 
 	    // Build result
 	    builderString = builderCellsList[1];
 	    if (builderString.contains("success"))
 		currentBuilder.setBuildResult(BuildResult.PASSED);
 	    else
 		currentBuilder.setBuildResult(BuildResult.FAILED);
 
 	    // Build number
 	    so = builderString.indexOf("<a href=\"") + "<a href=\"".length();
 	    eo = builderString.lastIndexOf("\">");
 	    String buildPath = builderString.substring(so, eo);
 	    String buildNumber = buildPath.substring(buildPath.lastIndexOf("/") + 1);
 	    currentBuilder.setBuildNumber(buildNumber);
 
 	    // SVN revision number
 	    so = eo + "\">".length();
 	    eo = builderString.indexOf("</a>");
 	    currentBuilder.setRevisionFromString(builderString.substring(so, eo));
 
 	    // Summary
 	    so = builderString.indexOf("<br/>") + "<br/>".length();
 	    currentBuilder.setSummary(builderString.substring(so));
 
 	    // Current status
 	    builderString = builderCellsList[2];
 	    so = builderString.indexOf("\">") + "\">".length();
 	    currentBuilder.setStatus(builderString.substring(so));
 	}
     }
 
     private boolean isBuildBotSupported(String name) {
 
 	// WebKit2 builders are not core buildbots yet
 	if (name.contains("WebKit2"))
 	    return false;
 
 	// Check the name against the supported regular expressions
 	for (String s : this.supportedRegexps) {
 	    if (Pattern.matches(s, name))
 		return true;
 	}
 	return false;
     }
 }
