 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class PrimitiveAnimator implements PrimitiveModifier, JSONSerializable{
 
 	PrimitiveAnimator(){}
 	PrimitiveAnimator(ScenePrimitive sp){
 		setPrimitive(sp);
 	}
 
 	public void setPrimitive(ScenePrimitive sp){
 		if (!(sp instanceof ZSprite))
 			throw new IllegalArgumentException("no zsprite");
 		prim=(ZSprite)sp;
 	}
 
 	public PrimitiveModifier clone(ScenePrimitive sp){
 		PrimitiveAnimator pa = new PrimitiveAnimator(sp);
 		pa.velocity = velocity;
 		for (Integer i: roiPos)
 			pa.roiPos.add(i.intValue());
 		for (Integer i: roiSize)
 			pa.roiSize.add(i.intValue());
 		return pa;
 	}
 
 	public float velocity;
 	public ArrayList<Integer> roiPos=new ArrayList<Integer>();
 	public ArrayList<Integer> roiSize=new ArrayList<Integer>();
 	public ZSprite prim;
 	public float time;
	public void calculate(int timeStep){
 		time += timeStep*velocity/1000.0f;
 		while (time>=roiPos.size()/2) time-=roiPos.size()/2;
 		while (time<0) time+=roiPos.size()/2;
 		int curRoi = (int)Math.floor(time);
 		int roiX = roiPos.get(curRoi*2);
 		int roiY = roiPos.get(curRoi*2+1);
 		int roiW = prim.w;
 		int roiH = prim.h;
 		if (2*curRoi+1<roiSize.size()){
 			roiW = roiSize.get(curRoi*2);
 			roiH = roiSize.get(curRoi*2+1);
 		}
 		prim.setROI(roiX, roiY, roiW, roiH);
 	}
 
 	public Map<String, Object> jsonSerialize() {
 		TreeMap<String,Object> tm = new TreeMap<String,Object>();
 		tm.put("type", "PrimitiveAnimator");
 		tm.put("roipos", roiPos);
 		tm.put("roisize", roiSize);
 		tm.put("velocity", velocity);
 		return tm;
 	}
 
 	public void jsonDeserialize(Object obj) {
 		Map<String, Object> map=(Map<String, Object>) obj;
 		ArrayList<Number> newa = (ArrayList<Number>)map.get("roipos");
 		roiPos.clear();
 		for (Number n: newa) roiPos.add(n.intValue());
 		newa = (ArrayList<Number>)map.get("roisize");
 		roiSize.clear();
 		for (Number n: newa) roiSize.add(n.intValue());
 		velocity=((Number) map.get("velocity")).floatValue();
 
 	}
 
 }
