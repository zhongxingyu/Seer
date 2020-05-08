 import java.util.LinkedList;
 import java.util.List;
 
 class Physics {
 
	// TODO gravity is stronger  | then -... why?
 	private Game game;
 	private double friction = 0.6;
 	private double pushOff = 0.08;
 
 	public Physics(Game game) {
 		this.game = game;
 	}
 
 	private void indirectGravity(Ball b) {
 		//Vektor v = new Vektor(b.position, getCenter(),
 		//		b.distanceTo(getCenter()).getLength() * 0.0001 + 0.2);
 		Vektor difference = game.getCenter().sub(b.position);
		double length = difference.getLength();
		difference.setLength(length * 0.0001 + 0.3);
 		b.accelerate(difference);
 	}
 
 	private void collide(Collision c) {
 		if ((c.a == game.active || c.b == game.active) && c.a.color.equals(c.b.color))
 			game.attachBall(c.a, c.b);
 		Vektor diff2 = c.diff.mul(pushOff);
 		c.a.accelerate(diff2.mul(-1));
 		c.b.accelerate(diff2);
 	}
 
 	private void moveBall(Ball b) {
 		b.speed = b.speed.add(b.acceleration);
 		b.position = b.position.add(b.speed);
 	}
 
 	private void friction(Ball b) {
 		b.accelerate(b.speed.mul(-friction));
 	}
 
 	private void moveActiveBall() {
 		Vektor accel = game.getPointer().sub(game.active.position);
 		game.active.accelerate(accel.mul(0.2));
 	}
 
 	public void physik() {
 		List<Collision> collisions = new LinkedList<Collision>();
 		// collisions = Collections.synchronizedList(collisions);
 		int balls_count = game.balls.size();
 		if (balls_count > 1) {
 			for (int i = 0; i < balls_count - 1; i++) {
 				for (int j = i + 1; j < balls_count; j++) {
 
 					Collision collision = Collision.test(game.balls.get(i), game.balls.get(j));
 					if (collision != null)
 						collisions.add(collision);
 				}
 			}
 		}
 		for (Ball b : game.balls) {
 			b.acceleration = new Vektor();
 			indirectGravity(b);
 			friction(b);
 		}
 		if (game.active != null) moveActiveBall();
 		for (Collision c : collisions)
 			collide(c);
 		for (Joint j : game.joints)
 			j.swingBalls();
 		for (Ball b : game.balls)
 			moveBall(b);
 	}
 }
