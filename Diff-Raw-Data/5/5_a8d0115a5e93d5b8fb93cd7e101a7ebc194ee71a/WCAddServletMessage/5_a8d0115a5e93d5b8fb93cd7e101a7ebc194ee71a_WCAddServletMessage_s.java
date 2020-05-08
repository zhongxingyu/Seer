 package com.novel.stdmsg.webcon;
 
 import com.novel.stdmsg.StandartMessage;
 import com.novel.odisp.common.Message;
 
 /**     WebCon.
  * <p>  ,            
  *    .       ,  
  *   isCorrect().</p>
  * <p>   ,          .</p>
  * @author <a href="mailto:valeks@novel-il.ru">Valentin A. Alekseev</a>
  * @author (C) 2004,  "-"
 * @version $Id: WCAddServletMessage.java,v 1.1 2004/03/27 21:27:40 valeks Exp $
  */
 
 public class WCAddServletMessage extends StandartMessage {
   /**   . */
   public static final String NAME = "wc_add_servlet";
   /**  . */
   private transient String servletMask = null;
   /**  . */
   private static final String SERVLETMASK_IDX = "0";
   /** - . */
   private transient Object servletHandler = null;
   /**  . */
   private static final String SERVLETHANDLER_IDX = "1";
   /**  .
    * @param webConName   webcon.    null   <tt>webcon</tt>.
    * @param objectName  
    * @param msgId      
    */
   public WCAddServletMessage(String webConName, final String objectName, final int msgId) {
     super(NAME, webConName, objectName, msgId);
     if (webConName == null) {
       //   Discovery
       setDestination("webcon");
     }
   }
 
   /**  .
    * @param msg   
    */
   public WCAddServletMessage(final Message msg) {
     super(msg);
   }
 
   /**  . */
   public final void setServletMask(final String newServletMask) {
     servletMask = newServletMask;
   }
 
   /**   . */
   public final String getServletMask() {
     if (isCE()) {
       return (String) getField(SERVLETMASK_IDX);
     }
     return servletMask;
   }
 
   /**  . */
  public final void setServletHandler(final String newServletHandler) {
     servletHandler = newServletHandler;
   }
 
   /**   . */
   public final Object getServletHandler() {
     if (isCE()) {
       return (Object) getField(SERVLETHANDLER_IDX);
     }
     return servletHandler;
   }
 
   /**   . */
   public final boolean isCorrect() {
     if (isCE()) {
       return isCE();
     }
     if (servletMask != null && servletHandler != null) {
       addField(SERVLETMASK_IDX, servletMask);
       addField(SERVLETHANDLER_IDX, servletHandler);
       setCE(true);
     }
     return isCE();
   }
 
   /**    . */
   public final boolean isRoutable() {
     return false;
   }
 }// WCAddServletMessage
