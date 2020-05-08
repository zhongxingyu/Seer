 /*
  * Copyright ucchy 2012
  */
 package com.github.ucchyocean.nicolivealert;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.Socket;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * @author ucchy
  * ニコニコ生放送のAPIへの接続クラス
  */
 public class NicoLiveConnector implements Runnable {
 
     // 取得したchatタグの検索とパース用の正規表現
     private static final String REGEX_CHAT = "<chat [^>]*>([^,]*),([^,]*),([^,]*)</chat>";
 
     private Pattern pattern;
     private NicoLiveAlertPlugin plugin;
     protected boolean isCanceled;
 
     /**
      * コンストラクタ。引数に、イベント通知先のNicoLiveAlertPluginを指定する。
      * @param plugin イベント通知先
      */
     public NicoLiveConnector(NicoLiveAlertPlugin plugin) {
         this.plugin = plugin;
         pattern = Pattern.compile(REGEX_CHAT);
         isCanceled = false;
     }
 
     /**
      * @see java.lang.Runnable#run()
      */
     public void run() {
 
         try {
             String[] alertServerInfo = getAlertServer();
             startListen(alertServerInfo);
         } catch (NicoLiveAlertException e) {
             e.printStackTrace();
             isCanceled = true;
         }
     }
 
     /**
      * NicoLiveConnectorを停止する。すぐには停止できないので、このメソッドを呼んだ後に、スレッドにjoinすること。
      */
     public void stop() {
         isCanceled = true;
     }
 
     /**
      * ニコニコ生放送のアラートサーバーの接続先を取得する。
      * @return result[0]がホスト名、result[1]がポート番号、result[2]がスレッドID
      * @throws NicoLiveAlertException ネットケーブルが繋がっていない時とか
      */
     private String[] getAlertServer() throws NicoLiveAlertException {
         return getXMLValuesFromURL(
                 "http://live.nicovideo.jp/api/getalertinfo",
                 new String[]{"addr", "port", "thread"});
     }
 
     /**
      * getAlertServer() で取得したサーバーに接続し、アラートの監視を行う。
      * @param server getAlertServer() の取得結果を指定する。
      * @throws NicoLiveAlertException サーバーとの接続が切断された時とか
      */
     private void startListen(String[] server) throws NicoLiveAlertException {
 
         String addr = server[0];
         int port = Integer.parseInt(server[1]);
         String thread = server[2];
 
         Socket socket = null;
         DataOutputStream out = null;
         DataInputStream in = null;
 
         try {
             plugin.logger.info("Connecting to " + addr + ":" + port + " ... ");
 
             socket = new Socket(addr, port);
             out = new DataOutputStream(socket.getOutputStream());
             in = new DataInputStream(socket.getInputStream());
 
             out.writeBytes("<thread thread=\"" + thread + "\" version=\"20061206\" res_from=\"-1\"/>\0");
             out.flush();
 
             plugin.logger.info("Connected to alert server.");
 
             int len;
             byte[] buffer = new byte[1024];
 
             while (0 <= (len = in.read(buffer))) {
                 String data = new String(buffer, 0, len);
                 Matcher matcher = pattern.matcher(data);
                 while ( matcher.find() ) {
 
                     //plugin.logger.finest(matcher.group(0));
 
                     if ( plugin.community.contains(matcher.group(2)) ||
                             plugin.user.contains(matcher.group(3)) ) {
                         // 一致するコミュニティまたはユーザーが見つかった
 
                         AlertFoundEvent event = new AlertFoundEvent();
                         event.id = matcher.group(1);
                         event.community = matcher.group(2);
                         event.user = matcher.group(3);
 
                         try {
                             String[] coNameAndTitle = getCommunityNameAndTitle(event.id);
                             event.communityName = coNameAndTitle[0];
                             event.title = coNameAndTitle[1];
                         } catch (NicoLiveAlertException e) {
                             e.printStackTrace();
                             event.communityName = "";
                             event.title = "";
                         }
 
                         plugin.onAlertFound(event);
                     }
                 }
 
                 if ( isCanceled ) {
                     break;
                 }
             }
 
         } catch (UnknownHostException e) {
             throw new NicoLiveAlertException("Error at starting listen!", e);
         } catch (IOException e) {
             throw new NicoLiveAlertException("Error at starting listen!", e);
         } finally {
             if ( out != null ) {
                 try {
                     out.close();
                 } catch (IOException e) {
                     // do nothing.
                 }
             }
             if ( in != null ) {
                 try {
                     in.close();
                 } catch (Exception e) {
                     // do nothing.
                 }
             }
             if ( socket != null ) {
                 try {
                     socket.close();
                 } catch (Exception e) {
                     // do nothing.
                 }
             }
         }
 
         plugin.logger.info("Disconnected from alert server.");
     }
 
     /**
      * 引数で指定した放送IDの、コミュニティ名と放送のタイトルを取得する。
      * @param programId 放送ID。lv123456 の lv を抜いた文字列を指定する。
      * @return result[0]がコミュニティ名、result[1]が放送のタイトル
      */
     private String[] getCommunityNameAndTitle(String programId)
             throws NicoLiveAlertException {
         return getXMLValuesFromURL(
                 "http://live.nicovideo.jp/api/getstreaminfo/lv" + programId,
                 new String[]{"name", "title"});
     }
 
     /**
      * XMLを返すURLに接続し、targetTagsに指定されたタグから、TextContent(タグの中の文字列)を取得するメソッド
      * @param url 接続先URL
      * @param targetTags 取得するタグ
      * @return targetTagsで指定したタグに入っていた文字列
      * @throws NicoLiveAlertException XMLのパースに失敗したときなど
      */
     private static String[] getXMLValuesFromURL(String url, String[] targetTags) throws NicoLiveAlertException {
 
         String[] results = new String[targetTags.length];
 
         HttpURLConnection urlconn = null;
 
         try {
             URL urlurl = new URL(url);
 
             urlconn = (HttpURLConnection)urlurl.openConnection();
             urlconn.setRequestMethod("GET");
             urlconn.setInstanceFollowRedirects(false);
             urlconn.setRequestProperty("Accept-Language", "ja;q=0.7,en;q=0.3");
             urlconn.connect();
 
             DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             Document document = docBuilder.parse(urlconn.getInputStream(), "UTF-8");
 
             for ( int i=0; i<targetTags.length; i++ ) {
                NodeList list = document.getElementsByTagName(targetTags[1]);
                 if ( list.getLength() <= 0 ) {
                    throw new NicoLiveAlertException("Error to get tag " + targetTags[1] + "!");
                 }
                 results[i] = list.item(0).getTextContent();
             }
 
             return results;
 
         } catch (MalformedURLException e) {
             throw new NicoLiveAlertException("Error at parse of responce!", e);
         } catch (ProtocolException e) {
             throw new NicoLiveAlertException("Error at parse of responce!", e);
         } catch (DOMException e) {
             throw new NicoLiveAlertException("Error at parse of responce!", e);
         } catch (IOException e) {
             throw new NicoLiveAlertException("Error at parse of responce!", e);
         } catch (ParserConfigurationException e) {
             throw new NicoLiveAlertException("Error at parse of responce!", e);
         } catch (SAXException e) {
             throw new NicoLiveAlertException("Error at parse of responce!", e);
         } finally {
             if ( urlconn != null ) {
                 try {
                     urlconn.disconnect();
                 } catch (Exception e) {
                     // do nothing.
                 }
             }
         }
     }
 }
