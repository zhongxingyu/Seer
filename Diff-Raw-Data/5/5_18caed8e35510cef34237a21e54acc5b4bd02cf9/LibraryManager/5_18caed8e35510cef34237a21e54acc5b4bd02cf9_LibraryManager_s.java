 /**
  * Manages the lifetime of Library objects
  */
 package jbookmanager.controller;
 
 import jbookmanager.model.*;
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.DecimalFormat;
 import java.util.Currency;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 /**
  *
  * @author uli
  */
 public abstract class LibraryManager
 {
 
     /**
      * This represents the library opened at this time
      */
     public static Library library = new Library();
    public static DecimalFormat currencyFormat = new DecimalFormat("#######0.00 " + Currency.getInstance(Locale.
            getDefault()));
 
     public static Library readLibrary(String file) throws FileNotFoundException
     {
         InputStream fin = null;
         try
         {
             fin = new GZIPInputStream(new FileInputStream(file));
             XMLDecoder dec = new XMLDecoder(fin);
             dec.close();
             return (Library) dec.readObject();
         }
         catch (IOException ex)
         {
             Logger.getLogger(LibraryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         finally
         {
             try
             {
                 fin.close();
             }
             catch (IOException ex)
             {
                 Logger.getLogger(LibraryManager.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return null;
     }
 
     public static void writeLibrary(Library lib, String file)
     {
         OutputStream fout = null;
         try
         {
             fout = new GZIPOutputStream(new FileOutputStream(file));
             XMLEncoder enc = new XMLEncoder(fout);
             enc.writeObject(lib);
             enc.close();
         }
         catch (IOException ex)
         {
             Logger.getLogger(LibraryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         finally
         {
             try
             {
                 fout.close();
             }
             catch (IOException ex)
             {
                 Logger.getLogger(LibraryManager.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
