 package org.svaor.preferences;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.springframework.context.ApplicationContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.util.Assert;
 import org.svaor.preferences.utils.ApplicationContextProvider;
 
 import java.util.prefs.Preferences;
 import java.util.prefs.PreferencesFactory;
 
 /**
  * {@link java.util.prefs.PreferencesFactory} implementation. This factory generates {@link java.util.prefs.Preferences} objects
  * for system scope and user scope.<br/>
  * In the case of user scope principal is requested from current {@link org.springframework.security.core.context.SecurityContext}.
  * @author Smelik Evgeniy
  */
 public class PreferencesFactoryImpl implements PreferencesFactory {
 	public PreferencesFactoryImpl() {
 		final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
 
 		final PreferencesProvider preferenceProvider = applicationContext.getBean("preferencesProvider", PreferencesProvider.class);
 		PreferencesImpl.setPreferencesProvider(preferenceProvider);
 	}
 
 	@Override
 	public Preferences systemRoot() {
 		return PreferencesImpl.getSystemRoot();
 	}
 
 	@Override
 	public Preferences userRoot() {
		final String username = ObjectUtils.toString(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), null);
 		Assert.state(username != null, "There is no authenticated user in current security context");
 		return PreferencesImpl.getUserRoot(username);
 	}
 }
