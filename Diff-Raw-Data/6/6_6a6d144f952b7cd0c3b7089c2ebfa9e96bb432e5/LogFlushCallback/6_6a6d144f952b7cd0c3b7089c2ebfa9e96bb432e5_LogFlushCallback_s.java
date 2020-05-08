 /*
  * Copyright 2010 NCHOVY
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logstorage;
 
 public interface LogFlushCallback {
 	/**
 	 * invoked when logs are inserted to table
 	 * 
 	 * @param tableName
 	 *            input table name
 	 * @param logBatch
 	 *            all logs' table name should be same
 	 */
	void onFlushCompleted(LogFlushCallbackArgs args);

 	void onFlush(LogFlushCallbackArgs arg);
 	
 	void onFlushException(LogFlushCallbackArgs arg, Throwable t);
 }
