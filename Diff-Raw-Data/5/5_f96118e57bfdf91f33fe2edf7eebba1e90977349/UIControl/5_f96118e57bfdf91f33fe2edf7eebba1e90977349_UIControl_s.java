 package com.evervoid.client.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.evervoid.client.KeyboardKey;
 import com.evervoid.client.graphics.EverNode;
 import com.evervoid.client.graphics.geometry.Transform;
 import com.evervoid.client.views.Bounds;
 import com.evervoid.state.geometry.Dimension;
 import com.jme3.math.Vector2f;
 
 public class UIControl extends EverNode implements Resizeable
 {
 	public enum BoxDirection
 	{
 		HORIZONTAL, VERTICAL;
 	}
 
 	private Bounds aComputedBounds = null;
 	private final List<Resizeable> aControls = new ArrayList<Resizeable>();
 	private final BoxDirection aDirection;
 	private UIFocusable aFocusedElement = null;
 	private Dimension aMinimumDimension = null;
 	private final Transform aOffset;
 	private UIControl aParent = null;
 	private final Map<Resizeable, Integer> aSprings = new HashMap<Resizeable, Integer>();
 
 	public UIControl()
 	{
 		this(BoxDirection.HORIZONTAL);
 	}
 
 	public UIControl(final BoxDirection direction)
 	{
 		aDirection = direction;
 		aOffset = getNewTransform();
 	}
 
 	void addChildUI(final Resizeable control)
 	{
 		addChildUI(control, 0);
 	}
 
 	void addChildUI(final Resizeable control, final int spring)
 	{
 		aControls.add(control);
 		aSprings.put(control, spring);
 		addNode((EverNode) control);
 		if (control instanceof UIControl) {
 			// Update parent
 			((UIControl) control).aParent = this;
 		}
 		recomputeAllBounds();
 	}
 
 	/**
 	 * Add a spacer to the inner UIControl, with no spring.
 	 * 
 	 * @param width
 	 *            The width of the spacer
 	 * @param height
 	 *            The height of the spacer
 	 */
 	public void addSpacer(final int width, final int height)
 	{
 		addUI(new SpacerControl(width, height));
 	}
 
 	/**
 	 * Add a control to the inner UIControl with no spring
 	 * 
 	 * @param control
 	 *            The control to add
 	 */
 	public void addUI(final Resizeable control)
 	{
 		addUI(control, 0);
 	}
 
 	/**
 	 * Add a control to the inner UIControl. Overridden by container subclasses
 	 * 
 	 * @param control
 	 *            The control to add
 	 * @param spring
 	 *            The spring value
 	 */
 	public void addUI(final Resizeable control, final int spring)
 	{
 		addChildUI(control, spring);
 	}
 
 	public void click(final Vector2f point)
 	{
 		if (aComputedBounds == null) {
 			return;
 		}
 		if (aComputedBounds.x > point.x || aComputedBounds.y > point.y || aComputedBounds.x + aComputedBounds.width < point.x
 				|| aComputedBounds.y + aComputedBounds.height < point.y) {
 			return; // Out of bounds
 		}
 		final UIControl root = getRootUI();
 		final UIFocusable focusedNode = root.aFocusedElement;
 		if (this instanceof UIFocusable && !equals(focusedNode)) {
 			// Got new focused element
 			if (focusedNode != null) {
 				focusedNode.defocus();
 			}
 			((UIFocusable) this).focus();
 			root.aFocusedElement = ((UIFocusable) this);
 		}
 		for (final Resizeable c : aControls) {
 			if (c instanceof UIControl) {
 				((UIControl) c).click(new Vector2f(point.x - aComputedBounds.x, point.y - aComputedBounds.y));
 			}
 		}
 	}
 
 	protected void deleteChild(final UIControl control)
 	{
 		if (aControls.remove(control)) {
 			// If removal was successful, recompute bounds
 			recomputeAllBounds();
 		}
 	}
 
 	protected void deleteUI()
 	{
 		if (aParent != null) {
 			aParent.deleteChild(this);
 		}
 	}
 
 	@Override
 	public Bounds getComputedBounds()
 	{
 		return aComputedBounds;
 	}
 
 	@Override
 	public Dimension getMinimumSize()
 	{
 		int totalWidth = 0;
 		int totalHeight = 0;
 		for (final Resizeable c : aControls) {
 			final Dimension d = c.getMinimumSize();
 			if (aDirection.equals(BoxDirection.HORIZONTAL)) {
 				totalWidth += d.width;
 				totalHeight = Math.max(totalHeight, d.height);
 			}
 			else {
 				totalWidth = Math.max(totalWidth, d.width);
 				totalHeight += d.height;
 			}
 		}
 		if (aMinimumDimension != null) {
 			totalWidth = Math.max(aMinimumDimension.width, totalWidth);
 			totalHeight = Math.max(aMinimumDimension.height, totalHeight);
 		}
 		return new Dimension(totalWidth, totalHeight);
 	}
 
 	protected UIControl getRootUI()
 	{
 		if (aParent == null) {
 			return this;
 		}
 		UIControl parent = aParent;
 		while (parent.aParent != null) {
 			parent = parent.aParent;
 		}
 		return parent;
 	}
 
 	public void onKeyPress(final KeyboardKey key)
 	{
 		final UIFocusable focused = getRootUI().aFocusedElement;
		if (focused != null) {
 			focused.onKeyPress(key);
 		}
 	}
 
 	public void onKeyRelease(final KeyboardKey key)
 	{
 		final UIFocusable focused = getRootUI().aFocusedElement;
		if (focused != null) {
 			focused.onKeyRelease(key);
 		}
 	}
 
 	/**
 	 * Recomputes all dimension of everything in the UI
 	 */
 	private void recomputeAllBounds()
 	{
 		final UIControl root = getRootUI();
 		if (root.aComputedBounds != null) {
 			root.setBounds(root.aComputedBounds);
 		}
 	}
 
 	@Override
 	public void setBounds(final Bounds bounds)
 	{
 		aComputedBounds = bounds;
 		aOffset.translate(bounds.x, bounds.y);
 		int availWidth = bounds.width;
 		int availHeight = bounds.height;
 		int totalSprings = 0;
 		final Map<Resizeable, Dimension> minimumSizes = new HashMap<Resizeable, Dimension>();
 		for (final Resizeable c : aControls) {
 			final Dimension d = c.getMinimumSize();
 			minimumSizes.put(c, d);
 			availWidth -= d.width;
 			availHeight -= d.height;
 			totalSprings += aSprings.get(c);
 		}
 		float springSize = availWidth / Math.max(1, totalSprings);
 		final List<Resizeable> controls = new ArrayList<Resizeable>(aControls);
 		if (aDirection.equals(BoxDirection.VERTICAL)) {
 			springSize = availHeight / Math.max(1, totalSprings);
 			// If this is vertical, we want the first control to be at the top, so reverse the
 			Collections.reverse(controls);
 		}
 		int currentX = 0;
 		int currentY = 0;
 		for (final Resizeable c : controls) {
 			final Dimension d = minimumSizes.get(c);
 			if (aDirection.equals(BoxDirection.HORIZONTAL)) {
 				final int cWidth = (int) (d.width + aSprings.get(c) * springSize);
 				c.setBounds(new Bounds(currentX, currentY, cWidth, bounds.height));
 				currentX += cWidth;
 			}
 			else {
 				final int cHeight = (int) (d.height + aSprings.get(c) * springSize);
 				c.setBounds(new Bounds(currentX, currentY, bounds.width, cHeight));
 				currentY += cHeight;
 			}
 		}
 	}
 
 	public void setMinimumDimension(final Dimension minimum)
 	{
 		aMinimumDimension = minimum;
 	}
 
 	@Override
 	public String toString()
 	{
 		return toString("");
 	}
 
 	@Override
 	public String toString(final String prefix)
 	{
 		String str = getClass().getSimpleName() + " - " + aComputedBounds + " with minimum " + getMinimumSize() + " ("
 				+ aDirection.toString().toLowerCase() + ")";
 		if (aControls.isEmpty()) {
 			return str;
 		}
 		str += " {\n";
 		for (final Resizeable c : aControls) {
 			str += prefix + "\tSpring " + aSprings.get(c) + ": " + c.toString(prefix + "\t") + "\n";
 		}
 		return str + prefix + "}";
 	}
 }
