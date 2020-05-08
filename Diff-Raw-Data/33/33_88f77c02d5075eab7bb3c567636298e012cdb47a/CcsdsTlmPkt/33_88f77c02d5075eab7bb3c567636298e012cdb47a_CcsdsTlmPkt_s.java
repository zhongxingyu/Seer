 package packets.ccsds;
 
 import packets.parameters.DataType;
 
 public class CcsdsTlmPkt extends CcsdsPkt {
 	public static final int CCSDS_IDX_TLM_HDR = 6;
 	public static final int CCSDS_IDX_TLM_DATA = 12;
 	public static final int CCSDS_TLM_HDR_LEN = 12;
 
 	/*
 	 * * Construct a packet from raw data stream
 	 */
 	public CcsdsTlmPkt(byte[] TlmMsg) {
 		super(TlmMsg);
 	}
 	
 	public static int getTime(byte[] data) {
		return Integer.parseInt(DataType.uint32Integer.getTlmStrArray(data,
 				CCSDS_IDX_TLM_HDR));
 	}
} // End class CcsdsTlmPkt
