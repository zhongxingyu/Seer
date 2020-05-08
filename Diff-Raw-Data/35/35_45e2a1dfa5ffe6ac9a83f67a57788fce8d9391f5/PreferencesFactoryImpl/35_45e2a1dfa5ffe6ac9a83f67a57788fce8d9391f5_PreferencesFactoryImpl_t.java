 package org.svaor.preferences;
 
 import org.apache.commons.lang.ObjectUtils;
import org.jetbrains.annotations.Nullable;
 import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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
 
	@Nullable
 	@Override
 	public Preferences userRoot() {
		final SecurityContext securityContext = SecurityContextHolder.getContext();
		final Authentication authentication = securityContext.getAuthentication();
		if (authentication == null) {
			return null;
		}

		final Object principal = authentication.getPrincipal();
		final String username = ObjectUtils.toString(principal, null);
 		Assert.state(username != null, "There is no authenticated user in current security context");
 		return PreferencesImpl.getUserRoot(username);
 	}
 }
