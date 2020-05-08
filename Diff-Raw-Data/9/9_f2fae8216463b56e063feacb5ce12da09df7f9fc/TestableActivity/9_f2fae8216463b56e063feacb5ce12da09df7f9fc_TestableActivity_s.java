 /**
  * 
  */
 package ru.altruix.commons.android;
 
 import android.app.Activity;
 
 /**
  * @author DP118M
  *
  */
public abstract class TestableActivity extends Activity implements IActivity {

	protected IAsyncTaskFactory asyncTaskFactory = new AsyncTaskFactory();
 	protected IIntentFactory intentFactory = new IntentFactory();
 	protected IWebServiceTaskHelperFactory helperFactory = new WebServiceTaskHelperFactory();
 
	public void setAsyncTaskFactory(final IAsyncTaskFactory asyncTaskFactory) {
		this.asyncTaskFactory = asyncTaskFactory;
	}

 	public void setIntentFactory(final IIntentFactory intentFactory) {
 		this.intentFactory = intentFactory;
 	}
 
 	protected WebServiceTaskHelper getWebServiceHelper() {
 		return this.helperFactory.create();
 	}
 }
