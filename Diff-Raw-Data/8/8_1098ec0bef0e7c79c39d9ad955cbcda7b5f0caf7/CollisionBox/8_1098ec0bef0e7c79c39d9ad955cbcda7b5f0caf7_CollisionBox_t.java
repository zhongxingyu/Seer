 package physics.collisionmodels;
 
 import game.Main;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import util.math.Line;
 import util.math.MathHelper;
 import util.math.Plane;
 
 /**
  * a Box shaped CollisionBox
  * @author Felix Schmidt
  */
 public class CollisionBox
 {
 	/**
 	 * one edge of the box where all the other vectors start
 	 */
 	public Vector3f startingPoint = new Vector3f(0, 0, 0);
 	
 	/**
 	 * a array which contains all 8 edges of the box
 	 */
 	public Vector3f[] points = new Vector3f[8];
 	
 	/**
 	 * the middle of the box
 	 */
 	public Vector3f middle = new Vector3f (0, 0, 0);
 	
 	/**
 	 * the depth of the box
 	 */
 	public Vector3f depth = new Vector3f (0, 0, 0);
 	
 	/**
 	 * the height of the box
 	 */
 	public Vector3f width = new Vector3f (0, 0, 0);
 	
 	/**
 	 * the width of the box
 	 */
 	public Vector3f height = new Vector3f (0, 0, 0);
 	
 	/**
 	 * the mass of the ellipsoid
 	 * used to calculate falling of objects
 	 */
 	public float mass = 0;
 	
 	/**
 	 * creates a standard CollisionBox object with all values set to 0
 	 */
 	public CollisionBox () {	}
 	
 	/**
 	 * creates a CollisionBox with a startingPoint, depth, width and height but mass set to 0
 	 * @param startingPoint one edge of the box where all the other vectors start
 	 * @param depth the depth of the box
 	 * @param width the width of the box
 	 * @param height the height of the box
 	 */
 	public CollisionBox (Vector3f startingPoint, Vector3f depth, Vector3f width, Vector3f height)
 	{
 		this.startingPoint = startingPoint;
 		this.depth = depth;
 		this.width = width;
 		this.height = height;
 		//setting the edges
 		points[0] = startingPoint;
 		Vector3f.add(startingPoint, width, points[1]);
 		Vector3f.add(points[1], depth, points[2]);
 		Vector3f.add(startingPoint, depth, points[3]);
 		Vector3f.add(startingPoint, height, points[4]);
 		Vector3f.add(points[4], width, points[5]);
 		Vector3f.add(points[5], depth, points[6]);
 		Vector3f.add(points[4], depth, points[7]);
 		//Setting the middle
 		Vector3f.add ((Vector3f)depth.scale (0.5f), (Vector3f)width.scale (0.5f), middle);
 		Vector3f.add ((Vector3f)middle, (Vector3f)height.scale (0.5f), middle);
 	}
 	
 	/**
 	 * creates a CollisionBox with a mass, startingPoint, depth, width and height
 	 * @param mass the mass of the surrounded object used to calculate falling of objects
 	 * @param startingPoint one edge of the box where all the other vectors start
 	 * @param depth the depth of the box
 	 * @param width the width of the box
 	 * @param height the height of the box
 	 */
 	public CollisionBox (float mass, Vector3f startingPoint, Vector3f depth, Vector3f width, Vector3f height)
 	{
 		this(startingPoint, depth, width, height);
 		this.mass = mass;
 	}
 	
 	/**
 	 * creates a CollisionBox based on points (from an object or the edges from the CollisionBox)
 	 * @param completeCollisionBox set to true if the points are only the 8 edges of the CollisionBox
 	 * @param points all the points of the object/CollisionBox (first the bottom clockwise than the top clockwise)
 	 */
 	public CollisionBox(boolean completeCollisionBox, Vector3f... points)
 	{
 		if(completeCollisionBox == true)
 		{
 			this.points = points;
 			startingPoint = points[0];
 			Vector3f.sub(points[1], points[0], width);
 			Vector3f.sub(points[3], points[0], depth);
 			Vector3f.sub(points[4], points[0], height);
 			Vector3f.add(width, depth, middle);
 			Vector3f.add(middle, height, middle);
 			middle.scale(0.5f);
 		}
 		else
 		{
 			CollisionBox temp = createCollisionBox(points);
 			startingPoint = temp.startingPoint;
 			this.points = temp.points;
 			middle = temp.middle;
 			width = temp.width;
 			depth = temp.depth;
 			height = temp.height;
 		}
 	}
 	
 	/**
 	 * creates a CollisionBox based on points (from a n object or the edges from the CollisionBox)
 	 * @param mass the mass of the surrounded object
 	 * @param completeCollisionBox set to true if the points are only the 8 edges of the CollisionBox
 	 * @param points all the points of the object/CollisionBox (first the bottom clockwise than the top clockwise)
 	 */
 	public CollisionBox (float mass, boolean completeCollisionBox, Vector3f... points)
 	{
 		this(completeCollisionBox, points);
 		this.mass = mass;
 	}
 	
 	/**
 	 * creates the best CollisionBox from an object given with points
 	 * @param points the points which represent the object
 	 * @return returns the best CollisionBox of the object
 	 */
 	/**
 	 * @param points
 	 * @return
 	 */
 	public static CollisionBox createCollisionBox(Vector3f... points)
 	{
 		//
 		//!!!VARIABLENAMES + COMMENTATION FOR AXIS IS FULLY BULLSHIT!!!
 		//
 		//in this code names are changed:
 		//from:	x-axis is right-left-axis, y-axis is front-back-axis, z-axis is height-axis
 		//to:	x-axis is right-left-axis, y-axis is height-axis, z-axis is front-back-axis
 		//Initializing standards to start with
 		float minX = points[0].x;
 		float minY = points[0].y;
 		float minZ = points[0].z;
 		float maxX = points[0].x;
 		float maxY = points[0].y;
 		float maxZ = points[0].z;
 		Vector3f pointBack = points[0];
 		Vector3f pointFront = points[0];
 		Vector3f pointLeft = points[0];
 		Vector3f pointRight = points[0];
 		Vector3f pointTop = points[0];
 		Vector3f pointBottom = points[0];
 		//setting the standards to a start value
 		for(int a = 0; a < points.length; a++)
 		{
 			if(points[a].x < minX)
 			{
 				minX = points[a].x;
 				pointLeft =points[a];
 			}
 			else if(points[a].x > maxX)
 			{
 				maxX = points[a].x;
 				pointRight =points[a];
 			}
 			if(points[a].y < minY)
 			{
 				minY = points[a].y;
 				pointBack =points[a];
 			}
 			else if(points[a].y > maxY)
 			{
 				maxY = points[a].y;
 				pointFront =points[a];
 			}
 			if(points[a].z < minZ)
 			{
 				minZ = points[a].z;
 				pointBottom =points[a];
 			}
 			else if(points[a].z > maxZ)
 			{
 				maxZ = points[a].z;
 				pointTop =points[a];
 			}
 		}
 		//
 		//Adjusting front/back-Plane
 		//
 		//Initializing "best values" for front and back Points and distances
 		Vector3f bestPointBack = new Vector3f();
 		Vector3f bestPointFront = new Vector3f();
 		Vector3f bestNormalFront = new Vector3f();
 		float minDistanceFrontBack = Math.abs(maxY - minY);
 		Vector3f normalFront = new Vector3f(0, 1, 0);
 		//working values for Planes and distances
 		float distanceFrontBack = Math.abs(maxY - minY);
 		Plane front = new Plane(normalFront, pointBack);
 		Plane back = new Plane(normalFront, pointFront);
 		front.transformToHesseNormalForm();
 		back.transformToHesseNormalForm();
 		//Rotating Planes around the object to a max value of 180 where the planes are just swapped versions of the starting planes
 		Plane rotationPlane = new Plane(new Vector3f(0, 0, 1), new Vector3f(0, 0, 0));
 		//Planes will rotate around the y-axis
 		for(int degree = 1; degree < 180; degree++)
 		{
 			MathHelper.rotateVector(normalFront, degree, rotationPlane);
 			normalFront.normalise();
 			//
 			//Rotating frontPlane and transforming it to the standard (HesseNormalForm)
 			//
 			front = new Plane(normalFront, pointFront);
 			front.transformToHesseNormalForm();
 			//Initializing temporary maximum values
 			Vector3f maxFrontPoint = new Vector3f (0, 0, 0);
 			float maxFrontDis = 0;
 			//calculating if the frontPlane has to be moved outwards
 			for(int i = 0; i < points.length; i++)
 			{
 				float frontDis = front.calculateDistancePoint(points[i]);
 				if(frontDis > maxFrontDis)
 				{
 					maxFrontDis = frontDis;
 					maxFrontPoint = points[i];
 				}
 			}
 			//calculating if the frontPlane has to be moved inwards
 			if(maxFrontDis == 0)
 			{
 				//Calculating every distance
 				ArrayList<Float> distances = new ArrayList<Float>();
 				for(int i = 0; i < points.length; i++)
 					distances.add(Float.valueOf(Math.abs(front.calculateDistancePoint(points[i]))));
 				//Comparing the distances and setting the minimum distance
 				float mindistance = Float.MAX_VALUE;
 				for(int i = 0; i < distances.size(); i++)
 					if(distances.get(i).floatValue() < mindistance)
 					{
 						maxFrontPoint = points[i];
 						mindistance = distances.get(i).floatValue();
 					}
 			}
 			//setting the temporary bestFrontPlane and normalize it
 			front = new Plane(normalFront, maxFrontPoint);
 			front.transformToHesseNormalForm();
 			//
 			//Rotating backPlane and transforming it to the standard (HesseNormalForm)
 			//
 			back = new Plane(normalFront, pointBack);
 			back.transformToHesseNormalForm();
 			if(front.normal == back.normal)
 				back.normal.negate();
 			//Initializing temporary maximum values
 			Vector3f maxBackPoint = new Vector3f (0, 0, 0);
 			float maxBackDis = 0;
 			//calculating if the backPlane has to be moved outwards
 			for(int i = 0; i < points.length; i++)
 			{
 				float backDis = back.calculateDistancePoint(points[i]);
 				if(backDis > maxBackDis)
 				{
 					maxBackDis = backDis;
 					maxBackPoint = points[i];
 				}
 			}
 			//calculating if the backPlane has to be moved inwards
 			if(maxBackDis == 0)
 			{
 				ArrayList<Float> distances = new ArrayList<Float>();
 				for(int i = 0; i < points.length; i++)
 					distances.add(Float.valueOf(Math.abs(back.calculateDistancePoint(points[i]))));
 				//Comparing the distances and setting the minimum distance
 				float mindistance = Float.MAX_VALUE;
 				for(int i = 0; i < distances.size(); i++)
 					if(distances.get(i).floatValue() < mindistance)
 					{
 						maxBackPoint = points[i];
 						mindistance = distances.get(i).floatValue();
 					}
 			}
 			//setting the round-best-Values (for the next rotation as compareValues)
 			back = new Plane(normalFront, maxBackPoint);
 			back.transformToHesseNormalForm();
			if(front.normal == back.normal)
				back.normal.negate();
 			distanceFrontBack = Math.abs(front.calculateDistancePoint(maxBackPoint));
 			if(distanceFrontBack < minDistanceFrontBack)
 			{
 				minDistanceFrontBack = distanceFrontBack;
 				bestPointFront = maxFrontPoint;
 				bestPointBack = maxBackPoint;
 				bestNormalFront = normalFront;
 			}
 			pointFront = maxFrontPoint;
 			pointBack = maxBackPoint;
 		}
 		//setting the final best front/back planes
 		front = new Plane(bestNormalFront, bestPointFront);
 		back = new Plane(bestNormalFront, bestPointBack);
 		front.transformToHesseNormalForm();
 		back.transformToHesseNormalForm();
 		//
 		//Adjusting left/right-Plane
 		//
 		//Initializing "best values" for left and right Points and distances
 		Vector3f bestPointLeft = new Vector3f();
 		Vector3f bestPointRight = new Vector3f();
 		Vector3f bestNormalLeft = MathHelper.createPerpendicularVector(bestNormalFront);
 		float minDistanceLeftRight = Math.abs(maxX - minX);
 		Vector3f normalLeft = bestNormalLeft;
 		//working values for Planes and distances
 		float distanceLeftRight = Math.abs(maxX - minX);
 		Plane left = new Plane(normalLeft, pointLeft);
 		Plane right = new Plane(normalLeft, pointRight);
 		left.transformToHesseNormalForm();
 		right.transformToHesseNormalForm();
 		//Rotating Planes around the object to a max value of 180 where the planes are just swapped versions of the starting planes
 		rotationPlane = front;
 		//Planes will rotate around the normalVector of the left/right-Plane
 		for(int degree = 1; degree < 180; degree++)
 		{
 			MathHelper.rotateVector(normalLeft, degree, rotationPlane);
 			normalLeft.normalise();
 			//
 			//Rotating leftPlane and transforming it to the standard (HesseNormalForm)
 			//
 			left = new Plane(normalLeft, pointLeft);
 			left.transformToHesseNormalForm();
 			//Initializing temporary maximum values
 			Vector3f maxLeftPoint = new Vector3f (0, 0, 0);
 			float maxLeftDis = 0;
 			//calculating if the leftPlane has to be moved outwards
 			for(int i = 0; i < points.length; i++)
 			{
 				float leftDis = left.calculateDistancePoint(points[i]);
 				if(leftDis > maxLeftDis)
 				{
 					maxLeftDis = leftDis;
 					maxLeftPoint = points[i];
 				}
 			}
 			//calculating if the leftPlane has to be moved inwards
 			if(maxLeftDis == 0)
 			{
 				//Calculating every distance
 				ArrayList<Float> distances = new ArrayList<Float>();
 				for(int i = 0; i < points.length; i++)
 					distances.add(Float.valueOf(Math.abs(left.calculateDistancePoint(points[i]))));
 				//Comparing the distances and setting the minimum distance
 				float mindistance = Float.MAX_VALUE;
 				for(int i = 0; i < distances.size(); i++)
 					if(distances.get(i).floatValue() < mindistance)
 					{
 						maxLeftPoint = points[i];
 						mindistance = distances.get(i).floatValue();
 					}
 			}
 			//setting the temporary bestLeftPlane and normalize it
 			left = new Plane(normalLeft, maxLeftPoint);
 			left.transformToHesseNormalForm();
 			//
 			//Rotating rightPlane and transforming it to the standard (HesseNormalForm)
 			//
 			right = new Plane(normalLeft, pointRight);
 			right.transformToHesseNormalForm();
 			if(left.normal == right.normal)
 				right.normal.negate();
 			//Initializing temporary maximum values
 			Vector3f maxRightPoint = new Vector3f (0, 0, 0);
 			float maxRightDis = 0;
 			//calculating if the leftPlane has to be moved outwards
 			for(int i = 0; i < points.length; i++)
 			{
 				float rightDis = right.calculateDistancePoint(points[i]);
 				if(rightDis > maxRightDis)
 				{
 					maxRightDis = rightDis;
 					maxRightPoint = points[i];
 				}
 			}
 			//calculating if the leftPlane has to be moved inwards
 			if(maxRightDis == 0)
 			{
 				ArrayList<Float> distances = new ArrayList<Float>();
 				for(int i = 0; i < points.length; i++)
 					distances.add(Float.valueOf(Math.abs(right.calculateDistancePoint(points[i]))));
 				//Comparing the distances and setting the minimum distance
 				float mindistance = Float.MAX_VALUE;
 				for(int i = 0; i < distances.size(); i++)
 					if(distances.get(i).floatValue() < mindistance)
 					{
 						maxRightPoint = points[i];
 						mindistance = distances.get(i).floatValue();
 					}
 			}
 			//setting the round-best-Values (for the next rotation as compareValues)
 			right = new Plane(normalLeft, maxRightPoint);
 			right.transformToHesseNormalForm();
			if(left.normal == right.normal)
				right.normal.negate();
 			distanceLeftRight = Math.abs(left.calculateDistancePoint(maxRightPoint));
 			if(distanceLeftRight < minDistanceLeftRight)
 			{
 				minDistanceLeftRight = distanceLeftRight;
 				bestPointRight = maxLeftPoint;
 				bestPointLeft = maxRightPoint;
 				bestNormalLeft = normalLeft;
 			}
 			pointLeft = maxLeftPoint;
 			pointRight = maxRightPoint;
 		}
 		//setting the final best left/right planes
 		left = new Plane(bestNormalLeft, bestPointLeft);
 		right = new Plane(bestNormalLeft, bestPointRight);
 		left.transformToHesseNormalForm();
 		right.transformToHesseNormalForm();
 		//
 		//Adjusting topPlane
 		//
 		//Initializing and setting the normalVector for top/bottom-plane
 		Vector3f bestNormalTop = new Vector3f();
 		Vector3f.cross(bestNormalFront, bestNormalLeft, bestNormalTop);
 		bestNormalTop.normalise();
 		//Initializing best values for top point
 		Vector3f maxPointTop = new Vector3f();
 		//working values for Planes
 		Plane top = new Plane(bestNormalTop, pointTop);
 		top.transformToHesseNormalForm();
 		float maxTopDis = 0;
 		//calculating if the topPlane has to be moved outwards
 		for(int i = 0; i < points.length; i++)
 		{
 			float topDis = top.calculateDistancePoint(points[i]);
 			if(topDis > maxTopDis)
 			{
 				maxTopDis = topDis;
 				maxPointTop = points[i];
 			}
 		}
 		//calculating if the topPlane has to be moved inwards
 		if(maxTopDis == 0)
 		{
 			ArrayList<Float> distances = new ArrayList<Float>();
 			for(int i = 0; i < points.length; i++)
 				distances.add(Float.valueOf(Math.abs(top.calculateDistancePoint(points[i]))));
 			//Comparing the distances and setting the minimum distance
 			float mindistance = Float.MAX_VALUE;
 			for(int i = 0; i < distances.size(); i++)
 				if(distances.get(i).floatValue() < mindistance)
 				{
 					maxPointTop = points[i];
 					mindistance = distances.get(i).floatValue();
 				}
 		}
 		top = new Plane(bestNormalTop, maxPointTop);
 		top.transformToHesseNormalForm();
 		//
 		//Adjusting bottomPlane
 		//
 		//Initializing best values for bottom point
 		Vector3f maxPointBottom = new Vector3f();
 		//working values for Planes
 		Plane bottom = new Plane(bestNormalTop, pointBottom);
 		bottom.transformToHesseNormalForm();
 		if(top.normal == bottom.normal)
 			bottom.normal.negate();
 		float maxBottomDis = 0;
 		//calculating if the bottomPlane has to be moved outwards
 		for(int i = 0; i < points.length; i++)
 		{
 			float bottomDis = bottom.calculateDistancePoint(points[i]);
 			if(bottomDis > maxBottomDis)
 			{
 				maxBottomDis = bottomDis;
 				maxPointBottom = points[i];
 			}
 		}
 		//calculating if the bottomPlane has to be moved inwards
 		if(maxBottomDis == 0)
 		{
 			ArrayList<Float> distances = new ArrayList<Float>();
 			for(int i = 0; i < points.length; i++)
 				distances.add(Float.valueOf(Math.abs(bottom.calculateDistancePoint(points[i]))));
 			//Comparing the distances and setting the minimum distance
 			float mindistance = Float.MAX_VALUE;
 			for(int i = 0; i < distances.size(); i++)
 				if(distances.get(i).floatValue() < mindistance)
 				{
 					maxPointBottom = points[i];
 					mindistance = distances.get(i).floatValue();
 				}
 		}
 		bottom = new Plane(bestNormalTop, maxPointBottom);
 		bottom.transformToHesseNormalForm();
		if(top.normal == bottom.normal)
			bottom.normal.negate();
 		//
 		//Calculating the edgePoints of the collisionBox
 		//
 		CollisionBox newColBox = new CollisionBox();
 		Line temp = front.intersectWithPlane(bottom);
 		//calculating the startingPoint
 		newColBox.startingPoint = left.intersectWithLine(temp);
 		//Calculating the rest points
 		newColBox.points[0] = newColBox.startingPoint;
 		newColBox.points[1] = right.intersectWithLine(temp);
 		temp = back.intersectWithPlane(bottom);
 		newColBox.points[2] = left.intersectWithLine(temp);
 		newColBox.points[3] = right.intersectWithLine(temp);
 		temp = front.intersectWithPlane(top);
 		newColBox.points[4] = left.intersectWithLine(temp);
 		newColBox.points[5] = right.intersectWithLine(temp);
 		temp = back.intersectWithPlane(top);
 		newColBox.points[6] = left.intersectWithLine(temp);
 		newColBox.points[7] = right.intersectWithLine(temp);
 		//Calculating the width
 		Vector3f.sub(newColBox.points[1], newColBox.points[0], newColBox.width);
 		//Calculating the depth
 		Vector3f.sub(newColBox.points[3], newColBox.points[0], newColBox.depth);
 		//Calculating the height
 		Vector3f.sub(newColBox.points[4], newColBox.points[0], newColBox.height);
 		//Calculating the middle
 		Vector3f.add(newColBox.width, newColBox.depth, newColBox.middle);
 		Vector3f.add(newColBox.middle, newColBox.height, newColBox.middle);
 		newColBox.middle.scale(0.5f);
 		return newColBox;
 	}
 }
