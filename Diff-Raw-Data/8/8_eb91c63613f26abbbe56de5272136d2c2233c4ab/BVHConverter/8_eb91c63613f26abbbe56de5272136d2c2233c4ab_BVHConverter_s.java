 package com.akjava.bvh.client.threejs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.akjava.bvh.client.BVHNode;
 import com.akjava.bvh.client.Channels;
 import com.akjava.bvh.client.Vec3;
 import com.akjava.gwt.lib.client.LogUtils;
 import com.akjava.gwt.three.client.core.Matrix4;
 import com.akjava.gwt.three.client.core.Vector3;
 import com.akjava.gwt.three.client.gwt.GWTThreeUtils;
 import com.akjava.gwt.three.client.gwt.animation.AngleAndMatrix;
 import com.akjava.gwt.three.client.gwt.animation.AnimationBone;
 import com.google.gwt.core.client.JsArray;
 
 public class BVHConverter {
 	public BVHNode convertBVHNode(JsArray<AnimationBone> bones){
 		List<BVHNode> bvhs=new ArrayList<BVHNode>();
 		
 		for(int i=0;i<bones.length();i++){
 			AnimationBone bone=bones.get(i);
 			BVHNode node=boneToBVHNode(bone);
 			bvhs.add(node);
 			//add parent
 			if(bone.getParent()!=-1){
 				bvhs.get(bone.getParent()).add(node);
 			}
 		}
 		
 		//add endsite
 		for(BVHNode node:bvhs){
 			if(node.getJoints().size()==0){//empty has end site
 				Vec3 endSite=node.getOffset().clone();//.multiplyScalar(2);
 				node.setEndSite(endSite);
 			}
 		}
 		
 		return bvhs.get(0);
 	}
 	
 public double[] angleAndMatrixsToMotion(List<AngleAndMatrix> matrixs,int mode,String order){
 		
 		List<Double> values=new ArrayList<Double>();
 		for(int i=0;i<matrixs.size();i++){
 			Matrix4 mx=matrixs.get(i).getMatrix();//TODO change angle
 			if((i==0 && mode==ROOT_POSITION_ROTATE_ONLY) || mode==POSITION_ROTATE){
 				Vector3 pos=GWTThreeUtils.toPositionVec(mx);
 				values.add(pos.getX());
 				values.add(pos.getY());
 				values.add(pos.getZ());
 			}
 			Vector3 tmprot=GWTThreeUtils.rotationToVector3(mx);
 			//TODO order convert;
 			if(!order.equals("XYZ")){
 				LogUtils.log("Warning:only support-XYZ");
 			}
 			Vector3 rotDegree=GWTThreeUtils.radiantToDegree(tmprot);
 			values.add(rotDegree.getX());
 			values.add(rotDegree.getY());
 			values.add(rotDegree.getZ());
 		}
 		
 		
 		
 		
 		double[] bt=new double[values.size()];
 		for(int i=0;i<values.size();i++){
 			bt[i]=values.get(i).doubleValue();
 		}
 		return bt;
 	}
 	/**
 	 * bugs cant handle over 90 angle
 	 * @deprecated
 	 * @param matrixs
 	 * @param mode
 	 * @param order
 	 * @return
 	 */
 	public double[] matrixsToMotion(List<Matrix4> matrixs,int mode,String order){
 		
 		List<Double> values=new ArrayList<Double>();
 		for(int i=0;i<matrixs.size();i++){
 			Matrix4 mx=matrixs.get(i);
 			if((i==0 && mode==ROOT_POSITION_ROTATE_ONLY) || mode==POSITION_ROTATE){
 				Vector3 pos=GWTThreeUtils.toPositionVec(mx);
 				values.add(pos.getX());
 				values.add(pos.getY());
 				values.add(pos.getZ());
 			}
 			Vector3 tmprot=GWTThreeUtils.rotationToVector3(mx);
 			//TODO order convert;
 			if(!order.equals("XYZ")){
 				LogUtils.log("Warning:only support-XYZ");
 			}
 			Vector3 rotDegree=GWTThreeUtils.radiantToDegree(tmprot);
 			values.add(rotDegree.getX());
 			values.add(rotDegree.getY());
 			values.add(rotDegree.getZ());
 		}
 		
 		
 		
 		
 		double[] bt=new double[values.size()];
 		for(int i=0;i<values.size();i++){
 			bt[i]=values.get(i).doubleValue();
 		}
 		return bt;
 	}
 	
 	
 	private BVHNode boneToBVHNode(AnimationBone bone){
 		BVHNode bvhNode=new BVHNode();
 		bvhNode.setName(bone.getName());
 		Vec3 pos=new Vec3(bone.getPos().get(0),bone.getPos().get(1),bone.getPos().get(2));
 		bvhNode.setOffset(pos);
 		//problem no angle
 		
 		//no channels
 		return bvhNode;
 	}
 	
 	public static int ROOT_POSITION_ROTATE_ONLY=0;
 	public static int ROTATE_ONLY=1;
 	public static int POSITION_ROTATE=2;
 	public void setChannels(BVHNode node,int mode,String order){
 		Channels ch=new Channels();
 		if(mode==ROOT_POSITION_ROTATE_ONLY || mode==POSITION_ROTATE){
 		ch.setXposition(true);
 		ch.setYposition(true);
 		ch.setZposition(true);
 		}
 		if(order.equals("XZY")){
 			ch.setXrotation(true);
 			ch.setZrotation(true);
 			ch.setYrotation(true);
 		}else if(order.equals("YZX")){
 			ch.setYrotation(true);
 			ch.setZrotation(true);
 			ch.setXrotation(true);
 		}else if(order.equals("YXZ")){
 			ch.setYrotation(true);
 			ch.setXrotation(true);
 			ch.setZrotation(true);
 		}else if(order.equals("ZXY")){
 			ch.setZrotation(true);
 			ch.setXrotation(true);
 			ch.setYrotation(true);
 		}else if(order.equals("ZYX")){
 			ch.setZrotation(true);
 			ch.setYrotation(true);
 			ch.setXrotation(true);
 		}else{//XYZ
 			ch.setXrotation(true);
 			ch.setYrotation(true);
 			ch.setZrotation(true);
 		}
 		node.setChannels(ch);
 		for(BVHNode child:node.getJoints()){
 			int m=mode;
 			if(mode==ROOT_POSITION_ROTATE_ONLY){
 				m=ROTATE_ONLY;
 			}
 			setChannels(child,m,order);
 		}
 	}
 }
