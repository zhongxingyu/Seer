 package witer;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import util.BasicUtil;
 import util.ConstantUtil;
 import util.LogUtil;
 
 import model.Movie_Info;
 
 public class ImageWriter {
 	
 	private static ImageWriter iw;
 	private static ArrayList<Movie_Info> movie_list = new ArrayList<Movie_Info>();
 	private static boolean halt = false;
 	private Runnable task;
 	
 	private ImageWriter(){}
 	
 	public synchronized static ImageWriter getInstance(){
 		if(iw == null){
 			iw = new ImageWriter();
 		}
 		return iw;
 	}
 	
 	private static int MAX_IMAGE_NUM = 10;
 	public void setMaxImageNum(int num){
 		MAX_IMAGE_NUM = num;
 	}
 	
 	public void addMovieList(ArrayList<Movie_Info> info){
 		synchronized(ImageWriter.movie_list){
 			while(ImageWriter.movie_list.size() > MAX_IMAGE_NUM){
 				try {
 					ImageWriter.movie_list.wait();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			ImageWriter.movie_list.addAll(info);
 			if(ImageWriter.movie_list.size() > MAX_IMAGE_NUM){
 				movie_list.notifyAll();
 			}
 		}
 	}
 	public void addMovieList(Movie_Info info){
 		synchronized(ImageWriter.movie_list){
 			while(ImageWriter.movie_list.size() > MAX_IMAGE_NUM){
 				try {
 					ImageWriter.movie_list.wait();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			ImageWriter.movie_list.add(info);
 			if(ImageWriter.movie_list.size() > MAX_IMAGE_NUM){
 				movie_list.notify();
 			}
 		}
 	}
 	
 	public void halt(){
 		halt = true;
 	}
 	
 	private final static int GET_IMAGE_FAILED = -1;
 	private final static int GET_IMAGE_RETRY = 0;
 	private final static int GET_IMAGE_DONE = 1;
 	public void start(){
 		task = new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				while(true){
 					ArrayList<Movie_Info> tmp = new ArrayList<Movie_Info>();
 					synchronized (movie_list) {
 						if(movie_list.size() == 0 && halt){
 							System.out.println("------------------------------------------------IamgeWriter halting------------------------------------------------");
 							break;
 						}
 						while(movie_list.size() == 0){
 							try {
 //								System.err.println("IamgeWriter waitting...");
 								movie_list.wait();
 							} catch (InterruptedException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 						if(movie_list.size() != 0){
 							tmp = (ArrayList<Movie_Info>) movie_list.clone();
 							movie_list.clear();
 							movie_list.notifyAll();
 						}
 					}
 					//批量获取海报
 					for(int i = 0; i < tmp.size(); i ++){
 						Movie_Info info = tmp.get(i);
 						int rep_code = write(info);
 						int retry = GET_IMAGE_FAILED;
 						do{
 							switch (rep_code){
 							case WRITE_OK:
 								retry = GET_IMAGE_DONE;
 								info.setHaiBaoPath(tmp_image_path);
 								info.setHaiBaoSize(tmp_image_size);
 								DBWriter.getInstance().writeMovieInfo(info);
 								break;
 							case TIME_OUT:
 								//超时则重新获取一次
 								if(retry == GET_IMAGE_FAILED){
 									LogUtil.getInstance().write("iamge " + tmp.get(i).getMovieName() + " time out retrying");
 									System.err.println("------------------------------------------------iamge " + tmp.get(i).getMovieName() + " time out retrying------------------------------------------------");
 									rep_code = write(tmp.get(i));
 									retry = GET_IMAGE_RETRY;
 								}else{
 									LogUtil.getInstance().write("iamge " + tmp.get(i).getMovieName() + " time out");
 									System.err.println("------------------------------------------------iamge " + tmp.get(i).getMovieName() + " time out-----------------------------------------------");
 									retry = GET_IMAGE_FAILED;
 								}
 								break;
 							case FILE_EXIST:
 								retry = GET_IMAGE_DONE;
 								break;
 							default:
 								LogUtil.getInstance().write("iamge " + tmp.get(i).getMovieName() + " write to File Failed");
 								System.err.println("------------------------------------------------iamge " + tmp.get(i).getMovieName() + " write to File Failed------------------------------------------------");
 								retry = GET_IMAGE_FAILED;
 								break;
 							}
 						}while(retry == GET_IMAGE_RETRY);
 						//超时的海报写入文件,备用
 						if(retry == GET_IMAGE_FAILED){
 							try {
 								FileWriter writer = new FileWriter(ConstantUtil.RETRYLIST_PATH, true);
 								writer.write(tmp.get(i).getMovieName() + "\r\n" + tmp.get(i).getHaiBaoPath() + "\r\n");
 								writer.close();
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							} 
 							
 						}
 					}
 				}
 			}
 			
 		};
 		Thread thread1 = new Thread(task);
 		Thread thread2 = new Thread(task);
 		Thread thread3 = new Thread(task);
 		Thread thread4 = new Thread(task);
 		Thread thread5 = new Thread(task);
 		thread1.start();
 		thread2.start();
 		thread3.start();
 		thread4.start();
 		thread5.start();
 	}
 	
 	private static String movie_src = "default";
 	/**
 	 * 设置海报存储的路径名 : image/网站名/电影名.jpg . 简单区分不同网站爬到的海报
 	 * @param src 网站名
 	 */
 	public void setMovieSrc(String src){
 		movie_src = src;
 	}
 	
 	private String tmp_image_path = null;
 	private long tmp_image_size = 0;
 	
 	private final static int UNKOWN_ERROR = -1;
 	private final static int WRITE_OK = 0;
 	private final static int WRITE_FAILED = 1;
 	private final static int TIME_OUT = 2;
 	private final static int URL_ERROR = 3;
 	private final static int FILE_EXIST = 4;
 	private final static int UNKOWN_HOST = 5;
 	/**
 	 * 获取单个海报,并写入文件
 	 * @param movie_info
 	 * @return resultCode
 	 */
 	private int write(Movie_Info movie_info){
 		URL url = null;
 		InputStream inputStream = null;
 		ByteArrayOutputStream outstream = null;
 		FileOutputStream fileoutStream = null;
 		tmp_image_size = 0;
 		try {
 			url = new URL(movie_info.getHaiBaoPath());
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
 			conn.setRequestMethod("GET");
 			conn.setConnectTimeout(6 * 1000);
 			conn.setReadTimeout(6 * 1000);
 			if (conn.getResponseCode() == 200) { 
 				inputStream = conn.getInputStream(); 
			    outstream = new ByteArrayOutputStream(1024 * 10); 
 			    byte[] buffer = new byte[1024 * 10];
 			    int len = -1; 
 			    while ((len = inputStream.read(buffer)) != -1) { 
 			        outstream.write(buffer, 0, len); 
 			    }  
			    tmp_image_size = outstream.size();
 			    tmp_image_path = ConstantUtil.IMAGE_ROOT_DIR + movie_src + "/" + BasicUtil.getMD5(movie_info.getMovieName().getBytes()) + ".jpg";
 			    File file = new File(tmp_image_path);
 				if(file.exists() && file.length() > tmp_image_size){
 					return FILE_EXIST;
 				}
 			    fileoutStream = new FileOutputStream(file);
 			    fileoutStream.write(outstream.toByteArray()); 
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			LogUtil.getInstance().write(e.getMessage() + "\nMalformedURLException : " + movie_info.getHaiBaoPath() + "\n");
 			e.printStackTrace();
 			return URL_ERROR;
 		}catch (SocketTimeoutException e) {
 			// TODO Auto-generated catch block
 			return TIME_OUT;
 		}catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			LogUtil.getInstance().write(e.getMessage() + "\nUnknownHostException : " + url + "\n");
 			e.printStackTrace();
 			return UNKOWN_HOST;
 		}catch (IOException e) {
 			// TODO Auto-generated catch block
 			LogUtil.getInstance().write(e.getMessage() + "\nIOException : " + movie_info.getMovieName() + "\n");
 			e.printStackTrace();
 			return WRITE_FAILED;
 		}catch (Exception e) {
 			// TODO Auto-generated catch block
 			LogUtil.getInstance().write(e.getMessage() + "\n");
 			e.printStackTrace();
 			System.err.println(url);
 			return UNKOWN_ERROR;
 		} finally{
 		    try {
 		    	if(outstream != null){
 		    		outstream.close();
 		    	}
 		    	if(inputStream != null){
 		    		inputStream.close();
 		    	}
 		    	if(fileoutStream != null){
 		    		fileoutStream.flush();
 					fileoutStream.close();
 		    	}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return WRITE_OK;
 	}
 }
