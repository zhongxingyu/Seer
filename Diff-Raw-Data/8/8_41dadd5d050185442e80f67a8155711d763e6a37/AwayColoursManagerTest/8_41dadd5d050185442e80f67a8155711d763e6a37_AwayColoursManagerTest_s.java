 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.awaycolours;
 
 import com.dmdirc.ChannelClientProperty;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.config.ConfigBinder;
 import com.dmdirc.events.ChannelUserAwayEvent;
 import com.dmdirc.events.ChannelUserBackEvent;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.parser.interfaces.ChannelClientInfo;
 import com.dmdirc.ui.messages.ColourManager;
 import com.dmdirc.util.colours.Colour;
 
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AwayColoursManagerTest {
 
     @Mock private DMDircMBassador eventBus;
     @Mock private AggregateConfigProvider config;
     @Mock private ConfigBinder binder;
     @Mock private ChannelUserAwayEvent awayEvent;
     @Mock private ChannelUserBackEvent backEvent;
     @Mock private ChannelClientInfo user;
     @Mock private Map<Object, Object> map;
     @Mock private ColourManager colourManager;
     private AwayColoursManager instance;
     private String red;
     private Colour redColour;
     private String black;
     private Colour blackColour;
 
     @Before
     public void setUp() throws Exception {
         red = "red";
         black = "black";
         redColour = Colour.RED;
         blackColour = Colour.BLACK;
        instance = new AwayColoursManager(eventBus, config, "test", colourManager);
         when(awayEvent.getUser()).thenReturn(user);
         when(backEvent.getUser()).thenReturn(user);
         when(user.getMap()).thenReturn(map);
         when(colourManager.getColourFromString(red, Colour.GRAY)).thenReturn(redColour);
         when(colourManager.getColourFromString(black, Colour.GRAY)).thenReturn(blackColour);
     }
 
     @Test
     public void testLoad() throws Exception {
         instance.load();
         verify(eventBus).subscribe(instance);
     }
 
     @Test
     public void testUnload() throws Exception {
         instance.unload();
         verify(eventBus).unsubscribe(instance);
     }
 
     @Test
     public void testHandleColourChange() throws Exception {
         instance.handleColourChange(red);
         instance.handleNicklistChange(true);
         instance.handleTextChange(true);
         instance.handleAwayEvent(awayEvent);
         verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, redColour);
         verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, redColour);
         instance.handleColourChange(black);
         instance.handleNicklistChange(true);
         instance.handleTextChange(true);
         instance.handleAwayEvent(awayEvent);
         verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, blackColour);
         verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, blackColour);
     }
 
     @Test
     public void testHandleNicklistChange() throws Exception {
         instance.handleColourChange(red);
         instance.handleNicklistChange(true);
         instance.handleTextChange(true);
         instance.handleAwayEvent(awayEvent);
         verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, redColour);
         verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, redColour);
         instance.handleColourChange(black);
         instance.handleNicklistChange(false);
         instance.handleTextChange(true);
         instance.handleAwayEvent(awayEvent);
         verify(map, never()).put(ChannelClientProperty.NICKLIST_FOREGROUND, blackColour);
         verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, blackColour);
     }
 
     @Test
     public void testHandleTextChange() throws Exception {
         instance.handleColourChange(red);
         instance.handleNicklistChange(true);
         instance.handleTextChange(true);
         instance.handleAwayEvent(awayEvent);
         verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, redColour);
         verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, redColour);
         instance.handleColourChange(black);
         instance.handleNicklistChange(true);
         instance.handleTextChange(false);
         instance.handleAwayEvent(awayEvent);
         verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, blackColour);
         verify(map, never()).put(ChannelClientProperty.TEXT_FOREGROUND, blackColour);
     }
 
     @Test
     public void testHandleAwayEvent() throws Exception {
         instance.handleNicklistChange(true);
         instance.handleTextChange(true);
         instance.handleBackEvent(backEvent);
         verify(map).remove(ChannelClientProperty.NICKLIST_FOREGROUND);
         verify(map).remove(ChannelClientProperty.TEXT_FOREGROUND);
     }
 
     @Test
     public void testHandleBackEvent() throws Exception {
         instance.handleColourChange(red);
         instance.handleNicklistChange(true);
         instance.handleTextChange(true);
         instance.handleAwayEvent(awayEvent);
         verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, redColour);
         verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, redColour);
     }
 }
