 /**
  *  
  *   The caMOD Software License, Version 1.0
  *
  *   Copyright 2005-2006 SAIC. This software was developed in conjunction with the National Cancer
  *   Institute, and so to the extent government employees are co-authors, any rights in such works
  *   shall be subject to Title 17 of the United States Code, section 105.
  *
  *   Redistribution and use in source and binary forms, with or without modification, are permitted
  *   provided that the following conditions are met:
  *
  *   1. Redistributions of source code must retain the above copyright notice, this list of conditions
  *   and the disclaimer of Article 3, below.  Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the following disclaimer in the documentation and/or
  *   other materials provided with the distribution.
  *
  *   2.  The end-user documentation included with the redistribution, if any, must include the
  *   following acknowledgment:
  *
  *   "This product includes software developed by the SAIC and the National Cancer
  *   Institute."
  *
  *   If no such end-user documentation is to be included, this acknowledgment shall appear in the
  *   software itself, wherever such third-party acknowledgments normally appear.
  *
  *   3. The names "The National Cancer Institute", "NCI" and "SAIC" must not be used to endorse or
  *   promote products derived from this software.
  *
  *   4. This license does not authorize the incorporation of this software into any third party proprietary
  *   programs.  This license does not authorize the recipient to use any trademarks owned by either
  *   NCI or SAIC.
  *
  *
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *   WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  *   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *   DISCLAIMED.  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR
  *   THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  *   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  *   OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  *   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * $Id$
  *
  * $Log$
  * Revision 1.1  2006/05/12 12:49:49  guptaa
  * Initial revision
  *
  * 
  */
 package gov.nih.nci.camod.webapp.servlet;
 
 
 import gov.nih.nci.camod.util.AjaxTagValuePair;
 import gov.nih.nci.camod.util.AutocompleteUtil;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.SortedSet;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.ajaxtags.helpers.AjaxXmlBuilder;
 import org.ajaxtags.servlets.BaseAjaxServlet;
 
 public class AutocompleteServlet extends BaseAjaxServlet {
 
 	/**
 	 * @see org.ajaxtags.demo.servlet.BaseAjaxServlet#getXmlContent(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	private static int ajaxIntNumber;
 
 	static
 	  {
 		InputStream in = null;
 		Properties sysProps = new Properties();
 		try {
 			in = Thread.currentThread().getContextClassLoader()
 					.getResourceAsStream("camod.properties");
 			sysProps.load(in);
 			String ajaxNumber = sysProps.getProperty("ajax.number");
 			ajaxIntNumber =  Integer.parseInt(ajaxNumber);
 		} catch (Exception e) {
 			System.err.println("Error loading system.properties file");
 			e.printStackTrace();
 		}
 	  }
 
 	public String getXmlContent(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 
 		String model = request.getParameter("modelDescriptor");
 		String geneName = request.getParameter("geneName");
 		String therapeuticApproach = request.getParameter("therapeuticApproach");
 		String NSCNumber = request.getParameter("NSCNumber");
 		String organ = request.getParameter("organ");
 		String tumorClassification = request.getParameter("tumorClassification");
 				
 		List theGenericDescriptorList = null;
 		
 		if (model != null&&  model.trim().length()>0) {
 			model = model.trim().toLowerCase();
 			theGenericDescriptorList = getMatchingModelDescriptor(model,ajaxIntNumber);
 		} 
 		else if (geneName != null&&  geneName.trim().length()>0) {
 			geneName = geneName.trim().toLowerCase();
 			theGenericDescriptorList = getGeneName(geneName,ajaxIntNumber);
 		}
 		else if (therapeuticApproach!= null&&  therapeuticApproach.trim().length()>0) {
 			therapeuticApproach = therapeuticApproach.trim().toLowerCase();	
 			theGenericDescriptorList = getTherapeuticApproach(therapeuticApproach,ajaxIntNumber);
 		}
 		else if  (NSCNumber != null&&  NSCNumber.trim().length()>0) {
 			NSCNumber = NSCNumber.trim().toLowerCase();
 			theGenericDescriptorList = getNSCNumber(NSCNumber,ajaxIntNumber);
 		}
 		else if (organ != null&&  organ.trim().length()>0) {
 			organ = organ.trim().toLowerCase();
 			theGenericDescriptorList = getOrgan(organ,ajaxIntNumber);
 		}
 		else if (tumorClassification!= null&&  tumorClassification.trim().length()>0) {
 			tumorClassification = tumorClassification.trim().toLowerCase();
 			theGenericDescriptorList = getTumorClassification(tumorClassification,ajaxIntNumber);
 		}
 		
 		String theXmlResponse = new AjaxXmlBuilder().addItems(
 				theGenericDescriptorList, "source", "target").toString();
 	
 		return theXmlResponse;
 	}
 	//for model descriptor
 	public List getMatchingModelDescriptor(String model, int inNumToReturn) throws Exception {
 		
 		List list = new ArrayList();
 		
 		SortedSet<String> theModelMatchingName = AutocompleteUtil.getMatchingAnimalModelNames(model, inNumToReturn);
 
 		List theModelNameList = new ArrayList();
 		for (String theModelName : theModelMatchingName) {
 			
 			AjaxTagValuePair atvp = new AjaxTagValuePair();
 				atvp.setTarget(theModelName);
 				atvp.setSource(theModelName );
 				theModelNameList.add(atvp);
 
 		}
 		return theModelNameList;
 
 	}
 	//for gene name
 	public List getGeneName(String geneName,int inNumToReturn) throws Exception {
 
 		List list = new ArrayList();
 		
 		SortedSet<String> theGeneMatchingName = AutocompleteUtil.getMatchingGeneNames(geneName, inNumToReturn);
 		
 		List theGeneNameList = new ArrayList();
 		for (String theGeneName : theGeneMatchingName) {
 			
 			AjaxTagValuePair atvp = new AjaxTagValuePair();
 				atvp.setTarget(theGeneName );
 				atvp.setSource(theGeneName );
 				theGeneNameList.add(atvp);
 
 		}
 		return theGeneNameList;
 
 	}
 	//for therapeutic approach
 	public List getTherapeuticApproach(String therapeuticApproach,int inNumToReturn) throws Exception {
 
 		List list = new ArrayList();
 		
 		SortedSet<String> theTherapeuticApproachMatchingName = AutocompleteUtil.getMatchingGeneNames(therapeuticApproach, inNumToReturn);
 		
 		List theTherapeuticApproachList = new ArrayList();
 		for (String theTherapeuticApproachName : theTherapeuticApproachMatchingName) {
 			
 			AjaxTagValuePair atvp = new AjaxTagValuePair();
 				atvp.setTarget(theTherapeuticApproachName );
 				atvp.setSource(theTherapeuticApproachName);
 				theTherapeuticApproachList.add(atvp);
 
 		}
 		return theTherapeuticApproachList;
 	}
 	//for NSCNumber
 	public List getNSCNumber(String NSCnumber,int inNumToReturn) throws Exception {
 		
 		List list = new ArrayList();
 		
		SortedSet<String> theNSCNumberMatchingName = AutocompleteUtil.getMatchingGeneNames(NSCnumber, inNumToReturn);
 		
 		List theNSCNumberList = new ArrayList();
 		for (String theNSCNumberName : theNSCNumberMatchingName) {
 			
 			AjaxTagValuePair atvp = new AjaxTagValuePair();
 				atvp.setTarget(theNSCNumberName);
 				atvp.setSource(theNSCNumberName);
 				theNSCNumberList.add(atvp);
 
 		}
 
 		
 		return theNSCNumberList;
 
 	}
 	public List getOrgan(String Organ,int inNumToReturn) throws Exception {
 		
 		List list = new ArrayList();
 		
 		SortedSet<String> theNSCNumberMatchingName = AutocompleteUtil.getMatchingGeneNames(Organ, inNumToReturn);
 		
 		List theOrganList = new ArrayList();
 		for (String theNSCNumberName : theNSCNumberMatchingName) {
 			
 			AjaxTagValuePair atvp = new AjaxTagValuePair();
 				atvp.setTarget(theNSCNumberName);
 				atvp.setSource(theNSCNumberName);
 				theOrganList.add(atvp);
 
 		}
 
 		
 		return theOrganList;
 
 	}
 	public List getTumorClassification(String tumorClassification,int inNumToReturn) throws Exception {
 		
 		List list = new ArrayList();
 		
 		SortedSet<String> theNSCNumberMatchingName = AutocompleteUtil.getMatchingGeneNames(tumorClassification, inNumToReturn);
 		
 		List theTumorClassificationList = new ArrayList();
 		for (String theNSCNumberName : theNSCNumberMatchingName) {
 			
 			AjaxTagValuePair atvp = new AjaxTagValuePair();
 				atvp.setTarget(theNSCNumberName);
 				atvp.setSource(theNSCNumberName);
 				theTumorClassificationList.add(atvp);
 
 		}
 
 		
 		return theTumorClassificationList;
 
 	}
 }
