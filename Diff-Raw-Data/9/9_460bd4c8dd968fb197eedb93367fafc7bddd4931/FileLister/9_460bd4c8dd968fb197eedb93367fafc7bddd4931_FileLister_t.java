 // FileLister: Class file for WO Component 'FileLister'
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.util.Calendar;
 import java.util.logging.Logger;
 
 import com.webobjects.appserver.WOActionResults;
 import com.webobjects.appserver.WOApplication;
 import com.webobjects.appserver.WOContext;
 import com.webobjects.appserver.WOComponent;
 import com.webobjects.appserver.WOElement;
 import com.webobjects.appserver.WORequestHandler;
 import com.webobjects.appserver.WOResponse;
 import com.webobjects.foundation.NSArray;
 
 public class FileLister extends WOComponent {
 	public File file;
 	public File item;
 //	public String target;
 	protected boolean justNavigate = false;
 //	public boolean noDelete;
 //	public boolean noAction;
 	public NamedFlags access;
 	
 	static {
 		WOApplication app = WOApplication.application();
 		WORequestHandler rh = app.requestHandlerForKey(FileRequestHandler.handlerKey);
 		if(rh == null)
 			app.registerRequestHandler(new FileRequestHandler(), FileRequestHandler.handlerKey);
 	}
 	
     public FileLister(WOContext context) {
         super(context);
     }
     
     public WOElement template() {
     	if(parent() != null) {
     		if(file == null)
     			file = (File)valueForBinding("file");
     		if(file == null) {
     			String filename = (String)valueForBinding("filePath");
     			if(filename != null)
     				file = new File(filename);
     		}
     		setJustNavigate(valueForBinding("justNavigate"));
     		access = (NamedFlags)valueForBinding("access");
     		if(access == null)
     			access = DegenerateFlags.ALL_TRUE;
     	} else {
     		access = DegenerateFlags.ALL_FALSE;
     	}
     	return super.template();
     }
     
     public NSArray files() {
     	File[] list = file.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				char c = name.charAt(0);
 				return (c != '.' && c != '_');
 			}
 		});
     	return new NSArray(list);
     }
     
     public void setFilePath(String filePath) {
     	file = new File(filePath);
     }
     
     public void setJustNavigate(Object value) {
     	justNavigate = Various.boolForObject(value);
     }
     
     public String href() {
 		if(standalone()) {
 			StringBuilder buf = new StringBuilder();
 			String path = context().request().requestHandlerPath();
 			int idx = path.lastIndexOf('/');
 			if(idx < path.length() -1) {
 				buf.append(path.substring(idx +1)).append('/');
 			}
 			buf.append(item.getName());
 			if(item.isDirectory() && !justNavigate)
 				buf.append('/');
 			return buf.toString();
 		} else if(justNavigate) {
 			return context().componentActionURL();
     	} else {
     		return FileRequestHandler.fileActionURLForFile(item, session(), null, null);
     	}
     }
     
     public boolean disableLink() {
     	if(standalone())
     		return false;
     	if(item.isDirectory())
     		return false;
     	return (justNavigate || !item.getName().endsWith(".zip"));
     }
     
     public WOActionResults open() {
 //    	file = item;
     	setValueForBinding(item, "file");
     	return null;
     }
     
     public String actionHover() {
     	if(!access.flagForKey("edit"))
     		return null;
     	if(item.isDirectory())
     		return (String)session().valueForKeyPath(
     				"strings.Reusables_Strings.uiElements.Archive");
     	else
     		return (String)session().valueForKeyPath(
 					"strings.Reusables_Strings.uiElements.Save");
     }    
     
     public String actionTitle() {
     	if(item.isDirectory()) {
     		if(standalone())
     			return null;
     		return "zip";
     	}
     	long size = item.length();
     	char[] letter = new char[] {'b','K','M','G','T'};
     	StringBuilder buf = new StringBuilder();
     	int extra = 0;
     	for (int i = 0; i < letter.length; i++) {
 			if(size < 1024) {
 				if(size < 64) {
 					buf.append(size);
 					if(extra >= 100) {
 						buf.append('.');
 						buf.append(extra / 100);
 					}
 				} else {
 					if(extra > 512)
 						size++;
 					buf.append(size);
 				}
 				buf.append(' ').append(letter[i]);
 				break;
 			}
 			extra = (int)(size % 1024);
 			size = size >> 10;
 		}
     	if(buf.length() == 0)
     		buf.append("&infin;");
     	return buf.toString();
     }
     
 	public WOActionResults perform() {
 		if(item.isDirectory()) {
 			StringBuilder buf = new StringBuilder(item.getName());
 			buf.append('z');
 			Calendar cal = Calendar.getInstance();
 			buf.append(cal.get(Calendar.YEAR));
 			int idx = cal.get(Calendar.MONTH);
 			if(idx < 10)
 				buf.append('0');
 			buf.append(idx);
 			idx = cal.get(Calendar.DAY_OF_MONTH);
 			if(idx < 10)
 				buf.append('0');
 			buf.append(idx);
 			buf.append(".zip");
 			File zip = new File(file,buf.toString());
 			Logger.getLogger("rujel.reusables").log(WOLogLevel.INFO,
 					"Creating ZIP file: " + item + " -> " + zip,session());
 			if(file.exists())
 				file.delete();
 			Thread thread = new Thread(new FileWriterUtil.Zipper(item, zip),"Zipper");
 			thread.setPriority(Thread.MIN_PRIORITY + 1);
 			thread.start();
 		} else {
 			try {
 				WOResponse response = application().createResponseInContext(context()); 
 				String conType = application().resourceManager().
 						contentTypeForResourceNamed(item.getName());
 				response.setHeader(conType,"Content-Type");
 				FileInputStream fis = new FileInputStream(item);
 				response.setContentStream(fis,4096,item.length());
 				StringBuilder buf = new StringBuilder("attachment; filename=\"");
 				buf.append(item.getName()).append('"');
 				response.setHeader(buf.toString(),"Content-Disposition");
 				response.disableClientCaching();
 				return response;
 			} catch (Exception e) {
 				session().takeValueForKey(e.getMessage(), "message");
 				Logger.getLogger("rujel.reusables").log(WOLogLevel.WARNING,
 						"Error reading file " + item, new Object[] {session(),e});
 			}
 		}
 		return null;
 	}
 	
 	public String onDelClick() {
 		StringBuilder buf = new StringBuilder("if(confirm('");
 		buf.append(session().valueForKeyPath("strings.Reusables_Strings.uiElements.Delete"));
 		buf.append(' ').append(item.getName()).append("?'))");
 		String onclick = (String)valueForBinding("onClick");
 		if(onclick == null)
 			buf.append("window.location='").append(context().componentActionURL()).append("';");
 		else
 			buf.append(onclick);
 		return buf.toString();
 	}
 	
 	public String actionOnClick() {
		if(!item.isDirectory()) {
 			String loadTarget = (String)valueForBinding("loadTarget");
 			if(loadTarget != null) {
 				StringBuilder buf = new StringBuilder("window.open('");
 				buf.append(context().componentActionURL()).append("','");
 				buf.append(loadTarget).append("');");
				return buf.toString();
 			}
 		}
		return (String)valueForBinding("onClick");
 	}
 	
 	public WOActionResults delete() {
 		Logger.getLogger("rujel.reusables").log(WOLogLevel.INFO,
 				"Deleting file " + item, session());
 		dirDelete(item);
 		return null;
 	}
     
 	public static boolean dirDelete(File file) {
 		if(file.isDirectory()) {
 			File[] dir = file.listFiles();
 			for (int i = 0; i < dir.length; i++) {
 				if(!dirDelete(dir[i]))
 					break;
 			}
 		}
 		return file.delete();
 	}
 	
     public boolean standalone() {
     	return context().page() == this;
     }
     
 	public boolean isStateless() {
 		return true;
 	}
 	
 	public boolean synchronizesVariablesWithBindings() {
         return false;
 	}
 		
 	public void reset() {
 		super.reset();
 //		noAction = Various.boolForObject(valueForBinding("noAction"));
 //		noDelete = Various.boolForObject(valueForBinding("noDelete"));
 		file = null;
 		item = null;
 		justNavigate = false;
 //		target = null;
 	}
 }
