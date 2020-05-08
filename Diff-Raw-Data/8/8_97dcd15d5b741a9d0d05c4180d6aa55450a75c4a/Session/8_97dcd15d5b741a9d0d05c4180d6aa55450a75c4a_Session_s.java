 package model;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Observable;
 
 import view.EditorView;
 
 public class Session extends Observable
 {
     private Mailbox mailbox;
 
     /**
      * Creates the htmledit session, and launches the GUI
      */
     public ArrayList<HTMLBuffer> bufferList;
     public ArrayList<Integer> indents;
     private Mediator mediator;
 
     private boolean autoIndent;
     private boolean autoWrap;
 
     /**
      * Creates a session, given the mailbox
      * 
      * @param mailbox
      *        the mailbox session can send mail to
      */
     public Session(Mailbox mailbox)
     {
         this.mailbox = mailbox;
 
         bufferList = new ArrayList<HTMLBuffer>();
         mediator = null;
 
         autoIndent = false;
         autoWrap = false;
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
        bufferList.add(new HTMLBuffer(file, 80, mediator, mailbox));
        this.setChanged();
        this.notifyObservers();
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
                 mailbox.addMessage(new Mail(EditorView.class,this.getClass(),"Not Well Formed","The document is not well formed, are you sure you want to save?"));
             }
         }
     }
 
     public HTMLBuffer getCurrentBuffer()
     {
         return null;
     }
 
     public void close()
     {
         for (HTMLBuffer buffer : bufferList)
         {
             if (buffer.isWellFormed())
             {
                 this.close(buffer);
             }
             else
             {
                 mailbox.addMessage(new Mail(EditorView.class,this.getClass(),"Unsaved Changes","The document has unsaved changes. Do you want to close anyway?"));
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
 }
