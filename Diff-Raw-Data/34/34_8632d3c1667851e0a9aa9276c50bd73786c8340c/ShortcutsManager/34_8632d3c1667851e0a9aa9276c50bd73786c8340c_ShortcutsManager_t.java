 /*
  * Jvifm - Java vifm (File Manager with vi like key binding)
  *
  * Copyright (C) 2006 wsn <shrek.wang@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  */
 
 package net.sf.jvifm.model;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.jvifm.util.HomeLocator;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 
 public class ShortcutsManager {
 
 	private static ShortcutsManager instance = null;
 	private String storePath = null;
 	private List<Shortcut> shortcutsList = new ArrayList<Shortcut>();
 	private List<ShortcutsListener> listeners = new ArrayList<ShortcutsListener>();
 
 	private ShortcutsManager() {
 		init();
 	}
 
 	public void addListener(ShortcutsListener listener) {
 		this.listeners.add(listener);
 	}
 
 	public void removeListener(ShortcutsListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	public void notifyAddshortcuts(Shortcut shortcut) {
 		for (ShortcutsListener listener : listeners ) {
 			listener.onAddshortcut(shortcut);
 		}
 	}
 
 	public void notifyChangeshortcuts(Shortcut shortcut) {
 		for (ShortcutsListener listener : listeners ) {
 			listener.onChangeshortcut(shortcut);
 		}
 	}
 
 	public static ShortcutsManager getInstance() {
 		if (instance == null)
 			instance = new ShortcutsManager();
 		return instance;
 	}
 
 	public Iterator<Shortcut> iterator() {
 		return shortcutsList.iterator();
 	}
 
 	public boolean isShortCut(String cmd) {
 
 		for (Shortcut shortcut : shortcutsList ) {
 			if (shortcut.getName().equals(cmd))
 				return true;
 		}
 		return false;
 	}
 
 	public Shortcut findByName(String cmd) {
 
 		for (Shortcut shortcut : shortcutsList ) {
 			if (shortcut.getName().equals(cmd))
 				return shortcut;
 		}
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	public ArrayList getCmdNameList() {
 		ArrayList result = new ArrayList();
 		for (Iterator it = shortcutsList.iterator(); it.hasNext();) {
 			Shortcut command = (Shortcut) it.next();
 			result.add(command.getName());
 		}
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public ArrayList getAll() {
 
 		ArrayList result = new ArrayList();
 		for (Iterator it = shortcutsList.iterator(); it.hasNext();) {
 			Shortcut command = (Shortcut) it.next();
 			result.add(command);
 		}
 		return result;
 	}
 
 	public void add(Shortcut command) {

		for (Iterator it = shortcutsList.iterator(); it.hasNext();) {
			Shortcut tempCmd = (Shortcut) it.next();
            //shortcut exists
            if (tempCmd.getName().equals(command.getName())) return;
        }
 		shortcutsList.add(command);
 		notifyAddshortcuts(command);
        store();
 	}
 
 	public void remove(Shortcut shortcut) {
 		shortcutsList.remove(shortcut);
        store();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void init() {
 
 		storePath = HomeLocator.getConfigHome() + File.separator
 				+ "commands.xml";
 		SAXReader saxReader = new SAXReader();
 		File file = new File(storePath);
 		Document document = null;
 
 		try {
 
 			if (file.exists()) {
 				FileInputStream is = new FileInputStream(file);
 				InputStreamReader reader = new InputStreamReader(is, "UTF-8");
 				BufferedReader br = new BufferedReader(reader);
 				document = saxReader.read(br);
 
 				List elementList = document.selectNodes("//command");
 
 				for (Iterator it = elementList.iterator(); it.hasNext();) {
 
 					Element shortcutElement = (Element) it.next();
 					String name = shortcutElement.elementText("name");
 					String text = shortcutElement.elementText("text");
 
 					Shortcut shortcut = new Shortcut();
 					shortcut.setText(text);
 					shortcut.setName(name);
 
 					shortcutsList.add(shortcut);
 				}
 
 				reader.close();
 				is.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void store() {
 
 		try {
 
 			Document document = DocumentHelper.createDocument();
 			Element root = document.addElement("commands");
 			
 			for (Shortcut shortcut : shortcutsList ) {
 
 				Element shortcutElement = root.addElement("command");
 
 				shortcutElement.addElement("name").addText(shortcut.getName());
 				shortcutElement.addElement("text").addText(shortcut.getText());
 
 			}
 
 			FileOutputStream fos = new FileOutputStream(storePath);
 			OutputFormat outformat = OutputFormat.createPrettyPrint();
 
 			outformat.setEncoding("UTF-8");
 			BufferedOutputStream out = new BufferedOutputStream(fos);
 			XMLWriter writer = new XMLWriter(out, outformat);
 
 			writer.write(document);
 			writer.flush();
 			writer.close();
 			fos.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
