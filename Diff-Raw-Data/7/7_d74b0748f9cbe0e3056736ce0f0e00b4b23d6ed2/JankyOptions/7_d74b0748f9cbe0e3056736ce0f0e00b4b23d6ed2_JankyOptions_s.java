 package com.zombietank.jankytray;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.inject.Inject;
 
 import org.eclipse.jface.preference.IPersistentPreferenceStore;
 import org.springframework.stereotype.Component;
 
 import com.zombietank.support.URLBuilder;
 
 @Component
 public class JankyOptions {
 	static final String JENKINS_URL_KEY = "com.zombietank.jankyray.jenkinsUrl";
 	static final String POLLING_INTERVAL_KEY = "com.zombietank.jankyray.pollingInterval";
 	static final int DEFAULT_POLLING_INTERVAL = 20;
 	private final IPersistentPreferenceStore preferenceStore;
 
 	@Inject
 	public JankyOptions(IPersistentPreferenceStore preferenceStore) {
 		this.preferenceStore = preferenceStore;
 	}
 	
 	public URL getJenkinsUrl() {
 		return hasJenkinsUrl() ? URLBuilder.forInput(preferenceStore.getString(JENKINS_URL_KEY)).build() : null;
 	}
 
 	public boolean hasJenkinsUrl() {
		return preferenceStore.contains(POLLING_INTERVAL_KEY);
 	}
 	
 	public void setJenkinsUrl(URL jenkinsUrl) {
 		preferenceStore.setValue(JENKINS_URL_KEY, jenkinsUrl.toExternalForm());
 	}
 
 	public int getPollingInterval() {
		return preferenceStore.contains(POLLING_INTERVAL_KEY) ? preferenceStore.getInt(POLLING_INTERVAL_KEY) : DEFAULT_POLLING_INTERVAL;
 	}
 
 	public void setPollingInterval(int pollingInterval) {
 		preferenceStore.setValue(POLLING_INTERVAL_KEY, pollingInterval);
 	}
 	
 	public void save() throws IOException {
 		preferenceStore.save();
 	}
 }
