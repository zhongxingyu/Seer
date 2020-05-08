 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.listener;
 
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.ColorTeamingConfig;
 import com.github.ucchyocean.ct.KanaConverter;
 
 /**
  * @author ucchy
  * チャットが発生したときに、チームチャットへ転送するためのリスナークラス
  */
 public class PlayerChatListener implements Listener {
 
     private static final String GLOBAL_CHAT_MARKER = "#GLOBAL#";
 
     /**
      * Playerがチャットを送信したときに発生するイベント
      * @param event
      */
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerChat(AsyncPlayerChatEvent event) {
 
         // GLOBALマーカーが付いていたら、/g コマンドを経由してきたので、
         // GLOBALマーカーを取り除いてから抜ける。
         if ( event.getMessage().startsWith(GLOBAL_CHAT_MARKER) ) {
             String newMessage = event.getMessage().substring(GLOBAL_CHAT_MARKER.length());
             if ( ColorTeamingConfig.showJapanizeGlobalChat ) {
                 newMessage = addJapanize(newMessage); // Japanize化
             }
             event.setMessage(newMessage);
             return;
         }
 
         // 設定に応じて、Japanize化する
         if ( ColorTeamingConfig.showJapanizeGlobalChat ) {
             event.setMessage( addJapanize(event.getMessage()) );
         }
 
         // チームチャット無効なら、何もせずに抜ける
         if ( !ColorTeamingConfig.isTeamChatMode ) {
             return;
         }
 
         Player player = event.getPlayer();
 
         // プレイヤーのゲームモードがクリエイティブなら、何もせずに抜ける
         if ( player.getGameMode() == GameMode.CREATIVE ) {
             return;
         }
        
        // チームに所属していなければ、何もせずに抜ける
        if ( ColorTeaming.getPlayerColor(player).equals("") ) {
            return;
        }
 
         // チームメンバに送信する
         ColorTeaming.sendTeamChat(player, event.getMessage());
 
         // 元のイベントをキャンセル
         event.setCancelled(true);
     }
     
     /**
      * ローマ字をかな変換して、うしろにくっつける
      * @param message 変換元
      * @return 変換後
      */
     private String addJapanize(String message) {
         // 2byteコードを含まない場合にのみ、処理を行う
         if ( message.getBytes().length == message.length() ) {
             String kana = KanaConverter.conv(message);
             message = message + "(" + kana + ")";
         }
         return message;
     }
 }
