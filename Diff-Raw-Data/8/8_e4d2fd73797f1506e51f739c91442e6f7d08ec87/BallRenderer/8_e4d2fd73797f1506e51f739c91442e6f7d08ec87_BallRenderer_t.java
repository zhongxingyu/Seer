 package org.doublelong.catchr.renderer;
 
 import org.doublelong.catchr.entity.Ball;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 
 public class BallRenderer
 {
 	private Ball ball;
 
 	private Sprite sprite;
 	private Texture texture;
 
 	public BallRenderer(Ball ball)
 	{
 		this.ball = ball;
 		this.texture = new Texture(Gdx.files.internal("assets/images/ballBlue.png"));
 		this.sprite = new Sprite(this.texture);
 	}
 
 	public void renderer(SpriteBatch batch, OrthographicCamera camera)
 	{
 		Vector2 pos = this.ball.getBody().getPosition();
		float rad = this.ball.getFixture().getShape().getRadius();
		//		this.sprite.setSize(Paddle.WIDTH * 2, Paddle.HEIGHT * 2);
 		//		this.sprite.setBounds(0, 0, Paddle.WIDTH, Paddle.HEIGHT);
		this.sprite.setPosition(pos.x - rad, pos.y - rad);
 
 		this.sprite.draw(batch);
 
 	}
 }
