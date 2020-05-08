 /**
  * 
  */
 package ecologylab.appframework.types.pref;
 
 import ecologylab.appframework.types.prefs.Pref;
 import ecologylab.appframework.types.prefs.PrefSetBaseClassProvider;
import ecologylab.platformspecifics.FundamentalPlatformSpecifics;
 import ecologylab.serialization.TranslationsClassProvider;
 
 /**
  * Provides an array of classes that can be used to translate a PrefSet in an authenticating
  * application.
  * 
  * @author Zachary O. Toups (zach@ecologylab.net)
  * 
  */
 public class PrefSetAuthClassProvider extends PrefSetBaseClassProvider
 {
 	public static final PrefSetAuthClassProvider	STATIC_INSTANCE				= new PrefSetAuthClassProvider();
 
 	protected PrefSetAuthClassProvider()
 	{
 	}
 
 	/**
 	 * @see ecologylab.appframework.types.prefs.PrefSetBaseClassProvider#specificSuppliedClasses()
 	 */
 	@Override
 	protected Class<? extends Pref<?>>[] specificSuppliedClasses()
 	{
 		Class[]													authPrefTranslations	=
 		{ PrefAuthList.class };
 		
		return TranslationsClassProvider.combineClassArrays(super.specificSuppliedClasses(), authPrefTranslations, FundamentalPlatformSpecifics.get().additionalPrefSetBaseTranslations());
 	}
 }
