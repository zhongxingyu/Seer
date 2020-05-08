 package com.scjci.contact.dummy;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Environment;
 import android.os.StrictMode;
 import android.util.Log;
 
 import com.google.gdata.client.Query;
 import com.google.gdata.client.contacts.ContactsService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.contacts.ContactEntry;
 import com.google.gdata.data.contacts.ContactFeed;
 import com.google.gdata.data.contacts.ContactGroupEntry;
 import com.google.gdata.data.contacts.ContactGroupFeed;
 import com.google.gdata.data.contacts.Event;
 import com.google.gdata.data.contacts.UserDefinedField;
 import com.google.gdata.data.contacts.Website;
 import com.google.gdata.data.extensions.Email;
 import com.google.gdata.data.extensions.PhoneNumber;
 import com.google.gdata.data.extensions.StructuredPostalAddress;
 import com.google.gdata.util.ServiceException;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.scjci.contact.vo.UserInfo;
 import com.util.SQLiteQueryHelper;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Helper class for providing sample content for user interfaces created by
  * Android template wizards.
  * <p/>
  * TODO: Replace all uses of this class before publishing your app.
  */
 public class DummyContent {
    private final static String userEmail = "xxx@gmail.com";
     private final static String userPassword = "xxx";
     private SQLiteQueryHelper sqliteQueryHelper = null;
     private Context mContext;
 
     public DummyContent(Context context) {

         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                 .detectDiskReads()
                 .detectDiskWrites()
                 .detectNetwork()   // or .detectAll() for all detectable problems
                 .penaltyLog()
                 .build());
         this.mContext = context;
         this.sqliteQueryHelper = new SQLiteQueryHelper(mContext);
 
         //debug use
       // this.sqliteQueryHelper.selectOperateView("select * from CONTACT",null);
     }
 
     public Cursor initData() {
         Log.i("Done", "initData");
         if(!dbHasContact()){
             syncContact(new Date("1990/1/1"));
         }
         return getContact();
     }
 
     public Cursor reloadData() {
         Log.i("Done", "reloadData");
         sqliteQueryHelper.transactionDeleteOperate("CONTACT", null, null);
         syncContact(new Date("1990/1/1"));
         return getContact();
     }
 
     public Cursor searchData(String condition) {
         Log.i("Done", "searchData");
         return searchContact(condition);
     }
 
     public Cursor searchContact(String condition){
         sqliteQueryHelper.open();
         Cursor cursor = sqliteQueryHelper.selectOperate(
                 "select USER_ID as _id,NAME,AVATARPATH from CONTACT where name like ?  or phone like ?",
                 new String[]{"%"+condition+"%","%"+condition+"%"});
        // Log.i("query",String.valueOf(cursor.getCount()));
 
         return cursor;
     }
 
     private boolean dbHasContact(){
         boolean ret = false;
         sqliteQueryHelper.open();
         Cursor cursor = sqliteQueryHelper.selectOperate("select * from CONTACT", null);
         if (cursor != null) {
             try {
                 if(cursor.getCount() > 0){
                     ret = true;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 if (cursor != null) {
                     cursor.close();
                 }
                 sqliteQueryHelper.close();
             }
         }
         return ret;
     }
 
     /*
      * set DataSource from sqLite
      */
     private Cursor getContact() {
         this.sqliteQueryHelper.open();
         Cursor cursor = sqliteQueryHelper.selectOperate("select USER_ID as _id,NAME,AVATARPATH from CONTACT", null);
         return cursor;
     }
 
     /*public HashMap<String, Object> getUserContact(String uid) {
         HashMap<String, Object> map = null;
         this.sqliteQueryHelper.open();
         Cursor cursor = sqliteQueryHelper.selectOperate("select * from CONTACT where  USER_ID = ? ", new String[]{uid});
         if (cursor != null) {
             try {
                 if (cursor.getCount() > 0) {
                     int userId = cursor.getColumnIndex("USER_ID");
                     int name = cursor.getColumnIndex("NAME");
                     int phone = cursor.getColumnIndex("PHONE");
                     int addr = cursor.getColumnIndex("ADDRESS");
                     int mail = cursor.getColumnIndex("MAIL");
                     int date = cursor.getColumnIndex("DATE");
                     int site = cursor.getColumnIndex("WEBSITE");
                     int other = cursor.getColumnIndex("OTHER");
                     int avatar = cursor.getColumnIndex("AVATARPATH");
 
                     cursor.moveToFirst();
                     do {
                         map = new HashMap<String, Object>();
                         map.put("userId", cursor.getString(userId));
                         map.put("name", cursor.getString(name));
                         map.put("phone", cursor.getString(phone));
                         map.put("addr", cursor.getString(addr));
                         map.put("date", cursor.getString(date));
                         map.put("mail", cursor.getString(mail));
                         map.put("site", cursor.getString(site));
                         map.put("other", cursor.getString(other));
                         map.put("avatarPath", cursor.getString(avatar));
                     } while (cursor.moveToNext());
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 if (cursor != null) {
                     cursor.close();
                     this.sqliteQueryHelper.close();
                 }
             }
         }
         return map;
     }*/
 
     public UserInfo getUserInfo(String uid) {
         UserInfo userInfo = null;
         JSONObject jsonObject = null;
         JSONObject jsonObjectAll = new JSONObject();
         this.sqliteQueryHelper.open();
         Cursor cursor = sqliteQueryHelper.selectOperate("select * from CONTACT where  USER_ID = ? ", new String[]{uid});
         if (cursor != null) {
             try {
                 if (cursor.getCount() > 0) {
                     int userId = cursor.getColumnIndex("USER_ID");
                     int name = cursor.getColumnIndex("NAME");
                     int couple = cursor.getColumnIndex("COUPLE");
                     int job = cursor.getColumnIndex("JOB");
                     int phoneInfo = cursor.getColumnIndex("PHONE");
                     int addrInfo = cursor.getColumnIndex("ADDRESS");
                     int mailInfo = cursor.getColumnIndex("MAIL");
                     int dateInfo = cursor.getColumnIndex("DATE");
                     int siteInfo = cursor.getColumnIndex("WEBSITE");
                     int otherInfo = cursor.getColumnIndex("OTHER");
                     int avatarPath = cursor.getColumnIndex("AVATARPATH");
 
                     jsonObject = new JSONObject();
                     cursor.moveToFirst();
                     do {
                         jsonObject.put("name",cursor.getString(name));
                         jsonObject.put("couple",cursor.getString(couple));
                         jsonObject.put("job",cursor.getString(job));
                         jsonObject.put("phoneInfo",cursor.getString(phoneInfo));
                         jsonObject.put("addrInfo",cursor.getString(addrInfo));
                         jsonObject.put("dateInfo",cursor.getString(dateInfo));
                         jsonObject.put("mailInfo",cursor.getString(mailInfo));
                         jsonObject.put("siteInfo",cursor.getString(siteInfo));
                         jsonObject.put("otherInfo",cursor.getString(otherInfo));
                         jsonObject.put("avatarPath",cursor.getString(avatarPath));
                         /*map = new HashMap<String, Object>();
                                                 map.put("userId", cursor.getString(userId));
                                                 map.put("name", cursor.getString(name));
                                                 map.put("phone", cursor.getString(phone));
                                                 map.put("addr", cursor.getString(addr));
                                                 map.put("date", cursor.getString(date));
                                                 map.put("mail", cursor.getString(mail));
                                                 map.put("site", cursor.getString(site));
                                                 map.put("other", cursor.getString(other));
                                                 map.put("avatarPath", cursor.getString(avatar));*/
                     } while (cursor.moveToNext());
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 if (cursor != null) {
                     cursor.close();
                     this.sqliteQueryHelper.close();
                 }
             }
         }
         if(jsonObject != null){
             /*try {
                 jsonObjectAll.put("UserInfo",jsonObject);
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             String json = jsonObjectAll.toString();*/
 
             String json = jsonObject.toString();
 
 
 
            /* JSONObject userInfo2 = new JSONObject();
             try {
                 userInfo2.put("name", "abc");
                 userInfo2.put("job", "def");
                 userInfo2.put("phoneInfo","{\"mobile\":\"1\",\"fax\":\"2\",\"tele\":\"3\"}");
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             String json3 =  new Gson().toJson(userInfo2);*/
 
             Gson gson = new Gson();
 
 
 
             //userInfo =gson.fromJson(json3,UserInfo.class);
             //userInfo =gson.fromJson(userInfo2.toString(), UserInfo.class);
            /* userInfo = new Gson().fromJson(json2,UserInfo.class);
             userInfo = new Gson().fromJson(json,UserInfo.class);*/
         }
         return userInfo;
     }
 
     /*
      * Sync to DB
      */
     private void syncContact(Date updateTime) {
         ContactsService service;
         URL feedUrl;
         ContactFeed resultFeed;
         List<UserDefinedField> userDefindList;
         List<PhoneNumber> phoneNumberList;
         List<Email> emailList;
         List<Website> webSiteList;
         List<StructuredPostalAddress> addressList;
         List<Event> eventList;
         String url = "https://www.google.com/m8/feeds/" + "contacts/" + userEmail + "/full";
 
         try {
             service = new ContactsService("Google-contactsExampleApp-3");
             service.setUserCredentials(userEmail, userPassword);
             feedUrl = new URL(url);
             resultFeed = service.getFeed(getQueryParameter(feedUrl, service, updateTime), ContactFeed.class);
 
             String avatarPath;
             String userId;
             String userName = "";
             String couple = "";
             String job = "";
             JSONObject mailJsonObject;
             JSONObject phoneJsonObject;
             JSONObject addrJsonObject;
             JSONObject dateJsonObject;
             JSONObject siteJsonObject;
             JSONObject otherJsonObject;
             String[] tmp;
             sqliteQueryHelper.open();
             for (ContactEntry entry : resultFeed.getEntries()) {
                 //ID
                 //Log.i("NBA:",entry.getId().toString());
                 userId = entry.getId();
                 tmp = userId.split("/");
                 userId = tmp[tmp.length-1];
 
                 //姓名
                 //System.err.println("姓名: " + entry.getName().getFullName().getValue());
                 userName = entry.getName().getFullName().getValue();
 
                 //email
                 mailJsonObject = new JSONObject();
                 emailList = entry.getEmailAddresses();
                 for (Email email : emailList) {
                     if (email.getLabel() == null) {
                         if (email.getRel().endsWith("home")) {
                             //System.err.println("住家Email: " + email.getAddress());
                             mailJsonObject.put("homeEmail", email.getAddress());
                         } else if (email.getRel().endsWith("work")) {
                             //System.err.println("公司Email: " + email.getAddress());
                             mailJsonObject.put("workEmail", email.getAddress());
                         }
                     } else {
                         //System.err.println(email.getLabel() +": " + email.getAddress());
                         if (email.getLabel().equals("E-mail")) {
                             mailJsonObject.put("mainEmail", email.getAddress());
                         } else {
                             mailJsonObject.put(email.getLabel(), email.getAddress());
                         }
                     }
                 }
 
                 //電話
                 phoneJsonObject = new JSONObject();
                 phoneNumberList = entry.getPhoneNumbers();
                 for (PhoneNumber pn : phoneNumberList) {
                     if (pn.getLabel() == null) {
                         if (pn.getRel().endsWith("mobile")) {
                             //System.err.println("手機: " + pn.getPhoneNumber());
                             phoneJsonObject.put("mobilePhone", pn.getPhoneNumber());
                         } else if (pn.getRel().endsWith("work")) {
                             //System.err.println("公司: " + pn.getPhoneNumber());
                             phoneJsonObject.put("workPhone", pn.getPhoneNumber());
                         } else if (pn.getRel().endsWith("home")) {
                             //System.err.println("住家: " + pn.getPhoneNumber());
                             phoneJsonObject.put("homePhone", pn.getPhoneNumber());
                         } else if (pn.getRel().endsWith("main")) {
                             //System.err.println("主機: " + pn.getPhoneNumber());
                             phoneJsonObject.put("mainPhone", pn.getPhoneNumber());
                         } else if (pn.getRel().endsWith("work_fax")) {
                             //System.err.println("公司傳真: " + pn.getPhoneNumber());
                             phoneJsonObject.put("workFax", pn.getPhoneNumber());
                         } else if (pn.getRel().endsWith("home_fax")) {
                             //System.err.println("住家傳真: " + pn.getPhoneNumber());
                             phoneJsonObject.put("homeFax", pn.getPhoneNumber());
                         } else if (pn.getRel().endsWith("pager")) {
                             //System.err.println("呼叫器: " + pn.getPhoneNumber());
                             phoneJsonObject.put("pager", pn.getPhoneNumber());
                         }
                     } else {
                         if (pn.getLabel().equals("grandcentral")) {
                             //System.err.println("Google語音服務: " + pn.getPhoneNumber());
                             phoneJsonObject.put("googleVoice", pn.getPhoneNumber());
                         } else {
                             //System.err.println(pn.getLabel() +": " + pn.getPhoneNumber());
                             if(pn.getLabel().equals("傳真")){
                                 phoneJsonObject.put("fax", pn.getPhoneNumber());
                             }else if(pn.getLabel().equals("電話")){
                                 phoneJsonObject.put("telephone", pn.getPhoneNumber());
                             }else{
                                 phoneJsonObject.put(pn.getLabel(), pn.getPhoneNumber());
                             }
                         }
                     }
                 }
 
 
                 //住址
                 addrJsonObject = new JSONObject();
                 addressList = entry.getStructuredPostalAddresses();
                 for (StructuredPostalAddress address : addressList) {
                     if (address.getLabel() == null) {
                         if (address.getRel().endsWith("home")) {
                             //System.err.println("住家: " + address.getFormattedAddress().getValue());
                             addrJsonObject.put("homeAddr", address.getFormattedAddress().getValue());
                         } else if (address.getRel().endsWith("work")) {
                             //System.err.println("公司: "  + address.getFormattedAddress().getValue());
                             addrJsonObject.put("workAddr", address.getFormattedAddress().getValue());
                         }
                     } else {
                         if (address.getLabel().equals("通訊處")) {
                             addrJsonObject.put("contactAddr", address.getFormattedAddress().getValue());
                         } else {
                             //System.err.println(address.getLabel() + ": " + address.getFormattedAddress().getValue());
                             addrJsonObject.put(address.getLabel(), address.getFormattedAddress().getValue());
                         }
                     }
                 }
 
                 //日期
                 dateJsonObject = new JSONObject();
                 //生日
                 if (entry.getBirthday() != null) {
                     //System.err.println("生日: " + entry.getBirthday().getValue());
                     dateJsonObject.put("birthday", entry.getBirthday().getValue());
                 }
 
                 //其他日期
                 eventList = entry.getEvents();
                 for (Event event : eventList) {
                     if (event.getLabel() == null) {
                         if (event.getRel().endsWith("anniversary")) {
                             //System.err.println("週年紀念日: " + event.getWhen().getStartTime());
                             dateJsonObject.put("anniversary", event.getWhen().getStartTime());
                         }
                     } else {
                         //System.err.println(event.getLabel() + ": " + event.getWhen().getStartTime());
                         if (event.getLabel().equals("入會日期")) {
                             dateJsonObject.put("joinDate", event.getWhen().getStartTime());
                         } else {
                             dateJsonObject.put(event.getLabel(), event.getWhen().getStartTime());
                         }
                     }
                 }
 
                 //網站
                 siteJsonObject = new JSONObject();
                 webSiteList = entry.getWebsites();
                 for (Website webSite : webSiteList) {
                     if (webSite.getLabel() == null) {
                         if (webSite.getRel().toString().toLowerCase().equals("profile")) {
                             //System.err.println("個人資料: " + webSite.getHref().toString());
                             siteJsonObject.put("profileWebsite", webSite.getHref().toString());
                         } else if (webSite.getRel().toString().toLowerCase().equals("blog")) {
                             //System.err.println("網誌: " + webSite.getHref().toString());
                             siteJsonObject.put("blog", webSite.getHref().toString());
                         } else if (webSite.getRel().toString().toLowerCase().equals("home_page")) {
                             //System.err.println("首頁: " + webSite.getHref().toString());
                             siteJsonObject.put("homePage", webSite.getHref().toString());
                         } else if (webSite.getRel().toString().toLowerCase().equals("work")) {
                             //System.err.println("公司: " + webSite.getHref().toString());
                             siteJsonObject.put("workWebsite", webSite.getHref().toString());
                         }
                     } else {
                         //System.err.println(webSite.getLabel() + ": " + webSite.getHref().toString());
                         siteJsonObject.put(webSite.getLabel(), webSite.getHref().toString());
                     }
                 }
 
                 //備註
                 /*if(entry.getContent() != null){
                     System.err.println("備註： " + entry.getTextContent().getContent().getPlainText());
 				}*/
 
                 //自訂欄位
                 otherJsonObject = new JSONObject();
                 userDefindList = entry.getUserDefinedFields();
                 for (UserDefinedField ud : userDefindList) {
                     //System.err.println(ud.getKey() + ": " + ud.getValue());
                     if (ud.getKey().equals("夫人")) {
                         couple = ud.getValue();
                         //otherJsonObject.put("couple", ud.getValue());
                     } else if (ud.getKey().equals("職業")) {
                         job = ud.getValue();
                         //otherJsonObject.put("job", ud.getValue());
                     } else {
                         otherJsonObject.put(ud.getKey(), ud.getValue());
                     }
                 }
 
                 //大頭照
                 avatarPath = genAvatarPath(userId, entry, service);
 
                 //存進DB
                 this.sqliteQueryHelper.transactionOperate(
                         "Insert or REPLACE into CONTACT (USER_ID,NAME,COUPLE,JOB,MAIL,PHONE,ADDRESS,DATE,WEBSITE,OTHER,AVATARPATH) "
                                 + "values (?,?,?,?,?,?,?,?,?,?,?)", new Object[]{
                         userId,
                         userName,
                         couple,
                         job,
                         mailJsonObject.toString(),
                         phoneJsonObject.toString(),
                         addrJsonObject.toString(),
                         dateJsonObject.toString(),
                         siteJsonObject.toString(),
                         otherJsonObject.toString(),
                         avatarPath
                 });
             }
         } catch (Exception e) {
             System.err.println("Exception: " + e.toString());
         } finally {
             sqliteQueryHelper.close();
         }
     }
 
     private String genAvatarPath(String userId, ContactEntry entry, ContactsService service) throws IOException, ServiceException {
         Link photoLink = entry.getContactPhotoLink();
         if (photoLink != null && photoLink.getEtag() != null) {
             InputStream in = service.getStreamFromLink(photoLink);
 
             //check SD wirteable
             if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                 String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "scjcicontactimg";
                 File dirFile = new File(path);
                 if(!dirFile.exists()){
                     dirFile.mkdir();
                 }
 
                 try {
                     File avatar = new File(path + File.separator + userId);
                     avatar.createNewFile();
                     FileOutputStream fos = new FileOutputStream(avatar);
                     byte[] b = new byte[1024];
                     int l = 0;
                     while (true) {
                         if ((l = in.read(b)) != -1) {
                             fos.write(b, 0, l);
                         } else {
                             fos.flush();
                             fos.close();
                             break;
                         }
                     }
                     return avatar.getAbsolutePath();
                 } catch (Exception e1) {
                     // TODO Auto-generated catch block
                     e1.printStackTrace();
                 }
             }
         }
         return null;
     }
 
     private Query getQueryParameter(URL feedUrl, ContactsService service, Date updateTime) throws IOException, ServiceException {
         Query myQuery = new Query(feedUrl);
         myQuery.setMaxResults(100);
         String groupId = getGroupId("Mine", service);
         if(groupId != null){
             myQuery.setStringCustomParameter("group", groupId);
         }
         myQuery.setUpdatedMin(new DateTime(updateTime));
         return myQuery;
     }
 
     private String getGroupId(String groupName, ContactsService service)
             throws IOException, ServiceException {
         URL feedUrl = new URL(
                 "https://www.google.com/m8/feeds/groups/" + userEmail + "/full");
         ContactGroupFeed resultFeed = service.getFeed(feedUrl,
                 ContactGroupFeed.class);
         for (ContactGroupEntry groupEntry : resultFeed.getEntries()) {
             if (groupEntry.getTitle().getPlainText().equals(groupName)) {
                 return groupEntry.getId();
             }
         }
         return null;
     }
 }
