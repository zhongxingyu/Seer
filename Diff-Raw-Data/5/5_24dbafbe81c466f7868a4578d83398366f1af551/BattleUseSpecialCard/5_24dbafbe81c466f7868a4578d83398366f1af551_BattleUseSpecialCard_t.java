 package com.ict.apps.bobb.common;
 
 import com.ict.apps.bobb.common.BattleUseKit.DeckNum;
 import com.ict.apps.bobb.data.BeetleKit;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 /**
  * 戦闘時に使用する特殊カード情報クラス
  *
  */
 public class BattleUseSpecialCard {
 
 	// SharedPref名
 	private static final String NAME_BATTLE_USE_SPE_CARD = "BattleUseSpecialCard";
 	// 対戦用特殊１
 	private static final String CARD1 = "card1";
 	// 対戦用特殊２
	private static final String CARD2 = "card2";
 	// 対戦用特殊３
	private static final String CARD3 = "card3";
 	
 	public enum CardNum {
 		CARD1,
 		CARD2,
 		CARD3,
 	}
 	
 	/**
 	 * SharedPreferencesへアクセス
 	 * @param context
 	 * @return
 	 */
 	private static SharedPreferences openPreferences(Context context) {
 		return context.getSharedPreferences(BattleUseSpecialCard.NAME_BATTLE_USE_SPE_CARD, Context.MODE_PRIVATE);
 	}
 
 	/**
 	 * 対戦時使用の特殊カードのキットを取得する。
 	 * 
 	 * BattleUseSpecialCard.CardNum.CARD1
 	 * 
 	 * @param context
 	 * @param deck　格納番号CARD1~CARD3 
 	 * @return　虫キットインスタンス
 	 */
 	public static BeetleKit getUseKit(Context context, CardNum number) {
 		
 		String cardNum = getDeckNum(number);
 
 		SharedPreferences pref = BattleUseSpecialCard.openPreferences(context);
 		long beetleId = pref.getLong(cardNum, 0);
 		
 		if (beetleId == 0) {
 			// 無い場合は、デフォルトの特殊虫キットを設定する。
 			beetleId = 2;
 			pref = BattleUseSpecialCard.openPreferences(context);
 			SharedPreferences.Editor editor = pref.edit();
 			editor.putLong(cardNum, beetleId);
 			editor.commit();
 		}
 		
 		// 虫キットIDをキーに虫キットインスタンスを生成する。
 		BeetleKitFactory factory = new BeetleKitFactory(context);
 		BeetleKit beelteKit = factory.getBeetleKit(beetleId);
 
 		return beelteKit;
 		
 	}
 
 	/**
 	 * 対戦時使用の特殊虫キットを設定する。
 	 * デッキ番号を指定する必要があります。
 	 * @param context
 	 * @param deck デッキ番号
 	 * @param kit
 	 */
 	public static void setUseKit(Context context, CardNum card, BeetleKit kit) {
 		
 		String cardNum = getDeckNum(card);
 		
 		// カブト虫キットを設定する。
 		SharedPreferences pref = BattleUseSpecialCard.openPreferences(context);
 		SharedPreferences.Editor editor = pref.edit();
 		editor.putLong(cardNum, kit.getBeetleKitId());
 		editor.commit();
 		
 	}
 
 	/**
 	 * 列挙型のデッキ番号を検索キーに変換する
 	 * @param deck 列挙型　デッキ番号
 	 * @return
 	 */
 	private static String getDeckNum(CardNum deck) {
 		String deckNum = null;
 		if (CardNum.CARD1 == deck) {
 			deckNum = BattleUseSpecialCard.CARD1;
 		}
 		else if (CardNum.CARD2 == deck) {
 			deckNum = BattleUseSpecialCard.CARD2;
 		}
 		else if (CardNum.CARD3 == deck) {
 			deckNum = BattleUseSpecialCard.CARD3;
 		}
 		return deckNum;
 	}
 
 }
