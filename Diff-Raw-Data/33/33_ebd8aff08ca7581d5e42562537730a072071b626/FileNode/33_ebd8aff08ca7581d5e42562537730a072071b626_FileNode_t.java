 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.demo.filebrowser;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.demo.fileservice.BrowserFile;
 import org.demo.fileservice.Folder;
 import org.openide.nodes.AbstractNode;
 import org.openide.nodes.ChildFactory;
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
import org.openide.util.Lookup;
 import org.openide.util.lookup.Lookups;
 
 /**
  *
  * @author edwin
  */
 public class FileNode extends AbstractNode {
 
    private BrowserFile key=null;
    
    public FileNode(Children children) {
        super(children);
    }    
     
     public FileNode(BrowserFile key) {
         super(Children.LEAF, Lookups.singleton(key));
         this.key=key;
     }
 
     private static final class CustomFileFilter implements FileFilter {
 
         @Override
         public boolean accept(File pathname) {
             return pathname.isFile();
         }
     };
 
     public static class FileNodeChildren extends ChildFactory<BrowserFile> {
 
         private List<BrowserFile> src = null;
 
         public FileNodeChildren(Folder src) {
             if (src != null) {
                 final File[] listFiles = src.listFiles(new CustomFileFilter());
                 if (listFiles != null) {
                     this.src=new ArrayList<BrowserFile>();
                     for (File file : listFiles) {
                         this.src.add(new BrowserFile(file));
                     }
                 }
             }
         }
 
         @Override
         protected boolean createKeys(List<BrowserFile> toPopulate) {
             if (src != null) {
                 toPopulate.addAll(src);
             }
             return true;
         }
 
         @Override
         protected Node createNodeForKey(BrowserFile key) {
             Node node = new FileNode(key);
             node.setDisplayName(key.getName());
             node.setShortDescription(key.getName());
 
             return node;
         }
     }
 }
