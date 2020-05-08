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
 
 package com.thalespf.library.wizardmaker.wizard;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 
 
 /**
  * A default implementation of a page
  * 
  * @author Thales Ferreira / l.thales.x@gmail.com
  *
  */
 
 public abstract class WizardPage implements IWizardPage {
 	
 	private IWizard wizard;
 	protected Fragment pageFragment;
 	
     private String name;
     private boolean required = false;
 
 	private boolean complete = true;
 	
 	protected int layoutPage;
 	
 	/**
 	 * A map that correlate the field in the page and your value
 	 */
 	private Map<String, String> data;
 	
 	/**
 	 * 
 	 */
 	public WizardPage(String name) {
 		this.name = name;
 		this.data = new HashMap<String, String>();
 	}
 	
 	/**
 	 * @param layoutPage
 	 */
 	public WizardPage(String name, int layoutPage) {
 		this.name = name;
 		this.layoutPage = layoutPage;
 	}
 
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#setWizard(com.thalespf.library.wizardmaker.wizard.Wizard)
 	 */
 	@Override
 	public void setWizard(IWizard wizard) {
 		this.wizard = wizard;
 	}
 
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#getWizard()
 	 */
 	@Override
 	public IWizard getWizard() {
 		return wizard;
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#canShiftNextPage()
 	 */
 	@Override
 	public boolean canShiftNextPage() {
 		if(isPageComplete() && wizard.getNextPage(this) != null) {
 			return true;
 		}
 		return false;
 	}
 	
	/** (non-Javadoc)
	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#isLastPage()
	 */
	@Override
	public boolean isLastPage() {
		if(!(this instanceof BranchPage) && wizard.getNextPage(this) == null) {
			return true;
		}
		return false;
	}
	
 	/**
 	 * 
 	 * 
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#isPageComplete()
 	 */
 	@Override
 	public boolean isPageComplete() {
 		return complete;
 	}
 
 	/**
 	 * @return
 	 */
 	@Override
 	public String getName() {
 		return name;
 	}
 	
 	protected Map<String, String> getData() {
 		return data;
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#getChoosedValue(java.lang.String)
 	 */
 	@Override
 	public String getChoosedValue(String field) {
 		return getData().get(field);
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#onValueChoosed(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void onValueChoosed(String field, String value) {
 		if(getData().containsKey(field)) {
 			getData().remove(field);
 			getData().put(field, value);
 		} else {
 			getData().put(field, value);
 		}
 		
 		if(getWizard() == null) {
 			Log.e("WizardMaker", "The wizard is null");
 			return;
 		}
 		
 		if(getWizard().getContainer() == null) {
 			Log.e("WizardMaker", "The container of the wizard is null");
 			return;
 		}
 		
 		getWizard().getContainer().onValueChoosed();
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#removeValueChoosed(java.lang.String)
 	 */
 	@Override
 	public void removeValueChoosed(String field) {
 		getData().remove(field);
 	}
 	
 	/**
 	 * @param required the required to set
 	 */
 	public IWizardPage setRequired(boolean required) {
 		this.required = required;
 		return this;
 	}
 	
 	@Override
 	public boolean isRequired() {
 		return required;
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#getView()
 	 */
 	@Override
 	public View getView() {
 		if(pageFragment != null) {
 			return pageFragment.getView();
 		}
 		return null;
 	}
 	
 	/**
 	 * Let the client define new elements
 	 * 
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#onCreateView(android.view.LayoutInflater, android.view.View)
 	 */
 	@Override
 	public abstract void onCreateView(LayoutInflater inflater, View rootView);
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.IWizardPage#getContext()
 	 */
 	@Override
 	public Context getContext() {
 		return getWizard().getContainer().getContext();
 	}
 
 }
