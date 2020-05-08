 package dj;
 
 import java.io.RandomAccessFile;
 import java.util.Arrays;
 import djExceptions.ID3Exception;
 import djExceptions.ID3FileException;
 
 public class ID3FrameHeader {
 	
 	private byte body[];
 	private RandomAccessFile ras;
	private int size = 0;	
 	
 	public static final int 
 							
 							FrameHeaderSize = 10,
 							
 							/*fourth bit set for body[8], body[9] flag byte otherwise */
 	
 							TagAlterPreservation = 0xE0,
 							FileAlterPreservation = 0xD0,
 							IsReadOnly = 0xC0,
 							IsPartOfGroup = 6,
 							IsCompressed = 3,
 							IsEncrypted = 2,
 							IsUnsynchronised = 1,
 							HasDataLengthIndicator = 0;
 	
 	
 	public ID3FrameHeader( RandomAccessFile r)throws ID3Exception
 	{
 		ras = r;
 		body = new byte[FrameHeaderSize];
 		
 		try {
 			ras.readFully(body);
 		} 
 		catch (Exception e) {
 			throw new ID3FileException("internal error occurred while reading FrameHeader");
 		}
 		
		if( !isValid() ){
			return;
		}
		
 		//frame sizes are supposed to be stored in syncsafe bit patterns
 		//unfortunately this is not always the case
 		
 		size = ID3Bitwise.compileIntFromSyncsafe(body[4], body[5], body[6], body[7]);
 		byte next[] = new byte[4];
 		
 		try {
 			long place = ras.getFilePointer();
 			ras.skipBytes( size );
 			ras.readFully(next);
 			ras.seek(place);
 		} 
 		catch (Exception e) {
 			throw new ID3FileException("internal error occurred while reading FrameHeader");
 		}
 		
 		if( !(next[0]==0 && next[1]==0 && next[2]==0 && next[3]==0) && !isvalid(next) ){
 			size = ID3Bitwise.compileIntFromUnsyncsafe(body[4], body[5], body[6], body[7]); // liars
 		}
 	}
 	
 	public boolean isValid()
 	{
 		return isvalid(body);
 	}
 	
 	public String getFrameID()
 	{
 		return new String(Arrays.copyOfRange(body, 0, 4));
 	}
 	
 	public int size()
 	{
 		return size;
 	}
 	
 	public boolean flagIsSet(int flag)
 	{
 		if((flag & 8) > 0){
 			flag &= ~8;
 			return (body[8] & (1 << flag)) > 0;
 		}
 		
 		return (body[9] & (1 << flag)) > 0;
 	}
 	
	public boolean isvalid(byte b[])
 	{
 		return (b[0] >= 'A' && b[0] <= 'Z') &&
 		((b[1] >= 'A' && b[1] <= 'Z') || (b[1] >= '0' && b[1] <= '9')) &&
 		((b[2] >= 'A' && b[2] <= 'Z') || (b[2] >= '0' && b[2] <= '9')) &&
 		((b[3] >= 'A' && b[3] <= 'Z') || (b[3] >= '0' && b[3] <= '9'));		 
 	}
 	
 	public byte[] getRawData()
 	{
 		return body;
 	}
 }
