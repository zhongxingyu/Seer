 package org.uigl.veemanage.httpd;
 
 import java.io.FilterInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.logging.Level;
 
 import org.uigl.veemanage.VeeManage;
 
 public class Template {
 
 	public static InputStream applyTemplate(String resourceName, Session args) {
 		InputStream template =  ClassLoader.class.getResourceAsStream(resourceName);
 		return new Template.TemplateStream(template, args);
 	}
 	
 	private static class TemplateStream extends FilterInputStream {
 
 	    LinkedList<Character> mBuffer = new LinkedList<Character>();
 	    Session mArgs;
 	    
 		protected TemplateStream(InputStream in, Session args) {
 			super(in);
 			mArgs = args;
 			if (mArgs == null)
 				VeeManage.LOGGER.logp(Level.SEVERE, TemplateStream.class.getName(), "TemplateStream(InputStream in, Session args)", "Null Args");
 		}
 
 		@Override
 	    public int read() throws IOException {
 	        if (!mBuffer.isEmpty())
 	            return mBuffer.remove();
 
 	        while (true) {
 		        int c1 = super.read();
 		        if (c1 != '<' || mArgs == null)
 		            return c1;
 	
 		        int c2 = super.read();
 	
 		        if (c2 == '$') {
 		            StringBuffer varName = new StringBuffer();
 		            while ((c2 = super.read()) != '>')
 		                varName.append((char) c2);
 		            
 		            Object varVal = mArgs.get(varName.toString());
 		            if (varVal == null || varVal.toString() == null) {
 		            	VeeManage.LOGGER.logp(Level.WARNING, TemplateStream.class.getName(), "read()", "Cannot find variable \"" + varName + "\"");
 		            	varVal = varName;
 		            }
 		            for (char vc : varVal.toString().toCharArray())
 		            	mBuffer.add(vc);
 		            
 		            if (mBuffer.isEmpty())
 		            	continue;
 		            return mBuffer.remove();
 		        }	
 		        if (c2 == '#') {
 		            StringBuffer incName = new StringBuffer();
 		            while ((c2 = super.read()) != '>')
 		            	incName.append((char) c2);
 		            
 		            try {
 			            InputStream template = ClassLoader.class.getResourceAsStream(incName.toString());
 			            TemplateStream includeStream =  new Template.TemplateStream(template, mArgs);
 			            int includeChar = includeStream.read();
 			            while (includeChar > 0) {
 			            	mBuffer.add((char)includeChar);
 			            	includeChar = includeStream.read();
 			            }
 			            includeStream.close();
 		            } catch (Exception e) {
 		            	VeeManage.LOGGER.logp(Level.WARNING, TemplateStream.class.getName(), "read()", "Cannot find resource \"" + incName + "\"");
 		            }
 		            
 		            if (mBuffer.isEmpty())
 		            	continue;
 		            return mBuffer.remove();
 		        }
 		        
 	        	mBuffer.add((char) c2);
 	            return c1;
 	        }
 	    }

		@Override
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}
		
 		@Override
 		public int read(byte[] b, int off, int len) throws IOException {
			if(len == 0 || off >= len)
				return -1;
 
 			int c, reads = 0;
			while ((off + reads) < len && (c = read()) >= 0)
 				b[off + reads++] = (byte) c;

 			return reads;
 		}
 		
 	}
 	
 }
