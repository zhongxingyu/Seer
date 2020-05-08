 package com.smileyjoedev.iou;
 
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Currency;
 import java.util.Date;
 import java.util.Locale;
 
 import com.smileyjoedev.genLibrary.Contacts;
 import com.smileyjoedev.genLibrary.Debug;
 import com.smileyjoedev.genLibrary.MinimalisticText;
 import com.smileyjoedev.iou.R;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.preference.ListPreference;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class Gen {
 	
 	//TODO: instead of include time send a constant through of the format //
 	public static String convertPdt(long pdt, boolean includeTime){
 		String format = "dd MMM yyyy - HH:mm";
 		
 		if(includeTime){
 			format = "dd MMM yyyy - HH:mm";
 		} else {
 			format = "dd MMM yyyy";
 		}
 		Date date = new Date(pdt);
 		SimpleDateFormat df = new SimpleDateFormat(format);
 		String text = df.format(date);
 		
 		return text;
 	}
 	
 	public static long getPdt(){
 		Calendar c = Calendar.getInstance();
 		Date pdt = c.getTime();
 		
 		return pdt.getTime();
 	}
 
     public static void fillWindow(Window window){
     	window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
     }
 	
 	public static void hideEmptyView(ArrayList<?> array, View view){
 		if(array.isEmpty()){
 			view.setVisibility(View.GONE);
 		}
 	}
 
 
 	public static void changeTheme(Activity activity)
 	{
 		activity.finish();
 		
 		activity.startActivity(new Intent(activity, activity.getClass()));
 	}
 
 	public static void setTheme(Activity activity)
 	{
 //		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
 //		int theme = Integer.parseInt(prefs.getString("theme", "0"));
 //		switch (theme)
 //		{
 //		default:
 //		case Constants.THEME_DEFAULT:
 //			activity.setTheme(R.style.global_dark);
 //			break;
 //		case Constants.THEME_DARK:
 //			activity.setTheme(R.style.global_dark);
 //			break;
 //		case Constants.THEME_LIGHT:
 //			activity.setTheme(R.style.global_light);
 //			break;
 //		}
 	}
 
 	public static boolean setUserImage(Context context, ImageView ivUserImage, User user){
 		boolean imageFound = false;
 		String userName = user.getName();
 //		java.io.File file = new java.io.File(Constants.IMAGE_PATH + userName + Constants.IMAGE_EXTENSION);
 //		if(file.exists()){
 //			Bitmap bm = BitmapFactory.decodeFile(Constants.IMAGE_PATH + userName + Constants.IMAGE_EXTENSION);
 //			ivUserImage.setImageBitmap(bm);
 //			imageFound = true;
 //		}else{
 			if(user.isInContactDir()){
 				Contacts cont = new Contacts(context);
 				Bitmap image = cont.getPhoto(user.getContactId());
 				
 				try{
 					image.equals(null);
 					ivUserImage.setImageBitmap(image);
 					imageFound = true;
 				} catch (NullPointerException e){
 					ivUserImage.setImageResource(R.drawable.default_user_large);
 					ivUserImage.setBackgroundColor(context.getResources().getColor(R.color.medium_grey));
 				}
 				
 			} else {
 				ivUserImage.setImageResource(R.drawable.default_user_large);
 				ivUserImage.setBackgroundColor(context.getResources().getColor(R.color.medium_grey));
 			}
 //		}
 		
 		return imageFound;
 	}
 	
 	public static boolean setActionImage(Context context, ImageView ivUserImage, User user){
 		boolean imageFound = false;
 		String userName = user.getName();
 		if(user.isInContactDir()){
 			Contacts cont = new Contacts(context);
 			Bitmap image = cont.getPhoto(user.getContactId());
 			
 			try{
 				image.equals(null);
 				ivUserImage.setImageBitmap(image);
 				imageFound = true;
 			} catch (NullPointerException e){
 				ivUserImage.setImageResource(R.drawable.default_user_large);
 //				ivUserImage.setBackgroundColor(context.getResources().getColor(R.color.medium_grey));
 			}
 			
 		} else {
 			ivUserImage.setImageResource(R.drawable.default_user_large);
 //			ivUserImage.setBackgroundColor(context.getResources().getColor(R.color.medium_grey));
 		}
 		
 		return imageFound;
 	}
 	
 	public static void setGroupImage(Context context, ArrayList<User> users, LinearLayout groupImage){
 		ArrayList<ImageView> images = new ArrayList<ImageView>();
 		boolean odd = false;
 		int size = (int) groupImage.getLayoutParams().width/2;
 		int orientation = LinearLayout.HORIZONTAL;
 		
 		if(users.size() == 1){
 			size = size * 2;
 		} else {
 			if(users.size() == 2){
 				size = size * 2;
 				orientation = LinearLayout.VERTICAL;
 			} else {
 				if(users.size()%2 != 0){
 					odd = true;
 				}				
 			}
 		}
 		
 		LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(size, size);		
 		
 		
 		for(int i = 0; i < users.size(); i++){
 			User user = users.get(i);
 			
 			ImageView image = new ImageView(context);
 			
 			if(odd && i == users.size() - 1){
 				imageLayoutParams = new LinearLayout.LayoutParams(size * 2, size * 2);
 			}
 			
 			image.setLayoutParams(imageLayoutParams);
 			Gen.setUserImage(context, image, user);
 			
 			images.add(image);
 		}
 		
 		for(int i = 0; i < users.size(); i = i + 2){
 			LinearLayout horLayout = new LinearLayout(context);
 			horLayout.setOrientation(orientation);
 			
 			try{
 				horLayout.addView(images.get(i));
 			} catch (IndexOutOfBoundsException e) {
 				
 			}
 			
 			try{
 				horLayout.addView(images.get(i+1));
 			} catch (IndexOutOfBoundsException e) {
 				
 			}
 			groupImage.addView(horLayout);
 		}
 	}
 	
 	public static String getAmountText(Context context, float amount){
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		String symbol = "";
 		boolean positive;
 		
 		if(amount < 0){
 			positive = false;
 		} else {
 			positive = true;
 		}
 		
 		String number = Gen.getFormattedAmount(Math.abs(amount));
 		
 		if(prefs.getBoolean("allow_custom_currency_symbol", false)){
 			symbol = prefs.getString("custom_currency_symbol", "");
 		} else {
 			Locale locale=Locale.getDefault();
 	        Currency currency=Currency.getInstance(locale);
 	        symbol = currency.getSymbol();
 		}
 		
 		if(prefs.getBoolean("currency_symbol_right", false)){
 			number = number + symbol;
 		} else {
 			number = symbol + number;
 		}
         
         if(!positive){
         	number = "-" + number;	        	
         }
         
 		return number;
 	}
 	
 	public static String getFormattedAmount(float amount){
 		String number = Float.toString(amount);
 		int decPos = number.indexOf(".");
 		String dec = number.substring(decPos + 1);
 		
 		if(dec.length() == 0){
 			number = number + "00";
 		} else{
 			if(dec.length() == 1){
 				number = number + "0";
 			} else {
 				number = number.replace("." + dec, "");
 				dec = dec.substring(0, 2);
 				number = number + "." + dec;
 			}
 		}
 		
 		return number;
 	}
 	
 	public static void displayMinimalisticText(Context context, User user, Payment payment){
 		
 		if(user.isUsingMinimalisticText()){
 			float newBalance = user.getBalance();
 			if(payment.isToUser()){
 				newBalance = newBalance + payment.getAmount();
 			} else {
 				newBalance = newBalance - payment.getAmount();
 			}
 			
 			Gen.displayMinimalisticText(context, user, newBalance);
 		}
 	}
 	
 	public static void displayMinimalisticText(Context context, User user, float amount){
 		Gen.displayMinimalisticText(context, user.getVariableName(), amount);
 	}
 	
 	public static void displayMinimalisticText(Context context, String variableName, float amount){
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		
 		if(prefs.getBoolean("allow_minimalistic_text", false)){
 			MinimalisticText.SendTo(context, variableName, Gen.getAmountText(context, amount));
 			if(amount == 0){
 				MinimalisticText.SendTo(context, variableName + "SIGN", "0");
 			} else {
 				if(amount > 0){
 					MinimalisticText.SendTo(context, variableName + "SIGN", "0");
 				} else {
 					MinimalisticText.SendTo(context, variableName + "SIGN", "1");
 				}
 			}
 		}
 		
 	}
 
     public static void sortUser(ArrayList<User> tempArray, int sort){
     	Collections.sort(tempArray, new SortUser(sort));
     }
     
     public static String createEmailBody(Context context, User user){
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
     	String msg = prefs.getString("default_email_reminder_body", context.getString(R.string.default_email_reminder_body));
 		String username = user.getName();
 		String balance = user.getBalanceText();
 		
		msg = msg.replace("%USERNAME", username);
 		msg = msg.replace("%BALANCE", balance);
     	
     	return msg;
     }
     
     public static String createSms(Context context, User user){
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
     	String message = prefs.getString("default_sms_reminder_body", context.getString(R.string.default_sms_reminder_body));
 		String username = user.getName();
 		String balance = user.getBalanceText();
 		
		message = message.replace("%USERNAME", username);
 		message = message.replace("%BALANCE", balance);
 		
 		return message;
     }
 
     public static String getUserCsv(ArrayList<User> users){
     	String csv = "";
     	boolean first = true;
     	
     	for(int i = 0; i < users.size(); i++){
     		if(first){
     			csv += users.get(i).getName();
     			first = false;
     		} else {
     			csv += ", " + users.get(i).getName();
     		}
     	}
     	
     	return csv;
     }
     
     public static ArrayList<Payment> getUserPayments(Context context, Group group, ArrayList<GroupPayment> payments){
     	 ArrayList<Payment> tempPayments = new ArrayList<Payment>();
          
          for(int i = 0; i < group.getUsers().size(); i++){
          	Payment payment = new Payment(context);
          	
          	payment.setUser(group.getUser(i));
          	payment.setUserId(group.getUser(i).getId());
          	
          	tempPayments.add(payment);
          }
          
          for(int i = 0; i < payments.size(); i++){
          	GroupPayment payment = payments.get(i);
          	
          	for(int j = 0; j < payment.getSplits().size(); j++){
          		for(int k = 0; k < tempPayments.size(); k++){
  	        		if(payment.getSplit(j).getUserId() == tempPayments.get(k).getUserId()){
  	        			if(payment.getSplit(j).isPaying()){
  	        				tempPayments.get(k).setAmount(tempPayments.get(k).getAmount() +  payment.getSplit(j).getAmount());
  	        			} else {
  	        				tempPayments.get(k).setAmount(tempPayments.get(k).getAmount() -  payment.getSplit(j).getAmount());
  	        			}
  	        		}
          		}
          	}
          }
          
          return tempPayments;
     }
     
     public static ArrayList<GroupRepayment> sortGroupRepayments(Context context, ArrayList<Payment> userPayments){
     	ArrayList<GroupRepayment> repayments = new ArrayList<GroupRepayment>();
     	
     	ArrayList<Payment> owedPayments = new ArrayList<Payment>();
         ArrayList<Payment> owingPayments = new ArrayList<Payment>();
         
         for(int i = 0; i < userPayments.size(); i++){
         	if(userPayments.get(i).getAmount() != 0){
         		if(userPayments.get(i).getAmount() > 0){
             		owedPayments.add(userPayments.get(i));
             	} else {
             		userPayments.get(i).setAmount(Math.abs(userPayments.get(i).getAmount()));
             		owingPayments.add(userPayments.get(i));
             	}           		
         	}
         }
         
         for(int i = 0; i < owingPayments.size(); i++){
         	while(owingPayments.get(i).getAmount() > 0){
         		if(owingPayments.get(i).getAmount() >= owedPayments.get(0).getAmount()){
         			GroupRepayment repayment = new GroupRepayment(context);
         			
         			repayment.setAmount(owedPayments.get(0).getAmount());
         			repayment.setOwedUser(owedPayments.get(0).getUser());
         			repayment.setOwingUser(owingPayments.get(i).getUser());
         			
         			repayments.add(repayment);
         			
         			owingPayments.get(i).setAmount(owingPayments.get(i).getAmount() - owedPayments.get(0).getAmount());
         			owedPayments.remove(0);
         		} else {
         			
         			GroupRepayment repayment = new GroupRepayment(context);
         			
         			repayment.setAmount(owingPayments.get(i).getAmount());
         			repayment.setOwedUser(owedPayments.get(0).getUser());
         			repayment.setOwingUser(owingPayments.get(i).getUser());
         			
         			repayments.add(repayment);
         			
         			owedPayments.get(0).setAmount(owedPayments.get(0).getAmount() - owingPayments.get(i).getAmount());
         			owingPayments.get(i).setAmount(0);
         		}
         	}
         }
     	
     	return repayments;
     }
     
     public static float formatNumber(float number){
     	DecimalFormat df = new DecimalFormat("###.##");
     	
     	try{
 			number = Float.parseFloat(df.format(number));
 		} catch(NumberFormatException e){
 		}
 		
     	return number;
     }
 }
