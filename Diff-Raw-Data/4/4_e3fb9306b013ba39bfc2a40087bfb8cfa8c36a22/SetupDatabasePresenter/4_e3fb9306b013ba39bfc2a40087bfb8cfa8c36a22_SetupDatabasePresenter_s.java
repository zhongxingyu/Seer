 package net.todd.biblestudy.rcp.presenters;
 
 import net.todd.biblestudy.common.BiblestudyException;
 import net.todd.biblestudy.common.ExceptionHandlerFactory;
 import net.todd.biblestudy.common.SeverityLevel;
 import net.todd.biblestudy.rcp.models.ISetupDatabaseModel;
 import net.todd.biblestudy.rcp.views.ISetupDatabaseView;
 import net.todd.biblestudy.rcp.views.UserCredentials;
 
 public class SetupDatabasePresenter
 {
 	private final ISetupDatabaseModel model;
 	private final ISetupDatabaseView view;
 
 	public SetupDatabasePresenter(ISetupDatabaseView view, ISetupDatabaseModel model)
 	{
 		this.view = view;
 		this.model = model;
 	}
 
 	public boolean setup()
 	{
 		boolean retVal = false;
 
 		try
 		{
 			if (model.areDatabaseCredentialsPresent())
 			{
 				if (!model.isVersionCurrent())
 				{
 					model.initializeDatabase();
 					retVal = true;
 				}
 			}
 			else
 			{
 				UserCredentials creds = view.promptUserForDatabaseCredentials();
 				if (creds != null)
 				{
 					String username = creds.getUser();
 					String password = creds.getPass();
 					String url = creds.getUrl();
 
 					if (model.validateDatabaseCredentials(username, password, url))
 					{
 						if (!model.isVersionCurrent())
 						{
 							model.initializeDatabase();
 							retVal = true;
 						}
 					}
 				}
 			}
 		}
 		catch (BiblestudyException e)
 		{
 			ExceptionHandlerFactory.getHandler().handle(
 					"An error occurred while trying to initialize the database.", this, e,
 					SeverityLevel.FATAL);
 		}
 
 		return retVal;
 	}
 }
