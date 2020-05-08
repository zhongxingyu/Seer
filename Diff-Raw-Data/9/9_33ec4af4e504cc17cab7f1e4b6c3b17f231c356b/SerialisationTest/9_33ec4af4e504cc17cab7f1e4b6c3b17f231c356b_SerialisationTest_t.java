 package org.acme;
 
 import static org.acme.TestMocks.*;
 import static org.dynamicvalues.Directives.*;
 import static org.junit.Assert.*;
 
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.dynamicvalues.Directives;
 import org.dynamicvalues.Dynamic;
 import org.dynamicvalues.Exclusion;
 import org.junit.Test;
 import org.virtualrepository.Property;
 import org.virtualrepository.RepositoryService;
 import org.virtualrepository.csv.CsvAsset;
 import org.virtualrepository.csv.CsvCodelist;
 import org.virtualrepository.sdmx.SdmxCodelist;
 import org.virtualrepository.spi.ServiceProxy;
 import org.virtualrepository.tabular.Column;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 
 public class SerialisationTest {
 
 	@Test
 	public void csvCanTravelOverATypedLink() throws Exception {
 
 		CsvAsset csv = new CsvAsset("testid", "testname", aService().name("repotest").get());
 		csv.properties().add(new Property("name", "value"));
 		CsvAsset parsed = xmlRoundTripOf(csv);
 
 		System.out.println(csv);
 		System.out.println(parsed);
 
 		assertNotEquals(csv, parsed); // service proxies are dropped
 	}
 
 	@Test
 	public void csvCanBeConvertedToValues() throws Exception {
 
 		CsvAsset csv = new CsvAsset("testid", "testname", aService().name("repotest").get());
 		csv.properties().add(new Property("name", "value"));
 
 		Map<String, Object> value = Dynamic.valueOf(csv, directives());
 
 		System.out.println(value);
 	}
 
 	@Test
 	public void csvCodelistsCanBeConvertedToValues() throws Exception {
 
 		CsvCodelist codelist = new CsvCodelist("testid", "testname", 0, aService().name("repotest").get());
 
 		Column c = new Column(new QName("code"), new QName("code"), String.class);
 		codelist.setColumns(c);
 
 		Map<String, Object> value = Dynamic.valueOf(codelist, directives());
 
 		System.out.println(codelist);
 		System.out.println(value);
 
 	}
 
 	@Test
 	public void csvCodelistsCanTravelOverATypedLink() throws Exception {
 
 		CsvCodelist codelist = new CsvCodelist("testid", "testname", 0, aService().name("repotest").get());
 
 		Column c = new Column(new QName("code"), new QName("code"), String.class);
 		codelist.setColumns(c);
 
 		CsvCodelist parsed = xmlRoundTripOf(codelist);
 
 		System.out.println(codelist);
 		System.out.println(parsed);
 
 		assertNotEquals(codelist, parsed); // service proxies are dropped
 
 	}
 
 	@Test
 	public void sdmxCodelistCanTravelOverATypedLink() throws Exception {
 
 		SdmxCodelist codelist = new SdmxCodelist("name", aService().name("repotest").get());
 		SdmxCodelist parsed = xmlRoundTripOf(codelist);
 
 		System.out.println(codelist);
 		System.out.println(parsed);
 
 		assertNotEquals(codelist, parsed);
 
 	}
 
 	@Test
 	public void sdmxCodelistCanBeConvertedToValues() throws Exception {
 
 		SdmxCodelist codelist = new SdmxCodelist("name", aService().name("repotest").get());
 
 		Map<String, Object> value = Dynamic.valueOf(codelist, directives());
 
 		System.out.println(codelist);
 		System.out.println(value);
 
 	}
 	
 
 	static <T> T xmlRoundTripOf(T o) throws Exception {
 
 		XStream x = new XStream(new StaxDriver());
 		x.omitField(RepositoryService.class, "proxy");
 
 		StringWriter w = new StringWriter();
 		x.marshal(o, new PrettyPrintWriter(w));
 
 		System.out.println(w.toString());
 
 		@SuppressWarnings("all")
 		T read = (T) x.fromXML(w.toString());
 
 		return read;
 	}
 
 	static Directives directives() {
		return by().excluding(new PrivateProperty(), type(ServiceProxy.class)).mapping(objectsToStringFor(QName.class));
 	}
 
 	public static class PrivateProperty implements Exclusion {
 		@Override
 		public boolean exclude(Object object, Field field) throws Exception {
 			if (!Property.class.isAssignableFrom(field.getClass()))
 				return false;
 
 			Property prop = Property.class.cast(field.get(object));
 			return !prop.isDisplay();
 
 		}
 
 	}
 }
