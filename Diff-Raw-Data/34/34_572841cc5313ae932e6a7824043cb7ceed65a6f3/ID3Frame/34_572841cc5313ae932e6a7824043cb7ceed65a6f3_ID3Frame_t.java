 package dj;
 
 import java.io.RandomAccessFile;
 import djExceptions.ID3Exception;
 import djExceptions.ID3FileException;
 
 public class ID3Frame {
 	
 	private ID3FrameHeader header;
 	private byte body[];
 	private byte groupId, encryptionType;
 	private int dataLengthIndicator;
 	private RandomAccessFile ras;
 	private int offset = 0;
 	
 	//text encoding types
 	public static final byte 
 							ISO_8859_1 = 0,
 							UTF_16 = 2,
 							UTF_16BE = 3,
 							UTF_8 = 4;
 	
 	public ID3Frame( ID3FrameHeader h, RandomAccessFile r) throws ID3Exception
 	{
 		ras = r;
 		header = h;
 		
 		try{
 			//these things _never_ exist but i need to test for them anyway
 			if( header.flagIsSet( ID3FrameHeader.IsPartOfGroup ) ){
 				groupId = ras.readByte();
 				offset++;
 			}
 			
 			if( header.flagIsSet( ID3FrameHeader.IsEncrypted) ){
 				encryptionType = ras.readByte();
 				offset++;
 			}
 			
 			if( header.flagIsSet(ID3FrameHeader.HasDataLengthIndicator)){
 				dataLengthIndicator = ID3Bitwise.compileIntFromSyncsafe(ras.readByte(), ras.readByte(), ras.readByte(), ras.readByte());
				offset+=4;
 			}
 			
 			body = new byte[ h.size() - offset ];
 			ras.readFully(body);	
 		}
 		catch(Exception e){
 			throw new ID3FileException("internal error occurred while reading ID3Frame");
 		}		
 	}
 	
 	public ID3FrameHeader getHeader()
 	{
 		return header;
 	}
 	
 	public byte getGroupIdentity()
 	{		
 		return groupId;
 	}
 	
 	public byte getEncryptionType()
 	{
 		return encryptionType;
 	}
 	
 	public int getDataLengthIndicator()
 	{		
 		return dataLengthIndicator;
 	}
 	
 	public byte getTextEncoding()
 	{
 		//gibberish if not a text frame, should implement an exception of some sort
 		return body[0];
 	}
 	
 	public String getText()
 	{
 		//gibberish if not a text frame
 		String out = "";
 		
 		for(int x=1; x < body.length; x++)
 			if(body[x] > 0)
 				out += (char)body[x];
 			
 		return out;
 	}
 	
 	public byte[] getRawData()
 	{
 		return body;
 	}
 }
