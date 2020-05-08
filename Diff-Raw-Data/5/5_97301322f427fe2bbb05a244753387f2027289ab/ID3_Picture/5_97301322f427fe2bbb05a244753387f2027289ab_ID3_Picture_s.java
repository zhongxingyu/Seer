 package id3TagStuff.id3Data;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Arrays;
 
 import util.Util;
 
 public class ID3_Picture implements ID3v2_XFrameData {
 
 	private String format_MIME, description;
 	private byte type, textEncoding;
 	private int[] data;
 	private int[] bytRepresentation = null;
 
 	// TODO this won't work with v2.3
 	public ID3_Picture(int majorVersion, int[] dataP) throws IOException {
 		if (Util.DEBUG)
 			System.out.println("We are creating a pic");
 		int offset = 0;
 		textEncoding = (byte) dataP[offset++];
 		if (majorVersion < 3) {
 			// the format is always 3 chars
 			format_MIME = new String(Util.castIntArrToByteArr(Arrays.copyOfRange(dataP,
 					offset, 4)));
 			offset += 3;
 		} else {
 			int mimelength = 0;
			//the +2 is a hack to get it to work for the first time
 			for (int i = offset; i < dataP.length; i++) {
 				if (dataP[i] == (byte) 00)
 					break;
				mimelength++;
 			}
 			format_MIME = new String(Util.castIntArrToByteArr(Arrays.copyOfRange(dataP,
 					offset, offset + mimelength)));
 			offset += mimelength;
 		}
 		type = (byte) dataP[offset++];
 		int descWidth = 0;
 		for (int i = offset; i < dataP.length; i++) {
 			if (dataP[i] == (byte) 00) {
 				break;
 			}
 			descWidth++;
 		}
 		description = new String(Util.castIntArrToByteArr(Arrays.copyOfRange(dataP, offset,
 				offset + descWidth)));
 		offset += descWidth;
 		this.data = Arrays.copyOfRange(dataP, offset + 1, dataP.length);
 	}
 
 	public ID3_Picture(int majorVersion, int[] dataP, File saveToLocation) throws IOException {
 		this(majorVersion, dataP);
 		PrintStream out = new PrintStream(saveToLocation);
 		Util.writeIntArrToStream(out, data);
 	}
 
 	public ID3_Picture(String descriptionP, byte textEncodingP, byte imageType,
 			String formatOrMIME, int[] picData) {
 		this.description = descriptionP;
 		textEncoding = textEncodingP;
 		type = imageType;
 		format_MIME = formatOrMIME;
 		data = picData;
 	}
 
 	@Override
 	public String toString() {
 		return "ID3 Picture: " + description;
 	}
 
 	@Override
 	public String getType() {
 		return "Picture";
 	}
 
 	public void saveAs(File loc) throws IOException {
 		String name = loc.getName();
 		if (!name.endsWith("." + format_MIME.toLowerCase())
 				&& !name.endsWith("." + format_MIME.toUpperCase())) {
 			name += "." + format_MIME;
 		}
 		PrintStream out = new PrintStream(new File(name));
 		Util.writeIntArrToStream(out, data);
 	}
 
 	@Override
 	public int[] getByteRepresentation(int majorVersion) {
 		if (majorVersion < 2 | majorVersion > 4)
 			throw new IllegalArgumentException("The version type doesnot exist");
 		if (bytRepresentation != null) // lazy loading
 			return bytRepresentation;
 
 		// encoding + mime_type
 		int arrLength = 1 + format_MIME.getBytes().length;
 		if (majorVersion > 2) // we need to null terminate the mimetype if the version is right
 			arrLength++;
 		// pic type + desc + nullterminator + data
 		arrLength += 1 + description.getBytes().length + 1 + data.length;
 		int[] ret = new int[arrLength];
 		// the offset that we are copying into ret at
 		int offset = 0;
 		// add encoding
 		ret[offset++] = 0;
 		// TODO somehow the mimetype is getting screwed here
 		// copy in the pic format or mimetype
 		System.arraycopy(Util.castByteArrToIntArr(format_MIME.getBytes()), 0, ret, offset,
 				format_MIME.getBytes().length);
 		// grow offset by the ammount we just put into the array
 		offset += format_MIME.getBytes().length;
 		if (majorVersion > 2) // we need to null terminate the mimetype if the version is right
 			ret[offset++] = 0;
 		// this is the pictype
 		ret[offset++] = 0;
 		// now we copy in the description
 		System.arraycopy(Util.castByteArrToIntArr(description.getBytes()), 0, ret, offset,
 				description.getBytes().length);
 		// ofset grows by description size
 		offset += description.getBytes().length;
 		// null terminate description
 		ret[offset++] = 0;
 		// copy in the actual picture data
 		System.arraycopy(data, 0, ret, offset, data.length); // it always crashes here
 		bytRepresentation = ret;
 		return bytRepresentation;
 	}
 }
