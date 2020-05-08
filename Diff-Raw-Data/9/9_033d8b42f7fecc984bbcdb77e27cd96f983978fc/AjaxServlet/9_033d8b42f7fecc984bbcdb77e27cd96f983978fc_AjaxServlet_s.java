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
 
         Utils.StopWatch stopWatch = new Utils.StopWatch();
 //        String max_tree_level_str =
 //            NCItBrowserProperties.getProperty(
 //                NCItBrowserProperties.MAXIMUM_TREE_LEVEL);
 //        int maxLevel = Integer.parseInt(max_tree_level_str);
         CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
         if (ontology_version != null) versionOrTag.setVersion(ontology_version);
 
         String jsonString =
             CacheController.getTree(
                 ontology_display_name, versionOrTag, node_id);
         debugJSONString("Section: search_tree", jsonString);
 
         _logger.debug("search_tree: " + stopWatch.getResult());
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
                 /*
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
                     */
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
 
 
         if (action.equals("search_hierarchy")) {
             search_hierarchy(request, response, node_id, ontology_display_name, ontology_version);
         } else if (action.equals("search_tree")) {
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
 
 
     public static void search_hierarchy(HttpServletRequest request, HttpServletResponse response, String node_id,
         String ontology_display_name, String ontology_version) {
 
       Enumeration parameters = request.getParameterNames();
       String param = null;
       while (parameters.hasMoreElements())
       {
          param = (String) parameters.nextElement();
          String paramValue = request.getParameter(param);
       }
       response.setContentType("text/html");
       PrintWriter out = null;
 
       try {
       	  out = response.getWriter();
       } catch (Exception ex) {
 		  ex.printStackTrace();
 		  return;
 	  }
 
 
       out.println("");
       out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/yahoo-min.js\" ></script>");
       out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/event-min.js\" ></script>");
       out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/dom-min.js\" ></script>");
       out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/animation-min.js\" ></script>");
       out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/container-min.js\" ></script>");
       out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/connection-min.js\" ></script>");
       //out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/autocomplete-min.js\" ></script>");
       out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/treeview-min.js\" ></script>");
       out.println("");
       out.println("");
       out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
       out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
       out.println("  <head>");
       out.println("  <title>Vocabulary Hierarchy</title>");
       out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
       out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/styleSheet.css\" />");
       out.println("  <link rel=\"shortcut icon\" href=\"/ncitbrowser/favicon.ico\" type=\"image/x-icon\" />");
       out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/fonts.css\" />");
       out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/grids.css\" />");
       out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/code.css\" />");
       out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/tree.css\" />");
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/script.js\"></script>");
       out.println("");
       out.println("  <script language=\"JavaScript\">");
       out.println("");
       out.println("    var tree;");
       out.println("    var nodeIndex;");
       out.println("    var rootDescDiv;");
       out.println("    var emptyRootDiv;");
       out.println("    var treeStatusDiv;");
       out.println("    var nodes = [];");
       out.println("");
       out.println("    function load(url,target) {");
       out.println("      if (target != '')");
       out.println("        target.window.location.href = url;");
       out.println("      else");
       out.println("        window.location.href = url;");
       out.println("    }");
       out.println("");
       out.println("    function init() {");
       out.println("");
       out.println("      rootDescDiv = new YAHOO.widget.Module(\"rootDesc\", {visible:false} );");
       out.println("      resetRootDesc();");
       out.println("");
       out.println("      emptyRootDiv = new YAHOO.widget.Module(\"emptyRoot\", {visible:true} );");
       out.println("      resetEmptyRoot();");
       out.println("");
       out.println("      treeStatusDiv = new YAHOO.widget.Module(\"treeStatus\", {visible:true} );");
       out.println("      resetTreeStatus();");
       out.println("");
       out.println("      initTree();");
       out.println("    }");
       out.println("");
       out.println("    function addTreeNode(rootNode, nodeInfo) {");
       out.println("      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       out.println("      var newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       out.println("      var newNode = new YAHOO.widget.TextNode(newNodeData, rootNode, false);");
       out.println("      if (nodeInfo.ontology_node_child_count > 0) {");
       out.println("        newNode.setDynamicLoad(loadNodeData);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function buildTree(ontology_node_id, ontology_display_name) {");
       out.println("      var handleBuildTreeSuccess = function(o) {");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("          if ( typeof(respObj.root_nodes) != \"undefined\") {");
       out.println("            var root = tree.getRoot();");
       out.println("            if (respObj.root_nodes.length == 0) {");
       out.println("              showEmptyRoot();");
       out.println("            }");
       out.println("            else {");
       out.println("              for (var i=0; i < respObj.root_nodes.length; i++) {");
       out.println("                var nodeInfo = respObj.root_nodes[i];");
       out.println("                var expand = false;");
       out.println("                addTreeNode(root, nodeInfo, expand);");
       out.println("              }");
       out.println("            }");
       out.println("");
       out.println("            tree.draw();");
       out.println("          }");
       out.println("        }");
       out.println("        resetTreeStatus();");
       out.println("      }");
       out.println("");
       out.println("      var handleBuildTreeFailure = function(o) {");
       out.println("        resetTreeStatus();");
       out.println("        resetEmptyRoot();");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var buildTreeCallback =");
       out.println("      {");
       out.println("        success:handleBuildTreeSuccess,");
       out.println("        failure:handleBuildTreeFailure");
       out.println("      };");
       out.println("");
       out.println("      if (ontology_display_name!='') {");
       out.println("        resetEmptyRoot();");
       out.println("");
       out.println("        showTreeLoadingStatus();");
       out.println("        var ontology_source = null;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=build_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version+'&ontology_source='+ontology_source,buildTreeCallback);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function resetTree(ontology_node_id, ontology_display_name) {");
       out.println("");
       out.println("      var handleResetTreeSuccess = function(o) {");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("          if ( typeof(respObj.root_node) != \"undefined\") {");
       out.println("            var root = tree.getRoot();");
       out.println("            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.root_node.ontology_node_id + \"');\";");
       out.println("            var rootNodeData = { label:respObj.root_node.ontology_node_name, id:respObj.root_node.ontology_node_id, href:nodeDetails };");
       out.println("            var expand = false;");
       out.println("            if (respObj.root_node.ontology_node_child_count > 0) {");
       out.println("              expand = true;");
       out.println("            }");
       out.println("            var ontRoot = new YAHOO.widget.TextNode(rootNodeData, root, expand);");
       out.println("");
       out.println("            if ( typeof(respObj.child_nodes) != \"undefined\") {");
       out.println("              for (var i=0; i < respObj.child_nodes.length; i++) {");
       out.println("                var nodeInfo = respObj.child_nodes[i];");
       out.println("                addTreeNode(ontRoot, nodeInfo);");
       out.println("              }");
       out.println("            }");
       out.println("            tree.draw();");
       out.println("            setRootDesc(respObj.root_node.ontology_node_name, ontology_display_name);");
       out.println("          }");
       out.println("        }");
       out.println("        resetTreeStatus();");
       out.println("      }");
       out.println("");
       out.println("      var handleResetTreeFailure = function(o) {");
       out.println("        resetTreeStatus();");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var resetTreeCallback =");
       out.println("      {");
       out.println("        success:handleResetTreeSuccess,");
       out.println("        failure:handleResetTreeFailure");
       out.println("      };");
       out.println("      if (ontology_node_id!= '') {");
       out.println("        showTreeLoadingStatus();");
       out.println("        var ontology_source = null;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=reset_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name + '&version='+ ontology_version +'&ontology_source='+ontology_source,resetTreeCallback);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function onClickTreeNode(ontology_node_id) {");
       out.println("      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       out.println("      var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("      load('/ncitbrowser/ConceptReport.jsp?dictionary='+ ontology_display_name + '&version='+ ontology_version  + '&code=' + ontology_node_id,top.opener);");
       out.println("    }");
       out.println("");
       out.println("    function onClickViewEntireOntology(ontology_display_name) {");
       out.println("      var ontology_display_name = document.pg_form.ontology_display_name.value;");
       out.println("      tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       out.println("      tree.draw();");
       out.println("      resetRootDesc();");
       out.println("      buildTree('', ontology_display_name);");
       out.println("    }");
       out.println("");
       out.println("    function initTree() {");
       out.println("");
       out.println("      tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       out.println("      var ontology_node_id = document.forms[\"pg_form\"].ontology_node_id.value;");
       out.println("      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       out.println("");
       out.println("      if (ontology_node_id == null || ontology_node_id == \"null\")");
       out.println("      {");
       out.println("          buildTree(ontology_node_id, ontology_display_name);");
       out.println("      }");
       out.println("      else");
       out.println("      {");
       out.println("          searchTree(ontology_node_id, ontology_display_name);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function initRootDesc() {");
       out.println("      rootDescDiv.setBody('');");
       out.println("      initRootDesc.show();");
       out.println("      rootDescDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function resetRootDesc() {");
       out.println("      rootDescDiv.hide();");
       out.println("      rootDescDiv.setBody('');");
       out.println("      rootDescDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function resetEmptyRoot() {");
       out.println("      emptyRootDiv.hide();");
       out.println("      emptyRootDiv.setBody('');");
       out.println("      emptyRootDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function resetTreeStatus() {");
       out.println("      treeStatusDiv.hide();");
       out.println("      treeStatusDiv.setBody('');");
       out.println("      treeStatusDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function showEmptyRoot() {");
       out.println("      emptyRootDiv.setBody(\"<span class='instruction_text'>No root nodes available.</span>\");");
       out.println("      emptyRootDiv.show();");
       out.println("      emptyRootDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function showNodeNotFound(node_id) {");
       out.println("      //emptyRootDiv.setBody(\"<span class='instruction_text'>Concept with code \" + node_id + \" not found in the hierarchy.</span>\");");
       out.println("      emptyRootDiv.setBody(\"<span class='instruction_text'>Concept not part of the parent-child hierarchy in this source; check other relationships.</span>\");");
       out.println("      emptyRootDiv.show();");
       out.println("      emptyRootDiv.render();");
       out.println("    }");
       out.println("    ");
       out.println("    function showPartialHierarchy() {");
       out.println("      rootDescDiv.setBody(\"<span class='instruction_text'>(Note: This tree only shows partial hierarchy.)</span>\");");
       out.println("      rootDescDiv.show();");
       out.println("      rootDescDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function showTreeLoadingStatus() {");
       out.println("      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Building tree ...</span>\");");
       out.println("      treeStatusDiv.show();");
       out.println("      treeStatusDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function showTreeDrawingStatus() {");
       out.println("      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Drawing tree ...</span>\");");
       out.println("      treeStatusDiv.show();");
       out.println("      treeStatusDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function showSearchingTreeStatus() {");
       out.println("      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Searching tree... Please wait.</span>\");");
       out.println("      treeStatusDiv.show();");
       out.println("      treeStatusDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function showConstructingTreeStatus() {");
       out.println("      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Constructing tree... Please wait.</span>\");");
       out.println("      treeStatusDiv.show();");
       out.println("      treeStatusDiv.render();");
       out.println("    }");
       out.println("");
       out.println("    function loadNodeData(node, fnLoadComplete) {");
       out.println("      var id = node.data.id;");
       out.println("");
       out.println("      var responseSuccess = function(o)");
       out.println("      {");
       out.println("        var path;");
       out.println("        var dirs;");
       out.println("        var files;");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        var fileNum = 0;");
       out.println("        var categoryNum = 0;");
       out.println("        if ( typeof(respObj.nodes) != \"undefined\") {");
       out.println("          for (var i=0; i < respObj.nodes.length; i++) {");
       out.println("            var name = respObj.nodes[i].ontology_node_name;");
       out.println("            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.nodes[i].ontology_node_id + \"');\";");
       out.println("            var newNodeData = { label:name, id:respObj.nodes[i].ontology_node_id, href:nodeDetails };");
       out.println("            var newNode = new YAHOO.widget.TextNode(newNodeData, node, false);");
       out.println("            if (respObj.nodes[i].ontology_node_child_count > 0) {");
       out.println("              newNode.setDynamicLoad(loadNodeData);");
       out.println("            }");
       out.println("          }");
       out.println("        }");
       out.println("        tree.draw();");
       out.println("        fnLoadComplete();");
       out.println("      }");
       out.println("");
       out.println("      var responseFailure = function(o){");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var callback =");
       out.println("      {");
       out.println("        success:responseSuccess,");
       out.println("        failure:responseFailure");
       out.println("      };");
       out.println("");
       out.println("      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       out.println("      var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("      var cObj = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=expand_tree&ontology_node_id=' +id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version,callback);");
       out.println("    }");
       out.println("");
       out.println("    function setRootDesc(rootNodeName, ontology_display_name) {");
       out.println("      var newDesc = \"<span class='instruction_text'>Root set to <b>\" + rootNodeName + \"</b></span>\";");
       out.println("      rootDescDiv.setBody(newDesc);");
       out.println("      var footer = \"<a onClick='javascript:onClickViewEntireOntology();' href='#' class='link_text'>view full ontology}</a>\";");
       out.println("      rootDescDiv.setFooter(footer);");
       out.println("      rootDescDiv.show();");
       out.println("      rootDescDiv.render();");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("    function searchTree(ontology_node_id, ontology_display_name) {");
       out.println("");
 
       out.println("      var root = tree.getRoot();");
 
       new ViewInHierarchyUtil().printTree(out, ontology_display_name, ontology_version, node_id);
 
       out.println("             tree.draw();");
 
       out.println("    }");
       out.println("");
       out.println("");
       out.println("    function addTreeBranch(ontology_node_id, rootNode, nodeInfo) {");
       out.println("      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       out.println("      var newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       out.println("");
       out.println("      var expand = false;");
       out.println("      var childNodes = nodeInfo.children_nodes;");
       out.println("");
       out.println("      if (childNodes.length > 0) {");
       out.println("          expand = true;");
       out.println("      }");
       out.println("      var newNode = new YAHOO.widget.TextNode(newNodeData, rootNode, expand);");
       out.println("      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       out.println("          newNode.labelStyle = \"ygtvlabel_highlight\";");
       out.println("      }");
       out.println("");
       out.println("      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       out.println("         newNode.isLeaf = true;");
       out.println("         if (nodeInfo.ontology_node_child_count > 0) {");
       out.println("             newNode.isLeaf = false;");
       out.println("             newNode.setDynamicLoad(loadNodeData);");
       out.println("         } else {");
       out.println("             tree.draw();");
       out.println("         }");
       out.println("");
       out.println("      } else {");
       out.println("          if (nodeInfo.ontology_node_id != ontology_node_id) {");
       out.println("          if (nodeInfo.ontology_node_child_count == 0 && nodeInfo.ontology_node_id != ontology_node_id) {");
       out.println("        newNode.isLeaf = true;");
       out.println("          } else if (childNodes.length == 0) {");
       out.println("        newNode.setDynamicLoad(loadNodeData);");
       out.println("          }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      tree.draw();");
       out.println("      for (var i=0; i < childNodes.length; i++) {");
       out.println("         var childnodeInfo = childNodes[i];");
       out.println("         addTreeBranch(ontology_node_id, newNode, childnodeInfo);");
       out.println("      }");
       out.println("    }");
       out.println("    YAHOO.util.Event.addListener(window, \"load\", init);");
       out.println("");
       out.println("  </script>");
       out.println("</head>");
       out.println("<body>");
       out.println("  ");
       out.println("    <!-- Begin Skip Top Navigation -->");
       out.println("      <a href=\"#evs-content\" class=\"hideLink\" accesskey=\"1\" title=\"Skip repetitive navigation links\">skip navigation links</A>");
       out.println("    <!-- End Skip Top Navigation --> ");
       out.println("    <div id=\"popupContainer\">");
       out.println("      <!-- nci popup banner -->");
       out.println("      <div class=\"ncipopupbanner\">");
       out.println("        <a href=\"http://www.cancer.gov\" target=\"_blank\" alt=\"National Cancer Institute\"><img src=\"/ncitbrowser/images/nci-banner-1.gif\" width=\"440\" height=\"39\" border=\"0\" alt=\"National Cancer Institute\" /></a>");
       out.println("        <a href=\"http://www.cancer.gov\" target=\"_blank\" alt=\"National Cancer Institute\"><img src=\"/ncitbrowser/images/spacer.gif\" width=\"48\" height=\"39\" border=\"0\" alt=\"National Cancer Institute\" class=\"print-header\" /></a>");
       out.println("      </div>");
       out.println("      <!-- end nci popup banner -->");
       out.println("      <div id=\"popupMainArea\">");
       out.println("        <a name=\"evs-content\" id=\"evs-content\"></a>");
       out.println("        <table class=\"evsLogoBg\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">");
       out.println("        <tr>");
       out.println("          <td valign=\"top\">");
       out.println("            <a href=\"http://evs.nci.nih.gov/\" target=\"_blank\" alt=\"Enterprise Vocabulary Services\">");
       out.println("              <img src=\"/ncitbrowser/images/evs-popup-logo.gif\" width=\"213\" height=\"26\" alt=\"EVS: Enterprise Vocabulary Services\" title=\"EVS: Enterprise Vocabulary Services\" border=\"0\" />");
       out.println("            </a>");
       out.println("          </td>");
       out.println("          <td valign=\"top\"><div id=\"closeWindow\"><a href=\"javascript:window.close();\"><img src=\"/ncitbrowser/images/thesaurus_close_icon.gif\" width=\"10\" height=\"10\" border=\"0\" alt=\"Close Window\" />&nbsp;CLOSE WINDOW</a></div></td>");
       out.println("        </tr>");
       out.println("        </table>");
       out.println("");
       out.println("");
 
 
 
       String release_date = DataUtils.getVersionReleaseDate(ontology_display_name, ontology_version);
       if (ontology_display_name.compareTo("NCI Thesaurus") == 0 || ontology_display_name.compareTo("NCI_Thesaurus") == 0) {
 
       out.println("    <div>");
       out.println("      <img src=\"/ncitbrowser/images/thesaurus_popup_banner.gif\" width=\"612\" height=\"56\" alt=\"NCI Thesaurus\" title=\"\" border=\"0\" />");
       out.println("      ");
       out.println("	 ");
       out.println("             <span class=\"texttitle-blue-rightjust-2\">" + ontology_version + " (Release date: " + release_date + ")</span>");
       out.println("      ");
       out.println("");
       out.println("    </div>");
 
       } else {
 
 
       out.println("    <div>");
       out.println("      <img src=\"/ncitbrowser/images/other_popup_banner.gif\" width=\"612\" height=\"56\" alt=\"" + ontology_display_name + "\" title=\"\" border=\"0\" />");
       out.println("      <div class=\"vocabularynamepopupshort\">" + ontology_display_name );
       out.println("      ");
       out.println("	 ");
       out.println("             <span class=\"texttitle-blue-rightjust\">" + ontology_version + " (Release date: " + release_date + ")</span>");
       out.println("         ");
       out.println(" ");
       out.println("      </div>");
       out.println("    </div>");
 
       }
 
 
       out.println("");
       out.println("        <div id=\"popupContentArea\">");
       out.println("          <table width=\"580px\" cellpadding=\"3\" cellspacing=\"0\" border=\"0\">");
       out.println("            <tr class=\"textbody\">");
       out.println("              <td class=\"pageTitle\" align=\"left\">");
       out.println("                " + ontology_display_name + " Hierarchy");
       out.println("              </td>");
       out.println("              <td class=\"pageTitle\" align=\"right\">");
       out.println("                <font size=\"1\" color=\"red\" align=\"right\">");
       out.println("                  <a href=\"javascript:printPage()\"><img src=\"/ncitbrowser/images/printer.bmp\" border=\"0\" alt=\"Send to Printer\"><i>Send to Printer</i></a>");
       out.println("                </font>");
       out.println("              </td>");
       out.println("            </tr>");
       out.println("          </table>");
       out.println("          <!-- Tree content -->");
       out.println("          <div id=\"rootDesc\">");
       out.println("            <div id=\"bd\"></div>");
       out.println("            <div id=\"ft\"></div>");
       out.println("          </div>");
       out.println("          <div id=\"treeStatus\">");
       out.println("            <div id=\"bd\"></div>");
       out.println("          </div>");
       out.println("          <div id=\"emptyRoot\">");
       out.println("            <div id=\"bd\"></div>");
       out.println("          </div>");
       out.println("          <div id=\"treecontainer\"></div>");
       out.println("");
       out.println("          <form id=\"pg_form\">");
       out.println("            ");
 
 
       // to be modified:
/*
       out.println("            <input type=\"hidden\" id=\"ontology_node_id\" name=\"ontology_node_id\" value=\"C37927\" />");
       out.println("            <input type=\"hidden\" id=\"ontology_display_name\" name=\"ontology_display_name\" value=\"NCI Thesaurus\" />");
       out.println("            <input type=\"hidden\" id=\"schema\" name=\"schema\" value=\"null\" />");
       out.println("            <input type=\"hidden\" id=\"ontology_version\" name=\"ontology_version\" value=\"11.11d\" />");
*/
 
 
 String ontology_node_id_value = HTTPUtils.cleanXSS(node_id);
 String ontology_display_name_value = HTTPUtils.cleanXSS(ontology_display_name);
 String ontology_version_value = HTTPUtils.cleanXSS(ontology_version);
 //String scheme_value = HTTPUtils.cleanXSS(schema);
 
       out.println("            <input type=\"hidden\" id=\"ontology_node_id\" name=\"ontology_node_id\" value=\"" + ontology_node_id_value + "\" />");
       out.println("            <input type=\"hidden\" id=\"ontology_display_name\" name=\"ontology_display_name\" value=\"" + ontology_display_name_value + "\" />");
       //out.println("            <input type=\"hidden\" id=\"schema\" name=\"schema\" value=\"" + scheme_value + "\" />");
       out.println("            <input type=\"hidden\" id=\"ontology_version\" name=\"ontology_version\" value=\"" + ontology_version_value + "\" />");

 
       out.println("");
       out.println("          </form>");
       out.println("          <!-- End of Tree control content -->");
       out.println("        </div>");
       out.println("      </div>");
       out.println("    </div>");
       out.println("  ");
       out.println("</body>");
       out.println("</html>");
    }
 
 
 
 }
