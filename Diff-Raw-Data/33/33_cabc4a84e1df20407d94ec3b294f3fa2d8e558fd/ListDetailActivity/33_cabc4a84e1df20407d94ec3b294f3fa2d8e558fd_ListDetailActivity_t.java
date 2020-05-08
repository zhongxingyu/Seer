 package whyq.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import whyq.WhyqApplication;
 import whyq.adapter.BasicImageListAdapter;
 import whyq.adapter.BasicUserAdapter;
 import whyq.adapter.ExpanMenuAdapter;
 import whyq.adapter.ExpandableListAdapter.ViewHolderMitemInfo;
 import whyq.adapter.WhyqAdapter.ViewHolder;
 import whyq.adapter.WhyqMenuAdapter;
 import whyq.controller.SreenGestureControllerwithParams;
 import whyq.interfaces.IServiceListener;
 import whyq.map.MapsActivity;
 import whyq.model.Bill;
 import whyq.model.GroupMenu;
 import whyq.model.Menu;
 import whyq.model.Photo;
 import whyq.model.ProductTypeInfo;
 import whyq.model.Promotion;
 import whyq.model.ResponseData;
 import whyq.model.Store;
 import whyq.model.User;
 import whyq.model.UserCheckBill;
 import whyq.service.Service;
 import whyq.service.ServiceAction;
 import whyq.service.ServiceResponse;
 import whyq.utils.UrlImageViewHelper;
 import whyq.utils.Util;
 import whyq.utils.WhyqUtils;
 import whyq.view.CustomViewPager;
 import whyq.view.ScreenGestureController;
 import whyq.view.ScrollviewCustom;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.DragEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewTreeObserver;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Toast;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.whyq.R;
 
 public class ListDetailActivity extends FragmentActivity implements
 		IServiceListener, OnScrollListener {
 
 	private Service service;
 	// ProgressDialog dialog;
 	ProgressBar progressBar;
 	private String id;
 	public static Store store;
 	private TextView tvAddresss;
 	private ImageView imgThumbnail;
 	private TextView tvNumberFavourtie;
 	private TextView tvOpeningTime;
 	private ImageButton imgFavourtieIcon;
 	private TextView tvTelephone;
 	private TextView tvStoreDes;
 	private TextView tvCommendRever;
 	private ImageView imgUserAvatar;
 	private TextView tvFriendNumber;
 	private EditText edSearch;
 	private ListView lvResult;
 	private ImageView imgFrienAvatar;
 	private Button btnDone;
 	private RadioButton imgBtnAbout;
 	private RadioButton imgBtnMenu;
 	private RadioButton imgBtnPromotion;
 	private String cateId;
 	private TextView tvHeaderTitle;
 	private TextView tvFromUsr;
 	private ImageView imgHeader;
 	private int storeType;
 	private RadioGroup radioGroup;
 	private LinearLayout lnAboutContent;
 	private LinearLayout lnMenuContent;
 	private LinearLayout lnPromotionContent;
 	private ExpandableListView lvMenu;
 	private TextView tvNumberDiscount;
 	private TextView tvDate;
 	private TextView tvDes;
 	private ImageView imgView;
 	private WhyqMenuAdapter menuAdapter;
 	private Button btnTotalValue;
 	private ArrayList<Menu> menuList;
 	private CustomViewPager vpPhotoList;
 	private Context context;
 	private BasicUserAdapter adapter;
 	private RelativeLayout rlPhotoList;
 	private ScrollviewCustom svContent;
 	private TextView tvCuisine;
 	private EditText etComment;
 	private TextView tvCuisineTitle;
 	private TextView tvOpeningTimeTitle;
 	private TextView tvTelephoneTitle;
 	private String storeName;
 	public static Bundle bundle;
 	public static HashMap<String, Bill> billList;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.store_detail);
 		context = ListDetailActivity.this;
 		id = getIntent().getStringExtra("id");
 		Log.d("ListDetailActivity", "id " + id);
 		service = new Service(this);
 		bundle = new Bundle();
 		svContent = (ScrollviewCustom) findViewById(R.id.svContent);
 		tvAddresss = (TextView) findViewById(R.id.tvAddress);
 		tvCuisine = (TextView) findViewById(R.id.tvCuisine);
 		tvCuisineTitle = (TextView) findViewById(R.id.tvCuisineTitle);
 		tvOpeningTimeTitle = (TextView) findViewById(R.id.tvOpeningTimeTitle);
 		tvTelephoneTitle = (TextView) findViewById(R.id.tvTelephoneTitle);
 		imgThumbnail = (ImageView) findViewById(R.id.imgThumbnail);
 		tvNumberFavourtie = (TextView) findViewById(R.id.tvNumberOfFavourite);
 		imgFavourtieIcon = (ImageButton) findViewById(R.id.imgFavouriteIcon);
 		tvOpeningTime = (TextView) findViewById(R.id.tvOpeningTime);
 		tvTelephone = (TextView) findViewById(R.id.tvTelephone);
 		tvStoreDes = (TextView) findViewById(R.id.tvStoreDes);
 		tvCommendRever = (TextView) findViewById(R.id.tvCommendReview);
 		imgFrienAvatar = (ImageView) findViewById(R.id.imgAvatar);
 		tvFriendNumber = (TextView) findViewById(R.id.tvFriendCount);
 		tvFromUsr = (TextView) findViewById(R.id.tvFromUser);
 		edSearch = (EditText) findViewById(R.id.etTextSearch);
 		lvResult = (ListView) findViewById(R.id.lvFindResult);
 		progressBar = (ProgressBar) findViewById(R.id.prgBar);
 		btnDone = (Button) findViewById(R.id.btn_done);
 		imgBtnAbout = (RadioButton) findViewById(R.id.rdoAbout);
 		imgBtnMenu = (RadioButton) findViewById(R.id.rdoMenu);
 		imgBtnPromotion = (RadioButton) findViewById(R.id.rdoPromotion);
 		tvHeaderTitle = (TextView) findViewById(R.id.tvHeaderTitle);
 		imgHeader = (ImageView) findViewById(R.id.imgHeader);
 		storeType = getIntent().getIntExtra("store_type", 0);
 		lnAboutContent = (LinearLayout) findViewById(R.id.lnAboutContent);
 		lnMenuContent = (LinearLayout) findViewById(R.id.lnMenuContent);
 		lnPromotionContent = (LinearLayout) findViewById(R.id.lnStoreDetailPromotion);
 		imgView = (ImageView) findViewById(R.id.imgView);
 		tvNumberDiscount = (TextView) findViewById(R.id.tvNumberDiscount);
 		tvDate = (TextView) findViewById(R.id.tvDate);
 		tvDes = (TextView) findViewById(R.id.tvDescription);
 		btnTotalValue = (Button) findViewById(R.id.btnTotalValue);
 		btnTotalValue.setText("0");
 		lvMenu = (ExpandableListView) findViewById(R.id.lvMenu);
 		billList = new HashMap<String, Bill>();
 		vpPhotoList = (CustomViewPager) findViewById(R.id.vpStorephoto);
 		rlPhotoList = (RelativeLayout) findViewById(R.id.rlPhotoList);
 		etComment = (EditText) findViewById(R.id.etComment);
 		// showHeaderImage();
 		storeName = getIntent().getStringExtra("store_name");
 		showPhotoList();
 		initTabbar();
 //		Util.hideSoftKeyboard(this);
 		vpPhotoList.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				vpPhotoList.setFocusable(true);
 			}
 		});
 		getDetailData();
 		// hide photos list when scroll
 		// lvResult.setOnScrollListener(this);
 
 		edSearch.addTextChangedListener(mTextEditorWatcher);
 		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
 		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				// TODO Auto-generated method stub
 				int selectedBtn = radioGroup.getCheckedRadioButtonId();
 				Log.d("setOnCheckedChangeListener", "selectedBtn "
 						+ selectedBtn);
 				if (selectedBtn == R.id.rdoAbout) {
 					exeAboutFocus();
 				} else if (selectedBtn == R.id.rdoMenu) {
 					exeMenuFocus();
 				} else if (selectedBtn == R.id.rdoPromotion) {
 					exePromotionFocus();
 				}
 			}
 		});
 		final GestureDetector gestureDetector = new GestureDetector(
 				new ScreenGestureController(ListDetailActivity.this, 1));
 		svContent.setOnTouchListener(new View.OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				gestureDetector.onTouchEvent(event);
 
 				return ScreenGestureController.isUp;
 			}
 		});
 
 		appyFont();
 	}
 
 
 
 	private void appyFont() {
 		// TODO Auto-generated method stub
 		Util.applyTypeface(tvAddresss, Util.sTypefaceBold);
 		Util.applyTypeface(tvCuisine, Util.sTypefaceItalic);
 		Util.applyTypeface(tvNumberFavourtie, Util.sTypefaceRegular);
 		Util.applyTypeface(tvOpeningTime, Util.sTypefaceRegular);
 		Util.applyTypeface(tvTelephone, Util.sTypefaceRegular);
 		Util.applyTypeface(tvDes, Util.sTypefaceRegular);
 		Util.applyTypeface(tvFromUsr, Util.sTypefaceRegular);
 		Util.applyTypeface(tvHeaderTitle, Util.sTypefaceRegular);
 		Util.applyTypeface(tvNumberDiscount, Util.sTypefaceRegular);
 		Util.applyTypeface(tvStoreDes, Util.sTypefaceRegular);
 		Util.applyTypeface(tvOpeningTimeTitle, Util.sTypefaceBold);
 		Util.applyTypeface(tvTelephoneTitle, Util.sTypefaceBold);
 	}
 
 	private String currentStoreId;
	private Promotion promotion;
 	public static String commentContent;
 
 	public void onDoneClicked(View v) {
 		if (store != null) {
 			Intent i = new Intent(context, MapsActivity.class);
 			i.putExtra("store", store);
 			startActivity(i);
 		}
 	}
 
 	public void hidePhotoList() {
 		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rlPhotoList
 				.getLayoutParams();
 		params.height = 0;
 		rlPhotoList.setLayoutParams(params);
 	}
 
 	public void showPhotoList() {
 		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rlPhotoList
 				.getLayoutParams();
 		params.height = (int) (WhyqApplication.Instance().getDisplayMetrics().widthPixels * 3 / 5);// WhyqApplication.Instance().getDensity()
 																									// *
 		rlPhotoList.setLayoutParams(params);
 	}
 
 	protected void exeAboutFocus() {
 		// TODO Auto-generated method stub
 		Log.d("exeAboutFocus", "exeAboutFocus");
 		setViewContent(1);
 	}
 
 	protected void exeMenuFocus() {
 		// TODO Auto-generated method stub
 		Log.d("exeMenuFocus", "exeMenuFocus");
 		setViewContent(2);
 	}
 
 	protected void exePromotionFocus() {
 		// TODO Auto-generated method stub
 		Log.d("exePromotionFocus", "exePromotionFocus");
 		setViewContent(3);
 		checkPromotionData();
 	}
 
 	private void checkPromotionData() {
 		// TODO Auto-generated method stub
 		boolean isHave = false;
 		if (store != null) {
 			if (store.getPromotionList() != null) {
 				if (store.getPromotionList().size() > 0) {
 					isHave = true;
 				}
 			}
 			if (!isHave) {
 				lnPromotionContent.setVisibility(View.INVISIBLE);
 				Toast.makeText(context, "No promotion in store",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	private void setViewContent(int i) {
 		// TODO Auto-generated method stub
 		if (i == 1) {
 			lnAboutContent.setVisibility(View.VISIBLE);
 			lnMenuContent.setVisibility(View.GONE);
 			lnPromotionContent.setVisibility(View.GONE);
 			lvResult.setVisibility(View.VISIBLE);
 			showPhotoList();
 		} else if (i == 2) {
 			lnAboutContent.setVisibility(View.GONE);
 			lnMenuContent.setVisibility(View.VISIBLE);
 			lnPromotionContent.setVisibility(View.GONE);
 			lvResult.setVisibility(View.GONE);
 			hidePhotoList();
 		} else if (i == 3) {
 			lnAboutContent.setVisibility(View.GONE);
 			lnMenuContent.setVisibility(View.GONE);
 			lnPromotionContent.setVisibility(View.VISIBLE);
 			lvResult.setVisibility(View.GONE);
 			showPhotoList();
 		}
 	}
 
 	private void showHeaderImage() {
 		// TODO Auto-generated method stub
 		if (storeType == 1) {
 			imgHeader.setImageResource(R.drawable.icon_tab_cutlery_active);
 		} else if (storeType == 2) {
 			imgHeader.setImageResource(R.drawable.icon_tab_wine_active);
 		} else if (storeType == 3) {
 			imgHeader.setImageResource(R.drawable.icon_tab_coffee_active);
 		} else if (storeType == 4) {
 			imgHeader.setImageResource(R.drawable.icon_tab_hotel_active);
 		}
 	}
 
 	private void initTabbar() {
 		// TODO Auto-generated method stub
 		// imgBtnAbout.setImageResource(R.color.transparent);
 		// imgBtnMenu.setImageResource(R.color.transparent);
 		// imgBtnPromotion.setImageResource(R.color.transparent);
 		if (storeName != null)
 			tvHeaderTitle.setText("" + storeName);
 		else
 			tvHeaderTitle.setText("");
 	}
 
 	private void bindData() {
 		// TODO Auto-generated method stub
 		try {
 			bindUserChecked();
 			if (store.getLogo() != null)
 				UrlImageViewHelper
 						.setUrlDrawable(imgThumbnail, store.getLogo());
 			tvAddresss.setText("" + store.getAddress());
 			tvCuisine.setText("" + store.getStyle());
 			tvNumberFavourtie.setText("" + store.getCountFavaouriteMember());
 
 			tvOpeningTime.setText(store.getStartTime() + " - "
 					+ store.getEndTime());
 			tvTelephone.setText("" + store.getPhoneStore());
 			tvStoreDes.setText("" + store.getIntroStore());
 			tvHeaderTitle.setText("" + store.getNameStore());
 			if (store.getDistance() != null) {
 				if (!store.getDistance().equals(""))
 					btnDone.setText(Math.round(Float.parseFloat(store
 							.getDistance())) + " Km");
 				else {
 					btnDone.setText(store.getDistance());
 				}
 
 			} else {
 				btnDone.setText("nodata Km");
 			}
 			btnDone.setVisibility(View.VISIBLE);
 			if (store.getIsFavourite()) {
 				imgFavourtieIcon.setImageResource(R.drawable.icon_fav_enable);
 			} else {
 				imgFavourtieIcon.setImageResource(R.drawable.icon_fav_disable);
 			}
 			// UrlImageViewHelper.setUrlDrawable(imgView, store.getPhotos());
 			if (!store.getCountFavaouriteMember().equals("0")) {
 				tvCommendRever.setText(store.getCountComment() + " comments");
 				tvCommendRever.setTextColor(getResources().getColor(
 						R.color.profifle_blue));
 				tvFromUsr.setVisibility(View.VISIBLE);
 			} else {
 				tvFromUsr.setVisibility(View.VISIBLE);
 			}
 
 			// UrlImageViewHelper.setUrlDrawable(imgFrienAvatar,
 			// store.getUserList()
 			// .get(0).getUrlAvatar());
 
 		} catch (Exception e) {
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 
 	}
 
 	private void bindUserChecked() {
 		// TODO Auto-generated method stub
 		try {
 			UserCheckBill userCheckBill = store.getUserCheckBill();
 			if (userCheckBill != null) {
 				if (userCheckBill.getTotalMember() != null
 						&& !userCheckBill.getTotalMember().equals("")) {
 					if (userCheckBill.getAvatar() != null
 							&& !userCheckBill.getAvatar().equals("")) {
 						if (Integer.parseInt(userCheckBill.getTotalMember()) > 0) {
 							tvFriendNumber.setText(userCheckBill.getFirstName()
 									+ " " + userCheckBill.getLastName() + " & "
 									+ userCheckBill.getTotalMember()
 									+ " others visited");
 						} else {
 							tvFriendNumber.setText(userCheckBill.getFirstName()
 									+ " " + userCheckBill.getLastName() + " & "
 									+ userCheckBill.getTotalMember()
 									+ " other visited");
 						}
 
 						imgFrienAvatar.setVisibility(View.VISIBLE);
 						UrlImageViewHelper.setUrlDrawable(imgFrienAvatar,
 								userCheckBill.getAvatar());
 					} else {
 						imgFrienAvatar.setVisibility(View.GONE);
 						if (Integer.parseInt(userCheckBill.getTotalMember()) > 0) {
 							tvFriendNumber.setText(userCheckBill
 									.getTotalMember() + " others visited");
 						} else {
 							tvFriendNumber.setText(userCheckBill
 									.getTotalMember() + " other visited");
 						}
 
 					}
 
 				}
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 	}
 
 	private void getDetailData() {
 		// TODO Auto-generated method stub
 		if (Util.checkInternetConnection()) {
 			showDialog();
 			service.getBusinessDetail(id);
 		} else {
 			Util.showNetworkError(context);
 		}
 	}
 
 	private void showDialog() {
 		// dialog.show();
 		progressBar.setVisibility(View.VISIBLE);
 	}
 
 	private void hideDialog() {
 		// dialog.dismiss();
 		progressBar.setVisibility(View.GONE);
 	}
 
 	@Override
 	public void onCompleted(Service service, ServiceResponse result) {
 		// TODO Auto-generated method stub
 		hideDialog();
 		if (result.getAction() == ServiceAction.ActionGetBusinessDetail) {
 			ResponseData data = (ResponseData) result.getData();
 			if (data != null) {
 				if (data.getStatus().equals("200")) {
 					store = (Store) data.getData();
 					if (store != null) {
 						storeType = Integer.valueOf(store.getCateid());
 						showHeaderImage();
 						bindData();
 						bindMenuData();
 						bindPromotionData();
 						bindImageList();
 						bindFriend();
 					}
 				} else if (data.getStatus().equals("401")) {
 					Util.loginAgain(context, data.getMessage());
 				} else {
 					Util.showDialog(context, data.getMessage());
 					finish();
 				}
 			}
 
 		} else if (result.getAction() == ServiceAction.ActionGetBusinessDetail) {
 			Toast.makeText(context, "Can not get store detail for now",
 					Toast.LENGTH_LONG).show();
 			finish();
 		} else if (result.isSuccess()
 				&& result.getAction() == ServiceAction.ActionPostFavorite) {
 			// Toast.makeText(context, "Favourite successfully",
 			// Toast.LENGTH_SHORT).show();
 			ResponseData data = (ResponseData) result.getData();
 
 			if (data.getStatus().equals("200")) {
 				updateFavoriteWitId(currentStoreId, true);
 			} else if (data.getStatus().equals("401")) {
 				Util.loginAgain(getParent(), data.getMessage());
 			} else {
 				// Util.showDialog(getParent(), data.getMessage());
 			}
 			hideDialog();
 		} else if (result.isSuccess()
 				&& result.getAction() == ServiceAction.ActionRemoveFavorite) {
 			// Toast.makeText(context, "Un favourite successfully",
 			// Toast.LENGTH_SHORT).show();
 			ResponseData data = (ResponseData) result.getData();
 			if (data.getStatus().equals("200")) {
 				updateFavoriteWitId(currentStoreId, false);
 			} else if (data.getStatus().equals("401")) {
 				Util.loginAgain(getParent(), data.getMessage());
 			} else {
 				// Util.showDialog(getParent(), data.getMessage());
 			}
 			hideDialog();
 		} else if (!result.isSuccess()
 				&& result.getAction() == ServiceAction.ActionPostFavorite) {
 			hideDialog();
 		} else if (!result.isSuccess()
 				&& result.getAction() == ServiceAction.ActionRemoveFavorite) {
 			hideDialog();
 		} else if (result.getAction() == ServiceAction.ActionGetUserChecked) {
 
 			ResponseData data = (ResponseData) result.getData();
 			if (data.getStatus().equals("200")) {
 				List<User> userList = (List<User>) data.getData();
 				if (userList != null) {
 					if (userList.size() > 0) {
 						loadCheckFriend(userList);
 					}
 				}
 			} else if (data.getStatus().equals("401")) {
 				Util.loginAgain(context, data.getMessage());
 			} else {
 				// Util.showDialog(context, data.getMessage());
 			}
 
 		}
 
 	}
 
 	private void updateFavoriteWitId(String id, boolean b) {
 		// TODO Auto-generated method stub
 		int value;
 		if (b) {
 			value = Integer.parseInt(tvNumberFavourtie.getText().toString())
 					+ Integer.parseInt("1");
 			imgFavourtieIcon.setImageResource(R.drawable.icon_fav_enable);
 			store.setIsFavourite(true);
 		} else {
 			value = Integer.parseInt(tvNumberFavourtie.getText().toString())
 					- Integer.parseInt("1");
 			imgFavourtieIcon.setImageResource(R.drawable.icon_fav_disable);
 			store.setIsFavourite(false);
 		}
 		if (value < 0)
 			value = 0;
 		tvNumberFavourtie.setText("" + value);
 		tvFriendNumber.setTag(store);
 	}
 
 	public void onFavouriteClicked(View v) {
 
 		if (store != null) {
 			currentStoreId = store.getStoreId();
 			if (store.getIsFavourite()) {
 				showDialog();
 				service.removeFavorite(currentStoreId);
 			} else {
 				showDialog();
 				service.postFavorite(currentStoreId);
 			}
 		}
 	}
 
 	private void bindFriend() {
 		// TODO Auto-generated method stub
 		service.getUserCheckedBills(WhyqApplication.Instance().getRSAToken(),
 				store.getStoreId(), null);
 		showDialog();
 	}
 
 	private void loadCheckFriend(List<User> list) {
 		if (adapter != null) {
 			adapter.resetData();
 		}
 		adapter = new BasicUserAdapter(ListDetailActivity.this, list);
 		lvResult.setAdapter(adapter);
 		adapter.notifyDataSetChanged();
 		lvResult.requestLayout();
 	}
 
 	private void bindImageList() {
 		// TODO Auto-generated method stub
 		ArrayList<Photo> photoList = store.getPhotos();
 		if (photoList.size() > 0) {
 			exePutPhotoListData();
 		}
 	}
 
 	private void exePutPhotoListData() {
 		// TODO Auto-generated method stub
 
 		List<Fragment> fragments = getFragments(store.getPhotos());
 		whyq.adapter.CoverStoryAdapter coverStoryAdapter = new whyq.adapter.CoverStoryAdapter(
 				getSupportFragmentManager(), fragments);
 		vpPhotoList.setAdapter(coverStoryAdapter);
 		coverStoryAdapter.notifyDataSetChanged();
 		vpPhotoList.requestLayout();
 
 	}
 
 	private List<Fragment> getFragments(ArrayList<Photo> photoList) {
 		// TODO Auto-generated method stub
 		List<Fragment> fList = new ArrayList<Fragment>();
 
 		for (int i = 0; i < photoList.size(); i++) {
 			try {
 				Photo photo = photoList.get(i);
 				fList.add(BasicImageListAdapter.newInstance(photo));
 			} catch (Exception e) {
 				// TODO: handle exception
 			}
 		}
 
 		return fList;
 	}
 
 	private void bindPromotionData() {
 		// TODO Auto-generated method stub
 		try {
 			if (store.getPromotionList() != null) {
 				if (store.getPromotionList().size() > 0) {
					promotion = store.getPromotionList().get(0);
 					tvNumberDiscount.setText(promotion.getValuePromotion() + ""
 							+ promotion.getTypeValue());
 					tvDes.setText(promotion.getDescriptionPromotion());
 					tvDate.setText(promotion.getStartDate() + " - "
 							+ promotion.getEndDate());
 				}
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 	}
 
 	private void bindMenuData() {
 		// TODO Auto-generated method stub
 		try {
 			menuList = store.getMenuList();
 
 			int size = menuList.size();
 			if (size > 0) {
 				// menuAdapter = new WhyqMenuAdapter(ListDetailActivity.this,
 				// menuList);
 				// lvMenu.setAdapter(menuAdapter);
 				// ExampleAdapter adapter = new
 				// ExampleAdapter(ListDetailActivity.this);
 				// lvMenu.setAdapter(adapter);
 				ArrayList<GroupMenu> mGroupCollection = new ArrayList<GroupMenu>();
 				ArrayList<String> idList = getProductTypeIdList(menuList);
 				int length = idList.size();
 				for (int i = 0; i < length; i++) {
 					try {
 
 						String id = idList.get(i);
 						GroupMenu group = getGroupFromId(menuList, id);
 
 						if (group != null)
 							mGroupCollection.add(group);
 
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 				}
 				ExpanMenuAdapter adapter = new ExpanMenuAdapter(
 						ListDetailActivity.this, lvMenu, mGroupCollection);
 
 				lvMenu.setAdapter(adapter);
 				adapter.notifyDataSetChanged();
 
 				lvMenu.setAdapter(adapter);
 				for (int i = 0; i < mGroupCollection.size(); i++) {
 					lvMenu.expandGroup(i);
 				}
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 	}
 
 	private GroupMenu getGroupFromId(List<Menu> storyList, String id) {
 		// TODO Auto-generated method stub
 		GroupMenu ge = new GroupMenu();
 		List<Menu> storiesList = new ArrayList<Menu>();
 		int storiesLength = storyList.size();
 		for (int j = 0; j < storiesLength; j++) {
 			Menu story = storyList.get(j);
 			if (story.getTypeProductId().equals(id)) {
 				storiesList.add(story);
 			}
 		}
 		if (storiesList.size() > 0) {
 			ge.setMenuList(storiesList);
 			String name = getNameProductTypeById(storyList, id);
 			if (name != null) {
 				ge.setName(name);
 			} else {
 				ge.setName("");
 			}
 
 			ge.setColor("ffffff");
 			return ge;
 		} else {
 			return null;
 		}
 
 	}
 
 	private String getNameProductTypeById(List<Menu> storyList, String id) {
 		// TODO Auto-generated method stub
 		for (Menu menu : storyList) {
 			try {
 				ArrayList<ProductTypeInfo> productTypeInfoList = menu
 						.getProductTypeInfoList();
 				for (ProductTypeInfo productTypeInfo : productTypeInfoList) {
 					try {
 						if (productTypeInfo.getId().equals(id)) {
 							return productTypeInfo.getNameProductType();
 						}
 
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 				}
 
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	private ArrayList<String> getProductTypeIdList(List<Menu> menuList) {
 		// TODO Auto-generated method stub
 		ArrayList<String> listId = new ArrayList<String>();
 		int length = menuList.size();
 		for (int i = 0; i < length; i++) {
 			Menu menu = menuList.get(i);
 			if (!listId.contains(menu.getTypeProductId())) {
 				listId.add(menu.getTypeProductId());
 			}
 		}
 		return listId;
 	}
 
 	public void onCutleryTabCliked(View v) {
 		resetTabBarFocus(1);
 	}
 
 	public void onWineTabCliked(View v) {
 		resetTabBarFocus(2);
 	}
 
 	public void onCoffeTabClicked(View v) {
 		resetTabBarFocus(3);
 	}
 
 	public void resetTabBarFocus(int index) {
 
 		switch (index) {
 		case 3:
 
 			imgBtnAbout.setBackgroundResource(R.drawable.bg_tab_normal);
 			imgBtnMenu.setBackgroundResource(R.drawable.bg_tab_normal);
 			imgBtnPromotion.setBackgroundResource(R.drawable.bg_tab_active);
 			// imgBtnMenu.setBackgroundResource(R.drawable.icon_cat_coffee);
 			// imgBtnPromotion.setBackgroundResource(R.drawable.bg_tab_active);
 			cateId = "0";
 			Log.d("resetTabBarFocus", "imgBtnAbout");
 			showPhotoList();
 			break;
 		case 1:
 			imgBtnAbout.setBackgroundResource(R.drawable.bg_tab_active);
 			imgBtnPromotion.setBackgroundResource(R.drawable.bg_tab_normal);
 			imgBtnMenu.setBackgroundResource(R.drawable.bg_tab_normal);
 			// imgBtnMenu.setBackgroundResource(R.drawable.icon_cat_coffee);
 			// imgBtnPromotion.setBackgroundResource(R.drawable.icon_cat_cutlery);
 			cateId = "1";
 			Log.d("resetTabBarFocus", "imgBtnMenu");
 			hidePhotoList();
 			break;
 		case 2:
 			imgBtnMenu.setBackgroundResource(R.drawable.bg_tab_active);
 			imgBtnAbout.setBackgroundResource(R.drawable.bg_tab_normal);
 			imgBtnPromotion.setBackgroundResource(R.drawable.bg_tab_normal);
 			// imgBtnAbout.setBackgroundResource(R.drawable.icon_cat_wine);
 			// imgBtnPromotion.setBackgroundResource(R.drawable.icon_cat_cutlery);
 			cateId = "2";
 			Log.d("resetTabBarFocus", "imgBtnPromotion");
 			showPhotoList();
 			break;
 
 		default:
 			break;
 		}
 
 	}
 
 	// public void onAddClicked(View v){
 	// Log.d("onAddClicked","id ="+v.getId());
 	// Menu item = (Menu)v.getTag();
 	// if(item!=null){
 	// if(billList.containsKey(item.getId())){
 	// int value = Integer.parseInt(billList.get(item.getId()).getUnit())+1;
 	// billList.get(item.getId()).setUnit(""+value);
 	// }else{
 	// Bill bill = new Bill();
 	// bill.setId(item.getId());
 	// bill.setPrice(item.getValue());
 	// bill.setUnit("1");
 	// billList.put(item.getId(),bill);
 	// }
 	//
 	// updateCount(item,true);
 	// }
 	// }
 	//
 	// public void onRemoveClicked(View v){
 	// Log.d("onRemoveClicked","id ="+v.getId());
 	// Menu item = (Menu)v.getTag();
 	// updateCount(item,false);
 	// if(billList.containsKey(item.getId())){
 	// int value = Integer.parseInt(billList.get(item.getId()).getUnit())-1;
 	//
 	// billList.get(item.getId()).setUnit(""+value);
 	// if(value < 0)
 	// billList.remove(item.getId());
 	// }else{
 	// Bill bill = new Bill();
 	// bill.setId(item.getId());
 	// bill.setPrice(item.getValue());
 	// bill.setThumb(item.getImageThumb());
 	// bill.setUnit("1");
 	// billList.put(item.getId(),bill);
 	// }
 	// }
 	public void onAddClicked(View v) {
 		Log.d("onAddClicked", "id =" + v.getId());
 		ViewHolderMitemInfo holder = (ViewHolderMitemInfo) v.getTag();
 		Menu item = getMenuById(holder.menuId);
 		if (item != null) {
 			if (billList.containsKey(item.getId())) {
 				int value = Integer.parseInt(billList.get(item.getId())
 						.getUnit()) + 1;
 				billList.get(item.getId()).setUnit("" + value);
 			} else {
 				Bill bill = new Bill();
 				bill.setId(item.getId());
 				bill.setPrice(item.getValue());
				if(promotion!=null)
					bill.setDiscount(promotion.getValuePromotion()!=null?promotion.getValuePromotion():""+0);
 				bill.setUnit("1");
 				bill.setProductId(item.getId());
 				bill.setProductName(item.getNameProduct());
 				billList.put(item.getId(), bill);
 			}
 
 			// updateCount(holder,true);
 			updateCountInExpandListview(holder, true);
 		}
 	}
 
 	private Menu getMenuById(String menuId) {
 		// TODO Auto-generated method stub
 		int size = menuList.size();
 		for (Menu menu : menuList) {
 			if (menuId.equals(menu.getId())) {
 				return menu;
 			}
 		}
 		return null;
 	}
 
 	public void onRemoveClicked(View v) {
 		Log.d("onRemoveClicked", "id =" + v.getId());
 		ViewHolderMitemInfo holder = (ViewHolderMitemInfo) v.getTag();
 		Menu item = getMenuById(holder.menuId);
 		// updateCount(holder,false);
 		updateCountInExpandListview(holder, false);
 		if (item != null) {
 			if (billList.containsKey(item.getId())) {
 				int value = Integer.parseInt(billList.get(item.getId())
 						.getUnit()) - 1;
 
 				billList.get(item.getId()).setUnit("" + value);
 				if (value < 0)
 					billList.remove(item.getId());
 			} else {
 				Bill bill = new Bill();
 				bill.setId(item.getId());
 				bill.setPrice(item.getValue());
				if(promotion!=null)
					bill.setDiscount(promotion.getValuePromotion()!=null?promotion.getValuePromotion():""+0);

 				bill.setThumb(item.getImageThumb());
 				bill.setUnit("1");
 				billList.put(item.getId(), bill);
 			}
 		}
 	}
 
 	public void onViewBillClicked(View v) {
 
 		commentContent = etComment.getText().toString();
 		Intent intent = new Intent(ListDetailActivity.this,
 				WhyQBillScreen.class);
 
 		bundle.putString("store_id", store.getStoreId());
 		bundle.putString("list_items", getListItem());
 		bundle.putString("lat", "" + store.getLatitude());
 		bundle.putString("lon", "" + store.getLongitude());
 		bundle.putString("start_time", "" + store.getStartTime());
 		bundle.putString("close_time", "" + store.getEndTime());
 		bundle.putBoolean("is_ordered", false);
 		intent.putExtra("data", bundle);
 		startActivity(intent);
 	}
 
 	private String getListItem() {
 		// TODO Auto-generated method stub
 		String result = "";
 
 		Iterator myVeryOwnIterator = billList.keySet().iterator();
 		while (myVeryOwnIterator.hasNext()) {
 			String key = (String) myVeryOwnIterator.next();
 			Bill bill = billList.get(key);
 			if (bill != null) {
 				if (result.equals("")) {
 					result += bill.getProductId() + ":" + bill.getUnit() + ":"
 							+ bill.getPrice();
 				} else {
 					result += "|" + bill.getProductId() + ":" + bill.getUnit()
 							+ ":" + bill.getPrice();
 				}
 
 			}
 		}
 		return result;
 	}
 
 	// private void updateCount(Menu item, boolean b) {
 	// // TODO Auto-generated method stub
 	// int size = lvMenu.getChildCount();
 	// float value,totalValue =
 	// Float.parseFloat(btnTotalValue.getText().toString());
 	// Menu item2;
 	// ViewHolderMitemInfo holder;
 	// for(int i=0;i< size;i++){
 	// try {
 	//
 	// item2 = store.getMenuList().get(i);
 	// if(item2.getId().equals(item.getId()) && !item.getValue().equals("")){
 	// holder = (ViewHolderMitemInfo)lvMenu.getChildAt(i).getTag();
 	// if(b){
 	// value =
 	// Float.parseFloat(holder.tvCount.getText().toString())+Float.parseFloat("1");
 	// totalValue+=Float.parseFloat(item.getValue());
 	// }else{
 	// value =
 	// Float.parseFloat(holder.tvCount.getText().toString())-Float.parseFloat("1");
 	// totalValue-=Float.parseFloat(item.getValue());
 	// }
 	// if(value < 0 )
 	// value= 0;
 	// if(totalValue < 0)
 	// totalValue = 0;
 	// holder.tvCount.setText(""+value);
 	// lvMenu.getChildAt(i).requestLayout();
 	// btnTotalValue.setText(""+totalValue);
 	// }
 	//
 	// } catch (Exception e) {
 	// // TODO: handle exception
 	// e.printStackTrace();
 	// }
 	// }
 	// }
 	private void updateCountInExpandListview(ViewHolderMitemInfo holder,
 			boolean b) {
 		int size = lvMenu.getChildCount();
 		float value, totalValue = Float.parseFloat(btnTotalValue.getText()
 				.toString());
 		Menu item2;
 		Menu item = getMenuById(holder.menuId);
 		if (b) {
 			value = Float.parseFloat(holder.tvCount.getText().toString())
 					+ Float.parseFloat("1");
 			totalValue += Float.parseFloat(item.getValue());
 		} else {
 			value = Float.parseFloat(holder.tvCount.getText().toString())
 					- Float.parseFloat("1");
 			totalValue -= Float.parseFloat(item.getValue());
 		}
 		if (value < 0)
 			value = 0;
 		if (totalValue < 0)
 			totalValue = 0;
 		holder.tvCount.setText("" + String.format("%.2f", value));
 		holder.tvCount.requestLayout();
 		btnTotalValue.setText("" + String.format("%.2f", totalValue));
 		checkCommentView(totalValue);
 	}
 
 	private void checkCommentView(float totalValue) {
 		// TODO Auto-generated method stub
 		Log.d("checkCommentView", "totalValue: " + totalValue);
 		if (totalValue > 0) {
 			if (etComment.getVisibility() != View.VISIBLE)
 				WhyqUtils.showViewFromBottonToTop(context, etComment);
 		} else {
 			if (etComment.getVisibility() == View.VISIBLE)
 				WhyqUtils.hideViewFromToptoBottm(context, etComment);
 		}
 	}
 
 	private void updateCount(ViewHolderMitemInfo holder, boolean b) {
 		// TODO Auto-generated method stub
 		int size = lvMenu.getChildCount();
 		float value, totalValue = Float.parseFloat(btnTotalValue.getText()
 				.toString());
 		Menu item2;
 		Menu item = getMenuById(holder.menuId);
 		for (int i = 0; i < size; i++) {
 			try {
 
 				item2 = store.getMenuList().get(i);
 				if (item2.getId().equals(item.getId())
 						&& !item.getValue().equals("")) {
 					// holder =
 					// (ViewHolderMitemInfo)lvMenu.getChildAt(i).getTag();
 					if (b) {
 						value = Float.parseFloat(holder.tvCount.getText()
 								.toString()) + Float.parseFloat("1");
 						totalValue += Float.parseFloat(item.getValue());
 					} else {
 						value = Float.parseFloat(holder.tvCount.getText()
 								.toString()) - Float.parseFloat("1");
 						totalValue -= Float.parseFloat(item.getValue());
 					}
 					if (value < 0)
 						value = 0;
 					if (totalValue < 0)
 						totalValue = 0;
 					holder.tvCount.setText("" + value);
 					lvMenu.getChildAt(i).requestLayout();
 					btnTotalValue.setText("" + totalValue);
 				}
 
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void gotoCommentScreen(View v) {
 		if (store != null) {
 			if (store.getCountFavaouriteMember() != null) {
 				if (store.getCountFavaouriteMember().equals("0")) {
 					Intent intent = new Intent(ListDetailActivity.this,
 							WhyqShareActivity.class);
 					intent.putExtra("store_id", store.getStoreId());
 					intent.putExtra("is_comment", true);
 
 					startActivity(intent);
 				} else {
 					Intent intent = new Intent(ListDetailActivity.this,
 							CommentActivity.class);
 					intent.putExtra("store_id", store.getStoreId());
 					startActivity(intent);
 				}
 
 			} else {
 				Intent intent = new Intent(ListDetailActivity.this,
 						WhyqShareActivity.class);
 				intent.putExtra("store_id", store.getStoreId());
 				startActivity(intent);
 			}
 		}
 	}
 
 	public void onBack(View v) {
 		finish();
 
 	}
 
 	private final TextWatcher mTextEditorWatcher = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 			// exeSearchFocus();
 		}
 
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 			// This sets a textview to the current length
 
 		}
 
 		public void afterTextChanged(Editable s) {
 
 			try {
 				String text = s.toString();
 				Log.d("Text serch", "Text " + text);
 				if (!text.equals("")) {
 					exeSearchUserChecked(text);
 				} else {
 
 				}
 
 			} catch (Exception e) {
 				// TODO: handle exception
 			}
 		}
 	};
 	private int mPosition;
 	private int mOffset;
 
 	protected void exeSearchUserChecked(String text) {
 		// TODO Auto-generated method stub
 		service.getUserCheckedBills(WhyqApplication.Instance().getRSAToken(),
 				store.getStoreId(), text);
 		showDialog();
 	}
 
 	public void refreshDataAfterScroll() {
 		// TODO Auto-generated method stub
 		if (ScreenGestureController.isUp) {
 			hidePhotoList();
 		} else {
 			showPhotoList();
 		}
 
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		// TODO Auto-generated method stub
 		// if(scrollState==0)
 		Log.d("onScrollStateChanged", "onScrollStateChanged " + scrollState);
 		refreshDataAfterScroll();
 	}
 
 }
