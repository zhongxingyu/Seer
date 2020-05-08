 package ecologylab.xml.library.kml;
 
 import ecologylab.xml.TranslationScope;
 import ecologylab.xml.library.kml.feature.KmlFeature;
 import ecologylab.xml.library.kml.feature.Placemark;
 import ecologylab.xml.library.kml.feature.container.Container;
 import ecologylab.xml.library.kml.feature.container.Document;
 import ecologylab.xml.library.kml.feature.container.Folder;
 import ecologylab.xml.library.kml.geometry.Coordinates;
 import ecologylab.xml.library.kml.geometry.Geometry;
 import ecologylab.xml.library.kml.geometry.LineString;
 import ecologylab.xml.library.kml.geometry.Point;
import ecologylab.xml.library.kml.geometry.InnerBoundaryIs;
import ecologylab.xml.library.kml.geometry.OuterBoundaryIs;
import ecologylab.xml.library.kml.geometry.LinearRing;
import ecologylab.xml.library.kml.geometry.Polygon;
 import ecologylab.xml.library.kml.view.AbstractView;
 import ecologylab.xml.library.kml.view.Camera;
 import ecologylab.xml.library.kml.view.LookAt;
 import ecologylab.xml.types.element.ElementTypeTranslations;
 
 /**
  * Provide XML translation mappings for use with KML.
  * 
  * @author Zachary O. Toups (toupsz@gmail.com)
  */
 public class KMLTranslations
 {
 	public static final String	PACKAGE_NAME	= "ecologylab.xml.library.kml";
 
 	public static final Class	TRANSLATIONS[]	=
 															{ Container.class,
 			Coordinates.class, Document.class, Geometry.class, Kml.class,
 			KmlFeature.class, KmlObject.class, Placemark.class, Point.class,
 			LineString.class, Folder.class, AbstractView.class, Camera.class,
			LookAt.class, InnerBoundaryIs.class, OuterBoundaryIs.class,
			LinearRing.class, Polygon.class};
 
 	/**
 	 * This accessor will work from anywhere, in any order, and stay efficient.
 	 * 
 	 * @return TranslationSpace for basic ecologylab.services
 	 */
 	public static TranslationScope get()
 	{
 		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS,
 				ElementTypeTranslations.get());
 	}
 }
