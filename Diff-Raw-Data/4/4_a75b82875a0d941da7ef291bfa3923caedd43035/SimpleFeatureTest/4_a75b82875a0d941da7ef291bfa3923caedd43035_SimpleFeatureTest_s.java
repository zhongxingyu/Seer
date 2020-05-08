 package com.bennight.serializers;
 
 import java.io.File;
 import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.geotools.data.DataUtilities;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import com.bennight.ShapefileReader;
 
 public class SimpleFeatureTest extends AbstractSerializer {
 
 
 	
 	@Override
 	public List<byte[]> Serialize(List<SimpleFeature> features) {
 		
 		StringBuilder sb = new StringBuilder();
 		for (SimpleFeature f : features){
			//sb.append(DataUtilities.encodeFeature(f));
			sb.append("c_03de13.1=MULTIPOLYGON (((-171.04048645299997 -11.082452137999951, -171.03939819299998 -11.083986281999955, -171.04257634399997 -11.08686948299993, -171.04260253899997 -11.086890220999976, -171.04408264199998 -11.087923049999972, -171.04589843799997 -11.089185714999928, -171.04949855299998 -11.088085467999974, -171.05088806199998 -11.086385726999936, -171.05091240799996 -11.086359097999946, -171.05190946399998 -11.08454626899993, -171.05198669399996 -11.084383964999972, -171.05068969699997 -11.081686973999979, -171.04788002399997 -11.080834331999938, -171.04739379899996 -11.080690383999979, -171.04675375999997 -11.080580699999928, -171.04499816899997 -11.080288886999938, -171.04491414599997 -11.080302390999975, -171.04159602699997 -11.080888646999938, -171.04048645299997 -11.082452137999951), (-171.04824829099996 -11.084536551999975, -171.04669189499998 -11.087289809999959, -171.04508972199997 -11.084585189999927, -171.04525756799998 -11.084311484999944, -171.04551696799996 -11.083907126999975, -171.04577636699997 -11.083460807999927, -171.04620361299996 -11.082786559999931, -171.04663085899998 -11.083086966999929, -171.047134399 -11.08342552199997, -171.04840087899998 -11.08428478199994, -171.04824829099996 -11.084536551999975)))|1|AS|PPG|Swains Island|60040|S||-171.045984785|-11.0843655158|0.0440795005116|6.71191815609E-5");
 			//System.out.println(sb.toString());
 			sb.append("\r\n");
 		}
 		List<byte[]> serializedData = new ArrayList<byte[]>();
 		serializedData.add(sb.toString().getBytes(StandardCharsets.UTF_8));
 		return serializedData;
 		
 	}
 
 	@Override
 	public void Deserialize(List<byte[]> serializedData) {
 		
 		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
 		for (String line : (new String(serializedData.get(0), StandardCharsets.UTF_8).split("\r\n"))){
 			features.add(DataUtilities.createFeature(ShapefileReader.FeatureType, line));
 		}
 		//System.out.println(features.size());
 	}
 
 	public String GetSerializerName() {
 		return "GeoTools Simple Feature";
 	}
 
 }
