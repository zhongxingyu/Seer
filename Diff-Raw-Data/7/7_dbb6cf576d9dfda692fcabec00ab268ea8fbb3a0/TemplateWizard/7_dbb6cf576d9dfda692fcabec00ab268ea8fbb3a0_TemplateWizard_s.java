 package nl.nikhef.jgridstart.gui.util;
 
 import java.awt.Color;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ComponentEvent;
 import java.awt.print.PrinterException;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;
 import nl.nikhef.xhtmlrenderer.swing.TemplateDocument;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xhtmlrenderer.extend.NamespaceHandler;
 import org.xhtmlrenderer.layout.SharedContext;
 import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
 import org.xhtmlrenderer.swing.FSMouseListener;
 import org.xhtmlrenderer.swing.LinkListener;
 import org.xml.sax.SAXException;
 
 /** HTML-based wizard dialog.
  * <p>
  * This is an {@link ITemplatePanel} with next/previous buttons by which
  * the user can walk through a list of pages. The pages that make up the
  * wizard are defined in the member {@link #pages}.
  * <p>
  * When you want to do something on a page change, use {@link #setHandler} with
  * a {@link PageListener} interface as argument. One can use {@link #setStep} and
  * {@link #setStepRelative} to change the current page.
  * <p>
  * To give the user an overview of the available steps in the wizard, there is
  * a variable {@code wizard.contents.html} which can be inserted in each step
  * using the default {@link TemplateDocument template} substitutions. It can be
  * fixed to the viewport with css like <code>position: fixed</code>.
  * {@link #setStepContents} generates this variable, which can be customized
  * by derived classes.
  * <p>
  * The default implementation of {@linkplain #setStepContents} retrieves the
  * HTML document title for each page to construct the overview of the available
  * steps.
  */
 public class TemplateWizard extends JDialog implements ITemplatePanel {
 
     /** wizard page templates for each step */
     public ArrayList<URL> pages = new ArrayList<URL>();
     /** list of loaded documents */
     private ArrayList<TemplateDocument> docs = new ArrayList<TemplateDocument>();
     /** currently active step (base 0), -1 for not initialized */
     protected int step = -1;
     /** actual dialog content with HTML and buttons */
     protected TemplateButtonPanel pane = null;
     protected JPanel btnRight = null;
     protected JPanel btnLeft = null;
     /** "Next" action */
     protected Action nextAction = null;
     /** "Previous" action */
     protected Action prevAction = null;
     /** "Cancel"/"Close" action */
     protected Action cancelAction = null;
     /** Listener for page changes */
     protected PageListener handler = null;
 
     // constructors
     public TemplateWizard()               { super(); initialize(); }
     public TemplateWizard(Dialog owner)   { super(owner); initialize(); }
     public TemplateWizard(Frame owner)    { super(owner); initialize(); }
     public TemplateWizard(Properties p)   { this(); setData(p); }
     public TemplateWizard(Dialog owner, Properties p) { this(owner); setData(p); }
     public TemplateWizard(Frame owner, Properties p)  { this(owner); setData(p);  }
     
     /** return the TemplateDocument by index.
      * <p>
     * The document is loaded on demand.
      *
      * @return loaded document on success, {@code null} on failure
      */
     protected TemplateDocument getDocument(int i)  {
 	// load when needed
	if (docs.size() < i || !docs.get(i).getDocumentURI().equals(pages.get(i).toExternalForm()) ) {
 	    try {
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		Document src = factory.newDocumentBuilder().parse(pages.get(i).toExternalForm());
 		TemplateDocument doc = new TemplateDocument(src, data());
 		doc.setDocumentURI(pages.get(i).toExternalForm());
 		docs.set(i, doc);
 		return doc;
 	    } catch (SAXException e) {
 	    } catch (IOException e) {
 	    } catch (ParserConfigurationException e) {
 	    }
 	    return null;
 	}
 	return docs.get(i);
     }
 
     /** set the currently displayed page */
     // TODO describe behaviour, especially what happens in last step
     //      (based on handler being present or not)
     public void setStep(int s) {
 	// handle final "Close" step which just quits the dialog
 	if (s == pages.size()) {
 	    if (handler!=null)
 		handler.pageChanged(this, s);
 	    else
 		dispose();
 	    return;
 	}
 	step = s;
 	// Update the navigation bar
 	setStepContents(step);
 	// set new contents and get title from that as well
 	pane.setDocument(getDocument(s));
 	setTitle(pane.getDocumentTitle());
 	// no "Previous" at start; no "Next" beyond the final "Close"
 	if (s == pages.size()-1)
 	    cancelAction.putValue(Action.NAME, "Close");
 	nextAction.setEnabled(step < (pages.size()-1) );
 	prevAction.setEnabled(step > 0);
 	if (handler!=null) handler.pageChanged(this, s);
 	
 	// pack here to give child classes a chance to setPreferredSize()
 	// in their constructors or in setStep(). This is only called if
 	// the window is not yet visible because pack()ing resets the
 	// dialog size to its preferred size, which is unwanted when the
 	// user resized the window.
 	if (!isVisible()) pack();
     }
     /** go to another page by relative distance */
     public void setStepRelative(int delta) {
 	if (step < 0)
 	    step = 0;
 	setStep(step + delta);
     }
     
     /** Update the contents list for a step.
      * <p>
      * This generates the property {@code wizard.contents.html} which can be
      * referenced in the template to include a table of contents. Derived
      * classes may override this for a customized view.
      * <p>
      * Note that property {@code wizard.contents.html.volatile} is set
      * to {@code true} to avoid it being saved to disk for persistent
      * property stores (like jGridstart's CertificatePair).
      * 
      * @param step step for which the contents is displayed; the default
      *             implementation marks the currently displayed step. 
      */
     public void setStepContents(int step) {
 	String v = "<ul class='wizard-contents'>\n";
 
 	for (int i=0; i<pages.size(); i++) {
 	    String li;
 	    if (step == i) {
 		li = "<li class='wizard-current'>";
 	    } else {
 		li = "<li>";
 	    }
 	    v += li+getSharedContext().getNamespaceHandler().getDocumentTitle(getDocument(i))+"</li>\n";
 	}
 	
 	v += "</ul>\n";
 	data().setProperty("wizard.contents.html", v);
 	data().setProperty("wizard.contents.html.volatile", "true");
     }
 
     /** Shows or hides the dialog.
      * <p>
      * This also sets the current step to the first one if none is selected
      * as of yet (only when there are pages defined).
      */
     public void setVisible(boolean visible) {
 	// need to setup some stuff if no step selected
 	if (visible && step < 0 && pages.size() > 0)
 	    setStep(0);
 	super.setVisible(visible);
     }
     
     /** Initialize and build the dialog
      * 
      * The background colour is set to the default control background colour as
      * specified by UIManager. It is still possible to override it in CSS/HTML.
      */
     protected void initialize() {
 	setModal(true);
 	getContentPane().removeAll();
 	pane = new TemplateButtonPanel();
 	getContentPane().add(pane);
 	pane.setBackground(UIManager.getColor("control"));
 
 	// create two button areas: one for prev/next to the right, one for
 	// extra buttons at the left.
 	btnLeft = new JPanel();
 	btnLeft.setLayout(new BoxLayout(btnLeft, BoxLayout.X_AXIS));
 	btnRight = new JPanel();
 	btnRight.setLayout(new BoxLayout(btnRight, BoxLayout.X_AXIS));
 	JPanel bpanel = pane.getButtonPane();
 	bpanel.removeAll();
 	bpanel.add(btnLeft, null);
 	bpanel.add(Box.createHorizontalGlue());
 	bpanel.add(btnRight, null);	
 	
 	// "Previous" button
 	prevAction = new AbstractAction("Previous") {
 	    public void actionPerformed(ActionEvent e) {
 		setStepRelative(-1);
 	    }
 	};
 	prevAction.putValue(Action.MNEMONIC_KEY, new Integer('P'));
 	
 	JButton prevButton = new JButton(prevAction);
 	prevButton.setName("wizard_previous");
 	pane.addButton(btnRight, prevButton, false, false);
 	// "Next" button; name and mnemonic set in setStep()
 	nextAction = new AbstractAction("Next") {
 	    public void actionPerformed(ActionEvent e) {
 		setStepRelative(1);
 	    }
 	};
 	nextAction.putValue(Action.MNEMONIC_KEY, new Integer('N'));
 	JButton nextButton = new JButton(nextAction);
 	nextButton.setName("wizard_next");
 	pane.addButton(btnRight, nextButton, true, false);
 	// close window on escape
 	btnRight.add(Box.createRigidArea(new Dimension(pane.btnBorderWidth*8, 0)));
 	cancelAction = new AbstractAction("Cancel") {
 	    public void actionPerformed(ActionEvent e) {
 		TemplateWizard.this.dispose();
 	    }
 	};
 	cancelAction.putValue(Action.MNEMONIC_KEY, new Integer('C'));
 	JButton cancelButton = new JButton(cancelAction);
 	cancelButton.setName("wizard_close");
 	pane.addButton(btnRight, cancelButton, true);
 	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 		KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
 	getRootPane().getActionMap().put("ESCAPE", cancelAction);
     }
     
     /** set the handler to be called when a page switch is done.
      * The handler is called after the page has been shown and can be
      * used to modify the dialog contents. */
     public void setHandler(PageListener l) {
 	handler = l;
     }
     
     public interface PageListener {
 	/** called when page was changed. This is called after the page has
 	 * been shown and can be used to modify the dialog contents.
 	 * 
 	 * @param w Current TemplateWizard
 	 * @param page Page number currently shown (index in pages)
 	 */
 	void pageChanged(TemplateWizard w, int page);
     }
     
 
     public void setBackground(Color c) {
 	super.setBackground(c);
 	if (pane!=null) pane.setBackground(c);
     }
     /*
      * 
      * Delegates
      * 
      */
     public void addMouseTrackingListener(FSMouseListener l) {
 	pane.addMouseTrackingListener(l);
     }
 
     public void componentHidden(ComponentEvent e) {
 	pane.componentHidden(e);
     }
 
     public void componentMoved(ComponentEvent e) {
 	pane.componentMoved(e);
     }
 
     public void componentResized(ComponentEvent e) {
 	pane.componentResized(e);
     }
 
     public void componentShown(ComponentEvent e) {
 	pane.componentShown(e);
     }
 
     public Properties data() {
 	return pane.data();
     }
 
     public Document getDocument() {
 	return pane.getDocument();
     }
 
     public String getDocumentTitle() {
 	return pane.getDocumentTitle();
     }
 
     public List<?> getMouseTrackingListeners() {
 	return pane.getMouseTrackingListeners();
     }
 
     public SharedContext getSharedContext() {
 	return pane.getSharedContext();
     }
 
     public URL getURL() {
 	return pane.getURL();
     }
 
     public boolean isActive(Element e) {
 	return pane.isActive(e);
     }
 
     public boolean isFocus(Element e) {
 	return pane.isFocus(e);
     }
 
     public boolean isHover(Element e) {
 	return pane.isHover(e);
     }
 
     public boolean print() throws PrinterException {
 	return pane.print();
     }
 
     public boolean refresh() {
 	return pane.refresh();
     }
 
     public void reloadDocument(Document doc) {
 	pane.reloadDocument(doc);
     }
 
     public void reloadDocument(String URI) {
 	pane.reloadDocument(URI);
     }
 
     public void removeMouseTrackingListener(FSMouseListener l) {
 	pane.removeMouseTrackingListener(l);
     }
 
     public void replaceLinkListener(LinkListener llnew) {
 	pane.replaceLinkListener(llnew);
     }
 
     public void setData(Properties p) {
 	pane.setData(p);
     }
 
     public void setDocument(TemplateDocument doc) {
 	pane.setDocument(doc);
     }
     
     public void setDocument(TemplateDocument doc, NamespaceHandler nsh) {
 	pane.setDocument(doc, nsh);
     }
 
     public void setDocument(Document doc, String url) {
 	pane.setDocument(doc, url);
     }
 
     public void setDocument(Document doc) {
 	pane.setDocument(doc);
     }
 
     public void setDocument(File file) throws Exception {
 	pane.setDocument(file);
     }
 
     public void setDocument(InputStream stream, String url, NamespaceHandler nsh) {
 	pane.setDocument(stream, url, nsh);
     }
 
     public void setDocument(InputStream stream, String url) throws Exception {
 	pane.setDocument(stream, url);
     }
 
     public void setDocument(String url, NamespaceHandler nsh) {
 	pane.setDocument(url, nsh);
     }
 
     public void setDocument(String uri) {
 	pane.setDocument(uri);
     }
 
     public void setDocumentFromString(String content, String url,
 	    NamespaceHandler nsh) {
 	pane.setDocumentFromString(content, url, nsh);
     }
 
     public void setFormSubmissionListener(FormSubmissionListener fsl) {
 	pane.setFormSubmissionListener(fsl);
     }
 
     public void setSharedContext(SharedContext ctx) {
 	pane.setSharedContext(ctx);
     }
 
     public void setSubmitAction(Action e) {
 	pane.setSubmitAction(e);
     }
 
     public void submit(String query) {
 	pane.submit(query);
     }
 
 }
