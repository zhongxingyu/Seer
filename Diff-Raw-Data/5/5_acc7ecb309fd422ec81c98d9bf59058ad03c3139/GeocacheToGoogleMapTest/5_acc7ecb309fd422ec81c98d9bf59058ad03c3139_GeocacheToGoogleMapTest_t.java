 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity.main.intents;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.ResourceProvider;
 import com.google.code.geobeagle.activity.main.intents.GeocacheToGoogleMap;
 
 import org.junit.Test;
 
 import java.util.Locale;
 
 public class GeocacheToGoogleMapTest {
     @Test
     public void testConvert() {
         Locale.setDefault(Locale.GERMANY);
         
         ResourceProvider resourceProvider = createMock(ResourceProvider.class);
         Geocache geocache = createMock(Geocache.class);
        expect(geocache.getIdAndName()).andReturn("GCFOO pb & j");
         expect(geocache.getLatitude()).andReturn(37.123);
         expect(geocache.getLongitude()).andReturn(122.345);
         expect(resourceProvider.getString(R.string.map_intent)).andReturn(
                 "geo:0,0?q=%1$.5f,%2$.5f (%3$s)");
 
         replay(geocache);
         replay(resourceProvider);
         GeocacheToGoogleMap geocacheToCachePage = new GeocacheToGoogleMap(resourceProvider);
        assertEquals("geo:0,0?q=37.12300,122.34500 (GCFOO pb &amp; j)", geocacheToCachePage.convert(geocache));
         verify(geocache);
         verify(resourceProvider);
     }
 }
