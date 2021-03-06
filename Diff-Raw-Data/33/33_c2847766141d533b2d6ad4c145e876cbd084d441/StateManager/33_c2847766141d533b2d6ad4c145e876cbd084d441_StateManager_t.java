 /*
  * $Id$
  *
  * This file is part of McIDAS-V
  *
  * Copyright 2007-2009
  * Space Science and Engineering Center (SSEC)
  * University of Wisconsin - Madison
  * 1225 W. Dayton Street, Madison, WI 53706, USA
  * http://www.ssec.wisc.edu/mcidas
  * 
  * All Rights Reserved
  * 
  * McIDAS-V is built on Unidata's IDV and SSEC's VisAD libraries, and
  * some McIDAS-V source code is based on IDV and VisAD source code.  
  * 
  * McIDAS-V is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * McIDAS-V is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser Public License
  * along with this program.  If not, see http://www.gnu.org/licenses.
  */
 
 package edu.wisc.ssec.mcidasv;
 
 import static edu.wisc.ssec.mcidasv.Constants.PROP_BUILD_DATE;
 import static edu.wisc.ssec.mcidasv.Constants.PROP_VERSION_MAJOR;
 import static edu.wisc.ssec.mcidasv.Constants.PROP_VERSION_MINOR;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import ucar.unidata.idv.IntegratedDataViewer;
 import ucar.unidata.util.IOUtil;
 import ucar.unidata.util.Misc;
 import ucar.unidata.util.StringUtil;
 
 public class StateManager extends ucar.unidata.idv.StateManager implements Constants, HyperlinkListener {
 	
 	private String version;
 	private String versionAbout;
 	
 	/** action listener */
 	private ActionListener actionListener;
 	
 	public StateManager(IntegratedDataViewer idv) {
 		super(idv);
 		actionListener = getIdv();
 	}
 	
 	/**
 	 * Handle a change to a link
 	 *
 	 * @param e  the link's event
 	 */
 	public void hyperlinkUpdate(HyperlinkEvent e) {
 		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 			if (e.getURL() == null) {
 				click(e.getDescription());
 			} else {
 				click(e.getURL().toString());
 			}
 		}
 	}
 	
 	/**
 	 * Handle a click on a link
 	 *
 	 * @param url  the link definition
 	 */
 	public void click(String url) {
 		actionListener.actionPerformed(new ActionEvent(this, 0, url));
 	}
 	
 	public String getOSName() {
 		String os = System.getProperty("os.name");
 		os = os.replaceAll(" ", "_");
 		return os;
 	}
 	
 	public String getMcIdasVersionAbout() {
 		
 		getMcIdasVersion();
         
         versionAbout = IOUtil.readContents((String) getProperty(Constants.PROP_ABOUTTEXT), "");
         versionAbout = StringUtil.replace(versionAbout, MACRO_VERSION, version);
         Properties props = Misc.readProperties(
         	(String) getProperty(Constants.PROP_VERSIONFILE), 
         	null, 
         	getClass()
         );
         
         String value = getIdvVersion();
         versionAbout = StringUtil.replace(versionAbout, Constants.MACRO_IDV_VERSION, value);
         value = props.getProperty(PROP_COPYRIGHT_YEAR, "");
         versionAbout = StringUtil.replace(versionAbout, Constants.MACRO_COPYRIGHT_YEAR, value);
         value = props.getProperty(PROP_BUILD_DATE, "Unknown");
         versionAbout = StringUtil.replace(versionAbout, Constants.MACRO_BUILDDATE, value);
        
 		return versionAbout;
 	}

 	public String getMcIdasVersion() {
 		if (version != null) {
 			return version;
 		}
 		
 		Properties props = new Properties();
 		props = Misc.readProperties((String) getProperty(Constants.PROP_VERSIONFILE), null, getClass());
 		String maj = props.getProperty(PROP_VERSION_MAJOR, "0");
 		String min = props.getProperty(PROP_VERSION_MINOR, "0");
 		String rel = props.getProperty(PROP_VERSION_RELEASE, "");
 		
 		version = maj.concat(".").concat(min).concat(rel);
 		
 		return version;
 	}
 
     /**
     * Get a property
     *
     * @param name name of the property
     *
     * @return  the property or null
     */
    @Override public Object getProperty(final String name) {
        Object value = null;
        if (McIDASV.isMac())
            value = getProperties().get("mac."+name);

        if (value == null) 
            value = getProperties().get(name);

        if (value == null) {
            String fixedName = StateManager.fixIds(name);
            if (!name.equals(fixedName))
                return getProperties().get(fixedName);
        }
        return value;
    }

    /**
      * Returns information about the current version of McIDAS-V and the IDV,
      * along with their respective build dates.
      * 
      * @return Hashtable containing versioning information.
      */
     public Hashtable<String, String> getVersionInfo() {
         Properties props = new Properties();
         props = Misc.readProperties((String) getProperty(Constants.PROP_VERSIONFILE), null, getClass());
 
         String mcvBuild = props.getProperty(PROP_BUILD_DATE, "Unknown");
 
         Hashtable<String, String> table = new Hashtable<String, String>();
         table.put("mcv.version.general", getMcIdasVersion());
         table.put("mcv.version.build", mcvBuild);
         table.put("idv.version.general", getVersion());
         table.put("idv.version.build", getBuildDate());
         return table;
     }
 
 	public String getIdvVersion() {
 		return getVersion();
 	}
 	
 	/**
 	 * Overridden to get dir of the unnecessary second level directory.
 	 * 
 	 * @see ucar.unidata.idv.StateManager#getStoreName()
 	 */
 	public String getStoreName() {
 		return "";
 	}
 	
 	/**
 	 * Connect to McIDAS website and look for latest version
 	 */
 	public String getMcIdasVersionLatest() {
 		String version = "";
 		try {
 			version = IOUtil.readContents(Constants.HOMEPAGE_URL + "/" + Constants.VERSION_URL + "?requesting=" + getMcIdasVersion() + "&os=" + getOSName(), "");
 		} catch (Exception e) {}
 		return version.trim();
 	}
 
 	/**
 	 * Connect to McIDAS website and look for latest notice
 	 */
 	public String getNoticeLatest() {
 		String notice = "";
 		try {
 			notice = IOUtil.readContents(Constants.HOMEPAGE_URL + "/" + Constants.NOTICE_URL + "?requesting=" + getMcIdasVersion() + "&os=" + getOSName(), "");
 		} catch (Exception e) {}
 		return notice.trim();
 	}
 
 	/**
 	 * Compare version strings
 	 *  0: equal
 	 * <0: this version is greater
 	 * >0: that version is greater
 	 */
 	private int compareVersions(String thisVersion, String thatVersion) {
 		int thisInt = versionToInteger(thisVersion);
 		int thatInt = versionToInteger(thatVersion);
 		return (thatInt - thisInt);
 	}
 	
 	/**
 	 * Turn version strings of the form #.#(a#)
 	 *  where # is one or two digits, a is one of alpha or beta, and () is optional
 	 * Into an integer... (empty) > beta > alpha
 	 */
 	private int versionToInteger(String version) {
 		int value = 0;
 		int p;
 		String part;
 		Character one = null;
 		
 		try {
 			
 			// Major version
 			p = version.indexOf('.');
 			if (p > 0) {
 				part = version.substring(0,p);
 				value += Integer.parseInt(part) * 1000000;
 				version = version.substring(p+1);
 			}
 			
 			// Minor version
 			int i=0;
 			for (i=0; i<2 && i<version.length(); i++) {
 				one = version.charAt(i);
 				if (Character.isDigit(one)) {
 					if (i==0) value += Character.digit(one, 10) * 100000;
 					else value += Character.digit(one, 10) * 10000;
 				}
 				else {
 					break;
 				}
 			}
 			if (one!=null) version = version.substring(i);
 	
 			// Alpha/beta status
 			if (version.length() == 0) value += 300;
 			else if (version.charAt(0) == 'b') value += 200;
 			else if (version.charAt(0) == 'a') value += 100;
 			for (i=0; i<version.length(); i++) {
 				one = version.charAt(i);
 				if (Character.isDigit(one)) break;
 			}
 			if (one!=null) version = version.substring(i);
 	
 			// Alpha/beta version
 			if (version.length() > 0)
 				value += Integer.parseInt(version);
 			
 		} catch (Exception e) {}
 
 		return value;
 	}
 	
 	public void checkForNewerVersion(boolean notifyDialog) {
 		
 		/** Shortcut this whole process if we are processing offscreen */
 		if (super.getIdv().getArgsManager().getIsOffScreen())
 			return;
 
 		String thisVersion = getMcIdasVersion();
 		String thatVersion = getMcIdasVersionLatest();
 		String titleText = "Version Check";
 		
 		if (thisVersion.equals("") || thatVersion.equals("")) {
 			if (notifyDialog) {
 				JOptionPane.showMessageDialog(null, "Version check failed", titleText, 
 						JOptionPane.WARNING_MESSAGE);
 			}
 		}
 		else if (compareVersions(thisVersion, thatVersion) > 0) {
 			String labelText = "<html>Version <b>" + thatVersion + "</b> is available<br><br>";
 			labelText += "Visit <a href=\"" + Constants.HOMEPAGE_URL + "\">";
 			labelText += Constants.HOMEPAGE_URL + "</a> to download</html>";
 			
 			JPanel backgroundColorGetterPanel = new JPanel();
 			JEditorPane messageText = new JEditorPane("text/html", labelText);
 			messageText.setBackground(backgroundColorGetterPanel.getBackground());
 			messageText.setEditable(false);
 			messageText.addHyperlinkListener(this);
 
 //			JLabel message = new JLabel(labelText, JLabel.CENTER);
 			JOptionPane.showMessageDialog(null, messageText, titleText, 
 					JOptionPane.INFORMATION_MESSAGE);
 		}
 		else {
 			if (notifyDialog) {
 				String labelText = "<html>This version (<b>" + thisVersion + "</b>) is up to date</html>";
 				JLabel message = new JLabel(labelText, JLabel.CENTER);
 				JOptionPane.showMessageDialog(null, message, titleText, 
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 		}
 		
 	}
 	
 	public void checkForNotice(boolean notifyDialog) {
 		
 		/** Shortcut this whole process if we are processing offscreen */
 		if (super.getIdv().getArgsManager().getIsOffScreen())
 			return;
 
 		String thisNotice = getNoticeCached().trim();
 		String thatNotice = getNoticeLatest().trim();
 		String titleText = "New Notice";
 		String labelText = thatNotice;
 		
 		if (thatNotice.equals("")) {
 			setNoticeCached(thatNotice);
 			if (notifyDialog) {
 				titleText = "No Notice";
 				JLabel message = new JLabel("There is no current notice", JLabel.CENTER);
 				JOptionPane.showMessageDialog(null, message, titleText, 
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 			return;
 		}
 		else if (!thisNotice.equals(thatNotice)) {
 			setNoticeCached(thatNotice);
 			
 			JPanel backgroundColorGetterPanel = new JPanel();
 			JEditorPane messageText = new JEditorPane("text/html", labelText);
 			messageText.setBackground(backgroundColorGetterPanel.getBackground());
 			messageText.setEditable(false);
 			messageText.addHyperlinkListener(this);
 
 //			JLabel message = new JLabel(labelText, JLabel.CENTER);
 			JOptionPane.showMessageDialog(null, messageText, titleText, 
 					JOptionPane.INFORMATION_MESSAGE);
 		}
 		else {
 			if (notifyDialog) {
 				titleText = "Previous Notice";
 				JLabel message = new JLabel(labelText, JLabel.CENTER);
 				JOptionPane.showMessageDialog(null, message, titleText, 
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 		}
 		
 	}
 	
 	private String getNoticePath() {
         String noticePath = System.getProperty("user.home");
         if (System.getProperty("os.name", "").startsWith("Windows"))
         	noticePath += "\\.mcidasv\\notice.txt";
         else
         	noticePath += "/.mcidasv/notice.txt";
         return noticePath;
 	}
 
 	//TODO: change the hardcoded .mcidasv directories
 	private String getNoticeCached() {
 	    String notice = "";
 		try{
 			FileReader fstream = new FileReader(getNoticePath());
 			BufferedReader in = new BufferedReader(fstream);
 		    String line;
 		    while ((line = in.readLine()) != null) {
 		    	notice += line + "\n";
 		    }
 			in.close();
 		} catch (Exception e){
 			System.err.println("Error: " + e.getMessage());
 		}
 		return notice;
 	}
 	
 	private void setNoticeCached(String notice) {
 		try{
 			FileWriter fstream = new FileWriter(getNoticePath());
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(notice);
 			out.close();
 		} catch (Exception e){
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 	
 }
