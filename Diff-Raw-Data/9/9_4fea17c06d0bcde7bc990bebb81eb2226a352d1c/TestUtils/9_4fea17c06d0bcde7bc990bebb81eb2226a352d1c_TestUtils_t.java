 package com.qeevee.gq.tests;
 
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.net.URL;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.io.SAXReader;
 
 import android.content.Intent;
 
 import com.xtremelabs.robolectric.Robolectric;
 
 import edu.bonn.mobilegaming.geoquest.GameLoader;
 import edu.bonn.mobilegaming.geoquest.GeoQuestActivity;
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.Start;
 import edu.bonn.mobilegaming.geoquest.mission.Mission;
 
 public class TestUtils {
 
 	public static Document loadTestGame(String gameName) {
 		Document document = null;
 		SAXReader reader = new SAXReader();
 		try {
 			File gameFile = getGameFile(gameName);
 			document = reader.read(gameFile);
 			GeoQuestApp.setRunningGameDir(new File(gameFile.getParent()));
 		} catch (DocumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return document;
 	}
 
 	public static File getGameFile(String gameName) {
 		URL xmlFileURL = TestUtils.class.getResource("/testgames/" + gameName
 				+ "/game.xml");
 		if (xmlFileURL == null)
 			fail("Resource file not found for game: " + gameName);
 		return new File(xmlFileURL.getFile());
 	}
 
 	/**
 	 * Sets up the test for a single mission type using a prepared game file
 	 * (game.xml) which must be stored in "testgames" directory and named
 	 * "<missionName>Test".
 	 * 
 	 * @param missionType
 	 *            must be a valid mission type for which a class exists in the
 	 *            mission implementation package.
 	 * @param missionID
 	 * @return a new Object of the according type for the given mission type
 	 *         name.
 	 * @throws ClassNotFoundException
 	 */
 	public static GeoQuestActivity setUpMissionTypeTest(String missionType,
 			String missionID) throws ClassNotFoundException {
 		return setUpMissionTypeTest(missionType + "Test", missionType,
 				missionID);
 	}
 
 	/**
 	 * Sets up the test for a single mission type using a prepared game file
 	 * (game.xml) which must be stored in "testgames" directory.
 	 * 
 	 * @param gameFileName
 	 *            the filename of the xml game specification used for this test.
 	 * @param missionType
 	 *            must be a valid mission type for which a class exists in the
 	 *            mission implementation package.
 	 * @param missionID
 	 * @return a new Object of the according type for the given mission type
 	 *         name.
 	 * @throws ClassNotFoundException
 	 */
 	public static GeoQuestActivity setUpMissionTypeTest(String gameFileName,
 			String missionType, String missionID) throws ClassNotFoundException {
 		Class<?> missionClass = Class.forName(Mission.getPackageBaseName()
 				+ missionType);
 		GeoQuestActivity missionActivity = null;
 		try {
 			missionActivity = (GeoQuestActivity) missionClass.newInstance();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Start start = new Start();
 		GeoQuestApp app = (GeoQuestApp) start.getApplication();
 		app.onCreate();
 		Mission.setMainActivity(start);
 		GameLoader.startGame(null, TestUtils.getGameFile(gameFileName));
 		Intent startMCQIntent = new Intent(start, missionClass);
 		startMCQIntent.putExtra("missionID", missionID);
 		Robolectric.shadowOf(missionActivity).setIntent(startMCQIntent);
 		return missionActivity;
 	}
 
 	/**
 	 * Lets you access the values of private or protected fields in your tests.
 	 * You will have to cast the resulting object down to the real type.
 	 * 
 	 * @param obj
 	 * @param fieldName
 	 * @return
 	 */
 	public static Object getFieldValue(Object obj, String fieldName) {
 		Object value = null;
 		try {
 			Field f = obj.getClass().getDeclaredField(fieldName);
 			f.setAccessible(true);
			value = f.get(obj);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} catch (NoSuchFieldException e) {
			fail("Implementation of type \"" + obj.getClass().getSimpleName()
					+ "\" misses a field named \"" + fieldName + "\"");
 		}
 		return value;
 	}
 
 }
