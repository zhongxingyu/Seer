 package gov.usgs.cida.coastalhazards.wps;
 
 import com.vividsolutions.jts.geom.CoordinateSequence;
 import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import gov.usgs.cida.coastalhazards.util.CRSUtils;
 import gov.usgs.cida.coastalhazards.wps.exceptions.UnsupportedFeatureTypeException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.geoserver.wps.gs.GeoServerProcess;
 import org.geotools.data.Query;
 import org.geotools.data.collection.ListFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.feature.collection.SortedSimpleFeatureCollection;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.filter.AttributeExpressionImpl;
 import org.geotools.filter.SortByImpl;
 import org.geotools.geometry.jts.Geometries;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.process.ProcessException;
 import org.geotools.process.factory.DescribeParameter;
 import org.geotools.process.factory.DescribeProcess;
 import org.geotools.process.factory.DescribeResult;
 import org.geotools.referencing.CRS;
 import org.geotools.util.logging.Logging;
 import org.opengis.coverage.grid.GridGeometry;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.filter.sort.SortBy;
 import org.opengis.filter.sort.SortOrder;
 import org.opengis.geometry.DirectPosition;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 
 /**
  *
  * @author dmsibley
  */
 @DescribeProcess(
 		title = "Shoreline Ribboning",
 		description = "Create ribbon geometries following a shoreline",
 		version = "1.0.0")
 public class RibboningProcess implements GeoServerProcess {
 
 	private final static Logger LOGGER = Logging.getLogger(RibboningProcess.class);
     
     private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));
 	
 	public static final String ribbonAttr = "RIBBONID";
 
 	@DescribeResult(name = "result", description = "Layer with ribboned clones")
 	public SimpleFeatureCollection execute(
 			@DescribeParameter(name = "features", min = 1, max = 1) SimpleFeatureCollection features,
 			@DescribeParameter(name = "bbox", min = 0, max = 1) ReferencedEnvelope bbox,
 			@DescribeParameter(name = "width", min = 1, max = 1) Integer width,
 			@DescribeParameter(name = "height", min = 1, max = 1) Integer height,
 			@DescribeParameter(name = "invert-side", min = 0, max = 1) Boolean invertSide,
 			@DescribeParameter(name = "calcAngles", min = 0, max = 1) Boolean calcAngles,
 			@DescribeParameter(name = "ribbon-count", min = 0, max = 1) Integer ribbonCount,
 			@DescribeParameter(name = "offset", min = 0, max = 1) Integer offset,
 			@DescribeParameter(name = "sort-attribute", min = 0, max = 1) String sortAttribute) throws Exception {
 		
 		
 		if (null == invertSide) {
 			invertSide = Boolean.FALSE;
 		}
 		if (null == calcAngles) {
 			calcAngles = Boolean.FALSE;
 		}
 		if (null == ribbonCount) {
 			ribbonCount = 1;
 		}
 		if (null == offset) {
 			offset = 5;
 		}
 		SimpleFeatureCollection featuresToProcess = features;
 		
 		if (null != sortAttribute) {
 			featuresToProcess = sortFeatures(sortAttribute, features);
 		}
 		
 		CoordinateReferenceSystem bboxCRS = bbox.getCoordinateReferenceSystem();
 		CoordinateReferenceSystem featureCRS = CRSUtils.getCRSFromFeatureCollection(featuresToProcess);
 		
 		double[] xyOffset = getXYOffset(bbox, bboxCRS, featureCRS, width, height, offset);
 		
 		return new Process(featuresToProcess, invertSide, sortAttribute, calcAngles, ribbonCount, xyOffset).execute();
 
 	}
 	
 	public SimpleFeatureCollection sortFeatures(String sortAttribute, SimpleFeatureCollection features) {
 		SimpleFeatureCollection result = features;
 		
 		AttributeDescriptor sortAttr = features.getSchema().getDescriptor(sortAttribute);
 		
 		if (null != sortAttr) {
 			SortBy sort = new SortByImpl(new AttributeExpressionImpl(sortAttr.getName()), SortOrder.ASCENDING);
 			result = new SortedSimpleFeatureCollection(features, new SortBy[] {sort});
 		} else {
 			LOGGER.log(Level.WARNING, "Could not find sort attribute {0}", sortAttribute);
 		}
 		
 		return result;
 	}
 
 	public Query invertQuery(Query targetQuery, GridGeometry targetGridGeometry) throws ProcessException {
 		Query result = new Query(targetQuery);
 		result.setProperties(Query.ALL_PROPERTIES);
 		return result;
 	}
 
 	private double[] getXYOffset(ReferencedEnvelope bbox, CoordinateReferenceSystem bboxCRS, CoordinateReferenceSystem featureCRS, Integer width, Integer height, Integer offset) {
 		double[] result = new double[] {offset, offset};
 		
 		try {
 			MathTransform transformToFeature = null;
 			try {
 				transformToFeature = CRS.findMathTransform(bboxCRS, featureCRS);
 			} catch (Exception e) {
 				LOGGER.log(Level.INFO, "Could not find transform for CRS", e);
 			}
 
 			double bbwidth = bbox.getWidth();
 			double bbheight = bbox.getHeight();
 			double pxwidth = width.doubleValue();
 			double pxheight = height.doubleValue();
 
 			if (null != transformToFeature) {
 				DirectPosition upperCorner = transformToFeature.transform(bbox.getUpperCorner(), null);
 				DirectPosition lowerCorner = transformToFeature.transform(bbox.getLowerCorner(), null);
 				
 				bbwidth = upperCorner.getOrdinate(0) - lowerCorner.getOrdinate(0);
 				bbheight = upperCorner.getOrdinate(1) - lowerCorner.getOrdinate(1);
 			}
 
 			result = new double[] {
 				(bbwidth / pxwidth) * offset,
 				(bbheight / pxheight) * offset
 			};
 		} catch (Exception e) {
 			LOGGER.log(Level.INFO, "Could not transform to feature CRS");
 		}
 		
 		return result;
 	}
 	
 	private class Process {
 		
 		private final double SEQUENTIAL_DISTANCE = 10.0;
 		
 		private final SimpleFeatureCollection featureCollection;
 		private final boolean invert;
 		private final String sortAttribute;
 		private final boolean calcAngles;
 		private final int ribbonCount;
 		private final double[] xyOffset;
 
 		Map<Integer, LinkedList<SimpleFeature>> baselineFeaturesMap;
 
 		private Process(SimpleFeatureCollection featureCollection,
 				boolean invert,
 				String sortAttribute,
 				boolean calcAngles,
 				int ribbonCount,
 				double[] offset) {
 			this.featureCollection = featureCollection;
 			this.invert = invert;
 			this.sortAttribute = sortAttribute;
 			this.calcAngles = calcAngles;
 			this.ribbonCount = ribbonCount;
 			this.xyOffset = offset;
 		}
 
 		private SimpleFeatureCollection execute() throws Exception {
 			ListFeatureCollection result = null;
 			
 			SimpleFeatureType sft = this.featureCollection.getSchema();
 			SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
 			sftb.addAll(sft.getAttributeDescriptors());
 			sftb.add(ribbonAttr, Integer.class);
 			sftb.setName(sft.getName());
 			SimpleFeatureType schema = sftb.buildFeatureType();
 			
 			result = new ListFeatureCollection(schema);
 			LineString prevLine = null;
 			double[] prevLineOffset = null;
 			SimpleFeatureIterator features = null;
 			try {
 				features = this.featureCollection.features();
 				while (features.hasNext()) {
 					SimpleFeature feature = features.next();
 
 					MultiLineString lines = getMultiLineString(feature);
 					if (null != lines) {
						List<MultiLineString> ribbonLines = new ArrayList<MultiLineString>();
 						for (int ribbonNum = 0; ribbonNum < ribbonCount; ribbonNum++) {
							ribbonLines.add((MultiLineString) lines.clone());
 						}
 
 						for (int geomNum = 0; geomNum < lines.getNumGeometries(); geomNum++) {
 							LineString line = (LineString) lines.getGeometryN(geomNum);
 
 							double[] lineOffset = computeXYOffset(prevLine, prevLineOffset, line);
 
 							if (null != lineOffset) {
 								for (int ribbonNum = 0; ribbonNum < ribbonCount; ribbonNum++) {
 									ribbonLines.get(ribbonNum).getGeometryN(geomNum).apply(new RibboningFilter(lineOffset, ribbonNum));
 								}
 							}
 
 							prevLine = line;
 							prevLineOffset = lineOffset;
 						}
 
 						List<SimpleFeature> ribbonedFeature = new ArrayList<SimpleFeature>();
 
 						for (int ribbonNum = 0; ribbonNum < ribbonCount; ribbonNum++) {
 							SimpleFeatureBuilder fb = new SimpleFeatureBuilder(schema);
 							fb.addAll(feature.getAttributes());
 							fb.set(ribbonAttr, new Integer(ribbonNum + 1));
 							fb.set(feature.getDefaultGeometryProperty().getName(), ribbonLines.get(ribbonNum));
 							ribbonedFeature.add(fb.buildFeature(null));
 						}
 
 						result.addAll(ribbonedFeature);
 					} else {
 						LOGGER.log(Level.WARNING, "feature is not a line?");
 					}
 				}
 			} finally {
 				if (null != features) {
 					features.close();
 				}
 			}
 
 			return result;
 		}
 		
 		private double[] computeXYOffset(LineString prevLine, double[] prevLineOffset, LineString currLine) {
 			double[] result = null;
 			
 			Point prevStart = null;
 			Point prevEnd = null;
 			if (null != prevLine) {
 				prevStart = prevLine.getStartPoint();
 				prevEnd = prevLine.getEndPoint();
 			}
 			
 			Point currStart = null;
 			Point currEnd = null;
 			if (null != currLine) {
 				currStart = currLine.getStartPoint();
 				currEnd = currLine.getEndPoint();
 			}
 			
 			boolean isSequential = isSequential(prevEnd, currStart);
 			Double angle = computeAngle(prevStart, prevEnd, currStart, currEnd, isSequential);
 			
 			double[] lineOffset = computeLineOffset(angle);
 			result = computeOffsetForPoints(lineOffset, prevLineOffset, isSequential);
 			
 			return result;
 		}
 		
 		private boolean isSequential(Point prevEnd, Point currStart) {
 			boolean isSequential = false;
 			
 			if (null != prevEnd && null != currStart) {
 				isSequential = prevEnd.isWithinDistance(currStart, SEQUENTIAL_DISTANCE);
 			}
 			
 			return isSequential;
 		}
 		
 		private Double computeAngle(Point prevStart, Point prevEnd, Point currStart, Point currEnd, boolean isSequential) {
 			Double angle = null;
 			
 			if ((null != prevStart && null != prevEnd)
 					&& (null != currStart && null != currEnd)) {
 				if (isSequential) {
 					LOGGER.log(Level.FINE, "Sequential order");
 					if (null != this.sortAttribute && this.calcAngles) {
 						angle = invertIfNecessary(getAngle(prevStart, currStart, currEnd), this.invert);
 					} else {
 						angle = invertIfNecessary(getAngle(currStart, currEnd), this.invert);
 					}
 				} else {
 					LOGGER.log(Level.FINE, "Broken order");
 					angle = invertIfNecessary(getAngle(currStart, currEnd), this.invert);
 				}
 			} else if ((null != currStart && null != currEnd)) {
 				LOGGER.log(Level.FINE, "just curr");
 				angle = invertIfNecessary(getAngle(currStart, currEnd), this.invert);
 			} else if ((null != prevStart && null != prevEnd)) {
 				LOGGER.log(Level.WARNING, "A prev, but no curr?");
 			} else {
 				LOGGER.log(Level.WARNING, "Everything is null?");
 			}
 			
 			return angle;
 		}
 		
 		private double[] computeLineOffset(Double angle) {
 			double[] result = null;
 			
 			if (null != angle) {
 				double xOffset = 0.0;
 				double yOffset = 0.0;
 
 				xOffset = getXOffset(angle, xyOffset[0]);
 				yOffset = getYOffset(angle, xyOffset[1]);
 
 				result = new double[] {xOffset, yOffset};
 				LOGGER.log(Level.FINE, "Angle : {0}, X : {1}, Y : {2}", new Object[] {angle, xOffset, yOffset});
 			} else {
 				LOGGER.log(Level.WARNING, "No angle computed");
 			}
 			
 			return result;
 		}
 		
 		private double[] computeOffsetForPoints(double[] lineOffset, double[] prevLineOffset, boolean isSequential) {
 			double[] result = null;
 			
 			if (null != lineOffset) {
 				if (null != this.sortAttribute && isSequential 
 						&& null != prevLineOffset
 						&& 1 < prevLineOffset.length
 						&& 0 == (prevLineOffset.length % 2)) {
 					result = new double[] {
 						prevLineOffset[prevLineOffset.length - 2],
 						prevLineOffset[prevLineOffset.length - 1],
 						lineOffset[0],
 						lineOffset[1]
 					};
 				} else {
 					result = lineOffset;
 				}
 			}
 			
 			return result;
 		}
 		
 		private double getXOffset(double angle, double offset) {
 			double result = offset * Math.cos(angle);
 			
 			return result;
 		}
 		
 		private double getYOffset(double angle, double offset) {
 			double result = offset * Math.sin(angle);
 			
 			return result;
 		}
 		
 		private Double invertIfNecessary(Double angle, boolean toInvert) {
 			Double result = angle;
 			
 			if (null != angle) {
 				double offset = 0.0d;
 				if (toInvert) {
 					offset = Math.PI;
 				}
 				result = new Double((angle.doubleValue() + offset) % (2 * Math.PI));
 			}
 			
 			return result;
 		}
 		
 		private Double getAngle(Point a, Point b) {
 			Double result = null;
 			double TWO_PI = 2 * Math.PI;
 			
 			if (null != a && null != b) {
 				double thetaLine = (Math.atan2(b.getY() - a.getY(), b.getX() - a.getX()) + TWO_PI) % TWO_PI;
 				
 				double theta = ((thetaLine - (TWO_PI / 4)) + TWO_PI) % TWO_PI;
 				
 				result = theta;
 			} else {
 				LOGGER.log(Level.WARNING, "Missing a point");
 			}
 			
 			return result;
 		}
 		
 		private Double getAngle(Point a, Point b, Point c) {
 			Double result = null;
 			double TWO_PI = 2 * Math.PI;
 			
 			if (null != a && null != b && null != c) {
 				double thetaA = (Math.atan2(b.getY() - a.getY(), b.getX() - a.getX()) + TWO_PI) % TWO_PI;
 				double thetaB = (Math.atan2(c.getY() - b.getY(), c.getX() - b.getX()) + TWO_PI) % TWO_PI;
 				
 				double theta = ((Math.PI - thetaA + thetaB) + TWO_PI) % TWO_PI;
 				double midAngle = theta / 2;
 				
 				result = thetaA - midAngle;
 			} else {
 				LOGGER.log(Level.WARNING, "Missing a point");
 			}
 			
 			return result;
 		}
 		
 		private MultiLineString getMultiLineString(SimpleFeature feature) {
 			MultiLineString result = null;
 			
 			if (null != feature) {
 				Geometry geometry = (Geometry) feature.getDefaultGeometry();
 				Geometries geomType = Geometries.get(geometry);
 				switch (geomType) {
 					case POLYGON:
 					case MULTIPOLYGON:
 						throw new UnsupportedFeatureTypeException("Polygons not supported");
 					case LINESTRING:
 						LineString lineString = (LineString) geometry;
 						result = geometryFactory.createMultiLineString(new LineString[] {lineString});
 						break;
 					case MULTILINESTRING:
 						result = (MultiLineString) geometry;
 						break;
 					case POINT:
 					case MULTIPOINT:
 						throw new UnsupportedFeatureTypeException("Points not supported");
 					default:
 						throw new UnsupportedFeatureTypeException("Only line type supported");
 				}
 			}
 			
 			return result;
 		}
 	}
 	
 	public static class RibboningFilter implements CoordinateSequenceFilter {
 		private final double[][] pointOffsets;
 		private final int ribbonNum;
 
         public RibboningFilter(double[] lineOffset, int ribbonNum) {
 			double[][] result = null;
 			if (null != lineOffset && 0 < lineOffset.length) {
 				result = new double[lineOffset.length / 2][];
 				for (int i = 0; i < lineOffset.length; i++) {
 					if (0 == (i % 2) && (i+1) < lineOffset.length) {
 						result[i/2] = new double[] {0.0, 0.0};
 						result[i/2][0] = lineOffset[i];
 					} else if (1 == (i % 2)) {
 						result[i/2][1] = lineOffset[i];
 					}
 				}
 			}
 			
 			this.pointOffsets = result;
 			this.ribbonNum = ribbonNum;
         }
 
         public void filter(CoordinateSequence seq, int i) {
 			double offsetX = 0.0;
 			double offsetY = 0.0;
 			
 			if (i < pointOffsets.length) {
 				offsetX = pointOffsets[i][0] * ribbonNum;
 				offsetY = pointOffsets[i][1] * ribbonNum;
 			} else {
 				offsetX = pointOffsets[pointOffsets.length - 1][0] * ribbonNum;
 				offsetY = pointOffsets[pointOffsets.length - 1][1] * ribbonNum;
 			}
 			
 			seq.setOrdinate(i, 0, seq.getOrdinate(i, 0) + offsetX);
 			seq.setOrdinate(i, 1, seq.getOrdinate(i, 1) + offsetY);
         }
 
         public boolean isDone() {
             return false;
         }
 
         public boolean isGeometryChanged() {
             return true;
         }
 
     }
 }
