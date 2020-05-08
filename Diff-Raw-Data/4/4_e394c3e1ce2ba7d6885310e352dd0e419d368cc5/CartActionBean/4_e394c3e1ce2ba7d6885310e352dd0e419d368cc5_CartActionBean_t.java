 package gov.nih.nci.evs.browser.bean;
 
 import gov.nih.nci.evs.browser.utils.RemoteServerUtil;
 import gov.nih.nci.evs.browser.utils.DataUtils;
 import gov.nih.nci.evs.browser.utils.SearchCart;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.LexGrid.LexBIG.DataModel.Collections.AbsoluteCodingSchemeVersionReferenceList;
 import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
 import org.LexGrid.LexBIG.Utility.Constructors;
 
 import javax.faces.component.html.HtmlSelectBooleanCheckbox;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletOutputStream;
 import javax.faces.model.SelectItem;
 
 import org.LexGrid.concepts.Entity;
 
 import org.apache.log4j.Logger;
 import org.LexGrid.naming.Mappings;
 import org.LexGrid.naming.SupportedCodingScheme;
 import org.LexGrid.naming.SupportedNamespace;
 import org.LexGrid.valueSets.DefinitionEntry;
 import org.LexGrid.valueSets.EntityReference;
 import org.LexGrid.valueSets.ValueSetDefinition; 
 import org.LexGrid.valueSets.ValueSetDefinitionReference;
 import org.LexGrid.valueSets.types.DefinitionOperator;
 import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * Action bean for cart operations
  *
  * @author garciawa2
  */
 public class CartActionBean {
 
     // Local class variables
     private static Logger _logger = Logger.getLogger(CartActionBean.class);
     private String _entity = null;
     private String _codingScheme = null;
     private HashMap<String, Concept> _cart = null;
     private String _version = null;
     private String _backurl = null;
     private boolean _messageflag = false;
     private String _message = null;
     private List<SelectItem> _selectVersionItems = null; 
     private List<String> _selectedVersionItems = null;
 
     // Local constants
     static public final String XML_FILE_NAME = "cart.xml";
     static public final String XML_CONTENT_TYPE = "text/xml";
     static public final String CSV_FILE_NAME = "cart.csv";
     static public final String CSV_CONTENT_TYPE = "text/csv";
 
     // Error messages
     
     static public final String NO_CONCEPTS = "No concepts in cart.";
     static public final String NOTHING_SELECTED = "No concepts selected.";
     static public final String EXPORT_COMPLETE = "Export completed.";
     static private final String PROD_VERSION = "[Production]";
     
     // Getters & Setters
 
     /**
      * Set name of parameter to use to acquire the code parameter
      * @param codename
      */
     public void setEntity(String entity) {
         this._entity = entity;
     }
 
     /**
      * Set name of parameter to use to acquire the coding scheme parameter
      * @param codingScheme
      */
     public void setCodingScheme(String codingScheme) {
         this._codingScheme = codingScheme;
     }
 
     /**
      * Set name of parameter to use to acquire the coding scheme version parameter
      * @param codingScheme
      */
     public void setVersion(String version) {
         this._version = version;
     }
     
     /**
      * Return number of items in cart
      * @return
      */
     public int getCount() {
         if (_cart == null) return 0;
         return _cart.size();
     }
 
     /**
      * Return Popup message flag
      * @return
      */
     public boolean getMessageflag() {
     	return _messageflag;
     }    
     
     /**
      * Return Popup message text
      * @return
      */
     public String getMessage() {
     	_messageflag = false;
     	return _message;
     }    
     
     /**
      * Compute a back to url that is not the cart page
      * @return
      */
     public String getBackurl() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         String targetPage = request.getHeader("Referer");
         targetPage = (targetPage == null) ? request.getContextPath() : targetPage;
         if (!targetPage.contains("cart.jsf")) _backurl = targetPage;
         return _backurl;
     }
 
     /**
      * Return the concept collection
      * @return
      */
     public Collection<Concept> getConcepts() {
         if (_cart == null) _init();
         return _cart.values();
     }
 
     public List<SelectItem> getSelectVersionItems() throws Exception {
         if (_selectVersionItems == null) _initDisplayItems();
         return _selectVersionItems;
     }
     
     public void setSelectVersionItems(List<SelectItem> selectVersionItems) {
     	this._selectVersionItems = selectVersionItems;
     }
     
     public List<String> getSelectedVersionItems() throws Exception {
     	
         if (_selectedVersionItems == null) _initDisplayItems();
         return _selectedVersionItems;
     }
     
     public void setSelectedVersionItems(List<String> selectedVersionItems) {
     	this._selectedVersionItems = selectedVersionItems;
     }    
     
     // ******************** Class methods ************************
     
     /**
      * Initialize the cart container
      */
 	private void _init() {
 		if (_cart == null)
 			_cart = new HashMap<String, Concept>();
 	}
 
 	private void _initDisplayItems() throws Exception {
 		SearchCart search = new SearchCart();
 		HashMap<String, SchemeVersion> versionList
 			= getSchemeVersionList(search);
 		
 		// Init scheme version scheme list		
 		initSelectVersionItems(versionList);
 		
 		// Init scheme version selected list
 		initSelectedVersionItems(versionList);		
 	}
 		
     /**
      * Add concept to the Cart
      * @return
      * @throws Exception
      */
     public String addToCart() throws Exception {
         String code = null;
         String codingScheme = null;
         String nameSpace = null;
         String name = null;
         String url = null;
         String version = null;
 
         _messageflag = false;
 
         // Get concept information from the Entity item passed in
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         // Get Entity object
         Entity curr_concept = (Entity) request.getSession().getAttribute(_entity);
         if (curr_concept == null) {
         	// Called from a non search area
         	_logger.error("*** Cart error: Entity object is null!");
         	return null;
         }
         code = curr_concept.getEntityCode(); // Store identifier
 
         // Get coding scheme
         codingScheme = (String)request.getSession().getAttribute(_codingScheme);
         version = (String)request.getSession().getAttribute(_version);
 
         // Get concept name space
         nameSpace = curr_concept.getEntityCodeNamespace();
 
         // Get concept name
         name = curr_concept.getEntityDescription().getContent();
 
         // Get concept URL
         String protocol = request.getScheme();
         String domain = request.getServerName();
         String port = Integer.toString(request.getServerPort());
         if (port.equals("80")) port = ""; else port = ":" + port;
         String path = request.getContextPath();
         url = protocol + "://" + domain
             + port + path
             + "/pages/concept_details.jsf?dictionary=" + codingScheme
             + "&version=" + version
             + "&code=" + code;
 
         // Add concept to cart
         if (_cart == null) _init();
         Concept item = new Concept();
         item.setCode(code);
         item.setCodingScheme(codingScheme);
         item.setNameSpace(nameSpace);
         item.setName(name);
         item.setVersion(version);
         item.setUrl(url);
 
         if (!_cart.containsKey(code))
         	_cart.put(code,item);
 
         // Add scheme and version back in for redisplay
         request.setAttribute("dictionary", codingScheme);
         request.setAttribute("version", version);
         
         // Rebuild version selected lists
         _initDisplayItems();
         
 		return null;
     }
 
     /**
      * Remove concept(s) from the Cart
      * @return
      */
     public String removeFromCart() {
     	_messageflag = false;
     	
     	if (getCount() < 1) {
         	_messageflag = true;
         	_message = NO_CONCEPTS;    		
     	} else if (!hasSelected()) {
         	_messageflag = true;
         	_message = NOTHING_SELECTED;        	
     	} else {    	
             for (Iterator<Concept> i = getConcepts().iterator(); i.hasNext();) {
                 Concept item = (Concept)i.next();
                 if (item.getCheckbox().isSelected()) {
                     if (_cart.containsKey(item.code))
                         i.remove();
                 }
             }
     	}
 	        
         return "showcart";
     }
 
     /*
      * Unselect all concept(s) in the Cart
      * @return
      */
     public String selectAllInCart() {
         _messageflag = false;
         
     	if (getCount() < 1) {
         	_messageflag = true;
         	_message = NO_CONCEPTS;    	 
     	} else {
             for (Iterator<Concept> i = getConcepts().iterator(); i.hasNext();) {
                 Concept item = (Concept)i.next();                
                 item.setSelected(true);
             }
         }
         return null;
     }    
 
     /**
      * Unselect all concept(s) in the Cart
      * @return
      */
     public String unselectAllInCart() {
         _messageflag = false;
         
     	if (getCount() < 1) {
         	_messageflag = true;
         	_message = NO_CONCEPTS;    		
     	} else if (!hasSelected()) {
         	_messageflag = true;
         	_message = NOTHING_SELECTED;        	
     	} else {
             for (Iterator<Concept> i = getConcepts().iterator(); i.hasNext();) {
                 Concept item = (Concept)i.next();
                 item.setSelected(false);
             }
         }
         return null;
     }    
 
     /**
      * Export cart in XML format
      * @return
      * @throws Exception
      */
     public String exportCartXML() throws Exception {
 
         SearchCart search = new SearchCart();
         ResolvedConceptReference ref = null;
         HashMap<String, SchemeVersion> versionList = null;
 
     	if (getCount() < 1) {
         	_messageflag = true;
         	_message = NO_CONCEPTS;
         	return null;
     	} 
     	if (!hasSelected()) {
         	_messageflag = true;
         	_message = NOTHING_SELECTED;        
         	return null;
     	}  	
 
         // Get Entities to be exported and build export XML string
         // in memory
 
         if (_cart != null && _cart.size() > 0) {
 
         	// Generate unique list of scheme / versions for the cart        	
         	versionList = getSchemeVersionList(search);
 
         	// Setup lexbig service
     		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil
 				.getLexEVSValueSetDefinitionServices();
 
             //instantiate the mappings
             Mappings maps = new Mappings();    		
     		
     		// Instantiate VSD
     		ValueSetDefinition vsd = new ValueSetDefinition();
     		vsd.setMappings(maps);
 			vsd.setValueSetDefinitionURI("EXPORT:VSDREF_CART");
 			vsd.setValueSetDefinitionName("VSDREF_CART");
 			vsd.setConceptDomain("Concepts");
 			
 			// Add supported coding schemes
 			DuplicateCheck dc = new DuplicateCheck();
 			for (Iterator<Entry<String, SchemeVersion>> i = versionList
 					.entrySet().iterator(); i.hasNext();) {
 				Entry<String, SchemeVersion> x = i.next();
 				SchemeVersion sv = x.getValue();
 				SupportedCodingScheme scs = new SupportedCodingScheme();
 				scs.setLocalId(sv.codingScheme);
 				scs.setUri(sv.uri);
 				if (dc.test(sv.codingScheme)) {
 					maps.addSupportedCodingScheme(scs);
 					_logger.debug("Adding CS: "
 							+ sv.codingScheme + " ("
 							+ sv.uri + ")");
 				}
 			}		
             
             // Add supported name spaces
 			dc.reset();
 			for (Iterator<Entry<String, SchemeVersion>> i = versionList
 					.entrySet().iterator(); i.hasNext();) {
 				Entry<String, SchemeVersion> x = i.next();
 				SchemeVersion sv = x.getValue();				
             	SupportedNamespace sns = new SupportedNamespace();	
             	sns.setLocalId(sv.namespace);
             	sns.setEquivalentCodingScheme(sv.codingScheme);
             	if (dc.test(sv.namespace)) {
             		maps.addSupportedNamespace(sns);
             		_logger.debug("Adding NS: " + sv.namespace);
             	}	
             }    
    
 			// Instantiate DefinitionEntry(Rule Set)
 			DefinitionEntry de = new DefinitionEntry();
 			de.setRuleOrder(1L);
 			de.setOperator(DefinitionOperator.OR);
 		
 			// Instantiate ValueSetDefinitionReference 
 			ValueSetDefinitionReference vsdRef = new ValueSetDefinitionReference();
 			vsdRef.setValueSetDefinitionURI("EXPORT:CART_NODES");
 			de.setValueSetDefinitionReference(vsdRef);
 			vsd.addDefinitionEntry(de);			
 			
 			// Add terms from the cart - DefinitionEntry(Rule Set)
 			
 			DefinitionEntry deSub = null;
 			long ruleOrder = 2;
             for (Iterator<Concept> i = getConcepts().iterator(); i.hasNext();) {
                 Concept item = (Concept) i.next();
                 ref = search.getConceptByCode(item.codingScheme, item.version, item.code);
                 
                 if (item.getSelected() && ref != null) {
 					_logger.debug("Exporting: " + ref.getCode() + "("
 							+ item.codingScheme + ":" + item.version + ")");
 
 					deSub = new DefinitionEntry();
 					deSub.setRuleOrder(ruleOrder); ruleOrder++;
 					deSub.setOperator(DefinitionOperator.OR);   					
 					
             		// Instantiate EntityReference
             		EntityReference entityRef = new EntityReference();					
                     String entityCode = ref.getEntity().getEntityCode();
                     String entityNameSpace = ref.getCodeNamespace();                     
             		 
             		// set appropriate values for entityReference
             		entityRef.setEntityCode(entityCode);
             		entityRef.setEntityCodeNamespace(entityNameSpace);
             		entityRef.setLeafOnly(false);
             		entityRef.setTransitiveClosure(false);            		
             		deSub.setEntityReference(entityRef);
             		vsd.addDefinitionEntry(deSub);  
                 }
             }
             
 			// Add list of coding schemes version reference
 			AbsoluteCodingSchemeVersionReferenceList csvList = new AbsoluteCodingSchemeVersionReferenceList();
 			boolean flag;
 			for (Iterator<Entry<String, SchemeVersion>> i = versionList
 					.entrySet().iterator(); i.hasNext();) {
 				Entry<String, SchemeVersion> x = i.next();
 				SchemeVersion sv = x.getValue();	
 				
 				// Add only selected scheme versions
 				flag = false;
 				if (inSelectedlist(sv.uri)) {
 					if (inSelectedlist(sv.uri,sv.version))
 						flag = true;
 				} else {
 					flag = true;
 				}
 		
 				if (flag) {						
 					csvList.addAbsoluteCodingSchemeVersionReference(Constructors
 						.createAbsoluteCodingSchemeVersionReference(
 							sv.uri,
 							sv.version));
 					_logger.debug("Adding CS Ver Ref: "
 							+ sv.uri
 							+ " (" +  sv.version + ")");
 				}				
 			}
 
             // Build a buffer holding the XML data
     		
             StringBuffer buf = null;           
     		InputStream reader = vsd_service.exportValueSetResolution(vsd, null,
     			csvList, null, false);
     		
 			if (reader != null) {
 				buf = new StringBuffer();
 				try {
 					for (int c = reader.read(); c != -1; c = reader.read()) {
 						buf.append((char) c);
 					}	
 				} catch (IOException e) {
 					throw e;
 				} finally {
 					try {
 						reader.close();
 					} catch (Exception e) {
 						// ignored
 					}
 				}
 			} else {
 				buf = new StringBuffer("<error>exportValueSetResolution returned null.</error>");
 			}
             
             // Send export XML string to browser
 
             HttpServletResponse response = (HttpServletResponse) FacesContext
                     .getCurrentInstance().getExternalContext().getResponse();
             response.setContentType(XML_CONTENT_TYPE);
             response.setHeader("Content-Disposition", "attachment; filename="
                     + XML_FILE_NAME);
             response.setContentLength(buf.length());
             ServletOutputStream ouputStream = response.getOutputStream();
             ouputStream.write(buf.toString().getBytes(), 0, buf.length());
             ouputStream.flush();
             ouputStream.close();
 
             // Don't allow JSF to forward to cart.jsf
             FacesContext.getCurrentInstance().responseComplete();
         }
 
         return null;
     }
 
     /**
      * Export cart in Excel format
      * @return
      * @throws Exception
      */
     public String exportCartCSV() throws Exception {
 
         _messageflag = false;
         
         SearchCart search = new SearchCart();
         ResolvedConceptReference ref = null;
         StringBuffer sb = new StringBuffer();
         
     	if (getCount() < 1) {
         	_messageflag = true;
         	_message = NO_CONCEPTS;
         	return null;
     	} 
     	if (!hasSelected()) {
         	_messageflag = true;
         	_message = NOTHING_SELECTED;        
         	return null;
     	}         
         
         // Get Entities to be exported and build export file
         // in memory
 
         if (_cart != null && _cart.size() > 0) {
             // Add header
             sb.append("Concept,");
             sb.append("Vocabulary,");
            sb.append("Version Code,");
            sb.append("Concept Code,");
             sb.append("URL");
             sb.append("\r\n");
 
             // Add concepts
             for (Iterator<Concept> i = getConcepts().iterator(); i.hasNext();) {
                 Concept item = (Concept)i.next();
                 ref = search.getConceptByCode(item.codingScheme, item.version, item.code);
                 if (ref != null) {
                     _logger.debug("Exporting: " + ref.getCode());
                     sb.append("\"" + clean(item.name) + "\",");
                     sb.append("\"" + clean(item.codingScheme) + "\",");
                     sb.append("\"" + clean(item.version) + "\",");
                     sb.append("\"" + clean(item.code) + "\",");
                     sb.append("\"" + clean(item.url) + "\"");
                     sb.append("\r\n");
                 }
             }
 
             // Send export file to browser
 
             HttpServletResponse response = (HttpServletResponse) FacesContext
                     .getCurrentInstance().getExternalContext().getResponse();
             response.setContentType(CSV_CONTENT_TYPE);
             response.setHeader("Content-Disposition", "attachment; filename="
                     + CSV_FILE_NAME);
             response.setContentLength(sb.length());
             ServletOutputStream ouputStream = response.getOutputStream();
             ouputStream.write(sb.toString().getBytes(), 0, sb.length());
             ouputStream.flush();
             ouputStream.close();
 
             // Don't allow JSF to forward to cart.jsf
             FacesContext.getCurrentInstance().responseComplete();
         }
 
 		return null;
     }
 
     /**
      * Subclass to hold contents of the cart
      * @author garciawa2
      */
     public class Concept {
         private String code = null;
         private String codingScheme = null;
         private String nameSpace = null;
         private String name = null;
         private String version = null;
         private String url = null;
         private String displayStatus = "";
         private String displayCodingSchemeName = "[Not Set]";
         private HtmlSelectBooleanCheckbox checkbox = null;
 
         // Getters & setters
 
         public String getCode() {
             return this.code;
         }
 
         public void setCode(String code) {
             this.code = code;
             initDisplayStatus();
         }
 
         public String getCodingScheme() {
             return this.codingScheme;
         }
 
         public String getDisplayCodingSchemeName() {
             return this.displayCodingSchemeName;
         }
         
         public void setCodingScheme(String codingScheme) {
             this.codingScheme = codingScheme;
             initDisplayStatus();
             initDisplayCodingSchemeName();
         }
         
         public String getNameSpace() {
             return this.nameSpace;
         }
 
         public void setNameSpace(String namespace) {
             this.nameSpace = namespace;
         }
 
         public String getName() {
             return this.name;
         }
 
         public String getDisplayStatus() {
             return this.displayStatus;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public String getVersion() {
             return this.version;
         }
 
         public void setVersion(String version) {
             this.version = version;
             initDisplayStatus();
             initDisplayCodingSchemeName();
         }
 
         public String getUrl() {
             return this.url;
         }
 
         public void setUrl(String url) {
             this.url = url;
         }
         
         public String getKey() {
         	return code + " (" + displayCodingSchemeName + " " + version + ")"; 
         }
         
         public HtmlSelectBooleanCheckbox getCheckbox() {
          	if (checkbox == null) checkbox = new HtmlSelectBooleanCheckbox();
             return checkbox;
         }
         
         public void setCheckbox(HtmlSelectBooleanCheckbox checkbox) {
             this.checkbox = checkbox;
         }
         
         // *** Private Methods ***
         
         private void setSelected(boolean selected) {
         	this.checkbox.setSelected(selected);
         }
         
         private boolean getSelected() {
         	return this.checkbox.isSelected();
         }    
 
         private void initDisplayStatus() {
             if (this.codingScheme == null || this.version == null || this.code == null)
                 return;
             
             String status = DataUtils.getConceptStatus(
                 this.codingScheme, this.version, null, this.code);
             if (status == null || status.length() <= 0)
                 return;
             status = status.replaceAll("_", " ");
             displayStatus = "(" + status + ")";
         }
 
         private void initDisplayCodingSchemeName() {
             if (this.codingScheme == null || this.version == null)
                 return;
             displayCodingSchemeName = DataUtils.getMetadataValue(
                 this.codingScheme, this.version, "display_name");
             if (displayCodingSchemeName == null)
                 displayCodingSchemeName = 
                     DataUtils.getLocalName(this.codingScheme);
         }
 
     } // End of Concept
 
     //**
     //* Utility methods
     //**
 
     /**
      * Class to hold a unique scheme version
      * @author garciaw     
      */
     public class SchemeVersion {
         private String uri = null;
         private String codingScheme = null;
         private String version = null; 
         private String namespace = null;
         private boolean mult = false;
         
         // Getters & setters
         
         public String getDisplayCodingSchemeName() {
             return this.codingScheme + " (" + this.version + ")";
         }        
     }    
 
 	/**
 	 * Create a display list of versions
 	 * @param versionList
 	 */
 	private void initSelectVersionItems(HashMap<String, SchemeVersion> versionList) {
 		_selectVersionItems = new ArrayList<SelectItem>();
 		for (Iterator<Entry<String, SchemeVersion>> i = versionList.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<String, SchemeVersion> x = i.next();
 			SchemeVersion vs = x.getValue();
 			if (vs.mult) {
 				_selectVersionItems.add(new SelectItem(
 					vs.uri + "|" + vs.version,
 					vs.getDisplayCodingSchemeName()));
 			}	
 		}
 		if (_selectVersionItems.size() < 1) {
 			_selectVersionItems.add(new SelectItem(
 				PROD_VERSION,
 				PROD_VERSION));
 		}		
 	}
 	
 	/**
 	 * Create a display list of 'selected' version display items
 	 * @param versionList
 	 */
 	private void initSelectedVersionItems(HashMap<String, SchemeVersion> versionList) {
 		_selectedVersionItems = new ArrayList<String>();
 		
 		for (Iterator<Entry<String, SchemeVersion>> i = versionList.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<String, SchemeVersion> x = i.next();
 			SchemeVersion vs = x.getValue();
 			if (vs.mult && inSelectedlist(vs.uri))
 				_selectedVersionItems.add(vs.uri + "|" + vs.version);
 		}
 	}	
 	
 	private boolean inSelectedlist(String uri) {
 		for (int x=0;x<_selectedVersionItems.size();x++) {
 			if (_selectedVersionItems.get(x).contains(uri)) return true;
 		}
 		return false;
 	}	
 	
 	private boolean inSelectedlist(String uri, String version) {
 		for (int x = 0; x < _selectedVersionItems.size(); x++) {
 			if (_selectedVersionItems.get(x).contains(uri + "|" + version))
 				return true;
 		}
 		return false;
 	}		
 
 	public String dumpSelectedlist() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("Listing selected versions...\n");
 		for (int x = 0; x < _selectedVersionItems.size(); x++) {
 			sb.append("\t    URI = " + _selectedVersionItems.get(x));	
 		}
 		return sb.toString();
 	}	
 
 	/**
 	 * Return a unique coding scheme map with versions for concepts
 	 *  in the cart
 	 * @param search
 	 * @return
 	 * @throws Exception 
 	 */
 	private HashMap<String, SchemeVersion> getSchemeVersionList(
 			SearchCart search) throws Exception {
 		HashMap<String, SchemeVersion> map = new HashMap<String, SchemeVersion>();
 		ResolvedConceptReference ref = null;
 
 		for (Iterator<Concept> i = getConcepts().iterator(); i.hasNext();) {
 			Concept item = (Concept) i.next();
 			ref = search.getConceptByCode(item.codingScheme, item.version,
 					item.code);
 			ArrayList<String> list = search.getSchemeVersions(ref.getCodingSchemeURI()); 
 			for(int x=0;x<list.size();x++) {
 				SchemeVersion vs = new SchemeVersion();
 				vs.uri = ref.getCodingSchemeURI();
 				vs.codingScheme = ref.getCodingSchemeName();
 				vs.version = list.get(x);
 				vs.namespace = ref.getCodeNamespace();
 				map.put(item.codingScheme + "|" + vs.version, vs);
 			}
 		}
 	
 		// Set 'has other version' flags 
 		for (Iterator<Entry<String, SchemeVersion>> i = map.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<String, SchemeVersion> x = i.next();
 			SchemeVersion vs = x.getValue();
 			vs.mult = schemeHasOtherVersion(map,vs.uri,vs.version);			
 		}		
 		
 		return map;
 	}
     
 	/**
 	 * Test if map contains other versions of a given scheme
 	 * @param map
 	 * @param uri
 	 * @param version
 	 * @return
 	 */
 	private boolean schemeHasOtherVersion(HashMap<String, SchemeVersion> map,
 			String uri, String version) {
 		for (Iterator<Entry<String, SchemeVersion>> i = map.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<String, SchemeVersion> x = i.next();
 			SchemeVersion vs = x.getValue();
 			if (vs.uri.equals(uri) && !vs.version.equals(version))
 				return true;
 		}
 		return false;
 	}
 	
     /**
      * Test any concept in the cart has been selected
      * @return
      */
     private boolean hasSelected() {
         if (_cart != null && _cart.size() > 0) {
             for (Iterator<Concept> i = getConcepts().iterator(); i.hasNext();) {
                 Concept item = (Concept)i.next();
                 if (item.getSelected()) return true;
             }
         }
         return false;
     }    
     
     /**
      * Dump contents of cart object
      * (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("Listing cart contents...\n");
 
         if (_cart != null && _cart.size() > 0) {
             sb.append("\tCart:\n");
             for (Iterator<Concept> i = getConcepts().iterator(); i.hasNext();) {
                 Concept item = (Concept)i.next();
                 sb.append("\t         Code = " + item.code + "\n");
                 sb.append("\tCoding scheme = " + item.codingScheme + "\n");
                 sb.append("\t      Version = " + item.version + "\n");
                 sb.append("\t   Name space = " + item.nameSpace + "\n");
                 sb.append("\t         Name = " + item.name + "\n");
                 sb.append("\t     Selected = " + item.getSelected() + "\n");
                 sb.append("\t          URL = " + item.url + "\n");
             }
         } else {
             sb.append("Cart is empty.");
         }
 
         return sb.toString();
     }
 
     
     /**
      * Dump scheme version map
      * @param map
      * @return
      */
     public String versionMapToString(HashMap<String, SchemeVersion> map) {
         StringBuffer sb = new StringBuffer();
         sb.append("Listing scheme versions...\n");
         
 		for (Iterator<Entry<String, SchemeVersion>> i = map.entrySet()
 				.iterator(); i.hasNext();) {	
 			Entry<String, SchemeVersion> x = i.next();
 			SchemeVersion vs = x.getValue();
 			sb.append("\t--------------------------------\n");
 			sb.append("\t          Key = " + x.getKey() + "\n");
 			sb.append("\tCoding Scheme = " + vs.codingScheme + "\n");
 			sb.append("\t          URI = " + vs.uri + "\n");
 			sb.append("\t      Version = " + vs.version + "\n");
 			sb.append("\t Multiple Ver = " + vs.mult + "\n");
 		}
 		return sb.toString();
     }    
     
     /**
      * Clean a string for use in file type CSV
      * @param str
      * @return
      */
     private String clean(String str) {
         String tmpStr = str.replace('"', ' ');
         return tmpStr;
     }
 
     /**
      * Utility class that helps check for duplicate entries
      * @author garciaw
      */
     public class DuplicateCheck {
     	
     	private ArrayList<String> list = null;
     	
     	/**
     	 * Constructors
     	 */
     	public DuplicateCheck() {
 			list = new ArrayList<String>();
 		}
 
     	/**
     	 * Add a key and check it it is already in the list
     	 * @param key
     	 * @return
     	 */
     	public boolean test(String key) {
     		if (list.contains(key)) return false;
     		list.add(key);
     		return true;
     	} 
     	
     	/**
     	 * Clear contents of duplicate check list
     	 */
     	public void reset() {
     		list.clear();
     	}
     	
     } // End of DuplicateCheck   
         
 } // End of CartActionBean
