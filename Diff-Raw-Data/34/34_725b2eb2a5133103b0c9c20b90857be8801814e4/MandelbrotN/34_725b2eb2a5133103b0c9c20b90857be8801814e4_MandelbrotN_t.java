 package ratson.genimageexplorer.generators;
 
public class MandelbrotN extends MandelbrotLike {
 	private int power;	
 	private double lnN;
 
 	
 	public MandelbrotN() {
 		setRMax(2.0);
 		setPower(3);
 	}
 
 	class Func extends Function{
 
 		@Override
 		public
 		float evaluate(double cx, double cy) {
 			double x=cx,y=cy,x2,y2,xx;
 			double xp, yp;
 			int iters=0;
 			double r2=0;
 			while (iters<maxIters && r2<r2Max){
 				x2=x*x;
 				y2=y*y;
 				r2=x2+y2;
 				
 				//powering x
 				xp=x;yp=y;
 				for (int i=1;i<power;++i){
 					xx=xp*x-yp*y;
 					yp=xp*y+yp*x;
 					xp=xx;
 				}
 				x=xp+cx;
 				y=yp+cy;
 				
 				iters++;
 			}
 			if (iters>=maxIters)
 				return -1.0f;
 			
 			//smooth extrapolation
 			if (isSmooth){
 				double dx=(ln_ln_r2max-Math.log(Math.log(r2)))/lnN;
 				return (float)dx+(float)iters;
 			}else{
 				return iters;
 			}		
 		}
 		
 	}
 	public void setPower(int v) {
 		power=v;
 		lnN=Math.log(v);
 	}
 	public int getPower() {
 		return power;
 	}
 
 	public Function get() {
 		return new Func();
 	}
 
 }
