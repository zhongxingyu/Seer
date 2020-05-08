 package fr.itinerennes.business.service;
 
 import org.acra.ErrorReporter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.view.Gravity;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import fr.itinerennes.R;
 import fr.itinerennes.database.DatabaseHelper;
 import fr.itinerennes.exceptions.GenericException;
 import fr.itinerennes.utils.ResourceResolver;
 
 /**
  * Methods to get an icon representing a transport line.
  * 
  * @author Jérémie Huchet
  */
 public class LineIconService extends AbstractService {
 
     /** The event logger. */
     private static final Logger LOGGER = LoggerFactory.getLogger(LineIconService.class);
 
     /** The context. */
     private final Context context;
 
     /**
      * Creates a line transport icon service.
      * 
      * @param context
      *            the context
      * @param dbHelper
      *            the database helper
      */
     public LineIconService(final Context context, final DatabaseHelper dbHelper) {
 
         super(dbHelper);
         this.context = context;
     }
 
     /**
      * Gets an icon representing a transport line.
      * 
      * @param line
      *            the name of the line you want the icon
      * @return the drawable
      * @throws GenericException
      *             when the drawable is not found
      */
     public final Drawable getIcon(final String line) throws GenericException {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getIcon.start - line={}", line);
         }
 
         final String resName = String.format("z_ic_line_%s",
                 line.toLowerCase().replaceAll("[^a-z0-9_.]", "_").replaceAll("ex$", ""));
 
         final int id = ResourceResolver.getDrawableId(context, resName, 0);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("icon resource : name={}, id={}", resName, id);
         }
 
         final Drawable image;
         if (0 == id) {
             final String msg = String.format("No icon found for line %s", line);
             LOGGER.warn(msg);
            ErrorReporter.getInstance().handleSilentException(new Resources.NotFoundException(msg));
             image = getDefaultIcon(context, line);
         } else {
             image = context.getResources().getDrawable(id);
         }
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getIcon.end - imageNotNull={}", null != image);
         }
         return image;
     }
 
     /**
      * Gets an icon for the given line.
      * <p>
      * If no icon was found, or if an error occurs a default icon is returned.
      * 
      * @param context
      *            a context
      * @param line
      *            the route short name you want the icon
      * @return an icon for the given route short name
      */
     public final Drawable getIconOrDefault(final Context context, final String line) {
 
         Drawable icon = null;
         try {
             icon = getIcon(line);
         } catch (final GenericException e) {
             LOGGER.error("unable to load the icon for the line {}", line);
         }
         if (icon == null) {
             icon = getDefaultIcon(context, line);
         }
         return icon;
     }
 
     /**
      * Returns a mock icon. This method always returns a displayable drawable.
      * 
      * @param context
      *            the context
      * @param line
      *            the name of the line you want the icon
      * @return the drawable
      */
     public final Drawable getDefaultIcon(final Context context, final String line) {
 
         final Bitmap icon = Bitmap.createBitmap(46, 46, Bitmap.Config.ARGB_8888);
         final Canvas canvas = new Canvas(icon);
 
         final LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
                 LayoutParams.FILL_PARENT);
 
         final Bitmap iconBackground = BitmapFactory.decodeResource(context.getResources(),
                 R.drawable.misc_default_line_icon);
 
         final TextView text = new TextView(context);
         text.setBackgroundDrawable(new BitmapDrawable(iconBackground));
         text.setText(line);
         text.setGravity(Gravity.CENTER);
         text.setLayoutParams(params);
         text.layout(0, 0, 46, 46);
 
         final RelativeLayout layout = new RelativeLayout(context);
         layout.addView(text);
         layout.forceLayout();
         layout.draw(canvas);
 
         final Drawable d = new BitmapDrawable(icon);
         d.setBounds(0, 0, 46, 46);
         return d;
     }
 }
