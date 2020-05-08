 package com.drtshock.willie.command.utility;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
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
 import java.sql.Date;
 import java.text.SimpleDateFormat;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 public class AuthorCommandHandler implements CommandHandler {
 
     private static final Logger LOG = Logger.getLogger(AuthorCommandHandler.class.getName());
 
     private SimpleDateFormat dateFormat;
 
     public AuthorCommandHandler() {
         this.dateFormat = new SimpleDateFormat("EEEE dd MMMM YYYY");
     }
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) throws Exception {
         LOG.info("Started to handle !author command from " + sender.getNick() + "...");
         if (args.length != 1 && args.length != 2) {
             nope(channel);
             return;
         }
 
         try {
             int amount = 5;
             if (args.length == 2) {
                 try {
                     amount = Integer.parseInt(args[1]);
                 } catch (NumberFormatException e) {
                     nope(channel);
                     return;
                 }
 
                 if (amount == 0) {
                     nope(channel);
                     return;
                 }
             }
 
             LOG.info("Selected amount: " + amount);
 
             SortedSet<Plugin> plugins = new TreeSet<>();
             boolean hasNextPage;
             Document document;
             String devBukkitLink = "http://dev.bukkit.org/";
             String profilePageLink = devBukkitLink + "profiles/" + args[0];
             String nextPageLink = profilePageLink + "/bukkit-plugins/";
             do {
                 // Get the page
                 LOG.info("Getting page \"" + nextPageLink + "\"...");
                 document = getPage(nextPageLink);
 
                 // Check if we will have to look at another page
                 Elements pages = document.getElementsByClass("listing-pagination-pages").get(0).children();
                 if (pages.size() > 1) {
                     Element lastLink = pages.get(pages.size() - 1).child(0);
                     if (lastLink.ownText().trim().startsWith("Next")) {
                         hasNextPage = true;
                         nextPageLink = devBukkitLink + lastLink.attr("href");
                     } else {
                         hasNextPage = false;
                         nextPageLink = null;
                     }
                 } else {
                     hasNextPage = false;
                     nextPageLink = null;
                 }
 
                 // List stuff on this page
                 Plugin plugin;
                 String date;
                 Elements pluginsTd = document.getElementsByClass("col-project");
                 for (Element e : pluginsTd) {
                     if ("td".equalsIgnoreCase(e.tagName())) {
                         plugin = new Plugin();
                         plugin.name = e.getElementsByTag("h2").get(0).getElementsByTag("a").get(0).ownText().trim();
                        date = e.nextElementSibling().attr("data-epoch");
                         try {
                             plugin.lastUpdate = Long.parseLong(date);
                         } catch (NumberFormatException ex) {
                             channel.sendMessage(Colors.RED + "An error occured: Cannot parse \"" + date + "\" as a long.");
                            LOG.info(e.nextElementSibling().html());
                             return;
                         }
                         LOG.info("Adding plugin " + plugin.name);
                         plugins.add(plugin);
                     }
                 }
             } while (hasNextPage);
 
             String name = document.getElementsByTag("h1").get(1).ownText().trim();
             String nbPlugins = document.getElementsByClass("listing-pagination-pages-total").get(0).ownText().trim();
 
             Iterator<Plugin> it = plugins.iterator();
 
             channel.sendMessage(name + " (" + profilePageLink + ")");
             channel.sendMessage("Plugins: " + nbPlugins);
             if (plugins.isEmpty()) {
                 channel.sendMessage(Colors.RED + "Unknown user or user without plugins");
             } else if (amount == 1) {
                 Plugin plugin = it.next();
                 channel.sendMessage("Last updated plugin: " + plugin.name + " (" + formatDate(plugin.lastUpdate) + ")");
             } else {
                 channel.sendMessage(amount + " last updated plugins:");
                 int i = 0;
                 while (it.hasNext() && i < amount) {
                     Plugin plugin = it.next();
                     channel.sendMessage("- " + plugin.name + " (" + formatDate(plugin.lastUpdate) + ")");
                     i++;
                 }
             }
 
             LOG.info("Command execution successful!");
         } catch (FileNotFoundException | MalformedURLException e) {
             channel.sendMessage(Colors.RED + "Unable to find that user!");
         } catch (IOException e) {
             channel.sendMessage(Colors.RED + "Failed: " + e.getMessage());
             throw e;
         }
     }
 
     private final class Plugin implements Comparable<Plugin> {
 
         public String name;
         public long lastUpdate;
 
         @Override
         public int compareTo(Plugin o) {
             return -Long.compare(lastUpdate, o.lastUpdate);
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
 
     private void nope(Channel channel) {
         channel.sendMessage(Colors.RED + "Look up an author with !author <name> [amount]");
     }
 
     private String formatDate(long date) {
         return this.dateFormat.format(new Date(date * 1000));
     }
 
 }
