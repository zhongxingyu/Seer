 package nl.sidn.dnslib.message.records;
 
 import nl.sidn.dnslib.message.util.NetworkData;
 
 public class TXTResourceRecord extends AbstractResourceRecord {
 	
 	private static final long serialVersionUID = 1L;
 	
 	protected String value;
	protected byte[] data;
 
 
 	@Override
 	public void decode(NetworkData buffer) {
 		super.decode(buffer);
 	
		data = new byte[rdLength];
 		buffer.readBytes(data);
 		
 		value = new String(data);
 		
 
 	}
 
 	@Override
 	public void encode(NetworkData buffer) {
 		super.encode(buffer);
 
 		//write rdlength
 		buffer.writeChar(value.length() );
 		
 		buffer.writeBytes(value.getBytes());
 		
 	}
 	
 	public String getCacheId(){
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		return "TXTResourceRecord [value=" + value + "]";
 	}
 
 
 	@Override
 	public String toZone(int maxLength) {
 		return super.toZone(maxLength) + "\t" + value;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 
 }
