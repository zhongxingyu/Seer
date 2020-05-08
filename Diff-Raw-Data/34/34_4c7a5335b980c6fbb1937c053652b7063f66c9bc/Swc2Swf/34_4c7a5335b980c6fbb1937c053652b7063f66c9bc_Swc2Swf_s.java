 package io.spaceport;
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 
 public class Swc2Swf {
 	public static final void main(String[] args) {
 		String inputPath = args[0];
 		String outputPath = args[1];
 
 		try {
			ZipFile file = new ZipFile(inputPath);
			ZipEntry library = file.getEntry("library.swf");
 			pipe(
 				file.getInputStream(library),
 				new BufferedOutputStream(new FileOutputStream(outputPath))
 			);
 		} catch (IOException e) {
			System.err.println("Unhandled exception:");
			e.printStackTrace();
 		}
 	}
 
 	// Java doesn't have basic necessities like piping streams, so we have to
 	// implement it ourselves.
 	public static final void pipe(InputStream in, OutputStream out)
 		throws IOException {
 		byte[] buffer = new byte[1024];
 		int len;
 
 		while ((len = in.read(buffer)) >= 0) {
 			out.write(buffer, 0, len);
 		}
 
 		in.close();
 		out.close();
 	}
 }
