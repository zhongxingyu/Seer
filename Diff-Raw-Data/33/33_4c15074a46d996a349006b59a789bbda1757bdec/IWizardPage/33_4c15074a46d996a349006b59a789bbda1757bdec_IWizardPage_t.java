 /**
  * 
  */
 package com.thalespf.library.wizardmaker.wizard;
 
 
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 
 /**
  * 
  * @author Thales Ferreira / l.thales.x@gmail.com
  */
 public interface IWizardPage {
 
 	/**
 	 * Set to page the wizard reference
 	 * 
 	 * @param wizard the wizard reference
 	 */
 	void setWizard(IWizard wizard);
 	
 	/**
 	 * Returns the wizard reference
 	 * 
 	 */
 	public IWizard getWizard();
 
 	/**
 	 * Create the page fragment
 	 * 
 	 * @return fragment the user defined fragment
 	 * 
 	 */
 	public Fragment createFragment();
 	
     /**
      * Returns whether the next page could be showed
      *
      * @return <code>true</code> if the next page could be displayed,
      *   and <code>false</code> otherwise
      */
 	public boolean canShiftNextPage();
 
     /**
      * Returns whether the page is completed
      *
      * @return <code>true</code> if the next page could be displayed,
      *   and <code>false</code> otherwise
      */
 	public boolean isPageComplete();
 
 	/**
 	 * Returns page name
 	 * 
 	 * @return name
 	 */
 	public String getName();
 	
 	/**
 	 * Sets a value to the field from this page. Called from the page
 	 * fragments
 	 * 
 	 * @param field
 	 * @param value
 	 */
 	public void onValueChoosed(String field, String value);
 
 	/**
 	 * Return the value from the field. The page stores the value
 	 * from each field in page
 	 * 
 	 * @return string corresponding to the choosed value on the fragment
 	 * from the field
 	 * 
 	 */
 	public String getChoosedValue(String field);
 
 	/**
 	 * @return
 	 */
 	boolean isRequired();
 
 	/**
 	 * @param field
 	 */
 	public void removeValueChoosed(String field);
 	
 	/**
 	 * Returns the view oh the page
 	 * 
 	 * @return the root view
 	 */
 	public View getView();
 
 	/**
 	 * Override to add new ui elements to the page
 	 * 
 	 * @param inflater
 	 * @param rootView
 	 */
 	public void onCreateView(LayoutInflater inflater, View rootView);
 	
 	public Context getContext();

	/**
	 * @return
	 */
	boolean isLastPage();
 	
 }
