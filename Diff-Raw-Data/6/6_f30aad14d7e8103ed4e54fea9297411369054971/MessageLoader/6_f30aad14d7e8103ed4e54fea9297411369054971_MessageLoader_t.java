 package edu.umd.cs.linqs.embers;
 
 import java.io.BufferedReader;
import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.umd.cs.linqs.embers.json.Entity;
 import edu.umd.cs.psl.database.Database;
 import edu.umd.cs.psl.model.argument.GroundTerm;
 import edu.umd.cs.psl.model.argument.IntegerAttribute;
 import edu.umd.cs.psl.model.argument.StringAttribute;
 import edu.umd.cs.psl.model.atom.RandomVariableAtom;
 import edu.umd.cs.psl.model.predicate.Predicate;
 
 
 public class MessageLoader {
 
 	private JSONObject json;
 	private List<Entity> entities;
 	private String embersId;
 	private String language;
 
 	public MessageLoader(String filename) {
 		BufferedReader reader;
 		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));
 
 			String line = reader.readLine();
 			json = new JSONObject(line);
 			
 			JSONArray entitiesArray = json.getJSONObject("BasisEnrichment").getJSONArray("entities");
 			
 			embersId = json.getString("embersId");
 			language = json.getJSONObject("BasisEnrichment").getString("language");
 			
 			
 			entities = new ArrayList<Entity>(entitiesArray.length());
 			
 			for (int i = 0 ; i < entitiesArray.length(); i++) {
 				entities.add(i, new Entity(entitiesArray.getJSONObject(i)));
 			}
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void insertAllEntities(Database db, Predicate entity) {
 		for (Entity e : entities) {
 			GroundTerm [] arguments = new GroundTerm[4];
 			arguments[0] = db.getUniqueID(embersId);
 			arguments[1] = new StringAttribute(e.getExpr());
 			arguments[2] = new StringAttribute(e.getNeType());
 			arguments[3] = new IntegerAttribute(e.getOffset());
 			RandomVariableAtom atom = (RandomVariableAtom) db.getAtom(entity, arguments);
 			atom.setValue(1.0);
 			db.commit(atom);
 		}
 	}
 	
 	public void insertWrittenIn(Database db, Predicate writtenIn) {
 		GroundTerm [] arguments = new GroundTerm[2];
 		arguments[0] = db.getUniqueID(embersId);
 		arguments[1] = new StringAttribute(language);
 		RandomVariableAtom atom = (RandomVariableAtom) db.getAtom(writtenIn, arguments);
 		atom.setValue(1.0);
 		db.commit(atom);
 	}
 	
 	public static void main(String [] args) {
 
 		MessageLoader ml = new MessageLoader("messages/b72bd8382210aff316127f05ebbac3dae78c96c3");
 		
 
 	}
 
 }
