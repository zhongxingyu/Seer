 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Aug 19, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.concord.otrunk.view.document;
 
 import java.awt.KeyEventDispatcher;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Element;
 import javax.swing.text.html.HTML;
 import javax.swing.text.html.HTMLDocument;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeNotifying;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTUser;
 import org.concord.framework.otrunk.view.OTExternalAppService;
 import org.concord.framework.otrunk.view.OTFrame;
 import org.concord.framework.otrunk.view.OTJComponentView;
 import org.concord.framework.otrunk.view.OTView;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.framework.otrunk.view.OTViewContainerChangeEvent;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTViewEntryAware;
 import org.concord.framework.otrunk.view.OTXHTMLView;
 import org.concord.otrunk.OTrunkUtil;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * @author scott
  * 
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Generation - Code and Comments
  */
 public class OTDocumentView extends AbstractOTDocumentView implements
 		ChangeListener, HyperlinkListener, OTXHTMLView, OTViewEntryAware {
 	public static boolean addedCustomLayout = false;
 
 	protected JTabbedPane tabbedPane = null;
 
 	protected JComponent previewComponent = null;
 
 	protected JEditorPane editorPane = null;
 
 	DocumentBuilderFactory xmlDocumentFactory = null;
 
 	DocumentBuilder xmlDocumentBuilder = null;
 
 	protected JTextArea parsedTextArea = null;
 
 	protected OTObject otObject;
 	
 	protected DocumentConfig documentConfig;
 	
 	protected OTViewEntry viewEntry;
 
 	private KeyEventDispatcher sourceViewDispatcher;
 	
 	private boolean updateViewOnStateChange = true;
 
 	public final static String XHTML_PREFIX_START =
 	// "<?xml version='1.0' encoding='UTF-8'?>\n" +
 	"<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>\n"
 			+ "\n" + "<head>\n" + "<title>default</title>\n"
 			+ "<style type=\"text/css\">\n";
 
 	public final static String XHTML_PREFIX_END = "</style>" + "</head>\n"
 			+ "<body>\n";
 
 	public final static String XHTML_SUFFIX = "</body>\n" + "</html>";
 
 	protected void setup(OTObject doc) {
 		// Don't call super.setup() to avoid listening to the ot object
 		// inneccesarily
 		pfObject = (OTDocument) doc;
 	}
 	
 	protected Action viewSourceAction = new AbstractAction("View Source.."){
 
 		public void actionPerformed(ActionEvent e)
         {
 			tabbedPane = new JTabbedPane();
 
 			textArea = new JTextArea(textAreaModel);
 			textArea.setEditable(false);
 			JScrollPane scrollPane = new JScrollPane(textArea);
 			tabbedPane.add("Source", scrollPane);
 
 			parsedTextArea.setEditable(false);
 			scrollPane = new JScrollPane(parsedTextArea);
 			tabbedPane.add("Parsed", scrollPane);
 			
 			JFrame frame = new JFrame("Source");
 			frame.getContentPane().add(tabbedPane);
 			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			
 			frame.setSize(600, 600);
 			frame.setVisible(true);
         }
 		
 	};
 	
 	private MouseListener mouseListener = new MouseAdapter(){
 		private void showPopup(MouseEvent e)
 		{
 	        JPopupMenu menu = new JPopupMenu(); 
 	        menu.add(viewSourceAction);
 	 
 	        Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), previewComponent);
 	        menu.show(previewComponent, pt.x, pt.y);				
 		}
 		
 		public void mousePressed(MouseEvent e)
 		{
 			if(!e.isPopupTrigger()){
 				return;
 			}
 			showPopup(e);
 		}
 		
 		public void mouseReleased(MouseEvent e)
 		{
 			if(!e.isPopupTrigger()){
 				return;
 			}
 			showPopup(e);
 		}
 	};
 	
 	public JComponent getComponent(OTObject otObject) {
 		this.otObject = otObject;
 		setup(otObject);
 		initTextAreaModel();
 		
 		if (otObject instanceof OTChangeNotifying){
 			((OTChangeNotifying)otObject).addOTChangeListener(this);
 		}
 		
 		if (tabbedPane != null) {
 			tabbedPane.removeChangeListener(this);
 		}
 
 		// need to use the PfCDEditorKit
 		updateFormatedView();
 
 		setReloadOnViewEntryChange(true);
 		
 		if (documentConfig != null){
 			OTViewContainer thisViewContainer = getViewContainer();
 			if (thisViewContainer != null){
 				getViewContainer().setUpdateable(documentConfig.getViewContainerIsUpdateable());
 			}
 		}
 
 		// JScrollPane renderedScrollPane = new JScrollPane(previewComponent);
 		// renderedScrollPane.getViewport().setViewPosition(new Point(0,0));
 		
 		previewComponent.addMouseListener(mouseListener);
 				
 		if (System.getProperty("otrunk.view.debug", "").equals("true")) {
 			tabbedPane = new JTabbedPane();
 
 			// need to add a listener so we can update the view pane
 			tabbedPane.add("View", previewComponent);
 
 			textArea = new JTextArea(textAreaModel);
 			JScrollPane scrollPane = new JScrollPane(textArea);
 			tabbedPane.add("Edit", scrollPane);
 
 			parsedTextArea.setEnabled(false);
 			scrollPane = new JScrollPane(parsedTextArea);
 			tabbedPane.add("Parsed", scrollPane);
 
 			tabbedPane.addChangeListener(this);
 
 			return tabbedPane;
 		} else {
 			return previewComponent;
 		}
 	}
 
 	public String updateFormatedView() {
 		if (pfObject == null)
 			return null;
 
 		// System.out.println(this+" updateFormatedView");
 
 		String markupLanguage = pfObject.getMarkupLanguage();
 		if (markupLanguage == null) {
 			markupLanguage = System.getProperty("org.concord.portfolio.markup",
 					null);
 		}
 
 		String bodyText = null;
 		try {
 			// The textAreaModel is out source for the text.  It was setup by
 			// the initTextAreaModel method.  It also listens to the pfObject
 			// so if it changes, the textAreaModel will get updated too.
 	        bodyText = textAreaModel.getText(0, textAreaModel.getLength());
         } catch (BadLocationException e) {
         	System.err.println("OTDocumentView: " + e.getMessage());
         	System.err.println("  continuing by requesting documentText");
         	bodyText = pfObject.getDocumentText();
         }
 		bodyText = substituteIncludables(bodyText);
 
 		// default to html viewer for now
 		// FIXME the handling of the plain markup is to test the new view entry
 		// code
 		// it isn't quite valid because plain text might have html chars in it
 		// so it will get parsed incorrectly.
 		if (markupLanguage == null
 				|| markupLanguage.equals(OTDocument.MARKUP_PFHTML)
 				|| markupLanguage.equals(OTDocument.MARKUP_PLAIN)) {
 			if (editorPane == null) {
 				editorPane = new JEditorPane();
 				OTHTMLFactory kitViewFactory = new OTHTMLFactory(this);
 				OTDocumentEditorKit editorKit = new OTDocumentEditorKit(
 						kitViewFactory);
 				editorPane.setEditorKit(editorKit);
 				editorPane.setEditable(false);
 				editorPane.addHyperlinkListener(this);
 			}
 			bodyText = htmlizeText(bodyText);
 			bodyText = addHiddenTextChanges(bodyText);
 
 			if (documentConfig != null) {
 				
 				String css = documentConfig.getCssText();
 				
 				String XHTML_PREFIX = XHTML_PREFIX_START + css
 						+ XHTML_PREFIX_END;
 				bodyText = XHTML_PREFIX + bodyText + XHTML_SUFFIX;
 			}
 			// when this text is set it will recreate all the
 			// OTDocumentObjectViews, so we need to clear and
 			// close all the old panels first
 			removeAllSubViews();
 
 			editorPane.setText(bodyText);
 			HTMLDocument doc = (HTMLDocument) editorPane.getDocument();
 			doc.setBase(otObject.getOTObjectService().getCodebase(otObject));
 
 			previewComponent = editorPane;
 
 			// we used to set thie caret pos so the view would
 			// scroll to the top, but now we disable scrolling
 			// during the load, and that seems to work better
 			// there is no flicker that way.
 			// editorPane.setCaretPosition(0);
 		} else {
 			System.err.println("xhtml markup not supported");
 		}
 
 		if (parsedTextArea == null) {
 			parsedTextArea = new JTextArea();
 		}
 		parsedTextArea.setText(bodyText);
 		
 		return bodyText;
 	}
 
 	/**
 	 * This method gets the object with idStr and tries to get the view entry
 	 * with the viewIdStr. If the viewIdStr is not null then it gets the view
 	 * for that view entry and the current view mode, and determines if it is a
 	 * {@link OTXHTMLView}. If it is then it gets the html text of that view.
 	 * If the viewIdStr is null then asks the viewFactory for a view of this
 	 * object which implements the OTXHTMLView interface. If such a view exists
 	 * then that is used to get the text.
 	 * 
 	 * The text returned is intended for use by the Matcher.appendReplacement
 	 * method. So $0 means to just leave the text as is. Because of this any
 	 * text needs to be escaped, specifically $ and \ needs to be escaped.
 	 * {@link OTrunkUtil#escapeReplacement(String)}
 	 * 
 	 * So far this is only used by the substitueIncludables method
 	 * 
 	 * @see OTrunkUtil#escapeReplacement(String)
 	 * @see #substituteIncludables(String)
 	 * 
 	 */
 	public String getIncludableReplacement(String idStr, String viewIdStr,
 			String modeStr, String userStr) {
 		// lookup the object at this id
 		OTObject referencedObject = getReferencedObject(idStr);
 		if (referencedObject == null) {
 			return "$0";
 		}
 		if (userStr != null) {
 			referencedObject = getRuntimeObject(referencedObject, userStr);
 		}
 
 		OTViewEntry viewEntry = null;
 		if (viewIdStr != null) {
 			OTObject viewEntryTmp = getReferencedObject(viewIdStr);
 			if (viewEntryTmp instanceof OTViewEntry) {
 				viewEntry = (OTViewEntry) viewEntryTmp;
 			} else {
 				System.err.println("viewid reference to a non viewEntry object");
 				System.err.println("  doc: " + pfObject.getGlobalId());
 				System.err.println("  refid: " + idStr);
 				System.err.println("  viewid: " + viewIdStr);
 			}
 
 		}
 
 		OTView view = null;
 
 		String viewMode = getViewMode();
 		if (modeStr != null) {
 			if (modeStr.length() == 0) {
 				viewMode = null;
 			} else {
 				viewMode = modeStr;
 			}
 		}
 
 		if (viewEntry != null) {
 			view = getViewFactory().getView(referencedObject, viewEntry,
 					viewMode);
 		} else {
 			view = getViewFactory().getView(referencedObject,
 					OTXHTMLView.class, viewMode);
 			
 			// if there isnt' a xhtml view and there is a view mode, then see if
 			// that viewMode of the jcomponent view is an xhtmlview.
 			// this logic is really contorted. The mixing of viewmodes and
 			// renderings needs to be well defined.
 			// TODO scytacki Feb 12, 2008 - this logic has been moved into the getView method of the viewfactory 
 			//   so if nothing goes wrong we should remove it from here.
 			if (view == null && getViewMode() != null) {
 				view = getViewFactory().getView(referencedObject,
 						OTJComponentView.class, viewMode);
 			}
 		}
 		
 		boolean alwaysEmbedXHTMLView = viewContext.getProperty(ALWAYS_EMBED_XHTML_VIEW) != null;
 		
 		if (view instanceof OTXHTMLView && 
 				(alwaysEmbedXHTMLView || ((OTXHTMLView)view).getEmbedXHTMLView())) {
 			OTXHTMLView xhtmlView = (OTXHTMLView) view;
 			try {
 				String replacement = xhtmlView.getXHTMLText(referencedObject);
 				if (replacement == null) {
 					// this is an empty embedded object
 					System.err.println("empty embedd obj: " + idStr);
 					return "";
 				}
 				return OTrunkUtil.escapeReplacement(replacement);
 			} catch (Exception e) {
 				System.err
 						.println("Failed to generate xhtml version of embedded object");
 				e.printStackTrace();
 			}
 		}
 		
 		// $0 means to just leave the text as is, don't replace anything
 		return "$0";
 	}
 
 	public String substituteIncludables(String inText) {
 		if (inText == null) {
 			return null;
 		}
 
 		Pattern p = Pattern.compile("<object[^>]*refid=\"([^\"]*)\"[^>]*>");
 		Pattern pViewId = Pattern.compile("viewid=\"([^\"]*)\"");
 		Pattern pMode = Pattern.compile("mode=\"([^\"]*)\"");
 		Pattern pUser = Pattern.compile("user=\"([^\"]*)\"");
 		Matcher m = p.matcher(inText);
 		StringBuffer parsed = new StringBuffer();
 		while (m.find()) {
 			String id = m.group(1);
 
 			String element = m.group(0);
 			Matcher mViewId = pViewId.matcher(element);
 			String viewIdStr = null;
 			if (mViewId.find()) {
 				viewIdStr = mViewId.group(1);
 			}
 
 			Matcher mMode = pMode.matcher(element);
 			String modeStr = null;
 			if (mMode.find()) {
 				modeStr = mMode.group(1);
 			}
 
 			Matcher mUser = pUser.matcher(element);
 			String userStr = null;
 			if (mUser.find()) {
 				userStr = mUser.group(1);
 			}
 			
 			String replacement = getIncludableReplacement(id, viewIdStr,
 					modeStr, userStr);
 			try {
 				m.appendReplacement(parsed, replacement);
 			} catch (IllegalArgumentException e) {
 				System.err.println("bad replacement: " + replacement);
 				e.printStackTrace();
 			}
 			
 		}
 		m.appendTail(parsed);
 		return parsed.toString();
 	}
 
 	public String htmlizeText(String inText) {
 		if (inText == null) {
 			return null;
 		}
 
 		inText = inText.replaceAll("<p[ ]*/>", "<p></p>");
 		inText = inText.replaceAll("<div([^>]*)/>", "<div$1></div>");
 		inText = inText.replaceAll("<([^>]*)/>", "<$1>");
 		
		if (otObject instanceof OTCompoundDoc && ((OTCompoundDoc)otObject).getDivClasses() != null){
			inText = addDivWrappers(inText, ((OTCompoundDoc)otObject).getDivClasses());
		}
		
 		return inText;
 
 		/*
 		 * Pattern p = Pattern.compile("<([^>]*)/>"); Matcher m =
 		 * p.matcher(inText); StringBuffer parsed = new StringBuffer();
 		 * while(m.find()) { String tagBody = m.group(1);
 		 *  // We need 6 backslashes because // first the java compiler strips
 		 * off half of them so the sting // becomes: \\\$ // then the replacer
 		 * uses the backslash as a quote, and the $ // character is used to
 		 * reference groups of characters, so it // must be escaped. So the 1st
 		 * two are turned into one, and the // 3rd one escapes the $. So the end
 		 * result is: // \$ // We need this \$ because the replacement below is
 		 * going to // parse the $ otherwise tagBody = tagBody.replaceAll("\\$",
 		 * "\\\\\\$"); try { m.appendReplacement(parsed, "<$1>" + tagBody +
 		 * ">"); } catch (IllegalArgumentException e) { System.err.println("bad
 		 * tag: " + tagBody); e.printStackTrace(); } } m.appendTail(parsed);
 		 * return parsed.toString();
 		 */
 	}
 	
 	/**
 	 * This method is like htmlizeText, but it applies the changes after the
 	 * editorPane has been created, so that any changes will not appear in
 	 * the author's src view
 	 * 
 	 * @param inText
 	 * @return
 	 */
 	private String addHiddenTextChanges(String inText){
 		if (otObject instanceof OTCompoundDoc && ((OTCompoundDoc)otObject).getDivClasses() != null){
 			inText = addDivWrappers(inText, ((OTCompoundDoc)otObject).getDivClasses());
 		}
 		
 		return inText;
 	}
 	
 	/*
 	 * If there are multiple div classes, they are wrapped backwards, so that
 	 * the last class is the inner-most wrapper. 
 	 */
 	private String addDivWrappers(String inText, String divClasses){
 		String[] classes = divClasses.split(" ");
 		for (int i = classes.length-1; i >= 0; i--) {
 			inText = "<div class=\""+classes[i]+"\">"+inText+"</div>";
         }
 		return inText;
 	}
 	
 	// FIXME: Does this do anything anymore? Nothing seems to call it. -SF
 	public Document parseString(String text, String systemId) {
 		try {
 			if (xmlDocumentFactory == null) {
 				xmlDocumentFactory = DocumentBuilderFactory.newInstance();
 				xmlDocumentFactory.setValidating(true);
 				xmlDocumentBuilder = xmlDocumentFactory.newDocumentBuilder();
 
 				// TODO Fix this
 				xmlDocumentBuilder.setErrorHandler(new DefaultHandler());
 			}
 			
 			StringReader stringReader = new StringReader(text);
 			InputSource inputSource = new InputSource(stringReader);
 			inputSource.setSystemId(systemId);
 			return xmlDocumentBuilder.parse(inputSource);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
 	 */
 	public void stateChanged(ChangeEvent event) {
 		// System.out.println(this+" -- TABS stateChanged");
 
 		updateFormatedView();
 	}
 
 	public void hyperlinkUpdate(HyperlinkEvent e) {
 
 		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 			try {
 				String linkTarget = e.getDescription();
 				if (linkTarget.startsWith("http")
 						|| linkTarget.startsWith("file")) {
 					OTExternalAppService extAppService = 
 						(OTExternalAppService)getViewService(OTExternalAppService.class);
 					if(extAppService.showDocument(new URL(linkTarget))){
 						return;
 					}
 				}
 
 				OTObject linkObj = getReferencedObject(linkTarget);
 				if (linkObj == null) {
 					// It wasn't an external link, and wasn't a reference to an OTObject
 					// lets assume it's a relative link to an external resource
 					// System.err.println("Invalid link: " + e.getDescription());
 					// System.err.print("Trying with codebase...");
 					URL codebase = otObject.getOTObjectService().getCodebase(otObject);
 					if (codebase == null) {
 						System.err.println("Invalid link: " + e.getDescription());
 						return;
 					}
 					
 					// construct a new URL using the document's codebase as a base
 					URL newTarget = new URL(codebase, linkTarget);
 					
 					if (newTarget != null) {
 						// try to launch the url in a browser window
     					OTExternalAppService extAppService = 
     						(OTExternalAppService)getViewService(OTExternalAppService.class);
     					if (extAppService.showDocument(newTarget)) {
     						// System.err.println(" success!");
     					} else {
     						System.err.println("Invalid link: " + e.getDescription());
     					}
     					// System.err.println("New url was: " + newTarget.toExternalForm());
 					}
 					return;
 				}
 
 				Element aElement = e.getSourceElement();
 				AttributeSet attribs = aElement.getAttributes();
 
 				// this is a hack because i don't really know what is going on
 				// here
 				AttributeSet tagAttribs = (AttributeSet) attribs
 						.getAttribute(HTML.Tag.A);
 				String target = null;
 				String viewEntryId = null;
 				String modeStr = null;
 				String userStr = null;
 				if(tagAttribs != null){
 					target = (String) tagAttribs
 					.getAttribute(HTML.Attribute.TARGET);
 					viewEntryId = (String) tagAttribs.getAttribute("viewid");
 					modeStr = (String) tagAttribs.getAttribute("mode");
 					userStr = (String) tagAttribs.getAttribute("user");
 				}
 				
 				OTViewEntry viewEntry = null;
 				if (viewEntryId != null) {
 					viewEntry = (OTViewEntry) getReferencedObject(viewEntryId);
 					if (viewEntry == null) {
 						System.err.println("Invalid link viewid attrib: "
 								+ viewEntryId);
 						return;
 					}
 				}
 
 				OTFrame targetFrame = null;
 				if (target != null){
 					// they want to use a frame
 					targetFrame = (OTFrame) getReferencedObject(target);					
 					if (targetFrame == null) {
 						System.err.println("Invalid link target attrib: "
 								+ target);
 						return;
 					}
 				}
 				
 				// mode used for switching viewmodes
 				if (modeStr != null && modeStr.length() == 0) {
 					modeStr = null;
 				} else if (modeStr == null) {
 					modeStr = getViewMode();
 				}
 									
 				if (userStr != null && userStr.length() != 0) {
 					linkObj = getRuntimeObject(linkObj, userStr);
 				}
 				
 				if (target == null) {
 					// FIXME deal with the mode
 					getViewContainer().setCurrentObject(linkObj, viewEntry);
 				} else {					
 					putObjectInFrame(linkObj, viewEntry,
 							targetFrame, modeStr);
 				}
 
 			} catch (Throwable t) {
 				t.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * This method is extracted from hyperlinkUpdate so subclasses
 	 * can override it.
 	 * 
 	 * @param linkObj
 	 * @param viewEntry
 	 * @param targetFrame
 	 * @param modeStr
 	 */
 	public void putObjectInFrame(OTObject linkObj, OTViewEntry viewEntry,
 		OTFrame targetFrame, String modeStr)
 	{
 		getFrameManager().putObjectInFrame(linkObj, viewEntry,
 				targetFrame, modeStr);		
 	}
 	
 	public String getXHTMLText(OTObject otObject) {
 		if (otObject == null) {
 			throw new IllegalArgumentException("otObject can't be null");
 		}
 		pfObject = (OTDocument) otObject;
 		this.otObject = otObject;
 		
 	//	return updateFormatedView().replace(viewEntry.getCss(), "");
 		
 		String bodyText = pfObject.getDocumentText();
 		bodyText = substituteIncludables(bodyText);
 		return bodyText;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.framework.otrunk.view.OTViewConfigAware#setViewConfig(org.concord.framework.otrunk.OTObject)
 	 */
 	public void setViewEntry(OTViewEntry viewEntry) {
 		super.setViewEntry(viewEntry);
 		this.viewEntry = viewEntry;
 		if (viewEntry instanceof DocumentConfig) {
 			documentConfig = (DocumentConfig) viewEntry;
 			setViewMode(documentConfig.getMode());
 		}
 	}
 
 	/** 
 	 * Retrieves the CSS style text used for the document view.
 	 * @return String containing (raw) CSS definitions, or a blank string.
 	 */
 	/**
 	protected String getCssText() {
 		String css = "";
 		
 		if (documentConfig.getCss() != null && documentConfig.getCss().length() > 0){
 			css += documentConfig.getCss();
 		}
 		
 		if (documentConfig.getCssBlocks() != null && documentConfig.getCssBlocks().getVector().size() > 0){
 			Vector cssBlocks =  documentConfig.getCssBlocks().getVector();
 			
 			for (int i = 0; i < cssBlocks.size(); i++) {
                 OTCssText cssText = (OTCssText) cssBlocks.get(i);
                 
                 // retrieve CSS definitions (originally) from the otml file
                 String text = cssText.getCssText();
                 
                 // if no cssText, then get src which is a URL for the css file
                 if (text == null) {
                 	text = "";
                 	URL url = cssText.getSrc();
                 	
                 	if (url != null) {
                 		try {
                 			URLConnection urlConnection = url.openConnection();
                 			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                 			String line = reader.readLine();
                 			while (line != null) {
                 				text += line;
                 				line = reader.readLine();
                 			}
                 		}
                 		catch (IOException e){
                 			text = "";
                 			e.printStackTrace();
                 		}
                 	}
                 }
                 css += " " + text;
             }
 		}
 		return css;
 	}
 	*/
 	
 	// If one of the child objects is replaced by another, we swap the old and new
 	// object in the documentRefs, and then change the object references in the
 	// html to refer to the new object.
 	public void currentObjectChanged(OTViewContainerChangeEvent evt)
     {
 		if (evt.getEventType() == OTViewContainerChangeEvent.REPLACE_CURRENT_OBJECT_EVT){
 			OTObject oldObject = evt.getPreviousObject();
 			OTObject newObject = evt.getValue();
 			
 			if (otObject instanceof OTCompoundDoc){
 				OTCompoundDoc compoundDoc = (OTCompoundDoc) otObject;
 				compoundDoc.getDocumentRefsAsObjectList().remove(oldObject);
 				compoundDoc.getDocumentRefsAsObjectList().add(newObject);
 				
 				String newBodyText = compoundDoc.getBodyText().replaceAll(
 						otObject.getOTObjectService().getExternalID(oldObject), 
 						otObject.getOTObjectService().getExternalID(newObject));
 				compoundDoc.setBodyText(newBodyText);
 				updateFormatedView();
 				
 			}
 		}
     }
 
 	public boolean getEmbedXHTMLView()
     {
 	    return true;
     }
 
 	public OTObject getRuntimeObject(OTObject object, String userStr) {
 		try {
 			OTObjectService objectService = otObject.getOTObjectService();
 			OTID userId = objectService.getOTID(userStr);
 			OTUser user = (OTUser) objectService.getOTObject(userId);
 			return getOTrunk().getUserRuntimeObject(object, user);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public void stateChanged(OTChangeEvent e)
     {
 		super.stateChanged(e);
 		if (updateViewOnStateChange()){
 			updateFormatedView();
 		}
     }
 	
 	public boolean updateViewOnStateChange(){
 		return updateViewOnStateChange;
 	}
 	
 	public void viewClosed()
 	{
 		super.viewClosed();	
 		if(editorPane != null){
 			editorPane.removeHyperlinkListener(this);
 		}
 
 		if(previewComponent != null){
 			previewComponent.removeMouseListener(mouseListener);
 			mouseListener = null;
 		}
 	}
 }
