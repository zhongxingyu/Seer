 package net.darchangel.shoppingTweeter;
 
 import net.darchangel.shoppingTweeter.util.HistoryTableDAO;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class ShoppingTweeter extends Activity {
 
 	private static final int REQUEST_HISTORY = 0;
 
 	EditText item = null;
 	EditText expense = null;
 	EditText comment = null;
 	Spinner category = null;
 	CheckBox creditcard = null;
 	CheckBox secret = null;
 	Button tweet = null;
 	Button reset = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.main);
 
 		setProgressBarIndeterminateVisibility(false);
 
 		item = (EditText) findViewById(R.id.item);
 		expense = (EditText) findViewById(R.id.expense);
 		comment = (EditText) findViewById(R.id.comment);
 		category = (Spinner) findViewById(R.id.category);
 		creditcard = (CheckBox) findViewById(R.id.creditcard);
 		secret = (CheckBox) findViewById(R.id.secret);
 		tweet = (Button) findViewById(R.id.tweet);
 		reset = (Button) findViewById(R.id.reset);
 
 		// ボタンのコールバックリスナーを登録
 		setAction();
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		if (Pref.getDefaultSecret(ShoppingTweeter.this)) {
 			// secretデフォルト有効の場合
 
 			// secretをチェック
 			secret.setChecked(true);
 		} else {
 			// secretデフォルト無効の場合
 
 			// secretを未チェック
 			secret.setChecked(false);
 		}
 
 		String auth_status = Pref.getStatus(this);
 		if (auth_status.equals("")) {
 			// ログインしてない場合
 
 			// Tweetボタンを非活性化
 			tweet.setEnabled(false);
 
 			// ログインするようメッセージを表示
 			Toast.makeText(ShoppingTweeter.this, R.string.please_login, Toast.LENGTH_LONG).show();
 
 		} else {
 			// ログイン済みの場合
 
 			// Tweetボタンを活性化
 			tweet.setEnabled(true);
 		}
 	}
 
 	/**
 	 * メニューボタンを押したときの動作
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	/**
 	 * 設定画面の呼び出し
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = null;
 		switch (item.getItemId()) {
 		case R.id.menu_pref:
 			// preferenceメニューが押された場合
 			intent = new Intent(this, (Class<?>) Pref.class);
 			startActivity(intent);
 			return false;
 		case R.id.history:
 			// historyメニューが押された場合
 			intent = new Intent(this, (Class<?>) History.class);
 			startActivityForResult(intent, REQUEST_HISTORY);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * 履歴画面からも戻ってきた場合の設定
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		// 履歴画面からIntentを受信している場合
 		if (requestCode == REQUEST_HISTORY && resultCode == RESULT_OK) {
 			// 品目名を入力
 			item.setText(intent.getStringExtra(HistoryTableDAO.COLUMNS[HistoryTableDAO.COLUMN_ITEM_NAME]));
 			// 金額を入力
 			expense.setText(intent.getStringExtra(HistoryTableDAO.COLUMNS[HistoryTableDAO.COLUMN_EXPENSE]));
 			// TODO ほかの項目も履歴から戻す？
 		}
 	}
 
 	/**
 	 * 動作を登録
 	 */
 	private void setAction() {
 		// Tweetボタンがクリックされた時に呼び出されるコールバックリスナーを登録
 		tweet.setOnClickListener(new View.OnClickListener() {
 			/**
 			 * ボタンがクリックされたときに呼び出し
 			 */
 			@Override
 			public void onClick(View v) {
 				TweetTask task = new TweetTask(ShoppingTweeter.this);
 				task.execute();
 			}
 		});
 
 		// Resetボタンがクリックされた時に呼び出されるコールバックリスナーを登録
 		reset.setOnClickListener(new View.OnClickListener() {
 			/**
 			 * ボタンがクリックされたときに呼び出し
 			 */
 			@Override
 			public void onClick(View v) {
 				// フォームをリセット
 				clearForm();
 			}
 		});
 	}
 
 	/**
 	 * フォームをリセットする
 	 */
 	public void clearForm() {
 		item.setText("");
 		expense.setText("");
 		comment.setText("");
 		category.setSelection(0);
 		creditcard.setChecked(false);

		if (Pref.getDefaultSecret(ShoppingTweeter.this)) {
			secret.setChecked(true);
		} else {
			secret.setChecked(false);
		}
 	}
 }
