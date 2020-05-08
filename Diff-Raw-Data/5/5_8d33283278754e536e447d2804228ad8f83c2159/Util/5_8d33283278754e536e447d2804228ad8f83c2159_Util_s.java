 package com.quanleimu.util;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.StringEntity;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.Point;
 import android.graphics.Bitmap.CompressFormat;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo.State;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Display;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.Paint;
 public class Util {
 	private static String[] keys;
 	private static String[] values;
 	
 	public static String qq_access_key="";
 	public static String qq_access_secret="";
 	/**
 	 * 
 	 * @author henry_young
 	 * @throws IOException
 	 * 
 	 * @保存方式：Stream 数据流方式
 	 * 
 	 * writeUTF(String str); 但是用Data包装后就会支持。
 	 * 
 	 * @操作模式: Context.MODE_PRIVATE：新内容覆盖原内容
 	 * 
 	 *        Context.MODE_APPEND：新内容追加到原内容后
 	 * 
 	 *        Context.MODE_WORLD_READABLE：允许其他应用程序读取
 	 * 
 	 *        Context.MODE_WORLD_WRITEABLE：允许其他应用程序写入，会覆盖原数据。
 	 */
 	
 	//数据保存SD卡
 	public static String saveDataToSdCard(String path, String file,
 			Object object) {
 		String res = null;
 		FileOutputStream fos = null;
 		ObjectOutputStream oos = null;
 		if (Environment.getExternalStorageState() != null) {
 			try {
 				File p = new File("/sdcard/" + path); // 创建目录
 				File f = new File("/sdcard/" + path + "/" + file + ".txt"); // 创建文件
 				if (!p.exists()) {
 					p.mkdir();
 				}
 				if (!f.exists()) {
 					f.createNewFile();
 				}
 				fos = new FileOutputStream(f);
 				oos = new ObjectOutputStream(fos);
 				oos.writeObject(object);
 				System.out.println(fos + "fos");
 				System.out.println(oos + "oos");
 			} catch (FileNotFoundException e) {
 				res = "没有找到文件";
 				e.printStackTrace();
 			} catch (IOException e) {
 				res = "没有数据";
 				e.printStackTrace();
 			} finally {
 				try {
 					oos.close();
 					fos.close();
 					res = "保存成功";
 				} catch (IOException e) {
 					res = "没有数据";
 					e.printStackTrace();
 				}
 			}
 		}
 		return res;
 	}
 
 	//从SD卡读取数据
 	public static Object loadDataFromSdCard(String path, String file) {
 		Object obj = null;
 		FileInputStream fis = null;
 		ObjectInputStream ois = null;
 		if (Environment.getExternalStorageState() != null) {
 			try {
 				fis = new FileInputStream("/sdcard/" + path + "/" + file
 						+ ".txt");
 				ois = new ObjectInputStream(fis);
 				obj = ois.readObject();
 			} catch (FileNotFoundException e) {
 				obj = null;
 				e.printStackTrace();
 			} catch (IOException e) {
 				obj = null;
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				obj = null;
 				e.printStackTrace();
 			} finally {
 				try {
 					ois.close();
 					fis.close();
 				} catch (Exception e) {
 					obj = null;
 					e.printStackTrace();
 				}
 			}
 		}
 		return obj;
 	}
 
 	//将数据保存到手机内存中
 	public static String saveDataToLocate(Context context, String file,
 			Object object) {
 		if(file != null && !file.equals("") && file.charAt(0) != '_'){
 			file = "_" + file;
 		}
 		String res = null;
 		FileOutputStream fos = null;
 		ObjectOutputStream oos = null;
 		try {
 			fos = context.openFileOutput(file, Activity.MODE_PRIVATE);
 			oos = new ObjectOutputStream(fos);
 			oos.writeObject(object);
 			System.out.println(fos + "fos");
 			System.out.println(oos + "oos");
 		} catch (FileNotFoundException e) {
 			res = "没有找到文件";
 			e.printStackTrace();
 		} catch (IOException e) {
 			res = "没有数据";
 			e.printStackTrace();
 		} finally {
 			try {
 				oos.close();
 				fos.close();
 				res = "保存成功";
 			} catch (IOException e) {
 				res = "没有数据";
 				e.printStackTrace();
 			}
 		}
 		return res;
 	}
 
 	//将数据从手机内存中读出来
 	public static Object loadDataFromLocate(Context context,String file) {
 		if(file != null && !file.equals("") && file.charAt(0) != '_'){
 			file = "_" + file;
 		}
 		Object obj = null;
 		FileInputStream fis = null;
 		ObjectInputStream ois = null;
 		try {
 			fis = context.openFileInput(file);
 			ois = new ObjectInputStream(fis);
 			obj = ois.readObject();
 			
 		} catch (FileNotFoundException e) {
 			obj = null;
 			System.out.println("文件没找到------>"+e.toString());
 			e.printStackTrace();
 		} catch (IOException e) {
 			obj = null;
 			System.out.println("输入输出错误------>"+e.toString());
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			obj = null;
 			System.out.println("类型转换错误------>"+e.toString());
 			e.printStackTrace();
 		}catch (Exception e) {
 			obj = null;
 			System.out.println("异常------>"+e.toString());
 			e.printStackTrace();
 		} finally {
 			try {
 				ois.close();
 				fis.close();
 			} catch (Exception e) {
 				obj = null;
 				e.printStackTrace();
 			}
 		}
 		return obj;
 	}
 	
 	public String[] getKeys() {
 		return keys;
 	}
 	public static void setKeys(Object...args) {
 		keys = new String[args.length];
 		for(int i=0;i<args.length;i++){
 			keys[i] = (String) args[i];
 		}
 	}
 	public static String[] getValues() {
 		return values;
 	}
 	public static void setValues(Object...args) {
 		values = new String[args.length];
 		for(int i=0;i<args.length;i++){
 			values[i] = (String) args[i];
 		}
 	}
 	
 	//����vkey1
 	public static String getStr(){
 		String vkey  = "";
 		//vkey = "api_key=" + Const.API_KEY;
 		for(int i=0;i<keys.length;i++){
 			vkey =vkey + "&" + keys[i] +"="+ values[i];
 		}
 		vkey = vkey.substring(1);
 		String vkey1 = vkey;
 		vkey = vkey + Const.API_SECRET;
 		System.out.println("vkey--->" + vkey);
 		vkey1 = vkey1 + "&access_token=" + MD5(vkey);
 		
 		System.out.println(vkey1);
 		return vkey1;	
 	}
 
 	//MD5加密
 	public  static String MD5(String inStr) {
 		MessageDigest md5 = null;
 		try {
 			md5 = MessageDigest.getInstance("MD5");
 		} catch (Exception e) {
 			//System.out.println(e.toString());
 			e.printStackTrace();
 			return "";
 		}
 		char[] charArray = inStr.toCharArray();
 		byte[] byteArray = new byte[charArray.length];
 
 		for (int i = 0; i < charArray.length; i++)
 			byteArray[i] = (byte) charArray[i];
 		byte[] md5Bytes = md5.digest(byteArray);
 		StringBuffer hexValue = new StringBuffer();
 		for (int i = 0; i < md5Bytes.length; i++) {
 			int val = ((int) md5Bytes[i]) & 0xff;
 			if (val < 16)
 				hexValue.append("0");
 			hexValue.append(Integer.toHexString(val));
 		}
 		return hexValue.toString();
 	}
 	
 	
 	
 	
 	
 	
 	public static String getJsonDataFromURLByPost(String path,String params) throws SocketTimeoutException, UnknownHostException {
 		//����һ��URL����
 //		HttpURLConnection urlCon = null;
 		BufferedReader reader = null ;
 		String str = "";
 		try {
 //			URL url = new URL(path);
 //			urlCon = (HttpURLConnection) url.openConnection();
 //			urlCon.setDoOutput(true);
 //			urlCon.setDoInput(true);
 //			urlCon.setRequestMethod("POST");
 //			urlCon.setUseCaches(false);
 //			urlCon.setInstanceFollowRedirects(true);
 //			urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 //			urlCon.connect();
 			
 			
 			HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
             
             HttpPost httpPost = new HttpPost(path); 
             
             ByteArrayEntity stringEntity = new ByteArrayEntity(params.getBytes());
             stringEntity.setContentType("application/x-www-form-urlencoded");
             httpPost.setEntity(stringEntity);
             
             HttpResponse response = httpClient.execute(httpPost);
             
 //			DataOutputStream out = new DataOutputStream(urlCon.getOutputStream());
 //			
 //			out.writeBytes(params);
 //			
 //			out.flush();
 //			out.close();
 			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 			String temp = "";
 			while((temp=reader.readLine())!=null){
 				str += (temp + "\n");
 			}
 			
 			httpClient.getConnectionManager().shutdown();
 		}catch(SocketTimeoutException ste){
 			Log.e("��ʱ", "��ʱ��");
 				throw ste; 
 		}catch(UnknownHostException h){
 			Log.e("��·", "���粻��ͨ");
 			throw h;
 		
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if(reader!=null)
 					reader.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 //			urlCon.disconnect();
 		}
 		Log.d("json", "activityjson--->" + str);
 		return str;
 	}
 	
 	//post�ύ����
 	public static String sendString(String str, String urlString) throws IOException{
 		DataInputStream dis = null;
 		String readUrl = "";
 		try {
 //			URL url = new URL(urlString);
 //			HttpURLConnection urlConn = (HttpURLConnection) url
 //					.openConnection();
 //			urlConn.setRequestMethod("POST");
 //			urlConn.setDoOutput(true);
 //			urlConn.setDoInput(true);
 //			OutputStream os = urlConn.getOutputStream();
 //			os.write(str.getBytes());
 			
 			
 			HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
             
             HttpPost httpPost = new HttpPost(urlString); 
             
             StringEntity stringEntity = new StringEntity(str, "UTF-8");
             httpPost.setEntity(stringEntity);
             
             HttpResponse response = httpClient.execute(httpPost);
             
 			dis = new DataInputStream(response.getEntity().getContent());
 			readUrl = dis.readLine();
 			dis.close();
 //			os.close();
 			
 			httpClient.getConnectionManager().shutdown();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return readUrl;
 	} 
 	
 	public static String saveBitmapToSdCard(String path,String name,Bitmap bitmap) {
 		String res = null;
 		FileOutputStream fos = null; 
 		if (Environment.getExternalStorageState() != null) {
 			try {
 				File p = new File("/sdcard/" + "quanleimu"); // 创建目录
 				File s = new File("/sdcard/" + "quanleimu" + "/" + path); // 创建目录
 				File f = new File("/sdcard/" + "quanleimu" + "/" + path + "/" + name + ".png"); // 创建文件
 				if (!p.exists()) {
 					p.mkdir();
 				}
 				if (!s.exists()) {
 					s.mkdir();
 				}
 				if (!f.exists()) {
 					f.createNewFile();
 				}
 				fos = new FileOutputStream(f);
 				
 				bitmap.compress(CompressFormat.JPEG, 100, fos);
 				fos.close();
 				res = f.getAbsolutePath();
 			} catch (FileNotFoundException e) {
 				res = "没有找到文件";
 				e.printStackTrace();
 			} catch (Exception e) {
 				res = "SD卡未安装";
 				e.printStackTrace();
 			}
 		}else{
 			res = "无SD卡";
 		}
 		return res;
 	}
 	
 	public static List<Bitmap> loadBitmapFromSdCard(String path,String name) {
 		List<Bitmap> b = new ArrayList<Bitmap>();
 		File file = new File("/sdcard/"+"quanleimu/"+path);
 		if (file != null) {
 			File[] files = file.listFiles();
 			if (files != null) {
 				for (File f : files) {
 					if(f.getAbsolutePath().contains(name)){
 					    BitmapFactory.Options o =  new BitmapFactory.Options();
 	                    o.inPurgeable = true;
 						b.add(BitmapFactory.decodeFile(f.getPath(), o));
 					}
 				}
 			}
 		}
 		b = replaceList(b);
 		return b;
 	}
 	
 	public static List<Bitmap> replaceList(List<Bitmap> bit) {
 		List<Bitmap> b = new ArrayList<Bitmap>();
 		for (int i = bit.size() - 1; i > -1; i--) {
 			b.add(bit.get(i));
 		}
 		return b;
 	}
 	
 	//下载图片
 	public static Bitmap getImage(String strURL)throws OutOfMemoryError{	
 		Log.d("img", strURL);
 				Bitmap img = null;
 				URLConnection conn = null;
 				try {
 					URL url = new URL(strURL);
 					conn = (URLConnection) url.openConnection();
 					BitmapFactory.Options o =  new BitmapFactory.Options();
 					o.inPurgeable = true;
 					Log.e("o", o.toString());
 					img = BitmapFactory.decodeStream(conn.getInputStream(), null, o);
 				}
 				catch (Exception e) { 
 					img = null;
 					e.printStackTrace();
 					//conn.disconnect();
 				}
 		return img;
 	}
 	
 	//将bitmap转成流存到file里面
 	public static void saveImage2File(Context context,Bitmap bmp,String fileName)
 	{
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
 		byte[] file = bos.toByteArray();
 		FileOutputStream fos = null;
 		try {
 			fos = context.openFileOutput(fileName, Activity.MODE_PRIVATE);
 			fos.write(file);
 			System.out.println("存储成功");
 			fos.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("FileNotFoundException ---- >" + e.toString());
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("IOException ---- >" + e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	//将流转成bitmap里面
 	public static Bitmap getBitmapFromInputstream(Context context,String fileName)
 	{
 		Bitmap bmp = null;
 		FileInputStream fis = null;
 		byte[] aa = null;
 		try {
 			fis = context.openFileInput(fileName);
 			
 			aa = new byte[fis.available()];
 			fis.read(aa);
 		} catch (FileNotFoundException e) {
 			fis = null;
 System.out.println("没找到文件");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if(fis == null)
 		{
 			bmp = null;
 		}
 		else
 		{
 //			bmp = BitmapFactory.decodeStream(fis);
 		    BitmapFactory.Options o =  new BitmapFactory.Options();
             o.inPurgeable = true;
 			bmp = BitmapFactory.decodeByteArray(aa, 0, aa.length, o);
 		}
 		return bmp;
 	}
 	
 	
 	//手机分辨率宽
 	public static int getWidth(Activity activity){
 		Display display = activity.getWindowManager().getDefaultDisplay();
 		int width = display.getWidth();
 		int a = 0;
 		if(width == 240)
 		{
 			a = 1;
 		}
 		else if(width == 320)
 		{
 			a = 2;
 		}
 		else if(width == 480)
 		{
 			a = 3;
 		}
 		else if(width == 540)
 		{
 			a = 4;
 		}
 		else if(width == 640)
 		{
 			a = 5;
 		}else{
 			a = 5;
 		}
 		
 		return a;
 	}
 	
 	public static int getWidthByContext(Context context){
 		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
 		int width = display.getWidth();
 //		int a = 0;
 //		if(width == 240)
 //		{
 //			a = 1;
 //		}
 //		else if(width == 320)
 //		{
 //			a = 2;
 //		}
 //		else if(width == 480)
 //		{
 //			a = 3;
 //		}
 //		else if(width == 540)
 //		{
 //			a = 4;
 //		}
 //		else if(width == 640)
 //		{
 //			a = 5;
 //		}
 		
 		return width;
 	}
 	
 	public static int getHeightByContext(Context context){
 		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
 		int height = display.getHeight();
 //		int a = 0;
 //		if(width == 240)
 //		{
 //			a = 1;
 //		}
 //		else if(width == 320)
 //		{
 //			a = 2;
 //		}
 //		else if(width == 480)
 //		{
 //			a = 3;
 //		}
 //		else if(width == 540)
 //		{
 //			a = 4;
 //		}
 //		else if(width == 640)
 //		{
 //			a = 5;
 //		}
 		
 		return height;
 	}
 	
 
 	//获得手机屏幕焦点
 	public static Point getSrccenPoint(Activity activity){
 		Display display = activity.getWindowManager().getDefaultDisplay();
 		return new Point(display.getWidth(), display.getHeight());
 	}
 	
     public static Bitmap newBitmap(Bitmap b, int w, int h)
     {
         float scaleWidth = 0;
         float scaleHeight = h;
         int width = b.getWidth();
         int height = b.getHeight();
         
         if (w == h)
         {
             int minValue = Math.min(b.getWidth(), b.getHeight());
             if (minValue >= w)
             {
                 float ratio = (float) w / (float) minValue;
                 scaleWidth = ratio;
                 scaleHeight = ratio;
             }
             else
             {
                 minValue = Math.max(b.getWidth(), b.getHeight());
                 float ratio = (float) w / (float) minValue;
                 scaleWidth = ratio;
                 scaleHeight = ratio;
             }
         }
         else
         {
             scaleWidth = ((float) w) / width;
             scaleHeight = ((float) h) / height;
         }
         
         Matrix matrix = new Matrix();
         matrix.postScale(scaleWidth, scaleHeight);// ����
         return Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);
     }
 	
 	//图片旋转
 	public static Bitmap rotate(Bitmap b, int degrees) {
 		if (degrees != 0 && b != null) {
 			Matrix m = new Matrix();
 			m.setRotate(degrees);
 			try {
 				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b
 						.getHeight(), m, true);
 				if (b != b2) {
 					b = b2;
 				}
 			} catch (OutOfMemoryError ex) {
 				ex.printStackTrace();
 			}
 		}
 		return b;
 	}
 	
 	//һ����ת
 	public static List<Bitmap> rotateList(List<Bitmap> bitmaps) {
 		List<Bitmap> bits = new ArrayList<Bitmap>();
 		for(Bitmap b : bitmaps){
 			bits.add(Util.rotate(b, -90));
 		}
 		return bits;
 	}
 	public static List<Bitmap> newBitmapList(List<Bitmap> bitmaps,Activity activity) {
 		List<Bitmap> bits = new ArrayList<Bitmap>();
 		for(Bitmap b : bitmaps){
 			if(Util.getWidth(activity)==1){
 				bits.add(Util.newBitmap(b, 90, 90));
 			}else if(Util.getWidth(activity)== 2){
 				bits.add(Util.newBitmap(b, 135, 135));
 			}
 			else if(Util.getWidth(activity)== 3){
 				bits.add(Util.newBitmap(b, 60, 60));
 			}
 			else if(Util.getWidth(activity)== 4){
 				bits.add(Util.newBitmap(b, 160, 160));
 			}
 			else if(Util.getWidth(activity)== 5){
 				bits.add(Util.newBitmap(b, 180, 180));
 			}
 			
 		}
 		return bits;
 	}
 	
 	public static Bitmap scaleBitmap(Bitmap src, int outputX, int outputY, int leftMask, int topMask, int rightMask, int bottomMask){
 		Bitmap toRet = null;
 		BitmapFactory.Options o =  new BitmapFactory.Options();
 		o.inPurgeable = true;
 		if(src.getWidth() > outputX){
 			Bitmap scaledBmp = newBitmap(src, outputX, (int)((((float)outputX) / src.getWidth()) * src.getHeight()));
 			if(outputY <= scaledBmp.getHeight()) return scaledBmp;
 	
			Bitmap lineBk = Bitmap.createBitmap(outputX, 1, src.getConfig());
 			Canvas canvas = new Canvas(lineBk);
 			Rect srcRc = new Rect();
 			srcRc.left = 0;
 			srcRc.top = topMask;
 			srcRc.right = outputX;
 			srcRc.bottom = topMask + 1;
 			
 			Rect destRc = new Rect();
 			destRc.left = 0;
 			destRc.top = 0;
 			destRc.right = outputX;
 			destRc.bottom = 1;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			
			toRet = Bitmap.createBitmap(outputX, outputY, src.getConfig());
 			canvas = new Canvas(toRet);
 			srcRc.left = 0;
 			srcRc.top = 0;
 			srcRc.right = outputX;
 			srcRc.bottom = topMask;
 			
 			destRc.left = srcRc.left;
 			destRc.right = srcRc.right;
 			destRc.top = srcRc.top;
 			destRc.bottom = srcRc.bottom;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			srcRc.top = topMask;
 			srcRc.bottom = topMask + 1;
 			for(int i = 0; i < outputY - topMask - bottomMask; ++ i){
 				destRc.top = i + topMask;
 				destRc.bottom = i + topMask + 1;
 				canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			}
 			srcRc.top = scaledBmp.getHeight() - bottomMask;
 			srcRc.bottom = scaledBmp.getHeight();
 			destRc.top = outputY - bottomMask;
 			destRc.bottom = outputY;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			lineBk.recycle();
 			scaledBmp.recycle();
 		}
 		else
 		{
 			Bitmap scaledBmp = newBitmap(src, (int)((((float)outputY) / src.getHeight()) * src.getWidth()), outputY);
 	
 			Bitmap lineBk = Bitmap.createBitmap(1, outputY, src.getConfig());
 			Canvas canvas = new Canvas(lineBk);
 			Rect srcRc = new Rect();
 			srcRc.left = leftMask;
 			srcRc.top = 0;
 			srcRc.right = leftMask + 1;
 			srcRc.bottom = scaledBmp.getHeight();
 			
 			Rect destRc = new Rect();
 			destRc.left = 0;
 			destRc.top = 0;
 			destRc.right = 1;
 			destRc.bottom = outputY;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			
 			toRet = Bitmap.createBitmap(outputX, outputY, src.getConfig());
 			canvas = new Canvas(toRet);
 			srcRc.left = 0;
 			srcRc.top = 0;
 			srcRc.right = leftMask;
 			srcRc.bottom = scaledBmp.getHeight();
 			
 			destRc.left = 0;
 			destRc.right = leftMask;
 			destRc.top = 0;
 			destRc.bottom = outputY;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			srcRc.left = leftMask;
 			srcRc.right = leftMask + 1;
 			for(int i = 0; i < outputX - leftMask - rightMask; ++ i){
 				destRc.left = i + leftMask;
 				destRc.right = i + leftMask + 1;
 				canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			}
 			srcRc.left = scaledBmp.getWidth() - bottomMask;
 			srcRc.right = scaledBmp.getWidth();
 			destRc.left = outputX - rightMask;
 			destRc.right = outputX;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			lineBk.recycle();
 			scaledBmp.recycle();
 		}
 		return toRet;
 	}
 	
 	//保存数据手机内存
 	public static void saveToData(Context ctx , String title,String key,String value){
 		SharedPreferences.Editor se = ctx.getSharedPreferences(title, Context.MODE_WORLD_WRITEABLE).edit();
 		se.putString(key, value);
 		se.commit();
 	}
 	
 	//获取数据从手机内从
 	public static String getFromData(Context ctx , String title,String key){
 		SharedPreferences se = ctx.getSharedPreferences(title, Context.MODE_WORLD_READABLE);
 		if(se==null){
 			return null;
 		}
 		String value = se.getString(key, "");
 		if(value.equals("")){
 			return null;
 		}else{
 			return value;
 		}
 	}
 	
 	
 	/**
 	 * recycle all the bitmaps in the list
 	 * @param listBm
 	 */
 	public static void recycle(List<Bitmap> listBm){
 		for(int i=0;i<listBm.size();i++){
 			if(listBm!=null&&listBm.get(i)!=null&&!listBm.get(i).isRecycled()){
 				listBm.get(i).recycle();
 			}
 		}
 	}
 	
 	//根据经纬度获取地址
 	public static String GetAddr(double latitude, double longitude) {  
 		String addr = "";  
 		
 		String url = String.format(  
 		  "http://ditu.google.cn/maps/geo?output=csv&key=abcdef&q=%s,%s",  
 		  latitude, longitude);  
 		URL myURL = null;  
 		URLConnection httpsConn = null;  
 		try {  
 			myURL = new URL(url);  
 		} catch (MalformedURLException e) {  
 			e.printStackTrace();  
 		 return null;
 		}  
 		try {  
 		 httpsConn = (URLConnection) myURL.openConnection();  
 		 if (httpsConn != null) {  
 		  InputStreamReader insr = new InputStreamReader(  
 		    httpsConn.getInputStream(), "UTF-8");  
 		  BufferedReader br = new BufferedReader(insr);  
 		  String data = null;  
 		  if ((data = br.readLine()) != null) {  
 		   String[] retList = data.split(",");  
 		   if (retList.length > 2 && ("200".equals(retList[0]))) {  
 		    addr = retList[2];  
 		    addr = addr.replace("/", "");  
 		   } else {  
 		    addr = "";  
 		   }  
 		  }  
 		  insr.close();  
 		 }  
 		} catch (IOException e) {  
 		 e.printStackTrace();  
 		 return null;  
 		}  
 		return addr;  
 	}
 	
 	static public boolean JadgeConnection (Context context)throws Exception
 	{
 		
 		boolean a = false;
 		ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		State mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
 		
 		State wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
 		
 		if(mobile.toString().equals("CONNECTED") || wifi.toString().equals("CONNECTED"))
 		{
 			a = true;
 		}
 		else 
 		{
 			a = false;
 		}
 		return a;
 	}
 	
 	//GSM和CDMA手机定位
 //	private boolean goGsm = true;
 //	private boolean running = true;
 //	public class NewThread extends Thread{
 //		private long time = 0;
 //		public void run(){
 //			time = System.currentTimeMillis();
 //			while(running){
 //				if(System.currentTimeMillis() - time >= 10000){
 //					break;
 //				}
 //			}
 //			if(running){
 //				goGsm = false;
 //				geoLat = 31.1984;
 //		      	geoLon = 121.436475;
 //		      	myHandler.sendEmptyMessage(1);
 //			}
 //		}
 //	}
 //	public void ddd(){
 //        Thread thread = new Thread(){
 //                public void run(){
 //                        try {
 //                                DefaultHttpClient client = new DefaultHttpClient();
 //                                HttpPost post = new HttpPost("http://www.google.com/loc/json");//http://www.google.com/glm/mmap");//
 //                                
 //                                JSONObject holder = new JSONObject();
 //                                holder.put("version", "1.1.0");
 //                                holder.put("request_address", true);
 //                                holder.put("address_language", "en-us");
 //
 //                                JSONObject data ;
 //                                JSONArray array = new JSONArray();
 //                                data = new JSONObject();
 //                                data.put("cell_id", c.getCid());  // 9457, 8321,8243
 //                                data.put("location_area_code", c.getLac());// 28602
 //                                data.put("mobile_country_code", ss.substring(0, 3));// 208
 //                                data.put("mobile_network_code", ss.substring(3, 5));// 0
 //                                data.put("age", 0);
 ////                                
 //                                array.put(data);
 //                                for(int i =0;i<(count<2?count:2);i++){
 //                                        data = new JSONObject();
 //                                        data.put("cell_id", ci[i]);  // 9457, 8321,8243
 //                                        data.put("location_area_code", lac[i]);// 28602
 //                                        data.put("mobile_country_code", ss.substring(0, 3));// 208
 //                                        data.put("mobile_network_code", ss.substring(3, 5));// 0
 //                                        data.put("age", 0);
 //                                        array.put(data);
 //                                }
 //                                holder.put("cell_towers", array);
 ////                                
 //                                StringEntity se = new StringEntity(holder.toString());
 //                                Log.e("Location send",holder.toString());
 //                                post.setEntity(se);
 //                                HttpResponse resp = client.execute(post);
 //                                
 //                                HttpEntity entity = resp.getEntity();
 //
 //                                BufferedReader br = new BufferedReader(new InputStreamReader(entity
 //                                                .getContent()));
 //                                StringBuffer sb = new StringBuffer();
 //                                String result = br.readLine();
 //                                while (result != null) {
 //                                        Log.e("Locaiton reseive", result);
 //                                        sb.append(result);
 //                                        result = br.readLine(); 
 //                                }
 //                                result_location = sb.toString();
 //                                
 //                                
 //                                data = new JSONObject(sb.toString());
 //                                data = (JSONObject) data.get("location");
 //                                geoLat = (Double) data.get("latitude");
 //                                geoLon = (Double) data.get("longitude");
 //                                if(geoLat != 0.0 && geoLon != 0.0 && geoLat != 0 && geoLon != 0)
 //								{
 //                                	running = false;
 //                                	if(goGsm == true)
 //                                	{
 //                                		myHandler.sendEmptyMessage(0);
 //                                	}
 //								}
 //								else
 //								{
 //                                	running = false;
 //									geoLat = 31.1984;
 //							      	geoLon = 121.436475;
 //							      	if(goGsm == true)
 //                                	{
 //                                		myHandler.sendEmptyMessage(1);
 //                                	} 
 //								}
 //                        } 
 //                        catch (ClientProtocolException e) {
 //                                e.printStackTrace();
 //                        } catch (IOException e) {
 //                                e.printStackTrace();
 //                        } 
 //                        catch (JSONException e) {
 //                                e.printStackTrace();
 ////                                loc.setText("");
 //                        }
 //                }
 //        };
 //        thread.start();
 //	}
 }
