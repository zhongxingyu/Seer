 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.channel4;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import java.util.List;
 
 import org.atlasapi.remotesite.FixedResponseHttpClient;
 import org.jmock.integration.junit3.MockObjectTestCase;
 
 import com.google.common.io.Resources;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class C4HomepageClientTest extends MockObjectTestCase {
 
 	String FOUR_OD_HOMEPAGE = "http://www.channel4.com/programmes/4od";
 	String HIGHLIGHTS_URI = "http://www.channel4.com/programmes/4od/highlights";
 	String MOST_POPULAR_URI = "http://www.channel4.com/programmes/4od/most-popular";
 	
 	private final SimpleHttpClient httpClient = FixedResponseHttpClient.respondTo(FOUR_OD_HOMEPAGE, Resources.getResource("4od-homepage.html"));
 	
 	public void testRetrievingMostPopularBrands() throws Exception {
 		
 		
 		C4HomePageClient client = new C4HomePageClient(httpClient);
 		
 		BrandListingPage page = client.get(MOST_POPULAR_URI);
 		
 		List<HtmlBrandSummary> mostPopular = page.getBrandList();
 		
 		assertThat(mostPopular.size(), is(5));
 		
 		HtmlBrandSummary firstBrand = mostPopular.get(0);
 		assertThat(firstBrand.getTitle(), is("Rude Tube"));
 		assertThat(firstBrand.getId(), is("rude-tube"));
		assertThat(firstBrand.getBrandPage(), is("http://www.channel4.com/programmes/rude-tube"));
 
 	}
 	
 	public void testRetrievingHighlightBrands() throws Exception {
 		
 		C4HomePageClient client = new C4HomePageClient(httpClient);
 		
 		BrandListingPage page = client.get(HIGHLIGHTS_URI);
 		
 		List<HtmlBrandSummary> highlights = page.getBrandList();
 		
 		assertThat(highlights.size(), is(10));
 		
 		HtmlBrandSummary firstBrand = highlights.get(0);
 		assertThat(firstBrand.getTitle(), is("Tsunami: Caught on Camera"));
 		assertThat(firstBrand.getId(), is("tsunami-caught-on-camera"));
		assertThat(firstBrand.getBrandPage(), is("http://www.channel4.com/programmes/tsunami-caught-on-camera"));
 		
 		HtmlBrandSummary lastBrand = highlights.get(9);
 		assertThat(lastBrand.getTitle(), is("Father Ted"));
 		assertThat(lastBrand.getId(), is("father-ted"));
		assertThat(lastBrand.getBrandPage(), is("http://www.channel4.com/programmes/father-ted"));
 
 	}
 }
