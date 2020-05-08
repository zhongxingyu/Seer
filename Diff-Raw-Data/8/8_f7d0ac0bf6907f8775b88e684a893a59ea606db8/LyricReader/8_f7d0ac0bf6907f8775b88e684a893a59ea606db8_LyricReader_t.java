 package com.twentysix20.lyrics;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.twentysix20.lyrics.lookup.AZLyricFactory;
 import com.twentysix20.lyrics.lookup.JustSomeLyricsFactory;
 import com.twentysix20.lyrics.lookup.LyricsManiaFactory;
 import com.twentysix20.lyrics.lookup.LyricsModeFactory;
 import com.twentysix20.lyrics.lookup.LyricsVIPFactory;
 import com.twentysix20.lyrics.lookup.LyricsWikiFactory;
 import com.twentysix20.lyrics.lookup.MetroLyricsFactory;
 import com.twentysix20.lyrics.lookup.STLyricsFactory;
 import com.twentysix20.lyrics.lookup.Sing365Factory;
 import com.twentysix20.lyrics.lookup.SongMeaningsFactory;
 import com.twentysix20.lyrics.lookup.TuneWikiFactory;
 import com.twentysix20.lyrics.lookup.UULyricsFactory;
 import com.twentysix20.util.html.InternetHtmlLoader;
 
 public class LyricReader {
 	
 	private static final String SONG_DIVIDER  = "****************************************************************************";
 	private static final String LYRIC_DIVIDER = "----------------------------------------------------------------------------";
 
 	public static void main(String[] args) throws Exception {
 		System.setProperty("http.agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
 		InputStreamReader isr = new InputStreamReader(System.in);
 		BufferedReader stdin = new BufferedReader(isr);
 
 		Map<String,List<LyricPageHandler>> artistTitleToLyricsMap = new HashMap<String,List<LyricPageHandler>>();
 		List<String> artistTitleList = new ArrayList<String>();
 
 // http://www.oldielyrics.com/lyrics/steve_perry/oh_sherrie.html
 // http://www.seeklyrics.com/lyrics/Steve-Perry/Oh-Sherrie.html
 // http://www.elyrics.net/read/s/steve-perry-lyrics/oh-sherrie-lyrics.html
 // http://www.project80s.com/lyrics/song-lyrics.php%3Fsong%3Doh-sherry-steve-perry
 // http://www.lyricsfreak.com/o/ocean%2Bcolour%2Bscene/hundred%2Bmile%2Bhigh%2Bcity_20102500.html
 // http://www.elyricsworld.com/hundred_mile_high_city_lyrics_ocean_colour_scene.html
 // http://www.mp3lyrics.org/o/ocean-colour-scene/hundred-mile-high-city/
 // http://www.lyricstime.com/ocean-colour-scene-hundred-mile-high-city-lyrics.html
 // http://www.songlyrics.com/aerosmith/attitude-adjustment-lyrics/
 // http://www.lyricsg.com/59703/lyrics/themonkees/randyscousegit.html
 // http://www.lyricsbay.com/attitude_adjustment_lyrics-aerosmith.html
 // http://www.lyricsdomain.com/20/the_monkees/randy_scouse_git.html
 // http://www.lyricsdepot.com/the-monkees/randy-scouse-git.html
 // http://www.nomorelyrics.net/the_powerpuff_girls_heroes_and_villains_soundtrack-lyrics/64837-frank_black_pray_for_the_girls-lyrics.html
 // http://www.allthelyrics.com/lyrics/heroes_and_villains_the_powerpuff_girls_soundtrack/frank_black_pray_for_the_girls-lyrics-76830.html
 // http://www.sweetslyrics.com/6692.DUSTY%2520SPRINGFIELD%2520-%2520Sunny.html
 // http://www.hiplyrics.com/lyrics/sunny-by-dusty-springfield-lyrics.html
 // http://artists.letssingit.com/beastie-boys-lyrics-brass-monkey-tqz5zrx
 		
 		LyricHandlerFactoryFactory handlerFactoryFactory = new LyricHandlerFactoryFactory();
 		handlerFactoryFactory.addParserFactory(new TuneWikiFactory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new SongMeaningsFactory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new UULyricsFactory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new LyricsModeFactory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new Sing365Factory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new MetroLyricsFactory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new AZLyricFactory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new STLyricsFactory(new InternetHtmlLoader()));
 //		handlerFactoryFactory.addParserFactory(new Lyrics007Factory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new LyricsManiaFactory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new LyricsWikiFactory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new LyricsVIPFactory(new InternetHtmlLoader()));
 		handlerFactoryFactory.addParserFactory(new JustSomeLyricsFactory(new InternetHtmlLoader()));
 
 		System.out.println("Enter:");
 		String input;
 		while (!"".equals(input = stdin.readLine())) {
 			if (input.endsWith(".mp3")) {
 				int slash = input.lastIndexOf('\\');
 				input = input.substring(slash + 1, input.length() - 4);
 			}
 			if (input.indexOf("(ft.") > -1) {
 				int pos = input.indexOf("(ft.");
 				input = input.substring(0, pos)
 						+ input.substring(input.indexOf(')', pos) + 1);
 			}
 			String[] ss = input.split(" [\\-] ");// also, mdash
 			if (ss.length != 2) {
 				System.out.println("Bad input: " + ss);
 			}
 			String artist = ss[0];
 			String title = ss[1];
 			String artistTitle = artist + " - " + title;
 			System.out.println("\nSearching: " + artistTitle);
 			artistTitleList.add(artistTitle);
 
 			List<LyricPageHandler> handlerList = new ArrayList<LyricPageHandler>();
 			artistTitleToLyricsMap.put(artistTitle, handlerList);
 
 			List<URL> urls = new GoogleSearch(new InternetHtmlLoader()).lookup("lyrics " + artist + " " + title);
 			for (URL url : urls) {
 				String urlString = url.toString();
 				try {
 					if (handlerFactoryFactory.hasHandlerFactory(urlString)) {
 						LyricHandlerFactory lhf = handlerFactoryFactory.getHandlerFactory(urlString);
 						LyricPageHandler handler = lhf.getLyricPageHandler(urlString);
 						System.out.print("<!> ");
 						handler.fetchLyrics();
 						handlerList.add(handler);
 					} else {
 						System.out.print("    ");
 					}
 				} finally {
 					System.out.println(url);
 				}
 			}
 		}
 
 		System.out.println("\n\n");
 		for (String artistTitle : artistTitleList) {
 			System.out.println(SONG_DIVIDER);
 			List<LyricPageHandler> handlerList = artistTitleToLyricsMap.get(artistTitle);
 			System.out.println(artistTitle + " ("+handlerList.size()+" versions found)");
 			for (LyricPageHandler handler : handlerList) {
 				System.out.println(LYRIC_DIVIDER);
 				System.out.println(LyricPrint.print(artistTitle, handler));
 				System.out.println(LYRIC_DIVIDER);
 			}
 		}
 		System.out.println(SONG_DIVIDER);
 	}
 }
