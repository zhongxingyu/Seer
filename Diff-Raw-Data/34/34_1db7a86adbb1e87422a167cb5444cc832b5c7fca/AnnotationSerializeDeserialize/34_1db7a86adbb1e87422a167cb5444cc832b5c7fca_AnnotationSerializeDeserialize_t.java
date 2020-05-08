 package ecologylab.bigsemantics.metadata;
 
import java.net.MalformedURLException;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.junit.Test;
 
 import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
 import ecologylab.bigsemantics.metadata.builtins.CreativeAct;
import ecologylab.bigsemantics.metadata.builtins.Document;
 import ecologylab.bigsemantics.metadata.builtins.HtmlText;
 import ecologylab.bigsemantics.metadata.builtins.RichArtifact;
import ecologylab.net.ParsedURL;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.formatenums.Format;
 import ecologylab.serialization.formatenums.StringFormat;
 import ecologylab.translators.sql.testing.ecologylabXmlTest.GeneratedMetadataTranslationScopeTest;
 
 public class AnnotationSerializeDeserialize
 {
 	
 	private static final SimplTypesScope	META_METADATA_TRANSLATIONS	= RepositoryMetadataTranslationScope.get();
 	
 	RichArtifact<HtmlText> annotation;
 	
	public AnnotationSerializeDeserialize() throws MalformedURLException
 	{
 		annotation = new RichArtifact<HtmlText>();
 		HtmlText text = new HtmlText();
 		text.setText("annotation. booyah!");
 		text.setHtmlString("<p>annotation. <b>booyah!</b></p>");
 		annotation.setMedia(text);
 		
 		CreativeAct creativeAct = new CreativeAct();
		creativeAct.setAction(2);
		Document creator = new Document();
		creator.setTitle("andrew");
		creator.setLocation(new ParsedURL(new URL("http://ecologylab.net/people/andrew")));
		creativeAct.setCreator(creator);
		creativeAct.setTime(new Date());
 		ArrayList<CreativeAct> creativeActs = new ArrayList<CreativeAct>();
 		creativeActs.add(creativeAct);
 		annotation.setCreativeActs(creativeActs);
 	}
 	
 	@Test
 	public void testAnnotationSerializeDeserialize()
 	{
 		try
 		{
 			StringBuilder serializedString = new StringBuilder();
 			SimplTypesScope.serialize(annotation, serializedString, StringFormat.JSON);
 			System.out.println(serializedString);
 			
 			Object deserialized = META_METADATA_TRANSLATIONS.deserialize(serializedString, StringFormat.JSON);
 			serializedString = new StringBuilder();
 			SimplTypesScope.serialize(deserialized, serializedString, StringFormat.JSON);
 			System.out.println(serializedString);
 			
 		} catch (SIMPLTranslationException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args)
 	{
		AnnotationSerializeDeserialize test;
		try
		{
			test = new AnnotationSerializeDeserialize();
			test.testAnnotationSerializeDeserialize();
		} 
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 
 }
