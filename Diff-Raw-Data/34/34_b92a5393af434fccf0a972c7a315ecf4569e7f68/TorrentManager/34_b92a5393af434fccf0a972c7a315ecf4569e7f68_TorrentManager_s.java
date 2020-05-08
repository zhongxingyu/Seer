 package torrent;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import torrent.interfaces.ITorrentActions;
 
 import common.interfaces.IFilter;
 import common.interfaces.ILog;
 import common.pojo.RetrievedEpisode;
 import common.pojo.Torrent;
 
 public class TorrentManager
 {
 
 	private ITorrentActions requester;
 	private IFilter showFilter;
 	private ILog log;
 
 	public TorrentManager(ITorrentActions requester, IFilter showFilter, ILog log)
 	{
 		this.requester = requester;
 		this.showFilter = showFilter;
 		this.log = log;
 	}
 
 	public void add(List<RetrievedEpisode> torrentList)
 	{
 		for (RetrievedEpisode episode : torrentList)
 			add(episode);
 	}
 
 	private void add(RetrievedEpisode episode)
 	{
 		common.pojo.Torrent torrent = (Torrent)episode.getFilterItem();
 		URL url = torrent.getUrl();
 
		log.write("try add url - "+url);
 		
 		File torrentFile = new File("temp.torrent");
 		
 		downloadTorrentFile(url, torrentFile);
		
 		requester.addFromFile(torrentFile);
 	}
 
 	private void downloadTorrentFile(
 		URL url,
 		File torrentFile)
 	{
 		HttpClient client = new DefaultHttpClient();
 		
 		HttpGet request = new HttpGet(url.toExternalForm());
 		
 		InputStream inputStream = null;
 		OutputStream outputStream = null;
 			
 		try
 		{
 			HttpResponse response = client.execute(request);
 			
 			inputStream = 
 					new GZIPInputStream(
 						response
 						.getEntity()
 						.getContent());
 			
 			outputStream = 
                     new FileOutputStream(torrentFile);
 						
 			int read = 0;
 			byte[] bytes = new byte[1024];
 			
 			while ((read = inputStream.read(bytes)) != -1)
 			{
 				outputStream.write(bytes, 0, read);
 			}
 		} 
		catch (ClientProtocolException e) {}
		catch (IllegalStateException e) {}
		catch (IOException e) {}
 		finally
 		{
 			if (inputStream != null) 
 			{
 				try 
 				{
 					inputStream.close();
 				}
 				catch (IOException e) {	}
 			}
 			
 			if (outputStream != null)
 			{
 				try 
 				{
 					outputStream.close();
 				} 
 				catch (IOException e) {	}
 			}			
 		}
 	}
 
 	public List<RetrievedEpisode> getDownloading()
 	{
 		List<torrent.linkStation.json.Torrent> downloadingTorrents = requester.getAll();
 		
 		List<RetrievedEpisode> downloadingEpisodes = showFilter.returnMatches(downloadingTorrents);
 		
 		return downloadingEpisodes;
 	}
 
 	public List<RetrievedEpisode> getAndRemoveCompleted()
 	{
 		List<torrent.linkStation.json.Torrent> completedTorrents = getCompletedTorrents();		
 		List<RetrievedEpisode> completedEpisodes = showFilter.returnMatches(completedTorrents);
 		
 		for (RetrievedEpisode episode : completedEpisodes)
 			removeTorrent(episode);
 		
 		return completedEpisodes;
 	}
 
 	private List<torrent.linkStation.json.Torrent> getCompletedTorrents()
 	{
 		List<torrent.linkStation.json.Torrent> torrentList = requester.getAll();
 		removeNonCompletedTorrents(torrentList);
 		
 		return torrentList;
 	}
 
 	private void removeNonCompletedTorrents(List<torrent.linkStation.json.Torrent> torrentList)
 	{
 		Iterator<torrent.linkStation.json.Torrent> it = torrentList.iterator();
 		while (it.hasNext())
 		{
 			torrent.linkStation.json.Torrent torrent = it.next();
 			if (torrent.getSize() != torrent.getDone())
 				it.remove();
 		}
 	}
 	
 	private void removeTorrent(RetrievedEpisode episode)
 	{
 		torrent.linkStation.json.Torrent completedTorrent = (torrent.linkStation.json.Torrent)episode.getFilterItem();
 		requester.removeTorrent(completedTorrent.getHash());
 	}
 }
