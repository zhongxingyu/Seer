 package net.anei.cadpage.donation;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Random;
 
 import net.anei.cadpage.ManagePreferences;
 import net.anei.cadpage.vendors.VendorManager;
 
 public class DonationManager {
   
   // How long user can run after initial install
   public static final int DEMO_LIMIT_DAYS = 30;
   
   // How long before subscription expires do we start warning users
   public static final int EXPIRE_WARN_DAYS = 30;
   
   // How many times expires users can ask for another day
   public static final int MAX_EXTRA_DAYS = 10;
   
   // How long a release exemption lasts
   public static final int REL_EXEMPT_DAYS = 10;
   
   public enum DonationStatus {FREE, LIFE, AUTH_DEPT, NEW, SPONSOR, PAID, PAID_WARN, PAID_EXPIRE, 
                                PAID_LIMBO, DEMO, DEMO_EXPIRE, EXEMPT};
   
   // Cached calculated values are only valid until this time
   private long validLimitTime = 0L;
   
   // Cached expiration date
   private Date expireDate;
   
   // Cached days since install
   private int daysSinceInstall;
   
   // cached days until Cadpage expires
   private int daysTillExpire;
   
   // Cached donation status
   private DonationStatus status;
   
   // Cached enable Cadpage status
   private boolean enabled;
   
   // Cached Sponsor name
   private String sponsor;
   
   
   /**
    * Calculate all cached values
    */
   private void calculate() {
     
     // If this is the free version of Cadpage, we can skip all of the hard stuff
     if (this.getClass().getName().contains(".cadpagefree")) {
       status = DonationStatus.FREE;
       return;
     }
     
     // If the current day hasn't changed, we can use the cached values
     long curTime = System.currentTimeMillis();
     if (curTime < validLimitTime) return;
     
     // Save the previous hold status so we can tell if it has been cleared
     boolean oldAlert = status == DonationStatus.DEMO_EXPIRE || status == DonationStatus.PAID_EXPIRE;
     
     // Convert current time to a JDate, and set the cache limit to midnight
     // of that day
     Date curDate = new Date(curTime);
     JulianDate curJDate = new JulianDate(curDate);
     validLimitTime = curJDate.validUntilTime();
     
     // And calculated cached values
     sponsor = VendorManager.instance().getSponsor();
     if (sponsor == null) {
       sponsor = ManagePreferences.getCurrentParser().getSponsor();
     }
     daysSinceInstall = ManagePreferences.calcAuthRunDays(sponsor == null ? curDate : null);
     int daysTillDemoEnds = DEMO_LIMIT_DAYS - daysSinceInstall;
     
     // Calculate expiration date
     // (one year past the purchase date anniversary in the last paid year)
     // ((Use install date if there is no purchase date))
     expireDate = null;
     int daysTillSubExpire = -99999;
     boolean limbo = false;
     int paidYear = ManagePreferences.paidYear();
     if (paidYear > 0) {
       Date tDate = ManagePreferences.purchaseDate();
       if (tDate == null) tDate = ManagePreferences.installDate();
       Calendar cal = new GregorianCalendar();
       cal.setTime(tDate);
       cal.set(Calendar.YEAR, paidYear+1);
       expireDate = cal.getTime();
       Date minExpireDate = ManagePreferences.minExpireDate();
       if (minExpireDate != null && minExpireDate.after(expireDate)) expireDate = minExpireDate;
       JulianDate jExpireDate = new JulianDate(expireDate);
       daysTillSubExpire = curJDate.diffDays(jExpireDate);
       if (daysTillSubExpire < 0) {
         JulianDate jReleaseDate = new JulianDate(ManagePreferences.releaseDate());
         limbo = jReleaseDate.diffDays(jExpireDate) >= 0;
       }
     }
     daysTillExpire = Math.max(daysTillDemoEnds, daysTillSubExpire);
     
     if (ManagePreferences.freeRider()) status = DonationStatus.LIFE;
     else if (ManagePreferences.authLocation().equals(ManagePreferences.location())) {
       status = DonationStatus.AUTH_DEPT;
     } else if (expireDate != null) {
       if (daysTillExpire > EXPIRE_WARN_DAYS) status = DonationStatus.PAID;
       else if (sponsor != null) status = DonationStatus.SPONSOR;
       else if (daysTillExpire >= 0) {
         if (daysTillExpire == daysTillSubExpire) {
           status = DonationStatus.PAID_WARN;
         } else {
           status = DonationStatus.DEMO;
         }
       } else if (limbo) status = DonationStatus.PAID_LIMBO;
       else status = DonationStatus.PAID_EXPIRE;
    } else if ("General".equals(ManagePreferences.location()) &&
                 ManagePreferences.filter().length()<=1) {
       status = DonationStatus.NEW;
    } else if (sponsor != null) status = DonationStatus.SPONSOR;
    else {
       if (daysTillExpire >= 0) status = DonationStatus.DEMO;
       else status = DonationStatus.DEMO_EXPIRE;
     }
     
     // If they don't have a clear green status, check for a special release exemption
     if (status != DonationStatus.LIFE && status != DonationStatus.PAID &&
         status != DonationStatus.AUTH_DEPT && status != DonationStatus.SPONSOR) {
       
       // This only returns a exempt date if it is still is valid for the current release
       // And it is only valid for period of time after the release was shipped
       Date exemptDate = ManagePreferences.authExemptDate();
       if (exemptDate != null) {
         JulianDate exJDate = new JulianDate(exemptDate);
         int days = exJDate.diffDays(curJDate);
         if (days <= REL_EXEMPT_DAYS) {
           status = DonationStatus.EXEMPT;
           daysTillExpire = REL_EXEMPT_DAYS - days;
         }
       }
     }
     
     // Cadpage should be enabled unless something has expired
     // If status changed from expired to non-expired, reset the extra day count
     enabled = (status != DonationStatus.DEMO_EXPIRE && status != DonationStatus.PAID_EXPIRE);
     if (enabled && oldAlert) ManagePreferences.resetAuthExtra();
     
     // If status is not enabled, check for the loopholes
     // First if if user has asked for an extra day
     if (!enabled) {
       Date extraDate = ManagePreferences.authExtraDate();
       if (extraDate != null && new JulianDate(extraDate).equals(curJDate)) enabled = true;
     }
   }
 
   /**
    * Reset donation status
    */
   public void reset() {
     validLimitTime = 0;
   }
   
   /**
    * @return overall donation status
    */
   public DonationStatus status() {
     calculate();
     return status;
   }
   
   /**
    * @return return sponsor paying for support
    */
   public String sponsor() {
     return sponsor;
   }
   
   /**
    * @return true if this is the free (unsupported) version of Cadpage
    */
   public boolean isFreeVersion() {
     return status() == DonationStatus.FREE;
   }
 
 
   /**
    * @return number of days since Cadpage was installed
    */
   public int daysSinceInstall() {
     calculate();
     return daysSinceInstall;
   }
   
   /**
    * @return days to subscription expires (can be negative)
    */
   public int daysTillExpire() {
     calculate();
     return daysTillExpire;
   }
   
   /**
    * @return subscription expiration date (null in no subscription)
    */
   public Date expireDate() {
     calculate();
     return expireDate;
   }
   
   /**
    * @return number of extra day authorizations left
    */
   public int extraAuthLeft() {
     return MAX_EXTRA_DAYS - ManagePreferences.authExtraCnt();
   }
   
   /**
    * @return true if full Cadpage functionality should be enabled.
    */
   public boolean isEnabled() {
     calculate();
     return enabled;
   }
   
   // Singleton instance
   private static DonationManager instance = new DonationManager();
   
   /**
    * @return singleton instance of DonationManager
    */
   public static DonationManager instance() {
     return instance;
   }
 
   /**
    * Determine if a user entered code is a valid authorization code
    * @param code entered code
    * @return type of valid auth string if it is a valid authorization code,
    * 0 if not a valid authorization code
    */
   public static int validateAuthCode(String code) {
     
     // Switch to upper case
     code = code.toUpperCase();
     
     // See if it matches todays hash code
     JulianDate jDate = new JulianDate(new Date());
     int type = validateAuthCode(code, jDate);
     if (type > 0) return type;
     
     // No luck, see if it matches yesterdays has code
     jDate = new JulianDate(jDate, -1);
     return validateAuthCode(code, jDate);
   }
   
   private static int validateAuthCode(String code, JulianDate jDate) {
     for (int type = 1; type < 3; type++) {
       if (code.equals(calcAuthCode(type, jDate))) return type;
     }
     return 0;
   }
   
   
   
   /**
    * @return Today's authorization code
    */
   public static String getAuthCode(int type) {
     return calcAuthCode(type, new JulianDate(new Date()));
   }
   
   /**
    * Return a hash authorization code from a date
    * @param date date to be hashed
    * @return return hashed authorization string
    */
   private static String calcAuthCode(int type, JulianDate jdate) {
     Random rnd = new Random(jdate.hashCode());
     for (int ndx = 1; ndx < type; ndx++) rnd.nextInt();
     int val = Math.abs(rnd.nextInt());
     StringBuffer sb = new StringBuffer();
     for (int j = 0; j < 8; j++) {
        sb.append((char)('A'+ (val % 26)));
        val = val / 26;
     }
     return sb.toString();
   }
 }
