 package data;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import main.Main;
 
 public class RectilinearPixelPoly {
 
 	static final Logger log = Logger.getLogger(RectilinearPixelPoly.class
 			.getName());
 	static final String className = RectilinearPixelPoly.class.getName();
 
 	HashSet<Point> mPoints = new HashSet<Point>();
 	Point temp = new Point();
 	boolean mIsUsed = true;
 	int mDataReadings = 0;
 	Regions mRegionManager;
 
 	protected RectilinearPixelPoly(Collection<Point> initial, Regions manager) {
 		log.entering(getClass().getName(), "ctor");
 
 		if (initial.size() == 0)
 			throw new IllegalArgumentException();
 
 		mRegionManager = manager;
 		mPoints.addAll(initial);
 
 		assert (isContigious());
 	}
 
 	public boolean contains(int x, int y) {
 		temp.x = x;
 		temp.y = y;
 		return contains(temp);
 	}
 
 	public boolean contains(Point p) {
 		// log.entering(className, "contains", p);
 		boolean result = mPoints.contains(p);
 		// log.exiting(className, "contains", result);
 		return result;
 	}
 
 	/**
 	 * Allows someone to consume pixels from this polygon. There are some edge
 	 * cases where the requested number of points my not have actually been
 	 * consumed if the expansion outward from the selected start point encounter
 	 * problems. I'm trying to remove this annoyance, but for now that means
 	 * that this method should be called in a loop, checking if the desired area
 	 * was received.
 	 * 
 	 * @param amountOfArea
 	 *            the number of pixels desired. If this is greater than the size
 	 *            of this polygon, all polygon pixels are returned and this poly
 	 *            will deregister itself from the area
 	 * @return all of the points that can be consumed, up to
 	 *         Math.min(amountOfArea,size). The caller should check the size to
 	 *         see if it got what it requested
 	 */
 	public Collection<Point> consumeArea(int amountOfArea, Point startPoint) {
 		log.entering(className, "consumeArea", new Object[] { amountOfArea,
 				startPoint });
 
 		if (amountOfArea >= getArea()) {
 			mRegionManager.removeRegion(this);
 			log.exiting(className, "consumeArea", "Consuming all");
 			return mPoints;
 		}
 
 		List<Point> consumablePoints = new ArrayList<Point>(amountOfArea + 8);
 		if (isConsumable(startPoint.x, startPoint.y, consumablePoints)) {
 			// TODO - better understanding of this section
 			log.finest("Start point is consumable");
 			consumablePoints.add(startPoint);
 		} else {
 			log
 					.info("Unable to consume area - the startPoint was not consumable");
 			log.exiting(className, "consumeArea");
 			return consumablePoints;
 		}
 
 		int i = 0;
 		int lastIterationListSize = 0;
 		while (consumablePoints.size() < amountOfArea) {
 			Point cur = null;
 			try {
 				cur = consumablePoints.get(i);
 			} catch (IndexOutOfBoundsException ide) {
 				// TODO this is a pretty lame terminating condition, but it
 				// avoids an infinite loop if we have stalled
 				if (lastIterationListSize == consumablePoints.size()) {
 					// Remove all the points we used
 					mPoints.removeAll(consumablePoints);
 
 					assert (isContigious());
 
 					return consumablePoints;
 				}
 
 				log.finest("Resetting index");
 				i = 0;
 				cur = consumablePoints.get(i);
 				lastIterationListSize = consumablePoints.size();
 			}
 
 			log.finest("Checking neighbors of point " + cur
 					+ " for consumability");
 
 			if (isConsumable(cur.x, cur.y - 1, consumablePoints)) {
 				consumablePoints.add(new Point(cur.x, cur.y - 1));
 				i = 0;
 				continue;
 			}
 			if (isConsumable(cur.x + 1, cur.y, consumablePoints)) {
 				consumablePoints.add(new Point(cur.x + 1, cur.y));
 				i = 0;
 				continue;
 			}
 			if (isConsumable(cur.x, cur.y + 1, consumablePoints)) {
 				consumablePoints.add(new Point(cur.x, cur.y + 1));
 				i = 0;
 				continue;
 
 			}
 			if (isConsumable(cur.x - 1, cur.y, consumablePoints)) {
 				consumablePoints.add(new Point(cur.x - 1, cur.y));
 				i = 0;
 				continue;
 			}
 
 			// TODO I could make the above significantly faster if I kept a
 			// 'minimum' i value that we reset to, instead of constantly
 			// resetting to 0. The 'minimum' could be incremented by one each
 			// time we make it through the entire set of 4 if statements,
 			// because we know that all possible consumable points for that
 			// point have been considered. I think there are some edge cases
 			// though, so I'm not implementing the optimization right now
 
 			++i;
 
 		}
 
 		// Remove any extra
 		while (consumablePoints.size() != amountOfArea)
 			consumablePoints.remove(amountOfArea);
 
 		// Remove all the points we used
 		mPoints.removeAll(consumablePoints);
 
 		// assert(isContigious());
 
 		log.exiting(className, "consumeArea");
 		return consumablePoints;
 	}
 
 	/**
 	 * Check if this polygon touches the passed polygon
 	 * 
 	 * @param other
 	 * @return true if the two polygons touch, false otherwise
 	 */
 	public boolean touches(RectilinearPixelPoly other) {
 		log.entering(className, "touches", other);
 		if (this == other)
 			return false;
 
 		// At a later point I could actually just check the edges. Currently I
 		// know that the contains method is very fast, so it would likely take
 		// more time to internally find the edges than it would take to simply
 		// check every pixel
 		for (Point p : mPoints) {
 			if (other.contains(p.x + 1, p.y) || other.contains(p.x - 1, p.y)
 					|| other.contains(p.x, p.y + 1)
 					|| other.contains(p.x, p.y - 1)) {
 				log.exiting(className, "touches", true);
 				return true;
 			}
 		}
 
 		log.exiting(className, "touches", false);
 		return false;
 	}
 
 	/**
 	 * Given another polygon, this walks all of the edges of this polygon and
 	 * finds a point on the <b>other</b> polygon that
 	 * <ul>
 	 * <li>touches one of the edges of this polygon</li>
 	 * <li>is consumable
 	 * <li>
 	 * <li>is a leaf point on the <b>other</b> polygon (if there are any)</li>
 	 * </ul>
 	 * 
 	 * @param other
 	 *            The polygon that this {@link RectilinearPixelPoly} is about to
 	 *            call consumeArea on
 	 * @return
 	 */
 	public Point getStartPoint(RectilinearPixelPoly other) {
 		log.entering(className, "getStartPoint", other);
 		if (this == other) {
 			log.severe("A polygon was passed to its own getStartPoint method");
 			return null;
 		}
 
 		// At a later point I could actually just check the edges. Currently I
 		// know that the contains method is very fast, so it would likely take
 		// more time to internally find the edges than it would take to simply
 		// check every pixel
 		List<Point> temp = new ArrayList<Point>(0);
 		List<Point> otherPoints = other.getBorder();
 		Collections.sort(otherPoints, other.MergeRanking);
 		for (Point p : otherPoints) {
 			if (false == other.isConsumable(p.x, p.y, temp))
 				continue;
 
 			if (this.contains(p.x + 1, p.y) || this.contains(p.x - 1, p.y)
 					|| this.contains(p.x, p.y + 1)
 					|| this.contains(p.x, p.y - 1)) {
 
 				log.exiting(className, "getStartPoint", p);
 				return p;
 			}
 		}
 
 		throw new IllegalStateException(
 				"There appear to be no available starting points");
 	}
 
 	/**
 	 * Divides thi8s one polygon into two
 	 * 
 	 * @param amountOfArea
 	 *            The amount of space that should be removed from this polygon
 	 *            and given to the new one
 	 */
 	public void split(int amountOfArea) {
 		log.entering(className, "split", amountOfArea);
 
 		Point edge = null;
 		List<Point> border = getBorder();
 		Collections.sort(border, MergeRanking);
 		for (Point p : border) {
 
 			if (isConsumable(p.x, p.y, null)) {
 				edge = p;
 				break;
 			}
 		}
 
 		if (edge == null) {
 			throw new IllegalStateException("No consumable edges were found");
 		}
 
 		log.fine("Found an appropriate starting edge: " + edge);
 
 		Collection<Point> points = consumeArea(amountOfArea, edge);
 
 		if (log.isLoggable(Level.FINEST)) {
 			log.finest("Consumed pixels are:" + points);
 		}
 
 		assert (isContigious());
 
 		mRegionManager.createRegion(points);
 	}
 
 	/**
 	 * Returns all points that are on the border of this polygon, but are not
 	 * touching the perimeter of the entire area that this border is contained
 	 * within (specifically, all points that do not touch the perimeter of
 	 * {@link Regions})
 	 * 
 	 * @return
 	 */
 	public List<Point> getInnerBorder() {
 		List<Point> border = getBorder();
 		Iterator<Point> it = border.iterator();
 		Rectangle b = mRegionManager.getBorderRectangle();
 		int left = b.x;
 		int right = b.x + b.width;
 		int top = b.y;
 		int bottom = b.y + b.height;
 		while (it.hasNext()) {
 			Point p = it.next();
 			if (p.x == left || p.x == right || p.y == top || p.y == bottom)
 				it.remove();
 		}
 
 		return border;
 	}
 
 	/**
 	 * Returns all points that are on the border of this polgyon. THe returned
 	 * list is not guaranteed to be in any order. Border points are defined as
 	 * points that are not surrounded on 8 sides by points which are also in
 	 * this polygon
 	 * 
 	 * @return
 	 */
 	// TODO build ordering comparators so that the returned points are optimized
 	// for certain operations, such as splitting and merging?
 	public List<Point> getBorder() {
 		List<Point> border = new ArrayList<Point>((int) (mPoints.size() * 0.75));
 
 		for (Point p : mPoints) {
 
 			boolean isBorder = false;
 			for (int x = -1; x != 2; x++) {
 				if (isBorder)
 					break;
 
 				for (int y = -1; y != 2; y++)
 					if (contains(p.x + x, p.y + y) == false) {
 						isBorder = true;
 						break;
 					}
 			}
 
 			if (isBorder)
 				border.add(p);
 		}
 
 		return border;
 	}
 
 	public int getArea() {
 		return mPoints.size();
 	}
 
 	public void merge(Collection<Point> consumablePoints) {
 		mPoints.addAll(consumablePoints);
 
 		assert (isContigious());
 	}
 
 	/**
 	 * Sanity check method used for asserting that the last operation has not
 	 * left this poly being non-contigious
 	 * 
 	 * @return true if everything is fine, false otherwise
 	 */
 	private boolean isContigious() {
 		if (mPoints.size() == 1)
 			return true;
 
 		for (Point p : mPoints) {
 			Point[] neighbors = getRectilinearNeighboringPoints(p);
 			if (mPoints.contains(neighbors[0])
 					|| mPoints.contains(neighbors[1])
 					|| mPoints.contains(neighbors[2])
 					|| mPoints.contains(neighbors[3]))
 				continue;
 
 			log.severe("Non-contigious point " + p + " in Poly "
 					+ this.toString());
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Used so the
 	 * {@link RectilinearPixelPoly#getRectilinearNeighboringPoints(Point)}
 	 * method doesn't have to allocate memory on every invocation
 	 */
 	private Point[] tempNeighboringResult;
 
 	/**
 	 * Given a {@link Point} p, this returns the 4 points neighboring it e.g. up
 	 * down left right
 	 * 
 	 * @param p
 	 * @return
 	 */
 	private Point[] getRectilinearNeighboringPoints(Point p) {
 		if (tempNeighboringResult == null) {
 			tempNeighboringResult = new Point[4];
 			tempNeighboringResult[0] = new Point();
 			tempNeighboringResult[1] = new Point();
 			tempNeighboringResult[2] = new Point();
 			tempNeighboringResult[3] = new Point();
 		}
 		Point[] result = tempNeighboringResult;
 		int x = p.x;
 		int y = p.y;
 
 		result[0].x = x - 1;
 		result[0].y = y;
 
 		result[1].x = x + 1;
 		result[1].y = y;
 
 		result[2].x = x;
 		result[2].y = y + 1;
 
 		result[3].x = x;
 		result[3].y = y - 1;
 
 		return result;
 	}
 
 	public boolean getIsUsed() {
 		return mIsUsed;
 	}
 
 	public void setIsUsed(boolean isIt) {
 		mIsUsed = isIt;
 	}
 
 	protected void addDataReading() {
 		++mDataReadings;
 	}
 
 	protected void resetDataReadingCount() {
 		mDataReadings = 0;
 	}
 
 	public int getDataReadingCount() {
 		return mDataReadings;
 	}
 
 	/**
 	 * Returns if p can be added to the list of consumed points without breaking
 	 * this polygon into two non-touching distinct polygons
 	 * 
 	 * @param p
 	 * @param toBeConsumed
 	 *            The list of points already expected to be consumed. If there
 	 *            are none, it is acceptable to pass null
 	 * @return
 	 */
 	List<Point> tempList = new ArrayList<Point>(0);
 
 	private boolean isConsumable(int x, int y, List<Point> toBeConsumed) {
 		log.entering(className, "isConsumable", new Object[] { new Point(x, y),
 				toBeConsumed });
 		temp.x = x;
 		temp.y = y;
 
 		if (toBeConsumed == null)
 			toBeConsumed = tempList;
 
 		if (false == contains(temp)) {
 			log.finest("Passed point is not contained in this polygon");
 			log.exiting(className, "isConsumable", false);
 			return false;
 		}
 
 		if (toBeConsumed.contains(temp)) {
 			log.finest("Passed point is already scheduled for consumption");
 			log.exiting(className, "isConsumable", false);
 			return false;
 		}
 
 		// We do own it, so now we need to check how many of it's neighboring
 		// pixels would be 'available' if this pixel was consumed. If 1 pixel
 		// would be available, then we are trimming a branch down. If 2 would be
 		// available, we cannot be sure that we are not splitting a polygon in
 		// two, so we leave that pixel alone. If 3 would be available, we cannot
 		// be sure we are not splitting a polygon. If 4 are available, something
 		// is wrong, because someone had to decide that they wanted to consume
 		// this pixel. If 0 are available, we are consuming the last pixel in
 		// this poly
 		int available = 0;
 
 		// Check if north is available
 		boolean northAvail = false;
 		if (contains(x, y + 1))
 			if (false == toBeConsumed.contains(new Point(x, y + 1))) {
 				northAvail = true;
 				++available;
 			}
 
 		// Check if east is available
 		boolean eastAvail = false;
 		if (contains(x + 1, y))
 			if (false == toBeConsumed.contains(new Point(x + 1, y))) {
 				eastAvail = true;
 				++available;
 			}
 
 		// Check if south is available
 		boolean southAvail = false;
 		if (contains(x, y - 1))
 			if (false == toBeConsumed.contains(new Point(x, y - 1))) {
 				southAvail = true;
 				++available;
 			}
 
 		// Check if west is available
 		boolean westAvail = false;
 		if (contains(x - 1, y))
 			if (false == toBeConsumed.contains(new Point(x - 1, y))) {
 				westAvail = true;
 				++available;
 			}
 
 		if (available == 4 && toBeConsumed.size() != 0)
 			throw new IllegalStateException("Why are 4 available?!");
 		else if (available == 4 && toBeConsumed.size() == 0) {
 			log.exiting(className, "isConsumable", true);
 			return true;
 		}
 
 		// Check if there will still be linkings between the remaining elements
 		if (available == 3) {
 			if (!northAvail) {
 				// Check bottom-left and bottom-right
 				if (false == contains(x - 1, y - 1)
 						|| toBeConsumed.contains(new Point(x - 1, y - 1))) {
 					log.exiting(className, "isConsumable", false);
 					return false;
 				}
 				if (false == contains(x + 1, y - 1)
 						|| toBeConsumed.contains(new Point(x + 1, y - 1))) {
 					log.exiting(className, "isConsumable", false);
 					return false;
 				}
 
 				log.exiting(className, "isConsumable", true);
 				return true;
 			} else if (!eastAvail) {
 				// Check top-left and bottom-left
 				if (false == contains(x - 1, y + 1)
 						|| toBeConsumed.contains(new Point(x - 1, y + 1))) {
 					log.exiting(className, "isConsumable", false);
 					return false;
 				}
 				if (false == contains(x - 1, y - 1)
 						|| toBeConsumed.contains(new Point(x - 1, y - 1))) {
 					log.exiting(className, "isConsumable", false);
 					return false;
 				}
 
 				log.exiting(className, "isConsumable", true);
 				return true;
 			} else if (!southAvail) {
 				// Check top-left and top-right
 				if (false == contains(x - 1, y + 1)
 						|| toBeConsumed.contains(new Point(x - 1, y + 1))) {
 					log.exiting(className, "isConsumable", false);
 					return false;
 				}
 				if (false == contains(x + 1, y + 1)
 						|| toBeConsumed.contains(new Point(x + 1, y + 1))) {
 					log.exiting(className, "isConsumable", false);
 					return false;
 				}
 
 				log.exiting(className, "isConsumable", true);
 
 				return true;
 			} else if (!westAvail) {
 				// Check top-right and bottom-right
 				if (false == contains(x + 1, y + 1)
 						|| toBeConsumed.contains(new Point(x + 1, y + 1))) {
 					log.exiting(className, "isConsumable", false);
 					return false;
 				}
 				if (false == contains(x + 1, y - 1)
 						|| toBeConsumed.contains(new Point(x + 1, y - 1))) {
 					log.exiting(className, "isConsumable", false);
 					return false;
 				}
 
 				log.exiting(className, "isConsumable", true);
 
 				return true;
 			} else
 				throw new IllegalStateException(
 						"The available count was incorrect");
 		}
 
 		// We have already checked if there are 3, so there are 2 and only 2.
 		// Therefore this point is either between an available N/S or E/W, in
 		// which case we don't want to remove from the middle. Alternatively,
 		// this point is at a corner of available space, which we don't want to
 		// consume because we don't know if the two points we were connecting
 		// will remain connected once we leave.
 		if (available == 2) {
 
 			if (northAvail && southAvail || eastAvail && westAvail) {
 				log.exiting(className, "isConsumable", false);
 				return false;
 			}
 
 			// If we are here, then this is a 'corner' point. Either E/N, E/S,
 			// W/N, or W/S are available. We want to check the other 'corner' of
 			// this 2x2 square to see if the two available points would still be
 			// touching if this one was consumed
 			int x2 = 0, y2 = 0;
 			if (northAvail)
 				y2 = y + 1;
 			else
 				y2 = y - 1;
 			if (eastAvail)
 				x2 = x + 1;
 			else
 				x2 = x - 1;
 
 			// Check if the original polygon would still be contiguous if we
 			// consumed this point
 			if (contains(x2, y2)
 					&& false == toBeConsumed.contains(new Point(x2, y2))) {
 				log.exiting(className, "isConsumable", true);
 				return true;
 			}
 
 			log.exiting(className, "isConsumable", false);
 			return false;
 		}
 
 		if (available == 1) {
 			log.exiting(className, "isConsumable", true);
 			return true;
 		}
 
 		if (available == 0)
 			return true;
 
 		if (available == 0 && toBeConsumed.size() != getArea())
 			throw new IllegalStateException(
 					"Why have we reached the maximum? tbc:"
 							+ toBeConsumed.size() + ", avail:" + getArea());
 
 		throw new IllegalAccessError("How did we get here?!");
 	}
 
 	public String toString() {
 		StringBuilder b = new StringBuilder("[count=");
 		b.append(mDataReadings);
 		b.append(",area=");
 		b.append(getArea());
 		b.append(",isused=");
 		b.append(mIsUsed);
 		b.append(",change=");
 		if (mDataReadings == Main.K)
 			b.append("none]");
 		else if (mDataReadings > Main.K)
 			b.append("shrink]");
 		else
 			b.append("grow]");
 		return b.toString();
 
 	}
 
 	public String toLongString() {
 		StringBuilder b = new StringBuilder("[count=");
 		b.append(mDataReadings);
 		b.append(",area=");
 		b.append(getArea());
 		b.append(",change=");
 		if (mDataReadings == Main.K)
 			b.append("none]");
 		else if (mDataReadings > Main.K)
 			b.append("shrink,");
 		else
 			b.append("grow,");
 
 		b.append("points=");
 		b.append(mPoints.toString());
 		b.append("]");
 		return b.toString();
 	}
 
 	/**
 	 * When used to sort a list of points that are contained within this
 	 * {@link RectilinearPixelPoly}, the best starting {@link Point}s for a
 	 * merge will be placed at the front of the list. The best starting points
 	 * are defined as being those that have only one neighboring pixel (e.g.
 	 * only N, E, W, or S), and therefore are the 'leaf' pixels of this polygon.
 	 * All other points are considered to be equal in desirability
 	 */
 	Comparator<Point> MergeRanking = new Comparator<Point>() {
 
 		@Override
 		public int compare(Point o1, Point o2) {
 			int avail1 = 0, avail2 = 0;
 			for (Point p : getRectilinearNeighboringPoints(o1))
 				if (mPoints.contains(p))
 					++avail1;
 
 			for (Point p : getRectilinearNeighboringPoints(o2))
 				if (mPoints.contains(p))
 					++avail2;
 
 			if (avail1 == 1 && avail2 != 1)
 				return -1;
 			else if (avail1 != 1 && avail2 == 1)
 				return 1;
 			else
 				return 0;
 		}
 	};
 
 }
