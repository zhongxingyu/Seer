 package fr.esiea.esieaddress;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Profile;
 import org.springframework.http.MediaType;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 
 import java.nio.charset.Charset;
 
 import static org.springframework.http.MediaType.APPLICATION_JSON;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
 import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 /**
  * Copyright (c) 2013 ESIEA M. Labusquiere D. Déïs
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(
 		locations = {
 				"classpath*:spring/application-context.xml",
 				"file:src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml"
 		})
 @WebAppConfiguration
 public class SpringMvcIntegrationTest {
 
 	@Autowired
 	private WebApplicationContext wac;
 
 	private MockMvc mockMvc;
 	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
 	private String jsonContact = "{\"id\":\"id10\",\"lastname\":\"Labusquiere\",\"firstname\":\"Maxence\",\"email\":\"labusquiere@gmail.com\",\"actif\":\"true\"}\n";
 	private String jsonContactUpdate = "{\"id\":\"id10\",\"lastname\":\"Labusquiere\",\"firstname\":\"Maxence\",\"email\":\"labusquiere@gmail.com\",\"actif\":\"false\"}\n" +
 			"\"";
 
 	@Before
 	public void setUp() throws Exception {
 
 		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
         Authentication auth = new UsernamePasswordAuthenticationToken("test@gmail.com", "pwd");
         SecurityContext securityContext = SecurityContextHolder.getContext();
         securityContext.setAuthentication(auth);
 	}
 
 	@Test
 	public void ContactInsertUpdateGetDelete() throws Exception {
 		this.mockMvc.perform(post("/contacts").accept(APPLICATION_JSON)
 				.contentType(APPLICATION_JSON_UTF8)
 				.content(jsonContact))
 				.andDo(print())
 				.andExpect(status().isCreated());
 
 		this.mockMvc.perform(put("/contacts").accept(APPLICATION_JSON)
 				.contentType(APPLICATION_JSON_UTF8)
 				.content(jsonContactUpdate))
 				//.andDo(print())
 				.andExpect(status().isNoContent());
 
 		this.mockMvc.perform(get("/contacts/id10").accept(APPLICATION_JSON))
 				//.andDo(print())
 				//Assert Content
 				.andExpect(status().isOk());
 
 		this.mockMvc.perform(delete("/contacts/id10").accept(APPLICATION_JSON))
 				//.andDo(print())
				.andExpect(status().isOk());
 	}
 
 	@Test
 	public void ContactGetAll() throws Exception {
 		this.mockMvc.perform(get("/contacts").accept(APPLICATION_JSON))
 				.andDo(print())
 				.andExpect(status().isOk());
 	}
 }
