 
 
 package es.igosoftware.euclid.experimental.vectorial.rendering;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import es.igosoftware.euclid.experimental.measurement.GArea;
 import es.igosoftware.euclid.experimental.measurement.IMeasure;
 import es.igosoftware.euclid.vector.IVector2;
 import es.igosoftware.util.GMath;
 import es.igosoftware.util.GPair;
 import es.igosoftware.util.LRUCache;
 
 
 public class GIconSymbol
          extends
             GSymbol {
 
 
    private static final LRUCache<BufferedImage, Double, RuntimeException>                 percentFilledCache;
    private static final LRUCache<GPair<BufferedImage, IVector2>, Image, RuntimeException> scaleCache;
 
    static {
       percentFilledCache = new LRUCache<BufferedImage, Double, RuntimeException>(20,
                new LRUCache.ValueFactory<BufferedImage, Double, RuntimeException>() {
                   @Override
                   public Double create(final BufferedImage image) {
                      return percentFilled(image);
                   }
                });
 
       scaleCache = new LRUCache<GPair<BufferedImage, IVector2>, Image, RuntimeException>(20,
                new LRUCache.ValueFactory<GPair<BufferedImage, IVector2>, Image, RuntimeException>() {
                   @Override
                   public Image create(final GPair<BufferedImage, IVector2> key) {
                      final BufferedImage image = key._first;
                      final IVector2 extent = key._second;
 
                      return image.getScaledInstance((int) extent.x(), (int) extent.y(), Image.SCALE_SMOOTH);
                   }
                });
    }
 
 
    private static double percentFilled(final BufferedImage image) {
       final double step = 1d / (image.getWidth() * image.getHeight());
       double acum = 0;
 
       for (int x = 0; x < image.getWidth(); x++) {
          for (int y = 0; y < image.getHeight(); y++) {
            final int alpha = (image.getRGB(x, y) >>> 24) & 0xFF;
            acum += step * (alpha / 255d);
          }
       }
 
       return acum;
    }
 
 
    private final Image    _scaledIcon;
    private final IVector2 _position;
    private final IVector2 _extent;
 
 
    public GIconSymbol(final BufferedImage icon,
                       final IVector2 point,
                       final IMeasure<GArea> pointSize,
                       final GVectorialRenderingContext rc) {
 
       final double percentFilled = percentFilledCache.get(icon);
 
       final double areaInSquaredMeters = pointSize.getValueInReferenceUnits();
 
      final double extent = GMath.sqrt(areaInSquaredMeters / percentFilled);
       final IVector2 pointPlusExtent = rc._renderingStyle.increment(point, rc._projection, extent, extent);
       _extent = rc.scaleExtent(pointPlusExtent.sub(point)).rounded();
 
       final IVector2 scaledPoint = rc.scaleAndTranslatePoint(point);
       _position = scaledPoint.sub(_extent.div(2)).rounded();
 
       _scaledIcon = scaleCache.get(new GPair<BufferedImage, IVector2>(icon, _extent));
    }
 
 
    @Override
    public boolean isBiggerThan(final double lodMinSize) {
       return ((_extent.x() * _extent.y()) > lodMinSize);
    }
 
 
    @Override
    public void draw(final Color fillColor,
                     final float borderWidth,
                     final Color borderColor,
                     final GVectorialRenderingContext rc) {
       //      rc.drawFlippedImage(_scaledIcon, _position.x(), _position.y(), _extent.x(), _extent.y(), fillColor);
       rc.drawFlippedImage(_scaledIcon, _position.x(), _position.y(), _extent.x(), _extent.y());
    }
 
 
 }
