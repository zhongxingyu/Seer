 
 
 
 public class SimpleVector {
 
 	
 	private double[] data;	//constructor will initialize data to 2 doubles
 
 
 	public SimpleVector(double vx, double vy) {
 		data = new double[2];
 
 		data[0] = vx;
 		data[1] = vy;
 	}
 
 	public void set(double vx, double vy) {
 		setvx(vx);
 		setvy(vy);
 	}
 
 	public void setvx(double vx) {
 		data[0] = vx;
 	}
 	public void setvy(double vy) {
 		data[1] = vy;
 	}
 
 
 	public void addvx(double vx) {
 		data[0] += vx;
 	}
 	public void addvy(double vy) {
 		data[0] += vy;
 	}
 
 
 	public void add(SimpleVector v) {
		addvx(v.vx();
		addvy(v.vy();
 	}
 
 	public double vx() {
 		return data[0];
 	}
 
 	public double vy() {
 		return data[1];
 	}
 
 
 
 }
