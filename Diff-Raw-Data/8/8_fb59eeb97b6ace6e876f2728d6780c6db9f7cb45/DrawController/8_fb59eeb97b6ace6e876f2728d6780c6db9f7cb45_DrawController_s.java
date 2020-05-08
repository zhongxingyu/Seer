 package de.redlion.civilwar.controls;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputAdapter;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Polygon;
 import com.badlogic.gdx.math.Quaternion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.Ray;
 
 import de.redlion.civilwar.Constants;
 import de.redlion.civilwar.GameSession;
 import de.redlion.civilwar.SinglePlayerGameScreen;
 import de.redlion.civilwar.units.EnemySoldier;
 import de.redlion.civilwar.units.PlayerSoldier;
 import de.redlion.civilwar.units.Soldier;
 
 public class DrawController extends InputAdapter{
 	
 	final OrthographicCamera camera;
 	final Vector3 curr = new Vector3();
 	final Vector2 last = new Vector2(0, 0);
 	final Vector2 delta = new Vector2();
 	
 	final Vector2 lastPoint = new Vector2();
 	
 //	final Dollar dollar = new Dollar(4);
 //	final DollarListener listener;
 //	final double MINSCORE = 0.82;
 	final float MAX_DISTANCE = 45.0f;
 	
 	ArrayList<Vector3> deletePath = new ArrayList<Vector3>();
 	
 	Ray picker;
 
 	public DrawController (final OrthographicCamera camera) {
 		this.camera = camera;
 	}
 
 	@Override
 	public boolean touchDragged (int x, int y, int pointer) {
 		
 		if(SinglePlayerGameScreen.paused && Gdx.input.isButtonPressed(Input.Buttons.RIGHT) ) {
 			delta.set(x, y).sub(last);
 			delta.mul(0.01f * Constants.MOVESPEED);
 			Vector3 temp = new Vector3(delta.y, 0, -delta.x);
 			Quaternion rotation = new Quaternion();
 			camera.combined.getRotation(rotation);
 			rotation.transform(temp);
 			camera.translate(temp);
 			camera.update();
 			last.set(x, y);
 			
 			updateDoodles();
 			
 			
 		}
 		else if(!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)){
 			Vector2 temp = new Vector2(x, y);
 
 			
 			// convert to screen space
 			y = -y + Gdx.graphics.getHeight();			
 			x = Math.max(Math.min(x, Gdx.graphics.getWidth()), 0);
 			y = Math.max(Math.min(y, Gdx.graphics.getHeight()), 0);
 			
 			temp = new Vector2(x, y);
 			
 			
 //			if(temp.dst(lastPoint) > 10 || SinglePlayerGameScreen.currentDoodleCollisionPoints.isEmpty()) {
 //				SinglePlayerGameScreen.currentDoodleCollisionPoints.add(temp);
 //				lastPoint.set(temp);
 //			}
 			
 			if(temp.dst(lastPoint) > 10 || SinglePlayerGameScreen.currentDoodle.isEmpty()) {
 				SinglePlayerGameScreen.currentDoodle.add(temp);
 				lastPoint.set(temp);
 			}
 		}
 		else if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
			y = -y + Gdx.graphics.getHeight();
 			
 			x = Math.max(Math.min(x, Gdx.graphics.getWidth()), 0);
 			y = Math.max(Math.min(y, Gdx.graphics.getHeight()), 0);
 			
 			Vector2 temp = new Vector2(x, y);
 			
 			Vector3 projected = new Vector3();
 			
 			picker = camera.getPickRay(temp.x, temp.y);
 			
 			Intersector.intersectRayTriangles(picker, SinglePlayerGameScreen.renderMap.heightMap.map, projected);
 			
 			deletePath.add(projected);
 			lastPoint.set(temp);
 		}
 		
 		return true;
 	}
 
 	@Override
 	public boolean touchUp (int x, int y, int pointer, int button) {
 		last.set(0, 0);
 		
 		if(SinglePlayerGameScreen.paused && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
 
 			if(SinglePlayerGameScreen.currentDoodle.size()%2 != 0)
 				SinglePlayerGameScreen.currentDoodle.add(SinglePlayerGameScreen.currentDoodle.get(SinglePlayerGameScreen.currentDoodle.size() - 1));
 			
 			ArrayList<Vector3> tempList = new ArrayList<Vector3>();
 			
 			for(Vector2 temp : SinglePlayerGameScreen.currentDoodle) {
 			
 				Vector3 projected = new Vector3();
 	
				picker = camera.getPickRay(temp.x, temp.y);
 				
 				Intersector.intersectRayTriangles(picker, SinglePlayerGameScreen.renderMap.heightMap.map, projected);
 				
 				tempList.add(projected);
 			}
 			
 			if(SinglePlayerGameScreen.currentDoodle.size() > 11) {
 				if(SinglePlayerGameScreen.circles.isEmpty() && circleTest((ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone())) {
 					
 					float[] points = new float[tempList.size() * 2 + 2];
 					
 					int i = 0;
 					for(Vector3 v : tempList) {
 					
 						points[i] = v.x;
 						points[i+1] = v.z;
 						
 						i+=2;
 						
 					}
 					
 					points[i] = tempList.get(0).x;
 					points[i+1] = tempList.get(0).z;
 					
 					
 					Polygon poly = new Polygon(points);
 					
 					ArrayList<PlayerSoldier> so = soldierTest(poly);
 					
 					if(!so.isEmpty()) {
 						SinglePlayerGameScreen.doodles.put(poly, (ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone());
 						SinglePlayerGameScreen.circles.put(poly, so);
 						
 						Gdx.app.log("POLYGON ADDED: ", "Polygon Number: " + SinglePlayerGameScreen.circles.size() + " with id " + poly.toString());
 					}
 					else
 						SinglePlayerGameScreen.currentDoodle.clear();
 				}
 				else if(!SinglePlayerGameScreen.circles.isEmpty()) {
 					
 					if(circleTest((ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone())) {
 					
 						float[] points = new float[tempList.size() * 2 + 2];
 						
 						int i = 0;
 						for(Vector3 v : tempList) {
 						
 							points[i] = v.x;
 							points[i+1] = v.z;
 							
 							i+=2;
 							
 						}
 						
 						points[i] = tempList.get(0).x;
 						points[i+1] = tempList.get(0).z;
 						
 						Polygon poly = new Polygon(points);
 						
 						boolean disjoint = checkDisjoint(poly);
 						
 						if(disjoint) {
 							ArrayList<PlayerSoldier> so = soldierTest(poly);
 							if(!so.isEmpty()) {
 								SinglePlayerGameScreen.doodles.put(poly, (ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone());
 								SinglePlayerGameScreen.circles.put(poly, so);
 								
 								Gdx.app.log("POLYGON ADDED: ", "Polygon Number: " + SinglePlayerGameScreen.circles.size() + " with id " + poly.toString());
 							}
 							else
 								SinglePlayerGameScreen.currentDoodle.clear();
 						}
 						else
 							SinglePlayerGameScreen.currentDoodle.clear();
 					}
 					else {
 						boolean deletedoodle = true;
 						for(Polygon p : SinglePlayerGameScreen.circles.keySet()) {
 							if(p.contains(tempList.get(0).x, tempList.get(0).z) && SinglePlayerGameScreen.paths.get(p) == null) {
 								
 								if(SinglePlayerGameScreen.circleHasPath.indexOf(p) == -1) {
 									//workaround because same polygon can't be in doodles twice
 									
 									Polygon pCopy = new Polygon(p.getTransformedVertices());
 									SinglePlayerGameScreen.circles.put(pCopy, SinglePlayerGameScreen.circles.get(p));
 									SinglePlayerGameScreen.doodles.put(pCopy,(ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone());
 									SinglePlayerGameScreen.paths.put(pCopy, (ArrayList<Vector3>) tempList.clone());			
 									SinglePlayerGameScreen.circleHasPath.add(p);
 									
 									Gdx.app.log("PATH ADDED: ", "Path Number: " + SinglePlayerGameScreen.paths.size() + " from Polygon " + p.toString());
 									Gdx.app.log("","" + SinglePlayerGameScreen.currentDoodle.get(0));
 									deletedoodle = false;
 									break;
 								}
 							}
 						}
 						if(deletedoodle) {
 //							SinglePlayerGameScreen.currentDoodleCollisionPoints.clear();
 							SinglePlayerGameScreen.currentDoodle.clear();
 						}
 					}
 					
 				}
 				else {
 //					SinglePlayerGameScreen.currentDoodleCollisionPoints.clear();
 					SinglePlayerGameScreen.currentDoodle.clear();
 				}
 			}
 			else {
 //				SinglePlayerGameScreen.currentDoodleCollisionPoints.clear();
 				SinglePlayerGameScreen.currentDoodle.clear();
 			}
 			
 			tempList.clear();
 		}
 		else if(SinglePlayerGameScreen.paused && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {			
 				
 			
 			ArrayList<Polygon> toDelete = new ArrayList<Polygon>();
 			ArrayList<Polygon> pathsToDelete = new ArrayList<Polygon>();
 			
 			//deletePath must start and end outside polygon but must contain at least one point inside polygon
 			boolean checkPoly = false;
 				
 			if(!deletePath.isEmpty()) {
 				
 				for(Polygon pol : SinglePlayerGameScreen.circles.keySet()) {
 					
 					if(!pol.contains(deletePath.get(0).x, deletePath.get(0).z) && !pol.contains(deletePath.get(deletePath.size()-1).x, deletePath.get(deletePath.size()-1).z))
 						checkPoly = true;
 					
 					if(checkPoly) {
 						for(Vector3 vec : deletePath) {
 							
 							if(pol.contains(vec.x, vec.z) && deletePath.indexOf(vec) != 0 && deletePath.indexOf(vec) != deletePath.size() -1) {
 								toDelete.add(pol);
 								Gdx.app.log("", "Circle deleted :(");
 								break;
 							}
 							
 						}
 					}
 					
 				}
 				
 				for(Polygon polyg : SinglePlayerGameScreen.paths.keySet()) {
 					
 					ArrayList<Vector3> trail = SinglePlayerGameScreen.paths.get(polyg);	
 					if(!trail.isEmpty()) {
 						
 						Vector2 start = new Vector2(deletePath.get(0).x,deletePath.get(0).z);
 						Vector2 end = new Vector2(deletePath.get(deletePath.size()-1).x,deletePath.get(deletePath.size()-1).z);
 						
 						for(int i=0; i<trail.size();i+=2) {
 							
 							Vector2 point1 = new Vector2(trail.get(i).x,trail.get(i).z);
 							Vector2 point2 = new Vector2(trail.get(i+1).x,trail.get(i+1).z);
 							
 							if(Intersector.intersectSegments(point1, point2, start, end, new Vector2()))
 								pathsToDelete.add(polyg);
 							
 						}						
 					}					
 				}
 				
 			}
 			
 			if(!toDelete.isEmpty()) {
 				for(Polygon pop : toDelete) {
 
 					for(PlayerSoldier a : SinglePlayerGameScreen.circles.remove(pop)) {
 						a.circled  = false;
 					}
 					SinglePlayerGameScreen.doodles.remove(pop);
 					SinglePlayerGameScreen.paths.remove(pop);
 				}
 			}
 			if(!pathsToDelete.isEmpty()) {
 				for(Polygon pop : pathsToDelete) {
 					SinglePlayerGameScreen.doodles.remove(pop);
 					SinglePlayerGameScreen.paths.remove(pop);
 				}
 			}
 			
 				
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public boolean touchDown (int x, int y, int pointer, int button) {
 		last.set(x, y);
 	
 		SinglePlayerGameScreen.currentDoodle.clear();
 //		SinglePlayerGameScreen.currentDoodleCollisionPoints.clear();
 		deletePath.clear();
 		
 //		y = -y + Gdx.graphics.getHeight();
 //		
 //		x = Math.max(Math.min(x, Gdx.graphics.getWidth()), 0);
 //		y = Math.max(Math.min(y, Gdx.graphics.getHeight()), 0);
 		
 //		Vector2 temp = new Vector2(x, y);
 //		Vector3 projected = new Vector3();
 //		picker = camera.getPickRay(temp.x, temp.y);
 //		Intersector.intersectRayTriangles(picker, SinglePlayerGameScreen.renderMap.heightMap.map, projected);
 //		GameSession.getInstance().soldiers.add(new EnemySoldier(2, new Vector2(projected.x, projected.z), new Vector2(1,0)));
 
 		return true;
 	}
 	
 	@Override
 	public boolean scrolled (int amount) {
 		camera.zoom -= -amount * Gdx.graphics.getDeltaTime() / 50;
 		camera.update();
 		
 		Gdx.app.log("", amount + "");
 		
 		for(ArrayList<Vector2> doodle : SinglePlayerGameScreen.doodles.values()) {
 			
 			
 			ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
 			for(Vector2 v : doodle) {
 				
 				v.add(amount,amount);
 				
 				newDoodle.add(v);
 				
 			}
 			
 			doodle.clear();
 			doodle.addAll(newDoodle);
 			
 		}
 		
 		
 		return true;
 	}
 	
 	boolean circleTest(ArrayList<Vector2> doodle) {
 		
 		if(doodle.get(0).dst(doodle.get(doodle.size() -1)) < MAX_DISTANCE)
 			return true;
 		else
 			return false;
 	}
 	
 	ArrayList<PlayerSoldier> soldierTest(Polygon polygon) {
 		
 		ArrayList<PlayerSoldier> soldiers = new ArrayList<PlayerSoldier>();
 		
 		for(Soldier s : GameSession.getInstance().soldiers) {
 			if(s instanceof PlayerSoldier) {
 				PlayerSoldier p = (PlayerSoldier) s;
 				if(polygon.contains(p.position.x, p.position.y)) {
 					Gdx.app.log("", p.dogTag + "");
 					p.circled = true;
 					soldiers.add(p);
 				}
 			}
 		}
 		
 		return soldiers;
 	}
 	
 	boolean checkDisjoint(Polygon polygon) {
 		
 		float[] list = polygon.getTransformedVertices();
 		
 		for(int j=0;j<list.length;j+=2) {
 			for(Polygon po : SinglePlayerGameScreen.circles.keySet()) {
 				if(po.contains(list[j], list[j+1]))
 					return false;
 			}
 		}
 		
 		ArrayList<Polygon> contained = new ArrayList<Polygon>();
 		for(Polygon po : SinglePlayerGameScreen.circles.keySet()) {
 			list = po.getTransformedVertices();
 			for(int j=0;j<list.length;j+=2) {
 				if(polygon.contains(list[j], list[j+1]))
 					contained.add(po);
 			}
 		}
 		
 		if(!contained.isEmpty()) {
 			for(Polygon pop : contained) {
 				list = polygon.getTransformedVertices();
 				for(int j=0;j<list.length;j+=2) {
 					if(pop.contains(list[j], list[j+1]))
 						return false;
 				}
 				SinglePlayerGameScreen.circles.remove(pop);
 				SinglePlayerGameScreen.doodles.remove(pop);
 				SinglePlayerGameScreen.paths.remove(pop);
 			}
 			
 		}
 		
 		return true;
 	}
 	
 	void updateDoodles() {
 		
 //		y = -y + Gdx.graphics.getHeight();
 		
 		Vector2 trans = delta.mul(100);
 		trans.y = -trans.y;
 //		trans.sub(last);
 //		trans.mul(0.01f);
 		
 		for(ArrayList<Vector2> doodle : SinglePlayerGameScreen.doodles.values()) {
 			
 			
 			ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
 			for(Vector2 v : doodle) {
 				
 				v.add(trans);
 				
 				newDoodle.add(v);
 				
 			}
 			
 			doodle.clear();
 			doodle.addAll(newDoodle);
 			
 		}
 		
 	}
 	
 }
