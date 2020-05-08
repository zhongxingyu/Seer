 /*
  * Copyright 2013 Thales Ferreira
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.thalespf.library.wizardmaker.wizard.internal.shifter;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 
 import com.thalespf.library.wizardmaker.R;
 import com.thalespf.library.wizardmaker.wizard.IWizardPage;
 import com.thalespf.library.wizardmaker.wizard.internal.ReviewWizardPage;
 import com.thalespf.library.wizardmaker.wizard.internal.WizardContainer.WizardContainerController;
 
 /**
  * @author Thales Ferreira / l.thales.x@gmail.com
  *
  */
 public class ShifterBottom extends AbstractShifter {
 
 	/**
 	 * @param controller
 	 */
 	public ShifterBottom(WizardContainerController controller) {
 		super(controller);
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.internal.shifter.AbstractShifter#onCreateShifterView()
 	 */
 	@Override
 	protected View onCreateShifterView() {
 		View view = LayoutInflater.from(wizardActivity).inflate(R.layout.wizard_maker_shifter2_view, null);
 		view.findViewById(R.id.shifterLeftButton).setEnabled(false);
 		view.findViewById(R.id.shifterRigthButton).setEnabled(false);
 		return view;
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.internal.shifter.AbstractShifter#updateShifter(int, int, int)
 	 */
 	@Override
 	public void updateShifter(IWizardPage page, int currentItem, int lastItem) {
 		super.updateShifter(page, currentItem, lastItem);
 		
 		Button rigthButton = (Button) shifterView.findViewById(R.id.shifterRigthButton);
 		rigthButton.setText(R.string.next);
 		if(page.isRequired() && !page.isPageComplete()) {
 			rigthButton.setEnabled(false);
 		} else {
 			rigthButton.setEnabled(true);
 		}
 		
 		if(currentItem != 0 && currentItem == lastItem) {
 			if(page.getWizard().isReviewPage()) {
 				rigthButton.setText(R.string.review);
 	            //rigthButton.setBackgroundResource(R.drawable.selectable_item_background);
 	            //TypedValue v = new TypedValue();
 	            //wizardActivity.getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
 	            //rigthButton.setTextAppearance(wizardActivity, v.resourceId);
 	            //rigthButton.setEnabled(position != mPagerAdapter.getCutOffPage());
 			} else {
 				rigthButton.setText(R.string.finish);
 				//rigthButton.setBackgroundResource(R.drawable.finish_background);
 				//rigthButton.setTextAppearance(wizardActivity, R.style.TextAppearanceFinish);
 			}
		} else {
			if(page.isLastPage()) {
				rigthButton.setText(R.string.finish);
			}
 		}
 		
		if(page instanceof ReviewWizardPage) {
 			rigthButton.setText(R.string.finish);
 			//rigthButton.setBackgroundResource(R.drawable.finish_background);
 			//rigthButton.setTextAppearance(wizardActivity, R.style.TextAppearanceFinish);
 		}
 	}
 
 }
