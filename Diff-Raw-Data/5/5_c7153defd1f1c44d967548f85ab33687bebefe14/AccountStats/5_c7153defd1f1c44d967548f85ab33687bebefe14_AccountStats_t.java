 package fm.audiobox.core.models;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IEntity;
 
 public final class AccountStats extends AbstractEntity implements Serializable {
   
   
   private static final long serialVersionUID = 1L;
   
   public static final String NAMESPACE = "stats";
   public static final String TAGNAME = NAMESPACE;
   
   
   private long data_served_this_month;
   private long data_served_overall;
   private long cloud_data_stored_overall;
   private long cloud_data_stored_this_month;
   private long local_data_stored_overall;
   private long local_data_stored_this_month;
   private long dropbox_data_stored_overall;
   private long dropbox_data_stored_this_month;
   private long gdrive_data_stored_this_month;
   private long gdrive_data_stored_overall;
   private long skydrive_data_stored_this_month;
   private long skydrive_data_stored_overall;
   private long box_data_stored_this_month;
   private long box_data_stored_overall;
   private long partner_data_stored_this_month;
   private long partner_data_stored_overall;
   
   
 
   public AccountStats(IConfiguration config) {
     super(config);
   }
 
   @Override
   public String getNamespace() {
     return NAMESPACE;
   }
 
   @Override
   public String getTagName() {
     return TAGNAME;
   }
 
   
   public long getDataServedThisMonth() {
     return data_served_this_month;
   }
 
   public void setDataServedThisMonth(long data_served_this_month) {
     this.data_served_this_month = data_served_this_month;
   }
 
   public long getDataServedOverall() {
     return data_served_overall;
   }
 
   public void setDataServedOverall(long data_served_overall) {
     this.data_served_overall = data_served_overall;
   }
 
   public long getCloudDataStoredOverall() {
     return cloud_data_stored_overall;
   }
 
   public void setCloudDataStoredOverall(long cloud_data_stored_overall) {
     this.cloud_data_stored_overall = cloud_data_stored_overall;
   }
 
   public long getCloudDataStoredThisMonth() {
     return cloud_data_stored_this_month;
   }
 
   public void setCloudDataStoredThisMonth(long cloud_data_stored_this_month) {
     this.cloud_data_stored_this_month = cloud_data_stored_this_month;
   }
 
   public long getLocalDataStoredOverall() {
     return local_data_stored_overall;
   }
 
   public void setLocalDataStoredOverall(long local_data_stored_overall) {
     this.local_data_stored_overall = local_data_stored_overall;
   }
 
   public long getLocalDataStoredThisMonth() {
     return local_data_stored_this_month;
   }
 
   public void setLocalDataStoredThisMonth(long local_data_stored_this_month) {
     this.local_data_stored_this_month = local_data_stored_this_month;
   }
 
   public long getDropboxDataStoredOverall() {
     return dropbox_data_stored_overall;
   }
 
   public void setDropboxDataStoredOverall(long dropbox_data_stored_overall) {
     this.dropbox_data_stored_overall = dropbox_data_stored_overall;
   }
 
   public long getDropboxDataStoredThisMonth() {
     return dropbox_data_stored_this_month;
   }
 
   public void setDropboxDataStoredThisMonth(long dropbox_data_stored_this_month) {
     this.dropbox_data_stored_this_month = dropbox_data_stored_this_month;
   }
 
   public long getGdriveDataStoredThisMonth() {
     return gdrive_data_stored_this_month;
   }
 
   public void setGdriveDataStoredThisMonth(long gdrive_data_stored_this_month) {
     this.gdrive_data_stored_this_month = gdrive_data_stored_this_month;
   }
 
   public long getGdriveDataStoredOverall() {
     return gdrive_data_stored_overall;
   }
 
   public void setGdriveDataStoredOverall(long gdrive_data_stored_overall) {
     this.gdrive_data_stored_overall = gdrive_data_stored_overall;
   }
 
   public long getSkydriveDataStoredThisMonth() {
     return skydrive_data_stored_this_month;
   }
 
   public void setSkydriveDataStoredThisMonth(long skydrive_data_stored_this_month) {
     this.skydrive_data_stored_this_month = skydrive_data_stored_this_month;
   }
 
   public long getSkydriveDataStoredOverall() {
     return skydrive_data_stored_overall;
   }
 
   public void setSkydriveDataStoredOverall(long skydrive_data_stored_overall) {
     this.skydrive_data_stored_overall = skydrive_data_stored_overall;
   }
 
   public long getBoxDataStoredThisMonth() {
     return box_data_stored_this_month;
   }
 
   public void setBoxDataStoredThisMonth(long box_data_stored_this_month) {
     this.box_data_stored_this_month = box_data_stored_this_month;
   }
 
   public long getBoxDataStoredOverall() {
     return box_data_stored_overall;
   }
 
   public void setBoxDataStoredOverall(long box_data_stored_overall) {
     this.box_data_stored_overall = box_data_stored_overall;
   }
 
   public long getPartnerDataStoredThisMonth() {
     return partner_data_stored_this_month;
   }
 
   public void setPartnerDataStoredThisMonth(long partner_data_stored_this_month) {
     this.partner_data_stored_this_month = partner_data_stored_this_month;
   }
 
   public long getPartnerDataStoredOverall() {
     return partner_data_stored_overall;
   }
 
   public void setPartnerDataStoredOverall(long partner_data_stored_overall) {
     this.partner_data_stored_overall = partner_data_stored_overall;
   }
 
   @Override
   public Method getSetterMethod(String tagName) throws SecurityException, NoSuchMethodException {
     if ( tagName.equals("data_served_this_month") ) {
       return this.getClass().getMethod("setDataServedThisMonth", long.class);
 
     } else if ( tagName.equals("data_served_overall") ) {
       return this.getClass().getMethod("setDataServedOverall", long.class);
 
     } else if ( tagName.equals("cloud_data_stored_overall") ) {
       return this.getClass().getMethod("setCloudDataStoredOverall", long.class);
 
     } else if ( tagName.equals("cloud_data_stored_this_month") ) {
       return this.getClass().getMethod("setCloudDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("local_data_stored_overall") ) {
       return this.getClass().getMethod("setLocalDataStoredOverall", long.class);
 
     } else if ( tagName.equals("local_data_stored_this_month") ) {
       return this.getClass().getMethod("setLocalDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("dropbox_data_stored_overall") ) {
       return this.getClass().getMethod("setDropboxDataStoredOverall", long.class);
 
     } else if ( tagName.equals("dropbox_data_stored_this_month") ) {
       return this.getClass().getMethod("setDropboxDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("gdrive_data_stored_this_month") ) {
       return this.getClass().getMethod("setGdriveDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("gdrive_data_stored_overall") ) {
       return this.getClass().getMethod("setGdriveDataStoredOverall", long.class);
 
     } else if ( tagName.equals("skydrive_data_stored_this_month") ) {
       return this.getClass().getMethod("setSkydriveDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("skydrive_data_stored_overall") ) {
       return this.getClass().getMethod("setSkydriveDataStoredOverall", long.class);
 
     } else if ( tagName.equals("box_data_stored_this_month") ) {
      return this.getClass().getMethod("setBoxDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("box_data_stored_overall") ) {
      return this.getClass().getMethod("setBoxDataStoredOverall", long.class);
 
     } else if ( tagName.equals("partner_data_stored_this_month") ) {
       return this.getClass().getMethod("setPartnerDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("partner_data_stored_overall") ) {
       return this.getClass().getMethod("setPartnerDataStoredOverall", long.class);
 
     }
     
     return null;
   }
 
   @Override
   public String getApiPath() {
     return null;
   }
 
   @Override
   public void setParent(IEntity parent) { }
 
   @Override
   protected void copy(IEntity entity) { }
   
 }
