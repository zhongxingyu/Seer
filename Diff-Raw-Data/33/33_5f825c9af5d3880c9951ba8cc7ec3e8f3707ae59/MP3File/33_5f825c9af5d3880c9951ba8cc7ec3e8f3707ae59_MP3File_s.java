 package awesome;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.imageio.stream.MemoryCacheImageInputStream;
 
 public class MP3File implements FilePathInfo {
 	
 	File file;
 	private String title = null;
 	private String artist = null;
 	private String album = null;
 	private String year = null;
	private BufferedImage cover = null;
 	
 	public MP3File(File file){
 		this.file = file; 
 	}
 	
 	public void tryToParse() {
 		try {
 			parse();//TODO: Better Parse Info Policy?
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 	
 	@Override
 	public File getFile() {
 		return file;
 	}
 
 	@Override
 	public boolean isDirectory() {
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return file.getName();
 	}
 
 	@Override
 	public List<FilePathInfo> listFiles() {
 		return null;
 	}
 
 	public static boolean isMP3(File f) {
 		if(!f.getName().endsWith(".mp3") || !f.exists()){
 			return false; //we only respect files with mp3 suffix as we don't want read every file.
 		}
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(f);
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace(); //TODO: Present Exception to user
 		}
 		byte[] isRightID3 = new byte[5];
 		try {
 			fis.read(isRightID3);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		byte[] firstThree = {isRightID3[0],isRightID3[1],isRightID3[2]};
 		byte[] nextTwo = {isRightID3[3],isRightID3[4]};
 		String identifier = new String(firstThree);
 		byte[] rightVersionNumber = {3,0};
 		return identifier.equals("ID3") && 
 				Arrays.equals(nextTwo,rightVersionNumber);
 	}
 
 	public static boolean containsMP3s(File rootFile) {
 		if(rootFile.isFile()) {
 			return MP3File.isMP3(rootFile);
 		} else if(rootFile.isDirectory()){
 			File[] subFiles = rootFile.listFiles();
 			for(File f:subFiles) {
 				if(MP3File.containsMP3s(f)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	
 	private void parse() throws IOException {
 		ID3InputStream input = new ID3InputStream(new FileInputStream(file));
 		
 		int id = input.readUnsignedShort() << 8;
 		id |= input.readUnsignedByte();
 		int version = input.readUnsignedShort();
 		if(id != 0x00494433 || version != 0x0300){
 			System.out.println("Unable to find ID3v2.3 Header!");
 			return; //maybe we should throw an exception here as MP3File already checked for header.
 		}
 		
 		int flags = input.readUnsignedByte();
 		int size = input.readSyncSafeSize();
 		//Extended Header
 		byte[] tag = new byte[size];
 		input.read(tag);
 		input.close();
 		input = new ID3InputStream(new ByteArrayInputStream(tag));
 		while(input.available() > 10){
 			parseFrame(input);
 			input.readPadding();
 		}
 		input.close();
 	}
 
 	private void parseFrame(awesome.ID3InputStream input) throws IOException {
 		String frameType = input.readStringOfLength(4);
 		//System.out.println("Frame Type: " + frameType);
 		int fsize = input.readInt();
 		//System.out.println("Frame Size: " + fsize);
 		int flags = input.readUnsignedShort();
 		switch (frameType) {
 			case "TALB" : setAlbum(input.readStringOfLengthWithCharsetByte(fsize)); break;
 			case "TIT2" : setTitle(input.readStringOfLengthWithCharsetByte(fsize)); break;
 			case "TPE1" : setArtist(input.readStringOfLengthWithCharsetByte(fsize)); break; //check whether TPE2 is correct
 			case "TYER" : setYear(input.readStringOfLengthWithCharsetByte(fsize)); break;
 			case "APIC" : parsePic(input, fsize); break;
 			default: byte[] buff2  = new byte[fsize]; input.read(buff2); break; //TODO: store these unknown tags for rewrite
 		}
 	}
 	
 	private void parsePic(ID3InputStream input, int fsize) throws FileNotFoundException, IOException {
 		Charset cs = input.readCharset();
 		String mime = input.readStringUntilZero(cs);
 		int picType = input.readUnsignedByte();
 		if(picType != 0x03) 
 			return;
 		String desc = input.readStringUntilZero(cs);
 		byte buff[] = new byte[fsize-1-mime.length()-1-1-desc.length()-1];
 		input.read(buff);
 		input.readPadding();
 		ByteArrayInputStream myByteBuffer = new ByteArrayInputStream(buff);
 		MemoryCacheImageInputStream imgInputStream = new MemoryCacheImageInputStream(myByteBuffer);
 		cover = ImageIO.read(imgInputStream);
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		if(title == null) {
 			tryToParse();
 		}
 		return title;
 	}
 
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the artist
 	 */
 	public String getArtist() {
 		if(artist == null) {
 			tryToParse();
 		}
 		return artist;
 	}
 
 	/**
 	 * @param artist the artist to set
 	 */
 	public void setArtist(String artist) {
 		this.artist = artist;
 	}
 
 	/**
 	 * @return the album
 	 */
 	public String getAlbum() {
 		if(album == null) {
 			tryToParse();
 		}
 		return album;
 	}
 
 	/**
 	 * @param album the album to set
 	 */
 	public void setAlbum(String album) {
 		this.album = album;
 	}
 
 	/**
 	 * @return the year
 	 */
 	public String getYear() {
 		if(year == null) {
 			tryToParse();
 		}
 		return year;
 	}
 
 	/**
 	 * @param year the year to set
 	 */
 	public void setYear(String year) {
 		this.year = year;
 	}
 
 	/**
 	 * @return the cover
 	 */
 	public BufferedImage getCover() {
 		if(cover == null) {
 			tryToParse();
 		}
 		return cover;
 	}
 
 	/**
 	 * @param cover the cover to set
 	 */
 	public void setCover(BufferedImage cover) {
 		this.cover = cover;
 	}
 	
 }
