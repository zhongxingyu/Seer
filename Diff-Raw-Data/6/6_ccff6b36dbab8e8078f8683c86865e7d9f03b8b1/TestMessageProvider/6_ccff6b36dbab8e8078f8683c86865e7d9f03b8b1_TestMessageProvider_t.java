 package ezhun.smsb.storage;
 
 import android.telephony.SmsManager;
 import ezhun.smsb.SmsPojo;
 
 import java.util.ArrayList;
 
 public class TestMessageProvider implements IMessageProvider{
     ArrayList<SmsPojo> mList;
     int mUnreadCount = 0;
 
     public TestMessageProvider(){
         mList = new ArrayList<SmsPojo>();
         SmsPojo sms = new SmsPojo();
         sms.setMessage("Hey you there! How you doin'?");
         sms.setSender("+380971122333");
         sms.setReceived(2000);
         mList.add(sms);
 
         sms = new SmsPojo();
         sms.setMessage("Nova aktsia vid magazinu Target! Kupuy 2 kartopli ta otrymay 1 v podarunok!");
         sms.setSender("TARGET");
         sms.setReceived(10000);
         mList.add(sms);
 
         sms = new SmsPojo();
         sms.setMessage("This is my new number. B. Obama.");
         sms.setSender("+1570333444555");
         sms.setReceived(15000);
         mList.add(sms);
 
         sms = new SmsPojo();
         sms.setMessage("How about having some beer tonight? Stranger.");
         sms.setSender("+380509998887");
         sms.setRead(true);
         sms.setReceived(20000);
         mList.add(sms);
 
         sms = new SmsPojo();
         sms.setMessage("Vash rakhunok na 9.05.2011 stanovyt 27,35 uah.");
         sms.setSender("KYIVSTAR");
         sms.setReceived(100000);
         mList.add(sms);
 
         sms = new SmsPojo();
         sms.setMessage("Skydky v ALDO. Kupuy 1 ked ta otrymuy dryguy v podarunok!");
         sms.setSender("ALDO");
         sms.setReceived(2000000);
         mList.add(sms);
 
         sms = new SmsPojo();
         sms.setMessage("Big football tonight v taverne chili!");
         sms.setSender("+380991001010");
         sms.setRead(true);
         sms.setReceived(2100000);
         mList.add(sms);
 
         sms = new SmsPojo();
         sms.setMessage("15:43 ZABLOKOVANO 17,99 UAH, SUPERMARKET PORTAL");
         sms.setSender("PUMB");
         sms.setReceived(3000000);
         mList.add(sms);
 
         mUnreadCount = 6;
     }
 
     public ArrayList<SmsPojo> getMessages() {
          return mList;
     }
 
     public SmsPojo getMessage(int id) {
         return mList.get(id);
     }
 
     public void read(int id){
        if (!mList.get(id).wasRead()){
            mList.get(id).setRead(true);
             mUnreadCount --;
        }
     }
 
     public int getUnreadCount(){
         return mUnreadCount;
     }
 }
