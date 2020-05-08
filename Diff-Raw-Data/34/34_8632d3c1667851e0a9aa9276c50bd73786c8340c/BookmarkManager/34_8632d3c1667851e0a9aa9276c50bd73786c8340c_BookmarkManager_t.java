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
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.jvifm.util.HomeLocator;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 
 public class BookmarkManager {
 
 	private static BookmarkManager instance = null;
 	private String storePath = null;
 	private ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
 	private ArrayList<BookmarkListener> listeners = new ArrayList<BookmarkListener>();
 	private HashMap<String, Bookmark> bookmarkMap = new HashMap<String,Bookmark>();
 
 	private BookmarkManager() {
 		init();
 	}
 
 	public void addListener(BookmarkListener listener) {
 		this.listeners.add(listener);
 	}
 
 	public void removeListener(BookmarkListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	public void notifyAddBookmark(Bookmark bookmark) {
 		for (BookmarkListener listener : listeners) {
 			listener.onAddBookmark(bookmark);
 		}
 	}
 
 	public void notifyChangeBookmark(String key, Bookmark bookmark) {
 		for (BookmarkListener listener : listeners) {
 			listener.onChangeBookmark(key, bookmark);
 		}
 	}
 
 	public static BookmarkManager getInstance() {
 		if (instance == null)
 			instance = new BookmarkManager();
 		return instance;
 	}
 
 	public Iterator<Bookmark> iterator() {
 		return bookmarkList.iterator();
 	}
 
 	public List<Bookmark> getAll() {
 		ArrayList<Bookmark> result = new ArrayList<Bookmark>();
 		for (Bookmark bm : bookmarkList) {
 			result.add(bm);
 		}
 		return result;
 	}
 
 	public Bookmark getBookmark(String key) {
 		return bookmarkMap.get(key);
 	}
 
 	public void add(Bookmark bm) {
 
        //bookmark has an empty name or in driver root path
        if (bm.getName().trim().equals("") || bm.getPath().trim().endsWith(":")) {
            return;
        }
        for (Iterator it = bookmarkList.iterator(); it.hasNext();) {
            Bookmark tempBm = (Bookmark) it.next();
            //bookmark exists
            if (tempBm.getName().equals(bm.getName())) return; 
        }

 		boolean keyExists = false;
 		if (bm.getKey() != null) {
 			if (bookmarkMap.keySet().contains(bm.getKey())) {
 				Bookmark oldBookmark = (Bookmark) bookmarkMap.get(bm.getKey());
 				bookmarkList.remove(oldBookmark);
 				keyExists = true;
 			}
 			bookmarkMap.put(bm.getKey(), bm);
 		}
 

 		bookmarkList.add(bm);
 		if (keyExists) {
 			notifyChangeBookmark(bm.getKey(), bm);
 		} else {
 			notifyAddBookmark(bm);
 		}
        store();
 
 	}
 
 	public void changeBookmarkKey(String oldKey, String newKey, Bookmark bm) {
 		if (oldKey != null) bookmarkMap.remove(oldKey);
 		
 		bookmarkMap.put(newKey, bm);
 
 	}
 
 	public void remove(Bookmark bm) {
 		bookmarkMap.values().remove(bm);
 		bookmarkList.remove(bm);
        store();
 	}
 
 	public void removeByKey(String key) {
 		if (key == null)
 			return;
 		if (bookmarkMap.keySet().contains(key)) {
 			Bookmark bm = (Bookmark) bookmarkMap.get(key);
 			bookmarkList.remove(bm);
 			bookmarkMap.remove(key);
 		}
        store();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void init() {
 
 		storePath = HomeLocator.getConfigHome() + File.separator
 				+ "bookmarks.xml";
 		SAXReader saxReader = new SAXReader();
 		File file = new File(storePath);
 		Document document = null;
 
 		try {
 
 			if (file.exists()) {
 				FileInputStream is = new FileInputStream(file);
 				InputStreamReader reader = new InputStreamReader(is, "UTF-8");
 				BufferedReader br = new BufferedReader(reader);
 				document = saxReader.read(br);
 
 				List elementList = document.selectNodes("//bookmark");
 
 				for (Iterator it = elementList.iterator(); it.hasNext();) {
 
 					Element bookmarkElement = (Element) it.next();
 					String name = bookmarkElement.elementText("name");
 					String path = bookmarkElement.elementText("path");
 					String key = bookmarkElement.elementText("key");
 
 					Bookmark bookmark = new Bookmark();
 					bookmark.setPath(path);
 					bookmark.setName(name);
 					bookmark.setKey(key);
 
 					if (key != null) {
 						bookmarkMap.put(key, bookmark);
 					}
 
 					bookmarkList.add(bookmark);
 				}
 
 				reader.close();
 				is.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public void store() {
 
 		try {
 
 			Document document = DocumentHelper.createDocument();
 			Element root = document.addElement("bookmarks");
 
 			for (Iterator it = bookmarkList.iterator(); it.hasNext();) {
 
 				Bookmark bm = (Bookmark) it.next();
 
 				Element bookmarkElement = root.addElement("bookmark");
 
 				bookmarkElement.addElement("name").addText(bm.getName());
 				bookmarkElement.addElement("path").addText(bm.getPath());
 				if (bm.getKey() != null)
 					bookmarkElement.addElement("key").addText(bm.getKey());
 
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
