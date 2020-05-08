 /**
  * Copyright (C) 2013 Seajas, the Netherlands.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3, as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.seajas.search.contender.scripting.scripts;
 
 import com.seajas.search.bridge.contender.metadata.SeajasEntry;
 import com.seajas.search.contender.scripting.FeedScriptsTestBase;
 import com.sun.syndication.feed.module.DCModule;
 import com.sun.syndication.feed.synd.SyndFeed;
 import java.util.Arrays;
import java.util.Date;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 
 /**
  * Flickr script test.
  *
  * @author Jasper van Veghel <jasper@seajas.com>
  */
 public class FlickrScriptTest extends FeedScriptsTestBase {
 	@Test
 	public void feed() throws Exception {
 		String searchUri = "https://secure.flickr.com/services/rest/?method=flickr.photos.search&text=bushokje&extras=date_taken,path_alias,owner_name,geo,url_sq,url_t,url_s,url_q,url_m,url_n,url_z,url_c,url_l,url_o&nojsoncallback=1&format=json&api_key=xxx";
 
 		bind(searchUri, "flickr/qbushokje.json");
 
 		SyndFeed feed = run(searchUri, "flickr/flickrSearch.js");
 
 		assertEquals("feed title", "Flickr Feed", feed.getTitle());
 		assertEquals("entry count", 100, feed.getEntries().size());
 
 		SeajasEntry first = getEntryById("https://secure.flickr.com/photos/roeljewel/9183032373/");
 
 		assertEquals("first title", "Handig, zo'n bushokje voor m'n fiets.. ;).", first.getTitle());
 		assertEquals("first description", "Handig, zo'n bushokje voor m'n fiets.. ;).", first.getDescriptionText());
 		assertEquals("first type", "flickr-image", first.getDCType());
 		// assertEquals("first timestamp", dates.parse("2013-07-01T17:39:03Z"), first.getPublishedDate());
 		assertEquals("first author", "roeljewel", first.getAuthor());
 		assertEquals("first creator", Arrays.asList("roeljewel", "RoelJewel", "92656241@N00"), ((DCModule) first.getModule(DCModule.URI)).getCreators());
 		assertEquals("first author name", "RoelJewel", first.getAuthorName());
 		assertEquals("first linked image", Arrays.asList("https://farm4.staticflickr.com/3707/9183032373_0a51e2e911_z.jpg"), first.getLinkedImageUrls());

		assertEquals("first published date", new Date(1385736285L * 1000), first.getPublishedDate());
 	}
 }
