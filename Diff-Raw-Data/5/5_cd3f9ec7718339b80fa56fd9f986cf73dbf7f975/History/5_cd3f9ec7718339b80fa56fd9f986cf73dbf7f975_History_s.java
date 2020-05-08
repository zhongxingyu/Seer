 package org.immopoly.common;
 
 import java.text.DecimalFormat;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public abstract class History implements JSONable {
 	public static DecimalFormat MONEYFORMAT = new DecimalFormat("0.00 Eur");
 	public static final int TYPE_EXPOSE_ADDED=1;
 	public static final int TYPE_EXPOSE_SOLD=2;
 	public static final int TYPE_EXPOSE_MONOPOLY_POSITIVE=3;
 	public static final int TYPE_EXPOSE_MONOPOLY_NEGATIVE=4;
 	//history2
 	public static final int TYPE_DAILY_PROVISION=5;
 	public static final int TYPE_DAILY_RENT=6;
 	// new
 	public static final int TYPE_EXPOSE_REMOVED = 7;
 
 	
 	public abstract void setTime(long time);
 	public abstract void setText(String text);
 	public abstract void setType(int type);
 	public abstract void setType2(int type2);
 	public abstract void setAmount(double amount);

 	public abstract void setExposeId(Long exposeId);
 
 	@Override
 	public void fromJSON(JSONObject o) {
 		try {
 			JSONObject h = o.getJSONObject("org.immopoly.common.History");
 			setTime(h.getLong("time"));
 			setText(h.getString("text"));
 			setType(h.getInt("type"));
 			if(h.has("type2"))
 				setType2(h.getInt("type2"));
 			if(h.has("amount"))
 				setAmount(h.getDouble("amount"));
 			if (h.has("exposeId"))
				setAmount(h.getLong("exposeId"));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 }
