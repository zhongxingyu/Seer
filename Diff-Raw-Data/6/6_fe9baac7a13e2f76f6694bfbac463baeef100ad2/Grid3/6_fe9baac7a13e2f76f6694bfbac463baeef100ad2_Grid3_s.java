 /**
  *
  */
 package org.jmist.toolkit;
 
 import org.jmist.util.MathUtil;
 
 /**
  * An axis-aligned box divided into a grid of cells along each of
  * the three axes.  Used for ray tracing with spacial subdivision.
  * @author bkimmel
  */
 public final class Grid3 {
 
 	/**
 	 * Initializes the bounds of the grid and the number of cells
 	 * in each direction.
 	 * @param bound The bounding box of the grid.  Must have volume greater
 	 * 		than zero.
 	 * @param nx The number of cells to divide the grid into along the x-axis.
 	 * 		Must be greater than zero.
 	 * @param ny The number of cells to divide the grid into along the y-axis.
 	 * 		Must be greater than zero.
 	 * @param nz The number of cells to divide the grid into along the z-axis.
 	 * 		Must be greater than zero.
 	 */
 	public Grid3(Box3 bound, int nx, int ny, int nz) {
 		assert(bound.getVolume() > 0.0);
 		assert(nx > 0 && ny > 0 && nz > 0);
 
 		this.bound = bound;
 		this.nx = nx;
 		this.ny = ny;
 		this.nz = nz;
 		this.dx = bound.getLengthX() / (double) nx;
 		this.dy = bound.getLengthY() / (double) ny;
 		this.dz = bound.getLengthZ() / (double) nz;
 	}
 
 	/**
 	 * Represents a cell in the grid.
 	 * @author bkimmel
 	 */
 	public final class Cell {
 
 		/**
 		 * Gets the index of this cell along the x-axis.
 		 * @return The index of this cell along the x-axis.
 		 */
 		public int getX() {
 			return cx;
 		}
 
 		/**
 		 * Gets the index of this cell along the y-axis.
 		 * @return The index of this cell along the y-axis.
 		 */
 		public int getY() {
 			return cy;
 		}
 
 		/**
 		 * Gets the index of this cell along the z-axis.
 		 * @return The index of this cell along the z-axis.
 		 */
 		public int getZ() {
 			return cz;
 		}
 
 		/**
 		 * Gets the bounds of this cell.
 		 * @return The bounding box of this cell.
 		 */
 		public Box3 getBoundingBox() {
 			return cellBounds(this.cx, this.cy, this.cz);
 		}
 
 		/**
 		 * Initializes the cell indices (this constructor should
 		 * only be available to the parent class (Grid3)).
 		 * @param cx The index of this cell along the x-axis.
 		 * @param cy The index of this cell along the y-axis.
 		 * @param cz The index of this cell along the z-axis.
 		 */
 		private Cell(int cx, int cy, int cz) {
 			this.cx = cx;
 			this.cy = cy;
 			this.cz = cz;
 		}
 
 		/** The cell indices */
 		private final int cx, cy, cz;
 
 	}
 
 	/**
 	 * An interface to allow {@link Grid3#intersect(Ray3, Interval, org.jmist.toolkit.Grid3.IVisitor)}
 	 * to notify the caller about each cell passed through by the ray.
 	 * @see {@link Grid3#intersect(Ray3, Interval, org.jmist.toolkit.Grid3.IVisitor)}
 	 */
 	public static interface IVisitor {
 
 		/**
 		 * Notifies the caller to Grid3.intersect that the ray has
 		 * passed through the specified cell.  It is guaranteed that
 		 * grid.hasCellAt(cell.getX(), cell.getY(), cell.getZ()) will
 		 * return true.
 		 * @param ray The ray passed to {@link Grid3#intersect(Ray3, Interval, org.jmist.toolkit.Grid3.IVisitor)}
 		 * @param I The interval along the ray that passes through the cell (i.e.,
 		 * 		cell.getBoundingBox().contains(ray.pointAt(t)) if and only if
 		 * 		I.contains(t)).
 		 * @param cell The cell being traversed.
 		 * @return A value indicating whether {@link Grid3#intersect(Ray3, Interval, org.jmist.toolkit.Grid3.IVisitor)}
 		 * 		should continue its traversal.
 		 * @see {@link Grid3#intersect(Ray3, Interval, org.jmist.toolkit.Grid3.IVisitor)},
 		 * 		{@link Grid3.Cell#getBoundingBox()} , {@link Box3#contains(Point3)},
 		 * 		{@link Ray3#pointAt(double)}, {@link Interval#contains(double)},
 		 * 		{@link Grid3#hasCellAt(int, int, int)}.
 		 */
 		boolean visit(Ray3 ray, Interval I, Cell cell);
 
 	}
 
 	/**
 	 * Indicates whether this grid has a cell at the specified indices.
 	 * @param cx The index along the x-axis.
 	 * @param cy The index along the y-axis.
 	 * @param cz The index along the
 	 * @return
 	 */
 	public boolean hasCellAt(int cx, int cy, int cz) {
 		return 0 <= cx && cx < nx && 0 <= cy && cy < ny && 0 <= cz && cz < nz;
 	}
 
 	/**
 	 * Computes the bounding box of a cell.  Requires that
 	 * this.hasCellAt(cx, cy, cz).
 	 * @param cx The index of the cell along the x-axis.
 	 * @param cy The index of the cell along the y-axis.
 	 * @param cz The index of the cell along the z-axis.
 	 * @return The bounding box of the specified cell.
 	 * @see {@link #hasCellAt(int, int, int)}
 	 */
 	public Box3 cellBounds(int cx, int cy, int cz) {
 		assert(this.hasCellAt(cx, cy, cz));
 
 		return new Box3(
 			bound.getMinimumX() + (double) cx * dx,
 			bound.getMinimumY() + (double) cy * dy,
 			bound.getMinimumZ() + (double) cz * dz,
			bound.getMaximumX() + (double) (cx + 1) * dx,
			bound.getMaximumY() + (double) (cy + 1) * dy,
			bound.getMaximumZ() + (double) (cz + 1) * dz
 		);
 	}
 
 	/**
 	 * Gets the bounding box for this grid.
 	 * @return The bounding box of this grid.
 	 */
 	public Box3 getBoundingBox() {
 		return bound;
 	}
 
 	/**
 	 * Returns the cell that contains the specified point.
 	 * @param p The point to check for.
 	 * @return The cell that contains the specified point, or
 	 * 		null if the point is not contained in the grid.
 	 */
 	public Cell cellAt(Point3 p) {
 		Cell cell = new Cell(
 			(int) Math.floor((p.x() - bound.getMinimumX()) / dx),
 			(int) Math.floor((p.y() - bound.getMinimumY()) / dy),
 			(int) Math.floor((p.z() - bound.getMinimumZ()) / dz)
 		);
 
 		if (this.hasCellAt(cell.cx, cell.cy, cell.cz)) {
 			return cell;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns the cell that is nearest to the specified point.  This
 	 * method is guaranteed not to return null.
 	 * @param p The point to consider.
 	 * @return The cell that is nearest to the specified point.
 	 */
 	public Cell nearestCell(Point3 p) {
 		return new Cell(
 			MathUtil.threshold((int) Math.floor((p.x() - bound.getMinimumX()) / dx), 0, nx - 1),
 			MathUtil.threshold((int) Math.floor((p.y() - bound.getMinimumY()) / dy), 0, ny - 1),
 			MathUtil.threshold((int) Math.floor((p.z() - bound.getMinimumZ()) / dz), 0, nz - 1)
 		);
 	}
 
 	/**
 	 * Intersects the ray with this grid and notifies a visitor
 	 * for each cell traversed.
 	 * @param ray The ray with which to traverse the grid.
 	 * @param I The interval along the ray to consider.
 	 * @param visitor The visitor to be notified for each cell
 	 * 		traversed.
 	 * @return A value indicating whether the ray intersects
 	 * 		the grid (i.e., this.getBoundingBox().intersect(ray).intersects(I)).
 	 * @see {@link #getBoundingBox()}, {@link Box3#intersect(Ray3)},
 	 * 		{@link Interval#intersects(Interval)}.
 	 */
 	public boolean intersect(Ray3 ray, Interval I, IVisitor visitor) {
 
 		I = I.intersect(bound.intersect(ray));
 
 		if (I.isEmpty()) { // missed the grid entirely
 			return false;
 		}
 
 		Interval	cellI = new Interval(I.getMinimum(), I.getMinimum());
 		Vector3		d = ray.getDirection();
 		Point3		p = ray.pointAt(I.getMinimum());
 		Cell		cell;
 		Cell		nextCell = this.nearestCell(p);
 		double		rx = p.x() - (bound.getMinimumX() + (double) nextCell.cx * dx);
 		double		ry = p.y() - (bound.getMinimumY() + (double) nextCell.cy * dy);
 		double		rz = p.z() - (bound.getMinimumZ() + (double) nextCell.cz * dz);
 
 		do {
 
 			cell = nextCell;
 
 			double	tx, ty, tz, t;
 
 			tx = (d.x() > 0.0) ? (dx - rx) / d.x() : -rx / d.x();
 			ty = (d.y() > 0.0) ? (dy - ry) / d.y() : -ry / d.y();
 			tz = (d.z() > 0.0) ? (dz - rz) / d.z() : -rz / d.z();
 
 			if (tx < ty && tx < tz) {
 				t = tx;
 				rx = (d.x() > 0.0) ? 0.0 : dx;
 				ry += t * d.y();
 				rz += t * d.z();
 				nextCell = new Cell(cell.cx + (d.x() > 0.0 ? 1 : -1), cell.cy, cell.cz);
 			} else if (ty < tz) {
 				t = ty;
 				rx += t * d.x();
 				ry = (d.y() > 0.0) ? 0.0 : dy;
 				rz += t * d.z();
 				nextCell = new Cell(cell.cx, cell.cy + (d.y() > 0.0 ? 1 : -1), cell.cz);
 			} else { // tz <= tx && tz <= ty
 				t = tz;
 				rx += t * d.x();
 				ry += t * d.y();
 				rz = (d.z() > 0.0) ? 0.0 : dz;
 				nextCell = new Cell(cell.cx, cell.cy, cell.cz + (d.z() > 0.0 ? 1 : -1));
 			}
 
 			cellI = new Interval(cellI.getMaximum(), cellI.getMaximum() + t);
 
 			if (cellI.getMaximum() > I.getMaximum()) {
 				cellI = cellI.intersect(I);
 				visitor.visit(ray, cellI, cell);
 				break;
 			}
 
 
 		} while (I.intersects(cellI) && visitor.visit(ray, cellI, cell) && this.hasCellAt(nextCell.cx, nextCell.cy, nextCell.cz));
 
 		return true;
 
 	}
 
 
 	/** The bounding box of this grid. */
 	private final Box3		bound;
 
 	/** The number of cells in each direction. */
 	private final int		nx, ny, nz;
 
 	/** The dimensions of the cells along each axis. */
 	private final double	dx, dy, dz;
 
 }
