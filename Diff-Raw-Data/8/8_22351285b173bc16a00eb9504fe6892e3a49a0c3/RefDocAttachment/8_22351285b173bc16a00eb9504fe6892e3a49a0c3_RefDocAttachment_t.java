 // Copyright (c) 2006 ScenPro, Inc.
 
// $Header: /cvsshare/content/cvsroot/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/RefDocAttachment.java,v 1.56 2009-04-15 17:53:38 hebell Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 //import files
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Random;
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import oracle.sql.BLOB;
 import org.apache.log4j.Logger;
 
 /**
  * The RefDocAttachment class is the main class to handle Reference 
  * Document Attachments page requests.<p>
  * 
  * @author Dan Grandquist
  * @version 3.1
  */
 
 /*
  The CaCORE Software License, Version 3.0 Copyright 2002-2005 ScenPro, Inc. (ScenPro)  
  Copyright Notice.  The software subject to this notice and license includes both
  human readable source code form and machine readable, binary, object code form
  (the CaCORE Software).  The CaCORE Software was developed in conjunction with
  the National Cancer Institute (NCI) by NCI employees and employees of SCENPRO.
  To the extent government employees are authors, any rights in such works shall
  be subject to Title 17 of the United States Code, section 105.    
  This CaCORE Software License (the License) is between NCI and You.  You (or Your)
  shall mean a person or an entity, and all other entities that control, are 
  controlled by, or are under common control with the entity.  Control for purposes
  of this definition means (i) the direct or indirect power to cause the direction
  or management of such entity, whether by contract or otherwise, or (ii) ownership
  of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial 
  ownership of such entity.  
  This License is granted provided that You agree to the conditions described below.
  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge,
  irrevocable, transferable and royalty-free right and license in its rights in the
  CaCORE Software to (i) use, install, access, operate, execute, copy, modify, 
  translate, market, publicly display, publicly perform, and prepare derivative 
  works of the CaCORE Software; (ii) distribute and have distributed to and by 
  third parties the CaCORE Software and any modifications and derivative works 
  thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to 
  third parties, including the right to license such rights to further third parties.
  For sake of clarity, and not by way of limitation, NCI shall have no right of 
  accounting or right of payment from You or Your sublicensees for the rights 
  granted under this License.  This License is granted at no charge to You.
  1.	Your redistributions of the source code for the Software must retain the above
  copyright notice, this list of conditions and the disclaimer and limitation of
  liability of Article 6, below.  Your redistributions in object code form must
  reproduce the above copyright notice, this list of conditions and the disclaimer
  of Article 6 in the documentation and/or other materials provided with the 
  distribution, if any.
  2.	Your end-user documentation included with the redistribution, if any, must 
  include the following acknowledgment: This product includes software developed 
  by SCENPRO and the National Cancer Institute.  If You do not include such end-user
  documentation, You shall include this acknowledgment in the Software itself, 
  wherever such third-party acknowledgments normally appear.
  3.	You may not use the names "The National Cancer Institute", "NCI" ScenPro, Inc.
  and "SCENPRO" to endorse or promote products derived from this Software.  
  This License does not authorize You to use any trademarks, service marks, trade names,
  logos or product names of either NCI or SCENPRO, except as required to comply with
  the terms of this License.
  4.	For sake of clarity, and not by way of limitation, You may incorporate this
  Software into Your proprietary programs and into any third party proprietary 
  programs.  However, if You incorporate the Software into third party proprietary
  programs, You agree that You are solely responsible for obtaining any permission
  from such third parties required to incorporate the Software into such third party
  proprietary programs and for informing Your sublicensees, including without 
  limitation Your end-users, of their obligation to secure any required permissions
  from such third parties before incorporating the Software into such third party
  proprietary software programs.  In the event that You fail to obtain such permissions,
  You agree to indemnify NCI for any claims against NCI by such third parties, 
  except to the extent prohibited by law, resulting from Your failure to obtain
  such permissions.
  5.	For sake of clarity, and not by way of limitation, You may add Your own 
  copyright statement to Your modifications and to the derivative works, and You 
  may provide additional or different license terms and conditions in Your sublicenses
  of modifications of the Software, or any derivative works of the Software as a 
  whole, provided Your use, reproduction, and distribution of the Work otherwise 
  complies with the conditions stated in this License.
  6.	THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  
  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SCENPRO, OR THEIR AFFILIATES 
  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 public class RefDocAttachment {
 	//String sAction;
 	String msg = null;
 	HttpSession session = null;
 	HttpServletRequest req = null;
 	HttpServletResponse res = null;
 	CurationServlet m_servlet;
 	Logger logger = Logger.getLogger(RefDocAttachment.class.getName());
 
 	/**
 	 * @param req
 	 * @param res
 	 * @param m_servlet
 	 */
 	public RefDocAttachment(HttpServletRequest req, HttpServletResponse res, CurationServlet m_servlet) {
 
 		session = req.getSession();
 		this.req = req;
 		this.res = res;
 		this.m_servlet = m_servlet;
 	}
 /**
  *  Open the Ref Documents attachments page.
  */
 @SuppressWarnings("unchecked")
 public void doOpen (){
 	
 	GetACService getAC = new GetACService(req, res, m_servlet);
 	Vector<TOOL_OPTION_Bean> vList = new Vector<TOOL_OPTION_Bean>();
     vList= getAC.getToolOptionData("CURATION","REFDOC_FILECACHE","");
     String RefDocFileCache = vList.get(0).getVALUE();
     vList = getAC.getToolOptionData("CURATION","REFDOC_FILEURL","");
     String RefDocFileUrl = vList.get(0).getVALUE();
 	Vector vSRows = (Vector)session.getAttribute("vSelRows");
    	GetACSearch getACSearch = new GetACSearch(req, res, m_servlet);
   	String sACSearch = (String)session.getAttribute("searchAC");
 		
 		getACSearch.getSelRowToUploadRefDoc(req, res, "");
 		
 		// Check to make sure it was a DE, DEC or VD
 		if (sACSearch.equals("DataElement")|| sACSearch.equals("DataElementConcept") || sACSearch.equals("ValueDomain")){
 			DataManager.setAttribute(session, "dispACType", sACSearch);
 			
 			String dispType = (String)session.getAttribute("displayType");
 		    if (dispType == null) dispType = "";
 
 		    REF_DOC_Bean refBean = new REF_DOC_Bean(); 
 		    	    
 		    // Get number of items
 		    Vector vRefDoc = (Vector)req.getAttribute("RefDocList");
 		    
 		    // Check for Reference Document for that AC 
 		    // Has 0 Reference Document(s)
 		    if (vRefDoc == null){		    	
 		    	vRefDoc = (Vector)session.getAttribute("RefDocList");
 		    	req.setAttribute("RefDocList", vRefDoc);
 		    }
  
 		    // Has Reference Document(s)
 		    if (vRefDoc != null){
 
 		    	Vector vRefDocRows = new Vector();
 		    	Vector vRefDocDocs = new Vector();
 		    	
 		    	// Loop thru each Reference Document
 		    	for(int i=0; i<(vRefDoc.size()); i++)
 			      {
 		    		
 		    		//	Get AC_IDSEQ from the m_DE bean
 		    		refBean = (REF_DOC_Bean)vRefDoc.elementAt(i);
 			    	
 	  			    // get RD_IDSEQ from REFERENCE_DOCUMENTS using AC_IDSEQ
 			    	String str = refBean.getREF_DOC_IDSEQ();
 
 			    	vRefDocRows.addElement(i);
   
 					//get users contexts ids
 					String RDContextID = "";
 					String docContext =  refBean.getCONTEXT_NAME();
 					String docContextID =  refBean.getCONTE_IDSEQ();
 				    Vector vContextID = (Vector)session.getAttribute("vWriteContextDE_ID");
 				    
 				    // Check to see if user has a context
 					if (vContextID != null) 
 				    {
 					  refBean.setIswritable(false);
 					  
 					  // Loop thru Reference Document contexts to see if users has context matches
 				      for (int ndx = 0; vContextID.size()>ndx; ndx++)
 				      {
 				    	RDContextID = (String)vContextID.elementAt(ndx);
 				    	
 				    	// If matches set writeable attribute to true
 				        if (docContextID.equals(RDContextID))
 				        {
 				        	refBean.setIswritable(true);
 				        }
 				      }
 				    }
 			    	
 			// SQL to get the results
 	        String select = "select NAME , blob_content from sbr.reference_blobs_view where rd_idseq = ?";
             PreparedStatement pstmt = null;
             ResultSet rs = null;
 	        // make plsql call 
 	     try {
 						pstmt = m_servlet.getConn().prepareStatement(select);
 						pstmt.setString(1 , str);
 						rs = pstmt.executeQuery();
 						int j = 0;
 						String Doclist = "<td>";
 						
 						while ( rs.next()){
 							
 							// iterate counter
 							j++;
 
 							// Build HTML text for table
 							String fileName = rs.getString(1);
 							String fileName2[] = fileName.split("/");
 							String fileDirectory = "";
 							if (docContext == null) docContext = "";	
 
 							//  if the ref doc is in a writable context add the delete X
 							if (refBean.getIswritable())	{
							Doclist = Doclist + "<img src=\"images/delete_small.gif\" title=\"Delete Attachment\" onclick=\"onDocDelete('" 
 											  + fileName + "' , '" 
 											  + fileName2[1] 
											  + "' );\">"
 											  + "&nbsp;&nbsp;"
 											  + "<a href=\""
 											  + RefDocFileUrl 
 											  + fileName 
 											  + "\" target=\"_blank\">"
 											  + fileName2[1]
 											  + "<a><br>";
 							
 							// else no X
 							}
 							else{
 								Doclist = Doclist + "&nbsp;&nbsp;&nbsp;&nbsp;"
 								  + "<a href=\""
 								  + RefDocFileUrl 
 								  + fileName 
 								  + "\" target=\"_blank\">"
 								  + fileName2[1]
 								  + "<a><br>";
 							}
 							
 							// Extract file to file system
 							BLOB bRefBlob = (BLOB)rs.getBlob(2);
 							doBlobtoFile(bRefBlob , fileName);
 							
 						} //end of while
 						Doclist = Doclist + "</td>";
 						
 						vRefDocDocs.addElement(Doclist);
 						
 		  			DataManager.setAttribute(session, "RefDocRows", vRefDocDocs);
 					} catch (SQLException e) {
 						logger.error("ERROR - RefDocAttachment: " + e.getMessage());
 						msg = "Unable to access the database.";
 					}
 					finally{
 						rs = SQLHelper.closeResultSet(rs);
 			            pstmt = SQLHelper.closePreparedStatement(pstmt);
 					
 					}			    	
 			  }//end of for
 		    m_servlet.ForwardJSP(req, res, "/RefDocumentUploadPage.jsp");
 		    	
 	    }
 	    else{
 	        //no ref docs
 	    	m_servlet.ForwardJSP(req, res, "/RefDocumentUploadPage.jsp");
 	    }
 
 		}
 		else 
 		{
 			msg = "The selected items are not supported for the document upload feature.";
 		}
   
 }
 
 
 /**
  * Preforms the actions nessesary to upload a file from the client 
  * computer to the database and returns the client to the Reference 
  * Document Attachments page.
  */
 public void doFileUpload ()
 {
 	
 	GetACService getAC = new GetACService(req, res, m_servlet);
 	PreparedStatement pstmt = null;
 	REF_DOC_Bean refBean = new REF_DOC_Bean(); 
 	HttpSession session = req.getSession();
 
     // Get number of items
     Vector vRefDoc = (Vector)session.getAttribute("RefDocList");
 	
 	// get option table data
 	Vector<TOOL_OPTION_Bean> vList = new Vector<TOOL_OPTION_Bean>();
     vList = getAC.getToolOptionData("CURATION","REFDOC_FILECACHE","");
     String RefDocFileCache = vList.get(0).getVALUE();
 	
 	RefDocMultipartForm refDocFormdata = new RefDocMultipartForm();
 	refDocFormdata.parse(req, RefDocFileCache);
 	
 	String writeObjSQL = "INSERT INTO sbr.reference_blobs_view( " +
     "RD_IDSEQ, " +
     "NAME, " +
     "MIME_TYPE, " +
     "DOC_SIZE, " +
     "DAD_CHARSET, " +
     "CONTENT_TYPE, BLOB_CONTENT) " +
     "VALUES (?, ?, ?, ?, ?, ?, empty_blob()) ";
 	
 	try {
 			/*
 			 * Due to the way the blob type works you have to turn off the 
 			 * auto commit on the insert query. If you do not the commit 
 			 * happens before the blob type if fully built causing the blob
 			 * to not be writen to the db and an exception is thrown.
 			 */
 		   m_servlet.getConn().setAutoCommit(false);
 			Vector<String> selectedRefDocs = refDocFormdata
 					.getSelectedRefDocs();
 
 			int jInt = selectedRefDocs.size();
 			
 			// Loop thru the selected row (should be only one)
 			for (int ndx = 0; ndx < jInt; ndx++) {
 
 				String str = selectedRefDocs.elementAt(ndx);
 				int refDocIndex = Integer.valueOf(str);
 
 				pstmt = m_servlet.getConn().prepareStatement(writeObjSQL);
 
 				// Get the RD_IDSEQ
 				if (vRefDoc != null) {
 					refBean = (REF_DOC_Bean) vRefDoc.elementAt(refDocIndex);
 					str = refBean.getREF_DOC_IDSEQ();
 				}
 
 				// set RD_IDSEQ
 				pstmt.setString(1, str);
 				// set NAME
 				String dbfileName = "F" + (new Random()).nextInt() + "/"
 						+ refDocFormdata.getFileName();
 				pstmt.setString(2, dbfileName);
 				// set MIME_TYPE
 				pstmt.setString(3, refDocFormdata.getMimeType());
 				// set DOC_SIZE
 				pstmt.setString(4, Integer.toString(refDocFormdata
 						.getFileSize()));
 				// set DAD_CHARSET
 				pstmt.setString(5, refDocFormdata.getCharSet());
 				// set CONTENT_TYPE
 				pstmt.setString(6, refDocFormdata.getContentType());
 
 				// get BLOB_CONTENT
 				pstmt.execute();
 				pstmt = SQLHelper.closePreparedStatement(pstmt);
                 
 				// upload blob
 				doFiletoBlob(m_servlet.getConn(), dbfileName);
 
 			}
 			// Commit
 			m_servlet.getConn().commit();
 		} catch (SQLException e) {
 			logger.error(e.toString(), e);
 		}finally{
 			pstmt = SQLHelper.closePreparedStatement(pstmt);
 		
     }
     //forward the page
     doOpen();
 }
 /**
  * Return the the results page and display the results.
  * 
  */
 @SuppressWarnings("unchecked")
 public void doBack (){
 	Vector vResult = new Vector();
 	GetACSearch serAC = new GetACSearch(req, res, m_servlet);
 	String sACSearch = (String)session.getAttribute("searchAC");
 	
 	// Get results for different AC types from the AC search bean
 	if (sACSearch.equals("DataElement"))
 	    serAC.getDEResult(req, res, vResult, "");
 	  else if (sACSearch.equals("DataElementConcept"))
 	  	serAC.getDECResult(req, res, vResult, "");
 	  else if (sACSearch.equals("ValueDomain"))
 	  	serAC.getVDResult(req, res, vResult, "");
 	
 	// Set results in the session
 	DataManager.setAttribute(session, "results", vResult);
 	DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, msg); 
 	
 	// Forward back to search AC page
 	m_servlet.ForwardJSP(req, res, "/SearchResultsPage.jsp");
 	 
 	}
 
 /**
  * Deletes a row from reference blobs view based on the unique name
  *
  */
 public void doDeleteAttachment (){
     PreparedStatement pstmt = null;
 	String fileName = (String)req.getParameter("RefDocTargetFile");
 	String DelObjSQL = "delete from sbr.REFERENCE_BLOBS_VIEW where NAME = ?";
 	try {
 		pstmt = m_servlet.getConn().prepareStatement(DelObjSQL);
 		pstmt.setString(1, fileName);
 		pstmt.execute();
     } catch (SQLException e) {
 		logger.error(e.toString(), e);
 		msg = "Reference Document Attachment: Unable to delete the Attachment from the database.";
 	}finally{
 	  pstmt = SQLHelper.closePreparedStatement(pstmt);
     }
   // Forward back to the open method
 	doOpen();
   
 }
 
   /**
    * removes all the attachments for the selected RD called while removing RD.
    * @param rd_idseq
    * @return String of error message
    */
   public String doDeleteAllAttachments (String rd_idseq)
   {
     PreparedStatement pstmt = null;
     msg = "";
     String DelObjSQL = "delete from sbr.REFERENCE_BLOBS_VIEW where RD_IDSEQ = ?";
      try {
       pstmt = m_servlet.getConn().prepareStatement(DelObjSQL);
       pstmt.setString(1, rd_idseq);
       pstmt.execute();
     } catch (SQLException e) {
       logger.error("Error - doDeleteAllAttachments: " + e.toString(), e);
       msg = "Unable to delete all the Attachments from the database for the selected Reference Document.";
     }finally{
 	  pstmt = SQLHelper.closePreparedStatement(pstmt);
     }
     return msg;
   }
 
 
   /**
    * Streams a blob from the database to the filecache on the web server
    * 
    * @param bRefBlob - BLOB Object
    * @param fileName - The name of the file to save the object ro
    */
   private void doBlobtoFile (BLOB bRefBlob, String fileName){
   	GetACService getAC = new GetACService(req, res, m_servlet);
   	Vector<TOOL_OPTION_Bean> vList = new Vector<TOOL_OPTION_Bean>();
       vList = getAC.getToolOptionData("CURATION","REFDOC_FILECACHE","");
       String RefDocFileCache = vList.get(0).getVALUE();
   
   	String fileName2[] = fileName.split("/");
   	String fileDirectory = "";
   	
   	// Extract file to file system
   	//BLOB bRefBlob = (BLOB)rs.getBlob(2);
   	InputStream is;
 
 		try {
 			is = bRefBlob.getBinaryStream();
 			
 			String strArray[] = fileName.split("/");
 			if (strArray.length > 1){
 				fileName = strArray[1];
 				fileDirectory = strArray[0];
 				File fileLocation = new File(RefDocFileCache + fileDirectory);
 				if (!fileLocation.isDirectory()){
 					if (!fileLocation.mkdir()) {
 						logger.error("Cannot create directory: " + RefDocFileCache + fileDirectory);
 						fileDirectory = "";
 					}
 					else
 					{
 						fileDirectory = fileDirectory + "/";
 					}
 				}
 				else {
 					fileDirectory = fileDirectory + "/";
 				}
 			}
 			
 			// Sentinel report directory
 			OutputStream os = new FileOutputStream(RefDocFileCache + fileDirectory + fileName);
 
 			final int BUFSIZ = 4096;
 			byte inbuf[]= new byte [BUFSIZ];
 			int bytesRead;
 			
 				while ((bytesRead = is.read(inbuf, 0, BUFSIZ)) != -1){
 					os.write(inbuf, 0, bytesRead);
 				}
 			os.close();
 			os = null;
 			is.close();
 			is = null;
 		} catch (FileNotFoundException e) {
 			logger.error("ERROR - RefDocAttachment: " + e.getMessage());
 			msg = "Unable to save file from database.";
 		} catch (SQLException se) {
 			logger.error("ERROR - RefDocAttachment: " + se.getMessage());
 			msg = "Unable to access the database to download file data.";
 		} catch (IOException ioe) {
 			logger.error("ERROR - RefDocAttachment: " + ioe.getMessage());
 			msg = "Problem accessing file.";
 		}
   }
 
 /**
  * Streams a file from the web server filechace directory to the empty blob in the reference blobs view
  * 
  * @param con  db conenction used to create the attachment row
  * @param fileName  Unique name for the attachment row created
  */
   private void doFiletoBlob (Connection con, String fileName){
   	
   	GetACService getAC = new GetACService(req, res, m_servlet);
   	Vector<TOOL_OPTION_Bean> vList = new Vector<TOOL_OPTION_Bean>();
       vList = getAC.getToolOptionData("CURATION","REFDOC_FILECACHE","");
       String RefDocFileCache = vList.get(0).getVALUE();
       
   	String UpdateObjSQL = "select blob_content from sbr.reference_blobs_view where name = ? for update";
   	OutputStream os;
   	FileInputStream is;
     PreparedStatement pstmt = null;
     ResultSet rs = null;
   	try {
   		pstmt = con.prepareStatement(UpdateObjSQL);
   		
   		String[] fileNameString = fileName.split("/");
   		String fileLocator = RefDocFileCache + fileNameString[1];
   		is = new FileInputStream( fileLocator );
   
   		
   		pstmt.setString(1, fileName);
   		rs = pstmt.executeQuery();
   		rs.next();
   
   		BLOB blob = (BLOB)rs.getBlob("blob_content");
   
   		os = blob.getBinaryOutputStream();
   		//	Read the file by chuncks and insert them in the Blob. The chunk size come from the blob
   		byte[] chunk = new byte[blob.getChunkSize()];
   		int i=-1;
   		while((i = is.read(chunk))!=-1)
   		{
   			os.write(chunk,0,i); //Write the chunk
   		}
   	  is.close();
       is = null;
   	  os.close();
       os = null;
   } catch (FileNotFoundException e) {
   		logger.error("ERROR - RefDocAttachment: " + e.getMessage());
   		msg = "File not found. Can not upload file to database.";
   	} catch (SQLException e) {
   		logger.error("ERROR - RefDocAttachment: " + e.getMessage());
   		msg = "Unable to access database for file upload.";
   	} catch (IOException e) {
   		logger.error("ERROR - RefDocAttachment: " + e.getMessage());
   		msg = "Unable to access file for upload to the database.";
   	}finally{
   		rs = SQLHelper.closeResultSet(rs);
         pstmt = SQLHelper.closePreparedStatement(pstmt);
     }
   }
 
 
 } // End of Class
