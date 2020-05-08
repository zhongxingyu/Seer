 package com.nyt.activity;
 
 import rx.Observable;
 import rx.Subscriber;
 import rx.Subscription;
 import rx.android.schedulers.AndroidSchedulers;
 import rx.functions.Action1;
 import rx.functions.Func1;
 import rx.subscriptions.Subscriptions;
 import android.app.Activity;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.ImageView;
 import butterknife.ButterKnife;
 import butterknife.InjectView;
 
 import com.nyt.R;
 import com.nyt.network.rxjava.NetworkFacadeObservableImpl;
 
 public class RxJavaComplexTestActivity extends Activity {
 
 	@InjectView(R.id.ivResult)
 	ImageView ivResult;
 
 	private Observable<Drawable> drawables;
 	private Subscription subscription = Subscriptions.empty();
 
 	class MySubscriber extends Subscriber<Drawable> {
 
 		@Override
 		public void onCompleted() {
 		}
 
 		@Override
 		public void onError(Throwable e) {
 		}
 
 		@Override
 		public void onNext(Drawable result) {
 			ivResult.setImageDrawable(result);
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_simple);
 		ButterKnife.inject(this);
 
 		drawables = NetworkFacadeObservableImpl
 				.requestDrawable(R.drawable.smile)
 				.observeOn(AndroidSchedulers.mainThread()).cache()
 				.map(new Func1<Drawable, Drawable>() {
 
 					@Override
 					public Drawable call(Drawable firstImage) {
 
 						Observable<Drawable> secondDrawables = NetworkFacadeObservableImpl
 								.requestDrawable(R.drawable.smile1)
 								.observeOn(AndroidSchedulers.mainThread())
 								.cache().map(new Func1<Drawable, Drawable>() {
 
 									@Override
 									public Drawable call(Drawable secondImage) {
 
 										Observable<Drawable> thirdDrawables = NetworkFacadeObservableImpl
												.requestDrawable(
														R.drawable.smile2)
												.observeOn(
														AndroidSchedulers
																.mainThread())
 												.cache();
 										subscription = thirdDrawables
 												.subscribe(new MySubscriber());
 										return secondImage;
 									}
 								});
 						subscription = secondDrawables
 								.subscribe(new MySubscriber());
 						return firstImage;
 					}
 				});
 
 		subscription = drawables.subscribe(new Action1<Drawable>() {
 			@Override
 			public void call(Drawable result) {
 				ivResult.setImageDrawable(result);
 			}
 		});
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		subscription.unsubscribe();
 	}
 }
