 package edu.cmu.square.client.ui.ManageProject;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.ProjectRole;
 import edu.cmu.square.client.navigation.HistoryManager;
 import edu.cmu.square.client.navigation.Pilot;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.ui.core.content.BreadCrumbMessages;
 
 
 /**
  *
  *
  */
 public class ManageProjectPilot extends Pilot
 {
 
 	public static class PageId
 	{
 		public static final String home = "settings"; 
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see edu.cmu.square.client.entryPoint.Pilot#navigateTo(java.lang.String, edu.cmu.square.client.entryPoint.State)
 	 */
 	
 	public Widget navigateTo(String pageId, State currentStateInformation)
 	{
 		if (pageId.equals(PageId.home)) 
 		{
 			return new ManageProjectPane(currentStateInformation);
 		} 
 
 		return null;
 	}
 
 	
 	public void determineAccessRights(String page, State currentState)
 	{
 		
		
		if (currentState.getUserProjectRole() == ProjectRole.Acquisition_Organization_Engineer)
 		{
 			currentState.setMode(GwtModesType.ReadWrite);
 		}
 		else if (currentState.getUserProjectRole() == ProjectRole.None)
 		{
 			currentState.setMode(GwtModesType.NoAccess);
 		}
 		else{
 		currentState.setMode(GwtModesType.ReadOnly);
 		}
 	}
 	
 	
 	
 	public String getBreadCrumb()
 	{
 		final BreadCrumbMessages messages = (BreadCrumbMessages) GWT.create(BreadCrumbMessages.class);
 		return messages.projectSettings();
 	}
 	
 
 	public static String generateNavigationId(String pageId)
 	{
 		return HistoryManager.ViewId.manageProject + "/" + pageId;
 	}
 }
