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
 package com.buglabs.nmea2;
 
 import com.buglabs.nmea.sentences.NMEAParserException;
 
 /**
  * A class for PTTK type NMEA sentences.
  * @author kgilmer
  *
  */
 public final class PTTK extends AbstractNMEASentence {
 
 	private String msg;
 	private Integer code;
 
 	protected PTTK(String sentence) {
 		super(sentence);
 	}
 
 	/**
 	 * @return Message
 	 */
 	public String getMessage() {
 		return msg;
 	}
 
 	/**
 	 * @return Error Code
 	 */
 	public int getErrorCode() {
 		return code.intValue();
 	}
 
 	protected void parseField(int index, String field, String fields[]) {
 
 		switch (index) {
 		case 1:
 			msg = field;
 			break;
 		case 2:
 			code = Integer.getInteger(field);
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see com.buglabs.nmea2.AbstractNMEASentence#validate()
 	 */
 	protected void validate() {
		if (msg == null || code == null) {
			throw new NMEAParserException("Invalid NMEA sentence data.", null);
 		}
 	}
 
 }
