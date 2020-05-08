 package org.gwtapp.ccalc.client.test;
 
 import java.util.Date;
 
 import org.gwtapp.ccalc.rpc.api.CCalcService;
 import org.gwtapp.ccalc.rpc.api.CCalcServiceAsync;
 import org.gwtapp.ccalc.rpc.data.book.Currency;
 import org.gwtapp.core.client.SimpleAsyncCallback;
 import org.junit.Test;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.junit.client.GWTTestCase;
 
 public abstract class AbstractGwtTestCCalcService extends GWTTestCase {
 
 	@Test
 	public void testRatioEURtoGBPon10092010() {
 		CCalcServiceAsync service = GWT.create(CCalcService.class);
 		service.getRatio(new Date(1284109200000L), Currency.EUR, Currency.GBP,
 				new SimpleAsyncCallback<Double>() {
 					public void onSuccess(Double result) {
						assertEquals(new Double(0.8266), result);
 						finishTest();
 					}
 
 					@Override
 					public void onFailure(Throwable e) {
 						e.printStackTrace();
 					}
 				});
		delayTestFinish(3500);
 	}
 }
