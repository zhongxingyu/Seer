 package ratson.genimageexplorer.generators;
 
public class MandelBarGenerator extends MandelbrotLike{
 	public MandelBarGenerator(){
 	}
 	
 	class Func extends Function{
 
 		@Override
 		public
 		float evaluate(double cx, double cy) {
 			double x=cx,y=cy,x2,y2,xx;
 			int iters=0;
 			double r2=0;
 			while (iters<maxIters && r2<r2Max){
 				x2=x*x;
 				y2=y*y;
 				r2=x2+y2;
 				
 				xx=x2-y2+cx;
 				y=-2*x*y+cy;
 				
 				x=xx;
 				
 				iters++;
 			}
 			if (iters>=maxIters)
 				return -1.0f;
 			
 			//smooth extrapolation
 			if (isSmooth){
 				double dx=(ln_ln_r2max-Math.log(Math.log(r2)))/ln2;
 				return (float)dx+(float)iters;
 			}else{
 				return iters;
 			}		
 		}
 		
 	};
 	Func instance = new Func();
 	public Function get() {
 		return instance;
 	}
 }
