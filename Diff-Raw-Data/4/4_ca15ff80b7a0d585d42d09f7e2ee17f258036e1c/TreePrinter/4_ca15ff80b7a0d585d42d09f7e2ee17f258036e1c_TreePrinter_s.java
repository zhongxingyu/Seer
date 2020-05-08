 /*
  * TreePrinter.java
  *
  * created at 2013-08-16 by Bernd Eckenfels <b.eckenfels@seeburger.de>
  *
  * Copyright (c) SEEBURGER AG, Germany. All Rights Reserved. TODO
  */
 package com.seeburger.vfs2.util;
 
 import java.io.PrintStream;
 import java.util.Date;
 
 import org.apache.commons.vfs2.FileContent;
 import org.apache.commons.vfs2.FileObject;
 import org.apache.commons.vfs2.FileSystemException;
 import org.apache.commons.vfs2.FileType;
 
 
 /**
  * Utility Functions for printing a VFS2 Tree.
  */
 public class TreePrinter
 {
     /**
      * Print the content of file and its childs to the given out writer.
      */
     public static void printTree(FileObject file, String prefix, PrintStream out) throws FileSystemException
     {
         String type = "";
         if (file.isHidden())
             type+="H";
         else
             type+=".";
         if (file.isReadable())
             type+="R";
         else
             type+=".";
         if (file.isWriteable())
             type+="W";
         else
             type+=".";
         type+=")";
         FileContent content = file.getContent();
         if (content != null)
         {
             try { type += " date=" + new Date(content.getLastModifiedTime()); }catch (Exception ig) { }
             try { type += " size=" + content.getSize();  }catch (Exception ig) { }
             try { type += " att=" + content.getAttributes();  }catch (Exception ig) { }
         }
         String fileName = file.getName().getPathDecoded();
 
         if (file.getType().hasChildren())
         {
             FileObject[] children = null;
             try
             {
                 children = file.getChildren();
                 out.println(prefix + fileName + " (d" + type);
             }
             catch (FileSystemException ignored)
             {
                 out.println(prefix + fileName + " (d"+type + " (" + ignored + ")");
             }
             if (children != null)
             {
                 for(FileObject fo : children)
                 {
                     printTree(fo, prefix + "  ", out);
                 }
             }
         }
         else if (file.getType() == FileType.FILE)
         {
             out.println(prefix + fileName + " (." + type);
         }
         else
         {
             out.println(prefix + fileName + " (-" + type);
         }
     }
 
 }
 
 
 
