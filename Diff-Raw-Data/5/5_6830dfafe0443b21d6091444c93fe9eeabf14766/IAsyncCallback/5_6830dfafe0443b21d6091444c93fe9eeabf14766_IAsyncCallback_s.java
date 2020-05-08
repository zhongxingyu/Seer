 package com.secondhand.controller;
 
 /**
  * Used in conjunction with AsyncTaskGameLoader to create the loading scene.
  */
 public interface IAsyncCallback {
	public void work();
 
	public void onWorkComplete();
 }
