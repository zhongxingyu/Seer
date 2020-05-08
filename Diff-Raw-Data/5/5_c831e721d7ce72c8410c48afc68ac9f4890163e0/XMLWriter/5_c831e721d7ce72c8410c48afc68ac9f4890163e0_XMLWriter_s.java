 package com.bugfullabs.mapeditor;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamWriter;
 
 
 public class XMLWriter{
 	
 	XMLWriter(){	
 	}
 	
 	public static File writeXML(File file, Level level){
 		
 		
 		try {
 			
 			
 			FileOutputStream fos = new FileOutputStream(file);
 			XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(new OutputStreamWriter(fos, "utf-8"));
 			
 			out.writeStartDocument();
 
 			out.writeCharacters("\n");
 			
 			out.writeStartElement("level");
 
 			out.writeAttribute("id", Integer.toString(level.getId()));
 			out.writeAttribute("rows", Integer.toString(level.getHeight()));
 			out.writeAttribute("columns", Integer.toString(level.getWidth()));
 			out.writeAttribute("levelpackid", Integer.toString(level.getLevelPack()));
 			out.writeAttribute("texture", level.getTexture());
 			
 				out.writeCharacters("\n");
 			
				for(int i = 0; i <= level.getHeight();i++)
 				{
 					
 				out.writeStartElement("row");
 				out.writeAttribute("number", Integer.toString(i));
 				
 				out.writeCharacters("\n");
 				
					for(int j = 0; j <= level.getWidth(); j++){
 				
 					out.writeStartElement("item");
 					out.writeAttribute("column", Integer.toString(j));
 					out.writeAttribute("id", Integer.toString(level.getItem(j, i)));
 					out.writeEndElement();//</item>
 				
 					out.writeCharacters("\n");
 					
 					}
 					
 				out.writeEndElement();//</row>
 				
 				out.writeCharacters("\n");
 				
 				}
 				
 				
 				out.writeStartElement("players");
 				out.writeAttribute("number", Integer.toString(level.getNumberOfPlayers()));
 				
 				out.writeCharacters("\n");
 				
 				for(int i = 0; i < level.getNumberOfPlayers(); i++){
 				
 					out.writeStartElement("player");
 					out.writeAttribute("color", Integer.toString(level.getPlayer(i).getColor()));
 					out.writeAttribute("column", Integer.toString(level.getPlayer(i).getColumn()));
 					out.writeAttribute("row", Integer.toString(level.getPlayer(i).getRow()));
 					out.writeAttribute("id", Integer.toString(i));
 					out.writeAttribute("dir", Integer.toString(level.getPlayer(i).getDir()));	
 					out.writeEndElement();
 				
 					out.writeCharacters("\n");
 					
 				}
 					
 			out.writeEndElement();//</level>
 			
 			out.writeCharacters("\n");
 			
 			out.writeEndDocument();
 			
 		out.close();
 		
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return file;
 	}
 	
 	
 }
