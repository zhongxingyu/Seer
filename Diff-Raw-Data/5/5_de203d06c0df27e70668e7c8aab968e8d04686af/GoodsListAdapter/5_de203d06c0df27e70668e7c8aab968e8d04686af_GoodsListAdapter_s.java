 package com.quanleimu.adapter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.Util;
 import android.os.Handler;
 
 public class GoodsListAdapter extends BaseAdapter {
 
 	private Context context;
 	private TextView tvDes, tvPrice, tvDateAndAddress;
 	private ImageView ivInfo;
 	private List<GoodsDetail> list = new ArrayList<GoodsDetail>();
 	private Button btnDelete;
 	private boolean hasDelBtn = false;
 	private Bitmap defaultBk1, defaultBk2;
 	private Handler handler = null;
 	private int messageWhat = -1;
 	
 	public void setMessageOutOnDelete(Handler h, int messageWhat){
 		this.handler = h;
 		this.messageWhat = messageWhat;
 	}
 	
 	public void setHasDelBtn(boolean has){
 		hasDelBtn = has;
 	}
 
 	public List<GoodsDetail> getList() {
 		return list;
 	}
 
 	public void setList(List<GoodsDetail> list) {
 		this.list = list;
 	}
 
 	public GoodsListAdapter(Context context, List<GoodsDetail> list) {
 		super();
 		this.context = context;
 		this.list = list;
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return list == null ? 0 : list.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		// TODO Auto-generated method stub
 		return arg0;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return position;
 	}
 	
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = LayoutInflater.from(context);
 		View v = convertView;
 		if(v == null){
 			v = inflater.inflate(R.layout.item_goodslist, null);
 		}
 		if (list.size() == 1) {
 			v.setBackgroundResource(R.drawable.btn_s_bg);
 		} else {
 			if (position == 0) {
 				v.setBackgroundResource(R.drawable.btn_top_bg);
 			} else if (position == list.size() - 1) {
 				v.setBackgroundResource(R.drawable.btn_down_bg);
 			} else {
 				v.setBackgroundResource(R.drawable.btn_m_bg);
 			}
 		}
 		tvDes = (TextView) v.findViewById(R.id.tvDes);
 		tvPrice = (TextView) v.findViewById(R.id.tvPrice);
 		tvPrice.setTextColor(Color.RED);
 		tvDateAndAddress = (TextView) v.findViewById(R.id.tvDateAndAddress);
 		tvDateAndAddress.setTextColor(R.color.hui);
 		btnDelete = (Button) v.findViewById(R.id.btnDelete);
 
 		if (!hasDelBtn) {
 			btnDelete.setVisibility(View.GONE);
 		} 
 		else{
 			btnDelete.setVisibility(View.VISIBLE);
 		}
 
 		ivInfo = (ImageView) v.findViewById(R.id.ivInfo);
 		if(QuanleimuApplication.isTextMode()){
 			ivInfo.setVisibility(View.GONE);
 		}
 		BitmapFactory.Options o =  new BitmapFactory.Options();
         o.inPurgeable = true;
         if(null == defaultBk1){
         	Bitmap tmb = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren1, o);
         	defaultBk1 = Helper.toRoundCorner(tmb, 10);
         	tmb.recycle();
         }
 		if(null == defaultBk2){
 			Bitmap tmb1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren, o);
 			defaultBk2 = Helper.toRoundCorner(tmb1, 10);
 			tmb1.recycle();
 		}
 		if(!QuanleimuApplication.isTextMode()){
 			ivInfo.setImageBitmap(defaultBk1);
 		}
 		
 		int type = Util.getWidthByContext(context);
 		RelativeLayout.LayoutParams lp = null;
 		switch (type) {
 		case 240:
 			lp = new RelativeLayout.LayoutParams(45, 45);
 			break;
 		case 320:
 			lp = new RelativeLayout.LayoutParams(60, 60);
 			break;
 		case 480:
 			lp = new RelativeLayout.LayoutParams(90, 90);
 			break;
 		case 540:
 			lp = new RelativeLayout.LayoutParams(100, 100);
 			break;
 		case 640:
 			lp = new RelativeLayout.LayoutParams(120, 120);
 			break;
 		default:
 			 lp= new RelativeLayout.LayoutParams(140,140);
 			break;
 		}
 
 		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		if(!QuanleimuApplication.isTextMode()){
 			ivInfo.setLayoutParams(lp);
 			
 			if (list.get(position).getImageList() == null
 					|| list.get(position).getImageList().equals("")
 					|| list.get(position).getImageList().getResize180() == null
 					|| list.get(position).getImageList().getResize180()
 							.equals("")) {
 				ivInfo.setImageBitmap(defaultBk2);
 			} else {
 	//			if (isConnect == 0) {
 	//				ivInfo.setImageBitmap(defaultBk2);
 	//			}
 	//			else{
 					String b = (list.get(position).getImageList().getResize180())
 					.substring(1, (list.get(position).getImageList()
 							.getResize180()).length() - 1);
 					b = Communication.replace(b);
 			
 					if (b.contains(",")) {
 						String[] c = b.split(",");
 						if (c[0] == null || c[0].equals("")) {
 							
 							ivInfo.setImageBitmap(defaultBk2);
 						} else {
 							ivInfo.setTag(c[0]);
 							SimpleImageLoader.showImg(ivInfo, c[0], this.context);
 							
 						}
 					} else {
 						if (b == null || b.equals("")) {
 							ivInfo.setImageBitmap(defaultBk2);
 						} else {
 							ivInfo.setTag(b);
 							SimpleImageLoader.showImg(ivInfo, b, this.context);				
 						}
 					}
 	//			}
 			}
 		}
 		String price = "";
 		try {
 			price = list.get(position).getMetaValueByKey("价格") + "";
 		} catch (Exception e) {
 			price = "";
 		}
 		if (price.equals("null") || price.equals("")) {
 			tvPrice.setVisibility(View.GONE);
 		} else {
 			tvPrice.setVisibility(View.VISIBLE);
 			tvPrice.setText(price);
 		}
 		tvDes.setText(list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE));
 
 		String dateV = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DATE);
 		if(dateV != null && !dateV.equals(""))
 		{
 			Date date = new Date(Long.parseLong(dateV) * 1000);
 			SimpleDateFormat df = new SimpleDateFormat("MM月dd日",
 					Locale.SIMPLIFIED_CHINESE);
 			
 			String areaV = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 			if(areaV != null && !areaV.equals(""))
 			{
 				tvDateAndAddress.setText(df.format(date) + " "
 						+ areaV);
 			}
 			else
 			{
 				tvDateAndAddress.setText(df.format(date));
 			}
 		}
 		else
 		{
 			String areaV = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 			if(areaV != null && !areaV.equals(""))
 			{
 				tvDateAndAddress.setText(areaV);
 			}
 			else
 			{
 				tvDateAndAddress.setVisibility(View.GONE);
 			}
 		}
 
 		btnDelete.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(GoodsListAdapter.this.handler == null) return;
 				Message msg = GoodsListAdapter.this.handler.obtainMessage();
 				msg.arg2 = position;
 				msg.what = GoodsListAdapter.this.messageWhat;
 //				list.remove(position);
 				GoodsListAdapter.this.notifyDataSetChanged();
 				GoodsListAdapter.this.handler.sendMessage(msg);
 			}
 		});
 		return v;
 	}
 
 }
