 package jp.gr.java_conf.pitto.definition;
 
import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineEvent;
 import javax.sound.sampled.LineListener;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 /**
  * 効果音に使う程度なら楽に再生できるクラス<br>
  * 再生できるファイルはwavだけ。<br>
  * <br>
  * 使い方：<br>
  * ・再生したい<br>
  * SEManager.play(ファイルパス);<br>
  * 以上。これだけ。<br>
  * もうしばらくその効果音を使わないならば<br>
  * SEManager.dipose(ファイルパス);<br>
  * 予めロードしておきたい時は<br>
  * SEManager.load(ファイルパス);<br>
  * おわり。<br>
  * <br>
  * 詳しい内部処理<br>
  * play実行時、ロードされてない効果音は自動的にロードしてから再生する<br>
  * 勝手にdiposeしないため、開放しないとどんどんメモリ食う。使わなくなったらdipose。これ約束<br>
  * FileInputStreamを使ってファイルを開いているため、jarファイル内とかの場所からはロードできない<br>
 * AudioInputStream stream = AudioSystem.getAudioInputStream(filePath);<br>
 * この行の引数をInputStreamインタフェースのものに変えたりすればjarファイル内のファイルにアクセスできる
  * @author pitto,ネットからのソースにちょい手を入れたもの
  */
 public class SEManager implements LineListener{
 	private static SEManager me=new SEManager();
 	private static HashMap<File,Clip> seData=new HashMap<File,Clip>();
 	private static ArrayList<Clip> taskList=new ArrayList<Clip>();
 	private SEManager(){
 
 	}
 	/**
 	 * 指定したファイルをロードする
 	 * @param filePath 再生したいファイルのファイルパス
 	 * @return 再生するためのデータ<br>
 	 * SEManager内で管理しているので特別に必要がなければ受け取る必要はない<br>
 	 * 既にロードしているファイルを指定しても既にロードしてあるデータのインスタンスを返すだけ<br>
 	 * 失敗したらnullを返す
 	 */
 	public static Clip load(File filePath){
 		if(seData.containsKey(filePath)){
 			return seData.get(filePath);
 		}
 		Clip c=create(filePath);
 		if(c!=null){
 			seData.put(filePath, c);
 		}
 		return c;
 	}
 	/**
 	 * ファイルパスから再生インスタンスを生成する
 	 * @param filePath
 	 * @return 再生インスタンス
 	 */
 	private static Clip create(File filePath){
 		try {
 			// オーディオストリームを開く
            AudioInputStream stream = AudioSystem.getAudioInputStream(filePath);
 
             // オーディオ形式を取得
             AudioFormat format = stream.getFormat();
             // ULAW/ALAW形式の場合はPCM形式に変更
             if ((format.getEncoding() == AudioFormat.Encoding.ULAW)
                     || (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
                 AudioFormat newFormat = new AudioFormat(
                         AudioFormat.Encoding.PCM_SIGNED,
                         format.getSampleRate(),
                         format.getSampleSizeInBits() * 2, format.getChannels(),
                         format.getFrameSize() * 2, format.getFrameRate(), true);
                 stream = AudioSystem.getAudioInputStream(newFormat, stream);
                 format = newFormat;
             }
 
             // ライン情報を取得
             DataLine.Info info = new DataLine.Info(Clip.class, format);
             // サポートされてる形式かチェック
             if (!AudioSystem.isLineSupported(info)) {
                 System.out.println("エラー: " + filePath + "はサポートされていない形式です");
                 System.exit(0);
             }
          // 空のクリップを作成
             Clip clip = (Clip) AudioSystem.getLine(info);
             // クリップのイベントを監視
             clip.addLineListener(me);
             // オーディオストリームをクリップとして開く
             clip.open(stream);
             return clip;
         } catch (UnsupportedAudioFileException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (LineUnavailableException e) {
 			// TODO 自動生成された catch ブロック
 			e.printStackTrace();
 		}
 		return null;
 	}
 	/**
 	 * 指定されたファイルを再生する<br>
 	 * ロードされてない場合はロードしてから再生する<br>
 	 * @param filePath ファイルパス
 	 * @throws FileNotFoundException 指定されたファイルが無かった場合
 	 */
 	public static void play(File filePath) throws FileNotFoundException{
 		Clip se=seData.get(filePath);
 		if(se==null){
 			se=load(filePath);
 		}
 		if(se==null){
 			throw new FileNotFoundException();
 		}
 		if(se.getLongFramePosition()==0){
 			se.start();
 		}else{
 			se=create(filePath);
 			taskList.add(se);
 			se.start();
 		}
 	}
 	/**
 	 * 指定されたファイルパスのサウンドインスタンスメモリデータを解放する
 	 * 指定したファイルパスが存在しなかったり、ロードしていなかった場合は何もしない
 	 * @param filePath ファイルパス
 	 */
 	public static void dispose(File filePath){
 		seData.remove(filePath);
 	}
 	public void update(LineEvent event) {
         // ストップか最後まで再生された場合
         if (event.getType() == LineEvent.Type.STOP) {
             Clip clip = (Clip) event.getSource();
             clip.stop();
             if(seData.containsValue(clip)){
             	clip.setFramePosition(0); // 再生位置を最初に戻す
             	if(taskList.contains(clip)){
             		taskList.remove(clip);
             		clip.start();
             	}
             }else{
             	while(taskList.remove(clip)){
             	}
             	clip.drain();
             	clip.close();
             }
         }
     }
 }
