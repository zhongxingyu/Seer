 package at.ac.tuwien.big.ewa.ue3;
 
 import java.rmi.RemoteException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.xml.rpc.ServiceException;
 
 import at.ac.tuwien.big.ewa.news.News;
 import at.ac.tuwien.big.ewa.news.NewsItem;
 import at.ac.tuwien.big.ewa.news.NewsServiceLocator;
 
 import com.icesoft.faces.async.render.OnDemandRenderer;
 
 public class NewsPoller extends TimerTask {
 
 	private static final int POLL_FREQUENCY = 20000;
 	private NewsItem latestNews;
 	private News news;
 	private OnDemandRenderer renderer;
 
 	public NewsPoller() {
 		final NewsServiceLocator newsServiceLocator = new NewsServiceLocator();
 
 		try {
 			news = newsServiceLocator.getNewsPort();
 		} catch (final ServiceException e) {
 			System.err.println("Could not get news service!");
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		try {
 			latestNews = news.getLatestNews();
 		} catch (final RemoteException e) {
 			e.printStackTrace();
 		}
 
 		final Timer poller = new Timer("NewsPoller", true);

		// delay of POLL_FREQUENCY because initial news are already polled, so it
		// doesn't get reloaded immediatly
		poller.schedule(this, NewsPoller.POLL_FREQUENCY, NewsPoller.POLL_FREQUENCY);
 	}
 
 	public NewsItem getLatestNews() {
 		return latestNews;
 	}
 
 	@Override
 	public void run() {
 		NewsItem newLatestNews;
 
 		try {
 			newLatestNews = news.getLatestNews();
 			System.out.println("polled news");
 		} catch (final RemoteException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		// if (latestNews == null || newLatestNews.getDate().compareTo(latestNews.getDate()) > 0) {
 		latestNews = newLatestNews;
 
 		if (renderer != null) renderer.requestRender();
 		// }
 	}
 
 	public void setRenderer(OnDemandRenderer renderer) {
 		this.renderer = renderer;
 	}
 
 }
