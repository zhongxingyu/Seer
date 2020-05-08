 package br.ufmg.dcc.vod.ncrawler.jobs.youtube.html;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.net.URLConnection;
 
 import br.ufmg.dcc.vod.ncrawler.CrawlJob;
 import br.ufmg.dcc.vod.ncrawler.common.Pair;
 import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
 import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
 
 public class YTHtmlAndStatsCrawlJob implements CrawlJob {
 
 	private static final long serialVersionUID = 1L;
 
 	private final String videoID;
 	private Evaluator eval;
 
 	public YTHtmlAndStatsCrawlJob(String videoID) {
 		this.videoID = videoID;
 	}
 	
 	@Override
 	public void collect() {
 		try {
 			URL videoUrl = new URL("http://www.youtube.com/watch?v="+videoID+"&gl=US&hl=en");
 			StringBuffer vidHtml = getHtml(videoUrl);
 			vidHtml.trimToSize();
 			
			URL statsUrl = new URL("http://www.youtube.com/insight_ajax?action_get_statistics_and_data=1&v="+videoID);
 			StringBuffer statsHtml = getHtml(statsUrl);
 			statsHtml.trimToSize();
 			
 			eval.evaluteAndSave(videoID, new Pair<byte[], byte[]>(vidHtml.toString().getBytes(), statsHtml.toString().getBytes()));
 		} catch (Exception e) {
 			eval.error(videoID, new UnableToCollectException(e.getMessage()));
 		}
 	}
 
 	private StringBuffer getHtml(URL u) throws IOException {
 		BufferedReader in = null;
 		try {
 			URLConnection openConnection = u.openConnection();
 			openConnection.setRequestProperty("User-Agent", "Research-Crawler-APIDEVKEY-AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
 			
 			openConnection.connect();
 			
 			StringWriter html = new StringWriter();
 			PrintWriter writer = new PrintWriter(html);
 			writer.println("<crawledvideoid = "+ videoID +" >");
 			String inputLine;
 			in = new BufferedReader(new InputStreamReader(openConnection.getInputStream()));
 			while ((inputLine = in.readLine()) != null) {
 				writer.println(inputLine);
 			}
 			writer.println("</crawledvideoid>");
 			writer.close();
 			return  html.getBuffer();
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 	
 	@Override
 	public String getID() {
 		return videoID;
 	}
 
 	@Override
 	public void setEvaluator(Evaluator eval) {
 		this.eval = eval;
 	}
 }
