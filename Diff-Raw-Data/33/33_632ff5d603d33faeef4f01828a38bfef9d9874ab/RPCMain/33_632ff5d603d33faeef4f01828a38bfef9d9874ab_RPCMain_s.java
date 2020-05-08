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
 		
		byte[] n = new byte[2];
		String classStr = n.getClass().toString();
		System.out.println(classStr);
		
		try {
			Class c = Class.forName("[B");
			System.out.println(c);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 }
