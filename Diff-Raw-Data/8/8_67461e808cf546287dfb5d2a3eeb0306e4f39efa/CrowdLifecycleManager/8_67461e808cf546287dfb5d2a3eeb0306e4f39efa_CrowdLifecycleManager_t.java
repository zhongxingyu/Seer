 package com.atlassian.sal.crowd.lifecycle;
 
 import com.atlassian.config.util.BootstrapUtils;
 import com.atlassian.sal.core.lifecycle.DefaultLifecycleManager;
 
 public class CrowdLifecycleManager extends DefaultLifecycleManager
 {
 	public boolean isApplicationSetUp()
 	{
 		return BootstrapUtils.getBootstrapManager().isSetupComplete();
 	}
	
	@Override
	protected boolean isStarted()
	{
		// Always return false for Crowd, because Crowd doesn't re-instantiate LifecycleManager after config import
		// TODO It sounds like a hack and we should think of a better solution.
		return false;
	}
 }
