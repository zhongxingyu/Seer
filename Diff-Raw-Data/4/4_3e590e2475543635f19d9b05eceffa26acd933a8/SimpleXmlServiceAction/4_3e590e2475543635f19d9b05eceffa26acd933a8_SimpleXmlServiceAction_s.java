 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 /**
  * @author Stefan Sik
  * @since 1.4
  */
 
 package org.infoglue.cms.applications.common.actions;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.ContentTypeDefinition;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
 import org.infoglue.cms.entities.management.RepositoryVO;
 import org.infoglue.cms.entities.management.TransactionHistoryVO;
 import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 
 import com.frovi.ss.Tree.BaseNode;
 import com.frovi.ss.Tree.INodeSupplier;
 
 public abstract class SimpleXmlServiceAction extends WebworkAbstractAction
 {
 	// protected static String ENCODING = "ISO-8859-1";
 	protected static String ENCODING = "UTF-8";
     protected static String TYPE_FOLDER = "Folder";
     protected static String TYPE_ITEM = "Item";
     protected static String TYPE_REPOSITORY = "Repository";
     protected String showLeafs = "yes";
     protected Integer parent = null;
     protected Integer repositoryId = null;
     protected String urlArgSeparator = "&";
     protected String action = "";
     protected boolean createAction = false;
     protected boolean useTemplate = false;
     protected VisualFormatter formatter = new VisualFormatter();
 	protected String[] allowedContentTypeNames = null;
 
 
 	public abstract INodeSupplier getNodeSupplier() throws SystemException;
 	
 	protected abstract BaseEntityVO getRootEntityVO(Integer repositoryId, InfoGluePrincipal principal) throws ConstraintException, SystemException;
 	
 	public List getContentTypeDefinitions() throws Exception
 	{
 		return ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
 	}      
 	
 	private String encode(String text)
 	{
 		return text;
 	}
 	
 	protected String makeAction(BaseNode node) throws UnsupportedEncodingException
 	{
 		String action = "javascript:onTreeItemClick(this,";
 		//action+="'" + node.getId() + "','" + repositoryId + "','" + URLEncoder.encode(node.getTitle(),ENCODING) + "');";
 		//action+="'" + node.getId() + "','" + repositoryId + "','" + new VisualFormatter().escapeForAdvancedJavascripts(node.getTitle()) + "');";
 		action+="'" + node.getId() + "','" + repositoryId + "','" + new VisualFormatter().escapeForAdvancedJavascripts(node.getTitle()) + "');";
         //System.out.println("action:" + action);
 		return action;
 	}
 	
 	protected String getFormattedDocument(Document doc)
 	{
 	    return getFormattedDocument(doc, true);
 	}
 	
 	protected String getFormattedDocument(Document doc, boolean compact)
 	{
 	    OutputFormat format = compact ? OutputFormat.createCompactFormat() : OutputFormat.createPrettyPrint(); 
 		format.setEncoding(ENCODING);
 		
 		format.setExpandEmptyElements(false);
 		StringWriter stringWriter = new StringWriter();
 		XMLWriter writer = new XMLWriter(stringWriter, format);
 		try
         {
             writer.write(doc);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 		return stringWriter.toString();
 	}
 	
     protected String out(String string) throws IOException
     {
 		getResponse().setContentType("text/xml; charset=" + ENCODING);
 		// getResponse().setContentLength(string.length());
 		/*OutputStream outs = getResponse().getOutputStream();
 		outs.write(string.getBytes());
 		outs.flush();
 		outs.close();*/
 		PrintWriter out = getResponse().getWriter();
 		out.println(string);
 		// out.write(new String(string.getBytes(), ENCODING));
 		return null;
     }
 	
     /*
      * Returns all Languages for a given repository (repositoryId)
      */
     public String doLanguage() throws Exception
 	{
         return null;
 	}
 
     /*
      * Returns all contentTypeDefinitions
      */
     public String doContentTypeDefinitions() throws Exception
 	{
     	List contentTypeDefinitions = getContentTypeDefinitions();
         Document doc = DocumentHelper.createDocument();
         Element root = doc.addElement("definitions");
 	    TransactionHistoryController transactionHistoryController = TransactionHistoryController.getController();
     	
     	for(Iterator i=contentTypeDefinitions.iterator();i.hasNext();)
     	{
     		ContentTypeDefinitionVO vo = (ContentTypeDefinitionVO) i.next();
     		if(vo.getType().compareTo(ContentTypeDefinitionVO.CONTENT)==0)
     		{
     		    TransactionHistoryVO transactionHistoryVO = transactionHistoryController.getLatestTransactionHistoryVOForEntity(ContentTypeDefinitionImpl.class, vo.getContentTypeDefinitionId());
     		    
 	    		Element definition = DocumentHelper.createElement("definition");
 	    		definition
 					.addAttribute("id", "" + vo.getContentTypeDefinitionId())
 					.addAttribute("type", "" + vo.getType())
 					.addAttribute("name", vo.getName())
 				;
 	    		
 	    		if(transactionHistoryVO!=null)
 	    		    definition.addAttribute("mod", formatDate(transactionHistoryVO.getTransactionDateTime()));
 	    		
 	    		Element schemaValue = definition.addElement("schemaValue");
 	    		schemaValue.addCDATA(vo.getSchemaValue());
 	    		root.add(definition);
     		}
     	}
 		return out(getFormattedDocument(doc));
     	
 	}
 	
     protected String formatDate(Date date)
     {
         return "" + date;
     }
 
     /*
      * Main action, returns the content tree
      */
     public String doExecute() throws Exception
     {
         if (useTemplate) return "success";
         
         Document doc = DocumentHelper.createDocument();
         Element root = doc.addElement("tree");
         
     	INodeSupplier sup;
 
     	if(repositoryId == null)
     	{
     	    List repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal());
     	    for(Iterator i=repositories.iterator();i.hasNext();)
     	    {
     	        RepositoryVO r = (RepositoryVO) i.next();
     			BaseEntityVO entityVO = getRootEntityVO(r.getId(), this.getInfoGluePrincipal());
     	        
     	        String src= action + "?repositoryId=" + r.getId() + urlArgSeparator + "parent=" + entityVO.getId();
 				if(createAction && src.length() >0) src += urlArgSeparator + "createAction=true";
 				if(action.length()>0 && src.length() >0) src += urlArgSeparator + "action=" + action;
 				String allowedContentTypeNamesUrlEncodedString = getAllowedContentTypeNamesAsUrlEncodedString();
 				System.out.println("allowedContentTypeNamesUrlEncodedString1:" + allowedContentTypeNamesUrlEncodedString);
 				if(allowedContentTypeNamesUrlEncodedString.length()>0 && src.length() >0) src += urlArgSeparator + allowedContentTypeNamesUrlEncodedString;
     	        
     			System.out.println("src1:" + src);
     			
 				String text=r.getName();
     	        Element element = root.addElement("tree");
     	        element
 	        	.addAttribute("id", "" + r.getId())
     	        	.addAttribute("repositoryId", "" + r.getId())
     	        	.addAttribute("text", encode(text))
     	        	.addAttribute("src", src)
     	        	.addAttribute("hasChildren", "true")
     	        	.addAttribute("type", TYPE_REPOSITORY);
     	    }
     	    out(getFormattedDocument(doc));
     		return null;
     	}
     	
     	sup = getNodeSupplier();
     	    	
     	if(parent == null)
     	{
     		BaseNode node = sup.getRootNode();
 			String text = node.getTitle();
 	        String type = TYPE_FOLDER;
 			String src = action + "?repositoryId=" + repositoryId + urlArgSeparator + "parent=" + node.getId();
 			if(createAction && src.length() >0) src += urlArgSeparator + "createAction=true";
 			if(action.length()>0 && src.length() >0) src += urlArgSeparator + "action=" + action;
 			String allowedContentTypeNamesUrlEncodedString = getAllowedContentTypeNamesAsUrlEncodedString();
 			System.out.println("allowedContentTypeNamesUrlEncodedString2:" + allowedContentTypeNamesUrlEncodedString);
 			if(allowedContentTypeNamesUrlEncodedString.length()>0 && src.length() >0) src += urlArgSeparator + allowedContentTypeNamesUrlEncodedString;
 	        
 			System.out.println("src2:" + src);
 
 	        Element elm = root.addElement("tree");
 	        elm
 	        	.addAttribute("id", "" + node.getId())
 	        	.addAttribute("repositoryId", "" + repositoryId)
 	        	.addAttribute("text", encode(text))
 	        	.addAttribute("src", src)
    	        	.addAttribute("hasChildren", "true")
 	        	.addAttribute("type", type);
 			
     	    out(getFormattedDocument(doc));
     		return null;
     	}
     	
     	if(parent.intValue() > -1)
     	{
 			Collection containerNodes = sup.getChildContainerNodes(parent);
 			Collection childNodes = sup.getChildLeafNodes(parent);
 			
 			ContentController contentController = ContentController.getContentController();
 			ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();
 
 			Iterator it = containerNodes.iterator();
 			while (it.hasNext())
 			{
 				BaseNode theNode = (BaseNode) it.next();
 				if (theNode.isContainer() && sup.hasChildren())
 				{
 					theNode.setChildren(sup.hasChildren(theNode.getId()));
 				}
 				
 				// String src = theNode.hasChildren() ? action + "?repositoryId=" + repositoryId + urlArgSeparator + "parent=" + theNode.getId(): "";
 				String src = action + "?repositoryId=" + repositoryId + urlArgSeparator + "parent=" + theNode.getId();
 				if(createAction && src.length() >0) src += urlArgSeparator + "createAction=true";
 				if(createAction && src.length() >0) src += urlArgSeparator + "showLeafs=" + showLeafs;
 				if(action.length()>0 && src.length() >0) src += urlArgSeparator + "action=" + action;
 				String allowedContentTypeNamesUrlEncodedString = getAllowedContentTypeNamesAsUrlEncodedString();
 				if(allowedContentTypeNamesUrlEncodedString.length()>0 && src.length() >0) src += urlArgSeparator + allowedContentTypeNamesUrlEncodedString;
     	        
 		        Element elm = root.addElement("tree");
 		        elm
 		        	.addAttribute("id", "" + theNode.getId())
 		        	.addAttribute("parent", "" + parent)
 		        	.addAttribute("repositoryId", "" + repositoryId)
 		        	.addAttribute("text", encode(theNode.getTitle()))
 		        	.addAttribute("src", src)
 		        	.addAttribute("type", TYPE_FOLDER)
 		        	.addAttribute("hasChildren", "" + theNode.hasChildren());
 		        
 		        if(createAction) elm.addAttribute("action", makeAction(theNode));
 			}
 			 
 			it = childNodes.iterator();
 			while (it.hasNext())
 			{
 				BaseNode theNode = (BaseNode) it.next();
 				String text = theNode.getTitle();
 				String action = makeAction(theNode);
 		        String type = TYPE_ITEM;
 		        Element elm = root.addElement("tree");
 		        elm
 		        	.addAttribute("id", "" + theNode.getId())
 		        	.addAttribute("parent", "" + parent)
 		        	.addAttribute("repositoryId", "" + repositoryId)
 		        	.addAttribute("text", encode(text))
 		        	.addAttribute("type", type)
 				;
 		        if(createAction) 
 		        	elm.addAttribute("action", action);
 		        else
 		        {
 			        ContentVersionVO activeVersion = contentVersionController.getLatestActiveContentVersionVO(theNode.getId(), LanguageController.getController().getMasterLanguage(repositoryId).getLanguageId());
 			        if(activeVersion!=null && !useTemplate)
 			            elm.addAttribute("activeVersion", "" + activeVersion.getContentVersionId());
 		        }
 		        
		        if(!useTemplate)
 		        {
 		            ContentTypeDefinitionVO contentTypeDefinitionVO = contentController.getContentTypeDefinition(theNode.getId());
 		        	if(contentTypeDefinitionVO != null)
 		        	    elm.addAttribute("contentTypeId","" + contentTypeDefinitionVO.getContentTypeDefinitionId());
 		        }
 		    }
 			
     	    out(getFormattedDocument(doc));
     		return null;
     	}
     	
     	return null;
     }
 
 
 	public Integer getParent() {
 		return parent;
 	}
 
 	public void setParent(Integer integer) {
 		parent = integer;
 	}
 
 	public Integer getRepositoryId() {
 		return repositoryId;
 	}
 
 	public void setRepositoryId(Integer integer) {
 		repositoryId = integer;
 	}
 
     public boolean isCreateAction()
     {
         return createAction;
     }
     public void setCreateAction(boolean createAction)
     {
         this.createAction = createAction;
     }
     public boolean isUseTemplate()
     {
         return useTemplate;
     }
     public void setUseTemplate(boolean useTemplate)
     {
         this.useTemplate = useTemplate;
     }
     public String getAction()
     {
         return action;
     }
     public void setAction(String action)
     {
         this.action = action;
     }
 	public String getShowLeafs() {
 		return showLeafs;
 	}
 	public void setShowLeafs(String showLeafs) {
 		this.showLeafs = showLeafs;
 	}
 	
     public String[] getAllowedContentTypeNames()
     {
         return allowedContentTypeNames;
     }
     
     public void setAllowedContentTypeNames(String[] allowedContentTypeNames)
     {
         this.allowedContentTypeNames = allowedContentTypeNames;
     }
     
 	public String getAllowedContentTypeNamesAsUrlEncodedString() throws Exception
     {
 	    if(allowedContentTypeNames == null)
 	        return "";
 	    
         StringBuffer sb = new StringBuffer();
         
         for(int i=0; i<allowedContentTypeNames.length; i++)
         {
             if(i > 0)
                 sb.append("&");
             
             sb.append("allowedContentTypeNames=" + URLEncoder.encode(allowedContentTypeNames[i], "UTF-8"));
         }
         
         return sb.toString();
     }
 }
