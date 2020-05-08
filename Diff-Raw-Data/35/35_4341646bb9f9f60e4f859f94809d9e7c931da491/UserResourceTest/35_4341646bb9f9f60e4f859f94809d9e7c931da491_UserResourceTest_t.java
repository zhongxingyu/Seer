 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 
 package org.lafayette.server.webapp.api.resources;
 
 import java.io.IOException;
 import org.codehaus.jettison.json.JSONException;
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.*;
 import org.lafayette.server.testing.ConetxtListeners;
 import org.lafayette.server.testing.ResourceTestCase;
 import org.lafayette.server.web.http.Constants;
 import org.lafayette.server.web.http.MediaType;
 import org.lafayette.server.web.servlet.ExtendedContextListener;
 import org.lafayette.server.webapp.api.servlet.ApiContextListener;
 
 /**
 * Tests for {@link UserResource}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 @ConetxtListeners({
     ExtendedContextListener.class,
     ApiContextListener.class
 })
 public class UserResourceTest extends ResourceTestCase {
 
     @Test
     public void indexAsUriList() throws JSONException, IOException {
         assertThat(resource().path("/user").accept(MediaType.TEXT_URI_LIST).get(String.class),
             is(equalTo("# Available URIs" + Constants.NL
                 + "# Version: " + getVersion() + " - Stage: " + STAGE + Constants.NL
                 + Constants.NL
                 + "http://localhost:9998/api/user/%7Bid%7D" + Constants.NL)));
     }
 
 }
