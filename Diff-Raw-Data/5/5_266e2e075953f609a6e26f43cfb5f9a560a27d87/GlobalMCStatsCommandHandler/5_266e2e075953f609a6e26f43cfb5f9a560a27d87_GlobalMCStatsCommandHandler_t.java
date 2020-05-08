 package com.drtshock.willie.command.minecraft;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import com.drtshock.willie.util.Tools;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.Comparator;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class GlobalMCStatsCommandHandler implements CommandHandler {
 
     private static final Logger LOG = Logger.getLogger(GlobalMCStatsCommandHandler.class.getName());
     private static DecimalFormat formatter;
 
     public GlobalMCStatsCommandHandler() {
         formatter = new DecimalFormat();
         formatter.setMaximumFractionDigits(2);
         formatter.setGroupingSize(3);
         DecimalFormatSymbols symbol = new DecimalFormatSymbols();
         symbol.setGroupingSeparator(' ');
         symbol.setDecimalSeparator('.');
         formatter.setDecimalFormatSymbols(symbol);
     }
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) throws Exception {
         if (args.length != 0 && args.length != 1) {
             nope(channel);
             return;
         }
 
         try {
             if (args.length == 0) {
                 for (String msg : new GlobalStats().getMessages()) {
                     channel.sendMessage(msg);
                 }
             } else {
                 switch (args[0].toLowerCase()) {
                     case "auth":
                         channel.sendMessage(new AuthStats().getMessage());
                     default:
                         channel.sendMessage(Colors.RED + "Invalid argument.");
                         break;
                 }
             }
         } catch (FileNotFoundException | MalformedURLException | IndexOutOfBoundsException | NumberFormatException |
                 SocketTimeoutException e) {
             LOG.log(Level.INFO, "Could not contact MCStats API / or invalid response received", e);
             channel.sendMessage(Colors.RED + "Could not contact MCStats API / or invalid response received");
         } catch (IOException e) {
             channel.sendMessage(Colors.RED + "Failed: " + e.getMessage());
             throw e; // Gist
         }
     }
 
     private String getPage(String urlString) throws IOException {
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
 
         return page;
     }
 
     // !gstats
     private class GlobalStats {
 
         public final String serversAmount;
         public final String serversDiff;
         public final String serversMax;
         public final String serversMin;
         public final String serversAvg;
         public final String playersAmount;
         public final String playersDiff;
         public final String playersMax;
         public final String playersMin;
         public final String playersAvg;
 
         public GlobalStats() throws IOException {
             final String apiUrl = "http://api.mcstats.org/1.0/All+Servers/graph/Global+Statistics";
             final String jsonString = getPage(apiUrl);
             final JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
             final JsonObject data = jsonObject.getAsJsonObject("data");
             final JsonArray playersArray = data.getAsJsonArray("Players");
             final JsonArray serversArray = data.getAsJsonArray("Servers");
 
             // Players:
 
             // Convert the Json map to a reversed Java SortedMap
             final SortedMap<Long, Long> playersMap = new TreeMap<>(new Comparator<Long>() {
                 @Override
                 public int compare(Long x, Long y) {
                     return -Long.compare(x, y);
                 }
             });
             for (JsonElement a : playersArray) {
                 playersMap.put(a.getAsJsonArray().get(0).getAsLong(), a.getAsJsonArray().get(1).getAsLong());
             }
 
             long totalForAvg = 0, nbForAvg = 0;
             long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
             for (final Long value : playersMap.values()) {
                 totalForAvg += value;
                 nbForAvg++;
                 if (value < min) {
                     min = value;
                 }
                 if (value > max) {
                     max = value;
                 }
             }
             this.playersMin = formatter.format(min);
             this.playersMax = formatter.format(max);
             this.playersAvg = formatter.format(((double) totalForAvg) / ((double) nbForAvg));
             long lastAmount = playersMap.get(playersMap.lastKey());
             this.playersAmount = formatter.format(lastAmount);
             playersMap.remove(playersMap.lastKey());
             long diff = lastAmount - playersMap.get(playersMap.lastKey());
             if (diff > 0) {
                 this.playersDiff = Colors.DARK_GREEN + Colors.BOLD + "+" + diff + Colors.NORMAL;
             } else if (diff == 0) {
                this.playersDiff = Colors.DARK_GRAY + Colors.BOLD + "Â±" + diff + Colors.NORMAL;
             } else {
                 this.playersDiff = Colors.RED + Colors.BOLD + diff + Colors.NORMAL;
             }
 
             // Same thing for Servers:
 
             // Convert the Json map to a reversed Java SortedMap
             final SortedMap<Long, Long> serversMap = new TreeMap<>(new Comparator<Long>() {
                 @Override
                 public int compare(Long x, Long y) {
                     return -Long.compare(x, y);
                 }
             });
             for (JsonElement a : serversArray) {
                 serversMap.put(a.getAsJsonArray().get(0).getAsLong(), a.getAsJsonArray().get(1).getAsLong());
             }
 
             totalForAvg = 0;
             nbForAvg = 0;
             min = Long.MAX_VALUE;
             max = Long.MIN_VALUE;
             for (final Long value : serversMap.values()) {
                 totalForAvg += value;
                 nbForAvg++;
                 if (value < min) {
                     min = value;
                 }
                 if (value > max) {
                     max = value;
                 }
             }
             this.serversMin = formatter.format(min);
             this.serversMax = formatter.format(max);
             this.serversAvg = formatter.format(((double) totalForAvg) / ((double) nbForAvg));
             lastAmount = serversMap.get(serversMap.lastKey());
             this.serversAmount = formatter.format(lastAmount);
             serversMap.remove(serversMap.lastKey());
             diff = lastAmount - serversMap.get(serversMap.lastKey());
             if (diff > 0) {
                 this.serversDiff = Colors.DARK_GREEN + Colors.BOLD + "+" + diff + Colors.NORMAL;
             } else if (diff == 0) {
                this.serversDiff = Colors.DARK_GRAY + Colors.BOLD + "Â±" + diff + Colors.NORMAL;
             } else {
                 this.serversDiff = Colors.RED + Colors.BOLD + diff + Colors.NORMAL;
             }
 
         }
 
         public String[] getMessages() {
             final String[] res = new String[4];
 
             String serversMessage1 = Colors.BOLD + "Servers: " + Colors.NORMAL;
             serversMessage1 += "Current: " + Colors.BLUE + Colors.BOLD + this.serversAmount + Colors.NORMAL;
             serversMessage1 += " (" + this.serversDiff + ")";
             res[0] = serversMessage1;
 
             String serversMessage2 = Colors.BOLD + "Servers: " + Colors.NORMAL;
             serversMessage2 += "Min: " + Colors.BLUE + Colors.BOLD + this.serversMin + Colors.NORMAL;
             serversMessage2 += " | Max: " + Colors.BLUE + Colors.BOLD + this.serversMax + Colors.NORMAL;
             serversMessage2 += " | Average: " + Colors.BLUE + Colors.BOLD + this.serversAvg + Colors.NORMAL;
             res[1] = serversMessage2;
 
             String playersMessage1 = Colors.BOLD + "Players: " + Colors.NORMAL;
             playersMessage1 += "Current: " + Colors.BLUE + Colors.BOLD + this.playersAmount + Colors.NORMAL;
             playersMessage1 += " (" + this.playersDiff + ")";
             res[2] = playersMessage1;
 
             String playersMessage2 = Colors.BOLD + "Players: " + Colors.NORMAL;
             playersMessage2 += "Min: " + Colors.BLUE + Colors.BOLD + this.playersMin + Colors.NORMAL;
             playersMessage2 += " | Max: " + Colors.BLUE + Colors.BOLD + this.playersMax + Colors.NORMAL;
             playersMessage2 += " | Average: " + Colors.BLUE + Colors.BOLD + this.playersAvg + Colors.NORMAL;
             res[3] = playersMessage2;
 
             return res;
         }
     }
 
     // !gstats auth
     private class AuthStats {
 
         public final String authOnAmount;
         public final double authOnPercentage;
         public final String authOffAmount;
         public final double authOffPercentage;
 
         public AuthStats() throws IOException {
             final String authUrl = "http://api.mcstats.org/1.0/All+Servers/graph/Auth+Mode";
             final String jsonString = getPage(authUrl);
             final JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
             final JsonArray data = jsonObject.getAsJsonArray("data");
             final JsonArray firstArray = data.get(0).getAsJsonArray();
             final JsonArray secondArray = data.get(1).getAsJsonArray();
             final String firstArrayString1 = firstArray.get(0).getAsString();
             final String firstArrayString2 = firstArray.get(1).getAsString();
             final String secondArrayString1 = secondArray.get(0).getAsString();
             final String secondArrayString2 = secondArray.get(1).getAsString();
 
             if (firstArrayString1.contains("Online")) {
                 // First is Online, Second is Offline
                 this.authOnAmount = firstArrayString1.substring(8, firstArrayString1.length() - 1);
                 this.authOnPercentage = Double.parseDouble(firstArrayString2);
                 this.authOffAmount = secondArrayString1.substring(9, secondArrayString1.length() - 1);
                 this.authOffPercentage = Double.parseDouble(secondArrayString2);
             } else {
                 // First is Offline, Second is Online
                 this.authOffAmount = firstArrayString1.substring(9, firstArrayString1.length() - 1);
                 this.authOffPercentage = Double.parseDouble(firstArrayString2);
                 this.authOnAmount = secondArrayString1.substring(8, secondArrayString1.length() - 1);
                 this.authOnPercentage = Double.parseDouble(secondArrayString2);
             }
         }
 
         public String getMessage() {
             return Tools.asciiBar(this.authOnPercentage, Colors.DARK_GREEN, this.authOffPercentage, Colors.RED, 20, '█', '|', Colors.DARK_GRAY);
         }
     }
 
     /*
      TODO
      // !gstats java
      // !gstats os
      // !gstats arch
      // !gstats cores
      // !gstats location
      // !gstats version
      // !gstats software
 
      */
     public void nope(Channel channel) {
         channel.sendMessage(Colors.RED + "Global MCStats stats with !gstats [auth]");
     }
 }
