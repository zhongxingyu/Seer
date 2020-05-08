 package de.raion.xmppbot.filter;
 /*
  * #%L
  * XmppBot Core
  * %%
  * Copyright (C) 2012 Bernd Kiefer
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.jivesoftware.smack.packet.Message;
 
/**
 * filter which uses a regular expression for matching 
 *
 */
 public class MessageBodyMatchesFilter extends AbstractMessageFilter {
 
 	private Pattern pattern;
 
 	/**
 	 * @param regex
 	 *            the pattern to use
 	 * @throws PatternSyntaxException
 	 *             if the expressions syntax is invalid
 	 */
 	public MessageBodyMatchesFilter(String regex) throws PatternSyntaxException {
 		try {
 			pattern = Pattern.compile(regex);
 		} catch (NullPointerException e) {
 			throw new PatternSyntaxException("null is not a valid regex", null,
 					-1);
 		}
 	}
 
 	@Override
 	public boolean accept(Message aMessage) {
 
 		String body = aMessage.getBody();
 
 		if (body == null || body.equals("")) {
 			return false;
 		}
 
 		return pattern.matcher(body).find();
 	}
 
 
 	public void setPattern(String regex) {
 		setPattern(regex, Pattern.CASE_INSENSITIVE);
 	}
 
 	public void setPattern(String aPattern, int flags) throws PatternSyntaxException{
 		try {
 			pattern = Pattern.compile(aPattern, flags);
 		} catch (NullPointerException e) {
 			throw new PatternSyntaxException("null is not a valid regex", null,
 					-1);
 		}
 
 	}
 
 }
