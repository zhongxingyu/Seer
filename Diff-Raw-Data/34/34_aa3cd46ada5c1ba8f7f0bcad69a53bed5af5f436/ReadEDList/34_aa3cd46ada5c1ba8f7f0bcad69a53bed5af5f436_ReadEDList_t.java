 package ru.sabstest;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class ReadEDList {
 	public List<ReadED> pList;
 
 
 
 	public ReadEDList()
 	{
 		pList = new ArrayList<ReadED>();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((pList == null) ? 0 : pList.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ReadEDList other = (ReadEDList) obj;
 		if (pList == null) {
 			if (other.pList != null)
 				return false;
 		} else if (!pList.equals(other.pList))
 			return false;
 		return true;
 	}
 
 	
 	public ReadED get(int i)
 	{
 		return pList.get(i);
 	}
 
 	public int getSize()
 	{
 		return pList.size();
 	}
 	
 	public void readFolderByFilter(String fld, FileFilter filter)
 	{
 		File[] files = new File(fld).listFiles(filter);
 
 		for(File fl:files)
 		{
 
 			Element root = XML.getXMLRootElement(fl.getAbsolutePath());
 			String type = root.getElementsByTagNameNS("*", "DocType").item(0).getTextContent();
 
 			ReadED p = Packet.createReadEDByFile(type);			
 
 			if(p != null)
 			{
 				if(type.equals("PacketEPDVER_B"))
 					p.readEncodedFile(fl, true);
 				else
 					p.readEncodedFile(fl, false);			
 				pList.add(p);
 			}
 		}
 		Collections.sort(pList);
 	}
 	
 	public void readFolder(String fld)
 	{
 		FileFilter filter = new FileFilter() {			
 			@Override
 			public boolean accept(File pathname) {
 				if(!pathname.getName().startsWith("deltadb"))
 					return true;
 				else
 					return false;
 			}
 		};	
 		readFolderByFilter(fld, filter);
 	}
 	
 	public void readFolderByType(String fld, final String type)
 	{
 		FileFilter filter = new FileFilter() {			
 			@Override
 			public boolean accept(File pathname) {
				if(pathname.getName().endsWith(type))
 					return true;
 				else
 					return false;
 			}
 		};	
 		readFolderByFilter(fld, filter);
 	}
 }
 
