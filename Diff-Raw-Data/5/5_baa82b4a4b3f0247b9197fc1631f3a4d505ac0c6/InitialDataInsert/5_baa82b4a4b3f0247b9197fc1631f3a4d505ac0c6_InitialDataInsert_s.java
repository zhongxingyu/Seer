 package jcube.manager.filter;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Properties;
 
 import manager.model.News;
 import manager.model.NewsStatus;
 
 import jcube.activerecord.fieldtypes.DateTime;
 import jcube.core.filter.JcubeChainFilterElement;
 import jcube.core.server.Environ;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class InitialDataInsert.
  */
 public class InitialDataInsert extends JcubeChainFilterElement
 {
 
 	/**
 	 * Instantiates a new initial data insert.
 	 * 
 	 * @param env
 	 *            the env
 	 * @param properties
 	 *            the properties
 	 * @throws Exception
 	 *             the exception
 	 */
 	public InitialDataInsert(Environ env, Properties properties) throws Exception
 	{
 		File file = new File("../.inited");
 		System.out.println("inside filer");
		if (file.exists())
 		{
 			// Generate statuses
 			NewsStatus open_status = new NewsStatus();
 			open_status.title = "Open";
 			open_status.save();
 
 			NewsStatus closed_status = new NewsStatus();
 			closed_status.title = "Closed";
 			closed_status.save();
 
 			NewsStatus moderate_status = new NewsStatus();
 			moderate_status.title = "Verifying";
 			moderate_status.save();
 
 			NewsStatus archived_status = new NewsStatus();
 			archived_status.title = "Archived";
 			archived_status.save();
 
 			ArrayList<NewsStatus> statuses = new ArrayList<NewsStatus>();
 			statuses.add(open_status);
 			statuses.add(closed_status);
 			statuses.add(moderate_status);
 			statuses.add(archived_status);
 
 			for (int i = 0; i <= 200; i++)
 			{
 
 				News news = new News();
 				news.title = "Item " + i;
 				news.status = statuses.get((int) (Math.random() * statuses.size()));
 				DateTime date = new DateTime();
 				date.shift(Calendar.DATE, (int) (Math.random() * 30));
 				news.added_on = date;
 				news.published = ((int) (Math.random() * 2) == 0) ? false : true;
 				news.desc = "Item description " + i;
 				news.save();
 			}
			file.delete();
 		}
 
 	}
 }
