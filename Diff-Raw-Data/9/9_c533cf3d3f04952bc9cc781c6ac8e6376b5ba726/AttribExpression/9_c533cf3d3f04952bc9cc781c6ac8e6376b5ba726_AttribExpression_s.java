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
 package com.buglabs.util.xml;
 
 /**
  * An XPath attribute.
  * 
  * @author kgilmer
  * 
  */
 class AttribExpression {
 	public static final int EQUAL_OPERATOR = 1;
 
 	public static final int NOT_EQUAL_OPERATOR = 2;
 
 	private String name;
 
 	private String value;
 
 	private String tagName;
 
 	private int operator;
 
 	boolean exists = false;
 
 	public AttribExpression(String exprToken) {
 		String[] b;
		String[] s = exprToken.split("[@");
 
 		if (s.length == 2) {
 			tagName = s[0];
 			b = s[1].split("=");
 
 			if (b.length == 2) {
 				name = b[0];
 				value = resolveValue(b[1].substring(0, b[1].length() - 1));
 				operator = EQUAL_OPERATOR;
 				exists = true;
 			} else {
 				b = s[1].split("!=");
 
 				if (b.length == 2) {
 					name = b[0];
 					value = b[1];
 					operator = NOT_EQUAL_OPERATOR;
 					exists = true;
 				}
 			}
 		}
 	}
 
 	private String resolveValue(String string) {
 		// this may resolve a variable to a literal.
 
 		return string.substring(1, string.length() - 1);
 	}
 
 	public String toString() {
 		if (!exists) {
 			return super.toString();
 		}
 		String op = "?";
 		if (operator == EQUAL_OPERATOR) {
 			op = "=";
 		} else if (operator == NOT_EQUAL_OPERATOR) {
 			op = "!=";
 		}
 
 		return tagName + "[@" + name + op + value + "]";
 	}
 
 	public String getTagName() {
 		return tagName;
 	}
 
 	public boolean isExists() {
 		return exists;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int getOperator() {
 		return operator;
 	}
 
 	public String getValue() {
 		return value;
 	}
 }
