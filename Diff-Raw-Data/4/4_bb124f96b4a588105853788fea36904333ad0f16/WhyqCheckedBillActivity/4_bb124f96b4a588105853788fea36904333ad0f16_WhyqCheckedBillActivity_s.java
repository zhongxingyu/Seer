 package whyq.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import whyq.WhyqApplication;
 import whyq.model.Bill;
 import whyq.model.BillItem;
 import whyq.model.BillPlaceItem;
 import whyq.model.ResponseData;
 import whyq.service.DataParser;
 import whyq.service.Service;
 import whyq.service.ServiceAction;
 import whyq.service.ServiceResponse;
 import whyq.utils.ImageViewHelper;
 import whyq.utils.Util;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 
 import com.whyq.R;
 
 public class WhyqCheckedBillActivity extends ImageWorkerActivity {
 
 	public static final String ARG_USER_ID = "userid";
 
 	protected static final String ARG_MODE = "mode";
 
 	protected static final String SAVING = "saving";
 
 	public static ArrayList<Bill> listBill = new ArrayList<Bill>();
 
 	private BillAdapter mBillAdapter;
 	
 	private PlaceAdapter mPlaceAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_checked_bill);
 
 		final String mode = getIntent().getStringExtra(ARG_MODE);
 
 		mBillAdapter = new BillAdapter(this, mImageWorker);
 		mPlaceAdapter = new PlaceAdapter(this, mImageWorker);
 	
 		
 		if (mode != null && mode.equals(SAVING)) {
 			setTitle(R.string.saving);
 			mBillAdapter.setSavingMode(true);
 			mPlaceAdapter.setSavingMode(true);
 		} else {
 			setTitle(R.string.checked_bills);
 			mBillAdapter.setSavingMode(false);
 			mPlaceAdapter.setSavingMode(false);
 		}
 
 		mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
 		mRadioGroup.getLayoutParams().width = WhyqApplication.sScreenWidth * 3 / 5;
 		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				bindAdapter();
 			}
 		});
 		
 		mListview = (ListView) findViewById(R.id.listview);
 		mListview.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 //				No need for now
 //				whyq.activity.WhyqCheckedBillActivity.BillAdapter.ViewHolder holder= (whyq.activity.WhyqCheckedBillActivity.BillAdapter.ViewHolder)arg1.getTag();
 //				BillItem item = (BillItem)holder.data;
 //				Intent intent = new Intent(WhyqCheckedBillActivity.this, WhyQBillScreen.class);
 //
 //				Bundle bundle = new Bundle();
 //				bundle.putString("store_id", item.getStore_id());
 ////				bundle.putString("list_items", item.getBusiness_info());
 //				bundle.putBoolean("ordered", true);
 //				bundle.putString("lat", "" + item.getBusiness_info().getLatitude());
 //				bundle.putString("lon", "" + item.getBusiness_info().getLongitude());
 //				bundle.putString("start_time", "" + item.getBusiness_info().getStart_time());
 //				bundle.putString("close_time", "" + item.getBusiness_info().getEnd_time());
 //				intent.putExtra("data", bundle);
 //				startActivity(intent);
 			}
 		});
 
 		setLoading(true);
 		final String userId = getIntent().getExtras().getString(ARG_USER_ID);
 		getService().getOrder(getEncryptedToken(), userId);
 	}
 
 	private List<BillItem> mBillItems;
 	private List<BillPlaceItem> mBillPlaceItems = new ArrayList<BillPlaceItem>();
 
 	private ListView mListview;
 
 	private RadioGroup mRadioGroup;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCompleted(Service service, ServiceResponse result) {
 		super.onCompleted(service, result);
 		setLoading(false);
 		if (result != null && result.isSuccess()
 				&& result.getAction() == ServiceAction.ActionGetOrder) {
 			DataParser parser = new DataParser();
 			Object dataObj = parser
 					.parseBills(String.valueOf(result.getData()));
 			if (dataObj == null) {
 				return;
 			}
 
 			ResponseData data = (ResponseData) dataObj;
 			if (data.getStatus().equals("401")) {
 				Util.loginAgain(this, data.getMessage());
 			} else {
 				createBillItems((List<BillItem>) data.getData());
 				mBillAdapter.setItems(mBillItems);
 				mPlaceAdapter.setItems(mBillPlaceItems);
 				bindAdapter();
 			}
 		}
 	}
 
 	private void bindAdapter() {
 		if (mRadioGroup.getCheckedRadioButtonId() == R.id.viewBill) {
 			mListview.setAdapter(mBillAdapter);
 		} else {
 			mListview.setAdapter(mPlaceAdapter);
 		}
 	}
 
 	private void createBillItems(List<BillItem> data) {
 		mBillItems = data;
 		for (BillItem billItem : data) {
 			try {
 				addToPlace(billItem);
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void addToPlace(BillItem item) {
 		int index = -1;
 		for (int i = 0, count = mBillPlaceItems.size(); i < count; i++) {
 			final BillPlaceItem placeItem = mBillPlaceItems.get(i);
 			if (placeItem.getStoreId().equals(item.getStore_id())) {
 				index = i;
 				break;
 			}
 		}
 
 		BillPlaceItem tmp = null;
 		if (index == -1) {
 			tmp = new BillPlaceItem();
 			tmp.setStoreId(item.getStore_id());
 			tmp.setCountVisited(1);
 			if(item.getBusiness_info()!=null)
 				tmp.setStoreAddress(item.getBusiness_info().getAddress());
 			tmp.setStoreName(item.getBusiness_info().getName_store());
 			tmp.setTotalDiscountValue(item.getDiscount_value());
 			tmp.setTotalValue(item.getTotal_value());
 			if(item.getBusiness_info()!=null)
 				tmp.setStoreLogo(item.getBusiness_info().getLogo());
 			mBillPlaceItems.add(tmp);
 		} else {
 			tmp = mBillPlaceItems.get(index);
 			tmp.setCountVisited(tmp.getCountVisited() + 1);
 			tmp.setTotalDiscountValue(tmp.getTotalDiscountValue()
 					+ item.getDiscount_value());
 			tmp.setTotalValue(tmp.getTotalValue() + item.getTotal_value());
 			mBillPlaceItems.set(index, tmp);
 		}
 
 	}
 
 	static class BillAdapter extends BaseAdapter {
 
 		private static final int PHOTO_SIZE = WhyqApplication.sBaseViewHeight / 5 * 4;
 		private Context mContext;
 		private List<BillItem> mItems;
 		private ImageViewHelper mImageWorker;
 		private boolean isSavingMode;
 
 		public BillAdapter(Context context, ImageViewHelper imageWorker) {
 			this.mContext = context;
 			this.mItems = new ArrayList<BillItem>();
 			this.mImageWorker = imageWorker;
 		}
 
 		public void setSavingMode(boolean savingMode) {
 			isSavingMode = savingMode;
 			notifyDataSetChanged();
 		}
 
 		public void setItems(List<BillItem> items) {
 			if (items == null || items.size() == 0) {
 				mItems.clear();
 			} else {
 				mItems = items;
 			}
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public int getCount() {
 			return mItems.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return mItems.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(mContext).inflate(
 						R.layout.whyq_bill_list_item, parent, false);
 				Util.applyTypeface(convertView,
 						WhyqApplication.sTypefaceRegular);
 			}
 
 			ViewHolder holder = getViewHolder(convertView);
 			BillItem item = mItems.get(position);
 			if (isSavingMode) {
 				int disCount = (int) (item.getDiscount_value() * 100 / item.getTotal_real_value());
 				holder.unit.setText("Discount " + disCount + " %");
 				holder.price.setText("$ " + item.getDiscount_value());
 			} else {
 				holder.unit.setText("Normal bill");
 				holder.price.setText("$ " + item.getTotal_value());
 			}
 			if(item.getBusiness_info()!=null)
 				holder.name.setText(item.getBusiness_info().getName_store());
 			holder.data = item;
			mImageWorker.downloadImage(item.getBusiness_info().getLogo(),
					holder.photo);
 			convertView.setTag(holder);
 			return convertView;
 		}
 
 		private ViewHolder getViewHolder(View view) {
 			ViewHolder holder = (ViewHolder) view.getTag();
 			if (holder == null) {
 				holder = new ViewHolder(view);
 				view.setTag(holder);
 			}
 			return holder;
 		}
 
 		class ViewHolder {
 			public BillItem data;
 			ImageView photo;
 			TextView name;
 			TextView unit;
 			Button price;
 
 			public ViewHolder(View view) {
 				photo = (ImageView) view.findViewById(R.id.photo);
 				photo.getLayoutParams().width = PHOTO_SIZE;
 				photo.getLayoutParams().height = PHOTO_SIZE;
 				name = (TextView) view.findViewById(R.id.name);
 				unit = (TextView) view.findViewById(R.id.unit);
 				price = (Button) view.findViewById(R.id.price);
 			}
 		}
 
 	}
 
 	static class PlaceAdapter extends BaseAdapter {
 
 		private static final int PHOTO_SIZE = WhyqApplication.sBaseViewHeight / 5 * 4;
 		private Context mContext;
 		private List<BillPlaceItem> mItems;
 		private ImageViewHelper mImageWorker;
 		private boolean isSavingMode;
 
 		public PlaceAdapter(Context context, ImageViewHelper imageWorker) {
 			this.mContext = context;
 			this.mItems = new ArrayList<BillPlaceItem>();
 			this.mImageWorker = imageWorker;
 		}
 
 		public void setSavingMode(boolean saving) {
 			isSavingMode = saving;
 			notifyDataSetChanged();
 		}
 
 		public void setItems(List<BillPlaceItem> items) {
 			if (items == null || items.size() == 0) {
 				mItems.clear();
 			} else {
 				mItems = items;
 			}
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public int getCount() {
 			return mItems.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return mItems.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(mContext).inflate(
 						R.layout.whyq_bill_list_item, parent, false);
 				Util.applyTypeface(convertView,
 						WhyqApplication.sTypefaceRegular);
 			}
 
 			ViewHolder holder = getViewHolder(convertView);
 			BillPlaceItem item = mItems.get(position);
 			int count = item.getCountVisited();
 			if (isSavingMode) {
 				holder.unit.setText("Visit " + count
 						+ (count > 1 ? " times" : " time"));
 				holder.price.setText("$ " + item.getTotalDiscountValue());
 			} else {
 				holder.unit.setText(item.getStoreAddress());
 				holder.price.setText("$ " + item.getTotalValue());
 			}
 			holder.name.setText(item.getStoreName());
 			holder.data = item;
 			mImageWorker.downloadImage(item.getStoreLogo(), holder.photo);
 			
 			convertView.setTag(holder);
 			return convertView;
 		}
 
 		private ViewHolder getViewHolder(View view) {
 			ViewHolder holder = (ViewHolder) view.getTag();
 			if (holder == null) {
 				holder = new ViewHolder(view);
 				view.setTag(holder);
 			}
 			return holder;
 		}
 
 		class ViewHolder {
 			public BillPlaceItem data;
 			ImageView photo;
 			TextView name;
 			TextView unit;
 			Button price;
 
 			public ViewHolder(View view) {
 				photo = (ImageView) view.findViewById(R.id.photo);
 				photo.getLayoutParams().width = PHOTO_SIZE;
 				photo.getLayoutParams().height = PHOTO_SIZE;
 				name = (TextView) view.findViewById(R.id.name);
 				unit = (TextView) view.findViewById(R.id.unit);
 				price = (Button) view.findViewById(R.id.price);
 			}
 		}
 
 	}
 	
 	public void onBackClicked(View v){
 		finish();
 	}
 }
