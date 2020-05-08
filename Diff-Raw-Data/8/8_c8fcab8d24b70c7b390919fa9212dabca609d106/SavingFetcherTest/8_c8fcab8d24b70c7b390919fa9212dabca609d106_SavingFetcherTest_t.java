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
 
 package org.uriplay.query.uri;
 
 import java.util.Set;
 
 import org.jherd.remotesite.Fetcher;
 import org.jherd.remotesite.timing.RequestTimer;
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.uriplay.media.entity.Brand;
 import org.uriplay.media.entity.Description;
 import org.uriplay.media.entity.Item;
 import org.uriplay.persistence.content.MutableContentStore;
 
 import com.google.common.collect.Sets;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 @SuppressWarnings("unchecked")
 public class SavingFetcherTest extends MockObjectTestCase {
 
 	Fetcher<Object> delegateFetcher = mock(Fetcher.class);
 	MutableContentStore store = mock(MutableContentStore.class);
 	
 	String URI = "http://example.com";
 	Item item1 = new Item();
 	Brand brand = new Brand();
 	Set<Description> itemAndBrand = Sets.newHashSet(item1, brand);
 	RequestTimer timer = mock(RequestTimer.class);
 	
 	public void testFetchesItemsFromDelegateAndSavesToStore() throws Exception {
 		
 		checking(new Expectations() {{ 
			one(delegateFetcher).fetch(URI, timer); will(returnValue(item1));
			one(store).createOrUpdateGraph(Sets.newHashSet(item1), false);
 		}});
 		
 		new SavingFetcher(delegateFetcher, store).fetch(URI, timer);
 	}
 	
 }
