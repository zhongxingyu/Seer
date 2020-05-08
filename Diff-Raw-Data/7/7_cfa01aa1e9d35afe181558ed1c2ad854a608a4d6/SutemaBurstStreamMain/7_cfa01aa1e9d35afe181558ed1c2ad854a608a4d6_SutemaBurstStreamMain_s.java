 package jp.co.touyouhk;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import jp.co.touyouhk.nichannel.Util;
 import jp.co.touyouhk.nichannel.subjecttext.SubjectTextEntity;
 import jp.co.touyouhk.nichannel.subjecttext.SubjectTextUtil;
 import jp.co.touyouhk.sutemaburststream.Constant;
 import jp.co.touyouhk.sutemaburststream.dao.QueueDAO;
 import jp.co.touyouhk.sutemaburststream.model.MailConf;
 import jp.co.touyouhk.sutemaburststream.model.ZweiConf;
 import jp.co.touyouhk.util.UniversalUtil;
 
 import org.apache.commons.mail.DefaultAuthenticator;
 import org.apache.commons.mail.Email;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  * ステマ バースト ストリーム
  * SutemaBurstStream (SBS)
  * Version 0.1
  * @author touyouhk
  */
 public class SutemaBurstStreamMain {
 
 	public static final String subjectTextDirectory = "subjectText";
 
 	//JSON
 	//http://gihyo.jp/dev/serial/01/engineer_toolbox/0028
 	//http://gihyo.jp/dev/serial/01/engineer_toolbox/0027?page=1
 
 	// ループする時間間隔(分単位)
 	public static int loopTime = 30;//デフォルト
 
 	// ループのカウンタ
 	private static int loopConter;
 
 	private static String sbsConfFileName = "sbs_conf.json";
 
 	/**
 	 * @param args　コマンドライン引数
 	 * @throws ClassNotFoundException
 	 * @throws IOException
 	 * @throws JsonMappingException
 	 * @throws JsonParseException
 	 * @throws EmailException
 	 */
 	public static void main(String[] args) throws ClassNotFoundException, JsonParseException, JsonMappingException,
 			IOException, EmailException {
 
 		Class.forName("org.sqlite.JDBC");
 
 		List<ZweiConf> zweiConfList;
 
 		// ■コマンドライン引数解釈
 		try {
 			if (!parseArgument(args)) {
 				//失敗ならアプリを終了
 				System.exit(-1);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// ObjectMapperを作成
 		ObjectMapper mapper = new ObjectMapper();
 		// mydata.jsonからルートノードを取得
 		JsonNode rootNode = mapper.readValue(new File(args[0]), JsonNode.class);
 
 		zweiConfList = parseZweiConf(rootNode);
 
 		JsonNode sbsNode = mapper.readValue(new File(sbsConfFileName), JsonNode.class);
 		MailConf mailConf = parseSbsConf(sbsNode);
 
 		//System.exit(0);
 
 		while (true) {
 			loopConter++;
 			System.out.println("------------ ループ " + loopConter + "回目");
 
 			String mailLine = "";
 
 			for (ZweiConf zweiConf : zweiConfList) {
 				String result = execute(zweiConf);
 				if( result != ""){
 					mailLine += result;
 				}
 			}
 
 			//Mail送信
 			if(mailLine != ""){
 				sendMail(mailConf, mailLine);
 			}
 
 			// ■スリープ
 			endLoopWorkAndSleep();
 		}
 
 	}
 
 	public static String execute(ZweiConf zweiConf) throws UnsupportedEncodingException {
 
 		String mailString="";
 
 		// TODO 毎回チェックするのは無駄なので処理をかえる
 		// ■subject.txt保存フォルダの生成
 		String saveFolder = saveFolderCreate(zweiConf.getUrl());
 		if (saveFolder == null) {
 			//失敗ならアプリを終了
 			System.exit(-1);
 		}
 
 		// ■subjectTextダウンロード
 		String subjectFileNameFullPath = SubjectTextUtil.download(zweiConf.getUrl(),
 				saveFolder);
 
 		if (subjectFileNameFullPath == null) {
 			System.out.println("subject.txtのダウンロードに失敗");
 			return null;
 		}
 
 		QueueDAO dao = new QueueDAO(Util.boardUrl2EnglishName(zweiConf.getUrl()));
 
 		// ■subjectTextのモデルの作成
 		List<SubjectTextEntity> subjectTextEntities = SubjectTextUtil
 				.subjectText2Entity(subjectFileNameFullPath);
 
 		for (SubjectTextEntity subjectTextEntity : subjectTextEntities) {
 
 			String title = new String(subjectTextEntity.getTitle().getBytes("UTF-8"),"UTF-8");
 
 			//TODO キーワード検索
 			boolean match = false;
 			String matchKeyWord = null;
 
 
 			for (String keyword : zweiConf.getKeyWords()) {
 				if(title.indexOf(keyword) != -1){
 					match = true;
 					matchKeyWord = keyword;
 					break;
 				}
 			}
 
 			if (match) {
 
 				if (!dao.findById(Integer.valueOf(subjectTextEntity.getThreadNumber()))) {
 
 					dao.regist(Integer.valueOf(subjectTextEntity.getThreadNumber()),
 							title, matchKeyWord);
 
 					mailString += "[" + zweiConf.getItaName() +"]" + " " + title + " /key= " +
 							matchKeyWord + System.getProperty("line.separator") + "====================" +
 								 System.getProperty("line.separator");
 				}
 			}
 		}
 
 		dao.closeHandler();
 		return mailString;
 
 	}
 
 	public static void sendMail(MailConf mailConf, String mailLine) throws EmailException{
 			Email email = new SimpleEmail();
 			email.setHostName(mailConf.getSmtp_server());
 			email.setSmtpPort(mailConf.getSmtp_port());
 			email.setAuthenticator(new DefaultAuthenticator(mailConf.getUser_name(), mailConf.getPassword()));
 			email.setSSLOnConnect(mailConf.isSmtp_ssl_flag());
 			email.setFrom(mailConf.getSrc_mail_adress());
 			email.setSubject(Constant.SOFTWARE_NAME + " " + System.currentTimeMillis());
 			email.setContent(mailLine,"text/plain; charset=ISO-2022-JP");
 			email.setCharset("ISO-2022-JP");
 //			try {
 //				email.setMsg(new String(mailLine.getBytes("iso-2022-jp")));
 //			} catch (UnsupportedEncodingException e) {
 //				// TODO 自動生成された catch ブロック
 //				e.printStackTrace();
 //			}
 			email.addTo(mailConf.getDest_mail_adress());
 			email.send();
 	}
 
 
 
 	/**
 	 * メインクラスに渡された引数を解釈して変数に格納します。
 	 * 引数が足りない場合false、それ以外true
 	 * @param argument メインクラスに渡された引数
 	 * @return 成功したらtrue、失敗したらfalse
 	 * @throws IOException
 	 * @throws JsonMappingException
 	 * @throws JsonParseException
 	 */
 	public static boolean parseArgument(String[] argument) throws JsonParseException, JsonMappingException, IOException {
 
 		if (argument.length == 0) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Subject.txtフォルダを生成します
 	 * すでにフォルダが生成されている場合は処理をせずに成功とみなします。
 	 * @return 成功したら、フォルダのフルパスを返却、失敗したらNULL
 	 */
 	public static String saveFolderCreate(String itaUrl) {
 		// ファイル区切り文字 (UNIX では「/」,Windowsでは「\」)
 		String fileSeparator = System.getProperties().getProperty(
 				"file.separator");
 
 		// カレントディレクトリの取得 eg: C:\work
 		String currentDirectory = System.getProperties().getProperty("user.dir");
 
 		//URLから板の英名抽出
 		String[] itaUrls = itaUrl.split("/");
 		String itaName = (itaUrls[itaUrls.length - 1]);
 
 		// subjectフォルダーのパス名の生成
 		// subjectTextフォルダの中に板名でフォルダを作成します
 		String createDirectoryPath = currentDirectory + fileSeparator
 				+ subjectTextDirectory + fileSeparator + itaName;
 
 		if (UniversalUtil.createDirectryExistCheak(createDirectoryPath)) {
 			return createDirectoryPath;
 		} else {
 			return null;
 		}
 
 	}
 
 	/**
 	 * 1ループ終了時の共通的な終了処理と、スレッドスリープを呼び出します。
 	 * スレッドスリープでの失敗は異常ルートなのでプロセスを終了させます。
 	 * @return true
 	 */
 	public static boolean endLoopWorkAndSleep() {
 		// ■スリープ
 		System.out.println("スリープモードに移行します " + loopTime + "分");
 		try {
 			Thread.sleep(loopTime * 1000 * 60);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			// スレッドの割り込み例外・・異常ルート
 			System.exit(-1);
 		}
 		return true;
 	}
 
 	public static List<ZweiConf> parseZweiConf(JsonNode rootNode) {
 
 		List<ZweiConf> zweiConfList = new ArrayList<ZweiConf>();
 
 		Iterator<String> itaField = rootNode.getFieldNames();
 		while (itaField.hasNext()) {
 
 			ZweiConf zweiConf = new ZweiConf();
 
 			String itaNodeField = itaField.next();
 			zweiConf.setItaName(itaNodeField);
 
 			//System.out.println("    " + itaNodeField + ": " + rootNode.get(itaNodeField));
 
 			//JsonNode current = rootNode.get(itaNodeField);
 
 			JsonNode url = rootNode.get(itaNodeField).get("url");
 			zweiConf.setUrl(url.getTextValue());
 
 			JsonNode keywords = rootNode.get(itaNodeField).get("keywords");
 			System.out.println(url.getTextValue());
 
 			Iterator<JsonNode> keywordList = keywords.getElements();
 
 			List<String> keyWordArrayList = new ArrayList<String>();
 			while (keywordList.hasNext()) {
 
 				//System.out.println(keywordList.next().toString());
 				keyWordArrayList.add(keywordList.next().getTextValue());
 			}
 
 			zweiConf.setKeyWords(keyWordArrayList);
 			zweiConfList.add(zweiConf);
 		}
 
 		return zweiConfList;
 
 	}
 
 	public static MailConf parseSbsConf(JsonNode rootNode) {
 
 		JsonNode loop_node = rootNode.get("loop_minute");
 		loopTime = loop_node.asInt();
 
 		JsonNode mail_node = rootNode.get("mail");
 		MailConf mailConf = new MailConf();
 
 		mailConf.setSmtp_server(mail_node.get("smtp_server").getTextValue());
 		mailConf.setSmtp_port(mail_node.get("smtp_port").asInt());
 		mailConf.setSmtp_ssl_flag(mail_node.get("smtp_ssl_flag").asBoolean());
 		mailConf.setUser_name(mail_node.get("user_name").getTextValue());
 		mailConf.setPassword(mail_node.get("password").getTextValue());
 		mailConf.setSrc_mail_adress(mail_node.get("src_mail_adress").getTextValue());
 		mailConf.setDest_mail_adress(mail_node.get("dest_mail_adress").getTextValue());
 		return mailConf;
 
 	}
 
 }
