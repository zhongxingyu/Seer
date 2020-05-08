 package gov.nih.nci.evs.browser.bean;
 
 import java.util.*;
 import java.net.URI;
 import java.io.*;
 
 import javax.faces.context.*;
 import javax.faces.event.*;
 import javax.faces.model.*;
 import javax.servlet.http.*;
 
 import gov.nih.nci.evs.browser.utils.*;
 import gov.nih.nci.evs.browser.properties.*;
 import gov.nih.nci.evs.browser.common.*;
 
 import org.LexGrid.LexBIG.DataModel.Collections.*;
 import org.LexGrid.LexBIG.DataModel.Core.*;
 import org.LexGrid.LexBIG.LexBIGService.*;
 import org.LexGrid.LexBIG.Utility.*;
 import org.LexGrid.codingSchemes.*;
 import org.LexGrid.naming.*;
 import org.LexGrid.LexBIG.Impl.Extensions.GenericExtensions.*;
 import org.apache.log4j.*;
 import javax.faces.event.ValueChangeEvent;
 
 import org.LexGrid.LexBIG.caCore.interfaces.LexEVSDistributed;
 import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
 import org.LexGrid.valueSets.ValueSetDefinition;
 import org.LexGrid.commonTypes.Source;
 import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
 import org.lexgrid.valuesets.dto.ResolvedValueSetDefinition;
 import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
 import javax.servlet.ServletOutputStream;
 import org.LexGrid.concepts.*;
 import org.lexgrid.valuesets.dto.ResolvedValueSetCodedNodeSet;
 
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
 import org.LexGrid.concepts.Definition;
 import org.LexGrid.commonTypes.PropertyQualifier;
 import org.LexGrid.commonTypes.Property;
 
 
 public class MappingThread implements Runnable
 {
   public HttpServletRequest request = null;
  HashMap<String, String> requestParameterMap = null;
   HashMap mapping_hmap = null;
   HttpSession session = null;
   String type;
 
   String vsd_uri = null;
 
   CodedNodeSet vsd_cns = null;
   String valueSetDefinitionRevisionId = null;
   AbsoluteCodingSchemeVersionReferenceList csVersionList;
   String csVersionTag;
   boolean advanced = false;
 
   public void setAdvanced(boolean flag) {
 	  this.advanced = advanced;
   }
 
   public MappingThread(HttpServletRequest request, HashMap mapping_hmap)
   {
     this.request = request;
    this.requestParameterMap = HTTPUtils.retrieveRequestParametersMap(request);
     this.mapping_hmap = mapping_hmap;
     this.session = request.getSession(true);
     this.type = "ncimeta"; //default
   }
 
   public MappingThread(HttpServletRequest request, HashMap mapping_hmap, String type)
   {
     this.request = request;
    this.requestParameterMap = HTTPUtils.retrieveRequestParametersMap(request);
     this.mapping_hmap = mapping_hmap;
     this.session = request.getSession(true);
     this.type = type;
   }
 
 
   public MappingThread(HttpServletRequest request, HashMap mapping_hmap, String type, boolean advanced)
   {
     this.request = request;
    this.requestParameterMap = HTTPUtils.retrieveRequestParametersMap(request);
     this.mapping_hmap = mapping_hmap;
     this.session = request.getSession(true);
     this.type = type;
     this.advanced = advanced;
   }
 
 
   public MappingThread(HttpServletRequest request, HashMap mapping_hmap, String type, String vsd_uri,
 		  String valueSetDefinitionRevisionId,
 		  AbsoluteCodingSchemeVersionReferenceList csVersionList,
 		  String csVersionTag)
   {
 
 System.out.println("MappingThread THREAD " + vsd_uri);
 
     this.request = request;
    this.requestParameterMap = HTTPUtils.retrieveRequestParametersMap(request);
     this.mapping_hmap = mapping_hmap;
     this.session = request.getSession(true);
     this.type = type;
 
     this.vsd_uri = vsd_uri;
     this.valueSetDefinitionRevisionId = valueSetDefinitionRevisionId;
     this.csVersionList = csVersionList;
     this.csVersionTag = csVersionTag;
 
 	LexEVSDistributed distributed = null;
 	LexEVSValueSetDefinitionServices vds = null;
 	try {
 		vds = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 
 if (vds == null) {
 	System.out.println("MappingThread vds == null??? " + vsd_uri);
 }
 
 
 		ResolvedValueSetDefinition rvsd = null;
 		try {
 
 System.out.println("MappingThread calling getCodedNodeSetForValueSetDefinition ..." + vsd_uri);
 
             ResolvedValueSetCodedNodeSet rvscns = vds.getCodedNodeSetForValueSetDefinition(new URI(vsd_uri), valueSetDefinitionRevisionId, csVersionList, csVersionTag);
 
 System.out.println("MappingThread getCodedNodeSetForValueSetDefinition DONE" + vsd_uri);
 
 
 
 			if (rvscns == null) {
 				System.out.println("ERROR: rvscns not found -- " + vsd_uri);
 			} else {
 				System.out.println("rvscns FOUND -- " + vsd_uri);
 				this.vsd_cns = rvscns.getCodedNodeSet();
 		    }
 		} catch (Exception ex) {
 			System.out.println("??? vds.resolveValueSetDefinition throws exception");
 			return;
 		}
 	} catch (Exception ex) {
 		ex.printStackTrace();
 	}
 
 
 System.out.println("MappingThread THREAD instantiated " + vsd_uri);
 
   }
 
 
   public void run()
   {
 	    //System.out.println("THREAD type: " + type);
 	    //System.out.println("THREAD advanced: " + advanced);
 	    String algorithm = null;
 	    String input_option = null;
 
 	    String property = null;
 
 		//String src_property = null;
 		String[] src_property = null;
 		String target_property = null;
 		String left_trim = null;
 		String right_trim = null;
 		String prefix = null;
 		String suffix = null;
 		String source_cs = null;
 
 		List input_list = null;
 
 
 	    synchronized (session) {
 	  	    algorithm = (String) session.getAttribute("algorithm");
 	  	    //System.out.println("THREAD algorithm: " + algorithm);
 
 	  	    input_option = (String) session.getAttribute("input_option");
 	  	    //System.out.println("THREAD input_option: " + input_option);
 
 //?????????????????????????????????????????????????????????????????????????????????????????????
 	  	    source_cs = (String) session.getAttribute("source_cs");
 	  	    //System.out.println("THREAD source_cs: " + source_cs);
 	  	    if (source_cs != null) {
 				session.setAttribute("source_cs", source_cs);
 			}
 
 	  	    if (advanced) {
 				//src_property = (String) session.getAttribute("src_property");
 				src_property = (String[]) session.getAttribute("src_property");
 				target_property = (String) session.getAttribute("target_property");
 				left_trim = (String) session.getAttribute("left_trim");
 				right_trim = (String) session.getAttribute("right_trim");
 				prefix = (String) session.getAttribute("prefix");
 				suffix = (String) session.getAttribute("suffix");
 			}
 	    }
 
 //System.out.println("THREAD type (run): " + type);
 
 
 		if (type.compareTo("valueset") == 0) {
 			input_list = (ArrayList) session.getAttribute("data");
 			//source_cs = (String) request.getAttribute("source_cs");
 
 			if (input_list == null || input_list.size() == 0) {
 				System.out.println("(*) input_list??? " );
 				return;
 			}
 
 
 System.out.println("THREAD input_list.size(): " + input_list.size());
 
 
 
 			String source_scheme = DataUtils.key2CodingSchemeName(source_cs);
 			String source_version = DataUtils.key2CodingSchemeVersion(source_cs);
 
 			//String target_scheme = DataUtils.key2CodingSchemeName(target_cs);
 			//String target_version = DataUtils.key2CodingSchemeVersion(target_cs);
 
 			String sourceCodingScheme = DataUtils.getFormalName(source_scheme);
 			if (sourceCodingScheme == null) sourceCodingScheme = source_scheme;
 
 			String sourceCodingSchemeVersion = null;
 			sourceCodingSchemeVersion = source_version;
 
 			//String targetCodingScheme = DataUtils.getFormalName(target_scheme);
 			//if (targetCodingScheme == null) targetCodingScheme = target_scheme;
 
 			//String targetCodingSchemeVersion = null;
 			//targetCodingSchemeVersion = target_version;
 
 System.out.println("THREAD sourceCodingScheme: " + sourceCodingScheme);
 System.out.println("THREAD sourceCodingSchemeVersion: " + sourceCodingSchemeVersion);
 
 
 			ArrayList list = new ArrayList();
 			HashSet hset = new HashSet();
 			String source = null;
 			boolean ranking = false;
 
 			int counter = 0;
 			for (int i=0; i<input_list.size(); i++) {
 				String input_value = (String) input_list.get(i);
 
 
 System.out.println("THREAD input_value: " + input_value);
 
 
 				String sourceCode = null;
 				String matchtext = null; //(String) values.elementAt(1);
 
 				if (input_option.compareToIgnoreCase("Code") == 0) {
 					sourceCode = input_value;
 
 				} else {
 				    Vector values = DataUtils.parseData(input_value);
 				    sourceCode = (String) values.elementAt(0);
 				    matchtext = (String) values.elementAt(1);
 				}
 
 				List match_list = new ArrayList();
 				ResolvedConceptReferencesIterator iterator = null;
 				String sourceName = "";
 				String sourceCodeNamespace = "";
 				String rel = "";
 				int score = 0;
 				String associationName = "mapsTo";
 
 				try {
 
 					Entity source_entity = MappingUtils.getConceptByCode(sourceCodingScheme,
 															sourceCodingSchemeVersion,
 															null,
 															sourceCode);
 
 					if (source_entity == null) {
 						//System.out.println("source_entity not found: " + sourceCode);
 					} else {
                         //System.out.println("source_entity found: " + sourceCode);
 						if (source_entity != null) {
 							sourceName = source_entity.getEntityDescription().getContent();
 							sourceCodeNamespace = source_entity.getEntityCodeNamespace();
 
 							//System.out.println("sourceName: " + sourceName);
 						}
 
 						if(input_option.compareToIgnoreCase("Code") == 0) {
 							System.out.println("calling getSynonyms: sourceCode " + sourceCode);
 
 							Vector matchtext_vec = new Vector();
 							Vector synonyms = DataUtils.getSynonyms(sourceCodingScheme, sourceCodingSchemeVersion, null, sourceCode);
 							if (synonyms != null) {
 								for (int k=0; k<synonyms.size(); k++) {
 									String synonym_str = (String) synonyms.elementAt(k);
 									Vector v = DataUtils.parseData(synonym_str);
 									String synonym = (String) v.elementAt(0);
 									matchtext_vec.add(synonym);
 									System.out.println(sourceCode + " " + synonym);
 								}
 								iterator = new SearchUtils().searchByName(vsd_cns, matchtext_vec, algorithm);
 						    }
 
 						} else if(input_option.compareTo("Name") == 0) {
 							//iterator = MappingUtils.searchByName(targetCodingScheme, targetCodingSchemeVersion, matchtext, null, algorithm, false, 10);
 							Vector matchtext_vec = new Vector();
 							matchtext_vec.add(matchtext);
 							iterator = new SearchUtils().searchByName(vsd_cns, matchtext_vec, algorithm);
 
 						}
 
 					}
 					if (iterator == null) {
 						System.out.println("******************* search returns null???");
 					} else {
 
 						try {
 							 HashSet key_hset = new HashSet();
 							 int numRemaining = iterator.numberRemaining();
 
 							 System.out.println("(*) valueset Number of matches: " + numRemaining);
 
 							 while (iterator.hasNext()) {
 								 ResolvedConceptReference rcr = (ResolvedConceptReference) iterator.next();
 
 								 String targetCodingScheme = rcr.getCodingSchemeName();
 								 String targetCodingSchemeVersion = rcr.getCodingSchemeVersion();
 
 								 System.out.println("targetCodingScheme: " + targetCodingScheme);
 								 System.out.println("targetCodingSchemeVersion: " + targetCodingSchemeVersion);
 
 
 								 Entity target_entity = MappingUtils.getConceptByCode(targetCodingScheme, targetCodingSchemeVersion, null, rcr.getConceptCode());
 
 								 String targetCode = rcr.getConceptCode();
 								 String targetName = "";
 								 String targetCodeNamespace = "";
 								 if (target_entity != null) {
 									targetName = target_entity.getEntityDescription().getContent();
 									targetCodeNamespace = target_entity.getEntityCodeNamespace();
 								 }
 
 								 String key = sourceCode + "|" + targetCode;
 								 if (!key_hset.contains(key)) {
 									 key_hset.add(key);
 
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
 									 match_list.add(mappingData);
 							     }
 							 }
 							 key_hset.clear();
 
 						 } catch (Exception ex) {
 							ex.printStackTrace();
 						 }
 					  }
 
 					  if (match_list.size() > 0) {
 						  //mapping_hmap.put(input_value, match_list);
 						  updataMappingData(input_value, match_list);
 
 						  synchronized (session) {
 							  session.setAttribute("mapping_hmap", mapping_hmap);
 						  }
 					  }
 					  synchronized (session) {
 						  session.setAttribute("counter", new Integer(i+1).toString());
 					  }
 
 
 				  } catch (Exception ex) {
 					  ex.printStackTrace();
 				  }
 				  counter++;
 				  String msg = "" + counter + " out of " + input_list.size() + " processed.";
 				  System.out.println(msg);
 				  synchronized (session) {
 						  session.setAttribute("batch_status", msg);
 				  }
 
 			}
 			synchronized (session) {
 				session.removeAttribute("refresh");
 				session.removeAttribute("batch_status");
 
 				HashMap mappings = (HashMap) session.getAttribute("mappings");
 				String id = (String) session.getAttribute("id");
 
 				System.out.println("id: " + id);
 
 				if (id != null) {
 					MappingObject obj = (MappingObject) mappings.get(id);
 					obj.setMappingHashMap(this.mapping_hmap);
 					obj.setData(input_list);
 					mappings.put(id, obj);
 
 
 					System.out.println("MAPPING DATA UPDATED.");
 					session.setAttribute("mappings", mappings);
 				}
 
 			}
 			return;
 
 
 	    } else if (type.compareTo("ncimeta") == 0) {
 			String ncim_version = (String) session.getAttribute("ncim_version");
 			if (ncim_version != null && ncim_version.compareTo("null") == 0) {
 				ncim_version = "";
 			} else if (ncim_version == null) {
 				ncim_version = "";
 			}
 
 			String source_abbrev = (String) session.getAttribute("source_abbrev");
 			String target_abbrev = (String) session.getAttribute("target_abbrev");
 
 			input_list = (ArrayList) session.getAttribute("data");
 
 			if (input_list == null || input_list.size() == 0) return;
 
 			String total_str = new Integer(input_list.size()).toString();
 
 			synchronized (session) {
 				session.setAttribute("total", total_str);
 			}
 
 			String source_scheme = null;
 			String source_version = null;
 			String source_namespace = null;
 			String target_scheme = null;
 			String target_version = null;
 
 			String source_code = null;
 			String source_name = null;
 			String rel = null;
 			String score = null;
 			String target_code = null;
 			String target_name = null;
 			String target_namespace = null;
 
 			MappingData mappingData = null;
 
 
 			System.out.println("MappingThread source_abbrev: " + source_abbrev);
 
 
 			if (source_abbrev != null && source_abbrev.compareTo("") != 0) {
 				source_scheme = DataUtils.getFormalName(source_abbrev);
 				source_version = DataUtils.getVocabularyVersionByTag(source_scheme, null);
 			} else {
 				source_scheme = "UNSPECIFIED";
 				source_version = "N/A";
 			}
 
 			target_scheme = DataUtils.getFormalName(target_abbrev);
 			target_version = DataUtils.getVocabularyVersionByTag(target_scheme, null);
 
 			int counter = 0;
 			for (int i=0; i<input_list.size(); i++) {
 				  String input_value = (String) input_list.get(i);
 				  try {
 					  List match_list = MappingUtils.process_ncimeta_mapping(ncim_version,
 														source_abbrev,
 														target_abbrev,
 														input_option,
 														algorithm,
 														input_value);
 					  if (match_list != null) {
 
 						  //mapping_hmap.put(input_value, match_list);
 						  updataMappingData(input_value, match_list);
 
 						  synchronized (session) {
 							  session.setAttribute("mapping_hmap", mapping_hmap);
 						  }
 					  }
 					  synchronized (session) {
 						  session.setAttribute("counter", new Integer(i+1).toString());
 					  }
 
 
 				  } catch (Exception ex) {
 					  ex.printStackTrace();
 				  }
 				  counter++;
 				  String msg = "" + counter + " out of " + input_list.size() + " processed.";
 				  System.out.println(msg);
 				  synchronized (session) {
 						  session.setAttribute("batch_status", msg);
 				  }
 
 			}
 			synchronized (session) {
 				session.removeAttribute("refresh");
 				session.removeAttribute("batch_status");
 
 				HashMap mappings = (HashMap) session.getAttribute("mappings");
 				String id = (String) session.getAttribute("id");
 
	            String mapping_name = requestParameterMap.get("identifier");
	            String mapping_version = requestParameterMap.get("mapping_version");
 
 System.out.println("mapping_name: " + mapping_name);
 System.out.println("mapping_version: " + mapping_version);
 
 				String mapping_key = MappingObject.computeKey(mapping_name, mapping_version);
 
 
 				System.out.println("************* META mapping id: " + id);
 
 				//if (id != null) {
 				//	MappingObject obj = (MappingObject) mappings.get(id);
 
 				if (mapping_key != null) {
 					MappingObject obj = (MappingObject) mappings.get(mapping_key);
 
 					if (obj != null) {
 
 						//session.setAttribute("mapping_hmap", mapping_hmap);
 
 
 						//obj.setMappingHashMap(this.mapping_hmap);
 
 						obj.setMappingHashMap((HashMap) session.getAttribute("mapping_hmap"));
 
 						obj.setData(input_list);
 						//mappings.put(id, obj);
 						mappings.put(mapping_key, obj);
 						System.out.println("MAPPING DATA UPDATED.");
 
 						session.setAttribute("mappings", mappings);
 				    }
 				}
 			}
 			return;
 
 		} else if (type.compareTo("codingscheme") == 0) {
 
 			input_list = (ArrayList) session.getAttribute("data");
 			String target_cs = null; //= (String) request.getParameter("target_cs");
 
 			CodedNodeSet restriction = null;
 
 			synchronized (session) {
 				source_cs = (String) session.getAttribute("source_cs");
 				target_cs = (String) session.getAttribute("target_cs");
 
 				HashMap restrictions = (HashMap) session.getAttribute("restrictions");
 
                 String identifier = null;
                 String mapping_version = null;
 
                 try {
 					identifier = (String) request.getSession().getAttribute("identifier");
 					mapping_version = (String) request.getSession().getAttribute("mapping_version");
 
 				} catch (Exception ex) {
 					identifier = (String) session.getAttribute("identifier");
 					mapping_version = (String) session.getAttribute("mapping_version");
 					//ex.printStackTrace();
 				}
 
 				String mapping_key = MappingObject.computeKey(identifier, mapping_version);
 				if (restrictions != null && mapping_key != null) {
 					ComponentObject obj = (ComponentObject) restrictions.get(mapping_key);
 					if (obj != null) {
 						restriction = obj.toCNS();
 					}
 				} else {
 					System.out.println("(*) MappingThread: No restriction is found for " + identifier + " " + mapping_version);
 				}
 			}
 
 			if (input_list == null || input_list.size() == 0) {
 				System.out.println("(*) input_list??? " );
 				return;
 			}
 
 			property = (String) session.getAttribute("property");
 
 			//src_property = (String) session.getAttribute("src_property");
 			src_property = (String[]) session.getAttribute("src_property");
 			target_property = (String) session.getAttribute("target_property");
 
 			left_trim = (String) session.getAttribute("left_trim");
 			right_trim = (String) session.getAttribute("right_trim");
 			prefix = (String) session.getAttribute("prefix");
 			suffix = (String) session.getAttribute("suffix");
 
 
 			String source_scheme = DataUtils.key2CodingSchemeName(source_cs);
 			String source_version = DataUtils.key2CodingSchemeVersion(source_cs);
 
 			String target_scheme = DataUtils.key2CodingSchemeName(target_cs);
 			String target_version = DataUtils.key2CodingSchemeVersion(target_cs);
 
 			String sourceCodingScheme = DataUtils.getFormalName(source_scheme);
 			if (sourceCodingScheme == null) sourceCodingScheme = source_scheme;
 
 			String sourceCodingSchemeVersion = null;
 			sourceCodingSchemeVersion = source_version;
 
 			String targetCodingScheme = DataUtils.getFormalName(target_scheme);
 			if (targetCodingScheme == null) targetCodingScheme = target_scheme;
 
 			String targetCodingSchemeVersion = null;
 			targetCodingSchemeVersion = target_version;
 
 			ArrayList list = new ArrayList();
 			HashSet hset = new HashSet();
 			String source = null;
 			boolean ranking = false;
 
 			MappingUtils mapping_utils = null;//
 			try {
 				mapping_utils = new MappingUtils();
 		    } catch (Exception ex) {
 				ex.printStackTrace();
 				return;
 			}
 
 			int counter = 0;
 			for (int i=0; i<input_list.size(); i++) {
 				String input_value = (String) input_list.get(i);
 				String sourceCode = null;
 				String matchtext = null;
 
 				if (input_option.compareToIgnoreCase("Code") == 0) {
 					sourceCode = input_value;
 
 				} else if (input_option.compareToIgnoreCase("Name") == 0) {
 				    Vector values = DataUtils.parseData(input_value);
 				    sourceCode = (String) values.elementAt(0);
 				    matchtext = (String) values.elementAt(1);
 				} else if (input_option.compareToIgnoreCase("Property") == 0) {
 					sourceCode = input_value;
 				}
 
 
 				List match_list = new ArrayList();
 				ResolvedConceptReferencesIterator iterator = null;
 				String sourceName = "";
 				String sourceCodeNamespace = "";
 				String rel = "";
 				int score = 0;
 				String associationName = "mapsTo";
 
 
 
 				try {
 
 					Entity source_entity = null;
 					if (sourceCodingScheme != null) {
 						source_entity = MappingUtils.getConceptByCode(sourceCodingScheme,
 															sourceCodingSchemeVersion,
 															null,
 															sourceCode);
 				    }
 					if (source_entity != null) {
 						if (source_entity != null) {
 							sourceName = source_entity.getEntityDescription().getContent();
 							sourceCodeNamespace = source_entity.getEntityCodeNamespace();
 						}
 					} else {
 							sourceName = matchtext;
 							sourceCodeNamespace = "UNSPECIFIED";
 							sourceCodingSchemeVersion = "N/A";
 							sourceCodeNamespace = "N/A";
 					}
 
 					if(input_option.compareToIgnoreCase("Code") == 0) {
 						Vector matchtext_vec = new Vector();
 						Vector synonyms = DataUtils.getSynonyms(sourceCodingScheme, sourceCodingSchemeVersion, null, sourceCode);
 						if (synonyms != null) {
 							for (int k=0; k<synonyms.size(); k++) {
 								String synonym_str = (String) synonyms.elementAt(k);
 								Vector v = DataUtils.parseData(synonym_str);
 								String synonym = (String) v.elementAt(0);
 								matchtext_vec.add(synonym);
 								System.out.println(sourceCode + " " + synonym);
 							}
 							iterator = new SearchUtils().searchByName(targetCodingScheme, targetCodingSchemeVersion, matchtext_vec, algorithm, restriction);
 						}
 
 					} else if(input_option.compareTo("Name") == 0) {
 						iterator = mapping_utils.searchByName(targetCodingScheme, targetCodingSchemeVersion, matchtext, null, algorithm, false, -1);
 
 					} else if (sourceCodingScheme != null) {
                         Vector cns_vec = new Vector();
 
                         for (int lcv=0; lcv<src_property.length; lcv++) {
 
 							Vector src_properties = mapping_utils.getPropertyValues(sourceCodingScheme, sourceCodingSchemeVersion,
 																			        sourceCode, src_property[lcv]);
 							for (int k=0; k<src_properties.size(); k++) {
 								Vector matchText_vec = new Vector();
 								String value = (String) src_properties.elementAt(k);
 								value = new SearchUtils().convertPropertyValue(value, right_trim, left_trim, prefix, suffix);
 								System.out.println(value);
 								matchText_vec.add(value);
 
 								Vector<CodedNodeSet> w = new SearchUtils().getCNSByMatchingProperty(
 									targetCodingScheme, targetCodingSchemeVersion, target_property, matchText_vec, algorithm, restriction);
 								if (w != null) {
 									for (int j=0; j<w.size(); j++) {
 										CodedNodeSet cns_element = (CodedNodeSet) w.elementAt(j);
 										cns_vec.add(cns_element);
 									}
 								}
 
 							}
 						}
 
                         /*
 						iterator = new SearchUtils().searchByProperty(
 								targetCodingScheme, targetCodingSchemeVersion, target_property, matchText_vec, algorithm, restriction);
 						*/
 						iterator = new SearchUtils().searchByProperty(cns_vec);
 
 					}
 
 
 					if (iterator == null) {
 						System.out.println("******************* search returns null???");
 					} else {
 
 
 						try {
 							 HashSet key_hset = new HashSet();
 							 int numRemaining = iterator.numberRemaining();
 
 System.out.println("******************* numRemaining " + numRemaining);
 
 
 							 while (iterator.hasNext()) {
 								 ResolvedConceptReference rcr = (ResolvedConceptReference) iterator.next();
 								 Entity target_entity = MappingUtils.getConceptByCode(targetCodingScheme, targetCodingSchemeVersion, null, rcr.getConceptCode());
 
 								 String targetCode = rcr.getConceptCode();
 								 String targetName = "";
 								 String targetCodeNamespace = "";
 								 if (target_entity != null) {
 									targetName = target_entity.getEntityDescription().getContent();
 									targetCodeNamespace = target_entity.getEntityCodeNamespace();
 								 }
 
 								 String key = sourceCode + "|" + targetCode;
 								 if (!key_hset.contains(key)) {
 									 key_hset.add(key);
 
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
 									 match_list.add(mappingData);
 							     }
 							 }
 							 key_hset.clear();
 
 						 } catch (Exception ex) {
 							ex.printStackTrace();
 						 }
 					  }
 
 					  if (match_list.size() > 0) {
 						  //mapping_hmap.put(input_value, match_list);
 						  updataMappingData(input_value, match_list);
 
 
 						  synchronized (session) {
 							  session.setAttribute("mapping_hmap", mapping_hmap);
 						  }
 					  }
 					  synchronized (session) {
 						  session.setAttribute("counter", new Integer(i+1).toString());
 					  }
 
 
 				  } catch (Exception ex) {
 					  ex.printStackTrace();
 				  }
 				  counter++;
 				  String msg = "" + counter + " out of " + input_list.size() + " processed.";
 				  System.out.println(msg);
 				  synchronized (session) {
 						  session.setAttribute("batch_status", msg);
 				  }
 
 			}
 
 			synchronized (session) {
 				session.removeAttribute("refresh");
 				session.removeAttribute("batch_status");
 
 				HashMap mappings = (HashMap) session.getAttribute("mappings");
 				String id = (String) session.getAttribute("id");
 
 				System.out.println("id: " + id);
 
 				if (id != null) {
 					MappingObject obj = (MappingObject) mappings.get(id);
 					obj.setMappingHashMap(this.mapping_hmap);
 					obj.setData(input_list);
 					mappings.put(id, obj);
 
 					System.out.println("MAPPING DATA UPDATED.");
 					session.setAttribute("mappings", mappings);
 
 
 				}
 			}
 			return;
 		}
     }
 
 
     public boolean searchMappingData(List match_list, String search_key) {
 		if (match_list == null) return false;
 		for (int i=0; i<match_list.size(); i++) {
 			MappingData mappingData = (MappingData) match_list.get(i);
 			String key = mappingData.getKey();
 			if (search_key.compareTo(key) == 0) {
                 return true;
 			}
 		}
 		return false;
 	}
 
 
     public void updataMappingData(String input_value, List match_list) {
 		List existing_match_list = (ArrayList) mapping_hmap.get(input_value);
         if (existing_match_list == null) {
 			existing_match_list = new ArrayList();
 		}
 		for (int i=0; i<match_list.size(); i++) {
 			MappingData mappingData = (MappingData) match_list.get(i);
 			String key = mappingData.getKey();
 			boolean found = searchMappingData(existing_match_list, key);
 			if (!found) {
 				existing_match_list.add(mappingData);
 			}
 		}
 		mapping_hmap.put(input_value, existing_match_list);
 	}
 
 }
 
