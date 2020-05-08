 package com.HuskySoft.metrobike.backend.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.HuskySoft.metrobike.backend.DirectionsRequest;
 import com.HuskySoft.metrobike.backend.Leg;
 import com.HuskySoft.metrobike.backend.Location;
 import com.HuskySoft.metrobike.backend.Route;
 import com.HuskySoft.metrobike.backend.Step;
 import com.HuskySoft.metrobike.backend.TransitDetails;
 import com.HuskySoft.metrobike.backend.WebRequestJSONKeys;
 
 /**
  * This class is responsible for testing the TransitDetails class.
  * 
  * @author Adrian Laurenzi
  * 
  */
 public class TransitDetailsTest extends TestCase {
     /**
      * To keep checkstyle happy.
      */
     private static final int EXPECTED_NUMBER_OF_STOPS = 10;
 
     /**
      * To keep checkstyle happy.
      */
     private static final double SEATTLE_LAT_2 = 47.6814690;
 
     /**
      * To keep checkstyle happy.
      */
     private static final double SEATTLE_LONG_2 = -122.3273540;
 
     /**
      * To keep checkstyle happy.
      */
     private static final double SEATTLE_LAT = 47.66134640;
 
     /**
      * To keep checkstyle happy.
      */
     private static final double SEATTLE_LONGITUDE = -122.312080;
 
     /**
      * This is a TransitDetails variable that is used for testing this class.
      */
     private TransitDetails transitDetails = null;
 
     /**
      * This initializes the transitDetails variable for each test.
      * 
      * @throws JSONException
      *             When something goes wrong.
      * 
      */
     public final void setUp() throws JSONException {
         JSONObject transitJSON = new JSONObject(dummyTransitJSON);
         JSONArray routesArray = transitJSON.getJSONArray(WebRequestJSONKeys.ROUTES.getLowerCase());
         Route route = Route.buildRouteFromJSON(routesArray.getJSONObject(0));
         List<Leg> legs = route.getLegList();
         List<Step> steps = legs.get(0).getStepList();
         transitDetails = steps.get(1).getTransitDetails();
     }
 
     /**
      * WhiteBox: This tests the getArrivalStop method.
      * 
      * @throws JSONException
      *             When something goes wrong.
      */
     public final void test_getArrivalStop() throws JSONException {
         setUp();
         Location expected = new Location(SEATTLE_LAT, SEATTLE_LONGITUDE);
         Location actual = transitDetails.getArrivalStop();
         Assert.assertEquals("Actual value for transitDetails.getArrivalStop() was: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getDepartureStop method.
      * 
      * @throws JSONException
      *             If something goes wrong.
      */
     public final void test_getDepartureStop() throws JSONException {
         setUp();
         Location expected = new Location(SEATTLE_LAT_2, SEATTLE_LONG_2);
         Location actual = transitDetails.getDepartureStop();
         Assert.assertEquals("Actual value for transitDetails.getDepartureStop() was: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getArrivalTime method.
      * 
      * @throws JSONException
      *             If something goes wrong.
      */
     public final void test_getArrivalTime() throws JSONException {
         setUp();
         String expected = "13:58";
         String actual = transitDetails.getArrivalTime();
         Assert.assertEquals("Actual value for transitDetails.getArrivalTime() was: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getDepartureTime method.
      * 
      * @throws JSONException
      *             If something goes wrong.
      */
     public final void test_getDepartureTime() throws JSONException {
         setUp();
         String expected = "13:48";
         String actual = transitDetails.getDepartureTime();
         Assert.assertEquals("Actual value for transitDetails.getDepartureTime() was: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getAgencyName method.
      * 
      * @throws JSONException
      *             If something goes wrong.
      */
     public final void test_getAgencyName() throws JSONException {
         setUp();
         String expected = "Metro Transit";
         String actual = transitDetails.getAgencyName();
         Assert.assertEquals("Actual value for transitDetails.getAgencyName() was: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getHeadsign method.
      * 
      * @throws JSONException
      *             If something goes wrong.
      */
     public final void test_getHeadsign() throws JSONException {
         setUp();
         String expected = "Mount Baker Transit Center, University District";
         String actual = transitDetails.getHeadsign();
         Assert.assertEquals("Actual value for transitDetails.getHeadsign() was: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getLineShortName method.
      * 
      * @throws JSONException
      *             If something goes wrong.
      */
     public final void test_getLineShortName() throws JSONException {
         setUp();
         String expected = "48";
         String actual = transitDetails.getLineShortName();
         Assert.assertEquals("Actual value for transitDetails.getLineShortName() was: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getLineShortName method.
      * 
      * @throws JSONException
      *             If something goes wrong.
      */
     public final void test_getVehicleType() throws JSONException {
         setUp();
         String expected = "BUS";
         String actual = transitDetails.getVehicleType();
         Assert.assertEquals("Actual value for transitDetails.getVehicleType() was: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getVehicleIconURL method.
      * 
      * @throws JSONException
      *             If something goes wrong.
      */
     public final void test_getVehicleIconURL() throws JSONException {
         setUp();
         String expected = "//maps.gstatic.com/mapfiles/transit/iw/6/bus.png";
         String actual = transitDetails.getVehicleIconURL();
         Assert.assertEquals("Actual value for transitDetails.getVehicleIconURL() was: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getLineShortName method.
      * 
      * @throws JSONException
      *             If something goes wrong.
      */
     public final void test_getNumStops() throws JSONException {
         setUp();
         int expected = EXPECTED_NUMBER_OF_STOPS;
         int actual = transitDetails.getNumStops();
         Assert.assertEquals("Actual value for transitDetails.getNumStops() was: " + actual,
                 expected, actual);
     }
 
     /**
      * BlackBox: Tests to be sure we can safely serialize and deserialize a
      * TransitDetails object. This functionality is used in the intent-passing
      * system.
      * 
      * @throws IOException
      *             if an IO exception occurs during processing
      * @throws ClassNotFoundException
      *             if a class cannot be found
      * @throws JSONException
      *             If something goes wrong.
      */
     public void testSerializationTestEmptyDRObject() throws IOException, ClassNotFoundException,
             JSONException {
         transitDetails = null;
 
         // Serialize the empty TD, then de-serialize it
         byte[] theBytes = helpSerialize(transitDetails);
         TransitDetails recreatedTD = helpDeserialize(theBytes);
 
         // Use string equality to check the request
         Assert.assertEquals("The toString() representation of a serialized->deserialized"
                + " object should remain unchanged.", transitDetails,
                recreatedTD);
     }
 
     /**
      * BlackBox: Tests to be sure we can safely serialize and deserialize a
      * non-empty TransitDetails object. This functionality is used in the
      * intent-passing system.
      * 
      * @throws IOException
      *             if an IO exception occurs during processing
      * @throws ClassNotFoundException
      *             if a class cannot be found
      * @throws JSONException
      *             If something goes wrong.
      */
     public void testSerializationTestNonEmptyDRObject() throws IOException, ClassNotFoundException,
             JSONException {
         setUp();
 
         // Serialize the empty TD, then de-serialize it
         byte[] theBytes = helpSerialize(transitDetails);
         TransitDetails recreatedTD = helpDeserialize(theBytes);
 
         // Use string equality to check the TD
         Assert.assertEquals("The toString() representation of a serialized->deserialized"
                 + " object should remain unchanged.", transitDetails.toString(),
                 recreatedTD.toString());
     }
 
     /**
      * Helper function for serializing a TransitDetails object. Help on testing
      * this based on:
      * http://www.ibm.com/developerworks/library/j-serialtest/index.html
      * 
      * @param toSerialize
      *            the DirectionsRequest to serialize
      * @return a byte array representing the serialized DirectionsRequest
      * @throws IOException
      *             if an IO exception occurs during processing
      */
     private byte[] helpSerialize(final TransitDetails toSerialize) throws IOException {
         ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
         ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
         objectOut.writeObject(toSerialize);
         objectOut.close();
         return byteOut.toByteArray();
     }
 
     /**
      * Helper function for serializing a DirectionsRequest object.Help on
      * testing this based on:
      * http://www.ibm.com/developerworks/library/j-serialtest/index.html
      * 
      * @param toDeSerialize
      *            a byte array representing the serialized DirectionsRequest
      * @return the deserialized DirectionsRequest
      * @throws IOException
      *             if an IO exception occurs during processing
      * @throws ClassNotFoundException
      *             if a class cannot be found
      */
     private TransitDetails helpDeserialize(final byte[] toDeSerialize)
             throws ClassNotFoundException, IOException {
         ByteArrayInputStream byteIn = new ByteArrayInputStream(toDeSerialize);
         ObjectInputStream objectIn = new ObjectInputStream(byteIn);
         return (TransitDetails) objectIn.readObject();
     }
 
     /**
      * Dummy JSON transit directions.
      */
     private final String dummyTransitJSON = "{   \"routes\" : [    "
             + "  {         \"bounds\" : {            \"northeast\" : { "
             + "              \"lat\" : 47.68152000000001,              "
             + " \"lng\" : -122.311880            },            "
             + "\"southwest\" : {               \"lat\" : 47.66135000000001,"
             + "               \"lng\" : -122.327360            }         }, "
             + "        \"copyrights\" : \"Map data ©2013 Google\",        "
             + " \"legs\" : [            {               \"arrival_time\" : { "
             + "                 \"text\" : \"13:58\",               "
             + "   \"time_zone\" : \"America/Los_Angeles\",           "
             + "       \"value\" : 1368997138               },         "
             + "      \"departure_time\" : {                 "
             + " \"text\" : \"13:47\",                  \"time_zone\" : "
             + "\"America/Los_Angeles\",                  \"value\" :"
             + " 1368996430               },               \"distance\""
             + " : {                  \"text\" : \"2.0 mi\",          "
             + "        \"value\" : 3142               },              "
             + " \"duration\" : {                 "
             + " \"text\" : \"11 mins\",                  \"value\" : 639 "
             + "              },               "
             + "\"end_address\" : \"1415 Northeast 45th Street, "
             + "University of Washington, Seattle, WA 98105, USA\",   "
             + "           \"end_location\" : {                "
             + "  \"lat\" : 47.66135000000001,                "
             + "  \"lng\" : -122.312080               },               "
             + "\"start_address\" : \"East Green LK Dr N & Latona Ave NE, "
             + "Seattle, WA 98115, USA\",               \"start_location\" : { "
             + "                 \"lat\" : 47.681470,                  "
             + "\"lng\" : -122.327360               },              "
             + " \"steps\" : [                  {                     "
             + "\"distance\" : {                        \"text\" : "
             + "\"1 ft\",                        \"value\" : 0        "
             + "             },                     \"duration\" : {     "
             + "                   \"text\" : \"1 min\",                 "
             + "       \"value\" : 0                     },                "
             + "     \"end_location\" : {                        \"lat\" :"
             + " 47.681470,                        \"lng\" : -122.327350    "
             + "                 },                     \"html_instructions\" "
             + ": \"Walk to East Green LK Dr N & Latona Ave NE\",       "
             + "              \"polyline\" : {                       "
             + " \"points\" : \"ex_bH~`siV?A\"                     },     "
             + "                \"start_location\" : {                    "
             + "    \"lat\" : 47.681470,                        \"lng\" "
             + ": -122.327360                     },                    "
             + " \"steps\" : [                        {                  "
             + "         \"distance\" : {                              "
             + "\"text\" : \"1 ft\",                             "
             + " \"value\" : 0                           },            "
             + "               \"duration\" : {                          "
             + "    \"text\" : \"1 min\",                            "
             + "  \"value\" : 0                           },           "
             + "                \"end_location\" : {                    "
             + "          \"lat\" : 47.681470,                        "
             + "      \"lng\" : -122.327350                          "
             + " },                           \"html_instructions\" : "
             + "\"Walk \u003cb\u003enorth-west\u003c/b\u003e\",        "
             + "                   \"polyline\" : {                   "
             + "           \"points\" : \"ex_bH~`siV?A\"              "
             + "             },                           \"start_location\""
             + " : {                              \"lat\" : 47.681470, "
             + "                             \"lng\" : -122.327360  "
             + "                         },                           "
             + "\"travel_mode\" : \"WALKING\"                        "
             + "}                     ],                    "
             + " \"travel_mode\" : \"WALKING\"                  },    "
             + "              {                     \"distance\" : {   "
             + "                     \"text\" : \"2.0 mi\",             "
             + "           \"value\" : 3142                     },       "
             + "              \"duration\" : {                        \"text\""
             + " : \"11 mins\",                        \"value\" : 639  "
             + "                   },                     \"end_location\" : "
             + "{                        \"lat\" : 47.66135000000001,    "
             + "                    \"lng\" : -122.312080                 "
             + "    },                     \"html_instructions\" : \"Bus"
             + " towards Mount Baker Transit Center, University District\","
             + "                     \"polyline\" : {                      "
             + "  \"points\" : \"ex_bH|`siVIMjAwBZo@x@}@l@e@b@Uf@QXA\\CR?VJ^]"
             + "fC}Br@m@zDiDXYtHaH@_B?y@?g@?K@Y?W@eA?o@?C?Q?y@@mC?_C@iC?_B?a@"
             + "@sI@{@?{A?oB@yB@yB?uB?}A?Y~@?t@?tACJ?fA?hA?p@?nHCT?T?r@?D?b@?"
             + "~@?v@?V?JA`G?tDAB?J?tKFX?xGBhA?~GDfGDpAA?P\"                  "
             + "   },                     \"start_location\" : {              "
             + "          \"lat\" : 47.681470,                        \"lng\""
             + " : -122.327350                     },                   "
             + "  \"transit_details\" : {                       "
             + " \"arrival_stop\" : {                           \"location\""
             + " : {                              \"lat\" : 47.66134640, "
             + "                             \"lng\" : -122.312080        "
             + "                   },                           \"name\" :"
             + " \"15th Ave NE & NE 45th St\"                        },   "
             + "                     \"arrival_time\" : {                  "
             + "         \"text\" : \"13:58\",                         "
             + "  \"time_zone\" : \"America/Los_Angeles\",                  "
             + "         \"value\" : 1368997138                        },    "
             + "                    \"departure_stop\" : {                  "
             + "         \"location\" : {                             "
             + " \"lat\" : 47.6814690,                             "
             + " \"lng\" : -122.3273540                           },     "
             + "                      \"name\" : \"East Green LK Dr N & "
             + "Latona Ave NE\"                        },                  "
             + "      \"departure_time\" : {                          "
             + " \"text\" : \"13:48\",                           \"time_zone\""
             + " : \"America/Los_Angeles\",                         "
             + "  \"value\" : 1368996499                        },      "
             + "                  \"headsign\" : \"Mount Baker Transit Center,"
             + " University District\",                        \"line\" : {  "
             + "                         \"agencies\" : [                     "
             + "         {                                 \"name\" :"
             + " \"Metro Transit\",                                "
             + " \"phone\" : \"(206) 553-3000\",                    "
             + "             \"url\" : \"http://metro.kingcounty.gov/\"  "
             + "                            }                      "
             + "    ],                           \"short_name\" : \"48\","
             + "                           \"url\" : "
             + "\"http://metro.kingcounty.gov/tops/bus/schedules/s048_0_.html\","
             + "                           \"vehicle\" : {  "
             + "                            \"icon\" : "
             + "\"//maps.gstatic.com/mapfiles/transit/iw/6/bus.png\","
             + "                              \"name\" : \"Bus\",        "
             + "                      \"type\" : \"BUS\"               "
             + "            }                        },                  "
             + "      \"num_stops\" : 10                     },         "
             + "            \"travel_mode\" : \"TRANSIT\"                "
             + "  }               ],               \"via_waypoint\" : [] "
             + "           }         ],         \"overview_polyline\" : {"
             + "            \"points\" : \"ex_bH~`siVIOfBgDx@}@l@e@jAg@v@E"
             + "R?VJfD{CnFwEnI{H@yC@eBD}QFq]?sE?Y~@?jCCrA?`MCrB?zCAfMArWJ"
             + "fPJpAA?P\"         },         \"warnings\" : [            "
             + "\"Walking directions are in beta.    Use caution – This route "
             + "may be missing sidewalks or pedestrian paths.\"         ],  "
             + "       \"waypoint_order\" : []      }   ],   \"status\" : \"OK\"}";
 
 }
