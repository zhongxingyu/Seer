 package io.spaceport;
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 
 public class Swc2Swf {
 	public static final void main(String[] args) {
		if (args.length != 2) {
			error("Usage: java -jar swc2swf.jar input.swc output.swf");
			return;
		}

 		String inputPath = args[0];
 		String outputPath = args[1];
 
		ZipFile file;
		try {
			file = new ZipFile(inputPath);
		} catch (IOException e) {
			error("Error while reading SWC file: " + e.getMessage());
			return;
		}

		ZipEntry library = file.getEntry("library.swf");
		if (library == null) {
			error("Missing library in SWC file; is it really a SWC file?");
			return;
		}

 		try {
 			pipe(
 				file.getInputStream(library),
 				new BufferedOutputStream(new FileOutputStream(outputPath))
 			);
 		} catch (IOException e) {
			error("Error while writing SWF file: " + e.getMessage());
			return;
 		}
 	}
 
	private static final void error(String message) {
		System.err.println(message);
		System.exit(1);
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
