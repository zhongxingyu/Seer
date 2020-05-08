 package util.math;
 
 import org.lwjgl.util.vector.Vector3f;
 
 
 public class Plane
 {
 	public Vector3f normal = new Vector3f();
 	public Vector3f startingPoint = new Vector3f();
 	
 	public Plane() { }
 	
 	public Plane(Vector3f normal, Vector3f startingPoint)
 	{
 		this.normal.set(normal.getX(), normal.getY(), normal.getZ());
		this.startingPoint.set(startingPoint.getX(), startingPoint.getY(), startingPoint.getZ());
 	}
 	
 	public Vector3f getNormal()
 	{
 		return new Vector3f(normal.x, normal.y, normal.z);
 	}
 	
 	public Vector3f getStartingPoint()
 	{
 		return new Vector3f(startingPoint.x, startingPoint.y, startingPoint.z);
 	}
 	
 	public void setNormal(Vector3f newNormal)
 	{
 		normal = MathHelper.cloneVector(newNormal);
 	}
 	
 	public void setStartingPoint(Vector3f newStartingPoint)
 	{
 		startingPoint = MathHelper.cloneVector(newStartingPoint);
 	}
 	
 	public void negateNormal()
 	{
 		this.normal.setX(this.normal.getX() * -1);
 		this.normal.setY(this.normal.getY() * -1);
 		this.normal.setZ(this.normal.getZ() * -1);
 	}
 	
 	public Vector3f getPoint(float x, float y)
 	{
 		return new Vector3f (x, y, (Vector3f.dot(normal, startingPoint) - normal.x * x - normal.y * y) / normal.z);
 	}
 	
 	public void transformToHesseNormalForm()
 	{
 		normal.normalise ();
 		//last factor of the coordinate-form
 		float lastFactor = Vector3f.dot(normal, (Vector3f) MathHelper.cloneVector(startingPoint).negate());
 		if(lastFactor > 0)
 			normal.negate ();
 	}
 	
 	/**
 	 * calculates the distance between a point and a plane
 	 * @param point the point
 	 * @return the distance
 	 */
 	public float calculateDistancePoint(Vector3f point)
 	{
 		this.transformToHesseNormalForm();
 		return (Vector3f.dot(this.normal, point) - Vector3f.dot(this.startingPoint, this.normal));
 	}
 	
 	/**
 	 * calculates the distance between a point and a plane
 	 * @param transformToHesseNormalFormFirst if this plane should be transformed
 	 * @param point the point
 	 * @return the distance
 	 */
 	public float calculateDistancePoint(boolean transformToHesseNormalFormFirst, Vector3f point)
 	{
 		if(transformToHesseNormalFormFirst)
 			this.transformToHesseNormalForm();
 		return (Vector3f.dot(this.normal, point) - Vector3f.dot(this.startingPoint, this.normal));
 	}
 	
 	/**
 	 * intersects a line with a plane
 	 * @param line the line
 	 * @param plane the plane
 	 * @return the Vector3f to the point if parallel method returns the nullVector
 	 */
 	public Vector3f intersectWithLine(Line line)
 	{
 		//Checks weather the line is parallel to the plane
 		if(Vector3f.dot(line.getDirection(), this.normal) == 0)
 			return null;
 		this.transformToHesseNormalForm();
 		//Calculates the factor for the direction-vector of the line
 		float factor = (Vector3f.dot(this.normal, this.startingPoint)-Vector3f.dot(line.getDirection(), line.getStartingPoint()))/
 						Vector3f.dot(line.getDirection(), this.normal);
 		return line.getPoint(factor);
 	}
 	
 	/**
 	 * gives back an array of 2 vectors with the intersectionPoints of the parabola with the plane
 	 * @param par the parabola
 	 * @return a Vector3f[2]-array of the 2 intersectionPoints: both null if no solution, second null if one solution, none null if 2 solutions
 	 */
 	public Vector3f[] intersectWithParabola(Parabola par)
 	{
 		this.transformToHesseNormalForm();
 		float discriminant = Vector3f.dot(normal, par.getDirection()) * Vector3f.dot(normal, par.getDirection()) - 4 *
 								Vector3f.dot(normal, par.getInfluence()) * (Vector3f.dot(normal, par.getStartingPoint()) -
 								Vector3f.dot(normal, startingPoint));
 		if(discriminant < 0)
 		{
 			Vector3f[] ret = {null, null};
 			return ret;
 		}
 		else if(discriminant == 0)
 		{
 			float factor = -Vector3f.dot(normal, par.getDirection()) / (2 * Vector3f.dot(normal, par.getInfluence()));
 			Vector3f[] ret = {par.getPoint(factor), null};
 			return ret;
 		}
 		else
 		{
 			float factor1 = (-Vector3f.dot(normal, par.getDirection()) + (float)Math.sqrt(discriminant))/ (2 * Vector3f.dot(normal, par.getInfluence()));
 			float factor2 = (-Vector3f.dot(normal, par.getDirection()) - (float)Math.sqrt(discriminant))/ (2 * Vector3f.dot(normal, par.getInfluence()));
 			Vector3f[] ret = {par.getPoint(factor1), par.getPoint(factor2)};
 			return ret;
 		}
 	}
 	
 	/**
 	 * intersects a plane with another one
 	 * @param plane the intersectionPlane plane
 	 * @return the intersectionLine of the 2 planes if parallel null will be returned
 	 */
 	public Line intersectWithPlane(Plane plane)
 	{
 		plane.transformToHesseNormalForm();
 		this.transformToHesseNormalForm();
 		//Checks weather the planes are parallel
 		if(plane.getNormal().x == this.getNormal().x && plane.getNormal().y == this.getNormal().y && plane.getNormal().z == this.getNormal().z)
 			return null;
 		Vector3f direction = new Vector3f();
 		Vector3f.cross(this.getNormal(), plane.getNormal(), direction);
 		direction.normalise();
 		//checks if plane1 is parallel to the x-axis
 		if(plane.getNormal().x == 0)
 		{
 			//If plane1 is parallel to the xy-plane
 			if(plane.getNormal().y == 0)
 			{
 				float z = plane.startingPoint.z;
 				//Check if this is parallel to the x-axis
 				 if(this.getNormal().x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.y;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(this.normal.z == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float x = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.x;
 					return new Line(x, 0, z, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.y;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the xz-plane
 			else if(plane.normal.z == 0)
 			{
 				float y = plane.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(this.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(this.normal.y == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//checks if this is parallel to the x-axis too
 			else if(this.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.z - (this.normal.y * plane.normal.z) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.z * z) / plane.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			//Check if this is parallel to the y-axis
 			else if(this.normal.y == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.z / plane.normal.z)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float z = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 		//checks if plane1 is parallel to the y-axis
 		else if(plane.normal.y == 0)
 		{
 			//If plane1 is parallel to the xy-plane
 			if(plane.normal.x == 0)
 			{
 				float z = plane.startingPoint.z;
 				//Check if this is parallel to the x-axis
 				 if(this.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.y;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(this.normal.z == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float x = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.x;
 					return new Line(x, 0, z, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.y;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(plane.normal.z == 0)
 			{
 				float x = plane.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(this.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(this.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the y-axis too
 			else if(this.normal.y == 0)
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.z / plane.normal.z)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float z = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(this.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.z - (this.normal.y * plane.normal.z) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.z * z) / plane.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 		}
 		//checks if plane1 is parallel to the z-axis
 		else if(plane.normal.z == 0)
 		{
 			//If plane1 is parallel to the xz-plane
 			if(plane.normal.x == 0)
 			{
 				float y = plane.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(this.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(this.normal.z == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(plane.normal.y == 0)
 			{
 				float x = plane.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(this.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(this.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the z-axis too
 			else if(this.normal.z == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(this.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.z - (this.normal.y * plane.normal.z) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.z * z) / plane.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.z / plane.normal.z)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float z = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 		//checks if this is parallel to the x-axis
 		else if(this.normal.x == 0)
 		{
 			//If plane1 is parallel to the xy-plane
 			if(this.normal.y == 0)
 			{
 				float z = this.startingPoint.z;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.y;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(plane.normal.z == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.x;
 					return new Line(x, 0, z, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.y;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the xz-plane
 			else if(this.normal.z == 0)
 			{
 				float y = this.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(plane.normal.y == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//checks if this is parallel to the x-axis too
 			else if(plane.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.z - (plane.normal.y * this.normal.z) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.z * z) / this.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			//Check if this is parallel to the y-axis
 			else if(plane.normal.y == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(this.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.z / this.normal.z)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float z = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 		//checks if this is parallel to the y-axis
 		else if(this.normal.y == 0)
 		{
 			//If plane1 is parallel to the xy-plane
 			if(this.normal.x == 0)
 			{
 				float z = this.startingPoint.z;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.y;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(plane.normal.z == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.x;
 					return new Line(x, 0, z, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.y;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(this.normal.z == 0)
 			{
 				float x = this.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(plane.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(plane.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the y-axis too
 			else if(plane.normal.y == 0)
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.z / this.normal.z)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float z = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(plane.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.z - (plane.normal.y * this.normal.z) / plane.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.z * z) / this.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 		}
 		//checks if this is parallel to the z-axis
 		else if(this.normal.z == 0)
 		{
 			//If plane1 is parallel to the xz-plane
 			if(this.normal.x == 0)
 			{
 				float y = this.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(plane.normal.z == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(this.normal.y == 0)
 			{
 				float x = this.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(plane.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(plane.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the z-axis too
 			else if(plane.normal.z == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(plane.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.z - (plane.normal.y * this.normal.z) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.z * z) / this.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.z / this.normal.z)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float z = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 		else
 		{
 			//If plane1 is parallel to the xz-plane
 			if(this.normal.x == 0)
 			{
 				float y = this.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(plane.normal.z == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(this.normal.y == 0)
 			{
 				float x = this.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(plane.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(plane.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the z-axis too
 			else if(plane.normal.z == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(plane.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.z - (plane.normal.y * this.normal.z) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.z * z) / this.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.z / this.normal.z)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float z = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "normal: " + normal.toString() + "\nstartingPoint: " + startingPoint.toString();
 	}
 }
