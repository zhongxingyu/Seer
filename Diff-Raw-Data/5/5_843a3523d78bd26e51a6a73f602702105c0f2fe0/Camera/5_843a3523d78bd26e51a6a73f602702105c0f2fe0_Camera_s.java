 package com.base.engine;
 
 import org.lwjgl.input.Keyboard;
 
 public class Camera
 {
 	public static final Vector3f yAxis = new Vector3f(0, 1, 0);
 	
 	private Vector3f position;
 	private Vector3f forward;
 	private Vector3f up;
 	
 	public Camera()
 	{
 		this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, 1, 0)); 
 	}
 	public Camera(Vector3f position, Vector3f forward, Vector3f up)
 	{
 		this.position = position;
 		this.forward = forward;
 		this.up = up;
 		
 		up.Normalize();
 		forward.Normalize();
 	}
 
 	public void move(Vector3f direction, float amount)
 	{
 		position = position.Add(direction.Mul(amount));
 	}
 	public void rotateX(float angle)
 	{
 		Vector3f Haxis = yAxis.Cross(forward);
 		Haxis.Normalize();
 
 		forward = forward.Rotate(angle, Haxis).Normalize();
 
 		up = forward.Cross(Haxis);
 		up.Normalize();
 	}
 	public void rotateY(float angle)
 	{
 		Vector3f Haxis = yAxis.Cross(forward);
 		Haxis.Normalize();
 
 		forward = forward.Rotate(angle, yAxis).Normalize();
 
 		up = forward.Cross(Haxis);
 		up.Normalize();
 	}
 
 	public void input()
 	{
 		float movementAmount = (float)(10 * Time.GetDelta());
 		float rotationAmount = (float)(100 * Time.GetDelta());
 		
 		if (Input.GetKey(Keyboard.KEY_W))
 			move(getForward(), movementAmount);
 		if (Input.GetKey(Keyboard.KEY_A))
 			move(getLeft(), movementAmount);
 		if (Input.GetKey(Keyboard.KEY_S))
 			move(getForward(), -movementAmount);
 		if (Input.GetKey(Keyboard.KEY_D))
 			move(getRight(), movementAmount);
 		
 		if (Input.GetKey(Keyboard.KEY_UP))
 			rotateX(-rotationAmount);
 		if (Input.GetKey(Keyboard.KEY_DOWN))
 			rotateX(rotationAmount);
 		if (Input.GetKey(Keyboard.KEY_LEFT))
 			rotateY(-rotationAmount);
 		if (Input.GetKey(Keyboard.KEY_RIGHT))
 			rotateY(rotationAmount);
 	}
 	
 	public Vector3f getLeft()
 	{
 		return forward.Cross(up).Normalize();
 	}
 	public Vector3f getRight()
 	{
 		return up.Cross(forward).Normalize();
 	}
 	
 	public Vector3f getPosition()
 	{
 		return position;
 	}
 	public Vector3f getForward()
 	{
 		return forward;
 	}
 	public Vector3f getUp()
 	{
 		return up;
 	}
 
 	public void setPosition(Vector3f position)
 	{
 		this.position = position;
 	}
 	public void setForward(Vector3f forward)
 	{
 		this.forward = forward;
 	}
 	public void setUp(Vector3f up)
 	{
 		this.up = up;
 	}
 }
