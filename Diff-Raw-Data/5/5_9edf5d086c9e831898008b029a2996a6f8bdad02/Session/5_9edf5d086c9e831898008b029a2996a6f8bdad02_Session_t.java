 package model;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Observable;
 
 import model.Mail.MailType;
 
 public class Session extends Observable
 {
     private Mailbox mailbox;
 
     /**
      * Creates the htmledit session, and launches the GUI
      */
     public ArrayList<HTMLBuffer> bufferList;
     public ArrayList<Integer> indents;
     private Mediator mediator;
     private HTMLBuffer current;
     private boolean autoIndent;
     private boolean autoWrap;
 
     /**
      * Creates a session, given the mailbox
      * 
      * @param mailbox
      *            the mailbox session can send mail to
      */
     public Session(Mailbox mailbox)
     {
         this.mailbox = mailbox;
 
         bufferList = new ArrayList<HTMLBuffer>();
         mediator = null;
 
         autoIndent = false;
         autoWrap = false;
     }
     /**
      * Opens a default file or a new file
      * @param file - the file to open
      */
     public void setInitialFile(File file){
         if(file == null){
             newFile();
            setChanged();
            notifyObservers();
         }else{
             open(file);
            setChanged();
            notifyObservers();
         }
     }
 
     public void setMediator(Mediator mediator)
     {
         this.mediator = mediator;
     }
 
     /**
      * @param args
      */
     public void setAutoIndent()
     {
         this.autoIndent = !autoIndent;
     }
 
     public void open(File file)
     {
         if (file != null)
         {
             bufferList.add(new HTMLBuffer(file, 80, mediator, mailbox));
             this.setChanged();
             this.notifyObservers();
         }
     }
 
     public void setAutoWrap()
     {
         this.autoWrap = !autoWrap;
     }
 
     public void setIndentSpacing(int level, int spaces)
     {
         indents.set(level, spaces);
     }
 
     public void saveAll()
     {
         for (HTMLBuffer buffer : bufferList)
         {
             if (!buffer.save())
             {
                 String memo = "This document is not well formed: save anyway?";
                 Mail m = new Mail(MailType.WELL_FORMED_ERROR, buffer, memo);
 
                 mailbox.addMail(m);
             }
         }
     }
 
     public HTMLBuffer getCurrentBuffer()
     {
         return current;
     }
 
     public void close()
     {
         for (HTMLBuffer buffer : bufferList)
         {
             if (!buffer.getChanged())
             {
                 this.close(buffer);
             }
             else
             {
                 String memo = "This document has unsaved changes: close anyway?";
                 Mail m = new Mail(MailType.UNSAVED_CHANGES, buffer, memo);
 
                 mailbox.addMail(m);
             }
         }
     }
 
     public void close(HTMLBuffer buffer)
     {
         bufferList.remove(buffer);
         this.setChanged();
         this.notifyObservers();
         if (bufferList.isEmpty())
         {
             System.exit(0);
         }
     }
 
     public void newFile()
     {
         this.open(new File("New Document"));
         this.setChanged();
         this.notifyObservers();
     }
 
     public void setCurrentTab(HTMLBuffer buffer)
     {
         current = buffer;
     }
 }
