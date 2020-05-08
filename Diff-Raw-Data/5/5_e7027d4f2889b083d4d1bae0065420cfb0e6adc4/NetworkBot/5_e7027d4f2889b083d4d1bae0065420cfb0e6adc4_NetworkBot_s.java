 package com.videoplaza.poker.server.bot;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLDecoder;
 
 import com.videoplaza.poker.game.model.Game;
 import com.videoplaza.poker.game.model.Player;
 import com.videoplaza.poker.server.util.GameMessageUtil;
 
 public class NetworkBot implements Bot {
    final Player player;
 
    public NetworkBot(Player player) {
       this.player = player;
    }
 
    @Override
    public BotResponse askForMove(Game game) {
       int playerBet = 0;
       String playerChatMessage = null;
       long startTimer = System.currentTimeMillis();
       try {
          // serialize game state
          String data = GameMessageUtil.serializeStateForPlayer(game, player);
 
          // connect to bot
          URL botUrl = new URL(player.getBotUrl());
          HttpURLConnection connection = (HttpURLConnection) botUrl.openConnection();
          connection.setRequestMethod("POST");
          connection.setDoOutput(true);
 
          // send state to bot
          OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
          writer.write(data);
          writer.flush();
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
 
          // read bots response
          BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          StringBuilder response = new StringBuilder();
          String inputLine;
          while ((inputLine = in.readLine()) != null) {
             response.append(inputLine);
          }
          in.close();
 
          // parse bots response
          String responseString = response.toString();
          System.out.println("Response from player " + player + ": " + responseString);
          responseString = responseString.trim();
          int firstSpaceIndex = responseString.indexOf(" ");
          if (firstSpaceIndex > 0) {
             try {
                playerBet = Integer.parseInt(responseString.substring(0, firstSpaceIndex));
             } catch (NumberFormatException e) {
                System.out.println("Invalid bet response format for player " + player + ": " + responseString.substring(0, firstSpaceIndex));
             }
             playerChatMessage = responseString.substring(firstSpaceIndex + 1);
             playerChatMessage = URLDecoder.decode(playerChatMessage, "UTF-8");
          } else {
             try {
                playerBet = Integer.parseInt(responseString);
             } catch (NumberFormatException e) {
                System.out.println("Invalid bet response format for player " + player + ": " + responseString.substring(0, firstSpaceIndex));
             }
 
          }
       } catch (Exception e) {
          e.printStackTrace();
       }
       long timer = System.currentTimeMillis() - startTimer;
       System.out.println("Player " + player + " responded in " + timer + " ms.");
       return new BotResponse(playerChatMessage, playerBet);
    }
 
 }
