 /**
  * 
  */
 package net.krks.android.roidcast;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
import android.widget.Toast;
 
 /**
  * ブラウザなどから呼び出されて、URLを保存するためのクラス
  * 正常にURLを保存できた時は、Roidcastのメイン画面へ遷移する
  * 
  * @author kakkyz
  *
  */
 public class ReceiveUrl extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
     	// メッセージとして受けたuriを取り出す
     	Intent intent = getIntent();
     	String uri = intent.getStringExtra(Intent.EXTRA_TEXT);
     	
     	Log.i(Roidcast.TAG,"onCreate @@@ roidast" + uri);
     	
     	// uriをパースしてpodcastオブジェクトにする
     	Podcast podcast = null;
     	XMLParse xmlParse = new XMLParse();
     	podcast = xmlParse.parsePodcastXML(uri);
     	
     	if(podcast != null) {
         	// パース結果を保存する
 	    	savePodcast(podcast);
 	    	// 次のActivity（メイン画面）を呼び出す
 	    	Intent nextIntent = new Intent(getApplicationContext(),Roidcast.class);
 	    	startActivity(nextIntent);
     	} else {
    		// パースできなかった場合、メッセージを出力して終了する
     		Log.i(Roidcast.TAG,"ParseError");
    		Toast.makeText(getApplicationContext()
    				, getString(R.string.reciveurl_could_not_parse)
					, Toast.LENGTH_SHORT).show();
    		finish();
     	}
         
     }
     
     /**
      * podcastを保存する 
      * @param podcast
      */
     protected void savePodcast(Podcast podcast) {
     	if(podcast == null) { throw new AssertionError(); }
 
     	RoidcastFileIo r = new RoidcastFileIo(getApplicationContext());
     	ArrayList<Podcast> podlist = r.doLoad(); // TODO 読み込んで追加して保存という手順が非効率な気がする
     	// 重複時は保存しない
     	if(!isDuplicate(podlist, podcast)){
 			podlist.add(podcast);
 			try {
 				r.doSave(podlist);
 			} catch (IOException e) {
 				new RoidcatUtil().eLog(e);
 			}
     	}
     	
     	r = null; // 後処理
     }
     
     /**
      * podcastが重複していないかチェックする
      * 
      */
     protected boolean isDuplicate(ArrayList<Podcast> a,Podcast newPodcast) {
     	for(Podcast p:a) {
     		if(p.getXmlUrl().equals(newPodcast.getXmlUrl())) {
     			return true;
     		}
     	}
     	return false;
     }
 }
