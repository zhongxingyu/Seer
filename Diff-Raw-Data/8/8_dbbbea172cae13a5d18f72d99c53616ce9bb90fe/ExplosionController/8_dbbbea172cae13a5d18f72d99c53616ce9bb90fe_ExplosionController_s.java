 package poomonkeys.common;
 
 import java.util.ArrayList;
 
 public class ExplosionController extends Thread
 {
 	ArrayList<ArrayList<Dirt>> explosions = new ArrayList<ArrayList<Dirt>>();
 	Terrain t;
 
 	static float DIRT_SIZE = .2f;
 	static ExplosionController instance = null;
 
 	public static ExplosionController getInstance()
 	{
 		if (instance == null)
 		{
 			instance = new ExplosionController();
 		}
 		return instance;
 	}
 
 	public void init(Terrain t)
 	{
 		this.t = t;
 	}
 
 	// x and y are relative to the bottom left of the terrain
 	public void explode(float x, float y, float r)
 	{
 		// Helpful square at click point
 		Dirt test = new Dirt();
 		test.x = x;
 		test.y = y;
 		test.width = 1;
 		test.height = 1;
 		t.registerDrawable(test);
 
 		// The min and max terrain index that lies within the explosion radius
 		int min_index = (int) ((x - r) / t.segmentWidth) + 1;
 		int max_index = (int) ((x + r) / t.segmentWidth);
 
 		if (max_index > t.NUM_POINTS - 1)
 		{
 			max_index = t.NUM_POINTS - 1;
 		}
 		if (min_index < 0)
 		{
 			min_index = 0;
 		}
 
 		// The actual x value at the min and max indexes
 		float minX = min_index * t.segmentWidth;
 		float maxX = max_index * t.segmentWidth;
 		
 		ArrayList<Dirt> dirt;
 		float totalDirtVolume = 0;
 
 		dirt = _generateDirtpoints(minX, maxX, x, y, r);
 		totalDirtVolume = _collapseTerrain(min_index, max_index, x, y, r);
 
 		// Assign a portion of the removed dirt volume to each dirt point
 		float individualDirtVolume = totalDirtVolume / dirt.size();
 		for (int d = 0; d < dirt.size(); d++)
 		{
 			dirt.get(d).volume = individualDirtVolume;
 		}
 
 		// Add the dirt point list as a new explosion to be animated
 		explosions.add(dirt);
 
 		// Start the animation process if it isn't running
 		if (!this.isAlive())
 		{
 			this.start();
 		}
 	}
 
 	/*
 	 * Sets terrain heights to the bottom of the explosion circle
 	 */
 	private float _collapseTerrain(int min_index, int max_index, float x, float y, float r)
 	{
 		float dirtVolume = 0;
 
 		for (int i = min_index; i <= max_index; i++)
 		{
 			float xFromI = i * t.segmentWidth;
 			float offset = (float) Math.sqrt(r * r - Math.pow(xFromI - x, 2));
 			float tCircleY = (float) (offset + y);
 			float bCircleY = (float) (y - offset);
 
 			// checks if the land is above the bottom of the circle, otherwise
 			// don't mess with it.
 			if (t.points[i] > bCircleY)
 			{
 				if (t.points[i] > tCircleY)
 				{
 					dirtVolume += t.points[i] - tCircleY;
 				}
 
 				t.points[i] = bCircleY;
 			}
 		}
 		t.buildGeometry(t.width, t.height);
 		t.finalizeGeometry();
 
 		return dirtVolume;
 	}
 
 	private ArrayList<Dirt> _generateDirtpoints(float minX, float maxX, float x, float y, float r)
 	{
 		ArrayList<Dirt> dirt = new ArrayList<Dirt>();
 
 		float explosion_width = Math.abs(maxX - minX);
 		int num_dirt_columns = (int) ((explosion_width + t.segmentWidth) / (2 * DIRT_SIZE));
 		float gapTotal = (explosion_width + t.segmentWidth) % (2 * DIRT_SIZE);
 		float gap = gapTotal / num_dirt_columns;
 		float col_x = minX - t.segmentWidth/2 + DIRT_SIZE + gap/2;
 		float end_x = maxX + t.segmentWidth/2 - DIRT_SIZE - gap/2;
 		float EPSILON = .0001f;
 		for (; col_x <= end_x + EPSILON; col_x += DIRT_SIZE * 2 + gap)
 		{
 			_generateDirtColumn(col_x, x, y, r, gap, dirt);
 		}
 
 		return dirt;
 	}
 
 	private float _generateDirtColumn(float col_x, float x, float y, float r, float gap, ArrayList<Dirt> dirt)
 	{
 		float thing = (float) (r * r - Math.pow(col_x - x, 2));
 		float offset = 0;
 		if(thing > 0)
 		{
 			offset = (float) Math.sqrt(thing);
 		}
 		else
 		{
 			offset = 0;
 		}
 		float tCircleY = (float) (offset + y);
 
 		int iFromX = (int) (col_x / t.segmentWidth);
 
 		float xPercent = (col_x % t.segmentWidth) / t.segmentWidth;
 		float p1y = t.points[iFromX];
 		float p2y = t.points[iFromX + 1];
 		float top = p1y + (p2y - p1y) * xPercent;
 		if (top > tCircleY)
 		{
 			float d = 0;
 			while (tCircleY + d <= top)
 			{
 				Dirt dirtPoint = new Dirt();
 				dirtPoint.x = col_x;
 				dirtPoint.y = tCircleY + d;
 				float distance = (float) Math.sqrt(Math.pow(dirtPoint.x - x, 2) + Math.pow(dirtPoint.y - y, 2));
 				float nx = (dirtPoint.x - x) / distance;
 				float ny = (dirtPoint.y - y) / distance;
 				distance += 1;
 				dirtPoint.vx = nx / (distance*distance) * 8f;
 				dirtPoint.vy = ny / (distance*distance) * 8f;
 				dirtPoint.width = DIRT_SIZE * 2 + gap;
 				dirtPoint.height = DIRT_SIZE * 2 + gap;
 				dirt.add(dirtPoint);
 				t.registerDrawable(dirtPoint);
 				d += DIRT_SIZE * 2 + gap;
 			}
 		}
 
 		return top - tCircleY;
 	}
 
 	/**
 	 * Animates the dirt points and handles adding them back to the terrain
 	 */
 	public void run()
 	{
 		while (!explosions.isEmpty())
 		{
 			for (int e = 0; e < explosions.size(); e++)
 			{
 				ArrayList<Dirt> dirt = explosions.get(e);
 				for (int i = 0; i < dirt.size(); i++)
 				{
 					Dirt d = dirt.get(i);
 					d.vy += -.01;
 					d.x += d.vx;
 					d.y += d.vy;
 					
 					int iFromX = (int) (d.x / t.segmentWidth);
 					int iFromPreviousX = (int) ((d.x-d.vx) / t.segmentWidth);
 
 					double percent = (d.x % t.segmentWidth) / t.segmentWidth;
 					double landYatX = t.points[iFromX] + (t.points[iFromX + 1] - t.points[iFromX]) * percent;
 					
 					if(d.y > landYatX)
 					{
 						continue;
 					}
 
 					int minIndex = iFromX;
 					int maxIndex = iFromPreviousX;
 					if(minIndex > maxIndex)
 					{
 						int temp = minIndex;
 						minIndex = maxIndex;
 						maxIndex = temp;
 					}
 					boolean dirtRemoved = false;
 					for(int s = minIndex; s <= maxIndex; s++)
 					{
 						float xFromIndex = s*t.segmentWidth;
 						float xFromNextIndex = (s+1)*t.segmentWidth;
 						float[] intersect = lineIntersect(d.x-d.vx, d.y-d.vy, d.x, d.y, xFromIndex, t.points[s], xFromNextIndex, t.points[s+1], t.previousPoints[s], t.previousPoints[s+1]);
 						if(intersect != null)
 						{
 							d.x = intersect[0];
 							d.y = intersect[1];
 							addDirt(d);
 							
 							t.unregisterDrawable(d);
 							dirt.remove(i);
 							dirtRemoved = true;
 							i--;
 							break;
 						}
 					}
 					if(dirtRemoved)
 					{
 						continue;
 					}
 				}
 			}
 			
 			for (int i = 0; i < t.offsets.length; i++)
 			{			
 				t.previousPoints[i] = t.points[i];
 				t.points[i] += t.offsets[i];
 				t.offsets[i] = 0;
 			}
 
 			t.buildGeometry(t.width, t.height);
 			t.finalizeGeometry();
 			
 			try
 			{
 				Thread.currentThread().sleep(20);
 			} catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void addDirt(Dirt d)
 	{
 		int firstIndex = Math.round((d.x - d.width/2) / t.segmentWidth);
 		int lastIndex = Math.round((d.x + d.width/2) / t.segmentWidth);
 		
 		if(firstIndex == lastIndex)
 		{
 			t.offsets[firstIndex] += d.volume;
 		}
 		else
 		{
 			for(int j = firstIndex; j <= lastIndex; j++)
 			{
 				float amount_outside = (float) (Math.max(d.x+d.width/2 - (j+.5)*t.segmentWidth, 0) + Math.max((j-.5)*t.segmentWidth-d.x+d.width/2, 0));
 				float percent_outside = amount_outside / d.width;
 				float percent_overlap = 1 - percent_outside;
 				
 				float difPrevious = Math.max(t.points[j] - t.points[j-1], 0);
 				float difNext = Math.max(t.points[j] - t.points[j+1], 0);
 				float totalDif = difPrevious + difNext;
 				
 				float volume = d.volume * percent_overlap;
 				
 				/*if(difPrevious > 0 && difNext > 0)
 				{
 					float percentFalling = Math.min(.75f, totalDif/.1f);
 					float previousPercent = difPrevious / totalDif;
 					float nextPercent = difNext / totalDif;
 					float fallingVolume = volume * percentFalling;
 					volume = volume - fallingVolume;
 					t.offsets[j-1] += fallingVolume*previousPercent;
 					t.offsets[j+1] += fallingVolume*nextPercent;
 				}
 				else if(difPrevious > 0)
 				{
 					float percentFalling = Math.min(.5f, difPrevious/.1f);
 					float fallingVolume = volume * percentFalling;
 					volume = volume - fallingVolume;
 					t.offsets[j-1] += fallingVolume;
 				}
 				else if(difNext > 0)
 				{
 					float percentFalling = Math.min(.5f, difNext/.1f);
 					float fallingVolume = volume * percentFalling;
 					volume = volume - fallingVolume;
 					t.offsets[j+1] += fallingVolume;
 				}*/
 				t.offsets[j] += volume;
 			}
 		}
 	}
 	
 	public float[] lineIntersect(float px1, float py1, float px2, float py2, float lx1, float ly1, float lx2, float ly2, float old_ly1, float old_ly2)
 	{
 		float a = px1;// (* dirt point x *)
 		float b = py1;// (* dirt point y *)
 		float c = px2 - px1;// (* dirt point x velocity *)
 		float d = py2 - py1;// (* dirt point y velocity *)
 		float w = lx2 - lx1;// (* terrain segment width *)
 		float i = lx1; // (* left terrain point x *)
 		float e = old_ly1; // (* previous left terrain point y *)
 		float f = old_ly2; // (* previous right terrain point y *)
 		float g = ly1;// (* current left terrain point y *)
 		float h = ly2;// (* current right terrain point y *)
 		float j = g - e;// (* change in left terrain point y *)
 		float k = h - f;// (* change in right terrain point y *)
 
 		if(j == 0 && k == 0)
 		{
 			// Terrain hasn't moved, perform standard line intersection
 			float denom = (ly2 - ly1) * (px2 - px1) - (lx2 - lx1) * (py2 - py1);
 			if(denom == 0)
 			{
 				return null;
 			}
 			
 			float ua = ((lx2 - lx1) * (py1 - ly1) - (ly2 - ly1) * (px1 - lx1)) / denom;
 			float ub = ((px2 - px1) * (py1 - ly1) - (py2 - py1) * (px1 - lx1)) / denom;
 			if (ua >= 0 && ua <= 1.0f && ub >= 0 && ub <= 1.0f)
 			{
 				float intersect[] = new float[2];
 				intersect[0] = px1 + ua * (px2 - px1);
 				intersect[1] = py1 + ua * (py2 - py1);
 				return intersect;
 			}
 			
 			return null;
 		}
 		
 		if(c==0)
 		{
 			// dirt is moving straight down, special case
 			float t = (-a*e + a*f + e*i - f*i - b*w + e*w)/(a*j - i*j - a*k + i*k + d*w - j*w);
 			float intersect[] = new float[2];
 			intersect[0] = px1+c*t;
 			intersect[1] = py1+d*t;
 			
 			return intersect;
 		}
 		
 		float A = c*(j-k);
 		float B = c*(e-f) + j*(a-i) + k*(i-a) + w*(d-j);
 		float C = a*(e-f) + i*(f-e) + w*(b-e);
 		
 		float t1 = (float) ((-B - Math.sqrt(B*B - 4*A*C)) / (2*A));
 		float t2 = (float) ((-B + Math.sqrt(B*B - 4*A*C)) / (2*A));
 		float m1 = (a+c*t1-i)/w;
 		float m2 = (a+c*t2-i)/w;
 		
 		float intersect[] = new float[2];
 		if(t1 >= 0 && t1 <= 1 && m1 >=0 && m1 <= 1)
 		{
 			intersect[0] = px1+c*t1;
 			intersect[1] = py1+d*t1;
 			
 			return intersect;
 		}
 		else if(t2 >= 0 && t2 <= 1 && m2 >= 0 && m2 <= 1)
 		{
 			intersect[0] = px1+c*t2;
 			intersect[1] = py1+d*t2;
 			return intersect;
 		}
 		return null;
 	}
 }
