 package com.twentysix20.lyrics.lookup;
 
 import com.twentysix20.lyrics.BaseLyricHandler;
 import com.twentysix20.lyrics.parsers.ConjoinedArtistTitleParser;
 import com.twentysix20.lyrics.parsers.OneShotLyricParser;
 import com.twentysix20.util.html.InternetHtmlLoader;
 
 public class MetroLyricsHandler extends BaseLyricHandler {
 	public MetroLyricsHandler(String siteName, InternetHtmlLoader loader, String urlString) {
 		super(siteName, loader, urlString);
 	}
 
 	@Override
 	protected void setupParsers() {
		lyricParser = new OneShotLyricParser("<div id=\"lyrics-body\">","</div>", true);
 		atParser = new ConjoinedArtistTitleParser("<title>"," \\- "," LYRICS</title>");
 	}
 
 	@Override
 	protected void parseData() {
         if (pageData.contains("due to licensing restrictions")) {
         	errorMessage = "No lyrics due to licensing restrictions.";
         	return;
         }
 
         super.parseData();
         
 
         if (lyrics.contains("Unfortunately, we don't have the lyrics for"))
         	errorMessage = "Lyrics page exists, but no lyrics available.";
 	}
 }
