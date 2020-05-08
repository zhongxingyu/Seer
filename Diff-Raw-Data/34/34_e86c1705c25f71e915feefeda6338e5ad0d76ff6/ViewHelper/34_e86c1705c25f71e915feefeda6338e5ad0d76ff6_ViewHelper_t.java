 package ws.wiklund.guides.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.net.URLEncoder;
 import java.nio.channels.FileChannel;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import ws.wiklund.guides.R;
 import ws.wiklund.guides.bolaget.SystembolagetParser;
 import ws.wiklund.guides.db.BeverageDatabaseHelper;
 import ws.wiklund.guides.model.Beverage;
 import ws.wiklund.guides.model.BeverageType;
 import ws.wiklund.guides.model.Category;
 import ws.wiklund.guides.model.Cellar;
 import ws.wiklund.guides.model.Country;
 import ws.wiklund.guides.model.Producer;
 import ws.wiklund.guides.model.Provider;
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ViewHelper {
 	public static final int GO_TO_CELLAR = 1234;
 
 	private final static DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
 	private final static DateFormat dateTimeFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
 	private final static DecimalFormat decimalFormat = new DecimalFormat("0.0");
 	private final static DecimalFormat currencyFormat = new java.text.DecimalFormat("SEK 0.00");
 	private final static String urlp3Api = "?chf=bg,s,65432100&cht=p3&chs=400x150&chl=";
 
 	private final static Calendar calendar = Calendar.getInstance();
 	private final static List<Integer> years = new ArrayList<Integer>();
 	
 	private static final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "guides" + File.separator);
 	
 	static {
 		decimalFormat.setDecimalSeparatorAlwaysShown(true);
 		decimalFormat.setParseBigDecimal(true);
 	}
 	
     public static void setText(TextView view, String value) {
 		if (value != null) {
 			view.setText(value);
 		}
 	}
     
     public static void setThumbFromUrl(ImageView view, String thumb) {
     	if(thumb != null) {
     		new DownloadImageTask(view, SystembolagetParser.BASE_URL, 50, 100).execute(thumb);
     	}
 	}
 	
     public static void setCountryThumbFromUrl(ImageView view, Country country) {
 		if (country != null && country.getThumbUrl() != null) {
 			new DownloadImageTask(view, SystembolagetParser.BASE_URL, 29, 17).execute(country != null ? country.getThumbUrl() : null);
 		}
 	}
     
 	public static String getDateAsString(Date date) {
 		return dateFormat.format(date);
 	}
 
 	public static String getDateTimeAsString(Date date) {
 		return dateTimeFormat.format(date);
 	}
 
 	public static String getDecimalStringFromNumber(Number value) {
 		String s = decimalFormat.format(value);
 				
 		if(s.endsWith(String.valueOf(decimalFormat.getDecimalFormatSymbols().getDecimalSeparator()))) {
 			return s.substring(0, s.length() - 1);
 		}
 		
 		return s;
 	}
 	
 	public static Double getDoubleFromDecimalString(String value) {
 		try {
 			BigDecimal bd = (BigDecimal) decimalFormat.parse(value);
 			return bd.doubleValue();
 		} catch (ParseException e) {
 			Log.d(ViewHelper.class.getName(), "Failed to parse string (" + value + ")", e);
 		}
 		
 		return -1d;
 	}
 
 	public static synchronized List<Integer> getYears() {
 		if(years.isEmpty()) {
 			for(int i = 1900; i<= calendar.get(Calendar.YEAR); i++) {
 				years.add(i);
 			}	
 		}
 		
 		return years;
 	}
 	
 	public static int getCurrentYear() {
 		return calendar.get(Calendar.YEAR);
 	}
 
 	public static String formatPrice(double price) {
 		return currencyFormat.format(price);
 	}
 	
 	public static String buildChartUrl(BeverageDatabaseHelper helper) {
 		StringBuilder urlBuilder = new StringBuilder();
 		StringBuilder values = new StringBuilder();
 
 		urlBuilder.append(urlp3Api);
 		
 		List<Beverage> beverages = helper.getAllBeverages();
 		
 		Map<BeverageType, Integer> m = new HashMap<BeverageType, Integer>();
 		for(Beverage beverage : beverages) {			
 			Integer amount = m.get(beverage.getBeverageType());
 			m.put(beverage.getBeverageType(), amount != null ? amount + 1 : 1);
 		}
 		
 		LinkedList<BeverageType> types = new LinkedList<BeverageType>(m.keySet());
 
 		BeverageType type;
 		while((type = types.poll()) != null) {
 			int amount = m.get(type);
 			
 			urlBuilder.append(URLEncoder.encode(type.toString())).append("(").append(amount).append(")");
 			values.append(amount);
 			
 			BeverageType peek = types.peek(); 
 			if(peek != null) {
 				urlBuilder.append("|");
 				values.append(",");
 			}				
 		}
 		
 		urlBuilder.append("&chd=t:");
 		urlBuilder.append(values.toString());
 
     	String url = urlBuilder.toString();
     	
 		Log.d(ViewHelper.class.getName(), "Chart URL[" + url + "]");		        	
 		
 		return url;
 	}
 	
 	public static boolean validateBeverage(Context c, Beverage b) {
 		String name = b.getName();
 		if(name == null || name.length() == 0) {
 			Toast.makeText(c, c.getString(R.string.missingName), Toast.LENGTH_SHORT).show();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public static File getRoot() {
 		if(!root.exists()) {
 			root.mkdirs();
 		}		
 
 		return root;
 	}
 	
 	public static Beverage getBeverageFromCursor(Cursor c) {
 		return new Beverage(
 				c.getInt(0),
 				c.getString(1),
 				c.getInt(2),
 				new BeverageType(c.getInt(3), c.getString(4)),
 				c.getString(5),
 				c.getString(6),
 				new Country(c.getInt(7), c.getString(8), c.getString(9)),
 				c.getInt(10),
 				new Producer(c.getInt(11), c.getString(12)),
 				c.getFloat(13),
 				c.getFloat(14),
 				c.getString(15),
 				c.getString(16),
 				new Provider(c.getInt(17), c.getString(18)),
 				c.getFloat(19),
 				c.getString(20),
 				isValidCategory(c) ? new Category(c.getInt(21), c.getString(22)) : null,
 				new Date(c.getLong(23)),
 				c.getInt(24));
 	}
 	
 	public static Cellar getCellarFromCursor(Cursor c) {
 		return new Cellar(
 				c.getInt(0),
 				c.getInt(1),
 				c.getInt(2),
 				c.getString(3),
 				c.getString(4),
 				new Date(c.getLong(5)),
 				c.isNull(6) ? null: new Date(c.getLong(6)),
 				c.getInt(7));
 	}	
 
 	public static View getView(LayoutInflater inflator, int item, View view, Cursor c) {
 		ViewHolder holder;
 		
 		if (view == null) {  
 			view = inflator.inflate(item, null);
 			
 			TextView titleView = (TextView) view.findViewById(R.id.itemTitle);  
 	        TextView textView = (TextView) view.findViewById(R.id.itemText);  
 	        TextView typeView = (TextView) view.findViewById(R.id.itemType);  
 	        ImageView imageView = (ImageView) view.findViewById(R.id.itemImage);
 	        RatingBar rating = (RatingBar) view.findViewById(R.id.itemRatingBar);
 
 	         
 	        holder = new ViewHolder();  
 	        holder.titleView = titleView;  
 	        holder.textView = textView;  
 	        holder.imageView = imageView;
 	        holder.rating = rating;
 	        holder.typeView = typeView;
 	         
 	        view.setTag(holder);
 		} else {
 			holder = (ViewHolder) view.getTag(); 
 		}
 		
 		if (c != null) {
 			Beverage b = getBeverageFromCursor(c);
 			
 			int noBottles = b.getBottlesInCellar(); 
 			StringBuilder name = new StringBuilder(b.getName());
 			
 			if(noBottles > 0) {
 				name.append("(").append(noBottles).append(")");
 			}
 					
 			holder.titleView.setText(name.toString());
 			
 			String type = b.getBeverageType().getName();
 			
 			Category category = b.getCategory();
 			if(category != null && category.getName() != null) {
 				type += " (" + category.getName() + ")";
 			}
 			
 			holder.typeView.setText(type);
 			
 			int year = b.getYear(); 
 			holder.textView.setText(b.getCountry().getName() + " " + (year != -1 ? year : ""));
 			holder.rating.setRating(b.getRating());
 
 			String u = b.getThumb();			
 			if (u != null) {
 				String url = b.isCustomThumb() ? u : SystembolagetParser.BASE_URL + u;
 				holder.imageView.setTag(url);
 				BitmapManager.INSTANCE.loadBitmap(url, holder.imageView, 50, 100);
 			} else {
 				holder.imageView.setImageResource(R.drawable.icon);
 			}
 		}
 		
 		return view;
 	}
 	
 	public static void copyFile(File src, File dst) throws IOException {
 		FileInputStream in = new FileInputStream(src);
 		FileOutputStream out = new FileOutputStream(dst);
 		FileChannel inChannel = in.getChannel();
 		FileChannel outChannel = out.getChannel();
 		
 		try {
 			inChannel.transferTo(0, inChannel.size(), outChannel);
 		} finally {
 			in.close();
 			out.close();
 			
 			if (inChannel != null)
 				inChannel.close();
 			if (outChannel != null)
 				outChannel.close();
 		}
 	}
 	
 	public static boolean isValidNo(Context context, BeverageDatabaseHelper helper, String no) {
 		if(no != null && no.length() > 0 && Pattern.matches("^\\d*$", no)) {
 			try {
 				if(!exists(helper, no)) {
 					return true;
 				} else {				
 					Toast.makeText(context, context.getString(R.string.exist) + " " + no, Toast.LENGTH_SHORT).show();  		
 				}
 			} catch (NumberFormatException e) {
 	        	Log.d(ViewHelper.class.getName(), "Invalid search string: " + no);		        	
 				Toast.makeText(context, String.format(context.getString(R.string.invalidNoError), no), Toast.LENGTH_SHORT).show();
 			}
 		} else {
 			Toast.makeText(context, context.getString(R.string.provideNo), Toast.LENGTH_SHORT).show();  		
 		}
 		
 		return false;		
 	}
 	
 	public static void handleAds(Activity activity) {
     	String id = activity.getString(R.string.publisher_id);
     	
     	Log.d(ViewHelper.class.getName(), "Got publisher id [" + id + "]");
     	
     	if(id == null || id.length() == 0) {
         	View v1 = activity.findViewById(R.id.adView);
         	if(v1 != null) {
         		v1.setVisibility(View.GONE);
         	}
 
         	View v2 = activity.findViewById(R.id.adView2);
         	if(v2 != null) {
         		v2.setVisibility(View.GONE);
         	}
 
     	}
 		
 	}
 	
 	private static boolean exists(BeverageDatabaseHelper helper, String no) throws NumberFormatException {
 		return helper.getBeverageIdFromNo(Integer.valueOf(no)) != -1;
 	}
 	
 	private static boolean isValidCategory(Cursor c) {
 		if(!c.isNull(21)) {			
 			return c.getInt(21) > 0;
 		}
 		
 		return false;
 	}
 
 	public static int getNoHitsFromString(String str) {
 		int idx1 = str.indexOf("(");
 		int idx2 = str.lastIndexOf(" tr");
		if(idx1 != -1 && idx2 != -1) {
 			return Integer.valueOf(str.substring(idx1 + 1, idx2));
 		}
 		
 		return -1;
 	}
 	
 }
