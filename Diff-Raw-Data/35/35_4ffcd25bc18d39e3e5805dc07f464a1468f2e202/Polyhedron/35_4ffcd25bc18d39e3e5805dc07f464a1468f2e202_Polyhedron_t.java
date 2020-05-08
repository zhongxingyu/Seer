 /********************************************************************************
  *                                                                              *
  *  This file is part of Recraft.                                               *
  *                                                                              *
  *  Recraft is free software: you can redistribute it and/or modify             *
  *  it under the terms of the GNU General Public License as published by        *
  *  the Free Software Foundation, either version 3 of the License, or           *
  *  (at your option) any later version.                                         *
  *                                                                              *
  *  Recraft is distributed in the hope that it will be useful,                  *
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  *  GNU General Public License for more details.                                *
  *                                                                              *
  *  You should have received a copy of the GNU General Public License           *
  *  along with Recraft.  If not, see <http://www.gnu.org/licenses/>.            *
  *                                                                              *
  *  Copyright 2012 Chris Foster.                                                *
  *                                                                              *
  ********************************************************************************/
 
 package recraft.core;
 
 import math.Matrix;
 import math.Ray;
 import math.Vector;
 
 public abstract class Polyhedron
 {
	protected Matrix transform;
	protected Matrix transformInverse;
 
 	public void rotate(float radiansX, float radiansY, float radiansZ)
 	{
		Matrix matrix = Matrix.rotate(radiansX, radiansY, radiansZ);
		this.transform = this.transform.multiply(matrix);
		this.transformInverse = this.transformInverse.multiply(matrix.inverted());
 	}
 
 	public void scale(float scaleX, float scaleY, float scaleZ)
 	{
		Matrix matrix = Matrix.scale(scaleX, scaleY, scaleZ);
		this.transform = this.transform.multiply(matrix);
		this.transformInverse = this.transformInverse.multiply(matrix.inverted());
 	}
 
 	public void translate(float translationX, float translationY, float translationZ)
 	{
		Matrix matrix = Matrix.translate(translationX, translationY, translationZ);
		this.transform = this.transform.multiply(matrix);
		this.transformInverse = this.transformInverse.multiply(matrix.inverted());
 	}
 
 	public void clearTransform()
 	{
 		this.transform = new Matrix();
		this.transformInverse = new Matrix();
 	}
 
 	/** Returns true if ray intersects this polyhedron */
 	public abstract boolean intersectRay(Ray ray);
 	/** Returns true if a sphere with the given radius cast along ray intersects this polyhedron */
 	public abstract boolean intersectSphere(Ray ray, float radius);
 	/** Returns true if the given polyhedron cast along ray intersects this polyhedron */
 	public abstract boolean intersectPolyhedron(Ray ray, Polyhedron polyhedron);
 
 	/** Returns intersection information if ray intersects this polyhedron.  Returns null otherwise. */
 	public abstract Intersection castRay(Ray ray);
 	/** Returns intersection information if a sphere with the given radius cast along ray intersects this polyhedron.  Returns null otherwise. */
 	public abstract Intersection castSphere(Ray ray, float radius);
 	/** Returns intersection information if the given polyhedron cast along ray intersects this polyhedron.  Returns null otherwise. */
 	public abstract Intersection castPolyhedron(Ray ray, Polyhedron polyhedron);
 
 	/** Returns the slide vector of ray if it were to collide with and then slide along the surface of this polyhedron.  Returns null if there was no collision. */
 	public abstract Vector slideRay(Ray ray);
 	/** Returns the slide vector of a sphere with the given radius if it were to collide with and then slide along the surface of this polyhedron.  Returns null if there was no collision. */
 	public abstract Vector slideSphere(Ray ray, float radius);
 	/** Returns the slide vector of the given polyhedron if it were to collide with and then slide along the surface of this polyhedron.  Returns null if there was no collision. */
 	public abstract Vector slidePolyhedron(Ray ray, Polyhedron polyhedron);

	protected static class Plane
	{
		public Vector origin;
		public Vector normal;
		public float worldOriginDistance;

		public Plane(Vector origin, Vector normal)
		{
			this.origin = origin;
			this.normal = normal;
			this.worldOriginDistance = origin.dot(normal);
		}
	}
 }
