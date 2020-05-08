 package gov.nih.nci.evs.browser.utils;
 
 import java.io.*;
 import java.net.URI;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import java.text.*;
 import java.util.*;
 import java.sql.*;
 import javax.faces.model.*;
 
 import org.LexGrid.LexBIG.DataModel.Collections.*;
 import org.LexGrid.LexBIG.DataModel.Core.*;
 import org.LexGrid.LexBIG.Exceptions.*;
 import org.LexGrid.LexBIG.History.*;
 import org.LexGrid.LexBIG.LexBIGService.*;
 import org.LexGrid.LexBIG.Utility.*;
 import org.LexGrid.concepts.*;
 import org.LexGrid.LexBIG.DataModel.InterfaceElements.*;
 import org.LexGrid.LexBIG.Utility.Iterators.*;
 import org.LexGrid.codingSchemes.*;
 import org.LexGrid.commonTypes.*;
 import org.LexGrid.relations.Relations;
 import org.LexGrid.versions.*;
 import org.LexGrid.naming.*;
 import org.LexGrid.LexBIG.DataModel.Core.types.*;
 import org.LexGrid.LexBIG.Extensions.Generic.*;
 
 import gov.nih.nci.evs.browser.properties.*;
 import static gov.nih.nci.evs.browser.common.Constants.*;
 
 import gov.nih.nci.evs.browser.common.Constants;
 import gov.nih.nci.evs.browser.bean.MappingData;
 
 import org.LexGrid.LexBIG.Extensions.Generic.MappingExtension;
 import org.LexGrid.LexBIG.Extensions.Generic.MappingExtension.Direction;
 import org.LexGrid.LexBIG.Extensions.Generic.MappingExtension.MappingSortOption;
 import org.LexGrid.LexBIG.Extensions.Generic.MappingExtension.MappingSortOptionName;
 import org.LexGrid.LexBIG.Extensions.Generic.MappingExtension.QualifierSortOption;
 import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
 import org.LexGrid.LexBIG.caCore.interfaces.LexEVSDistributed;
 
 //import org.LexGrid.LexBIG.Utility.ServiceUtility;
 import org.LexGrid.LexBIG.Extensions.Generic.SupplementExtension;
 import org.LexGrid.relations.AssociationPredicate;
 
 
 import org.LexGrid.LexBIG.caCore.interfaces.LexEVSDistributed;
 import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
 
 import org.LexGrid.valueSets.ValueSetDefinition;
 import org.LexGrid.commonTypes.Source;
 
 
 import org.apache.log4j.*;
 import org.LexGrid.LexBIG.Extensions.Generic.MappingExtension.Mapping;
 import org.LexGrid.LexBIG.Extensions.Generic.MappingExtension.Mapping.SearchContext;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
 import org.LexGrid.LexBIG.caCore.interfaces.LexEVSApplicationService;
 import org.lexevs.property.PropertyExtension;
 
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
  * @author EVS Team
  * @version 1.0
  *
  *          Modification history Initial implementation kim.ong@ngc.com
  *
  */
 public class DataUtils {
     private static Logger _logger = Logger.getLogger(DataUtils.class);
     //private static LocalNameList _noopList = Constructors.createLocalNameList("_noop_");
     private static LocalNameList _noopList = new LocalNameList();
     private int _maxReturn = 5000;
     private Connection _con;
     private Statement _stmt;
     private ResultSet _rs;
 
     private static List _ontologies = null;
 
     private static org.LexGrid.LexBIG.LexBIGService.LexBIGService _lbSvc = null;
     public org.LexGrid.LexBIG.Utility.ConvenienceMethods _lbConvMethods = null;
     public CodingSchemeRenderingList _csrl = null;
 
     private static HashSet _codingSchemeHashSet = null;
     private static HashMap _csnv2codingSchemeNameMap = null;
     private static HashMap _csnv2VersionMap = null;
 
     private static boolean initializeValueSetHierarchy = false;
 
     // ==================================================================================
     // For customized query use
 
     public static int ALL = 0;
     public static int PREFERRED_ONLY = 1;
     public static int NON_PREFERRED_ONLY = 2;
 
     private static int RESOLVE_SOURCE = 1;
     private static int RESOLVE_TARGET = -1;
     private static int RESTRICT_SOURCE = -1;
     private static int RESTRICT_TARGET = 1;
 
     public static final int SEARCH_NAME_CODE = 1;
     public static final int SEARCH_DEFINITION = 2;
 
     public static final int SEARCH_PROPERTY_VALUE = 3;
     public static final int SEARCH_ROLE_VALUE = 6;
     public static final int SEARCH_ASSOCIATION_VALUE = 7;
 
     private static final List<String> STOP_WORDS =
         Arrays.asList(new String[] { "a", "an", "and", "by", "for", "of", "on",
             "in", "nos", "the", "to", "with" });
 
     public static final String TYPE_ROLE = "type_role";
     public static final String TYPE_ASSOCIATION = "type_association";
     public static final String TYPE_SUPERCONCEPT = "type_superconcept";
     public static final String TYPE_SUBCONCEPT = "type_subconcept";
     public static final String TYPE_INVERSE_ROLE = "type_inverse_role";
     public static final String TYPE_INVERSE_ASSOCIATION = "type_inverse_association";
 
     public String _ncicbContactURL = null;
     public String _terminologySubsetDownloadURL = null;
     public String _term_suggestion_application_url = null;
     public String _ncitBuildInfo = null;
     public String _ncitAppVersion = null;
     public String _ncitAnthillBuildTagBuilt = null;
     public String _evsServiceURL = null;
     public String _ncimURL = null;
     public String _ncitURL = null;
 
 /*
     public static HashMap _namespace2CodingScheme = null;
 
     public static HashMap _formalName2LocalNameHashMap = null;
     public static HashMap _localName2FormalNameHashMap = null;
     public static HashMap _formalName2MetadataHashMap = null;
     public static HashMap _displayName2FormalNameHashMap = null;
 
     public static HashMap _formalNameVersion2LocalNameHashMap = null;
     public static HashMap _localNameVersion2FormalNameVersionHashMap = null;
     public static HashMap _formalNameVersion2MetadataHashMap = null;
     public static HashMap _displayNameVersion2FormalNameVersionHashMap = null;
     public static HashMap _uri2CodingSchemeNameHashMap = null;
     public static HashMap _codingSchemeName2URIHashMap = null;
 
     public static Vector _nonConcept2ConceptAssociations = null;
     public static String _defaultOntologiesToSearchOnStr = null;
 
     public static HashSet _vocabulariesWithConceptStatusHashSet = null;
     public static HashSet _vocabulariesWithoutTreeAccessHashSet = null;
 
     public static HashMap _formalName2NCImSABHashMap = null;
 
     public static HashMap _isMappingHashMap = null;
     public static HashMap _mappingDisplayNameHashMap = null;
 
     public static HashMap _codingSchemeTagHashMap = null;
 
     public static HashMap _valueSetDefinitionHierarchyHashMap = null;
     public static Vector  _availableValueSetDefinitionSources = null;
     public static Vector  _valueSetDefinitionHierarchyRoots = null;
 
     public static HashMap _codingScheme2MappingCodingSchemes = null;
 
     public static Vector _valueSetDefinitionMetadata = null;
 
 
     public static HashMap _formalName2VersionsHashMap = null;
     public static HashMap _versionReleaseDateHashMap = null;
 
 */
    private static final String[] rel_options = new String[] {"SY", "BT", "NT", "RO"};
    private static final String[] score_options = new String[] {"0", "1", "2", "3", "4", "5", "6"};
 
     private static HashMap _namespace2CodingScheme = null;
 
     private static HashMap _formalName2LocalNameHashMap = null;
     private static HashMap _localName2FormalNameHashMap = null;
     private static HashMap _formalName2MetadataHashMap = null;
     private static HashMap _displayName2FormalNameHashMap = null;
 
     private static HashMap _formalNameVersion2LocalNameHashMap = null;
     private static HashMap _localNameVersion2FormalNameVersionHashMap = null;
     private static HashMap _formalNameVersion2MetadataHashMap = null;
     private static HashMap _displayNameVersion2FormalNameVersionHashMap = null;
     private static HashMap _uri2CodingSchemeNameHashMap = null;
     private static HashMap _codingSchemeName2URIHashMap = null;
 
     //private static Vector _nonConcept2ConceptAssociations = null;
 
     private static final String[] _nonConcept2ConceptAssociations = new String[] {"domain", "range", "instance"};
 
     private static String _defaultOntologiesToSearchOnStr = null;
 
     private static HashSet _vocabulariesWithConceptStatusHashSet = null;
     private static HashSet _vocabulariesWithoutTreeAccessHashSet = null;
 
     private static HashMap _formalName2NCImSABHashMap = null;
 
     private static HashMap _isMappingHashMap = null;
     private static HashMap _mappingDisplayNameHashMap = null;
 
     private static HashMap _codingSchemeTagHashMap = null;
 
     private static final HashMap _valueSetDefinitionHierarchyHashMap = null;
     private static Vector  _availableValueSetDefinitionSources = null;
     private static Vector  _valueSetDefinitionHierarchyRoots = null;
 
     private static HashMap _codingScheme2MappingCodingSchemes = null;
 
     private static Vector _valueSetDefinitionMetadata = null;
 
 
     private static HashMap _formalName2VersionsHashMap = null;
     private static HashMap _versionReleaseDateHashMap = null;
 
     private static Vector _source_code_schemes = null;
 
     private static HashMap sourceValueSetTree = null;
     private static HashMap terminologyValueSetTree = null;
 
 
     private static final String[] status_options = new String[] {"Valid", "Invalid", "Pending"}; //, "Undecided"};
     private static final String   default_status = "Undecided";
 
     private static final String[] export_options = new String[] {"ALL", "Valid", "Invalid", "Pending"}; //, "Undecided"};
     private static final String   default_export_option = "ALL";
 
     private static final String[] hide_options = new String[] {"NONE", "Valid", "Invalid", "Pending"}; //, "Undecided"};
 
     private static final String   default_hide_option = "NONE";
 
     private static Vector _valueset_item_vec = null;
     private static Vector valueSetDefinitionOnServer_uri_vec = null;
 
     private static String _mode_of_operation = null;
     private static HashMap _mapping_namespace_hmap = null;
 
 
     // ==================================================================================
 
     public DataUtils() {
         // setCodingSchemeMap();
     }
 
 
     static {
 		setCodingSchemeMap();
 
 		if (_valueSetDefinitionMetadata == null) {
 			_valueSetDefinitionMetadata = getValueSetDefinitionMetadata();
         }
 
         if (_namespace2CodingScheme == null) {
             _namespace2CodingScheme = getNamespaceId2CodingSchemeFormalNameMapping();
 		}
 
 		if (_defaultOntologiesToSearchOnStr == null) {
             _defaultOntologiesToSearchOnStr = getDefaultOntologiesToSearchOnStr();
 		}
 
 	}
 
 	public static HashMap get_mapping_namespace_hmap() {
 		return _mapping_namespace_hmap;
 	}
 
 
     public static List getOntologyList() {
         if (_ontologies == null)
             setCodingSchemeMap();
         return _ontologies;
     }
 
     public static HashMap get_localName2FormalNameHashMap() {
 		return _localName2FormalNameHashMap;
 	}
 
 
 
 
     public static String[] get_status_options() {
 		return Arrays.copyOf(status_options, status_options.length);
 	}
 
     public static String get_default_status() {
 		return default_status;
 	}
 
     public static String[] get_export_options() {
 		return Arrays.copyOf(export_options, export_options.length);
 	}
 
     public static String get_default_export_option() {
 	    return default_export_option;
     }
 
     public static String[] get_hide_options() {
 	    return Arrays.copyOf(hide_options, hide_options.length);
     }
 
     public static String get_default_hide_option() {
 		return default_hide_option;
 	}
 
 	public static String[] get_rel_options() {
 		return Arrays.copyOf(rel_options, rel_options.length);
 	}
 
 	public static String[] get_score_options() {
 		return Arrays.copyOf(score_options, score_options.length);
 	}
 
 
     public static HashSet get_vocabulariesWithoutTreeAccessHashSet() {
 		return _vocabulariesWithoutTreeAccessHashSet;
 	}
 
 
     private static boolean isCodingSchemeSupported(String codingSchemeName) {
         if (_codingSchemeHashSet == null)
             setCodingSchemeMap();
         return _codingSchemeHashSet.contains(codingSchemeName);
     }
 
 
     public static HashMap get_codingSchemeName2URIHashMap() {
 		return _codingSchemeName2URIHashMap;
 	}
 
 
     public static String getDefaultOntologiesToSearchOnStr() {
         if (_ontologies == null)
             setCodingSchemeMap();
 
         _defaultOntologiesToSearchOnStr = "|";
         for (int i = 0; i < _ontologies.size(); i++) {
             SelectItem item = (SelectItem) _ontologies.get(i);
             String value = (String) item.getValue();
             if (value.indexOf("Metathesaurus") == -1) {
                 _defaultOntologiesToSearchOnStr =
                     _defaultOntologiesToSearchOnStr + value + "|";
             }
         }
         return _defaultOntologiesToSearchOnStr;
     }
 
 /*
     private static boolean isCodingSchemeSupported(String codingSchemeName) {
         if (_codingSchemeHashSet == null)
             setCodingSchemeMap();
         return _codingSchemeHashSet.contains(codingSchemeName);
     }
 */
 
 
      private static void setMappingDisplayNameHashMap() {
  		_mappingDisplayNameHashMap = new HashMap();
  		_mapping_namespace_hmap = new HashMap();
  		Iterator it = _csnv2codingSchemeNameMap.keySet().iterator();
  		while (it.hasNext()) {
  			String value = (String) it.next();
  			String cs = (String) _csnv2codingSchemeNameMap.get(value);
  			String version = (String) _csnv2VersionMap.get(value);
 
 //System.out.println("=== setMappingDisplayNameHashMap: cs " + cs + " version: " + version);
 
  			HashMap hmap = MetadataUtils.getMappingDisplayHashMap(cs, version);
  			if (hmap != null) {
  				_mappingDisplayNameHashMap.put(cs, hmap);
 				Iterator it2 = hmap.entrySet().iterator();
 				while (it2.hasNext()) {
 					Entry thisEntry = (Entry) it2.next();
 					String key = (String) thisEntry.getKey();
 					String value2 = (String) thisEntry.getValue();
                     _mapping_namespace_hmap.put(key, value2);
 				}
  			}
  		}
 	}
 
 	public static void initializeCodingSchemeMap() {
 		setCodingSchemeMap();
 	}
 
 
     private static void setCodingSchemeMap() {
         _logger.debug("Initializing ...");
         _codingSchemeHashSet = new HashSet();
         _ontologies = new ArrayList();
         // codingSchemeMap = new HashMap();
         _csnv2codingSchemeNameMap = new HashMap();
         _csnv2VersionMap = new HashMap();
         _formalName2LocalNameHashMap = new HashMap();
         _localName2FormalNameHashMap = new HashMap();
         _formalName2MetadataHashMap = new HashMap();
         _displayName2FormalNameHashMap = new HashMap();
         _vocabulariesWithConceptStatusHashSet = new HashSet();
         _vocabulariesWithoutTreeAccessHashSet = new HashSet();
 
         _formalNameVersion2LocalNameHashMap = new HashMap();
         _localNameVersion2FormalNameVersionHashMap = new HashMap();
         _formalNameVersion2MetadataHashMap = new HashMap();
         _displayNameVersion2FormalNameVersionHashMap = new HashMap();
         _uri2CodingSchemeNameHashMap = new HashMap();
         _codingSchemeName2URIHashMap = new HashMap();
 
         _isMappingHashMap = new HashMap();
 
         _codingSchemeTagHashMap = new HashMap();
 
         _formalName2VersionsHashMap = new HashMap();
         _versionReleaseDateHashMap = new HashMap();
 
         Vector nv_vec = new Vector();
         boolean includeInactive = false;
 
         _mapping_namespace_hmap = new HashMap();
 
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             if (lbSvc == null) {
                 _logger
                     .warn("WARNING: Unable to connect to instantiate LexBIGService ???");
                 return;
             }
             CodingSchemeRenderingList csrl = null;
             try {
                 csrl = lbSvc.getSupportedCodingSchemes();
             } catch (LBInvocationException ex) {
                 ex.printStackTrace();
                 _logger.error("lbSvc.getSupportedCodingSchemes() FAILED..."
                     + ex.getCause());
                 return;
             }
             CodingSchemeRendering[] csrs = csrl.getCodingSchemeRendering();
             for (int i = 0; i < csrs.length; i++) {
                 int j = i + 1;
                 CodingSchemeRendering csr = csrs[i];
                 CodingSchemeSummary css = csr.getCodingSchemeSummary();
                 String formalname = css.getFormalName();
 
                 if (!_formalName2VersionsHashMap.containsKey(formalname)) {
 					Vector version_vec = new Vector();
 					_formalName2VersionsHashMap.put(formalname, version_vec);
 				}
 
                 Boolean isActive = null;
                 if (csr.getRenderingDetail() == null) {
                     _logger.warn("\tcsr.getRenderingDetail() == null");
                 } else if (csr.getRenderingDetail().getVersionStatus() == null) {
                     _logger
                         .warn("\tcsr.getRenderingDetail().getVersionStatus() == null");
                 } else {
 
                     isActive =
                         csr.getRenderingDetail().getVersionStatus().equals(
                             CodingSchemeVersionStatus.ACTIVE);
                 }
 
                 String representsVersion = css.getRepresentsVersion();
                 _logger.debug("(" + j + ") " + formalname + "  version: "
                     + representsVersion);
                 _logger.debug("\tActive? " + isActive);
 
                 if ((includeInactive && isActive == null)
                     || (isActive != null && isActive.equals(Boolean.TRUE))
                     || (includeInactive && (isActive != null && isActive
                         .equals(Boolean.FALSE)))) {
                     // nv_vec.add(value);
                     // csnv2codingSchemeNameMap.put(value, formalname);
                     // csnv2VersionMap.put(value, representsVersion);
 
                     // KLO 010810
                     CodingSchemeVersionOrTag vt =
                         new CodingSchemeVersionOrTag();
                     vt.setVersion(representsVersion);
 
                     Vector ver_vec = (Vector) _formalName2VersionsHashMap.get(formalname);
                     if (ver_vec == null) {
 						ver_vec = new Vector();
 					}
 					if (!ver_vec.contains(representsVersion)) {
 						ver_vec.add(representsVersion);
 						_formalName2VersionsHashMap.put(formalname, ver_vec);
 					}
 
                     try {
                         CodingScheme cs =
                             lbSvc.resolveCodingScheme(formalname, vt);
 
                         _uri2CodingSchemeNameHashMap.put(cs.getCodingSchemeURI(), cs.getCodingSchemeName());
                         _uri2CodingSchemeNameHashMap.put(formalname, cs.getCodingSchemeName());
 
                         _codingSchemeName2URIHashMap.put(cs.getCodingSchemeName(), cs.getCodingSchemeURI());
 
                         boolean isMapping = isMapping(cs.getCodingSchemeName(), representsVersion);
                         _isMappingHashMap.put(cs.getCodingSchemeName(), Boolean.valueOf(isMapping));
 
                         String[] localnames = cs.getLocalName();
                         for (int m = 0; m < localnames.length; m++) {
                             String localname = localnames[m];
                             _logger.debug("\tlocal name: " + localname);
                             _localName2FormalNameHashMap.put(localname,
                                 formalname);
                         }
                         _localName2FormalNameHashMap.put(cs.getCodingSchemeURI(), formalname);
 
                         NameAndValue[] nvList =
                             MetadataUtils.getMetadataProperties(cs);
                         if (nvList != null) {
 
                             String css_local_name = css.getLocalName();
                             boolean localname_exist = false;
                             for (int lcv = 0; lcv < localnames.length; lcv++) {
                                 String local_nm = (String) localnames[lcv];
                                 if (local_nm.compareTo(css_local_name) == 0) {
                                     localname_exist = true;
                                     break;
                                 }
                             }
                             if (!localname_exist) {
                                 _logger.debug("\tlocal name (*): "
                                     + css_local_name);
                             }
 
                             String value =
                                 formalname + " (version: " + representsVersion
                                     + ")";
                             _logger.debug("\tformalname & verson: " + value);
 
                             Vector<String> propertyNames =
                                 getPropertyNameListData(formalname,
                                     representsVersion);
                             if (propertyNames.contains("Concept_Status")) {
                                 _vocabulariesWithConceptStatusHashSet
                                     .add(formalname);
                                 _logger
                                     .debug("\tConcept_Status property supported.");
                             }
 
                             if (!_codingSchemeHashSet.contains(formalname)) {
                                 _codingSchemeHashSet.add(formalname);
                             }
 
                             _formalName2LocalNameHashMap.put(formalname,
                                 css_local_name);
 
                             _formalNameVersion2LocalNameHashMap.put(formalname + "$" + representsVersion,
                                  css_local_name);
 
                             _formalName2LocalNameHashMap.put(css_local_name,
                                 css_local_name);
 
                             _localName2FormalNameHashMap.put(formalname,
                                 formalname);
 
                             _localNameVersion2FormalNameVersionHashMap.put(css_local_name +"$" + representsVersion,
                                                                            formalname + "$" + representsVersion);
 
                             _localName2FormalNameHashMap.put(css_local_name,
                                 formalname);
 
                             _codingSchemeName2URIHashMap.put(formalname, cs.getCodingSchemeURI());
 
                             // String displayName = getMetadataValue(formalname,
                             // "display_name");
                             // _logger.debug("\tdisplay_name: " + displayName);
                             // displayName2FormalNameHashMap.put(displayName,
                             // formalname);
 
                             Vector metadataProperties = new Vector();
                             for (int k = 0; k < nvList.length; k++) {
                                 NameAndValue nv = (NameAndValue) nvList[k];
                                 metadataProperties.add(nv.getName() + "|"
                                     + nv.getContent());
 
                                 // to be modified
                                 //_vocabulariesWithoutTreeAccessHashSet
                                 if (nv.getName().compareTo(Constants.TREE_ACCESS_ALLOWED) == 0 && nv.getContent().compareTo("false") == 0) {
 									_vocabulariesWithoutTreeAccessHashSet.add(formalname);
 									_logger.debug("\t" + "Tree not accessible.");
 								}
 
 								if (nv.getName().compareTo("version_releaseDate") == 0) {
 									_versionReleaseDateHashMap.put(formalname + "$" + representsVersion, nv.getContent());
 								}
                             }
 
                             // _logger.debug("\t" +
                             // mdpl.getMetadataPropertyCount() +
                             // " MetadataProperties cached for " + formalname);
                             _logger.debug("\t" + nvList.length
                                 + " MetadataProperties cached for "
                                 + formalname);
                             _formalName2MetadataHashMap.put(formalname,
                                 metadataProperties);
 
                             _formalNameVersion2MetadataHashMap.put(formalname + "$" + representsVersion,
                                 metadataProperties);
 
                             String displayName =
                                 getMetadataValue(formalname, "display_name");
                             _logger.debug("\tdisplay_name: " + displayName);
                             _displayName2FormalNameHashMap.put(displayName,
                                 formalname);
 
                             _displayNameVersion2FormalNameVersionHashMap.put(displayName + "$" + representsVersion,
                                 formalname + "$" + representsVersion);
 
                             String term_browser_version =
                                 getMetadataValue(formalname,
                                     "term_browser_version");
                             _logger.debug("\tterm_browser_version: "
                                 + term_browser_version);
 
                             // MetadataPropertyList mdpl =
                             // MetadataUtils.getMetadataPropertyList(lbSvc,
                             // formalname, representsVersion, null);
                             // if (mdpl != null) {
                             // Note: Need to set sorting to false (in the
                             // following line)
                             // so source_help_info.jsp and
                             // term_type_help_info.jsp
                             // will show up correctly.
                             // Vector metadataProperties =
                             // MetadataUtils.getMetadataNameValuePairs(mdpl,
                             // false);
                             // _logger.debug("\t" +
                             // mdpl.getMetadataPropertyCount() +
                             // " MetadataProperties cached for " + formalname);
                             // formalName2MetadataHashMap.put(formalname,
                             // metadataProperties);
 
                             nv_vec.add(value);
                             _logger.debug("\tformal name: " + formalname);
                             _csnv2codingSchemeNameMap.put(value, formalname);
 
                             _csnv2VersionMap.put(value, representsVersion);
                             _logger.debug("\trepresentsVersion: "
                                 + representsVersion);
 
                         } else {
                             _logger
                                 .error("WARNING: MetadataUtils.getMetadataPropertyList returns null??? "
                                     + formalname);
                             _logger.error("\t\trepresentsVersion "
                                 + representsVersion);
                         }
                     } catch (Exception ex) {
                         _logger
                             .error("\tWARNING: Unable to resolve coding scheme "
                                 + formalname
                                 + " possibly due to missing security token.");
                         _logger.error("\t\tAccess to " + formalname
                             + " denied.");
                             ex.printStackTrace();
                     }
 
                 } else {
                     _logger.error("\tWARNING: setCodingSchemeMap discards "
                         + formalname);
                     _logger.error("\t\trepresentsVersion " + representsVersion);
                 }
             }
         } catch (Exception e) {
              e.printStackTrace();
             // return null;
         }
         if (nv_vec.size() > 0) {
             nv_vec = SortUtils.quickSort(nv_vec);
             for (int k = 0; k < nv_vec.size(); k++) {
                 String value = (String) nv_vec.elementAt(k);
                 _ontologies.add(new SelectItem(value, value));
             }
         }
         _formalName2NCImSABHashMap = createFormalName2NCImSABHashMap();
         dumpHashMap(_formalName2NCImSABHashMap);
 
         setMappingDisplayNameHashMap();
 
         if (initializeValueSetHierarchy) {
 			_logger.error("Initializing Value Set Metadata ...");
 			getValueSetDefinitionMetadata();
 			_logger.error("Done Initializing Value Set Metadata ...");
 			_logger.error("\tInitializing ValueSetHierarchy ...");
 			System.out.println("\tgetValueSetSourceHierarchy ...");
 			HashMap src_hier_hashmap = ValueSetHierarchy.getValueSetSourceHierarchy();
 			System.out.println("\tgetValueSetDefinitionURI2VSD_map ...");
 			HashMap vsduri2vsd_hashmap = ValueSetHierarchy.getValueSetDefinitionURI2VSD_map();
 			System.out.println("\tpreprocessSourceHierarchyData ...");
 			ValueSetHierarchy.preprocessSourceHierarchyData();
 			System.out.println("\tgetValueSetParticipationHashSet ...");
 			ValueSetHierarchy.getValueSetParticipationHashSet();
 			System.out.println("\tcreateVSDSource2VSDsMap ...");
 			ValueSetHierarchy.createVSDSource2VSDsMap();
 			System.out.println("\tinitializeCS2vsdURIsMap ...");
 			ValueSetHierarchy.initializeCS2vsdURIs_map();
 			_logger.debug("\tDone initializing ValueSetHierarchy ...");
 	    }
     }
 
     public static String getMetadataValue(String scheme, String propertyName) {
         Vector v = getMetadataValues(scheme, propertyName);
         if (v == null || v.size() == 0)
             return null;
         return (String) v.elementAt(0);
     }
 
     public static Vector getMetadataValues(String scheme, String propertyName) {
         if (_formalName2MetadataHashMap == null) {
             setCodingSchemeMap();
         }
 
         if (!_formalName2MetadataHashMap.containsKey(scheme)) {
             return null;
         }
         Vector metadata = (Vector) _formalName2MetadataHashMap.get(scheme);
         if (metadata == null || metadata.size() == 0) {
             return null;
         }
         Vector v = MetadataUtils.getMetadataValues(metadata, propertyName);
         return v;
     }
 
 
 
     public static String getMetadataValue(String scheme, String version, String propertyName) {
         Vector v;
         if (version != null && ! version.equalsIgnoreCase("null"))
             v = getMetadataValues(scheme, version, propertyName);
         else v = getMetadataValues(scheme, propertyName);
         if (v == null || v.size() == 0)
             return null;
         return (String) v.elementAt(0);
     }
 
     public static Vector getMetadataValues(String scheme, String version, String propertyName) {
         if (_formalName2MetadataHashMap == null) {
             setCodingSchemeMap();
         }
 
         if (!_formalNameVersion2MetadataHashMap.containsKey(scheme + "$" + version)) {
             return null;
         }
         Vector metadata = (Vector) _formalNameVersion2MetadataHashMap.get(scheme + "$" + version);
         if (metadata == null || metadata.size() == 0) {
             return null;
         }
         Vector v = MetadataUtils.getMetadataValues(metadata, propertyName);
         return v;
     }
 
     public static boolean isCodingSchemeLoaded(String scheme, String version) {
 		if (_formalNameVersion2MetadataHashMap == null) {
 			setCodingSchemeMap();
 		}
 
         boolean isLoaded = _formalNameVersion2MetadataHashMap.containsKey(scheme + "$" + version);
         if (isLoaded) {
             return isLoaded;
 		}
 
         String formalName = getFormalName(scheme);
         if (formalName == null) {
 
 		} else if (formalName.equals(scheme)) {
             return isLoaded;
 	    }
 
         isLoaded = _formalNameVersion2MetadataHashMap.containsKey(formalName + "$" + version);
         return isLoaded;
     }
 
 
     public static String getLocalName(String key) {
         if (_formalName2LocalNameHashMap == null) {
             setCodingSchemeMap();
         }
         return (String) _formalName2LocalNameHashMap.get(key);
     }
 
     public static String getFormalName(String key) {
         if (key == null) {
             return null;
         }
         if (_localName2FormalNameHashMap == null) {
             setCodingSchemeMap();
         }
         if (!_localName2FormalNameHashMap.containsKey(key))
             return null;
         return (String) _localName2FormalNameHashMap.get(key);
     }
 
     public static Vector<String> getSupportedAssociationNames(String key) {
         if (_csnv2codingSchemeNameMap == null) {
             setCodingSchemeMap();
             return getSupportedAssociationNames(key);
         }
         String codingSchemeName = (String) _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null)
             return null;
         String version = (String) _csnv2VersionMap.get(key);
         if (version == null)
             return null;
         return getSupportedAssociationNames(codingSchemeName, version);
     }
 
     public static Vector<String> getSupportedAssociationNames(
         String codingSchemeName, String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
 
         CodingScheme scheme = null;
         try {
             // RemoteServerUtil rsu = new RemoteServerUtil();
             // EVSApplicationService lbSvc = rsu.createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null) {
                 _logger.warn("scheme is NULL");
                 return null;
             }
 
             Vector<String> v = new Vector<String>();
             SupportedAssociation[] assos =
                 scheme.getMappings().getSupportedAssociation();
             for (int i = 0; i < assos.length; i++) {
                 SupportedAssociation sa = (SupportedAssociation) assos[i];
                 v.add(sa.getLocalId());
             }
             return v;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static Vector<String> getPropertyNameListData(String key) {
         if (_csnv2codingSchemeNameMap == null) {
             setCodingSchemeMap();
         }
 
         String codingSchemeName = (String) _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null) {
             return null;
         }
         String version = (String) _csnv2VersionMap.get(key);
         if (version == null) {
             return null;
         }
         return getPropertyNameListData(codingSchemeName, version);
     }
 
     public static Vector<String> getPropertyNameListData(
         String codingSchemeName, String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
         CodingScheme scheme = null;
         try {
             // RemoteServerUtil rsu = new RemoteServerUtil();
             // EVSApplicationService lbSvc = rsu.createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
 
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null)
                 return null;
             Vector<String> propertyNameListData = new Vector<String>();
             SupportedProperty[] properties =
                 scheme.getMappings().getSupportedProperty();
             for (int i = 0; i < properties.length; i++) {
                 SupportedProperty property = properties[i];
                 propertyNameListData.add(property.getLocalId());
             }
             return propertyNameListData;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static Vector<String> getRepresentationalFormListData(String key) {
         String codingSchemeName = (String) _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null)
             return null;
         String version = (String) _csnv2VersionMap.get(key);
         if (version == null)
             return null;
         return getRepresentationalFormListData(codingSchemeName, version);
     }
 
     public static Vector<String> getRepresentationalFormListData(
         String codingSchemeName, String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
         CodingScheme scheme = null;
         try {
             // RemoteServerUtil rsu = new RemoteServerUtil();
             // EVSApplicationService lbSvc = rsu.createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null)
                 return null;
             Vector<String> propertyNameListData = new Vector<String>();
             SupportedRepresentationalForm[] forms =
                 scheme.getMappings().getSupportedRepresentationalForm();
             if (forms != null) {
                 for (int i = 0; i < forms.length; i++) {
                     SupportedRepresentationalForm form = forms[i];
                     propertyNameListData.add(form.getLocalId());
                 }
             }
             return propertyNameListData;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static Vector<String> getPropertyQualifierListData(String key) {
         String codingSchemeName = (String) _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null)
             return null;
         String version = (String) _csnv2VersionMap.get(key);
         if (version == null)
             return null;
         return getPropertyQualifierListData(codingSchemeName, version);
     }
 
     public static Vector<String> getPropertyQualifierListData(
         String codingSchemeName, String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
         CodingScheme scheme = null;
         try {
             // RemoteServerUtil rsu = new RemoteServerUtil();
             // EVSApplicationService lbSvc = rsu.createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null)
                 return null;
             Vector<String> propertyQualifierListData = new Vector<String>();
             SupportedPropertyQualifier[] qualifiers =
                 scheme.getMappings().getSupportedPropertyQualifier();
             for (int i = 0; i < qualifiers.length; i++) {
                 SupportedPropertyQualifier qualifier = qualifiers[i];
                 propertyQualifierListData.add(qualifier.getLocalId());
             }
 
             return propertyQualifierListData;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static Vector<String> getSourceListData(String key) {
         if (_csnv2codingSchemeNameMap == null) {
             setCodingSchemeMap();
             return getSourceListData(key);
         }
         String codingSchemeName = (String) _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null)
             return null;
         String version = (String) _csnv2VersionMap.get(key);
         if (version == null)
             return null;
         return getSourceListData(codingSchemeName, version);
     }
 
     public static Vector<String> getSourceListData(String codingSchemeName,
         String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
         CodingScheme scheme = null;
         try {
             // RemoteServerUtil rsu = new RemoteServerUtil();
             // EVSApplicationService lbSvc = rsu.createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null)
                 return null;
             Vector<String> sourceListData = new Vector<String>();
 
             // Insert your code here
             SupportedSource[] sources =
                 scheme.getMappings().getSupportedSource();
             for (int i = 0; i < sources.length; i++) {
                 SupportedSource source = sources[i];
                 sourceListData.add(source.getLocalId());
             }
 
             return sourceListData;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static String int2String(Integer int_obj) {
         if (int_obj == null) {
             return null;
         }
 
         String retstr = Integer.toString(int_obj);
         return retstr;
     }
 
     // ==================================================================================================================================
 
     public static Entity getConceptByCode(String codingSchemeName,
         String vers, String ltag, String code) {
         return SearchUtils.matchConceptByCode(codingSchemeName, vers, code,
             null, "LuceneQuery");
     }
 
     public static NameAndValueList createNameAndValueList(String[] names,
         String[] values) {
         NameAndValueList nvList = new NameAndValueList();
         for (int i = 0; i < names.length; i++) {
             NameAndValue nv = new NameAndValue();
             nv.setName(names[i]);
             if (values != null) {
                 nv.setContent(values[i]);
             }
             nvList.addNameAndValue(nv);
         }
         return nvList;
     }
 
     public ResolvedConceptReferenceList getNext(
         ResolvedConceptReferencesIterator iterator) {
         return iterator.getNext();
     }
 
     public Vector getParentCodes(String scheme, String version, String code) {
         Vector hierarchicalAssoName_vec =
             getHierarchyAssociationId(scheme, version);
         if (hierarchicalAssoName_vec == null
             || hierarchicalAssoName_vec.size() == 0) {
             return null;
         }
         String hierarchicalAssoName =
             (String) hierarchicalAssoName_vec.elementAt(0);
         // KLO, 01/23/2009
         // Vector<Concept> superconcept_vec = util.getAssociationSources(scheme,
         // version, code, hierarchicalAssoName);
         Vector superconcept_vec =
             getAssociationSourceCodes(scheme, version, code,
                 hierarchicalAssoName);
         //if (superconcept_vec == null)
         //    return null;
         // SortUtils.quickSort(superconcept_vec, SortUtils.SORT_BY_CODE);
         return superconcept_vec;
 
     }
 
     public Vector getAssociationSourceCodes(String scheme, String version,
         String code, String assocName) {
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         if (version != null)
             csvt.setVersion(version);
         ResolvedConceptReferenceList matches = null;
         Vector v = new Vector();
         try {
             // EVSApplicationService lbSvc = new
             // RemoteServerUtil().createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodedNodeGraph cng = lbSvc.getNodeGraph(scheme, csvt, null);
             //Boolean restrictToAnonymous = Boolean.TRUE;
             Boolean restrictToAnonymous = Boolean.FALSE;
             //6.0 mod (KLO, 101810)
             cng = cng.restrictToAnonymous(restrictToAnonymous);
 
             NameAndValueList nameAndValueList =
                 createNameAndValueList(new String[] { assocName }, null);
 
             NameAndValueList nameAndValueList_qualifier = null;
             cng =
                 cng.restrictToAssociations(nameAndValueList,
                     nameAndValueList_qualifier);
 
             matches =
                 cng.resolveAsList(ConvenienceMethods.createConceptReference(
                     code, scheme), false, true, 1, 1, new LocalNameList(),
                     null, null, _maxReturn);
 
             if (matches.getResolvedConceptReferenceCount() > 0) {
 				/*
                 Enumeration<ResolvedConceptReference> refEnum = matches.enumerateResolvedConceptReference();
                 while (refEnum.hasMoreElements()) {
                     ResolvedConceptReference ref = refEnum.nextElement();
                 */
 
                 java.util.Enumeration<? extends ResolvedConceptReference> refEnum = matches.enumerateResolvedConceptReference();
                 while (refEnum.hasMoreElements()) {
                     ResolvedConceptReference ref = (ResolvedConceptReference) refEnum.nextElement();
 
                     AssociationList targetof = ref.getTargetOf();
                     Association[] associations = targetof.getAssociation();
 
                     for (int i = 0; i < associations.length; i++) {
                         Association assoc = associations[i];
                         //6.0 mod (KLO, 101810)
                         //assoc = processForAnonomousNodes(assoc);
                         AssociatedConcept[] acl =
                             assoc.getAssociatedConcepts()
                                 .getAssociatedConcept();
                         for (int j = 0; j < acl.length; j++) {
                             AssociatedConcept ac = acl[j];
                             v.add(ac.getReferencedEntry().getEntityCode());
                         }
                     }
                 }
                 SortUtils.quickSort(v);
             }
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return v;
     }
 
     public static ConceptReferenceList createConceptReferenceList(
         String[] codes, String codingSchemeName) {
         if (codes == null) {
             return null;
         }
         ConceptReferenceList list = new ConceptReferenceList();
         for (int i = 0; i < codes.length; i++) {
             ConceptReference cr = new ConceptReference();
             cr.setCodingSchemeName(codingSchemeName);
             cr.setConceptCode(codes[i]);
             list.addConceptReference(cr);
         }
         return list;
     }
 
     public static ConceptReferenceList createConceptReferenceList(Vector codes,
         String codingSchemeName) {
         if (codes == null) {
             return null;
         }
         ConceptReferenceList list = new ConceptReferenceList();
         for (int i = 0; i < codes.size(); i++) {
             String code = (String) codes.elementAt(i);
             ConceptReference cr = new ConceptReference();
             cr.setCodingSchemeName(codingSchemeName);
             cr.setConceptCode(code);
             list.addConceptReference(cr);
         }
         return list;
     }
 
     public Vector getSubconceptCodes(String scheme, String version, String code) { // throws
         // LBException{
         Vector v = new Vector();
         try {
             // EVSApplicationService lbSvc = new
             // RemoteServerUtil().createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             LexBIGServiceConvenienceMethods lbscm =
                 (LexBIGServiceConvenienceMethods) lbSvc
                     .getGenericExtension("LexBIGServiceConvenienceMethods");
             lbscm.setLexBIGService(lbSvc);
 
             CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
             csvt.setVersion(version);
             /* String desc = null;
             try {
                 desc =
                     lbscm
                         .createCodeNodeSet(new String[] { code }, scheme, csvt)
                         .resolveToList(null, null, null, 1)
                         .getResolvedConceptReference(0).getEntityDescription()
                         .getContent();
 
             } catch (Exception e) {
                 desc = "<not found>";
 
             }*/
 
             // Iterate through all hierarchies and levels ...
             String[] hierarchyIDs = lbscm.getHierarchyIDs(scheme, csvt);
             for (int k = 0; k < hierarchyIDs.length; k++) {
                 String hierarchyID = hierarchyIDs[k];
                 AssociationList associations = null;
                 associations = null;
                 try {
                     associations =
                         lbscm.getHierarchyLevelNext(scheme, csvt, hierarchyID,
                             code, false, null);
                 } catch (Exception e) {
                     _logger
                         .error("getSubconceptCodes - Exception lbscm.getHierarchyLevelNext  ");
                     return v;
                 }
 
                 for (int i = 0; i < associations.getAssociationCount(); i++) {
                     Association assoc = associations.getAssociation(i);
                     AssociatedConceptList concepts =
                         assoc.getAssociatedConcepts();
                     for (int j = 0; j < concepts.getAssociatedConceptCount(); j++) {
                         AssociatedConcept concept =
                             concepts.getAssociatedConcept(j);
                         String nextCode = concept.getConceptCode();
                         v.add(nextCode);
                     }
                 }
             }
         } catch (Exception ex) {
             // ex.printStackTrace();
         }
         return v;
     }
 
     public Vector getSuperconceptCodes(String scheme, String version,
         String code) { // throws LBException{
         long ms = System.currentTimeMillis();
         Vector v = new Vector();
         try {
             // EVSApplicationService lbSvc = new
             // RemoteServerUtil().createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             LexBIGServiceConvenienceMethods lbscm =
                 (LexBIGServiceConvenienceMethods) lbSvc
                     .getGenericExtension("LexBIGServiceConvenienceMethods");
             lbscm.setLexBIGService(lbSvc);
             CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
             csvt.setVersion(version);
             /*String desc = null;
             try {
                 desc =
                     lbscm
                         .createCodeNodeSet(new String[] { code }, scheme, csvt)
                         .resolveToList(null, null, null, 1)
                         .getResolvedConceptReference(0).getEntityDescription()
                         .getContent();
             } catch (Exception e) {
                 desc = "<not found>";
             }*/
 
             // Iterate through all hierarchies and levels ...
             String[] hierarchyIDs = lbscm.getHierarchyIDs(scheme, csvt);
             for (int k = 0; k < hierarchyIDs.length; k++) {
                 String hierarchyID = hierarchyIDs[k];
                 AssociationList associations =
                     lbscm.getHierarchyLevelPrev(scheme, csvt, hierarchyID,
                         code, false, null);
                 for (int i = 0; i < associations.getAssociationCount(); i++) {
                     Association assoc = associations.getAssociation(i);
                     AssociatedConceptList concepts =
                         assoc.getAssociatedConcepts();
                     for (int j = 0; j < concepts.getAssociatedConceptCount(); j++) {
                         AssociatedConcept concept =
                             concepts.getAssociatedConcept(j);
                         String nextCode = concept.getConceptCode();
                         v.add(nextCode);
                     }
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             _logger
                 .debug("Run time (ms): " + (System.currentTimeMillis() - ms));
         }
         return v;
     }
 
     public Vector getHierarchyAssociationId(String scheme, String version) {
 
         Vector association_vec = new Vector();
         try {
             // EVSApplicationService lbSvc = new
             // RemoteServerUtil().createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
 
             // Will handle secured ontologies later.
             CodingSchemeVersionOrTag versionOrTag =
                 new CodingSchemeVersionOrTag();
             versionOrTag.setVersion(version);
             CodingScheme cs = lbSvc.resolveCodingScheme(scheme, versionOrTag);
             Mappings mappings = cs.getMappings();
             SupportedHierarchy[] hierarchies = mappings.getSupportedHierarchy();
             java.lang.String[] ids = hierarchies[0].getAssociationNames();
 
             for (int i = 0; i < ids.length; i++) {
                 if (!association_vec.contains(ids[i])) {
                     association_vec.add(ids[i]);
                 }
             }
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return association_vec;
     }
 
     public static String getVersion() {
         return getVersion(CODING_SCHEME_NAME);
     }
 
     public static String getVersion(String scheme) {
         String info = getReleaseDate(scheme);
         /*
         String full_name = DataUtils.getMetadataValue(scheme, "full_name");
         if (full_name == null || full_name.compareTo("null") == 0) {
             full_name = scheme;
 		}*/
         String version =
             getMetadataValue(scheme, "term_browser_version");
 
         if (version == null)
             version = getVocabularyVersionByTag(scheme, null);
 
         if (version != null && version.length() > 0)
             info += " (" + version + ")";
         return info;
     }
 
     public static String getReleaseDate() {
         return getReleaseDate(CODING_SCHEME_NAME);
     }
 
     public static String getReleaseDate(String coding_scheme_name) {
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             DateFormat formatter = new SimpleDateFormat("MMMM d, yyyy");
             HistoryService hs = null;
             try {
                 hs = lbSvc.getHistoryService(coding_scheme_name);
             } catch (Exception ex) {
                 _logger.error("WARNING: HistoryService is not available for "
                     + coding_scheme_name);
             }
             if (hs != null) {
                 SystemRelease release = hs.getLatestBaseline();
                 java.util.Date date = release.getReleaseDate();
                 return formatter.format(date);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return "";
     }
 
     /*
      * public static String getVocabularyVersionByTag(String codingSchemeName,
      * String ltag) { if (codingSchemeName == null) return null; try {
      * LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
      * CodingSchemeRenderingList lcsrl = lbSvc.getSupportedCodingSchemes();
      * CodingSchemeRendering[] csra = lcsrl.getCodingSchemeRendering(); for (int
      * i = 0; i < csra.length; i++) { CodingSchemeRendering csr = csra[i];
      * CodingSchemeSummary css = csr.getCodingSchemeSummary(); if
      * (css.getFormalName().compareTo(codingSchemeName) == 0 ||
      * css.getLocalName().compareTo(codingSchemeName) == 0) { if (ltag == null)
      * return css.getRepresentsVersion(); RenderingDetail rd =
      * csr.getRenderingDetail(); CodingSchemeTagList cstl = rd.getVersionTags();
      * java.lang.String[] tags = cstl.getTag(); for (int j = 0; j < tags.length;
      * j++) { String version_tag = (String) tags[j]; if
      * (version_tag.compareToIgnoreCase(ltag) == 0) { return
      * css.getRepresentsVersion(); } } } } } catch (Exception e) {
      * _logger.error("Version corresponding to tag " + ltag + " is not found " +
      * " in " + codingSchemeName);
      *
      * //e.printStackTrace(); } return null; }
      */
 
     public static String getVocabularyVersionByTag(String codingSchemeName,
         String ltag) {
 
         if (codingSchemeName == null)
             return null;
         String version = null;
         int knt = 0;
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodingSchemeRenderingList lcsrl = lbSvc.getSupportedCodingSchemes();
             CodingSchemeRendering[] csra = lcsrl.getCodingSchemeRendering();
             for (int i = 0; i < csra.length; i++) {
                 CodingSchemeRendering csr = csra[i];
                 CodingSchemeSummary css = csr.getCodingSchemeSummary();
                 if (css.getFormalName().compareTo(codingSchemeName) == 0
                     || css.getLocalName().compareTo(codingSchemeName) == 0
                     || css.getCodingSchemeURI().compareTo(codingSchemeName) == 0) {
 					version = css.getRepresentsVersion();
                     knt++;
 
                     if (ltag == null)
                         return version;
                     RenderingDetail rd = csr.getRenderingDetail();
                     CodingSchemeTagList cstl = rd.getVersionTags();
                     java.lang.String[] tags = cstl.getTag();
                     // KLO, 102409
                     if (tags == null)
                         return version;
 
                     if (tags.length > 0) {
                         for (int j = 0; j < tags.length; j++) {
                             String version_tag = (String) tags[j];
 
                             if (version_tag != null && version_tag.compareToIgnoreCase(ltag) == 0) {
                                 return version;
                             }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         _logger.warn("Version corresponding to tag " + ltag + " is not found "
             + " in " + codingSchemeName);
         if (ltag != null && ltag.compareToIgnoreCase("PRODUCTION") == 0
             & knt == 1) {
             _logger.warn("\tUse " + version + " as default.");
             return version;
         }
         return null;
     }
 
     public static Vector<String> getVersionListData(String codingSchemeName) {
 
         Vector<String> v = new Vector();
         try {
             // RemoteServerUtil rsu = new RemoteServerUtil();
             // EVSApplicationService lbSvc = rsu.createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodingSchemeRenderingList csrl = lbSvc.getSupportedCodingSchemes();
             if (csrl == null) {
                 _logger.warn("csrl is NULL");
                 return null;
 			}
 
             CodingSchemeRendering[] csrs = csrl.getCodingSchemeRendering();
             for (int i = 0; i < csrs.length; i++) {
                 CodingSchemeRendering csr = csrs[i];
                 Boolean isActive =
                     csr.getRenderingDetail().getVersionStatus().equals(
                         CodingSchemeVersionStatus.ACTIVE);
                 // if (isActive != null && isActive.equals(Boolean.TRUE)) {
                 CodingSchemeSummary css = csr.getCodingSchemeSummary();
                 String formalname = css.getFormalName();
                 if (formalname.compareTo(codingSchemeName) == 0) {
                     String representsVersion = css.getRepresentsVersion();
                     v.add(representsVersion);
                 }
                 // }
             }
         } catch (Exception ex) {
 
         }
         return v;
     }
 
     public static String getFileName(String pathname) {
         File file = new File(pathname);
         String filename = file.getName();
         return filename;
     }
 
 /*
     protected static Association processForAnonomousNodes(Association assoc) {
         // clone Association except associatedConcepts
         Association temp = new Association();
         temp.setAssociatedData(assoc.getAssociatedData());
         temp.setAssociationName(assoc.getAssociationName());
         temp.setAssociationReference(assoc.getAssociationReference());
         temp.setDirectionalName(assoc.getDirectionalName());
         temp.setAssociatedConcepts(new AssociatedConceptList());
 
         for (int i = 0; i < assoc.getAssociatedConcepts()
             .getAssociatedConceptCount(); i++) {
             // Conditionals to deal with anonymous nodes and UMLS top nodes
             // "V-X"
             // The first three allow UMLS traversal to top node.
             // The last two are specific to owl anonymous nodes which can act
             // like false
             // top nodes.
             if (assoc.getAssociatedConcepts().getAssociatedConcept(i)
                 .getReferencedEntry() != null
                 && assoc.getAssociatedConcepts().getAssociatedConcept(i)
                     .getReferencedEntry().getIsAnonymous() != null
                 && assoc.getAssociatedConcepts().getAssociatedConcept(i)
                     .getReferencedEntry().getIsAnonymous() != false
                 && !assoc.getAssociatedConcepts().getAssociatedConcept(i)
                     .getConceptCode().equals("@")
                 && !assoc.getAssociatedConcepts().getAssociatedConcept(i)
                     .getConceptCode().equals("@@")) {
                 // do nothing
             } else {
                 temp.getAssociatedConcepts().addAssociatedConcept(
                     assoc.getAssociatedConcepts().getAssociatedConcept(i));
             }
         }
         return temp;
     }
 */
 
     // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     public static LocalNameList vector2LocalNameList(Vector<String> v) {
         if (v == null)
             return null;
         LocalNameList list = new LocalNameList();
         for (int i = 0; i < v.size(); i++) {
             String vEntry = (String) v.elementAt(i);
             list.addEntry(vEntry);
         }
         return list;
     }
 
     protected static NameAndValueList createNameAndValueList(Vector names,
         Vector values) {
         if (names == null)
             return null;
         NameAndValueList nvList = new NameAndValueList();
         for (int i = 0; i < names.size(); i++) {
             String name = (String) names.elementAt(i);
             String value = (String) values.elementAt(i);
             NameAndValue nv = new NameAndValue();
             nv.setName(name);
             if (value != null) {
                 nv.setContent(value);
             }
             nvList.addNameAndValue(nv);
         }
         return nvList;
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     protected static CodingScheme getCodingScheme(String codingScheme,
         CodingSchemeVersionOrTag versionOrTag) throws LBException {
 
         CodingScheme cs = null;
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             cs = lbSvc.resolveCodingScheme(codingScheme, versionOrTag);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return cs;
     }
 
     public static Vector<SupportedProperty> getSupportedProperties(
         CodingScheme cs) {
         if (cs == null)
             return null;
         Vector<SupportedProperty> v = new Vector<SupportedProperty>();
         SupportedProperty[] properties =
             cs.getMappings().getSupportedProperty();
         for (int i = 0; i < properties.length; i++) {
             SupportedProperty sp = (SupportedProperty) properties[i];
             v.add(sp);
         }
         return v;
     }
 
     public static Vector<String> getSupportedPropertyNames(CodingScheme cs) {
         Vector w = getSupportedProperties(cs);
         if (w == null)
             return null;
 
         Vector<String> v = new Vector<String>();
         for (int i = 0; i < w.size(); i++) {
             SupportedProperty sp = (SupportedProperty) w.elementAt(i);
             v.add(sp.getLocalId());
         }
         return v;
     }
 
     public static Vector<String> getSupportedPropertyNames(String codingScheme,
         String version) {
         CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
         if (version != null)
             versionOrTag.setVersion(version);
         try {
             CodingScheme cs = getCodingScheme(codingScheme, versionOrTag);
             return getSupportedPropertyNames(cs);
         } catch (Exception ex) {
         }
         return null;
     }
 
     public static Vector getPropertyNamesByType(Entity concept,
         String property_type) {
         Vector v = new Vector();
         org.LexGrid.commonTypes.Property[] properties = null;
 
         if (property_type.compareToIgnoreCase("GENERIC") == 0) {
             properties = concept.getProperty();
         } else if (property_type.compareToIgnoreCase("PRESENTATION") == 0) {
             properties = concept.getPresentation();
             // } else if (property_type.compareToIgnoreCase("INSTRUCTION") == 0)
             // {
             // properties = concept.getInstruction();
         } else if (property_type.compareToIgnoreCase("COMMENT") == 0) {
             properties = concept.getComment();
         } else if (property_type.compareToIgnoreCase("DEFINITION") == 0) {
             properties = concept.getDefinition();
         }
 
         if (properties == null || properties.length == 0)
             return v;
         for (int i = 0; i < properties.length; i++) {
             Property p = (Property) properties[i];
             // v.add(p.getText().getContent());
             v.add(p.getPropertyName());
         }
         return v;
     }
 
     /*
      * public static Vector getPropertyValues(Concept concept, String
      * property_type, String property_name) { Vector v = new Vector();
      * org.LexGrid.commonTypes.Property[] properties = null;
      *
      *
      * if (property_type.compareToIgnoreCase("GENERIC") == 0) { properties =
      * concept.getProperty(); } else if
      * (property_type.compareToIgnoreCase("PRESENTATION") == 0) { properties =
      * concept.getPresentation(); //} else if
      * (property_type.compareToIgnoreCase("INSTRUCTION") == 0) { // properties =
      * concept.getInstruction(); } else if
      * (property_type.compareToIgnoreCase("COMMENT") == 0) { properties =
      * concept.getComment(); } else if
      * (property_type.compareToIgnoreCase("DEFINITION") == 0) { properties =
      * concept.getDefinition(); } else {
      *
      * _logger.warn("WARNING: property_type not found -- " + property_type);
      *
      * }
      *
      *
      * if (properties == null || properties.length == 0) return v; for (int i =
      * 0; i < properties.length; i++) { Property p = (Property) properties[i];
      * if (property_name.compareTo(p.getPropertyName()) == 0) { String t =
      * p.getValue().getContent();
      *
      * Source[] sources = p.getSource(); if (sources != null && sources.length >
      * 0) { Source src = sources[0]; t = t + "|" + src.getContent();
      *
      * } v.add(t); } } return v; }
      */
 
     public static String getPropertyQualfierValues(
         org.LexGrid.commonTypes.Property p) {
 
         //String s = "";
         StringBuffer buf = new StringBuffer();
         PropertyQualifier[] qualifiers = p.getPropertyQualifier();
         if (qualifiers != null && qualifiers.length > 0) {
             for (int j = 0; j < qualifiers.length; j++) {
                 PropertyQualifier q = qualifiers[j];
                 String qualifier_name = q.getPropertyQualifierName();
                 //KLO, 110910
                 if (qualifier_name.compareTo("label") != 0) {
 					String qualifier_value = q.getValue().getContent();
 
 					//s = s + qualifier_name + ": " + qualifier_value;
 					buf.append(qualifier_name + ": " + qualifier_value);
 					if (j < qualifiers.length - 1) {
 						//s = s + "; ";);
 						buf.append("; ");
 					}
 				}
             }
         }
         String s = buf.toString();
         return s;
     }
 
     public static Vector getPropertyValues(Entity concept,
         String property_type, String property_name) {
         Vector v = new Vector();
         org.LexGrid.commonTypes.Property[] properties = null;
 
         boolean addQualifiers = false;
         if (property_type.compareToIgnoreCase("GENERIC") == 0) {
             properties = concept.getProperty();
             addQualifiers = true;
         } else if (property_type.compareToIgnoreCase("PRESENTATION") == 0) {
             properties = concept.getPresentation();
         } else if (property_type.compareToIgnoreCase("COMMENT") == 0) {
             properties = concept.getComment();
             addQualifiers = true;
         } else if (property_type.compareToIgnoreCase("DEFINITION") == 0) {
             properties = concept.getDefinition();
 
         } else {
             System.out.println("WARNING: property_type not found -- "
                 + property_type);
         }
 
         if (properties == null || properties.length == 0)
             return v;
         for (int i = 0; i < properties.length; i++) {
             Property p = (Property) properties[i];
             if (property_name.compareTo(p.getPropertyName()) == 0) {
                 String t = p.getValue().getContent();
 
                 // #27034
                 if (addQualifiers) {
                     String qualifiers = getPropertyQualfierValues(p);
                     if (qualifiers.compareTo("") != 0) {
                         t = t + " (" + getPropertyQualfierValues(p) + ")";
                     }
                 }
 
                 Source[] sources = p.getSource();
                 if (sources != null && sources.length > 0) {
                     Source src = sources[0];
                     t = t + "|" + src.getContent();
                 }
 
                 v.add(t);
             }
         }
         return v;
     }
 
     // =====================================================================================
 
     public List getSupportedRoleNames(LexBIGService lbSvc, String scheme,
         String version) {
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         if (version != null)
             csvt.setVersion(version);
 
         List list = new ArrayList();
         try {
             CodingScheme cs = lbSvc.resolveCodingScheme(scheme, csvt);
             Relations[] relations = cs.getRelations();
             for (int i = 0; i < relations.length; i++) {
                 Relations relation = relations[i];
 
                 _logger.debug("** getSupportedRoleNames containerName: "
                     + relation.getContainerName());
 
                 if (relation.getContainerName().compareToIgnoreCase("roles") == 0
                     || relation.getContainerName().compareToIgnoreCase(
                         "relations") == 0) {
                     //org.LexGrid.relations.Association[] asso_array =
                     org.LexGrid.relations.AssociationPredicate[] asso_array =
                         relation.getAssociationPredicate();
                     for (int j = 0; j < asso_array.length; j++) {
                         org.LexGrid.relations.AssociationPredicate association =
                             (org.LexGrid.relations.AssociationPredicate) asso_array[j];
                         // list.add(association.getAssociationName());
                         // KLO, 092209
                         //list.add(association.getForwardName());
                         list.add(association.getAssociationName());
                     }
                 }
             }
         } catch (Exception ex) {
 
         }
         return list;
     }
 
     public static void sortArray(ArrayList list) {
         String tmp;
         if (list.size() <= 1)
             return;
         for (int i = 0; i < list.size(); i++) {
             String s1 = (String) list.get(i);
             for (int j = i + 1; j < list.size(); j++) {
                 String s2 = (String) list.get(j);
                 if (s1.compareToIgnoreCase(s2) > 0) {
                     tmp = s1;
                     list.set(i, s2);
                     list.set(j, tmp);
                 }
             }
         }
     }
 
     public static void sortArray(String[] strArray) {
         String tmp;
         if (strArray.length <= 1)
             return;
         for (int i = 0; i < strArray.length; i++) {
             for (int j = i + 1; j < strArray.length; j++) {
                 if (strArray[i].compareToIgnoreCase(strArray[j]) > 0) {
                     tmp = strArray[i];
                     strArray[i] = strArray[j];
                     strArray[j] = tmp;
                 }
             }
         }
     }
 
     public String[] getSortedKeys(HashMap map) {
         if (map == null)
             return null;
         Set keyset = map.keySet();
         String[] names = new String[keyset.size()];
         Iterator it = keyset.iterator();
         int i = 0;
         while (it.hasNext()) {
             String s = (String) it.next();
             names[i] = s;
             i++;
         }
         sortArray(names);
         return names;
     }
 
     public String getPreferredName(Entity c) {
 
         Presentation[] presentations = c.getPresentation();
         for (int i = 0; i < presentations.length; i++) {
             Presentation p = presentations[i];
             if (p.getPropertyName().compareTo("Preferred_Name") == 0) {
                 return p.getValue().getContent();
             }
         }
         return null;
     }
 
     public LexBIGServiceConvenienceMethods createLexBIGServiceConvenienceMethods(
         LexBIGService lbSvc) {
         LexBIGServiceConvenienceMethods lbscm = null;
         try {
             lbscm =
                 (LexBIGServiceConvenienceMethods) lbSvc
                     .getGenericExtension("LexBIGServiceConvenienceMethods");
             lbscm.setLexBIGService(lbSvc);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return lbscm;
     }
 
     // [#24809] Missing relas
     private String replaceAssociationNameByRela(AssociatedConcept ac,
         String associationName) {
 
 
 
 
 
 
 
         if (ac.getAssociationQualifiers() == null)
             return associationName;
         if (ac.getAssociationQualifiers().getNameAndValue() == null)
             return associationName;
 
         for (NameAndValue qual : ac.getAssociationQualifiers()
             .getNameAndValue()) {
             String qualifier_name = qual.getName();
             String qualifier_value = qual.getContent();
             if (qualifier_name.compareToIgnoreCase("rela") == 0) {
                 return qualifier_value; // replace associationName by Rela value
             }
         }
         return associationName;
     }
 
     public HashMap getRelationshipHashMap(String scheme, String version,
         String code) {
 
 System.out.println("==================================================================");
 System.out.println("DataUtils.getRelationshipHashMap scheme: " + scheme);
 System.out.println("DataUtils.getRelationshipHashMap version: " + version);
 System.out.println("DataUtils.getRelationshipHashMap code: " + code);
 System.out.println("==================================================================");
 
         boolean isMapping = isMapping(scheme, version);
 
 System.out.println("DataUtils.getRelationshipHashMap isMapping: " + isMapping);
 
         NameAndValueList navl = null;
         if (isMapping) navl = getMappingAssociationNames(scheme, version);
 
         LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
         LexBIGServiceConvenienceMethods lbscm =
             createLexBIGServiceConvenienceMethods(lbSvc);
 
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         if (version != null)
             csvt.setVersion(version);
 
         ConceptReference cr = ConvenienceMethods.createConceptReference(code, scheme);
         String entityCodeNamespace = null;
 
 		Entity concept = getConceptByCode(scheme, version, null, code);
 		if (concept == null) {
 			if (!isMapping) {
 				System.out.println("*** WARNING: concept not found in: " + scheme + " version: " + version + " code: " + code);
 				return null;
 		    }
 		} else {
 			entityCodeNamespace = concept.getEntityCodeNamespace();
 		}
 
         if (entityCodeNamespace != null) {
 			cr.setCodingSchemeName(entityCodeNamespace);
 		}
 
 
 
         // Perform the query ...
         ResolvedConceptReferenceList matches = null;
         List list = getSupportedRoleNames(lbSvc, scheme, version);
 
         ArrayList roleList = new ArrayList();
         ArrayList associationList = new ArrayList();
 
         ArrayList inverse_roleList = new ArrayList();
         ArrayList inverse_associationList = new ArrayList();
 
         ArrayList superconceptList = new ArrayList();
         ArrayList subconceptList = new ArrayList();
 
         HashMap map = new HashMap();
 
         // Exclude hierarchical relationships:
         String[] associationsToNavigate =
             TreeUtils.getAssociationsToNavigate(scheme, version);
         Vector w = new Vector();
         if (associationsToNavigate != null) {
             for (int k = 0; k < associationsToNavigate.length; k++) {
                 w.add(associationsToNavigate[k]);
             }
         }
 
          // superconcepts:
 		if (!isMapping) {
 				HashMap hmap_super = TreeUtils.getSuperconcepts(scheme, version, code);
 				if (hmap_super != null) {
 					TreeItem ti = (TreeItem) hmap_super.get(code);
 					if (ti != null) {
 						for (String association : ti._assocToChildMap.keySet()) {
 							List<TreeItem> children =
 								ti._assocToChildMap.get(association);
 							for (TreeItem childItem : children) {
 								superconceptList.add(childItem._text + "|"
 									+ childItem._code);
 							}
 						}
 					}
 				}
 				//Collections.sort(superconceptList);
 				SortUtils.quickSort(superconceptList);
 
 		}
         map.put(TYPE_SUPERCONCEPT, superconceptList);
 
         /*
          * HashMap hmap_sub = TreeUtils.getSubconcepts(scheme, version, code);
          * if (hmap_sub != null) { TreeItem ti = (TreeItem) hmap_sub.get(code);
          * if (ti != null) { for (String association :
          * ti.assocToChildMap.keySet()) { List<TreeItem> children =
          * ti.assocToChildMap.get(association); for (TreeItem childItem :
          * children) { subconceptList.add(childItem.text + "|" +
          * childItem.code); } } } }
          */
 
         // subconcepts:
 		if (!isMapping) {
 				subconceptList =
 					TreeUtils.getSubconceptNamesAndCodes(scheme, version, code);
 				//KLO
 				//Collections.sort(subconceptList);
 				SortUtils.quickSort(subconceptList);
 
 		}
         map.put(TYPE_SUBCONCEPT, subconceptList);
 
         // associations:
         try {
             CodedNodeGraph cng = lbSvc.getNodeGraph(scheme, csvt, null);
 
             if (isMapping) {
                  if (navl != null) {
 					 cng = cng.restrictToAssociations(navl, null);
  				 }
 			}
 
             matches = null;
             try {
                 matches =
                     cng.resolveAsList(cr,
                             true, false, 0, 1, null, null, null, null, -1,
                             false);
 
             } catch (Exception e) {
                 _logger
                     .error("ERROR: DataUtils getRelationshipHashMap cng.resolveAsList throws exceptions."
                         + code);
             }
 
             if (matches != null
                 && matches.getResolvedConceptReferenceCount() > 0) {
                 Enumeration<? extends ResolvedConceptReference> refEnum =
                     matches.enumerateResolvedConceptReference();
 
                 while (refEnum.hasMoreElements()) {
                     ResolvedConceptReference ref = (ResolvedConceptReference) refEnum.nextElement();
                     AssociationList sourceof = ref.getSourceOf();
                     if (sourceof != null) {
                         Association[] associations = sourceof.getAssociation();
                         if (associations != null) {
                             for (int i = 0; i < associations.length; i++) {
                                 Association assoc = associations[i];
                                 String associationName = null;
 
                                 try {
 									associationName =
 										lbscm
 											.getAssociationNameFromAssociationCode(
 												scheme, csvt, assoc
 													.getAssociationName());
 								} catch (Exception ex) {
 									associationName = assoc.getAssociationName();
 								}
 
 								//associationName = assoc.getDirectionalName();
 
                                 boolean isRole = false;
                                 if (list.contains(associationName)) {
                                     isRole = true;
                                 }
 
                                 AssociatedConcept[] acl =
                                     assoc.getAssociatedConcepts()
                                         .getAssociatedConcept();
 
                                 for (int j = 0; j < acl.length; j++) {
                                     AssociatedConcept ac = acl[j];
 
                                     String ac_csn = ac.getCodingSchemeName();
 
                                     // [#26283] Remove self-referential
                                     // relationships.
                                     boolean include = true;
                                     if (ac.getConceptCode().compareTo(code) == 0)
                                         include = false;
 
                                     if (include) {
 
                                         EntityDescription ed =
                                             ac.getEntityDescription();
 
                                         String name = "No Description";
                                         if (ed != null)
                                             name = ed.getContent();
                                         String pt = name;
 
                                         if (associationName
                                             .compareToIgnoreCase("equivalentClass") != 0
                                             && ac.getConceptCode().indexOf("@") == -1) {
                                             if (!w.contains(associationName)) {
                                                 // String s = associationName +
                                                 // "|" + pt + "|" +
                                                 // ac.getConceptCode();
                                                 String relaValue =
                                                     replaceAssociationNameByRela(
                                                         ac, associationName);
 
 												StringBuffer buf = new StringBuffer();
                                                 buf.append(
                                                     relaValue + "|" + pt + "|"
                                                         + ac.getConceptCode() + "|"
                                                         + ac.getCodingSchemeName());
 
                                                 if (isMapping) {
 													if (ac.getAssociationQualifiers() != null) {
 														//String qualifiers = "";
 														StringBuffer buf3 = new StringBuffer();
 
 														for (NameAndValue qual : ac
 																.getAssociationQualifiers()
 																.getNameAndValue()) {
 															String qualifier_name = qual.getName();
 															String qualifier_value = qual.getContent();
 															//qualifiers = qualifiers + (qualifier_name + ":" + qualifier_value) + "$";
 															buf3.append((qualifier_name + ":" + qualifier_value) + "$");
 
 														    //System.out.println("qualifiers: " + qualifiers);
 
 														}
 														String qualifiers = buf3.toString();
 
 														buf.append("|" + qualifiers);
 														//s = s + "|" + qualifiers;
 													}
 													//s = s + "|" + ac.getCodeNamespace();
 													buf.append("|" + ac.getCodeNamespace());
 												}
 												String s = buf.toString();
 
 
                                                 if (isRole) {
                                                     // if
                                                     // (associationName.compareToIgnoreCase("hasSubtype")
                                                     // != 0) {
                                                     // _logger.debug("Adding role: "
                                                     // +
                                                     // s);
                                                     roleList.add(s);
                                                     // }
                                                 } else {
                                                     // _logger.debug("Adding association: "
                                                     // + s);
                                                     associationList.add(s);
 
                                                     //System.out.println("Add to association list: " + s);
                                                 }
 
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
 
             cng = lbSvc.getNodeGraph(scheme, csvt, null);
 
             if (isMapping) {
                  if (navl != null) {
 					 cng = cng.restrictToAssociations(navl, null);
  				 }
 			}
 
             matches = null;
             try {
                 /*
                  * CodedNodeSet.PropertyType[] propertyTypes = new
                  * CodedNodeSet.PropertyType[1]; propertyTypes[0] =
                  * PropertyType.PRESENTATION; int resolveCodedEntryDepth = 0;
                  */
                 matches =
                     cng.resolveAsList(cr,
                             // false, true, 0, 1, new LocalNameList(), null,
                             // null, 10000);
                             // false, true, 0, 1, null, new LocalNameList(),
                             // null, null, -1, false);
                             false, true, 0, 1, null, null, null, null, -1,
                             false);
                 /*
                  * matches = cng.resolveAsList(ConvenienceMethods
                  * .createConceptReference(code, scheme), //true, true, 1, 1,
                  * noopList_, null, null, null, -1, false); //true, true, 1, 1,
                  * noopList_, propertyTypes, null, null, -1, false); true, true,
                  * 0, 1, null, propertyTypes, null, null, -1, false);
                  */
 
             } catch (Exception e) {
                 _logger
                     .error("ERROR: DataUtils getRelationshipHashMap cng.resolveAsList throws exceptions."
                         + code);
             }
 
             if (matches != null
                 && matches.getResolvedConceptReferenceCount() > 0) {
                 Enumeration<? extends ResolvedConceptReference> refEnum =
                     matches.enumerateResolvedConceptReference();
 
                 while (refEnum.hasMoreElements()) {
                     ResolvedConceptReference ref = refEnum.nextElement();
 
                     // inverse roles and associations
                     AssociationList targetof = ref.getTargetOf();
                     if (targetof != null) {
                         Association[] inv_associations =
                             targetof.getAssociation();
                         if (inv_associations != null) {
                             for (int i = 0; i < inv_associations.length; i++) {
                                 Association assoc = inv_associations[i];
                                 // String associationName =
                                 // assoc.getAssociationName();
 
 
                                 String associationName = null;
                                 try {
                                     associationName = lbscm
                                         .getAssociationNameFromAssociationCode(
                                             scheme, csvt, assoc
                                                 .getAssociationName());
 							    } catch (Exception ex) {
 									associationName = assoc.getAssociationName();
 								}
 
 								//associationName = assoc.getDirectionalName();
 
                                 boolean isRole = false;
                                 if (list.contains(associationName)) {
                                     isRole = true;
                                 }
                                 AssociatedConcept[] acl =
                                     assoc.getAssociatedConcepts()
                                         .getAssociatedConcept();
                                 for (int j = 0; j < acl.length; j++) {
                                     AssociatedConcept ac = acl[j];
 
                                     // [#26283] Remove self-referential
                                     // relationships.
                                     boolean include = true;
                                     if (ac.getConceptCode().compareTo(code) == 0)
                                         include = false;
 
                                     if (include) {
 
                                         EntityDescription ed =
                                             ac.getEntityDescription();
 
                                         String name = "No Description";
                                         if (ed != null)
                                             name = ed.getContent();
 
                                         String pt = name;
 
                                         // [#24749] inverse association names
                                         // are empty for domain and range
                                         if (associationName.compareTo("domain") == 0
                                             || associationName
                                                 .compareTo("range") == 0) {
 
 											try {
 												pt =
 													lbscm
 														.getAssociationNameFromAssociationCode(
 															scheme, csvt, ac
 																.getConceptCode());
 											} catch (Exception ex) {
 												pt = ac.getConceptCode();
 											}
                                         }
 
                                         // if
                                         // (associationName.compareToIgnoreCase("equivalentClass")
                                         // != 0) {
                                         if (associationName
                                             .compareToIgnoreCase("equivalentClass") != 0
                                             && ac.getConceptCode().indexOf("@") == -1) {
 
                                             if (!w.contains(associationName)) {
                                                 // String s = associationName +
                                                 // "|" + pt + "|" +
                                                 // ac.getConceptCode();
                                                 String relaValue =
                                                     replaceAssociationNameByRela(
                                                         ac, associationName);
 
                                                 String s =
                                                     relaValue + "|" + pt + "|"
                                                          + ac.getConceptCode() + "|"
                                                          + ac.getCodingSchemeName();
 
 
                                                 if (isMapping) {
 													if (ac.getAssociationQualifiers() != null) {
 														//String qualifiers = "";
 														StringBuffer buf2 = new StringBuffer();
 														for (NameAndValue qual : ac
 																.getAssociationQualifiers()
 																.getNameAndValue()) {
 															String qualifier_name = qual.getName();
 															String qualifier_value = qual.getContent();
 															//qualifiers = qualifiers + (qualifier_name + ":" + qualifier_value) + "$";
 															buf2.append((qualifier_name + ":" + qualifier_value) + "$");
 														}
 														String qualifiers = buf2.toString();
 
 														s = s + "|" + qualifiers;
 													}
 													s = s + "|" + ac.getCodeNamespace();
 												}
 
                                                 if (isRole) {
                                                     inverse_roleList.add(s);
                                                 } else {
                                                     inverse_associationList
                                                         .add(s);
                                                     //System.out.println("Add to inverse_associationList list: " + s);
                                                 }
                                             }
 
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
 
             if (roleList.size() > 0) {
                 //Collections.sort(roleList);
                 SortUtils.quickSort(roleList);
 
             }
 
             if (associationList.size() > 0) {
                 //Collections.sort(associationList);
                 SortUtils.quickSort(associationList);
             }
 
             map.put(TYPE_ROLE, roleList);
             map.put(TYPE_ASSOCIATION, associationList);
 
             if (inverse_roleList.size() > 0) {
                 //Collections.sort(inverse_roleList);
                 SortUtils.quickSort(inverse_roleList);
             }
 
             if (inverse_associationList.size() > 0) {
                 //Collections.sort(inverse_associationList);
                 SortUtils.quickSort(inverse_associationList);
             }
 
             map.put(TYPE_INVERSE_ROLE, inverse_roleList);
             map.put(TYPE_INVERSE_ASSOCIATION, inverse_associationList);
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return map;
     }
 
     public Vector getSuperconcepts(String scheme, String version, String code) {
         // String assocName = "hasSubtype";
         String hierarchicalAssoName = "hasSubtype";
         Vector hierarchicalAssoName_vec =
             getHierarchyAssociationId(scheme, version);
         if (hierarchicalAssoName_vec != null
             && hierarchicalAssoName_vec.size() > 0) {
             hierarchicalAssoName =
                 (String) hierarchicalAssoName_vec.elementAt(0);
         }
         return getAssociationSources(scheme, version, code,
             hierarchicalAssoName);
     }
 
     public Vector getAssociationSources(String scheme, String version,
         String code, String assocName) {
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         if (version != null)
             csvt.setVersion(version);
         ResolvedConceptReferenceList matches = null;
         Vector v = new Vector();
         try {
             // EVSApplicationService lbSvc = new
             // RemoteServerUtil().createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodedNodeGraph cng = lbSvc.getNodeGraph(scheme, csvt, null);
             NameAndValueList nameAndValueList =
                 createNameAndValueList(new String[] { assocName }, null);
 
             NameAndValueList nameAndValueList_qualifier = null;
             cng =
                 cng.restrictToAssociations(nameAndValueList,
                     nameAndValueList_qualifier);
             ConceptReference graphFocus =
                 ConvenienceMethods.createConceptReference(code, scheme);
 
             boolean resolveForward = false;
             boolean resolveBackward = true;
 
             int resolveAssociationDepth = 1;
             int maxToReturn = 1000;
 
             ResolvedConceptReferencesIterator iterator =
                 codedNodeGraph2CodedNodeSetIterator(cng, graphFocus,
                     resolveForward, resolveBackward, resolveAssociationDepth,
                     maxToReturn);
 
             v = resolveIterator(iterator, maxToReturn, code);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return v;
     }
 
     public Vector getSubconcepts(String scheme, String version, String code) {
         // String assocName = "hasSubtype";
         String hierarchicalAssoName = "hasSubtype";
         Vector hierarchicalAssoName_vec =
             getHierarchyAssociationId(scheme, version);
         if (hierarchicalAssoName_vec != null
             && hierarchicalAssoName_vec.size() > 0) {
             hierarchicalAssoName =
                 (String) hierarchicalAssoName_vec.elementAt(0);
         }
         return getAssociationTargets(scheme, version, code,
             hierarchicalAssoName);
     }
 
     public Vector getAssociationTargets(String scheme, String version,
         String code, String assocName) {
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         if (version != null)
             csvt.setVersion(version);
         ResolvedConceptReferenceList matches = null;
         Vector v = new Vector();
         try {
             // EVSApplicationService lbSvc = new
             // RemoteServerUtil().createLexBIGService();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodedNodeGraph cng = lbSvc.getNodeGraph(scheme, csvt, null);
             NameAndValueList nameAndValueList =
                 createNameAndValueList(new String[] { assocName }, null);
 
             NameAndValueList nameAndValueList_qualifier = null;
             cng =
                 cng.restrictToAssociations(nameAndValueList,
                     nameAndValueList_qualifier);
             ConceptReference graphFocus =
                 ConvenienceMethods.createConceptReference(code, scheme);
 
             boolean resolveForward = true;
             boolean resolveBackward = false;
 
             int resolveAssociationDepth = 1;
             int maxToReturn = 1000;
 
             ResolvedConceptReferencesIterator iterator =
                 codedNodeGraph2CodedNodeSetIterator(cng, graphFocus,
                     resolveForward, resolveBackward, resolveAssociationDepth,
                     maxToReturn);
 
             v = resolveIterator(iterator, maxToReturn, code);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return v;
     }
 
     public ResolvedConceptReferencesIterator codedNodeGraph2CodedNodeSetIterator(
         CodedNodeGraph cng, ConceptReference graphFocus,
         boolean resolveForward, boolean resolveBackward,
         int resolveAssociationDepth, int maxToReturn) {
         CodedNodeSet cns = null;
         try {
             cns =
                 cng.toNodeList(graphFocus, resolveForward, resolveBackward,
                     resolveAssociationDepth, maxToReturn);
 
             if (cns == null) {
                 _logger.warn("cng.toNodeList returns null???");
                 return null;
             }
 
             SortOptionList sortCriteria = null;
             // Constructors.createSortOptionList(new String[]{"matchToQuery",
             // "code"});
 
             LocalNameList propertyNames = null;
             CodedNodeSet.PropertyType[] propertyTypes = null;
             ResolvedConceptReferencesIterator iterator = null;
             try {
                 iterator =
                     cns.resolve(sortCriteria, propertyNames, propertyTypes);
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             if (iterator == null) {
                 _logger.warn("cns.resolve returns null???");
             }
             return iterator;
 
         } catch (Exception ex) {
             ex.printStackTrace();
             return null;
         }
     }
 
     public Vector resolveIterator(ResolvedConceptReferencesIterator iterator,
         int maxToReturn) {
         return resolveIterator(iterator, maxToReturn, null);
     }
 
     public Vector resolveIterator(ResolvedConceptReferencesIterator iterator,
         int maxToReturn, String code) {
         Vector v = new Vector();
         if (iterator == null) {
             _logger.warn("No match.");
             return v;
         }
         try {
             int iteration = 0;
             while (iterator.hasNext()) {
                 iteration++;
                 iterator = iterator.scroll(maxToReturn);
                 ResolvedConceptReferenceList rcrl = iterator.getNext();
                 ResolvedConceptReference[] rcra =
                     rcrl.getResolvedConceptReference();
                 for (int i = 0; i < rcra.length; i++) {
                     ResolvedConceptReference rcr = rcra[i];
                     org.LexGrid.concepts.Entity ce = rcr.getReferencedEntry();
                     // _logger.debug("Iteration " + iteration + " " +
                     // ce.getEntityCode() + " " +
                     // ce.getEntityDescription().getContent());
                     if (code == null) {
                         v.add(ce);
                     } else {
                         if (ce.getEntityCode().compareTo(code) != 0)
                             v.add(ce);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return v;
     }
 
     public static Vector<String> parseData(String line) {
 		if (line == null) return null;
         String tab = "|";
         return parseData(line, tab);
     }
 
     public static Vector<String> parseData(String line, String tab) {
 		if (line == null) return null;
         Vector data_vec = new Vector();
         StringTokenizer st = new StringTokenizer(line, tab);
         while (st.hasMoreTokens()) {
             String value = st.nextToken();
             if (value.compareTo("null") == 0)
                 value = " ";
             data_vec.add(value);
         }
         return data_vec;
     }
 
     public static String getHyperlink(String url, String codingScheme,
         String code) {
         codingScheme = codingScheme.replace(" ", "%20");
         String link =
             url + "/ConceptReport.jsp?dictionary=" + codingScheme + "&code="
                 + code;
         return link;
     }
 
     public static Vector getSynonyms(String scheme, String version, String tag,
         String code) {
         //Vector v = new Vector();
         Entity concept = getConceptByCode(scheme, version, tag, code);
         // KLO, 091009
         // getSynonyms(concept);
         return getSynonyms(scheme, concept);
     }
 
     /*
      * public static Vector getSynonyms(Concept concept) { if (concept == null)
      * return null; Vector v = new Vector(); Presentation[] properties =
      * concept.getPresentation(); int n = 0; for (int i = 0; i <
      * properties.length; i++) { Presentation p = properties[i]; //if
      * (p.getPropertyName().compareTo("FULL_SYN") == 0) { String term_name =
      * p.getValue().getContent(); String term_type = "null"; String term_source
      * = "null"; String term_source_code = "null";
      *
      * PropertyQualifier[] qualifiers = p.getPropertyQualifier(); if (qualifiers
      * != null) { for (int j = 0; j < qualifiers.length; j++) {
      * PropertyQualifier q = qualifiers[j]; String qualifier_name =
      * q.getPropertyQualifierName(); String qualifier_value =
      * q.getValue().getContent(); if (qualifier_name.compareTo("source-code") ==
      * 0) { term_source_code = qualifier_value; break; } } } term_type =
      * p.getRepresentationalForm(); Source[] sources = p.getSource(); if
      * (sources != null && sources.length > 0) { Source src = sources[0];
      * term_source = src.getContent(); } v.add(term_name + "|" + term_type + "|"
      * + term_source + "|" + term_source_code); //} } SortUtils.quickSort(v);
      * return v; }
      */
 
     public static Vector getSynonyms(Entity concept) {
         if (concept == null)
             return null;
         Vector v = new Vector();
         Presentation[] properties = concept.getPresentation();
         int n = 0;
         for (int i = 0; i < properties.length; i++) {
             Presentation p = properties[i];
             String term_name = p.getValue().getContent();
             String term_type = "null";
             String term_source = "null";
             String term_source_code = "null";
 
             PropertyQualifier[] qualifiers = p.getPropertyQualifier();
             if (qualifiers != null) {
                 for (int j = 0; j < qualifiers.length; j++) {
                     PropertyQualifier q = qualifiers[j];
                     String qualifier_name = q.getPropertyQualifierName();
                     String qualifier_value = q.getValue().getContent();
                     if (qualifier_name.compareTo("source-code") == 0) {
                         term_source_code = qualifier_value;
                         break;
                     }
                 }
             }
             term_type = p.getRepresentationalForm();
             Source[] sources = p.getSource();
             if (sources != null && sources.length > 0) {
                 Source src = sources[0];
                 term_source = src.getContent();
             }
             v.add(term_name + "|" + term_type + "|" + term_source + "|"
                 + term_source_code);
 
         }
         SortUtils.quickSort(v);
         return v;
     }
 
     public static Vector getSynonyms(String scheme, Entity concept) {
         if (concept == null)
             return null;
         Vector v = new Vector();
         Presentation[] properties = concept.getPresentation();
         int n = 0;
         boolean inclusion = true;
         for (int i = 0; i < properties.length; i++) {
             Presentation p = properties[i];
 
             // for NCI Thesaurus or Pre-NCI Thesaurus, show FULL_SYNs only
             if (scheme != null && scheme.indexOf(CODING_SCHEME_NAME) != -1) {
                 inclusion = false;
                 if (p.getPropertyName().compareTo("FULL_SYN") == 0) {
                     inclusion = true;
                 }
             }
 
             if (inclusion) {
                 String term_name = p.getValue().getContent();
                 String term_type = "null";
                 String term_source = "null";
                 String term_source_code = "null";
 
                 PropertyQualifier[] qualifiers = p.getPropertyQualifier();
                 if (qualifiers != null) {
                     for (int j = 0; j < qualifiers.length; j++) {
                         PropertyQualifier q = qualifiers[j];
                         String qualifier_name = q.getPropertyQualifierName();
                         String qualifier_value = q.getValue().getContent();
                         if (qualifier_name.compareTo("source-code") == 0) {
                             term_source_code = qualifier_value;
                             break;
                         }
                     }
                 }
                 term_type = p.getRepresentationalForm();
                 Source[] sources = p.getSource();
                 if (sources != null && sources.length > 0) {
                     Source src = sources[0];
                     term_source = src.getContent();
                 }
                 v.add(term_name + "|" + term_type + "|" + term_source + "|"
                     + term_source_code);
             }
         }
         SortUtils.quickSort(v);
         return v;
     }
 
     public String getNCICBContactURL() {
         if (_ncicbContactURL != null) {
             return _ncicbContactURL;
         }
         String default_url = "ncicb@pop.nci.nih.gov";
         NCImtBrowserProperties properties = null;
         try {
             properties = NCImtBrowserProperties.getInstance();
             _ncicbContactURL =
                 properties.getProperty(NCImtBrowserProperties.NCICB_CONTACT_URL);
             if (_ncicbContactURL == null) {
                 _ncicbContactURL = default_url;
             }
         } catch (Exception ex) {
 
         }
 
         // _logger.debug("getNCICBContactURL returns " + NCICBContactURL);
         return _ncicbContactURL;
     }
 
     public String getTerminologySubsetDownloadURL() {
         NCImtBrowserProperties properties = null;
         try {
             properties = NCImtBrowserProperties.getInstance();
             _terminologySubsetDownloadURL =
                 properties
                     .getProperty(NCImtBrowserProperties.TERMINOLOGY_SUBSET_DOWNLOAD_URL);
         } catch (Exception ex) {
 
         }
         return _terminologySubsetDownloadURL;
     }
 
     public String getNCITBuildInfo() {
         if (_ncitBuildInfo != null) {
             return _ncitBuildInfo;
         }
         String default_info = "N/A";
         NCImtBrowserProperties properties = null;
         try {
             properties = NCImtBrowserProperties.getInstance();
             _ncitBuildInfo =
                 properties.getProperty(NCImtBrowserProperties.NCIT_BUILD_INFO);
             if (_ncitBuildInfo == null) {
                 _ncitBuildInfo = default_info;
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         return _ncitBuildInfo;
     }
 
     public String getApplicationVersion() {
         if (_ncitAppVersion != null) {
             return _ncitAppVersion;
         }
         String default_info = "1.0";
         NCImtBrowserProperties properties = null;
         try {
             properties = NCImtBrowserProperties.getInstance();
             _ncitAppVersion =
                 properties.getProperty(NCImtBrowserProperties.NCIT_APP_VERSION);
             if (_ncitAppVersion == null) {
                 _ncitAppVersion = default_info;
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         return _ncitAppVersion;
     }
 
     public String getNCITAnthillBuildTagBuilt() {
         if (_ncitAnthillBuildTagBuilt != null) {
             return _ncitAnthillBuildTagBuilt;
         }
         String default_info = "N/A";
         NCImtBrowserProperties properties = null;
         try {
             properties = NCImtBrowserProperties.getInstance();
             _ncitAnthillBuildTagBuilt =
                 properties
                     .getProperty(NCImtBrowserProperties.ANTHILL_BUILD_TAG_BUILT);
             if (_ncitAnthillBuildTagBuilt == null) {
                 _ncitAnthillBuildTagBuilt = default_info;
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         return _ncitAnthillBuildTagBuilt;
     }
 
     public String getNCItURL() {
         if (_ncitURL != null) {
             return _ncitURL;
         }
         String default_info = "N/A";
         NCImtBrowserProperties properties = null;
         try {
             properties = NCImtBrowserProperties.getInstance();
             _ncitURL = properties.getProperty(NCImtBrowserProperties.NCIT_URL);
             if (_ncitURL == null) {
                 _ncitURL = default_info;
             }
         } catch (Exception ex) {
 
         }
         return _ncitURL;
     }
 
 
     public String getNCImURL() {
         if (_ncimURL != null) {
             return _ncimURL;
         }
         String default_info = "N/A";
         NCImtBrowserProperties properties = null;
         try {
             properties = NCImtBrowserProperties.getInstance();
             _ncimURL = properties.getProperty(NCImtBrowserProperties.NCIM_URL);
             if (_ncimURL == null) {
                 _ncimURL = default_info;
             }
         } catch (Exception ex) {
 
         }
         return _ncimURL;
     }
 
     public String getEVSServiceURL() {
         if (_evsServiceURL != null) {
             return _evsServiceURL;
         }
         String default_info = "Local LexEVS";
         NCImtBrowserProperties properties = null;
         try {
             properties = NCImtBrowserProperties.getInstance();
             _evsServiceURL =
                 properties.getProperty(NCImtBrowserProperties.EVS_SERVICE_URL);
             if (_evsServiceURL == null) {
                 _evsServiceURL = default_info;
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         return _evsServiceURL;
     }
 
     public String getTermSuggestionURL() {
         NCImtBrowserProperties properties = null;
         try {
             properties = NCImtBrowserProperties.getInstance();
             _term_suggestion_application_url =
                 properties
                     .getProperty(NCImtBrowserProperties.TERM_SUGGESTION_APPLICATION_URL);
         } catch (Exception ex) {
 
         }
         return _term_suggestion_application_url;
     }
 
     // /////////////////////////////////////////////////////////////////////////////
 
     public static CodingScheme resolveCodingScheme(LexBIGService lbSvc,
         String formalname, CodingSchemeVersionOrTag versionOrTag) {
         try {
             CodingScheme cs =
                 lbSvc.resolveCodingScheme(formalname, versionOrTag);
             return cs;
         } catch (Exception ex) {
             _logger.error("(*) Unable to resolveCodingScheme " + formalname);
             _logger.error("(*) \tMay require security token. ");
 
         }
         return null;
     }
 
     public static HashMap getNamespaceId2CodingSchemeFormalNameMapping() {
         if (_namespace2CodingScheme != null) {
             return _namespace2CodingScheme;
         }
 
         HashMap hmap = new HashMap();
         LexBIGService lbSvc = new RemoteServerUtil().createLexBIGService();
         if (lbSvc == null) {
             _logger
                 .error("ERROR: setCodingSchemeMap RemoteServerUtil().createLexBIGService() returns null.");
             return null;
         }
 
         CodingSchemeRenderingList csrl = null;
         CodingSchemeRendering[] csrs = null;
 
         try {
             csrl = lbSvc.getSupportedCodingSchemes();
             csrs = csrl.getCodingSchemeRendering();
         } catch (LBInvocationException ex) {
             _logger.error("lbSvc.getSupportedCodingSchemes() FAILED..."
                 + ex.getCause());
             return null;
         }
 
         for (int i = 0; i < csrs.length; i++) {
             CodingSchemeRendering csr = csrs[i];
             if (csr != null && csr.getRenderingDetail() != null) {
                 Boolean isActive = null;
                 if (csr.getRenderingDetail() == null) {
                     _logger.warn("\tcsr.getRenderingDetail() == null");
                 } else if (csr.getRenderingDetail().getVersionStatus() == null) {
                     _logger
                         .warn("\tcsr.getRenderingDetail().getVersionStatus() == null");
                 } else {
 
                     isActive =
                         csr.getRenderingDetail().getVersionStatus().equals(
                             CodingSchemeVersionStatus.ACTIVE);
                 }
 
                 // if (isActive != null && isActive.equals(Boolean.TRUE))
                 {
                     CodingSchemeSummary css = csr.getCodingSchemeSummary();
                     String formalname = css.getFormalName();
                     java.lang.String version = css.getRepresentsVersion();
                     CodingSchemeVersionOrTag versionOrTag =
                         new CodingSchemeVersionOrTag();
                     if (version != null)
                         versionOrTag.setVersion(version);
 
                     CodingScheme cs = null;
                     cs = resolveCodingScheme(lbSvc, formalname, versionOrTag);
                     if (cs != null) {
                         Mappings mapping = cs.getMappings();
                         if (mapping != null) {
                             SupportedNamespace[] namespaces =
                                 mapping.getSupportedNamespace();
                             if (namespaces != null) {
                                 for (int j = 0; j < namespaces.length; j++) {
                                     SupportedNamespace ns = namespaces[j];
                                     if (ns != null) {
                                         java.lang.String ns_name =
                                             ns.getEquivalentCodingScheme();
                                         java.lang.String ns_id =
                                             ns.getContent();
                                         // _logger.debug("\tns_name: " + ns_name
                                         // + " ns_id:" + ns_id);
                                         if (ns_id != null
                                             && ns_id.compareTo("") != 0) {
 
                                             hmap.put(ns_id, formalname);
                                             // _logger.debug("ns_id: " + ns_id +
                                             // " -> " + formalname);
 
                                         }
                                     }
                                 }
                             }
                         }
                     } else {
                         _logger.warn("??? Unable to resolveCodingScheme "
                             + formalname);
                     }
                 }
             }
         }
 
         _namespace2CodingScheme = hmap;
         return hmap;
     }
 
     public static String getCodingSchemeName(String key) {
         return key2CodingSchemeName(key);
     }
 
     public static String getCodingSchemeVersion(String key) {
         return key2CodingSchemeVersion(key);
     }
 
     public static String key2CodingSchemeName(String key) {
         if (key == null) {
             return null;
         }
         if (_csnv2codingSchemeNameMap == null) {
             _logger.debug("setCodingSchemeMap");
             setCodingSchemeMap();
         }
 
         if (key.indexOf("%20") != -1) {
             key = key.replaceAll("%20", " ");
         }
 
         if (_csnv2codingSchemeNameMap == null) {
             _logger.warn("csnv2codingSchemeNameMap == NULL???");
             return key;
         }
 
         String value = (String) _csnv2codingSchemeNameMap.get(key);
         if (value == null) {
             // _logger.debug("key2CodingSchemeName returns " + key);
             return key;
         }
         return value;
     }
 
     public static String key2CodingSchemeVersion(String key) {
         if (key == null) {
             return null;
         }
         if (_csnv2VersionMap == null)
             setCodingSchemeMap();
         if (key.indexOf("%20") != -1) {
             key = key.replaceAll("%20", " ");
         }
 
         if (_csnv2VersionMap == null) {
             _logger.warn("csnv2VersionMap == NULL???");
             return key;
         }
 
         String value = (String) _csnv2VersionMap.get(key);
         return value;
     }
 
     public static String replaceAll(String s, String t1, String t2) {
         s = s.replaceAll(t1, t2);
         return s;
     }
 
     // ////////////////////////////////////////////////////////////////
 
     public static String getDownloadLink(String url) {
         String t =
             "<a href=\"" + url + "\" target=\"_blank\" alt=\"Download Site\">"
                 + url + "</a>";
         return t;
     }
 
     /*
      * To convert a string to the URL-encoded form suitable for transmission as
      * a query string (or, generally speaking, as part of a URL), use the escape
      * function. This function works as follows: digits, Latin letters and the
      * characters + - * / . _ @ remain unchanged; all other characters in the
      * original string are replaced by escape-sequences %XX, where XX is the
      * ASCII code of the original character. Example: escape("It's me!") //
      * result: It%27s%20me%21
      */
 
     public static String htmlEntityEncode(String s) {
         StringBuilder buf = new StringBuilder(s.length());
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0'
                 && c <= '9') {
                 buf.append(c);
             } else {
                 buf.append("&#").append((int) c).append(";");
             }
         }
         return buf.toString();
     }
 
     public void dumpRelationshipHashMap(HashMap hmap) {
         ArrayList superconcepts =
             (ArrayList) hmap.get(DataUtils.TYPE_SUPERCONCEPT);
         ArrayList subconcepts = (ArrayList) hmap.get(DataUtils.TYPE_SUBCONCEPT);
         ArrayList roles = (ArrayList) hmap.get(DataUtils.TYPE_ROLE);
         ArrayList associations =
             (ArrayList) hmap.get(DataUtils.TYPE_ASSOCIATION);
 
         ArrayList inverse_roles =
             (ArrayList) hmap.get(DataUtils.TYPE_INVERSE_ROLE);
         ArrayList inverse_associations =
             (ArrayList) hmap.get(DataUtils.TYPE_INVERSE_ASSOCIATION);
         ArrayList concepts = null;
 
         concepts = superconcepts;
         String label = "\nParent Concepts:";
 
         if (concepts == null || concepts.size() <= 0) {
             _logger.debug(label + "(none)");
         } else if (concepts.size() == 1) {
             String s = (String) concepts.get(0);
             Vector ret_vec = DataUtils.parseData(s, "|");
             String cName = (String) ret_vec.elementAt(0);
             String cCode = (String) ret_vec.elementAt(1);
 
             _logger.debug(label + " " + cName + "(" + cCode + ")");
         } else {
             _logger.debug(label);
             for (int i = 0; i < concepts.size(); i++) {
                 String s = (String) concepts.get(i);
                 Vector ret_vec = DataUtils.parseData(s, "|");
                 String cName = (String) ret_vec.elementAt(0);
                 String cCode = (String) ret_vec.elementAt(1);
                 _logger.debug("\t" + " " + cName + "(" + cCode + ")");
             }
         }
 
         concepts = subconcepts;
         label = "\nChild Concepts:";
 
         if (concepts == null || concepts.size() <= 0) {
             _logger.debug(label + "(none)");
         } else if (concepts.size() == 1) {
             String s = (String) concepts.get(0);
             Vector ret_vec = DataUtils.parseData(s, "|");
             String cName = (String) ret_vec.elementAt(0);
             String cCode = (String) ret_vec.elementAt(1);
 
             _logger.debug(label + " " + cName + "(" + cCode + ")");
         } else {
             _logger.debug(label);
             for (int i = 0; i < concepts.size(); i++) {
                 String s = (String) concepts.get(i);
                 Vector ret_vec = DataUtils.parseData(s, "|");
                 String cName = (String) ret_vec.elementAt(0);
                 String cCode = (String) ret_vec.elementAt(1);
                 _logger.debug("\t" + " " + cName + "(" + cCode + ")");
             }
         }
 
         concepts = roles;
         label = "\nRoles:";
 
         if (concepts == null || concepts.size() <= 0) {
             _logger.debug(label + "(none)");
         } else if (concepts.size() == 1) {
             String s = (String) concepts.get(0);
             Vector ret_vec = DataUtils.parseData(s, "|");
             String cName = (String) ret_vec.elementAt(0);
             String cCode = (String) ret_vec.elementAt(1);
 
             _logger.debug(label + " " + cName + "(" + cCode + ")");
         } else {
             _logger.debug(label);
             for (int i = 0; i < concepts.size(); i++) {
                 String s = (String) concepts.get(i);
                 Vector ret_vec = DataUtils.parseData(s, "|");
                 String cName = (String) ret_vec.elementAt(0);
                 String cCode = (String) ret_vec.elementAt(1);
                 _logger.debug("\t" + " " + cName + "(" + cCode + ")");
             }
         }
         concepts = associations;
         label = "\nAssociations:";
 
         if (concepts == null || concepts.size() <= 0) {
             _logger.debug(label + "(none)");
         } else if (concepts.size() == 1) {
             String s = (String) concepts.get(0);
             Vector ret_vec = DataUtils.parseData(s, "|");
             String cName = (String) ret_vec.elementAt(0);
             String cCode = (String) ret_vec.elementAt(1);
 
             _logger.debug(label + " " + cName + "(" + cCode + ")");
         } else {
             _logger.debug(label);
             for (int i = 0; i < concepts.size(); i++) {
                 String s = (String) concepts.get(i);
                 Vector ret_vec = DataUtils.parseData(s, "|");
                 String cName = (String) ret_vec.elementAt(0);
                 String cCode = (String) ret_vec.elementAt(1);
                 _logger.debug("\t" + " " + cName + "(" + cCode + ")");
             }
         }
 
         concepts = inverse_roles;
         label = "\nInverse Roles:";
 
         if (concepts == null || concepts.size() <= 0) {
             _logger.debug(label + "(none)");
         } else if (concepts.size() == 1) {
             String s = (String) concepts.get(0);
             Vector ret_vec = DataUtils.parseData(s, "|");
             String cName = (String) ret_vec.elementAt(0);
             String cCode = (String) ret_vec.elementAt(1);
 
             _logger.debug(label + " " + cName + "(" + cCode + ")");
         } else {
             _logger.debug(label);
             for (int i = 0; i < concepts.size(); i++) {
                 String s = (String) concepts.get(i);
                 Vector ret_vec = DataUtils.parseData(s, "|");
                 String cName = (String) ret_vec.elementAt(0);
                 String cCode = (String) ret_vec.elementAt(1);
                 _logger.debug("\t" + " " + cName + "(" + cCode + ")");
             }
         }
         concepts = inverse_associations;
         label = "\nInverse Associations:";
 
         if (concepts == null || concepts.size() <= 0) {
             _logger.debug(label + "(none)");
         } else if (concepts.size() == 1) {
             String s = (String) concepts.get(0);
             Vector ret_vec = DataUtils.parseData(s, "|");
             String cName = (String) ret_vec.elementAt(0);
             String cCode = (String) ret_vec.elementAt(1);
 
             _logger.debug(label + " " + cName + "(" + cCode + ")");
         } else {
             _logger.debug(label);
             for (int i = 0; i < concepts.size(); i++) {
                 String s = (String) concepts.get(i);
                 Vector ret_vec = DataUtils.parseData(s, "|");
                 String cName = (String) ret_vec.elementAt(0);
                 String cCode = (String) ret_vec.elementAt(1);
                 _logger.debug("\t" + " " + cName + "(" + cCode + ")");
             }
         }
 
     }
 
     public static Vector getStatusByConceptCodes(String scheme, String version,
         String ltag, Vector codes) {
         boolean conceptStatusSupported = false;
         if (_vocabulariesWithConceptStatusHashSet.contains(scheme))
             conceptStatusSupported = true;
 
         Vector w = new Vector();
         long ms = System.currentTimeMillis();
         for (int i = 0; i < codes.size(); i++) {
             if (conceptStatusSupported) {
                 String code = (String) codes.elementAt(i);
                 Entity c = getConceptByCode(scheme, version, ltag, code);
                 if (c != null) {
                     w.add(c.getStatus());
                 } else {
                     _logger
                         .warn("WARNING: getStatusByConceptCode returns null on "
                             + scheme + " code: " + code);
                     w.add(null);
                 }
             } else {
                 w.add(null);
             }
         }
         _logger.debug("getStatusByConceptCodes Run time (ms): "
             + (System.currentTimeMillis() - ms) + " number of concepts: "
             + codes.size());
         return w;
     }
 
 
     public static String getConceptStatus(String scheme, String version,
         String ltag, String code) {
         boolean conceptStatusSupported = false;
         if (_vocabulariesWithConceptStatusHashSet.contains(scheme))
             conceptStatusSupported = true;
         if (!conceptStatusSupported)
             return null;
 
         Entity c =
             getConceptWithProperty(scheme, version, code, "Concept_Status");
         String con_status = null;
         if (c != null) {
             Vector status_vec = getConceptPropertyValues(c, "Concept_Status");
             if (status_vec == null || status_vec.size() == 0) {
                 con_status = c.getStatus();
             } else {
                 con_status = DataUtils.convertToCommaSeparatedValue(status_vec);
             }
             return con_status;
         }
         return null;
     }
 
     public static Vector getConceptStatusByConceptCodes(String scheme,
         String version, String ltag, Vector codes) {
         boolean conceptStatusSupported = false;
         if (_vocabulariesWithConceptStatusHashSet.contains(scheme))
             conceptStatusSupported = true;
 
         Vector w = new Vector();
         long ms = System.currentTimeMillis();
         for (int i = 0; i < codes.size(); i++) {
             if (conceptStatusSupported) {
                 String code = (String) codes.elementAt(i);
                 Entity c =
                     getConceptWithProperty(scheme, version, code,
                         "Concept_Status");
                 String con_status = null;
                 if (c != null) {
                     Vector status_vec =
                         getConceptPropertyValues(c, "Concept_Status");
                     if (status_vec == null || status_vec.size() == 0) {
                         con_status = c.getStatus();
                     } else {
                         con_status =
                             DataUtils.convertToCommaSeparatedValue(status_vec);
                     }
                     w.add(con_status);
                 } else {
                     w.add(null);
                 }
             } else {
                 w.add(null);
             }
         }
         _logger.debug("getConceptStatusByConceptCodes Run time (ms): "
             + (System.currentTimeMillis() - ms) + " number of concepts: "
             + codes.size());
         return w;
     }
 
     // for HL7 fix
     public static String searchFormalName(String s) {
         if (s == null)
             return null;
 
         if (_formalName2LocalNameHashMap == null) {
             setCodingSchemeMap();
         }
 
         if (_formalName2LocalNameHashMap.containsKey(s))
             return s;
         Iterator it = _formalName2LocalNameHashMap.keySet().iterator();
         while (it.hasNext()) {
             String t = (String) it.next();
             String t0 = t;
             t = t.trim();
             if (t.compareTo(s) == 0)
                 return t0;
         }
         return s;
     }
 
     public static String getFormalNameByDisplayName(String s) {
         if (_displayName2FormalNameHashMap == null)
             setCodingSchemeMap();
         return (String) _displayName2FormalNameHashMap.get(s);
     }
 
     public static void main(String[] args) {
         String scheme = "NCI Thesaurus";
         String version = null;
         // Breast Carcinoma (Code C4872)
         String code = "C4872";
 
         DataUtils test = new DataUtils();
 
         HashMap hmap = test.getRelationshipHashMap(scheme, version, code);
         test.dumpRelationshipHashMap(hmap);
 
     }
 
 
 
     // [#25034] Remove hyperlink from instances on the Relationship tab. (KLO,
     // 121709)
     public static boolean isNonConcept2ConceptAssociation(String associationName) {
 		/*
         if (_nonConcept2ConceptAssociations == null) {
             _nonConcept2ConceptAssociations = new Vector();
             _nonConcept2ConceptAssociations.add("domain");
             _nonConcept2ConceptAssociations.add("range");
             _nonConcept2ConceptAssociations.add("instance");
         }
         */
         if (associationName == null)
             return false;
         return Arrays.asList(_nonConcept2ConceptAssociations).contains(associationName);
     }
 
     // [#25027] Encountering "Service Temporarily Unavailable" on display of
     // last search results page (see NCIm #24585) (KLO, 121709)
     public HashMap getPropertyValuesForCodes(String scheme, String version,
         Vector codes, String propertyName) {
         try {
             LexBIGService lbSvc = new RemoteServerUtil().createLexBIGService();
 
             if (lbSvc == null) {
                 _logger.warn("lbSvc = null");
                 return null;
             }
 
             CodingSchemeVersionOrTag versionOrTag =
                 new CodingSchemeVersionOrTag();
             versionOrTag.setVersion(version);
 
             ConceptReferenceList crefs =
                 createConceptReferenceList(codes, scheme);
 
             CodedNodeSet cns = null;
 
             try {
                 cns = lbSvc.getCodingSchemeConcepts(scheme, versionOrTag);
             } catch (Exception e1) {
                 e1.printStackTrace();
             }
 
             if (cns == null) return null;
 
             cns = cns.restrictToCodes(crefs);
 
             try {
                 LocalNameList propertyNames = new LocalNameList();
                 propertyNames.addEntry(propertyName);
                 CodedNodeSet.PropertyType[] propertyTypes = null;
 
                 //long ms = System.currentTimeMillis(), delay = 0;
                 SortOptionList sortOptions = null;
                 LocalNameList filterOptions = null;
                 boolean resolveObjects = true; // needs to be set to true
                 int maxToReturn = 1000;
 
                 ResolvedConceptReferenceList rcrl =
                     cns.resolveToList(sortOptions, filterOptions,
                         propertyNames, propertyTypes, resolveObjects,
                         maxToReturn);
 
                 // _logger.debug("resolveToList done");
                 HashMap hmap = new HashMap();
 
                 if (rcrl == null) {
                     _logger.debug("Concept not found.");
                     return null;
                 }
 
                 if (rcrl.getResolvedConceptReferenceCount() > 0) {
                     // ResolvedConceptReference[] list =
                     // rcrl.getResolvedConceptReference();
                     for (int i = 0; i < rcrl.getResolvedConceptReferenceCount(); i++) {
                         ResolvedConceptReference rcr =
                             rcrl.getResolvedConceptReference(i);
                         // _logger.debug("(*) " + rcr.getCode());
                         Entity c = rcr.getReferencedEntry();
                         if (c == null) {
                             _logger.debug("Concept is null.");
                         } else {
                             _logger
                                 .debug(c.getEntityDescription().getContent());
                             Property[] properties = c.getProperty();
                             //String values = "";
                             StringBuffer buf = new StringBuffer();
                             for (int j = 0; j < properties.length; j++) {
                                 Property prop = properties[j];
                                 //values = values + prop.getValue().getContent();
                                 buf.append(prop.getValue().getContent());
                                 if (j < properties.length - 1) {
                                     //values = values + "; ";
                                     buf.append("; ");
                                 }
                             }
                             String values = buf.toString();
                             hmap.put(rcr.getCode(), values);
                         }
                     }
                 }
                 return hmap;
 
             } catch (Exception e) {
                 _logger.error("Method: SearchUtil.searchByProperties");
                 _logger.error("* ERROR: cns.resolve throws exceptions.");
                 _logger.error("* " + e.getClass().getSimpleName() + ": "
                     + e.getMessage());
                 e.printStackTrace();
             }
         } catch (Exception ex) {
 			ex.printStackTrace();
         }
         return null;
     }
 
     public static Entity getConceptWithProperty(String scheme, String version,
         String code, String propertyName) {
         try {
             LexBIGService lbSvc = new RemoteServerUtil().createLexBIGService();
 
             if (lbSvc == null) {
                 _logger.warn("lbSvc = null");
                 return null;
             }
 
             CodingSchemeVersionOrTag versionOrTag =
                 new CodingSchemeVersionOrTag();
             if (version != null) versionOrTag.setVersion(version);
 
             ConceptReferenceList crefs =
                 createConceptReferenceList(new String[] { code }, scheme);
             CodedNodeSet cns = null;
 
             try {
                 cns = lbSvc.getCodingSchemeConcepts(scheme, versionOrTag);
             } catch (Exception e1) {
                 e1.printStackTrace();
             }
             if (cns == null) return null;
 
             cns = cns.restrictToCodes(crefs);
 
             try {
                 LocalNameList propertyNames = new LocalNameList();
                 if (propertyName != null) propertyNames.addEntry(propertyName);
                 CodedNodeSet.PropertyType[] propertyTypes = null;
 
                 //long ms = System.currentTimeMillis(), delay = 0;
                 SortOptionList sortOptions = null;
                 LocalNameList filterOptions = null;
                 boolean resolveObjects = true; // needs to be set to true
                 int maxToReturn = 1000;
 
                 ResolvedConceptReferenceList rcrl =
                     cns.resolveToList(sortOptions, filterOptions,
                         propertyNames, propertyTypes, resolveObjects,
                         maxToReturn);
 
                 //HashMap hmap = new HashMap();
                 if (rcrl == null) {
                     _logger.warn("Concep not found.");
                     return null;
                 }
 
                 if (rcrl.getResolvedConceptReferenceCount() > 0) {
                     // ResolvedConceptReference[] list =
                     // rcrl.getResolvedConceptReference();
                     for (int i = 0; i < rcrl.getResolvedConceptReferenceCount(); i++) {
                         ResolvedConceptReference rcr =
                             rcrl.getResolvedConceptReference(i);
                         Entity c = rcr.getReferencedEntry();
                         return c;
                     }
                 }
 
                 return null;
 
             } catch (Exception e) {
                 _logger.error("Method: SearchUtil.getConceptWithProperty");
                 _logger.error("* ERROR: getConceptWithProperty throws exceptions.");
                 _logger.error("* " + e.getClass().getSimpleName() + ": "
                     + e.getMessage());
                 //e.printStackTrace();
             }
         } catch (Exception ex) {
                 _logger.error("Method: SearchUtil.getConceptWithProperty");
                 _logger.error("* ERROR: getConceptWithProperty throws exceptions.");
                 _logger.error("* " + ex.getClass().getSimpleName() + ": "
                     + ex.getMessage());
 
         }
         return null;
     }
 
     public static Vector getConceptPropertyValues(Entity c, String propertyName) {
         if (c == null)
             return null;
         Vector v = new Vector();
         Property[] properties = c.getProperty();
 
         for (int j = 0; j < properties.length; j++) {
             Property prop = properties[j];
             //int i = j+1;
             //System.out.println("(" + i + ")" + prop.getPropertyName());
 
             if (prop.getPropertyName().compareTo(propertyName) == 0) {
                 v.add(prop.getValue().getContent());
             }
         }
         return v;
     }
 
     public static String convertToCommaSeparatedValue(Vector v) {
         if (v == null)
             return null;
         //String s = "";
         StringBuffer buf = new StringBuffer();
         if (v.size() == 0)
             return buf.toString();//s;
         //s = (String) v.elementAt(0);
         buf.append((String) v.elementAt(0));
         for (int i = 1; i < v.size(); i++) {
             String next = (String) v.elementAt(i);
             //s = s + "; " + next;
             buf.append("; " + next);
         }
         return buf.toString();//s;
     }
 
     public static String replaceInnerEvalExpressions(String s, Vector from_vec,
         Vector to_vec) {
         String openExp = "<%=";
         String closeExp = "%>";
         //String t = "";
         StringBuffer buf = new StringBuffer();
 
         int idx = s.indexOf(openExp);
         if (idx == -1)
             return s;
 
         while (idx != -1) {
             String lhs = s.substring(0, idx);
             //t = t + lhs;
             buf.append(lhs);
 
             String res = s.substring(idx + 3, s.length());
             int idx2 = s.indexOf(closeExp);
 
             String expression = s.substring(idx, idx2 + 2);
 
             String expressionValue = s.substring(idx + 3, idx2);
 
             for (int i = 0; i < from_vec.size(); i++) {
                 String from = (String) from_vec.elementAt(i);
                 String to = (String) to_vec.elementAt(i);
                 if (expressionValue.compareTo(from) == 0) {
                     expression = to;
                     break;
                 }
             }
             buf.append(expression);
 
             //t = t + expression;
 
             String rhs = s.substring(idx2 + 2, s.length());
 
             s = rhs;
             idx = s.indexOf(openExp);
         }
         buf.append(s);
         String t = buf.toString();
         //t = t + s;
         return t;
     }
 
     public static String replaceContextPath(String s, String contextPath) {
         if (s == null || contextPath == null)
             return s;
         String openExp = "<%=";
         String closeExp = "%>";
         //String t = "";
         StringBuffer buf = new StringBuffer();
 
         int idx = s.indexOf(openExp);
         if (idx == -1)
             return s;
 
         while (idx != -1) {
             String lhs = s.substring(0, idx);
             //t = t + lhs;
             buf.append(lhs);
 
             String res = s.substring(idx + 3, s.length());
             int idx2 = s.indexOf(closeExp);
 
             String expression = s.substring(idx, idx2 + 2);
             String expressionValue = s.substring(idx + 3, idx2);
 
             if (expression.indexOf("request.getContextPath()") != -1) {
                 expression = contextPath;
             }
 
             //t = t + expression;
             buf.append(expression);
 
             String rhs = s.substring(idx2 + 2, s.length());
 
             s = rhs;
             idx = s.indexOf(openExp);
         }
         buf.append(s);
         String t = buf.toString();
         //t = t + s;
         return t;
     }
 
     public static Vector getPresentationProperties(Entity concept) {
         Vector v = new Vector();
         org.LexGrid.commonTypes.Property[] properties =
             concept.getPresentation();
         // name$value$isPreferred
 
         if (properties == null || properties.length == 0)
             return v;
         for (int i = 0; i < properties.length; i++) {
             Presentation p = (Presentation) properties[i];
             String name = p.getPropertyName();
             String value = p.getValue().getContent();
             String isPreferred = "false";
             if (p.getIsPreferred() != null) {
                 isPreferred = p.getIsPreferred().toString();
 			}
             String t = name + "$" + value + "$" + isPreferred;
             v.add(t);
         }
         return v;
     }
 
     private static Vector getSupportedSources(String codingScheme, String version)
     {
 		Vector v = new Vector();
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             if (lbSvc == null) {
                 _logger
                     .warn("WARNING: Unable to connect to instantiate LexBIGService ???");
                 return v;
             }
 
 			CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
             if (version != null) versionOrTag.setVersion(version);
 
             CodingScheme cs = null;
 			try {
 			    cs = lbSvc.resolveCodingScheme(codingScheme, versionOrTag);
 			} catch (Exception ex2) {
 				cs = null;
 			}
 			if (cs != null)
 			{
 				SupportedSource[] sources = cs.getMappings().getSupportedSource();
 				for (int i=0; i<sources.length; i++)
 				{
 					v.add(sources[i].getLocalId());
 				}
 		    }
 	    } catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return v;
 	}
 
 	public static String getNCImSAB(String formalName) {
 		if (_formalName2NCImSABHashMap.containsKey(formalName)) {
 			String value = (String) _formalName2NCImSABHashMap.get(formalName);
 			return value;
 		}
 		return null;
 	}
 
 	private static HashMap createFormalName2NCImSABHashMap() {
 		HashMap hmap = new HashMap();
 		Vector sab_vec = getSupportedSources("NCI Metathesaurus", null);
 		for (int i=0; i<sab_vec.size(); i++) {
 			String sab = (String) sab_vec.elementAt(i);
 			if (_localName2FormalNameHashMap.containsKey(sab)) {
 				String value = (String) _localName2FormalNameHashMap.get(sab);
 				hmap.put(value, sab);
 			}
 		}
 		return hmap;
 	}
 
 
 
 
 
     private static void dumpHashMap(HashMap hmap) {
 		_logger.warn("\n\n");
 		if (hmap == null) return;
 
 		Iterator it = hmap.entrySet().iterator();
 		while (it.hasNext()) {
 			Entry thisEntry = (Entry) it.next();
 			String key = (String) thisEntry.getKey();
 			String value = (String) thisEntry.getValue();
 			System.out.println(key + " --> " + value);
 		}
 	}
 
 
 ///////////////////////////////////////////////////////////////////////////////////////////////
 //
 // Mapping
 //
 ///////////////////////////////////////////////////////////////////////////////////////////////
 
 
 //public static HashMap _codingScheme2MappingCodingSchemes = null;
 
     public static boolean hasMapping(String codingScheme) {
 		Vector v = getMappingCodingSchemes(codingScheme);
 		if (v != null && v.size() > 0) return true;
 		return false;
 	}
 
 	public static Vector getMappingCodingSchemes(String codingScheme) {
         if (_codingSchemeHashSet == null)
             setCodingSchemeMap();
 
         if (_codingScheme2MappingCodingSchemes == null) {
 			_codingScheme2MappingCodingSchemes = new HashMap();
 		}
 
 
 //System.out.println("getMappingCodingSchemes: " + codingScheme);
         String formalName = getFormalName(codingScheme);
 
         if (_codingScheme2MappingCodingSchemes.containsKey(formalName)) {
 			return (Vector) _codingScheme2MappingCodingSchemes.get(formalName);
 		}
 
 //System.out.println("getMappingCodingSchemes formalname: " + codingScheme);
 
 		Vector v = new Vector();
 		List ontology_list = getOntologyList();
 		LexBIGService lbSvc = new RemoteServerUtil().createLexBIGService();
         for (int i = 0; i < ontology_list.size(); i++) {
 			SelectItem item = (SelectItem) ontology_list.get(i);
 			String value = (String) item.getValue();
 			String label = (String) item.getLabel();
 
 			String scheme = key2CodingSchemeName(value);
 			String version = key2CodingSchemeVersion(value);
 
 			if (isMapping(scheme, version)) {
 
 //System.out.println("getMappingCodingSchemes: label " + label);
 
 
 				try {
 
 					CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
 					if (version != null)
 						csvt.setVersion(version);
 					CodingScheme cs = lbSvc.resolveCodingScheme(scheme, csvt);
 					Relations[] relations = cs.getRelations();
 					for (int j = 0; j < relations.length; j++) {
 						Relations relation = relations[j];
 						Boolean bool_obj = relation.isIsMapping();
 						if (bool_obj != null && bool_obj.equals(Boolean.TRUE)) {
 
 							//System.out.println("\trelation.getSourceCodingScheme(): " + relation.getSourceCodingScheme());
 							//System.out.println("\trelation.getTargetCodingScheme(): " + relation.getTargetCodingScheme());
 
 
                             if (codingScheme.compareTo(getFormalName(relation.getSourceCodingScheme())) == 0 ||
                                 codingScheme.compareTo(getFormalName(relation.getTargetCodingScheme())) == 0) {
 								v.add(label);
 								//System.out.println("\tadding " + label);
 
 								break;
 							}
 						}
 					}
 				} catch (Exception ex) {
                     ex.printStackTrace();
 				}
 			}
 		}
 
 		_codingScheme2MappingCodingSchemes.put(formalName, v);
 		return SortUtils.quickSort(v);
 
 	}
 
 
 
     public static boolean isMapping(String scheme, String version) {
 		if (_isMappingHashMap == null) {
 			setCodingSchemeMap();
 		}
 		if (_isMappingHashMap.containsKey(scheme)) {
 			Boolean is_mapping = (Boolean) _isMappingHashMap.get(scheme);
 			return is_mapping.booleanValue();
 		}
 
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         if (version != null)
             csvt.setVersion(version);
 
 		//List list = new ArrayList();
 		try {
 			LexBIGService lbSvc = new RemoteServerUtil().createLexBIGService();
 			CodingScheme cs = lbSvc.resolveCodingScheme(scheme, csvt);
 			Relations[] relations = cs.getRelations();
 			if (relations.length == 0) {
 				_isMappingHashMap.put(scheme, Boolean.FALSE);
 				return false;
 			}
 			for (int i = 0; i < relations.length; i++) {
 				Relations relation = relations[i];
 				Boolean bool_obj = relation.isIsMapping();
 				if (bool_obj == null || bool_obj.equals(Boolean.FALSE)) {
 					_isMappingHashMap.put(scheme, Boolean.FALSE);
 					return false;
 				}
 			}
 		} catch (Exception ex) {
 			_isMappingHashMap.put(scheme, Boolean.FALSE);
             return false;
         }
         _isMappingHashMap.put(scheme, Boolean.TRUE);
         return true;
     }
 
 
 	public static boolean isExtension(String codingScheme, String version) {
 		CodingSchemeVersionOrTag tagOrVersion = new CodingSchemeVersionOrTag();
 		if (version != null) tagOrVersion.setVersion(version);
 		try {
 			LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
 			SupplementExtension supplementExtension =
 				(SupplementExtension) lbSvc.getGenericExtension("SupplementExtension");
 
 			return supplementExtension.isSupplement(codingScheme, tagOrVersion);
 			//return ServiceUtility.isSupplement(codingScheme, tagOrVersion);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.out.println("DataUtils SupplementExtension.isSupplement throws exeption???");
 		}
 		return false;
 	}
 
 
     public Vector getMappingData(String scheme, String version) {
         Vector v = new Vector();
 		LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
         try {
 			CodedNodeGraph cng = lbSvc.getNodeGraph(scheme, null, null);
 			//PrintUtility.print(cng);
 
 			ResolvedConceptReferenceList rcrl;
 			ResolvedConceptReference[] rcrArray;
 			try {
 				// to be modified (using container isMapping???)
                 NameAndValueList association = createNameAndValueList(new String[] {"mapsTo"}, null);
                 NameAndValueList associationQualifiers = null;
                 cng = cng.restrictToAssociations(association, associationQualifiers);
 
  				rcrl = cng.resolveAsList(null, true, false, 0, -1, null, null, null, -1);
 
 
 				rcrArray = rcrl.getResolvedConceptReference();
 
 				String sourceCode = null;
 				String sourceName = null;
 				String sourceCodingScheme = null;
 				String sourceCodingSchemeVersion = null;
 				String sourceCodeNamespace = null;
 				String associationName = null;
 				String rel = null;
 				int score = 0;
 				String targetCode = null;
 				String targetName = null;
 				String targetCodingScheme = null;
 				String targetCodingSchemeVersion = null;
 				String targetCodeNamespace = null;
 
 				for (ResolvedConceptReference ref : rcrArray) {
                     int depth = 0;
 					String description;
 					if(ref.getEntityDescription() == null) {
 						description = "NOT AVAILABLE";
 					} else {
 						description = ref.getEntityDescription().getContent();
 					}
 					//System.out.println("Code: " + ref.getCode() + ", Description: " + description + " Hash: " + ref.hashCode() + " " + "Coding Scheme: " + ref.getCodingSchemeName() + ", Version: " + ref.getCodingSchemeVersion()
 					//    + ", Namespace: " + ref.getCodeNamespace());
 
                     sourceCode = ref.getCode();
                     sourceName = description;
                     sourceCodingScheme = ref.getCodingSchemeName();
                     sourceCodingSchemeVersion = ref.getCodingSchemeVersion();
                     sourceCodeNamespace = ref.getCodeNamespace();
 
                     AssociationList assocs = ref.getSourceOf();
 					if(assocs != null){
 
 						//PrintUtility.print(ref.getSourceOf(), depth+1);
 
 						for(Association assoc : assocs.getAssociation()){
 							//System.out.println(buildPrefix(depth) + "Association: " + assoc.getAssociationName() + " Container: " + assoc.getRelationsContainerName());
 
 							associationName = assoc.getAssociationName();
 
 							for(AssociatedConcept ac : assoc.getAssociatedConcepts().getAssociatedConcept()){
 								//print(concept, depth+1);
 
 								if(ac.getEntityDescription() == null) {
 									description = "NOT AVAILABLE";
 								} else {
 									description = ac.getEntityDescription().getContent();
 								}
 								//System.out.println(buildPrefix(depth) + "Code: " + ac.getCode() + ", Description: " + description + " Hash: " + ac.hashCode() + " " +
 								//   "Coding Scheme: " + ac.getCodingSchemeName() + ", Version: " + ac.getCodingSchemeVersion() + ", Namespace: " + ac.getCodeNamespace());
 
 								targetCode = ac.getCode();
 								targetName = description;
 								targetCodingScheme = ac.getCodingSchemeName();
 								targetCodingSchemeVersion = ac.getCodingSchemeVersion();
 								targetCodeNamespace = ac.getCodeNamespace();
 
 								for (NameAndValue qual : ac.getAssociationQualifiers()
 									.getNameAndValue()) {
 									String qualifier_name = qual.getName();
 									String qualifier_value = qual.getContent();
 									if (qualifier_name.compareTo("rel") == 0) {
                                         rel = qualifier_value;
 									} else if (qualifier_name.compareTo("score") == 0) {
 										score = Integer.parseInt(qualifier_value);
 									} else if (qualifier_name.compareTo("maprank") == 0) {
 										score = Integer.parseInt(qualifier_value);
  								    }
 								}
 
 								MappingData mappingData = new MappingData(
 									sourceCode,
 									sourceName,
 									sourceCodingScheme,
 									sourceCodingSchemeVersion,
 									sourceCodeNamespace,
 									associationName,
 									rel,
 									score,
 									targetCode,
 									targetName,
 									targetCodingScheme,
 									targetCodingSchemeVersion,
 									targetCodeNamespace);
 								v.add(mappingData);
 							}
 						}
 
 					}
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return v;
 	}
 
 
     public void dumpMappingData(Vector v) {
 	    for (int i=0; i<v.size(); i++) {
 			MappingData mappingData = (MappingData) v.elementAt(i);
 			System.out.println(mappingData.getSourceCode() + "|"
 			                 + mappingData.getSourceName() + "|"
 			                 + mappingData.getSourceCodingScheme() + "|"
 			                 + mappingData.getSourceCodingSchemeVersion() + "|"
 			                 + mappingData.getSourceCodeNamespace() + "|"
 			                 + mappingData.getAssociationName() + "|"
 			                 + mappingData.getRel() + "|"
 			                 + mappingData.getScore() + "|"
 			                 + mappingData.getTargetCode() + "|"
 			                 + mappingData.getTargetName() + "|"
 			                 + mappingData.getTargetCodingScheme() + "|"
 			                 + mappingData.getTargetCodingSchemeVersion() + "|"
 			                 + mappingData.getTargetCodeNamespace());
 		}
 
 	}
 
 /*
     public static int COL_SOURCE_CODE = 1;
     public static int COL_SOURCE_NAME = 2;
     public static int COL_SOURCE_NAMESPACE = 3;
     public static int COL_REL = 4;
     public static int COL_SCORE = 5;
     public static int COL_TARGET_CODE = 6;
     public static int COL_TARGET_NAME = 7;
     public static int COL_TARGET_NAMESPACE = 8;
 */
 
 	public static List<MappingSortOption> createMappingSortOption(int sortBy) {
         List<MappingSortOption> list = new ArrayList<MappingSortOption>();
         MappingSortOption option = null;
         QualifierSortOption qualifierOption = null;
         switch (sortBy) {
             case 1:
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 qualifierOption = new QualifierSortOption(Direction.ASC, "rel");
                 list.add(qualifierOption);
                 qualifierOption = new QualifierSortOption(Direction.DESC, "score");
                 list.add(qualifierOption);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 break;
 
             case 2:
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_CODE, Direction.ASC);
 				list.add(option);
                 qualifierOption = new QualifierSortOption(Direction.ASC, "rel");
                 list.add(qualifierOption);
                 qualifierOption = new QualifierSortOption(Direction.DESC, "score");
                 list.add(qualifierOption);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 break;
 
             // to be modified
             case 3:
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_CODE, Direction.ASC);
 				list.add(option);
                 qualifierOption = new QualifierSortOption(Direction.ASC, "rel");
                 list.add(qualifierOption);
                 qualifierOption = new QualifierSortOption(Direction.DESC, "score");
                 list.add(qualifierOption);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 break;
 
             case 4:
                 qualifierOption = new QualifierSortOption(Direction.ASC, "rel");
                 list.add(qualifierOption);
                 qualifierOption = new QualifierSortOption(Direction.DESC, "score");
                 list.add(qualifierOption);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 break;
 
             case 5:
                 qualifierOption = new QualifierSortOption(Direction.DESC, "score");
                 list.add(qualifierOption);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 qualifierOption = new QualifierSortOption(Direction.ASC, "rel");
                 list.add(qualifierOption);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 break;
 
             case 6:
 				option = new MappingSortOption(MappingSortOptionName.TARGET_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 qualifierOption = new QualifierSortOption(Direction.ASC, "rel");
                 list.add(qualifierOption);
                 qualifierOption = new QualifierSortOption(Direction.DESC, "score");
                 list.add(qualifierOption);
                 break;
 
             case 7:
 				option = new MappingSortOption(MappingSortOptionName.TARGET_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
  				option = new MappingSortOption(MappingSortOptionName.TARGET_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 qualifierOption = new QualifierSortOption(Direction.ASC, "rel");
                 list.add(qualifierOption);
                 qualifierOption = new QualifierSortOption(Direction.DESC, "score");
                 list.add(qualifierOption);
                break;
 
             // to be modified
             case 8:
  				//option = new MappingSortOption(MappingSortOptionName.TARGET_NAMESPACE, Direction.ASC);
                 //list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.TARGET_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
  				option = new MappingSortOption(MappingSortOptionName.TARGET_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_CODE, Direction.ASC);
 				list.add(option);
 				option = new MappingSortOption(MappingSortOptionName.SOURCE_ENTITY_DESCRIPTION, Direction.ASC);
                 list.add(option);
                 qualifierOption = new QualifierSortOption(Direction.ASC, "rel");
                 list.add(qualifierOption);
                 qualifierOption = new QualifierSortOption(Direction.DESC, "score");
                 list.add(qualifierOption);
                break;
 
             default:
                return createMappingSortOption(1);
 		}
 		return list;
 	}
 
 
 //MappingSortOptionName.RELATIONSHIP,
 //MappingSortOptionName.QUALIFIER
 
 
     public static ResolvedConceptReferencesIterator getMappingDataIterator(String scheme, String version) {
 		return getMappingDataIterator(scheme, version, MappingData.COL_SOURCE_CODE);
 	}
 
     public static ResolvedConceptReferencesIterator getMappingDataIterator(String scheme, String version, int sortBy) {
 		List<MappingSortOption> sortOptionList = createMappingSortOption(sortBy);
 		return getMappingDataIterator(scheme, version, sortOptionList);
 	}
 
 
     public static ResolvedConceptReferencesIterator getMappingDataIterator(String scheme, String version, List<MappingSortOption> sortOptionList) {
 		CodingSchemeVersionOrTag versionOrTag =
 			new CodingSchemeVersionOrTag();
 		if (version != null) {
 			versionOrTag.setVersion(version);
 		}
 		String relationsContainerName = null;
 
         LexBIGService distributed = RemoteServerUtil.createLexBIGService();
         try {
 			CodingScheme cs = distributed.resolveCodingScheme(scheme, versionOrTag);
 			if (cs == null) return null;
 
 			java.util.Enumeration<? extends Relations> relations = cs.enumerateRelations();
 			while (relations.hasMoreElements()) {
 				Relations relation = (Relations) relations.nextElement();
 				Boolean isMapping = relation.getIsMapping();
 				System.out.println("isMapping: " + isMapping);
 				if (isMapping != null && isMapping.equals(Boolean.TRUE)) {
  					relationsContainerName = relation.getContainerName();
 					//System.out.println(relationsContainerName);
 					break;
 				}
 			}
 			if (relationsContainerName == null) {
 				System.out.println("WARNING: Mapping container not found in " + scheme);
 				return null;
 			}
 
 			MappingExtension mappingExtension = (MappingExtension)
 				distributed.getGenericExtension("MappingExtension");
 
 			ResolvedConceptReferencesIterator itr = mappingExtension.resolveMapping(
 					scheme, //"NCIt_to_ICD9CM_Mapping",
 					versionOrTag,
 					relationsContainerName,//"NCIt_to_ICD9CM_Mappings",
 					sortOptionList);
 
 			return itr;
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 
 	}
 
 
     public static NameAndValueList getMappingAssociationNames(String scheme, String version) {
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         if (version != null)
             csvt.setVersion(version);
 
 		NameAndValueList navList = new NameAndValueList();
 		try {
 			LexBIGService lbSvc = null;
 			lbSvc = new RemoteServerUtil().createLexBIGService();
 			CodingScheme cs = lbSvc.resolveCodingScheme(scheme, csvt);
 			Relations[] relations = cs.getRelations();
 			for (int i = 0; i < relations.length; i++) {
 				Relations relation = relations[i];
 				System.out.println(relation.getContainerName());
                 Boolean isMapping = relation.isIsMapping();
                 if (isMapping != null && isMapping.equals(Boolean.TRUE)) {
 					AssociationPredicate[] associationPredicates = relation.getAssociationPredicate();
 					for (int j=0; j<associationPredicates.length; j++) {
 						AssociationPredicate associationPredicate = associationPredicates[j];
 						String name = associationPredicate.getAssociationName();
 						NameAndValue vNameAndValue = new NameAndValue();
 						vNameAndValue.setName(name);
 						navList.addNameAndValue(vNameAndValue);
 
 
 						System.out.println("getMappingAssociationNames " + name);
 					}
 					return navList;
 				} else {
 					return null;
 				}
 			}
 		} catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     //test case: C17014
     public static Vector getMappingCodingSchemesEntityParticipatesIn(String code, String namespace) {
         Vector v = new Vector();
         try {
 			LexBIGService distributed = RemoteServerUtil.createLexBIGService();
 
 			MappingExtension mappingExtension =
 				(MappingExtension)distributed.getGenericExtension("MappingExtension");
 
 			AbsoluteCodingSchemeVersionReferenceList mappingSchemes =
 				mappingExtension.getMappingCodingSchemesEntityParticipatesIn(code, namespace);
 
 			//output is all of the mapping ontologies that this code participates in.
 			for(AbsoluteCodingSchemeVersionReference ref : mappingSchemes.getAbsoluteCodingSchemeVersionReference()){
 				//System.out.println("URI: " + ref.getCodingSchemeURN());
 				//System.out.println("Version: " + ref.getCodingSchemeVersion());
 				v.add(ref.getCodingSchemeURN() + "|" + ref.getCodingSchemeVersion());
 			}
 
 		} catch (Exception ex) {
             ex.printStackTrace();
         }
 		return v;
 	}
 
 	public static String uri2CodingSchemeName(String uri) {
 	    if (!_uri2CodingSchemeNameHashMap.containsKey(uri)) return null;
 	    return (String) _uri2CodingSchemeNameHashMap.get(uri);
 	}
 
 	public static String codingSchemeName2URI(String name) {
 	    if (!_codingSchemeName2URIHashMap.containsKey(name)) return null;
 	    return (String) _codingSchemeName2URIHashMap.get(name);
 	}
 
 	public static Vector getConceptDomainNames() {
 		String scheme = "conceptDomainCodingScheme";
 		//scheme = "http://lexevs.org/codingscheme/conceptdomain";
 
 		String version = null;
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         /*if (version != null)
             csvt.setVersion(version);
         */
 		Vector conceptDomainName_vec = new Vector();
 		try {
 			LexBIGService lbSvc = null;
 			lbSvc = new RemoteServerUtil().createLexBIGService();
 
 			LocalNameList entityTypes = new LocalNameList();
 			entityTypes.addEntry("conceptDomain");
 
 			//CodedNodeSet cns = lbSvc.getNodeSet(lbSvc, scheme, csvt);
 
 			CodedNodeSet cns = lbSvc.getNodeSet(scheme, csvt, entityTypes);
 
 			SortOptionList sortOptions = null;
 			LocalNameList filterOptions = null;
 			LocalNameList propertyNames = null;
 			CodedNodeSet.PropertyType[] propertyTypes = null;
 			boolean resolveObjects = true;
 			int maxToReturn = 1000;
             ResolvedConceptReferenceList rcrl = cns.resolveToList(sortOptions, filterOptions, propertyNames, propertyTypes, resolveObjects, maxToReturn);
 
             System.out.println("Number of concept domains: " + rcrl.getResolvedConceptReferenceCount());
             for (int i=0; i<rcrl.getResolvedConceptReferenceCount(); i++) {
 				ResolvedConceptReference rcr = rcrl.getResolvedConceptReference(i);
 				Entity entity = rcr.getReferencedEntry();
 				conceptDomainName_vec.add(entity.getEntityDescription().getContent());
 			}
 		} catch (Exception ex) {
 			//ex.printStackTrace();
 			System.out.println("Unable to resolve " + scheme + "  -- concept domain may not be available.");
 		}
 		conceptDomainName_vec = SortUtils.quickSort(conceptDomainName_vec);
 		return conceptDomainName_vec;
 	}
 
 
 	public static Vector getCodingSchemeFormalNames() {
         if (_codingSchemeHashSet == null)
             setCodingSchemeMap();
 
 		Vector v = new Vector();
 		Set keyset = _formalName2LocalNameHashMap.keySet();
 		Iterator iterator = keyset.iterator();
 		while (iterator.hasNext()) {
 			String t = (String) iterator.next();
 			v.add(t);
 		}
 		return SortUtils.quickSort(v);
 	}
 
 
 	public static Vector getValueSetURIs() {
 		Vector v = new Vector();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         for (int i=0; i<list.size(); i++) {
 			String t = (String) list.get(i);
 			v.add(t);
 		}
 		return SortUtils.quickSort(v);
 	}
 
 
     public static ValueSetDefinition findValueSetDefinitionByURI(String uri) {
 		if (uri == null) return null;
 	    if (uri.indexOf("|") != -1) {
 			Vector u = parseData(uri);
 			uri = (String) u.elementAt(1);
 		}
 
 		String valueSetDefinitionRevisionId = null;
 		try {
 			LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 			ValueSetDefinition vsd = vsd_service.getValueSetDefinition(new URI(uri), valueSetDefinitionRevisionId);
 			return vsd;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 
 	public static Vector getValueSetNamesAndURIs() {
 		Vector v = new Vector();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         for (int i=0; i<list.size(); i++) {
 			String t = (String) list.get(i);
 			ValueSetDefinition vsd = findValueSetDefinitionByURI(t);
 			String name = vsd.getValueSetDefinitionName();
 			if (name == null) {
 				name = "<NOT ASSIGNED>";
 			}
 
 			v.add(name + "|" + t);
 		}
 		return SortUtils.quickSort(v);
 	}
 
 
     public static String valueSetDefiniionURI2Name(String vsd_uri) {
 		String metadata = getValueSetDefinitionMetadata(vsd_uri);
 		Vector v = parseData(metadata);
 		return (String) v.elementAt(0);
 	}
 
 
     public static HashMap getCodingSchemeURN2ValueSetMetadataHashMap(Vector vsd_vec) {
         HashMap hmap = new HashMap();
 
         for (int i=0; i<vsd_vec.size(); i++) {
 		    String vsd_str = (String) vsd_vec.elementAt(i);
 
 System.out.println("vsd_str " + vsd_str);
 
 		    Vector u = parseData(vsd_str);
 		    String name = (String) u.elementAt(0);
 		    String uri = (String) u.elementAt(1);
 
 		    String description = (String) u.elementAt(2);
 
 		    Vector cs_vec = getCodingSchemeURNsInValueSetDefinition(uri);
 
             if (cs_vec != null) {
 
 				for (int k=0; k<cs_vec.size(); k++) {
 					String cs_urn = (String) cs_vec.elementAt(k);
 
 					String cs_name = uri2CodingSchemeName(cs_urn);
 
 					if (hmap.containsKey(cs_name)) {
 						Vector v = (Vector) hmap.get(cs_name);
 						if (!v.contains(vsd_str)) {
 							v.add(vsd_str);
 							hmap.put(cs_name, v);
 						}
 					} else {
 						Vector v = new Vector();
 						v.add(vsd_str);
 						hmap.put(cs_name, v);
 					}
 				}
 		    }
 		}
 		return hmap;
 	}
 
 
     public static HashMap getSource2ValueSetMetadataHashMap(Vector vsd_vec) {
         HashMap hmap = new HashMap();
 
         for (int i=0; i<vsd_vec.size(); i++) {
 		    String vsd_str = (String) vsd_vec.elementAt(i);
 
 		    Vector u = parseData(vsd_str);
 		    String name = (String) u.elementAt(0);
 		    String uri = (String) u.elementAt(1);
 		    String description = (String) u.elementAt(2);
 		    String domain = (String) u.elementAt(3);
 		    String src_str = (String) u.elementAt(4);
 		    if (src_str == null || src_str.compareTo("<NOT ASSIGNED>") == 0) {
 				String key = "<NOT ASSIGNED>";
 				if (hmap.containsKey(key)) {
 					Vector v = (Vector) hmap.get(key);
 					if (!v.contains(vsd_str)) {
 						v.add(vsd_str);
 						hmap.put(key, v);
 					}
 				} else {
 					Vector v = new Vector();
 					v.add(vsd_str);
 					hmap.put(key, v);
 				}
 			} else {
 		    	Vector src_vec = parseData(src_str);
 		    	for (int j=0; j<src_vec.size(); j++) {
 					String src = (String) src_vec.elementAt(j);
 					if (hmap.containsKey(src)) {
 						Vector v = (Vector) hmap.get(src);
 						if (!v.contains(vsd_str)) {
 							v.add(vsd_str);
 
 							System.out.println("hmap " + src);
 
 							hmap.put(src, v);
 						}
 					} else {
 						Vector v = new Vector();
 						v.add(vsd_str);
 						hmap.put(src, v);
 					}
 
 			    }
 			}
 		}
 		return hmap;
 	}
 
 
 
 
     public static Vector getCodingSchemeURNsInValueSetDefinition(String uri) {
 		try {
 			java.net.URI valueSetDefinitionURI = new URI(uri);
 			Vector v = new Vector();
 			try {
 				LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 				AbsoluteCodingSchemeVersionReferenceList codingSchemes =
 					vsd_service.getCodingSchemesInValueSetDefinition(valueSetDefinitionURI);
 
                 if (codingSchemes != null) {
 					//output is all of the mapping ontologies that this code participates in.
 					for(AbsoluteCodingSchemeVersionReference ref : codingSchemes.getAbsoluteCodingSchemeVersionReference()){
 						//System.out.println("URI: " + ref.getCodingSchemeURN());
 						v.add(ref.getCodingSchemeURN());
 					}
 					return SortUtils.quickSort(v);
 			    } else {
 					System.out.println("WARNING: DataUtils.getCodingSchemeURNsInValueSetDefinition returns null (URI: "
 					   + uri + ").");
 				}
 
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				System.out.println("WARNING: DataUtils.getCodingSchemeURNsInValueSetDefinition throws exceptions.");
 			}
 			return v;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.out.println("WARNING: DataUtils.getCodingSchemeURNsInValueSetDefinition throws exceptions.");
 		}
 		return null;
 	}
 
 //===========================================================================================================================
 // Value Set Hierarchy
 //===========================================================================================================================
 
     public static ResolvedConceptReferenceList getValueSetHierarchyRoots() {
         String scheme = "Terminology Value Set";
         String version = getVocabularyVersionByTag(scheme, "PRODUCTION");
         ResolvedConceptReferenceList rcrl = TreeUtils.getHierarchyRoots(scheme, version);
         return rcrl;
 	}
 
 	public static void geValueSetHierarchy(HashMap hmap, Vector v) {
 
     }
 
 	public static void geValueSetHierarchy() {
         HashMap hmap = new HashMap();
         ResolvedConceptReferenceList roots = getValueSetHierarchyRoots();
         Vector v = new Vector();
         for (int i=0; i<roots.getResolvedConceptReferenceCount(); i++) {
 			ResolvedConceptReference rcr = roots.getResolvedConceptReference(i);
 			v.add(rcr);
 		}
         geValueSetHierarchy(hmap, v);
 	}
 
 
 
 
     public static Vector getValueSetDefinitionsBySource(String source) {
 		if (_availableValueSetDefinitionSources != null) {
 			if (!_availableValueSetDefinitionSources.contains(source)) return null;
 		}
 		Vector v = new Vector();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         if (list == null) return null;
         for (int i=0; i<list.size(); i++) {
 			String uri = (String) list.get(i);
 			ValueSetDefinition vsd = findValueSetDefinitionByURI(uri);
 			java.util.Enumeration<? extends Source> sourceEnum = vsd.enumerateSource();
             boolean found = false;
 			while (sourceEnum.hasMoreElements()) {
 				Source src = (Source) sourceEnum.nextElement();
 				String src_str = src.getContent();
 				if (src_str.compareTo(source) == 0) {
 					v.add(vsd);
 					break;
 				}
 			}
 		}
 		return v;
 	}
 
     static {
 		_availableValueSetDefinitionSources = new Vector();
 		HashSet hset = new HashSet();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         if (list != null) {
 			for (int i=0; i<list.size(); i++) {
 				String uri = (String) list.get(i);
 				ValueSetDefinition vsd = findValueSetDefinitionByURI(uri);
 				java.util.Enumeration<? extends Source> sourceEnum = vsd.enumerateSource();
 
 				while (sourceEnum.hasMoreElements()) {
 					Source src = (Source) sourceEnum.nextElement();
 					String src_str = src.getContent();
 					if (!hset.contains(src_str)) {
 						hset.add(src_str);
 						_availableValueSetDefinitionSources.add(src_str);
 					}
 				}
 			}
 			SortUtils.quickSort(_availableValueSetDefinitionSources);
 	    }
 	}
 
     public static Vector getAvailableValueSetDefinitionSources() {
 		/*
 		if (_availableValueSetDefinitionSources != null) return _availableValueSetDefinitionSources;
 		_availableValueSetDefinitionSources = new Vector();
 		HashSet hset = new HashSet();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         if (list == null) return null;
         for (int i=0; i<list.size(); i++) {
 			String uri = (String) list.get(i);
 			ValueSetDefinition vsd = findValueSetDefinitionByURI(uri);
 			java.util.Enumeration<? extends Source> sourceEnum = vsd.enumerateSource();
 
 			while (sourceEnum.hasMoreElements()) {
 				Source src = (Source) sourceEnum.nextElement();
 				String src_str = src.getContent();
 				if (!hset.contains(src_str)) {
 					hset.add(src_str);
 					_availableValueSetDefinitionSources.add(src_str);
 				}
 			}
 		}
 
 		return SortUtils.quickSort(_availableValueSetDefinitionSources);
 		*/
 		return _availableValueSetDefinitionSources;
 	}
 
 
 
     public static void printValueSetDefinitionHierarchyNode(int level, String root) {
 		//String indent = "";
 		StringBuffer buf = new StringBuffer();
 
 		for (int i=0; i<level; i++) {
 			//indent = indent + "\t";
 			buf.append("\t");
 		}
 		String indent = buf.toString();
 		System.out.println(indent + root);
 		Vector children = (Vector) _valueSetDefinitionHierarchyHashMap.get(root);
 		if (children != null) {
 			for (int j=0; j<children.size(); j++) {
 				String child = (String) children.elementAt(j);
                 printValueSetDefinitionHierarchyNode(level+1, child);
 			}
 		}
 	}
 
     static {
 		_valueSetDefinitionMetadata = new Vector();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         if (list != null) {
 			for (int i=0; i<list.size(); i++) {
 				String uri = (String) list.get(i);
 				ValueSetDefinition vsd = findValueSetDefinitionByURI(uri);
 				String metadata = getValueSetDefinitionMetadata(vsd);
 				_valueSetDefinitionMetadata.add(metadata);
 			}
 			SortUtils.quickSort(_valueSetDefinitionMetadata);
 	    }
 
 	}
 
 	public static Vector getValueSetDefinitionMetadata() {
 		/*
 		if (_valueSetDefinitionMetadata != null) return _valueSetDefinitionMetadata;
 		_valueSetDefinitionMetadata = new Vector();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         if (list == null) return null;
         for (int i=0; i<list.size(); i++) {
 			String uri = (String) list.get(i);
 			ValueSetDefinition vsd = findValueSetDefinitionByURI(uri);
 			String metadata = getValueSetDefinitionMetadata(vsd);
 			_valueSetDefinitionMetadata.add(metadata);
 		}
 		SortUtils.quickSort(_valueSetDefinitionMetadata);
 		*/
 		return _valueSetDefinitionMetadata;
 	}
 
 	public static String getValueSetDefinitionMetadata(String vsd_uri) {
 		ValueSetDefinition vsd = findValueSetDefinitionByURI(vsd_uri);
 		if (vsd == null) return null;
 		return getValueSetDefinitionMetadata(vsd);
 	}
 
 
     public static String getValueSetDefinitionMetadata(ValueSetDefinition vsd) {
 		if (vsd== null) return null;
 		String name = "";
 		String uri = "";
 		String description = "";
 		String domain = "";
 		//String src_str = "";
 		StringBuffer buf = new StringBuffer();
 
 		uri = vsd.getValueSetDefinitionURI();
 		name = vsd.getValueSetDefinitionName();
 		if (name == null || name.compareTo("") == 0) {
 			name = "<NOT ASSIGNED>";
 		}
 
 		domain = vsd.getConceptDomain();
 		if (domain == null || domain.compareTo("") == 0) {
 			domain = "<NOT ASSIGNED>";
 		}
 
 		java.util.Enumeration<? extends Source> sourceEnum = vsd.enumerateSource();
 
 		while (sourceEnum.hasMoreElements()) {
 			Source src = (Source) sourceEnum.nextElement();
 			//src_str = src_str + src.getContent() + ";";
 			buf.append(src.getContent() + ";");
 		}
 		String src_str = buf.toString();
 
 
 		if (src_str.length() > 0) {
 			src_str = src_str.substring(0, src_str.length()-1);
 		}
 
 		if (src_str == null || src_str.compareTo("") == 0) {
 			src_str = "<NOT ASSIGNED>";
 		}
 
 		if (vsd.getEntityDescription() != null) {
 			description = vsd.getEntityDescription().getContent();
 			if (description == null || description.compareTo("") == 0) {
 				description = "<NO DESCRIPTION>";
 			}
 		} else {
 			description = "<NO DESCRIPTION>";
 		}
 
 		return name + "|" + uri + "|" + description + "|" + domain + "|" + src_str;
 	}
 
 
     public static Vector getCodingSchemesInValueSetDefinition(String uri) {
 		HashSet hset = new HashSet();
 		try {
 			java.net.URI valueSetDefinitionURI = new URI(uri);
 			Vector v = new Vector();
 			try {
 				LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 				AbsoluteCodingSchemeVersionReferenceList codingSchemes =
 					vsd_service.getCodingSchemesInValueSetDefinition(valueSetDefinitionURI);
 
 				//output is all of the mapping ontologies that this code participates in.
 				for(AbsoluteCodingSchemeVersionReference ref : codingSchemes.getAbsoluteCodingSchemeVersionReference()){
 					String urn = ref.getCodingSchemeURN();
 					System.out.println("URI: " + ref.getCodingSchemeURN());
 					if (!hset.contains(urn)) {
 					System.out.println("Version: " + ref.getCodingSchemeVersion());
 					    v.add(ref.getCodingSchemeURN());
 					    hset.add(urn);
 				    }
 				}
 
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			return v;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 
 
 
 
     public static Vector getCodingSchemeVersionsByURN(String urn) {
         try {
 			Vector v = new Vector();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             if (lbSvc == null) {
                 _logger
                     .warn("WARNING: Unable to connect to instantiate LexBIGService ???");
                 return null;
             }
             CodingSchemeRenderingList csrl = null;
             try {
                 csrl = lbSvc.getSupportedCodingSchemes();
             } catch (LBInvocationException ex) {
                 ex.printStackTrace();
                 _logger.error("lbSvc.getSupportedCodingSchemes() FAILED..."
                     + ex.getCause());
                 return null;
             }
             CodingSchemeRendering[] csrs = csrl.getCodingSchemeRendering();
             for (int i = 0; i < csrs.length; i++) {
                 int j = i + 1;
                 CodingSchemeRendering csr = csrs[i];
                 CodingSchemeSummary css = csr.getCodingSchemeSummary();
                 Boolean isActive =
                         csr.getRenderingDetail().getVersionStatus().equals(
                             CodingSchemeVersionStatus.ACTIVE);
 
                 if (isActive != null && isActive.equals(Boolean.TRUE)) {
                 	String uri = css.getCodingSchemeURI();
                 	if (uri.compareTo(urn) == 0) {
 						String representsVersion = css.getRepresentsVersion();
 						v.add(representsVersion);
 					}
 				}
 			}
 			return v;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 
     public static Vector getCodingSchemeReferencesInValueSetDefinition(String uri) {
 		System.out.println("getCodingSchemeReferencesInValueSetDefinition: " + uri);
 		HashSet hset = new HashSet();
 	    if (uri.indexOf("|") != -1) {
 			Vector u = DataUtils.parseData(uri);
 			uri = (String) u.elementAt(1);
 		}
 
 		try {
 			Vector w = new Vector();
 			Vector urn_vec = getCodingSchemeURNsInValueSetDefinition(uri);
 			if (urn_vec != null) {
 				for (int i=0; i<urn_vec.size(); i++) {
 					String urn = (String) urn_vec.elementAt(i);
 					Vector v = getCodingSchemeVersionsByURN(urn);
 					if (v != null) {
 						for (int j=0; j<v.size(); j++) {
 							String version = (String) v.elementAt(j);
 							String urn_version = urn + "|" + version;
 							if (!hset.contains(urn_version)) {
 								hset.add(urn_version);
 							    w.add(urn_version);
 						    }
 						}
 					}
 				}
 				w = SortUtils.quickSort(w);
 				return w;
 		    } else {
 				System.out.println("WARNING: DataUtils.getCodingSchemeReferencesInValueSetDefinition returns null? (URI: "
 				   + uri + ").");
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 
     public static AbsoluteCodingSchemeVersionReferenceList vector2CodingSchemeVersionReferenceList(Vector v) {
 		if (v == null) return null;
 		AbsoluteCodingSchemeVersionReferenceList list = new AbsoluteCodingSchemeVersionReferenceList();
 		for (int i=0; i<v.size(); i++) {
 			String s = (String) v.elementAt(i);
 			Vector u = DataUtils.parseData(s);
 			String uri = (String) u.elementAt(0);
 			String version = (String) u.elementAt(1);
 
 //System.out.println("(***) URI: " + uri + " version: " + version);
 
 			AbsoluteCodingSchemeVersionReference vAbsoluteCodingSchemeVersionReference
 			    = new AbsoluteCodingSchemeVersionReference();
 			vAbsoluteCodingSchemeVersionReference.setCodingSchemeURN(uri);
 			vAbsoluteCodingSchemeVersionReference.setCodingSchemeVersion(version);
 			list.addAbsoluteCodingSchemeVersionReference(vAbsoluteCodingSchemeVersionReference);
 		}
 		return list;
 	}
 
 
     public static Vector getCodingSchemeReferencesInValueSetDefinition(String uri, String tag) {
 		try {
 			Vector w = new Vector();
 			Vector urn_vec = getCodingSchemeURNsInValueSetDefinition(uri);
 			if (urn_vec != null) {
 				for (int i=0; i<urn_vec.size(); i++) {
 					String urn = (String) urn_vec.elementAt(i);
 					Vector v = getCodingSchemeVersionsByURN(urn, tag);
 					if (v != null) {
 						for (int j=0; j<v.size(); j++) {
 							String version = (String) v.elementAt(j);
 							w.add(urn + "|" + version);
 						}
 					}
 				}
 				w = SortUtils.quickSort(w);
 				return w;
 		    }
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 
 
     public static Vector getCodingSchemeVersionsByURN(String urn, String tag) {
         try {
 			Vector v = new Vector();
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             if (lbSvc == null) {
                 _logger
                     .warn("WARNING: Unable to connect to instantiate LexBIGService ???");
                 return null;
             }
             CodingSchemeRenderingList csrl = null;
             try {
                 csrl = lbSvc.getSupportedCodingSchemes();
             } catch (LBInvocationException ex) {
                 ex.printStackTrace();
                 _logger.error("lbSvc.getSupportedCodingSchemes() FAILED..."
                     + ex.getCause());
                 return null;
             }
             CodingSchemeRendering[] csrs = csrl.getCodingSchemeRendering();
             for (int i = 0; i < csrs.length; i++) {
                 int j = i + 1;
                 CodingSchemeRendering csr = csrs[i];
                 CodingSchemeSummary css = csr.getCodingSchemeSummary();
                 Boolean isActive =
                         csr.getRenderingDetail().getVersionStatus().equals(
                             CodingSchemeVersionStatus.ACTIVE);
 
                 if (isActive != null && isActive.equals(Boolean.TRUE)) {
                 	String uri = css.getCodingSchemeURI();
                 	if (uri.compareTo(urn) == 0) {
 						String representsVersion = css.getRepresentsVersion();
 						if (tag != null) {
 							String cs_tag = getVocabularyVersionTag(uri, representsVersion);
 							if (cs_tag.compareToIgnoreCase(tag) == 0) {
 								v.add(representsVersion);
 							}
 						} else {
 							v.add(representsVersion);
 						}
 					}
 				}
 			}
 			return v;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 
 
 
     public static Vector getMatchedMetathesaurusCUIs(String scheme, String version,
         String ltag, String code) {
 
 System.out.println("(*) getMatchedMetathesaurusCUIs scheme: " + scheme);
 System.out.println("(*) getMatchedMetathesaurusCUIs version: " + version);
 System.out.println("(*) getMatchedMetathesaurusCUIs code: " + code);
 
 
         Entity c = getConceptByCode(scheme, version, ltag, code);
         //Entity c = getConceptWithProperty(scheme, version, code, "NCI_META_CUI");
         if (c != null) {
             Vector v = getConceptPropertyValues(c, "NCI_META_CUI");
             if (v == null || v.size() == 0) {
 				return getConceptPropertyValues(c, "UMLS_CUI");
 			}        }
         return null;
     }
 
 
     public static Vector getMatchedMetathesaurusCUIs(Entity c) {
         if (c != null) {
             Vector v = getConceptPropertyValues(c, "NCI_META_CUI");
             if (v == null || v.size() == 0) {
 				return getConceptPropertyValues(c, "UMLS_CUI");
 			}
         }
         return null;
     }
 
     public static String getMappingDisplayName(String scheme, String name) {
 		HashMap hmap = (HashMap) _mappingDisplayNameHashMap.get(scheme);
 		if (hmap == null) {
 
 			System.out.println("_mappingDisplayNameHashMap.get " + scheme + " returns NULL???");
 
 			return name;
 		}
 		Object obj = hmap.get(name);
 		if (obj == null) {
 			return name;
 		}
 		return obj.toString();
 	}
 
 
 //  Key coding scheme name$version$code
 //  Value: property value (delimeter: ;)
 
 
 
     public static Map getPropertyValuesInBatch(String scheme, String version, String propertyName, List<String> codes) {
         if (codes == null) return null;
         Map map = null;
         if (codes.size() == 0) return map;
 
         PropertyExtension extension = null;
         try {
             LexBIGService lbSvc = new RemoteServerUtil().createLexBIGService();
 
             if (lbSvc == null) {
                 _logger.warn("lbSvc = null");
                 return null;
             }
             extension = (PropertyExtension) lbSvc.getGenericExtension("property-extension");
             if (extension == null) {
                 _logger.error("Error! PropertyExtension is null!");
                 return null;
             }
 		} catch (Exception ex) {
 			return null;
 		}
 		CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
 		if (version != null) {
 			csvt.setVersion(version);
 		}
 
 	    try {
 			map = extension.getProperty(scheme, csvt, propertyName, codes);
 			return map;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 
 
     public static HashMap getPropertyValuesInBatch(List list, String propertyName) {
 
         if (list == null) return null;
         HashMap hmap = new HashMap();
         if (list.size() == 0) return hmap;
 
         PropertyExtension extension = null;
         try {
             LexBIGService lbSvc = new RemoteServerUtil().createLexBIGService();
 
             if (lbSvc == null) {
                 _logger.warn("lbSvc = null");
                 return null;
             }
             extension = (PropertyExtension) lbSvc.getGenericExtension("property-extension");
             if (extension == null) {
                 _logger.error("Error! PropertyExtension is null!");
                 return null;
             }
 		} catch (Exception ex) {
 			return null;
 		}
 
 
         //Vector cs_name_vec = new Vector();
         //Vector cs_version_vec = new Vector();
         HashSet hset = new HashSet();
 
         HashMap csnv2codesMap = new HashMap();
 
 		for (int i=0; i<list.size(); i++) {
 			ResolvedConceptReference rcr = (ResolvedConceptReference) list.get(i);
 			String cs_name = rcr.getCodingSchemeName();
 
 			boolean conceptStatusSupported = false;
 			if (_vocabulariesWithConceptStatusHashSet.contains(getFormalName(cs_name))) {
 				String version = rcr.getCodingSchemeVersion();
 				String cs_name_and_version = cs_name + "$" + version;
 				if (!hset.contains(cs_name_and_version)) {
 					hset.add(cs_name_and_version);
 					//cs_name_vec.add(cs_name);
 					//cs_version_vec.add(version);
 					ArrayList alist = new ArrayList();
 					alist.add(rcr.getConceptCode());
 					csnv2codesMap.put(cs_name_and_version, alist);
 
 				} else {
 					ArrayList alist = (ArrayList) csnv2codesMap.get(cs_name_and_version);
 					alist.add(rcr.getConceptCode());
 					csnv2codesMap.put(cs_name_and_version, alist);
 				}
 
 			}
 		}
 
 		Iterator it = csnv2codesMap.entrySet().iterator();
 		while (it.hasNext()) {
 			Entry thisEntry = (Entry) it.next();
 			String key = (String) thisEntry.getKey();
 			ArrayList alist = (ArrayList) thisEntry.getValue();
 			Vector u = DataUtils.parseData(key, "$");
 			String scheme = (String) u.elementAt(0);
 			String version = (String) u.elementAt(1);
 			CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
 			if (version != null) csvt.setVersion(version);
 			String[] a = new String[alist.size()];
 			for (int i=0; i<alist.size(); i++) {
 				a[i] = (String) alist.get(i);
 			}
 
             try {
 				Map<String,String> propertyMap =
 					extension.getProperty(scheme, csvt, propertyName, Arrays.asList(a));
 
 				for (Entry<String, String> entry : propertyMap.entrySet()) {
 					//System.out.println("Code: " + entry.getKey());
 					//System.out.println(" - Property: " + entry.getValue());
 					hmap.put(key + "$" + entry.getKey(), entry.getValue());
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				return null;
 			}
 		}
 
         return hmap;
 	}
 
     public static List<String> getDistinctNamespacesOfCode(
             String codingScheme,
             String version,
             String code) {
 
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             LexBIGServiceConvenienceMethods lbscm =
                 (LexBIGServiceConvenienceMethods) lbSvc
                     .getGenericExtension("LexBIGServiceConvenienceMethods");
             lbscm.setLexBIGService(lbSvc);
 
             CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
             csvt.setVersion(version);
 
             return lbscm.getDistinctNamespacesOfCode(codingScheme, csvt, code);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
         return null;
 	}
 
 
     public static String getVocabularyVersionTag(String codingSchemeName,
         String version) {
 
         if (codingSchemeName == null)
             return null;
 
 
         if (_codingSchemeTagHashMap != null) {
 			String key = null;
 			if (version == null) {
 				key = codingSchemeName + "$null";
 			} else {
 				key = codingSchemeName + "$" + version;
 			}
 			if (_codingSchemeTagHashMap.containsKey(key)) {
 				String tag = (String) _codingSchemeTagHashMap.get(key);
 				return tag;
 			}
 		}
 
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodingSchemeRenderingList lcsrl = lbSvc.getSupportedCodingSchemes();
             CodingSchemeRendering[] csra = lcsrl.getCodingSchemeRendering();
             for (int i = 0; i < csra.length; i++) {
                 CodingSchemeRendering csr = csra[i];
                 CodingSchemeSummary css = csr.getCodingSchemeSummary();
                 if (css.getFormalName().compareTo(codingSchemeName) == 0
                     || css.getLocalName().compareTo(codingSchemeName) == 0
                     || css.getCodingSchemeURI().compareTo(codingSchemeName) == 0) {
 
 					if (version == null) return "PRODUCTION";
 
 					String representsVersion = css.getRepresentsVersion();
 
                     if (representsVersion.compareTo(version) == 0) {
 						RenderingDetail rd = csr.getRenderingDetail();
 						CodingSchemeTagList cstl = rd.getVersionTags();
 						String tag_str = "";
 						java.lang.String[] tags = cstl.getTag();
 						if (tags == null)
 							return "NOT ASSIGNED";
 
 						if (tags.length > 0) {
 							tag_str = "";
 							for (int j = 0; j < tags.length; j++) {
 								String version_tag = (String) tags[j];
 								if (j == 0) {
 									tag_str = version_tag;
 								} else if (j == tags.length-1) {
 									tag_str = tag_str + version_tag;
 								} else {
 									tag_str = tag_str + version_tag + "|";
 								}
 							}
 						} else {
 							return "<NOT ASSIGNED>";
 						}
 
                         if (_codingSchemeTagHashMap == null) {
 							_codingSchemeTagHashMap = new HashMap();
 						}
 						String key = null;
 						key = codingSchemeName + "$" + version;
 						_codingSchemeTagHashMap.put(key, tag_str);
 						return tag_str;
 					}
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return "<NOT AVAILABLE>";
     }
 
 
     public static boolean isNull(String value) {
 		if (value == null || value.compareTo("null") == 0) return true;
 		return false;
 	}
 
 
 	public static Vector getNonProductionOntologies(Vector v, String scheme) {
 		Vector u = new Vector();
 		for (int i = 0; i < v.size(); i++) {
 			OntologyInfo info = (OntologyInfo) v.elementAt(i);
 			if (scheme.compareTo(info.getCodingScheme()) == 0) {
 				if (DataUtils.isNull(info.getTag()) || info.getTag().compareToIgnoreCase("PRODUCTION") != 0) {
 					u.add(info);
 				}
 			}
 		}
 		if (u.size() > 0) {
 			Collections.sort(u, new OntologyInfo.ComparatorImpl());
 		}
 		return u;
 	}
 
 
 
 	public static Vector sortOntologyInfo(Vector v) {
 		Vector u = new Vector();
         Collections.sort(v, new OntologyInfo.ComparatorImpl());
 		for (int i = 0; i < v.size(); i++) {
 			OntologyInfo info = (OntologyInfo) v.elementAt(i);
 			if (!DataUtils.isNull(info.getTag()) && info.getTag().compareToIgnoreCase("PRODUCTION") == 0) {
 				u.add(info);
 			    if (info.getExpanded()) {
 					Vector w = getNonProductionOntologies(v, info.getCodingScheme());
 					for (int j=0; j<w.size(); j++) {
 						OntologyInfo ontologyInfo = (OntologyInfo) w.elementAt(j);
 						u.add(ontologyInfo);
 					}
 				}
 			}
 		}
 		return u;
 	}
 
 	public static String getNavigationTabType(String dictionary, String version,
 	        String vsd_uri, String nav_type) {
 	    _logger.debug("Method: DataUtils.getNavigationTabType");
 		_logger.debug("  * dictionary: " + dictionary);
 		_logger.debug("  * version: " + version);
 		_logger.debug("  * vsd_uri: " + vsd_uri);
 		_logger.debug("  * nav_type: " + nav_type);
 
 		if (nav_type != null)
 		    return nav_type;
 		if (vsd_uri != null)
 			return "valuesets";
 		if (dictionary != null) {
 			boolean isMapping = isMapping(dictionary, version);
 			return isMapping ? "mappings" : "terminologies";
 		}
 		return null;
 	}
 
 	public static boolean validateCodingSchemeVersion(String codingSchemeName, String version) {
 		if (!_localName2FormalNameHashMap.containsKey(codingSchemeName)) return false;
 	    String formalname = (String) _localName2FormalNameHashMap.get(codingSchemeName);
 	    if (formalname == null) return false;
 
 	    if (!_formalName2VersionsHashMap.containsKey(formalname)) return false;
 	    Vector version_vec = (Vector) _formalName2VersionsHashMap.get(formalname);
 	    if (version_vec == null) return false;
 	    if (!version_vec.contains(version)) return false;
 	    return true;
     }
 
 
 	public static String getVersionReleaseDate(String codingSchemeName, String version) {
         if (_versionReleaseDateHashMap == null) {
             setCodingSchemeMap();
         }
 		String key = getFormalName(codingSchemeName) + "$" + version;
 		if (_versionReleaseDateHashMap.containsKey(key)) {
 			return (String) _versionReleaseDateHashMap.get(key);
 		}
 		/*
 
 		Vector v = MetadataUtils.getMetadataNameValuePairs(codingSchemeName, version);
 		if (v == null) return null;
 		if (v.size() == 0) return null;
 		String release_date = (String) v.elementAt(0);
 		_versionReleaseDateHashMap.put(key, release_date);
 		return release_date;
 		*/
 		return null;
 	}
 
 
     static {
 		_valueset_item_vec = new Vector();
 		valueSetDefinitionOnServer_uri_vec = new Vector();
 		Vector key_vec = new Vector();
 
 		HashMap hmap = new HashMap();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         for (int i=0; i<list.size(); i++) {
 			String t = (String) list.get(i);
 			ValueSetDefinition vsd = findValueSetDefinitionByURI(t);
 			if (vsd != null) {
 				String name = vsd.getValueSetDefinitionName();
 				String uri = vsd.getValueSetDefinitionURI();
 				if (name == null) {
 					name = "<NOT ASSIGNED>";
 				}
 				hmap.put(name, vsd);
 			    valueSetDefinitionOnServer_uri_vec.add(uri);
 			    key_vec.add(name);
 		    }
 		}
 
 		key_vec = SortUtils.quickSort(key_vec);
 		for (int i=0; i<key_vec.size(); i++) {
 			String key = (String) key_vec.elementAt(i);
 			ValueSetDefinition vsd = (ValueSetDefinition) hmap.get(key);
 			_valueset_item_vec.add(new SelectItem(vsd.getValueSetDefinitionURI(), key)); // value, label
 		}
 
 	}
 
 
 	public static Vector getValueSetDefinitions() {
 		/*
 		if (_valueset_item_vec != null) return _valueset_item_vec;
 
 		_valueset_item_vec = new Vector();
 		valueSetDefinitionOnServer_uri_vec = new Vector();
 		Vector key_vec = new Vector();
 
 		HashMap hmap = new HashMap();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         for (int i=0; i<list.size(); i++) {
 			String t = (String) list.get(i);
 			ValueSetDefinition vsd = findValueSetDefinitionByURI(t);
 			if (vsd != null) {
 				String name = vsd.getValueSetDefinitionName();
 				String uri = vsd.getValueSetDefinitionURI();
 				if (name == null) {
 					name = "<NOT ASSIGNED>";
 				}
 				hmap.put(name, vsd);
 			    valueSetDefinitionOnServer_uri_vec.add(uri);
 			    key_vec.add(name);
 		    }
 		}
 
 		key_vec = SortUtils.quickSort(key_vec);
 		for (int i=0; i<key_vec.size(); i++) {
 			String key = (String) key_vec.elementAt(i);
 			ValueSetDefinition vsd = (ValueSetDefinition) hmap.get(key);
 			_valueset_item_vec.add(new SelectItem(vsd.getValueSetDefinitionURI(), key)); // value, label
 		}
 		*/
 		return _valueset_item_vec;
 	}
 
 	public static HashMap code2Name(String scheme, String version, List codes) {
 		HashMap hmap = new HashMap();
 
         try {
             LexBIGService lbSvc = new RemoteServerUtil().createLexBIGService();
             if (lbSvc == null) {
                 _logger.warn("lbSvc == null???");
                 return null;
             }
 
             CodingSchemeVersionOrTag versionOrTag =
                 new CodingSchemeVersionOrTag();
             versionOrTag.setVersion(version);
 
             ConceptReferenceList crefs = new ConceptReferenceList();
             for (int i=0; i<codes.size(); i++) {
 				String code = (String) codes.get(i);
 				ConceptReference vConceptReference = ConvenienceMethods.createConceptReference(code, scheme);
 				crefs.addConceptReference(vConceptReference);
 			}
 
             CodedNodeSet cns = null;
 
             try {
                 cns = lbSvc.getCodingSchemeConcepts(scheme, versionOrTag);
 				cns = cns.restrictToCodes(crefs);
 				ResolvedConceptReferenceList matches =
 					cns.resolveToList(null, null, null, null, false, -1);
 
 				if (matches == null) {
 					_logger.warn("Concept not found.");
 					return null;
 				}
 
 				// Analyze the result ...
 				for (int k=0; k<matches.getResolvedConceptReferenceCount(); k++) {
 					ResolvedConceptReference ref =
 						(ResolvedConceptReference) matches
 							.getResolvedConceptReference(k);
 					hmap.put(ref.getConceptCode(), ref.getEntityDescription().getContent());
 				}
 
             } catch (Exception e1) {
                 e1.printStackTrace();
             }
 
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         return hmap;
 	}
 
 
 	public static String getModeOfOperation() {
 	    if (_mode_of_operation != null) return _mode_of_operation;
         try {
             NCImtBrowserProperties properties = NCImtBrowserProperties.getInstance();
             _mode_of_operation = properties.getProperty(NCImtBrowserProperties.MODE_OF_OPERATION);
 	    } catch (Exception ex) {
 	        ex.printStackTrace();
 	    }
     	return _mode_of_operation;
 	}
 
 
 	//    public static String[] hide_options = new String[] {"NONE", "Valid", "Invalid", "Pending"}; //, "Undecided"};
 	public static Vector getShowOptions(String[] hide_options) {
 
 		Vector v = new Vector();
 		for (int i=0; i<hide_options.length; i++) {
 			String s = hide_options[i];
 			v.add(s);
         }
 
         Vector show_options = new Vector();
         for (int i=0; i<status_options.length; i++) {
 	        String t = status_options[i];
 	        show_options.add(t);
         }
 
 		if (v.contains("NONE")) return show_options;
 		for (int i=0; i<hide_options.length; i++) {
 			String s = hide_options[i];
 			show_options.remove(s);
 		}
 		return show_options;
 	}
 
      public static CodedNodeSet getNodeSet(LexBIGService lbSvc, String scheme, CodingSchemeVersionOrTag versionOrTag)
         throws Exception {
 		CodedNodeSet cns = null;
 		try {
 			cns = lbSvc.getCodingSchemeConcepts(scheme, versionOrTag);
 			CodedNodeSet.AnonymousOption restrictToAnonymous = CodedNodeSet.AnonymousOption.NON_ANONYMOUS_ONLY;
 			cns = cns.restrictToAnonymous(restrictToAnonymous);
 	    } catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
  	    return cns;
     }
 
     public static ResolvedConceptReferencesIterator getEntitiesInCodingScheme(String codingSchemeName, String vers) {
         try {
             LexBIGService lbSvc = new RemoteServerUtil().createLexBIGService();
             if (lbSvc == null) {
                 System.out.println("lbSvc == null???");
                 return null;
             }
             CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
             if (vers != null) versionOrTag.setVersion(vers);
 
             CodedNodeSet cns = null;
             try {
 				try {
 					cns = getNodeSet(lbSvc, codingSchemeName, versionOrTag);
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
                 if (cns == null) {
 					System.out.println("getConceptByCode getCodingSchemeConcepts returns null??? " + codingSchemeName);
 					return null;
 				}
 				try {
 					ResolvedConceptReferencesIterator iterator = cns.resolve(null, null, null, null, false);
 					int numberRemaining = iterator.numberRemaining();
 
 					System.out.println("numberRemaining: " + numberRemaining);
 
 					return iterator;
 				} catch (Exception e) {
 					System.out.println("cns.resolveToList failed???");
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
 	public static String getValueSetDefinitionURIByName(String vsd_name) {
 		//Vector v = new Vector();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         List list = vsd_service.listValueSetDefinitionURIs();
         for (int i=0; i<list.size(); i++) {
 			String uri = (String) list.get(i);
 			ValueSetDefinition vsd = findValueSetDefinitionByURI(uri);
 			String name = vsd.getValueSetDefinitionName();
 			if (name != null && name.compareTo(vsd_name) == 0) {
 				return uri;
 			}
 		}
 		return null;
 	}
 
 	public static boolean isInteger( String input )
 	{
 	   if (input == null) return false;
 	   try {
 		  Integer.parseInt( input );
 		  return true;
 	   } catch( Exception e) {
 		  return false;
 	   }
 	}
 
 }
