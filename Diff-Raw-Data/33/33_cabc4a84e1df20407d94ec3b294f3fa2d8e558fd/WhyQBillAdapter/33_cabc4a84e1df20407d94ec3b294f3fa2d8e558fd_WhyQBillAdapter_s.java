 package whyq.adapter;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import whyq.activity.WhyQBillScreen;
 import whyq.model.Bill;
 import whyq.utils.UrlImageViewHelper;
 import android.content.Context;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.whyq.R;
 
 public class WhyQBillAdapter extends ArrayAdapter<Bill> {
 
 	private final List<Bill> list;
 	private Context context;
 	private HashMap<String, View> viewList = new HashMap<String, View>();
 	public static List<ViewBillHolder> listSectionView = new ArrayList<ViewBillHolder>();
 
 	public WhyQBillAdapter(Context context, List<Bill> billList) {
 		super(context, R.layout.whyq_bill_item, billList);
 		this.context = context;
 		this.list = billList;
 	}
 	public List<Bill> getData(){
 		return list;
 	}
 	public static class ViewBillHolder {
 		public TextView tvName,tvPrice, unit,tvAmount;
 //		public ImageView imgThumb;
 		public String storeId;
 		public ViewBillHolder() {
 
 		}
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = null;
 		convertView = viewList.get(""+position);
 		Bill item = list.get(position);
 		if (convertView == null) {
 			LayoutInflater inflator = ((WhyQBillScreen) context)
 					.getLayoutInflater();
 			view = inflator.inflate(R.layout.whyq_bill_item, null);
 			final ViewBillHolder viewHolder = new ViewBillHolder();
 			viewHolder.tvPrice = (TextView) view.findViewById(R.id.tvPrice);
 //			viewHolder.imgThumb = (ImageView) view.findViewById(R.id.imgThumb);
 			viewHolder.tvName = (TextView)view.findViewById(R.id.tvName);
 			viewHolder.unit = (TextView)view.findViewById(R.id.tvUnit);
 			viewHolder.tvAmount = (TextView)view.findViewById(R.id.tvAmount);
			viewHolder.tvPrice.setText("$"+item.getPrice());
 			viewHolder.unit.setText(item.getUnit());
 			float value = Float.parseFloat(item.getUnit())*Float.parseFloat(item.getPrice());
			viewHolder.tvAmount.setText(""+value);
 //			viewHolder.tvCount.setText(item.getSort());
 //			UrlImageViewHelper.setUrlDrawable(viewHolder.imgThumb, item.getThumb());
 			viewHolder.tvName.setText(Html.fromHtml(item.getProductName()));
 			
 			view.setTag(viewHolder);
 			
 			viewList.put(String.valueOf(item.getId()), view);
 		} else {
 			view = convertView;
 
 		}
 
 		return view;
 	}
 
 }
