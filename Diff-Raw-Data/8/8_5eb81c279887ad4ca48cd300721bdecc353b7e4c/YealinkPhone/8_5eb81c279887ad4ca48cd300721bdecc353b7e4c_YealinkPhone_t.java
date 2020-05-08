 /*
  * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
  * Author: Konstantin S. Vishnivetsky
  * E-mail: info@siplabs.ru
  * Contributors retain copyright to elements licensed under a Contributor Agreement.
  * Licensed to the User under the LGPL license.
  *
 */
 
 package org.sipfoundry.sipxconfig.phone.yealink;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 
 import static java.lang.String.format;
 
 import org.sipfoundry.sipxconfig.registrar.RegistrarSettings;
 import org.sipfoundry.sipxconfig.bulk.ldap.LdapManager;
 import org.sipfoundry.sipxconfig.common.User;
 import org.sipfoundry.sipxconfig.device.Device;
 import org.sipfoundry.sipxconfig.device.DeviceVersion;
 import org.sipfoundry.sipxconfig.device.Profile;
 import org.sipfoundry.sipxconfig.device.ProfileLocation;
 import org.sipfoundry.sipxconfig.device.ProfileContext;
 import org.sipfoundry.sipxconfig.device.ProfileFilter;
 import org.sipfoundry.sipxconfig.phone.Line;
 import org.sipfoundry.sipxconfig.phone.LineInfo;
 import org.sipfoundry.sipxconfig.phone.Phone;
 import org.sipfoundry.sipxconfig.phone.PhoneModel;
 import org.sipfoundry.sipxconfig.phone.PhoneContext;
 import org.sipfoundry.sipxconfig.phonebook.Phonebook;
 import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
 import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
 import org.sipfoundry.sipxconfig.setting.AbstractSettingVisitor;
 import org.sipfoundry.sipxconfig.setting.Setting;
 import org.sipfoundry.sipxconfig.setting.SettingArray;
 import org.sipfoundry.sipxconfig.setting.SettingImpl;
 import org.sipfoundry.sipxconfig.setting.SettingValue;
 import org.sipfoundry.sipxconfig.setting.SettingValueImpl;
 import org.sipfoundry.sipxconfig.setting.SettingValueHandler;
 import org.sipfoundry.sipxconfig.setting.ConditionalSettingImpl;
 import org.sipfoundry.sipxconfig.setting.type.EnumSetting;
 import org.sipfoundry.sipxconfig.setting.type.MultiEnumSetting;
 import org.sipfoundry.sipxconfig.setting.type.StringSetting;
 import org.sipfoundry.sipxconfig.setting.type.SettingType;
 import org.sipfoundry.sipxconfig.setting.SettingExpressionEvaluator;
 import org.sipfoundry.sipxconfig.speeddial.SpeedDial;
 import org.sipfoundry.sipxconfig.speeddial.Button;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sipfoundry.sipxconfig.upload.UploadManager;
 import org.sipfoundry.sipxconfig.upload.UploadSpecification;
 import org.sipfoundry.sipxconfig.upload.yealink.YealinkUpload;
 
 /**
  * Yealink abstract phone.
  */
 public class YealinkPhone extends Phone {
     private static final Log LOG = LogFactory.getLog(YealinkPhone.class);
     public static final String BEAN_ID = "yealink";
 
     // Common members
     private SpeedDial m_speedDial;
 
     private RegistrarSettings m_registrarSettings;
 
     private LdapManager m_ldapManager;
 
     private UploadManager m_uploadManager;
 
     public YealinkPhone() {
         if (null == getSerialNumber()) {
             setSerialNumber(YealinkConstants.MAC_PREFIX);
         }
     }
 
     @Override
     public void setModel(PhoneModel model) {
         super.setModel(model);
         setDeviceVersion(((YealinkModel) model).getDefaultVersion());
     }
 
     public String getDefaultVersionId() {
         DeviceVersion version = getDeviceVersion();
         return version != null ? version.getVersionId() : null;
     }
 
     @Override
     public void setDeviceVersion(DeviceVersion version) {
         super.setDeviceVersion(version);
         DeviceVersion myVersion = getDeviceVersion();
         if (myVersion == YealinkModel.VER_7X) {
             getModel().setProfileTemplate("yealinkPhone/config_v7x.vm");
             getModel().setSettingsFile("phone-7X.xml");
             getModel().setLineSettingsFile("line-7X.xml");
         } else {
             // we need to explicitly define these here otherwise changing versions will not work
             getModel().setSettingsFile("phone-6X.xml");
             getModel().setLineSettingsFile("line-6X.xml");
         }
     }
 
     public void setRegistrarSettings(RegistrarSettings rs) {
         m_registrarSettings = rs;
     }
 
     public RegistrarSettings getRegistrarSettings() {
         return m_registrarSettings;
     }
 
     public void setLdapManager(LdapManager manager) {
         m_ldapManager = manager;
     }
 
     public LdapManager getLdapManager() {
         return m_ldapManager;
     }
 
     public void setUploadManager(UploadManager manager) {
         m_uploadManager = manager;
     }
 
     public UploadManager getUploadManager() {
         return m_uploadManager;
     }
 
 
     public int getMaxLineCount() {
         YealinkModel model = (YealinkModel) getModel();
         if (null != model) {
             return model.getMaxLineCount();
         }
         return 0;
     }
 
     /**
     *
     * @deprecated Use getMaxDSSKeyCount() instead!!!
     */
     @Deprecated
     public int getMemoryKeyCount() {
         YealinkModel model = (YealinkModel) getModel();
         if (null != model) {
             return model.getMemoryKeyCount();
         } else {
             return 0;
         }
     }
 
     public int getMaxDSSKeyCount() {
         YealinkModel model = (YealinkModel) getModel();
         if (null != model) {
             return model.getMemoryKeyCount();
         } else {
             return 0;
         }
     }
 
     public boolean getHasHD() {
         return getModel().isSupported(YealinkConstants.FEATURE_HDSOUND);
     }
 
     public String getDirectedCallPickupString() {
         return getPhoneContext().getPhoneDefaults().getDirectedCallPickupCode();
     }
 // DSS keys routines
     private boolean isDSSLineKey(Integer i) {
         if (getModel().getModelId().matches("yealinkPhoneSIPT46.*")) {
             return (i > 4)&&(i < 4 + 1 + getModel().getMaxLineCount());
         } else if (getModel().getModelId().matches("yealinkPhoneSIPT4[12].*")) {
             return (i > 2)&&(i < 2 + 1 + getModel().getMaxLineCount());
         } else if (getModel().getModelId().matches("yealinkPhoneSIPT[123].*")) {
             return i < 1 + getModel().getMaxLineCount();
         } else {
             return false;
         }
     }
 
     private Setting createSetting(String stName, String stId, String sName, String defaultValue) {
         SettingType st;
         if (stName.equals("enum")) {
             st = new EnumSetting();
         } else {
             st = new StringSetting();
         }
         st.setId(stId);
         ConditionalSettingImpl s = new ConditionalSettingImpl();
         s.setName(sName);
         s.setType(st);
         if (null == s.getValue()) {
             s.setValue(defaultValue);
         }
         return s;
     }
 
     private String getDSSTypeForKey(Integer i, String defaultType) {
         return isDSSLineKey(i)?"15":defaultType;
     }
 
     private String getDSSEnumId() {
         if (getModel().getModelId().matches("yealinkPhoneSIPT4.*"))
             return "DSSKeyType_71_type";
         if (getModel().getModelId().matches("yealinkPhoneSIPT[1-3].*"))
             return "DSSKeyType_70_type";
         return "DSSKeyType_6X_type";
     }
 
     private void addLineKeySettings(SettingArray setting) {
 
         setting.addSetting(createSetting("enum", getDSSEnumId(), "type", "0"));
         setting.addSetting(createSetting("string", null, "value", null));
         setting.addSetting(createSetting("string", null, "label", null));
         if (getModel().getModelId().matches("yealinkPhoneSIPT4.*")) {
             setting.addSetting(createSetting("enum", "yealink_line_type", "line", "1")); // Set line keys for SIPT4X
         } else {
             setting.addSetting(createSetting("enum", "yealink_line_type", "line", "0"));
         }
         if (getModel().getModelId().matches("yealinkPhoneSIPT4.*")) {
             setting.addSetting(createSetting("string", null, "extension", getRegistrarSettings().getDirectedCallPickupCode()));
         } else {
             setting.addSetting(createSetting("string", null, "pickup_value", getRegistrarSettings().getDirectedCallPickupCode()));
         }
         if (getModel().getModelId().matches("yealinkPhoneSIPT[1-3].*")) {
             setting.addSetting(createSetting("enum", "XMLPB_type", "xml_phonebook", "0"));
         }
     }
 
     private void addProgramableKeySettings(SettingArray setting) {
 
         setting.addSetting(createSetting("enum", "PKeyType_70_type", "type", null));
         setting.addSetting(createSetting("string", null, "value", null));
         setting.addSetting(createSetting("string", null, "label", null));
         if (getModel().getModelId().matches("yealinkPhoneSIPT4.*")) {
             setting.addSetting(createSetting("enum", "yealink_line_type", "line", null)); // Set line keys for SIPT4X
         } else {
             setting.addSetting(createSetting("enum", "yealink_line_type", "line", null));
         }
         if (getModel().getModelId().matches("yealinkPhoneSIPT[1-3].*")) {
             setting.addSetting(createSetting("enum", "XMLPB_type", "xml_phonebook", null));
             setting.addSetting(createSetting("string", null, "history", null));
         }
     }
 
     @Override
     public void setSettings(Setting settings) {
         SpeedDial sd = getPhoneContext().getSpeedDial(this);
         List<Button> sdButtons = null!=sd?sd.getButtons():new ArrayList<Button>();
 
         SettingArray lineKeys = (SettingArray) settings.getSetting("DSSKeys/line-keys/linekey");
         if (null != lineKeys) {
             lineKeys.setSize(getMaxLineCount() + getMaxDSSKeyCount());
             addLineKeySettings(lineKeys);
         }
         SettingArray programableKeys = (SettingArray) settings.getSetting("DSSKeys/programable-keys/programablekey");
         if (null != programableKeys) {
             programableKeys.setSize(15);
             addProgramableKeySettings(programableKeys);
         }
 
 //        settings.acceptVisitor(new PhonebooksSetter("remote_phonebook\\.data\\.[1-5]\\.name"));
         settings.acceptVisitor(new PhonebooksSetter("contacts/RemotePhoneBook/_RemotePhonebooks"));
         settings.acceptVisitor(new PhonebooksSelectSetter(".*/xml_phonebook\\[\\d+\\]"));
         settings.acceptVisitor(new RingtonesSetter("(distinctive-ringer/alert-info/ringer\\[\\d+\\])|preference/phone_setting\\.ring_type"));
         settings.acceptVisitor(new DSSKeySetter("DSSKeys/line-keys/linekey/type\\[\\d+\\]", getModel().getModelId().matches("yealinkPhoneSIPT4.*")?YealinkConstants.DKTYPES_V71:YealinkConstants.DKTYPES_V70));
         settings.acceptVisitor(new DSSKeySetter("DSSKeys/programable-keys/programablekey/type\\[\\d+\\]", YealinkConstants.PKTYPES_V70));
         // Commmon
         settings.acceptVisitor(new LineCountSetter(".*/((line|line_id)\\[\\d+\\])", 1, !getModel().getModelId().matches("yealinkPhoneSIPT4.*")));
         settings.acceptVisitor(new LineCountSetter("dialplan/area-code/dialplan\\.area_code\\.line_id", 1, false));
         // For W52
         settings.acceptVisitor(new LineCountSetter(".*/((incoming_lines|dial_out_lines|dial_out_default_line)\\[\\d+\\])", 1, false));
         super.setSettings(settings);
     }
 
     @Override
     public void initialize() {
         addDefaultBeanSettingHandler(new YealinkPhoneDefaults(getPhoneContext().getPhoneDefaults(), this));
         addDefaultSettingHandler(new DynamicDefaults(getPhoneContext().getSpeedDial(this)));
     }
 
     @Override
     public void initializeLine(Line line) {
         m_speedDial = getPhoneContext().getSpeedDial(this);
         line.addDefaultBeanSettingHandler(new YealinkLineDefaults(getPhoneContext().getPhoneDefaults(), line));
     }
 
     /**
     * Copy common configuration files.
     */
     @Override
     protected void copyFiles(ProfileLocation location) {
     }
 
     @Override
     protected SettingExpressionEvaluator getSettingsEvaluator() {
         return new YealinkSettingExpressionEvaluator(getModel().getModelId());
     }
 
     @Override
     public void removeProfiles(ProfileLocation location) {
         Profile[] profiles = getProfileTypes();
         for (Profile profile : profiles) {
             location.removeProfile(profile.getName());
         }
     }
 
     @Override
     public Profile[] getProfileTypes() {
         Profile[] profileTypes = new Profile[] {
             new DeviceProfile(getDeviceFilename())
         };
 
         if (getPhonebookManager().getPhonebookManagementEnabled()) {
             if (getModel().isSupported(YealinkConstants.FEATURE_PHONEBOOK)) {
                 PhonebookManager pbm = getPhonebookManager();
                 if (null != pbm) {
                     User user = getPrimaryUser();
                     if (null != user) {
                         Collection<Phonebook> phoneBooks = pbm.getAllPhonebooksByUser(user);
                         if (null != phoneBooks) {
                             Integer i = 0;
                             for(Phonebook pb : phoneBooks) {
                                 if (null != pb) {
                                     if (pb.getShowOnPhone()) {
                                         profileTypes = (Profile[]) ArrayUtils.add(profileTypes, new DirectoryProfile(getDirectoryFilename(i++), pb));
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return profileTypes;
     }
 
     @Override
     public String getProfileFilename() {
         return getSerialNumber();
     }
 
     public String getDeviceFilename() {
         return format("%s.cfg", getSerialNumber());
     }
 
     public String getDirectoryFilename(int n) {
         return format("%s-%d-%s", getSerialNumber(), n, YealinkConstants.XML_CONTACT_DATA);
     }
 
     @Override
     public void restart() {
         sendCheckSyncToFirstLine();
     }
 
     public SpeedDial getSpeedDial() {
         return m_speedDial;
     }
 
     private Collection<String> getFileListForDirectory(String dirName) {
         Collection<String> files = new ArrayList<String>();
         File rintonesLoc = new File(dirName);
         if (rintonesLoc.exists()) {
             if (rintonesLoc.isDirectory()) {
                 File[] ringtoneFiles = rintonesLoc.listFiles();
                 for (File f : ringtoneFiles) {
                     if (f.isFile()) {
                         files.add(f.getName());
                     }
                 }
             }
         }
         return files;
     }
 
     private String getMappedDirectoryName(String dirName) {
         UploadSpecification ylUploadSpec = getUploadManager().getSpecification("yealinkFiles");
         YealinkUpload ylUpload = (YealinkUpload)getUploadManager().newUpload(ylUploadSpec);
         return ylUpload.getDestinationDirectory() + dirName;
     }
 
     public Collection<String> getRingTones() {
         return getModel().isSupported(YealinkConstants.FEATURE_RINGTONES)?getFileListForDirectory(getMappedDirectoryName(YealinkUpload.DIR_YEALINK + YealinkUpload.DIR_RINGTONES)):Collections.EMPTY_LIST;
     }
 
     public Collection<String> getWallPapers() {
         return getModel().isSupported(YealinkConstants.FEATURE_WALLPAPERS)?getFileListForDirectory(getMappedDirectoryName(YealinkUpload.DIR_YEALINK + YealinkUpload.DIR_WALLPAPERS)):Collections.EMPTY_LIST;
     }
 
     public Collection<String> getScreenSavers() {
         return getModel().isSupported(YealinkConstants.FEATURE_SCREENSAVERS)?getFileListForDirectory(getMappedDirectoryName(YealinkUpload.DIR_YEALINK + YealinkUpload.DIR_SCREENSAVERS)):Collections.EMPTY_LIST;
     }
 
     public Collection<String> getLanguages() {
         return getModel().isSupported(YealinkConstants.FEATURE_LANGUAGES)?getFileListForDirectory(getMappedDirectoryName(YealinkUpload.DIR_YEALINK + YealinkUpload.DIR_LANGUAGES)):Collections.EMPTY_LIST;
     }
 
     public String getValueForSetting(Setting setting) {
         return null;
     }
 
     /**
      * Each subclass must decide how as much of this generic line information translates into its
      * own setting model.
      */
     @Override
     protected void setLineInfo(Line line, LineInfo info) {
 
         line.setSettingValue(YealinkConstants.USER_ID_V6X_SETTING, info.getUserId());
         line.setSettingValue(YealinkConstants.DISPLAY_NAME_V6X_SETTING, info.getDisplayName());
         line.setSettingValue(YealinkConstants.PASSWORD_V6X_SETTING, info.getPassword());
         line.setSettingValue(YealinkConstants.REGISTRATION_SERVER_HOST_V6X_SETTING, info.getRegistrationServer());
         line.setSettingValue(YealinkConstants.REGISTRATION_SERVER_PORT_V6X_SETTING, info.getRegistrationServerPort());
         line.setSettingValue(YealinkConstants.VOICE_MAIL_NUMBER_V6X_SETTING, info.getVoiceMail());
     }
 
     /**
      * Each subclass must decide how as much of this generic line information can be contructed
      * from its own setting model.
      */
     @Override
     protected LineInfo getLineInfo(Line line) {
         LineInfo info = new LineInfo();
         info.setDisplayName(line.getSettingValue(YealinkConstants.DISPLAY_NAME_V6X_SETTING));
         info.setUserId(line.getSettingValue(YealinkConstants.USER_ID_V6X_SETTING));
         info.setPassword(line.getSettingValue(YealinkConstants.PASSWORD_V6X_SETTING));
         info.setRegistrationServer(line.getSettingValue(YealinkConstants.REGISTRATION_SERVER_HOST_V6X_SETTING));
         info.setRegistrationServerPort(line.getSettingValue(YealinkConstants.REGISTRATION_SERVER_PORT_V6X_SETTING));
         info.setVoiceMail(line.getSettingValue(YealinkConstants.VOICE_MAIL_NUMBER_V6X_SETTING));
         return info;
     }
 
     static class DeviceProfile extends Profile {
         public DeviceProfile(String name) {
             super(name, YealinkConstants.MIME_TYPE_PLAIN);
         }
 
         @Override
         protected ProfileFilter createFilter(Device device) {
             return null;
         }
 
         @Override
         protected ProfileContext createContext(Device device) {
             YealinkPhone phone = (YealinkPhone) device;
             YealinkModel model = (YealinkModel) phone.getModel();
             return new YealinkDeviceConfiguration(phone, model.getProfileTemplate());
         }
     }
 
     static class DirectoryProfile extends Profile {
        private Phonebook m_phonebook;
 
         public DirectoryProfile(String name, Phonebook phonebook) {
             super(name, YealinkConstants.MIME_TYPE_PLAIN);
             m_phonebook = phonebook;
         }
 
         @Override
         protected ProfileFilter createFilter(Device device) {
             return null;
         }
 
         @Override
         protected ProfileContext createContext(Device device) {
             YealinkPhone phone = (YealinkPhone) device;
             YealinkModel model = (YealinkModel) phone.getModel();
             Collection<PhonebookEntry> entries = phone.getPhonebookManager().getEntries(m_phonebook);
             return new YealinkDirectoryConfiguration(phone, entries, model.getDirectoryProfileTemplate());
         }
     }
 
     static class YealinkSettingExpressionEvaluator implements SettingExpressionEvaluator {
         private final String m_model;
 
         public YealinkSettingExpressionEvaluator(String model) {
             m_model = model;
         }
 
         public boolean isExpressionTrue(String expression, Setting setting_) {
             return m_model.matches(expression);
         }
     }
 
     private class PhonebooksSetter extends YealinkEnumSetter {
         private Collection<Phonebook> m_pbs = new ArrayList<Phonebook>();
 
         public PhonebooksSetter(String pattern) {
             super(pattern);
             PhonebookManager pbm = getPhonebookManager();
             if (null != pbm) {
                 User user = getPrimaryUser();
                 if (null != user) {
                     Collection<Phonebook> phoneBooks = pbm.getAllPhonebooksByUser(user);
                     if (null != phoneBooks) {
                         Integer i = 0;
                         for(Phonebook pb : phoneBooks) {
                             if (null != pb) {
                                 if (pb.getShowOnPhone()) {
                                     m_pbs.add(pb);
                                 }
                             }
                         }
                     }
                 }
             }
         }
 
         @Override
         protected void addMultiEnums(String settingName, Integer settingIndex, MultiEnumSetting enumSetting) {
             // Clean enumerator before adding new values for model
             enumSetting.clearEnums();
             Integer i = 0;
             for(Phonebook pb : m_pbs) {
                 enumSetting.addEnum(pb.getName(), pb.getName());
             }
         }
     }
 
     private class PhonebooksSelectSetter extends YealinkEnumSetter {
 
         public PhonebooksSelectSetter(String pattern) {
             super(pattern);
         }
 
         @Override
         protected void addEnums(String settingName, Integer settingIndex, EnumSetting enumSetting) {
             enumSetting.clearEnums();
             for(Integer i = 0; i < 5; i++) {
                 enumSetting.addEnum(i.toString(), null);
             }
         }
     }
 
     private class RingtonesSetter extends YealinkEnumSetter {
 
         public RingtonesSetter(String pattern) {
             super(pattern);
         }
 
         @Override
         protected void addEnums(String settingName, Integer settingIndex, EnumSetting enumSetting) {
             if (settingName.equals("ringtone.ring_type")) {
                 enumSetting.addEnum("common", "common");
             }
             for (String rt : getRingTones()) {
                 enumSetting.addEnum(rt, rt);
             }
         }
     }
 
     private class LineCountSetter extends YealinkEnumSetter {
         private Integer m_base = 0;
         private boolean m_hasAuto = false;
 
         public LineCountSetter(String pattern, Integer base, boolean hasAuto) {
             super(pattern);
             m_base = base;
             m_hasAuto = hasAuto;
         }
 
         private void setEnum(EnumSetting enumSetting, Integer l, String valueTemplate, String label) {
             enumSetting.addEnum(String.format(valueTemplate, l + m_base), label);
         }
 
         @Override
         protected void addEnums(String settingName, Integer settingIndex, EnumSetting enumSetting) {
             // Celan enumerator before adding new values for model.
             enumSetting.clearEnums();
             if (m_hasAuto) {
                 enumSetting.addEnum(String.format("%d", 0), null); // Auto for SIP-T[123]X models
             }
             for (Integer l = 0; l<getMaxLineCount(); l++) {
                 Line line = null;
                 String userName = null;
                 if (l < getLines().size()) {
                     line = getLine(l);
                     userName = line.getUserName();
                 }
                 setEnum(enumSetting, l, "%d", ((null==line)||(null==userName))?String.format("%d", l + m_base):String.format("%d (%s)", l + m_base, userName));
             }
         }
 
         @Override
         protected void addMultiEnums(String settingName, Integer settingIndex, MultiEnumSetting enumSetting) {
             // Celan enumerator before adding new values for model.
             enumSetting.clearEnums();
             for (Integer l = 0; l<getMaxLineCount(); l++) {
                 Line line = null;
                 String userName = null;
                 if (l < getLines().size()) {
                     line = getLine(l);
                     userName = line.getUserName();
                 }
                 setEnum(enumSetting, l, "enum%d", ((null==line)||(null==userName))?String.format("%d", l + m_base):String.format("%d (%s)", l + m_base, userName));
             }
         }
     }
 
     private class DSSKeySetter extends YealinkEnumSetter {
         private String m_keyTypes;
 
         public DSSKeySetter(String pattern, String keyTypes) {
             super(pattern);
             m_keyTypes = keyTypes;
         }
 
         @Override
         protected void addEnums(String settingName, Integer settingIndex, EnumSetting enumSetting) {
             enumSetting.clearEnums();
             List<String> keyTypesList = Arrays.asList(StringUtils.split(m_keyTypes, ','));
             for (String dkt : keyTypesList) {
                 enumSetting.addEnum(dkt, null);
             }
         }
     }
 
     private class DynamicDefaults implements SettingValueHandler {
         private Integer m_BLFIndex = 0;
         private List<Button> m_sdButtons;
         private Map<Integer,Integer> m_BLF = new HashMap();
 
         public DynamicDefaults(SpeedDial sd) {
             super();
             m_sdButtons = null!=sd?sd.getButtons():new ArrayList<Button>();
             Integer sdIndex = 0;
            // Loop while i is less then summary DSS count and there is available SpeedDials to assign
            for (Integer i=0;(i<getMaxLineCount()+getMaxDSSKeyCount())&&(sdIndex<m_sdButtons.size());i++) {
                 if (!isLineKey(i)) {
                     m_BLF.put(i, sdIndex++);
                } else {
                 }
             }
         }
 
         private boolean isLineKey(Integer i) {
             if (getModel().getModelId().matches("yealinkPhoneSIPT46.*") && (i > 4 && i < (4+getMaxLineCount()+1))) {
                 return true;
             } else if (getModel().getModelId().matches("yealinkPhoneSIPT4[12].*") && (i > 2 && i < (2+getMaxLineCount()+1))) {
                 return true;
            } else if (getModel().getModelId().matches("yealinkPhoneSIPT[1-3].*") && (i > -1 && i < (0+getMaxLineCount()))) {
                 return true;
             }
             return false;
         }
 
         private Integer getLineKeyLineId(Integer i) {
             if (getModel().getModelId().matches("yealinkPhoneSIPT46.*") && (i > 4 && i < (4+getMaxLineCount()+1))) {
                 return i - 4;
             } else if (getModel().getModelId().matches("yealinkPhoneSIPT4[12].*") && (i > 2 && i < (2+getMaxLineCount()+1))) {
                 return i - 2;
             } else if (getModel().getModelId().matches("yealinkPhoneSIPT[1-3].*") && (i > -1 && i < (0+getMaxLineCount()+1))) {
                 return i + 1;
             }
             return new Integer(getModel().getModelId().matches("yealinkPhoneSIPT4.*")?1:0);
         }
 
         private Integer getLineKeyType(Integer i) {
             Integer result = 0;
             Integer BLFIndex;
             if (isLineKey(i)) {
                 return 15;
             } else if (null != (BLFIndex = m_BLF.get(i))) {
                 Button sdButton = m_sdButtons.get(BLFIndex);
                 result = sdButton.isBlf()?16:13;
             }
             return result;
         }
 
         private String getLineKeyValue(Integer i) {
             Integer BLFIndex = m_BLF.get(i);
             if (null != BLFIndex) {
                 Button sdButton = m_sdButtons.get(BLFIndex);
                 return sdButton.getNumber();
             }
             return null;
         }
 
         private String getLineKeyLabel(Integer i) {
             Integer BLFIndex = m_BLF.get(i);
             if (null != BLFIndex) {
                 Button sdButton = m_sdButtons.get(BLFIndex);
                 return sdButton.getLabel();
             }
             return null;
         }
 
         private Integer getProgramableKeyType(Integer i) {
             if (i == 0) return 28;
             if (i == 1) return 22;
             if (i == 2) return 5;
             if (i == 3) return 30;
             if (i == 4) return 28;
             if (i == 5) return 29;
             if (i == 8) return 33;
             return 0;
         }
 
         private SettingValue getLineKeyLineDefaultValue(Setting setting) {
             return new SettingValueImpl(getLineKeyLineId(setting.getIndex()).toString());
         }
 
         private SettingValue getLineKeyTypeDefaultValue(Setting setting) {
             return new SettingValueImpl(getLineKeyType(setting.getIndex()).toString());
         }
 
         private SettingValue getLineKeyValueDefaultValue(Setting setting) {
             return new SettingValueImpl(getLineKeyValue(setting.getIndex()));
         }
 
         private SettingValue getLineKeyLabelDefaultValue(Setting setting) {
             return new SettingValueImpl(getLineKeyLabel(setting.getIndex()));
         }
 
         private SettingValue getProgramableKeyLineDefaultValue(Setting setting) {
             return new SettingValueImpl(getModel().getModelId().matches("yealinkPhoneSIPT4.*")?"1":"0");
         }
 
         private SettingValue getProgramableKeyTypeDefaultValue(Setting setting) {
             return new SettingValueImpl(getProgramableKeyType(setting.getIndex()).toString());
         }
 
         private SettingValue getHSLineDefaultValue(Setting setting) {
             return new SettingValueImpl(String.format("%s%d", (setting.getName().matches("(incoming_lines|dial_out_lines)"))?"enum":"", setting.getIndex() + 1));
         }
 
         private SettingValue getHSNameDefaultValue(Setting setting) {
             return new SettingValueImpl(String.format("HS%d", setting.getIndex() + 1));
         }
 
         public SettingValue getSettingValue(Setting setting) {
             if (setting.getPath().matches("DSSKeys/programable-keys/programablekey/line.+")) {
                 return getProgramableKeyLineDefaultValue(setting);
             } else if (setting.getPath().matches("DSSKeys/programable-keys/programablekey/type.+")) {
                 return getProgramableKeyTypeDefaultValue(setting);
             }  else if (setting.getPath().matches("DSSKeys/line-keys/linekey/line.+")) {
                 return getLineKeyLineDefaultValue(setting);
             } else if (setting.getPath().matches("DSSKeys/line-keys/linekey/type.+")) {
                 return getLineKeyTypeDefaultValue(setting);
             } else if (setting.getPath().matches("DSSKeys/line-keys/linekey/value.+")) {
                 return getLineKeyValueDefaultValue(setting);
             } else if (setting.getPath().matches("DSSKeys/line-keys/linekey/label.+")) {
                 return getLineKeyLabelDefaultValue(setting);
             } else if (setting.getPath().matches(".*/((incoming_lines|dial_out_lines|dial_out_default_line)\\[\\d+\\])")) {
                 return getHSLineDefaultValue(setting);
             } else if (setting.getPath().matches("handsets/handset/name\\[\\d+\\]")) {
                 return getHSNameDefaultValue(setting);
             }
             return null;
         }
     }
 }
