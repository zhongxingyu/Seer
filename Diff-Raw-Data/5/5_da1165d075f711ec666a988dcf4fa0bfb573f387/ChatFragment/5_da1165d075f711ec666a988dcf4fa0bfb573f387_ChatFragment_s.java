 package ru.tulupov.nsuconnect.fragment;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.Date;
 
 import ru.tulupov.nsuconnect.R;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.helper.BitmapHelper;
 import ru.tulupov.nsuconnect.helper.IntentActionHelper;
 import ru.tulupov.nsuconnect.helper.SettingsHelper;
 import ru.tulupov.nsuconnect.images.ImageCacheManager;
 import ru.tulupov.nsuconnect.model.Chat;
 import ru.tulupov.nsuconnect.service.DataService;
 
 
 public class ChatFragment extends BaseFragment {
     private static final int REQUEST_CODE_TAKE_PHOTO = 0;
     private static final int REQUEST_CODE_IMPORT_PHOTO = 1;
     private static final String ARGS_CHAT_ID = "chat_id";
     private static final String TAG = ChatFragment.class.getSimpleName();
 
     public static ChatFragment newInstance(final Context context, int chatId) {
         final Bundle args = new Bundle();
         args.putInt(ARGS_CHAT_ID, chatId);
         return (ChatFragment) Fragment.instantiate(context, ChatFragment.class.getName(), args);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fgt_chat, container, false);
     }
 
     private boolean isTyping;
     private int chatId;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.fgt_chat, menu);
         super.onCreateOptionsMenu(menu, inflater);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 
             case android.R.id.home:
                 closeFragment();
                 break;
 
             case R.id.menu_upload:
 
 
                 startActivityForResult(IntentActionHelper.getCameraIntent(getActivity()), REQUEST_CODE_TAKE_PHOTO);
                 break;
 
            case R.id.menu_close:
                getActivity().stopService(new Intent(getActivity(), DataService.class));
                closeFragment();
                break;
             default:
                 return super.onOptionsItemSelected(item);
         }
         return true;
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         chatId = getArguments().getInt(ARGS_CHAT_ID);
         if (savedInstanceState == null) {
 
             getChildFragmentManager().beginTransaction()
                     .add(R.id.message_container, ConversationFragment.newInstance(getActivity(), chatId))
                     .commit();
         }
 
 
 //        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
 //            @Override
 //            public void onClick(View view) {
 //                Message message = new Message();
 //                message.setMessage("fff " + System.currentTimeMillis());
 //                try {
 //                    HelperFactory.getHelper().getMessageDao().create(message);
 //
 //                    getActivity().sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_MESSAGE_LIST));
 //                } catch (SQLException e) {
 //                    Log.e("xxx", e.getLocalizedMessage());
 //                }
 //            }
 //        });
 
         final EditText edit = (EditText) view.findViewById(R.id.edit);
         view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (edit.getText().length() == 0) {
                     return;
                 }
                 getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_SEND_MESSAGE).putExtra(DataService.EXTRA_ID, chatId).putExtra(DataService.EXTRA_MESSAGE, edit.getText().toString()));
                 edit.setText(null);
             }
         });
 
         edit.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 if (edit.getText().length() == 0) {
                     isTyping = false;
                     handler.removeCallbacks(stopTypingRunnable);
                     getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_STOP_TYPING).putExtra(DataService.EXTRA_ID, chatId));
                 } else {
                     if (!isTyping) {
                         getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_START_TYPING).putExtra(DataService.EXTRA_ID, chatId));
                         handler.removeCallbacks(stopTypingRunnable);
                         handler.postDelayed(stopTypingRunnable, TYPING_TIMEOUT);
                         isTyping = true;
                     }
                 }
 
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
 
             }
         });
 
 
         final View rootView = getView();
 
         rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                 SlidingMenu slidingMenu = getSlidingMenu();
                 if (slidingMenu != null) {
                     // detect if keyboard opened
                     if (heightDiff > 250) {
                         slidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
                             @Override
                             public void onOpen() {
                                 if (getActivity() != null) {
                                     InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                             Context.INPUT_METHOD_SERVICE);
                                     imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                                 }
                             }
                         });
 
                     }
                 }
 
             }
         });
 
 
         Chat chat;
         try {
             chat = HelperFactory.getHelper().getChatDao().getChat(chatId);
             if (!chat.isActive()) {
                 findViewById(R.id.send_container).setVisibility(View.GONE);
                 findViewById(R.id.shadow).setVisibility(View.GONE);
             }
             getActivity().setTitle(chat.getName());
         } catch (SQLException e) {
             Log.e(TAG, "cannot create chat entity", e);
         }
 
 
     }
 
     private Runnable stopTypingRunnable = new Runnable() {
         @Override
         public void run() {
             isTyping = false;
             if (getActivity() != null) {
                 getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_STOP_TYPING));
             }
         }
     };
 
     private static final long TYPING_TIMEOUT = 1500;
     private Handler handler = new Handler();
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
 
         if ((requestCode == REQUEST_CODE_TAKE_PHOTO || requestCode == REQUEST_CODE_IMPORT_PHOTO) && resultCode == Activity.RESULT_OK) {
             final String picturePath = BitmapHelper.getPicturePath(getActivity(), data);
 
 
             Bitmap bitmap = BitmapHelper.getNormalPhoto(picturePath);
 
             File file = BitmapHelper.saveBitmapToTmpFile(bitmap);
 
 
             if (file != null) {
                 ImageCacheManager.getInstance().putBitmap(file.getPath(), bitmap);
                 getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_SEND_MESSAGE).putExtra(DataService.EXTRA_FILE, file.getPath()));
             }
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
 
 }
