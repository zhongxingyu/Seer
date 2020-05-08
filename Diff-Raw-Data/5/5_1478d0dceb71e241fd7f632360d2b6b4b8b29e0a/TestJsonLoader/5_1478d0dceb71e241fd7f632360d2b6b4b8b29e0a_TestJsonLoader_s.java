 package fi.dratini.keikkalista.test.utils;
 
 import fi.dratini.keikkalista.core.integration.IJsonLoader;
 import fi.dratini.keikkalista.core.integration.PeriodValues;
 
 public class TestJsonLoader implements IJsonLoader {
 
    @Override
     public String GetEvents(int count) {
         return TestUtils.readFile("res\\test\\getEvents.json");
     }
 
    @Override
     public String GetTopArtists(String username, PeriodValues period, int count) {
         return TestUtils.readFile("res\\test\\getTopArtists.json");
     }
 
    @Override
     public String GetLovedTracks(String username, int count) {
         return TestUtils.readFile("res\\test\\getLovedTracks.json");
     }
 }
