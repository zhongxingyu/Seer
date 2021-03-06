 package simciv.buildings;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.state.StateBasedGame;
 
 import simciv.Game;
 import simciv.World;
 import simciv.content.Content;
 import simciv.jobs.InternalJob;
 import simciv.jobs.Job;
 import simciv.jobs.Taxman;
import simciv.movements.RandomRoadMovement;
 import simciv.units.Citizen;
 
 public class TaxmenOffice extends Workplace
 {
 	private static BuildingProperties properties;
 	private static SpriteSheet sprites;
 	
 	static
 	{
 		properties = new BuildingProperties("Taxmen office");
 		properties.setCost(200).setSize(2, 2, 1).setUnitsCapacity(6);
 	}
 	
 	public TaxmenOffice(World w)
 	{
 		super(w);
 		state = Building.NORMAL;
 
 		if(sprites == null)
 		{
 			sprites = new SpriteSheet(Content.images.buildTaxmenOffice, 
 					Game.tilesSize * getWidth(),
 					Game.tilesSize * (getHeight() + getZHeight()));
 		}
 	}
 
 	@Override
 	protected void tick()
 	{
 		if(state == Building.NORMAL)
 		{
 			if(!needEmployees())
 			{
 				state = Building.ACTIVE;
 				for(Citizen emp : employees.values())
 				{
 					if(emp.getJob().getID() == Job.TAXMAN && !emp.isOut())
					{
 						emp.exitBuilding(); // mission begin
						emp.setMovement(new RandomRoadMovement());
					}
 				}
 			}
 		}
 		else if(state == Building.ACTIVE)
 		{
 			if(needEmployees())
 			{
 				System.out.println("aaa");
 				state = Building.NORMAL;
 				// TODO out employees must end their mission
 			}
 		}
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics gfx)
 	{
 		int gx = posX * Game.tilesSize;
 		int gy = posY * Game.tilesSize;
 
 		if(state == Building.ACTIVE)
 			gfx.drawImage(sprites.getSprite(1, 0), gx, gy - Game.tilesSize);
 		else
 			gfx.drawImage(sprites.getSprite(0, 0), gx, gy - Game.tilesSize);
 	}
 
 	@Override
 	public int getProductionProgress()
 	{
 		return 0; // Taxmen don't produce anything.
 	}
 
 	@Override
 	public Job giveNextJob(Citizen citizen)
 	{
 		if(needEmployees())
 		{
 			Job job;
 			if(employees.size() < 4) // 4 internals,
 				job = new InternalJob(citizen, this, Job.TAXMEN_OFFICE_INTERNAL);
 			else // and 2 for patrol
 				job = new Taxman(citizen, this);
 				
 			addEmployee(citizen);
 			return job;
 		}
 		return null;
 	}
 
 	@Override
 	public BuildingProperties getProperties()
 	{
 		return properties;
 	}
 
 	@Override
 	protected int getTickTime()
 	{
 		return 1000;
 	}
 
 }
