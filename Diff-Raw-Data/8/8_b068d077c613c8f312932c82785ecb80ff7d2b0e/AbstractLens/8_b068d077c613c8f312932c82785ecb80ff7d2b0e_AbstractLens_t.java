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
 
 package ca.eandb.jmist.framework.lens;
 
 import ca.eandb.jmist.framework.Lens;
 import ca.eandb.jmist.framework.Random;
 import ca.eandb.jmist.framework.ScatteredRay;
 import ca.eandb.jmist.framework.color.WavelengthPacket;
 import ca.eandb.jmist.framework.path.EyeNode;
 import ca.eandb.jmist.framework.path.PathInfo;
 import ca.eandb.jmist.math.Point2;
 
 /**
  * @author brad
  *
  */
 public abstract class AbstractLens implements Lens {
 
	/** Serialization version ID. */
	private static final long serialVersionUID = 693720662240456494L;

 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Lens#rayAt(ca.eandb.jmist.math.Point2, ca.eandb.jmist.framework.color.WavelengthPacket, ca.eandb.jmist.framework.Random)
 	 */
 	public final ScatteredRay rayAt(Point2 p, WavelengthPacket lambda, Random rnd) {
 		PathInfo path = new PathInfo(lambda);
		EyeNode node = sample(p, path, rnd.next(), rnd.next(), rnd.next());
		return node.sample(rnd.next(), rnd.next(), rnd.next());
 	}
 
 }
