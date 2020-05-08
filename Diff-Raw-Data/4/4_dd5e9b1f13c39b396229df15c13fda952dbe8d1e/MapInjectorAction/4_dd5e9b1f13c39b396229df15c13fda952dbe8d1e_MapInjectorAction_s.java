 package com.attask.jenkins;
 
 import hudson.EnvVars;
 import hudson.model.AbstractBuild;
 import hudson.model.EnvironmentContributingAction;
 
 import java.util.Map;
 
 /**
  * User: Joel Johnson
  * Date: 1/15/13
  * Time: 5:02 PM
  */
public class MapInjectorAction implements EnvironmentContributingAction {
 	private final Map<?, ?> map;
 
 	public MapInjectorAction(Map<?, ?> map) {
 		this.map = map;
 	}
 	public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
 		for (Map.Entry<?, ?> entry : map.entrySet()) {
 			Object key = entry.getKey();
 			Object value = entry.getValue();
 			env.put(String.valueOf(key), String.valueOf(value));
 		}
 	}
 
 	public String getIconFileName() {
 		return null;
 	}
 
 	public String getDisplayName() {
 		return null;
 	}
 
 	public String getUrlName() {
 		return null;
 	}
 }
