 package agent;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 
 import board.BoardView;
 import board.Point;
 
 /**
  * Klasa reprezentujca wygld Agenta na planszy.
  * 
  * Jeli chcesz mie dostp do prywatnych pl/metod Agenta, nie pisz przy nich
  * adnego modyfikatora dostpu: ani private, ani public, ani protected. Wtedy
  * pole/metoda bd "package-private", czyli prywatne dla kadego z zewntrz,
  * ale publiczne w danym package (kada klasa w agent.* widzi pola bez
  * modyfikatora w kadej innej klasie w agent.*). -- m.
  */
 public class AgentView {
 
 	Agent agent;
 
 	public AgentView(Agent agent) {
 		this.agent = agent;
 	}
 
 	/** Rysowanko! */
 	public void paint(BoardView bv, Graphics2D g2, double scale) {
 		if (agent.exited)
 			return;
 
 		// push()
 		AffineTransform at = g2.getTransform();
 
 		Point p = agent.getPosition();
 
 		int w = (int) Math.round(Agent.BROADNESS * scale);
 		int h = (int) Math.round(Agent.THICKNESS * scale);
 
 		// move & rotate LCS (local coordinate system)
 		g2.translate(bv.translateX(p), bv.translateY(p));
 		g2.rotate(Math.toRadians(90 - agent.getOrientation()));
 
		Color agent_color = Color.CYAN;
		switch(agent.getStance()){
			case BENT:
				agent_color = agent_color.darker(); 
				break;				
			case CRAWL:
				agent_color = agent_color.darker().darker();  
				break;	
		}
		
 		// oval
		g2.setColor(agent.isAlive() ? agent_color : Color.PINK);
 		g2.fillOval(-w / 2, -h / 2, w, h); 
 		g2.setColor(agent.isAlive() ? Color.BLUE : Color.RED);
 		g2.drawOval(-w / 2, -h / 2, w, h);
 
 		/*
 		 * BoardView.drawVector(g2, 0, 0, (int)
 		 * Math.round(Agent.ORIENTATION_VECTOR * scale), agent.velocity > 0.0);
 		 */
 
 		// pop()
 		g2.setTransform(at);
 	}
 
 }
