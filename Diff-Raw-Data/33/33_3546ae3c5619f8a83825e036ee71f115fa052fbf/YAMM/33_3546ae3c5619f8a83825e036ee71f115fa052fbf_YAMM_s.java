/*  $Id: YAMM.java,v 1.58 2003/03/07 10:52:09 fredde Exp $
 *  Copyright (C) 1999-2001 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.gjt.fredde.yamm;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.text.*;
 
 import javax.swing.*;
 import javax.swing.tree.*;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.event.*;
 import javax.swing.table.*;
 import javax.swing.border.*;
 
 import org.gjt.fredde.yamm.mail.*;
 import org.gjt.fredde.yamm.gui.*;
 import org.gjt.fredde.util.gui.*;
 import org.gjt.fredde.util.net.Browser;
 import org.gjt.fredde.yamm.gui.main.*;
 import org.gjt.fredde.yamm.encode.*;
 
 /**
  * The big Main-class of YAMM
  *
  * @author Fredrik Ehnbom
 * @version $Id: YAMM.java,v 1.58 2003/03/07 10:52:09 fredde Exp $
  */
 public class YAMM
 	extends JFrame
 	implements HyperlinkListener
 {
 	public static int INDEX_NUM = 0;
 	public static int INDEX_SUBJECT = 1;
 	public static int INDEX_FROM = 2;
 	public static int INDEX_DATE = 3;
 	public static int INDEX_STATUS = 4;
 	public static int INDEX_SKIP = 5;
 
 	/** The home of yamm */
 	public static String home = Utilities.replace(System.getProperty("user.home") + "/.yamm");
 
 	/** To get the Language strings for buttons, menus, etc... */
 	private static ResourceBundle         res;
 
 	/** The box the user has selected. */
 	public static String selectedbox = Utilities.replace(home + "/boxes/");
 
 	/** The version of YAMM */
	public static String version  = "0.8";
 
 	/** The compileDate of YAMM */
 	public static String compDate = Utilities.getCompileDate();
 
 	/** the file that contains the current mail */
 	public String mailPageString = "file:///" + home + "/tmp/cache/";
 	public URL mailPage;
 
 	/** The vector containing the attaced files. */
 	public Vector attach = new Vector();
 
 	/** To check if the user has sun.misc.Base64Decoder */
 	private boolean base64 = false;
 	public static boolean base64enc = false;
 
 	/** The vector containing the mails of a box */
 	public String[][] listOfMails;
 	public int[] keyIndex;
 
 	/** The properties to get configuration stuff from */
 	static protected Properties props = new Properties();
 
 	/** The Table that lists the mails in listOfMails */
 	public mainTable mailList;
 
 	/** The box tree */
 	public mainJTree tree;
 
 	/** If the buttons should be painted with icons, text or both */
 	public static boolean ico = true, text = true;
 
 	/** The JEditorPane for this frame */
 	public JEditorPane mail;
 
 	/** Attachment list */
 	public AttachList myList;
 
 	/** The toolbar */
 	public mainToolBar tbar;
 
 	/** The splitpanes */
 	protected JSplitPane   SPane, SPane2;
 
 	/** The statusrow */
 	public statusRow status;
 
 	/** The mail/attach tabbedpane */
 	protected JTabbedPane JTPane;
 
 	/** The names for the exception dialogs buttons */
 	public static String[] exceptionNames = null;
 
 	public static PrintStream debug = null;
 
 	/**
 	 * The profile manager
 	 */
 	public static Profiler profiler = null;
 
 	/**
 	 * Returns the translated string.
 	 *
 	 * @param s The string to get translation for.
 	 */
 	public static final String getString(String s) {
 		return res.getString(s);
 	}
 
 	/**
 	 * Returns the translated string
 	 *
 	 * @param s The string to get translation for.
 	 * @param args The arguments to send to MessageFormat.format
 	 */
 	public static final String getString(String s, Object[] args) {
 		return MessageFormat.format(res.getString(s), args);
 	}
 
 	/**
 	 * Returns the property
 	 *
 	 * @param property The property to get.
 	 */
 	public static String getProperty(String property) {
 		return props.getProperty(property);
 	}
 
 	/**
 	 * Returns the property
 	 *
 	 * @param prop The property to get.
 	 * @param def The default string to return if prop is null.
 	 */
 	public static String getProperty(String prop, String def) {
 		return props.getProperty(prop, def);
 	}
 
 	/**
 	 * Sets a property
 	 *
 	 * @param property The property to set.
 	 * @param value The value of the property.
 	 */
 	public static void setProperty(String property, String value) {
 		props.setProperty(property, value);
 	}
 
 	/**
 	 * Creates the main-window and adds all the components in it.
 	 */
 	public YAMM() {
 		try {
 			mailPage=new URL(mailPageString + res.getString("box.inbox") + "/0.html");
 		} catch (MalformedURLException mue) {
 			new ExceptionDialog(YAMM.getString("msg.error"),
 					mue, YAMM.exceptionNames);
 		}
 
 		System.out.println("Locale: " + getProperty("locale", Locale.getDefault().toString()));
 		System.out.println("Checking for sun classes...");
 
 		if (ClassLoader.getSystemResource("sun/misc/BASE64Decoder.class") != null) {
 			base64 = true;
 		}
 		if (ClassLoader.getSystemResource("sun/misc/BASE64Encoder.class") != null) {
 			base64enc = true;
 		}
 		System.out.println("sun.misc.BASE64Decoder: " + (base64 ? "yes" : "no"));
 		System.out.println("sun.misc.BASE64Encoder: " + (base64enc ? "yes" : "no"));
 
 		// get the main window's settings and default them if they
 		// don't exist
 		int mainx = Integer.parseInt(props.getProperty("mainx", "0"));
 		int mainy = Integer.parseInt(props.getProperty("mainy", "0"));
 		int mainw = Integer.parseInt(props.getProperty("mainw", "650"));
 		int mainh = Integer.parseInt(props.getProperty("mainh", "380"));
 		int hsplit = Integer.parseInt(props.getProperty("hsplit", "80"));
 		int vsplit = Integer.parseInt(props.getProperty("vsplit", "125"));
 
 		setBounds(mainx, mainy, mainw, mainh);
 		getContentPane().setLayout(new BorderLayout());
 
 		// the menubar
 		setJMenuBar(new mainMenu(this));
 
 		// the toolbar
 		tbar = new mainToolBar(this);
 		getContentPane().add("North", tbar);
 
 		// create a list of mails in the selected box
 		Mailbox.createList(selectedbox, this);
 
 		// the tablemodel for the maillist table
 
 		mail = new JEditorPane();
 		mail.setContentType("text/html");
 		mail.setEditable(false);
 		mail.addHyperlinkListener(this);
 
 		Mailbox.getMail(selectedbox, 0, 0);
 		try {
 			mail.setPage(mailPage);
 		} catch (IOException ioe) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				ioe,
 				YAMM.exceptionNames
 			);
 		}
 
 		JTPane = new JTabbedPane(JTabbedPane.BOTTOM);
 		JTPane.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		if (text && !ico) {
 			JTPane.addTab(res.getString("mail"), new JScrollPane(mail));
 		} else if (ico && !text) {
 			JTPane.addTab("",
 			new ImageIcon(getClass().getResource("/images/buttons/mail.gif")),
 			new JScrollPane(mail));
 		} else {
 			JTPane.addTab(res.getString("mail"),
 			new ImageIcon(getClass().getResource("/images/buttons/mail.gif")),
 			new JScrollPane(mail));
 		}
 
 		JPanel myPanel = new JPanel(new BorderLayout());
 
 		Box hori1 = Box.createHorizontalBox();
 
 		JButton b = new JButton();
 		b.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		if (text) {
 			b.setText(res.getString("button.view_extract"));
 		}
 		if (ico) {
 			b.setIcon(new ImageIcon(getClass().getResource("/images/buttons/search.gif")));
 		}
 		b.setToolTipText(res.getString("button.view_extract"));
 		b.addActionListener(BListener);
 		hori1.add(b);
 
 		// The ListModel for the attachment list
 		ListModel attachModel = new AbstractListModel() {
 			public Object getElementAt(int index) {
 				return ((Vector)attach.elementAt(index)).elementAt(0);
 			}
 			public int getSize() {
 				return attach.size();
 			}
 		};
 
 		myList = new AttachList(this, attachModel, attach, AttachList.DRAGMODE);
 		myPanel.add("Center", myList);
 		myPanel.add("South", hori1);
 
 		Border ram = BorderFactory.createEtchedBorder();
 		myList.setBorder(ram);
 
 		if (ico && !text) {
 			JTPane.addTab(
 				"",
 				new ImageIcon(getClass().getResource("/images/buttons/attach.gif")),
 				myPanel
 			);
 		} else if (text && !ico) {
 			JTPane.addTab(res.getString("mail.attachment"),myPanel);
 		} else {
 			JTPane.addTab(
 				res.getString("mail.attachment"),
 				new ImageIcon(getClass().getResource("/images/buttons/attach.gif")),
 				myPanel
 			);
 		}
 
 		mailList = new mainTable(this);
 		mailList.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
 		createAttachList();
 
		// When the mails in the maillist doesn't take all the
		// maillists space, this fix still paints the background
		// in the maillists background color.
		JPanel bgFix = new JPanel(new BorderLayout());
		bgFix.setBackground(mailList.getBackground());
		bgFix.add("Center", new JScrollPane(mailList));
 
		SPane = new JSplitPane(0, bgFix, JTPane);
 		SPane.setDividerLocation(hsplit);
 		SPane.setMinimumSize(new Dimension(0, 0));
 		SPane.setPreferredSize(new Dimension(300, 50));
 		SPane.setOneTouchExpandable(true);
 
 		tree = new mainJTree(this, tbar);
 
		SPane2 = new JSplitPane(1, tree, SPane);
 		SPane2.setDividerLocation(vsplit);
 		SPane2.setOneTouchExpandable(true);
 		getContentPane().add("Center", SPane2);
 
 		status = new statusRow();
 		status.button.setText(res.getString("button.cancel"));
 		getContentPane().add("South", status);
 		addWindowListener(new FLyssnare());
 		show();
 	}
 
 
 	/**
 	 * Checks if the link is a mailto:-link. If it is, it starts a
 	 * write-window with the specified mailto:-address,
 	 * if it isn't, it starts the webbrowser for that platform.
 	 */
 	public void hyperlinkUpdate(HyperlinkEvent e) {
 		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 			String link = e.getURL().toString();
 
 			if ((e.getURL().toString()).startsWith("mailto:")) {
 				link = link.substring(link.indexOf("mailto:") +	7, link.length());
 				new YAMMWrite(link);
 			} else {
 				try {
 					Browser.open(link);
 				} catch (InterruptedException ie) {
 					new ExceptionDialog(
 						YAMM.getString("msg.error"),
 						ie,
 						YAMM.exceptionNames
 					);
 				} catch(IOException ioe) {
 					new ExceptionDialog(
 						YAMM.getString("msg.error"),
 						ioe,
 						YAMM.exceptionNames
 					);
 				}
 			}
 		} else if (e.getEventType() ==	HyperlinkEvent.EventType.ENTERED) {
 			mail.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		} else if (e.getEventType() ==	HyperlinkEvent.EventType.EXITED) {
 			mail.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 		}
 	}
 
 	/**
 	 * The actionListener for the view/extract button
 	 */
 	ActionListener BListener = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			int whichAtt = myList.getSelectedIndex();
 
 			if (whichAtt != -1) {
 				String filename	= ((Vector)attach.elementAt(whichAtt)).elementAt(0).toString();
 				String realFile = ((Vector)attach.elementAt(whichAtt)).elementAt(1).toString();
 
 				JFileChooser jfs = new JFileChooser();
 				jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				jfs.setMultiSelectionEnabled(false);
 				jfs.setSelectedFile(new File(filename));
 				int ret = jfs.showSaveDialog(null);
 
 				if (ret == JFileChooser.APPROVE_OPTION) {
 					if (jfs.getSelectedFile() != null) {
 						BufferedInputStream in = null;
 						BufferedOutputStream out = null;
 						byte[] buffer = new byte[65536];
 						try {
 							in = new BufferedInputStream(new FileInputStream(realFile));
 							out = new BufferedOutputStream(new FileOutputStream(jfs.getSelectedFile()));
 
 							while (true) {
 								int len = in.read(buffer);
 								if (len == -1) break;
 
 								out.write(buffer, 0, len);
 							}
 						} catch (IOException ioe) {
 							new ExceptionDialog(
 								YAMM.getString("msg.error"),
 								ioe,
 								YAMM.exceptionNames
 							);
 
 						} finally {
 							try {
 								if (in != null) in.close();
 								if (out != null) out.close();
 							} catch (IOException ioe) {}
 						}
 					}
 				}
 			}
 		}
 	};
 
 	public void createAttachList() {
 		attach.clear();
 
 		String boxName = selectedbox.substring(
 				selectedbox.indexOf("boxes") + 6,
 				selectedbox.length()) + "/";
 
 		String base = home +  "/tmp/cache/" + boxName;
 
 		int msgNum = mailList.getSelectedMessage();
 
 		if (msgNum == -1) {
 			JTPane.setEnabledAt(1, false);
 			return;
 		}
 
 		String[] test = new File(base).list();
 
 		for(int i = 0; i < test.length; i++ ) {
 			if (test[i].indexOf(msgNum + ".attach.") != -1 && !test[i].endsWith(".tmp")) addinfo(base + test[i], attach);
 		}
 		JTPane.setEnabledAt(1, attach.size() != 0);
 	}
 
 	private void addinfo(String where, Vector attach) {
 		Vector tmp = new Vector();
 
 		tmp.add(where.substring(where.indexOf(".attach.") + 8));
 		tmp.add(where);
 		attach.add(tmp);
 	}
 /*
   public int print(Graphics g, PageFormat pageFormat,
                         int pageIndex) throws PrinterException {
     mail.paint(g);
 
     return Printable.PAGE_EXISTS;
   }
 */
 
 	public void Exit() {
 		Rectangle rv = new Rectangle();
 		getBounds(rv);
 
 		mailList.save();
 		props.setProperty("mainx", "" + rv.x);
 		props.setProperty("mainy", "" + rv.y);
 		props.setProperty("mainw", "" + rv.width);
 		props.setProperty("mainh", "" + rv.height);
 		props.setProperty("vsplit", "" + SPane2.getDividerLocation());
 		props.setProperty("hsplit", "" + SPane.getDividerLocation());
 
 		try {
 			OutputStream out = new FileOutputStream(home + "/.config");
 			props.store(out, "YAMM configuration file");
 			out.close();
 		} catch (IOException propsioe) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				propsioe,
 				YAMM.exceptionNames
 			);
 		}
 		Utilities.delUnNeededFiles();
 		if (debug != System.err) {
 			debug.flush();
 			debug.close();
 		}
 		dispose();
 		System.exit(0);
 	}
 
 	/**
 	 * The WindowListener.
 	 */
 	class FLyssnare extends WindowAdapter {
 		public void windowClosing(WindowEvent event) {
 			Exit();
 		}
 	}
 
 	private static void createFiles() {
 		try {
 			new File(home + "/servers").mkdirs();
 			new File(home + "/boxes").mkdirs();
 			new File(home + "/.config").createNewFile();
 			new File(home + "/.profiles").createNewFile();
 			new File(home + "/.filters").createNewFile();
 			new File(home + "/tmp").mkdirs();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	private static void convert() {
 		OutputStream out = null;
 		Properties newProps = new Properties();
 
 		newProps.setProperty("profile.num", "1");
 		newProps.setProperty("profile.0.name", props.getProperty("username"));
 		newProps.setProperty("profile.0.email", props.getProperty("email"));
 		newProps.setProperty("profile.0.sign", props.getProperty("signatur"));
 
 		try {
 			out = new FileOutputStream(home + "/.profiles");
 
 			newProps.store(out, "YAMM profiles");
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 			} catch (IOException ioe) {}
 		}
 	}
 
 	private static void createWelcomeMessage() {
 		try {
 			String user = System.getProperty("user.name");
 			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
 
 			InetAddress myInetaddr = InetAddress.getLocalHost();
 			String host = myInetaddr.getHostName();
 			if (host == null) host = "localhost";
 
 			PrintWriter out = new PrintWriter(
 				new BufferedOutputStream(
 				new FileOutputStream(home + "/boxes/" +
 				YAMM.getString("box.inbox")))
 			);
 
 			out.println("Date: " + dateFormat.format(new Date()));
 			out.println("From: Fredrik Ehnbom <fredde@gjt.org>");
 			out.println("To: " + user + "@" + host);
 			Object[] args = {
 				YAMM.version,
 				user
 			};
 			out.println(YAMM.getString("msg.welcome", args));
 			out.close();
 
 			new File(home + "/boxes/" + getString("box.outbox")).createNewFile();
 			new File(home + "/boxes/" + getString("box.trash")).createNewFile();
 			new File(home + "/boxes/" + getString("box.sent")).createNewFile();
 		} catch(IOException ioe) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				ioe,
 				YAMM.exceptionNames
 			);
 		}
 	}
 
 	/**
 	 * Shows a SplashScreen while starting the program.
 	 * It checks if the user has config-files, if not it'll
 	 * create such and write a little welcome message in
 	 * ~home/.yamm/boxes/local/inbox.
 	 */
 	public static void main(String argv[]) {
 		YAMM nFrame = null;
 		SplashScreen splash = null;
 		boolean firstRun = false;
 
 		if (!(new File(home)).exists()) {
 			firstRun = true;
 			createFiles();
 		}
 
 
 		try {
 			InputStream in = new FileInputStream(home + "/.config");
 			props.load(in);
 			in.close();
 		} catch (IOException propsioe) {
 			propsioe.printStackTrace();
 			System.exit(1);
 		}
 
 		// convert the old profile format to the new
 		if (new File(home + "/.config").exists() && !new File(home + "/.profiles").exists()) {
 			convert();
 		}
 
 		Locale l = Locale.getDefault();
 		String resLanguage = l.getLanguage();
 		String resCountry = l.getCountry();
 
 		if (props.getProperty("locale.language") != null) {
 			resLanguage = props.getProperty("locale.language");
 		}
 
 		if (props.getProperty("locale.country") != null) {
 			resCountry = props.getProperty("locale.country");
 		}
 
 		l = new Locale(resLanguage, resCountry);
 		Locale.setDefault(l);
 
 		try {
 			res = ResourceBundle.getBundle("YAMM", l);
 		} catch (MissingResourceException mre) {
 			mre.printStackTrace();
 			System.exit(1);
 		}
 
 		if (props.getProperty("debug.file") != null) {
 			try {
 				debug = new PrintStream(
 					new BufferedOutputStream(
 						new FileOutputStream(props.getProperty("debug.file"))
 					)
 				);
 			} catch (IOException ioe) {
 				debug = System.err;
 			}
 		} else debug = System.err;
 
 		if (props.getProperty("plaf") != null) {
 			try {
 				UIManager.setLookAndFeel(props.getProperty("plaf"));
 			} catch (Exception e) {};
 		} else {
 			try {
 				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			} catch (Exception e) {};
 		}
 
 		if (props.getProperty("splashscreen", "yes").equals("yes")) {
 			splash = new SplashScreen("YAMM " + version +
 				" Copyright (c) 1999-2003 Fredrik Ehnbom",
 				props.getClass().getResource("/images/logo.gif"));
 		}
 
 		selectedbox += "/" + res.getString("box.inbox");
 		selectedbox = Utilities.replace(selectedbox);
 
 		String[] tmp = {
 			res.getString("exception.ok"),
 			res.getString("exception.more"),
 			res.getString("exception.less")
 		};
 
 		exceptionNames = tmp;
 
 		if (firstRun) {
 			createWelcomeMessage();
 		}
 
 		if (props.getProperty("button.mode", "both").equals("text")) {
 			ico = false;
 		} else if (props.getProperty("button.mode", "both").equals("icon")) {
 			text = false;
 		}
 
 		profiler = new Profiler();
 		nFrame = new YAMM();
 		nFrame.setTitle("Yet Another Mail Manager " + version);
 		if (splash != null) splash.dispose();
 	}
 }
 
 /*
  * Changes
  * $Log: YAMM.java,v $
  * Revision 1.58  2003/03/07 10:52:09  fredde
  * Attachments work again now
  *
  * Revision 1.57  2003/03/06 20:16:28  fredde
  * A few fixes regarding attachments to work with the new mailparsing system
  *
  * Revision 1.56  2003/03/05 14:59:29  fredde
  * Added INDEX_* constants. Added keyIndex variable. Updated Copyright info
  *
  * Revision 1.55  2001/05/27 08:56:33  fredde
  * replaced deprecationed code thanks to wYRd
  *
  * Revision 1.54  2001/04/21 09:31:20  fredde
  * drag and drop from attachlist
  *
  * Revision 1.53  2001/03/18 17:05:46  fredde
  * better init
  *
  * Revision 1.52  2000/12/26 11:21:11  fredde
  * YAMM.listOfMails is now of type String[][]
  *
  * Revision 1.51  2000/12/25 09:50:48  fredde
  * Some cleanups
  *
  * Revision 1.50  2000/08/09 16:23:46  fredde
  * readability fixes + added support for base64 encoding
  *
  * Revision 1.49  2000/07/16 17:48:36  fredde
  * lots of Windows compatiblity fixes
  *
  * Revision 1.48  2000/04/15 13:27:54  fredde
  * version tag changed to 0.7.7
  *
  * Revision 1.47  2000/04/11 13:26:42  fredde
  * changed version number to 0.7.6
  *
  * Revision 1.46  2000/04/01 20:50:09  fredde
  * fixed to make the profiling system work
  *
  * Revision 1.45  2000/03/18 17:36:20  fredde
  * changed the version form cvs-tag to 0.7.5....
  *
  * Revision 1.44  2000/03/15 13:40:59  fredde
  * cleaned up
  *
  * Revision 1.43  2000/03/15 11:13:45  fredde
  * boxes now show if and how many unread messages they have
  *
  * Revision 1.42  2000/03/05 18:02:53  fredde
  * now gets the images used for the jar-file
  *
  * Revision 1.41  2000/02/28 13:45:44  fredde
  * added changelog and some javadoc tags. Moved some functions to Utilities. Added a debug PrintStream and smart version control. fixed some startup bugs. cleaned up a little
  *
  */
