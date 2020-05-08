 // vim:filetype=java:ts=4
 /*
 	Copyright (c) 2007
 	Conor McDermottroe.  All rights reserved.
 
 	Redistribution and use in source and binary forms, with or without
 	modification, are permitted provided that the following conditions
 	are met:
 	1. Redistributions of source code must retain the above copyright
 	   notice, this list of conditions and the following disclaimer.
 	2. Redistributions in binary form must reproduce the above copyright
 	   notice, this list of conditions and the following disclaimer in the
 	   documentation and/or other materials provided with the distribution.
 	3. Neither the name of the author nor the names of any contributors to
 	   the software may be used to endorse or promote products derived from
 	   this software without specific prior written permission.
 
 	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 	"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 	A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 	HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 	TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 	OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 	OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package junit.com.mcdermottroe.exemplar.utils;
 
 import java.text.MessageFormat;
 
 import com.mcdermottroe.exemplar.utils.Strings;
 
 import junit.com.mcdermottroe.exemplar.UtilityClassTestCase;
 
 /** Test class for {@link Strings}.
 
 	@author	Conor McDermottroe
 	@since	0.2
 */
 public class StringsTest extends UtilityClassTestCase {
	/** Basic sanity check for {@link Strings.formatMessage(String, Object...)}.
 	*/
 	public void testFormatMessageOneArg() {
 		String formatString = "{0}";
 		String variable = "variable";
 		if (variable.equals(Strings.formatMessage(formatString, variable))) {
 			assertTrue(
 				"Strings.formatMessage(String, Object) works as expected",
 				true
 			);
 		} else {
 			fail("Bad result from Strings.formatMessage(String, Object)");
 		}
 	}
 
	/** Basic sanity check for {@link Strings.formatMessage(String, Object...)}.
 	*/
 	public void testFormatMessageManyArgs() {
 		String formatMessage = "{{0}{}{1,date,long}}";
 		String result = Strings.formatMessage(formatMessage, "foo", 0);
 		String expectedResult =	"{foo{}" +
 								MessageFormat.format("{0,date,long}", 0) +
 								"}";
 
 		assertEquals(
 			"Strings.formatMessage(String, Object...)",
 			expectedResult,
 			result
 		);
 	}
 }
