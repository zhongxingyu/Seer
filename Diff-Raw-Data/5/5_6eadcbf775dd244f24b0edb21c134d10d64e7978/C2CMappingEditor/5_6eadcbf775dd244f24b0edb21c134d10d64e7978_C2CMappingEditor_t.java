 
 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 
 package cytoscape.visual.ui.editors.continuous;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.visual.VisualPropertyType;
 
 import cytoscape.visual.mappings.BoundaryRangeValues;
 import cytoscape.visual.mappings.continuous.ContinuousMappingPoint;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 
 import java.beans.PropertyChangeEvent;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 
 
 /**
  * Continuous-Continuous mapping editor.<br>
  *
  * <p>
  *     This is a editor for continuous values, i.e., numbers.
  * </p>
  *
  * @version 0.7
  * @since Cytoscape 2.5
  * @author Keiichiro Ono
  *
   */
 public class C2CMappingEditor extends ContinuousMappingEditorPanel {
 	/**
 	 * Creates a new C2CMappingEditor object.
 	 *
 	 * @param type DOCUMENT ME!
 	 */
 	public C2CMappingEditor(VisualPropertyType type) {
 		super(type);
 		abovePanel.setVisible(false);
 		belowPanel.setVisible(false);
 		pack();
 		setSlider();
 		((ContinuousTrackRenderer) slider.getTrackRenderer()).addPropertyChangeListener(this);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param width DOCUMENT ME!
 	 * @param height DOCUMENT ME!
 	 * @param title DOCUMENT ME!
 	 * @param type DOCUMENT ME!
 	 */
 	public static Object showDialog(final int width, final int height, final String title,
 	                                VisualPropertyType type) {
 		editor = new C2CMappingEditor(type);
 		editor.setSize(new Dimension(width, height));
 		editor.setTitle(title);
 		editor.setAlwaysOnTop(true);
 		editor.setLocationRelativeTo(Cytoscape.getDesktop());
 		editor.setVisible(true);
 
 		return editor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static ImageIcon getIcon(final int iconWidth, final int iconHeight,
 	                                VisualPropertyType type) {
 		editor = new C2CMappingEditor(type);
 
 		ContinuousTrackRenderer rend = (ContinuousTrackRenderer) editor.slider
 		                                                                                                          .getTrackRenderer();
 		rend.getRendererComponent(editor.slider);
 
 		return rend.getTrackGraphicIcon(iconWidth, iconHeight);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param width DOCUMENT ME!
 	 * @param height DOCUMENT ME!
 	 * @param type DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static ImageIcon getLegend(final int width, final int height,
 	                                  final VisualPropertyType type) {
 		editor = new C2CMappingEditor(type);
 
 		final ContinuousTrackRenderer rend = (ContinuousTrackRenderer) editor.slider
 		                                                                                                                              .getTrackRenderer();
 		rend.getRendererComponent(editor.slider);
 
 		return rend.getLegend(width, height);
 	}
 
 	@Override
 	protected void addButtonActionPerformed(ActionEvent evt) {
 		BoundaryRangeValues newRange;
 
 		if (mapping.getPointCount() == 0) {
 			slider.getModel().addThumb(50f, 5f);
 
 			newRange = new BoundaryRangeValues(below, 5f, above);
 			mapping.addPoint(maxValue / 2, newRange);
 			Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 
 			slider.repaint();
 			repaint();
 
 			return;
 		}
 
 		// Add a new white thumb in the min.
 		slider.getModel().addThumb(100f, 5f);
 
 		// Update continuous mapping
 		final Double newVal = maxValue;
 
 		// Pick Up first point.
 		final ContinuousMappingPoint previousPoint = mapping.getPoint(mapping.getPointCount() - 1);
 
 		final BoundaryRangeValues previousRange = previousPoint.getRange();
 		newRange = new BoundaryRangeValues(previousRange);
 
 		newRange.lesserValue = slider.getModel().getSortedThumbs()
 		                             .get(slider.getModel().getThumbCount() - 1);
 		System.out.println("EQ color = " + newRange.lesserValue);
 		newRange.equalValue = 5f;
 		newRange.greaterValue = previousRange.greaterValue;
 		mapping.addPoint(maxValue, newRange);
 
 		updateMap();
 
 		Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 
 		slider.repaint();
 		repaint();
 	}
 
 	@Override
 	protected void deleteButtonActionPerformed(ActionEvent evt) {
 		final int selectedIndex = slider.getSelectedIndex();
 
 		if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() > 1)) {
 			slider.getModel().removeThumb(selectedIndex);
 			mapping.removePoint(selectedIndex);
 
 			updateMap();
 			((ContinuousTrackRenderer) slider.getTrackRenderer()).removeSquare(selectedIndex);
 
 			mapping.fireStateChanged();
 
 			Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 			repaint();
 		}
 	}
 
 	private void setSlider() {
 		Dimension dim = new Dimension(600, 100);
 		setPreferredSize(dim);
 		setSize(dim);
 		setMinimumSize(new Dimension(300, 80));
 		slider.updateUI();
 
 		double actualRange = Math.abs(minValue - maxValue);
 
 		BoundaryRangeValues bound;
 		Float fraction;
 
 
		if(allPoints == null) {
			return;
		}
 		for (ContinuousMappingPoint point : allPoints) {
 			bound = point.getRange();
 
 			fraction = ((Number) ((point.getValue() - minValue) / actualRange)).floatValue() * 100;
 			slider.getModel().addThumb(fraction, ((Number) bound.equalValue).floatValue());
 		}
 
 		if (allPoints.size() != 0) {
 			below = (Number) allPoints.get(0).getRange().lesserValue;
 			above = (Number) allPoints.get(allPoints.size() - 1).getRange().greaterValue;
 		} else {
 			below = 30f;
 			above = 30f;
 		}
 
 		/*
 		 * get min and max for the value object
 		 */
 		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer(slider);
 
 		ContinuousTrackRenderer cRend = new ContinuousTrackRenderer(type, minValue, maxValue,
 		                                                            (Number) below, (Number) above);
 
 		slider.setThumbRenderer(thumbRend);
 		slider.setTrackRenderer(cRend);
 		slider.addMouseListener(new ThumbMouseListener());
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param evt DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (evt.getPropertyName().equals(ContinuousMappingEditorPanel.BELOW_VALUE_CHANGED)) {
 			below = evt.getNewValue();
 		} else if (evt.getPropertyName().equals(ContinuousMappingEditorPanel.ABOVE_VALUE_CHANGED)) {
 			above = evt.getNewValue();
 		}
 	}
 }
