 package ch17.ex02;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 
 public class DataHandler {
 	private WeakReference<File> lastFile; // （おそらく）最後に読んだファイル
 	private WeakReference<byte[]> lastData; // （おそらく）最後のデータ
 	
 	public byte[] readFile(File file) {
 		byte[] data;
 		
 		//　データを記憶しているか調べる
 		if (lastFile != null && file.equals(lastFile.get())) {
 			System.out.println("Data still is restored");
 			data = lastData.get();
 			if (data != null)
 				return data;
 		}
 		
 		// 記憶していないので、読み込む
 		data = readBytesFromFile(file);
 		lastFile = new WeakReference<File>(file);
 		lastData = new WeakReference<byte[]>(data);
 		return data;
 	}
 	
 	public byte[] readBytesFromFile(File file) {
 		// テスト用
 	    byte[] buf = { 0x41, 0x42, 0x43, 0x0d, 0x0a };
 		return buf;
 	}
 	
 	public static void main(String[] args) {
 		// テスト用ファイルオブジェクト生成
 	    File file = new File("/Users/knishide/Desktop/test.dat");
 		
 		DataHandler handler = new DataHandler();
 		handler.readFile(file);
		handler.readFile(file); // "Data still is restored"が出力されていればOK
 	}
 }
