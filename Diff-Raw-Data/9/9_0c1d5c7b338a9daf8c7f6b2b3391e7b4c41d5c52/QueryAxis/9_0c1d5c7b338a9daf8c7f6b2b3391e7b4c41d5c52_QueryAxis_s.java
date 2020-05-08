 package es.cgalesanco.olap4j.query;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.olap4j.Axis;
 import org.olap4j.OlapException;
 import org.olap4j.mdx.AxisNode;
 import org.olap4j.mdx.IdentifierNode;
 import org.olap4j.mdx.ParseTreeNode;
 import org.olap4j.metadata.Dimension;
 import org.olap4j.metadata.Hierarchy;
 import org.olap4j.metadata.Member;
 import org.olap4j.metadata.Property;
 
 import es.cgalesanco.olap4j.query.DrillTree.Visitor;
 import es.cgalesanco.olap4j.query.mdx.CrossJoinBuilder;
 import es.cgalesanco.olap4j.query.mdx.Mdx;
 
 /**
  * <p>
  * An axis within an OLAP Query.
  * <p>
  * 
  * <p>
  * An axis has a location (columns, rows, etc) and has zero or more dimensions
  * that are placed on it.
  * </p>
  * 
  * <p>
  * {@link QueryAxis} located in the {@link org.olap4j.Axis.Standard#FILTER}
  * (filter axis) axis behave slightly different from {@link QueryAxis} located
  * in regular query axes.
  * </p>
  * 
  * <p>
  * Positioned members in an axis can be drilled/undrilled.
  * </p>
  * 
  * @author César García
  * 
  */
 public class QueryAxis {
 	private final List<QueryHierarchy> hierarchies;
 	private final DrillTree drillTree;
 	private Axis axis;
 	private boolean nonEmpty;
 	private final Query query;
 	private List<Member> sortPosition;
 	private SortOrder sortOrder;
 	private List<Property> properties;
 	private List<HierarchyExpander> expanders;
 
 	/**
 	 * Creates a {@link QueryAxis}.
 	 * 
 	 * @param query
 	 *            Query that the axis belongs to.
 	 * @param location
 	 *            Location of axis (e.g. ROWS, COLUMNS).
 	 */
 	QueryAxis(Query query, Axis location) {
 		this.axis = location;
 		this.query = query;
 		hierarchies = new ArrayList<QueryHierarchy>();
 		expanders = new ArrayList<HierarchyExpander>();
 		drillTree = new DrillTree();
 	}
 
 	/**
 	 * Returns the query this instance belongs to.
 	 * 
 	 * @return the query this instance belongs to.
 	 */
 	public Query getQuery() {
 		return query;
 	}
 
 	/**
 	 * Returns the location of this axis in its query.
 	 * 
 	 * @return the location of this axis in its query. {@code null} if this is
 	 *         the special <em>unused</em> axis.
 	 */
 	public Axis getLocation() {
 		return axis;
 	}
 
 	/**
 	 * Returns whether this {@link QueryAxis} filters out empty rows. If true,
 	 * the axis filters out empty rows, and the MDX to evaluate the axis will be
 	 * generated with the "NON EMPTY" expression.
 	 * 
 	 * @return Whether this axis should filter out empty rows.
 	 */
 	public boolean isNonEmpty() {
 		return nonEmpty;
 	}
 
 	/**
 	 * Sets whether this QueryAxis filters out empty rows.
 	 * 
 	 * @see #isNonEmpty().
 	 * 
 	 * @param v
 	 *            Whether this axis should filter out empty rows
 	 */
 	public void setNonEmpty(boolean v) {
 		nonEmpty = v;
 	}
 
 	/**
 	 * Returns an unmodifiable list of the hierarchies placed on this
 	 * {@link QueryAxis}.
 	 * 
 	 * @return list of query hierarchies.
 	 */
 	public List<QueryHierarchy> getHierarchies() {
 		return Collections.unmodifiableList(hierarchies);
 	}
 
 	/**
 	 * Places a {@link QueryHierarchy} at the end of this this axis. Any
 	 * hierarchy belonging to the same dimension (including itself) are first
 	 * removed from any axis they could be placed on.
 	 * 
 	 * @param hierarchy
 	 *            The query hierarchy to add to this axis.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             {@code hierarchy} does not belongs to the axis' query (i.e.
 	 *             {@code !this.getQuery().equals(hierarchy.getQuery())}).
 	 */
 	public void addHierarchy(QueryHierarchy hierarchy) {
 		if (getQuery() != hierarchy.getQuery())
 			throw new IllegalArgumentException();
 
 		QueryAxis prevAxis = hierarchy.getAxis();
 		if (hierarchies.contains(hierarchy))
 			return;
 
 		prevAxis.doRemove(hierarchy);
 		hierarchies.add(hierarchy);
 		expanders.add(new HierarchyExpander());
 		hierarchy.setAxis(this);
 		clearSort();
 
 		if (this.getLocation() != null) {
 			Dimension dimension = hierarchy.getHierarchy().getDimension();
 			for (Hierarchy h : dimension.getHierarchies()) {
 				if (!h.equals(hierarchy.getHierarchy())) {
 					QueryHierarchy qh = getQuery().getHierarchy(h.getName());
 					if (qh != null && qh.getAxis().getLocation() != null) {
 						qh.getAxis().removeHierarchy(qh);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Removes a query hierarchy from this axis.
 	 * 
 	 * @param hierarchy
 	 *            The query hierarchy to remove from this axis.
 	 * @throws IllegalArgumentException
 	 *             {@code hierarchy} does not belongs to the axis' query (i.e.
 	 *             {@code !this.getQuery().equals(hierarchy.getQuery())}).
 	 */
 	public void removeHierarchy(QueryHierarchy hierarchy) {
 		if (getQuery() != hierarchy.getQuery())
 			throw new IllegalArgumentException();
 
 		getQuery().getUnusedAxis().addHierarchy(hierarchy);
 	}
 
 	/**
 	 * <p>
 	 * Places a {@link QueryHierarchy} object one position before in the list of
 	 * hierarchies for this axis.
 	 * </p>
 	 * 
 	 * <p>
 	 * Uses a 0 based index. For example, to place the 5th dimension on the
 	 * current axis one position before, one would need to call
 	 * {@code pullUp(4)}, so the dimension would then use axis index 4 and the
 	 * previous dimension at that position gets pushed down one position.
 	 * </p>
 	 * 
 	 * @param index
 	 *            The index of the dimension to move up one notch. It uses a
 	 *            zero based index.
 	 */
 	public void pullUp(int index) {
 		if (index <= 0)
 			return;
 
 		drillTree.prune(index - 1);
 		QueryHierarchy h = hierarchies.remove(index);
 		HierarchyExpander e = expanders.remove(index);
 		hierarchies.add(index - 1, h);
 		expanders.add(index - 1, e);
 	}
 
 	/**
 	 * Places a {@link QueryHierarchy) object one position lower in the list of
 	 * current dimensions. Uses a 0 based index. For example, to place the 4th
 	 * dimension on the current axis one position lower, one would need to call
 	 * {@code pushDown(3)}, so the dimension would then use axis index 4 and the
 	 * previous dimension at that position gets pulled up one position.
 	 * 
 	 * @param index
 	 *            The index of the dimension to move down one notch. It uses a
 	 *            zero based index.
 	 */
 	public void pushDown(int index) {
 		if (index >= hierarchies.size() - 1)
 			return;
 
 		drillTree.prune(index);
 		QueryHierarchy h = hierarchies.remove(index);
 		HierarchyExpander e = expanders.remove(index);
 		hierarchies.add(index + 1, h);
 		expanders.add(index + 1, e);
 	}
 
 	/**
 	 * Drills down a positioned member in this axis.
 	 * 
 	 * @param drilledMember
 	 *            the positioned member to drill.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the member cannot be positioned in this axis:
 	 *             drilledMember.size() > getDimensions().size() or
 	 *             !drilledMember
 	 *             .get(i).getDimension().equals(getDimensions().get(i)) for
 	 *             some 0 &lt;=i &lt; drilledMember.size()
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the drill position does not match with the axis hierarchy
 	 *             list.
 	 */
 	public void drill(Member... drilledMember) throws IllegalArgumentException {
 		checkDrillStructure(drilledMember);
 		if (expanders.get(drilledMember.length - 1).isHierarchyExpanded())
 			drillTree.remove(drilledMember);
 		else
 			drillTree.add(drilledMember);
 	}
 
 	/**
 	 * Undrills a previously drilled positioned member
 	 * 
 	 * @param position
 	 *            the positioned member to undrill.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the member cannot be positioned in this axis:
 	 *             {@code drilledMember.size() > getDimensions().size()} or
 	 *             {@code !drilledMember
 	 *             .get(i).getDimension().equals(getDimensions().get(i)) for
 	 *             some 0 <=i < drilledMember.size()}
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the drill position does not match with the axis hierarchy
 	 *             list.
 	 */
 	public void undrill(Member... position) throws IllegalArgumentException {
 		checkDrillStructure(position);
 		if (expanders.get(position.length - 1).isHierarchyExpanded())
 			drillTree.add(position);
 		else
 			drillTree.remove(position);
 	}
 
 	/**
 	 * Tests if a specified position is drilled.
 	 * 
 	 * @param position
 	 *            the position to test.
 	 * @return true if the position is drilled for this axis instance.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the drill position does not match with the axis hierarchy
 	 *             list.
 	 */
 	public boolean isDrilled(Member... position) {
 		checkDrillStructure(position);
 		boolean hierachyExpanded = expanders.get(position.length - 1)
 				.isHierarchyExpanded();
 		if (drillTree.isDrilled(position)) {
 			return !hierachyExpanded;
 		}
 
 		return hierachyExpanded;
 	}
 
 	/**
 	 * Generates a list of drilled positions within this axis instance.
 	 * 
 	 * @return the list of drilled positions.
 	 */
 	public List<Member[]> listDrills() {
 		DrillLister lister = new DrillLister();
 		try {
 			drillTree.visit(lister);
 		} catch (OlapException e) {
 			throw new RuntimeException(e);
 		}
 		return lister.getList();
 	}
 
 	/**
 	 * Expands the full hierarchy, showing every member. Removes any previous
 	 * drill/undrill operation and starts recording undrills in the drill list.
 	 * 
 	 * @param h
 	 *            hierarchy to expand.
 	 */
 	public void expandHierarchy(QueryHierarchy h) {
 		int pos = hierarchies.indexOf(h);
 		if (pos < 0)
 			return;
 
 		expanders.get(pos).expandHierarchy();
 		if (drillTree != null)
 			drillTree.clearLevel(pos);
 	}
 
 	/**
 	 * Collapses the full hierarchy, showing only its roots members. Removes any
 	 * previous drill/undrill operation and starts recording drills in the drill
 	 * list.
 	 * 
 	 * @param h
 	 *            hierarchy to collapse.
 	 */
 	public void collapseHierarchy(QueryHierarchy h) {
 		int pos = hierarchies.indexOf(h);
 		if (pos < 0)
 			return;
 
 		expanders.get(pos).collapseHierarchy();
 		if (drillTree != null)
 			drillTree.clearLevel(pos);
 	}
 
 	/**
 	 * Checks if a hierarchy is initially expanded or collapsed.
 	 * 
 	 * @param h
 	 *            hierarchy to check.
 	 * @return {@code true} if this hierarchy is initially expanded and is
 	 *         recording undrills; {@code false} if this hierarchy is initially
 	 *         collapsed and is recording drills.
 	 */
 	public boolean isExpanded(QueryHierarchy h) {
 		int pos = hierarchies.indexOf(h);
 		if (pos < 0)
 			return false;
 
 		return expanders.get(pos).isHierarchyExpanded();
 	}
 
 	/**
 	 * <P>
 	 * Sorts the positions of this axis by the value of given cell coordinates
 	 * using a given {@code SortOrder}.
 	 * </P>
 	 * 
 	 * <p>
 	 * At MDX generation time wraps the axis set expression with a
 	 * <code>SORT</code> call using a tuple with the cell coordinates as sort
 	 * expression.
 	 * </p>
 	 * 
 	 * @param coordinates
 	 *            cell coordinates to retrieve the numeric value for sorting.
 	 * @param order
 	 *            the sort order to use.
 	 */
 	public void sort(Member[] coordinates, SortOrder order) {
		if (sortPosition == null)
			sortPosition = Arrays.asList(coordinates);
		else {
 			sortPosition.clear();
 			Collections.addAll(sortPosition, coordinates);
 		}
 		sortOrder = order;
 	}
 
 	/**
 	 * Clears the current sorting settings.
 	 */
 	public void clearSort() {
 		sortPosition = null;
 		sortOrder = null;
 	}
 
 	/**
 	 * Returns the current cell coordiantes used for sorting this query.
 	 * 
 	 * @see #sort(Member[], SortOrder)
 	 * @return the current sort position. {@code null} for unsorted axes.
 	 */
 	public Member[] getSortCoordinates() {
 		if (sortPosition == null)
 			return null;
 		return sortPosition.toArray(new Member[sortPosition.size()]);
 	}
 
 	/**
 	 * Returns the current sort order for this query.
 	 * 
 	 * @return the current sort order. {@code null} for unsorted axes.
 	 */
 	public SortOrder getSortOrder() {
 		return sortOrder;
 	}
 
 	public void addDimensionProperty(Property prop) {
 		properties.add(prop);
 	}
 
 	public void addDimensionProperty(int index, Property prop) {
 		properties.add(index, prop);
 	}
 
 	public List<Property> getDimensionProperties() {
 		return Collections.unmodifiableList(properties);
 	}
 
 	/**
 	 * Sets the location of this axis in its query. Used by
 	 * {@link Query#swapAxes()}.
 	 * 
 	 * @param v
 	 *            the new location.
 	 */
 	void setLocation(Axis v) {
 		this.axis = v;
 	}
 
 	/**
 	 * Generates a {@link org.olap4j.mdx.AxisNode} representing the current
 	 * axis. Used by {@link Query#getSelect()} to generate the full query.
 	 * 
 	 * @return a {@link org.olap4j.mdx.AxisNode} representing the current axis
 	 *         state.
 	 * @throws OlapException
 	 *             If an error occurs while generating the MDX expression for
 	 *             this axis.
 	 */
 	AxisNode toOlap4j() throws OlapException {
 		if (axis != Axis.FILTER) {
 			if (hierarchies.isEmpty())
 				return null;
 			ParseTreeNode axisExpression = 
 					Mdx.hierarchize(drillTree.toOlap4j(hierarchies,
 					expanders));
 			if (sortPosition != null) {
 				axisExpression = Mdx.order(axisExpression, sortPosition,
 						sortOrder);
 			}
 			List<IdentifierNode> props = listDimensionProperties();
 			return new AxisNode(null, nonEmpty, axis, props, axisExpression);
 		} else {
 			return toOlap4jFilter();
 		}
 	}
 
 	/**
 	 * Helper function to compute the list of dimension properties to retrieve
 	 * for this axis.
 	 * 
 	 * @return the list of dimension properties to retrieve for this axis.
 	 */
 	private List<IdentifierNode> listDimensionProperties() {
 		List<IdentifierNode> props = null;
 		if (axis != Axis.FILTER && properties != null) {
 			props = new ArrayList<IdentifierNode>();
 			for (Property p : properties) {
 				props.add(IdentifierNode.parseIdentifier(p.getUniqueName()));
 			}
 		}
 		return props;
 	}
 
 	/**
 	 * Helper function to generate {@link org.olap4j.mdx.AxisNode} representing
 	 * the current FILTER axis state.
 	 * 
 	 * @return a {@link org.olap4j.mdx.AxisNode} representing the current filter
 	 *         axis state.
 	 */
 	private AxisNode toOlap4jFilter() {
 		CrossJoinBuilder xJoin = new CrossJoinBuilder();
 		for (QueryHierarchy h : hierarchies) {
 			xJoin.join(h.toOlap4j());
 		}
 		return new AxisNode(null, false, axis, null, xJoin.getJoinNode());
 	}
 
 	/**
 	 * Helper function to check if a position matchs the current list of
 	 * hierarchies.
 	 * 
 	 * @param drilledMember
 	 *            the position to check.
 	 */
 	private void checkDrillStructure(Member[] drilledMember) {
 		int drillDepth = drilledMember.length;
 		if (drillDepth > getHierarchies().size())
 			throw new IllegalArgumentException(
 					"Drill member does not match axis structure");
 		for (int i = 0; i < drillDepth; ++i) {
 			if (!getHierarchies().get(i).getHierarchy()
 					.equals(drilledMember[i].getHierarchy()))
 				throw new IllegalArgumentException(
 						"Drill member does not match axis structure");
 		}
 	}
 
 	/**
 	 * Helper function to remove a hierarchy from this axis. Produces a
 	 * <em>dangling</em> hierarchy.
 	 * 
 	 * @param hierarchy
 	 *            hierarchy to remove.
 	 */
 	private void doRemove(QueryHierarchy hierarchy) {
 		int position = hierarchies.indexOf(hierarchy);
 		if (position >= 0) {
 			drillTree.prune(position);
 			hierarchies.remove(position);
 			expanders.remove(position);
 		}
 	}
 
 	/**
 	 * Implementation of a {@link es.cgalesanco.query.DrillTree.Visitor} to
 	 * generate the list of drilled positions in this axis.
 	 * 
 	 * @author César García
 	 * 
 	 */
 	// TODO check if this code should belong to DrillTree.
 	private static class DrillLister implements Visitor {
 
 		private final List<Member[]> result;
 
 		public DrillLister() {
 			result = new ArrayList<Member[]>();
 		}
 
 		public List<Member[]> getList() {
 			return result;
 		}
 
 		@Override
 		public void visit(List<Member> parents, List<Member> drills)
 				throws OlapException {
 			if (drills != null && !drills.isEmpty()) {
 				for (Member m : drills) {
 					Member[] next = parents
 							.toArray(new Member[parents.size() + 1]);
 					next[parents.size()] = m;
 					result.add(next);
 				}
 			}
 		}
 
 		@Override
 		public boolean shouldVisitChild(int hierarchyPos, Member child) {
 			return true;
 		}
 	}
 }
