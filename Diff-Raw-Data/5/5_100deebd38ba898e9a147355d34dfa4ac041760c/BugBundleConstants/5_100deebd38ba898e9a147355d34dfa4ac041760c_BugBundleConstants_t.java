 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.util;
 
 /**
  * Constants relating to bundles.
  * 
  * @author kgilmer
  * 
  */
 public interface BugBundleConstants {
 	/**
 	 * A designation for core runtime bundles that provide core services.
 	 */
 	public static final String BUG_BUNDLE_CORE = "Core";
 
 	/**
 	 * A module desginated as a Module bundle, providing module services.
 	 */
 	public static final String BUG_BUNDLE_MODULE = "Module";
 
 	/**
 	 * A module designated as an application.
 	 */
 	public static final String BUG_BUNDLE_APPLICATION = "Application";
 
 	/**
	 * A module designated as a library.
	 */
	public static final String BUG_BUNDLE_LIBRARY = "Library";
	
	/**
 	 * Header to define and describe the type of bundle in the bug context.
 	 */
 	public static final String BUG_BUNDLE_TYPE_HEADER = "Bug-Bundle-Type";
 
 	/**
 	 * The slot ID the module is connected to.
 	 */
 	public static final String BUG_BUNDLE_SLOT_ID_HEADER = "Bug-Slot-Index";
 
 	/**
 	 * This header represents the version of the hardware module.
 	 */
 	public static final String BUG_BUNDLE_MODULE_VERSION = "Bug-Module-Version";
 
 	/**
 	 * The Module ID header as presented by the BMI bus.
 	 */
 	public static final String BUG_BUNDLE_MODULE_ID = "Bug-Module-Id";
 }
