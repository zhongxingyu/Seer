 
 package com.michitsuchida.marketfavoritter.main;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.michitsuchida.marketfavoritter.db.DBMainStore;
 import com.michitsuchida.marketfavoritter.util.SdUtils;
 import com.michitsuchida.marketfavoritter.util.StringUtils;
 import com.michitsuchida.marketfavoritter.util.XmlUtils;
 
 /**
  * このアプリのメインのActivity。Bookmark一覧を表示する。
  * 
  * @author MichiTsuchida
  */
 public class MarketFavoritterActivity extends Activity {
 
     /** LOG TAG */
     static final String LOG_TAG = "MarketBookmark";
 
     /** SharedPreferenceに保存するためのキー */
     private static final String SHARED_PREF_KEY_SORT_ODER = "SortOrder";
 
     private static final String SHARED_PREF_KEY_FILTER = "Filter";
 
     /** インポート処理のMessageのwhat */
     private static final int MESSAGE_IMPORT = 0;
 
     /** エクスポート処理のMessageのwhat */
     private static final int MESSAGE_EXPORT = 1;
 
     /** メニューID */
     // 並び替え
     private final int MENU_ID1 = Menu.FIRST;
 
     // フィルタ
     private final int MENU_ID2 = Menu.FIRST + 1;
 
     // アイテム編集
     private final int MENU_ID3 = Menu.FIRST + 2;
 
     // アイテム削除
     private final int MENU_ID4 = Menu.FIRST + 3;
 
     // 他のアプリに共有
     private final int MENU_ID5 = Menu.FIRST + 4;
 
     // インポート/エクスポート
     private final int MENU_ID6 = Menu.FIRST + 5;
 
     /** このアプリのContext */
     private final Context mContext = this;
 
     /** アプリのリスト */
     private List<AppElement> mAppList = new ArrayList<AppElement>();
 
     /** DBを操作する */
     private DBMainStore mMainStore;
 
     /** SharedPreferenceオブジェクト */
     private SharedPreferences mSharedPref;
 
     /** 処理中にくるくるさせるダイアログ */
     private ProgressDialog mProg;
 
     /**
      * onCreate.
      * 
      * @param savedInstanceState
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(LOG_TAG, "onCreate()");
 
         // SharedPreferenceオブジェクトの初期化
         mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 
         setContentView(R.layout.main);
         if (mMainStore == null) {
             mMainStore = new DBMainStore(this, true);
         }
         // onResume()で呼ぶのでここでは呼ばない
         // buildListView();
     }
 
     /**
      * onRestart.
      */
     @Override
     public void onRestart() {
         super.onRestart();
         Log.d(LOG_TAG, "onRestert()");
         if (mMainStore == null) {
             mMainStore = new DBMainStore(this, true);
         }
         buildListView();
     }
 
     /**
      * onResume.
      */
     @Override
     public void onResume() {
         super.onResume();
         Log.d(LOG_TAG, "onResume()");
         if (mMainStore == null) {
             mMainStore = new DBMainStore(this, true);
         }
         buildListView();
     }
 
     /**
      * onPause.
      */
     @Override
     public void onPause() {
         super.onPause();
         Log.d(LOG_TAG, "onPause()");
         mMainStore.close();
         mMainStore = null;
         putFilter("");
     }
 
     /**
      * メニューボタンが押された時のイベントハンドラ。
      * 
      * @param menu メニュー
      * @return メニューが表示される場合はtrue、そうでなければfalse
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         boolean ret = super.onCreateOptionsMenu(menu);
         menu.add(0, MENU_ID1, Menu.NONE, R.string.menu_sort_item);
         menu.add(0, MENU_ID2, Menu.NONE, R.string.menu_filter_item);
         menu.add(0, MENU_ID3, Menu.NONE, R.string.menu_edit_item);
         menu.add(0, MENU_ID4, Menu.NONE, R.string.menu_remove_item);
         menu.add(0, MENU_ID5, Menu.NONE, R.string.menu_share_item);
         menu.add(0, MENU_ID6, Menu.NONE, R.string.menu_import_export);
         return ret;
     }
 
     /**
      * メニューから項目を選択された時のイベントハンドラ。
      * 
      * @param item メニューアイテム
      * @return メニューアイテムが表示される場合はtrue、そうでなければfalse
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         boolean ret = super.onOptionsItemSelected(item);
 
         switch (item.getItemId()) {
             // リストをソート
             case MENU_ID1:
                 String[] orders = this.getResources().getStringArray(R.array.sort_order);
                 AlertDialog.Builder dialogSort = new AlertDialog.Builder(this);
                 dialogSort.setTitle(R.string.sort_title);
                 dialogSort.setItems(orders, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         switch (which) {
                             case 0:
                                 // デフォルト(_id昇順)
                                 sort(DBMainStore.COLUMN_ID, DBMainStore.ASC);
                                 break;
 
                             case 1:
                                 // アプリ名昇順
                                 sort(DBMainStore.COLUMN_APP_NAME, DBMainStore.ASC);
                                 break;
 
                             case 2:
                                 // アプリ名降順
                                 sort(DBMainStore.COLUMN_APP_NAME, DBMainStore.DESC);
                                 break;
 
                             case 3:
                                 // パッケージ名昇順
                                 sort(DBMainStore.COLUMN_APP_PACKAGE, DBMainStore.ASC);
                                 break;
 
                             case 4:
                                 // パッケージ名降順
                                 sort(DBMainStore.COLUMN_APP_PACKAGE, DBMainStore.DESC);
                                 break;
 
                             default:
                                 // do nothing.
                                 break;
                         }
                     }
                 });
                 dialogSort.show();
                 break;
 
             // リストをlabelでフィルタリング
             case MENU_ID2:
                 final String[] labelArray = buildLabelList();
                 String[] labelArrayWithCount = new String[labelArray.length];
 
                 // labelごとのアプリの数を数える
                 DBMainStore mainStore = new DBMainStore(this, true);
                 int appCount = 0;
                 // List<String> labelList = new ArrayList<String>();
                 for (int i = 0; i < labelArray.length; i++) {
                     // 配列の1,2番目は「フィルタをクリア」と「ラベル未設定」が入ってるのでそれは件数を数えない
                     if (i <= 1) {
                         // labelList.add(labelArray[i]);
                         labelArrayWithCount[i] = labelArray[i];
                     } else {
                         appCount = mainStore.getAppCountOfLabel(labelArray[i]);
                         // labelList.add(labelArray[i] + "(" + appCount + ")");
                         labelArrayWithCount[i] = labelArray[i] + "(" + appCount + ")";
                     }
                 }
 
                 AlertDialog.Builder dialogFilter = new AlertDialog.Builder(this);
                 dialogFilter.setTitle(R.string.filter_title);
                 dialogFilter.setItems(labelArrayWithCount, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int labelIndex) {
                         if (labelIndex == 0) {
                            // 「フィルタをクリア」だから、SharedPreferenceの値をリセットする
                            putFilter("");
                             buildListView();
                         } else if (labelIndex == 1) {
                             // 「ラベル未設定」なので、空文字でフィルタする
                             filter("");
                         } else {
                             // 「フィルタをクリア」以外だったらフィルタリングする
                             filter(labelArray[labelIndex]);
                         }
                     }
                 });
                 dialogFilter.show();
                 break;
 
             // リストを編集
             case MENU_ID3:
                 List<String> editIds = new ArrayList<String>();
                 for (int i = 0; i < mAppList.size(); i++) {
                     if (mAppList.get(i).getIsChecked()) {
                         editIds.add(String.valueOf(mAppList.get(i).get_id()));
                     }
                 }
 
                 if (editIds.size() > 0) {
                     // 編集画面のActivityをチェックついたアイテムの数だけ呼び出す
                     for (int i = 0; i < editIds.size(); i++) {
                         // 編集画面を呼び出す
                         Intent intent = new Intent(this, MarketFavoritterEditActivity.class);
                         // _idだけ渡して向こうでデータを取得する
                         intent.putExtra("ID", editIds.get(i));
                         startActivity(intent);
                     }
                     buildListView();
                 } else {
                     Log.i(LOG_TAG, "There are no selected item(s) of edit");
                     Toast.makeText(this, R.string.toast_no_item_is_checked, Toast.LENGTH_LONG)
                             .show();
                 }
                 break;
 
             // リストから削除
             case MENU_ID4:
                 final List<String> delIds = new ArrayList<String>();
                 for (int i = 0; i < mAppList.size(); i++) {
                     if (mAppList.get(i).getIsChecked()) {
                         delIds.add(String.valueOf(mAppList.get(i).get_id()));
                     }
                 }
 
                 if (delIds.size() > 0) {
                     AlertDialog.Builder dialogRemove = new AlertDialog.Builder(this);
                     dialogRemove.setTitle(R.string.dialog_remove_title);
                     dialogRemove.setMessage(R.string.dialog_remove_text);
 
                     // OKボタン
                     dialogRemove.setPositiveButton(R.string.button_ok,
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int i) {
                                     // mMainStore.delete(delIds.toArray(new
                                     // String[] {}));
                                     mMainStore.delete(delIds.toArray(new String[delIds.size()]));
                                     Log.i(LOG_TAG, "Selected item was deleted");
                                     buildListView();
                                 }
                             });
                     // キャンセルボタン
                     dialogRemove.setNegativeButton(R.string.button_cancel,
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int i) {
                                     // 何もしない
                                 }
                             });
                     // キャンセル可能にする
                     dialogRemove.setCancelable(true);
 
                     dialogRemove.show();
                 } else {
                     Log.i(LOG_TAG, "There are no selected item(s) of delete");
                     Toast.makeText(this, R.string.toast_no_item_is_checked, Toast.LENGTH_LONG)
                             .show();
                 }
                 break;
 
             // アイテムを他のアプリに共有
             case MENU_ID5:
                 buildShareIntent();
                 break;
 
             // インポート/エクスポート
             case MENU_ID6:
                 importAndExport();
                 break;
 
             // default
             default:
                 break;
         }
         return ret;
     }
 
     /**
      * 他のアプリに共有するためのIntentを作成する。<br>
      * "<アプリ名> <マーケットURL>"という感じのテキストを投げる。
      */
     private void buildShareIntent() {
         List<String> shareIds = new ArrayList<String>();
         for (int i = 0; i < mAppList.size(); i++) {
             if (mAppList.get(i).getIsChecked()) {
                 shareIds.add(String.valueOf(mAppList.get(i).get_id()));
             }
         }
         if (shareIds.size() == 1) {
             AppElement elem = mMainStore.fetchAppDataByColumnAndValue(DBMainStore.COLUMN_ID,
                     shareIds.get(0));
 
             // Intentにアプリ名とマーケットのURLを埋め込む
             Intent intent = new Intent(Intent.ACTION_SEND);
             intent.setType("text/plain");
             intent.putExtra(Intent.EXTRA_TEXT, elem.getAppName() + " " + elem.getMarketUrl());
             try {
                 // Intent投げる
                 startActivity(Intent.createChooser(intent, getString(R.string.dialog_share_title)
                         + elem.getAppName()));
             } catch (android.content.ActivityNotFoundException e) {
                 // 該当するActivityがないときの処理
                 e.printStackTrace();
             }
         } else if (shareIds.size() > 1) {
             Log.d(LOG_TAG, "There are many selected items " + shareIds.size());
             Toast.makeText(this, R.string.toast_many_item_is_checked, Toast.LENGTH_LONG).show();
         } else {
             Log.i(LOG_TAG, "There are no selected item to share");
             Toast.makeText(this, R.string.toast_no_item_is_checked, Toast.LENGTH_LONG).show();
         }
     }
 
     /**
      * ラベルの一覧を作成する。先頭に「フィルタをクリア」を格納する。
      * 
      * @return ラベル一覧の配列
      */
     private String[] buildLabelList() {
 
         // すべてのデータのラベル部分を取得する
         List<AppElement> apps = mMainStore.fetchAllAppData(null);
         List<String> labelList = new ArrayList<String>();
         for (AppElement elem : apps) {
             labelList.add(elem.getLabel());
         }
         // Log.d(LOG_TAG, labelList.toString());
         // ラベルの重複をなくす
         List<String> splittedLabelList = new ArrayList<String>();
         for (String string1 : labelList) {
             // ラベルが1個もないとぬるぽになる
             if (string1 != null) {
                 String[] str = string1.split(",");
                 for (String string2 : str) {
                     splittedLabelList.add(string2);
                 }
             }
         }
         // Log.d(LOG_TAG, splittedLabelList.toString());
         List<String> duplicatedLabelList = new ArrayList<String>();
         for (int i = 0; i < splittedLabelList.size(); i++) {
             if (!duplicatedLabelList.contains(splittedLabelList.get(i))
                     && !splittedLabelList.get(i).equals("")) {
                 duplicatedLabelList.add(splittedLabelList.get(i));
             }
         }
         // 完成したラベルリストを並び替える
         Collections.sort(duplicatedLabelList);
 
         // リストの最初に「フィルタをクリア」を格納、2番目には「ラベル未設定」を格納し、配列に変換
         duplicatedLabelList.add(0, getString(R.string.filter_clear_label));
         duplicatedLabelList.add(1, getString(R.string.filter_label_na));
         Log.d(LOG_TAG, duplicatedLabelList.toString());
 
         return duplicatedLabelList.toArray(new String[duplicatedLabelList.size()]);
     }
 
     /**
      * アプリのリストを作成する。<br>
      * 並び替えのオーダーがある場合は、それに従う。
      */
     private void buildListView() {
         // SharedPreferenceに保存された並び替えのオーダーを取得して、その順でリストを作成する
         mAppList = mMainStore.fetchAllAppData(getSortOrder());
 
         if (mMainStore.getCount() == 0) {
             Log.i(LOG_TAG, "AppList is empty!!");
 
             // DBの件数が0件だったら、インポートしますか?的なダイアログを出す
             AlertDialog.Builder dialog = new AlertDialog.Builder(this);
             dialog.setTitle(R.string.dialog_import_title);
             dialog.setMessage(R.string.dialog_import_text);
             // OKボタン
             dialog.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int i) {
                     // XMLをインポートする
                     // 1.プログレスバーをくるくる
                     mProg = new ProgressDialog(mContext);
                     mProg.setTitle(R.string.progress_import_title);
                     mProg.setMessage(getString(R.string.progress_import_body));
                     mProg.show();
                     // 2.別スレッドを生成して、インポートする
                     (new Thread(importRunnable)).start();
                 }
             });
             // キャンセルボタン
             dialog.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int i) {
                     // Activityを終了する
                     finish();
                     Toast.makeText(mContext, R.string.toast_no_item_in_list, Toast.LENGTH_LONG)
                             .show();
                 }
             });
             dialog.show();
 
         }
 
         AppElementAdapter adapter = new AppElementAdapter(mContext, R.id.inflaterLayout, mAppList);
         ListView listView = (ListView) findViewById(R.id.mainAppListView);
         listView.setAdapter(adapter);
     }
 
     /**
      * XMLフォーマットで書き出すための書式に成形する。
      * 
      * @param elements AppElementのリスト
      * @return XMLフォーマットに整形された文字列
      */
     private String buildXmlString(List<AppElement> elements) {
         StringBuffer buff = new StringBuffer();
         buff.append(StringUtils.XML_ROOT_ELEMENT_START);
         for (AppElement appElement : elements) {
             buff.append(StringUtils.convertAppElementToXmlFormat(appElement.getAppName(),
                     appElement.getPkgName(), appElement.getMarketUrl(), appElement.getLabel()));
         }
         buff.append(StringUtils.XML_ROOT_ELEMENT_END);
         String xml = buff.toString();
         Log.d(LOG_TAG, "Convert to XML");
         // Log.d(LOG_TAG, "Convert to XML: " + xml);
         return xml;
     }
 
     /**
      * インポート/エクスポートを実行する。<br>
      * エクスポートするときは、すでにXMLがある場合は上書きされる。<br>
      * 処理中はプログレスバーを表示させる。
      */
     private void importAndExport() {
         // インポートするかエクスポートするかのダイアログ出す
         // すでにエクスポートしたXMLがある場合は上書きされる警告も出す
         AlertDialog.Builder dialog = new AlertDialog.Builder(this);
         dialog.setTitle(R.string.dialog_import_or_export_title);
         dialog.setMessage(R.string.dialog_import_or_export_text);
 
         // インポートボタン
         dialog.setPositiveButton(R.string.button_import, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int i) {
                 // XMLをインポートする
                 // 1.プログレスバーをくるくる
                 mProg = new ProgressDialog(mContext);
                 mProg.setTitle(R.string.progress_import_title);
                 mProg.setMessage(getString(R.string.progress_import_body));
                 mProg.show();
                 // 2.別スレッドを生成して、インポートする
                 (new Thread(importRunnable)).start();
             }
         });
         // エクスポートボタン
         dialog.setNegativeButton(R.string.button_export, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 // XMLをエクスポートする
                 // 1.プログレスバーをくるくる
                 mProg = new ProgressDialog(mContext);
                 mProg.setTitle(R.string.progress_export_title);
                 mProg.setMessage(getString(R.string.progress_export_body));
                 mProg.show();
                 // 2.別スレッドを生成して、エクスポートする
                 (new Thread(exportRunnable)).start();
             }
         });
         dialog.show();
     }
 
     /**
      * インポート処理を実行するためのスレッド。
      */
     private Runnable importRunnable = new Runnable() {
         @Override
         public void run() {
             // 実際に別スレッドでやりたい処理はここで
             // 3.UIのテキストを変更 UIを書き換える場合はHandlerに処理を渡す
             boolean result = importXml();
             Message msg = Message.obtain(handler, MESSAGE_IMPORT);
             msg.obj = result;
             handler.sendMessage(msg);
 
             // 4.実際に行いたい処理が終わったらダイアログを消去
             mProg.dismiss();
         };
     };
 
     /**
      * XMLを読み出し、DBに登録する。<br>
      * 処理中はプログレスバーを表示させる。
      * 
      * @return DBへ登録成功した場合はtrue、エラー発生時はfalse
      */
     private boolean importXml() {
         Log.i(LOG_TAG, "Import XML");
         // XMLを読み出す
         String importText = SdUtils.readFromSdCard();
         if (importText != null) {
             // Log.d(LOG_TAG, importText);
             // 読み出せたらXMLをパースする
             List<AppElement> appList = XmlUtils.analyzeXml(importText);
             if (appList == null) {
                 return false;
             }
 
             // パース出来たらDBのデータと読み出したデータのpkg要素を比較して重複チェックする
             DBMainStore mainStore = new DBMainStore(this, true);
             // mainStore.mCountにDBの件数を格納するために実行する
             mainStore.fetchAllAppData(null);
 
             AppElement dbAppElement = new AppElement();
             if (mainStore.getCount() > 0) {
                 for (AppElement appElement : appList) {
                     dbAppElement = mainStore.fetchAppDataByColumnAndValue(
                             DBMainStore.COLUMN_APP_PACKAGE, appElement.getPkgName());
                     if (dbAppElement != null) {
                         // すでにDBに同じPackage名のアプリがある場合は飛ばす
                         Log.d(LOG_TAG, "Overlapped data detect, skip");
                         continue;
                     } else {
                         // Log.d(LOG_TAG, "Added to database: " +
                         // appElement.toString());
                         // 重複してなかったら追加する
                         mainStore.add(appElement.getAppName(), appElement.getPkgName(),
                                 appElement.getMarketUrl(), appElement.getLabel());
                     }
                 }
             } else {
                 for (AppElement appElement : appList) {
                     // Log.d(LOG_TAG, "Added all apps to database: " +
                     // appElement.toString());
                     // DBにデータが何も無いのでそのまま追加する
                     mainStore.add(appElement.getAppName(), appElement.getPkgName(),
                             appElement.getMarketUrl(), appElement.getLabel());
                 }
             }
             return true;
         } else {
             // SDカードにファイルがなかった、SDカードがマウントされてなかった、とか。。
             return false;
         }
     }
 
     /**
      * エクスポート処理を実行するためのスレッド。
      */
     private Runnable exportRunnable = new Runnable() {
         @Override
         public void run() {
             // 実際に別スレッドでやりたい処理はここで
             // 3.UIのテキストを変更 UIを書き換える場合はHandlerに処理を渡す
             boolean result = exportXml();
             Message msg = Message.obtain(handler, MESSAGE_EXPORT);
             msg.obj = result;
             handler.sendMessage(msg);
 
             // 4.実際に行いたい処理が終わったらダイアログを消去
             mProg.dismiss();
         };
     };
 
     /**
      * アプリ一覧をXML形式にして、XMLに書き出す。<br>
      * 処理中はプログレスバーを表示させる。
      * 
      * @return SDカードへの書き出しに成功した場合はtrue、そうでなければfalse
      */
     private boolean exportXml() {
         Log.i(LOG_TAG, "Export XML");
         // アプリ一覧をXMLフォーマットに整形する
         String xml = buildXmlString(mAppList);
         if (xml.length() != 0) {
             // SDカードに書き出して、その結果をreturnする
             return SdUtils.writeToSdCard(xml);
         }
         return false;
     }
 
     /**
      * 別スレッドで実行したインポート/エクスポートのHandler。
      */
     private final Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             if (msg.obj instanceof Boolean) {
                 // Messageのwhatでインポート/エクスポートを判別する
                 Boolean result = (Boolean) msg.obj;
                 switch (msg.what) {
                     // インポート
                     case MESSAGE_IMPORT:
                         if (result) {
                             Log.d(LOG_TAG, "Import XML done!");
                             Toast.makeText(mContext, R.string.toast_import_xml_success,
                                     Toast.LENGTH_LONG).show();
                             buildListView();
                         } else {
                             Log.e(LOG_TAG, "Failed to read XML from SDcard!!");
                             finish();
                             Toast.makeText(mContext, R.string.toast_import_xml_fail,
                                     Toast.LENGTH_LONG).show();
                         }
                         break;
 
                     // エクスポート
                     case MESSAGE_EXPORT:
                         if (result) {
                             Log.d(LOG_TAG, "Export XML done!");
                             Toast.makeText(mContext, R.string.toast_export_xml_success,
                                     Toast.LENGTH_LONG).show();
                         } else {
                             Log.e(LOG_TAG, "Failed to write XML to SDcard!!");
                             // finish();
                             Toast.makeText(mContext, R.string.toast_export_xml_fail,
                                     Toast.LENGTH_LONG).show();
                         }
                         break;
 
                     default:
                         break;
                 }
             }
         }
     };
 
     /**
      * 並び替えを実行する。<br>
      * 並び替えた後、アプリのリスト作成まで行う。
      * 
      * @param orderColumn 並び替えの列(データベースのカラム名で指定)
      * @param sortOrder 昇順(ASC)または降順(DESC)
      */
     private void sort(String orderColumn, String sortOrder) {
         String order = orderColumn + " " + sortOrder;
         Log.d(LOG_TAG, "Sort order: " + order);
         mAppList = mMainStore.fetchFilteredAppData(getFilter(), order);
 
         AppElementAdapter adapter = new AppElementAdapter(this, R.id.inflaterLayout, mAppList);
         ListView listView = (ListView) findViewById(R.id.mainAppListView);
         listView.setAdapter(adapter);
 
         // 並び替えのオーダーを保存しておく
         putSortOrder(order);
     }
 
     /**
      * フィルタリングを実行する。<br>
      * フィルタリングした後、アプリのリスト作成まで行う。
      * 
      * @param filter フィルタリングするラベル
      */
     private void filter(String filter) {
         Log.d(LOG_TAG, "Filter of: " + filter);
         mAppList = mMainStore.fetchFilteredAppData(filter, getSortOrder());
 
         AppElementAdapter adapter = new AppElementAdapter(this, R.id.inflaterLayout, mAppList);
         ListView listView = (ListView) findViewById(R.id.mainAppListView);
         listView.setAdapter(adapter);
 
         // フィルタリングのラベルを保存しておく
         putFilter(filter);
     }
 
     /**
      * フィルタリングのラベルを取得する。<br>
      * 初回起動時は値が無いので、第2引数の値をdefault値として取得する。<br>
      * また、もし値が取得出来なかった時もこの値が取得される。
      * 
     * @return 取得したフィルタリングのラベル、値がない場合は""(空文字)
      */
     private String getFilter() {
        return this.mSharedPref.getString(SHARED_PREF_KEY_FILTER, "");
     }
 
     /**
      * フィルタリングのラベルをSharedPreferenceに書き込む。<br>
      * 最後のcommit()を行った時点で書き込まれる。<br>
      * 
      * @param filter フィルタリングのラベル
      */
     private void putFilter(String filter) {
         this.mSharedPref.edit().putString(SHARED_PREF_KEY_FILTER, filter).commit();
         Log.d(LOG_TAG, "Filtering label put to share preference [" + filter + "]");
     }
 
     /**
      * 並び替えのオーダーを取得する。<br>
      * 初回起動時は値が無いので、第2引数の値をdefault値として取得する。<br>
      * また、もし値が取得出来なかった時もこの値が取得される。
      * 
      * @return 取得した並び替えのオーダー、値がない場合はnull
      */
     private String getSortOrder() {
         return this.mSharedPref.getString(SHARED_PREF_KEY_SORT_ODER, null);
     }
 
     /**
      * 並び替えのオーダーをSharedPreferenceに書き込む。<br>
      * 最後のcommit()を行った時点で書き込まれる。<br>
      * 
      * @param order 並び替えのオーダー
      */
     private void putSortOrder(String order) {
         this.mSharedPref.edit().putString(SHARED_PREF_KEY_SORT_ODER, order).commit();
         Log.d(LOG_TAG, "Sort order put to share preference [" + order + "]");
     }
 
     // ============================================================================================
     /**
      * ArrayAdapterを拡張したインナークラス。
      */
     class AppElementAdapter extends ArrayAdapter<AppElement> {
 
         /** AppElement型のリスト */
         private List<AppElement> mList;
 
         /** カスタムListViewを作成するためのInflator */
         private LayoutInflater mInflater;
 
         /**
          * コンストラクタ。
          * 
          * @param context このアプリケーションのコンテキスト
          * @param resourceId リソースのID
          * @param mList 描画するデータの格納されたリスト
          */
         public AppElementAdapter(Context context, int resourceId, List<AppElement> list) {
             super(context, resourceId, list);
             this.mList = list;
             this.mInflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         }
 
         /**
          * AppElement分のViewをInflateして作成する。
          * 
          * @param position AppElementリストのインデックス
          * @param convertView View
          * @param parent このViewの親View
          * @return 作成されたカスタムListView
          */
         @Override
         public View getView(final int position, View convertView, ViewGroup parent) {
             // Viewが使いまわされていない場合、nullが格納されている
             if (convertView == null) {
                 // 1行分layoutからViewの塊を生成
                 convertView = mInflater.inflate(R.layout.inflater, null);
                 // Log.d(LOG_TAG, "New convertView create");
             }
 
             // listからAppのデータ、viewから画面にくっついているViewを取り出して値を格納する
             final AppElement app = mList.get(position);
             TextView appNameText = (TextView) convertView.findViewById(R.id.inflaterAppName);
             appNameText.setText(app.getAppName());
             TextView appPkgText = (TextView) convertView.findViewById(R.id.inflaterAppPkgName);
             appPkgText.setText(app.getPkgName());
             TextView appLabel = (TextView) convertView.findViewById(R.id.inflaterLabel);
             // ラベルのカンマはスペースに直して表示する
             String tmpLabel = app.getLabel();
             if (tmpLabel == null) {
                 tmpLabel = "";
                 Log.d(LOG_TAG, "Label is empty!");
             } else {
                 tmpLabel = app.getLabel().replace(",", " ");
             }
             appLabel.setText(tmpLabel);
 
             // Buttonを実装
             Button button = (Button) convertView.findViewById(R.id.inflaterButton);
             button.setText(R.string.button_market);
             button.setOnClickListener(new OnClickListener() {
                 public void onClick(View v) {
                     // MarketアプリへのIntentを作成して投げる
                     // AndroidMarketのアプリ詳細画面を開く
                     // market://details?id=<pkg name>
                     // AndroidMarketをアプリ開発者名で検索
                     // market://search?q=pub:<publisher name>
                     // AndroidMarketをフリーワード検索
                     // market://search?q=<words>
                     Uri uri = Uri.parse("market://details?id=" + app.getPkgName());
                     startActivity(new Intent(Intent.ACTION_VIEW, uri));
                     Log.d(LOG_TAG, "Throw intent for AndroidMarket. Uri: " + uri.toString());
                 }
             });
 
             // CheckBoxを実装
             final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.inflaterCheckBox);
             final int pos = position;
             // setChecked()をやる前にリスナ登録しないと、
             // 使いまわしてる他のViewのチェックも道連れにチェックされるｗ
             checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                 public void onCheckedChanged(CompoundButton compoundbutton, boolean isChecked) {
                     Log.d(LOG_TAG,
                             "pos: " + String.valueOf(pos) + ", mIsChecked: "
                                     + String.valueOf(isChecked));
                     mList.get(pos).setIsChecked(isChecked);
                 }
             });
             checkBox.setChecked(mList.get(position).getIsChecked());
 
             // これでロングタップした時のポップアップメニューが作れるよ!!
             // v.setOnCreateContextMenuListener(new
             // OnCreateContextMenuListener() {
             // public void onCreateContextMenu(ContextMenu contextmenu, View
             // view, ContextMenuInfo contextmenuinfo) {
             // }
             // });
             return convertView;
         }
     }
 }
