 import java.io.*;
 import java.net.*;
 import static java.lang.System.*;
 import javax.swing.JOptionPane;
 
 public class Client_App {
 	private static int alpha = 2521;
 	private static int p = 10859;
 	private static int client_rnd_seed;
 	private static String Secret_key;
 	public Client_App(){};
 	public static void Execute(String id, String passwd, String path) throws IOException{
 		System.out.println("Welcome to Securoid!");
 		
 		Socket sock = new Socket("14.63.198.240", 1988);
 		System.out.println("Attempt to make connection to address, 14.63.198.240 with port number 1988");
 		//making socket for client with temperature ip addr. and port number 1988 which i defined in socket_server class
 				
 		PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
 		//print writer which sends messages via socket
 		BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
 		//buffered reader which gets messages from socket
 		
 		System.out.println("Success");
 		
 		int type = 0;
 		String device_id = "1234567891011121";
 		//just hard-coded device_id
 		
 		
 		int r = 0;
 		String otp_key = null;
 		String key = "";
 		//r, otp_key, key will be taken from the server
 		//with some processes
 		
 		String snd_packet = "";
 		byte data[] = new byte[16];
 		//variables for sending
 
 		String rcv_packet = "";
 		String rcv_id;
 		int rcv_type;
 		String rcv_data, rcv_data2 = "";
 		//variables for received packet
 				
 		Boolean success = false;
 		int cnt = 0;
 		//initial partition
 		
 		while(!success){
 			if(cnt > 3)
 				break;
 			//this loop will be ended when the process is success or process is failed for trois(3 times)
 			
 			if(type == 0){
 				
 				client_rnd_seed = random_client_rnd_seed();
 				client_rnd_seed = (int) Math.pow(alpha,client_rnd_seed)%p;
 				snd_packet = id + " " + String.valueOf(type) + " " + client_rnd_seed;
 				pw.println(snd_packet);
 				
 				type++;
 				
 			}//first handshake
 			
 			rcv_packet = br.readLine();
 			if(rcv_packet == null)
 				continue;
 			
 			String[] toks = rcv_packet.split(" ");
 			//packet is splited into three positions
 			//1. client's ID
 			//2. packet's type
 			// 0 : pre-handshake
 			// 1 : log-in process
 			// 2 : otp process
 			//3. packet's data
 			
 			rcv_id = toks[0];
 			if(!rcv_id.equals(id))
 				continue;
 			rcv_type = Integer.parseInt(toks[1]);
 			if(rcv_type==4){
 				JOptionPane.showMessageDialog(null, "Can not find User information");
 				break;
 			} else {
 				rcv_data = toks[2];
 
 				if (toks.length >= 4 && toks[3] != null)
 					rcv_data2 = toks[3];
 				
 				//pre-handshake for diffie hellman key exchange
 				if (rcv_type == 0) {
 					
 					int server_rnd_seed = Integer.parseInt(rcv_data);
 					Secret_key = Diffie_Hellman_Key(server_rnd_seed,
 							client_rnd_seed);
 
 					// --------------------------------------------------------------------------------------------------------------------
 					// Key generatioin check code start
 					System.out.println("server_rnd_seed : " + server_rnd_seed);
 					System.out.println("client_rnd_seed : " + client_rnd_seed);
 					System.out.println("generated_Key : " + Secret_key);
 					// --------------------------------------------------------------------------------------------------------------------
 					// Key generatioin check code end
 
 					//log-in process starting
 					
 					String passwd_Hash = "";
 					String passwd_Send = "";
 
 					Securoid_Hashing hash = new Securoid_Hashing();
 					//class for hashing
 					passwd_Hash = hash.MD5(passwd);
 					byte[] passwd_Output = new byte[16];
 					passwd_Output = SeedEncryption(hexToByteArray(passwd_Hash),
 							hexToByteArray(Secret_key));
 					//passwd from user's input is hashed and seed encrypted
 
 					passwd_Send = byteArrayToHex(passwd_Output);
 
 					System.out.println("=========passwd_Send.length : "
 							+ passwd_Send.length());
 					System.out.println("-------- Original Password : "
 							+ passwd_Send);
 
 					snd_packet = id + " " + String.valueOf(type) + " "
 							+ passwd_Send;
 
 					pw.println(snd_packet);
 					type++;
 				} else if (rcv_type == 1) {
 					// OTP Process
 					r = Integer.parseInt(byteArrayToHex(SeedDecryption(hexToByteArray(rcv_data),hexToByteArray(Secret_key))));
 					otp_key = byteArrayToHex(SeedDecryption(hexToByteArray(rcv_data2),hexToByteArray(Secret_key)));
 					//get encrypted r, otp_key
 					//so decrypt them
 
 					Securoid_Hashing hash = new Securoid_Hashing();
 
 					String tmp;
 					tmp = otp_key;
 					for (int i = 0; i < r; i++)
 						tmp = hash.MD5(tmp);
 					//r times hashing otp_key
 
 					String hashed_otp_key = byteArrayToHex(SeedEncryption(hexToByteArray(tmp),hexToByteArray(Secret_key)));
 					//and r times hashed otp_key is encrypted
 					
					System.out.println("=========hashed_otp_key.length : "
							+ hashed_otp_key.length());
 					System.out.println("-------- Original otp_key : "
 							+ otp_key);
 					
 					snd_packet = id + " " + String.valueOf(type) + " "
 							+ hashed_otp_key;
 					pw.println(snd_packet);
 					type++;
 					
 				} else if (rcv_type == 2) {
 					// OTP Authentication completed
 					byte[] tmp_data = SeedDecryption(hexToByteArray(rcv_data), hexToByteArray(Secret_key));
 					key = byteArrayToHex(SeedDecryption(tmp_data, hexToByteArray(otp_key))); 
 					
 					// finally we get the key for decryption
 					success = true;
 					System.out.println("Got password : " + key);
 
 					decrypt_File(path, key);
 					JOptionPane.showMessageDialog(null, "Securoid Works Done!");
 					//decrypt the file
 
 				} else {
 					//failure
 					type = 0;
 					cnt++;
 					// retry trois
 				}
 			}
 		}
 		
 		br.close();
 		pw.close();
 		sock.close();
 		//all should be closed after working
 	}
 	
 	public static int random_client_rnd_seed(){
 		int r;
 		int max = 100000;
 		int min = 1000;
 		
 		r = (int)(Math.random()*(max-min+1))+min;
 		
 		return r;
 	}//for handshake 0(sharing key)
 	
 	
 	// hex to byte[]
 	public static byte[] hexToByteArray(String hex) {
 	    if (hex == null || hex.length() == 0) {
 	        return null;
 	    }
 	 
 	    byte[] ba = new byte[hex.length() / 2];
 	    for (int i = 0; i < ba.length; i++) {
 	        ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
 	    }
 	    return ba;
 	}
 	 
 	// byte[] to hex
 	public static String byteArrayToHex(byte[] ba) {
 	    if (ba == null || ba.length == 0) {
 	        return null;
 	    }
 	 
 	    StringBuffer sb = new StringBuffer(ba.length * 2);
 	    String hexNumber;
 	    for (int x = 0; x < ba.length; x++) {
 	        hexNumber = "0" + Integer.toHexString(0xff & ba[x]);
 	 
 	        sb.append(hexNumber.substring(hexNumber.length() - 2));
 	    }
 	    return sb.toString();
 	}
 	
 	public static byte[] SeedEncryption(byte[] passwd_Input, byte[] encrypt_key) {
 		
 		byte[] passwd_Output= new byte[16];
 		int pdwRoundKey[] = new int[32];
 			
 		SeedX seed = new SeedX();
 		//round key array for seed algorithm
 				
 		seed.SeedRoundKey(pdwRoundKey, encrypt_key);
 		seed.SeedEncrypt(passwd_Input, pdwRoundKey, passwd_Output);
 		
 		return passwd_Output;
 	}
 	
 	public static byte[] SeedDecryption(byte[] decrypt_Input, byte[] decrypt_key) {
 		byte[] decrypt_Output= new byte[16];
 		int pdwRoundKey[] = new int[32];
 			
 		SeedX seed = new SeedX();
 		//round key array for seed algorithm
 				
 		seed.SeedRoundKey(pdwRoundKey, decrypt_key);
 		seed.SeedDecrypt(decrypt_Input, pdwRoundKey, decrypt_Output);
 		
 		return decrypt_Output;
 	}
 	
 	public static String Diffie_Hellman_Key(int key1, int key2){
 		double DH_key = Math.pow(key1,key2)%p;
 		
 		Securoid_Hashing hash = new Securoid_Hashing();
 		String key = hash.MD5(String.valueOf(DH_key));
 				
 		return key; 
 	}
 	
 	public static void decrypt_File(String path, String key) {
 		try {
 			FileInputStream in;
 			FileOutputStream out;
 			SeedX seed = new SeedX();
 
 			String encrypted = new String(path);
 			String decrypted = encrypted.substring(0, encrypted.length() - 14);
 			
 
 			in = new FileInputStream(encrypted);
 			out = new FileOutputStream(decrypted);
 
 			byte[] unmask_Buf = new byte[62682];
 			byte[] buf = new byte[16];
 			byte[] pbPlain = new byte[16];
 			int pdwRoundKey[] = new int[32];
 			byte[] decrypt_Key = hexToByteArray(key);
 
 			
 			in.read(unmask_Buf);
 			
 			seed.SeedRoundKey(pdwRoundKey, decrypt_Key);
 			while ((in.read(buf)) != -1) {
 				seed.SeedDecrypt(buf, pdwRoundKey, pbPlain);
 				out.write(pbPlain);
 			}
 
 			in.close();
 			out.close();
 
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
