 /*
  * File:   ServerLink.java
  * Author: Catherine
  *
  * Created on April 10, 2009, 2:55 AM
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient.network;
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import java.nio.ByteBuffer;
 import java.util.concurrent.*;
 import java.security.*;
 import javax.crypto.Cipher;
 import javax.crypto.spec.SecretKeySpec;
 import javax.swing.JOptionPane;
 import shoddybattleclient.BattlePanel;
 import shoddybattleclient.BattleWindow;
 import shoddybattleclient.ChatPane;
 import shoddybattleclient.GameVisualisation.VisualPokemon;
 import shoddybattleclient.LobbyWindow;
 import shoddybattleclient.Preference;
 import shoddybattleclient.ServerConnect;
 import shoddybattleclient.shoddybattle.Pokemon;
 import shoddybattleclient.shoddybattle.PokemonMove;
 import shoddybattleclient.shoddybattle.PokemonNature;
 import shoddybattleclient.shoddybattle.PokemonSpecies;
 import shoddybattleclient.utils.ClauseList.Clause;
 import shoddybattleclient.utils.MoveListParser;
 import shoddybattleclient.utils.SpeciesListParser;
 import shoddybattleclient.utils.Text;
 
 /**
  * An instance of this class acts as the client's link to the Shoddy Battle 2
  * server.
  *
  * @author Catherine
  */
 public class ServerLink extends Thread {
 
     public static class TimerOptions {
         public int pool;
         public int periods;
         public int periodLength;
         public TimerOptions(int pool, int pers, int periodLen) {
             this.pool = pool;
             this.periods = pers;
             this.periodLength = periodLen;
         }
     }
 
     public static interface RuleSet {
         public TimerOptions getTimerOptions();
         public int[] getClauses(List<Clause> clauses);
     }
 
     public interface ChallengeMediator {
         /**
          * Get the team being used for this challenge.
          */
         public Pokemon[] getTeam();
 
         /**
          * Called when the challenge has been resolved, either through it
          * being accepted or rejected. If it was accepted, the method should
          * send the client's team to the server.
          */
         public void informResolved(boolean accepted);
 
         /**
          * Get the name of the user who has been challenged.
          */
         String getOpponent();
 
         /**
          * Get the generation being played.
          */
         public int getGeneration();
 
         /**
          * Get the active party size ("n").
          */
         public int getActivePartySize();
 
         /**
          * Get the maximum team length
          */
         public int getMaxTeamLength();
 
         /**
          * Get the applied clauses
          */
         public int[] getClauses();
 
         /**
          * Gets the timer options
          */
         public TimerOptions getTimerOptions();
 
         /**
          * Gets the metagame, or -1 if custom rules are being used
          */
         public int getMetagame();
     }
 
     public static class Metagame implements RuleSet {
         private int m_idx;
         private String m_name;
         private String m_id;
         private String m_description;
         private int m_partySize;
         private int m_maxTeamLength;
         private List<String> m_banList;
         private List<String> m_clauses;
         private Pokemon[] m_team;
         private TimerOptions m_timerOptions;
         public Metagame(int idx, String name, String id, String description,
                 int partySize, int maxTeamLength, List<String> banList,
                 List<String> clauses, TimerOptions timeOps) {
             m_idx = idx;
             m_name = name;
             m_id = id;
             m_description = description;
             m_partySize = partySize;
             m_maxTeamLength = maxTeamLength;
             m_banList = banList;
             m_clauses = clauses;
             m_timerOptions = timeOps;
         }
         public int getIdx() {
             return m_idx;
         }
         public String getName() {
             return m_name;
         }
         public String getId() {
             return m_id;
         }
         public String getDescription() {
             return m_description;
         }
         public int getPartySize() {
             return m_partySize;
         }
         public int getMaxTeamLength() {
             return m_maxTeamLength;
         }
         public String[] getBanList() {
             return m_banList.toArray(new String[m_banList.size()]);
         }
         public String[] getClauseList() {
             return m_clauses.toArray(new String[m_clauses.size()]);
         }
         public int[] getClauses(List<Clause> clauses) {
             int[] ret = new int[m_clauses.size()];
             for (int i = 0; i < m_clauses.size(); i++) {
                 String name = m_clauses.get(i);
                 ret[i] = clauses.indexOf(new Clause(name, null));
             }
             return ret;
         }
         public TimerOptions getTimerOptions() {
             return m_timerOptions;
         }
         public void setTeam(Pokemon[] team) {
             m_team = team;
         }
         public Pokemon[] getTeam() {
             return m_team;
         }
         @Override
         public String toString() {
             return m_name;
         }
     }
 
     //Provies callbacks for elements trying to receive a user's personal message
     public static interface MessageListener {
         public void informMessageRecevied(String user, String msg);
     }
 
     /**
      * Messages sent by the client to the server.
      */
     public static class OutMessage extends ByteArrayOutputStream {
         protected final DataOutputStream m_stream = new DataOutputStream(this);
         public OutMessage(int type) {
             try {
                 m_stream.write(type);
                 m_stream.writeInt(0); // insert in 0 for size for now
             } catch (IOException e) {
 
             }
         }
         @Override
         public byte[] toByteArray() {
             byte[] bytes = super.toByteArray();
             ByteBuffer buffer = ByteBuffer.wrap(bytes);
             buffer.putInt(1, bytes.length - 5);
             return bytes;
         }
     }
 
     public static class RequestChallengeMessage extends OutMessage {
         public RequestChallengeMessage(String user) {
             super(0); // see network.cpp
             try {
                 m_stream.writeUTF(user);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class ChallengeResponseMessage extends OutMessage {
         public ChallengeResponseMessage(byte[] response) {
             super(1);
 
             try {
                 m_stream.write(response, 0, 16);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class RegisterAccountMessage extends OutMessage {
         public RegisterAccountMessage(String user, String password) {
             super (2);
 
             try {
                 m_stream.writeUTF(user);
                 m_stream.writeUTF(password);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class JoinChannel extends OutMessage {
         public JoinChannel(String channel) {
             super(3);
             try {
                 m_stream.writeUTF(channel);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class ChannelMessage extends OutMessage {
         public ChannelMessage(int channel, String message) {
             super(4);
             try {
                 m_stream.writeInt(channel);
                 m_stream.writeUTF(message);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class ModeMessage extends OutMessage {
         public ModeMessage(int channel, String user, int mode, boolean enable) {
             super(5);
             try {
                 m_stream.writeInt(channel);
                 m_stream.writeUTF(user);
                 m_stream.write(mode);
                 m_stream.write(enable ? 1 : 0);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class OutgoingChallenge extends OutMessage {
         public OutgoingChallenge(ChallengeMediator mediator) {
             super(6);
             try {
                 m_stream.writeUTF(mediator.getOpponent());
                 m_stream.writeByte(mediator.getGeneration());
                 m_stream.writeInt(mediator.getActivePartySize());
                 m_stream.writeInt(mediator.getMaxTeamLength());
                 m_stream.writeInt(mediator.getMetagame());
                 if (mediator.getMetagame() != -1) {
                     return;
                 }
                 int[] clauses = mediator.getClauses();
                 m_stream.write(clauses.length);
                 for (int i = 0; i < clauses.length; i++) {
                     m_stream.write(clauses[i]);
                 }
                 TimerOptions ops = mediator.getTimerOptions();
                 if (ops == null) {
                     m_stream.write(0);
                 } else {
                     m_stream.write(1);
                     m_stream.writeInt(ops.pool);
                     m_stream.write(ops.periods);
                     m_stream.writeInt(ops.periodLength);
                 }
             } catch (Exception e) {
 
             }
         }
     }
 
     public void writePokemon(Pokemon pokemon, DataOutputStream stream)
                 throws IOException {
         stream.writeInt(PokemonSpecies.getIdFromName(
                 m_speciesList, pokemon.species));
         stream.writeUTF(pokemon.nickname);
         stream.write(pokemon.shiny ? 1 : 0);
         stream.write(pokemon.gender.getValue());
         stream.write(pokemon.happiness);
         stream.writeInt(pokemon.level);
         stream.writeUTF((pokemon.item == null) ? "" : pokemon.item);
         stream.writeUTF(pokemon.ability);
         PokemonNature nature = PokemonNature.getNature(pokemon.nature);
         stream.writeInt(nature.getInternalValue());
         stream.writeInt(pokemon.moves.length);
         for (int i = 0; i < pokemon.moves.length; ++i) {
             stream.writeInt(PokemonMove.getIdFromName(
                     m_moveList, pokemon.moves[i]));
             stream.writeInt(pokemon.ppUps[i]);
         }
         for (int i = 0; i < Pokemon.STAT_COUNT; ++i) {
             stream.writeInt(pokemon.ivs[i]);
             stream.writeInt(pokemon.evs[i]);
         }
     }
 
     public void writeTeam(Pokemon[] team, DataOutputStream stream)
                 throws IOException {
         stream.writeInt(team.length);
         for (Pokemon i : team) {
             writePokemon(i, stream);
         }
     }
 
     public static class ResolveChallenge extends OutMessage {
         public ResolveChallenge(ServerLink link,
                 String opponent,
                 boolean accepted,
                 Pokemon[] team) {
             super(7);
             try {
                 m_stream.writeUTF(opponent);
                 m_stream.write(accepted ? 1 : 0);
                 if (accepted) {
                     link.writeTeam(team, m_stream);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public static class ChallengeTeam extends OutMessage {
         public ChallengeTeam(ServerLink link,
                 String opponent,
                 Pokemon[] team) {
             super(8);
             try {
                 m_stream.writeUTF(opponent);
                 link.writeTeam(team, m_stream);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class WithdrawChallenge extends OutMessage {
         public WithdrawChallenge(String opponent) {
             super(9);
             try {
                 m_stream.writeUTF(opponent);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class BattleAction extends OutMessage {
         public BattleAction(int fid, int turnType, int index, int target) {
             super(10);
             try {
                 m_stream.writeInt(fid);
                 m_stream.write(turnType);
                 m_stream.write(index);
                 m_stream.write(target);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class PartChannelMessage extends OutMessage {
         public PartChannelMessage(int channel) {
             super(11);
             try {
                 m_stream.writeInt(channel);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class RequestChannelListMessage extends OutMessage {
         public RequestChannelListMessage() {
             super(12);
         }
     }
 
     public static class MetagameQueueMessage extends OutMessage {
         public MetagameQueueMessage(ServerLink link,
                 int metagame, boolean rated, Pokemon[] team) {
             super(13);
             try {
                 m_stream.write(metagame);
                 m_stream.write(rated ? 1 : 0);
                 link.writeTeam(team, m_stream);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class BanMessage extends OutMessage {
         public BanMessage(int channel, String user, int date) {
             super(14);
             try {
                 m_stream.writeInt(channel);
                 m_stream.writeUTF(user);
                 m_stream.writeInt(date);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class UserDetailMessage extends OutMessage {
         public UserDetailMessage(String user) {
             super(15);
             try {
                 m_stream.writeUTF(user);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class PersonalUserMessage extends OutMessage {
         public PersonalUserMessage(String message) {
             super(16);
             try {
                 m_stream.writeUTF(message);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static class RequestUserMessage extends OutMessage {
         public RequestUserMessage(String user) {
             super(17);
             try {
                 m_stream.writeUTF(user);
             } catch (Exception e) {
 
             }
         }
     }
 
     public static abstract class MessageHandler {
         /**
          * Handle a message from the server by reading values from the
          * DataInputStream. The underlying InputStream is a byte array, not
          * a socket, so the method is unable to ruin the connection with
          * the server.
          */
         public abstract void handle(ServerLink link, DataInputStream is)
                 throws IOException;
     }
 
     /**
      * Messages _received_ from the server that need to be handled by the
      * client.
      *
      * Note that the codes from this enum MUST match the codes from the
      * OutMessage::TYPE enum in the server.
      */
     public static class ServerMessage {
         static {
             m_map = new HashMap<Integer, ServerMessage>();
 
             // WELCOME_MESSAGE
             new ServerMessage(0, new MessageHandler() {
                 // int32  : server version
                 // string : server name
                 // string : welcome message
                 // byte   : can register?
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int version = is.readInt();
                     String name = is.readUTF();
                     String welcome = is.readUTF();
                     boolean canRegister = (is.readUnsignedByte() != 0);
 
                     link.m_serverConnect =
                             new ServerConnect(link, name, welcome, canRegister);
                     link.m_serverConnect.setVisible(true);
 
                     //System.out.println("Received WELCOME_MESSAGE.");
                     System.out.println("Server version: " + version);
                     //System.out.println("Server name: " + name);
                     //System.out.println("Welcome message: " + welcome);
                 }
             });
 
             // PASSWORD_CHALLENGE
             new ServerMessage(1, new MessageHandler() {
                 // byte[16] : the challenge
                 // byte     : style of secret:
                 //                0 - secret := password
                 //                1 - secret := md5(password)
                 //                2 - secret := md5(md5(password) + salt)
                 // string   : salt, if relevant
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     byte[] challenge = new byte[16];
                     is.readFully(challenge);
                     int style = is.readUnsignedByte();
                     String salt = is.readUTF();
 
                     link.createKeySpec(style, salt);
                     link.m_password = null;
 
                     // decrypt the challenge
                     try {
                         Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
 
                         // pass 1
                         cipher.init(Cipher.DECRYPT_MODE, link.m_key[1]);
                         challenge = cipher.doFinal(challenge, 0, 16);
 
                         // pass 2
                         cipher.init(Cipher.DECRYPT_MODE, link.m_key[0]);
                         challenge = cipher.doFinal(challenge, 0, 16);
 
                         ByteBuffer buffer = ByteBuffer.wrap(challenge);
                         int r = buffer.getInt(0) + 1;
                         buffer.putInt(0, r);
 
                         // pass 1
                         cipher.init(Cipher.ENCRYPT_MODE, link.m_key[0]);
                         challenge = cipher.doFinal(challenge, 0, 16);
 
                         // pass 2
                         cipher.init(Cipher.ENCRYPT_MODE, link.m_key[1]);
                         challenge = cipher.doFinal(challenge, 0, 16);
 
                         link.sendMessage(
                                 new ChallengeResponseMessage(challenge));
 
                         // don't keep the keys in memory indefinitely
                         link.m_key[0] = null;
                         link.m_key[1] = null;
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             });
 
             // REGISTRY_RESPONSE
             new ServerMessage(2, new MessageHandler() {
                 // byte : type
                 // string : details
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int type = is.readUnsignedByte();
                     String details = is.readUTF();
 
                     ServerConnect conn = link.m_serverConnect;
 
                     // see network.cpp for these values
                     switch (type) {
                         case 0:
                             conn.informNameUnavailable();
                             break;
                         case 1:
                             conn.informRegisterSuccess();
                             break;
                         case 2:
                             conn.informInvalidName();
                             break;
                         case 3:
                             conn.informNameTooLong();
                             break;
                         case 4:
                             conn.informNonexistentAccount();
                             break;
                         case 5:
                             conn.informFailedChallenge();
                             break;
                         case 6:
                             ServerConnect.informUserBanned(details);
                             break;
                         case 7:
                             conn.informSuccessfulLogin();
                             break;
                         case 8:
                             conn.informAlreadyLoggedIn();
                             break;
                     }
                 }
             });
 
             // CHANNEL_INFO
             new ServerMessage(4, new MessageHandler() {
                 // int32  : channel id
                 // byte   : channel info
                 // string : channel name
                 // string : channel topic
                 // int32  : channel flags
                 // int32  : number of users
                 // for each user:
                 //      string : name
                 //      int32  : flags
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     int type = is.read();
                     String channelName = is.readUTF();
                     String topic = is.readUTF();
                     int channelFlags = is.readInt();
                     int count = is.readInt();
                     LobbyWindow.Channel channel =
                             new LobbyWindow.Channel(id, type, channelName,
                             topic, channelFlags);
                     for (int i = 0; i < count; ++i) {
                         String name = is.readUTF();
                         int flags = is.readInt();
                         channel.addUser(name, flags);
                     }
                     link.m_lobby.addChannel(channel);
                 }
             });
 
             // CHANNEL_JOIN_PART
             new ServerMessage(5, new MessageHandler() {
                 // int32 : channel id
                 // string : user
                 // byte : joining?
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     String user = is.readUTF();
                     boolean join = (is.readByte() != 0);
                     link.m_lobby.handleJoinPart(id, user, join);
                 }
             });
 
             // CHANNEL_STATUS
             new ServerMessage(6, new MessageHandler() {
                 // int32  : channel id
                 // string : the person who set the mode
                 // string : user
                 // int32  : flags
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     String setter = is.readUTF();
                     String user = is.readUTF();
                     int flags = is.readInt();
                     link.m_lobby.handleUpdateStatus(id, setter, user, flags);
                 }
             });
 
             // CHANNEL_LIST
             new ServerMessage(7, new MessageHandler() {
                 // int32 : number of channels
                 // for each channel:
                 //      string : name
                 //      byte   : type
                 //      string : topic
                 //      int32  : population
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     List<BattlePanel.Battle> battles =
                             new ArrayList<BattlePanel.Battle>();
 
                     int count = is.readInt();
                     for (int i = 0; i < count; ++i) {
                         String name = is.readUTF();
                         int type = is.read();
                         String topic = is.readUTF();
                         int population = is.readInt();
 
                         if (type == 1) {
                             // It's a battle.
                             BattlePanel.Battle battle =
                                     new BattlePanel.Battle();
                             battle.id = Integer.valueOf(name).intValue();
                             battle.population = population;
                             String[] parts = topic.split(",");
                             battle.players =
                                     new String[] { parts[0], parts[1] };
                             battle.generation =
                                     Integer.valueOf(parts[2]).intValue();
                             battle.n = Integer.valueOf(parts[3]).intValue();
                             // TODO: ladder
                             battles.add(battle);
                         }
                     }
 
                     BattlePanel.Battle[] arr =
                             (BattlePanel.Battle[])battles.toArray(
                                     new BattlePanel.Battle[battles.size()]);
                     link.m_lobby.getBattlePanel().setBattles(arr);
                 }
             });
 
             // CHANNEL_MESSAGE
             new ServerMessage(8, new MessageHandler() {
                 // int32 : channel id
                 // string : user
                 // string : message
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     String user = is.readUTF();
                     String message = is.readUTF();
                     link.m_lobby.handleChannelMessage(id, user, message);
                 }
             });
 
             // INCOMING_CHALLENGE
             new ServerMessage(9, new MessageHandler() {
                 // string : user
                 // byte : generation
                 // int32 : active party size
                 // int32 : max team length
                // byte : metagame
                // if metagame != -1:
                 //     byte : number of clauses
                 //     for each:
                 //         byte : clause index
                 // byte : if timing is enabled
                 // if timing is enabled:
                 //     int16 : starting time bank
                 //     byte  : number of periods
                 //     int16 : period length
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     String user = is.readUTF();
                     int generation = is.read();
                     int partySize = is.readInt();
                     int teamLength = is.readInt();
                     int metagame = is.readInt();
                     int pool = 0;
                     int periods = 0;
                     int periodLength = 0;
 
                     if ((partySize > 6) || (teamLength > 6)) {
                         // The client only supports a max team/party size of 6
                         // Block out all challenges that go beyond this limit
                         link.resolveChallenge(user, false, null);
                         return;
                     }
                     
                     if (metagame != -1) {
                         Metagame mg = link.getMetagames()[metagame];
                         link.m_lobby.addChallenge(user, true, generation,
                                 partySize, teamLength, metagame, mg);
                     } else {
                         int size = is.read();
                         final int[] clauses = new int[size];
                         for (int i = 0; i < size; i++) {
                             clauses[i] = is.read();
                         }
                         final TimerOptions ops;
                         if (is.read() != 0) {
                             pool = is.readInt();
                             periods = is.read();
                             periodLength = is.readInt();
                             ops = new TimerOptions(pool, periods, periodLength);
                         } else {
                             ops = null;
                         }
                         RuleSet rules = new RuleSet() {
                             @Override
                             public TimerOptions getTimerOptions() {
                                 return ops;
                             }
                             @Override
                             public int[] getClauses(List<Clause> cl) {
                                 return clauses;
                             }
                         };
                         link.m_lobby.addChallenge(user, true, generation,
                                 partySize, teamLength, metagame, rules);
                     }
                 }
             });
 
             // FINALISE_CHALLENGE
             new ServerMessage(10, new MessageHandler() {
                 // string : user
                 // byte : whether the challenge was accepted
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     String user = is.readUTF();
                     boolean accepted = (is.read() != 0);
 
                     ChallengeMediator mediator = link.m_challenges.get(user);
                     if (mediator != null) {
                         mediator.informResolved(accepted);
                     }
                     if (!accepted) {
                         link.m_challenges.remove(user);
                     }
                 }
             });
 
             // CHALLENGE_WITHDRAWN
             new ServerMessage(11, new MessageHandler() {
                // string : opponent
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     String user = is.readUTF();
                     if (link.m_lobby.isUserPanelSelected(user)) {
                         JOptionPane.showMessageDialog(link.m_lobby, user +
                                 " withdrew his or her challenge");
                     }
                     link.m_lobby.removeUserPanel(user);
                 }
             });
 
             // BATTLE_BEGIN
             new ServerMessage(12, new MessageHandler() {
                 // int32  : field id
                 // string : opponent
                 // byte   : party
                 // int16  : metagame (-1 for a direct challenge)
                 // byte   : rated
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int id = is.readInt();
                     String user = is.readUTF();
                     int party = is.read();
                     int metagameId = is.readShort();
                     boolean rated = (is.read() != 0);
                     String[] users = null;
 
                     ChallengeMediator mediator = null;
 
                     if (party == 0) {
                         users = new String[] { link.m_name, user };
                     } else {
                         users = new String[] { user, link.m_name };
                     }
                     int partySize;
                     int maxTeamLength;
                     Pokemon[] team;
                     TimerOptions opts;
                     if (metagameId != -1) {
                         Metagame metagame = link.m_metagames[metagameId];
                         partySize = metagame.getPartySize();
                         maxTeamLength = metagame.getMaxTeamLength();
                         team = metagame.getTeam();
                         opts = metagame.getTimerOptions();
                         link.getLobby().getFindPanel().informMatchStarted();
                     } else {
                         if (party == 0) {
                             // we made the original challenge
                             mediator = link.m_challenges.get(user);
                             link.m_challenges.remove(user);
                         } else {
                             // we were challenged
                             mediator = link.m_lobby.getChallengeMediator(user);
                             link.m_lobby.cancelChallenge(user);
                         }
                         partySize = mediator.getActivePartySize();
                         team = mediator.getTeam();
                         maxTeamLength = mediator.getMaxTeamLength();
                         opts = mediator.getTimerOptions();
                         link.m_lobby.removeUserPanel(user);
                     }
 
                     int periods = (opts == null) ? -1 : opts.periods;
                     int periodLength = (opts == null) ? -1 : opts.periodLength;
 
                     BattleWindow wnd = new BattleWindow(link, id, partySize,
                             maxTeamLength, party, users, team, periods, periodLength);
 
                     link.m_battles.put(id, wnd);
                     wnd.setVisible(true);
                 }
             });
 
             // REQUEST_ACTION
             new ServerMessage(13, new MessageHandler() {
                  // int32 : field id
                  // byte  : slot of relevant pokemon
                  // byte  : position of relevant pokemon
                  // byte  : whether this is a replacement
                  // int32 : number of pokemon
                  // for each pokemon:
                  //      byte : whether it is legal to switch to this pokemon
                  // if not replacement:
                  //      byte : whether switching is legal
                  //      byte : whether there is a forced move
                  //      if not forced:
                  //          int32 : total number of moves
                  //          for each move:
                  //              byte : whether the move is legal
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int slot = is.read();
                     int pos = is.read();
 
                     boolean replacement = (is.read() != 0);
                     int count = is.readInt();
                     boolean[] switches = new boolean[count];
                     for (int i = 0; i < count; ++i) {
                         switches[i] = (is.read() != 0);
                     }
                     if (replacement) {
                         wnd.requestReplacement();
                         wnd.setValidSwitches(switches);
                     } else {
                         wnd.requestAction(pos, slot);
                         boolean canSwitch = (is.read() != 0);
                         if (!canSwitch) {
                             Arrays.fill(switches, false);
                         }
                         wnd.setValidSwitches(switches);
                         boolean forced = (is.read() != 0);
                         wnd.setForced(forced);
                         if (!forced) {
                             count = is.readInt();
                             boolean[] legal = new boolean[count];
                             for (int i = 0; i < count; ++i) {
                                 legal[i] = (is.read() != 0);
                             }
                             wnd.setValidMoves(legal);
                         }
                     }
                 }
             });
 
             // BATTLE_POKEMON
             new ServerMessage(14, new MessageHandler() {
                  // int32 : field id
                  // for 0...1:
                  //     for 0...n-1:
                  //         int16 : species id
                  //         if id != -1:
                  //             byte : gender
                  //             byte : level
                  //             byte : whether the pokemon is shiny
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int size = wnd.getPartySize();
                     for (int i = 0; i < 2; ++i) {
                         for (int j = 0; j < size; ++j) {
                             short id = is.readShort();
                             if (id == -1) {
                                 wnd.updateSprite(i, j);
                                 continue;
                             }
 
                             String species = PokemonSpecies.getNameFromId(
                                     link.m_speciesList, id);
                             wnd.setSpecies(i, j, species);
 
                             int gender = is.readUnsignedByte();
                             int level = is.readUnsignedByte();
                             boolean shiny = (is.read() != 0);
 
                             VisualPokemon p = wnd.getPokemonForSlot(i, j);
                             if (p == null) continue;
 
                             p.setSpeciesId(id);
                             p.setGender(gender);
                             p.setLevel(level);
                             p.setShiny(shiny);
                             wnd.updateSprite(i, j);
                         }
                     }
                 }
             });
 
             // BATTLE_PRINT
             new ServerMessage(15, new MessageHandler() {
                 // int32 : field id
                 // byte  : category
                 // int16 : message id
                 // byte  : number of arguments
                 // for each argument:
                 //     string : value of the argument
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int category = is.readUnsignedByte();
                     int msg = is.readShort();
                     int count = is.readUnsignedByte();
                     String[] args = new String[count];
                     for (int i = 0; i < count; ++i) {
                         args[i] = is.readUTF();
                     }
 
                     String message = Text.getText(category, msg, args, wnd);
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_VICTORY
             new ServerMessage(16, new MessageHandler() {
                 // int32 : field id
                 // int16 : party id
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.readShort();
                     wnd.informVictory(party);
                 }
             });
 
             // BATTLE_USE_MOVE
             new ServerMessage(17, new MessageHandler() {
                 // int32 : field id
                 // byte : party
                 // byte : slot
                 // string : user [nick]name
                 // int16 : move id
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     String name = is.readUTF();
                     int idx = is.readShort();
 
                     String move =
                             PokemonMove.getNameFromId(link.m_moveList, idx);
                     name = Text.formatName(name, (party == wnd.getParty()));
                     move = "<font class='move'>" + move + "</font>";
 
                     String message = Text.getText(4, 10,
                             new String[] { name, move });
 
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_WITHDRAW
             new ServerMessage(18, new MessageHandler() {
                 // int32 : field id
                 // byte : party
                 // byte : slot
                 // string : user [nick]name
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     String name = is.readUTF();
 
                     boolean us = (party == wnd.getParty());
                     String trainer = Text.formatTrainer(wnd.getTrainer(party),
                             wnd.getParty(), party);
                     name = Text.formatName(name, us);
                     String message = Text.getText(4, 11,
                             new String[] { trainer, name });
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_SEND_OUT
             new ServerMessage(19, new MessageHandler() {
                 // int32  : field id
                 // byte   : party
                 // byte   : slot
                 // byte   : index
                 // string : user [nick]name
                 // int16  : species id
                 // byte   : gender
                 // byte   : level
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     int index = is.read();
                     String name = is.readUTF();
                     int speciesId = is.readShort();
                     int gender = is.readUnsignedByte();
                     int level = is.readUnsignedByte();
 
                     String species =
                             PokemonSpecies.getNameFromId(link.m_speciesList,
                             speciesId);
 
                     wnd.sendOut(party, slot, index, speciesId, species,
                             name, gender, level);
 
                     if (gender != 0) {
                         species += " ";
                         boolean male = (gender ==
                                 Pokemon.Gender.GENDER_MALE.getValue());
                         species += male ? '\u2642' : '\u2640';
                     }
 
                     String trainer = Text.formatTrainer(wnd.getTrainer(party),
                             wnd.getParty(), party);
 
                     name = Text.formatName(name, wnd.getParty() == party);
 
                     String message = Text.getText(4, 12,
                             new String[] { trainer,
                                 name,
                                 String.valueOf(level),
                                 species });
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_HEALTH_CHANGE
             new ServerMessage(20, new MessageHandler() {
                 // int32  : field id
                 // byte   : party
                 // byte   : slot
                 // int16  : delta health in [0, 48]
                 // int16  : new total health [0, 48]
                 // int16  : denominator
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     int delta = is.readShort();
                     int total = is.readShort();
                     int denominator = is.readShort();
 
                     // Update the health bars.
                     wnd.updateHealth(party, slot, total, denominator);
 
                     boolean ally = wnd.getParty() == party;
                     String name = Text.formatName(
                             wnd.getNameForSlot(party, slot),
                             ally);
                     String number;
 
                     int id = (delta >= 0) ? 13 : 14;
                     delta = Math.abs(delta);
 
                     number = Text.formatHealthChange(delta, denominator,
                             Preference.getHealthDisplay(ally));
 
                     String message = Text.getText(4, id,
                             new String[] { name, number });
                     wnd.addMessage(null, message, false);
                 }
             });
 
             // BATTLE_SET_PP
             new ServerMessage(21, new MessageHandler() {
                 // int32  : field id
                 // byte   : pokemon
                 // byte   : move
                 // byte   : pp
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int i = is.readUnsignedByte();
                     int j = is.readUnsignedByte();
                     int pp = is.readUnsignedByte();
 
                     wnd.setPp(i, j, pp);
                 }
             });
 
             // BATTLE_FAINTED
             new ServerMessage(22, new MessageHandler() {
                 // int32 : field id
                 // byte : party
                 // byte : slot
                 // string : user [nick]name
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int party = is.read();
                     int slot = is.read();
                     String name = is.readUTF();
                     name = Text.formatName(name, wnd.getParty() == party);
                     String message = Text.getText(4, 15, new String[] { name });
                     wnd.addMessage(null, message, false);
                     wnd.faint(party, slot);
                 }
             });
 
             // BATTLE_BEGIN_TURN
             new ServerMessage(23, new MessageHandler() {
                 // int32 : field id
                 // int16 : turn count
                 // bool  : if timing is enabled
                 // if timing is enabled:
                 //     for each player:
                 //        int16 : remaining time in the pool/period
                 //        byte  : number of periods remaining
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int count = is.readShort();
                     wnd.informTurnStart(count);
 
                     boolean timing = (is.read() != 0);
                     if (timing) {
                         for (int i = 0; i < 2; i++) {
                             int remaining = is.readShort();
                             int periods = is.read();
                             wnd.synchroniseClock(i, remaining, periods);
                         }
                     }
                 }
             });
 
             // SPECTATOR BEGIN
             new ServerMessage(24, new MessageHandler() {
                 // int32  : field id
                 // string : first player
                 // string : second player
                 // byte   : active party size
                 // byte   : maximum party size
                 // byte   : maximum timer periods or -1 if no timing
                 //
                 // for 0...1:
                 //     byte : party size
                 //     for 0...party size:
                 //         byte : has the pokemon been revealed
                 //         if revealed:
                 //             int16  : slot the pokemon is in or -1 if no slot
                 //             string : the nickname of the pokemon
                 //             int16  : species id
                 //             byte : gender
                 //             byte : level
                 //             byte : whether the pokemon is shiny
                 //             byte : whether the pokemon is fainted
                 //             if not fainted:
                 //                 byte : present hp in [0, 48]
                 //                 byte : number of status effects
                 //                 for each status:
                 //                     string : id
                 //                     string : message
                 //                     byte   : effect radius
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
                     String[] player = new String[2];
                     player[0] = is.readUTF();
                     player[1] = is.readUTF();
                     int n = is.read();
                     int max = is.read();
                     int maxPeriods = is.readByte();
 
                     BattleWindow battle = new BattleWindow(
                             link, fid, n, max, player, maxPeriods);
                     link.m_battles.put(fid, battle);
 
                     for (int i = 0; i < 2; ++i) {
                         int size = is.readUnsignedByte();
                         for (int j = 0; j < size; ++j) {
                             VisualPokemon p = battle.getPokemon(i, j);
                             int revealed = is.readUnsignedByte();
                             if (revealed != 0) {
                                 int slot = is.readShort();
                                 String name = is.readUTF();
                                 int id = is.readShort();
                                 int gender = is.read();
                                 int level = is.readUnsignedByte();
                                 boolean shiny = (is.read() != 0);
 
                                 String species = PokemonSpecies.getNameFromId(
                                         link.m_speciesList, id);
 
                                 p.setSpeciesId(id);
                                 p.setSpecies(species);
                                 p.setName(name);
                                 p.setLevel(level);
                                 p.setGender(gender);
                                 p.setShiny(shiny);
 
                                 if (slot != -1) {
                                     battle.sendOut(i, slot, j, id, species,
                                             name, gender, level);
                                     battle.setSpecies(i, slot, species);
                                 }
                                 
                                 boolean fainted = (is.read() != 0);
                                 int hp = (fainted) ? 0 : is.read();
                                 p.setHealth(hp, 48);
                                 if (p.getSlot() != -1) {
                                     battle.updateHealth(i, p.getSlot(), hp, 48);
                                 }
 
                                 if (fainted) {
                                     p.faint();
                                 } else {
                                     int nStatus = is.readUnsignedByte();
                                     for (int k = 0; k < nStatus; k++) {
                                         String statusId = is.readUTF();
                                         String msg = is.readUTF();
                                         int radius = is.readUnsignedByte();
                                         msg = Text.parse(msg, battle);
                                         battle.updateStatus(i, j, radius, 
                                                 statusId, msg, true);
                                     }
                                 }
 
                                 if (slot != -1) {
                                     battle.updateSprite(i, slot);
                                 }
                             }
                         }
                     }
 
                     battle.setVisible(true);
                 }
             });
 
             // BATTLE_SET_MOVE
             new ServerMessage(25, new MessageHandler() {
                 // int32  : field id
                 // byte   : pokemon
                 // byte   : move slot
                 // int16  : new move
                 // byte   : pp
                 // byte   : max pp
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int fid = is.readInt();
 
                     BattleWindow wnd = link.m_battles.get(fid);
                     if (wnd == null) return;
 
                     int i = is.readUnsignedByte();
                     int j = is.readUnsignedByte();
                     int move = is.readShort();
                     int pp = is.readUnsignedByte();
                     int maxPp = is.readUnsignedByte();
 
                     wnd.setPokemonMove(i, j, move, pp, maxPp);
                 }
             });
 
             // METAGAME_LIST
             new ServerMessage(26, new MessageHandler() {
                 // int16  : metagame count
                 // for each metagame:
                 //     byte   : id
                 //     string : name
                 //     string : "id", the table name of the metagame
                 //     string : description
                 //     byte   : party size (n)
                 //     byte   : max team length
                 //     int16  : number of bans
                 //     for each ban:
                 //         int16  : pokemon id
                 //     int16  : number of clauses
                 //     for each clause:
                 //         string : name of clause
                 //     byte   : if timing is enabled
                 //     if timing is enabled:
                 //         short : pool length
                 //         byte  : number of periods
                 //         short : period length
                 public void handle(ServerLink link, DataInputStream is)
                         throws IOException {
                     int count = is.readShort();
                     Metagame[] metagames = new Metagame[count];
                     for (int i = 0; i < metagames.length; ++i) {
                         int idx = is.readUnsignedByte();
                         String name = is.readUTF();
                         String id = is.readUTF();
                         String description = is.readUTF();
                         int partySize = is.readUnsignedByte();
                         int maxTeamLength = is.readUnsignedByte();
                         List<String> banList = new ArrayList<String>();
                         int banLength = is.readShort();
                         for (int j = 0; j < banLength; ++j) {
                             int entry = is.readShort();
                             String species = PokemonSpecies.getNameFromId(
                                     link.m_speciesList, entry);
                             banList.add(species);
                         }
                         List<String> clauses = new ArrayList<String>();
                         int clauseLength = is.readShort();
                         for (int j = 0; j < clauseLength; ++j) {
                             String clause = is.readUTF();
                             clauses.add(clause);
                         }
                         TimerOptions ops;
                         boolean timing = (is.read() != 0);
                         if (timing) {
                             int pool = is.readShort();
                             int periods = is.read();
                             int periodLength = is.readShort();
                             ops = new TimerOptions(pool, periods, periodLength);
                         } else {
                             ops = null;
                         }
                         metagames[i] = new Metagame(idx, name, id,
                                 description, partySize, maxTeamLength,
                                 banList, clauses, ops);
                     }
                     link.m_metagames = metagames;
                     if (link.m_lobby != null) {
                         link.m_lobby.getFindPanel().updateMetagames();
                     }
                 }
             });
 
             //KICK_BAN_MESSAGE
             new ServerMessage(27, new MessageHandler() {
                 //int32  : channel
                 //string : mod
                 //string : user
                 //int32  : date
                 public void handle(ServerLink link, DataInputStream is)
                                                             throws IOException{
                     int id = is.readInt();
                     String mod = is.readUTF();
                     String user = is.readUTF();
                     int date = is.readInt();
                     link.getLobby().handleBanMessage(id, mod, user, date);
                 }
             });
 
             //USER_DETAILS
             new ServerMessage(28, new MessageHandler() {
                 //string : name of the user
                 //string : ip
                 //byte   : number of aliases
                 //for each alias:
                 //    string : alias
                 //byte  : number of bans
                 //for each ban:
                 //    int32  : channel
                 //    string : banned name
                 //    int32  : expiry
                 public void handle(ServerLink link, DataInputStream is)
                                                         throws IOException {
                     String user = is.readUTF();
                     if ("".equals(user)) {
                         is.readUTF();
                         is.readByte();
                         is.readByte();
                         link.getLobby().informBadLookup();
                     } else {
                         String ip = is.readUTF();
                         byte acount = is.readByte();
                         List<String> aliases = new ArrayList<String>();
                         for (byte i = 0; i < acount; i++) {
                             aliases.add(is.readUTF());
                         }
                         byte bcount = is.readByte();
                         List<BanElement> bans = new ArrayList<BanElement>();
                         for (byte i = 0; i < bcount; i++) {
                             int channel = is.readInt();
                             String name = is.readUTF();
                             int expiry = is.readInt();
                             //insert java tuples here
                             bans.add(new BanElement(channel, name, expiry));
                         }
                         link.getLobby().showLookupResults(user, ip, aliases, bans);
                     }
                 }
             });
 
             //USER_PERSONAL_MESSAGE
             new ServerMessage(29, new MessageHandler() {
                 //string : user
                 //string : message
                 public void handle(ServerLink link, DataInputStream is)
                                                             throws IOException{
                     String user = is.readUTF();
                     String message = is.readUTF();
                     link.informPersonalMessageReceived(user, message);
                 }
             });
 
             //BATTLE_STATUS_CHANGE
             new ServerMessage(30, new MessageHandler() {
                 // int32  : field id
                 // byte   : party
                 // byte   : position
                 // byte   : type
                 // byte   : effect radius
                 // string : id
                 // string : message
                 // byte   : whether the status was applied
                 public void handle(ServerLink link, DataInputStream is)
                                                         throws IOException {
                     int fid = is.readInt();
                     int party = is.readUnsignedByte();
                     int position = is.readUnsignedByte();
                     int type = is.readUnsignedByte();
                     int radius = is.readUnsignedByte();
                     String id = is.readUTF();
                     String msg = is.readUTF();
                     boolean applied = (is.read() != 0);
                     BattleWindow wnd = link.getBattle(fid);
                     if (wnd == null) return;
                     msg = Text.parse(msg, wnd);
 
                     switch(type) {
                         case 0:
                             wnd.updateStatus(party, position, radius, id,
                                     msg, applied);
                             VisualPokemon p = wnd.getPokemon(party, position);
                             if (p.getSlot() != -1) {
                                 wnd.updateSprite(party, p.getSlot());
                             }
                             break;
                         case 1:
                             if (applied)
                                 System.out.println("ITEM ADDED: " + msg);
                             else
                                 System.out.println("ITEM REMOVED: " + msg);
                             break;
                         case 2:
                             if (applied)
                                 System.out.println("ABILITY ADDED: " + msg);
                             else
                                 System.out.println("ABILITY REMOVED: " + msg);
                             break;
                     }
                 }
             });
 
             //CLAUSE_LIST
             new ServerMessage(31, new MessageHandler() {
                 //int16 : number of clauses
                 //for each clause:
                 //    string : name
                 //    string : description
                 public void handle(ServerLink link, DataInputStream is)
                                                         throws IOException {
                     int count = is.readShort();
                     List<Clause> clauses = new ArrayList<Clause>();
                     for (int i = 0; i < count; i++) {
                         String name = is.readUTF();
                         String desc = is.readUTF();
                         clauses.add(new Clause(name, desc));
                     }
                     link.setClauseList(clauses);
                 }
             });
 
             //INVALID_TEAM
             new ServerMessage(32, new MessageHandler() {
                 //string : user of the challenge or empty for matchmaking
                 //byte   : size of team
                 //int16  : number of violations
                 //for each violation:
                 //    int16 : index of the violated clause
                 public void handle(ServerLink link, DataInputStream is)
                                                         throws IOException {
                     String user = is.readUTF();
                     int teamSize = is.readUnsignedByte();
                     int count = is.readShort();
                     int[] clauses = new int[count];
                     for (int i = 0; i < count; i++) {
                         clauses[i] = is.readShort();
                     }
                     link.m_lobby.informInvalidTeam(user, teamSize, clauses);
                 }
             });
 
             // add additional messages here
         }
 
         private static Map<Integer, ServerMessage> m_map;
         private MessageHandler m_handler;
         ServerMessage(int code, MessageHandler handler) {
             m_handler = handler;
             m_map.put(code, this);
         }
         public void handle(ServerLink link, DataInputStream is)
                 throws IOException {
             m_handler.handle(link, is);
         }
         public static ServerMessage getMessage(int code) {
             return m_map.get(code);
         }
     }
 
     public static class BanElement {
         public int channel;
         public String name;
         public int expiry;
         public BanElement(int channel, String name, int expiry) {
             this.channel = channel;
             this.name = name;
             this.expiry = expiry;
         }
     }
 
     private BlockingQueue<OutMessage> m_queue =
             new LinkedBlockingQueue<OutMessage>();
     private Socket m_socket;
     private DataInputStream m_input;
     private DataOutputStream m_output;
     private SecretKeySpec[] m_key = new SecretKeySpec[2];
     private String m_name, m_password;
     private Thread m_messageThread;
     private ServerConnect m_serverConnect;
     private LobbyWindow m_lobby;
     private static List<PokemonSpecies> m_speciesList;
     private List<PokemonMove> m_moveList;
     private Map<String, ChallengeMediator> m_challenges =
             new HashMap<String, ChallengeMediator>();
     private Map<Integer, BattleWindow> m_battles =
             new HashMap<Integer, BattleWindow>();
     private Metagame[] m_metagames;
     private List<MessageListener> m_msgListeners = new ArrayList<MessageListener>();
     private List<Clause> m_clauseList;
 
     public Metagame[] getMetagames() {
         return m_metagames;
     }
 
     public static List<PokemonSpecies> getSpeciesList() {
         return m_speciesList;
     }
 
     public List<PokemonMove> getMoveList() {
         return m_moveList;
     }
 
     public void setClauseList(List<Clause> clauses) {
         m_clauseList = clauses;
     }
 
     public List<Clause> getClauseList() {
         return m_clauseList;
     }
 
     public ServerLink(String host, int port)
             throws IOException, UnknownHostException {
         m_socket = new Socket();
         m_socket.bind(null);
         m_socket.connect(new InetSocketAddress(host, port), 5000);
         m_input = new DataInputStream(m_socket.getInputStream());
         m_output = new DataOutputStream(m_socket.getOutputStream());
     }
 
     public BattleWindow getBattle(int id) {
         return m_battles.get(id);
     }
 
     public void sendBattleMessage(int id, String message)
             throws ChatPane.CommandException {
         m_lobby.getChannel(id).getChatPane().sendMessage(message);
     }
 
     public LobbyWindow getLobby() {
         return m_lobby;
     }
 
     public void postChallenge(ChallengeMediator mediator) {
         m_challenges.put(mediator.getOpponent(), mediator);
         sendMessage(new OutgoingChallenge(mediator));
     }
 
     public void resolveChallenge(String opponent,
             boolean accepted,
             Pokemon[] team) {
         sendMessage(new ResolveChallenge(this, opponent, accepted, team));
     }
 
     public void postChallengeTeam(String opponent, Pokemon[] team) {
         sendMessage(new ChallengeTeam(this, opponent, team));
     }
 
     public void withdrawChallenge(String opponent) {
         sendMessage(new WithdrawChallenge(opponent));
     }
 
     public void addMessageListener(MessageListener ml) {
         m_msgListeners.add(ml);
     }
 
     public void removeMessageListener(MessageListener ml) {
         m_msgListeners.remove(ml);
     }
 
     public void informPersonalMessageReceived(String user, String message) {
         for (MessageListener ml : m_msgListeners) {
             ml.informMessageRecevied(user, message);
         }
     }
 
     public void loadSpecies(String file) {
         SpeciesListParser slp = new SpeciesListParser();
         m_speciesList = slp.parseDocument(file);
     }
 
     public void loadMoves(String file) {
         MoveListParser mlp = new MoveListParser();
         m_moveList = mlp.parseDocument(file);
     }
 
     public void registerAccount(String user, String password) {
         sendMessage(new RegisterAccountMessage(user, password));
     }
 
     public void setLobbyWindow(LobbyWindow window) {
         m_lobby = window;
     }
 
     public void joinChannel(String name) {
         sendMessage(new JoinChannel(name));
     }
 
     public void partChannel(int channel) {
         sendMessage(new PartChannelMessage(channel));
     }
 
     public void requestChannelList() {
         sendMessage(new RequestChannelListMessage());
     }
 
     public void sendChannelMessage(int id, String message) {
         sendMessage(new ChannelMessage(id, message));
     }
 
     public void sendSwitchAction(int fid, int idx) {
         sendMessage(new BattleAction(fid, 1, idx, -1));
     }
 
     public void sendMoveAction(int fid, int idx, int target) {
         sendMessage(new BattleAction(fid, 0, idx, target));
     }
 
     public void updateMode(int channel, String user, int mode, boolean enable) {
         sendMessage(new ModeMessage(channel, user, mode, enable));
     }
 
     public void queueTeam(int metagame, boolean rated, Pokemon[] team) {
         sendMessage(new MetagameQueueMessage(this, metagame, rated, team));
         m_metagames[metagame].setTeam(team);
     }
 
     public void sendBanMessage(int channel, String user, long length) {
         int date;
         if (length == 0) {
             date = 0;
         } else if (length < 0) {
             date = 1;
         } else {
             long d = (System.currentTimeMillis() / 1000) + length;
             if (d > Integer.MAX_VALUE) d = Integer.MAX_VALUE;
             else if (d < Integer.MIN_VALUE) d = Integer.MIN_VALUE;
             date = (int)d;
         }
         sendMessage(new BanMessage(channel, user, date));
     }
 
     public void requestUserLookup(String user) {
         sendMessage(new UserDetailMessage(user));
     }
 
     public void updatePersonalMessage(String msg) {
         sendMessage(new PersonalUserMessage(msg));
     }
 
     public void requestUserMessage(String user) {
         sendMessage(new RequestUserMessage(user));
     }
 
     private static char[] HEX_TABLE = {
         '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
         'a', 'b', 'c', 'd', 'e', 'f'
     };
 
     /**
      * Convert an encoded String to a String with a hexadecimal format.
      */
     public static String toHexString(String encodedString) {
         byte[] data = new byte[0];
         try {
             data = encodedString.getBytes("ISO-8859-1");
         } catch (UnsupportedEncodingException ex) {
         }
 
         int end = data.length;
         StringBuffer s = new StringBuffer(end * 2);
 
         for (int i = 0; i < end; i++) {
             int high_nibble = (data[i] & 0xf0) >>> 4;
             int low_nibble = (data[i] & 0x0f);
             s.append(HEX_TABLE[high_nibble]);
             s.append(HEX_TABLE[low_nibble]);
         }
 
         return s.toString();
     }
 
     private static String md5(String input) throws NoSuchAlgorithmException,
             UnsupportedEncodingException {
         MessageDigest digest = MessageDigest.getInstance("MD5");
         byte[] hash = digest.digest(input.getBytes("ISO-8859-1"));
         return toHexString(new String(hash, "ISO-8859-1"));
     }
 
     private void createKeySpec(int style, String salt) {
         try {
             String secret;
             if (style == 0) {
                 secret = m_password;
             } else if (style == 1) {
                 secret = md5(m_password);
             } else if (style == 2) {
                 secret = md5(md5(m_password) + salt);
             } else {
                 System.out.println("Unknown secret style = " + style);
                 m_key[0] = m_key[1] = null;
                 return;
             }
 
             MessageDigest digest = MessageDigest.getInstance("SHA-256");
             byte[] key = digest.digest(secret.getBytes("ISO-8859-1"));
             m_key[0] = new SecretKeySpec(key, 0, 16, "AES");
             m_key[1] = new SecretKeySpec(key, 16, 16, "AES");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void attemptAuthentication(String user, String password) {
         m_name = user;
         m_password = password;
         sendMessage(new RequestChallengeMessage(user));
     }
 
     /**
      * Send a message to the server.
      * @param msg the message to send.
      */
     public void sendMessage(OutMessage msg) {
         try {
             m_queue.put(msg);
         } catch (InterruptedException e) {
 
         }
     }
 
     void spawnMessageQueue() {
         m_messageThread = new Thread(new Runnable() {
             public void run() {
                 while (!interrupted()) {
                     OutMessage msg;
                     try {
                         msg = m_queue.take();
                     } catch (InterruptedException e) {
                         return; // end the thread
                     }
                     byte bytes[] = msg.toByteArray();
                     try {
                         m_output.write(bytes);
                     } catch (IOException e) {
 
                     }
                 }
             }
         });
         m_messageThread.start();
     }
 
     public void close() {
         try {
             m_input.close();
         } catch (Exception e) {
 
         }
         try {
             m_output.close();
         } catch (Exception e) {
 
         }
     }
 
     /**
      * protocol is simple:
      *     byte type : type of message
      *     int32 length : length of message body
      *     byte[length] : message body
      */
     @Override
     public void run() {
         spawnMessageQueue();
         while (true) {
             try {
                 int type = m_input.read();
                 int length = m_input.readInt();
                 byte[] body = new byte[length];
                 m_input.readFully(body);
 
                 // find the right handler to call
                 final ServerMessage msg = ServerMessage.getMessage(type);
                 if (msg == null) {
                     // unknown message type - but we can skip over it and live
                     System.out.println("Unkown message type: " + type);
                     continue;
                 }
 
                 // call the handler
                 final DataInputStream stream = new DataInputStream(
                         new ByteArrayInputStream(body));
                 try {
                     java.awt.EventQueue.invokeAndWait(new Runnable() {
                         public void run() {
                             try {
                                 msg.handle(ServerLink.this, stream);
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                         }
                     });
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
             } catch (IOException e) {
                 // fatal error - exit the while loop
                 break;
             }
         }
 
         // interrupt the message thread
         m_messageThread.interrupt();
         m_lobby.addImportantMessage("Disconnected from server");
         String message = Text.addClass("Disconnected from server", "important");
         for (BattleWindow battle : m_battles.values()) {
             battle.addMessage(null, message, false);
         }
     }
 
     public static void main(String[] args) throws Exception {
         ServerLink link = new ServerLink("localhost", 8446);
         link.attemptAuthentication("Catherine", "test");
         link.run(); // block
     }
 
 }
