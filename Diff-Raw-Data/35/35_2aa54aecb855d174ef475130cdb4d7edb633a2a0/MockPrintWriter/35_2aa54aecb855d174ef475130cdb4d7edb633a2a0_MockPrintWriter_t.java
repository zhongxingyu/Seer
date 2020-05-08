 /**
  * 
  */
 package com.portletguru.portlettester.mocks.http;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import com.portletguru.portlettester.TestResultHolder;
 
 /**
  * @author Derek Linde Li
 * 
  */
 public class MockPrintWriter extends Writer {

 	private TestResultHolder resultHolder;

 	public MockPrintWriter(TestResultHolder resultHolder) {
		this.resultHolder = resultHolder;
 	}

 	@Override
 	public void write(int c) throws IOException {
 		char[] content2Add = new char[1];
		content2Add[0] = (char) c;
 		resultHolder.appendOutputContent(new String(content2Add));
 	}
 
 	@Override
 	public void write(char[] cbuf, int off, int len) throws IOException {
 		int length = len;
		if (off > cbuf.length) {
 			return;
 		}
		if (len > cbuf.length) {
 			length = cbuf.length - off;
 		}
 		char[] content2Add = new char[length];
 		System.arraycopy(cbuf, off, content2Add, 0, length);
 		resultHolder.appendOutputContent(new String(content2Add));
 	}
 
 	@Override
	public void flush() throws IOException {
		// DO NOTHING
 	}
 
 	@Override
 	public void close() throws IOException {
		// DO NOTHING
	}

 }
