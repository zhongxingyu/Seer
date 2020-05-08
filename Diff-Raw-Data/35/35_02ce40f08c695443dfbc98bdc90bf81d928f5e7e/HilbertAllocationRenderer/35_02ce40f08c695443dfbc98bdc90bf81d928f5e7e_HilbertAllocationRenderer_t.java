 /*
 	Copyright: Marcus Cobden (2011)
 	This file is part of AllocationCurve.
 
 	AllocationCurve is free software: you can redistribute it and/or modify
 	it under the terms of version 3 of the GNU Lesser General Public License
 	as published by the Free Software Foundation.
 
 	AllocationCurve is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 	GNU Lesser General Public License for more details.
 
 	You should have received a copy of the GNU Lesser General Public License
 	along with AllocationCurve. If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.marcuscobden.allocationcurve.renderer;
 
 import java.awt.Dimension;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import uk.co.marcuscobden.allocationcurve.AllocationRecord;
 import uk.co.marcuscobden.allocationcurve.allocation.InetNetworkAllocationBlock;
 
 public abstract class HilbertAllocationRenderer
 		implements
 			AllocationRecordRenderer
 {
 
 	public static Point2D.Double[] caluclateCurve(final Dimension size,
 			final int iterations)
 	{
 		List<Point2D.Double> curve = new ArrayList<Point2D.Double>(
 				(int) Math.pow(4, iterations));
 
 		caluclateCurve(curve, 0, 0, size.width, 0, 0, size.height, iterations);
 
 		return curve.toArray(new Point2D.Double[0]);
 	}
 
 	protected static void caluclateCurve(final List<Point2D.Double> results,
 			final double x0, final double y0, final double xi, final double xj,
 			final double yi, final double yj, final int n)
 	{
 		/* x and y are the coordinates of the bottom left corner */
 		/* xi & xj are the i & j components of the unit x vector of the frame */
 		/* similarly yi and yj */
 
 		if (n <= 0)
 		{
 			results.add(new Point2D.Double(x0 + (xi + yi) / 2, y0 + (xj + yj)
 					/ 2));
 		}
 		else
 		{
 			caluclateCurve(results, x0, y0, yi / 2, yj / 2, xi / 2, xj / 2,
 					n - 1);
 			caluclateCurve(results, x0 + yi / 2, y0 + yj / 2, xi / 2, xj / 2,
 					yi / 2, yj / 2, n - 1);
 			caluclateCurve(results, x0 + yi / 2 + xi / 2, y0 + yj / 2 + xj / 2,
 					xi / 2, xj / 2, yi / 2, yj / 2, n - 1);
 			caluclateCurve(results, x0 + yi / 2 + xi, y0 + yj / 2 + xj,
 					-yi / 2, -yj / 2, -xi / 2, -xj / 2, n - 1);
 		}
 	}
 
 	protected final Dimension size;
 
 	public HilbertAllocationRenderer(final Dimension size)
 	{
 		this.size = size;
 	}
 
 	public Rectangle2D.Double getBlockBounds(
 			final InetNetworkAllocationBlock<InetAddress> block,
 			final int startBit, final int finishBit)
 	{
 		// State shapes
 		// 0 = u, 1 = c, 2 = n, 3 = ]
 		// Initial state of lines = 3
 		int state = 0;
 
 		double x, y, oX, oY, w, h;
 		x = y = 0;
 		// inital offset is half the size
 		oX = size.width / 2d;
 		oY = size.height / 2d;
 		w = size.width;
 		h = size.height;
 
 		// Which positions cause x/y offset, by state
 		byte match[][][] = { { { 2, 3 }, { 1, 2 } }, { { 0, 3 }, { 0, 1 } },
 				{ { 0, 1 }, { 0, 3 } }, { { 1, 2 }, { 2, 3 } } };
 
 		// What is the next state, indexed by state, then quad position
 		byte next[][] = { { 3, 0, 0, 1 }, { 2, 1, 1, 0 }, { 1, 2, 2, 3 },
 				{ 0, 3, 3, 2 } };
 
 		// 0 = down, 1 = left, 2 = up, 3 = right
 		// if we change the initial state, these will need to change
 		byte direction[][] = { { 0, -1, 2, -1 }, { 1, -1, 3, -1 },
 				{ 2, -1, 0, -1 }, { 3, -1, 1, -1 } };
 
 		// we compare 2 bits each iteration.
 		for (int bit = startBit; bit + 1 <= finishBit && bit < block.getSize(); bit += 2)
 		{
 			int pos = getPos(block, bit);
 
 			if (pos == match[state][0][0] || pos == match[state][0][1])
 				x += oX;
 			if (pos == match[state][1][0] || pos == match[state][1][1])
 				y += oY;
 
 			int next_state = next[state][pos];
 
 			// shrink the size of the offsets for the next set of comparisons
 			oX /= 2;
 			oY /= 2;
 			w /= 2;
 			h /= 2;
 
 			if (bit + 1 >= block.getSize())
 			{
 				byte d = direction[state][pos];
 				assert (d != -1);
 
 				if ((direction[state][pos] % 2) != 0)
 					w *= 2;
 				else
 					h *= 2;
 
 				if (d == 1)
 					x -= w / 2;
 				else if (d == 2)
 					y -= h / 2;
 			}
 
 			state = next_state;
 		}
 
 		return new Rectangle2D.Double(x, y, w, h);
 	}
 
 	protected int getPos(final InetNetworkAllocationBlock<InetAddress> block,
 			final int bit)
 	{
 		char block1 = (char) Math.floor(bit / 8d);
 		char block2 = (char) Math.floor((bit + 1) / 8d);
 
 		byte bits1 = block.getAddress().getAddress()[block1];
 		byte bits2 = block.getAddress().getAddress()[block2];
 
 		int sub_bit1 = (8 - 1) - ((bit) % 8);
 		int sub_bit2 = (8 - 1) - ((bit + 1) % 8);
 
 		// build the masks
 		byte mask1 = (byte) (1 << sub_bit1);
 		byte mask2 = (byte) (1 << sub_bit2);
 
 		return ((bits1 & mask1) != 0 ? 2 : 0) + ((bits2 & mask2) != 0 ? 1 : 0);
 	}
 	
 	public static int[] getBitRange(final AllocationRecord root, Collection<AllocationRecord> leaves)
 	{
 		int startBit, finishBit;
		Class<? extends InetAddress> ipVersion = null;
 		Set<InetNetworkAllocationBlock<InetAddress>> blocks = root.getBlocks();
 
 		if (blocks == null || blocks.size() == 0)
 		{
 			// Should not happen if the allocs have already been verified.
 			throw new AssertionError(
					"Alocation should have failed verification. How did we get here?");
 		}
 		else if (blocks.size() == 1)
 		{
 			@SuppressWarnings("unchecked")
 			InetNetworkAllocationBlock<InetAddress> rootBlock = blocks
 					.toArray(new InetNetworkAllocationBlock[1])[0];
 			startBit = rootBlock.getSize();
			ipVersion = rootBlock.getAddress().getClass();
 		}
 		else
 		{
 			// TODO implement code for multiple root blocks.
			throw new UnsupportedOperationException("Support for multiple root blocks is not implemented");
 		}
 		finishBit = startBit;
 		
 		for (AllocationRecord leaf : leaves)
 		{
 			for (InetNetworkAllocationBlock<InetAddress> b : leaf.getBlocks())
 				finishBit = Math.max(finishBit, b.getSize());
 		}
 		
		if ((ipVersion == Inet6Address.class && finishBit == 128) ||
				(ipVersion == Inet4Address.class && finishBit == 32))
 		{
 			startBit--;
 			finishBit--;
 		}
 	
 		int[] out = {startBit, finishBit};
 		return out;
 	}
 
 	public void render(final OutputStream output, final AllocationRecord root)
 	{
 		render(output, root, -1);
 	}
 }
