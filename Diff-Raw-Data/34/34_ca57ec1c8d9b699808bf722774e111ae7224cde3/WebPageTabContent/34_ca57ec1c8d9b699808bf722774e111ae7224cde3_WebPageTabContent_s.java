 package net.marcuswhybrow.uni.g52gui.cw2.visual.tabs;
 
 import java.awt.Component;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.swing.JEditorPane;
 import javax.swing.JScrollPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.Document;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLFrameHyperlinkEvent;
 import net.marcuswhybrow.uni.g52gui.cw2.Browser;
 import net.marcuswhybrow.uni.g52gui.cw2.Page;
 import net.marcuswhybrow.uni.g52gui.cw2.visual.AddressBar;
import net.marcuswhybrow.uni.g52gui.cw2.visual.StatusBar;
 import net.marcuswhybrow.uni.g52gui.cw2.visual.ToolBar;
 import net.marcuswhybrow.uni.g52gui.cw2.visual.Window;
 
 /**
  *
  * @author Marcus Whybrow
  */
 public class WebPageTabContent extends JScrollPane implements TabContent, HyperlinkListener, PropertyChangeListener
 {
 	private JEditorPane pane;
 	private Tab tab;
 
 	public static enum State {EMPTY, WAITING, LOADING, DONE};
 	private State state = State.EMPTY;
 
 	public WebPageTabContent(Tab tab)
 	{
 		this(tab, null);
 	}
 
 	public WebPageTabContent(Tab tab, String address)
 	{
 		this.tab = tab;
 
 		if (address != null)
 		{
 			try
 			{
 				this.pane = new EditorPane(address);
 			}
 			catch (IOException ioe)
 			{
 				this.pane = new EditorPane();
 			}
 		}
 		else
 			this.pane = new EditorPane();
 		
 		this.pane.setEditable(false);
 		this.setViewportView(pane);
 		this.pane.addHyperlinkListener(this);
 		this.pane.addPropertyChangeListener(this);
 
 		ToolBar tb = this.tab.getToolBar();
 		AddressBar ab = tb.getAddressBar();
 		ab.setText(address);
 	}
 
 	public void hyperlinkUpdate(HyperlinkEvent e)
 	{
 		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
 		{
 			JEditorPane source = (JEditorPane) e.getSource();
 			if (e instanceof HTMLFrameHyperlinkEvent)
 			{
 				HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
 				HTMLDocument doc = (HTMLDocument)source.getDocument();
 				doc.processHTMLFrameHyperlinkEvent(evt);
 			}
 			else
 			{
 				this.tab.goToWithoutHistory(e.getURL().toString());
 			}
 		}
 		else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED)
 		{
 			this.getWindow().getStatusBar().setText(e.getURL().toString());
 		}
 		else if (e.getEventType() == HyperlinkEvent.EventType.EXITED)
 		{
 			this.getWindow().getStatusBar().clear();
 		}
 	}
 
 	public void propertyChange(PropertyChangeEvent pce)
 	{
 		if ("document".equals(pce.getPropertyName()))
 			this.setState(WebPageTabContent.State.LOADING);
 		else if ("page".equals(pce.getPropertyName()))
 			this.setState(WebPageTabContent.State.DONE);
 	}
 
 	public void goTo(URL location)
 	{
 		try
 		{
 			pane.setPage(location);
 		}
 		catch (IOException ex)
 		{
 			System.err.println("Malformed URL: " + location.toString());
 		}
 	}
 
 	public void goTo(String address)
 	{
 		try
 		{
 			this.goTo(new URL(address));
 		}
 		catch (MalformedURLException ex)
 		{
 			System.err.println("Could not convert a string to a URL");
 		}
 	}
 
 	public Component getContent()
 	{
 		return this;
 	}
 
 	public void setTab(Tab tab)
 	{
 		this.tab = tab;
 	}
 
 	public Tab getTab()
 	{
 		return this.tab;
 	}
 
 	public String getShortenedWebPageTitle()
 	{
 		String title = getWebPageTitle();
 		return title.length() >= 25 ? title.substring(0, 25) + " ..." : title;
 	}
 
 	public String getWebPageTitle()
 	{
 
 		String title = (String) this.pane.getDocument().getProperty("title");
 		if (title == null)
 			title = (String) this.pane.getDocument().getProperty("TITLE");
 		if (title == null)
 			title = "Untitled Document";
 		return title;
 	}
 
 	public void setState(State state)
 	{
 		this.state = state;
 		Page page = this.tab.getCurrentLocation();
 		switch (this.state)
 		{
 			case EMPTY:
 				break;
 			case WAITING:
 				if (page != null)
 					this.getWindow().getStatusBar().setText("Waiting for " + page.getHost());
 				break;
 			case LOADING:
 				if (page != null)
 					this.getWindow().getStatusBar().setImportantText("Transfering data from " + page.getHost());
 				this.tab.getTabButton().setIcon(Browser.get().getTabLoadingIcon());
 				break;
 			case DONE:
 				this.getWindow().getStatusBar().doneWithImportantText();
 				page.setTitle(this.getWebPageTitle());
 				this.tab.setTitle(this.getShortenedWebPageTitle());
 				this.tab.getTabButton().setIcon(page.getFavIcon());
 				break;
 		}
 	}
 
 	public Window getWindow()
 	{
 		return this.tab.getWindow();
 	}
 
 	private class EditorPane extends JEditorPane
 	{
 		private void allowCurrentPageToRefresh()
 		{
 			this.getDocument().putProperty(Document.StreamDescriptionProperty, null);
 		}
 
 		public EditorPane()
 		{
 			super();
 		}
 
 		public EditorPane(String address) throws IOException
 		{
 			super(address);
 		}
 
 		public EditorPane(URL address) throws IOException
 		{
 			super(address);
 		}
 
 		@Override
 		public void setPage(URL url) throws IOException
 		{
 			setState(State.WAITING);
 			this.allowCurrentPageToRefresh();
 			super.setPage(url);
 		}
 	}
 }
