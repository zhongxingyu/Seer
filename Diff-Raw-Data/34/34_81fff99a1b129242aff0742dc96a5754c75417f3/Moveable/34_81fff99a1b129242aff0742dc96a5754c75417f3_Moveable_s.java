 package org.frankversnel.nl.poortjes.movement;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import org.frankversnel.nl.poortjes.GameObject;
 import org.frankversnel.nl.poortjes.component.Component;
 import org.frankversnel.nl.poortjes.component.Speed;
 import org.frankversnel.nl.poortjes.component.Transform;
 import org.frankversnel.nl.poortjes.gameloop.GameLoop;
 
 public class Moveable extends Component implements ActionListener {
	GameLoop gameLoop;
 
 	public Moveable(GameObject gameObject, GameLoop gameLoop) {
 		super(gameObject);
 
 		this.gameLoop = gameLoop;
 		gameLoop.addActionListener(this);
 	}
 
 	public void move(int timeSinceLastEvent) {
 		Transform transform = getGameObject().getComponent(Transform.class);
 		Speed speed = getGameObject().getComponent(Speed.class);
 
 		if (transform != null && speed != null) {
 			transform.rotate(speed.getRotation() * timeSinceLastEvent);
			transform.translate(speed.getDistance() * timeSinceLastEvent,
					speed.getDistance() * timeSinceLastEvent);
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		int timeSinceLastEvent = ((GameLoop) event.getSource()).getDelay();
 		move(timeSinceLastEvent);
 	}
 
 	@Override
 	public void remove() {
 		gameLoop.removeActionListener(this);
 	}
 
 }
