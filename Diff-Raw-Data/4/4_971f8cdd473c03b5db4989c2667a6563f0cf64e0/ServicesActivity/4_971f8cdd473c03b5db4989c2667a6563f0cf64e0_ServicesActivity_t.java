 package com.chute.android.photopickerplus.ui.activity;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Window;
 import android.widget.Toast;
 
 import com.araneaapps.android.libs.logger.ALog;
 import com.chute.android.photopickerplus.R;
 import com.chute.android.photopickerplus.dao.MediaDAO;
 import com.chute.android.photopickerplus.ui.adapter.AssetSelectListener;
 import com.chute.android.photopickerplus.ui.fragment.AccountFilesListener;
 import com.chute.android.photopickerplus.ui.fragment.CursorFilesListener;
 import com.chute.android.photopickerplus.ui.fragment.EmptyFragment;
 import com.chute.android.photopickerplus.ui.fragment.FragmentRoot;
 import com.chute.android.photopickerplus.ui.fragment.FragmentServices.ServiceClickedListener;
 import com.chute.android.photopickerplus.ui.fragment.FragmentSingle;
 import com.chute.android.photopickerplus.util.AppUtil;
 import com.chute.android.photopickerplus.util.Constants;
 import com.chute.android.photopickerplus.util.NotificationUtil;
 import com.chute.android.photopickerplus.util.PhotoFilterType;
 import com.chute.android.photopickerplus.util.PhotoPickerPreferenceUtil;
 import com.chute.android.photopickerplus.util.intent.IntentUtil;
 import com.chute.android.photopickerplus.util.intent.PhotosIntentWrapper;
 import com.chute.sdk.v2.api.accounts.GCAccounts;
 import com.chute.sdk.v2.api.authentication.AuthenticationFactory;
 import com.chute.sdk.v2.model.AccountModel;
 import com.chute.sdk.v2.model.AssetModel;
 import com.chute.sdk.v2.model.enums.AccountType;
 import com.chute.sdk.v2.model.response.ListResponseModel;
 import com.chute.sdk.v2.utils.PreferenceUtil;
 import com.dg.libs.rest.callbacks.HttpCallback;
 import com.dg.libs.rest.domain.ResponseStatus;
 
 public class ServicesActivity extends FragmentActivity implements AccountFilesListener,
     CursorFilesListener,
     ServiceClickedListener {
 
   private static final String TAG = ServicesActivity.class.getSimpleName();
 
   private FragmentTransaction fragmentTransaction;
   private static FragmentManager fragmentManager;
   private AccountType accountType;
   private boolean dualPanes;
   private ArrayList<Integer> selectedItemPositions;
   private String folderId;
   private AccountModel account;
   private AssetSelectListener assetSelectListener;
   private FragmentSingle fragmentSingle;
   private FragmentRoot fragmentRoot;
   private int photoFilterType;
 
   public AssetSelectListener getAssetSelectListener() {
     return assetSelectListener;
   }
 
   public void setAssetSelectListener(AssetSelectListener assetSelectListener)
   {
     this.assetSelectListener = assetSelectListener;
   }
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     fragmentManager = getSupportFragmentManager();
     requestWindowFeature(Window.FEATURE_NO_TITLE);
     setContentView(R.layout.main_layout);
 
     retrieveValuesFromBundle(savedInstanceState);
 
     dualPanes = getResources().getBoolean(R.bool.has_two_panes);
     if (dualPanes
         && savedInstanceState == null
         && getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
       replaceContentWithEmptyFragment();
     }
 
   }
 
   @Override
   public void takePhoto() {
     if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
       NotificationUtil.makeToast(getApplicationContext(), R.string.toast_feature_camera);
       return;
     }
     final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     if (AppUtil.hasImageCaptureBug() == false) {
       intent.putExtra(MediaStore.EXTRA_OUTPUT,
           Uri.fromFile(AppUtil.getTempFile(ServicesActivity.this)));
     } else {
       intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
           android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
     }
     startActivityForResult(intent, Constants.CAMERA_PIC_REQUEST);
 
   }
 
   @Override
   public void lastPhoto() {
     Uri uri = MediaDAO.getLastPhotoFromCameraPhotos(getApplicationContext());
     if (uri.toString().equals("")) {
       NotificationUtil.makeToast(getApplicationContext(),
           getResources().getString(R.string.no_camera_photos));
     } else {
       final AssetModel model = new AssetModel();
       model.setThumbnail(uri.toString());
       model.setUrl(uri.toString());
 
       IntentUtil.deliverDataToInitialActivity(ServicesActivity.this, model);
     }
 
   }
 
   @Override
   public void photoStream() {
     photoFilterType = PhotoFilterType.ALL_PHOTOS.ordinal();
     selectedItemPositions = null;
     if (!dualPanes) {
       final PhotosIntentWrapper wrapper = new PhotosIntentWrapper(ServicesActivity.this);
       wrapper.setFilterType(PhotoFilterType.ALL_PHOTOS);
       wrapper.startActivityForResult(ServicesActivity.this,
           PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
     } else {
       replaceContentWithRootFragment(null, PhotoFilterType.ALL_PHOTOS);
     }
 
   }
 
   @Override
   public void cameraRoll() {
     photoFilterType = PhotoFilterType.CAMERA_ROLL.ordinal();
     selectedItemPositions = null;
     if (!dualPanes) {
       final PhotosIntentWrapper wrapper = new PhotosIntentWrapper(ServicesActivity.this);
       wrapper.setFilterType(PhotoFilterType.CAMERA_ROLL);
       wrapper.startActivityForResult(ServicesActivity.this,
           PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
     } else {
       replaceContentWithRootFragment(null, PhotoFilterType.CAMERA_ROLL);
     }
 
   }
 
   public void accountClicked(AccountModel account) {
     photoFilterType = PhotoFilterType.SOCIAL_PHOTOS.ordinal();
     selectedItemPositions = null;
    this.account = account;
     if (!dualPanes) {
       final PhotosIntentWrapper wrapper = new PhotosIntentWrapper(ServicesActivity.this);
       wrapper.setFilterType(PhotoFilterType.SOCIAL_PHOTOS);
       wrapper.setAccount(account);
       wrapper.startActivityForResult(ServicesActivity.this,
           PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
     } else {
       replaceContentWithRootFragment(account, PhotoFilterType.SOCIAL_PHOTOS);
     }
 
   }
 
   public void replaceContentWithSingleFragment(AccountModel account, String folderId,
       ArrayList<Integer> selectedItemPositions) {
     fragmentTransaction = fragmentManager.beginTransaction();
     fragmentTransaction.replace(R.id.fragments,
         FragmentSingle.newInstance(account, folderId, selectedItemPositions),
         Constants.TAG_FRAGMENT_FILES);
     fragmentTransaction.addToBackStack(null);
     fragmentTransaction.commit();
 
   }
 
   public void replaceContentWithRootFragment(AccountModel account,
       PhotoFilterType filterType) {
     fragmentTransaction = fragmentManager.beginTransaction();
     fragmentTransaction.replace(R.id.fragments,
         FragmentRoot.newInstance(account, filterType, selectedItemPositions),
         Constants.TAG_FRAGMENT_FOLDER);
     fragmentTransaction.commit();
   }
 
   public void replaceContentWithEmptyFragment() {
     fragmentTransaction = fragmentManager.beginTransaction();
     fragmentTransaction.replace(R.id.fragments, EmptyFragment.newInstance(),
         Constants.TAG_FRAGMENT_EMPTY);
     fragmentTransaction.commit();
   }
 
   @Override
   public void accountLogin(AccountType type) {
     accountType = type;
     PhotoPickerPreferenceUtil.get().setAccountType(accountType);
     if (PreferenceUtil.get().hasAccount(type.getLoginMethod())) {
       AccountModel account = PreferenceUtil.get()
           .getAccount(type.getLoginMethod());
       accountClicked(account);
     } else {
       AuthenticationFactory.getInstance().startAuthenticationActivity(
           ServicesActivity.this, accountType);
     }
 
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     if (resultCode == Activity.RESULT_OK) {
       GCAccounts.allUserAccounts(getApplicationContext(), new AccountsCallback())
           .executeAsync();
       if (requestCode == PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY) {
         finish();
       } else if (requestCode == Constants.CAMERA_PIC_REQUEST) {
         String path = "";
         File tempFile = AppUtil.getTempFile(getApplicationContext());
         if (AppUtil.hasImageCaptureBug() == false && tempFile.length() > 0) {
           try {
             android.provider.MediaStore.Images.Media.insertImage(getContentResolver(),
                 tempFile.getAbsolutePath(), null, null);
             tempFile.delete();
             path = MediaDAO.getLastPhotoFromCameraPhotos(getApplicationContext())
                 .toString();
           } catch (FileNotFoundException e) {
             Log.d(TAG, "", e);
           }
         } else {
           Log.e(TAG, "Bug " + data.getData().getPath());
           path = Uri.fromFile(
               new File(AppUtil.getPath(getApplicationContext(), data.getData())))
               .toString();
         }
         Log.d(TAG, path);
         final AssetModel model = new AssetModel();
         model.setThumbnail(path);
         model.setUrl(path);
 
         IntentUtil.deliverDataToInitialActivity(this, model);
       }
     }
   }
 
   @Override
   protected void onNewIntent(Intent intent) {
     super.onNewIntent(intent);
     setResult(Activity.RESULT_OK, new Intent().putExtras(intent.getExtras()));
     ServicesActivity.this.finish();
   }
 
   private final class AccountsCallback implements
       HttpCallback<ListResponseModel<AccountModel>> {
 
     @Override
     public void onSuccess(ListResponseModel<AccountModel> responseData) {
       if (accountType == null) {
         accountType = PhotoPickerPreferenceUtil.get().getAccountType();
       }
       if (responseData.getData().size() == 0) {
         Toast.makeText(getApplicationContext(),
             getResources().getString(R.string.no_albums_found),
             Toast.LENGTH_SHORT).show();
         return;
       }
       for (AccountModel accountModel : responseData.getData()) {
         if (accountModel.getType().equals(accountType.getLoginMethod())) {
           PreferenceUtil.get().saveAccount(accountModel);
           accountClicked(accountModel);
         }
       }
 
     }
 
     @Override
     public void onHttpError(ResponseStatus responseStatus) {
       ALog.d("Http Error: " + responseStatus.getStatusCode() + " "
           + responseStatus.getStatusMessage());
     }
 
   }
 
   @Override
   public void onDeliverAccountFiles(ArrayList<AssetModel> assetList) {
     IntentUtil.deliverDataToInitialActivity(ServicesActivity.this, assetList);
 
   }
 
   @Override
   public void onDeliverCursorAssets(ArrayList<String> assetPathList) {
     IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
         AppUtil.getPhotoCollection(assetPathList));
 
   }
 
   @Override
   public void onAccountFilesSelect(AssetModel assetModel) {
     IntentUtil.deliverDataToInitialActivity(ServicesActivity.this, assetModel);
   }
 
   @Override
   public void onCursorAssetsSelect(AssetModel assetModel) {
     IntentUtil.deliverDataToInitialActivity(ServicesActivity.this, assetModel);
   }
 
   @Override
   public void onAccountFolderSelect(AccountModel account,
       String folderId) {
     selectedItemPositions = null;
     photoFilterType = PhotoFilterType.SOCIAL_PHOTOS.ordinal();
     this.folderId = folderId;
     this.account = account;
     replaceContentWithSingleFragment(account, folderId, selectedItemPositions);
   }
 
   @Override
   protected void onSaveInstanceState(Bundle outState) {
     super.onSaveInstanceState(outState);
     outState.putString(Constants.KEY_FOLDER_ID, folderId);
     outState.putParcelable(Constants.KEY_ACCOUNT, account);
     outState.putInt(Constants.KEY_PHOTO_FILTER_TYPE, photoFilterType);
     if (assetSelectListener != null
         && assetSelectListener.getSelectedItemPositions() !=
         null) {
       outState.putIntegerArrayList(Constants.KEY_SELECTED_ITEMS,
           assetSelectListener
               .getSelectedItemPositions());
     }
 
   }
 
   @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState) {
     super.onRestoreInstanceState(savedInstanceState);
     fragmentSingle = (FragmentSingle) getSupportFragmentManager().findFragmentByTag(
         Constants.TAG_FRAGMENT_FILES);
     fragmentRoot = (FragmentRoot) getSupportFragmentManager().findFragmentByTag(
         Constants.TAG_FRAGMENT_FOLDER);
     if (fragmentSingle != null
         && photoFilterType == PhotoFilterType.SOCIAL_PHOTOS.ordinal()
         && fragmentSingle.isInLayout()) {
       fragmentSingle.setRetainInstance(true);
       fragmentSingle.updateFragment(account, folderId, selectedItemPositions);
     }
     if (fragmentRoot != null
         && photoFilterType != PhotoFilterType.SOCIAL_PHOTOS.ordinal()) {
       fragmentRoot.setRetainInstance(true);
       fragmentRoot.updateFragment(null, PhotoFilterType.values()[photoFilterType],
           selectedItemPositions);
     }
   }
 
   private void retrieveValuesFromBundle(Bundle savedInstanceState) {
     selectedItemPositions = savedInstanceState != null ? savedInstanceState
         .getIntegerArrayList(Constants.KEY_SELECTED_ITEMS)
         : null;
 
     folderId = savedInstanceState != null ? savedInstanceState
         .getString(Constants.KEY_FOLDER_ID)
         : null;
 
     account = (AccountModel) (savedInstanceState != null ? savedInstanceState
         .getParcelable(Constants.KEY_ACCOUNT)
         : null);
    
     photoFilterType = savedInstanceState != null ? savedInstanceState
         .getInt(Constants.KEY_PHOTO_FILTER_TYPE)
         : 0;
 
   }
 
   @Override
   public void onDestroy() {
     Fragment fragmentFolder =
         fragmentManager.findFragmentByTag(Constants.TAG_FRAGMENT_FOLDER);
     Fragment fragmentFiles =
         fragmentManager.findFragmentByTag(Constants.TAG_FRAGMENT_FILES);
     if (fragmentFolder != null && fragmentFolder.isResumed()) {
       fragmentManager.beginTransaction().remove(fragmentFolder).commit();
     }
     if (fragmentFiles != null && fragmentFiles.isResumed()) {
       fragmentManager.beginTransaction().remove(fragmentFiles).commit();
     }
     super.onDestroy();
   }
 
   @Override
   public void onSessionExpired(AccountType accountType) {
     PhotoPickerPreferenceUtil.get().setAccountType(accountType);
     AuthenticationFactory.getInstance().startAuthenticationActivity(
         ServicesActivity.this, accountType);
   }
 
 }
