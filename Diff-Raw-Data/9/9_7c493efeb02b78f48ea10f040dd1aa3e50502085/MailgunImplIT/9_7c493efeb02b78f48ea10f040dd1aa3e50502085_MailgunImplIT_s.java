 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.threewks.thundr.mailgun;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 
 import org.apache.commons.lang3.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.threewks.thundr.http.service.HttpService;
 import com.threewks.thundr.http.service.ning.HttpServiceNing;
 import com.threewks.thundr.mailgun.Log.LogItem;
 
 public class MailgunImplIT {
 	private Mailgun mailgun;
 	private HttpService httpService;
 
 	@Before
 	public void before() {
 		httpService = new HttpServiceNing();
 		String key = String.format("%s%s%d", "key-8xv" + "qutmnj", "o3cchv5xv17rxltfcr8k6e", 7);
 		mailgun = new MailgunImpl(httpService, "thundr-mailgun-test.mailgun.org", key);
 	}
 
 	@Test
 	public void shouldSendAnEmail() {
 		MessageSend result = mailgun.sendEmail("test@thundr-mailgun-test.mailgun.org", "test.3wks@gmail.com", "conversation_1234-1234@thundr-mailgun-test.mailgun.org", "Test Mailgun", "<html><body><h1>Yeah!</h1</body></html>");
 
 		assertThat(result.id, is(not(nullValue())));
 
 		String expectedId = StringUtils.replaceChars(result.id, "<>", "");
 		String actualId = null;
 
		Log log = mailgun.log(0, 10);
 		for (LogItem item : log.items) {
 			if (item.message_id.equals(expectedId)) {
 				actualId = item.message_id;
 			}
 		}
 		assertThat(actualId, is(expectedId));
 	}
 
 	@Test
 	public void shouldReturnContentOnLogRequest() {
 		Log log = mailgun.log(0, 10);
 		assertThat(log.total_count, is(greaterThan(0)));
 	}
 }
