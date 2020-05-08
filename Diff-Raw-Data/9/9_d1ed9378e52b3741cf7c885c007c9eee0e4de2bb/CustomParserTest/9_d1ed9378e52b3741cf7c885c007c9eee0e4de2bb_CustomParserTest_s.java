 package mikera.edn.pojos;
 
 import static org.junit.Assert.*;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import mikera.edn.pojos.CustomParser;
 import mikera.edn.pojos.CustomParsers;
 
 import org.junit.Test;
 
 import us.bpsm.edn.Keyword;
 import us.bpsm.edn.parser.Parseable;
 import us.bpsm.edn.parser.Parsers;
 
 public class CustomParserTest {
 
     @Test
     public void testVectorParser() {
         CustomParser<List<Long>> p =
             new CustomParsers.VectorParser<Long>(new CustomParsers.LongParser());
         Parseable r = Parsers.newParseable("[1 2 3 4 5]");
 
         List<Long> v = p.nextValue(r);
         assertEquals(5, v.size());
         assertEquals(1, (long) v.get(0));
         assertEquals(5, (long) v.get(4));
     }
 
     @Test
     public void testArrayParser() {
         CustomParser<Long[]> p =
             new CustomParsers.ArrayParser<Long>(new CustomParsers.LongParser()) {
                 @Override
                 public Long[] createArray(int size) {
                     return new Long[size];
                 }
             };
         Parseable r = Parsers.newParseable("[1 2 3 4 5]");
 
         Long[] v = p.nextValue(r);
         assertEquals(5, v.length);
         assertEquals(1L, (long) v[0]);
         assertEquals(5L, (long) v[4]);
     }
 
     @Test
     public void testMapParser() {
         // Point parser
        final Keyword kx = Keyword.newKeyword(null, "x");
        final Keyword ky = Keyword.newKeyword(null, "y");
         CustomParser<Double> doubleParser = CustomParsers.DOUBLE_PARSER;
         HashMap<Keyword, CustomParser<Double>> pointMap =
             new HashMap<Keyword, CustomParser<Double>>();
         pointMap.put(kx, doubleParser);
         pointMap.put(ky, doubleParser);
         CustomParser<Point2D.Double> pointParser =
             new CustomParsers.MapParser<Point2D.Double>(pointMap) {
                 @Override
                 public Point2D.Double construct(Map<?, ?> data) {
                     double x = (Double) data.get(kx);
                     double y = (Double) data.get(ky);
                     return new Point2D.Double(x, y);
                 }
             };
 
         // Rectangle parser
        final Keyword kul = Keyword.newKeyword(null, "upperLeft");
        final Keyword klr = Keyword.newKeyword(null, "lowerRight");
         HashMap<Keyword, CustomParser<Point2D.Double>> rectMap =
             new HashMap<Keyword, CustomParser<Point2D.Double>>();
         rectMap.put(kul, pointParser);
         rectMap.put(klr, pointParser);
         CustomParser<Rectangle2D.Double> rectParser =
             new CustomParsers.MapParser<Rectangle2D.Double>(rectMap) {
                 @Override
                 public Rectangle2D.Double construct(Map<?, ?> data) {
                     Point2D.Double ul = (Point2D.Double) data.get(kul);
                     Point2D.Double lr = (Point2D.Double) data.get(klr);
                     Rectangle2D.Double rect =
                         new Rectangle2D.Double(ul.x, ul.y, 0, 0);
                     rect.add(lr);
                     return rect;
                 }
             };
 
         Parseable r =
             Parsers
                 .newParseable("{:upperLeft {:x 1.0 :y -1.0} :lowerRight {:x 5.0 :y 4}}");
 
         Rectangle2D.Double rect = rectParser.nextValue(r);
         assertEquals(1.0, rect.x, 0.0);
         assertEquals(-1.0, rect.y, 0.0);
 
         assertEquals(4.0, rect.width, 0.0);
         assertEquals(5.0, rect.height, 0.0);
 
     }
 }
