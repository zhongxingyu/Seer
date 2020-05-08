 package mocap.reader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.vecmath.Vector3d;
 
 import mocap.figure.AnimData;
 import mocap.figure.Bone;
 
 public class BVHReader {
 
 	public Bone skeleton;
 	public AnimData data;
 	
 	private double[][] motValues;
 	private int indexCounter;
 	private float targetHeight;
	private double maxRootDistance, scale = 1.0;
 	private List<Bone> allBones;
 	
 	abstract class BVHNode { Vector3d offset; }
 	class BVHLeaf extends BVHNode { }
 	class BVHJoint extends BVHNode 
 	{
 		String name;
 		ArrayList<BVHJoint> children = new ArrayList<BVHJoint>();
 		int dof;
 		int index;
 		boolean isRoot = false;
 		BVHLeaf endpt = null;
 		
 		void scale(double scale) {
 
 			offset.scale(scale);
 			for (BVHJoint j : children) {
 				j.scale(scale);
 			}
 			if (endpt != null) {
 				endpt.offset.scale(scale);
 			}
 			// System.out.println("scale " + name + ": " + offset);
 		}
 	}
 	
 	/**
 	 * Reads a BVH file. Currently only reads the motion values part of 
 	 * the file, we need to implement Bone and others for the rest. Stores
 	 * the motion vales in the motValues 2-D array.
 	 * @param bvhFile the file to read from
 	 * @return True if the file was successfully read from. False if the file
 	 *   could not be read from, was missing information, or was incorrectly
 	 *   formatted in any way.
 	 * @throws FileNotFoundException if the file could not be found.
 	 */
 	public boolean readFile(File bvhFile, float targetHeight) throws FileNotFoundException {
 		Scanner in = new Scanner(bvhFile);
 		this.targetHeight = targetHeight;
 		while (in.hasNextLine()) {
 			String line = in.nextLine();
 			
 			if (line.equals("HIERARCHY"))
 			{
 				// skip to next line
 				if (!in.hasNextLine()) //there should be more lines
 					return false;
 				line = in.nextLine().trim();
 				if (line.startsWith("ROOT"))
 				{
 					BVHJoint root = readBones(in, null, line.substring(line.indexOf(" ") + 1));
 					root.isRoot = true;
 					if (targetHeight > -1) {
 						//TODO: I'm not sure about this 10, it was a 2 in the 
 						//original code but the figure was still too big so...
 						double s = targetHeight / (10 * maxRootDistance);
 						 System.out.println("### scaling: " + s);
 						root.scale(s);
 						scale = s;
 					}
 					allBones = new ArrayList<Bone>();
 					this.skeleton = processBVHNodes(root, null);
					this.skeleton.scale(this.scale);
 				}
 			}
 			
 			if (line.equals("MOTION")) {
 				//"Frames: XX"
 				if (!in.hasNextLine()) //there should be more lines
 					return false;
 				line = in.nextLine();
 				if (line.indexOf("Frames") < 0) //the next line should be "Frames: XX"
 					return false;
 				int frames = 0;
 				try {
 					frames = Integer.parseInt(line.substring(line.indexOf(" ")).trim());
 				} catch (NumberFormatException e) {
 					return false;
 				}
 				
 				//"Frame Time: XX"
 				if (!in.hasNextLine())
 					return false;
 				line = in.nextLine();
 				if (line.indexOf("Frame Time") < 0)
 					return false;
 				String[] tokens = line.split(" ");
 				float frameTime = 0; //TODO: not sure what this is for but
 									  //I assume we need it!
 				try {
 					frameTime = Float.parseFloat(tokens[2]);
 				} catch (NumberFormatException e) {
 					return false;
 				}
 				
 				//Now read in all the numbers
 				if (!in.hasNextLine())
 					return false;
 				line = in.nextLine();
 				String[] nums = line.split(" ");
 				motValues = new double[frames][nums.length];
 				try {
 					for (int i=0; i<nums.length; i++) {
 						motValues[0][i] = Double.parseDouble(nums[i]);
 					}
 					
 					int frameCount = 1;
 					while (in.hasNextLine()) {
 						line = in.nextLine();
 						nums = line.split(" ");
 						
 						for (int i=0; i<nums.length; i++) {
 							motValues[frameCount][i] = Double.parseDouble(nums[i]);
 						}
 						
 						frameCount++;
 					}
 					
 					if (frameCount < frames) {
 						//I.e., if the actual number of frames in the file
 						//is less than the number it says there are
 						return false;
 					}
 					
 				} catch (NumberFormatException e) {
 					return false;
 				} catch (IndexOutOfBoundsException e) {
 					//If there were more lines in the file than it said there were
 					return false;
 				}
 				
 				// use the AnimData object to hold this data
 				data = new AnimData(allBones.size());
 				data.setFps(1.f / frameTime);
 				data.setNumFrames(frames);
 				
 				float[][] motion = new float[allBones.size()][];
 				
 				// for each line, separate out each bone's motions
 				for (int i = 0; i < allBones.size(); i++)
 				{
 					Bone b = allBones.get(i);
 					int dof = b.getDOF();
 					motion[i] = new float[dof * frames]; // initialize this bone's movements
 					
 					int index = b.getIndex();  // gets the starting index of this 
 											   // bone's motion in any frame line
 					for (int f = 0; f < motValues.length; f++)
 					{
 						int motionIndex = f * dof; // base index for frame-based 
 						                           // indexing for each bone
 
 						// force the translation, if it exists, to the end of the vector to 
 						// match the TX,TY,TZ in Bone.java
 						if (dof == 6)
 						{
 							for (int d = 0; d < 3; d++)
 								motion[i][motionIndex + 3 + d] = (float)motValues[f][index + d];
 							for (int d = 3; d < dof; d++)
 							{
 								float v = (float)Math.toRadians((float)motValues[f][index + d]);
 								motion[i][motionIndex - 3 + d] = v;
 							}
 						} else {
 							for (int d = 0; d < dof; d++)
 							{
 								float v = (float)Math.toRadians(((float)motValues[f][index + d]));
 					            motion[i][motionIndex + d] = v;
 							}
 						}
 					}
 					
 					// put this bone's motion data into the AnimData object
 					data.putBoneData(i, motion[i]);
 					
 					// reset the index to be its index relative to all other objects
 					// (since the animation data is now stored bone-major)
 					b.setIndex(i);
 				}
 			}
 		}
 		return true;
 	}
 
 	public BVHJoint readBones(Scanner in, BVHJoint parent, String name)
 	{
 		BVHJoint curJoint = new BVHJoint();
 		curJoint.name = name;
 		
 		String line;
 		while (in.hasNextLine())
 		{
 			line = in.nextLine().trim();
 			if (line.startsWith("JOINT"))
 			{
 				BVHJoint child = readBones(in, curJoint, line.substring(line.indexOf(" ") + 1));
 				curJoint.children.add(child);
 			} else if (line.startsWith("OFFSET")) {
 				curJoint.offset = readVector(line.substring(line.indexOf(" ")));
 			} else if (line.startsWith("CHANNELS")) {
 				curJoint.index = indexCounter;
 				curJoint.dof = Integer.parseInt(line.substring(8).trim().split(" ")[0]);
 				indexCounter += curJoint.dof;
 			} else if (line.startsWith("}")) {
 				break;
 			} else if (line.startsWith("End Site")) {
 				BVHLeaf end = new BVHLeaf();
 				while ((line = in.nextLine()) != null)
 				{
 					line = line.trim();
 					if (line.startsWith("OFFSET"))
 						end.offset = readVector(line.substring(6));
 					else if (line.startsWith("}"))
 						break;
 				}
 				curJoint.endpt = end;
 				maxRootDistance = Math.max(maxRootDistance,
 						curJoint.endpt.offset.length());
 			}
 		}
 		
 		return curJoint;
 	}
 
 	
 	private Bone processBVHNodes(BVHJoint node, Bone parent) {
 		Bone curBone = new Bone(node.name, null, node.dof, node.index);
 		if (parent != null) {
 			curBone.setParent(parent);
 			parent.geometry(node.offset, scale);
 		}
 		curBone.setBaseTranslation(node.offset);
 		
 		// keep track of bones for matching up animation numbers
 		allBones.add(curBone);
 		
 		Bone[] children = new Bone[node.children.size()];
 		for (int i = 0; i < node.children.size(); i++) 
 		{
 			children[i] = processBVHNodes((BVHJoint)node.children.get(i), curBone);
 		}
 		curBone.setChildren(children);
 		
 		if (node.endpt != null)
 			curBone.geometry(node.endpt.offset, scale);
 		
 		return curBone;		
 	}
 
 	private Vector3d readVector(String line)
 	{
 		String[] parts = line.trim().split("\\s+");
 		return new Vector3d(Double.parseDouble(parts[0]),
 				            Double.parseDouble(parts[1]),
 				            Double.parseDouble(parts[2]));
 	}
 	
 	
 	public double[][] getMotVals() {
 		return motValues;
 	}
 
 	public double getScale() {
 		return scale;
 	}
 }
