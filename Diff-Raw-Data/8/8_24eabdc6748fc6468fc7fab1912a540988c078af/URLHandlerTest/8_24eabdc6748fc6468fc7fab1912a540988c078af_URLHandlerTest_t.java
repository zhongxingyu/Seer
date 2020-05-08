 /*******************************************************************************
  * Copyright (c) 2010 Ryan Crichton.
  * 
  * This file is part of Jembi SDMX-HD Library.
  * 
  * Jembi SDMX-HD Library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Jembi SDMX-HD Library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with Jembi SDMX-HD Library.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 /*
  * Copyright (c) 2004-2005, University of Oslo
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  * * Neither the name of the <ORGANIZATION> nor the names of its contributors may
  *   be used to endorse or promote products derived from this software without
  *   specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jembi.sdmxhd.util;
 
 import java.io.InputStream;
 import junit.framework.TestCase;
 import org.junit.Test;
import org.jembi.sdmxhd.util.URLHandler;;
 
 /**
  * 
  * @author bobj
  * @version created 11-Dec-2009
  */
 public class URLHandlerTest extends TestCase {
 
 	protected URLHandler uri_handler;
 
 	protected void setUp() throws Exception {
 		// set base url to sample zip file
		String sample = "test/org/jembi/sdmxhd/include/SDMX-HD.v1.0 sample1.zip";
 		String baseURL = "jar:file:" + sample + "!";
 		// create a handler for this zipfile
 		uri_handler = new URLHandler(baseURL);
 	}
 
 	@Test
 	public void testZipURI() {
 
 		try {
 			InputStream in_stream = uri_handler
 					.getInputStream("./COMMON/SDMX-HD/v1.0/codelists/CL_LOGICAL+SDMX-HD+1.0.xml");
 		} catch (Exception e) {
 			System.err.println("oops: " + e);
 			assert (false);
 		}
 	}
 
 	@Test
 	public void testHttpURI() {
 
 		try {
 			InputStream in_stream = uri_handler
 					.getInputStream("http://www.google.com/");
 		} catch (Exception e) {
 			System.err.println("oops: " + e);
 			assert (false);
 		}
 	}
 
 }
