 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.laukvik.ftp;
 
 import com.enterprisedt.net.ftp.FTPFile;
 import com.enterprisedt.net.ftp.FileTransferClient;
 import java.util.Vector;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 import org.laukvik.cache.Cache;
 
 /**
  *
  * @author morten
  */
 public class FtpTreeModel implements TreeModel{
 
     Vector<TreeModelListener> listeners;
     FileTransferClient ftp;
     FTPFile root;
     String [] ignoreFiles = { ".", ".." };
     Cache cache;
     FtpServer server;
     
     public FtpTreeModel( FileTransferClient ftp ) {
         super();
         cache = new Cache( 300 );
         listeners = new Vector<TreeModelListener>();
         this.ftp = ftp;
         root = new FTPFile("/");
         root.setName("/");
         root.setDir( true );
     }
     
     public void setFtpServer( FtpServer server ){
         this.server = server;
         connect();
     }
     
    private static void log( Object message ){
        System.out.println( "FtpTreeModel: " + message );
    }
    
     private void connect(){
        if (server == null){
            return;
        }
        log( "root: " + server );
         root.setName( server.title );
         root.setDir( true );
         root.setPath( server.path );
         try{
             ftp.setRemotePort( server.port );
             ftp.setRemoteHost( server.host );
             ftp.setUserName( server.user );
             ftp.setPassword( server.password ); 
             ftp.connect();
             cache.clear(); 
             valueForPathChanged( new TreePath( root ), this );
         } catch(Exception e){
             e.printStackTrace();
         }
     }
     
     public synchronized FTPFile [] listFiles( FTPFile file ){
         FTPFile [] files = (FTPFile[]) cache.getItem( file );
         if (files == null){
             files = listFilesReal( file ); 
             cache.add( file, files );
         } else {
         }
         return files;
     }
     
     public synchronized FTPFile [] listFilesReal( FTPFile file ){
         if (!ftp.isConnected()){
             connect();
             return new FTPFile[ 0 ];
         }
         String newPath = file.getPath() + "/" + file.getName();
         if (file == root){
             newPath = "www/test";
         }
         System.out.println( "listing: " + file.getPath() + " " + file.getName()  + " - " + newPath );
         if (file == null){
             return new FTPFile[ 0 ];
         } else {
             FTPFile [] files = new FTPFile[ 0 ];
 
             try {
                 files = ftp.directoryList( newPath );
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             
             Vector<FTPFile> items = new Vector<FTPFile>();
             for (FTPFile f : files){
                 
                 
                 if (f.getName().equalsIgnoreCase(".") || f.getName().equalsIgnoreCase("..")){
                 } else {
                     items.add( f );
                 }
             }
             FTPFile[] arr = new FTPFile[ items.size() ];
             items.toArray( arr );
             return arr;
         }
     }
 
     public Object getRoot() {
         return root;
     }
 
     @Override
     public Object getChild(Object o, int i) {
         if (o == null){
             return null;
         } else {
             FTPFile [] files = listFiles( (FTPFile)o );
             return files[ i ];
         }
     }
     @Override
     public int getChildCount(Object o) {
         
         FTPFile [] files = listFiles( (FTPFile)o );
         return files.length;
 
     } 
 
     @Override
     public boolean isLeaf(Object o) {
         if (o instanceof FTPFile){
             return !((FTPFile) o).isDir();
         } else {
             return true;
         } 
     }
 
     @Override
     public void valueForPathChanged(TreePath path, Object o) {
         for (TreeModelListener l : listeners){
                 l.treeStructureChanged( new TreeModelEvent( this , path ) );
         }
     }
 
     @Override
     public int getIndexOfChild( Object parent, Object child ) {
         if (parent == null || child == null){
             return -1;
         } else {
             if (parent instanceof FTPFile){
                 FTPFile parentFile = (FTPFile) parent;
                 FTPFile childFile = (FTPFile) child;
                 
                 FTPFile [] files = listFiles( (FTPFile)parentFile );
                 for (int x=0; x<files.length; x++){
                     FTPFile f = files[ x ];
                     if (childFile.getPath().equalsIgnoreCase( f.getPath() )){
                         return x;
                     }
                 }
                 return -1;
             } else {
                 return -1;
             }
         }
     }
 
     @Override
     public void addTreeModelListener(TreeModelListener tl) {
         listeners.add( tl );
     }
 
     @Override
     public void removeTreeModelListener(TreeModelListener tl) {
         listeners.remove( tl );
     }
     
 }
