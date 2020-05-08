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
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.config.ConfigBinder;
 import com.dmdirc.config.ConfigBinding;
 import com.dmdirc.events.ChannelUserAwayEvent;
 import com.dmdirc.events.ChannelUserBackEvent;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.ui.messages.ColourManager;
 import com.dmdirc.util.colours.Colour;
 
 import javax.inject.Inject;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Adds away colours to DMDirc.
  */
 public class AwayColoursManager {
 
     private final DMDircMBassador eventBus;
     private final ConfigBinder binder;
     private final ColourManager colourManager;
     private Colour colour = Colour.BLACK;
     private boolean nicklist = true;
     private boolean text = true;
 
     @Inject
     public AwayColoursManager(final DMDircMBassador eventBus,
             @GlobalConfig final AggregateConfigProvider config,
             @PluginDomain(AwayColoursPlugin.class) final String domain,
             @GlobalConfig final ColourManager colourManager) {
         this.eventBus = eventBus;
         this.colourManager = colourManager;
         binder = config.getBinder().withDefaultDomain(domain);
     }
 
     public void load() {
         eventBus.subscribe(this);
         binder.bind(this, AwayColoursManager.class);
     }
 
     public void unload() {
         eventBus.unsubscribe(this);
         binder.unbind(this);
     }
 
     @ConfigBinding(key = "colour")
     public void handleColourChange(final String colour) {
         this.colour = colourManager.getColourFromString(colour, Colour.GRAY);
     }
 
     @ConfigBinding(key = "nicklist")
     public void handleNicklistChange(final boolean nicklist) {
         this.nicklist = nicklist;
     }
 
     @ConfigBinding(key = "text")
     public void handleTextChange(final boolean text) {
         this.text = text;
     }
 
     @Handler
     public void handleAwayEvent(final ChannelUserAwayEvent event) {
         if (nicklist) {
             event.getUser().getMap().put(ChannelClientProperty.NICKLIST_FOREGROUND, colour);
         }
         if (text) {
             event.getUser().getMap().put(ChannelClientProperty.TEXT_FOREGROUND, colour);
         }
     }
 
     @Handler
     public void handleBackEvent(final ChannelUserBackEvent event) {
         if (nicklist) {
             event.getUser().getMap().remove(ChannelClientProperty.NICKLIST_FOREGROUND);
         }
         if (text) {
             event.getUser().getMap().remove(ChannelClientProperty.TEXT_FOREGROUND);
         }
     }
 }
