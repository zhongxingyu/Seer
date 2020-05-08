 import java.io.File;
 import java.io.IOException;
 
 /**
  * 
  * A test file for my data compressor and decompressor.
  * 
  * @author Jay Fleischer
  * @version 1.0
  * 
  */
 public class DataCompression {
 	public static void main(String[] args) throws IOException {
		BitOutputStream s = new BitOutputStream(System.out);
		s.write(false);
		for (int i = 0; i < 7; i++)
			s.write(true);
		s.write(92);
		s.flush();
 		s.close();
 		File f = new File("files/test.pre");
 		FileCompressor.compress(f, "Hello World!");
 		System.out.println(FileCompressor.decompress(f));
 	}
 }
