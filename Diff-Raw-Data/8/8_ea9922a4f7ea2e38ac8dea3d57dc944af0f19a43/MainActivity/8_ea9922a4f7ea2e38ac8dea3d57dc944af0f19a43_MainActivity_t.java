 package com.discretelab.fantasycard;
 
 import com.discretelab.cardinfo.CardInfo;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	private MyCardSlotViewGroup mySlotView;
 	private DeckManager mDeckManager;
 	
 	private CardInfo[] mMyHands = new CardInfo[3];
 	
 	private TextView mtxtTurnCnt;
 	private TextView mtxtManaCnt;
 	private TextView mtxtLifeCnt;
 	
 	private ImageView[] mImgMyCard = new ImageView[3];
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		init();
 		
 	}
 	
 	@Override
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		appOnAttachedToWindow();
 	}
 	
 	public void init() {
 		setDensity();
 		appOnAttachedToWindow();
 		
 		mySlotView = (MyCardSlotViewGroup)findViewById(R.id.myslotview);
 		
 		///TurnCount初期化
 		mtxtTurnCnt = new TextView(this);
 		mtxtTurnCnt.setTextColor(Color.rgb(246, 196, 5));
 		mtxtTurnCnt.setText("Turn : 0");
         mySlotView.addView(mtxtTurnCnt);
         
 		RelativeLayout.LayoutParams turnparams = (RelativeLayout.LayoutParams) mtxtTurnCnt.getLayoutParams();
		mtxtTurnCnt.setLayoutParams(UILayoutParams.changeMargin(turnparams, new Rect(10, 5, 10, 35)));
 
 		///ManaCount初期化
 		mtxtManaCnt = new TextView(this);
 		mtxtManaCnt.setTextColor(Color.rgb(246, 196, 5));
 		mtxtManaCnt.setText("Mana : 0");
 		mySlotView.addView(mtxtManaCnt);
         
 		RelativeLayout.LayoutParams manaparams = (RelativeLayout.LayoutParams) mtxtManaCnt.getLayoutParams();
		mtxtManaCnt.setLayoutParams(UILayoutParams.changeMargin(manaparams, new Rect(100, 5, 10, 35)));
 		
 		///LifeCount初期化
 		mtxtLifeCnt = new TextView(this);
 		mtxtLifeCnt.setTextColor(Color.rgb(246, 196, 5));
 		mtxtLifeCnt.setText("Life : 0");
         mySlotView.addView(mtxtLifeCnt);
         
 		RelativeLayout.LayoutParams lifeparams = (RelativeLayout.LayoutParams) mtxtLifeCnt.getLayoutParams();
		mtxtLifeCnt.setLayoutParams(UILayoutParams.changeMargin(lifeparams, new Rect(210, 5, 10, 35)));
         
 		mDeckManager = new DeckManager();
 		mDeckManager.init();
 		
 		drawCard();
 	}
 	
 	private void drawCard(){
 		for(int i=0;i<3;i++){
 			if(mMyHands[i] == null){
 				mMyHands[i] = mDeckManager.getCardfromDeck();
 				
 				ImageView cardImgView = getCardImageView(mMyHands[i]);
 				mySlotView.addView(cardImgView);
 
 				RelativeLayout.LayoutParams lifeparams = (RelativeLayout.LayoutParams) cardImgView.getLayoutParams();
 				cardImgView.setLayoutParams(UILayoutParams.changeRect(lifeparams, new Rect(12 + (i*90), 430, 70, 90)));
 				
 				mImgMyCard[i] = cardImgView;
 			}
 		}
 	}
 	
 	private ImageView getCardImageView(CardInfo cardinfo) {
 		ImageView cardimg = new ImageView(this);
 		switch(cardinfo.card_id){
 			case 1:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.element1));
 				break;
 			case 2:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.element2));
 				break;
 			case 3:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.element3));
 				break;
 			case 4:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_1_1));
 				break;
 			case 5:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_1_2));
 				break;
 			case 6:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_1_3));
 				break;
 			case 7:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_2_1));
 				break;
 			case 8:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_2_2));
 				break;
 			case 9:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_2_3));
 				break;
 			case 10:
 				cardimg.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_3_1));
 				break;
 		}
 		
 		return cardimg;
 	}
 
 	/**
 	 * viewport 設定処理
 	 */
     private void setDensity() {
     	DisplayMetrics dm = getResources().getDisplayMetrics();
     	float wdp = dm.widthPixels / dm.density;
     	if(wdp > 720) wdp = 720;
     	UILayoutParams.DENSITY_DPI = Math.floor(320 * 160 / wdp);
     	if(UILayoutParams.DENSITY_DPI > 160) UILayoutParams.DENSITY_DPI = 160;
 	}
     
 	/**
 	 * 画面表示時に端末のdp設定処理
 	 */
 	private void appOnAttachedToWindow() {
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
         int px = getWindowManager().getDefaultDisplay().getWidth();
         float dp = px / getResources().getDisplayMetrics().density;
 		if(dp > 720 ){
 			UILayoutParams.SCALED_DENSITY = (float) ((720f / 320f) * metrics.scaledDensity);
 		}else{
 			UILayoutParams.SCALED_DENSITY = (float) (dp / 320) * metrics.scaledDensity;
 		}
 	}
 }
