 package com.lcm.data;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.text.format.DateFormat;
 import android.util.Log;
 
 
 // TODO: matching data with database
 public class ParsedData implements Parcelable {
 	private static final String TAG = "ParsedData";
 	
 	private int spent;
 	private String category;
 	private Calendar date;
 	private String detail;
 	private Location location;
 	private String bank;
 	private int sms_id;
 	private boolean flag;
 	private int installment;
 	
 	public int getSms_id() {
 		return sms_id;
 	}
 	
 	public void setSms_id(int sms_id) {
 		this.sms_id = sms_id;
 	}
 
 	public ParsedData(Parcel parcel) {
 		spent = parcel.readInt();
 		installment = parcel.readInt();
 		category = parcel.readString();
 		category = "unknown";//parcel.readString();
 		date = new GregorianCalendar();
 		date.setTime(new Date(parcel.readLong()));
 		detail = parcel.readString();
 		location = parcel.readParcelable(Location.class.getClassLoader());
 		bank = parcel.readString();
 //		bank = "unknown";//parcel.readString();
 		sms_id = parcel.readInt();
 		flag = false;
 //		Log.e("ParsedData","contruct with parcel -- "+spent+","+category+","+date+","+detail+","+location+","+bank+","+sms_id);
 //		new ParsedData(spent, category,new Date(date), detail, location, bank);
 	}
 	
 	public ParsedData(int spent, int installment, String category, Calendar date,String detail,Location location, String bank, int sms_id) {
 		this.spent = spent;
 		this.installment = installment;
 		this.category = category;
 		this.date = date;
 		this.detail = detail;
 		this.bank = "unknown";
 		this.location = location;
 		this.bank = bank;
 		this.sms_id = sms_id;
 		flag = false;
 	}
 
 	public ParsedData(int spent, int installment, String category, Calendar date,String detail,String bank, int sms_id) {
 		this.spent = spent;
 		this.installment = installment;
 		this.category = "unknown";
 		this.date = date;
 		this.detail = detail;
 		this.bank = bank;
 		this.location = null;
 		this.sms_id	= sms_id;
 		flag = false;
 	}
 
 	public ParsedData(int spent, int installment, String category, Calendar date,String detail) {
 		this.spent = spent;
 		this.installment = installment;
 		this.category = category;
 		this.date = date;
 		this.detail = detail;
 		this.bank = "unknown";
 		this.location = null;
 		this.sms_id = -1;
 		flag = false;
 	}
 
 	public ParsedData(int spent, int installment, String category, Calendar date) {
 		this.spent = spent;
 		this.installment = installment;
 		this.category = category;
 		this.date = date;
 		this.detail = "";
 		this.bank = "unknown";
 		this.location = null;
 		this.sms_id = -1;
 		flag = false;
 	}
 	
 	public String getDetail() {
 		return detail;
 	}
 
 	public void setDetail(String detail) {
 		this.detail = detail;
 	}
 
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	public int getSpent() {
 		return spent;
 	}
 	public void setSpent(int spent) {
 		this.spent = spent;
 	}
 	public String getPlace() {
 		return detail;
 	}
 	public void setPlace(String place) {
 		this.detail = place;
 	}
 	public Calendar getDate() {
 		return date;
 	}
 	public void setDate(Calendar date) {
 		this.date = date;
 	}
 
 	public String getBank() {
 		return bank;
 	}
 
 	public void setBank(String bank) {
 		this.bank = bank;
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 //	private int spent;
 //	private String category;
 //	private Date date;
 //	private String detail;
 //	private Location location;
 //	private String bank;
 //	private int sms_id;
 	
 	public boolean isFlag() {
 		return flag;
 	}
 
 	public void setFlag(boolean flag) {
 		this.flag = flag;
 	}
 
 	@Override
 	public String toString() {
 		return "spend: " + spent + " category: " + category + " date: " + date.toString() + " detail: " + detail + " bank: " + bank + " sms_id: " + sms_id;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		// TODO Auto-generated method stub
 		dest.writeInt(spent);
 		dest.writeInt(installment);
 		dest.writeString(category);
 		dest.writeLong(date.getTimeInMillis());
 		dest.writeString(detail);
 		dest.writeParcelable(location, flags);
 		dest.writeString(bank);
 		dest.writeInt(sms_id);
 	}
 	
 	public static final Parcelable.Creator<ParsedData> CREATOR = new Parcelable.Creator<ParsedData>() {
 		@Override
 		public ParsedData createFromParcel(Parcel source) {
 			return new ParsedData(source);
 		}
 		@Override
 		public ParsedData[] newArray(int size) {
 			return new ParsedData[size];
 		}
 	};
 	
 	public String getDateFormat() {
 		return DateFormat.format("MMM dd, yyyy h:mmaa",date.getTime()).toString();
 	}
 	
 	public ArrayList<InstallmentDatePrice> getInstallmentDatePrice(Context context) {
 		ArrayList<InstallmentDatePrice> result = new ArrayList<ParsedData.InstallmentDatePrice>();
 		int eachPrice = spent/installment;
 		SharedPreferences sPref = context.getSharedPreferences(SettingsPreference.PREFERENCES_NAME, 0);
 		int accountingDate = Integer.parseInt(sPref.getString(SettingsPreference.PREF_CAL_FROM,"15"));
 		
 		Calendar eachDate = new GregorianCalendar(date.get(Calendar.YEAR),date.get(Calendar.MONTH),accountingDate);
 		Calendar eachDay = date; //new Date(eachDate.getTimeInMillis());
 		for(int i=0; i<installment; i++) {
			Calendar insertDate = new GregorianCalendar();
			insertDate.setTimeInMillis(eachDay.getTimeInMillis());
			insertDate.add(Calendar.MILLISECOND, 1);
			Log.e(TAG,"Year:" + eachDay.get(Calendar.YEAR) + " Month:" +
					(eachDay.get(Calendar.MONTH)+1) + "Date:" + eachDay.get(Calendar.DAY_OF_MONTH) +
					"Time: " + eachDay.getTimeInMillis() + ", " + insertDate.getTimeInMillis() +
					"," + (eachDay.getTimeInMillis() - insertDate.getTimeInMillis()));
 			result.add(new InstallmentDatePrice(insertDate, eachPrice));
 			eachDate.add(Calendar.MONTH, 1);
 			eachDay = eachDate;
 		}
 		return result;
 	}
 	
 	public int getInstallment() {
 		return installment;
 	}
 
 	public class InstallmentDatePrice {
 		private Calendar installmentDate;
 		private int installmentPrice;
 		
 		public InstallmentDatePrice(Calendar inst, int price) {
 			installmentDate = inst;
 			installmentPrice = price;
 		}
 		public Calendar getInstallmentDate() {
 			return installmentDate;
 		}
 		public int getInstallmentPrice() {
 			return installmentPrice;
 		}
 	}
 }
