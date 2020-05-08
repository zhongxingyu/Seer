 package Messages;
 
 public class Message {
 	int op=-1;
 	long length=-1;
 	long reserved=0;
 	String options="";
 	boolean correct;
 	public final static int minSize=132;
 	public Message(int _op,long _length,long _reserved,String _options){
 		op=_op;
 		length=_length;
 		reserved=_reserved;
 		options=_options;
 	}
 	
 	public boolean getCorrect(){
 		return correct;
 	}
 	protected long fromByteArray(byte[] bytes) {
 		long total=0;
 	    for(int i=0;i<bytes.length;i++){
 	    	total+=(long)i*Math.pow(2, 8*i);
 	    }
 	    return total;
 	}
 	public byte[] convert(){
 		byte[] storage=new byte[minSize];
 		int total=0;
 		byte[] temp=numToByte(op,1);
 		for(int i=0;i<temp.length;i++){
 			storage[total+i]=temp[i];
 		}
 		total+=temp.length;
 		temp=numToByte((int)length,2);
 		for(int i=0;i<temp.length;i++){
 			storage[total+i]=temp[i];
 		}
 		total+=temp.length;
 		temp=numToByte((int)reserved,1);
 		for(int i=0;i<temp.length;i++){
 			storage[total+i]=temp[i];
 		}
 		total+=temp.length;
 		temp=options.getBytes();
 		for(int i=0;i<temp.length;i++){
 			storage[total+i]=temp[i];
 		}
 		total+=temp.length;
 		return storage;
 	}
 	
 	public byte[] numToByte(int num,int numBytes){
 		String numstr=Integer.toBinaryString(num);
 		byte[] storage=new byte[numBytes];
		for(int i=0;i<numBytes && i<Integer.toString(num).length();i++){
			System.out.println(num);
			System.out.println(numstr.substring(numstr.length()-8*(i+1),numstr.length()-8*i));
 			storage[i]=Byte.decode(numstr.substring(numstr.length()-8*(i+1),numstr.length()-8*i));	
 		}
 		return storage;
 	}
 }
