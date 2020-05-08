 package org.andengine.extension.rubeloader;
 
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

 /*
  Author: Chris Campbell - www.iforce2d.net
 
  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.
  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:
  1. The origin of this software must not be misrepresented; you must not
  claim that you wrote the original software. If you use this software
  in a product, an acknowledgment in the product documentation would be
  appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
  misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.
 */
 
 /*
 Ported to AndEngine: Bart Hirst - http://www.linkedin.com/in/louisbhirst
 */
 
 
 /**
  * 
  *
  */
 public class Image {
 	public String name;
 	public String file;
 	public Body body;
 	public Vector2 center;
 	public float angle;
 	public float scale;
 	public boolean flip;
 	public float opacity;
 	public int filter; // 0 = nearest, 1 = linear
 	public float renderOrder;
 
 	public Vector2 corners[];
 
 	public int numPoints;
 	public float points[];
 	public float uvCoords[];
 	public int numIndices;
 	public short indices[];
 
 }
 
