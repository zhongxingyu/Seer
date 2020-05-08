 package model;
 
 import java.io.File;
 import java.util.ArrayList;
import java.util.Observable;

 import model.Mail.MailType;
 
 public class Mediator
 {
     private Session session;
     private Mailbox mailbox;
 
     public Mediator(Mailbox mailbox)
     {
         this.mailbox = mailbox;
     }
 
     public void setSession(Session session)
     {
         this.session = session;
     }
 
     public void setAutoIndent()
     {
         session.setAutoIndent();
     }
     //force closes the buffer
     public void forceClose(HTMLBuffer buf){
         session.close(buf);
     }
     public void close(HTMLBuffer buf)
     {

         if (!buf.getChanged())
         {
             session.close(buf);
         }
         else
         {
             String memo = "This document has unsaved changes: close anyway?";
             Mail m = new Mail(MailType.UNSAVED_CHANGES, buf, memo);
 
             mailbox.addMail(m);
         }
     }
 
     public void closeAll()
     {
         session.close();
     }
 
     public void indent(boolean full)
     {
         session.getCurrentBuffer().indent(full);
     }
 
     public void insert(String html)
     {
         session.getCurrentBuffer().insert(html);
     }
 
     public void newFile()
     {
         session.newFile();
     }
 
     public void open(File file)
     {
         session.open(file);
     }
 
     public void save()
     {
         HTMLBuffer buf = session.getCurrentBuffer();
 
         if (!buf.save())
         {
             String memo = "This document is not well formed: save anyway?";
             Mail m = new Mail(MailType.WELL_FORMED_ERROR, buf, memo);
 
             mailbox.addMail(m);
         }
     }
 
     public void saveAll()
     {
         session.saveAll();
     }
 
     public void save(File file)
     {
         HTMLBuffer buf = session.getCurrentBuffer();
 
         if (!buf.save(file))
         {
             String memo = "This document is not well formed: save anyway?";
             Mail m = new Mail(MailType.WELL_FORMED_ERROR, buf, memo);
 
             mailbox.addMail(m);
         }
     }
 
     public void setIndentSpacing(int level, int spaces)
     {
         session.setIndentSpacing(level, spaces);
     }
 
     public void isWellFormed()
     {
         HTMLBuffer buf = session.getCurrentBuffer();
 
         if (!buf.isWellFormed())
         {
             String memo = "This document is not well formed: save anyway?";
             Mail m = new Mail(MailType.WELL_FORMED_ERROR, buf, memo);
 
             mailbox.addMail(m);
         }
         else
         {
             String memo = "This document is well formed!";
             Mail m = new Mail(MailType.WELL_FORMED_SUCCESS, buf, memo);
             
             mailbox.addMail(m);
         }
     }
 
     public void setAutoWrap()
     {
         session.setAutoWrap();
     }
 
     public ArrayList<Integer> getIndents()
     {
         return session.indents;
     }
     public void setCurrentTab(HTMLBuffer buffer){
         session.setCurrentTab(buffer);
     }
 }
