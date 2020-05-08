 package com.sound.ampache;
 
 import java.net.*;
 import java.io.*;
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 import java.util.ArrayList;
 import com.sound.ampache.objects.*;
 import android.content.SharedPreferences;
 import android.content.Context;
 import android.widget.Toast;
 import android.preference.PreferenceManager;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import java.io.*;
 import java.net.*;
 import java.math.BigInteger;
 import java.lang.Integer;
 import java.lang.Long;
 import java.security.MessageDigest;
 import java.util.Date;
 import java.util.List;
 
 public class ampacheCommunicator
 {
 
     public String authToken = "";
     public int artists;
     public int albums;
     public int songs;
     private String update;
     private Context mCtxt;
     public String lastErr;
 
     private XMLReader reader;
 
     private SharedPreferences prefs;
 
     ampacheCommunicator(SharedPreferences preferences, Context context) throws Exception {
         prefs = preferences;
         mCtxt = context;
         System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
         reader = XMLReaderFactory.createXMLReader();
     }
 
     public void ping() {
         dataHandler hand = new dataHandler();
         reader.setContentHandler(hand);
         try {
             reader.parse(new InputSource(fetchFromServer("auth=" + this.authToken)));
             if (hand.errorCode == 401) {
                 this.perform_auth_request();
             }
         } catch (Exception poo) {
         }
     }
 
     public void perform_auth_request() throws Exception {
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         /* Get the current time, and convert it to a string */
         String time = Long.toString((new Date()).getTime() / 1000);
         
         /* build our passphrase hash */
         md.reset();
         
         /* first hash the password */
         String pwHash = prefs.getString("server_password_preference", "");
         md.update(pwHash.getBytes(), 0, pwHash.length());
         String preHash = time + asHex(md.digest());
         
         /* then hash the timestamp in */
         md.reset();
         md.update(preHash.getBytes(), 0, preHash.length());
         String hash = asHex(md.digest());
         
         /* request server auth */
         ampacheAuthParser hand = new ampacheAuthParser();
         reader.setContentHandler(hand);
         String user = prefs.getString("server_username_preference", "");
         try {
             reader.parse(new InputSource(fetchFromServer("action=handshake&auth="+hash+"&timestamp="+time+"&version=350001&user="+user)));
         } catch (Exception poo) {
             lastErr = "Could not connect to server";
         }
 
         if (hand.errorCode != 0) {
             lastErr = hand.error;
         }
 
         authToken = hand.token;
         artists = hand.artists;
         albums = hand.albums;
         songs = hand.songs;
         update = hand.update;
     }
    
     public InputStream fetchFromServer(String append) throws Exception {
         URL fullUrl = new URL(prefs.getString("server_url_preference", "") + "/server/xml.server.php?" + append);
         return fullUrl.openStream();
     }
 
     public interface ampacheDataReceiver
     {
         public void receiveObjects(ArrayList data);
     }
     
     public class ampacheRequestHandler extends Thread
     {
         private ampacheDataReceiver recv = null;
         private dataHandler hand;
         private Context mCtx;
         
         private String type;
         private String filter;
         
         public Handler incomingRequestHandler;
         
         public void run() {
             Looper.prepare();
             
             incomingRequestHandler = new Handler() {
                     public void handleMessage(Message msg) {
                         String[] directive = (String[]) msg.obj;
                         String append = "";
                         boolean goodcache = false;
                         String error = null;
                         Message reply = new Message();
                         ArrayList<ampacheObject> goods = null;
                         InputSource dataIn = null;
                         
                         append = "action=" + directive[0];
                         
                         if (directive[0].equals("artists")) {
                             hand = new ampacheArtistParser();
                         } else if (directive[0].equals("artist_albums")) {
                             append += "&filter=" + directive[1];
                             hand = new ampacheAlbumParser();
                         } else if (directive[0].equals("artist_songs")) {
                             append += "&filter=" + directive[1];
                             hand = new ampacheSongParser();
                         } else if (directive[0].equals("album_songs")) {
                             append += "&filter=" + directive[1];
                             hand = new ampacheSongParser();        
                         } else if (directive[0].equals("playlist_songs")) {
                             append += "&filter=" + directive[1];
                             hand = new ampacheSongParser();
                         } else if (directive[0].equals("genre_artists")) {
                             append += "&filter=" + directive[1];
                             hand = new ampacheArtistParser();
                         } else if (directive[0].equals("genres")) {
                             hand = new ampacheGenreParser();
                         } else if (directive[0].equals("albums")) {
                             hand = new ampacheAlbumParser();
                         } else if (directive[0].equals("playlists")) {
                             hand = new ampachePlaylistParser();
                         } else if (directive[0].equals("songs")) {
                             hand = new ampacheSongParser();
                         } else {
                             return; // new ArrayList();
                         }
                         
                         if (msg.what == 0x1336) {
                             append += "&offset=" + msg.arg1 + "&limit=100";
                             reply.arg1 = msg.arg1;
                             reply.arg2 = msg.arg2;
                         }
 
                         append += "&auth=" + authToken;
 
                         /* now we fetch */
                         try {
                             URL theUrl = new URL(prefs.getString("server_url_preference", "") + "/server/xml.server.php?" + append);
                             dataIn = new InputSource(theUrl.openStream());
                         } catch (Exception poo) {
                             error = poo.toString();
                         }
 
                         /* all done loading data, now to parse */
                         reader.setContentHandler(hand);
                         try {
                             reader.parse(dataIn);
                         } catch (Exception poo) {
                             error = poo.toString();;
                         }
                         
                         if (hand.error != null) {
                             if (hand.errorCode == 401) {
                                 try {
                                     ampacheCommunicator.this.perform_auth_request();
                                     this.sendMessage(msg);
                                 } catch (Exception poo) {
                                 }
                                 return;
                             }
                             error = hand.error;
                         }
 
                         if (error == null) {
                             reply.what = msg.what;
                             reply.obj = hand.data;
                         } else {
                             reply.what = 0x1338;
                             reply.obj = error;
                         }
                         try {
                             msg.replyTo.send(reply);
                         } catch (Exception poo) {
                             //well shit, that sucks doesn't it
                         }
                     }
                 };
             Looper.loop();
         }
     }     
     
     private class dataHandler extends DefaultHandler {
         public ArrayList<ampacheObject> data = new ArrayList();
         public String error = null;
         public int errorCode = 0;
         protected CharArrayWriter contents = new CharArrayWriter();
 
         public void startDocument() throws SAXException {
             
         }
         
         public void endDocument() throws SAXException {
 
         }
 
         public void characters( char[] ch, int start, int length )throws SAXException {
             contents.write( ch, start, length );
         }
 
         public void startElement( String namespaceURI,
                                   String localName,
                                   String qName,
                                   Attributes attr) throws SAXException {
 
             if (localName.equals("error"))
                 errorCode = Integer.parseInt(attr.getValue("code"));
             contents.reset();
         }
 
         public void endElement( String namespaceURI,
                                 String localName,
                                 String qName) throws SAXException {
             if (localName.equals("error")) {
                 error = contents.toString();
             }
         }
         
     }
 
     private class ampacheAuthParser extends dataHandler {
         public String token = "";
         public int artists = 0;
         public int albums = 0;
         public int songs = 0;
         public String update = "";
 
         public void endElement( String namespaceURI,
                                 String localName,
                                 String qName) throws SAXException {
 
             super.endElement(namespaceURI, localName, qName);
 
             if (localName.equals("auth")) {
                 token = contents.toString();
             }
 
             if (localName.equals("artists")) {
                 artists = Integer.parseInt(contents.toString());
             }
             if (localName.equals("albums")) {
                albums = Integer.parseInt(contents.toString());
             }
             if (localName.equals("songs")) {
                songs = Integer.parseInt(contents.toString());
             }
 
             if (localName.equals("add")) {
                 update = contents.toString();
             }
         }
     }
     
     private class ampacheArtistParser extends dataHandler {
         private Artist current;
         
         public void startElement( String namespaceURI,
                                   String localName,
                                   String qName,
                                   Attributes attr) throws SAXException {
             
             super.startElement(namespaceURI, localName, qName, attr);
 
             if (localName.equals("artist")) {
                 current = new Artist();
                 current.id = attr.getValue("id");
             }
         }
         
         public void endElement( String namespaceURI,
                                 String localName,
                                 String qName) throws SAXException {
             
             super.endElement(namespaceURI, localName, qName);
 
             if (localName.equals("name")) {
                 current.name = contents.toString();
             }
 
             if (localName.equals("artist")) {
                 data.add(current);
             }
 
         }
     }
     
     private class ampacheAlbumParser extends dataHandler {
         private Album current;
         
         public void startElement( String namespaceURI,
                                   String localName,
                                   String qName,
                                   Attributes attr) throws SAXException {
             
             super.startElement(namespaceURI, localName, qName, attr);
 
             if (localName.equals("album")) {
                 current = new Album();
                 current.id = attr.getValue("id");
             }
         }
         
         public void endElement( String namespaceURI,
                                 String localName,
                                 String qName) throws SAXException {
             
             super.endElement(namespaceURI, localName, qName);
 
             if (localName.equals("name")) {
                 current.name = contents.toString();
             }
             if (localName.equals("album")) {
                 data.add(current);
             }
         }
     }
     
     private class ampacheGenreParser extends dataHandler {
         private Genre current;
         
         public void startElement( String namespaceURI,
                                   String localName,
                                   String qName,
                                   Attributes attr) throws SAXException {
             
             super.startElement(namespaceURI, localName, qName, attr);
 
             if (localName.equals("genre")) {
                 current = new Genre();
                 current.id = attr.getValue("id");
             }
         }
         
         public void endElement( String namespaceURI,
                                 String localName,
                                 String qName) throws SAXException {
             
             super.endElement(namespaceURI, localName, qName);
 
             if (localName.equals("name")) {
                 current.name = contents.toString();
             }
             if (localName.equals("genre")) {
                 data.add(current);
             }
         }
     }
     
     private class ampachePlaylistParser extends dataHandler {
         private Playlist current;
         
         public void startElement( String namespaceURI,
                                   String localName,
                                   String qName,
                                   Attributes attr) throws SAXException {
             
             super.startElement(namespaceURI, localName, qName, attr);
 
             if (localName.equals("playlist")) {
                 current = new Playlist();
                 current.id = attr.getValue("id");
             }
         }
         
         public void endElement( String namespaceURI,
                                 String localName,
                                 String qName) throws SAXException {
             
             super.endElement(namespaceURI, localName, qName);
 
             if (localName.equals("name")) {
                 current.name = contents.toString();
             }
             if (localName.equals("playlist")) {
                 data.add(current);
             }
         }
     }
     
     private class ampacheSongParser extends dataHandler {
         private Song current;
         
         public void startElement( String namespaceURI,
                                   String localName,
                                   String qName,
                                   Attributes attr) throws SAXException {
             
             super.startElement(namespaceURI, localName, qName, attr);
 
             if (localName.equals("song")) {
                 current = new Song();
                 current.id = attr.getValue("id");
             }
         }
         
         public void endElement( String namespaceURI,
                                 String localName,
                                 String qName) throws SAXException {
             
             super.endElement(namespaceURI, localName, qName);
 
             if (localName.equals("song")) {
                 data.add(current);
             }
             
             if (localName.equals("title")) {
                 current.name = contents.toString();
             }
             
             if (localName.equals("artist")) {
                 current.artist = contents.toString();
             }
             
             if (localName.equals("art")) {
                 current.art = contents.toString();
             }
             
             if (localName.equals("url")) {
                 current.url = contents.toString();
             }
 
             if (localName.equals("album")) {
                 current.album = contents.toString();
             }
 
             if (localName.equals("genre")) {
                 current.genre = contents.toString();
             }
         }
     }
 
     private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
 
     public static String asHex(byte[] buf)
     {
         char[] chars = new char[2 * buf.length];
         for (int i = 0; i < buf.length; ++i)
             {
                 chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
                 chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
             }
         return new String(chars);
     }
 }
