 package node.rpc;
 import java.util.Vector;
 
 public class RPCMain {
 	public static void main(String args[]) {
 		Vector<String> params = new Vector<String>();
 		params.add("hey");
 		params.add("jude");
 		
 		StringBuffer sb = RPCNode.serialize(new RPCMethodCall("foo", params));
 		System.out.println(sb.toString());
 		
 		RPCMethodCall methodCall = RPCNode.parseString(sb);
 		
		long i1 = 0x12345678;
		long i2 = 0xdeadb33f;
		long il = 0x00000000ffffffffL & (long)i1;
		il = ((0x00000000ffffffffL & (long)i1) << 32L) | (0x00000000ffffffffL & (long)i2);
		String str = Long.toHexString(il);
		System.out.println(il);
 	}
 }
