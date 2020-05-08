 package com.github.alexesprit.chatlogs.parser;
 
 import com.github.alexesprit.chatlogs.item.Message;
 import com.github.alexesprit.chatlogs.util.Util;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public final class FreizeOrgLoader extends LogLoader {
     public FreizeOrgLoader(String conference) {
         super(conference);
     }
 
     @Override
     public ArrayList<Message> getMessages() {
         String content = Util.getURLContent(getURLWithDate());
         if (null != content) {
             ArrayList<Message> messages = new ArrayList<Message>();
            Pattern message = Pattern.compile(">\\[(.+?)\\].+?&lt;(.+?)&gt;</span> (.+)<br.*/>\n");
             Matcher matcher = message.matcher(content);
             while (matcher.find()) {
                 Message msg = new Message();
                 msg.nick = matcher.group(2);
                 msg.time = matcher.group(1);
                 msg.text = Util.removeTags(matcher.group(3));
                 messages.add(msg);
             }
             return messages;
         }
         return null;
     }
 
     @Override
     protected String getLogsRoot() {
         return "http://www.freize.org/log/logs/";
     }
 }
