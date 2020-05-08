 package gov.nih.nci.evs.browser.servlet;
 
 import org.json.*;
 
 import gov.nih.nci.evs.browser.utils.*;
 
 import java.io.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.apache.log4j.*;
 
 import gov.nih.nci.evs.browser.properties.*;
 import static gov.nih.nci.evs.browser.common.Constants.*;
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
 import org.LexGrid.valueSets.ValueSetDefinition;
 
 
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
  * Modification history
  *     Initial implementation kim.ong@ngc.com
  *
  */
 
 public final class AjaxServlet extends HttpServlet {
     private static Logger _logger = Logger.getLogger(AjaxServlet.class);
     /**
      * local constants
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Validates the Init and Context parameters, configures authentication URL
      *
      * @throws ServletException if the init parameters are invalid or any other
      *         problems occur during initialisation
      */
     public void init() throws ServletException {
 
     }
 
     /**
      * Route the user to the execute method
      *
      * @param request The HTTP request we are processing
      * @param response The HTTP response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet exception occurs
      */
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         execute(request, response);
     }
 
     /**
      * Route the user to the execute method
      *
      * @param request The HTTP request we are processing
      * @param response The HTTP response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a Servlet exception occurs
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         execute(request, response);
     }
 
     private static void debugJSONString(String msg, String jsonString) {
     	boolean debug = false;  //DYEE_DEBUG
     	if (! debug) 
     		return;
     	_logger.debug(Utils.SEPARATOR);
 	    if (msg != null && msg.length() > 0)
 	    	_logger.debug(msg);
 	    _logger.debug("jsonString: " + jsonString);
 	    _logger.debug("jsonString length: " + jsonString.length());
 	    Utils.debugJSONString(jsonString);
     }
     
     public static void search_tree(HttpServletResponse response, String node_id,
         String ontology_display_name, String ontology_version) {
         try {
             String jsonString = search_tree(node_id,
                 ontology_display_name, ontology_version);
             if (jsonString == null)
                 return;
     
             JSONObject json = new JSONObject();
             JSONArray rootsArray = new JSONArray(jsonString);
             json.put("root_nodes", rootsArray);
     
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             response.getWriter().write(json.toString());
             response.getWriter().flush();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static String search_tree(String node_id,
         String ontology_display_name, String ontology_version) throws Exception {
         if (node_id == null || ontology_display_name == null)
             return null;
 
        long ms = System.currentTimeMillis();
        String max_tree_level_str =
            NCItBrowserProperties.getProperty(
                NCItBrowserProperties.MAXIMUM_TREE_LEVEL);
        int maxLevel = Integer.parseInt(max_tree_level_str);
         CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
         if (ontology_version != null) versionOrTag.setVersion(ontology_version);
 
         String jsonString =
             CacheController.getTree(
                 ontology_display_name, versionOrTag, node_id);
         debugJSONString("Section: search_tree", jsonString);
 
        _logger.debug("Run time (milliseconds): "
            + (System.currentTimeMillis() - ms));
         return jsonString;
     }
 
     /**
      * Process the specified HTTP request, and create the corresponding HTTP
      * response (or forward to another web component that will create it).
      *
      * @param request The HTTP request we are processing
      * @param response The HTTP response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet exception occurs
      */
     public void execute(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         // Determine request by attributes
         String action = request.getParameter("action");// DataConstants.ACTION);
         String node_id = request.getParameter("ontology_node_id");// DataConstants.ONTOLOGY_NODE_ID);
         String ontology_display_name =
             request.getParameter("ontology_display_name");// DataConstants.ONTOLOGY_DISPLAY_NAME);
 
         String ontology_version = request.getParameter("version");
         if (ontology_version == null) {
 			ontology_version = DataUtils.getVocabularyVersionByTag(ontology_display_name, "PRODUCTION");
 		}
 
         long ms = System.currentTimeMillis();
 
         if (action.equals("expand_tree")) {
             if (node_id != null && ontology_display_name != null) {
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
                 try {
 
                     // for HL7 (temporary fix)
                     ontology_display_name =
                         DataUtils.searchFormalName(ontology_display_name);
 
                     nodesArray =
                         CacheController.getInstance().getSubconcepts(
                             ontology_display_name, ontology_version, node_id);
                     if (nodesArray != null) {
                         json.put("nodes", nodesArray);
                     }
 
                 } catch (Exception e) {
                 }
 
                 debugJSONString("Section: expand_tree", json.toString());
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             }
         }
 
         /*
          * else if (action.equals("search_tree")) {
          *
          *
          * if (node_id != null && ontology_display_name != null) {
          * response.setContentType("text/html");
          * response.setHeader("Cache-Control", "no-cache"); JSONObject json =
          * new JSONObject(); try { // testing // JSONArray rootsArray = //
          * CacheController.getInstance().getPathsToRoots(ontology_display_name,
          * // null, node_id, true);
          *
          * String max_tree_level_str = null; int maxLevel = -1; try {
          * max_tree_level_str = NCItBrowserProperties .getInstance()
          * .getProperty( NCItBrowserProperties.MAXIMUM_TREE_LEVEL); maxLevel =
          * Integer.parseInt(max_tree_level_str);
          *
          * } catch (Exception ex) {
          *
          * }
          *
          * JSONArray rootsArray = CacheController.getInstance()
          * .getPathsToRoots(ontology_display_name, null, node_id, true,
          * maxLevel);
          *
          * if (rootsArray.length() == 0) { rootsArray =
          * CacheController.getInstance() .getRootConcepts(ontology_display_name,
          * null);
          *
          * boolean is_root = isRoot(rootsArray, node_id); if (!is_root) {
          * //rootsArray = null; json.put("dummy_root_nodes", rootsArray);
          * response.getWriter().write(json.toString());
          * response.getWriter().flush();
          *
          * _logger.debug("Run time (milliseconds): " +
          * (System.currentTimeMillis() - ms)); return; } }
          * json.put("root_nodes", rootsArray); } catch (Exception e) {
          * e.printStackTrace(); }
          *
          * response.getWriter().write(json.toString());
          * response.getWriter().flush();
          *
          * _logger.debug("Run time (milliseconds): " +
          * (System.currentTimeMillis() - ms)); return; } }
          */
         if (action.equals("search_tree")) {
             search_tree(response, node_id, ontology_display_name, ontology_version);
         }
 
         else if (action.equals("build_tree")) {
             if (ontology_display_name == null)
                 ontology_display_name = CODING_SCHEME_NAME;
 
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             JSONObject json = new JSONObject();
             JSONArray nodesArray = null;// new JSONArray();
             try {
                 nodesArray =
                     CacheController.getInstance().getRootConcepts(
                         ontology_display_name, ontology_version);
                 if (nodesArray != null) {
                     json.put("root_nodes", nodesArray);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             debugJSONString("Section: build_tree", json.toString());
             response.getWriter().write(json.toString());
             // response.getWriter().flush();
 
             _logger.debug("Run time (milliseconds): "
                 + (System.currentTimeMillis() - ms));
             return;
 
         } else if (action.equals("build_vs_tree")) {
 
             if (ontology_display_name == null)
                 ontology_display_name = CODING_SCHEME_NAME;
 
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             JSONObject json = new JSONObject();
             JSONArray nodesArray = null;// new JSONArray();
             try {
 				//HashMap getRootValueSets(String codingSchemeURN)
 				String codingSchemeVersion = null;
                 nodesArray =
                     CacheController.getInstance().getRootValueSets(
                         ontology_display_name, codingSchemeVersion);
                 if (nodesArray != null) {
                     json.put("root_nodes", nodesArray);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             response.getWriter().write(json.toString());
             //System.out.println(json.toString());
 
             _logger.debug("Run time (milliseconds): "
                 + (System.currentTimeMillis() - ms));
             return;
 
         } else if (action.equals("expand_vs_tree")) {
             if (node_id != null && ontology_display_name != null) {
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
 
                 try {
                     nodesArray =
                         CacheController.getInstance().getSubValueSets(
                             ontology_display_name, ontology_version, node_id);
                     if (nodesArray != null) {
 						System.out.println("expand_vs_tree nodesArray != null");
                         json.put("nodes", nodesArray);
                     } else {
 						System.out.println("expand_vs_tree nodesArray == null???");
 					}
 
                 } catch (Exception e) {
                 }
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             }
 
 
         } else if (action.equals("expand_entire_vs_tree")) {
             if (node_id != null && ontology_display_name != null) {
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
 
                 try {
                     nodesArray =
                         CacheController.getInstance().getSourceValueSetTree(
                             ontology_display_name, ontology_version, true);
                     if (nodesArray != null) {
 						System.out.println("expand_entire_vs_tree nodesArray != null");
                         json.put("root_nodes", nodesArray);
                     } else {
 						System.out.println("expand_entire_vs_tree nodesArray == null???");
 					}
 
                 } catch (Exception e) {
                 }
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             }
 
         } else if (action.equals("expand_entire_cs_vs_tree")) {
             //if (node_id != null && ontology_display_name != null) {
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
 
                 try {
                     nodesArray =
                         CacheController.getInstance().getCodingSchemeValueSetTree(
                             ontology_display_name, ontology_version, true);
                     if (nodesArray != null) {
 						System.out.println("expand_entire_vs_tree nodesArray != null");
                         json.put("root_nodes", nodesArray);
                     } else {
 						System.out.println("expand_entire_vs_tree nodesArray == null???");
 					}
 
                 } catch (Exception e) {
                 }
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             //}
 
 
         } else if (action.equals("build_cs_vs_tree")) {
 
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             JSONObject json = new JSONObject();
             JSONArray nodesArray = null;// new JSONArray();
             try {
 				//HashMap getRootValueSets(String codingSchemeURN)
 				String codingSchemeVersion = null;
                 nodesArray =
                     CacheController.getInstance().getRootValueSets(true);
 
                 if (nodesArray != null) {
                     json.put("root_nodes", nodesArray);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             response.getWriter().write(json.toString());
 
             _logger.debug("Run time (milliseconds): "
                 + (System.currentTimeMillis() - ms));
             return;
         } else if (action.equals("expand_cs_vs_tree")) {
 
 			response.setContentType("text/html");
 			response.setHeader("Cache-Control", "no-cache");
 			JSONObject json = new JSONObject();
 			JSONArray nodesArray = null;
 
 			String vsd_uri = ValueSetHierarchy.getValueSetURI(node_id);
 			node_id = ValueSetHierarchy.getCodingSchemeName(node_id);
 
             //if (node_id != null && ontology_display_name != null) {
 			if (node_id != null) {
 				ValueSetDefinition vsd = ValueSetHierarchy.findValueSetDefinitionByURI(vsd_uri);
 				if (vsd == null) {
 					System.out.println("(****) coding scheme name: " + node_id);
 
 				   try {
 					   //
 					    nodesArray = CacheController.getInstance().getRootValueSets(node_id, null);
 						//nodesArray = CacheController.getInstance().getRootValueSets(node_id, null); //find roots (by source)
 
 						if (nodesArray != null) {
 							json.put("nodes", nodesArray);
 						} else {
 							System.out.println("expand_vs_tree nodesArray == null???");
 						}
 
 					} catch (Exception e) {
 					}
 			    } else {
 					try {
 						nodesArray =
 							CacheController.getInstance().getSubValueSets(
 								node_id, null, vsd_uri);
 
 						if (nodesArray != null) {
 							json.put("nodes", nodesArray);
 						}
 
 					} catch (Exception e) {
 					}
 				}
 
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             }
 
 
         } else if (action.equals("build_src_vs_tree")) {
 
 
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             JSONObject json = new JSONObject();
             JSONArray nodesArray = null;// new JSONArray();
             try {
 				//HashMap getRootValueSets(String codingSchemeURN)
 				String codingSchemeVersion = null;
                 nodesArray =
                     //CacheController.getInstance().getRootValueSets(true, true);
                     CacheController.getInstance().build_src_vs_tree();
 
                 if (nodesArray != null) {
                     json.put("root_nodes", nodesArray);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             response.getWriter().write(json.toString());
             //System.out.println(json.toString());
 
             _logger.debug("Run time (milliseconds): "
                 + (System.currentTimeMillis() - ms));
             return;
         } else if (action.equals("expand_src_vs_tree")) {
 
             if (node_id != null && ontology_display_name != null) {
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
 				nodesArray = CacheController.getInstance().expand_src_vs_tree(node_id);
 
 				if (nodesArray == null) {
                     System.out.println("(*) CacheController returns nodesArray == null");
 				}
 
 
                 try {
                     if (nodesArray != null) {
 						System.out.println("expand_src_vs_tree nodesArray != null");
                         json.put("nodes", nodesArray);
                     } else {
 						System.out.println("expand_src_vs_tree nodesArray == null???");
 					}
 
                 } catch (Exception e) {
 					e.printStackTrace();
 
                 }
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             }
         }
 
 
     }
 
     private boolean isRoot(JSONArray rootsArray, String code) {
         for (int i = 0; i < rootsArray.length(); i++) {
             String node_id = null;
             try {
                 JSONObject node = rootsArray.getJSONObject(i);
                 node_id = (String) node.get(CacheController.ONTOLOGY_NODE_ID);
                 if (node_id.compareTo(code) == 0)
                     return true;
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return false;
     }
 }
