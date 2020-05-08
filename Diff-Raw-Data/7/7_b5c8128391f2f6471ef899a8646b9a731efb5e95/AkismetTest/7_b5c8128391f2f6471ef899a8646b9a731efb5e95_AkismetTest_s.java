 /**
  * Created by Michael Simons, michael-simons.eu
  * and released under The BSD License
  * http://www.opensource.org/licenses/bsd-license.php
  *
  * Copyright (c) 2011, Michael Simons
  * All rights reserved.
  *
  * Redistribution  and  use  in  source   and  binary  forms,  with  or   without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source   code must retain   the above copyright   notice,
  *   this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary  form must reproduce  the above copyright  notice,
  *   this list of conditions  and the following  disclaimer in the  documentation
  *   and/or other materials provided with the distribution.
  *
  * * Neither the name  of  michael-simons.eu   nor the names  of its contributors
  *   may be used  to endorse   or promote  products derived  from  this  software
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
  * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
  * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
  * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
  * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package ac.simons.tests.akismet;
 
 import junit.framework.Assert;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.junit.Test;
 
 import ac.simons.akismet.Akismet;
 import ac.simons.akismet.AkismetComment;
 import ac.simons.akismet.AkismetException;
 
 /**
  * @author Michael J. Simons
  */
 
 public class AkismetTest {
 	private final static String validApiKey;
 	private final static String validApiConsumer;
 	
 	static {
 		validApiKey = System.getProperty("akismetApiKey");
 		validApiConsumer = System.getProperty("akismetConsumer");
 		
 		if(validApiKey == null || validApiConsumer == null)
 			throw new RuntimeException("Both api key and consumer must be specified!");
 	}
 	
 	@Test
 	public void verify() throws AkismetException {
 		final Akismet akismet = new Akismet(new DefaultHttpClient());		
 		
 		akismet.setApiKey(validApiKey);		
 		akismet.setApiConsumer(validApiConsumer);		
 		Assert.assertTrue(akismet.verifyKey());
 		
 		akismet.setApiKey("123test");		
 		akismet.setApiConsumer("http://test.com");
		Assert.assertFalse(akismet.verifyKey());
		
		akismet.setApiEndpoint("test.com");
		akismet.setApiKey("123test");		
		akismet.setApiConsumer("http://test.com");
		Assert.assertFalse(akismet.verifyKey());
 	}
 	
 	@Test
 	public void checkComment() throws AkismetException {
 		final Akismet akismet = new Akismet(new DefaultHttpClient());		
 		akismet.setApiKey(validApiKey);		
 		akismet.setApiConsumer(validApiConsumer);
 		
 		AkismetComment comment = new AkismetComment();
 		comment.setUserIp("80.138.52.114");
 		comment.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.552.224 Safari/534.10");
 		comment.setPermalink("http://dailyfratze.de/marie/2011/1/3");
 		comment.setCommentType("comment");
 		comment.setCommentAuthor("Michael");
 		comment.setCommentAuthorEmail("misi@planet-punk.de");
 		comment.setCommentAuthorUrl("http://planet-punk.de");
 		comment.setCommentContent("Scharfes Outfit :D");
 		Assert.assertFalse(akismet.commentCheck(comment));
 		
 		comment = new AkismetComment();
 		comment.setUserIp("80.138.52.114");
 		comment.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; de; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13");
 		comment.setPermalink("http://dailyfratze.de/marie/2011/1/3");
 		comment.setCommentType("comment");
 		comment.setCommentAuthor("viagra-test-123");
 		comment.setCommentAuthorEmail("viagra-test-123@test.com");
 		comment.setCommentAuthorUrl("http://test.com");
 		comment.setCommentContent("Scharfes Outfit :D");
 		Assert.assertTrue(akismet.commentCheck(comment));		
 	}
 	
 	@Test
 	public void submitSpam() throws AkismetException {
 		final Akismet akismet = new Akismet(new DefaultHttpClient());		
 		akismet.setApiKey(validApiKey);		
 		akismet.setApiConsumer(validApiConsumer);
 		
 		AkismetComment comment = new AkismetComment();
 		comment.setUserIp("201.45.14.18");
 		comment.setUserAgent("UserAgent");
 		comment.setPermalink("http://dailyfratze.de/app/news/show/256");
 		comment.setCommentType("comment");
 		comment.setCommentAuthor("yohlctfwnem");
 		comment.setCommentAuthorEmail("rcphwp@nvwcjd.com");
 		comment.setCommentAuthorUrl("http://dzhnjufiaxlf.com/");
 		comment.setCommentContent("yKWClC  <a href=\"http://thmntcyecyjz.com/\">thmntcyecyjz</a>, [url=http://bfvheegcdlmi.com/]bfvheegcdlmi[/url], [link=http://pizhqyywdhzu.com/]pizhqyywdhzu[/link], http://gowqkgqrfpag.com/");
 		Assert.assertTrue(akismet.submitSpam(comment));				
 	}
 	
 	@Test
 	public void submitHam() throws AkismetException {
 		final Akismet akismet = new Akismet(new DefaultHttpClient());		
 		akismet.setApiKey(validApiKey);		
 		akismet.setApiConsumer(validApiConsumer);
 		
 		AkismetComment comment = new AkismetComment();
 		comment.setUserIp("80.138.52.114");
 		comment.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.552.224 Safari/534.10");
 		comment.setPermalink("http://dailyfratze.de/marie/2011/1/3");
 		comment.setCommentType("comment");
 		comment.setCommentAuthor("Michael");
 		comment.setCommentAuthorEmail("misi@planet-punk.de");
 		comment.setCommentAuthorUrl("http://planet-punk.de");
 		comment.setCommentContent("Scharfes Outfit :D");
 		Assert.assertTrue(akismet.submitHam(comment));				
 	}
 }
