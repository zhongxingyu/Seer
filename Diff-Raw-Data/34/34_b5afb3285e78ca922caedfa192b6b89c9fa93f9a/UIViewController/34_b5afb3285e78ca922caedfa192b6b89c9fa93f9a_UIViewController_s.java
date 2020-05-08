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
 
 import org.cocoa4android.cg.CGRect;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.view.animation.Animation.AnimationListener;
 
 public class UIViewController extends UIResponder{
 	protected static boolean isTransition = false;
 	
 	protected UIView view;
 	
 	protected UIView otherViews;
 	
 	protected String title;
 	
 	
 	private boolean isPresent;
 	
 	
 	
 	public UIView view() {
 		return view;
 	}
 
 	public void setView(UIView view) {
 		this.view = view;
 		if(view!=null){
 			this.view.getView().postDelayed(new Runnable(){
 	
 				@Override
 				public void run() {
 					UIViewController.this.viewDidLoad();
 				}
 			},200);
 		}
 	}
 	protected void viewDidLoad(){
 		
 	}
 	protected void viewDidUnload(){
 		
 	}
 	protected void viewDidAppear(boolean animated){
 		
 	}
 	
 	
 	protected int viewid;
 	protected LayoutInflater inflater;
 	
 	/**
 	 * if not argument create empty view
 	 */
 	public UIViewController(){
 		this.setView(new UIView());
 	}
 	public UIViewController(int viewid){
 		this.viewid = viewid;
 		inflater = LayoutInflater.from(UIApplication.sharedApplication().getContext());  
 		View v =  inflater.inflate(viewid, null);
 		this.setView(new UIView(v));
 	}
 	public UIViewController(String xib){
 		
 	}
 
 
 	public UINavigationController navigationController() {
 		return navigationController;
 	}
 
 	public void setNavigationController(UINavigationController navigationController) {
 		this.navigationController = navigationController;
 		this.setParentViewController(navigationController);
 	}
 
 
 	public UIViewController parentViewController() {
 		return parentViewController;
 	}
 
 	public void setParentViewController(UIViewController parentViewController) {
 		this.parentViewController = parentViewController;
 	}
 
 
 	private UITabBarItem tabBarItem = new UITabBarItem("Label",null,0);
 	
 	protected void setTabBarItem(UITabBarItem tabBarItem){
 		this.tabBarItem = tabBarItem;
 		if (this.tabBarController!=null) {
 			this.tabBarController.setTabBarItem(tabBarItem, this);
 		}
 	}
 	public UITabBarItem tabBarItem() {
 		return this.tabBarItem;
 	}
 	
 	public UITabBarController tabBarController() {
 		return tabBarController;
 	}
 
 	public void setTabBarController(UITabBarController tabBarController) {
 		this.tabBarController = tabBarController;
 		this.setParentViewController(tabBarController);
 	}
 
 
 
 	protected UITabBarController tabBarController;
 	protected UINavigationController navigationController;
 	protected UIViewController parentViewController;
 	
 	protected UIViewController presentingViewController;
 	
 	protected UIViewController presentedViewController;
 	
 	public void presentModalViewController(UIViewController viewController,boolean animated){
 		if (!isTransition) {
 			isTransition = YES;
 			UIView modalView = viewController.view();
			modalView.setFrame(UIScreen.mainScreen().applicationFrame());
 			modalView.setBackgroundColor(UIColor.whiteColor());
 			UIApplication.sharedApplication().getWindow().addSubview(modalView);
 			this.setPresentedViewController(viewController);
 			
 			if(animated){
 				this.translateBetweenViews(viewController.view(), true);
 			}else{
 				this.view().setHidden(true);
 				viewController.viewDidAppear(NO);
 				isTransition = NO;
 			}
 			viewController.setParentViewController(this);
 			
 		}
 		
 	}
 	public void dismissModalViewController(boolean animated){
 		this.dismissModalViewController(animated, true);
 	}
 	protected void dismissModalViewController(boolean animated,boolean checkParent){
 		if (!isTransition) {
 			isTransition = YES;
 			if(this.presentedViewController()!=null){
 				this.view().setHidden(false);
 				if(animated){
 					this.translateBetweenViews(this.presentedViewController().view(), false);
 				}else{
 					this.presentedViewController().view().removeFromSuperView();
 					this.presentedViewController().viewDidUnload();
 					this.setPresentedViewController(null);
 					this.viewDidAppear(NO);
 					isTransition = NO;
 				}
 			}else if(checkParent&&this.parentViewController()!=null){
 				isTransition = NO;
 				this.parentViewController().dismissModalViewController(animated,false);
 			}
 		}
 		
 	}
 	private void translateBetweenViews(UIView modalView ,boolean isPresent){
 		//ʾ
 		this.isPresent = isPresent;
 		
 		
 		CGRect applicationFrame = UIScreen.mainScreen().applicationFrame;
 		float applicationheight = applicationFrame.size().height();
 		TranslateAnimation animation = null;
 		if(isPresent){
 			animation = new TranslateAnimation(0,0,applicationheight,0);
 		}else{
 			animation = new TranslateAnimation(0,0,0,applicationheight);
 		}
 		
 		
 		
 		animation.setDuration(500);
 		modalView.getView().startAnimation(animation);
 		animation.startNow();
 		
 		
 		animation.setAnimationListener(new AnimationListener(){
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				//HIDE
 				//UINavigationController.this.getView().setHidden(true);
 				if(UIViewController.this.isPresent){
 					UIViewController.this.view().setHidden(true);
 					UIViewController.this.presentedViewController.viewDidAppear(YES);
 				}else{
 					UIViewController.this.presentedViewController().view().removeFromSuperView();
 					UIViewController.this.presentedViewController().viewDidUnload();
 					UIViewController.this.setPresentedViewController(null);
 					UIViewController.this.viewDidAppear(YES);
 					
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
 
 	public UIViewController presentedViewController() {
 		return presentedViewController;
 	}
 
 	public void setPresentedViewController(UIViewController presentedViewController) {
 		if(this.presentedViewController!=null){
 			this.presentedViewController.setPresentingViewController(null);
 		}
 		this.presentedViewController = presentedViewController;
 		if(presentedViewController!=null){
 			presentedViewController.setPresentingViewController(this);
 		}
 	}
 
 	public UIViewController presentingViewController() {
 		return presentingViewController;
 	}
 
 
 	public void setPresentingViewController(UIViewController presentingViewController) {
 		this.presentingViewController = presentingViewController;
 	}
 	
 	public boolean onBackPressed(){
 		if(this.presentedViewController!=null){
 			this.dismissModalViewController(true);
 			return YES;
 		}
 		return NO;
 	}
 
 	public String title() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 		if (this.navigationController!=null) {
 			this.navigationController.setTitle(title);
 		}
 	}
 }
