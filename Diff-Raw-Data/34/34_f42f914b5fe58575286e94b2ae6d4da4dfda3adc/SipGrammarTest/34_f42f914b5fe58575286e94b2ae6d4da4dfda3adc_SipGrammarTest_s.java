 // ========================================================================
 // Copyright 2007-2008 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 package org.cipango.sip;
 
 import javax.servlet.sip.ServletParseException;
 import javax.servlet.sip.URI;
 
 import org.cipango.sip.SipGrammar;
 import org.cipango.sip.URIFactory;
 
 
 import junit.framework.TestCase;
 
 public class SipGrammarTest extends TestCase
 {
 	public void testToken()
 	{
 		assertTrue(SipGrammar.isToken("INVITE"));
 		assertTrue(SipGrammar.isToken("+hello+-%world%!"));
 		assertFalse(SipGrammar.isToken("(hello"));
 	}
 	
 	public void testLWS()
 	{
 		assertTrue(SipGrammar.isLWS(' '));
 		assertTrue(SipGrammar.isLWS('\r'));	
 		assertTrue(SipGrammar.isLWS('\t'));		
 		assertFalse(SipGrammar.isLWS('a'));
 	}
 	
 	public void testURIScheme()
 	{
 		assertTrue(SipGrammar.isURIScheme("http"));
 		assertTrue(SipGrammar.isURIScheme("hello+world"));
 		assertFalse(SipGrammar.isURIScheme(".foo"));
 	}
 	
 	public void testGenericUri() throws Exception
 	{
 		URI uri = URIFactory.parseURI("foo://bar");
 		assertEquals("foo", uri.getScheme());
 		assertEquals("foo://bar", uri.toString());
 		
 		try
 		{
 			URIFactory.parseURI("1foo://bar");
 			fail();
 		}
 		catch (ServletParseException _)
 		{ }
 	}
 }
