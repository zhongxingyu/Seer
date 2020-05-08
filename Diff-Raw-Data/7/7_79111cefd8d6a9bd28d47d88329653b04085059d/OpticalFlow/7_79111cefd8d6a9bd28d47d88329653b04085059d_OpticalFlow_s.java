 package kr.hs.sshs.JavaPTS;
 
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindCornerSubPix;
 import static com.googlecode.javacv.cpp.opencv_imgproc.cvGoodFeaturesToTrack;
 import static com.googlecode.javacv.cpp.opencv_video.CV_LKFLOW_INITIAL_GUESSES;
 import static com.googlecode.javacv.cpp.opencv_video.CV_LKFLOW_PYR_A_READY;
 import static com.googlecode.javacv.cpp.opencv_video.cvCalcOpticalFlowPyrLK;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
 import com.googlecode.javacv.cpp.opencv_core.CvSize;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 public class OpticalFlow {
 	// / Image size
 	static CvSize _size;
 
 	/**
 	 * Process optical flow calculation from 2 successive frames<br>
 	 * and a Pyramid image to be used in the L-K method.<br>
 	 * (NOTE: video version, uses flag)
 	 * @param imgPrev		8-bit single channel image of prev frame
 	 * @param imgCurr		8-bit single channel image of curr frame
 	 * @param imgPyrA		32-bit single channel image used to store and compute pyramid from prev frame<br>
 	 * 						(if flag, pyramid will be read from this image)
 	 * @param imgPyrB		32-bit single channel image used to compute pyramid from curr frame
 	 * @param isPyrANeeded	true:  will use imgCurr of 1 cycle ago as imgPrev of now<br>
 	 * 						false: will cvCreateImage a new one
 	 * @return Returns the movement vector of the background in the form of double[] {xshift, yshift}
 	 */
 	public double[] processOpticalFlow(IplImage imgPrev, IplImage imgCurr, IplImage imgPyrA, IplImage imgPyrB, boolean isPyrANeeded) {
 		CvSize _winSize = new CvSize(10,10);
 
 		_size=cvGetSize(imgPrev);
 		// Find good features to track
 		IplImage imgEig = cvCreateImage(_size, IPL_DEPTH_32F, 1);
 		IplImage imgTemp = cvCreateImage(_size, IPL_DEPTH_32F, 1);
 
 		// Max corner counts to find from the prev frame
 		final int _maxCornerCount = 600;
 
 		CvPoint2D32f cornersA = new CvPoint2D32f(_maxCornerCount);
 		int[] cornerCount = {_maxCornerCount};
 		cvGoodFeaturesToTrack(
 				imgPrev,
 				imgEig,
 				imgTemp,
 				cornersA,
 				cornerCount,
 				0.10,
 				0.1,
 				null, 3, 0, 0.04
 				);
 		cvReleaseImage(imgEig);
 		cvReleaseImage(imgTemp);
 		System.out.println("# of corners: " + cornerCount[0]);
 		//System.out.println(m.doubleArrayToString(cornersA.get()));
 
 		// Find subpixel corners
 		cvFindCornerSubPix(
 				imgPrev,
 				cornersA,
 				cornerCount[0],
 				_winSize,
 				cvSize(-1,-1),
 				cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, .03)
 				);
 
 		// Optical Flow
 		CvSize _pyrSize = new CvSize(_size.width()+8, _size.height()/3+1);
 		CvPoint2D32f cornersB = new CvPoint2D32f(_maxCornerCount);
 		byte[] status = new byte[cornerCount[0]];
 		float[] featureErrors = new float[cornerCount[0]];
 
 		// Memory mangement
		cvReleaseImage(imgPyrB);	// WHAT D'YA KNOW
 		imgPyrB = cvCreateImage(_pyrSize, IPL_DEPTH_32F, 1);	// Always shining new baby, BUT DONT RELEASE IT
		if (isPyrANeeded) imgPyrA = cvCreateImage(_pyrSize, IPL_DEPTH_32F, 1);
 		
 		cvCalcOpticalFlowPyrLK(
 				imgPrev,
 				imgCurr,
 				imgPyrA,
 				imgPyrB,
 				cornersA,
 				cornersB,
 				cornerCount[0],
 				_winSize,
 				5,
 				status,
 				featureErrors,
 				cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, .3),
 				isPyrANeeded?(0):(CV_LKFLOW_PYR_A_READY | CV_LKFLOW_INITIAL_GUESSES)
 				);
 		cvReleaseImage(imgPyrA);	// imgPyrA will never be used again (almost)
 									// imgPyrB will be used as imgPyrA' in the next call
 									// Release of imgCurr and imgPrev will be taken care by the caller
 		System.out.println("CornerB: " + cornersB.get().length);
 
 		// Show what we are looking at
 		float errorCriteria = 500.0f;
 		List<Vector> successAPointsL = new ArrayList<Vector>();		// Prev
 		List<Vector> successBPointsL = new ArrayList<Vector>();		// Curr
 
 		for (int i=0; i<cornerCount[0]; i++) {
 			double p0x = cornersA.get()[2*i];
 			double p0y = cornersA.get()[2*i+1];
 			double p1x = cornersB.get()[2*i];
 			double p1y = cornersB.get()[2*i+1];
 
 			//System.out.print("Status of " + (i+1) + " [" + p0.x() + "," + p0.y() + "]	: " + status[i]);
 			if (status[i]==0) { 
 				System.out.println("	<<< Error -- Zero status");
 				continue;
 			} else if (featureErrors[i] > errorCriteria) {
 				System.out.println("	<<< Error -- Too long error (" + featureErrors[i] + ", criteria:" + errorCriteria + ")");
 				continue;
 			} else { // Passed the test!
 				System.out.println();
 				successAPointsL.add(new Vector(p0x, p0y));
 				successBPointsL.add(new Vector(p1x, p1y));
 			}
 		}
 
 		/// Calculation
 		double[] shift = new double[2];
 		try {
 			shift = findBgMovement(successAPointsL, successBPointsL);
 			assert (successAPointsL.size() == successBPointsL.size()): "success points list size error";	
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return shift;
 	}
 
 	/**
 	 * 
 	 * @param APoints	Successfully tracked points in prev frame
 	 * @param BPoints	Successfully tracked points in curr frame
 	 * @return double[] {xshift, yshift}
 	 */
 	public double[] findBgMovement(List<Vector> APoints, List<Vector> BPoints) throws Exception {
 		int flowsCount = APoints.size();
 		if (flowsCount != BPoints.size()) throw new Exception("Two Points vector lists have different size -- somthing went wrong");
 		System.out.println("Successful: " + flowsCount);
 
 		List<Vector> flows = new ArrayList<Vector>();
 		for(int i=0; i<APoints.size(); i++) {
 			Vector APoint = APoints.get(i);
 			Vector BPoint = BPoints.get(i);
 			flows.add(Vector.sub(BPoint, APoint));
 		}		
 		/*for(int i=0; i<flows.size(); i++) {
 			Vector v = flows.get(i);
 			double theta = v.theta();
 			double distance = v.length();
 			System.out.println("Flow " + i + ": [" + v.x() + ", " + v.y() + "] theta:" + theta + " distance: " + distance);
 		}*/
 		
 		///
 		/// Calculation
 		///
 		// Find min and max
 		double mintheta = 2*Math.PI;
 		double maxtheta = -2*Math.PI;
 		double mindistance = 1000.0;
 		double maxdistance = 0.0;
 		for (Vector v : flows) {
 			double theta = v.theta();
 			double distance = v.length();
 			if (theta < mintheta) mintheta = theta;
 			if (theta > maxtheta) maxtheta = theta;
 			if (distance < mindistance) mindistance = distance;
 			if (distance > maxdistance) maxdistance = distance;
 		}
 		int roomsCount = 1000;
 		//double thetaInterval = (Math.PI / 180 ) * 5.0;		// 'Hard' interval (based on absolute theta)
 		double thetaInterval = (maxtheta - mintheta) / roomsCount;		// 'Soft' interval (based on interval counts)
 		//double thetaInterval = (Math.PI / 180 ) * 5.0;		// 'Hard' interval (based on absolute theta)
 		double distanceInterval = (maxdistance - mindistance) / roomsCount;		// 'Soft' interval (based on interval counts)
 		
 		// 'Rooms' where vectors get sorted in
 		List<ArrayList<Vector>> thetaRooms = new ArrayList<ArrayList<Vector>>();	
 		List<ArrayList<Vector>> distanceRooms = new ArrayList<ArrayList<Vector>>();	
 		int thetaRoomsCount = (int)Math.floor((maxdistance-mindistance) / distanceInterval) + 1;
 		int distanceRoomsCount = (int)Math.floor((maxtheta-mintheta) / thetaInterval) + 1;
 		for(int i=0; i<thetaRoomsCount; i++) thetaRooms.add(new ArrayList<Vector>());	// Initialize
 		for(int i=0; i<distanceRoomsCount; i++) distanceRooms.add(new ArrayList<Vector>());	// Initialize
 		// Sort and add vectors
 		for (int i=0; i<flows.size(); i++) {
 			Vector v = flows.get(i);
 			v.setIndex(i);				// Important!! : Index(i) will be used when retrieving back the pointA and pointB vector
 			double theta = v.theta();
 			double distance = v.length();
 			int indexTheta = (int)Math.floor((theta - mintheta) / thetaInterval);	// from 0
 			int indexDistance = (int)Math.floor((distance - mindistance) / distanceInterval);	// from 0
 			if(indexDistance == roomsCount) indexDistance--;
 			if(indexTheta == roomsCount) indexTheta--; 
 			thetaRooms.get(indexTheta).add(v);
 			distanceRooms.get(indexDistance).add(v);
 		}
 		
 		// Now let's find the most 'populated' rooms
 		ArrayList<Vector> biggestTRoom1 = null;
 		ArrayList<Vector> biggestDRoom1 = null;
 		int biggestTRoomSize1=0, biggestTRoomSize2=0;
 		int biggestDRoomSize1=0, biggestDRoomSize2=0;
 		double tSum1=0, dSum1=0;
 		for(ArrayList<Vector> al : thetaRooms) {
 			int roomSize = al.size();
 			if (biggestTRoomSize1 < roomSize) {
 				biggestTRoom1 = al;
 				biggestTRoomSize1 = roomSize;
 			}
 		}
 		for(ArrayList<Vector> al : thetaRooms) {
 			int roomSize = al.size();
 			if (biggestTRoomSize2 < roomSize && roomSize < biggestTRoomSize1) {
 				// biggestTRoom2 = al;
 				biggestTRoomSize2 = roomSize;
 			}
 		}
 		for(ArrayList<Vector> al : distanceRooms) {
 			int roomSize = al.size();
 			if (biggestDRoomSize1 < roomSize) {
 				biggestDRoom1 = al;
 				biggestDRoomSize1 = roomSize;
 			}
 		}
 		for(ArrayList<Vector> al : distanceRooms) {
 			int roomSize = al.size();
 			if (biggestDRoomSize2 < roomSize && roomSize < biggestDRoomSize1) {
 				// biggestDRoom2 = al;
 				biggestDRoomSize2 = roomSize;
 			}
 		}
 		// Calculate average
 		for (Vector v : biggestTRoom1) {
 			tSum1 += v.theta();
 		}
 		for (Vector v : biggestDRoom1) {
 			dSum1 += v.length();
 		}
 		double probableTheta1 = tSum1 / biggestTRoomSize1;
 		double probableDistance1 = dSum1 / biggestDRoomSize1;
 
 		// Massive sysouts
 		/*System.out.println();
 		System.out.println("Maxtheta: " + maxtheta);
 		System.out.println("Mintheta: " + mintheta);
 		System.out.println("1st probable theta count: " + biggestTRoomSize1);
 		System.out.println("2nd probable theta count: " + biggestTRoomSize2);
 		System.out.println("1st probable theta AVG: " + probableTheta1);		// Remember, Y-axis is inverted
 		System.out.println("2nd probable theta AVG: " + probableTheta2);
 		System.out.println();
 		System.out.println("Maxdistance: " + maxdistance);
 		System.out.println("Mindistance: " + mindistance);
 		System.out.println("1st probable distance count: " + biggestDRoomSize1);
 		System.out.println("2nd probable distance count: " + biggestDRoomSize2);
 		System.out.println("1st probable distance AVG: " + probableDistance1);
 		System.out.println("2nd probable distance AVG: " + probableDistance2);
 		System.out.println();*/
 		
 		List<Vector> backgroundFlows = biggestDRoom1;
 		List<Vector> backgroundAPoints = new ArrayList<Vector>();
 		List<Vector> backgroundBPoints = new ArrayList<Vector>();
 		for(int i=0; i<backgroundFlows.size(); i++) {
 			Vector v = backgroundFlows.get(i);
 			int index = v.getIndex();
 			backgroundAPoints.add(APoints.get(index));
 			backgroundBPoints.add(BPoints.get(index));
 		}
 		
 		//findThetaDistance(backgroundAPoints, backgroundBPoints, backgroundFlows);
 		double xshift = probableDistance1 * Math.cos(probableTheta1);
 		double yshift = probableDistance1 * Math.sin(probableTheta1);
 		return new double[] {xshift, yshift};
 	}
 	
 	/**
 	 * Solve Least Square Problem and find the 'most appropriate' vector.
 	 * @param flows 
 	 */
 	@SuppressWarnings("unused")
 	public void findThetaDistance(List<Vector> APoints, List<Vector> BPoints, List<Vector> flows) {
 		assert (APoints.size()==BPoints.size() && BPoints.size()==flows.size()):"WTF index doesn't match??!";
 //		System.out.println("Background flow vectors: " + flows.size());
 		
 //		Subtract 1 vector from other vectors -> Rotation
 		Vector AStandard = APoints.get(0); // Standard flow vector to subtract
 		Vector BStandard = BPoints.get(0);
 		
 		ArrayList<Vector> deltaAPoints = new ArrayList<Vector>();
 		ArrayList<Vector> deltaBPoints = new ArrayList<Vector>();
 		for (int i=0; i<flows.size(); i++) {
 			Vector APoint = APoints.get(i);
 			Vector BPoint = BPoints.get(i);
 			deltaAPoints.add(APoint.sub(AStandard));
 			deltaBPoints.add(BPoint.sub(BStandard));
 		}
 
 		// TEST AREA
 
 		if (false) {
 			deltaAPoints.clear();
 			deltaBPoints.clear();
 			deltaAPoints.add(new Vector(0.99619, 0.087156));
 			deltaAPoints.add(new Vector(1.9319, 0.51764));
 			deltaAPoints.add(new Vector(2.7189, 1.2679));
 			deltaBPoints.add(new Vector(0.70711, 0.70711));
 			deltaBPoints.add(new Vector(1.1472, 1.6383));
 			deltaBPoints.add(new Vector(1.2679, 2.7189));
 		}
 		
 		///
 		/// Rather primitive matrix calculation
 		///
 		int n=deltaAPoints.size();
 		System.out.println(n);
 		// S=(A^T)*A
 		double s11=0, s12=0, s21=0, s22=0;
 		Vector Ak;
 		for (int k=1; k<n; k++) {
 			Ak = deltaAPoints.get(k);
 			s11 += Ak.x()*Ak.x();
 			s12 += Ak.x()*Ak.y();
 			s22 += Ak.y()*Ak.y();
 		}
 		s21 = s12;
 		// SI = S^(-1)
 		double detS = s11*s22 - s12*s21;
 		double si11 = s22 / detS;
 		double si12 = - s12 / detS;
 		double si21 = - s21 / detS;
 		double si22 = s11 / detS;
 		// R = (A^T)*b
 		double r1=0, r2=0;
 		for (int k=1; k<n; k++) {
 			r1 += deltaAPoints.get(k).x()*deltaBPoints.get(k).x();
 			r2 += deltaAPoints.get(k).y()*deltaBPoints.get(k).x();
 		}
 		// Approx. solution of A*p=b (A:deltaAPoints, p:(cosT,sinT), b:x of deltaBPoints)
 		double p1 = si11*r1 + si12*r2;	// ~ cosT
 		double p2 = si21*r1 + si22*r2;	// ~ sinT
 		
 		System.out.println("cosT ~ "+p1);
 		System.out.println("sinT ~ "+-1*p2);
 		System.out.println("c^2+s^2= " + (p1*p1 + p2*p2));
 		System.out.println("Accuracy= " + ((p1*p1 + p2*p2)-1)*100);
 		
 		// Now get p and q (deprecated)
 	}
 }
