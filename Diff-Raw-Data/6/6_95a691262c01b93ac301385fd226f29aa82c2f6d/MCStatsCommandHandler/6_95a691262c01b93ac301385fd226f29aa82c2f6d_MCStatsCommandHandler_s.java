 package com.drtshock.willie.command.minecraft;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import com.drtshock.willie.util.WebHelper;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class MCStatsCommandHandler implements CommandHandler {
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) throws Exception {
         if (args.length != 1) {
             channel.sendMessage(Colors.RED + "Look up a plugin with !plugin <name>");
             return;
         }
 
         String mcStatsURL = "http://mcstats.org/plugin/";
         String pluginStatsURL = mcStatsURL + args[0];
 
         try {
             Document doc = getPage(pluginStatsURL);
 
             PluginStats stats = PluginStats.get(doc);
 
            channel.sendMessage("Plugin: " + stats.name + " - Rank: " + stats.rank + " - " + WebHelper.shortenURL(pluginStatsURL));
            channel.sendMessage("Servers: " + stats.servers + " | Players: " + stats.players);
        } catch (FileNotFoundException | MalformedURLException e) {
             channel.sendMessage(Colors.RED + "Plugin unknown by MCStats");
         } catch (IOException e) {
             channel.sendMessage(Colors.RED + "Failed: " + e.getMessage());
             throw e; // Gist
         }
     }
 
     private Document getPage(String urlString) throws IOException {
         URL url = new URL(urlString);
 
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         connection.setUseCaches(false);
 
         BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
         StringBuilder buffer = new StringBuilder();
         String line;
 
         while ((line = input.readLine()) != null) {
             buffer.append(line);
             buffer.append('\n');
         }
 
         String page = buffer.toString();
 
         input.close();
 
         return Jsoup.parse(page);
     }
 
     private static class PluginStats {
         public String name;
         public String rank;
         public String servers;
         public String players;
 
         private PluginStats() {
         }
 
         public static PluginStats get(Document doc) {
             PluginStats res = new PluginStats();
             res.name = doc.getElementsByClass("open").get(0).getElementsByTag("strong").get(0).ownText().trim();
 
             Element statBoxes = doc.getElementsByClass("stat-boxes").get(0);
 
             Element rankLi = statBoxes.child(0);
             Element serversLi = statBoxes.child(1);
             Element playersLi = statBoxes.child(2);
 
             Element rankLiStrong = rankLi.getElementsByClass("right").get(0).getElementsByTag("strong").get(0);
             if (rankLiStrong.children().size() == 0) {
                 res.rank = rankLiStrong.ownText().trim();
             } else {
                 res.rank = rankLiStrong.child(0).ownText().trim();
             }
 
             Element serversLiStrong = serversLi.getElementsByClass("right").get(0).getElementsByTag("strong").get(0);
             if (serversLiStrong.children().size() == 0) {
                 res.servers = serversLiStrong.ownText().trim();
             } else {
                 res.servers = serversLiStrong.child(0).ownText().trim();
             }
 
             Element playersLiStrong = playersLi.getElementsByClass("right").get(0).getElementsByTag("strong").get(0);
             if (playersLiStrong.children().size() == 0) {
                 res.players = playersLiStrong.ownText().trim();
             } else {
                 res.players = playersLiStrong.child(0).ownText().trim();
             }
 
             return res;
         }
     }
 
 }
