 package edu.exigen.client;
 
 import edu.exigen.client.gui.LibraryClientComponent;
 import edu.exigen.server.ProvidersHolder;
 import edu.exigen.server.provider.BookProvider;
 import edu.exigen.server.provider.ReaderProvider;
 import edu.exigen.server.provider.ReservationRecordProvider;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.swing.*;
 import java.rmi.RemoteException;
 
 /**
  * @author Tedikova O.
  * @version 1.0
  */
 public class LibraryClient {
 
     private static final String BOOK_PROVIDER_URL = "rmi://localhost/book_provider";
     private static final String READER_PROVIDER_URL = "rmi://localhost/reader_provider";
     private static final String RECORD_PROVIDER_URL = "rmi://localhost/record_provider";
 
    private ProvidersHolder providersHolder;
 
    public LibraryClient(ProvidersHolder providersHolder) {
        this.providersHolder = providersHolder;
     }
 
     public static void main(String[] args) {
         Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
             @Override
             public void uncaughtException(Thread t, Throwable e) {
                 e.printStackTrace();
                 if (e.getMessage().contains("java.net.ConnectException")) {
                     JOptionPane.showMessageDialog(null, "Client was disconnected, please, check server.", "Library client", JOptionPane.INFORMATION_MESSAGE);
                     try {
                         Context namingContext = new InitialContext();
                         BookProvider bookProvider = (BookProvider) namingContext.lookup(BOOK_PROVIDER_URL);
                         ReaderProvider readerProvider = (ReaderProvider) namingContext.lookup(READER_PROVIDER_URL);
                         ReservationRecordProvider recordProvider = (ReservationRecordProvider) namingContext.lookup(RECORD_PROVIDER_URL);
                        providersHolder = new ProvidersHolder(bookProvider, readerProvider, recordProvider);
                     } catch (NamingException ne) {
                        JOptionPane.showMessageDialog(null, "Can't find 'Library Server' at localhost.", "Library client", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(-1);
                     }
                 } else {
                     JOptionPane.showMessageDialog(null, e.getMessage(), "Library client", JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         });
         try {
             Context namingContext = new InitialContext();
             BookProvider bookProvider = (BookProvider) namingContext.lookup(BOOK_PROVIDER_URL);
             ReaderProvider readerProvider = (ReaderProvider) namingContext.lookup(READER_PROVIDER_URL);
             ReservationRecordProvider recordProvider = (ReservationRecordProvider) namingContext.lookup(RECORD_PROVIDER_URL);
            ProvidersHolder providersHolder = new ProvidersHolder(bookProvider, readerProvider, recordProvider);
            final LibraryClient libraryClient = new LibraryClient(providersHolder);
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     final LibraryClientComponent clientComponent;
                     try {
                         clientComponent = new LibraryClientComponent(libraryClient);
                         JFrame clientFrame = clientComponent.getLibraryClientFrame();
                         clientFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                         clientFrame.setLocationRelativeTo(null);
                         clientFrame.setVisible(true);
                     } catch (RemoteException e) {
                         JOptionPane.showMessageDialog(null, "Read server data failed.", "Library client", JOptionPane.INFORMATION_MESSAGE);
                         System.exit(-1);
                     }
                 }
             });
         } catch (NamingException e) {
             JOptionPane.showMessageDialog(null, "Can't find 'Library Server' at localhost.", "Library client", JOptionPane.INFORMATION_MESSAGE);
             System.exit(-1);
         }
     }
 
     public ProvidersHolder getProvidersHolder() {
         return providersHolder;
     }
 
     public void setProvidersHolder(ProvidersHolder providersHolder) {
         this.providersHolder = providersHolder;
     }
 }
