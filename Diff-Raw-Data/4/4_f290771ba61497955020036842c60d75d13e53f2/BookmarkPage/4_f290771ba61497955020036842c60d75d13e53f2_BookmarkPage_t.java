 /**********************************************************************************
  *
  * $URL: https://source.sakaiproject.org/contrib/etudes/melete/trunk/melete-app/src/java/org/etudes/tool/melete/BookmarkPage.java $
  * $Id: BookmarkPage.java 56408 2008-12-19 21:16:52Z mallika@etudes.org $
  ***********************************************************************************
  * Copyright (c) 2010 Etudes, Inc.
  *
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  **********************************************************************************/
 
 package org.etudes.tool.melete;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.component.app.melete.*;
 import org.etudes.api.app.melete.*;
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.application.Application;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.html.*;
 import javax.faces.component.*;
 
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.io.*;
 
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.event.ActionEvent;
 
 import javax.faces.context.ExternalContext;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 
 import org.etudes.api.app.melete.BookmarkService;
 import org.etudes.api.app.melete.BookmarkObjService;
 
 public class BookmarkPage implements Serializable
 {
 
 	/** identifier field */
 	
 	protected MeleteSiteAndUserInfo meleteSiteAndUserInfo;
 
 	private BookmarkObjService bookmark;
 
 	private BookmarkService bookmarkService;
 
 	protected SectionService sectionService;
 
 	private String sectionId;
 
 	private String sectionTitle;
 
 	private List bmList;
 
 	private boolean instRole;
 
 	private int deleteBookmarkId;
 
 	private String deleteBookmarkTitle;
 
 	private boolean nobmsFlag;
 
 	/** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(BookmarkPage.class);
 
 	public BookmarkPage()
 	{
 
 	}
 
 
 	public String addBookmark()
 	{
 		  FacesContext context = FacesContext.getCurrentInstance();
 	  Map sessionMap = context.getExternalContext().getSessionMap();
 	  ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 	  if (bookmarkService == null)
 	    bookmarkService = getBookmarkService();
 
 	  /*if (bookmark == null)
 	    bookmark = new Bookmark();*/
 
 	    this.bookmark.setSiteId((String)sessionMap.get("courseId"));
 	    this.bookmark.setUserId((String)sessionMap.get("userId"));
 	    this.bookmark.setSectionId(Integer.parseInt(this.sectionId));
 	    try
 	    {
 	      bookmarkService.insertBookmark(this.bookmark);
 	    }catch(Exception ex)
 		{
 	    	String errMsg = bundle.getString(ex.getMessage());
 	    	context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage(),errMsg));
 			return "failure";
 		}
	    resetValues();
 		return "confirm_bookmark";
 
 	}
 
 	   public void viewSection(ActionEvent evt)
 		{
 			FacesContext ctx = FacesContext.getCurrentInstance();
 			UIViewRoot root = ctx.getViewRoot();
 
 			UICommand cmdLink = (UICommand)evt.getComponent();
 
 	      	List cList = cmdLink.getChildren();
 	      	if(cList == null || cList.size() <1) return;
 	    	UIParameter param1 = (UIParameter) cList.get(0);
 			ValueBinding binding = Util.getBinding("#{viewSectionsPage}");
 
 			ViewSectionsPage vsPage = (ViewSectionsPage) binding.getValue(ctx);
 
 			vsPage.resetValues();
 			vsPage.setSectionId(((Integer)param1.getValue()).intValue());
 			Section sec = (Section)sectionService.getSection(((Integer)param1.getValue()).intValue());
 			vsPage.setModuleId(sec.getModuleId());
 			vsPage.setModuleSeqNo(sec.getModule().getCoursemodule().getSeqNo());
 			vsPage.setSection(sec);
 			vsPage.setModule(null);
 
 		}
 
 	public String redirectViewSection()
 	{
 		return "view_section";
 	}
 	  public void deleteAction(ActionEvent evt)
 		{
 			UICommand cmdLink = (UICommand)evt.getComponent();
 
 	      	List cList = cmdLink.getChildren();
 	      	if(cList == null || cList.size() <2) return;
 	    	UIParameter param1 = (UIParameter) cList.get(0);
 	    	UIParameter param2 = (UIParameter) cList.get(1);
 
 		    setDeleteBookmarkId(((Integer)param1.getValue()).intValue());
 			setDeleteBookmarkTitle((String)param2.getValue());
 			return;
 		}
 
 	  public String redirectDeleteLink()
 		{
 			 return "delete_bookmark";
 		}
 
 
 	public String deleteBookmark()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		 ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		try
 	    {
 	      bookmarkService.deleteBookmark(this.deleteBookmarkId);
 	    }catch(Exception ex)
 		{
 	    	String errMsg = bundle.getString(ex.getMessage());
 	    	context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage(),errMsg));
 			return "failure";
 		}
 	    resetValues();
 	    return "list_bookmarks";
 	}
 
	public String cancelDeleteBookmark()
   	{
   		return "list_bookmarks";
   	}
 
 
 	public void resetValues()
 	{
       deleteBookmarkId = 0;
       deleteBookmarkTitle = null;
       bmList = null;
       nobmsFlag = true;
 	}
 	
 	public void exportNotes(ActionEvent evt)
 	{
 		String packagingdirpath = ServerConfigurationService.getString("melete.packagingDir", "");
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		
 		File packagedir = null;
 		ResourceLoader bundle = new ResourceLoader(
 		"org.etudes.tool.melete.bundle.Messages");
 		try {
 			if(packagingdirpath == null || packagingdirpath.length() <=0 )
 			{
 				logger.warn("Melete Packaging Dir property is not set. Please check melete's readme file. ");
 				return;
 			}
 			File basePackDir = new File(packagingdirpath);
 			if (!basePackDir.exists())
 				basePackDir.mkdirs();
 
 			String title = getMeleteSiteAndUserInfo().getCourseTitle();
 			title = title.trim();
 			
 			String courseId = (String)sessionMap.get("courseId");
 			String userId = (String)sessionMap.get("userId");
 			
 			packagedir = new File(basePackDir.getAbsolutePath()
 					+ File.separator + courseId + "_" + userId + File.separator + title.replace(' ', '_'));
 			
 			if (!packagedir.exists())
 				packagedir.mkdirs();
 
 			String outputfilename = packagedir.getParentFile()
 			.getAbsolutePath() + File.separator  + title.replace(' ', '_') + "_my_bookmarks_notes.txt";
 	
 			bookmarkService.createFile(bmList, outputfilename);
 			File outfile = new File(outputfilename);
 			
 			download(new File(outfile.getAbsolutePath()));
 
 			FacesContext facesContext = FacesContext
 			.getCurrentInstance();
 			facesContext.responseComplete();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			String errMsg = bundle.getString("list_bookmarks_export_error");
 			FacesMessage msg = new FacesMessage(null, errMsg);
 			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			context.addMessage(null, msg);
 		} finally {
 			// delete the files - Directory courseid_instructorid and
 			// it's child
 			if (packagedir != null && packagedir.exists())
 				bookmarkService.deleteFiles(packagedir
 						.getParentFile());
 
 		}
 
 	}
 	
 	public String redirectExportNotes()
 	{
 		return "list_bookmarks";
 	}
 	
 	
 	/**
 	 * writes the text file to browser
 	 *
 	 * @param file -
 	 *            text file to download
 	 * @throws Exception
 	 */
 	private void download(File file) throws Exception {
 		FileInputStream fis = null;
 		ServletOutputStream out = null;
 		try {
 			String disposition = "attachment; filename=\"" + file.getName()
 					+ "\"";
 			fis = new FileInputStream(file);
 
 			FacesContext cxt = FacesContext.getCurrentInstance();
 			ExternalContext context = cxt.getExternalContext();
 			HttpServletResponse response = (HttpServletResponse) context
 					.getResponse();
 			response.setContentType("application/text"); // application/text
 			response.addHeader("Content-Disposition", disposition);
 			// Contributed by Diego for ME-233
 			response.setHeader("Pragma", "public");
 			response.setHeader("Cache-Control",
 					"public, post-check=0, must-revalidate, pre-check=0");
 
 			out = response.getOutputStream();
 
 			int len;
 			byte buf[] = new byte[102400];
 			while ((len = fis.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 
 			out.flush();
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			try {
 				if (out != null)
 					out.close();
 			} catch (IOException e1) {
 			}
 
 			try {
 				if (fis != null)
 					fis.close();
 			} catch (IOException e2) {
 			}
 		}
 	}	
 
 	public int getDeleteBookmarkId() {
 	      return deleteBookmarkId;
 	   }
 
     public void setDeleteBookmarkId(int deleteBookmarkId) {
 	     this.deleteBookmarkId = deleteBookmarkId;
 	 }
 
 	public String getDeleteBookmarkTitle() {
 		  return deleteBookmarkTitle;
 	}
 
     public void setDeleteBookmarkTitle(String deleteBookmarkTitle) {
 		     this.deleteBookmarkTitle = deleteBookmarkTitle;
 	}
 
 	public boolean getInstRole()
     {
     	FacesContext context = FacesContext.getCurrentInstance();
 	  	Map sessionMap = context.getExternalContext().getSessionMap();
 	  	if ((String)sessionMap.get("role") !=null)
 	  		this.instRole = ((String)sessionMap.get("role")).equals("INSTRUCTOR");
 	  	else this.instRole = false;
     	return instRole;
     }
 
 	public boolean getNobmsFlag()
 	{
 		getBmList();
 		return this.nobmsFlag;
 	}
 
 	public void setNobmsFlag(boolean nobmsFlag)
 	{
 		this.nobmsFlag = nobmsFlag;
 	}
 
     public BookmarkObjService getBookmark() {
  	  FacesContext context = FacesContext.getCurrentInstance();
 	  Map sessionMap = context.getExternalContext().getSessionMap();
 	  if (bookmark == null)
 	  {
 		  bookmark = bookmarkService.getBookmark((String)sessionMap.get("userId"),(String)sessionMap.get("courseId"),Integer.parseInt(this.sectionId));
           if (bookmark == null)
           {
         	  bookmark = new Bookmark();
         	  bookmark.setTitle(getSectionTitle());
           }
 	  }
 	return bookmark;
     }
 
    public void setBookmark(BookmarkObjService bookmark) {
     this.bookmark = bookmark;
   }
 
    public List getBmList()
    {
 	   FacesContext context = FacesContext.getCurrentInstance();
 	   Map sessionMap = context.getExternalContext().getSessionMap();
 	   if (bmList == null)
 	   {	   
 	     bmList = bookmarkService.getBookmarks((String)sessionMap.get("userId"),(String)sessionMap.get("courseId"));
 	     if ((bmList != null)&&(bmList.size() > 0))
 	     {
 		   setNobmsFlag(false);
 	     }
 	     else
 	     {
 		   setNobmsFlag(true);
 	     }
 	   }  
 	   return bmList;
    }
 
    public void setBmList(List bmList)
    {
 	   this.bmList = bmList;
    }
 
   public String getSectionId() {
       return sectionId;
    }
 
    public void setSectionId(String sectionId) {
      this.sectionId = sectionId;
    }
 
    public String getSectionTitle() {
 	      return sectionTitle;
    }
 
    public void setSectionTitle(String sectionTitle) {
 	     this.sectionTitle = sectionTitle;
    }
 	/**
 	 * @return Returns the BookmarkService.
 	 */
 	public BookmarkService getBookmarkService()
 	{
 		return bookmarkService;
 	}
 
 	/**
 	 * @param bookmarkService The bookmarkService to set.
 	 */
 	public void setBookmarkService(BookmarkService bookmarkService)
 	{
 		this.bookmarkService = bookmarkService;
 	}
 
 	/**
      * @return Returns the SectionService.
      */
     public SectionService getSectionService() {
             return sectionService;
     }
 
     /**
      * @param SectionService The SectionService to set.
      */
     public void setSectionService(SectionService sectionService) {
             this.sectionService = sectionService;
     }
     
     /**
 	 * get MeleteSiteAndUserInfo
 	 *
 	 * @return
 	 */
 	private MeleteSiteAndUserInfo getMeleteSiteAndUserInfo() {
 		if (meleteSiteAndUserInfo == null) {
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			meleteSiteAndUserInfo = (MeleteSiteAndUserInfo) binding
 					.getValue(context);
 
 			return meleteSiteAndUserInfo;
 		} else
 			return meleteSiteAndUserInfo;
 	}
 
 
 }
