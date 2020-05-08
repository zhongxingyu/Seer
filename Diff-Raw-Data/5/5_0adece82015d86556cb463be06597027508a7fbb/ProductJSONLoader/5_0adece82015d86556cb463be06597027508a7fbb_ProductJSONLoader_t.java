 package dk.tweenstyle.android.app.dao;
 
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.util.Log;
 import dk.tweenstyle.android.app.model.Gender;
 import dk.tweenstyle.android.app.model.Product;
 
 public class ProductJSONLoader implements JSONLoader<Product> {
 	
 	@Override
 	public Product loadObject(JSONObject object) {
 		Product product = new Product();
 		try {
 			product.setId(object.getString("id"));
 			String gender = object.getString("gender");
 			if (gender.startsWith("f")) {
 				product.setGender(Gender.FEMALE);
 			}
 			else if (gender.startsWith("m")) {
 				product.setGender(Gender.MALE);
 			}
 			else {
 				product.setGender(Gender.UNISEX);
 			}
 			product.setVariantId(object.getString("variantId"));
 			product.setBasePrice(object.getDouble("basePrice"));
			double currentPrice = object.optDouble("currentPrice");
			if(!Double.isNaN(currentPrice)){
				product.setCurrentPrice(currentPrice);
			}
 			product.setNumber(object.getString("number"));
 			product.setName(object.getString("name"));
 			product.setActive(object.getBoolean("isActive"));
 			product.setStock(object.getInt("stock"));
 			product.setShortDescription(object.getString("shortDescription"));
 			product.setLongDescription(object.getString("longDescription"));
 			//DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
 			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
 			product.setTimeCreated(format.parse(object.getString("timeCreated")));
 			product.setTimeUpdated(format.parse(object.getString("timeUpdated")));
 			product.setDefaultVariantCombination(object
 					.getString("defaultVariantCombination"));
 			product.setNew(object.getBoolean("isNew"));
 			product.setMinAge(object.getInt("minAge"));
 			product.setMaxAge(object.getInt("maxAge"));
 			product.setMinShoeSize(object.getInt("minShoeSize"));
 			product.setMaxShoeSize(object.getInt("maxShoeSize"));
 			product.setWebsite(object.getString("manufacturerWebsite"));
 			product.setLogo(object.getString("manufacturerLogo"));
 			product.setDescription(object.getString("manufacturerDescription"));
 			
 			JSONArray discounts = object.getJSONArray("discounts");
 			for (int i = 0, max = discounts.length(); i < max; i++) {
 				String discount = discounts.getString(i);
 				product.addDiscount(discount);
 			}
 			
 			JSONArray groups = object.getJSONArray("groups");
 			for (int i = 0, max = groups.length(); i < max; i++) {
 				String group = groups.getString(i);
 				product.addGpr(group);
 			}
 			
 			JSONArray variants = object.optJSONArray("variants");
 			if (variants != null) {
 				for (int i = 0, max = variants.length(); i < max; i++) {
 					JSONObject variant = variants.getJSONObject(i);
 					product.addProduct(this.loadObject(variant));
 				}
 			}
 		}
 		catch (Exception e) {
 			product = null;
 			Log.d("json", "Trouble loading Product object from JSON", e);
 		}
 		
 		return product;
 	}
 	
 }
