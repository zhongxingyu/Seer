 import java.util.*;
 import java.io.*;
 import net.sf.samtools.util.BlockCompressedInputStream;
 import net.sf.samtools.util.BlockCompressedFilePointerUtil;
 
 
 
 public class LargeFileReader {
     
     private RandomAccessFile txt = null; //text reader
     private BlockCompressedInputStream bgzf = null; //Block compressed gzip reader
     //BufferedReader in;
     boolean isASCII = true;
     long file_size = 0;
     
     public LargeFileReader(final String filename) throws IOException {
 	//Determine if file is ascii
 	if(isText(filename)){
 	    isASCII = true;
 	    txt = new RandomAccessFile(filename, "r");
 	    bgzf = null;
 	}else if(isBGZF(filename)){
 	    isASCII = false;
 	    bgzf = new BlockCompressedInputStream(new File(filename));
 	    bgzf.seek(0);
 	    file_size = (new File(filename)).length();
 	    //bgzf = new BlockCompressedInputStream(new RandomAccessFile(filename, "r"));
 	    txt = null;
 	}
 	
     }
 
     private boolean isText(final String filename) throws IOException {
 	BufferedInputStream bufferedInput = new BufferedInputStream(new FileInputStream(filename));
         byte[] buffer = new byte[1024];
         int numchars = bufferedInput.read(buffer);
         for(int i = 0; i < numchars; i++){
             char c = (char)buffer[i];
             if((c < 32 || c > 126) && !Character.isWhitespace(c)){
 		return false;
             }
         }
 	
         bufferedInput.close();
 	return true;
     }
 
     private boolean isBGZF(final String filename) throws IOException {
 	BufferedInputStream bufferedInput = new BufferedInputStream(new FileInputStream(filename));
 	try {
 	    return BlockCompressedInputStream.isValidFile(bufferedInput);
 	    
 	}catch(RuntimeException re){
 	    System.out.println("Cannot test non-buffered stream.");
 	}
 	return false;
     }
 
     public void close() throws IOException {
 	if(bgzf != null) bgzf.close();
 	if(txt != null) txt.close();
     }
 
     public long getFilePointer() throws IOException {
 	if(isASCII && txt != null) return txt.getFilePointer();
 	if(!isASCII && bgzf != null){ 
 	    try {
 		return bgzf.getFilePointer();
 	    }catch(Exception e){
 		e.printStackTrace();
 	    }
 	}
 	return 0;
     }
 
     public long getRealOffset() throws IOException {
 	if(isASCII && txt != null) return txt.getFilePointer();
 	if(!isASCII && bgzf != null){
 	    return BlockCompressedFilePointerUtil.getBlockAddress(bgzf.getFilePointer());
 	}
 	
 	return 0;
 
     }
 
     public long length() throws IOException { 
 	if(isASCII && txt != null) return txt.length();
 	if(!isASCII && bgzf != null) return file_size;
 	return 0; 
     }
 
     public int read(byte[] b) throws IOException {
 	if(isASCII && txt != null) return txt.read(b);
 	if(!isASCII && bgzf != null) return bgzf.read(b);
 	return 0;
     }
 
     public String readLine() throws IOException {
 	if(isASCII && txt != null){
 	    return txt.readLine();
 	}
 	if(!isASCII && bgzf != null){
 	    String ans = "";
 	    char c;
	    while((c = (char)bgzf.read())>=0){
		//System.out.println(c);
 		if(c != '\n') ans += c;
 		else return ans;
 	    }
 	}
 	return null;
     }
 
     public void seek(long pos) throws IOException {
 	if(isASCII && txt != null) txt.seek(pos);
 	//System.out.println(pos);
 	//System.out.println("hey hey " + BlockCompressedFilePointerUtil.getBlockAddress(pos));
 	if(!isASCII && bgzf != null) bgzf.seek(pos);
     }
 
 }
