 package com.tigris.adk.pedometer;
 
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import com.android.future.usb.UsbAccessory;
 import com.android.future.usb.UsbManager;
 
 import android.os.ParcelFileDescriptor;
 import android.util.Log;
 
 public class AdkHandler implements Runnable {
 
 	
     ParcelFileDescriptor mFileDescriptor;
     FileInputStream mInputStream;
     FileOutputStream mOutputStream;
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 	}
 	
 	public void open(UsbManager usbManager, UsbAccessory accessory){
 	     mFileDescriptor = usbManager.openAccessory(accessory);
 	     if (mFileDescriptor != null) {
 	          FileDescriptor fd = mFileDescriptor.getFileDescriptor();
 	          mInputStream = new FileInputStream(fd);
 	          mOutputStream = new FileOutputStream(fd);
 
 	          // ׼ ϴ ڵ  尡 lock  ʵ   Ѵ.
 	          // 忡 FileInputStream ü  ׼κ ͸ д ۾ Ѵ.
 	          // ̴  ¿ ϵ ϰڴ.
 	          /*
 	          Thread thread = new Thread(null, this, "ADK Example");
 	          thread.start();
 	          */
 	          Log.d(AdkHandler.class.getName(), "  ");
 	     } else {
 	          Log.d(AdkHandler.class.getName(), "  ");
 	     }
 	}
 	
 	public void close(){
 	     try {
 	          if (mFileDescriptor != null) {
 	               mFileDescriptor.close();
 	          }
 	     } catch (IOException e) {
 	     } finally {
 	          mFileDescriptor = null;
 	          mInputStream = null;
 	          mOutputStream = null;
 	     }
 	}
 	
 	public boolean isConnected(){
       return (mInputStream != null && mOutputStream != null);
 	}
 	
 	public void write(byte command, byte target, int value) {
	     byte[] buffer = new byte[1024];
 	     if (value > 255)
 	          value = 255; //  ͸ 0, 1 ϰ Ƴα ʹ 0 ~ 255     ִ.
 
 	     buffer[0] = command;
 	     buffer[1] = target;
 	     buffer[2] = (byte) value;
 	     if (mOutputStream != null && buffer[1] != -1) {
 	          try {
 	               mOutputStream.write(buffer);
 	          } catch (IOException e) {
 	               Log.e(AdkHandler.class.getName(), "write failed", e);
 	          }
 	     }
 	}
 }
