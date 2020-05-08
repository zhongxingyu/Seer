 package net.marcuswhybrow.uni.g52gui.cw2;
 
 import com.apple.eawt.AppEvent.PreferencesEvent;
 import com.apple.eawt.PreferencesHandler;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Stack;
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import net.marcuswhybrow.uni.g52gui.cw2.bookmarks.Bookmark;
 import net.marcuswhybrow.uni.g52gui.cw2.bookmarks.BookmarkItem;
 import net.marcuswhybrow.uni.g52gui.cw2.bookmarks.Folder;
 import net.marcuswhybrow.uni.g52gui.cw2.bookmarks.RootFolder;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author Marcus Whybrow
  */
 public class Browser implements ActionListener
 {
 	private static Browser browser = null;
 
 	private ArrayList<Window> windows = new ArrayList<Window>();
 	private Stack<Reopenable> closedItems = new Stack<Reopenable>();
 
 	private Frame activeFrame = null;
 	private Window activeWindow = null;
 
 	private Folder bookmarksBar;
 	private Folder otherBookmarks;
 
 	private Settings preferences;
 
 	private Image icon;
 
 	public enum OperatingSystem {WINDOWS, MAC, LINUX_OR_UNIX, OTHER};
 	private OperatingSystem os;
 
 	private Browser() {}
 
 	private void determineOperatingSystem()
 	{
 		String osName = System.getProperty("os.name").toLowerCase();
 		if (osName.indexOf("win") >= 0)
 			this.os = OperatingSystem.WINDOWS;
 		else if (osName.indexOf("mac") >= 0)
 			this.os = OperatingSystem.MAC;
 		else if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0)
 			this.os = OperatingSystem.LINUX_OR_UNIX;
 		else
 			this.os = OperatingSystem.OTHER;
 	}
 
 	private void determineIcon()
 	{
 		try
 		{
 			icon = ImageIO.read(getClass().getClassLoader().getResource("assets/icon.png"));
 		}
 		catch (IOException ex)
 		{
 			System.err.println("Couldn't find the Browser icon image");
 		}
 	}
 
 	private void doMacSpecificSetup()
 	{
 		com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
 		app.setDockIconImage(this.getIcon());
 		app.setDefaultMenuBar(new MenuBar());
 		app.setPreferencesHandler(new PreferencesHandler() {
 			public void handlePreferences(PreferencesEvent pe)
 			{
 				Settings.get().showSettings();
 			}
 		});
 	}
 
 	private void setup()
 	{
 		this.determineOperatingSystem();
 		this.determineIcon();
 
 		switch (os)
 		{
 			case MAC:
 				this.doMacSpecificSetup();
 				break;
 			case WINDOWS:
 				break;
 			case LINUX_OR_UNIX:
 				break;
 			case OTHER:
 				break;
 		}
 		
 		this.openWindow();
 
 		this.preferences = Settings.get();
 
 		this.bookmarksBar = readFromFile("bookmarks_bar.xml");
 		this.otherBookmarks = readFromFile("other_bookmarks.xml");
 
 		if (this.bookmarksBar == null)
 			this.bookmarksBar = new RootFolder("Bookmarks Bar");
 		if (this.otherBookmarks == null)
 			this.otherBookmarks = new RootFolder("Other Bookmarks");
 	}
 
 	public Folder getBookmarksBarBookmarks()
 	{
 		return bookmarksBar;
 	}
 
 	public Folder getOtherBookmarksBookmarks()
 	{
 		return otherBookmarks;
 	}
 
 	private static Folder readFromFile(String filePath)
 	{
 		Document doc = null;
 		try
 		{
 			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filePath));
 		}
 		catch (ParserConfigurationException ex)
 		{
 			System.err.println("parser configuration error");
 		}
 		catch (SAXException ex)
 		{
 			System.err.println("SAX error");
 		}
 		catch (IOException ex)
 		{
 			System.err.println("IO error");
 		}
 
 		if (doc != null)
 		{
 			doc.getDocumentElement().normalize();
 			return (Folder) convert(doc.getDocumentElement());
 		}
 		return null;
 	}
 
 	private static void writeToFile(Folder folder, String filePath)
 	{
 		Document doc = null;
 		try
 		{
 			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		}
 		catch (ParserConfigurationException ex)
 		{
 			System.err.println("Some sort of XML error");
 		}
 
 		if (doc != null)
 		{
 			doc.appendChild(folder.convertToNode(doc));
 			try
 			{
 				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(new File(filePath)));
 			}
 			catch (TransformerException ex)
 			{
 				System.err.print("XML transformer error");
 			}
 		}
 	}
 
 	private static BookmarkItem convert(Element element)
 	{
 		if (element.getNodeName().equals(Folder.getRootFolderName()))
 		{
 			Folder rootFolder = new RootFolder(element.getAttribute("name"));
 			NodeList nodes = element.getChildNodes();
 			for (int i = 0; i < nodes.getLength(); i++)
 				rootFolder.addChild(convert((Element) nodes.item(i)));
 			return rootFolder;
 		}
 		else if (element.getNodeName().equals(Bookmark.getXmlElementName()))
 			return new Bookmark(element.getAttribute("address"), element.getAttribute("title"));
 		else if (element.getNodeName().equals(Folder.getXmlElementName()))
 		{
 			Folder folder = new Folder(element.getAttribute("name"));
 			NodeList nodes = element.getChildNodes();
 			for (int i = 0; i < nodes.getLength(); i++)
 				folder.addChild(convert((Element) nodes.item(i)));
 			return folder;
 		}
 		return null;
 	}
 
 	public static synchronized Browser get()
 	{
 		if (browser == null)
 		{
 			browser = new Browser();
 			browser.setup();
 		}
 		return browser;
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 
 	public void openWindow()
 	{
 		Window window = new Window();
 		windows.add(window);
 		setActiveFrame(window);
 	}
 
 	public void setActiveFrame(Frame frame)
 	{
 		activeFrame = frame;
 		if (frame instanceof Window)
 			activeWindow = (Window) frame;
 	}
 
 	public void windowHasClosed(Window window)
 	{
 		windowHasClosed(window, true);
 	}
 
 	public void windowHasClosed(Window window, boolean putOnClosedItemStack)
 	{
 		windows.remove(window);
 		if (putOnClosedItemStack)
 			addClosedItem(window);
 		if (windows.size() == 0)
 			close();
 	}
 
 	public void close()
 	{
 		Browser.writeToFile(bookmarksBar, "bookmarks_bar.xml");
 		Browser.writeToFile(otherBookmarks, "other_bookmarks.xml");
 		System.exit(0);
 	}
 
 	public void addClosedItem(Reopenable item)
 	{
 		closedItems.push(item);
 	}
 
 	public void openLastClosedItem()
 	{
 		Reopenable item;
 
 		while (!closedItems.isEmpty())
 		{
 			item = closedItems.pop();
 			if (item.isClosed())
 			{
 				System.out.println("Opening item: " + item.toString());
 				item.reopen();
 				break;
 			}
 		}
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		System.out.println(e.getActionCommand());
 		
 		if ("New Window".equals(e.getActionCommand()))
 			Browser.get().openWindow();
 		else if ("New Tab".equals(e.getActionCommand()))
 			activeWindow.getTabs().openWebPageTab();
 		else if ("Close Window".equals(e.getActionCommand()) && activeFrame != null)
 			activeFrame.close();
 		else if ("Close Tab".equals(e.getActionCommand()) && activeWindow != null)
 			activeWindow.getTabs().getActiveTab().close();
 		else if ("Re-open Closed Tab".equals(e.getActionCommand()))
 			openLastClosedItem();
 		else if ("Open Location".equals(e.getActionCommand()) && activeWindow != null)
 			activeWindow.getTabs().getActiveTab().getAddressBar().requestFocus();
 		else if ("Bookmark Manager".equals(e.getActionCommand()) && activeWindow != null)
 			activeWindow.getTabs().openBookmarkManagerTab();
 
 		else if ("Home".equals(e.getActionCommand()) && activeWindow != null)
 			activeWindow.getTabs().getActiveTab().home();
 		else if ("Back".equals(e.getActionCommand()) && activeWindow != null)
 			activeWindow.getTabs().getActiveTab().back();
 		else if ("Forward".equals(e.getActionCommand()) && activeWindow != null)
 			activeWindow.getTabs().getActiveTab().forward();
 	}
 
 	public void showPreferences()
 	{
 		preferences.setVisible(true);
 	}
 
 	public Image getIcon()
 	{
 		return icon;
 	}
 
 	public static void main(String[] args)
 	{
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 		System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
 		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Browser");
 
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e)
 		{
 			System.err.println("Unable to set look and feel");
 		}
 
 		try
 		{
 			// Set the tree leaf icon in a JTree to be the same as the folder icon
 			UIDefaults def = UIManager.getLookAndFeelDefaults();
			// def.put("Tree.leafIcon", def.get("Tree.closedIcon"));
			def.put("Tree.closedIcon", def.get("Tree.leafIcon"));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		Browser.get();
 	}
 
 	public OperatingSystem getOperatingSystem()
 	{
 		return this.os;
 	}
 
 	public Window getActiveWindow()
 	{
 		return this.activeWindow;
 	}
 
 	public Frame getActiveFrame()
 	{
 		return this.activeFrame;
 	}
 }
