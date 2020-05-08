 import java.io.*;
 import java.util.*;
 
 public class FILE
 {
     private File f;
 
     public FILE(String pathname)
     {
         f=new File(pathname);
     }
     
     public long getSize(){return f.length();}
     public long getDate(){return f.lastModified();}
     public String getName(){return f.getName();}
     public boolean isHidden(){return f.isHidden();} 
     public boolean isVisible(){return !f.isHidden();} 
     public boolean exists(){return f.isFile();}
     
     public byte[] getBytes(){return getBytes(false);};
     public byte[] getBytes(boolean gzip){return getBytes(0, this.getSize()-1, gzip);};
     public byte[] getBytes(long from, long to){return getBytes(from, to, false);};
     public byte[] getBytes(long from, long to, boolean gzip){
         if( this.getSize()==0 )
             return null;
         if( !f.isFile() || f.isDirectory() || from>to || this.getSize()-1<to ){
             LOG.write("File Error! "+this.getName()+";from:"+from+";to:"+to+";", 700);
             return null;
         }
         if((to+1)-from > Integer.MAX_VALUE){
             LOG.write("File section to big! "+this.getName()+";from:"+from+";to:"+to+";", 701);
             return null;
         }
         
        byte[] out=null;
         FileInputStream fis=null;
         
         try{
             fis = new FileInputStream(f);
             fis.skip(from);
             byte[] cbuf=new byte[((int) ((to+1)-from))]; //! MAX OF 2.147.483.647 Bytes!
             int red = fis.read(cbuf);
             out=cbuf;
         }catch ( FileNotFoundException e ){
                 System.err.println( "Datei gibt’s nicht!" );
         }catch ( Exception e ){
                 LOG.write("Failed to read file section! "+this.getName()+";from:"+from+";to:"+to+";", 703);
         }finally{
             try{fis.close();}catch(Exception e){;}
         }
         
         if( out!=null && gzip )
             out=(new GZIP().compress(out));
         
        try{
            System.out.println("file to byte array! "+this.getName()+"; from:"+from+"; to:"+to+"; expect:"+((to+1)-from)+"; real:"+out.length+";");
        }catch(Exception e){;}
         return out;
     }
 }
