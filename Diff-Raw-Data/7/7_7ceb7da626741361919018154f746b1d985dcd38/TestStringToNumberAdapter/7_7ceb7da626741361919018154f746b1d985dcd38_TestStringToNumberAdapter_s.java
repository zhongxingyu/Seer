 /*
  * Copyright [2013] [Eric Poitras]
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package epic.data.adapters.adapters;
 
 import junit.framework.Assert;
 import org.junit.Test;
import epic.data.adapters.transforms.StringToNumberAdapter;
 
 import java.text.DecimalFormat;
 import java.util.Locale;
 
 /**
  * Test String to Numbers adapters.
  */
 public class TestStringToNumberAdapter {
 
     @Test
     public void testSimpleAdapt() {
 
         StringToNumberAdapter dfa = new StringToNumberAdapter( DecimalFormat.getIntegerInstance( Locale.ENGLISH ) );
         Number n = dfa.apply( "124" );
 
         Assert.assertEquals( n.intValue(), 124 );
     }
 
    @Test
     public void testSimpleFailure() {
         StringToNumberAdapter dfa = new StringToNumberAdapter( DecimalFormat.getIntegerInstance( Locale.ENGLISH ) );
         Number n = dfa.apply( "NAN" );
 
         Assert.assertEquals( n.intValue(), 124 );
 
     }
 
 }
