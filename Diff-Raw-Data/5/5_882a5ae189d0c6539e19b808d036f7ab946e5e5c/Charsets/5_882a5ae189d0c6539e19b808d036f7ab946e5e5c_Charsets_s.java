 /*
  * Copyright (c) 2008-2012 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License.  You may
  * obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.  See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package iudex.util;
 
 import java.nio.charset.Charset;
 
 public class Charsets extends com.gravitext.util.Charsets
 {
     // Windows charsets
     public static final Charset WINDOWS_874 =
             Charset.forName( "windows-874" );
     public static final Charset WINDOWS_949 =
             Charset.forName( "windows-949" );
     public static final Charset WINDOWS_31J =
             Charset.forName( "windows-31j" );
     public static final Charset WINDOWS_1252 =
         Charset.forName( "windows-1252" );
     public static final Charset WINDOWS_1254 =
             Charset.forName( "windows-1254" );
 
     // ISO charsets
     public static final Charset ISO_8859_9 =
             Charset.forName( "ISO-8859-9" );
     public static final Charset ISO_8859_11 =
             Charset.forName( "ISO-8859-11" );
    
     // Local charsets
     public static final Charset KS_C_5601_1987 =
             Charset.forName( "KS_C_5601-1987" );
     public static final Charset SHIFT_JIS =
             Charset.forName( "Shift_JIS" );
     public static final Charset TIS_620 =
             Charset.forName( "TIS-620" );
     public static final Charset EUC_KR =
             Charset.forName( "EUC-KR" );
     public static final Charset GBK =
             Charset.forName( "GBK" );
     public static final Charset GB2312 =
             Charset.forName( "GB2312" );
 
     /**
      * Expand provided encoding to a super-set encoding if possible, to maximize
      * character mappings. Note that some mappings are excluded because the destination
      * encodings are not supported by the JVM.
     * 
      * @see http://www.whatwg.org/specs/web-apps/current-work/multipage/parsing.html#character-encodings-0
      */
     public static Charset expand( final Charset in ) {
         Charset out = in;
 
         if( in.equals( ASCII ) ||
             in.equals( ISO_8859_1 ) )
         {
             out = WINDOWS_1252;
         }
         else if( in.equals( SHIFT_JIS ))
         {
             out = WINDOWS_31J;
         }
         else if( in.equals( GB2312 ) )
         {
             out = GBK;
         }
         else if( in.equals( ISO_8859_11 ) ||
                  in.equals( TIS_620 ) )
         {
             out = WINDOWS_874;
         }
         else if( in.equals( EUC_KR ) ||
                  in.equals( KS_C_5601_1987 ) )
         {
             out = WINDOWS_949;
         }
         else if( in.equals( ISO_8859_9 ))
         {
             out = WINDOWS_1254;
         }
 
         return out;
     }
 
     public static Charset defaultCharset()
     {
         return _default;
     }
 
     private static Charset _default = WINDOWS_1252;
 }
