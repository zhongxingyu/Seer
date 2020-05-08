 package edu.rit.se.sse.rapdevx.gui.screens;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.rit.se.sse.rapdevx.api.dataclasses.Ability;
 import edu.rit.se.sse.rapdevx.clientmodels.Ship;
 import edu.rit.se.sse.rapdevx.gui.RectangleBackground;
 import edu.rit.se.sse.rapdevx.gui.Screen;
 import edu.rit.se.sse.rapdevx.gui.drawable.Text;
 
 /**
  * The stats screen that shows up when you hover
  * 
  * @author Kristen Mills
  * 
  */
 public class StatsScreen extends Screen
 {
 
 	private Ship ship;
 
 	private int x;
 	private int y;
 	private Text name;
 	private Text moveRadius;
 	private Text HP;
 	private Text hpMaxHp;
 	private Text abilitiesWord;
 	private ArrayList<Text> abilities = new ArrayList<Text>();
 
 	// private Ship ship;
 
 	/**
 	 * Constrctor for the Stats screen
 	 * 
 	 * @param width
 	 *            the width of the screen
 	 * @param height
 	 *            the height of the screen
 	 * @param wWidth
 	 *            the width of the window
 	 * @param wHeight
 	 *            the height of the window
 	 */
 	public StatsScreen(int wWidth, int wHeight, Ship ship)
 	{
 		super(wWidth, wHeight);
 		x = screenWidth - 300;
 		y = screenHeight - 200;
 		this.ship = ship;
 		
 		name = new Text(ship.getClassName(), x + 10, y + 10, 2.5);
 		HP = new Text("HP", x + 25, y + 38, 2.0);
 		moveRadius = new Text("Move Radius: 5", x + 10, y + 65, 2.5);
 
 		double hp = ship.getHp();
 		double maxHp = ship.getMaxHp();
 
 		hpMaxHp = new Text((int) hp + "/" + (int) maxHp, x + 65 + 145, y + 20,
 				1.5);
 
 		abilitiesWord = new Text("Abilites:", x + 10, y + 95, 2.5);
 		
 		int yPosition = 115;
 		
 		List<Ability> abilities = new ArrayList<Ability>();
 		abilities.add(new Ability());
 		abilities.get(0).setDefault_damage(5);
 		abilities.get(0).setName("Bob");
 		abilities.get(0).setRadius(9);
 		abilities.add(new Ability());
 		
		//GameSession.get().findByClassId(ship.getClassID()).getAbilities();
 		
 		for (Ability ability : abilities)
 		{
 			this.abilities.add(new Text(ability.getName() + ": Radius: "
 					+ ability.getRadius() + " Damage: "
 					+ ability.getDefault_damage(), x + 35, y + yPosition, 1.5));
 			yPosition += 10;
 		}
 		
 	}
 
 	@Override
 	public void update(boolean hasFocus, boolean isVisible)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void updateTransition(double position, int direction)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	public void draw(Graphics2D gPen)
 	{
 		new RectangleBackground(x, y, 300, 200).draw(gPen);
 		gPen.setColor(Color.BLACK);
 		name.draw(gPen);
 		drawHPBar(gPen);
 		moveRadius.draw(gPen);
 		drawAbilities(gPen);
 	}
 
 	public void drawHPBar(Graphics2D gPen)
 	{
 		Color startColor = gPen.getColor();
 		HP.draw(gPen);
 
 		double hp = ship.getHp();
 		double maxHp = ship.getMaxHp();
 		hpMaxHp.setText(hp + "/" + maxHp);
 		
 		
 		double scale = hp / maxHp * 195.0;
 		gPen.setColor(new Color(127, 127, 127));
 		gPen.fill(new Rectangle(x + 65, y + 35, 195, 20));
 		hpMaxHp.draw(gPen);
 		if (scale < 1.0 / 3.0 * 195.0)
 		{
 			gPen.setColor(Color.RED);
 		}
 		else if (scale < 2.0 / 3.0 * 195.0)
 		{
 			gPen.setColor(Color.YELLOW);
 		}
 		else
 		{
 			gPen.setColor(Color.GREEN);
 		}
 		gPen.fill(new Rectangle(x + 65, y + 35, (int) scale, 20));
 		gPen.setColor(startColor);
 	}
 
 	public void drawAbilities(Graphics2D gPen)
 	{
 		abilitiesWord.draw(gPen);
 		for (Text ability : abilities)
 		{
 			ability.draw(gPen);
 		}
 	}
 
 	public Ship getShip()
 	{
 		return ship;
 	}
 
 	public void setShip(Ship ship)
 	{
 		this.ship = ship;
 	}
 
 }
