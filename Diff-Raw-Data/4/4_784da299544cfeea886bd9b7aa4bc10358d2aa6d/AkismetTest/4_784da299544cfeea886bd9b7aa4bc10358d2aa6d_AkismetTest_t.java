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
 
 import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
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
 		
 		comment = new AkismetComment();
 		comment.setUserIp("92.99.136.158");
 		comment.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.5) Gecko/2008120122 Firefox/3.0.5");
 		comment.setPermalink("http://dailyfratze.de/tina/2009/6/5" );
 		comment.setCommentType("comment");
 		comment.setCommentAuthor("Yesid");
 		comment.setCommentAuthorEmail("");
 		comment.setCommentAuthorUrl("");
 		comment.setCommentContent("hello!This was a really otsitandung blog!I come from itlay, I was fortunate to approach your Topics in baiduAlso I obtain a lot in your topic really thanks very much  i will come later");
 		Assert.assertTrue(akismet.commentCheck(comment));
 		
 		comment = new AkismetComment();
 		comment.setUserIp("77.79.229.62");
 		comment.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.5) Gecko/2008120122 Firefox/3.0.5");
 		comment.setPermalink("http://dailyfratze.de/app/news/show/250" );
 		comment.setCommentType("comment");
 		comment.setCommentAuthor("Payal");
 		comment.setCommentContent("ett tips kan ju vara att du lc3a4gger ut olika magar ocksc3a5? Nu fc3b6rstc3a5r jag ju visserligen om du inte vill att din blogg ska fc3b6rvandlas till en blogg med massa elidbr pc3a5 random personers kroppar helt, men nc3a4r du c3a4ndc3a5 c3a4r inne pc3a5 spc3a5ret sc3a5 c3a4r ju mc3a5nga osc3a4kra pc3a5 sina magar. Jag vet att jag c3a4r det. Tvc3a5 personer har frc3a5gat mig helt random om jag c3a4r gravid trots att jag inte c3a4r det eftersom min mage putar ut (de kc3a4nner inte ens varandra). Jag c3a4lskar din blogg i vanliga fall, men nu c3a4lskar jag den c3a4nnu mer. Jag bc3b6rjar kc3a4nna lite att mina lc3a5r duger som de c3a4r. Tack Egoina fc3b6r att du bc3a5de inspirerar och bryr dig om personerna som lc3a4ser din blogg! <3");
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
