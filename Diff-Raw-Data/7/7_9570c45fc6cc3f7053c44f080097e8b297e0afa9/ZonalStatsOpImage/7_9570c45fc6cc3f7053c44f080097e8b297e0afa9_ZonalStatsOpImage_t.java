 /*
  * Copyright 2009 Michael Bedward
  *
  * This file is part of jai-tools.
  *
  * jai-tools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * jai-tools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package jaitools.media.jai.zonalstats;
 
 import jaitools.CollectionFactory;
 import jaitools.numeric.StreamingSampleStats;
 import jaitools.numeric.Statistic;
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.image.RenderedImage;
 import java.util.Map;
 import java.util.SortedSet;
 import javax.media.jai.AreaOpImage;
 import javax.media.jai.ImageLayout;
 import javax.media.jai.NullOpImage;
 import javax.media.jai.OpImage;
 import javax.media.jai.ROI;
 import javax.media.jai.iterator.RandomIter;
 import javax.media.jai.iterator.RandomIterFactory;
 import javax.media.jai.iterator.RectIter;
 import javax.media.jai.iterator.RectIterFactory;
 
 /**
  * Calculates image summary statistics for a data image within zones defined by
  * a integral valued zone image. If a zone image is not provided all data image
  * pixels are treated as being in the same zone (zone 0).
  * <p>
  * An {@code ROI} can be provided to specify a subset of the data image that will
  * be included in the calculations.
  *
  * @see ZonalStatsDescriptor Description of the algorithm and example
  *
  * @author Michael Bedward
  * @since 1.0
  * @source $URL$
  * @version $Id$
  */
 public class ZonalStatsOpImage extends NullOpImage {
 
     private int srcBand;
 
     private ROI roi;
 
     private Statistic[] stats;
 
     private RenderedImage dataImage;
     private Rectangle dataImageBounds;
     private RenderedImage zoneImage;
     private AffineTransform zoneTransform;
     private SortedSet<Integer> zones;
 
     /**
      * Constructor
      *
      * @param dataImage a {@code RenderedImage} from which data values will be read
      *
      * @param zoneImage an optional {@code RenderedImage} of integral data type that defines
      *        the zones for which to calculate summary data
      * 
      * @param config configurable attributes of the image (see {@link AreaOpImage})
      *
      * @param layout an optional {@code ImageLayout} object
      *
      * @param stats an array of {@code Statistic} constants specifying the data required
      *
      * @param band the data image band to process
      *
      * @param roi an optional {@code ROI} for data image masking
      *
      * @see ZonalStatsDescriptor
      * @see Statistic
      */
     public ZonalStatsOpImage(RenderedImage dataImage, RenderedImage zoneImage,
             Map config,
             ImageLayout layout,
             Statistic[] stats,
             int band,
             ROI roi,
             AffineTransform zoneTransform) {
 
         super(dataImage, layout, config, OpImage.OP_COMPUTE_BOUND);
 
         this.dataImage = dataImage;
 
         dataImageBounds = new Rectangle(
                 dataImage.getMinX(), dataImage.getMinY(),
                 dataImage.getWidth(), dataImage.getHeight());
 
         this.zoneImage = zoneImage;
         this.zoneTransform = zoneTransform;
 
         this.stats = stats;
         this.srcBand = band;
 
         this.roi = roi;
     }
 
     /**
      * Compiles the set of zone ID values from the zone image. Note, we are
      * assuming that the zone values are in band 0.
      * <p>
      * If a zone image wasn't provided we treat all data image pixels as
      * belonging to zone 0.
      */
     private void buildZoneList() {
         zones = CollectionFactory.newTreeSet();
         if (zoneImage != null) {
             RectIter iter = RectIterFactory.create(zoneImage, null);
             do {
                 do {
                     zones.add(iter.getSample());
                 } while (!iter.nextPixelDone());
                 iter.startPixels();
             } while (!iter.nextLineDone());
         } else {
             zones.add(0);
         }
     }
 
     /**
      * Delegates calculation of statistics to either {@linkplain #compileZonalStatistics()}
      * or {@linkplain #compileUnzonedStatistics()}.
      *
      * @return the results as a new instance of {@code ZonalStats}
      */
     private ZonalStats compileStatistics() {
         if (zoneImage != null) {
             return compileZonalStatistics();
         } else {
             return compileUnzonedStatistics();
         }
     }
 
     /**
      * Used to calculate statistics when a zone image was provided.
      *
      * @return the results as a new instance of {@code ZonalStats}
      */
     private ZonalStats compileZonalStatistics() {
         buildZoneList();
 
         Map<Integer, StreamingSampleStats> results = CollectionFactory.newTreeMap();
 
         for (Integer zone : zones) {
             StreamingSampleStats sampleStats = new StreamingSampleStats();
             sampleStats.setStatistics(stats);
             results.put(zone, sampleStats);
         }
 
         RectIter dataIter = RectIterFactory.create(dataImage, null);
         if (zoneTransform == null) {  // Idenity transform assumed
             RectIter zoneIter = RectIterFactory.create(zoneImage, dataImageBounds);
 
             int y = dataImage.getMinY();
             do {
                 int x = dataImage.getMinX();
                 do {
                     if (roi == null || roi.contains(x, y)) {
                         double value = dataIter.getSampleDouble(srcBand);
                         int zone = zoneIter.getSample();
                         results.get(zone).addSample(value);
                     }
                     zoneIter.nextPixelDone(); // safe call
                     x++;
                 } while (!dataIter.nextPixelDone());
 
                 dataIter.startPixels();
                 zoneIter.startPixels();
                 zoneIter.nextLineDone(); // safe call
 
             } while (!dataIter.nextLineDone());
 
         } else {
             RandomIter zoneIter = RandomIterFactory.create(zoneImage, dataImageBounds);
             Point2D.Double dataPos = new Point2D.Double();
             Point2D.Double zonePos = new Point2D.Double();
             dataPos.y = dataImage.getMinY();
             do {
                 dataPos.x = dataImage.getMinX();
                 do {
                     if (roi == null | roi.contains(dataPos)) {
                         double value = dataIter.getSampleDouble(srcBand);
                         zoneTransform.transform(dataPos, zonePos);
                         int zone = zoneIter.getSample((int)zonePos.x, (int)zonePos.y, 0);
                         results.get(zone).addSample(value);
                     }
                     dataPos.x++ ;
                 } while (!dataIter.nextPixelDone());
 
                 dataIter.startPixels();
                 dataPos.y++ ;
 
             } while (!dataIter.nextLineDone());
         }
 
         ZonalStats zonalStats = new ZonalStats(stats, zones);
 
         for (Integer zone : zones) {
             zonalStats.setZoneResults(zone, results.get(zone));
         }
 
         return zonalStats;
     }
 
 
     /**
      * Used to calculate statistics when no zone image was provided.
      *
      * @return the results as a new instance of {@code ZonalStats}
      */
     private ZonalStats compileUnzonedStatistics() {
         buildZoneList();
         Integer zoneID = zones.first();
 
         StreamingSampleStats sampleStats = new StreamingSampleStats();
         sampleStats.setStatistics(stats);
 
         RectIter dataIter = RectIterFactory.create(dataImage, null);
         int y = dataImage.getMinY();
         do {
             int x = dataImage.getMinX();
             do {
                 if (roi == null || roi.contains(x, y)) {
                     double value = dataIter.getSampleDouble(srcBand);
                     sampleStats.addSample(value);
                 }
                 x++;
             } while (!dataIter.nextPixelDone());
 
             dataIter.startPixels();
             y++ ;
 
         } while (!dataIter.nextLineDone());
 
 
         ZonalStats zonalStats = new ZonalStats(stats, zones);
         zonalStats.setZoneResults(zoneID, sampleStats);
         return zonalStats;
     }
 
     /**
      * Get the specified property.
      * <p>
      * Use this method to retrieve the calculated statistics as a {@code ZonalStats}
      * object by setting {@code name} to {@linkplain ZonalStatsDescriptor#ZONAL_STATS_PROPERTY}.
      *
      * @param name property name
      *
      * @return the requested property
      */
     @Override
     public Object getProperty(String name) {
         if (ZonalStatsDescriptor.ZONAL_STATS_PROPERTY.equalsIgnoreCase(name)) {
             return compileStatistics();
         } else {
             return super.getProperty(name);
         }
     }
 
 
     /**
      * Get the class of the given property. For
      * {@linkplain ZonalStatsDescriptor#ZONAL_STATS_PROPERTY} this will return
      * {@code ZonalStats.class}.
      *
      * @param name property name
      *
      * @return the property class
      */
     @Override
     public Class getPropertyClass(String name) {
         if (ZonalStatsDescriptor.ZONAL_STATS_PROPERTY.equalsIgnoreCase(name)) {
             return ZonalStats.class;
         } else {
             return super.getPropertyClass(name);
         }
     }
 
     /**
      * Get all property names
      * @return property names as an array of Strings
      */
     @Override
     public String[] getPropertyNames() {
         String[] names;
         int k = 0;
 
         String[] superNames = super.getPropertyNames();
         if (superNames != null) {
             names = new String[superNames.length + 1];
             for (String name : super.getPropertyNames()) {
                 names[k++] = name;
             }
         } else {
             names = new String[1];
         }
 
         names[k] = ZonalStatsDescriptor.ZONAL_STATS_PROPERTY;
         return names;
     }
 
 }
 
