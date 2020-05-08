 package org.muis.core;
 
 import java.util.Iterator;
 
 /**
  * Represents a capture of an element structure at a point in time. X and Y attributes are available if this is used for elements under a
  * screen point
  */
 public class MuisElementCapture implements prisms.util.Sealable, Iterable<MuisElementCapture>
 {
 	/** This capture element's parent in the hierarchy */
 	public final MuisElementCapture parent;
 
 	/** The element that this capture represents */
 	public final MuisElement element;
 
 	/** The x-coordinate of the screen point relative to this element */
 	public final int x;
 
 	/** The y-coordinate of the screen point relative to this element */
 	public final int y;
 
 	private MuisElementCapture [] theChildren;
 
 	private boolean isSealed;
 
 	/**
 	 * @param aParent This capture element's parent in the hierarchy
 	 * @param el The element that this capture represents
 	 * @param anX The x-coordinate of the screen point relative to this element
 	 * @param aY The y-coordinate of the screen point relative to this element
 	 */
 	public MuisElementCapture(MuisElementCapture aParent, MuisElement el, int anX, int aY)
 	{
 		parent = aParent;
 		element = el;
 		x = anX;
 		y = aY;
 		theChildren = new MuisElementCapture[0];
 	}
 
 	/**
 	 * @param child The child to add to this capture
 	 * @throws SealedException If this capture has been sealed
 	 */
 	public void addChild(MuisElementCapture child) throws SealedException
 	{
 		if(isSealed)
 			throw new SealedException(this);
 		theChildren = prisms.util.ArrayUtils.add(theChildren, child);
 	}
 
 	/** @return The number of children this capture has--that is, the number of children this capture's element had at the moment of capture */
 	public int getChildCount()
 	{
 		return theChildren.length;
 	}
 
 	/**
 	 * @param index The index of the child to get
 	 * @return This capture's child at the given index
 	 */
 	public MuisElementCapture getChild(int index)
 	{
 		return theChildren[index];
 	}
 
 	/** @return An iterator over this capture's immediate children */
 	public Iterable<MuisElementCapture> getChildren()
 	{
 		return prisms.util.ArrayUtils.iterable(theChildren);
 	}
 
 	/** @return An iterator of each end point (leaf node) in this hierarchy */
 	public Iterable<MuisElementCapture> getTargets()
 	{
 		if(theChildren.length == 0)
 			return new Iterable<MuisElementCapture>() {
 				@Override
 				public Iterator<MuisElementCapture> iterator()
 				{
 					return new SelfIterator();
 				}
 			};
 		else
 			return new Iterable<MuisElementCapture>() {
 				@Override
 				public Iterator<MuisElementCapture> iterator()
 				{
 					return new MuisCaptureIterator(false, true);
 				}
 			};
 	}
 
 	/** Performs a depth-first iteration of this capture structure */
 	@Override
 	public Iterator<MuisElementCapture> iterator()
 	{
 		return new MuisCaptureIterator(true, true);
 	}
 
 	/**
 	 * @param depthFirst Whether to iterate depth-first or breadth-first
 	 * @return An iterable to iterate over every element in this hierarchy
 	 */
 	public Iterable<MuisElementCapture> iterate(final boolean depthFirst)
 	{
 		return new Iterable<MuisElementCapture>() {
 			@Override
 			public Iterator<MuisElementCapture> iterator()
 			{
 				return new MuisCaptureIterator(true, depthFirst);
 			}
 		};
 	}
 
 	/**
 	 * @param el The element to search for
 	 * @return The capture of the given element in this hierarhcy, or null if the given element was not located in this capture
 	 */
 	public MuisElementCapture find(MuisElement el)
 	{
 		MuisElement [] path = MuisUtils.path(el);
 		MuisElementCapture ret = this;
 		int pathIdx;
 		for(pathIdx = 1; pathIdx < path.length; pathIdx++)
 		{
 			boolean found = false;
 			for(MuisElementCapture child : ret.theChildren)
 				if(child.element == path[pathIdx])
 				{
 					found = true;
 					ret = child;
 					break;
 				}
 			if(!found)
 				return null;
 		}
 		return ret;
 	}
 
 	/** @return The last end point (leaf node) in this hierarchy */
 	public MuisElementCapture getTarget()
 	{
 		if(theChildren.length == 0)
 			return this;
 		return theChildren[theChildren.length - 1].getTarget();
 	}
 
 	@Override
 	public boolean isSealed()
 	{
 		return isSealed;
 	}
 
 	@Override
 	public void seal()
 	{
 		isSealed = true;
 	}
 
 	private class SelfIterator implements Iterator<MuisElementCapture>
 	{
 		private boolean hasReturned;
 
 		SelfIterator()
 		{
 		}
 
 		@Override
 		public boolean hasNext()
 		{
 			return !hasReturned;
 		}
 
 		@Override
 		public MuisElementCapture next()
 		{
 			return MuisElementCapture.this;
 		}
 
 		@Override
 		public void remove()
 		{
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	private class MuisCaptureIterator implements Iterator<MuisElementCapture>
 	{
 		private int theIndex;
 
 		private Iterator<MuisElementCapture> theChildIter;
 
 		private boolean isReturningSelf;
 
 		private boolean isDepthFirst;
 
 		private boolean hasReturnedSelf;
 
 		MuisCaptureIterator(boolean returnSelf, boolean depthFirst)
 		{
 			isReturningSelf = returnSelf;
 			isDepthFirst = depthFirst;
 		}
 
 		@Override
 		public boolean hasNext()
 		{
 			if(isReturningSelf && !isDepthFirst && !hasReturnedSelf)
 				return true;
 			while(theIndex < getChildCount())
 			{
 				if(theChildIter == null)
 					theChildIter = getChild(theIndex).getTargets().iterator();
 				if(theChildIter.hasNext())
 					return true;
 				else
 					theChildIter = null;
 				theIndex++;
 			}
 			return isReturningSelf && !hasReturnedSelf;
 		}
 
 		@Override
 		public MuisElementCapture next()
 		{
 			if(isReturningSelf && !isDepthFirst && !hasReturnedSelf)
 			{
 				hasReturnedSelf = true;
 				return MuisElementCapture.this;
 			}
 			if(theChildIter != null)
 				return theChildIter.next();
 			if(isReturningSelf && isDepthFirst && !hasReturnedSelf)
 			{
 				hasReturnedSelf = true;
 				return MuisElementCapture.this;
 			}
 			return null;
 		}
 
 		@Override
 		public void remove()
 		{
 			throw new UnsupportedOperationException();
 		}
 	}
 }
