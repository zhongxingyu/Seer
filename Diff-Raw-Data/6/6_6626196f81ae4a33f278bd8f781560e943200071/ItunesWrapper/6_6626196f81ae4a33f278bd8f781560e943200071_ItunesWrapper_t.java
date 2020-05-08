 package ru.compscicenter.appRecommendation.wrapper;
 
 import java.net.URL;
 import java.io.*;
 import java.util.Scanner;
 
 import org.webharvest.definition.ScraperConfiguration;
 import org.webharvest.runtime.Scraper;
 
 import ru.compscicenter.appRecommendation.Wrapper;
 import ru.compscicenter.appRecommendation.exception.WebHarvestException;
 
 public class ItunesWrapper extends Wrapper {
 	private final String workingDir = System.getProperty("user.dir");
 	private final String wrapLinkConfigPath = "wrapper/configs/downloader/downloadAllLinks.xml";
 	private final String wrapPageConfigPath = "wrapper/configs/itunes_apple.xml";
 	private final String tempPath = "data";
 	private final String linksFilePath = tempPath + "/" + "allLinks.txt";
 
 
 	private void wrapLinks(int maxloops) throws WebHarvestException {
 		try {
 			ScraperConfiguration config =
 					new ScraperConfiguration(wrapLinkConfigPath);
 			Scraper scraper = new Scraper(config, workingDir);
 			scraper.addVariableToContext("path", linksFilePath);
 			scraper.addVariableToContext("maxloops", maxloops);
 			System.out.println("Start. Wrapping links");
 			log.info("Start. Wrapping links");
 			scraper.execute();
 			log.info("End.Wrapping links");
 		} catch (Exception exp) {
 			exp.printStackTrace();
 			throw new WebHarvestException();
 		}
 	}
 
 	private void downloadLinks() throws Exception {
 		log.info("Start. Downloading links");
 		int i = 1;
 		Scanner sc = new Scanner(new File(linksFilePath));
 		//todo download only new links..  (hashset fro db)
 		while (sc.hasNext()) {
 			String url = sc.next();
 			String filePath = tempPath + "/" + i + ".html";
 			downloadOneLink(url, filePath);
 			++i;
 		}
 		log.info("End. Downloading links");
 	}
 
 	private void wrapPages() throws WebHarvestException {
 		try {
 			ScraperConfiguration config =
 					new ScraperConfiguration(wrapPageConfigPath);
 			Scraper scraper = new Scraper(config, workingDir);
 			scraper.addVariableToContext("path", tempPath);
 			log.info("Start. Wrapping pages");
 			scraper.execute();
 			log.info("End. Wrapping pages");
 		} catch (Exception exp) {
 			exp.printStackTrace();
 			throw new WebHarvestException();
 		}
 	}
 
 	private static void downloadOneLink(String urlStr, String filePath) throws Exception {
 		log.debug("Downloading " + urlStr + " to " + filePath);
 		URL url = new URL(urlStr);
 		InputStream response = url.openStream();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(response));
 		FileWriter fw = new FileWriter(new File(filePath));
 		String line = reader.readLine();
 		while (line != null) {
 			fw.write(line);
 			line = reader.readLine();
 		}
 		reader.close();
 		fw.close();
 	}
 
 	public void wrap() {
 		try{
			log.info("Starting wrapping");
 			//wrapLinks(1);
 			//downloadLinks();
 			//wrapPages();
			log.info("End wrapping");
 		} catch ( Exception exp){
 			exp.printStackTrace();
 		}
 	}
 
 	public void afterPropertiesSet() {
 		wrap();
 	}
 
 	public static void main(String argv[]) {
 		ItunesWrapper wrapper = new ItunesWrapper();
 		wrapper.wrap();
 	}
 }
