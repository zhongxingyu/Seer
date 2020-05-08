 package org.fastorm.defns;
 
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.fastorm.constants.FastOrmKeys;
 import org.fastorm.temp.ISecondaryTempTableMaker;
 import org.fastorm.temp.impl.TempTableMakerFactory;
 import org.fastorm.utilities.collections.Files;
 import org.fastorm.utilities.maps.Maps;
 import org.fastorm.utilities.tests.IIntegrationTest;
 import org.fastorm.xmlToMap.IMapPrinter;
 import org.fastorm.xmlToMap.MapToXmlAttributePrinter;
 import org.fastorm.xmlToMap.MapToXmlElementPrinter;
 import org.fastorm.xmlToMap.XmlToMapParser;
 import org.springframework.core.io.ClassPathResource;
 
 public class EntityDefnParserTest extends TestCase implements IIntegrationTest {
 
 	public void testSampleFilesAllMeanSameThing() {
 		checkFile("sample1.xml");
 		checkFile("sample2.xml");
 		checkFile("sample3.xml");
 		checkFile("sample4.xml");
 	}
 
 	private void checkFile(String string) {
 		String xml = Files.getTextFromClassPath(getClass(), string);
 		XmlToMapParser<String, Object> parser = IEntityDefn.Utils.makeXmlParser();
 		Object actual = parser.parse(xml);
 		// EntityDefn expectedDefn = new EntityDefn(//
 		// Maps.<String, String> makeMap(FastOrmKeys.entityName, "person", FastOrmKeys.tableName, "person", FastOrmKeys.idColumn, "id", FastOrmKeys.versionColumn, "p_version"),//
 		// Arrays.asList(new EntityDefn(Maps.<String, String> makeMap(FastOrmKeys.entityName, "address", FastOrmKeys.tableName, "address", FastOrmKeys.idColumn, "a_id", FastOrmKeys.versionColumn,
 		// "version", FastOrmKeys.childLink, "a_person"), Collections.EMPTY_LIST), new EntityDefn(Maps.<String, String> makeMap(FastOrmKeys.entityName, "telephone", FastOrmKeys.tableName, "telephone",
 		// FastOrmKeys.idColumn, "id", FastOrmKeys.versionColumn, "version", FastOrmKeys.childLink, "t_person"), Collections.EMPTY_LIST), new EntityDefn(Maps.<String, String>
 		// makeMap(FastOrmKeys.entityName, "employer", FastOrmKeys.tableName, "employer", FastOrmKeys.idColumn, "id", FastOrmKeys.versionColumn, "version", FastOrmKeys.parentLink, "p_employer"),
 		// Collections.EMPTY_LIST)));
 
 		Map<Object, Object> expected = Maps.makeMap(//
 				FastOrmKeys.entityName, "person",//
 				FastOrmKeys.tableName, "person",//
 				FastOrmKeys.tempTableName, "person_temp",//
 				FastOrmKeys.idColumn, "id",//
 				FastOrmKeys.idType, "integer",//
 				FastOrmKeys.versionColumn, "p_version",//
 				FastOrmKeys.useTemporaryTable, true,//
 				"address", Maps.makeMap(//
 						FastOrmKeys.entityName, "address",//
 						FastOrmKeys.tableName, "address",//
 						FastOrmKeys.tempTableName, "address_temp",//
 						FastOrmKeys.idColumn, "a_id",//
 						FastOrmKeys.idType, "integer",//
 						FastOrmKeys.versionColumn, "version", //
 						FastOrmKeys.useTemporaryTable, true,//
 						FastOrmKeys.childLink, "a_person"),//
 				"telephone", Maps.makeMap(//
 						FastOrmKeys.entityName, "telephone",//
 						FastOrmKeys.tableName, "telephone",//
 						FastOrmKeys.tempTableName, "telephone_temp",//
 						FastOrmKeys.idColumn, "id",//
 						FastOrmKeys.idType, "integer",//
 						FastOrmKeys.versionColumn, "version", //
 						FastOrmKeys.useTemporaryTable, true,//
 						FastOrmKeys.childLink, "t_person"),//
 				"employer", Maps.makeMap(//
 						FastOrmKeys.entityName, "employer",//
 						FastOrmKeys.tableName, "employer",//
 						FastOrmKeys.tempTableName, "employer_temp",//
 						FastOrmKeys.idColumn, "id",//
 						FastOrmKeys.idType, "integer",//
 						FastOrmKeys.versionColumn, "version", //
 						FastOrmKeys.useTemporaryTable, true,//
 						FastOrmKeys.parentLink, "p_employer"));//
 		assertEquals(expected, actual);
 	}
 
 	public void testMakersAreAllocated() {
 		final TempTableMakerFactory factory = new TempTableMakerFactory();
 		ClassPathResource resource = new ClassPathResource("sample1.xml", getClass());
 		IEntityDefn entityDefn = IEntityDefn.Utils.parse(factory, resource);
 		IEntityDefn.Utils.walk(entityDefn, new IEntityDefnVisitor() {
 			@Override
 			public void acceptPrimary(IEntityDefn primary) throws Exception {
 				assertNull(primary.getMaker());
 			}
 
 			@Override
 			public void acceptChild(IEntityDefn parent, IEntityDefn child) throws Exception {
 				Map<String, String> parentParameters = parent.parameters();
 				Map<String, String> childParameters = child.parameters();
 				ISecondaryTempTableMaker maker = factory.findMakerFor(parentParameters, childParameters);
 				assertNotNull(child.getEntityName(), maker);
 				ISecondaryTempTableMaker childMaker = child.getMaker();
 				assertNotNull(child.getEntityName(), childMaker);
 				assertEquals(maker.getClass(), childMaker.getClass());
 			}
 		});
 	}
 
 	public void testSmokeTestForDefn() {
 		XmlToMapParser<String, Object> parser = IEntityDefn.Utils.makeXmlParser();
 		Map<String, Object> expected = Maps.makeMap(//
 				FastOrmKeys.tableName, "",//
 				FastOrmKeys.tempTableName, "_temp",//
 				FastOrmKeys.entityName, "",//
 				FastOrmKeys.versionColumn, "",//
 				FastOrmKeys.idColumn, "",//
 				FastOrmKeys.useTemporaryTable, true,//
 				FastOrmKeys.idType, "integer");
 		checkXml(parser, expected, new MapToXmlElementPrinter<String, Object>("Entity"));
 		checkXml(parser, expected, new MapToXmlAttributePrinter<String, Object>("Entity"));
 	}
 
 	private void checkXml(XmlToMapParser<String, Object> parser, Map<String, Object> expected, IMapPrinter<String, Object> printer) {
 		String xml = printer.print(expected);
 		Map<String, Object> defn = parser.parse(xml);
 		assertEquals(expected, defn);
 	}
 
 }
