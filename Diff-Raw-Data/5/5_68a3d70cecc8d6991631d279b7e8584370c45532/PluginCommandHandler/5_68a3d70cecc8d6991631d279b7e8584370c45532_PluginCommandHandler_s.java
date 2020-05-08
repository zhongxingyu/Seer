 package com.drtshock.willie.command.utility;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import com.drtshock.willie.util.Tools;
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
 
 public class PluginCommandHandler implements CommandHandler {
 
     private SimpleDateFormat dateFormat;
 
     public PluginCommandHandler() {
         this.dateFormat = new SimpleDateFormat("YYYY-MM-dd");
     }
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) throws Exception {
         if (args.length != 1) {
             channel.sendMessage(Colors.RED + "Look up a plugin with !plugin <name>");
             return;
         }
 
         try {
             URL url = new URL("http://dev.bukkit.org/projects/" + args[0] + "/");
 
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
 
             Document document = Jsoup.parse(page);
 
             String name = document.getElementsByTag("h1").get(1).ownText().trim();
             StringBuilder authors = new StringBuilder();
             int downloads = Integer.parseInt(document.getElementsByAttribute("data-value").first().attr("data-value"));
 
             Elements containers = document.getElementsByClass("user-container");
 
             if (!containers.isEmpty()) {
                 authors.append(Tools.silence(containers.get(0).text().trim()));
             }
 
             for (int i = 1; i < containers.size(); ++i) {
                 authors.append(", ");
                 String author = containers.get(i).text().trim();
                 authors.append(Tools.silence(author));
             }
 
 	        String files;
 	        Elements filesList = document.getElementsByClass("file-type");
 	        if (filesList.size() > 0) {
 		        Element latest = filesList.get(0);
 		        String version = latest.nextElementSibling().ownText();
 		        String bukkitVersion = latest.parent().ownText().split("for")[1].trim();
		        long fileDate = Long.parseLong(latest.nextElementSibling().attr("data-epoch"));
 		        String date = this.dateFormat.format(new Date(fileDate * 1000));
		        files = "Latest File: " + version + " for " + bukkitVersion + "(" + date + ")";
 	        } else {
 		        files = "No files (yet!)";
 	        }
 
             channel.sendMessage(name + " (" + connection.getURL().toExternalForm() + ")");
             channel.sendMessage("Authors: " + authors.toString());
             channel.sendMessage("Downloads: " + downloads);
             channel.sendMessage(files);
         } catch (FileNotFoundException e) {
             channel.sendMessage(Colors.RED + "Project not found");
         } catch (MalformedURLException e) {
             channel.sendMessage(Colors.RED + "Unable to find that plugin!");
         } catch (IOException e) {
             channel.sendMessage(Colors.RED + "Failed: " + e.getMessage());
             throw e; // Gist
         }
     }
 }
