 /*
  * Copyright (C) 2010 Benoit Guerout <bguerout at gmail dot com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package fr.keemto.web;
 
 import com.google.common.collect.Lists;
 import fr.keemto.core.Event;
 import fr.keemto.core.EventRepository;
 import org.codehaus.jackson.JsonNode;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.mockito.Mockito.when;
 
 public class EventControllerTest extends ControllerTestCase {
 
    @Mock
     private EventRepository eventRepository;
 
     private EventController controller;
 
     @Before
     public void prepare() throws Exception {
        controller = new EventController(eventRepository);
         request.addHeader("Accept", "application/json");
     }
 
     @Test
     public void shouldReturnAllEvents() throws Exception {
 
         request.setMethod("GET");
         request.setRequestURI("/api/events");
 
         ArrayList<Event> events = Lists.newArrayList(new Event(1, "user1", "message", "provider"));
         when(eventRepository.getAllEvents()).thenReturn(events);
 
         handlerAdapter.handle(request, response, controller);
 
         assertThat(response.getStatus(), equalTo(200));
         JsonNode eventsAsJSon = toJsonNode(response.getContentAsString());
         assertThat(eventsAsJSon, notNullValue());
         assertThat(eventsAsJSon.findPath("user").getValueAsText(), equalTo("user1"));
     }
 
 }
