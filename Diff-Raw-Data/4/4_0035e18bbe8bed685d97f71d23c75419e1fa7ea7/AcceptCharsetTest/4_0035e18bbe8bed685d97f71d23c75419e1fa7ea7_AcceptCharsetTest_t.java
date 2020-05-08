 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2003 Jim Wright
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Jim Wright <jimw@paneris.org>
  *     Bohemian Enterprise
  *     Predmerice nad Jizerou 77
  *     294 74
  *     Mlada Boleslav
  *     Czech Republic
  */
 
 package org.melati.util.test;
 
 import org.melati.util.AcceptCharset;
 
 import junit.framework.TestCase;
 
 import java.util.*;
 
 /**
  * Tests the corresponding class in the superpackage.
  *
  * @see AcceptCharset
  * @author jimw@paneris.org
  * @version $Version$
  */
 public class AcceptCharsetTest extends TestCase {
 
   public AcceptCharsetTest(String testCaseName) {
     super(testCaseName);
   }
 
   /**
    * Test choosing charsets.
    */
   public void testChoices() throws Exception {
 
     String headerValue = "ISO-8859-2, utf-8;q=0.66, *;q=0.66";
     String supportedPreference[] = new String[] {
       "UTF-16",
       "UTF-8",
       "ISO-8859-1",
     };
     AcceptCharset ac = new AcceptCharset(headerValue, supportedPreference);
     assertEquals("ISO-8859-2", ac.clientChoice());
     assertEquals("UTF-16", ac.serverChoice());
 
     headerValue = "utf-8;q=0.66,ISO-8859-3,ISO-8859-2";
     supportedPreference = new String[] {
       "ISO-8859-1",
       "UTF-16",
       "UTF-8",
       "BOLLOX",
     };
     ac = new AcceptCharset(headerValue, supportedPreference);
     assertEquals("ISO-8859-3", ac.clientChoice());
     assertEquals("ISO-8859-1", ac.serverChoice());
 
     headerValue = "*;q=0.0";
     supportedPreference = new String[] {
       "UTF-16",
       "UTF-8",
       "BOLLOX",
       "ISO-8859-1",
     };
     ac = new AcceptCharset(headerValue, supportedPreference);
     assertEquals(null, ac.clientChoice());
     assertEquals(null, ac.serverChoice());

    ac = new AcceptCharset(null, supportedPreference);
    assertEquals("ISO-8859-1", ac.clientChoice());
    assertEquals("ISO-8859-1", ac.serverChoice());
   }
 
 }
 
