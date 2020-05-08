 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.parser;
 
 import com.github.fhirschmann.clozegen.lib.multiset.MapMultiset;
 import com.github.fhirschmann.clozegen.lib.parser.FrequencyParser;
 import com.google.common.collect.Multiset;
 import com.google.common.io.Resources;
 import java.io.IOException;
 import java.net.URL;
 import junit.framework.TestCase;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class FrequencyParserTest extends TestCase {
     private URL trigrams;
     private URL bigrams;
 
 
     @Before
     public void setUp() {
         trigrams = Resources.getResource(
                 "com/github/fhirschmann/clozegen/lib/frequency/trigrams.txt");
         bigrams = Resources.getResource(
                 "com/github/fhirschmann/clozegen/lib/frequency/bigrams.txt");
     }
 
     @Test
     public void testParseMultiset() throws IOException {
         Multiset<String> ms = FrequencyParser.parseMultiset(trigrams);
         assertEquals(806, ms.count("one of the"));
         assertEquals(188, ms.count("and in the"));
         assertEquals(174, ms.count("because of the"));
     }
 
     public void testParseMapMultiset() throws IOException {
         MapMultiset<String, String> mms = FrequencyParser.parseMapMultiset(bigrams, 0);
        assertEquals(1404, mms.get("one").count("of"));
 
         MapMultiset<String, String> mms2 = FrequencyParser.parseMapMultiset(bigrams, 1);
        assertEquals(436, mms2.get("of").count("because"));
     }
 }
