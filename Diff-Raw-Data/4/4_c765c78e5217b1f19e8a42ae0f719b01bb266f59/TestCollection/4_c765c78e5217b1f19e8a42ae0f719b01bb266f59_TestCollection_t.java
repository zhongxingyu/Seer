 package tutorials.multiformat.testcases.collections;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.ElementState.FORMAT;
 import ecologylab.serialization.tlv.Utils;
 
 public class TestCollection
 {
 	/**
 	 * @param args
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws SIMPLTranslationException, IOException
 	{
 		Container test = new Container();
 		test.initializeInstance();
 
 		final StringBuilder sb = new StringBuilder();
 		OutputStream outputStream = new OutputStream()
 		{
 			@Override
 			public void write(int b) throws IOException
 			{
 				sb.append((char) b);
 			}
 		};
 
 		TranslationScope containerTranslations = TranslationScope.get("container", Container.class,
 				ClassA.class, ClassB.class);
 
 		test.serialize(System.out, FORMAT.XML);
 		System.out.println();
 		System.out.println();
 		test.serialize(System.out, FORMAT.JSON);
 
 		System.out.println();
 		System.out.println();
 
 		test.serialize(outputStream, FORMAT.JSON);
 		System.out.println(sb);
 
 		System.out.println();
 		Container data = (Container) containerTranslations.deserializeCharSequence(sb, FORMAT.JSON);
 		data.serialize(System.out, FORMAT.XML);
 		System.out.println();
 		System.out.println();
 		data.serialize(System.out, FORMAT.JSON);
 
 		System.out.println();
		System.out.println();		
 
 		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
 
 		test.serialize(dataOutputStream, FORMAT.TLV);
 
 		Utils.writeHex(System.out, byteArrayOutputStream.toByteArray());
 
 		System.out.println();
 		System.out.println();
 
 		Container deContainer = (Container) containerTranslations.deserializeByteArray(
 				byteArrayOutputStream.toByteArray(), FORMAT.TLV);
 		deContainer.serialize(System.out, FORMAT.XML);
 
 	}
 }
