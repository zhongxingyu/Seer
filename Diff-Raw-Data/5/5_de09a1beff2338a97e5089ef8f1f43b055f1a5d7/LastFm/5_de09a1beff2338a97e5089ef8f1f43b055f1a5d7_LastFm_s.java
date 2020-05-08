 package erki.xpeter.parsers;
 
 import java.util.Collection;
 
 import net.roarsoftware.lastfm.Caller;
 import net.roarsoftware.lastfm.Track;
 import net.roarsoftware.lastfm.User;
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.con.Connection;
 import erki.xpeter.msg.DelayedMessage;
 import erki.xpeter.msg.TextMessage;
 import erki.xpeter.util.BotApi;
 
 /**
  * Enables the bot to retrieve information from Last.FM and tell people what they are listening to.
  * 
  * @author Edgar Kalkowski
  */
 public class LastFm implements Parser, Observer<TextMessage> {
     
     private static final String LAST_FM_API_KEY = "2ff20341447560ee47656ce6e572107e";
     
     @Override
     public void init(Bot bot) {
         bot.register(TextMessage.class, this);
         Caller.getInstance().setUserAgent("xpeter");
     }
     
     @Override
     public void destroy(Bot bot) {
         bot.deregister(TextMessage.class, this);
     }
     
     @Override
     public void inform(TextMessage msg) {
         Connection con = msg.getConnection();
         String nick = con.getNick();
         String text = msg.getText();
         
         if (!BotApi.addresses(text, nick)) {
             return;
         }
         
         text = BotApi.trimNick(text, nick);
         String queryNick = null;
         
         if (text.toLowerCase().trim().startsWith("np ")) {
             queryNick = text.substring("np ".length());
         }
         
         if (text.toLowerCase().trim().equals("np")) {
             queryNick = msg.getNick();
         }
         
         if (queryNick != null) {
             Collection<Track> tracks = User.getRecentTracks(queryNick, LAST_FM_API_KEY);
             
             if (!tracks.isEmpty()) {
                 Track track = tracks.iterator().next();
                 
                 if (track.isNowPlaying()) {
                     con.send(new DelayedMessage(queryNick + " hört gerade " + formatTrack(track)
                             + ".", 1000));
                 } else {
                     con.send(new DelayedMessage(queryNick + " hört gerade gar nix.", 1000));
                 }
                 
             } else {
                 con.send(new DelayedMessage("Last.FM weiß leider nicht, was " + queryNick
                         + " gerade hört. :(", 1500));
                con.send(new DelayedMessage("Tut mir Leid.", 2500));
             }
         }
     }
     
     private String formatTrack(Track track) {
         return "„" + track.getName() + "“ von »" + track.getArtist() + "« auf dem Album „"
                 + track.getAlbum() + "“";
     }
 }
