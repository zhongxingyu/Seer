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
 
 public class UITabBarController extends UIViewController {
 
 	private UIViewController[] viewControllers;
 	private UIView tabBar;
 	private UIView container;
 	
 	private int selectedIndex=-1;
 	private int previousSelectedIndex = -1;
 	
 	public UITabBarController(){
 		super();
 		//add two part of view					 
 		int tabBarHeight = 49;
 		CGRect frame = UIScreen.getMainScreen().getApplicationFrame();
 		container = new UIView(new CGRect(0,0,frame.size().width(),frame.size().height()-tabBarHeight));
 		this.view.addSubView(container);
 		
 		//default set tabBar 49
 		tabBar = new UIView(new CGRect(0,frame.size().height()-tabBarHeight,frame.size().width(),tabBarHeight));
 		tabBar.setBackgroundColor(UIColor.blackColor());
 		this.view.addSubView(tabBar);
 	}
 	
 	public UIViewController[] getViewControllers() {
 		return viewControllers;
 	}
 
 	public void setViewControllers(UIViewController[] viewControllers) {
 		if(viewControllers!=null&&viewControllers.length > 0){
 			this.viewControllers = viewControllers;
 			
 			for(int i = 0;i<viewControllers.length;i++){
 				UIViewController viewController = viewControllers[i];
 				viewController.setTabBarController(this);
 			}
 			this.loadViewController(0);
 			//default select the first one
 			//this.setSelectedIndex(0);
 			this.selectedIndex = 0;
 		}
 	}
 	private boolean loadViewController(int index){
 		if(index<this.viewControllers.length){
 			UIViewController viewController = viewControllers[index];
 			UIView view = viewController.getView();
 			if(view.superView()==null){
 				container.addSubView(view);
 			}
 			if(view.isHidden()){
 				view.setHidden(NO);
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	public int getSelectedIndex() {
 		return selectedIndex;
 	}
 
 	public void setSelectedIndex(int selectedIndex) {
 		if(this.viewControllers!=null&&this.loadViewController(selectedIndex)){
 			if(previousSelectedIndex!=-1){
				this.viewControllers[previousSelectedIndex].getView().setHidden(YES);
 			}
			previousSelectedIndex = this.selectedIndex;
 			this.selectedIndex = selectedIndex;
 		}
 	}
 	public UIView getTabBar() {
 		return tabBar;
 	}
 	
 	@Override
 	public boolean backKeyDidClicked(){
 		if(!super.backKeyDidClicked()){
 			return false;
 		}
 		//current node
 		if(this.viewControllers!=null&&this.viewControllers.length>0){
 			//current node
 			UIViewController viewController = this.viewControllers[this.selectedIndex];
 			if(!viewController.backKeyDidClicked()){
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
