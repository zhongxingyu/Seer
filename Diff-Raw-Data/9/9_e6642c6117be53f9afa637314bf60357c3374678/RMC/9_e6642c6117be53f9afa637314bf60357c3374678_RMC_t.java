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
 
 import com.buglabs.nmea.StringUtil;
 
 /**
  * A NMEA sentence for position and other information.
  * 
  * @author aroman
  * 
  */
 public class RMC extends PositionSentence {
 
 	private String timeOfFix;
 	private String dataStatus;
 	private String groundSpeed;
 	private String dateStamp;
 	private String magneticVariation;
 	private String trackMadeGood;
 
 	public RMC() {
 		super("$GPRMC");
 	}
 
 	public RMC(com.buglabs.nmea2.RMC newRMC) {
 		super("$GPRMC");
 		this.timeOfFix = newRMC.getTimeOfFix();
 		this.dataStatus = newRMC.getDataStatus();
 		this.groundSpeed = newRMC.getGroundSpeed();
 		this.dateStamp = newRMC.getDateStamp();
 		this.magneticVariation = newRMC.getMagneticVariation();
 		this.trackMadeGood = newRMC.getTrackMadeGood();
		this.setLatitude(newRMC.getLatitude());
		this.setLongitude(newRMC.getLongitude());
 	}
 	
 	public Object parse(String sentence) {
 		magneticVariation = "";
 
 		String[] splitSentence = StringUtil.split(sentence, "*");
 
 		if (splitSentence.length > 1) {
 			setChecksum(splitSentence[1]);
 		}
 
 		String[] fields = StringUtil.split(splitSentence[0], ",");
 
 		for (int i = 0; i < fields.length; ++i) {
 			switch (i) {
 			case 1:
 				timeOfFix = fields[i];
 				break;
 			case 2:
 				dataStatus = fields[i];
 				break;
 			case 3:
 			case 4:
 				setLatitude(fields[i] + "," + fields[i + 1]);
 				i++;
 				break;
 			case 5:
 			case 6:
 				setLongitude(fields[i] + "," + fields[i + 1]);
 				i++;
 				break;
 			case 7:
 				groundSpeed = fields[i];
 				break;
 			case 8:
 				trackMadeGood = fields[i];
 				break;
 			case 9:
 				dateStamp = fields[i];
 				break;
 			case 10:
 			case 11:
 				if (fields.length > 11) {
 					magneticVariation = fields[i] + "," + fields[i + 1];
 					if (magneticVariation.length() == 1) {
 						magneticVariation = "";
 					}
 				}
 				i++;
 				break;
 			default:
 				break;
 			}
 		}
 
 		return this;
 	}
 
 	public String getTimeOfFix() {
 		return timeOfFix;
 	}
 
 	public String getDataStatus() {
 		return dataStatus;
 	}
 
 	public String toDegrees(String measure) {
 		int index = measure.indexOf(".");
 		return measure.substring(0, index - 2);
 	}
 
 	public String getGroundSpeed() {
 		return groundSpeed;
 	}
 
 	public String getTrackMadeGood() {
 		return trackMadeGood;
 	}
 
 	public String getDateStamp() {
 		return dateStamp;
 	}
 
 	public String getMagneticVaration() {
 		return magneticVariation;
 	}
 	
 	public String toString() {
 		return "timeOfFix: " + timeOfFix + "  dataStatus: " + dataStatus + "  groundSpeed: " + groundSpeed + " trackMadeGood: " + trackMadeGood + " dateStamp: " + dateStamp + " magneticVariation: " + magneticVariation + "  " + super.toString();
 	}
 }
