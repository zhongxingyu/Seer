 package model;
 
 import java.io.File;
 import java.util.ArrayList;
 import model.Mail.MailType;
 
 /**
  * The mediator is the single point of access for model functionality the
  * commands may wish to use
  * 
  * @author Jeremy Vasta
  */
 public class Mediator
 {
     /**
      * The mediator's reference to the application session
      */
     private Session session;
     
     private Outliner outliner;
 
     private Mailbox editorMailbox;
 
     /**
      * Constructs the mediator given the mailbox reference
      * 
      * @param mainMailbox
      *        the mediator can use to communicate back to the view
      * @param editorMailbox 
      */
     public Mediator(Mailbox mainMailbox, Mailbox editorMailbox)
     {
         this.editorMailbox = editorMailbox;
     }
 
     /**
      * Give the mediator a reference to the application session
      * 
      * @param session
      */
     public void setSession(Session session)
     {
         this.session = session;
     }
     
     public void setOutliner(Outliner outliner)
     {
         this.outliner = outliner;
     }
 
     public void setAutoIndent()
     {
         session.setAutoIndent();
     }
     
     public void forceClose(HTMLBuffer buf)
     {
         session.markToForceClose(buf);
     }
 
     /**
      * Checks for buffer changes, allows the GUI to respond to any errors, and
      * then causes the session to close out the buffer
      * 
      * @param buf
      *        the buffer to close
      */
     public void close(HTMLBuffer buf)
     {
         if (!buf.getChanged())
         {
             session.close(buf);
         }
         else
         {
             String name = buf.getFile().getName() + " ";
             
             String memo = name + "has unsaved changes: close anyway?";
             Mail m = new Mail(MailType.UNSAVED_CHANGES, buf, memo);
 
             editorMailbox.addMail(m);
             
             session.commitForceClose();
         }
     }
 
     /**
      * Causes the session to iterate through its buffers and close each
      * individually
      */
     public void closeAll()
     {
         session.closeAll();
     }
 
     /**
      * Indents the current buffer
      * 
      * @param full
      *        Whether to indent the whole buffer, or just the selected component
      *        of the buffer
      */
     public void indent(boolean full)
     {
         session.getCurrentBuffer().indent(full);
     }
 
     /**
      * Insert the given HTML text into the buffer at the current position
      * 
      * @param html
      *        the HTML text to insert
      */
     public void insert(String html)
     {
         session.getCurrentBuffer().insert(html);
     }
 
     /**
      * Launches a new file
      */
     public void newFile()
     {
         session.newFile();
     }
 
     /**
      * Opens the given file
      * 
      * @param file
      *        the file to open in a new buffer
      */
     public void open(File file)
     {
         session.open(file);
     }
     
     /**
      * Save the buffer even if the well-formed check fails
      */
     public void forceSave(HTMLBuffer buffer)
     {
        buffer.forceSave();
     }
     /**
      * Save the current buffer. If the buffer has well-formed problems, don't
      * save, but instead, notify the view
      */
     public void save()
     {
         HTMLBuffer buf = session.getCurrentBuffer();
 
         if (!buf.save())
         {
             String memo = "This document is not well formed: save anyway?";
             Mail m = new Mail(MailType.WELL_FORMED_ERROR, buf, memo);
 
             editorMailbox.addMail(m);
         }
     }
 
     /**
      * Save all the buffers
      */
     public void saveAll()
     {
         session.saveAll();
     }
 
     /**
      * Save the current buffer to the given file (save as)
      * 
      * @param file
      *        the file to save the current buffer to
      */
     public void save(File file)
     {
         HTMLBuffer buf = session.getCurrentBuffer();
 
         if (!buf.save(file))
         {
             String memo = "This document is not well formed: save anyway?";
             Mail m = new Mail(MailType.WELL_FORMED_ERROR, buf, memo);
 
             editorMailbox.addMail(m);
         }
     }
     
     /**
      * Sets the spaces per unit of indentation at the given level
      * 
      * @param level
      * @param spaces
      */
     public void setIndentSpacing(int level, int spaces)
     {
         session.setIndentSpacing(level, spaces);
     }
 
     /**
      * Runs a check on the current buffer. A message is added to the mailbox
      * with the check results
      */
     public void isWellFormed()
     {
         HTMLBuffer buf = session.getCurrentBuffer();
 
         if (!buf.isWellFormed())
         {
             String memo = "This document is not well formed: save anyway?";
             Mail m = new Mail(MailType.WELL_FORMED_ERROR, buf, memo);
 
             editorMailbox.addMail(m);
         }
         else
         {
             String memo = "This document is well formed!";
             Mail m = new Mail(MailType.WELL_FORMED_SUCCESS, buf, memo);
 
             editorMailbox.addMail(m);
         }
     }
 
     /**
      * Toggles the auto wrap settings
      */
     public void setAutoWrap()
     {
         session.setAutoWrap();
     }
 
     /**
      * @return the indentation array
      */
     public ArrayList<Integer> getIndents()
     {
         return session.getIndents();
     }
 
     /**
      * Sets which buffer tab is currently active
      * 
      * @param buffer
      *        the buffer tab to set as active
      */
     public void setCurrentTab(HTMLBuffer buffer)
     {
         session.setCurrentTab(buffer);
     }
 
     public void undo()
     {
         session.getCurrentBuffer().undo();
     }
 
     public void redo()
     {
         session.getCurrentBuffer().redo();
     }
 
     public void refresh()
     {
         //session.getCurrentBuffer().refresh();
     }
 
     public void refreshOutline()
     {
         outliner.refreshOutline();
     }
 }
