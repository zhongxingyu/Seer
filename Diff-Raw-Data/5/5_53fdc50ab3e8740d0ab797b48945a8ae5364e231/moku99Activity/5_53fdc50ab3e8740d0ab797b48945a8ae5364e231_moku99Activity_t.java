 package jp.moku99.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class moku99Activity extends Activity {
 	class Communicator implements Runnable {
 		static final int MAX_PACKET_SIZE = 104857600;
 		Socket clientSock;
 		byte[] myId;
 		HashMap<byte[], String> dataMap;
 
 		public Communicator() {
 			myId = genId();
 			dataMap = new HashMap<byte[], String>();
 		}
 
 		/**
 		 * クライアントIDを生成して返す。これは当然呼ぶ度に別のIDを
 		 * 
 		 * @return String クライアントID
 		 */
 		public byte[] genId() {
 			Random rnd = new Random();
 			ByteBuffer buf = ByteBuffer.allocate(4);
 			buf.putInt(rnd.nextInt());
 			return buf.array();
 		}
 
 		public void run() {
 			connect("127.0.0.1", 2525);
 			// TODO: 接続が失われた際の再接続方法をほどよく提供する必要がある
 			int ret = load();
 			if (ret != 0) {
 				// loadの結果、自IDが一覧に含まれずエラー終了する(クライアント数上限の場合)など
 				Log.d("moku99", "connection failed. return code: " + ret);
 			}
 		}
 		
 		/**
 		 * 指定のホスト/ポートに接続する。既に接続されている場合は切断、Socketの際生成を行う
 		 * @param host
 		 * @param port
 		 * @return 成功時は0。失敗時は負の値
 		 */
 		public int connect(String host, int port) {
 			InetSocketAddress addr = new InetSocketAddress(host, port);
 			try {
 				if (clientSock != null && clientSock.isConnected()) {
 					clientSock.close();
 				}
 				clientSock = new Socket();
 				clientSock.connect(addr);
 				// TODO: 接続が失われた際の再接続方法を考えておく必要がある
 			} catch (SocketException e) {
 				Log.d("moku99", e.getLocalizedMessage());
 				return -1;
 			} catch (UnknownHostException e) {
 				Log.d("moku99", e.getLocalizedMessage());
 				return -2;
 			} catch (IOException e) {
 				Log.d("moku99", e.getLocalizedMessage());
 				return -3;
 			}
 			return 0;
 		}
 
 		/**
 		 * socketからサーバへと情報を送る。
 		 * Java側からの扱いが楽なので文字列を受け渡すようにしてる。
 		 * バイナリを扱いたければbyte列を使うように要改訂。
 		 * (バックエンド的には問題なく作れているはず)。その際はsendPacket()の
 		 * 実装も変更するか、sendPacket()に'\0'を渡すようにするか、要選択。
 		 * @param src
 		 * @return 正常終了時は0。サーバへの接続が失われていれば-4。接続数上限等で自クライアント情報が含まれなければ-3
 		 */
 		public int send(String src) {
			if (sendPacket(src) == 0) {
 				return readInfo();
 			}
 			else {
 				return -2;
 			}
 		}
 		
 		/**
 		 * サーバ上から最新情報を取得する
 		 * @return 正常終了時は0。サーバへの接続が失われていれば-4。接続数上限等で自クライアント情報が含まれなければ-3
 		 */
 		public int load() {
 			return send("");
 		}
 
 		/**
 		 * 入力データからパケットを構築して送信する。今回はSocketを複数開いたり、
 		 * ということは起こらないので本メソッドに集約してしまってる。空文字列が送られてきたら
 		 * 特殊パターンとしてrefresh用のパケットを構築する(つまり\0単独は特殊パターン)
 		 * @return 正常終了時は0。サーバへの接続が失われていれば-4。その他I/Oエラー時は-1。
 		 */
 		private int sendPacket(String src) {
 			OutputStream writer;
 			Log.d("moku99", "sending a packet");
 			try {
 				if (clientSock.isConnected() == false) {
 					return -4;
 				}
 				writer = clientSock.getOutputStream();
 				if (src.equals("")) {
 					// 空の特殊パターン
 					ByteBuffer buf = ByteBuffer.allocate(8);
 					buf.put(myId);
 					buf.putInt(0);
 					writer.write(buf.array());
 				}
 				else {
 					// 通常メッセージ送信パターン
 					byte[] strBuf = src.getBytes();
 					ByteBuffer buf = ByteBuffer.allocate(8 + strBuf.length);
 					buf.put(myId);
 					buf.putInt(strBuf.length);
 					buf.put(strBuf);
 					writer.write(buf.array());
 				}
 			} catch (IOException e) {
 				Log.d("moku99", e.getLocalizedMessage());
 				return -1;
 			}
 			return 0;
 		}
 		
 		/**
 		 * サーバからの情報読取を行う。
 		 * @return 正常終了時は0。接続数上限等で自クライアント情報が含まれなければ-3。サーバへの接続が失われていれば-4。
 		 */
 		private int readInfo() {
 			// TODO: サーバから切断されていた場合を考える必要がある。
 			InputStream reader;
 			try {
 				if (clientSock.isConnected() == false) {
 					return -4;
 				}
 				reader = clientSock.getInputStream();
 				// socketからまず4byte読み出す。何かを送りつけたタイミングでは
 				// 常時これが読めるはずなので、タイムアウト等になった場合は結構致命的なエラー。
 				byte[] size = new byte[4];
 				reader.read(size, 0, 4);
 				int packetSize = ByteBuffer.wrap(size).getInt();
 				
 				// 更に指定サイズだけ情報を読み取ってくる
 				// さすがに数GBとかのデータを送りつけられたらエラーとして処理する
 				if (MAX_PACKET_SIZE < packetSize) {
 					Log.d("moku99", "packet size is too large to read");
 					return -2;
 				}
 				byte[] body = new byte[packetSize];
 				reader.read(body, 0, packetSize);
 				
 				int offset = 0;
 				if (packetSize == 0) {
 					dataMap.clear();
 				}
 				else {
 					// 継続してデータを読み取れるように、データオブジェクトの構築完了後にインスタンスを入れ替える
 					HashMap<byte[], String> tmpMap = new HashMap<byte[], String>();
 					while (true) {
 						// レスポンスエントリのレイアウト:
 						// [クライアントID:4bytes][レスポンスサイズ:4bytes][レスポンスボディ:指定サイズ分]
 						int len = ByteBuffer.wrap(body, offset + 4, 4).getInt();
 						tmpMap.put(
 								ByteBuffer.wrap(body, offset, 4).array(),
 								new String(ByteBuffer.wrap(body, offset + 8, len).array())
 								);
 						offset += 8 + len;
 						if (offset == packetSize) {
 							break;
 						}
 					}
 					if (tmpMap.containsKey(myId) == false) {
 						return -3;
 					}
 					dataMap = tmpMap;
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				return -1;
 			}
 			return 0;
 		}
 		
 		/**
 		 * サーバ上の情報をダンプする。割とデバッグ用なので、通常は端末ごとに
 		 * 分解されたデータを使うと思う。
 		 * @return
 		 */
 		public String dumpInfo() {
 			String ret = "";
 			for (Map.Entry<byte[], String> entry : dataMap.entrySet()) {
				ret += Integer.toString(ByteBuffer.wrap(entry.getKey()).getInt()) + entry.getValue() + "\n";
 			}
 			return ret;
 		}
 	}
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         final Handler mHandler = new Handler();
         final Communicator communicator = new Communicator();
         Thread communicationThread = new Thread(communicator);
         Button button = (Button) findViewById(R.id.Button01);
         final EditText editText = (EditText) findViewById(R.id.EditText01);
         button.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 				// 実際に使う上ではI/O発生するあたりからスレッドにちゃんと切るべき
 				Random rnd = new Random();
 				communicator.send("test string:" + Float.toString(rnd.nextFloat()));
 				final String info = communicator.dumpInfo();
 				mHandler.post(new Runnable() {
 					public void run() {
 						Date now = new Date();
 						editText.append(now.toLocaleString() + "\n" + info + "\n");
 					}
 				});
 			}
 		});
         communicationThread.start();
     }
 }
