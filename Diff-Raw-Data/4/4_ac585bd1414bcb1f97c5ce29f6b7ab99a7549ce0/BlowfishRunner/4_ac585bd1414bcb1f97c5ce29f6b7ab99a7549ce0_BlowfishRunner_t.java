 package implementation;
 
 
 import java.util.Arrays;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.File;
import java.io.IOException;
 
 import edu.rit.util.Hex;
 import edu.rit.util.Packing;
 
 /**
  * uses Electronic Codebook Mode to encrypt/decrypt a file
  * @param args	array of length 4 containing the key, plaintext file to read, and ciphertext file to write, respectively
  */
 public class BlowfishRunner
 {
	public static void main(String[] args) throws IOException
 	{
 		BlockCipher cipher = new Blowfish01();
 
 		byte[] plaintext = new byte[8];
 		byte[] key = new byte[8];
 
 		if(args.length!=3)
 		{
 			Arrays.fill(key, (byte)0x00);
 			Arrays.fill(plaintext, (byte)0x00);
 		}
 		else key=Hex.toByteArray(args[0]);
 		
 		cipher.setKey(key);
 
 		FileInputStream read=null;
 		FileOutputStream write=null;
 		
 		try
 		{
 			read=new FileInputStream(new File(args[1]));
 			File outfile=new File(args[2]);
 			if(!outfile.exists())
 			{
 				outfile.createNewFile();
 			}
 			//write=new FileOutputStream();
 		}
 		catch(java.io.FileNotFoundException e)
 		{
 			//System.out.println("File not Found: " + args[1]);
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		try
 		{
 			while(read.available()>0)
 			{
 				boolean write0s=false;
 		
 				int i=read.read(plaintext);
 				/*for(i=0; i<8 && read.available()>0; i++)
 				{
 					plaintext[i]=read.read();
 				}*/
 			
 				if(i<8)	//fill block with a 1 then rest 0s
 				{
 					plaintext[i++]=1;
 					if(i<8)
 						for(; i<8; i++) plaintext[i]=0;
 					else	//fill another block with all 0s
 						write0s=true;
 				}
 
 				cipher.encrypt(plaintext);		
 				System.out.println(Hex.toString(plaintext));
 			
 				write.write(plaintext);
 			
 				if(write0s)	//add a final plaintext block, padded with 0s
 				{
 					for(i=0; i<8; i++) plaintext[i]=0;
 					cipher.encrypt(plaintext);
 					write.write(plaintext);
 				}
 			
 				//cipher.decrypt(plaintext);
 				//System.out.println(Hex.toString(plaintext));
 			
 				read.close();
 				write.close();
 			}
 		}
 		catch(java.io.IOException e)
 		{
 			System.out.println("Bad stuff: \n");
 			e.printStackTrace();
 		}
 	}
 }
