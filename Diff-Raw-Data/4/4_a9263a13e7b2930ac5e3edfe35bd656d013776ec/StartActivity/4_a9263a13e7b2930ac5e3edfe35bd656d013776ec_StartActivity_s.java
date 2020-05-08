 package ru.rutube.RutubeApp.ui;
 
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.util.Log;
 
 import ru.rutube.RutubeAPI.BuildConfig;
 import ru.rutube.RutubeAPI.models.Constants;
 import ru.rutube.RutubeApp.R;
 import ru.rutube.RutubeApp.ctrl.MainPageController;
 import ru.rutube.RutubeApp.ui.dialog.LoginDialogFragment;
 import ru.rutube.RutubeApp.ui.feed.PlaFeedFragment;
 
 import java.util.HashMap;
 
 public class StartActivity extends ActionBarActivity implements MainPageController.MainPageView, ActionBar.TabListener {
     private static final String LOG_TAG = StartActivity.class.getName();
     private static final String CONTROLLER = "controller";
     private static final int LOGIN_REQUEST_CODE = 1;
     private static final boolean D = BuildConfig.DEBUG;
 
     private MainPageController mController;
     private HashMap<String, ActionBar.Tab> mTabMap = new HashMap<String, ActionBar.Tab>();
     private HashMap<String, Fragment> mFragmentMap = new HashMap<String, Fragment>();
     private FragmentTransaction mFragmentTransaction;
     private Fragment mCurrentFragment;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         if (savedInstanceState != null)
             mController = savedInstanceState.getParcelable(CONTROLLER);
         else
             mController = new MainPageController();
         super.onCreate(savedInstanceState);
         setContentView(R.layout.start_activity);
         mController.attach(this, this);
         initTabs();
     }
 
     @Override
     public void onAttachFragment(Fragment fragment) {
         // После вызова super.onCreate из сохраненного состояния автоматически восстанавливается
         // последний фрагмент.
         super.onAttachFragment(fragment);
         if (mFragmentMap.get(fragment.getTag()) == null)
             mFragmentMap.put(fragment.getTag(), fragment);
         mCurrentFragment = fragment;
     }
 
     /**
      * Проксирует обработку выбора вкладки в контроллер, попутно запоминая текущую транзакцию
      * фрагмента для использования в обратном вызове
      * @param tab тег вкладки
      * @param fragmentTransaction
      */
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         mFragmentTransaction = fragmentTransaction;
         String tag = (String)tab.getTag();
         mController.onTabSelected(tag);
         mFragmentTransaction = null;
 
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 
     }
 
     @Override
     public void showLoginDialog() {
         LoginDialogFragment.InputDialogFragmentBuilder builder = new LoginDialogFragment.InputDialogFragmentBuilder(this);
         builder.setOnDoneListener(new LoginDialogFragment.OnDoneListener() {
             @Override
             public void onFinishInputDialog(String emailText, String passwordText) {
                 mController.startLoginRequests(emailText, passwordText);
             }
         }).setOnCancelListener(new LoginDialogFragment.OnCancelListener() {
             @Override
             public void onCancel() {
                 mController.loginCanceled();
             }
         }).show();
     }
 
     @Override
     public void showError() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.
                 setTitle(android.R.string.dialog_alert_title).
                 setMessage(getString(ru.rutube.RutubePlayer.R.string.failed_to_load_data)).
                 create().
                 show();
     }
 
     /**
      * Делает активной вкладку с тегом
      * @param tag
      */
     public void selectTab(String tag) {
         getSupportActionBar().selectTab(mTabMap.get(tag));
     }
 
     /**
      * Добавляет новую вкладку в таб-навигацию
      * @param title
      * @param tag
      */
     public void addFeedTab(String title, String tag) {
         ActionBar actionBar = getSupportActionBar();
         assert actionBar != null;
         ActionBar.Tab tab = actionBar.newTab();
         tab.setText(title);
         tab.setTabListener(this);
         tab.setTag(tag);
         mTabMap.put(tag, tab);
         actionBar.addTab(tab);
     }
 
     /**
      * Делает активным фрагмент ленты с определенным тегом
      * @param tag тег фрагмента
      * @param feedUri uri ленты
      */
     public void showFeedFragment(String tag, Uri feedUri) {
         // Транзакция может уже быть открыта, если метод вызывается в обработчике таб-навигации,
         if (mFragmentTransaction != null){
             replaceFragmentInTransaction(mFragmentTransaction, tag, feedUri);
         } else {
             FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
             replaceFragmentInTransaction(ft, tag, feedUri);
             ft.commit();
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putParcelable(CONTROLLER, mController);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (D) Log.d(LOG_TAG, "onActivityResult");
         if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
             if (D) Log.d(LOG_TAG, "loginSuccessful");
             mController.loginSuccessful();
         } else {
             if (D) Log.d(LOG_TAG, "smth strange");
             super.onActivityResult(requestCode, resultCode, data);
         }
     }
 
     /**
      * Настраивает таб-навигацию
      */
     private void initTabs() {
         if (D) Log.d(LOG_TAG, "initTabs");
         ActionBar actionBar = getSupportActionBar();
         assert actionBar != null;
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         mController.initTabs();
         if (D) Log.d(LOG_TAG, "initTabs done");
     }
 
     /**
      * Заменяет фрагмент ленты в UI рассчитывая на нахождение в контексте транзакции фрагмента.
      */
     private void replaceFragmentInTransaction(FragmentTransaction ft, String tag, Uri feedUri) {
         // ищем фрагмент в локальном кэше
         Fragment fragment = mFragmentMap.get(tag);
         // не нашли, конструируем новый фрагмент с feedUri
         if (fragment == null) {
             fragment = createFeedFragment(feedUri);
             // добавляем в кэш
             mFragmentMap.put(tag, fragment);
         }
 
         if (mCurrentFragment != null){
             if (mCurrentFragment.equals(fragment))
                 return;
             ft.remove(mCurrentFragment);
         }
         ft.add(R.id.feed_fragment_container, fragment, tag);
         mCurrentFragment = fragment;
     }
     /**
      * Конструирует новый фрагмент с лентой
      * @param feedUri uri ленты
      * @return готовый к использованию фрагмент ленты
      */
     private Fragment createFeedFragment(Uri feedUri) {
         Fragment fragment = new PlaFeedFragment();
         Bundle args = new Bundle();
         args.putParcelable(Constants.Params.FEED_URI, feedUri);
         fragment.setArguments(args);
         return fragment;
     }
 
     // TODO: обработка ссылок
 
 //    @Override
 //    public void onLoginResult(int result) {
 //        if (result == RESULT_OK)
 //            processCurrentTab();
 //    }
 }
