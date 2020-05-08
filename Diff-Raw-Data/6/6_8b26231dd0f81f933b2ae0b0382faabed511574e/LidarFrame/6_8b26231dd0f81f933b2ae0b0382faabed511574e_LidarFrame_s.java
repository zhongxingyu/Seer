 package VelodyneDataIO;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 
 import javax.management.RuntimeErrorException;
 
 import calibration.BodyFrame;
 import calibration.CoordinateFrame;
 import calibration.FrameTransformer;
 
 import com.jogamp.common.nio.Buffers;
 
 public class LidarFrame {
 	public final static double MAX_RANGE=50.0;
 	public final static double X_MIN = -0.8;//x is right
 	public final static double X_MAX = 0.8;
 	public final static double Y_MIN = -2.5;//y is forward
 	public final static double Y_MAX = 2.5;
 	
 	float time;//system time in AidedINS
 	//ArrayList<Point3D> dataPoints;//data points in x,y,z coordinates
 	float [] dataPoints;
 	Point3D[] dataPoints3D;
 	float [] intensityArray;//
 	int pointNum;//number of points in this frame
 	CoordinateFrame localWorldFrame;//params of local world frame w.r.t world frame
 	BodyFrame bodyFrame;//body of vehicle itself
 	
 	public LidarFrame(float time, BodyFrame bodyFrame, CoordinateFrame localWorldFrame, int pointNum) {
 		this.time = time;
 		this.localWorldFrame = localWorldFrame;
 		this.bodyFrame = bodyFrame;
 		this.pointNum = pointNum;
 		dataPoints = new float[pointNum*3];
 		intensityArray = new float[pointNum];
 		dataPoints3D = new Point3D[pointNum];
 	}
 	
 	public void putData(int idx, float x, float y, float z, float intensity){
 		dataPoints[idx*3]=x;dataPoints[idx*3+1]=y;dataPoints[idx*3+2]=z;
 		intensityArray[idx]=intensity;
 	}
 	
 	/**
 	 * transform data points from body to local world frame using transFrame
 	 * and set body to local world frame
 	 * local world frame has same rotation world frame(NED) and same position to body frame  
 	 * @param trans
 	 */
 	public void transfromToLocalWorldFrame(FrameTransformer trans){
 		if(this.localWorldFrame!=null){
 			throw new RuntimeErrorException(new Error("lidarFrame already has local world frame, this function should only be called to lidarFrame from raw data"));
 		}
 		//change new body frame to local world frame
 		this.localWorldFrame = this.bodyFrame.cloneFrame();
 		this.localWorldFrame.copyRotation(trans.getWorldFrame().getRotation());
 		//change point from body to local world frame(new body frame)
 		Point3D[] points = trans.transform4D(this.bodyFrame, this.localWorldFrame, dataPoints);
 		for(int i=0; i<points.length; i++){
			this.dataPoints[i*3] = (float)this.dataPoints3D[i].x;
			this.dataPoints[i*3+1] = (float)this.dataPoints3D[i].y;
			this.dataPoints[i*3+2] = (float)this.dataPoints3D[i].z;
 		}
 	}
 	/**
 	 * from localWorldFrame to world frame
 	 * @param trans
 	 * @return
 	 */
 	public Point3D[] transformToWorld(FrameTransformer trans){
 		return trans.transform4D(localWorldFrame, null, this.dataPoints);
 		//return trans.transform4D(null, null, this.dataPoints);
 	}
 
 	
 	public FloatBuffer getDataBuffer(){
 		return Buffers.newDirectFloatBuffer(dataPoints);
 	}
 	
 	public float[] getDataArray(){
 		return this.dataPoints;
 	}
 	
 	public void makePoints(){
 		for(int idx=0; idx<this.pointNum; idx++){
 			dataPoints3D[idx] = new Point3D(dataPoints[idx*3], dataPoints[idx*3+1], dataPoints[idx*3+2]);
 		}
 	}
 	
 	public Point3D[] getDataPoints(boolean needMaxRange){
 		Point3D origin = new Point3D(0, 0, 0);
 		ArrayList<Point3D> points = new ArrayList<Point3D>();
 		for(int idx=0; idx<this.pointNum; idx++){
 //			if(!needMaxRange && calcDist(dataPoints[idx*3], dataPoints[idx*3+1], dataPoints[idx*3+2])>=MAX_RANGE){
 			if(!needMaxRange && this.dataPoints3D[idx].calcDist(origin) >= MAX_RANGE){
 				continue;
 			}
 			points.add(new Point3D(dataPoints[idx*3], dataPoints[idx*3+1], dataPoints[idx*3+2]));
 		}
 		
 		return points.toArray(new Point3D[points.size()]);
 	}
 
 	public Point3D getDataPoint(int idx){
 		return this.dataPoints3D[idx];
 	}
 	
 	public float getIntensity(int idx){
 		return this.intensityArray[idx];
 	}
 	
 	public int getPointNum(){
 		return this.pointNum;
 	}
 	
 	public int getDataNum(){
 		return this.pointNum*3;
 	}
 	
 	public CoordinateFrame getLocalWorldFrame(){
 		return this.localWorldFrame;
 	}
 	
 	public BodyFrame getBodyFrame(){
 		return this.bodyFrame;
 	}
 	
 	public float getTime(){
 		return this.time;
 	}
 	
 	private double calcDist(float x, float y, float z){
 		return Math.sqrt((x*x+y*y+z*z));
 	}
 }
