 package cc.warlock.rcp.ui.client;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.model.IWorkbenchAdapter;
 
 import cc.warlock.core.client.IWarlockClient;
 
 public class WarlockClientAdaptable implements IAdaptable, IWorkbenchAdapter {
 
 	protected IWarlockClient client;
 	
 	public WarlockClientAdaptable (IWarlockClient client)
 	{
 		this.client = client;
 	}
 	
 	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(IWarlockClient.class))
 		{
 			return this.client;
 		}
 		
 		return null;
 	}
 
 	public Object[] getChildren(Object o) {
 		return new Object[0];
 	}
 	
 	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
 	}
 	
 	public String getLabel(Object o) {
 		return client.getDefaultStream().getTitle().get();
 	}
 	
 	public Object getParent(Object o) {
 		return null;
 	}
 }
