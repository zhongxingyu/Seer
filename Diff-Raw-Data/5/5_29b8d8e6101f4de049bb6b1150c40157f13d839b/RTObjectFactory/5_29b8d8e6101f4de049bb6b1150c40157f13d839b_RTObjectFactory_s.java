 /*
  * Copyright (c) 2010 Henrik Gustafsson <henrik.gustafsson@fnord.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 package se.fnord.rt.client.internal;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import se.fnord.rt.client.RTCustomField;
 import se.fnord.rt.client.RTHistory;
 import se.fnord.rt.client.RTHistoryAttributes;
 import se.fnord.rt.client.RTLinkType;
 import se.fnord.rt.client.RTQueue;
 import se.fnord.rt.client.RTTicket;
 import se.fnord.rt.client.RTTicketAttributes;
 import se.fnord.rt.client.RTUser;
 import se.fnord.rt.client.RTUserAttributes;
 
 public final class RTObjectFactory {
     private static final Pattern FIELD_PATTERN = Pattern.compile("^(.+) \\(([^)]+)?\\): (\\S+)(?: (.*))?$");
     private RTObjectFactory() {
     }
 
     public static RTUser createUser(final String data) {
         final Map<String, String> attributes = new HashMap<String, String>();
 
         ParseUtils.parseAttributes(data, attributes);
         ParseUtils.filterNotSet(attributes);
 
         EnumMap<RTUserAttributes, Object> fields = new EnumMap<RTUserAttributes, Object>(RTUserAttributes.class);
         for (RTUserAttributes attr : RTUserAttributes.values())
             if (attributes.containsKey(attr.getName()))
                 fields.put(attr, attr.parse(attributes.get(attr.getName())));
 
         return new RTUser(fields);
     }
 
     public static RTTicket createPartialTicket(final String data) {
         final Map<String, String> attributes = new HashMap<String, String>();
 
         ParseUtils.parseAttributes(data, attributes);
         ParseUtils.filterNotSet(attributes);
 
         final EnumMap<RTTicketAttributes, Object> fields = new EnumMap<RTTicketAttributes, Object>(RTTicketAttributes.class);
         final Iterator<Map.Entry<String, String>> i = attributes.entrySet().iterator();
         while (i.hasNext()) {
             final Map.Entry<String, String> e = i.next();
             final RTTicketAttributes attr = RTTicketAttributes.getByName(e.getKey());
             if (attr != null) {
                 i.remove();
                 fields.put(attr, attr.parse(e.getValue()));
             }
         }
 
         return new RTTicket(fields, attributes);
     }
 
     public static RTTicket createFullTicket(final String data, final String multiPartHistory, final String linksData) {
         final Map<String, String> attributes = new HashMap<String, String>();
 
         ParseUtils.parseAttributes(data, attributes);
         ParseUtils.filterNotSet(attributes);
 
         final EnumMap<RTTicketAttributes, Object> fields = new EnumMap<RTTicketAttributes, Object>(RTTicketAttributes.class);
         final Iterator<Map.Entry<String, String>> i = attributes.entrySet().iterator();
         while (i.hasNext()) {
             final Map.Entry<String, String> e = i.next();
             final RTTicketAttributes attr = RTTicketAttributes.getByName(e.getKey());
             if (attr != null) {
                 i.remove();
                 fields.put(attr, attr.parse(e.getValue()));
             }
         }
 
         final List<RTHistory> history = createHistory(multiPartHistory);
         final Map<RTLinkType, List<Integer>> links = createLinks(linksData);
 
         return new RTTicket(fields, attributes, history, links);
     }
 
     private static Map<RTLinkType, List<Integer>> createLinks(String linksData) {
         final Map<String, String> attributes = new HashMap<String, String>();
         ParseUtils.parseAttributes(linksData, attributes);
         ParseUtils.filterNotSet(attributes);
 
         final EnumMap<RTLinkType,List<Integer>> links = new EnumMap<RTLinkType, List<Integer>>(RTLinkType.class);
         for (Map.Entry<String, String> attribute : attributes.entrySet()) {
             final RTLinkType linkType = RTLinkType.getByName(attribute.getKey());
             if (linkType != null)
                 links.put(linkType, linkType.parse(attribute.getValue()));
         }
         return links;
     }
 
     public static List<RTTicket> createPartialTickets(final String multiPartTickets) {
         final String[] ticketParts = ParseUtils.splitMultiPart(multiPartTickets);
         final ArrayList<RTTicket> tickets = new ArrayList<RTTicket>(ticketParts.length);
         for (final String ticketPart : ticketParts) {
             tickets.add(createPartialTicket(ticketPart));
         }
         return tickets;
     }
 
     private static List<RTHistory> createHistory(final String multiPartHistory) {
         final String[] historyParts = ParseUtils.splitMultiPart(multiPartHistory);
         final ArrayList<RTHistory> history = new ArrayList<RTHistory>(historyParts.length);
         for (String historyPart : historyParts) {
             history.add(createHistoryItem(historyPart));
         }
         return history;
     }
 
     private static RTHistory createHistoryItem(final String data) {
         final Map<String, String> attributes = new HashMap<String, String>();
         ParseUtils.parseAttributes(data, attributes);
         ParseUtils.filterNotSet(attributes);
 
         EnumMap<RTHistoryAttributes, Object> fields = new EnumMap<RTHistoryAttributes, Object>(RTHistoryAttributes.class);
         for (RTHistoryAttributes attr : RTHistoryAttributes.values())
             if (attributes.containsKey(attr.getName()))
                 fields.put(attr, attr.parse(attributes.get(attr.getName())));
 
         return new RTHistory(fields);
     }
 
     private static RTCustomField createCustomField(final String data) {
         Matcher matcher = FIELD_PATTERN.matcher(data);
         if (!matcher.matches())
             throw new IllegalArgumentException(String.format("\"%s\" does not match pattern \"\"", data, FIELD_PATTERN.pattern()));
         return new RTCustomField(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
     }
 
     private static List<RTCustomField> createCustomFields(final String[] fieldDescriptions) {
         final List<RTCustomField> fields = new ArrayList<RTCustomField>(fieldDescriptions.length);
         for (final String fieldDescription : fieldDescriptions) {
             final String d = fieldDescription.trim();
             if (!d.isEmpty() && !d.startsWith("id:"))
                 fields.add(createCustomField(fieldDescription));
         }
         return fields;
     }
 
     public static RTQueue createQueue(final String queueInfo, final String queueFields, final String ticketFields) {
         List<RTCustomField> qf = createCustomFields(ParseUtils.splitLines(queueFields));
         List<RTCustomField> tf = createCustomFields(ParseUtils.splitLines(ticketFields));
 
         final Map<String, String> attributes = new HashMap<String, String>();
         ParseUtils.parseAttributes(queueInfo, attributes);
         ParseUtils.filterNotSet(attributes);
        return new RTQueue(Integer.parseInt(attributes.get("id")), attributes.get("Name"), attributes.get("Description"), qf, tf);
     }
 
 }
