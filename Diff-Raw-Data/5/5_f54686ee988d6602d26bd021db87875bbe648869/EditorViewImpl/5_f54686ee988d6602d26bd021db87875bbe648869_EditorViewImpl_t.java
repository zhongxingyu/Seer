 /**
  * 
  */
 package ch.ethz.e4mooc.client.widgets.editor;
 
import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.client.ui.NavLink;
 import com.github.gwtbootstrap.client.ui.NavTabs;
 import com.github.gwtbootstrap.client.ui.constants.IconType;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author hce
  *
  */
 public class EditorViewImpl extends Composite implements EditorView {
 
 	private static EditorViewImplUiBinder uiBinder = GWT
 			.create(EditorViewImplUiBinder.class);
 
 	interface EditorViewImplUiBinder extends UiBinder<Widget, EditorViewImpl> {
 	}
 
 	/** the div element where we insert the ACE editor **/
 	@UiField
 	DivElement aceContainer;
 	@UiField
 	NavTabs navTabs;
 	@UiField
 	Button reloadBtn;
 	
 	/** List which stores the navLinks currently displayed. NavLinks are the UI elements looking like tabs. */
 	private final List<NavLink> navLinks;
 
 	/** the Presenter belonging to this view **/
 	private EditorPresenter presenter;
 	/** JS object that hold the reference to the ace editor.**/
 	private JavaScriptObject aceEditor;
 	/** The id for the div into which the ace editor is inserted **/
 	private final String aceId = "_aceEditor";	
 	/** the index of the currently selected tab */
 	private int currentlySelectedTabIndex;
 			
 	/**
 	 * Constructor
 	 */
 	public EditorViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		navLinks = new LinkedList<NavLink>();
 		
 		reloadBtn.setIcon(IconType.REFRESH);
 		reloadBtn.setTitle("Click to reload the\noriginal version of the\ncurrent example. Your\nchanges will be lost.");
 		
 		// we need to set the id of the <div> element. Can't do that through the XML file.
 		aceContainer.setId(aceId);
 	}
 	
 	public native void addAceEditor(String startFile) /*-{		
 		// we get access to the GWT javascript code through the window $wnd (all GWT JS is running in an iframe)
 		var editor = $wnd.ace.edit(this.@ch.ethz.e4mooc.client.widgets.editor.EditorViewImpl::aceId);
 		
 		// make the editor available within the java code
 		this.@ch.ethz.e4mooc.client.widgets.editor.EditorViewImpl::aceEditor = editor;
 		
 		// we don't want to display the print margin
 		editor.setShowPrintMargin(false);
 		
 		// set the default text for the editor
 		editor.getSession().setValue(startFile);
 	}-*/;
 
 	/**
 	 * @see ch.ethz.EiffelPageView.MainPageView.tool.widgest.SimplePageView.getAceText
 	 */
 	public native String getEditorText() /*-{
 		var editor = this.@ch.ethz.e4mooc.client.widgets.editor.EditorViewImpl::aceEditor;
 		return editor.getSession().getValue();
 	}-*/;
 
 	/**
 	 * @see ch.ethz.EiffelPageView.MainPageView.tool.widgest.SimplePageView.setAceText
 	 */
 	public native void setEditorText(String text) /*-{
 		var editor = this.@ch.ethz.e4mooc.client.widgets.editor.EditorViewImpl::aceEditor;
 		editor.getSession().setValue(text);
 		editor.resize(true); // we do a resize here because otherwise an unnecessary scrollbar may remain
 	}-*/;
 	
 	
 	public native void setEditorMode(String mode) /*-{
 		var editor = this.@ch.ethz.e4mooc.client.widgets.editor.EditorViewImpl::aceEditor;
 		editor.getSession().setMode("ace/mode/" + mode);
 	}-*/;
 	
 
 	@Override
 	public void addTabs(List<String> tabNames) {
 		
 		// remove any currently existing tabs in the nav bar
 		navTabs.clear();
 		// clear the list were we keep references to the navLinks
 		navLinks.clear();
 		
		// sort the tabNames so they are in alphabetic order
		Collections.sort(tabNames);
		
 		for(String tabName: tabNames) {
 			// create the navLink object which represents a tab
 			final NavLink navLink = new NavLink(tabName);
 			// add the tab
 			navTabs.add(navLink);
 			// keep the reference to the tab
 			navLinks.add(navLink);
 			// assign a click handler to the tab
 			navLink.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					presenter.onTabLinkClick(navTabs.getWidgetIndex(navLink));
 				}
 			});
 		}
 	}
 
 	@Override
 	public void updateSelectedTab(int tabIndex) {
 		// deactivate all nav-links
 		for(NavLink nl: navLinks) {
 			nl.setActive(false);
 		}
 		// activate the right one
 		navLinks.get(tabIndex).setActive(true);
 		currentlySelectedTabIndex = tabIndex;
 	}
 	
 	@Override
 	public int getCurrentlySelectedTabIndex() {
 		return currentlySelectedTabIndex;
 	}
 
 	@UiHandler("reloadBtn")
 	public void onClick(ClickEvent e) {
 		presenter.onReloadBtnClick(getCurrentlySelectedTabIndex());
 	}
 
 	@Override
 	public void setPresenter(
 			ch.ethz.e4mooc.client.widgets.editor.EditorPresenter presenter) {
 		this.presenter = presenter;
 		
 	}
 }
