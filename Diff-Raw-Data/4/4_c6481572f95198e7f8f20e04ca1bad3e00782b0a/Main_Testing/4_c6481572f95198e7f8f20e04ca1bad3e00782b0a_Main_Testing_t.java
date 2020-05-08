 package kr.hs.sshs.JavaPTS;
 
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_highgui.*;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 public class Main_Testing {
 	IplImage imgA;
 	IplImage imgB;
 
 	static CvSize _size = new CvSize(640,480);
 	
 	public static void main(String[] args) {
 		Main_Testing mt = new Main_Testing();
 		//mt.imgA = cvCreateImage(_size,IPL_DEPTH_8U,1);
 		//mt.imgB = cvCreateImage(_size,IPL_DEPTH_8U,1);
 		
 		mt.imgA = cvLoadImage("video/137.jpg", CV_LOAD_IMAGE_GRAYSCALE);
 		mt.imgB = cvLoadImage("video/137.jpg", CV_LOAD_IMAGE_GRAYSCALE);
 
 		mt.imgB = mt.imgA;
 		cvSaveImage("video/isValue.jpg", mt.imgB);
 		mt.releaseAB(mt.imgA, mt.imgB);
 		cvSaveImage("video/isValue1.jpg", mt.imgB);
 	}
 	
 	public void releaseAB(IplImage imgA, IplImage imgB) {
 		//imgA = cvCreateImage(_size,IPL_DEPTH_8U,1);
 		//cvSmooth(imgA,imgA,CV_GAUSSIAN, 9);
 		cvReleaseImage(imgA);
 		cvSaveImage("video/isValue2.jpg", imgB);
 	}
 	
 	public void changeApple(Apple c) {
 		c.price = 30;
 	}
 }
 
 class Apple {
 	int price = 0;
 }
