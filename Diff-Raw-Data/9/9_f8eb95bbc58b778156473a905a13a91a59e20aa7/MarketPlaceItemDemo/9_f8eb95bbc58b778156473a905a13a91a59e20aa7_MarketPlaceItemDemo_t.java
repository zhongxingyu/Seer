 package edu.gatech.CS2340.GrandTheftPoke.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 
 import edu.gatech.CS2340.GrandTheftPoke.GTPoke;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlaceItem;
 
 public class MarketPlaceItemDemo extends AbstractScreen{
 	MarketPlaceItem item = new MarketPlaceItem(5000, 5000);
 	Table table = new Table(getSkin());
 	boolean lastLeft = false,lastRight = false,lastDown = false;
 	private Vector3 touchPos;
 	public MarketPlaceItemDemo(GTPoke game){
 		super(game);
 	}
 	@Override
 	public void show(){
 		super.show();
 		table.debug();
 		touchPos = new Vector3();
 	}
 
 	@Override
 	public void render(float delta ){
 		super.render( delta );
 
 		if(Gdx.input.isTouched()) {
 			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
 			item = new MarketPlaceItem(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
 		}
 		
 		if(Gdx.input.isKeyPressed(Keys.LEFT))
 			item.sell(2);
 		if(Gdx.input.isKeyPressed(Keys.RIGHT))
 			item.buy(2);
 		if(!Gdx.input.isKeyPressed(Keys.RIGHT) && !Gdx.input.isKeyPressed(Keys.LEFT))
 			item.update();
		//item.drawcurves();
 	}
 
 	@Override
 	public void resize(int width, int height){
 		super.resize( width, height );      
 	}
 
 	@Override
 	public void dispose(){
 		super.dispose();
 	}
 }
