 /*
  * Copyright (C) 2012 Wu Tong
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.cocoa4android.ui;
 
 import java.util.Stack;
 
 import org.cocoa4android.R;
 import org.cocoa4android.cg.CGPoint;
 import org.cocoa4android.cg.CGRect;
 import org.cocoa4android.ns.NSTextAlignment;
 import org.cocoa4android.ui.UIControl.UIControlEvent;
 
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Shader.TileMode;
 import android.graphics.drawable.BitmapDrawable;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.TranslateAnimation;
 
 public class UINavigationController extends UIViewController {
 	
 	
 	private boolean navigationBarHidden;
 	private UIView contentView;
 	//================================================================================
     // Constructor
     //================================================================================
 	public UINavigationController(){
 		super();
 		appFrame = UIScreen.mainScreen().applicationFrame();
 		navigationBarHeight = appFrame.size.height/11.0f;
 		contentView = new UIView(CGRectMake(0, navigationBarHeight, appFrame.size.width, appFrame.size.height-navigationBarHeight));
 		this.view.addSubview(contentView);
 		this.initNavigationBar();
 	}
 	
 	//================================================================================
     // NavigationBar
     //================================================================================
 	private UIView navigationBar;
 	private float navigationBarHeight = 1.0f;
 	private UILabel titleLabel = null;
 	private UIButton backButton = null;
 	private CGRect appFrame = null;
 	private void initNavigationBar() {
 		navigationBar = new UIView(CGRectMake(0, 0, appFrame.size.width, navigationBarHeight));
 		
 		Bitmap bitmap = BitmapFactory.decodeResource(UIApplication.sharedApplication().getActivity().getResources(), R.drawable.zz_c4a_navigationbar_background_default);  
 		BitmapDrawable bd = new BitmapDrawable(bitmap);  
 		bd.setTileModeX(TileMode.REPEAT);
 		bd.setDither(true);  
 		navigationBar.setBackgroundImage(new UIImage(bd.getBitmap()));
 		this.view.addSubview(navigationBar);
 		
 		int halfWidth = ((int)appFrame.size.width)>>1;
 		titleLabel = new UILabel(CGRectMake(halfWidth>>1, 0, halfWidth, navigationBarHeight));
 		titleLabel.setTextAlignment(NSTextAlignment.NSTextAlignmentCenter);
 		titleLabel.setTextColor(UIColor.whiteColor());
 		titleLabel.setFontSize(14);
 		titleLabel.getLabel().setShadowLayer(0.4f, 0, -1, 0x55000000);
 		navigationBar.addSubview(titleLabel);
 	}
 	
 	public UIView navigationBar(){
 		return this.navigationBar;
 	}
 	
 	//FIXME no animation on buttons and labels
 	private void invalidateBackButton(){
 		if (stack.size()>1&&!navigationBarHidden) {
 			if (backButton==null) {
				int backWidth = (int)(appFrame.size.width)>>4;
				int backHeight = (int) (backWidth*1.3f);
 				backButton = new UIButton(CGRectMake(0, 0, backWidth, backHeight));
 				backButton.setKeepAspectRatio(YES);
 				backButton.setAutoHighlight(YES);
 				backButton.setImage(UIImage.imageNamed(R.drawable.zz_c4a_navigationbar_back));
 				backButton.addTarget(this, selector("popViewController"), UIControlEvent.UIControlEventTouchUpInside);
 				backButton.setCenter(CGPointMake(appFrame.size.width/12.0f,((int)navigationBarHeight)>>1));
 				backButton.titleLabel().setTextAlignment(NSTextAlignment.NSTextAlignmentCenter);
 				backButton.titleLabel().setFontSize(7);
 				backButton.titleLabel().getView().setPadding(((int)appFrame.size.width)>>5, backHeight>>5, 0, 0);
 				backButton.titleLabel().getLabel().setShadowLayer(0.4f, 0, -2, 0x55000000);
 				backButton.titleLabel().setNumberOfLines(1);
 				this.navigationBar.addSubview(backButton);
 			}
 			backButton.setHidden(NO);
 			/*
 			String title = stack.get(stack.size()-2).title();
 			if (title==null) {
 				backButton.setTitle("");
 			}else{
 				backButton.setTitle(title);
 			}
 			*/
 		}else{
 			if (backButton!=null) {
 				backButton.setHidden(YES);
 			}
 		}
 		titleLabel.setText(toViewController.title());
 	}
 	public void setTitle(String title){
 		titleLabel.setText(title);
 	}
 	public boolean isNavigationBarHidden() {
 		return navigationBarHidden;
 	}
 	public void setNavigationBarHidden(boolean navigationBarHidden) {
 		this.navigationBarHidden = navigationBarHidden;
 		navigationBar.setHidden(navigationBarHidden);
 		if (navigationBarHidden) {
 			contentView.setFrame(UIScreen.mainScreen().applicationFrame());
 		}
 	}
 	
 	
 	//================================================================================
     // Push&Pop
     //================================================================================
 	private Stack<UIViewController> stack = new Stack<UIViewController>();
 	private UIView fromView;
 	private UIViewController toViewController;
 	private UIViewController fromViewController;
 	private boolean isPush;
 	public void pushViewController(UIViewController viewController,boolean animated){
 		if (!isTransition) {
 			isTransition = YES;
 			UIView lastView = null;
 			if(stack.size()>0){
 				lastView =stack.peek().view();
 			}
 			viewController.setNavigationController(this);
 			UIView view = viewController.view();
 			this.contentView.addSubview(view);
 			
 			if(animated&&lastView!=null){
 				this.translateBetweenViews(lastView, view,true);
 			}else{
 				if(lastView!=null){
 					lastView.setHidden(true);
 				}
 				viewController.viewDidAppear(NO);
 				isTransition = NO;
 			}
 			toViewController = viewController;
 			stack.push(viewController);
 		}
 		this.invalidateBackButton();
 	}
 	public void popViewController(){
 		this.popViewController(YES);
 	}
 	public void popViewController(boolean animated){
 		if (!isTransition) {
 			isTransition = YES;
 			if(stack.size()>1){
 				fromViewController =  stack.pop();
 				toViewController = stack.peek();
 				if(animated){
 					this.translateBetweenViews(fromViewController.view(), toViewController.view(),false);
 				}else{
 					CGRect frame = UIScreen.mainScreen().applicationFrame();
 					toViewController.view().setFrame(new CGRect(0,0,frame.size().width(),frame.size().height()));
 					toViewController.view().setHidden(false);
 					
 					toViewController.viewDidAppear(NO);
 					isTransition = NO;
 					
 					fromViewController.view().removeFromSuperView();
 					fromViewController.viewDidUnload();
 				}
 			}
 			this.invalidateBackButton();
 		}
 		
 		
 	}
 	
 	public void popToRootViewController(boolean animated){
 		//TODO popToRootViewController
 		this.invalidateBackButton();
 	}
 	public void popToViewController(UIViewController viewController,boolean animated){
 		//TODO popToViewController
 		this.invalidateBackButton();
 	}
 	private void translateBetweenViews(UIView fromView ,UIView toView ,boolean isPush){
 		//show
 		toView.setHidden(false);
 		
 		this.fromView = fromView;
 		this.isPush = isPush;
 		
 		//get the real application frame
 		CGRect applicationFrame = UIScreen.mainScreen().applicationFrame;
 		float applicationWidth = applicationFrame.size().width();
 		TranslateAnimation animation1 = null;
 		TranslateAnimation animation2 = null;
 		if(isPush){
 			animation1 = new TranslateAnimation(0,-applicationWidth,0,0);
 			animation2 = new TranslateAnimation(applicationWidth,0,0,0);
 		}else{
 			animation1 = new TranslateAnimation(0,applicationWidth,0,0);
 			animation2 = new TranslateAnimation(-applicationWidth,0,0,0);
 		}
 		
 		
 		animation1.setDuration(400);
 		fromView.getView().startAnimation(animation1);
 		animation1.startNow();
 		
 		animation2.setDuration(400);
 		toView.getView().startAnimation(animation2);
 		animation2.startNow();
 		
 		
 		animation1.setAnimationListener(new AnimationListener(){
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				//HIDE
 				if(UINavigationController.this.isPush){
 					UINavigationController.this.fromView.setHidden(true);
 				}else{
 					UINavigationController.this.fromView.removeFromSuperView();
 					UINavigationController.this.fromViewController.viewDidUnload();
 				}
 				if (UINavigationController.this.toViewController!=null) {
 					UINavigationController.this.toViewController.viewDidAppear(YES);
 					UINavigationController.this.toViewController = null;
 				}
 				isTransition = NO;
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				
 			}
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				
 			}
 			
 		});
 	}
 	
 	//================================================================================
     // backKey
     //================================================================================
 	@Override
 	public boolean onBackPressed(){
 		if (super.onBackPressed()) {
 			return YES;
 		}
 		if(stack.size()>1){
 			this.popViewController(true);
 			return YES;
 		}else if(stack.size()==1){
 			UIViewController currentViewController = this.stack.firstElement();
 			return currentViewController.onBackPressed();
 		}
 		return NO;
 	}
 }
