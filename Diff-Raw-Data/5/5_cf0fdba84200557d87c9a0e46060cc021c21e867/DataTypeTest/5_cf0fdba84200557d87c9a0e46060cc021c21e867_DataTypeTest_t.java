 /**
  * 
  */
 package org.bnl.gov.unitconversion;
 
 import static org.bnl.gov.unitconversion.ConversionData.ConversionDataBuilder.conversionDataOfType;
 import static org.bnl.gov.unitconversion.Device.DeviceBuilder.device;
 import static org.bnl.gov.unitconversion.MagnetMeasurementData.MagnetMeasurementDataBuilder.magnetMeasurements;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.core.MultivaluedHashMap;
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.junit.Assert;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 /**
  * @author shroffk
  * 
  */
 public class DataTypeTest {
     @Rule
     public TemporaryFolder tempFolder = new TemporaryFolder();
 
     /**
      * An object that represents a device It
      */
     @Test
     public void testDevice() {
 	/**
 	 * Example list of Devices [{"type_description":
 	 * "68mm, SHORT SEXTUPOLE", "vendor": "Danfysik, Denmark", "name":
 	 * "SH1G2C30A", "install_id": 172, "system": "Storage Ring",
 	 * "cmpnt_type_name": "Sext A"}, {"type_description":
 	 * "66mm, DBL COIL, SHORT QUAD", "vendor": "Tesla, England", "name":
 	 * "QH1G2C30A", "install_id": 173, "system": "Storage Ring",
 	 * "cmpnt_type_name": "Quad D"}]
 	 */
 	Device device = device("SH1G2C30A").system("Storage Ring")
 		.installId(172).componentTypeName("Sext A")
 		.typeDescription("68mm, SHORT SEXTUPOLE")
 		.vendor("Danfysik, Denmark").build();
 	/**
 	 * {"type_description": "66mm, SNGL COIL, SHORT QUAD", "vendor":
 	 * "BINP, Russia", "name": "QM1G4C24B", "install_id": 740, "serial_no":
 	 * "1", "system": "Storage Ring", "inventory_id": 41, "cmpnt_type_name":
 	 * "Quad A"}
 	 */
 	device = device("QM1G4C24B").system("Storage Ring").installId(740)
 		.serialNumber(10)
 		.typeDescription("66mm, SNGL COIL, SHORT QUAD")
 		.componentTypeName("Quad A").vendor("BINP, Russia").build();
 
 	// We test the json parsing of the device objects
 
 	try {
 	    ObjectMapper objectMapper = new ObjectMapper();
 	    File jsonDevice = tempFolder.newFile("jsonDevice");
 	    objectMapper.writeValue(jsonDevice, device);
 	    Device parsedDevice = objectMapper.readValue(jsonDevice,
 		    Device.class);
 	    Assert.assertEquals("Failed to correctly parse Device object ",
 		    device, parsedDevice);
 	} catch (Exception e) {
 	    Assert.fail(e.getMessage());
 	}
     }
 
     @Test
     public void testMagnetMeasurementData() {
 	List<Double> current = Arrays.asList(4.99619, 9.99787, 14.9953,
 		19.99719, 29.99279, 39.99175, 49.99177, 59.99112, 69.98786,
 		79.98677, 89.9856, 99.98575, 109.98234, 119.98134, 129.98055,
 		139.97986, 149.97671, 159.97629, 164.97395, 169.97598,
 		174.97374, 179.97451, 184.97307, 184.97331, 179.97483,
 		174.97416, 169.97644, 164.97447, 159.97667, 149.97713,
 		139.98046, 129.9809, 119.982, 109.98274, 99.98262, 89.98605,
 		79.98715, 69.98771, 59.98827, 49.99177, 39.9919, 29.99267,
 		19.99405, 14.99525, 9.99781, 4.99612);
 	List<Double> field = Arrays.asList(0.006010210477162501,
 		0.01037091727400225, 0.014952996384287503,
 		0.019652527931825252, 0.02922136869282225,
 		0.038904351322243756, 0.048631397704142765, 0.058366488481056,
 		0.06809398675870351, 0.07781522917796252, 0.08751049433027999,
 		0.097182549513, 0.10681568997290102, 0.1163986521945975,
 		0.12591185008119374, 0.13531951052102, 0.14454029422523604,
 		0.15345847200230403, 0.1577250523778825, 0.16182213025381198,
 		0.16570048172748, 0.16937280553178302, 0.17283865625925676,
 		0.17283998110621726, 0.16996242076436174, 0.16685922153058202,
 		0.163474594704162, 0.15974051058658875, 0.15565475228153028,
 		0.14678675275035977, 0.1374723356113695, 0.1279484244114475,
 		0.11832390575145, 0.10864271304540851, 0.09892675354149001,
 		0.08919255076144877, 0.079438526136865, 0.0696759812402745,
 		0.05989820061200851, 0.05012107621269351, 0.04032921268034751,
 		0.030530575789565254, 0.020726159132717505, 0.0158212603472,
 		0.01091867053736725, 0.006011142231052001);
 	List<String> direction = Arrays.asList("Up", "Up", "Up", "Up", "Up",
 		"Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up",
 		"Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Dn", "Dn",
 		"Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn",
 		"Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn",
 		"Dn");
 	List<Double> magneticLength = Collections.emptyList();
 	MagnetMeasurementData magnetMeasurementData = magnetMeasurements()
 		.Current(current).CurrentUnit("A").Field(field)
 		.FieldUnit("T-m").Direction(direction)
 		.MagneticLength(magneticLength).build();
 	try {
 	    ObjectMapper objectMapper = new ObjectMapper();
 	    File jsonMagnetMeasurementData = tempFolder
 		    .newFile("jsonMagnetMeasurementData");
 	    objectMapper.writeValue(jsonMagnetMeasurementData,
 		    magnetMeasurementData);
 	    System.out.println(objectMapper
 		    .writeValueAsString(magnetMeasurementData));
 	    MagnetMeasurementData parsedMagnetMeasurementData = objectMapper
 		    .readValue(jsonMagnetMeasurementData,
 			    MagnetMeasurementData.class);
 	    Assert.assertEquals("Failed to correctly parse Device object ",
 		    magnetMeasurementData, parsedMagnetMeasurementData);
 	} catch (Exception e) {
 	    Assert.fail(e.getMessage());
 	}
 
     }
 
     @Test
     public void testConversionData() {
 	// preliminary test
 	// ConversionDataBuilder.conversionDataOfType(type)
 	/**
 	 * Example: Standard { "BS-Q1BD1": { "name": "BS-Q1BD1", "system":
 	 * "BST",
 	 * 
 	 * "cmpnt_type": "BST Quadrupole 5200",
 	 * 
 	 * "municonv":
 	 * 
 	 * {"standard": {
 	 * 
 	 * "direction": ["Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up",
 	 * "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up",
 	 * "Up", "Up", "Up", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn",
 	 * "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn",
 	 * "Dn", "Dn", "Dn", "Dn"],
 	 * 
 	 * "run_number": [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
 	 * 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
 	 * 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
 	 * 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0],
 	 * 
 	 * "elem_name": "BS-Q1BD1",
 	 * 
 	 * "ref_draw": "BST-MG-QDP-5200",
 	 * 
 	 * "field_unit": "T-m",
 	 * 
 	 * "magnetic_len_design": 0.35,
 	 * 
 	 * "device_name": "BS-Q1BD1",
 	 * 
 	 * "current": [4.99619, 9.99787, 14.9953, 19.99719, 29.99279, 39.99175,
 	 * 49.99177, 59.99112, 69.98786, 79.98677, 89.9856, 99.98575, 109.98234,
 	 * 119.98134, 129.98055, 139.97986, 149.97671, 159.97629, 164.97395,
 	 * 169.97598, 174.97374, 179.97451, 184.97307, 184.97331, 179.97483,
 	 * 174.97416, 169.97644, 164.97447, 159.97667, 149.97713, 139.98046,
 	 * 129.9809, 119.982, 109.98274, 99.98262, 89.98605, 79.98715, 69.98771,
 	 * 59.98827, 49.99177, 39.9919, 29.99267, 19.99405, 14.99525, 9.99781,
 	 * 4.99612],
 	 * 
 	 * "field": [0.006010210477162501, 0.01037091727400225,
 	 * 0.014952996384287503, 0.019652527931825252, 0.02922136869282225,
 	 * 0.038904351322243756, 0.048631397704142765, 0.058366488481056,
 	 * 0.06809398675870351, 0.07781522917796252, 0.08751049433027999,
 	 * 0.097182549513, 0.10681568997290102, 0.1163986521945975,
 	 * 0.12591185008119374, 0.13531951052102, 0.14454029422523604,
 	 * 0.15345847200230403, 0.1577250523778825, 0.16182213025381198,
 	 * 0.16570048172748, 0.16937280553178302, 0.17283865625925676,
 	 * 0.17283998110621726, 0.16996242076436174, 0.16685922153058202,
 	 * 0.163474594704162, 0.15974051058658875, 0.15565475228153028,
 	 * 0.14678675275035977, 0.1374723356113695, 0.1279484244114475,
 	 * 0.11832390575145, 0.10864271304540851, 0.09892675354149001,
 	 * 0.08919255076144877, 0.079438526136865, 0.0696759812402745,
 	 * 0.05989820061200851, 0.05012107621269351, 0.04032921268034751,
 	 * 0.030530575789565254, 0.020726159132717505, 0.0158212603472,
 	 * 0.01091867053736725, 0.006011142231052001],
 	 * 
 	 * "b2k": [0, "input/(0.0175*3.335646*energy)", 2],
 	 * 
 	 * "ref_radius": 0.0175,
 	 * 
 	 * "energy_default": 3.0,
 	 * 
 	 * "current_unit": "A",
 	 * 
 	 * "serial": 8,
 	 * 
 	 * "i2b": [1,
 	 * "-4.88005454146e-07*input**2 + 0.0010428585052*input  -0.00105886879746"
 	 * ] } },
 	 * 
 	 * "serial": "8",
 	 * 
 	 * "design_length": "0.35" } }
 	 */
 
 	Device device = device("BS-Q1BD1").system("BST").installId(740)
 		.serialNumber(10)
 		.typeDescription("66mm, SNGL COIL, SHORT QUAD")
 		.componentTypeName("BST Quadrupole 5200")
 		.vendor("BINP, Russia").build();
 	List<Double> current = Arrays.asList(4.99619, 9.99787, 14.9953,
 		19.99719, 29.99279, 39.99175, 49.99177, 59.99112, 69.98786,
 		79.98677, 89.9856, 99.98575, 109.98234, 119.98134, 129.98055,
 		139.97986, 149.97671, 159.97629, 164.97395, 169.97598,
 		174.97374, 179.97451, 184.97307, 184.97331, 179.97483,
 		174.97416, 169.97644, 164.97447, 159.97667, 149.97713,
 		139.98046, 129.9809, 119.982, 109.98274, 99.98262, 89.98605,
 		79.98715, 69.98771, 59.98827, 49.99177, 39.9919, 29.99267,
 		19.99405, 14.99525, 9.99781, 4.99612);
 	List<Double> field = Arrays.asList(0.006010210477162501,
 		0.01037091727400225, 0.014952996384287503,
 		0.019652527931825252, 0.02922136869282225,
 		0.038904351322243756, 0.048631397704142765, 0.058366488481056,
 		0.06809398675870351, 0.07781522917796252, 0.08751049433027999,
 		0.097182549513, 0.10681568997290102, 0.1163986521945975,
 		0.12591185008119374, 0.13531951052102, 0.14454029422523604,
 		0.15345847200230403, 0.1577250523778825, 0.16182213025381198,
 		0.16570048172748, 0.16937280553178302, 0.17283865625925676,
 		0.17283998110621726, 0.16996242076436174, 0.16685922153058202,
 		0.163474594704162, 0.15974051058658875, 0.15565475228153028,
 		0.14678675275035977, 0.1374723356113695, 0.1279484244114475,
 		0.11832390575145, 0.10864271304540851, 0.09892675354149001,
 		0.08919255076144877, 0.079438526136865, 0.0696759812402745,
 		0.05989820061200851, 0.05012107621269351, 0.04032921268034751,
 		0.030530575789565254, 0.020726159132717505, 0.0158212603472,
 		0.01091867053736725, 0.006011142231052001);
 	List<String> direction = Arrays.asList("Up", "Up", "Up", "Up", "Up",
 		"Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up",
 		"Up", "Up", "Up", "Up", "Up", "Up", "Up", "Up", "Dn", "Dn",
 		"Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn",
 		"Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn", "Dn",
 		"Dn");
 	List<Double> magneticLength = Collections.emptyList();
 	MagnetMeasurementData magnetMeasurementData = magnetMeasurements()
 		.Current(current).CurrentUnit("A").Field(field)
 		.FieldUnit("T-m").Direction(direction)
 		.MagneticLength(magneticLength).build();
 	Map<String, ConversionAlgorithm> conversionFunctions = new HashMap<String, ConversionAlgorithm>();
 	conversionFunctions
 		.put("i2b",
 			new ConversionAlgorithm(1,
 				"-4.88005454146e-07*input**2 + 0.0010428585052*input  -0.00105886879746"));
 	conversionFunctions.put("b2k", new ConversionAlgorithm(0,
 		"input/(0.0175*3.335646*energy)", 2));
 	/**
 	 * A Single ConversionData object
 	 */
 	ConversionData conversionData = conversionDataOfType("Standard")
 		.withMagnetMeasurementData(magnetMeasurementData)
 		.withDefaultBeamEnergy(3.0).withMagneticLengthDesign(0.35)
 		.withConversionFunctions(conversionFunctions).build();
 
 	try {
 	    ObjectMapper objectMapper = new ObjectMapper();
 	    File jsonConversionData = tempFolder.newFile("jsonConversionData");
 	    objectMapper.writeValue(jsonConversionData, conversionData);
 	    System.out.println(objectMapper.writeValueAsString(conversionData));
 	    ConversionData parsedConversionData = objectMapper.readValue(
 		    jsonConversionData, ConversionData.class);
 	    Assert.assertEquals(
 		    "Failed to correctly parse ConversionData object ",
 		    conversionData, parsedConversionData);
 	} catch (Exception e) {
 	    Assert.fail(e.getMessage());
 	}
 
 	/**
 	 * Magnet-Chain {"LN-SO5":
 	 * 
 	 * {"municonv_chain":
 	 * 
 	 * {"standard":
 	 * 
 	 * {"raw":
 	 * "(-0.000456230223511*I+-0.000234111416058)*(1-0.0723486665586)",
 	 * 
 	 * "i2b": [0, "-0.000423222575196*input -0.00021717376728"] } },
 	 * 
 	 * "municonv":
 	 * 
 	 * {"standard":
 	 * 
 	 * {"direction": ["na", "na", "na", "na", "na", "na", "na", "na", "na",
 	 * "na", "na", "na", "na", "na", "na", "na", "na", "na", "na", "na",
 	 * "na"],
 	 * 
 	 * "current_unit": "A",
 	 * 
 	 * "sig_current": ["", "", "", "", "", "", "", "", "", "", "", "", "",
 	 * "", "", "", "", "", "", "", ""],
 	 * 
 	 * "sig_field": ["", "", "", "", "", "", "", "", "", "", "", "", "", "",
 	 * "", "", "", "", "", "", ""],
 	 * 
 	 * "field_unit": "T",
 	 * 
 	 * "device_name": "LN-SO5",
 	 * 
 	 * "current": [0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0,
 	 * 50.0, 55.0, 60.0, 65.0, 70.0, 75.0, 80.0, 85.0, 90.0, 95.0, 100.0],
 	 * 
 	 * "field": [-0.000132, -0.001228, -0.002381, -0.003523, -0.004655,
 	 * -0.005798, -0.00693, -0.00806, -0.009285, -0.010423, -0.011546,
 	 * -0.012694, -0.013817, -0.014941, -0.016098, -0.017217, -0.018358,
 	 * -0.019476, -0.020613, -0.021764, -0.022898],
 	 * 
 	 * "magnetic_len": ["", "", "", "", "", "", "", "", "", "", "", "", "",
 	 * "", "", "", "", "", "", "", ""],
 	 * 
 	 * "elem_name": "LN-SO5",
 	 * 
 	 * "serial": 53,
 	 * 
 	 * "i2b": [0, "0.000228046038239*input + 0.000113748"] } } } }
 	 */
 	final Map<String, ConversionAlgorithm> conversionFunctions1 = new HashMap<>();
 	conversionFunctions1.put("i2b", new ConversionAlgorithm(0,
 		"-0.000423222575196*input -0.00021717376728"));
 	MultivaluedMap<String, Map<String, ConversionData>> map = new MultivaluedHashMap<String, Map<String, ConversionData>>();
 	map.add("municonvChain", new HashMap<String, ConversionData>() {
 	    {
 		put("standard", conversionDataOfType("Standard")
 			.withConversionFunctions(conversionFunctions1).build());
 	    }
 	});
 	List<Double> LNSO5current = Arrays.asList(0.0, 5.0, 10.0, 15.0, 20.0,
 		25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 55.0, 60.0, 65.0, 70.0,
 		75.0, 80.0, 85.0, 90.0, 95.0, 100.0);
 	List<String> LNSO5direction = Arrays.asList("na", "na", "na", "na",
 		"na", "na", "na", "na", "na", "na", "na", "na", "na", "na",
 		"na", "na", "na", "na", "na", "na", "na");
 	List<Double> LNSO5field = Arrays.asList(-0.000132, -0.001228,
 		-0.002381, -0.003523, -0.004655, -0.005798, -0.00693, -0.00806,
 		-0.009285, -0.010423, -0.011546, -0.012694, -0.013817,
 		-0.014941, -0.016098, -0.017217, -0.018358, -0.019476,
 		-0.020613, -0.021764, -0.022898);
 	final MagnetMeasurementData magnetMeasurementData2 = magnetMeasurements()
 		.Current(LNSO5current).CurrentUnit("A").Field(LNSO5field)
 		.FieldUnit("T").Direction(LNSO5direction).build();
 	final Map<String, ConversionAlgorithm> conversionFunctions2 = new HashMap<String, ConversionAlgorithm>();
 	conversionFunctions2.put("i2b", new ConversionAlgorithm(0,
 		"0.000228046038239*input + 0.000113748"));
 
 	/**
 	 * Data in the form that it is returned from the service
 	 */
 	map.add("municonv", new HashMap<String, ConversionData>() {
 	    {
 		put("standard",
 			conversionDataOfType("Standard")
 				.withMagnetMeasurementData(
 					magnetMeasurementData2)
 				.withDefaultBeamEnergy(3.0)
 				.withMagneticLengthDesign(0.35)
 				.withConversionFunctions(conversionFunctions2)
 				.build());
 	    }
 	});
 
 	try {
 	    ObjectMapper objectMapper = new ObjectMapper();
 	    File jsonMap = tempFolder.newFile("jsonMap");
 	    objectMapper.writeValue(jsonMap, map);
 	    System.out.println(objectMapper.writeValueAsString(map));
 	    MultivaluedMap<String, Map<String, ConversionData>> parsedMap = objectMapper
 		    .readValue(
 			    jsonMap,
 			    new TypeReference<MultivaluedHashMap<String, Map<String, ConversionData>>>() {
 			    });
 	    Assert.assertEquals(
 		    "Failed to correctly parse ConversionData map object ",
 		    map, parsedMap);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail(e.getMessage());
 	}
 
 	/**
 	 * 
 	 * Complex {"A1BD1": {
 	 * 
 	 * "name": "A1BD1",
 	 * 
 	 * "system": "Booster",
 	 * 
 	 * "cmpnt_type": "BS Dipole BD1",
 	 * 
 	 * "municonv": {"complex":
 	 * 
 	 * {"1": {"i2b": [1,
 	 * "2.717329e-13*input**4 -4.50853e-10*input**3 + 2.156812e-07*input**2 + 0.001495718*input + 0.0014639"
 	 * ], "field_unit": "T", "current_unit": "A", "description":
 	 * "Dipole field component for a combined function magnet" },
 	 * 
 	 * "3": {"i2b": [1,
 	 * "-7.736754e-11*input**4 + 1.078356e-07*input**3 -4.27955e-05*input**2 + 0.061426*input + 0.031784"
 	 * ], "field_unit": "T/m^2", "current_unit": "A", "description":
 	 * "Sextupole field component for a combined function magnet" },
 	 * 
 	 * "2": {"i2b": [1,
 	 * "1.239146e-12*input**4 -2.242334e-09*input**3 + 1.117486e-06*input**2 + 0.007377142*input + 0.007218819"
 	 * ], "field_unit": "T/m", "current_unit": "A", "description":
 	 * "Quadrupole field component for a combined function magnet" } },
 	 * 
 	 * "standard": {"field_unit": "T", "current_unit": "A", "b2i": [1,
 	 * "-33.289411*input**4 + 84.116293*input**3 -61.320653*input**2 + 668.452373*input -0.969042"
 	 * ]}}, "serial": "24", "design_length": "1.3" } }
 	 */
 
 	MultivaluedMap<String, ConversionData> complexMap = new MultivaluedHashMap<String, ConversionData>();
 
 	Map<String, ConversionAlgorithm> conversionFunctionsCmplx1 = new HashMap<>();
 	conversionFunctionsCmplx1
 		.put("i2b",
 			new ConversionAlgorithm(
 				1,
 				"2.717329e-13*input**4 -4.50853e-10*input**3 + 2.156812e-07*input**2 + 0.001495718*input + 0.0014639"));
 	complexMap
 		.add("municonv",
 			conversionDataOfType("Complex:1")
 				.withMagnetMeasurementData(
 					magnetMeasurements().FieldUnit("T")
 						.CurrentUnit("A").build())
 				.withConversionFunctions(
 					conversionFunctionsCmplx1)
 				.description(
 					"Dipole field component for a combined function magnet")
 				.build());
 	Map<String, ConversionAlgorithm> conversionFunctionsCmplx2 = new HashMap<>();
 	conversionFunctionsCmplx2
 		.put("i2b",
 			new ConversionAlgorithm(
 				1,
 				"1.239146e-12*input**4 -2.242334e-09*input**3 + 1.117486e-06*input**2 + 0.007377142*input + 0.007218819"));
 	complexMap
 		.add("municonv",
 			conversionDataOfType("Complex:2")
 				.withMagnetMeasurementData(
 					magnetMeasurements().FieldUnit("T/m")
 						.CurrentUnit("A").build())
 				.withConversionFunctions(
 					conversionFunctionsCmplx2)
 				.description(
 					"Quadrupole field component for a combined function magnet")
 				.build());
 	Map<String, ConversionAlgorithm> conversionFunctionsCmplx3 = new HashMap<>();
 	conversionFunctionsCmplx2
 		.put("i2b",
 			new ConversionAlgorithm(
 				1,
 				"-7.736754e-11*input**4 + 1.078356e-07*input**3 -4.27955e-05*input**2 + 0.061426*input + 0.031784"));
 	complexMap
 		.add("municonv",
 			conversionDataOfType("Complex:3")
 				.withMagnetMeasurementData(
 					magnetMeasurements().FieldUnit("T/m^2")
 						.CurrentUnit("A").build())
 				.withConversionFunctions(
 					conversionFunctionsCmplx3)
 				.description(
 					"Sextupole field component for a combined function magnet")
 				.build());
 	Map<String, ConversionAlgorithm> conversionFunctionsStandard = new HashMap<>();
 	conversionFunctionsCmplx2
 		.put("b2i",
 			new ConversionAlgorithm(
 				1,
 				"-33.289411*input**4 + 84.116293*input**3 -61.320653*input**2 + 668.452373*input -0.969042"));
 	complexMap.add(
 		"municonv",
 		conversionDataOfType("standard")
 			.withMagnetMeasurementData(
 				magnetMeasurements().FieldUnit("T")
 					.CurrentUnit("A").SerialNumber(24)
 					.build())
 			.withConversionFunctions(conversionFunctionsStandard)
 			.withMagneticLengthDesign(1.3).build());
 	try {
 	    ObjectMapper objectMapper = new ObjectMapper();
 	    File jsonComplexMap = tempFolder.newFile("jsonComplexMap");
 	    objectMapper.writeValue(jsonComplexMap, complexMap);
 	    System.out.println(objectMapper.writeValueAsString(complexMap));
 	    MultivaluedMap<String, ConversionData> parsedComplexMap = objectMapper
 		    .readValue(
 			    jsonComplexMap,
 			    new TypeReference<MultivaluedHashMap<String, ConversionData>>() {
 			    });
 	    Assert.assertEquals(
 		    "Failed to correctly parse ConversionData ComplexMap object ",
 		    complexMap, parsedComplexMap);
 	} catch (Exception e) {
 	    Assert.fail(e.getMessage());
 	}
     }
 
     @Test
     public void testDataTypes() {
 	/**
 	 * A second option for representing the converionInfo
 	 */
 	Map<String, Map<String, ConversionData>> complexMap = new HashMap<String, Map<String, ConversionData>>();
 
 	final Map<String, ConversionAlgorithm> conversionFunctionsCmplx1 = new HashMap<>();
 	conversionFunctionsCmplx1
 		.put("i2b",
 			new ConversionAlgorithm(
 				1,
 				"2.717329e-13*input**4 -4.50853e-10*input**3 + 2.156812e-07*input**2 + 0.001495718*input + 0.0014639"));
 	final Map<String, ConversionAlgorithm> conversionFunctionsCmplx2 = new HashMap<>();
 	conversionFunctionsCmplx2
 		.put("i2b",
 			new ConversionAlgorithm(
 				1,
 				"1.239146e-12*input**4 -2.242334e-09*input**3 + 1.117486e-06*input**2 + 0.007377142*input + 0.007218819"));
 
 	final Map<String, ConversionAlgorithm> conversionFunctionsCmplx3 = new HashMap<>();
 	conversionFunctionsCmplx3
 		.put("i2b",
 			new ConversionAlgorithm(
 				1,
 				"-7.736754e-11*input**4 + 1.078356e-07*input**3 -4.27955e-05*input**2 + 0.061426*input + 0.031784"));
 
 	final Map<String, ConversionAlgorithm> conversionFunctionsStandard = new HashMap<>();
 	conversionFunctionsCmplx2
 		.put("b2i",
 			new ConversionAlgorithm(
 				1,
 				"-33.289411*input**4 + 84.116293*input**3 -61.320653*input**2 + 668.452373*input -0.969042"));
 	complexMap.put("municonv", new HashMap<String, ConversionData>() {
 	    {
 		put("Complex:1",
 			conversionDataOfType("Complex:1")
 				.withMagnetMeasurementData(
 					magnetMeasurements().FieldUnit("T")
 						.CurrentUnit("A").build())
 				.withConversionFunctions(
 					conversionFunctionsCmplx1)
 				.description(
 					"Dipole field component for a combined function magnet")
 				.build());
 		put("Complex:2",
 			conversionDataOfType("Complex:2")
 				.withMagnetMeasurementData(
 					magnetMeasurements().FieldUnit("T/m")
 						.CurrentUnit("A").build())
 				.withConversionFunctions(
 					conversionFunctionsCmplx2)
 				.description(
 					"Quadrupole field component for a combined function magnet")
 				.build());
 		put("Complex:3",
 			conversionDataOfType("Complex:3")
 				.withMagnetMeasurementData(
 					magnetMeasurements().FieldUnit("T/m^2")
 						.CurrentUnit("A").build())
 				.withConversionFunctions(
 					conversionFunctionsCmplx3)
 				.description(
 					"Sextupole field component for a combined function magnet")
 				.build());
 		put("standard",
 			conversionDataOfType("standard")
 				.withMagnetMeasurementData(
 					magnetMeasurements().FieldUnit("T")
 						.CurrentUnit("A")
 						.SerialNumber(24).build())
 				.withConversionFunctions(
 					conversionFunctionsStandard)
 				.withMagneticLengthDesign(1.3).build());
 	    }
 	});
 	try {
 	    ObjectMapper objectMapper = new ObjectMapper();
 	    File jsonComplexMap = tempFolder.newFile("jsonComplexMap");
 	    objectMapper.writeValue(jsonComplexMap, complexMap);
 	    System.out.println(objectMapper.writeValueAsString(complexMap));
 	    Map<String, Map<String, ConversionData>> parsedComplexMap = objectMapper
 		    .readValue(
 			    jsonComplexMap,
 			    new TypeReference<HashMap<String, HashMap<String, ConversionData>>>() {
 			    });
 	    Assert.assertEquals(
 		    "Failed to correctly parse ConversionData ComplexMap object ",
 		    complexMap, parsedComplexMap);
 	} catch (Exception e) {
 	    Assert.fail(e.getMessage());
 	}
     }
 
     @Test
     public void testResult() {
 
     }
 }
