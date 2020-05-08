 package eu.nanairo_reader.ui.activity;
 
 import static eu.nanairo_reader.ui.constant.NanairoUiConstant.SUBSCRIPTION;
 import static eu.nanairo_reader.ui.service.SampleService.MIDOKU_COUNT;
 import static eu.nanairo_reader.ui.service.SampleService.SAMPLE_SERVICE_ACTION;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 import eu.nanairo_reader.NanairoApplication;
 import eu.nanairo_reader.R;
 import eu.nanairo_reader.business.bean.Subscription;
 import eu.nanairo_reader.business.service.RssService;
 import eu.nanairo_reader.ui.component.SubscriptionArrayAdapter;
 import eu.nanairo_reader.ui.service.SampleService;
 
 public class SubscriptionListActivity extends BaseActivity {
 	/***/
 	private final static int CONTEXT_ITEM_SUBSCRIPTION_DELETE = 1000;
 
 	/***/
 	private final static int CONTEXT_ITEM_ALL_KIDOKU = 1001;
 
 	@Inject
 	RssService rssService;
 
 	private MyServiceReceiver receiver = new MyServiceReceiver();
 
 	private List<Subscription> subscriptionList = new ArrayList<Subscription>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.subscription_list);
 
 		((NanairoApplication) getApplication()).inject(this.rssService);
 
 		// レシーバー登録
 		IntentFilter filter = new IntentFilter(SAMPLE_SERVICE_ACTION);
 		registerReceiver(this.receiver, filter);
 
 		// ListView
 		ListView listView = (ListView) findViewById(R.id.listView);
 
 		// Adapterの設定
 		SubscriptionArrayAdapter listAdapter = new SubscriptionArrayAdapter(getApplicationContext(), this.subscriptionList);
 		listView.setAdapter(listAdapter);
 
 		// コンテキストメニュー登録
 		registerForContextMenu(listView);
 
 		// ListViewクリック
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				ListView listView = (ListView) findViewById(R.id.listView);
 				Subscription subscription = (Subscription) listView.getItemAtPosition(position);
 
 				// インテントのインスタンス生成
 				Intent intent = new Intent(SubscriptionListActivity.this, ArticleListActivity.class);
 				intent.putExtra(SUBSCRIPTION, subscription);
 				startActivity(intent);
 			}
 		});
 
 		// 更新ボタン
 		Button updateButton = (Button) findViewById(R.id.updateButton);
 		updateButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(SubscriptionListActivity.this, SampleService.class);
 				startService(intent);
 			}
 		});
 
 		// 登録ボタン
 		Button entryButton = (Button) findViewById(R.id.entryButton);
 		entryButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(SubscriptionListActivity.this, SubscriptionEntryActivity.class);
 				startActivity(intent);
 			}
 		});
 
 		// 購読一覧を構築
 		rebuildSubscriptionList();
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
 		super.onCreateContextMenu(menu, view, info);
 		AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) info;
 		ListView listView = (ListView) view;
 		Subscription subscription = (Subscription) listView.getItemAtPosition(adapterContextMenuInfo.position);
 
 		// コンテキストメニューの作成
 		menu.setHeaderTitle("メニュー:" + subscription.getTitle());
 		menu.add(0, CONTEXT_ITEM_ALL_KIDOKU, 0, "全て既読にする");
 		menu.add(0, CONTEXT_ITEM_SUBSCRIPTION_DELETE, 0, "購読を削除");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) item.getMenuInfo();
 		ListView listView = (ListView) findViewById(R.id.listView);
 		Subscription subscription = (Subscription) listView.getItemAtPosition(adapterInfo.position);
 
 		switch (item.getItemId()) {
 		case CONTEXT_ITEM_ALL_KIDOKU:
 			// TODO 全既読処理
 			break;
 		case CONTEXT_ITEM_SUBSCRIPTION_DELETE:
			// 購読削除
 			this.rssService.delete(subscription.getId());
			this.subscriptionList.remove(subscription);
			((SubscriptionArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
 			break;
 		}
 
 		return true;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// TODO パフォーマンスが悪いからやめたい
 		// 購読一覧を構築
 		rebuildSubscriptionList();
 	}
 
 	@Override
 	protected void onDestroy() {
 		// レシーバー解除
 		unregisterReceiver(receiver);
 		super.onDestroy();
 	}
 
 	private void rebuildSubscriptionList() {
 		// TODO 未読数を更新する。clearしないように。要リファクタリング
 		List<Subscription> subscriptionList2 = SubscriptionListActivity.this.rssService.getSubscriptionList();
 		this.subscriptionList.clear();
 		this.subscriptionList.addAll(subscriptionList2);
 		ListView listView = (ListView) findViewById(R.id.listView);
 		((SubscriptionArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
 	}
 
 	// Receiverクラス
 	class MyServiceReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			int count = intent.getIntExtra(MIDOKU_COUNT, 0);
 			Toast.makeText(SubscriptionListActivity.this, "更新件数:" + count, Toast.LENGTH_SHORT).show();
 
 			// 購読一覧を構築
 			rebuildSubscriptionList();
 		}
 	}
 }
