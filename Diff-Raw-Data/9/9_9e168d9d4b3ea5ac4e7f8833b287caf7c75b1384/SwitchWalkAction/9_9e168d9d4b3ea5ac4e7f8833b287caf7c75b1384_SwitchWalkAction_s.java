 /*
 
  * Copyright 2012 Anthony Cassidy
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.github.a2g.core.action;
 
 import com.github.a2g.core.action.BaseAction;
 import com.github.a2g.core.interfaces.ConstantsForAPI.Special;
 import com.github.a2g.core.interfaces.IDialogTreePresenterFromActions;
 import com.github.a2g.core.interfaces.IInventoryPresenterFromActions;
 import com.github.a2g.core.interfaces.IMasterPresenterFromActions;
 import com.github.a2g.core.interfaces.IScenePresenterFromActions;
 import com.github.a2g.core.interfaces.IScenePresenterFromWalkAction;
 import com.github.a2g.core.interfaces.ITitleCardPresenterFromActions;
 
public class SwitchWalkAction extends MoveWhilstAnimatingAction {
 	private IScenePresenterFromWalkAction scene;
 
 	public SwitchWalkAction(BaseAction parent, short ocode, double endX,
 			double endY, int delay, boolean isLinear) {
 		super(parent, ocode, isLinear);
 		super.setEndX(endX);
 		super.setEndY(endY);
 		this.setHoldLastFrame(false);
 	}
 
 	@Override
 	public void runGameAction() {
 		
 		double startX = scene.getBaseMiddleXByOtid(getOtid());
 		double startY = scene.getBaseMiddleYByOtid(getOtid());
 
 		double diffX = startX - getEndX();
 		System.out.println(" walkto " + startX + " " + getEndX());
 		double diffY = startY - getEndY();
 
 		// anim
 		String anim = "";
 		int width = scene.getSceneGuiWidth();
 		int height = scene.getSceneGuiHeight();
 
 		if ((diffX * width) * (diffX * width) > (diffY * height)
 				* (diffY * height)) {
 			if (getEndX() < startX) {
 				anim = scene.getSpecialAnimationByOtid(getOtid(), Special.West);
 			} else {
 				anim = scene.getSpecialAnimationByOtid(getOtid(), Special.East);
 			}
 		} else {
 			if (getEndY() < startY) {
 				anim = scene
 						.getSpecialAnimationByOtid(getOtid(), Special.North);
 			} else {
 				anim = scene
 						.getSpecialAnimationByOtid(getOtid(), Special.South);
 			}
 		}
 
 		// we've set it up now, pass to MoveWhilstAnimatingAction to execute
 		scene.setAsACurrentAnimationByAtid(anim);
 		super.runGameAction();
 	}
 
 	@Override
 	// on complete walking
 	protected boolean onCompleteGameAction() {
 		boolean result = super.onCompleteGameAction();
 		return result;
 
 		// best to set initial animation at the end, since:
 		// - if the walk animation is a cycle then no frame will be completely
 		// stationary
 		// - to make it consistent with everything else
 		// super.getObject().setCurrentAnimation(south);super.getObject().setCurrentFrame(0);
 		// moveWhilstAnimateAlreadySetsToInitial
 		//scene.setToInitialAnimationWithoutChangingFrameByOtid(getOtid());
 	}
 
 	@Override
 	public void setAll(IMasterPresenterFromActions master,
 			IScenePresenterFromActions scene,
 			IDialogTreePresenterFromActions dialogTree,
 			ITitleCardPresenterFromActions titleCard, IInventoryPresenterFromActions inventory) {
 		setSceneForWalkTo(scene);
 		super.setAll(master, scene, dialogTree, titleCard, inventory);
 
 	}
 
 	public void setSceneForWalkTo(IScenePresenterFromWalkAction scene) {
 		this.scene = scene;
 	}
 }
