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
 package com.buglabs.nmea.sentences;
 
 import com.buglabs.nmea.DegreesMinutesSeconds;
 
 /**
  * A NMEA class for position.
  * 
  * @author aroman
  * 
  */
 public abstract class PositionSentence extends NMEASentence {
 
 	private String latitude;
 	private String longitude;
 
 	public PositionSentence(String type) {
 		super(type);
 	}
 
 	public String getLatitude() {
 		return latitude;
 	}
 
 	/**
 	 * @return true if either longitude or latitude are null or empty, false
 	 *         otherwise.
 	 */
 	public boolean isEmpty() {
 		return isEmpty(latitude) || isEmpty(longitude);
 	}
 
 	/**
	 * For a given string, return true if is null or empty, false otherwise.
 	 * 
 	 * @param val
 	 * @return
 	 */
 	private boolean isEmpty(String val) {
 		return val == null || val.length() == 0;
 	}
 
 	protected void setLatitude(String latitude) {
 		this.latitude = latitude;
 	}
 
 	public String getLongitude() {
 		return longitude;
 	}
 
 	public DegreesMinutesSeconds getLatitudeAsDMS() {
 		return new DegreesMinutesSeconds(getLatitude());
 	}
 
 	public DegreesMinutesSeconds getLongitudeAsDMS() {
 		return new DegreesMinutesSeconds(getLongitude());
 	}
 
 	protected void setLongitude(String longitude) {
 		this.longitude = longitude;
 	}
 }
