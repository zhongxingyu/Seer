 package Messages;
 
 
 public class UDPBroadcastMessage extends Message {
 	public String senderUsername=null;
 	public String targetUsername=null;
 	public String senderIP=null;
         
 	public static final long minSize=271;//260;
         
 	public UDPBroadcastMessage(int _op,long _length,long _reserved,String _options,byte[] body){
 		super(_op,_length,_reserved,_options);
 		processBody(body);
 		if(op!=1){
 			correct=false;
 		}
 	}
 	public UDPBroadcastMessage(int _op,long _length,long _reserved,String _options,String _senderUsername,String _targetUsername,String _senderIP){
 		super(_op,_length,_reserved,_options);
 		senderUsername=_senderUsername;
 		targetUsername= _targetUsername;
 		senderIP=_senderIP;
 		if(op!=1){
 			correct=false;
 		}
 	}
         
 	private void processBody(byte[] body){
 		if(body.length!=minSize){
 			correct=false;
 			return;
 		}
 
                 byte [] senderUserArray = new byte[128];
                 for (int i = 0; i < body.length && i < 128; i++){
                     senderUserArray[i] = body[i];
                 }
 		senderUsername=new String(senderUserArray,0,senderUserArray.length);
 		
 		int offset=128;
		byte [] ipArray = new byte[11];
        for (int i = 0; i < body.length && i < 11; i++){
             ipArray[i] = body[i+offset];
         }
         senderIP=new String(ipArray,0,ipArray.length);
 		 
        offset+=11;
                 byte [] targetUserArray = new byte[128];
                 for (int i = 0; i < body.length && i < 128; i++){
                     targetUserArray[i] = body[offset+i];
                 }
 		targetUsername=new String(targetUserArray,0,targetUserArray.length);
 		
 	}
         
 	public byte[] convert(){
 		byte[] upper=super.convert();
 		byte[] storage=new byte[(int) (upper.length+minSize)];
 		for(int i=0;i<upper.length;i++){
 			storage[i]=upper[i];
 		}
 		
                 int total=upper.length;
 
 		byte[] tmp=null;
 		
                 tmp=senderUsername.getBytes();
 		for(int i=0;i<tmp.length;i++){
 			storage[total+i]=tmp[i];
 		}
                 
 		total+=128;
                 
 		tmp=senderIP.getBytes();
 		for(int i=0;i<tmp.length;i++){
 			storage[total+i]=tmp[i];
 		}
                 
 		total+=15;
                 
                 tmp=targetUsername.getBytes();
                 for(int i=0;i<tmp.length;i++){
 			storage[total+i]=tmp[i];
 		}
                 
 		total+=128;
                 
 		return storage;
 	}
 }
