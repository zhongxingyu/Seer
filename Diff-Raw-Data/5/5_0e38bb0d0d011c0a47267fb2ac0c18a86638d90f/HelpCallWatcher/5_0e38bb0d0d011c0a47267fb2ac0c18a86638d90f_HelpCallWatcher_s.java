 /**
  * 
  */
 package org.mklab.taskit.client;
 
 import org.mklab.taskit.client.LocalDatabase.Query;
 import org.mklab.taskit.client.activity.HelpCallObserver;
 import org.mklab.taskit.shared.HelpCallProxy;
 import org.mklab.taskit.shared.TaskitRequestFactory;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 
 import com.google.gwt.media.client.Audio;
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 
 /*
  * TODO パッケージの移動 
  * TODO Server Push
  */
 /**
  * ヘルプコールの取得、変更の監視を行うクラスです。
  * 
  * @author Yuhi Ishikura
  */
 public class HelpCallWatcher {
 
   private HelpCallObserver helpCallObserver;
   private LocalDatabase database;
   private int lastHelpCallCount = 0;
 
   private Query<Integer> helpCallCountQuery = new Query<Integer>() {
 
     @Override
     public void query(TaskitRequestFactory requestFactory, Receiver<Integer> receiver) {
       requestFactory.helpCallRequest().getHelpCallCount().fire(receiver);
     }
   };
 
   /**
    * {@link HelpCallWatcher}オブジェクトを構築します。
    */
   HelpCallWatcher(LocalDatabase database) {
     if (database == null) throw new NullPointerException();
     this.database = database;
   }
 
   /**
    * 最後に取得したヘルプコール数を取得します。
    * 
    * @return ヘルプコール数
    */
   public int getHelpCallCount() {
     return this.lastHelpCallCount;
   }
 
   /**
    * 呼び出し一覧を取得します。
    * 
    * @param receiver 結果を受け取るレシーバー。結果は、キャッシュが存在する場合は、そのキャッシュと新たに取得したものの二回渡されます。
    */
   public void getHelpCallList(final Receiver<List<HelpCallProxy>> receiver) {
     this.database.getCacheAndExecute(LocalDatabase.CALL_LIST, new Receiver<List<HelpCallProxy>>() {
 
       @Override
       public void onSuccess(List<HelpCallProxy> response) {
         receiver.onSuccess(response);
         fireHelpCallCountChanged(response.size());
       }
 
       /**
        * {@inheritDoc}
        */
       @Override
       public void onFailure(ServerFailure error) {
         receiver.onFailure(error);
       }
 
       /**
        * {@inheritDoc}
        */
       @Override
       public void onConstraintViolation(Set<ConstraintViolation<?>> violations) {
         receiver.onConstraintViolation(violations);
       }
     });
   }
 
   /**
    * 今ヘルプコール数を取得するようリクエストします。
    * <p>
    * 取得完了までブロックすることはありません。
    */
   public void updateHelpCallCount() {
     fetchHelpCallCount();
   }
 
   /**
    * ヘルプコールの監視オブジェクトを設定します。
    * 
    * @param observer 監視オブジェクト
    */
   public void setHelpCallObserver(HelpCallObserver observer) {
     this.helpCallObserver = observer;
   }
 
   void fireHelpCallCountChanged(int count) {
     if (this.helpCallObserver != null && this.lastHelpCallCount != count) {
       this.helpCallObserver.helpCallCountChanged(count);
 
       // TAへの通知。オブザーバーに記述したいけれどもアクティビティの切り替えの度に音がなることになってしまう
      if (this.lastHelpCallCount == 0 && count > 0) {
         playCallSound();
       }
     }
     this.lastHelpCallCount = count;
   }
 
   private static void playCallSound() {
     final Audio audio = Audio.createIfSupported();
     if (audio != null) {
       audio.setSrc("taskit/call.mp3"); //$NON-NLS-1$
       audio.play();
     }
   }
 
   private void fetchHelpCallCount() {
     this.database.execute(this.helpCallCountQuery, new Receiver<Integer>() {
 
       @Override
       public void onSuccess(Integer response) {
         fireHelpCallCountChanged(response.intValue());
       }
     });
   }
 }
