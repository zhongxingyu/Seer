 package gov.nih.nci.evs.browser.utils;
 
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
 
 import java.text.DecimalFormat;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 public class HierarchyHelper {
     // -------------------------------------------------------------------------
     private static Logger _logger = Logger.getLogger(HierarchyHelper.class);
     private static DecimalFormat _iFormatter = new java.text.DecimalFormat("0000");
     private static final String INDENT = "  ";
     private final int INDENT_PIXELS = 4;
     private String _basePath = "";
     private String _leafIcon = "";
     private String _expandIcon = "";
     private String _collapseIcon = "";
     private int _ctr = 0;
    private static boolean _debug = false;  // DYEE_DEBUG
     
     // -------------------------------------------------------------------------
     public HierarchyHelper(String basePath) {
         if (basePath == null)
             basePath = "";
         _basePath = basePath;
         _leafIcon = _basePath + "/images/yui/treeview/ln.gif";
         _expandIcon = _basePath + "/images/yui/treeview/lp.gif";
         _collapseIcon = _basePath + "/images/yui/treeview/lm.gif";
     }
 
     public HierarchyHelper() {
         this(null);
     }
 
     public String getLeafIcon() {
         return _leafIcon;
     }
 
     public String getExpandIcon() {
         return _expandIcon;
     }
 
     public String getCollapseIcon() {
         return _collapseIcon;
     }
     
     // -------------------------------------------------------------------------
 	public TreeItem getSampleTree() {
 		TreeItem parent = new TreeItem("root", "Root");
 		
 		add(parent, "C12913", "Abnormal Cell");
 		addActivity(add(parent, "C43431", "Activity"));
 		add(parent, "C12219", "Anatomic Structure, System, or Substance");
 
 		return parent;
 	}
 	
 	private void addActivity(TreeItem parent) {
 		add(parent, "C25404", "Action");
 		add(parent, "C49235", "Administrative Activity");
 		addBehavior(add(parent, "C16326", "Behavior"));
 		add(parent, "C16203", "Clinical or Research Activity");
 		add(parent, "C51306", "Educational Activity");
 		add(parent, "C17706", "Physical Activity");
 		add(parent, "C16847", "Technique");
 	}
 	
 	private void addBehavior(TreeItem parent) {
 		add(parent, "C94296", "Antisocial Behavior");
 		add(parent, "C54264", "Avoidance");
 		add(parent, "C93233", "Ceremony");
 	}
 
     // -------------------------------------------------------------------------
 	public void debug(String text) {
         text = _iFormatter.format(_ctr) + ": " + text; ++_ctr;
         _logger.debug(text);
         //System.out.println(text);
 	}
 	
 	public void debug(TreeItem top, String indent) {
         if (top._expandable)
             debug(indent + "+ " + top._text + " (" + top._code + ")");
         else debug(indent + "* " + top._text + " (" + top._code + ")");
 
 		Map<String, List<TreeItem>> map = top._assocToChildMap;
 		Set<String> keys = map.keySet();
 		Iterator<String> iterator_key = keys.iterator();
 		while (iterator_key.hasNext()) {
 			String key = iterator_key.next();
 			List<TreeItem> items = map.get(key);
 			Iterator<TreeItem> iterator_treeItem = items.iterator();
 			while (iterator_treeItem.hasNext()) {
 				TreeItem item = iterator_treeItem.next(); 
 				debug(item, indent + INDENT);
 			}
 		}
 	}
 	
     // -------------------------------------------------------------------------
     private TreeItem add(TreeItem parent, String code, String text) {
         TreeItem child = new TreeItem(code, text);
         parent.addChild("child", child);
         return child;
     }
     
     private void append(StringBuffer buffer, String text) {
        if (_debug)
            debug(text);
         buffer.append(text + "\n");
     }
     
     // -------------------------------------------------------------------------
 	private StringBuffer getHtml(HttpServletRequest request, String dictionary, 
 			String version, StringBuffer buffer, TreeItem top, String indent, 
 			boolean skip) {
 		Map<String, List<TreeItem>> map = top._assocToChildMap;
 		Set<String> keys = map.keySet();
 		
 		if (! skip) {
     		boolean isLeafNode = ! top._expandable;
     
     		append(buffer, indent + "<table border=0>");
     		append(buffer, indent + "  <tr>");
     		append(buffer, indent + "    <td width=\"" + INDENT_PIXELS + "\"></td>");
     		append(buffer, indent + "    <td>");
 
     		if (isLeafNode)
     			append(buffer, indent + "      " 
     				+ "<img src=\"" + _leafIcon +  "\">"
     				+ "<a href=\"" + JSPUtils.getConceptUrl(request, dictionary, version, top._code) +
     				"\">" + top._text + "</a>" 
     				+ "<div>");
     		else
     			append(buffer, indent + "      " 
     				+ "<div onclick=\"toggle(this)\">" 
     				+ "<img src=\"" + _collapseIcon + "\">" 
     				+ "<a href=\"" + JSPUtils.getConceptUrl(request, dictionary, version, top._code) + "\">" + top._text + "</a></div>"
     				+ "<div>");
 		}
     		
 		Iterator<String> iterator_key = keys.iterator();
 		while (iterator_key.hasNext()) {
 			String key = iterator_key.next();
 			List<TreeItem> items = map.get(key);
 			Iterator<TreeItem> iterator_treeItem = items.iterator();
 			while (iterator_treeItem.hasNext()) {
 				TreeItem item = iterator_treeItem.next(); 
 				String indent2 = indent;
 				if (! skip)
 				    indent2 += "      " + INDENT;
 				getHtml(request, dictionary, version, buffer, item, indent2, false);
 			}
 		}
 
 		if (! skip) {
     		append(buffer, indent + "      </div>");
     		append(buffer, indent + "    </td>");
     		append(buffer, indent + "  </tr>");
     		append(buffer, indent + "</table>");
 		}
 		return buffer;
 	}
 	
 	public String getHtml(HttpServletRequest request, String dictionary, 
 		String version, TreeItem top) {
        Utils.StopWatch stopWatch = new Utils.StopWatch();
 	    _ctr = 0;
 	    StringBuffer buffer = new StringBuffer();
 	    getHtml(request, dictionary, version, buffer, top, "", true);
	    _logger.debug("getHtml: " + stopWatch.getResult());
 	    return buffer.toString();
 	}
 
     // -------------------------------------------------------------------------
 	public static void main(String[] args) {
         String basePath = "/ncitbrowser";
         String dictionary = "NCI Thesaurus";
         String version = "11.09d";
 
         HierarchyHelper helper = new HierarchyHelper(basePath);
 		TreeItem root = helper.getSampleTree();
 		helper.debug(Utils.SEPARATOR);
 		helper.debug(root, "");
 
 //		helper.debug(Utils.SEPARATOR);
 //		String tree = helper.getHtml(null, dictionary, version, root);
 //		helper.debug(tree);
 	}
 }
