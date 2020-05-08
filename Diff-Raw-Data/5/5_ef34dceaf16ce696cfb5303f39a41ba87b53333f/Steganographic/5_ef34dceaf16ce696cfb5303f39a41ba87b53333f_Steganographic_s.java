 package stega;
 import java.io.*;
 
 
 
 
 public class Steganographic {
 	private static int buffer_size_wav = 1024;
 	private static int buffer_size_msg = 1024;
 
 
 
 	public static void main(String[] args) {
 		String mode = args[0];
 		File payload = null;
 		File carrier = null;
 		File output = null;
 		FileInputStream inStr = null;
 		FileInputStream msgStr = null;
 		FileOutputStream outStr = null;
 		byte[] wavHeader = new byte[44];
 		byte[] payloadSize = null;
 		byte[] wavBuffer = null;
 		byte[] payloadBuffer = null;
 		byte[] processedBuffer = null;
 		boolean littleEndian = true;
 		String resultMsg = null;
 
 		try {
 			carrier = new File(args[1]);
 			carrier.setReadOnly();
 
 			if (mode.equals("--encode")) {
 
 				payload = new File(args[2]);
 				output = new File(args[3]);
 				inStr = new FileInputStream(carrier);
 
 				inStr.read(wavHeader);
 
 				long payload_size = payload.length();
 				byte[] wavSize = new byte[4];
 
 				for (int i = 0; i < 4; i++) {
 					wavSize[i] = wavHeader[40 + i];
 				}
 
 				long wav_size = bytesToNumber(wavSize, true);
 
 				if (sizeChecking(payload_size, wav_size)) {
 					byte[] encodeArray = new byte[16];
 					payloadSize = numberToBytes(payload_size);
 					msgStr = new FileInputStream(payload);
 					outStr = new FileOutputStream(output);
 
 					outStr.write(wavHeader);
 
 					wavBuffer = new byte[buffer_size_wav];
 					payloadBuffer = new byte[buffer_size_msg];
 					processedBuffer = new byte[buffer_size_wav];
 
 					int wavBufferIndex = 0;
 					int wavBufferMax = 0;
 					int payloadBufferIndex = 0;
 					int payloadBufferMax = 0;
 
 					inStr.read(wavBuffer);
 					wavBufferMax = (int)Math.min(wav_size, buffer_size_wav);
 					wav_size -= buffer_size_wav;
 
 					//first, write in the size of the payload
 					for (int i = 0; i < 4; i++) {
 						System.arraycopy(wavBuffer, wavBufferIndex, encodeArray, 0, 16);
 						ByteOp.encode(payloadSize[i], encodeArray, littleEndian);
 						System.arraycopy(encodeArray, 0, processedBuffer, wavBufferIndex, 16);
 						wavBufferIndex += 16;
 					}
 
 					//then, process the payload itself
 					while (payload_size > 0 || (payloadBufferIndex < payloadBufferMax)) {
 						if (payloadBufferIndex >= payloadBufferMax) {
 							msgStr.read(payloadBuffer);
 							payloadBufferIndex = 0;
 							payloadBufferMax = (int)Math.min(payload_size, buffer_size_msg);
 							payload_size -= buffer_size_msg;
 						}
 
 						while (wavBufferIndex < wavBufferMax && payloadBufferIndex < payloadBufferMax) {
 							System.arraycopy(wavBuffer, wavBufferIndex, encodeArray, 0, 16);
 							ByteOp.encode(payloadBuffer[payloadBufferIndex], encodeArray, littleEndian);
 							System.arraycopy(encodeArray, 0, processedBuffer, wavBufferIndex, 16);
 							wavBufferIndex += 16;
 							payloadBufferIndex++;
 						}
 
 						if (payload_size < 0 && payloadBufferIndex >= payloadBufferMax) {
 							//that's the end of the payload, fill the processed buffer
 							System.arraycopy(wavBuffer, wavBufferIndex, processedBuffer, wavBufferIndex, wavBufferMax - wavBufferIndex);
 						}
 
 
 
 						//if full, write processed buffer to output
 						if (wavBufferIndex >= wavBufferMax) {
 							outStr.write(processedBuffer, 0, wavBufferMax);
 
 							//if there's more, refill wav buffer
 
 							if (wav_size > 0) {
 								inStr.read(wavBuffer);
 
 								//update indices and maximums
 								wavBufferIndex = 0;
 								wavBufferMax = (int)Math.min(wav_size, buffer_size_wav);
 								wav_size -= buffer_size_wav;
 							}
 
 						}
 
 					}
 
 					//if the payload's done, copy over the rest of the wav file (if any)
 					if (wavBufferIndex < wavBufferMax) {//it's been reset; there's more
 						System.arraycopy(wavBuffer, wavBufferIndex, processedBuffer, wavBufferIndex, wavBufferMax - wavBufferIndex);
 						outStr.write(processedBuffer, 0, wavBufferMax);
 						while (wav_size > 0) {
 							inStr.read(wavBuffer);
 
 							//update indices and maximums
 							wavBufferIndex = 0;
 							wavBufferMax = (int)Math.min(wav_size, buffer_size_wav);
 							wav_size -= buffer_size_wav;
 							System.arraycopy(wavBuffer, wavBufferIndex, processedBuffer, wavBufferIndex, wavBufferMax - wavBufferIndex);
 							outStr.write(processedBuffer, 0, wavBufferMax);
 						}
 					}
 
 
 					//everything's written, you're done(?)
 					resultMsg = "Huzzah!";
 					/*don't need to worry about RIFX anymore
 
                     		   if ((char)wavHeader[3] == 'F') {
 
                     		   littleEndian = true;
 
                      		   }*/
 
 				}
 
 				else {
 					resultMsg = "Payload too large for carrier";
 				}
 
 
 
 			}else if (mode.equals("--decode")) {
 				// file where output should go
 				output = new File(args[3]);
 
 				// opens an input stream for carrier
 				inStr = new FileInputStream(carrier);
 
 				// opens a stream to the output file
 				outStr = new FileOutputStream(output);
 
 				// determines if carrier is RIFF or RIFX
 				// wavHeader = first 44 bytes
 				inStr.read(wavHeader);
 				if ((char)wavHeader[3] == 'F'){
 					littleEndian = true;
 				}
 				else{
 					littleEndian = false;
 				}
 
 				// gets the size of the carrier using final 4 bytes of wav header
 				byte[] wavSize = new byte[4];
 				for (int i = 0; i < 4; i++) {
 					wavSize[i] = wavHeader[40 + i];
 				}
 				// converts the wav size to a long
 				long wav_size = bytesToNumber(wavSize, true);
 
				// read in the carrier file 16 bytes at a time (one sample)
 				wavBuffer = new byte[16];
 				int carrierChunksRead = 0;
 
 				// a byte included in the message
 				byte messageByte;
 				// size of the message
 				byte[] messageSizeBytes = new byte[4];
 				long messageSize = 0;
 
 
 				while((messageSize==0 || sizeChecking(messageSize, wav_size)) ){
 					// Reset the messageByte to 0
 					messageByte = 0;
 
 					// Another 16-byte chunk has been read, add it to our count
 					inStr.read(wavBuffer);
 					carrierChunksRead++;
 
 					// retrieve the messageByte by calling decode
 					messageByte = ByteOp.decode(wavBuffer, littleEndian);
 
 					// if within the first 4, we're still reading the payload size
 					if(carrierChunksRead <=4){
 						messageSizeBytes[carrierChunksRead-1] = messageByte;
 					}
 					
					// if we just read the 4th sample, we can determine the message size
 					if(messageSize==0 && carrierChunksRead==4){
 						// first 4 bytes contain the message size
 						messageSize = bytesToNumber(messageSizeBytes, true);
 					}
 					
 					// We're at the actual message now, so write it to the output stream
 					if(carrierChunksRead > 4){
 						outStr.write(messageByte);
 					}
 				}
 			}
 
 
 
 
 			if (inStr != null) {
 				inStr.close();
 			}
 
 			if (outStr != null) {
 				outStr.flush();
 				outStr.close();
 			}
 
 		}
 
 		catch(Exception e) {
 			e.printStackTrace();
 			resultMsg = "SomeErrorMsg";
 		}
 
 		finally {
 			System.out.println(resultMsg);
 		}
 
 	}
 
 
 
 
 
 	private static boolean sizeChecking(long size_msg, long size_wav_data) {
 		int portion = 16;
 		boolean re = false;
 		//+4 to include the size, * portion given the spacing of the bits 
 		if((size_msg + 4)*portion <= size_wav_data) {
 			re = true;
 		}
 		return re;
 	}
 
 
 
 	//little endian
 	private static byte[] numberToBytes(long size) {
 		byte[] re = new byte[4];
 		for (int i = 0; i < 4; i++) {
 			re[i] = 0;
 			re[i] |= size >> (i*8);
 		}
 		return re;
 	}
 
 
 
 	//ridiculous kludge to work with little-endian, unsigned numbers in Java
 	private static long bytesToNumber(byte[] bytes, boolean littleEndian) {
 		long re = 0;
 		byte[] newBytes = new byte[4];
 
 		if (!littleEndian) {
 			newBytes = bytes.clone();
 		} else {
 			for (int i = 0; i < 4; i++) {
 				newBytes[i] = bytes[3 - i];
 			}
 		}
 		int i;
 		for (i = 0; i < 3; i++) {
 			re |= (0x00ff & (short)newBytes[i]);
 			re <<= 8;
 		}
 		re |= (0x00ff & (short)newBytes[i]);
 
 		return re;
 
 	}
 
 }
