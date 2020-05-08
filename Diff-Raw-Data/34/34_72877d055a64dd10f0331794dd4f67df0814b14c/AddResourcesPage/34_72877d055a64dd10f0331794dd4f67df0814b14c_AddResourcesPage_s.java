 
 /*
 * Copyright (c) 2004, 2005, 2006, 2007 Foothill College, ETUDES Project
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
  * Created on Oct 10, 2006  @author Rashmi
  *
  * Captures author preferences
  *
  *
  */
 package org.sakaiproject.tool.melete;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Set;
 import java.util.List;
 import java.util.Iterator;
 import java.io.File;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import javax.faces.component.*;
 
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 import javax.faces.event.*;
 import javax.faces.el.ValueBinding;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.sakaiproject.api.app.melete.MeleteCHService;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 
 public class AddResourcesPage {
   private String fileType;
   private String numberItems;
   private int maxUploadSize;
   private int removeLinkIndex;
   private List utList;
   protected MeleteCHService meleteCHService;
   private UIData table;
   private ArrayList<String> err_fields = null;
   private ArrayList<String> success_fields = null;
   /** Dependency:  The logging service. */
   protected Log logger = LogFactory.getLog(AddResourcesPage.class);
 
   public AddResourcesPage()
   {
   }
 
   public String getNumberItems()
   {
 	return this.numberItems;
   }
 
   public void setNumberItems(String numberItems)
   {
 	this.numberItems = numberItems;
   }
 
   public int getMaxUploadSize()
   {
         /*
          * get from session
          */
 	  	  if(maxUploadSize == 0)
 	  	  {
           FacesContext context = FacesContext.getCurrentInstance();
           Map sessionMap = context.getExternalContext().getSessionMap();
           maxUploadSize = 2;
           if((String)sessionMap.get("maxSize") != null)
         	  maxUploadSize = Integer.parseInt((String)sessionMap.get("maxSize"));
           if (logger.isDebugEnabled()) logger.debug("Size is "+maxUploadSize);
 	  	  }
         return maxUploadSize;
   }
 
   public void resetValues()
   {
 	  this.utList = null;
 	  this.numberItems = null;
 	  this.fileType = null;
 	  this.maxUploadSize = 0;
 	  err_fields = null;
 	  success_fields = null;
   }
   public void cancelResetValues()
   {
 	  this.utList = null;
 	  this.numberItems = "1";
   }
   public String addItems()
   {
 	  byte[] secContentData;
 	  String secResourceName;
 	  String secContentMimeType;
 
 	  FacesContext context = FacesContext.getCurrentInstance();
 	  ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 	  String addCollId = getMeleteCHService().getUploadCollectionId();
 
 	  //Code that validates required fields
 	  int emptyCounter = 0;
 	  String linkValue, titleValue;
 	  boolean emptyLinkFlag = false;
 	  boolean emptyTitleFlag = false;

 	  if (this.fileType.equals("upload"))
 	  {
 	    for (int i=1; i<=10; i++)
 	    {
 			org.apache.commons.fileupload.FileItem fi = (org.apache.commons.fileupload.FileItem) context.getExternalContext().getRequestMap().get("file"+i);
 			if(fi == null || fi.getName() == null || fi.getName().length() ==0)
             {
 		    emptyCounter = emptyCounter + 1;
 		    }
 		 }
 
 		 if (emptyCounter == 10) return "manage_content";
  	  /* try
 	    {
 			if (emptyCounter == 10) throw new MeleteException("all_uploads_empty");
 		}
 	    catch (MeleteException mex)
         {
 		  String errMsg = bundle.getString(mex.getMessage());
 	      context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,mex.getMessage(),errMsg));
 		  return "failure";
         }
         */
 
 
         for (int i=1; i<=10; i++)
         {
     	  try
           {
               org.apache.commons.fileupload.FileItem fi = (org.apache.commons.fileupload.FileItem) context.getExternalContext().getRequestMap().get("file"+i);
 
               if(fi !=null && fi.getName() != null && fi.getName().length() !=0)
               {
             	  //validate fileName
             	  Util.validateUploadFileName(fi.getName());
             	  validateFileSize(fi.getSize());
                   // filename on the client
                  secResourceName = fi.getName();
                  if (secResourceName.indexOf("/") != -1)
                  {
                    secResourceName = secResourceName.substring(secResourceName.lastIndexOf("/")+1);
                  }
                  if (secResourceName.indexOf("\\") != -1)
                  {
                    secResourceName = secResourceName.substring(secResourceName.lastIndexOf("\\")+1);
                  }
 
                  if (logger.isDebugEnabled()) logger.debug("Rsrc name is "+secResourceName);
                  if (logger.isDebugEnabled()) logger.debug("upload section content data " + (int)fi.getSize());
 
                  secContentData= new byte[(int)fi.getSize()];
                  InputStream is = fi.getInputStream();
                  is.read(secContentData);
 
                  secContentMimeType = fi.getContentType();
 
                  if (logger.isDebugEnabled()) logger.debug("file upload success" + secContentMimeType);
                  if (logger.isDebugEnabled()) logger.debug("new names for upload content is" + secContentMimeType +"," + secResourceName);
 
                  addItem(secResourceName,secContentMimeType, addCollId, secContentData);
                  if(success_fields == null) success_fields = new ArrayList<String>();
                  success_fields.add(new Integer(i).toString());
                  success_fields.add(bundle.getString("add_item_success") + secResourceName);
                }
                else
                {
                   logger.info("File being uploaded is NULL");
                   continue;
                 }
           }
           catch (MeleteException mex)
           {
         	  String mexMsg = mex.getMessage();
            if(mex.getMessage().equals("embed_img_bad_filename"))mexMsg = "img_bad_filename";
           String errMsg = bundle.getString(mexMsg);
   	   //   context.addMessage ("FileUploadForm:chooseFile"+i, new FacesMessage(FacesMessage.SEVERITY_ERROR,mex.getMessage(),errMsg));
   	    //  logger.error("error in uploading multiple files" + errMsg);
   	      if(err_fields == null) err_fields = new ArrayList<String>();
   	      err_fields.add(new Integer(i).toString());
   	      err_fields.add(errMsg);
 		  //return "failure";
           }
           catch(Exception e)
           {
           logger.error("file upload FAILED" + e.toString());
           }
       }
       if(err_fields != null)
       {
     	  logger.debug("err found in fields" + err_fields.toString());
     	  return "file_upload_view";
       }
 	  }
 
 
       if(this.fileType.equals("link"))
       {
       Iterator utIterator = utList.iterator();
          //Finish validating here
       int count = -1;
         while (utIterator.hasNext())
 			{
         		count++;
 				try
 				{
 					UrlTitleObj utObj = (UrlTitleObj) utIterator.next();
 					if (utObj.title != null) utObj.title = utObj.title.trim();
 					String linkUrl = utObj.getUrl();
 					if(linkUrl != null) linkUrl = linkUrl.trim();
 					String checkUrl = linkUrl;
 					if(checkUrl != null)
 						{
 						checkUrl = checkUrl.replace("http://", "");
 						checkUrl = checkUrl.replace("https://", "");
 						}
 					if((utObj.title == null || utObj.title.length() == 0) && (checkUrl == null || checkUrl.length() == 0))
 					{
 						utIterator.remove();
 						continue;
 					}
 					if (utObj.title == null || utObj.title.length() == 0)
 					{
 						context.addMessage("LinkUploadForm:utTable:"+count+":title", new FacesMessage(FacesMessage.SEVERITY_ERROR, "URL_title_reqd", bundle.getString("URL_title_reqd")));
 						return "#";
 					}
 
 					if (checkUrl == null || checkUrl.length() == 0)
 					{
 						context.addMessage("LinkUploadForm:utTable:"+count+":url", new FacesMessage(FacesMessage.SEVERITY_ERROR, "URL_reqd", bundle.getString("URL_reqd")));
 						return "#";
 					}
 					Util.validateLink(linkUrl);
 				}
 				catch (MeleteException mex)
 				{
 					String errMsg = bundle.getString(mex.getMessage());
 					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "add_section_bad_url_formats", bundle
 							.getString("add_section_bad_url_formats")));
 					return "failure";
 				}
 				catch (Exception e)
 				{
 					logger.error("link upload FAILED" + e.toString());
 				}
 			}
          utIterator = utList.iterator();
          while (utIterator.hasNext())
         {
     	  UrlTitleObj utObj = (UrlTitleObj) utIterator.next();
     	  try
     	  {
               secContentMimeType=getMeleteCHService().MIME_TYPE_LINK;
               String linkUrl = utObj.getUrl();
               secResourceName = utObj.getTitle();
               if ((linkUrl != null)&&(linkUrl.trim().length() > 0)&&(secResourceName != null)&&(secResourceName.trim().length() > 0))
               {
                 secContentData = new byte[linkUrl.length()];
                 secContentData = linkUrl.getBytes();
                 addItem(secResourceName,secContentMimeType, addCollId, secContentData);
               }
             }
             catch (MeleteException mex)
             {
               String errMsg = bundle.getString(mex.getMessage());
       	      context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,mex.getMessage(),errMsg));
 			  return "failure";
             }
             catch(Exception e)
             {
               logger.error("link upload FAILED" + e.toString());
              }
           }
       }
       FacesContext ctx = FacesContext.getCurrentInstance();
       ValueBinding binding =Util.getBinding("#{manageResourcesPage}");
   	  ManageResourcesPage manResPage = (ManageResourcesPage) binding.getValue(ctx);
   	  manResPage.refreshCurrSiteResourcesList();
   	  manResPage.resetValues();
 	    return "manage_content";
       }
 
 
   private void addItem(String secResourceName, String secContentMimeType,String addCollId, byte[] secContentData) throws MeleteException
   {
 	  ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(false,secResourceName,"");
       if (logger.isDebugEnabled()) logger.debug("add resource now " + secContentData );
       try
       {
         String newResourceId = getMeleteCHService().addResourceItem(secResourceName,secContentMimeType, addCollId, secContentData,res );
       }
       catch(MeleteException me)
 	  {
 	     logger.error("error in creating resource for section content");
 	     throw me;
 	  }
       catch(Exception e)
 	  {
 	     logger.error("error in creating resource for section content");
 	     e.printStackTrace();
 	     throw new MeleteException("add_item_fail");
 	   }
   }
 
   public void updateNumber(ValueChangeEvent event)throws AbortProcessingException
   {
           UIInput numberItemsInput = (UIInput)event.getComponent();
 
           this.numberItems = (String)numberItemsInput.getValue();
 
           if (Integer.parseInt(this.numberItems) > this.utList.size())
           {
         	  int newItemsCount = Integer.parseInt(this.numberItems) - this.utList.size();
         	  for (int i=0; i< newItemsCount; i++)
         	  {
         		UrlTitleObj utObj = new UrlTitleObj("http://","");
       			this.utList.add(utObj);
         	  }
           }
           if (Integer.parseInt(this.numberItems) < this.utList.size())
           {
         	  int listSize = this.utList.size();
         	   for (int i=Integer.parseInt(this.numberItems); i< listSize; i++)
         	  {
 
         		  this.utList.remove(Integer.parseInt(this.numberItems));
         	  }
           }
 
 
   }
 
   public void removeLink(ActionEvent evt)
   {
 	  FacesContext ctx = FacesContext.getCurrentInstance();
       UICommand cmdLink = (UICommand)evt.getComponent();
 	  String selclientId = cmdLink.getClientId(ctx);
 	  selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 	  selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 	  String rowId = selclientId.substring(0,selclientId.indexOf(':'));
 	  this.removeLinkIndex = Integer.parseInt(rowId);
 	  if (Integer.parseInt(this.numberItems) > 1)
 	  {
 	    this.numberItems = String.valueOf(Integer.parseInt(this.numberItems) - 1);
 	    this.utList.remove(this.removeLinkIndex);
 	  }
 	  else
 	  {
 		  this.utList = new ArrayList();
 		  this.utList.add(new UrlTitleObj("http://",""));
 	  }
 
   }
 
   public String redirectToLinkUpload()
   {
 	  return "link_upload_view";
   }
 
   public String cancel()
   {
 	  FacesContext ctx = FacesContext.getCurrentInstance();
       ValueBinding binding =Util.getBinding("#{manageResourcesPage}");
   	  ManageResourcesPage manResPage = (ManageResourcesPage) binding.getValue(ctx);
   	  manResPage.refreshCurrSiteResourcesList();
   	  manResPage.resetValues();
 	  return "manage_content";
   }
 
 public MeleteCHService getMeleteCHService()
 {
 	return this.meleteCHService;
 }
 
 public void setMeleteCHService(MeleteCHService meleteCHService)
 {
 	this.meleteCHService = meleteCHService;
 }
 
 public String getFileType()
 {
 	return this.fileType;
 }
 
 public void setFileType(String fileType)
 {
 	this.fileType = fileType;
 }
 
 public List getUtList()
 {
 	if (this.utList == null)
 	{
 	utList = new ArrayList();
 	if (this.fileType.equals("link"))
 	{
 		for (int i=0; i< Integer.parseInt(this.numberItems); i++)
 		{
 			UrlTitleObj utObj = new UrlTitleObj("http://","");
 			utList.add(utObj);
 		}
 	}
 	}
 	return this.utList;
 }
 
 public void setUtList(List utList)
 {
 	this.utList = utList;
 }
 public class UrlTitleObj{
 	String url,title;
 	public UrlTitleObj(String url, String title)
 	{
 		this.url = url;
 		this.title = title;
 	}
 	public String getUrl()
 	{
 		return this.url;
 	}
 	public void setUrl(String url)
 	{
 		this.url = url;
 	}
 	public String getTitle()
 	{
 		return this.title;
 	}
 	public void setTitle(String title)
 	{
 		this.title = title;
 	}
 }
 public UIData getTable()
 {
 	return this.table;
 }
 
 public void setTable(UIData table)
 {
 	this.table = table;
 }
 
 /*public boolean validateFile(String up_field_name)
 {
 	  FacesContext ctx = FacesContext.getCurrentInstance();
 	  org.apache.commons.fileupload.FileItem fi = (org.apache.commons.fileupload.FileItem) ctx.getExternalContext().getRequestMap().get(up_field_name);
 	  if(fi != null)
 	  {
 		  System.out.println("file throu apache uploads is not null" + fi.getName());
 	  } else System.out.println("fi is null");
 	  return true;
 }*/
 
 public boolean validateFile(String up_field)
 {
 //	File f = new File(up_field);
 	try{
 	Util.validateUploadFileName(up_field);
 	} catch(MeleteException me)
 	{
 		return false;
 	}
 	return true;
 }
 
 public void validateFileSize(long sz) throws MeleteException
 {
 	//1 MB = 1048576 bytes
 	if (new Long((sz/1048576)).intValue() > getMaxUploadSize())
 		throw new MeleteException("file_too_large");
 }
 
 /**
  * @return the err_fields
  */
 public ArrayList<String> getErr_fields()
 {
 	return this.err_fields;
 }
 
 /**
  * @return the success_fields
  */
 public ArrayList<String> getSuccess_fields()
 {
 	return this.success_fields;
 }
 
 }
 
