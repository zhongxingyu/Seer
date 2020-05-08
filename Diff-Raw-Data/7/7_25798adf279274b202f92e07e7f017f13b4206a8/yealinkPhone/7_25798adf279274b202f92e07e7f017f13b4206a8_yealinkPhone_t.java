 /*
  * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
  * Author: Konstantin S. Vishnivetsky
  * E-mail: info@siplabs.ru
  * Contributors retain copyright to elements licensed under a Contributor Agreement.
  * Licensed to the User under the LGPL license.
  *
 */
 
 package org.sipfoundry.sipxconfig.phone.yealink;
 
 import java.util.Collection;
 
 import static java.lang.String.format;
 
 import org.sipfoundry.sipxconfig.bulk.ldap.LdapManager;
 import org.sipfoundry.sipxconfig.device.Device;
 import org.sipfoundry.sipxconfig.device.DeviceVersion;
 import org.sipfoundry.sipxconfig.device.Profile;
 import org.sipfoundry.sipxconfig.device.ProfileLocation;
 import org.sipfoundry.sipxconfig.device.ProfileContext;
 import org.sipfoundry.sipxconfig.device.ProfileFilter;
 import org.sipfoundry.sipxconfig.phone.Line;
 import org.sipfoundry.sipxconfig.phone.LineInfo;
 import org.sipfoundry.sipxconfig.phone.Phone;
 import org.sipfoundry.sipxconfig.phone.PhoneContext;
 import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
 import org.sipfoundry.sipxconfig.setting.Setting;
 import org.sipfoundry.sipxconfig.setting.SettingExpressionEvaluator;
 import org.sipfoundry.sipxconfig.speeddial.SpeedDial;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  * Yealink abstract phone.
  */
 public class yealinkPhone extends Phone {
     public static final String BEAN_ID = "yealink";
 
     // Common static members
     private static final Log LOG = LogFactory.getLog(yealinkPhone.class);
 
     // Common members
     private SpeedDial m_speedDial;
 
     private LdapManager m_ldapManager;
 
     public yealinkPhone() {
     }
 
     public String getDefaultVersionId() {
         DeviceVersion version = getDeviceVersion();
         return version != null ? version.getVersionId() : null;
     }
 
     @Override
     public void setDeviceVersion(DeviceVersion version) {
         super.setDeviceVersion(version);
         DeviceVersion myVersion = getDeviceVersion();
         if (myVersion == yealinkModel.VER_7X) {
             getModel().setProfileTemplate("yealinkPhone/config_v7x.vm");
            getModel().setSettingsFile("phone-7X.xml");
            getModel().setLineSettingsFile("line-7X.xml");
         } else {
             // we need to explicitly define these here otherwise changing versions will not work
            getModel().setSettingsFile("phone-6X.xml");
            getModel().setLineSettingsFile("line-6X.xml");
         }
     }
 
     public void setLdapManager(LdapManager ldapManager) {
         m_ldapManager = ldapManager;
     }
 
     public LdapManager getLdapManager() {
         return m_ldapManager;
     }
     
     public int getMaxLineCount() {
         yealinkModel model = (yealinkModel) getModel();
         if (null != model) {
             return model.getMaxLineCount();
         }
         return 0;
     }
 
     public int getSoftKeyCount() {
         yealinkModel model = (yealinkModel) getModel();
         if (null != model) {
             return model.getSoftKeyCount();
         } else {
             return 0;
         }
     }
 
     public boolean getHasHD() {
         yealinkModel model = (yealinkModel) getModel();
         if (null != model) {
             return !model.getNoHD();
         } else {
             return false;
         }
     }
 
     public String getDirectedCallPickupString() {
         return getPhoneContext().getPhoneDefaults().getDirectedCallPickupCode();
     }
 
     @Override
     public void initialize() {
         addDefaultBeanSettingHandler(new yealinkPhoneDefaults(getPhoneContext().getPhoneDefaults(), this));
     }
 
     @Override
     public void initializeLine(Line line) {
         m_speedDial = getPhoneContext().getSpeedDial(this);
         line.addDefaultBeanSettingHandler(new yealinkLineDefaults(getPhoneContext().getPhoneDefaults(), line));
     }
 
     /**
     * Copy common configuration file.
     */
     @Override
     protected void copyFiles(ProfileLocation location) {
 	yealinkModel model = (yealinkModel) getModel();
     }
 
     @Override
     protected SettingExpressionEvaluator getSettingsEvaluator() {
 	return new yealinkSettingExpressionEvaluator(getModel().getModelId());
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
     	yealinkModel model = (yealinkModel) getModel();
     	Profile[] profileTypes = new Profile[] {
     	    new DeviceProfile(getDeviceFilename())
     	};
     
             if (getPhonebookManager().getPhonebookManagementEnabled()) {
         	    if (model.getUsePhonebook()) {
                     profileTypes = (Profile[]) ArrayUtils.add(profileTypes, new DirectoryProfile(getDirectoryFilename(0)));
         	    }
             }
     
             if (model.getHasSeparateDialNow()) {
     	        profileTypes = (Profile[]) ArrayUtils.add(profileTypes, new DialNowProfile(getDialNowFilename()));
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
 	return format("%s-%d-%s", getSerialNumber(), n, yealinkConstants.XML_CONTACT_DATA);
     }
 
     public String getDialNowFilename() {
 	return format("%s-%s", getSerialNumber(), yealinkConstants.XML_DIAL_NOW);
     }
 
     @Override
     public void restart() {
 	sendCheckSyncToFirstLine();
     }
 
     public SpeedDial getSpeedDial() {
 	return m_speedDial;
     }
 
     /**
      * Each subclass must decide how as much of this generic line information translates into its
      * own setting model.
      */
     @Override
     protected void setLineInfo(Line line, LineInfo info) {
         line.setSettingValue(yealinkConstants.USER_ID_V6X_SETTING, info.getUserId());
         line.setSettingValue(yealinkConstants.DISPLAY_NAME_V6X_SETTING, info.getDisplayName());
         line.setSettingValue(yealinkConstants.PASSWORD_V6X_SETTING, info.getPassword());
         line.setSettingValue(yealinkConstants.REGISTRATION_SERVER_HOST_V6X_SETTING, info.getRegistrationServer());
         line.setSettingValue(yealinkConstants.REGISTRATION_SERVER_PORT_V6X_SETTING, info.getRegistrationServerPort());
         line.setSettingValue(yealinkConstants.VOICE_MAIL_NUMBER_V6X_SETTING, info.getVoiceMail());
     }
 
     /**
      * Each subclass must decide how as much of this generic line information can be contructed
      * from its own setting model.
      */
     @Override
     protected LineInfo getLineInfo(Line line) {
         LineInfo info = new LineInfo();
         info.setDisplayName(line.getSettingValue(yealinkConstants.DISPLAY_NAME_V6X_SETTING));
         info.setUserId(line.getSettingValue(yealinkConstants.USER_ID_V6X_SETTING));
         info.setPassword(line.getSettingValue(yealinkConstants.PASSWORD_V6X_SETTING));
         info.setRegistrationServer(line.getSettingValue(yealinkConstants.REGISTRATION_SERVER_HOST_V6X_SETTING));
         info.setRegistrationServerPort(line.getSettingValue(yealinkConstants.REGISTRATION_SERVER_PORT_V6X_SETTING));
         info.setVoiceMail(line.getSettingValue(yealinkConstants.VOICE_MAIL_NUMBER_V6X_SETTING));
         return info;
     }
 
     static class DeviceProfile extends Profile {
     	public DeviceProfile(String name) {
     	    super(name, yealinkConstants.MIME_TYPE_PLAIN);
     	}
     
     	@Override
     	protected ProfileFilter createFilter(Device device) {
     	    return null;
     	}
     
     	@Override
     	protected ProfileContext createContext(Device device) {
     	    yealinkPhone phone = (yealinkPhone) device;
     	    yealinkModel model = (yealinkModel) phone.getModel();
     	    return new yealinkDeviceConfiguration(phone, model.getProfileTemplate());
     	}
     }
 
     static class DialNowProfile extends Profile {
     	public DialNowProfile(String name) {
     	    super(name, yealinkConstants.MIME_TYPE_XML);
     	}
     
     	@Override
     	protected ProfileFilter createFilter(Device device) {
     	    return null;
     	}
     
     	@Override
     	protected ProfileContext createContext(Device device) {
     	    yealinkPhone phone = (yealinkPhone) device;
     	    yealinkModel model = (yealinkModel) phone.getModel();
     	    return new yealinkDialNowConfiguration(phone, model.getDialNowProfileTemplate());
     	}
     }
 
     static class DirectoryProfile extends Profile {
     	public DirectoryProfile(String name) {
     	    super(name, yealinkConstants.MIME_TYPE_PLAIN);
     	}
     
     	@Override
     	protected ProfileFilter createFilter(Device device) {
     	    return null;
     	}
     
     	@Override
     	protected ProfileContext createContext(Device device) {
     	    yealinkPhone phone = (yealinkPhone) device;
     	    yealinkModel model = (yealinkModel) phone.getModel();
     	    PhoneContext phoneContext = phone.getPhoneContext();
     	    Collection<PhonebookEntry> entries = phoneContext.getPhonebookEntries(phone);
     	    return new yealinkDirectoryConfiguration(phone, entries, model.getDirectoryProfileTemplate());
     	}
     }
 
     static class yealinkSettingExpressionEvaluator implements SettingExpressionEvaluator {
     	private final String m_model;
     
     	public yealinkSettingExpressionEvaluator(String model) {
     	    m_model = model;
     	}
     
     	public boolean isExpressionTrue(String expression, Setting setting_) {
     	    return m_model.matches(expression);
     	}
     }
 }
