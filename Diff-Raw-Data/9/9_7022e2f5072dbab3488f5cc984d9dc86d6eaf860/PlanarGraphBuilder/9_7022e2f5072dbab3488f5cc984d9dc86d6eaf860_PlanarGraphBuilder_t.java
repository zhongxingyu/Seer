 /**
  * GDMS-Topology  is a library dedicated to graph analysis. It's based on the JGraphT available at
  * http://www.jgrapht.org/. It ables to compute and process large graphs using spatial
  * and alphanumeric indexes.
  * This version is developed at French IRSTV institut as part of the
  * EvalPDU project, funded by the French Agence Nationale de la Recherche
  * (ANR) under contract ANR-08-VILL-0005-01 and GEBD project
  * funded by the French Ministery of Ecology and Sustainable Development .
  *
  * GDMS-Topology  is distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
  * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
  *
  * GDMS-Topology is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult: <http://trac.orbisgis.org/>
  * or contact directly:
  * info_at_ orbisgis.org
  */
 package org.gdms.gdmstopology.process;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import org.gdms.data.DataSourceCreationException;
 
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.NoSuchTableException;
 import org.gdms.data.NonEditableDataSourceException;
 import org.gdms.data.indexes.IndexException;
 import org.gdms.data.schema.DefaultMetadata;
 import org.gdms.data.types.Constraint;
 import org.gdms.data.types.Type;
 import org.gdms.data.types.TypeFactory;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DriverException;
 import org.gdms.driver.driverManager.DriverLoadException;
 import org.orbisgis.progress.ProgressMonitor;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.IntersectionMatrix;
 import com.vividsolutions.jts.operation.linemerge.LineMerger;
 import com.vividsolutions.jts.operation.polygonize.Polygonizer;
 import java.io.File;
 import org.gdms.data.DataSource;
 import org.gdms.data.indexes.DefaultSpatialIndexQuery;
 import org.gdms.data.indexes.rtree.DiskRTree;
 import org.gdms.data.types.GeometryDimensionConstraint;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DiskBufferDriver;
 import org.gdms.gdmstopology.model.GraphSchema;
 
 public class PlanarGraphBuilder {
 
         GeometryFactory gf = new GeometryFactory();
         public Collection edges;
         private DataSourceFactory dsf;
         private ProgressMonitor pm;
         public final static Integer MINUS_ONE = new Integer(-1);
         private String ds_edges_name = ".edges";
         private String ds_nodes_name = ".nodes";
         private String ds_polygons_name = ".polygons";
         private String output_name;
 
         /**
          * This class is used to computed a planar graph where spatial entities are represented in 3 datasources
          * : points, lines and polygons.
          * A topological data structures is used to represent  topological spatial relationships between points, lines and polygons.
          * 
          * @param dsf
          * @param pm
          */
         public PlanarGraphBuilder(DataSourceFactory dsf, ProgressMonitor pm) {
                 this.pm = pm;
                 this.dsf = dsf;
         }
 
         public void setOutput_name(String output_name) {
                 this.output_name = output_name;
         }
 
         /**
          * Create the datasources that contains edges without self-intersection and
          * all nodes of the graph with a primary ID
          * @param sds
          * @throws DriverException, IOException
          */
         public void buildGraph(DataSet dataSet) throws DriverException, IOException {
                 pm.startTask("Create edges graph", 100);
 
                 DefaultMetadata edgeMedata = new DefaultMetadata(new Type[]{
                                 TypeFactory.createType(Type.GEOMETRY,
                                 new Constraint[]{new GeometryDimensionConstraint(1)}),
                                 TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT)}, new String[]{
                                 "the_geom", GraphSchema.ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.RIGHT_FACE, GraphSchema.LEFT_FACE});
                 int edgesFieldsCount = edgeMedata.getFieldCount();
                 // Get linear elements from all geometries in the layer
                 Collection<Geometry> geomColl = getLines(dataSet);
 
                 // Create the edge layer by merging lines between 3+ order nodes
                 // (Merged lines are multilines)
                 LineMerger lineMerger = new LineMerger();
                 for (Geometry geometry : geomColl) {
                         lineMerger.add(geometry);
                 }
 
                 DefaultMetadata nodeMedata = new DefaultMetadata(new Type[]{
                                 TypeFactory.createType(Type.POINT),
                                 TypeFactory.createType(Type.INT)}, new String[]{"the_geom",
                                 GraphSchema.ID});
 
                 DiskBufferDriver nodeDriver = new DiskBufferDriver(dsf.getResultFile("gdms"), nodeMedata);
 
 
                 DiskRTree diskRTree = new DiskRTree();
                 diskRTree.newIndex(new File(dsf.getTempFile()));
 
                 edges = lineMerger.getMergedLineStrings();
 
                 DiskBufferDriver edgesDriver = new DiskBufferDriver(dsf.getResultFile("gdms"), edgeMedata);
 
                 int gidNode = 1;
                 int i = 1;
 
                 Value[] values = new Value[edgesFieldsCount];
                 for (Iterator it = edges.iterator(); it.hasNext();) {
                         Geometry geom = (Geometry) it.next();
                         values[0] = ValueFactory.createValue(geom);
                         values[1] = ValueFactory.createValue(i);
                         i++;
                         Coordinate[] cc = geom.getCoordinates();
                         Coordinate start = cc[0];
                         Coordinate end = cc[cc.length - 1];
                         int[] gidsStart = diskRTree.query(new Envelope(start));
                         if (gidsStart.length == 0) {
                                 values[2] = ValueFactory.createValue(gidNode);
                                 nodeDriver.addValues(new Value[]{ValueFactory.createValue(gf.createPoint(start)),
                                                 ValueFactory.createValue(gidNode)});
                                 diskRTree.insert(new Envelope(start), gidNode);
                                 gidNode++;
                         } else {
                                 values[2] = ValueFactory.createValue(gidsStart[0]);
                         }
                         int[] gidsEnd = diskRTree.query(new Envelope(end));
                         if (gidsEnd.length == 0) {
                                 values[3] = ValueFactory.createValue(gidNode);
                                 nodeDriver.addValues(new Value[]{ValueFactory.createValue(gf.createPoint(end)),
                                                 ValueFactory.createValue(gidNode)});
                                 diskRTree.insert(new Envelope(end), gidNode);
                                 gidNode++;
                         } else {
                                 values[3] = ValueFactory.createValue(gidsEnd[0]);
                         }
                         values[4] = ValueFactory.createValue(-1);
                         values[5] = ValueFactory.createValue(-1);
                         edgesDriver.addValues(values);
 
                 }
 
                 // write envelope
                 edgesDriver.writingFinished();
                 nodeDriver.writingFinished();
                 ds_nodes_name = dsf.getSourceManager().getUniqueName(output_name + ds_nodes_name);
                 dsf.getSourceManager().register(ds_nodes_name, nodeDriver.getFile());
                 ds_edges_name = dsf.getSourceManager().getUniqueName(output_name + ds_edges_name);
                 dsf.getSourceManager().register(ds_edges_name, edgesDriver.getFile());
         }
 
         /**
          * Extract all lines as a set of connected and splitted lines.
          * Self-intersection is not allowed.
          * This method uses the union operator. It must be changed in the futur to limit memory overhead.
          * @param dataSet
          * @return
          * @throws DriverException
          */
         public Collection<Geometry> getLines(DataSet dataSet)
                 throws DriverException {
                 LineNoder linenoder = new LineNoder(dataSet);
                 Collection lines = linenoder.getLines();
 
                 final Geometry nodedGeom = linenoder.getNodeLines((List) lines);
 
                 return linenoder.toLines(nodedGeom);
 
         }
 
         /**
          * Create the datasource that contains polygons.
          * @param inputSDS
          * @param omEdges
          * @throws DriverException
          * @throws NonEditableDataSourceException
          * @throws NoSuchTableException
          * @throws IndexException
          */
         public void createPolygonAndTopology() throws DriverException,
                 NonEditableDataSourceException, NoSuchTableException,
                 IndexException,
                 IOException,
                 DriverLoadException,
                 DataSourceCreationException {
                 // Create the face layer
 
                 DefaultMetadata faceMedata = new DefaultMetadata(new Type[]{
                                 TypeFactory.createType(Type.POLYGON),
                                 TypeFactory.createType(Type.INT)}, new String[]{"the_geom",
                                 GraphSchema.ID});
 
                 DiskBufferDriver faceDriver = new DiskBufferDriver(dsf.getResultFile("gdms"), faceMedata);
 
                 Polygonizer polygonizer = new Polygonizer();
                 polygonizer.add(edges);
                 Collection polygons = polygonizer.getPolygons();
 
 
                 int no = 1;
                 for (Iterator it = polygons.iterator(); it.hasNext();) {
                         Geometry face = (Geometry) it.next();
                         face.normalize(); // add on 2007-08-11
                         faceDriver.addValues(new Value[]{ValueFactory.createValue(face),
                                         ValueFactory.createValue(new Integer(no++))});
 
                 }
 
                 faceDriver.writingFinished();
 
                 ds_polygons_name = dsf.getSourceManager().getUniqueName(output_name + ds_polygons_name);
                 dsf.getSourceManager().register(ds_polygons_name, faceDriver);
 
                 DataSource sdsFaces = dsf.getDataSource(ds_polygons_name);
 
                 DataSource sdsEdges = dsf.getDataSource(ds_edges_name);
 
                 sdsFaces.open();
                 sdsEdges.open();
 
                 // inscrit les numéros de face dans les arcs
                 // Les arcs qui sont en bords de face sont codés à -1.
 
                 int leftFaceIndex = sdsEdges.getFieldIndexByName(GraphSchema.LEFT_FACE);
                 int rigthFaceIndex = sdsEdges.getFieldIndexByName(GraphSchema.RIGHT_FACE);
 
 
                 long rowCount = sdsEdges.getRowCount();
                 for (int i = 0; i < rowCount; i++) {
 
                         Geometry g1 = sdsEdges.getGeometry(i);
 
                         sdsEdges.setInt(i, leftFaceIndex,
                                 MINUS_ONE);
                         sdsEdges.setInt(i, rigthFaceIndex,
                                 MINUS_ONE);
                         Iterator<Integer> iterator = query(sdsFaces, g1.getEnvelopeInternal());
 
                         while (iterator.hasNext()) {
                                 Integer index = iterator.next();
                                 Geometry g = sdsFaces.getGeometry(index);
                                 Value val = ValueFactory.createValue(index + 1);
                                 IntersectionMatrix im = g1.relate(g);
                                 // intersection between boundaries has dimension 1
                                 if (im.matches("*1*******")) {
                                         int edgeC0 = getIndex(g1.getCoordinates()[0], g);
                                         int edgeC1 = getIndex(g1.getCoordinates()[1], g);
 
                                         // The Math.abs(edgeC1-edgeC0) test inverse the rule when
                                         // the two
                                         // consecutive
                                         // points are the last point and the first point of a
                                         // ring...
 
                                         if ((edgeC1 > edgeC0 && Math.abs(edgeC1 - edgeC0) == 1)
                                                 || (edgeC1 < edgeC0 && Math.abs(edgeC1 - edgeC0) > 1)) {
                                                 sdsEdges.setFieldValue(i, rigthFaceIndex, val);
                                         } else {
                                                 sdsEdges.setFieldValue(i, leftFaceIndex, val);
                                         }
                                 } // intersection between the line and the polygon interior has
                                 // dimension
                                 // 1
                                 else if (im.matches("1********")) {
                                         sdsEdges.setFieldValue(i, rigthFaceIndex, val);
                                         sdsEdges.setFieldValue(i, leftFaceIndex, val);
 
                                 } // intersection between the line and the polygon exterior has
                                 // dimension
                                 // 1
                                 // else if (im.matches("F********")) {}
                                 else;
 
                         }
 
                 }
                 sdsEdges.commit();
                 sdsEdges.close();
                 sdsFaces.close();
 
         }
 
         // Returns the index of c in the geometry g or -1 if c is not a vertex of g
         private int getIndex(Coordinate c, Geometry g) {
                 Coordinate[] cc = g.getCoordinates();
                 for (int i = 0; i < cc.length; i++) {
                         if (cc[i].equals(c)) {
                                 return i;
                         }
                 }
                 return -1;
         }
 
         /**
          * A quick search for geometries, using an envelope comparison.
          *
          * @param envelope
          *            the envelope to query against
          * @return geometries whose envelopes intersect the given envelope
          */
         public Iterator<Integer> query(DataSource sdsFaces, Envelope envelope)
                 throws DriverException, NoSuchTableException, IndexException {
 
                 if (!dsf.getIndexManager().isIndexed(ds_polygons_name, "the_geom")) {
                         dsf.getIndexManager().buildIndex(ds_polygons_name, "the_geom", pm);
                 }
 
                DefaultSpatialIndexQuery query = new DefaultSpatialIndexQuery("the_geom", envelope);
                 Iterator<Integer> it = sdsFaces.queryIndex(query);
                 return it;
         }
 }
