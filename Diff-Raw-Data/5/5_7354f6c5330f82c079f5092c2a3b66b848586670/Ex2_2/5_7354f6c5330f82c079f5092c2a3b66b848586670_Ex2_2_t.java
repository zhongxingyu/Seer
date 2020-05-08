 import java.awt.BorderLayout;
 import java.awt.Checkbox;
 import java.awt.CheckboxGroup;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Label;
 import java.awt.Panel;
 import java.awt.Rectangle;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashSet;
 import java.util.Set;
 
 import jv.geom.PgElementSet;
 import jv.geom.PgPolygonSet;
 import jv.object.PsMainFrame;
 import jv.project.PgGeometryIf;
 import jv.project.PvCameraEvent;
 import jv.project.PvCameraListenerIf;
 import jv.project.PvGeometryListenerIf;
 import jv.project.PvLightIf;
 import jv.vecmath.PdVector;
 import jv.vecmath.PiVector;
 import jv.viewer.PvDisplay;
 import jv.viewer.PvViewer;
 
 /**
  * Solution to second exercise of second project
  * 
  * @author		Milian Wolff
  * @version		24.11.2011, 1.00 created
  */
 public class Ex2_2 implements PvGeometryListenerIf, PvCameraListenerIf, ItemListener {
 	public static void main(String[] args)
 	{
 		new Ex2_2(args);
 	}
 
 	private PsMainFrame m_frame;
 	private PvDisplay m_disp;
 	private PgPolygonSet m_silhouette;
 	private Checkbox m_vertexSilhouette;
 	private Checkbox m_faceSilhouette;
 	private Checkbox m_disableSilhouette;
 	private enum SilhouetteType {
 		Disabled,
 		FaceBased,
 		VertexBased
 	}
 	private SilhouetteType m_silhouetteType;
 	public Ex2_2(String args[])
 	{
 		// Create toplevel window of application containing the applet
 		m_frame	= new PsMainFrame("SciVis - Project 2 - Exercise 1 - Milian Wolff", args);
 
 		// Create viewer for viewing 3d geometries, and register m_frame.
 		PvViewer viewer = new PvViewer(null, m_frame);
 
 		// Get default display from viewer
 		m_disp = (PvDisplay) viewer.getDisplay();
 		m_disp.setEnabledZBuffer(true);
 		m_disp.setEnabledAntiAlias(true);
 		// listener
 		m_disp.addGeometryListener(this);
 		m_disp.addCameraListener(this);
 
 		// Add display to m_frame
 		m_frame.add((Component)m_disp, BorderLayout.CENTER);
 
 		// buttons
 		Panel buttons = new Panel();
 		buttons.setLayout(new GridBagLayout());
 		m_frame.add(buttons, BorderLayout.EAST);
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridwidth = 1;
 		c.gridx = 0;
 		c.gridy = 0;
 
 		c.gridy++;
 		c.fill = GridBagConstraints.CENTER;
 		buttons.add(new Label("Silhouette"), c);
 		// silhouette method choice
 		CheckboxGroup group = new CheckboxGroup();
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		// disable silhouette
 		m_disableSilhouette = new Checkbox("Disabled", group, false);
 		m_disableSilhouette.addItemListener(this);
 		c.gridy++;
 		buttons.add(m_disableSilhouette, c);
 
 		// face based silhouette (2.a)
 		m_faceSilhouette = new Checkbox("Face Based", group, false);
 		m_faceSilhouette.addItemListener(this);
 		c.gridy++;
 		buttons.add(m_faceSilhouette, c);
 
 		// default colors
 		m_vertexSilhouette = new Checkbox("Vertex Based", group, true);
 		m_vertexSilhouette.addItemListener(this);
 		c.gridy++;
 		buttons.add(m_vertexSilhouette, c);
 
 		m_silhouetteType = SilhouetteType.FaceBased;
 		group.setSelectedCheckbox(m_faceSilhouette);
 
 		m_frame.pack();
 		// Position of left upper corner and size of m_frame when run as application.
 		m_frame.setBounds(new Rectangle(420, 5, 640, 550));
 		m_frame.setVisible(true);
 	}
 	//BEGIN: PvGeometryListenerIf
 	@Override
 	public void addGeometry(PgGeometryIf geometry)
 	{
 		// do nothing
 	}
 	@Override
 	public void removeGeometry(PgGeometryIf geometry)
 	{
 		// do nothing
 	}
 	@Override
 	public void selectGeometry(PgGeometryIf geometry)
 	{
 		assert m_disp.getSelectedGeometry() == geometry;
 		viewUpdated();
 	}
 	@Override
 	public String getName()
 	{
 		return "Ex2_2";
 	}
 	//END PvGeometryListenerIf
 	//BEGIN PvCameraListenerIf
 	@Override
 	public void dragCamera(PvCameraEvent arg0)
 	{
 		viewUpdated();
 	}
 	@Override
 	public void pickCamera(PvCameraEvent arg0)
 	{
 		viewUpdated();
 	}
 	//END PvCameraListenerIf
 	//BEGIN ItemListener
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		Object source = e.getSource();
 		if (source == m_disableSilhouette) {
 			m_silhouetteType = SilhouetteType.Disabled;
 		} else if (source == m_faceSilhouette) {
 			m_silhouetteType = SilhouetteType.FaceBased;
 		} else if (source == m_vertexSilhouette) {
 			m_silhouetteType = SilhouetteType.VertexBased;
 		} else {
 			assert false;
 		}
 		viewUpdated();
 	}
 	//END ItemListener
 	private void viewUpdated()
 	{
 		if (m_disp.getSelectedGeometry() == m_silhouette) {
 			return;
 		}
 		PgElementSet geometry = (PgElementSet) m_disp.getSelectedGeometry();
 		if (geometry == null) {
 			return;
 		}
 
 		// clear last silhouette if needed
 		clearSilhouette();
 
 		switch(m_silhouetteType) {
 		case Disabled:
 			// nothing to do, since we always clear
 			break;
 		case FaceBased:
 			m_silhouette = createFaceBasedSilhouette(geometry);
 			break;
 		case VertexBased:
 			m_silhouette = createVertexBasedSilhouette(geometry);
 			break;
 		}
 
 		if (m_silhouette != null) {
 			assert m_silhouetteType == SilhouetteType.FaceBased ||
 					m_silhouetteType == SilhouetteType.VertexBased;
 			System.out.println("adding: " + m_silhouette.getName());
 			m_silhouette.showVertices(false);
 			m_silhouette.showEdgeColorFromVertices(true);
 			m_silhouette.setGlobalPolygonColor(Color.black);
 
 			m_disp.addGeometry(m_silhouette);
 			m_disp.update(m_silhouette);
 
 			// disable lightning to get completely white surface
 			m_disp.setLightingModel(PvLightIf.MODEL_SURFACE);
 			// 3D look is nicer imo
 			m_disp.setEnabled3DLook(true);
 
 			geometry.setGlobalElementColor(Color.white);
 			m_disp.update(geometry);
 		} else {
 			assert m_silhouetteType == SilhouetteType.Disabled;
 			// restore settings
 			geometry.setGlobalElementColor(Color.cyan);
 			m_disp.update(geometry);
 			m_disp.setLightingModel(PvLightIf.MODEL_LIGHT);
 			m_disp.setEnabled3DLook(false);
 		}
 	}
 	private PgPolygonSet createFaceBasedSilhouette(PgElementSet geometry)
 	{
 		PgPolygonSet silhouette = new PgPolygonSet();
		silhouette.setName("Face Based Silhouette of " + geometry.getName());
 
 		// find visible faces
 		PdVector ray = m_disp.getCamera().getViewDir();
 		Set<Integer> visibleFaces = new HashSet<Integer>();
 		geometry.assureElementNormals();
 		assert geometry.hasElementNormals();
 		for(int i = 0; i < geometry.getNumElements(); ++i) {
 			double dot = ray.dot(geometry.getElementNormal(i));
 			// if the dot product is zero, the face is either visible or hidden :-/
 			// we ignore this case, assuming that it only happens for faces somewhere
 			// in the middle of the visible surface, hence they do not play a role
 			// in finding the silhouette anyways.
 			// so we are only interested in the faces with _negative_ dot product
 			// as those are visible to us (we are looking along the direction of ray)
 			if (dot < 0) {
 				visibleFaces.add(i);
 			}
 		}
 		// find visible edges by iterating over the corner table
 		CornerTable table = new CornerTable(geometry);
 		for(Corner corner : table.corners()) {
 			// an edge is part of the silhouette if 
 			// a) it is part of a visible face
 			// b) either it has adjacent face
 			// c) or its adjacent face is not visible
 			if (visibleFaces.contains(corner.triangle)
 				&& (corner.opposite == null || !visibleFaces.contains(corner.opposite.triangle)))
 			{
 				int a = silhouette.addVertex(geometry.getVertex(corner.next.vertex));
 				int b = silhouette.addVertex(geometry.getVertex(corner.prev.vertex));
 				silhouette.addPolygon(new PiVector(a, b));
 			}
 		}
 		return silhouette;
 	}
 	/**
 	 * interpolate linearly between p1 with visibility a and p2 with visibility b
 	 *
 	 * @returns zero level
 	 */
 	private PdVector findZeroLevel(PdVector p1, double a, PdVector p2, double b)
 	{
 		if (b == 0) {
 			return (PdVector) p2.clone();
 		} else if (a == 0) {
 			return (PdVector) p1.clone();
 		}
 		// edge points from p1 to p2
 		// hence our "x" axis starts at p1
 		PdVector edge = PdVector.subNew(p2, p1);
 		double x0 = a / (a - b);
 
 		assert x0 >= -1 && x0 <= 1;
 		assert (b-a) * x0 + a <= 1E-10;
 		edge.multScalar(x0);
 		return PdVector.addNew(p1, edge);
 	}
 	private PgPolygonSet createVertexBasedSilhouette(PgElementSet geometry)
 	{
 		PgPolygonSet silhouette = new PgPolygonSet();
		silhouette.setName("Vertex Based Silhouette of " + geometry.getName());
 
 		// find visible faces by linear interpolation of dot product of vertices
 		// we iterate over all edges, if the dot product flips sign between
 		// corner base vertex and next and prev vertex, we draw the zero level set
 		// to find it we interpolate the dot products (cmp. barycentric coordinates)
 		PdVector ray = m_disp.getCamera().getViewDir();
 		CornerTable table = new CornerTable(geometry);
 		for(Corner corner : table.corners()) {
 			// TODO: optimize: only compute visibility (i.e. dot product) once for each vertex
 			// but see whether this is actually noticeably faster
 			double a = ray.dot(geometry.getVertexNormal(corner.vertex));
 			double b = ray.dot(geometry.getVertexNormal(corner.next.vertex));
 			double c = ray.dot(geometry.getVertexNormal(corner.prev.vertex));
 			// we look for faces with one visible and two hidden vertices
 			// or vice versa, i.e. two invisible and one visible vertex
 			// via the corner table we look for the corner that is the
 			// single visible or hidden vertex
 			if ((a <= 0 && b >= 0 && c >= 0) || (a >= 0 && b <= 0 && c <= 0)) {
 				// a is our single vertex, find the zero level set via interpolation
 				int v1 = silhouette.addVertex(
 						findZeroLevel(geometry.getVertex(corner.vertex), a,
 									  geometry.getVertex(corner.next.vertex), b)
 				);
 				int v2 = silhouette.addVertex(
 						findZeroLevel(geometry.getVertex(corner.vertex), a,
 									  geometry.getVertex(corner.prev.vertex), c)
 				);
 				silhouette.addPolygon(new PiVector(v1, v2));
 				continue;
 			}
 		}
 		return silhouette;
 	}
 	private void clearSilhouette()
 	{
 		if (m_silhouette != null) {
 			m_disp.removeGeometry(m_silhouette);
 			m_disp.update(m_silhouette);
 			m_silhouette = null;
 		}
 	}
 }
