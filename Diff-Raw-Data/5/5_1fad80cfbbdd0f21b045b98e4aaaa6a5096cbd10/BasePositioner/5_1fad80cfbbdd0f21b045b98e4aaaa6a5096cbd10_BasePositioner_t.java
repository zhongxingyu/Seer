 package org.andengine.limbo.utils.positioner;
 
 import org.andengine.entity.IEntity;
 import org.andengine.util.adt.transformation.Transformation;
 import org.andengine.util.math.MathConstants;
 import org.andengine.util.math.MathUtils;
 /**
  * (c) 2013 Michal Stawinski (nazgee)
  *
  * @author Michal Stawinski
  * @since 20:35:05 - 08.05.2013
  */
 public abstract class BasePositioner {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 	abstract public void place(IEntity pImmovable, IEntity pMovable, AnchorPointsPair pAnchorsPair,
 			final float tX, final float tY, boolean pMoveVertically, boolean pMoveHorizontally);
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	/**
 	 * This method will attempt to extract rotation from a given transformation.
 	 * Unfortunately it works fine only if scaling is uniform (sX == sY) and no skew is applied.
 	 * @param transformation
 	 * @param pReuse
 	 * @return
 	 */
 	protected static float calculateRotation(Transformation transformation, final float[] pReuse) {
 		// calculate translation
 		pReuse[0] = 0;
 		pReuse[1] = 0;
 		transformation.transform(pReuse);
 		final float tX = pReuse[0];
 		final float tY = pReuse[1];
 
 		// calculate rotation
 		pReuse[0] = 1;
 		pReuse[1] = 0;
 		transformation.transform(pReuse);
 		final float rotX = pReuse[0] - tX;
 		final float rotY = pReuse[1] - tY;
 		final float rotation = MathConstants.RAD_TO_DEG * MathUtils.atan2(rotY, rotX);
 
 		return rotation;
 	}
 
 	public void placeTopOfAndCenter(IEntity pImmovable, IEntity pMovable) {
 		placeTopOfAndCenter(pImmovable, pMovable, 0, 0);
 	}
 	public void placeTopOfAndCenter(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.VER_TOP_C.relation, tx, ty);
 	}
 
 	public void placeTopOfLeftsAligned(IEntity pImmovable, IEntity pMovable) {
 		placeTopOfLeftsAligned(pImmovable, pMovable, 0, 0);
 	}
 	public void placeTopOfLeftsAligned(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.VER_TOP_L.relation, tx, ty);
 	}
 
 	public void placeTopOfRightsAligned(IEntity pImmovable, IEntity pMovable) {
 		placeTopOfRightsAligned(pImmovable, pMovable, 0, 0);
 	}
 	public void placeTopOfRightsAligned(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.VER_TOP_R.relation, tx, ty);
 	}
 
 	public void placeBelowOfAndCenter(IEntity pImmovable, IEntity pMovable) {
 		placeBelowOfAndCenter(pImmovable, pMovable, 0, 0);
 	}
 	public void placeBelowOfAndCenter(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.VER_BTM_C.relation, tx, ty);
 	}
 
 	public void placeBelowOfLeftsAligned(IEntity pImmovable, IEntity pMovable) {
 		placeBelowOfLeftsAligned(pImmovable, pMovable, 0, 0);
 	}
 	public void placeBelowOfLeftsAligned(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.VER_BTM_L.relation, tx, ty);
 	}
 
 	public void placeBelowOfRightsAligned(IEntity pImmovable, IEntity pMovable) {
 		placeBelowOfRightsAligned(pImmovable, pMovable, 0, 0);
 	}
 	public void placeBelowOfRightsAligned(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.VER_BTM_R.relation, tx, ty);
 	}
 
 	public void placeLeftOfAndCenter(IEntity pImmovable, IEntity pMovable) {
 		placeLeftOfAndCenter(pImmovable, pMovable, 0, 0);
 	}
 	public void placeLeftOfAndCenter(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.HOR_L_CNT.relation, tx, ty);
 	}
 
 	public void placeLeftOfTopsAligned(IEntity pImmovable, IEntity pMovable) {
 		placeLeftOfTopsAligned(pImmovable, pMovable, 0, 0);
 	}
 	public void placeLeftOfTopsAligned(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.HOR_L_TOP.relation, tx, ty);
 	}
 
 	public void placeLeftOfBottomsAligned(IEntity pImmovable, IEntity pMovable) {
 		placeLeftOfBottomsAligned(pImmovable, pMovable, 0, 0);
 	}
 	public void placeLeftOfBottomsAligned(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.HOR_L_BTM.relation, tx, ty);
 	}
 
 	public void placeRightOfAndCenter(IEntity pImmovable, IEntity pMovable) {
 		placeRightOfAndCenter(pImmovable, pMovable, 0, 0);
 	}
 	public void placeRightOfAndCenter(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.HOR_R_CNT.relation, tx, ty);
 	}
 
 	public void placeRightOfTopsAligned(IEntity pImmovable, IEntity pMovable) {
 		placeRightOfTopsAligned(pImmovable, pMovable, 0, 0);
 	}
 	public void placeRightOfTopsAligned(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.HOR_R_TOP.relation, tx, ty);
 	}
 
 	public void placeRightOfBottomsAligned(IEntity pImmovable, IEntity pMovable) {
 		placeRightOfBottomsAligned(pImmovable, pMovable, 0, 0);
 	}
 	public void placeRightOfBottomsAligned(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.HOR_R_BTM.relation, tx, ty);
 	}
 
 	public void centerOnHorizontalAxes(IEntity pImmovable, IEntity pMovable) {
 		centerOnHorizontalAxes(pImmovable, pMovable, 0, 0);
 	}
 	public void centerOnHorizontalAxes(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
		placeHorizontal(pImmovable, pMovable, eAnchorPointsPair.IN_C_CNT.relation, tx, ty);
 	}
 
 	public void centerOnVerticalAxes(IEntity pImmovable, IEntity pMovable) {
 		centerOnVerticalAxes(pImmovable, pMovable, 0, 0);
 	}
 	public void centerOnVerticalAxes(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
		placeVertical(pImmovable, pMovable, eAnchorPointsPair.IN_C_CNT.relation, tx, ty);
 	}
 
 	public void center(IEntity pImmovable, IEntity pMovable) {
 		center(pImmovable, pMovable, 0, 0);
 	}
 	public void center(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.IN_C_CNT.relation, tx, ty);
 	}
 
 	public void alignLeftEdges(IEntity pImmovable, IEntity pMovable) {
 		alignLeftEdges(pImmovable, pMovable, 0, 0);
 	}
 	public void alignLeftEdges(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		placeHorizontal(pImmovable, pMovable, eAnchorPointsPair.IN_L_CNT.relation, tx, ty);
 	}
 
 	public void alignLeftEdgesAndCenter(IEntity pImmovable, IEntity pMovable) {
 		alignLeftEdgesAndCenter(pImmovable, pMovable, 0, 0);
 	}
 	public void alignLeftEdgesAndCenter(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.IN_L_CNT.relation, tx, ty);
 	}
 
 	public void alignRightEdges(IEntity pImmovable, IEntity pMovable) {
 		alignRightEdges(pImmovable, pMovable, 0, 0);
 	}
 	public void alignRightEdges(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		placeHorizontal(pImmovable, pMovable, eAnchorPointsPair.IN_R_CNT.relation, tx, ty);
 	}
 
 	public void alignRightEdgesAndCenter(IEntity pImmovable, IEntity pMovable) {
 		alignRightEdgesAndCenter(pImmovable, pMovable, 0, 0);
 	}
 	public void alignRightEdgesAndCenter(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.IN_R_CNT.relation, tx, ty);
 	}
 
 	public void alignTopEdges(IEntity pImmovable, IEntity pMovable) {
 		alignTopEdges(pImmovable, pMovable, 0, 0);
 	}
 	public void alignTopEdges(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		placeVertical(pImmovable, pMovable, eAnchorPointsPair.IN_C_TOP.relation, tx, ty);
 	}
 
 	public void alignTopEdgesAndCenter(IEntity pImmovable, IEntity pMovable) {
 		alignTopEdgesAndCenter(pImmovable, pMovable, 0, 0);
 	}
 	public void alignTopEdgesAndCenter(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.IN_C_TOP.relation, tx, ty);
 	}
 
 	public void alignTopRightEdges(IEntity pImmovable, IEntity pMovable) {
 		alignTopRightEdges(pImmovable, pMovable, 0, 0);
 	}
 	public void alignTopRightEdges(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.IN_R_TOP.relation, tx, ty);
 	}
 
 	public void alignTopLeftEdges(IEntity pImmovable, IEntity pMovable) {
 		alignTopLeftEdges(pImmovable, pMovable, 0, 0);
 	}
 	public void alignTopLeftEdges(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.IN_L_TOP.relation, tx, ty);
 	}
 
 	public void alignBottomEdges(IEntity pImmovable, IEntity pMovable) {
 		alignBottomEdges(pImmovable, pMovable, 0, 0);
 	}
 	public void alignBottomEdges(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		placeVertical(pImmovable, pMovable, eAnchorPointsPair.IN_C_BTM.relation, tx, ty);
 	}
 
 	public void alignBottomEdgesAndCenter(IEntity pImmovable, IEntity pMovable) {
 		alignBottomEdgesAndCenter(pImmovable, pMovable, 0, 0);
 	}
 	public void alignBottomEdgesAndCenter(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.IN_C_BTM.relation, tx, ty);
 	}
 
 	public void alignBottomRightEdges(IEntity pImmovable, IEntity pMovable) {
 		alignBottomRightEdges(pImmovable, pMovable, 0, 0);
 	}
 	public void alignBottomRightEdges(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.IN_R_BTM.relation, tx, ty);
 	}
 
 	public void alignBottomLeftEdges(IEntity pImmovable, IEntity pMovable) {
 		alignBottomLeftEdges(pImmovable, pMovable, 0, 0);
 	}
 	public void alignBottomLeftEdges(IEntity pImmovable, IEntity pMovable, final float tx, final float ty) {
 		place(pImmovable, pMovable, eAnchorPointsPair.IN_L_BTM.relation, tx, ty);
 	}
 
 	public void place(IEntity pImmovable, IEntity pMovable, AnchorPointsPair pAnchorsPair) {
 		place(pImmovable, pMovable, pAnchorsPair, 0, 0);
 	}
 
 	public void place(IEntity pImmovable, IEntity pMovable, AnchorPointsPair pAnchorsPair, final float tX, final float tY) {
 		place(pImmovable, pMovable, pAnchorsPair, tX, tY, true, true);
 	}
 
 	public void placeVertical(IEntity pImmovable, IEntity pMovable, AnchorPointsPair pAnchorsPair, final float tX, final float tY) {
 		place(pImmovable, pMovable, pAnchorsPair, tX, tY, true, false);
 	}
 
 	public void placeHorizontal(IEntity pImmovable, IEntity pMovable, AnchorPointsPair pAnchorsPair, final float tX, final float tY) {
 		place(pImmovable, pMovable, pAnchorsPair, tX, tY, false, true);
 	}
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 	public static class AnchorPoint {
 		public final float X;
 		public final float Y;
 
 		private AnchorPoint(final float pX, final float pY) {
 			this.X = pX;
 			this.Y = pY;
 		}
 	}
 
 	public static class AnchorPointsPair {
 		public final AnchorPoint anchorA;
 		public final AnchorPoint anchorB;
 
 		private AnchorPointsPair(final AnchorPoint pAnchorA, final AnchorPoint pAnchorB) {
 			this.anchorA = pAnchorA;
 			this.anchorB = pAnchorB;
 		}
 	}
 
 	public static enum eAnchorPoint {
 		L_TOP(0.0f,1.0f),C_TOP(0.5f,1.0f),R_TOP(1.0f,1.0f),
 		L_CNT(0.0f,0.5f),C_CNT(0.5f,0.5f),R_CNT(1.0f,0.5f),
 		L_BTM(0.0f,0.0f),C_BTM(0.5f,0.0f),R_BTM(1.0f,0.0f);
 
 		public final AnchorPoint anchor;
 
 		private eAnchorPoint(final float pX, final float pY) {
 			anchor = new AnchorPoint(pX, pY);
 		}
 	}
 
 	public static enum eAnchorPointsPair {
 		HOR_L_TOP(eAnchorPoint.L_TOP.anchor, eAnchorPoint.R_TOP.anchor),
 		HOR_L_CNT(eAnchorPoint.L_CNT.anchor, eAnchorPoint.R_CNT.anchor),
 		HOR_L_BTM(eAnchorPoint.L_BTM.anchor, eAnchorPoint.R_BTM.anchor),
 
 		HOR_R_TOP(eAnchorPoint.R_TOP.anchor, eAnchorPoint.L_TOP.anchor),
 		HOR_R_CNT(eAnchorPoint.R_CNT.anchor, eAnchorPoint.L_CNT.anchor),
 		HOR_R_BTM(eAnchorPoint.R_BTM.anchor, eAnchorPoint.L_BTM.anchor),
 
 		VER_TOP_L(eAnchorPoint.L_TOP.anchor, eAnchorPoint.L_BTM.anchor),
 		VER_TOP_C(eAnchorPoint.C_TOP.anchor, eAnchorPoint.C_BTM.anchor),
 		VER_TOP_R(eAnchorPoint.R_TOP.anchor, eAnchorPoint.R_BTM.anchor),
 
 		VER_BTM_L(eAnchorPoint.L_BTM.anchor, eAnchorPoint.L_TOP.anchor),
 		VER_BTM_C(eAnchorPoint.C_BTM.anchor, eAnchorPoint.C_TOP.anchor),
 		VER_BTM_R(eAnchorPoint.R_BTM.anchor, eAnchorPoint.R_TOP.anchor),
 
 		IN_L_TOP(eAnchorPoint.L_TOP.anchor),
 		IN_L_CNT(eAnchorPoint.L_CNT.anchor),
 		IN_L_BTM(eAnchorPoint.L_BTM.anchor),
 
 		IN_R_TOP(eAnchorPoint.R_TOP.anchor),
 		IN_R_CNT(eAnchorPoint.R_CNT.anchor),
 		IN_R_BTM(eAnchorPoint.R_BTM.anchor),
 
 		IN_C_TOP(eAnchorPoint.C_TOP.anchor),
 		IN_C_CNT(eAnchorPoint.C_CNT.anchor),
 		IN_C_BTM(eAnchorPoint.C_BTM.anchor);
 
 		public final AnchorPointsPair relation;
 
 		private eAnchorPointsPair(final AnchorPoint pAnchorA, final AnchorPoint pAnchorB) {
 			relation = new AnchorPointsPair(pAnchorA, pAnchorB);
 		}
 
 		private eAnchorPointsPair(final AnchorPoint pAnchor) {
 			relation = new AnchorPointsPair(pAnchor, pAnchor);
 		}
 	}
 }
