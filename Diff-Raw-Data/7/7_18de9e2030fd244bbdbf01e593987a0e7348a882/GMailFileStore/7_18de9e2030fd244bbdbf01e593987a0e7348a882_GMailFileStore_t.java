 /*
 
 Copyright (c) 2004, Garrick Toubassi
 
 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the "Software"),
 to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense,
 and/or sell copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 
 */
 
 /*
  * Created on Jul 22, 2004
  */
 package com.toubassi.filebunker.vault;
 
 import com.meterware.httpunit.HttpUnitOptions;
 import com.meterware.httpunit.PostMethodWebRequest;
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebResponse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Functionality implemented by this subclass is fully thread safe
  * as there are no instance variables added.
  * @author garrick
  */
 public class GMailFileStore extends WebMailFileStore
 {
     public static final String UserAuthenticationNotification = "UserAuthenticationNotification";
     public static final String ImageUrlKey = "ImageUrlKey";
     public static final String ImageTextKey = "ImageTextKey";
     
 	private static final Pattern messageIdPattern = Pattern.compile("\\[\\s*\"([^\"]+)\"");
 	private static final Pattern utilizationPattern = Pattern.compile("D\\(\\[\"qu\",\"(\\d+) MB\",\"(\\d+) MB");
 	
 	protected TimeOutWebConversation createLoggedInConversation() throws VaultException
 	{	    	    
 		try {
 			HttpUnitOptions.setScriptingEnabled(false);
 			
 			// For now we assume a time out of 10 minutes.
 			TimeOutWebConversation wc = new TimeOutWebConversation(10*60);
 	
 			// For debugging with Charles.exe
 			// wc.setEnableProxy("localhost", 8888);
 			
 			WebResponse wr = wc.getResponse("http://gmail.google.com/");
 			
 			String[] frameNames = wc.getFrameNames();
 			WebForm loginForm = null;
 			for (int i = 0; i < frameNames.length; i++) {
 				wr = wc.getFrameContents(frameNames[i]);
 				WebForm[] forms = wr.getForms();
 				for (int j = 0; j < forms.length; j++) {
 				    if (forms[j].hasParameterNamed("Email") && forms[j].hasParameterNamed("Passwd")) {
 				        loginForm = forms[j];
 				        break;
 				    }
 				}
 			}
 			
 			if (loginForm == null) {
 			    throw new FailedLoginException("Could not find the login form");
 			}
 			
 			loginForm.setParameter("Email", username());
 			loginForm.setParameter("Passwd", accountPassword());
 			wr = loginForm.submit();
 			
 			// gtoubassi 4/1/05
 			// We want to just do wc.getResponse(wr.getRefreshRequest()), but
 			// httpunit has a bug whereby it mishandles refresh urls with query params
 			// (sometimes).  See httpunit bug 1175246 or:
 			// http://sourceforge.net/tracker/index.php?func=detail&aid=1175246&group_id=6550&atid=106550
 			
 			String refreshHeaders[] = wr.getMetaTagContent("http-equiv", "refresh");//wr.getHeaderField("Refresh");
 			if (refreshHeaders != null && refreshHeaders.length > 0) {
 			    String refreshHeader = refreshHeaders[0];
 			    int firstEquals = refreshHeader.indexOf("=");
 			    if (firstEquals != -1) {
 			        String url = refreshHeader.substring(firstEquals + 1);
 			        
 			        if (url.length() > 0) {
 			            wr = wc.getResponse(url);
 			        }
 			    }
 			}
 
 			/*
 			
 			This was for back when they would randomly throw up a captcha.
 			They may still do it, but since the login sequence changed, I don't
 			know where it comes up and I can't repro (see the captcha comment in FileBunker.java)
 			 
 			while (link == null) {
 			    
 			    // We are probably being asked to provide characters in an image,
 			    // but lets make sure.  We should still see the loginForm
 
 			    URL imageUrl = null;
 				WebImage[] images = wr.getImages();
 				for (int i = 0; i < images.length; i++) {
 				    WebImage image = images[i];
 				    String source = image.getSource();
 				    if (source.indexOf("Captcha") != -1) {
 				        String base = wr.getURL().toExternalForm();
 				        int lastSlash = base.lastIndexOf('/');
 				        if (lastSlash != -1) {
 				            base = base.substring(0, lastSlash + 1);
 				        }
 				        imageUrl = new URL(base + source);
 				        break;
 				    }
 				}
 				
 				String imageText = null;
 				if (imageUrl != null) {
 			        HashMap map = new HashMap();
 			        map.put(ImageUrlKey, imageUrl);
 			        
 			        NotificationCenter.sharedCenter().post(UserAuthenticationNotification, this, map);
 
 			        imageText = (String)map.get(ImageTextKey);
 				}
 
 				if (imageText == null) {
 				    throw new FailedLoginException("Could not pass GMail's user authentication (image text) for " + email(), this);
 		        }
 
 				loginForm = wr.getForms()[0];
 				loginForm.setParameter("Email", username());
 				loginForm.setParameter("Passwd", accountPassword());
 				loginForm.setParameter("captcha", imageText);
 				wr = loginForm.submit();
 				link = wr.getLinkWith("click here to continue");
 			}
 			
 			wr = link.click();
 			
 			WebRequest request = wr.getRefreshRequest();
 		    wc.setAutoloadSubframes(false);
 			try {
 			    wr = wc.getResponse(request);
 			}
 		    finally {
 				wc.setAutoloadSubframes(true);		        
 		    }
 		    
 			*/
 
 			if (wr.getResponseCode() != 200) {
 			    throw new FailedLoginException("Failed to connect to gmail account " + email(), this);
 			}
 			return wc;
 		}
 		catch (VaultException e) {
 			throw e;
 		}
 		catch (Exception e) {
 			throw new FailedLoginException("Failed to connect to gmail account " + email(), e, this);
 		}
 	}
 	
     protected void disposeConversation(TimeOutWebConversation wc) throws VaultException
     {
         try {
             WebResponse wr = wc.getResponse("https://gmail.google.com/gmail?logout");
         }
         catch (Exception e) {
             throw new VaultException(e);
         }
     }
 
 	
 	public InputStream restoreFile(RevisionIdentifier identifier, Date date) throws VaultException
 	{
 	    TimeOutWebConversation wc = checkOutConversation();
 	    InputStream input = null;
 	    
 	    try
 		{
 
 		    HashMap messageIdToGroups = search(wc, identifier.guid(), backupFileSubjectPattern());
 
 		    if (messageIdToGroups.isEmpty()) {
 		        return null;
 		    }
 		    
 			String[] messageIds = new String[messageIdToGroups.size()];
 			Iterator i = messageIdToGroups.entrySet().iterator();
 			while (i.hasNext()) {
 			    Map.Entry entry = (Map.Entry)i.next();
 			    String messageId = (String)entry.getKey();
 			    String[] groups = (String[])entry.getValue();
 			    int part = Integer.parseInt(groups[1]);
 			    if (part >= messageIds.length) {
 					throw new VaultException("Found inconsistent part #s and gmail search results for " + identifier);			        
 			    }
 			    messageIds[part] = messageId;
 			}
 			
 			// The GMailMultiPartInputStream will check the conversation back in
 			input = new GMailMultiPartInputStream(this, wc, messageIds);
 			return FileStoreUtil.restoreInputStream(input, vaultConfig.passwordForDate(date));
 		}
 		catch (Exception e) {
 		    // If an exception occurred, we may need to check in the web conversation
 		    // ourselves.  If we have an input stream, then closing it will check it in.
 		    // If not do it ourselves.
 		    
 		    if (input != null) {
 		        try {
 		            input.close();
 		        }
 		        catch (IOException ioException) {
 		            //swallow
 		        }
 		    }
 		    else {
 		        checkInConversation(wc);
 		    }
 		    
 		    if (e instanceof VaultException) {
 		        throw (VaultException)e;
 		    }
 		    
 			throw new VaultException("Failed to restore file " + identifier, e);
 		}
 	}
 	
 	protected boolean deleteFile(RevisionIdentifier identifier) throws VaultException
 	{
 	    TimeOutWebConversation wc = checkOutConversation();
 	    boolean didDelete = false;
 
 	    try {
 		    HashMap messageIdToGroups = search(wc, identifier.guid(), backupFileSubjectPattern());
 		    
 		    if (!messageIdToGroups.isEmpty()) {
 	
 		        String[] messageIds = (String[])messageIdToGroups.keySet().toArray(new String[messageIdToGroups.size()]);
 	
 			    // tr: move to trash
 				performAction(wc, messageIds, "http://gmail.google.com/gmail?search=inbox&view=tl&start=0", "tr");
 	
 				// dl: delete forever
 				performAction(wc, messageIds, "http://gmail.google.com/gmail?search=trash&view=tl&start=0", "dl");
 				didDelete = true;
 		    }
 	    }
 	    finally {
 		    checkInConversation(wc);	        
 	    }
 	    
 	    return didDelete;
 	}
 
     protected synchronized long computeAvailableBytes() throws VaultException
     {
 	    TimeOutWebConversation wc = checkOutConversation();
 
 	    try
 		{	
 			WebResponse wr = wc.getResponse("http://gmail.google.com/gmail?search=inbox&view=tl&start=0&init=1");
 			String text = wr.getText();
 
 			Matcher matcher = utilizationPattern.matcher(text);
 			
 			if (!matcher.find()) {
 				throw new VaultException("Failed to match utilization pattern.");
 			}
 			
			long usedMB = Long.parseLong(matcher.group(1));
			long totalMB = Long.parseLong(matcher.group(2));
 			if (usedMB > .8 * totalMB) {
 			    // 10MB slop factor to make sure we don't go overfill.
 		        usedMB += 10;
 			}
 			if (usedMB >= totalMB) {
 			    return 0;
 			}
			return (totalMB - usedMB)*1024*1024;
 		}
 		catch (VaultException e) {
 		    throw e;
 		}
 		catch (Exception e) {
 		    throw new VaultException("Could not determine the amount of available space", e);
 		}
 		finally {
 			checkInConversation(wc);		    
 		}
     }
 
     protected void cleanupInbox() throws VaultException
 	{
         TimeOutWebConversation wc = checkOutConversation();
         
         try {
             
             while (true) {
 
 			    HashMap messageIdToGroups = search(wc, "in:inbox FileBunker:", backupFileSubjectPattern());
 			    
 			    if (messageIdToGroups.isEmpty()) {
 			        break;
 			    }
 	
 		        String[] messageIds = (String[])messageIdToGroups.keySet().toArray(new String[messageIdToGroups.size()]);
 	
 				performAction(wc, messageIds, "http://gmail.google.com/gmail?search=inbox&view=tl&start=0", "rc_^i");
             }
         }
         finally {
     	    checkInConversation(wc);            
         }
 	}
 
 	protected int maximumMessageSize()
 	{
 	    // 10Mb limit, minus the base64 encoding overhead (~8/6 expansion),
 	    // minus some additional slop.
 	    return 7000000;
 	}
 	
 	private HashMap search(WebConversation wc, String searchString, Pattern searchResultPattern) throws VaultException
 	{
 		try
 		{
 	        String encodedSearchString = URLEncoder.encode(searchString, "UTF-8");
 
 		    HashMap searchResults = new HashMap();
 		    
 			while (true) {
 			    int numResults = searchResults.size();
 				String queryUrl = "http://gmail.google.com/gmail?search=query&q=" + encodedSearchString + "&view=tl&start=" + numResults;
 				
 				WebResponse wr = wc.getResponse(queryUrl);
 	
 				String resultsText = wr.getText();
 				
 				Matcher matcher = searchResultPattern.matcher(resultsText);
 				
 				while (matcher.find()) {
 				    int subjectIndex = matcher.start();
 				    int leftBracket = findNearestUnquotedChar(resultsText, '[', subjectIndex - 1);
 				    Matcher messageIdMatcher = messageIdPattern.matcher(resultsText);
 				    if (!messageIdMatcher.find(leftBracket)) {
 				        throw new VaultException("Could not extract message id from search results of " + searchString);
 				    }
 
 				    // Extract the groups from the searchResultPattern
 				    String[] groups = new String[matcher.groupCount() + 1];
 				    for (int i = 0; i < groups.length; i++) {
 				        groups[i] = matcher.group(i);
 				    }
 				    
 				    // This maps the messageId to the groups found in the searchResultPattern
 				    searchResults.put(messageIdMatcher.group(1), groups);
 				}
 				
 				// If we didn't find any more parts in this pass, then we must be done
 				if (numResults == searchResults.size()) {
 				    break;
 				}
 			}
 			
 			return searchResults;
 		}
 		catch (VaultException e) {
 			throw e;
 		}
 		catch (Exception e) {
 			throw new VaultException("Failed to perform search for " + searchString, e);
 		}
 	}
 
 	private void performAction(WebConversation wc, String[] messageIds, String url, String action) throws VaultException
 	{
 	    try {
 			PostMethodWebRequest request = new PostMethodWebRequest(url);
 			request.setParameter("act", action);
 			request.setParameter("at", wc.getCookieValue("GMAIL_AT"));
 			request.setParameter("vp", "");
 			request.setParameter("t", messageIds);
 			wc.getResponse(request);    	        
 	    }
 	    catch (Exception e) {
 	        throw new VaultException("Performing action " + action, e);
 	    }
 	}
 
 	private int findNearestUnquotedChar(String searchResults, char ch, int start)
 	{
 	    boolean inString = false;
 	    
 	    while (inString || searchResults.charAt(start) != ch) {
 	        if (searchResults.charAt(start) == '"') {
 	            inString = !inString;
 	        }
 	        start--;
 	    }
 	    return start;
 	}
 }
