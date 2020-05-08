 package org.seasr.meandre.component.opennlp;
 
 
 
 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 
 //
 //simple helper class to maintain text spans
 //this one keeps the actual text as well
 // this class should NOT be coupled tightly 
 // with any OPEN NLP stuff
 //
 
 
 public class TextSpan {
 	
 	int startIdx;
 	int endIdx;
 	String span;
 	
 	public TextSpan()
 	{
 		reset();
 	}
 
 	public void reset()
 	{
 		startIdx = endIdx = 0;
 		this.span = null;
 	}
 	
 	public void setStart(int s) {
 		startIdx = s;
 	}
 	
 	public void setEnd(int e) {
 		endIdx = e;
 	}
 	
 	public void setSpan(String s) 
 	{
 		String textSpan = s.substring(startIdx, endIdx);
 		this.span = textSpan.replace("\n", " ").trim();
 	}
 	
 	public boolean equals(TextSpan b) {
 		
 		if (b == null) return false;
 		
 		if (this.span == null &&
 			   b.span == null) return true;
 		
		if (this.span == null) return false;
 		
 		return this.startIdx == b.startIdx &&
 		       this.endIdx   == b.endIdx   &&
 		       this.span.equals(b.span);
 		
 	}
 	
 	public int getStart()   {return startIdx;}
 	public int getEnd()     {return endIdx;}
 	public String getText() {return span;}
 
 }
