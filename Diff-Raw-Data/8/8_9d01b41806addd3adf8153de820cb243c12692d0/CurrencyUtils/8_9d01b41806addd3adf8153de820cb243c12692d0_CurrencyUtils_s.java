 package com.dzebsu.acctrip.currency.utils;
 
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.dzebsu.acctrip.models.CurrencyPair;
 import com.dzebsu.acctrip.models.Operation;
 
 public class CurrencyUtils {
 
 	public static final int ACCURACY_IS_IMPORTANT = 2;
 
 	public static final int ACCURACY_IS_NOT_IMPORTANT = 2;
 
 	// TODO remove all E!! and provide different length after
 	// point
 
 	public static String formatDecimalImportant(double value) {
 		return formatAfterPoint(value, ACCURACY_IS_IMPORTANT, false);
 	}
 
 	public static String formatDecimalNotImportant(double value) {
 		return formatAfterPoint(value, ACCURACY_IS_NOT_IMPORTANT, true);
 	}
 
 	public static String formatAfterPoint(double value, int decimalDigits, boolean separator) {
 		char[] digitsAfterPoint = new char[decimalDigits];
 		Arrays.fill(digitsAfterPoint, '#');
 		String beforePoint = separator ? "###,###,###." : "##.";
 		DecimalFormat money = new DecimalFormat(beforePoint + String.valueOf(digitsAfterPoint));
 		money.setRoundingMode(RoundingMode.UP);
 		return money.format(value);
 	}
 
 	public static double getDouble(String s) {
		if (s.isEmpty())
 			return 0.;
		else return Double.parseDouble(s);
 	}
 
 	public static boolean isPrimaryLeftOrientation(double rate) {
 		return rate < (1 / rate) ? false : true;
 	}
 
 	public static List<CurrencyPair> autoEvolveRates(long primaryCurrencyIdNow, Map<Long, CurrencyPair> cps) {
 		List<CurrencyPair> cpList = new ArrayList<CurrencyPair>(cps.size());
 		Iterator<CurrencyPair> it = cps.values().iterator();
 		CurrencyPair cp;
 		double newRate = 1 / cps.get(primaryCurrencyIdNow).getRate();
 		while (it.hasNext()) {
 			cp = it.next();
 			cp.setRate(cp.getRate() * newRate);
 			cpList.add(cp);
 		}
 
 		return cpList;
 	}
 
 	public static double getTotalEventExpenses(List<Operation> operations, Map<Long, CurrencyPair> currencyPairs) {
 		double sum = 0.;
 		for (Operation op : operations) {
 			sum += op.getValue() / currencyPairs.get(op.getCurrency().getId()).getRate();
 		}
 		return sum;
 	}
 
 	public static double getOperationExpensesInPrimary(Operation op, Map<Long, CurrencyPair> currencyPairRates) {
 
 		return op.getValue() / currencyPairRates.get(op.getCurrency().getId()).getRate();
 	}
 }
