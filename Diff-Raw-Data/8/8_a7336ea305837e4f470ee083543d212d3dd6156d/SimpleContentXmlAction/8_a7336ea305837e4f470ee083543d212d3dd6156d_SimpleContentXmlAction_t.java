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
 
 package org.infoglue.cms.applications.contenttool.actions;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.infoglue.cms.applications.common.actions.SimpleXmlServiceAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
 import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.DigitalAssetVO;
 import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.TransactionHistoryVO;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.treeservice.ss.ContentNodeSupplier;
 
 import com.frovi.ss.Tree.INodeSupplier;
 
 public class SimpleContentXmlAction extends SimpleXmlServiceAction
 {
     private final static Logger logger = Logger.getLogger(SimpleContentXmlAction.class.getName());
 
 	private static final long serialVersionUID = 1L;
 	
     private static String TYPE_FOLDER = "ContentFolder";
     private static String TYPE_ITEM = "ContentItem";
 	private String digitalAssetKey;
 	private Integer digitalAssetId;
 	private Integer languageId;
 
 	public INodeSupplier getNodeSupplier() throws SystemException
 	{
 		ContentNodeSupplier sup =  new ContentNodeSupplier(getRepositoryId(), this.getInfoGluePrincipal());
 		sup.setShowLeafs(showLeafs.compareTo("yes")==0);		
 		sup.setAllowedContentTypeIds(allowedContentTypeIds);
 		return sup;
 	}
 	
 	public String doDigitalAssets() throws Exception
 	{
 		String ret = "";
 		DigitalAssetVO digitalAssetVO = null;
 
 		if (digitalAssetId != null) {
 			digitalAssetVO = DigitalAssetController
 					.getDigitalAssetVOWithId(digitalAssetId);
 		} else {
 			digitalAssetVO = DigitalAssetController.getDigitalAssetVO(
 					parent, languageId, digitalAssetKey, true);
 		}
 
 		ret = "<digitalAssetInfo>"
 				+ "<assetURL>"
 				+ DigitalAssetController.getDigitalAssetUrl(digitalAssetVO.getId()) 
 				+ "</assetURL>" 
 				+ "<assetId>"
 				+ digitalAssetVO.getId() 
 				+ "</assetId>" 
 				+ "</digitalAssetInfo>";
 
 		return ret;
 	}
 	
 	
	
     public ContentVersionVO getLatestContentVersionVO(Integer contentId, Integer languageId)
 	{
 		ContentVersionVO contentVersionVO = null;
 		try
 		{
 			contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred when we tried to get the latest version for the content:" + e.getMessage(), e);
 		}
 		
 		return contentVersionVO;
 	}
 
     public Element getContentVersionElement(Integer contentVersionId) throws SystemException, Bug, UnsupportedEncodingException
     {
 		ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();
         ContentVersionVO vo = contentVersionController.getContentVersionVOWithId(contentVersionId);
         return getContentVersionElement(vo);
     }
     
     public Element getContentVersionElement(ContentVersionVO vo) throws SystemException, Bug, UnsupportedEncodingException
     {
         Element element = DocumentHelper.createElement("contentVersion");
         Element head = DocumentHelper.createElement("head");
         Element value = DocumentHelper.createElement("value");
 
         head.addAttribute("id", "" + vo.getContentVersionId());
 	    head.addAttribute("languageId", "" + vo.getLanguageId());
 	    head.addAttribute("languageName", vo.getLanguageName());
 	    head.addAttribute("isActive", "" + vo.getIsActive());
 
	    /*
 	    TransactionHistoryController transactionHistoryController = TransactionHistoryController.getController();
         TransactionHistoryVO transactionHistoryVO = transactionHistoryController.getLatestTransactionHistoryVOForEntity(ContentVersionImpl.class, vo.getContentVersionId());
 	    if(transactionHistoryVO!=null)
 	        head.addAttribute("mod", formatDate(transactionHistoryVO.getTransactionDateTime()));
	    */
	    
        head.addAttribute("mod", "" + vo.getModifiedDateTime().getTime());
         value.addCDATA(URLEncoder.encode(vo.getVersionValue(),"UTF-8"));
         element.add(head);
         element.add(value);
         return element;
     }
     
     /*
      * Returns document for a single contentVersion (parent)
      */
     public String doContentVersion() throws Exception
 	{
         Document doc = DocumentHelper.createDocument();
         doc.add(getContentVersionElement(parent));
 	    return out(getFormattedDocument(doc));
 	}
     
     /*
      * Returns all contentVersions for a given content (parent)
      */
     public String doContentVersions() throws Exception
 	{
         Document doc = DocumentHelper.createDocument();
         Element root = doc.addElement("contentVersions");
         Collection availableLanguages = ContentController.getContentController().getRepositoryLanguages(parent);
         for(Iterator i=availableLanguages.iterator();i.hasNext();)
         {
         	LanguageVO lvo = (LanguageVO) i.next();
         	ContentVersionVO vo = getLatestContentVersionVO(parent, lvo.getLanguageId());
         	if(vo!=null)
         		root.add(getContentVersionElement(vo));
         }
         
 		ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();
         return out(getFormattedDocument(doc));
 	}
 
 	/* (non-Javadoc)
 	 * @see org.infoglue.cms.applications.common.actions.SimpleXmlServiceAction#getRootEntityVO(java.lang.Integer, org.infoglue.cms.security.InfoGluePrincipal)
 	 */
 	protected BaseEntityVO getRootEntityVO(Integer repositoryId, InfoGluePrincipal principal) throws ConstraintException, SystemException {
 		return ContentControllerProxy.getController().getRootContentVO(repositoryId, principal.getName());
 	}
 	
 }
