 package com.twentysix20.lyrics.lookup;
 
 import com.twentysix20.lyrics.BaseLyricHandlerFactory;
 import com.twentysix20.lyrics.LyricPageHandler;
 import com.twentysix20.util.html.InternetHtmlLoader;
 
 public class MetroLyricsFactory extends BaseLyricHandlerFactory {
 	public static final String NAME = "MetroLyrics";
 
 	public MetroLyricsFactory(InternetHtmlLoader loader) {
 		super(loader);
 	}
 
 	@Override
 	public LyricPageHandler getLyricPageHandler(String urlString) {
 		return new MetroLyricsHandler(NAME, loader, urlString);
 	}
 
 	@Override
 	public boolean matches(String url) {
 		return url.contains("metrolyrics.com") && url.contains("-lyrics-");
 	}
 
 	@Override
 	public boolean verify() {
 		String verificationURL = "http://www.metrolyrics.com/no-one-knows-lyrics-queens-of-the-stone-age.html";
 		String verificationLyricStart = "We get these pills";
 		String verificationLyricEnd = "No one knows";
 		String verificationArtist = "QUEENS OF THE STONE AGE";
 		String verificationTitle = "NO ONE KNOWS";
 		return verify(verificationURL, verificationLyricStart, verificationLyricEnd, verificationArtist, verificationTitle);
 	}
 
 	@Override
 	public String nameOfSite() {
 		return NAME;
 	}

	static public void main (String s[]) {
		new MetroLyricsFactory(new InternetHtmlLoader()).verify();
	}
 }
