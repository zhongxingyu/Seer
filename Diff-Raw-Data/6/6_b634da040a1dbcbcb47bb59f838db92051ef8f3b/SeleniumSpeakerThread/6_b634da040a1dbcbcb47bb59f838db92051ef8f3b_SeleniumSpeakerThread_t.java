 package com.googlecode.mad_schuelerturnier.test.matchonly;
 
 import com.googlecode.mad_schuelerturnier.test.SeleniumDriverWrapper;
 import com.googlecode.mad_schuelerturnier.test.SeleniumMainThread;
 import org.apache.log4j.Logger;
 
 //
 
 /**
  * Created with IntelliJ IDEA. User: dama Date: 02.01.13 Time: 19:30 To change this template use File | Settings | File Templates.
  */
 public class SeleniumSpeakerThread extends Thread {
 
     private static final Logger LOG = Logger.getLogger(SeleniumMainThread.class);
 
     private SeleniumDriverWrapper util = new SeleniumDriverWrapper();
 
     @Override
     public void run() {
 
         this.util.login("root", "root");
         this.util.clickById("form_m:m_speaker");
 
         for (int i = 0; i < 10; i++) {
 
             if (MatchOnly.restart) {
                 break;
             }
 
             try {
                 this.util.sleepAMoment();
                 //this.util.clickById("form1:j_idt46:dataTable2:0:j_idt74",false);
 
             } catch (final Exception e) {
                 SeleniumSpeakerThread.LOG.error("not found: j_idt74");
             }
 
             this.util.sleepAMoment();
             try {
 
                 this.util.sleepAMoment();

                this.util.clickById("form1:j_idt54:dataTable3:0:j_idt98");
 
             } catch (final Exception e) {
                 this.util.sleepAMoment();
                SeleniumSpeakerThread.LOG.error("not found: form1:j_idt54:dataTable3:0:j_idt98");
             }
 
             this.util.sleepAMoment(10);
 
         }
         this.util.distroy();
     }
 
     public static void main(final String[] args) {
         final SeleniumSpeakerThread th = new SeleniumSpeakerThread();
         th.run();
         try {
             th.join();
         } catch (final InterruptedException e) {
             e.printStackTrace();
         }
     }
 }
