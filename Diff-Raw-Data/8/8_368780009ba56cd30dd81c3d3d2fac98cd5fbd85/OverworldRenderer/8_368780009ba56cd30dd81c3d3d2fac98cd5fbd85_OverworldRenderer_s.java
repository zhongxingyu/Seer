 package com.slauson.tactics.view;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Matrix4;
 import com.slauson.tactics.model.Overworld;
 import com.slauson.tactics.model.Player;
 import com.slauson.tactics.model.Region;
 import com.slauson.tactics.model.Unit;
 
 /**
  * Renders the overworld.
  * @author josh
  *
  */
 public class OverworldRenderer extends Renderer {
 	
 	private static final Color SELECTED_REGION_COLOR = Color.WHITE;
 	private static final Color SELECTED_REGION_OUTLINE_COLOR = Color.WHITE;
 	private static final float MARKED_REGION_COLOR_FACTOR = 0.25f;
 	private static final float NON_TURN_COLOR_FACTOR = 0.5f;
 	
 	private ShapeRenderer renderer;
 	
 	private Overworld overworld;
 	
 	public OverworldRenderer(Overworld overworld) {
 		super();
 		this.overworld = overworld;
 		
 		renderer = new ShapeRenderer();
 	}
 	
 	/**
 	 * Renders the overworld.
 	 */
 	@Override
 	public void render(OrthographicCamera camera, float delta, boolean debug) {
 		renderer.setProjectionMatrix(camera.combined);
 		
 		// draw colors
 		renderer.begin(ShapeType.FilledRectangle);
 		for (Region region : overworld.regionList) {
 			// selected region
 			if (region.selected) {
 				renderer.setColor(SELECTED_REGION_COLOR);
 			}
 			// marked region
 			else if (region.marked) {
 				renderer.setColor(region.player.color.r*MARKED_REGION_COLOR_FACTOR, region.player.color.g*MARKED_REGION_COLOR_FACTOR, region.player.color.b*MARKED_REGION_COLOR_FACTOR, 0);
 			}
 			// normal region
 			else {
 				renderer.setColor(region.player.color);
 			}
 			
 			renderer.filledRect(region.position.x, region.position.y, region.bounds.width, region.bounds.height);
 		}
 		renderer.end();
 		
 		// draw units
 		for (Region region : overworld.regionList) {
 			
 			if (region.unit != null) {
 			
 				// player with current turn has black units
 				if (region.player == overworld.players[overworld.playerTurnIndex] && (region.unit.hasAttack || region.unit.hasMove)) {
 					renderer.setColor(Color.BLACK);
 				}
 				// other players are lighter
 				else {
 					renderer.setColor(region.player.color.r*NON_TURN_COLOR_FACTOR,
 							region.player.color.g*NON_TURN_COLOR_FACTOR,
 							region.player.color.b*NON_TURN_COLOR_FACTOR,
 							1);
 				}
 				
 				// set color based on health and player color
 //				renderer.setColor(region.player.color.r*(Unit.MAX_HEALTH-region.unit.health)/Unit.MAX_HEALTH/2,
 //						region.player.color.g*(Unit.MAX_HEALTH-region.unit.health)/Unit.MAX_HEALTH/2,
 //						region.player.color.b*(Unit.MAX_HEALTH-region.unit.health)/Unit.MAX_HEALTH/2,
 //						1);
 				
 				// size of unit is based on health
				float sizeFactor = 0.5f + (region.unit.health / Unit.MAX_HEALTH / 2);
 				
 				
 				switch(region.unit.type) {
 				case CIRCLE:
 					renderer.begin(ShapeType.FilledCircle);
 					renderer.filledCircle(region.position.x + region.bounds.width/2, region.position.y + region.bounds.height/2, 3*region.bounds.width/8*sizeFactor, 20);
 					renderer.end();
 					break;
 				case SQUARE:
 					renderer.begin(ShapeType.FilledRectangle);
 					renderer.filledRect(region.position.x + region.bounds.width/2 - 3*region.bounds.width/8*sizeFactor, region.position.y + region.bounds.height/2 - 3*region.bounds.height/8*sizeFactor, 3*region.bounds.width/4*sizeFactor, 3*region.bounds.height/4*sizeFactor);
 					renderer.end();
 					break;
 				case TRIANGLE:
 					renderer.begin(ShapeType.FilledTriangle);
 					renderer.filledTriangle(region.position.x + region.bounds.width/2 - 3*region.bounds.width/8*sizeFactor, region.position.y + region.bounds.height/2 - 3*region.bounds.height/8*sizeFactor,
 							region.position.x + region.bounds.width/2, region.position.y + region.bounds.height/2 + 3*region.bounds.height/8*sizeFactor,
 							region.position.x + region.bounds.width/2 + 3*region.bounds.width/8*sizeFactor, region.position.y + region.bounds.width/2 - 3*region.bounds.height/8*sizeFactor);
 //					renderer.filledTriangle(region.position.x + region.bounds.width/8, region.position.y + region.bounds.height/8,
 //							region.position.x + region.bounds.width/2, region.position.y + 7*region.bounds.height/8,
 //							region.position.x + 7*region.bounds.width/8, region.position.y + region.bounds.height/8);
 					renderer.end();
 					break;
 				case RANGED_CIRCLE:
 					renderer.begin(ShapeType.FilledCircle);
 					//renderer.filledCircle(region.position.x + region.bounds.width/2, region.position.y + region.bounds.height/2, 3*region.bounds.width/8*sizeFactor, 20);
 					renderer.filledCircle(region.position.x + region.bounds.width/2, region.position.y + region.bounds.height/2 - 3*region.bounds.height/16*sizeFactor, 3*region.bounds.width/16*sizeFactor, 20);
 					renderer.filledCircle(region.position.x + region.bounds.width/2, region.position.y + region.bounds.height/2 + 3*region.bounds.height/16*sizeFactor, 3*region.bounds.width/16*sizeFactor, 20);
 					renderer.end();
 					renderer.identity();
 					break;
 				case RANGED_SQUARE:
 					renderer.begin(ShapeType.FilledRectangle);
 					renderer.filledRect(region.position.x + region.bounds.width/2 - 3*region.bounds.width/16*sizeFactor, region.position.y + region.bounds.height/2 - 3*region.bounds.height/8*sizeFactor, 3*region.bounds.width/8*sizeFactor, 3*region.bounds.height/4*sizeFactor);
 					renderer.end();
 					break;
 				case RANGED_TRIANGLE:
 					renderer.begin(ShapeType.FilledTriangle);
 					renderer.filledTriangle(region.position.x + region.bounds.width/2 - 3*region.bounds.width/16*sizeFactor, region.position.y + region.bounds.height/2 - 3*region.bounds.height/8*sizeFactor,
 							region.position.x + region.bounds.width/2, region.position.y + region.bounds.height/2 + 3*region.bounds.height/8*sizeFactor,
 							region.position.x + region.bounds.width/2 + 3*region.bounds.width/16*sizeFactor, region.position.y + region.bounds.width/2 - 3*region.bounds.height/8*sizeFactor);
 //					renderer.filledTriangle(region.position.x + region.bounds.width/2 - 3*region.bounds.width/16*sizeFactor, region.position.y + region.bounds.height/2 - 3*region.bounds.height/8*sizeFactor,
 //							region.position.x + region.bounds.width/2, region.position.y + region.bounds.height/2,
 //							region.position.x + region.bounds.width/2 + 3*region.bounds.width/16*sizeFactor, region.position.y + region.bounds.width/2 - 3*region.bounds.height/8*sizeFactor);
 //					renderer.filledTriangle(region.position.x + region.bounds.width/2 - 3*region.bounds.width/16*sizeFactor, region.position.y + region.bounds.height/2,
 //							region.position.x + region.bounds.width/2, region.position.y + region.bounds.height/2 + 3*region.bounds.height/8*sizeFactor,
 //							region.position.x + region.bounds.width/2 + 3*region.bounds.width/16*sizeFactor, region.position.y + region.bounds.width/2);
 					renderer.end();
 					break;
 				}
 			}
 		}
 
 		// draw outline
 		renderer.begin(ShapeType.Rectangle);
 		renderer.setColor(Color.BLACK);
 		for (Region region : overworld.regionList) {
 			renderer.rect(region.position.x, region.position.y, region.bounds.width, region.bounds.height);
 		}
 		renderer.end();
 		
 		// draw turns
 		// TODO factor in number of player regions, unit strength, reinforcements
 		screenRenderer.begin(ShapeType.FilledRectangle);
 		for (int i = 0; i < overworld.players.length; i++) {
 			Player player = overworld.players[(i + overworld.playerTurnIndex) % overworld.players.length];
 			screenRenderer.setColor(player.color);
 			screenRenderer.filledRect(screenWidth - 20, screenHeight - (i+1)*20, 20, 20);
 		}
 		screenRenderer.end();
 		
 		super.render(camera, delta, debug);
 	}
 }
