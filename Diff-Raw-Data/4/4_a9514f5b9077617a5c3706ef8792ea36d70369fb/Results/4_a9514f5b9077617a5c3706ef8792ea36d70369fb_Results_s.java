 package com.example.proceeds;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import java.util.Calendar;
 import java.util.Date;
 
 public class Results extends Activity {
 
 	/* Called when the activity is first created. */
 
 	@Override
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.results);
 		Bundle extras = getIntent().getExtras();
 		if (extras == null) {
 			return;
 		}
 		
 		//Calc & set commission
 		float commissionRate = extras.getFloat("commissionRate");
 		float sellingPrice = extras.getFloat("sellingPrice");
 		float commission = commissionRate * sellingPrice;
 		TextView text1 = (TextView) findViewById(R.id.res_commission_decimal);
 		commission = roundMoney(commission);
 		final String commissionOut = Float.toString(commission);
 		text1.setText(commissionOut);
 		
 		//Calc & set conveyance fee
 		/* Easy example */ 
 		String county = extras.getString("county");
 			TextView conveyance= (TextView) findViewById(R.id.res_conveyance_fee_decimal);
 		float countyRate = getCountyRate(county);
 		float conveyanceFee = getConveyanceFee(countyRate, sellingPrice);
 		conveyanceFee = roundMoney(conveyanceFee);
 		conveyance.setText(Float.toString(conveyanceFee));
 		
 		//Calc & set title premium
 		TextView homeTitle = (TextView) findViewById(R.id.res_homeowner_title_decimal);
 		float titleCost = getTitleCost(sellingPrice);
 		titleCost = roundMoney(titleCost);
 		homeTitle.setText(Float.toString(titleCost));
 		
 		// Calc & set prorated taxes
 		TextView proTax = (TextView) findViewById(R.id.res_prorated_taxes_decimal);
 		//float proratedTax = getProratedTax(annualTax, paidThrough,);
 		String paidThrough = extras.getString("paidThrough");
 		float taxRate = extras.getFloat("annualTax");
 		int year = extras.getInt("year");
 		int month = extras.getInt("month");
 		int day = extras.getInt("day");
 		int daysBetween = getDaysBetween(paidThrough, year, month, day);
 		float taxAmt = getTaxAmt(daysBetween, taxRate);
 		proTax.setText(Float.toString(taxAmt));
 		
 		// Calc & set hoa fees
 		// Check to see that sale day is allocated to seller
 		String hoaFreq = extras.getString("hoaFreq");
 		float hoaFee = extras.getFloat("hoaFee");
 		TextView proHoa = (TextView) findViewById(R.id.res_prorated_hoa_decimal);
 		float feeAmount = getFeeAmount(hoaFreq, hoaFee, year, month, day);
 		System.out.println("Fee amt is " + feeAmount);
 		proHoa.setText(Float.toString(feeAmount));
 	
 		float firstMortgage = extras.getFloat("firstMortgage");
 		float secondMortgage = extras.getFloat("secondMortgage");
 		float otherLiens = extras.getFloat("otherLiens");
 		float otherRealtor = extras.getFloat("otherRealtor");
 		float gasLine = extras.getFloat("gasLine");
 		float homeWarranty = extras.getFloat("homeWarranty");
 		float sellerConcessions = extras.getFloat("sellerConcessions");
 		
 		/* Not worth grabbing these from config for now */
 		float shipping = (float) 25.0;
 		float settlement = (float) 50.0;
 		float titleExam = (float) 185.0;
 		float titleInsurance = (float) 50.0;
 		float deedPrep = (float) 50.0;
 		
 		float netToSeller = sellingPrice - commission - conveyanceFee - titleCost
 				- taxAmt - (-1 * feeAmount) - firstMortgage - secondMortgage
 				- otherLiens - otherRealtor - gasLine - homeWarranty - 
 				sellerConcessions - shipping - settlement - titleExam -
 				titleInsurance - deedPrep;
 		
 		TextView netSell = (TextView) findViewById(R.id.res_netSeller_decmial);
 		netSell.setText(Float.toString(netToSeller));
 		
 
 	}
 	
 	public float getCountyRate(String county){
 		/* Get the county rates, as per the email sent to me */
         /* Not worth putting these in config file for now */
 		float rate = (float) 0.0;
 		if (county.equals("Delaware")){
 			rate = (float) 0.30;
 		}
 		else if (county.equals("Franklin") || county.equals("Licking") || county.equals("Union")){
 			rate = (float) 0.20;
 		}
 		else if (county.equals("Fairfield") || county.equals("Other")){
 			rate = (float) 0.40;
 		}
 		return rate;
 			
 	}
 	
 	public float getConveyanceFee(float countyRate, float sellingPrice){
 		/* For every hundred of sale price, charge the county rate */
 		float tempPrice = Math.round(sellingPrice / 100) * 100;
 		if (sellingPrice > tempPrice){
 			tempPrice = tempPrice + 100;
 		}
 		tempPrice = tempPrice / 100;
 		return countyRate * tempPrice;
 		
 	}
 	
 	/* Could easily just do a round to 10^n function here instead */
 	public float roundThou(float input){
 		/* round to the nearest thousand and always up */
 		float temp = Math.round(input / 1000) * 1000;
 		if (input > temp){
 			temp = temp + 1000;
 		}
 		return temp;
 	}
 	
 	public float roundHun(float input){
 		/* round to nearest hundred and always up */
 		float temp = Math.round(input / 100) * 100;
 		if (input > temp){
 			temp = temp + 100;
 		}
 		return temp;
 	}
 	
 	public float getTitleCost(float sellingPrice){
 		
 		float price = (float) 0.0;
 		float sellPrice = roundThou(sellingPrice) / 1000;
 		float baseRate = (float) 6.6125;
 		float secondRate = (float) 5.175;
 		float thirdRate = (float) 4.025;
 		float fourthRate = (float) 3.1625;
 		float fifthRate = (float) 2.5875;
 
 		// If under 150k, simple and return
 		if (sellPrice <= 150.0){
			price = (float) (baseRate * sellingPrice);
 			return price;
 		}
 		
 		// If > 150k, just add that to price
 		if (sellPrice > 150.0){
 			price = (float) (baseRate * 150.0);
 		}
 		// Get price if in the middle of the next bracket
 		if (sellPrice > 150 && sellPrice < 250){
 			price += (float) (secondRate * (sellPrice - 150));
 		}
 		// Get price if at or beyond the bracket
 		else if (sellPrice >= 250){ 
 			price += (float) (secondRate * 100.0);
 		}
 		// So on...
 		if (sellPrice < 500 && sellPrice > 250){
 			price += (float) (thirdRate * (sellPrice - 250));
 		}
 		else if (sellPrice >= 500){
 			price += (float) (thirdRate * 250);
 		}
 		if (sellPrice > 500 && sellPrice < 1000){
 			price += (float) (fourthRate * (sellPrice - 500));
 		}
 		else if (sellPrice >= 1000){
 			price += (float) (fourthRate * 9500);
 		}
 		if (sellPrice > 1000){
 			price += (float) (fifthRate * (sellPrice - 1000));
 		}
 		return price;
 	}
 
     public float roundMoney(float input){
     	input = input * 100;
     	input = Math.round(input);
     	input = input / 100;
     	return input;
     }
 
     public int getDaysBetween(String paidThrough, int year, int month, int day){
     	String[] temp = paidThrough.split(" ");
     	int startYear = Integer.valueOf(temp[2]);
     	int startMonth = 1;
     	int startDay = 1;
     	
     	//July 2nd is the 183 day, there are 182 days on each side.
     	// What to use if paid through first half of year
     	if (temp[0].equals("First")){
     		startMonth = 6; // 6 since Calendar is 0 indexed
     		startDay = 2;
     	}
     	// Dec 31 is final day in year
     	else{
     		startDay = 31;
     		startMonth = 11; // Month is 11 since Calendar is 0 indexed
     	}
     	
     	// Get difference between two dates
     	Calendar first = Calendar.getInstance();
     	Calendar second = Calendar.getInstance();
     	first.set(year, month, day);
     	second.set(startYear, startMonth, startDay);
     	int between = daysBetween(second, first);
     	return between;
     }
 
     public float getTaxAmt(int daysBetween, float taxRate){
     	return (float) (daysBetween * (taxRate / 365.0));
     }
     
     public float getFeeAmount(String hoaFreq, float hoaAmount, int year, int month, int day){
     	Calendar sellDate = Calendar.getInstance();
     	Calendar startDate = Calendar.getInstance();
     	sellDate.set(year, month, day);
     	
     	//Start from 1st of month
     	if (hoaFreq.equals("Monthly")){
     		startDate.set(year, month, 1);
     	}
     	// Start from 1st of Year
     	if (hoaFreq.equals("Yearly")){
     		startDate.set(year, 0, 1);
     	}
     	
     	int days = daysBetween(startDate, sellDate);
     	System.out.println("between is " + days);
     	
     	float rate = (float) 0.0;
     	if (hoaFreq.equals("Monthly")){
     		System.out.println("days in month is " + sellDate.getActualMaximum(sellDate.DAY_OF_MONTH));
     		rate = (float) (hoaAmount / sellDate.getActualMaximum(sellDate.DAY_OF_MONTH));
     		return (float) (hoaAmount - (days * rate));
     	}
     	else{
     		rate = (float) (hoaAmount / 365.0);
     		// Add the + 1 as it should compensate for the middle day of the year
     		return (float) ((days + 1) * rate);
     	}
     	
     }
     
     public static int daysBetween(Calendar startDate, Calendar endDate) {  
     	  Calendar date = (Calendar) startDate.clone();  
     	  long daysBetween = 0;  
     	  while (date.before(endDate)) {  
     	    date.add(Calendar.DAY_OF_MONTH, 1);  
     	    daysBetween++;  
     	  }  
     	  return (int) daysBetween;
     }
 
 
 };
