 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 package com.dmdirc.addons.parser_xmpp;
 
 import com.dmdirc.parser.common.AwayState;
 import com.dmdirc.parser.common.BaseClientInfo;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

import com.google.common.collect.Lists;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.packet.Presence.Mode;
 import org.jivesoftware.smack.packet.Presence.Type;
 
 /**
  * An XMPP-specific client info object.
  */
 public class XmppClientInfo extends BaseClientInfo {
 
     /** A map of known endpoints for this client. */
     private final Map<String, XmppEndpoint> endpoints = new HashMap<>();
 
     /**
      * Creates a new XMPP Client Info object with the specified details.
      *
      * @param parser The parser that owns this client info object
      * @param nick   The nickname of the user this object represents
      * @param user   The username of the user this object represents
      * @param host   The hostname of the user this object represents
      */
     public XmppClientInfo(final XmppParser parser, final String nick, final String user,
             final String host) {
         super(parser, nick, user, host);
     }
 
     @Override
     public int getChannelCount() {
         return 0;
     }
 
    @Override
    public List<ChannelClientInfo> getChannelClients() {
        return Lists.newArrayList();
    }

     /**
      * Retrieves the map of known endpoints for this client.
      *
      * @return A map of endpoint names to presence states.
      */
     public Map<String, XmppEndpoint> getEndpoints() {
         return Collections.unmodifiableMap(endpoints);
     }
 
     /**
      * Sets the presence of one endpoint of this client.
      *
      * @param packet The presence packet received
      */
     public void setPresence(final Presence packet) {
         final String[] parts = getParser().parseHostmask(packet.getFrom());
         final boolean wasAway = isAway();
 
         if (packet.getType() == Type.unavailable) {
             endpoints.remove(parts[1]);
         } else {
             if (!endpoints.containsKey(parts[1])) {
                 endpoints.put(parts[1], new XmppEndpoint());
             }
 
             final XmppEndpoint endpoint = endpoints.get(parts[1]);
             endpoint.setPriority(packet.getPriority());
             endpoint.setMode(packet.getMode());
             endpoint.setType(packet.getType());
             endpoint.setStatus(packet.getStatus());
         }
 
         final boolean isAway = isAway();
 
         if (wasAway != isAway) {
             // Their away status has changed
             getParser().handleAwayStateChange(this, wasAway);
         }
     }
 
     /**
      * Determines whether or not this contact is "away" - that is, their aggregate presence state is
      * "Unavailable", "Away", "DND" or "XA".
      *
      * @return True if the client is away, false otherwise
      */
     public boolean isAway() {
         final Mode presence = getAggregatePresence();
 
         return presence == null || presence.compareTo(Mode.away) >= 0;
     }
 
     @Override
     public AwayState getAwayState() {
         return isAway() ? AwayState.AWAY : AwayState.HERE;
     }
 
     /**
      * Gets the aggregate presence of this contact.
      *
      * @return The highest available state of the contact's highest priority endpoints, * or
      *         <code>null</code> if no endpoints are available.
      */
     public Mode getAggregatePresence() {
         final List<XmppEndpoint> sortedEndpoints = new ArrayList<>(endpoints.values());
         Collections.sort(sortedEndpoints);
 
         // Get the set of the highest priority endpoints
         final Set<XmppEndpoint> bestEndpoints = new HashSet<>(sortedEndpoints.size());
         int best = 0;
         for (XmppEndpoint endpoint : sortedEndpoints) {
             if (endpoint.getType() != Type.available) {
                 continue;
             }
 
             if (bestEndpoints.isEmpty()) {
                 best = endpoint.getPriority();
             }
 
             if (endpoint.getPriority() == best && best >= 0) {
                 bestEndpoints.add(endpoint);
             }
         }
 
         // Find the best presence
         Mode bestPresence = null;
         for (XmppEndpoint endpoint : bestEndpoints) {
             final Mode targetMode = endpoint.getMode() == null ? Mode.available : endpoint.getMode();
             if (bestPresence == null || bestPresence.compareTo(targetMode) > 0) {
                 bestPresence = targetMode;
             }
         }
 
         return bestPresence;
     }
 
     /**
      * Updates this client with information from the roster.
      *
      * @param entry The roster entry to update this client with
      */
     public void setRosterEntry(final RosterEntry entry) {
         setRealname(entry.getName());
     }
 
     @Override
     public XmppParser getParser() {
         return (XmppParser) super.getParser();
     }
 
 }
