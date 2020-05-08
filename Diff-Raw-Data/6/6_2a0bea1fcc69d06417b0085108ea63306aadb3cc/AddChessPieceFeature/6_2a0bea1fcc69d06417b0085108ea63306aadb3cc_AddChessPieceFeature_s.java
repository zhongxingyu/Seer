 /**
  * <copyright>
  * 
  * Copyright (c) 2011, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  * 
  * </copyright>
  */
 package org.eclipse.graphiti.examples.chess.features;
 
 import org.eclipse.graphiti.examples.mm.chess.Colors;
 import org.eclipse.graphiti.examples.mm.chess.Piece;
 import org.eclipse.graphiti.examples.mm.chess.Types;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
 import org.eclipse.graphiti.mm.algorithms.Polygon;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.ICreateService;
import org.eclipse.graphiti.services.IGaLayoutService;
 import org.eclipse.graphiti.util.IColorConstant;
 
 public class AddChessPieceFeature extends AbstractAddShapeFeature implements IAddFeature {
 
	private static final int SQUARE_SIZE = 50;

 	public AddChessPieceFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public boolean canAdd(IAddContext context) {
 		if (context.getNewObject() instanceof Piece) {
 			return context.getTargetContainer().getChildren().size() < 64;
 		}
 		return false;
 	}
 
 	@Override
 	public PictogramElement add(IAddContext context) {
 		// Get the Graphiti services
 		ICreateService createService = Graphiti.getCreateService();
		IGaLayoutService layoutService = Graphiti.getGaLayoutService();
 
 		// Get the piece to add
 		Piece piece = (Piece) context.getNewObject();
 
 		// Get the image
 		int[] points = null;
 		if (Types.BISHOP.equals(piece.getType())) {
 			points = new int[] { 10, 45, 10, 40, 15, 30, 20, 25, 15, 20, 15, 15, 20, 10, 20, 5, 30, 5, 30, 10, 35, 15,
 					35, 20, 30, 25, 35, 30, 40, 40, 40, 45 };
 		} else if (Types.KING.equals(piece.getType())) {
 			points = new int[] { 10, 45, 15, 40, 10, 35, 5, 25, 5, 5, 10, 15, 15, 5, 20, 15, 25, 5, 30, 15, 35, 5, 40,
 					15, 45, 5, 45, 25, 40, 35, 35, 40, 40, 45 };
 		} else if (Types.KNIGHT.equals(piece.getType())) {
 			points = new int[] { 20, 45, 25, 35, 20, 25, 10, 35, 5, 30, 5, 25, 10, 15, 15, 10, 15, 5, 20, 10, 25, 5,
 					25, 10, 35, 15, 40, 20, 45, 30, 45, 45 };
 		} else if (Types.PAWN.equals(piece.getType())) {
 			points = new int[] { 15, 45, 15, 40, 20, 30, 15, 25, 20, 20, 20, 15, 25, 10, 30, 15, 30, 20, 35, 25, 30,
 					30, 35, 40, 35, 45 };
 		} else if (Types.QUEEN.equals(piece.getType())) {
 			points = new int[] { 10, 45, 15, 40, 10, 35, 5, 25, 5, 15, 10, 10, 15, 10, 20, 15, 25, 5, 30, 15, 35, 10,
 					40, 10, 45, 15, 45, 25, 40, 35, 35, 40, 40, 45 };
 		} else if (Types.ROOK.equals(piece.getType())) {
 			points = new int[] { 10, 45, 10, 40, 15, 30, 15, 15, 10, 10, 10, 5, 15, 5, 15, 10, 20, 10, 20, 5, 30, 5,
 					30, 10, 35, 10, 35, 5, 40, 5, 40, 10, 35, 15, 35, 30, 40, 40, 40, 45 };
 		}
 
 		// Create the visualization of the board as a square
 		Shape pieceShape = createService.createShape(context.getTargetContainer(), true);
 		Polygon polygon = createService.createPolygon(pieceShape, points);
 
 		// Set the line color; it needs to be the opposite color of the square
 		if (Colors.LIGHT.equals(piece.getSquare().getColor())) {
 			polygon.setForeground(manageColor(IColorConstant.BLACK));
 		} else {
 			polygon.setForeground(manageColor(IColorConstant.WHITE));
 		}
 		polygon.setLineWidth(2);
 
 		// Set the fill color; it needs to be the color of the owner of the
 		// piece
 		if (Colors.LIGHT.equals(piece.getOwner())) {
 			polygon.setBackground(manageColor(IColorConstant.WHITE));
 		} else {
 			polygon.setBackground(manageColor(IColorConstant.BLACK));
 		}
 
 		// Link the visualization with the board
 		link(pieceShape, piece);
 
 		return pieceShape;
 	}
 }
