 package com.lake.tahoe.models;
 
import android.content.Intent;

 import com.google.android.gms.maps.model.LatLng;
 import com.lake.tahoe.callbacks.ModelCallback;
import com.lake.tahoe.callbacks.ModelFindCallback;
 import com.lake.tahoe.callbacks.ModelGetCallback;
import com.lake.tahoe.intents.Constants;
import com.parse.GetCallback;
 import com.parse.ParseClassName;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.paypal.android.sdk.payments.PayPalPayment;
 
 import java.math.BigDecimal;
 import java.text.NumberFormat;
 
 /**
  * Created by steffan on 10/21/13.
  */
 @ParseClassName("Request")
 public class Request extends ParseObject {
 
 	public enum State {
 
 		/**
 		 * Client creates a new Request
 		 */
 		OPEN,
 
 		/**
 		 * Vendor accepts Request
 		 */
 		ACTIVE,
 
 		/**
 		 * Vendor completes Request
 		 */
 		PENDING,
 
 		/**
 		 * Vendor receives Payment
 		 */
 		FULFILLED,
 
 		/**
 		 * Vendor cancels Payment
 		 */
 		CANCELLED
 
 	}
 
 	public Request() {
 		super();
 	}
 
 	public Request(State state) {
 		super();
 		setState(state);
 	}
 
 	public User getClient() {
 		return (User) getParseObject("client");
 	}
 
 	public void setClient(User client) {
 		put("client", client);
 	}
 
 	public User getVendor() {
 		return (User) getParseObject("vendor");
 	}
 
 	public void setVendor(User vendor) {
 		if (vendor == null) remove("vendor");
 		else put("vendor", vendor);
 	}
 
 	public String getTitle() {
 		return getString("title");
 	}
 
 	public void setTitle(String title) {
 		put("title", title);
 	}
 
 	public String getDescription() {
 		return getString("description");
 	}
 
 	public void setDescription(String description) {
 		put("description", description);
 	}
 
 	public int getCents() {
 		return getInt("cents");
 	}
 
 	public String getDisplayDollars() {
 		// TODO potentially move this helper to a separate currency class for reusability
 		return NumberFormat.getCurrencyInstance().format(getDollars());
 	}
 
 	public BigDecimal getDollars() {
 		return new BigDecimal(getCents() / 100.0);
 	}
 
 	public PayPalPayment getPaypalPayment() {
 		String currency = NumberFormat.getCurrencyInstance().getCurrency().getCurrencyCode();
 		return new PayPalPayment(getDollars(), currency, getTitle());
 	}
 
 	public void setCents(Integer cents) {
 		put("cents", cents);
 	}
 
 	public State getState() {
 		return State.valueOf(getString("state"));
 	}
 
 	public void setState(State state) {
 		put("state", state.toString());
 	}
 
 	public ParseGeoPoint getLocation() {
 		return getClient().getLocation();
 	}
 
 	public LatLng getGoogleMapsLocation() {
 		return getClient().getGoogleMapsLocation();
 	}
 
 	public static ParseQuery<Request> getRequestQuery() {
 		return ParseQuery.getQuery(Request.class);
 	}
 
 	public static void getByObjectId(String requestId, ModelCallback<Request> callback) {
 		ParseQuery<Request> query = Request.getRequestQuery();
 
 		query.whereEqualTo("objectId", requestId);
 		query.include("client");
 
 		query.getFirstInBackground(new ModelGetCallback<Request>(callback));
 	}
 }
