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
 
     private static boolean _debug = false; // DYEE_DEBUG
     
     public static void println(PrintWriter out, String text) {
        if (! _debug)
            return;
        System.out.println("DBG: " + text);
         out.println(text);
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
 
 
       println(out, "");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/yahoo-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/event-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/dom-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/animation-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/container-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/connection-min.js\" ></script>");
       //println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/autocomplete-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/treeview-min.js\" ></script>");
       println(out, "");
       println(out, "");
       println(out, "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
       println(out, "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
       println(out, "  <head>");
       println(out, "  <title>Vocabulary Hierarchy</title>");
       println(out, "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/styleSheet.css\" />");
       println(out, "  <link rel=\"shortcut icon\" href=\"/ncitbrowser/favicon.ico\" type=\"image/x-icon\" />");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/fonts.css\" />");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/grids.css\" />");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/code.css\" />");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/tree.css\" />");
       println(out, "  <script type=\"text/javascript\" src=\"/ncitbrowser/js/script.js\"></script>");
       println(out, "");
       println(out, "  <script language=\"JavaScript\">");
       println(out, "");
       println(out, "    var tree;");
       println(out, "    var nodeIndex;");
       println(out, "    var rootDescDiv;");
       println(out, "    var emptyRootDiv;");
       println(out, "    var treeStatusDiv;");
       println(out, "    var nodes = [];");
       println(out, "");
       println(out, "    function load(url,target) {");
       println(out, "      if (target != '')");
       println(out, "        target.window.location.href = url;");
       println(out, "      else");
       println(out, "        window.location.href = url;");
       println(out, "    }");
       println(out, "");
       println(out, "    function init() {");
       println(out, "");
       println(out, "      rootDescDiv = new YAHOO.widget.Module(\"rootDesc\", {visible:false} );");
       println(out, "      resetRootDesc();");
       println(out, "");
       println(out, "      emptyRootDiv = new YAHOO.widget.Module(\"emptyRoot\", {visible:true} );");
       println(out, "      resetEmptyRoot();");
       println(out, "");
       println(out, "      treeStatusDiv = new YAHOO.widget.Module(\"treeStatus\", {visible:true} );");
       println(out, "      resetTreeStatus();");
       println(out, "");
       println(out, "      initTree();");
       println(out, "    }");
       println(out, "");
       println(out, "    function addTreeNode(rootNode, nodeInfo) {");
       println(out, "      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       println(out, "      var newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       println(out, "      var newNode = new YAHOO.widget.TextNode(newNodeData, rootNode, false);");
       println(out, "      if (nodeInfo.ontology_node_child_count > 0) {");
       println(out, "        newNode.setDynamicLoad(loadNodeData);");
       println(out, "      }");
       println(out, "    }");
       println(out, "");
       println(out, "    function buildTree(ontology_node_id, ontology_display_name) {");
       println(out, "      var handleBuildTreeSuccess = function(o) {");
       println(out, "        var respTxt = o.responseText;");
       println(out, "        var respObj = eval('(' + respTxt + ')');");
       println(out, "        if ( typeof(respObj) != \"undefined\") {");
       println(out, "          if ( typeof(respObj.root_nodes) != \"undefined\") {");
       println(out, "            var root = tree.getRoot();");
       println(out, "            if (respObj.root_nodes.length == 0) {");
       println(out, "              showEmptyRoot();");
       println(out, "            }");
       println(out, "            else {");
       println(out, "              for (var i=0; i < respObj.root_nodes.length; i++) {");
       println(out, "                var nodeInfo = respObj.root_nodes[i];");
       println(out, "                var expand = false;");
       println(out, "                addTreeNode(root, nodeInfo, expand);");
       println(out, "              }");
       println(out, "            }");
       println(out, "");
       println(out, "            tree.draw();");
       println(out, "          }");
       println(out, "        }");
       println(out, "        resetTreeStatus();");
       println(out, "      }");
       println(out, "");
       println(out, "      var handleBuildTreeFailure = function(o) {");
       println(out, "        resetTreeStatus();");
       println(out, "        resetEmptyRoot();");
       println(out, "        alert('responseFailure: ' + o.statusText);");
       println(out, "      }");
       println(out, "");
       println(out, "      var buildTreeCallback =");
       println(out, "      {");
       println(out, "        success:handleBuildTreeSuccess,");
       println(out, "        failure:handleBuildTreeFailure");
       println(out, "      };");
       println(out, "");
       println(out, "      if (ontology_display_name!='') {");
       println(out, "        resetEmptyRoot();");
       println(out, "");
       println(out, "        showTreeLoadingStatus();");
       println(out, "        var ontology_source = null;");
       println(out, "        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       println(out, "        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=build_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version+'&ontology_source='+ontology_source,buildTreeCallback);");
       println(out, "      }");
       println(out, "    }");
       println(out, "");
       println(out, "    function resetTree(ontology_node_id, ontology_display_name) {");
       println(out, "");
       println(out, "      var handleResetTreeSuccess = function(o) {");
       println(out, "        var respTxt = o.responseText;");
       println(out, "        var respObj = eval('(' + respTxt + ')');");
       println(out, "        if ( typeof(respObj) != \"undefined\") {");
       println(out, "          if ( typeof(respObj.root_node) != \"undefined\") {");
       println(out, "            var root = tree.getRoot();");
       println(out, "            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.root_node.ontology_node_id + \"');\";");
       println(out, "            var rootNodeData = { label:respObj.root_node.ontology_node_name, id:respObj.root_node.ontology_node_id, href:nodeDetails };");
       println(out, "            var expand = false;");
       println(out, "            if (respObj.root_node.ontology_node_child_count > 0) {");
       println(out, "              expand = true;");
       println(out, "            }");
       println(out, "            var ontRoot = new YAHOO.widget.TextNode(rootNodeData, root, expand);");
       println(out, "");
       println(out, "            if ( typeof(respObj.child_nodes) != \"undefined\") {");
       println(out, "              for (var i=0; i < respObj.child_nodes.length; i++) {");
       println(out, "                var nodeInfo = respObj.child_nodes[i];");
       println(out, "                addTreeNode(ontRoot, nodeInfo);");
       println(out, "              }");
       println(out, "            }");
       println(out, "            tree.draw();");
       println(out, "            setRootDesc(respObj.root_node.ontology_node_name, ontology_display_name);");
       println(out, "          }");
       println(out, "        }");
       println(out, "        resetTreeStatus();");
       println(out, "      }");
       println(out, "");
       println(out, "      var handleResetTreeFailure = function(o) {");
       println(out, "        resetTreeStatus();");
       println(out, "        alert('responseFailure: ' + o.statusText);");
       println(out, "      }");
       println(out, "");
       println(out, "      var resetTreeCallback =");
       println(out, "      {");
       println(out, "        success:handleResetTreeSuccess,");
       println(out, "        failure:handleResetTreeFailure");
       println(out, "      };");
       println(out, "      if (ontology_node_id!= '') {");
       println(out, "        showTreeLoadingStatus();");
       println(out, "        var ontology_source = null;");
       println(out, "        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       println(out, "        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=reset_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name + '&version='+ ontology_version +'&ontology_source='+ontology_source,resetTreeCallback);");
       println(out, "      }");
       println(out, "    }");
       println(out, "");
       println(out, "    function onClickTreeNode(ontology_node_id) {");
       println(out, "      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       println(out, "      var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       println(out, "      load('/ncitbrowser/ConceptReport.jsp?dictionary='+ ontology_display_name + '&version='+ ontology_version  + '&code=' + ontology_node_id,top.opener);");
       println(out, "    }");
       println(out, "");
       println(out, "    function onClickViewEntireOntology(ontology_display_name) {");
       println(out, "      var ontology_display_name = document.pg_form.ontology_display_name.value;");
       println(out, "      tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       println(out, "      tree.draw();");
       println(out, "      resetRootDesc();");
       println(out, "      buildTree('', ontology_display_name);");
       println(out, "    }");
       println(out, "");
       println(out, "    function initTree() {");
       println(out, "");
       println(out, "      tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       println(out, "      var ontology_node_id = document.forms[\"pg_form\"].ontology_node_id.value;");
       println(out, "      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       println(out, "");
       println(out, "      if (ontology_node_id == null || ontology_node_id == \"null\")");
       println(out, "      {");
       println(out, "          buildTree(ontology_node_id, ontology_display_name);");
       println(out, "      }");
       println(out, "      else");
       println(out, "      {");
       println(out, "          searchTree(ontology_node_id, ontology_display_name);");
       println(out, "      }");
       println(out, "    }");
       println(out, "");
       println(out, "    function initRootDesc() {");
       println(out, "      rootDescDiv.setBody('');");
       println(out, "      initRootDesc.show();");
       println(out, "      rootDescDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function resetRootDesc() {");
       println(out, "      rootDescDiv.hide();");
       println(out, "      rootDescDiv.setBody('');");
       println(out, "      rootDescDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function resetEmptyRoot() {");
       println(out, "      emptyRootDiv.hide();");
       println(out, "      emptyRootDiv.setBody('');");
       println(out, "      emptyRootDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function resetTreeStatus() {");
       println(out, "      treeStatusDiv.hide();");
       println(out, "      treeStatusDiv.setBody('');");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showEmptyRoot() {");
       println(out, "      emptyRootDiv.setBody(\"<span class='instruction_text'>No root nodes available.</span>\");");
       println(out, "      emptyRootDiv.show();");
       println(out, "      emptyRootDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showNodeNotFound(node_id) {");
       println(out, "      //emptyRootDiv.setBody(\"<span class='instruction_text'>Concept with code \" + node_id + \" not found in the hierarchy.</span>\");");
       println(out, "      emptyRootDiv.setBody(\"<span class='instruction_text'>Concept not part of the parent-child hierarchy in this source; check other relationships.</span>\");");
       println(out, "      emptyRootDiv.show();");
       println(out, "      emptyRootDiv.render();");
       println(out, "    }");
       println(out, "    ");
       println(out, "    function showPartialHierarchy() {");
       println(out, "      rootDescDiv.setBody(\"<span class='instruction_text'>(Note: This tree only shows partial hierarchy.)</span>\");");
       println(out, "      rootDescDiv.show();");
       println(out, "      rootDescDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showTreeLoadingStatus() {");
       println(out, "      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Building tree ...</span>\");");
       println(out, "      treeStatusDiv.show();");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showTreeDrawingStatus() {");
       println(out, "      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Drawing tree ...</span>\");");
       println(out, "      treeStatusDiv.show();");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showSearchingTreeStatus() {");
       println(out, "      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Searching tree... Please wait.</span>\");");
       println(out, "      treeStatusDiv.show();");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showConstructingTreeStatus() {");
       println(out, "      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Constructing tree... Please wait.</span>\");");
       println(out, "      treeStatusDiv.show();");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function loadNodeData(node, fnLoadComplete) {");
       println(out, "      var id = node.data.id;");
       println(out, "");
       println(out, "      var responseSuccess = function(o)");
       println(out, "      {");
       println(out, "        var path;");
       println(out, "        var dirs;");
       println(out, "        var files;");
       println(out, "        var respTxt = o.responseText;");
       println(out, "        var respObj = eval('(' + respTxt + ')');");
       println(out, "        var fileNum = 0;");
       println(out, "        var categoryNum = 0;");
       println(out, "        if ( typeof(respObj.nodes) != \"undefined\") {");
       println(out, "          for (var i=0; i < respObj.nodes.length; i++) {");
       println(out, "            var name = respObj.nodes[i].ontology_node_name;");
       println(out, "            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.nodes[i].ontology_node_id + \"');\";");
       println(out, "            var newNodeData = { label:name, id:respObj.nodes[i].ontology_node_id, href:nodeDetails };");
       println(out, "            var newNode = new YAHOO.widget.TextNode(newNodeData, node, false);");
       println(out, "            if (respObj.nodes[i].ontology_node_child_count > 0) {");
       println(out, "              newNode.setDynamicLoad(loadNodeData);");
       println(out, "            }");
       println(out, "          }");
       println(out, "        }");
       println(out, "        tree.draw();");
       println(out, "        fnLoadComplete();");
       println(out, "      }");
       println(out, "");
       println(out, "      var responseFailure = function(o){");
       println(out, "        alert('responseFailure: ' + o.statusText);");
       println(out, "      }");
       println(out, "");
       println(out, "      var callback =");
       println(out, "      {");
       println(out, "        success:responseSuccess,");
       println(out, "        failure:responseFailure");
       println(out, "      };");
       println(out, "");
       println(out, "      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       println(out, "      var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       println(out, "      var cObj = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=expand_tree&ontology_node_id=' +id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version,callback);");
       println(out, "    }");
       println(out, "");
       println(out, "    function setRootDesc(rootNodeName, ontology_display_name) {");
       println(out, "      var newDesc = \"<span class='instruction_text'>Root set to <b>\" + rootNodeName + \"</b></span>\";");
       println(out, "      rootDescDiv.setBody(newDesc);");
       println(out, "      var footer = \"<a onClick='javascript:onClickViewEntireOntology();' href='#' class='link_text'>view full ontology}</a>\";");
       println(out, "      rootDescDiv.setFooter(footer);");
       println(out, "      rootDescDiv.show();");
       println(out, "      rootDescDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "");
       println(out, "    function searchTree(ontology_node_id, ontology_display_name) {");
       println(out, "");
 
       println(out, "      var root = tree.getRoot();");
 
       new ViewInHierarchyUtil().printTree(out, ontology_display_name, ontology_version, node_id);
 
       println(out, "             tree.draw();");
 
       println(out, "    }");
       println(out, "");
       println(out, "");
       println(out, "    function addTreeBranch(ontology_node_id, rootNode, nodeInfo) {");
       println(out, "      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       println(out, "      var newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       println(out, "");
       println(out, "      var expand = false;");
       println(out, "      var childNodes = nodeInfo.children_nodes;");
       println(out, "");
       println(out, "      if (childNodes.length > 0) {");
       println(out, "          expand = true;");
       println(out, "      }");
       println(out, "      var newNode = new YAHOO.widget.TextNode(newNodeData, rootNode, expand);");
       println(out, "      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       println(out, "          newNode.labelStyle = \"ygtvlabel_highlight\";");
       println(out, "      }");
       println(out, "");
       println(out, "      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       println(out, "         newNode.isLeaf = true;");
       println(out, "         if (nodeInfo.ontology_node_child_count > 0) {");
       println(out, "             newNode.isLeaf = false;");
       println(out, "             newNode.setDynamicLoad(loadNodeData);");
       println(out, "         } else {");
       println(out, "             tree.draw();");
       println(out, "         }");
       println(out, "");
       println(out, "      } else {");
       println(out, "          if (nodeInfo.ontology_node_id != ontology_node_id) {");
       println(out, "          if (nodeInfo.ontology_node_child_count == 0 && nodeInfo.ontology_node_id != ontology_node_id) {");
       println(out, "        newNode.isLeaf = true;");
       println(out, "          } else if (childNodes.length == 0) {");
       println(out, "        newNode.setDynamicLoad(loadNodeData);");
       println(out, "          }");
       println(out, "        }");
       println(out, "      }");
       println(out, "");
       println(out, "      tree.draw();");
       println(out, "      for (var i=0; i < childNodes.length; i++) {");
       println(out, "         var childnodeInfo = childNodes[i];");
       println(out, "         addTreeBranch(ontology_node_id, newNode, childnodeInfo);");
       println(out, "      }");
       println(out, "    }");
       println(out, "    YAHOO.util.Event.addListener(window, \"load\", init);");
       println(out, "");
       println(out, "  </script>");
       println(out, "</head>");
       println(out, "<body>");
       println(out, "  ");
       println(out, "    <!-- Begin Skip Top Navigation -->");
       println(out, "      <a href=\"#evs-content\" class=\"hideLink\" accesskey=\"1\" title=\"Skip repetitive navigation links\">skip navigation links</A>");
       println(out, "    <!-- End Skip Top Navigation --> ");
       println(out, "    <div id=\"popupContainer\">");
       println(out, "      <!-- nci popup banner -->");
       println(out, "      <div class=\"ncipopupbanner\">");
       println(out, "        <a href=\"http://www.cancer.gov\" target=\"_blank\" alt=\"National Cancer Institute\"><img src=\"/ncitbrowser/images/nci-banner-1.gif\" width=\"440\" height=\"39\" border=\"0\" alt=\"National Cancer Institute\" /></a>");
       println(out, "        <a href=\"http://www.cancer.gov\" target=\"_blank\" alt=\"National Cancer Institute\"><img src=\"/ncitbrowser/images/spacer.gif\" width=\"48\" height=\"39\" border=\"0\" alt=\"National Cancer Institute\" class=\"print-header\" /></a>");
       println(out, "      </div>");
       println(out, "      <!-- end nci popup banner -->");
       println(out, "      <div id=\"popupMainArea\">");
       println(out, "        <a name=\"evs-content\" id=\"evs-content\"></a>");
       println(out, "        <table class=\"evsLogoBg\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">");
       println(out, "        <tr>");
       println(out, "          <td valign=\"top\">");
       println(out, "            <a href=\"http://evs.nci.nih.gov/\" target=\"_blank\" alt=\"Enterprise Vocabulary Services\">");
       println(out, "              <img src=\"/ncitbrowser/images/evs-popup-logo.gif\" width=\"213\" height=\"26\" alt=\"EVS: Enterprise Vocabulary Services\" title=\"EVS: Enterprise Vocabulary Services\" border=\"0\" />");
       println(out, "            </a>");
       println(out, "          </td>");
       println(out, "          <td valign=\"top\"><div id=\"closeWindow\"><a href=\"javascript:window.close();\"><img src=\"/ncitbrowser/images/thesaurus_close_icon.gif\" width=\"10\" height=\"10\" border=\"0\" alt=\"Close Window\" />&nbsp;CLOSE WINDOW</a></div></td>");
       println(out, "        </tr>");
       println(out, "        </table>");
       println(out, "");
       println(out, "");
 
 
 
       String release_date = DataUtils.getVersionReleaseDate(ontology_display_name, ontology_version);
       if (ontology_display_name.compareTo("NCI Thesaurus") == 0 || ontology_display_name.compareTo("NCI_Thesaurus") == 0) {
 
       println(out, "    <div>");
       println(out, "      <img src=\"/ncitbrowser/images/thesaurus_popup_banner.gif\" width=\"612\" height=\"56\" alt=\"NCI Thesaurus\" title=\"\" border=\"0\" />");
       println(out, "      ");
       println(out, "	 ");
       println(out, "             <span class=\"texttitle-blue-rightjust-2\">" + ontology_version + " (Release date: " + release_date + ")</span>");
       println(out, "      ");
       println(out, "");
       println(out, "    </div>");
 
       } else {
 
 
       println(out, "    <div>");
       println(out, "      <img src=\"/ncitbrowser/images/other_popup_banner.gif\" width=\"612\" height=\"56\" alt=\"" + ontology_display_name + "\" title=\"\" border=\"0\" />");
       println(out, "      <div class=\"vocabularynamepopupshort\">" + ontology_display_name );
       println(out, "      ");
       println(out, "	 ");
       println(out, "             <span class=\"texttitle-blue-rightjust\">" + ontology_version + " (Release date: " + release_date + ")</span>");
       println(out, "         ");
       println(out, " ");
       println(out, "      </div>");
       println(out, "    </div>");
 
       }
 
 
       println(out, "");
       println(out, "        <div id=\"popupContentArea\">");
       println(out, "          <table width=\"580px\" cellpadding=\"3\" cellspacing=\"0\" border=\"0\">");
       println(out, "            <tr class=\"textbody\">");
       println(out, "              <td class=\"pageTitle\" align=\"left\">");
       println(out, "                " + ontology_display_name + " Hierarchy");
       println(out, "              </td>");
       println(out, "              <td class=\"pageTitle\" align=\"right\">");
       println(out, "                <font size=\"1\" color=\"red\" align=\"right\">");
       println(out, "                  <a href=\"javascript:printPage()\"><img src=\"/ncitbrowser/images/printer.bmp\" border=\"0\" alt=\"Send to Printer\"><i>Send to Printer</i></a>");
       println(out, "                </font>");
       println(out, "              </td>");
       println(out, "            </tr>");
       println(out, "          </table>");
       println(out, "          <!-- Tree content -->");
       println(out, "          <div id=\"rootDesc\">");
       println(out, "            <div id=\"bd\"></div>");
       println(out, "            <div id=\"ft\"></div>");
       println(out, "          </div>");
       println(out, "          <div id=\"treeStatus\">");
       println(out, "            <div id=\"bd\"></div>");
       println(out, "          </div>");
       println(out, "          <div id=\"emptyRoot\">");
       println(out, "            <div id=\"bd\"></div>");
       println(out, "          </div>");
       println(out, "          <div id=\"treecontainer\"></div>");
       println(out, "");
       println(out, "          <form id=\"pg_form\">");
       println(out, "            ");
 
 
       // to be modified:
 
       println(out, "            <input type=\"hidden\" id=\"ontology_node_id\" name=\"ontology_node_id\" value=\"C37927\" />");
       println(out, "            <input type=\"hidden\" id=\"ontology_display_name\" name=\"ontology_display_name\" value=\"NCI Thesaurus\" />");
       println(out, "            <input type=\"hidden\" id=\"schema\" name=\"schema\" value=\"null\" />");
       println(out, "            <input type=\"hidden\" id=\"ontology_version\" name=\"ontology_version\" value=\"11.11d\" />");
 
 
 String ontology_node_id_value = HTTPUtils.cleanXSS(node_id);
 String ontology_display_name_value = HTTPUtils.cleanXSS(ontology_display_name);
 String ontology_version_value = HTTPUtils.cleanXSS(ontology_version);
 //String scheme_value = HTTPUtils.cleanXSS(schema);
 
 
 System.out.println("ontology_node_id_value: " + ontology_node_id_value);
 System.out.println("ontology_display_name_value: " + ontology_display_name_value);
 System.out.println("ontology_version_value: " + ontology_version_value);
 
 
 /*
       println(out, "            <input type=\"hidden\" id=\"ontology_node_id\" name=\"ontology_node_id\" value=\"" + ontology_node_id_value + "\" />");
       println(out, "            <input type=\"hidden\" id=\"ontology_display_name\" name=\"ontology_display_name\" value=\"" + ontology_display_name_value + "\" />");
       //println(out, "            <input type=\"hidden\" id=\"schema\" name=\"schema\" value=\"" + scheme_value + "\" />");
       println(out, "            <input type=\"hidden\" id=\"ontology_version\" name=\"ontology_version\" value=\"" + ontology_version_value + "\" />");
 */
 
       println(out, "");
       println(out, "          </form>");
       println(out, "          <!-- End of Tree control content -->");
       println(out, "        </div>");
       println(out, "      </div>");
       println(out, "    </div>");
       println(out, "  ");
       println(out, "</body>");
       println(out, "</html>");
    }
 
 
 
 }
