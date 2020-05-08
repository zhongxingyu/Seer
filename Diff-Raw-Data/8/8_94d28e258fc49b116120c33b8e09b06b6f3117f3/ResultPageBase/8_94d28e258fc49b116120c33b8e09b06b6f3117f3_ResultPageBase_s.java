 /**********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
 package org.sakaiproject.tool.search;
 
 import org.sakaiproject.tool.OSIDRepository.*;
 import org.sakaiproject.tool.util.*;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import javax.xml.parsers.*;
 
 import org.w3c.dom.*;
 import org.w3c.dom.html.*;
 import org.xml.sax.*;
 
 /**
  * Result rendering - base class and helpers
  */
 public class ResultPageBase implements ResultPageInterface {
 	/**
 	 * Common "resource-search" javascript
 	 */
  private static final String COMMONJS = "SearchResultCommon.js";
 	/**
 	 * Default image count (we'd like to place this many on a page)
 	 */
 	public final static String IMAGES_PER_PAGE = "27";
 	/**
 	 * Search form name
 	 */
 	public static final String FORMNAME = "SEARCHFORM";
 
  protected String						_css;
   protected String  					_database;
   protected String  					_searchQuery;
   protected String						_searchResponseString;
   protected HTMLDocument			_renderedResultDocument;
 	protected SearchResultBase	_searchResult;
 
 	/**
 	 * Constructor
 	 */
   public ResultPageBase() {
     super();
   }
 
 	/**
 	 * Save various attributes of the general search request
 	 * @param query The QueryBase extension that sent the search request
 	 */
   public void initialize(QueryBase query, SearchResultBase searchResult) {
   	_css 										= query.getRequestParameter("cssFile");
   	_database 							= query.getRequestParameter("database");
   	_searchQuery			 			= query.getRequestParameter("searchString");
   	_searchResponseString		= query.getResponseString();
     _renderedResultDocument	= createRenderedResultDocument();
     _searchResult						= searchResult;
   }
 
 	/**
 	 * Save attributes for initial query and error formatting (in some cases
 	 * our caller won't have search results to fetch the attributes from)
 	 * @param database Query format
 	 * @param cssFile The CSS file
 	 */
   public void initialize(String database, String cssFile) {
   	_css 										= cssFile;
   	_database 							= database;
     _renderedResultDocument	= createRenderedResultDocument();
   }
 
 	/**
 	 * Fetch the search response text
 	 * @return Search engine response
 	 */
 	public String getSearchResponseString() {
 		return _searchResponseString;
 	}
 
 	/**
 	 * Get search results for this query (mathing items and convenience methods)
 	 * @return The encapsulated search result object
 	 */
 	public SearchResultBase getSearchResult() {
 		return _searchResult;
 	}
 
 	/**
 	 * Create an empty HTML rendered response document
 	 * @return an HTML Document
 	 */
 	public HTMLDocument createRenderedResultDocument() {
 		return DomUtils.createHtmlDocument();
 	}
 
 	/**
 	 * Get our rendered response Document
 	 * @return Rendered HTML Document
 	 */
 	public HTMLDocument getRenderedResponseDocument() {
 		return _renderedResultDocument;
 	}
 
 	/**
 	 * Fetch the search query
 	 * @return Query text
 	 */
   public String getQuery() {
     return _searchQuery;
   }
 
 	/**
 	 * Fetch the database name
 	 * @return DB name
 	 */
   public String getDatabase() {
     return _database;
   }
 
   /**
    * Render the HTML HEAD portion of our response
    */
   public Element doResultPageHeader() {
   	Document	renderedDocument = getRenderedResponseDocument();
     Element 	element;
     Element 	head;
     String		css;
 
     head = DomUtils.createElement(renderedDocument.getDocumentElement(), "HEAD");
 
     element = DomUtils.createElement(head, "TITLE");
     DomUtils.addText(element, "Resource Search");
 
 		if (_css != null) {
 	    element = DomUtils.createElement(head, "LINK");
 	  	element.setAttribute("href", _css);
 	    element.setAttribute("type", "TEXT/CSS");
 	    element.setAttribute("rel", "STYLESHEET");
 	    element.setAttribute("media", "all");
 		}
 
     element = doJavaScriptElement(head);
     element.setAttribute("src", COMMONJS);
 
     return head;
   }
 
   /**
    * Render the HTML BODY portion of our response
    */
   public Element doResultPageBody() {
   	Document	renderedDocument = getRenderedResponseDocument();
     Element 	body;
 
     body = DomUtils.createElement(renderedDocument.getDocumentElement(), "BODY");
     doSearchHeading(body);
     doSearchForm(body);
 
     return body;
   }
 
   /**
    * Render the HTML BODY portion of an error response
    * @param text Error text
    */
   public Element doResultPageErrorBody(String text) {
   	Document	renderedDocument = getRenderedResponseDocument();
   	Element 	body, table;
 
   	body 	= doResultPageBody();
   	table	= doStandardTableSetup(body);
 
   	doNoResultForQuery(table, "", text);
   	return body;
   }
 
   /**
    * Render the HTML BODY and FORM for the initial query page
    */
   public Element doInitialQueryBody() {
   	Document	renderedDocument = getRenderedResponseDocument();
     Element 	body;
 
     body = DomUtils.createElement(renderedDocument.getDocumentElement(), "BODY");
 		doSearchHeading(body);
     return body;
   }
 
   /**
    * Render page heading
 	 * @param parent Add heading to this HTML element
    */
   public void doSearchHeading(Element parent) {
     doSearchHeading(parent, "Search Library Resources - Note this is a example servlet intented to illustrate the use of the O.K.I. Repository OSID as the Sakai service for accessing data sources.");
   }
 
   /**
    * Render page heading
 	 * @param parent Add heading to this HTML element
 	 * @param heading Heading text
    */
   public void doSearchHeading(Element parent, String heading) {
     Element element;
 
     element = DomUtils.createElement(parent, "H4");
     DomUtils.addText(element, heading);
   }
 
   /**
    * Render the HTML BODY portion of our response
    */
   public void doResultPageFooter() {
   }
 
 	/**
 	 * Render the search form
 	 * @param body The HTML BODY element
 	 */
   public Element doSearchForm(Element body) {
     Element element;
     Element form;
     Element select;
     Element table, tr, td;
 
 		/*
 		 * Standard search form
 		 */
     form = DomUtils.createElement(body, "FORM");
     form.setAttribute("name", FORMNAME);
     form.setAttribute("action", "http:/sakai-osid-repo-test/OSIDRepositoryTool");
     form.setAttribute("onSubmit",
     									"var s=!SRC_isNull(this.searchString.value); "
     							+		"return (s ? SRC_formSetup(this) : s);" );
 
     table = DomUtils.createElement(form, "TABLE");
     table.setAttribute("border", "0");
 
     tr = DomUtils.createElement(table, "TR");
     td = DomUtils.createElement(tr, "TD");
     td.setAttribute("valign", "middle");
 		/*
 		 * Hidden inputs (to persist values)
 		 */
     element = DomUtils.createElement(td, "INPUT");
     element.setAttribute("name", "searchEngine");
     element.setAttribute("type", "hidden");
 
     element = DomUtils.createElement(td, "INPUT");
     element.setAttribute("name", "cssFile");
     element.setAttribute("value", _css);
     element.setAttribute("type", "hidden");
 		/*
 		 * Visible input areas (search string, search source pulldown, submit)
 		 */
     DomUtils.addText(td, "Find");
     td = DomUtils.createElement(tr, "TD");
 
     element = DomUtils.createElement(td, "INPUT");
     element.setAttribute("name", "searchString");
     element.setAttribute("type", "text");
     element.setAttribute("size", "20");
     element.setAttribute("value", _searchQuery == null ? "" : _searchQuery);
     element.setAttribute("onClick", "SRC_statusDefaults(); return true;");
 
     td = DomUtils.createElement(tr, "TD");
     td.setAttribute("valign", "middle");
 
 		DomUtils.addEntity(td, "nbsp");
     DomUtils.addText(td, "in");
 
     td = DomUtils.createElement(tr, "TD");
 		doSearchSourcePulldown(td, _database);
 
     td = DomUtils.createElement(tr, "TD");
     element = DomUtils.createElement(td, "INPUT");
     element.setAttribute("type", "submit");
     element.setAttribute("value", "Search");
 		/*
 		 * Link to the "next" supported form type
 		 */
     td = DomUtils.createElement(tr, "TD");
  		doNextFormLink(td);
 
     body.setAttribute("onLoad",
     									"SRC_timedWindowVerification(); "
     								+	"document." + FORMNAME + ".searchString.focus();");
 
     body.setAttribute("onUnLoad", "SRC_statusDefaults();");
     return form;
   }
 
 	/**
 	 * Render a link to another search page
 	 * @param td Table cell we're to render to
 	 * @return Anchor element (null if nothing was rendered)
 	 */
 	public Element doNextFormLink(Element td) {
 		SearchSource	source;
 		String				label;
 		Element 			anchor;
 
 		if (!SearchSource.alternateFormsEnabled()) {
 			return null;
 		}
 
 		source = SearchSource.getSourceByName(SearchSource.getNextFormName(_database));
 
 		label = "Standard";
 		if (source.isAlternateForm()) {
 			label = source.getName();
 		}
 
     td.setAttribute("style", "text-align: right; vertical-align: bottom");
     td.setAttribute("nowrap", "nowrap");
 
 		DomUtils.addEntity(td, "nbsp");
 		DomUtils.addEntity(td, "nbsp");
 		DomUtils.addEntity(td, "nbsp");
 		DomUtils.addEntity(td, "nbsp");
 
 	  anchor = DomUtils.createElement(td, "A");
 	  anchor.setAttribute("title", "Display the " + label + " search form");
     anchor.setAttribute("style", "text-decoration: underline; font-size: x-small");
     anchor.setAttribute("href", "/OSIDRepository/OSIDRepositoryTool?"
     													+	"initialQuery=true&cssFile=" + _css
     													+ "&database=" + source.getName());
 
     DomUtils.addText(anchor, label + " Search>");
     return anchor;
   }
 
 	/**
 	 * Render a link to add a persistent URL into the edited text document, adding
 	 * default anchor and tooltip text
 	 *
 	 * @param parent HTML "parent" element (the link is attached here)
 	 * @param	url Persistent URL (link to save)
 	 * @param urlParams URL parameters
 	 * @param urlText Anchor text for the saved link
 	 * @param urlParamsForEscape URL parameters which require escape() encoding
 	 */
   public void doPersistentLink(Element parent,
 														 String url, String urlParams,
 														 String	urlText, String	urlParamsForEscape) {
 
 		doPersistentLink(parent, url, urlParams, urlText, urlParamsForEscape,
 														 "Add", "Add this link to your document");
 	}
 
 	/**
 	 * Render a link to add a persistent URL into the edited text document
 	 * @param parent HTML "parent" element (the link is attached here)
 	 * @param	url Persistent URL (link to save)
 	 * @param urlParams URL parameters
 	 * @param urlText Anchor text for the saved link
 	 * @param urlParamsForEscape URL parameters which require escape() encoding
 	 * @param anchorText Text for the "save this URL" link
 	 * @param anchorTitle Tooltip text for the "save URL" link
 	 */
 	public void doPersistentLink(Element parent,
 															 String url, String urlParams,
 															 String	urlText, String	urlParamsForEscape,
 															 String anchorText, String anchorTitle) {
 		Element anchor;
 		Element span;
 		String 	escapedParameters;
 
 		escapedParameters = urlParamsForEscape;
 		if (escapedParameters == null) {
 				escapedParameters = "";
 		}
 
     span = DomUtils.createElement(parent, "SPAN");
 	  span.setAttribute("summary", "Persistent URL");
 
 	  anchor = DomUtils.createElement(span, "A");
     anchor.setAttribute("style", "text-decoration: underline");
     anchor.setAttribute("href", "#");
     anchor.setAttribute("title", anchorTitle);
     anchor.setAttribute("onClick", "return SRC_addCitation(\""
       	                +            url
       	                + 					 normalizeJS(urlParams)
         	              +            "\", \""
           	            +            urlText
             	          +            "\", \""
               	        +            normalizeJS(escapedParameters)
                 	      +            "\");");
 
   	DomUtils.addText(anchor, anchorText);
 	}
 
 	/**
 	 * Render the "search source" pulldown list
 	 * @param parent Attach the SELECT list here
 	 * @param selected Search source selected by our user (null for none)
 	 */
 	public void doSearchSourcePulldown(Element parent, String selected) {
 		Element 	select;
 
 		/*
 		 * Set up the SELECT element
 		 */
 	  select = DomUtils.createElement(parent, "SELECT");
 	  select.setAttribute("name", "database");
 	  select.setAttribute("multiple","true");
 	  
 	  /*
 	   * This is all we can do if the search source list was never populated
 	   */
 	  SearchSource.populate();
 	  if (!SearchSource.isSourceListPopulated()) {
 		  return;
 	  }
 	  /*
 	   * Start the list with the currently selected & enabled option (if any)
 	   */
 	  if (!StringUtils.isNull(selected)) {
 		  
 		  for (Iterator i = SearchSource.getSearchListIterator(); i.hasNext(); ) {
 			  SearchSource ss = (SearchSource) i.next();
 			  
 			  if (ss.isAlternateForm()) {
 				  continue;
 			  }
 			  
 			  if (ss.getName().equalsIgnoreCase(selected)) {
 				  addOption(select, ss.getName(), ss.getName(), ss.isEnabled());
 			  }
 		  }
 	  }
 	  /*
 	   * Add in the other enabled choices
 	   */
 	  for (Iterator i = SearchSource.getSearchListIterator(); i.hasNext(); ) {
 		  SearchSource ss = (SearchSource) i.next();
 		  
 		  if (ss.isAlternateForm()) {
 			  continue;
 		  }
 		  
 		  if (!ss.getName().equalsIgnoreCase(selected)) {
 			  addOption(select, ss.getName(), ss.getName(), ss.isEnabled());
 		  }
 	  }
 	}
 
 	/**
 	 * Render citation table HTML (a small table containing the both the
 	 *			original anchor (for preview) and an "Add" link used to save
 	 *			the persistent URL
 	 * @param parent Parent HTML element
 	 * @param number Item number (one based, just a count)
 	 * @param item Search result: one matching item
 	 */
   public Element doCitationTable(Element parent, int number, MatchItem item) {
 		return doCitationTable(parent, number, item, null);
 	}
 
 	/**
 	 * Render the citation table HTML (a small table containing the both the
 	 *			original anchor (for preview) and an "Add" link used to save
 	 *			the persistent URL.  Default values are supplied for the "add"
 	 *			persistent link text and tooltip.
 	 * @param parent Parent HTML element
 	 * @param number Item number (one based, just a count)
 	 * @param item Search result: one matching item
 	 * @param alternateText Alternate text for the persisted URL - this
 	 *											overrides <code>MatchItem.getPersistentText() </code>
 	 */
   public Element doCitationTable(Element parent, int number,
   															 MatchItem item, String alternateText) {
 		return doCitationTable(parent, number, item, alternateText,
 													 "Add", "Add this link to your document");
 	}
 
 	/**
 	 * Render the citation table HTML (a small table containing the both the
 	 *			original anchor (for preview) and an "Add" link used to save
 	 *			the persistent URL
 	 * @param parent Parent HTML element
 	 * @param number Item number (one based, just a count)
 	 * @param item Search result: one matching item
 	 * @param alternateText Alternate text for the persisted URL - this
 	 *											overrides <code>MatchItem.getPersistentText()</code>
 	 * @param saveLinkText Text label for the "add this link" anchor
 	 * @param saveLinkTitle Tool tip for the "add this link" anchor
 	 */
   public Element doCitationTable(Element parent, int number,
   															 MatchItem item,
   															 String alternateText,
   															 String saveLinkText, String saveLinkTitle) {
 
     Element anchor, span, table, row, td, renderElement;
     String	anchorUrl, anchorText, citationText, persistentText;
     String  persistentUrl, persistentArgs, paramsForEscape;
 
 	anchorUrl		= item.getPreviewUrl();
     anchorText 		= item.getPreviewText();
     citationText	= normalizeText(item.getDescription());
 	persistentUrl	= item.getPersistentUrl();
 	persistentArgs 	= item.getPersistentUrlParameters();
 	paramsForEscape	= item.getPersistentUrlParametersForEncoding();
     persistentText	= normalizeText(item.getPersistentText());
 
     if (!StringUtils.isNull(alternateText)) {
     	persistentText = normalizeText(alternateText);
     }
 
     renderElement = DomUtils.createElement(parent, "TR");
     renderElement = DomUtils.createElement(renderElement, "TD");
 		/*
 		 * Table setup
 		 */
     table = DomUtils.createElement(renderElement, "TABLE");
     table.setAttribute("border", "0");
     table.setAttribute("cellpadding", "4");
     table.setAttribute("width", "100%");
     table.setAttribute("summary", "Citation Table");
 
     row = DomUtils.createElement(table, "TR");
     row.setAttribute("valign", "top");
 
     td = DomUtils.createElement(row, "TD");
     td.setAttribute("width", "5%");
 		/*
  		 * Preview Anchor, text, additional information (if any)
  		 */
     anchor = DomUtils.createElement(td, "A");
     anchor.setAttribute("name", String.valueOf(number));
     DomUtils.addText(anchor, number + ". ");
 
     td = DomUtils.createElement(row, "TD");
     td.setAttribute("width", "90%");
     span = DomUtils.createElement(td, "SPAN");
     span.setAttribute("summary", "Citation");
 
     anchor = DomUtils.createElement(span, "A");
     anchor.setAttribute("href", anchorUrl);
     DomUtils.addText(anchor, anchorText);
 
 		if (!StringUtils.isNull(citationText)) {
 	    DomUtils.createElement(span, "BR");
   	  DomUtils.addText(span, citationText);
 		}
 		/*
 		 * Persistent URL
 		 */
 		if (!StringUtils.isNull(persistentUrl)) {
 
 	    td = DomUtils.createElement(row, "TD");
 	    td.setAttribute("align", "center");
 	    td.setAttribute("valign", "middle");
 	    td.setAttribute("nowrap", "nowrap");
 
 	   	doPersistentLink(td, persistentUrl, persistentArgs, persistentText,
 	   									     paramsForEscape, saveLinkText, saveLinkTitle);
 		}
     return renderElement;
   }
 
 	/**
 	 * Render image table HTML (a small table containing the both the
 	 *			original anchor (for preview) and an "Add" link used to save
 	 *			the persistent URL.  Images are rendered three across; each is
 	 *			placed in a table using a default size and background color.
 	 * @param parent Parent HTML element
 	 * @param recordCount Image count
 	 * @param item Search results: one matching item
 	 */
   public Element doImageTable(Element 		parent,
   			 										  int 				recordCount,
 															MatchItem		item) {
 
 		return doImageTable(parent, recordCount, item, 210, 210);
 	}
 
 	/**
 	 * Render image table HTML (a small table containing the both the
 	 *			original anchor (for preview) and an "Add" link used to save
 	 *			the persistent URL.  Images are rendered three across; each is
 	 *			placed in a table of specified size with the default background color.
 	 * @param parent Parent HTML element
 	 * @param recordCount Image count
 	 * @param item Search results: one matching item
 	 * @param maxHeight Maximum image height
 	 * @param maxWidth Maximum image width (this may interact with the table
 	 * 						width specified in <code>doStandardTableSetup()</code>
 	 */
   public Element doImageTable(Element 		parent,
   			 										  int 				recordCount,
 															MatchItem		item,
                               int					maxHeight,
                               int					maxWidth) {
 
 		return doImageTable(parent, recordCount, item,
 												maxHeight, maxWidth, "#cccccc");
 	}
 
 	/**
 	 * Render image table HTML (a small table containing the both the
 	 *			original anchor (for preview) and an "Add" link used to save
 	 *			the persistent URL.  Images are rendered three across; each is
 	 *			placed in a table of specified size and background color.
 	 * @param parent Parent HTML element
 	 * @param recordCount Image count (caller should start at one and increment)
 	 * @param item Search results: one matching item
 	 * @param maxHeight Maximum image height
 	 * @param maxWidth Maximum image width (this may interact with the table
 	 * 						width specified in <code>doStandardTableSetup()</code>
 	 * @param backgroundColor Color for image table-cell
 	 * 						("<code>#ffffff"</code>) - specify <code>null</code> for none
 	 */
   public Element doImageTable(Element 		parent,
   			 										  int 				recordCount,
 															MatchItem		item,
                               int					maxHeight,
                               int					maxWidth,
                               String			backgroundColor) {
 
     String  persistentUrl, persistentArgs, paramsForEscape;
     String	anchorText, persistentText;
 
     Element anchor, br, image, span, table, row, td;
     Element renderElement;
 
 		/*
 		 * Establish text and URL references
 		 */
     anchorText 			= item.getPreviewText();
     persistentText	= normalizeText(item.getPersistentText());
 		persistentUrl		= item.getPersistentUrl();
 		persistentArgs 	= "";
     paramsForEscape	= item.getPersistentUrlParametersForEncoding();
 		/*
 		 * New row?
 		 */
     renderElement 	= parent;
     if (rowComplete(recordCount, 3)) {
       renderElement = DomUtils.createElement(parent, "TR");
     }
     /*
      * Use a cell to pull the image table to the top
      */
     renderElement = DomUtils.createElement(renderElement, "TD");
     renderElement.setAttribute("align", "center");
     renderElement.setAttribute("valign", "top");
 		/*
 		 * One table per image
 		 */
     table = DomUtils.createElement(renderElement, "TABLE");
     table.setAttribute("border", "0");
     table.setAttribute("cellpadding", "0");
     table.setAttribute("cellspacing", "0");
     table.setAttribute("width", String.valueOf(maxWidth));
     table.setAttribute("summary", "Image Table");
 		/*
 		 * Anchor (with image) row
 		 */
     row = DomUtils.createElement(table, "TR");
     row.setAttribute("align", "center");
     row.setAttribute("valign", "middle");
 
     td  = DomUtils.createElement(row, "TD");
     if (!StringUtils.isNull(backgroundColor)) {
     	td.setAttribute("bgcolor", backgroundColor);
   	}
     td.setAttribute("height", String.valueOf(maxHeight));
     td.setAttribute("width", String.valueOf(maxWidth));
 
     anchor = DomUtils.createElement(td, "A");
     anchor.setAttribute("name", String.valueOf(recordCount));
     anchor.setAttribute("href", item.getPreviewUrl());
 
     image = DomUtils.createElement(anchor, "IMG");
     image.setAttribute("src", item.getPreviewImage());
     image.setAttribute("border", "0");
     image.setAttribute("alt", ((item.getPreviewImage()==null) ? "No Preview" : anchorText) );
     image.setAttribute("title", "Click for enlarged image and details");
 		image.setAttribute("style", "margin-left: auto; margin-right: auto; display: block;");
 		/*
 		 * Anchor (with text) row
 		 */
     row = DomUtils.createElement(table, "TR");
     td  = DomUtils.createElement(row, "TD");
     td.setAttribute("style", "text-align: center");
 
 	  anchor = DomUtils.createElement(td, "A");
     anchor.setAttribute("href", item.getPreviewUrl());
     DomUtils.addText(anchor, anchorText);
 
     row = DomUtils.createElement(table, "TR");
     td  = DomUtils.createElement(row, "TD");
     td.setAttribute("style", "text-align: center");
 
 		doPersistentLink(td, persistentUrl, persistentArgs,
 												 persistentText, paramsForEscape,
 												 "Add", "Add image and details to your document");
 
     row = DomUtils.createElement(table, "TR");
     td  = DomUtils.createElement(row, "TD");
 		td.setAttribute("height", "12");
 
     return renderElement;
   }
 
 	/**
 	 * Set up the standard HTML result table
 	 * @param body The HTML body element
 	 */
 	public Element doStandardTableSetup(Element body) {
     Element table		= DomUtils.createElement(body, "TABLE");
 
     table.setAttribute("border", "0");
     table.setAttribute("cellpadding", "0");
     table.setAttribute("width", "650");
     table.setAttribute("summary", "Page Wrapper");
 
 		return table;
 	}
 
 	/**
 	 * Add a JavaScript SCRIPT Element
 	 * @param parent Add the SCRIPT here
 	 * @return The new SCRIPT element
 	 */
 	public Element doJavaScriptElement(Element parent) {
 		Element scriptElement;
 
     scriptElement = DomUtils.createElement(parent, "SCRIPT");
     scriptElement.setAttribute("type", "text/javascript");
 
 		return scriptElement;
 	}
 
 	/**
 	 * Add a populated JavaScript segment
 	 * @param parent Add the SCRIPT here
 	 * @param javaScript javaScript The JavaScript text
 	 * @return The new SCRIPT element
 	 */
 	public Element doJavaScript(Element parent, String javaScript) {
 		Element scriptElement = doJavaScriptElement(parent);
 
     DomUtils.addText(scriptElement, javaScript);
 		return scriptElement;
 	}
 
 	/**
 	 * Render the "No results for query" table block
 	 * Table The table Element we're to update
 	 * @param text Optional query text (null for none)
 	 */
   public void doNoResultForQuery(Element table, String text) {
   	doNoResultForQuery(table, "No results for query", text);
   }
 
 	/**
 	 * Render a "no results" table block
 	 * Table The table Element we're to update
 	 * @param label Core message (eg "Error encountered", "No match", etc)
 	 * @param text Additional text (null for none)
 	 */
   public void doNoResultForQuery(Element table, String label, String text) {
   	Element element;
 
     element = DomUtils.createElement(table, "TR");
     element = DomUtils.createElement(element, "TD");
     DomUtils.addText(element, " ");
 
     element = DomUtils.createElement(table, "TR");
     element = DomUtils.createElement(element, "TD");
     element.setAttribute("nowrap", "nowrap");
 
     DomUtils.addText(element, label);
 
     if (!StringUtils.isNull(text)) {
       DomUtils.addText(element, ": ");
 
       element = DomUtils.createElement(element, "I");
       DomUtils.addText(element, text);
     }
   }
 
   /*
    * Helpers
    */
 
 	/**
 	 * "Normalize" text for display
 	 * @return Trimmed original text, with &quot; replacing "
 	 */
 	public String normalizeText(String text) {
 		String result;
 
 		if (StringUtils.isNull(text)) {
 			return "";
 		}
 		result = StringUtils.replace(text, "\"", "&quot;");
 		return result.trim();
 	}
 
 	/**
 	 * "Normalize" text for use as a Javascript argument
 	 * (eg <code>return xxx('text');</code>)
 	 * @return Trimmed original text, with &quot; replacing '"', \' replacing ', %27
 	 */
 	public String normalizeJS(String text) {
 		String result;
 
 		if (StringUtils.isNull(text)) {
 			return "";
 		}
 
 		result = StringUtils.replace(text, "%27", "\\\\'");
 		result = StringUtils.replace(result, "'", "\\\\'");
 
 		return normalizeText(result);
 	}
 
 	/**
 	 * Is this image rendering table row complete? Used by
 	 * <code>doImageTable()</code>.
 	 * @param recordCount Records in row
 	 * @param imagesPerRow Images in each row
 	 * @return true if we've filled the row
 	 */
 	private boolean rowComplete(int recordCount, int imagesPerRow) {
     return ((recordCount + selectRecordCountOffset(getSearchResult().getSearchStart() % imagesPerRow) - 1) % imagesPerRow) == 0;
 	}
 
 	/**
 	 * Select a record offset.  This offset, when added to an arbitrary
 	 * record number, should allow <code>record % images-in-row</code> to
 	 * return zero when a new row is required.
 	 * @param modOfStartingValue Remainder of <starting-record / images-in-row</code>
 	 * @return offset added to each record number
 	 */
 	private int selectRecordCountOffset(int modOfStartingValue) {
 		switch (modOfStartingValue) {
 			case 0:
 				return 1;
 			case 1:
 				return 0;
 			default:
 				break;
 		}
 		return -(modOfStartingValue - 1);
 	}
 
  	/**
 	 * Locate select attribute of the first matching image
 	 * @param parent Parent element (look here for IMG tag)
 	 * @param name Attribute name (src, alt, etc)
 	 * @return Image name value (null if none)
 	 */
   public String getImageAttribute(Element parent, String name) {
     Element image;
     String	value;
 
  		if ((image = DomUtils.getElement(parent, "IMG")) == null) {
  		  return null;
  		}
 
     value = image.getAttribute(name);
     return StringUtils.isNull(value) ? null : value;
 	}
 
 	/**
 	 * Conditionally format an <code><option></code> Element
 	 * @param select The select element we're to update
 	 * @param visibleText Display text for this option
 	 * @param value The option value
 	 * @param add Add this option? (boolean expression)
 	 * @return The option Element
 	 */
   private Element addOption(Element select,
   													String visibleText, String value, boolean add) {
 		if (add) {
 			return addOption(select, visibleText, value);
 		}
 		return null;
 	}
 
 	/**
 	 * Format an <code><option></code> Element
 	 * @param select The select element we're to update
 	 * @param value The option value
 	 * @param visibleText Display text for this option
 	 * @return The option Element
 	 */
   public Element addOption(Element select, String visibleText, String value) {
     Element element;
 
     element = DomUtils.createElement(select, "OPTION");
     element.setAttribute("value", value);
     DomUtils.addText(element, visibleText);
 
     return element;
   }
 
   /**
    * Add an OPTION element, set selected attribute if desired
    * @param select SELECT element
    * @param name Option name
    * @param value Option value
    * @selectedValue Currently selected option value
 	 * @return The option Element
    */
   public Element addAndSelectOption(Element select,
                                  String name, String value, String selectedValue) {
     Element option;
 
     option = addOption(select, name, value);
 
     if (value.equals(selectedValue)){
       option.setAttribute("selected", "true");
     }
     return option;
   }
 }
