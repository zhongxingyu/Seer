 package org.i5y.json.workshop.currency;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.json.Json;
 import javax.json.JsonArrayBuilder;
 import javax.json.JsonNumber;
 import javax.json.JsonObject;
 import javax.json.JsonObjectBuilder;
 import javax.json.JsonReader;
 import javax.json.JsonValue;
 
 public class Rates {
 
 	public static class RateNotPresentException extends RuntimeException {
 		private static final long serialVersionUID = 1L;
 	}
 
 	/**
 	 * Get an exchange rate from USD to the provided currency on a certain date
 	 * 
 	 * @param currency
 	 * @param date
 	 *            formatted e.g. 2012-12-01
 	 * @return
 	 * @throws RateNotPresentException
 	 *             if the rate is not available for the currency/date pair
 	 */
 	public static double getRate(String currency, String date) {
 		JsonObject obj = createJsonReader(date).readObject();
 		return getRate(obj, currency);
 	}
 
 	private static double getRate(JsonObject jsonDoc, String currency) {
 		JsonObject rates = jsonDoc.getValue("rates", JsonObject.class);
 		if (rates != null) {
 			JsonValue jn = rates.get(currency);
 			if (jn instanceof JsonNumber) {
 				return ((JsonNumber) jn).getDoubleValue();
 			} else {
 				throw new RateNotPresentException();
 			}
 		} else {
 			throw new RateNotPresentException();
 		}
 	}
 
 	/**
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static Collection<String> findBaseEquivalentCurrencies(String date) {
 		JsonObject obj = createJsonReader(date).readObject();
 		JsonObject rates = obj.getValue("rates", JsonObject.class);
 		Collection<String> result = new HashSet<>();
 		String baseCurrency = obj.getStringValue("base", "USD");
 		if (rates != null) {
 			for (Entry<String, JsonValue> entry : rates.entrySet()) {
 				if (entry.getValue() instanceof JsonNumber) {
 					if (((JsonNumber) entry.getValue()).getDoubleValue() == 1.0) {
 						if (!entry.getKey().equals(baseCurrency)) {
 							result.add(entry.getKey());
 						}
 					}
 				}
 			}
 		} else {
 			throw new RateNotPresentException();
 		}
 		return result;
 	}
 
 	/**
 	 * Generates a JSON report for a specific currency for a week from the
 	 * provided date in the format:
 	 * 
 	 * { currency : "USD", rates: [ { timestamp :"Sat Feb 01 23:00:09 GMT 2013",
 	 * value: 1.x}, ...] }
 	 * 
 	 * @param currency
 	 * @param date
 	 * @return
 	 */
 	public static JsonObject generateWeekReport(String currency,
 			String startDate) {
 		String[] segments = startDate.split("-");
 		int day = Integer.parseInt(segments[2]);
 
 		List<String> dates = new ArrayList<>();
 		for (int i = 0; i < 7; i++) {
 			String dayStr = Integer.toString(day + i);
 			if (dayStr.length() == 1)
 				dayStr = "0" + dayStr;
 			dates.add(segments[0] + "-" + segments[1] + "-" + dayStr);
 		}
 
 		JsonArrayBuilder ratesArrayBuilder = Json.createArrayBuilder();
 
 		for (String date : dates) {
 			JsonObject obj = createJsonReader(date).readObject();
 			JsonNumber jn = obj.getValue("timestamp", JsonNumber.class);
 			long time = jn.getLongValue();
 			double rate = getRate(obj, currency);
 			ratesArrayBuilder.add(Json.createObjectBuilder()
 					.add("timestamp", new Date(time * 1000l).toString())
 					.add("rate", rate));
 		}
 
 		JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
 				.add("currency", currency).add("rates", ratesArrayBuilder);
 
 		return objectBuilder.build();
 	}
 
 	/**
 	 * Return a JsonReader object which will read the document for the
 	 * appropriate date
 	 * 
 	 * @param date
 	 * @return
 	 */
 	private static JsonReader createJsonReader(String date) {
 		InputStream is = Rates.class.getResourceAsStream("/" + date + ".json");
 		return Json.createReader(is);
 	}
 }
