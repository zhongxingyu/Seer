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
 package org.lafayette.server.web.service.data;
 
 import java.io.File;
 import java.io.IOException;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.*;
 import org.junit.Rule;
 import org.junit.rules.TemporaryFolder;
 import org.lafayette.server.web.service.ServiceDescriptor;
 import org.mapdb.DB;
 import org.mapdb.DBMaker;
 import static org.mockito.Mockito.*;
 
 /**
  * Tests for {@link DataService}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class DataServiceTest {
 
     @Rule
    //CHECKSTYLE:OFF
     public final TemporaryFolder tmp = new TemporaryFolder();
    //CHECKSTYLE:ON
 
     @Test
     public void getPutAndDeleteData() throws JSONException, IOException {
         final File dbFile = tmp.newFile("db");
         final DB db = DBMaker.newFileDB(dbFile)
                 .make();
         final DataService sut = new DataService(db);
         assertThat(sut.getData("foo", "1"), is(nullValue()));
         JSONObject object = new JSONObject();
         object.put("foo", "bar");
         assertThat(sut.putData("foo", "1", object), is(nullValue()));
         object = sut.getData("foo", "1");
         assertThat(object.getString("foo"), is(equalTo("bar")));
         sut.deleteData("foo", "1");
         assertThat(sut.getData("foo", "1"), is(nullValue()));
     }
 
     @Test
     public void getDescription() {
         final DataService sut = new DataService(mock(DB.class));
         final ServiceDescriptor desc = sut.getDescription();
         assertThat(desc.getServiceDescription(), is(equalTo("Service to store JSON data.")));
         assertThat(desc.getApiDescription(), containsInAnyOrder(
                 "Get data by user name and unique id.",
                 "Store data by user name and unique id.",
                "Delete data by user name and unique id.",
                "Get all resource ids for an user."));
     }
 }
