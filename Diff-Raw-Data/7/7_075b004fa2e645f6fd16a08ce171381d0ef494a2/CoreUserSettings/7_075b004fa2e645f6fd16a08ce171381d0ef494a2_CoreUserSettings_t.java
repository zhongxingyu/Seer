 package info.mikaelsvensson.ftpbackup.conf;
 
 import info.mikaelsvensson.ftpbackup.ApplicationLocale;
 import info.mikaelsvensson.ftpbackup.util.I18n;
 import info.mikaelsvensson.ftpbackup.util.Translator;
 
 public class CoreUserSettings extends AbstractUserSettings {
 // ------------------------------ FIELDS ------------------------------
 
     private final static Translator<? extends UserSetting> NAME_TRANSLATOR = new Translator<UserSetting>() {
         @Override
         public String localize(final UserSetting object) {
             return I18n.text(I18n.CORE_STRINGS, "CoreUserSettings." + object.getId() + ".name");
         }
     };
     private final static Translator<? extends UserSetting> DESCRIPTION_TRANSLATOR = new Translator<UserSetting>() {
         @Override
         public String localize(final UserSetting object) {
             return I18n.text(I18n.CORE_STRINGS, "CoreUserSettings." + object.getId() + ".description");
         }
     };
     protected static CoreUserSettings singleton;
 
 //    protected StringUserSetting locale = new StringUserSetting(Locale.getDefault().toLanguageTag(), NAME_TRANSLATOR, DESCRIPTION_TRANSLATOR);
     protected EnumUserSetting<ApplicationLocale> applicationLocale = new EnumUserSetting<>(ApplicationLocale.EN, NAME_TRANSLATOR, DESCRIPTION_TRANSLATOR, ApplicationLocale.class);
    protected StringUserSetting smtpUsername = new StringUserSetting(null, NAME_TRANSLATOR, DESCRIPTION_TRANSLATOR);
    protected StringUserSetting smtpPassword = new StringUserSetting(null, NAME_TRANSLATOR, DESCRIPTION_TRANSLATOR);
     protected StringUserSetting smtpFrom = new StringUserSetting("noreply@backup.mikaelsvensson.info", NAME_TRANSLATOR, DESCRIPTION_TRANSLATOR);
     protected IntegerUserSetting smtpPort = new IntegerUserSetting(25, NAME_TRANSLATOR, DESCRIPTION_TRANSLATOR);
    protected StringUserSetting smtpHost = new StringUserSetting(null, NAME_TRANSLATOR, DESCRIPTION_TRANSLATOR);
 
 // -------------------------- STATIC METHODS --------------------------
 
     static {
         try {
             singleton = new CoreUserSettings(PreferencesUserSettingsPersistenceStrategy.getInstance());
         } catch (UserSettingsPersistenceStrategyException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 /*
     static {
         singleton.load();
     }
 */
 
     public static CoreUserSettings getInstance() {
         return singleton;
     }
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     protected CoreUserSettings(UserSettingsPersistenceStrategy persistenceStrategy) throws UserSettingsPersistenceStrategyException {
         super(persistenceStrategy);
         initAndLoadAllSettings();
     }
 
 // --------------------- GETTER / SETTER METHODS ---------------------
 
     public EnumUserSetting<ApplicationLocale> getLocale() {
         return applicationLocale;
     }
 
 /*
     public StringUserSetting getLocale() {
         return locale;
     }
 */
 
     public StringUserSetting getSmtpFrom() {
         return smtpFrom;
     }
 
     public StringUserSetting getSmtpHost() {
         return smtpHost;
     }
 
     public StringUserSetting getSmtpPassword() {
         return smtpPassword;
     }
 
     public IntegerUserSetting getSmtpPort() {
         return smtpPort;
     }
 
     public StringUserSetting getSmtpUsername() {
         return smtpUsername;
     }
 }
