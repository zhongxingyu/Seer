 /*
  * Copyright 2013 The British Library / The SCAPE Project Consortium
  * Author: William Palmer (William.Palmer@bl.uk)
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package uk.bl.dpt.pdfextractstreams;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class PositionReader {
 
 	private FileReader fr;
 	private static int BUFSIZE = 32768;
 	private char[] buffer = new char[BUFSIZE];
 	private long readOffset = 0;
 	private int bufOffset = 0;
 	private int bufRead = 0;
 	private boolean end = false;
 	//private char prev = '\0';
 	//String line = "";
 	
 	public PositionReader(String file) {
 		
 		try {
 			fr = new FileReader(file);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//initially fill buffer
 		try {
 			bufRead = fr.read(buffer, 0, BUFSIZE);
 			readOffset += bufRead;
 			bufOffset = 0;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void fillBuffer()  {
 		if(end) return;
 		//int count = 0;
 		if(bufRead<BUFSIZE-1) {
 			//don't read any more
 			//System.out.println("EOF: "+getPos());
 			end = true;
 			return;
 		}
 		try {
 			bufRead = fr.read(buffer, 0, BUFSIZE);
 			readOffset += bufRead;
 			bufOffset = 0;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public char nextChar() {
 		if(end) {
 			return '\0';
 		}
 		if(bufOffset == bufRead) {
 			fillBuffer(); 
 		}
 		return buffer[bufOffset++];
 	}
 	
 	public char peekNextChar() {
 		if(end) {
 			return '\0';
 		}
 		if(bufOffset == bufRead) {
 			fillBuffer(); 
 		}
 		return buffer[bufOffset];
 	}
 	
 	public String readLine() {
 		//read next line from the
 		String ret = "";
 		char c;
 		while(true) {
 			if(!ready()) break;
 			c = nextChar();
 			//chomp carriage returns
 			if((c=='\r')||(c=='\n')) {
 				char peek = peekNextChar(); 
 				if((peek=='\n')||(peek=='\r')) {
 					nextChar();
 				}
 				if(ret.trim().equals("")) {
					//HACK: chomp whitespace
					while((""+peekNextChar()).equals("")) {
						nextChar();
					}
 					ret = "";
 					continue;
 				} else {
 					break;
 				}
 			}			
 			ret += c;
 		}
 		//if(ret.trim().equals("")) return readLine();
 		//System.out.println("Ret: "+ret);
 		return ret;
 	}
 	
 	public void skip(long x) {
 		long left = x;
 		if(left<BUFSIZE-bufOffset) {
 			//we can skip within our open buffer
 			bufOffset += left;
 			return;
 		}
 		left -= BUFSIZE-bufOffset;
 		bufOffset = BUFSIZE;
 		//skip as many full buffers as we can
 		int fullBuffers = (int)Math.floor(left/BUFSIZE);
 		try {
 			fr.skip(fullBuffers*BUFSIZE);
 			left -= fullBuffers*BUFSIZE;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		readOffset += fullBuffers*BUFSIZE;
 		fillBuffer();
 		bufOffset = (int)left;
 	}
 	
 	public void close() {
 		try {
 			fr.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean ready() {
 		return (!end);
 	}
 	
 	public long getPos() {
 		if(readOffset==0) return 0;
 		return readOffset - bufRead + bufOffset;
 	}
 	
 }
