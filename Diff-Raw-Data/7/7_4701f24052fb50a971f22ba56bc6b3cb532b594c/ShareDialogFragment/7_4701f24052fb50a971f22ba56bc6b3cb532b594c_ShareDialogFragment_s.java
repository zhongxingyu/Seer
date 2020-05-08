 package com.webthreeapp.sreader;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.FacebookAuthorizationException;
 import com.facebook.FacebookOperationCanceledException;
 import com.facebook.FacebookRequestError;
 import com.facebook.Request;
 import com.facebook.RequestAsyncTask;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.Session.OpenRequest;
 import com.facebook.SessionLoginBehavior;
 import com.facebook.SessionState;
 import com.facebook.model.GraphObject;
 
 /*
  * 各種snsに投稿するためのカスタムダイアログ
  *
  */
 
 public class ShareDialogFragment extends DialogFragment {
 
 	private String TAG = "ShareDialogFragment";
 
 	private MySwitch facebookButton = null;
 	private MySwitch twitterButton = null;
 
 	private Button shareButton = null;
 	private TextView sendContentText = null;
 
 	private boolean fbLogin = false;
 	private boolean sendingFbFeed = false;
 
 	private Bundle savedInstanceState = null;
 
 	//Facebookのパーミッション
 	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
 
 	private Session.StatusCallback statusCallback = new SessionStatusCallback();
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 
 		this.savedInstanceState = savedInstanceState;
 
 		Dialog dialog = new Dialog(getActivity());
 		dialog.setContentView(R.layout.share_dialog);
 
 		//投稿スペースなどを画面サイズに対応した大きさにする
 		Display display = getActivity().getWindowManager().getDefaultDisplay();
 
 		sendContentText = (TextView) dialog.findViewById(R.id.sendContentText);
 		sendContentText.setWidth(display.getWidth() - 40);
 
 		shareButton = (Button) dialog.findViewById(R.id.button_share);
 		shareButton.setWidth(display.getWidth() - 40);
 
 		Resources res = getResources();
 		dialog.setTitle(res.getString(R.string.share));
 
 		/*
 		 * ボタンにイベント設定
 		 */
 
 		//facebookボタン
 		facebookButton = (MySwitch) dialog.findViewById(R.id.facebookButton);
 		facebookButton.setOnCheckedChangeListener(new FBOnCheckedChangeListener(this, savedInstanceState)); //トグルボタンバージョン
 		//facebookButton.setOnDrawerOpenListener(new FBOnDrawerOpenListener()); //slidingDrawerを使ったバージョン
 		//facebookButton.setOnDrawerCloseListener(new FBOnDrawerCloseListener(this));
 
 		//facebook session管理
 		Session session = Session.getActiveSession();
 		if (session == null) {
 			if (savedInstanceState != null) {
 				session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
 			}
 			if (session == null) {
 				session = new Session(getActivity());
 			}
 			Session.setActiveSession(session);
 
 			 //facebookのオンオフを記録しておいて、ダイアログ起動時に反映すうｒ
 			if (session.getState() == SessionState.OPENED || session.getState() == SessionState.OPENED_TOKEN_UPDATED) {
 				fbLogin = true;
 				facebookButton.setChecked(true); //ボタンをチェック状態に
 			}
 
 		} else {
 			//開いて、すでにセッションがOPENだったら
 			if (session.getState() == SessionState.OPENED || session.getState() == SessionState.OPENED_TOKEN_UPDATED) {
 				fbLogin = true;
 				facebookButton.setChecked(true); //ボタンをチェック状態に
 			}
 		}
 
 		//twitterボタン
 		twitterButton = (MySwitch) dialog.findViewById(R.id.twitterButton);
 		twitterButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO 自動生成されたメソッド・スタブ
 
 			}
 		});
 
 		//SNSに投稿
 		shareButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				if (fbLogin == true) {
 					sendFBFeed(sendContentText.getText().toString());
 				}
 
 			}
 		});
 
 		return dialog;
 	}
 
 	//facebookボタンのクリック時(トグルボタンバージョン)
 
 	public class FBOnCheckedChangeListener implements OnCheckedChangeListener {
 
 		Fragment fragment = null;
 		private Bundle savedInstanceState;
 
 		public FBOnCheckedChangeListener(Fragment fragment, Bundle savedInstanceState) {
 			this.fragment = fragment;
 			this.savedInstanceState = savedInstanceState;
 
 		}
 
 		@Override
 		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 
 
 			if (isChecked) {
 				//チェックされた時 = ログイン
 				checkLogin();
 			} else {
 				Session session = Session.getActiveSession();
 				//チェックが外れた時 = ログアウト
 				if (!session.isClosed()) {
 					session.closeAndClearTokenInformation();
 				}
 			}
 
 		}
 	}
 
 	//ログイン処理
 	public boolean checkLogin(){
 		Session session = Session.getActiveSession();
 
 
 		//もしセッションがOPEN状態でなければ
 		if (session == null) {
 			if (savedInstanceState != null) {
 				session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
 			}
 			if (session == null) {
 				session = new Session(getActivity());
 			}
 			Session.setActiveSession(session);
 			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
 				session.openForRead(new Session.OpenRequest(getActivity()).setCallback(statusCallback));
 
 				return false; //新たにログインする時はfalseを返す
 			}
 
 		}
 		SessionState state = session.getState();
 		if (state != SessionState.OPENED ) {
 			if (state == SessionState.CLOSED || state == SessionState.CLOSED_LOGIN_FAILED) {
 				//セッションが閉じてたら新しいセッションを開始
 				session = new Session(getActivity());
				Session.setActiveSession(session);
				return false; //新たにログインする時はfalseを返す
 
 			}
 			if (state != SessionState.OPENING && state != SessionState.OPENED && state != SessionState.OPENED_TOKEN_UPDATED){
 				//オープンしてるか、オープン処理中じゃなきゃセッションオープンを試みる
 				//session.openForPublish(new Session.OpenRequest(getActivity()).setCallback(statusCallback).setPermissions(PERMISSIONS));
 				Session.OpenRequest openRequest = new OpenRequest(getActivity()).setCallback(statusCallback).setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);
 				session.openForRead(openRequest);
 
 				return false; //新たにログインする時はfalseを返す
 			}
 
 		}
 
 		return true; //ログイン済みならtrueを変えす
 
 	}
 
 	/*
 	 * slideing drawerを使ったバージョン
 	public class FBOnDrawerOpenListener implements SlidingDrawer.OnDrawerOpenListener {
 
 
 
 
 	@Override
 	public void onDrawerOpened() {
 
 		Session session = Session.getActiveSession();
 		//チェックが外れた時 = ログアウト
 		if (!session.isClosed() ) {
 			session.closeAndClearTokenInformation();
 		};
 
 
 	}
 	}
 
 	public class FBOnDrawerCloseListener implements SlidingDrawer.OnDrawerCloseListener {
 
 	Fragment fragment = null;
 
 
 	public FBOnDrawerCloseListener(Fragment fragment) {
 		this.fragment = fragment;
 
 	}
 	@Override
 	public void onDrawerClosed() {
 		Session session = Session.getActiveSession();
 		if (!session.isOpened() ) {
 			if (session.isClosed()) {
 				//セッションが閉じてたら新しいセッションを開始
 				session = new Session(getActivity());
 				Session.setActiveSession(session);
 			}
 			//セッションオープンを試みる
 			session.openForPublish(new Session.OpenRequest(getActivity()).setCallback(statusCallback).setPermissions(PERMISSIONS));
 		} else {
 			//既にOPEN状態だった時は
 			Session.openActiveSession(getActivity(), fragment, true, statusCallback);
 
 		}
 
 	}
 	}
 	*/
 
 	private void sendFBFeed(String feedString) {
 		sendingFbFeed = true;
 		Session session = Session.getActiveSession();
 
 		//ログインチェック
 		if(checkLogin() != true){
 			return; //ログインされてない時はまた今度
 		}
 
 		// Check for publish permissions
 		List<String> permissions = session.getPermissions();
 		//求めるパーミッションがなかったら
 		if (!isSubsetOf(PERMISSIONS, permissions)) {
 			//パーミッションをリクエスト
 			//フラグメントじゃなくてアクティビティの方につけてみた
 			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(getActivity(),
 					PERMISSIONS).setCallback(statusCallback).setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);
 			session.requestNewPublishPermissions(newPermissionsRequest);
 			return; //迂回してしまう
 		}
 
 		//投稿リクエスト
 		Request request = Request.newStatusUpdateRequest(session, feedString, new Request.Callback() {
 			public void onCompleted(Response response) {
 				showPublishResult(response.getGraphObject(), response.getError());
 			}
 		});
 
 		RequestAsyncTask task = new RequestAsyncTask(request);
 		task.execute();
 	}
 
 	//コレクションに含まれているかチェックする関数
 	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
 		for (String string : subset) {
 			if (!superset.contains(string)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	//結果をアラートする関数
 	private void showPublishResult(GraphObject result, FacebookRequestError error) {
 
 		if (error == null) {
 			Toast.makeText(getActivity(), "facebook投稿成功", Toast.LENGTH_LONG).show();
 			sendContentText.setText(""); //投稿成功したら空に
 			sendingFbFeed = false;
 
 		} else {
 			Toast.makeText(getActivity(), "facebook投稿失敗", Toast.LENGTH_LONG).show();
 
 		}
 
 	}
 
 	//ログイン状態変化のコールバック
 	private class SessionStatusCallback implements Session.StatusCallback {
 		@Override
 		public void call(Session session, SessionState sessionState, Exception exception) {
 
 			//ログインエラーがあったら
 			if ((exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
 				fbLogin = false;
 				facebookButton.setChecked(false);//トグルボタンをオフに
 			} else {
 
 					if (sendingFbFeed == true) {
 						//まだ送信中の時は、投稿を試みる
 						sendFBFeed(sendContentText.getText().toString());
 					}
 
 					if (sessionState == SessionState.OPENED || sessionState == SessionState.OPENED_TOKEN_UPDATED){
 						//ログインしている時
 						fbLogin = true;
 
 						Toast.makeText(getActivity(), "facebook認証成功", Toast.LENGTH_SHORT).show();
 
 						if (!facebookButton.isChecked()) {
 							facebookButton.setChecked(true);//トグルボタンをオンに
 						}
 
 					}
 
 					//OPENING時はなにもしない
 
 					if (sessionState == SessionState.CLOSED || sessionState == SessionState.CLOSED_LOGIN_FAILED){
 						fbLogin = false;
 						if (facebookButton.isChecked()) {
 							facebookButton.setChecked(false);//トグルボタンをオフに
 						}
 					}
 
 			}
 
 
 		}
 	}
 
 	//fragmentの各ライフサイクル
 	@Override
 	public void onStart() {
 		super.onStart();
 		Session.getActiveSession().addCallback(statusCallback);
 
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		Session.getActiveSession().removeCallback(statusCallback);
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Session session = Session.getActiveSession();
 
 		if (session.getState().equals(SessionState.CLOSED_LOGIN_FAILED)
 				|| session.getState().equals(SessionState.CLOSED)) {
 			Toast.makeText(getActivity(), "Facebook認証失敗", Toast.LENGTH_LONG).show();
 
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		Session session = Session.getActiveSession();
 		Session.saveSession(session, outState);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
 	}
 
 }
