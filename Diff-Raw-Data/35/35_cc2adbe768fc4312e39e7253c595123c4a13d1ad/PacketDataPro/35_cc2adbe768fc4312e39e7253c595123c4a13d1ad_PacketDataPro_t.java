 package org.sdu.server;
 
 import java.nio.ByteBuffer;
 
 /**
  * 
  */
 public class PacketDataPro {
 	private ByteBuffer opt;
 	public PacketDataPro(ByteBuffer indata){
 		opt = indata;
 	}
 	public int GetLength(){
 		return opt.capacity();
 	}
 	public int GetFInst(){
 		return opt.get(0);
 	}
 	public int GetSInst(){
 		opt.position(2);
 		return opt.get(1);
 	}
 	public String GetParam(){
 		int param_length;
 		byte[] tmp1 = new byte[1000];
 		String tmp;
 			if (opt.get(opt.position())== 0x05) {
 				param_length = (opt.get(opt.position()+1) << 8)+opt.get(opt.position()+2);
 				opt.position(opt.position()+3);
				for (int i=opt.position();i<=opt.position()+param_length-1;i++){
 					tmp1[i-opt.position()] = opt.get(i);
 				}
				tmp = new String(tmp1,0,param_length);
				if (opt.position()+param_length < opt.capacity()-1) opt.position(opt.position()+param_length);
 		}
 			else{
 				tmp = "";
 			}
 		return tmp;
 	}
 	public byte[] GetParamB(){
 		int param_length;
 		int a;
 		byte[] tmp;
 			if (opt.get(opt.position())== 0x05) {
 				a = opt.position();
 				param_length = (opt.get(a+1) << 8)+opt.get(a+2);
 				tmp = new byte[param_length];	
 				a = opt.position()+3;
 				opt.position(a);
				for (int i=opt.position();i<=opt.position()+param_length-1;i++){
 					tmp[i-opt.position()] = opt.get(i);
 				}
				if (opt.position()+param_length < opt.capacity()-1) opt.position(opt.position()+param_length);
 				}
 			else{
 				tmp = null;
 			}
 		return tmp;
 	}
 }
