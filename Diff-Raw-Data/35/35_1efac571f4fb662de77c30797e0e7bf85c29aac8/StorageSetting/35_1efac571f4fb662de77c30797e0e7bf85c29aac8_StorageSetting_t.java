 /*
  *	  This file is part of the Bytewalla Project
  *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/092106/".
  *    
  *    Copyright 2009 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
  *    
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  *    
  */
 package se.kth.ssvl.tslab.wsn.general.servlib.config.settings;
 
 /**
  * This class represents StorageSetting in the configuration file
  * 
  * @author Rerngvit Yanggratoke (rerngvit@kth.se)
  */
 public class StorageSetting {
 
 	/**
 	 * Relative storage path where to put DTNBundle data. If the storage type is
 	 * SDCard, it's relative to root of the SDCard ( normally "/sdcard" in
 	 * filesystem ). If the storage type is Phone, it's relative to the
 	 * application specific folder in the phone
 	 */
 	private String storage_path_;
 
 	/**
 	 * Quota of storage to consume, Unit is in MB
 	 */
 	private int quota;
	
	private boolean test_data_log;
 
 	/**
 	 * Accessor for the quota of storage to consume, Unit is in MB
 	 * 
 	 * @return the quota
 	 */
 	public int quota() {
 		return quota;
 	}
 
 	/**
 	 * Setter for the quota of storage to consume, Unit is in MB
 	 * 
 	 * @param quota
 	 *            the quota to set
 	 */
 	public void set_quota(int quota) {
 		this.quota = quota;
 	}
 
 	/**
 	 * Accessor for the relative storage path where to put DTNBundle data. If
 	 * the storage type is SDCard, it's relative to root of the SDCard (
 	 * normally "/sdcard" in filesystem ). If the storage type is Phone, it's
 	 * relative to the application specific folder in the phone
 	 * 
 	 * @return the storage_path_
 	 */
 	public String storage_path() {
 		return storage_path_;
 	}
 
 	/**
 	 * Setter for the relative storage path where to put DTNBundle data. If the
 	 * storage type is SDCard, it's relative to root of the SDCard ( normally
 	 * "/sdcard" in filesystem ). If the storage type is Phone, it's relative to
 	 * the application specific folder in the phone
 	 * 
 	 * @param storagePath
 	 *            the storage_path_ to set
 	 */
 	public void set_storage_path(String storagePath) {
 		storage_path_ = storagePath;
 	}
 
	public boolean test_data_log() {
		return test_data_log;
	}

	public void set_test_data_log(boolean test_data_log) {
		this.test_data_log = test_data_log;
	}

 }
