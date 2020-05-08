 /*
  * Copyright (c) 2008 Bradley W. Kimmel
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.eandb.jmist.framework.shader.ray;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Stack;
 
 import ca.eandb.jmist.framework.Illuminable;
 import ca.eandb.jmist.framework.Intersection;
 import ca.eandb.jmist.framework.Light;
 import ca.eandb.jmist.framework.LightSample;
 import ca.eandb.jmist.framework.Material;
 import ca.eandb.jmist.framework.Medium;
 import ca.eandb.jmist.framework.Modifier;
 import ca.eandb.jmist.framework.NearestIntersectionRecorder;
 import ca.eandb.jmist.framework.RayShader;
 import ca.eandb.jmist.framework.ScatteredRay;
 import ca.eandb.jmist.framework.ScatteredRays;
 import ca.eandb.jmist.framework.SceneElement;
 import ca.eandb.jmist.framework.Shader;
 import ca.eandb.jmist.framework.ShadingContext;
 import ca.eandb.jmist.framework.ScatteredRay.Type;
 import ca.eandb.jmist.framework.color.Color;
 import ca.eandb.jmist.framework.color.ColorModel;
 import ca.eandb.jmist.math.Basis3;
 import ca.eandb.jmist.math.Point2;
 import ca.eandb.jmist.math.Point3;
 import ca.eandb.jmist.math.Ray3;
 import ca.eandb.jmist.math.Vector3;
 
 /**
  * @author brad
  *
  */
 public final class SceneRayShader implements RayShader {
 
 	private final Light light;
 
 	private final SceneElement root;
 
 	private final RayShader background;
 
 	/**
 	 * @param caster
 	 * @param light
 	 * @param background
 	 */
 	public SceneRayShader(SceneElement root, Light light, RayShader background) {
 		this.root = root;
 		this.light = light;
 		this.background = background;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.RayShader#shadeRay(ca.eandb.jmist.math.Ray3)
 	 */
 	@Override
 	public Color shadeRay(Ray3 ray) {
 		Context context = new Context();
 		return context.castPrimaryRay(ray);
 	}
 
 	private final class LocalContext {
 
 		public double distance;
 		public Color importance;
 		public Ray3 ray;
 		public List<LightSample> samples;
 		public Modifier modifier;
 		public ScatteredRays scatteredRays;
 		public Shader shader;
 		public boolean front;
 		public Basis3 basis;
 		public Basis3 shadingBasis;
 		public Point3 position;
 		public Point2 uv;
 		public Medium medium;
 		public Material material;
 		public int primitiveIndex;
 
 	}
 
 	private class Context implements ShadingContext, Illuminable {
 
 		private final Stack<LocalContext> stack = new Stack<LocalContext>();
 		private final EnumMap<ScatteredRay.Type, Integer> depth = new EnumMap<ScatteredRay.Type, Integer>(ScatteredRay.Type.class);
 		private int totalDepth = 0;
 
 		public Color castPrimaryRay(Ray3 ray) {
 			Intersection x = NearestIntersectionRecorder.computeNearestIntersection(ray, root);
 
 			if (x != null) {
 				LocalContext local = new LocalContext();
 				local.ray = ray;
 				local.distance = x.getDistance();
 				local.front = x.isFront();
 				local.medium = Medium.VACUUM;
 				local.importance = getColorModel().getWhite();
 
 				stack.push(local);
 				x.prepareShadingContext(this);
 
 				local.scatteredRays = new ScatteredRays(this, ray.direction(), local.material);
 
 				Color color = shade();
 
 				stack.pop();
 				return color;
 			} else {
 				return background.shadeRay(ray);
 			}
 		}
 
 		@Override
 		public Color castRay(ScatteredRay sr) {
 			ScatteredRay.Type type = sr.getType();
 			Ray3 ray = sr.getRay();
 			Intersection x = NearestIntersectionRecorder.computeNearestIntersection(ray, root);
 
 			if (x != null) {
 				totalDepth++;
				depth.put(type, depth.get(type) + 1);
 
 				LocalContext local = new LocalContext();
 				local.ray = ray;
 				local.distance = x.getDistance();
 				local.front = x.isFront();
 				local.medium = Medium.VACUUM;
 				local.importance = sr.getColor().times(stack.peek().importance);
 
 				stack.push(local);
 				x.prepareShadingContext(this);
 
 				local.scatteredRays = new ScatteredRays(this, ray.direction(), local.material);
 
 				Color color = shade();
 
 				stack.pop();
 				depth.put(type, depth.get(type) - 1);
 				totalDepth--;
 				return color;
 			} else {
 				return background.shadeRay(ray);
 			}
 		}
 
 		@Override
 		public ColorModel getColorModel() {
 			return ColorModel.getInstance();
 		}
 
 		@Override
 		public Color getImportance() {
 			return stack.peek().importance;
 		}
 
 		@Override
 		public Iterable<LightSample> getLightSamples() {
 			List<LightSample> samples = stack.peek().samples;
 			if (samples == null) {
 				samples = new ArrayList<LightSample>();
 				stack.peek().samples = samples;
 				light.illuminate(this, this);
 			}
 			return samples;
 		}
 
 		@Override
 		public int getPathDepth() {
 			return totalDepth;
 		}
 
 		@Override
 		public int getPathDepthByType(Type type) {
			return depth.get(type);
 		}
 
 		@Override
 		public ScatteredRays getScatteredRays() {
 			return stack.peek().scatteredRays;
 		}
 
 		@Override
 		public boolean isEyePath() {
 			return true;
 		}
 
 		@Override
 		public boolean isLightPath() {
 			return false;
 		}
 
 		@Override
 		public Color shade() {
 			return getShader().shade(this);
 		}
 
 		@Override
 		public double getDistance() {
 			return stack.peek().distance;
 		}
 
 		@Override
 		public Vector3 getIncident() {
 			return stack.peek().ray.direction();
 		}
 
 		@Override
 		public boolean isFront() {
 			return stack.peek().front;
 		}
 
 		@Override
 		public Basis3 getBasis() {
 			return stack.peek().basis;
 		}
 
 		@Override
 		public Vector3 getNormal() {
 			return stack.peek().basis.w();
 		}
 
 		@Override
 		public Point3 getPosition() {
 			return stack.peek().position;
 		}
 
 		@Override
 		public Basis3 getShadingBasis() {
 			return stack.peek().shadingBasis;
 		}
 
 		@Override
 		public Vector3 getShadingNormal() {
 			return stack.peek().shadingBasis.w();
 		}
 
 		@Override
 		public Vector3 getTangent() {
 			return stack.peek().basis.u();
 		}
 
 		@Override
 		public Point2 getUV() {
 			return stack.peek().uv;
 		}
 
 		@Override
 		public boolean visibility(Ray3 ray) {
 			return root.visibility(ray);
 		}
 
 		@Override
 		public boolean visibility(Point3 p, Point3 q) {
 			return root.visibility(p, q);
 		}
 
 		@Override
 		public void addLightSample(LightSample sample) {
 			List<LightSample> samples = stack.peek().samples;
 			assert(samples != null);
 			samples.add(sample);
 		}
 
 		@Override
 		public Modifier getModifier() {
 			return stack.peek().modifier;
 		}
 
 		@Override
 		public Ray3 getRay() {
 			return stack.peek().ray;
 		}
 
 		@Override
 		public Shader getShader() {
 			return stack.peek().shader;
 		}
 
 		@Override
 		public void setAmbientMedium(Medium medium) {
 			stack.peek().medium = medium;
 		}
 
 		@Override
 		public void setBasis(Basis3 basis) {
 			stack.peek().basis = basis;
 		}
 
 		@Override
 		public void setMaterial(Material material) {
 			stack.peek().material = material;
 		}
 
 		@Override
 		public void setModifier(Modifier modifier) {
 			stack.peek().modifier = modifier;
 		}
 
 		@Override
 		public void setNormal(Vector3 normal) {
 			stack.peek().basis = Basis3.fromW(normal);
 		}
 
 		@Override
 		public void setPosition(Point3 position) {
 			stack.peek().position = position;
 		}
 
 		@Override
 		public void setPrimitiveIndex(int index) {
 			stack.peek().primitiveIndex = index;
 		}
 
 		@Override
 		public void setShader(Shader shader) {
 			stack.peek().shader = shader;
 		}
 
 		@Override
 		public void setShadingBasis(Basis3 basis) {
 			stack.peek().shadingBasis = basis;
 		}
 
 		@Override
 		public void setShadingNormal(Vector3 normal) {
 			stack.peek().shadingBasis = Basis3.fromW(normal);
 		}
 
 		@Override
 		public void setUV(Point2 uv) {
 			stack.peek().uv = uv;
 		}
 
 		@Override
 		public Medium getAmbientMedium() {
 			return stack.peek().medium;
 		}
 
 		@Override
 		public Material getMaterial() {
 			return stack.peek().material;
 		}
 
 		@Override
 		public int getPrimitiveIndex() {
 			return stack.peek().primitiveIndex;
 		}
 
 		@Override
 		public boolean visibility(Ray3 ray, double maximumDistance) {
 			return root.visibility(ray, maximumDistance);
 		}
 
 	}
 
 }
