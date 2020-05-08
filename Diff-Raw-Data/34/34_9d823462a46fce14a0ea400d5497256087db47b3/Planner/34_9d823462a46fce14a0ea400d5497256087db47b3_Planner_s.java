 package com.nicholasvaidyanathan.community.agents;
 
 import java.util.Arrays;
 
 import com.nicholasvaidyanathan.community.strategies.Strategy;
 
 public abstract class Planner implements Agent {
	private boolean canOverride = true;
	private Object[] data;
 	
 	public Planner(Object...objects) {
 		setData(objects);
 	}
 	
	public Planner(boolean override, Object...objects) {
		canOverride = override;
		setData(objects);
	}
	
	private void setData(Object...objects){
		if(	objects.length != 2 && 
			!(objects[0] instanceof String) && 
			!(objects[1] instanceof String) ){
			throw new UnsupportedOperationException("Should be called with two strings");
		}
		data = objects;
	}
 	
 	public Object[] getData() { return Arrays.copyOf(data, data.length, Object[].class); }
 
	public abstract Strategy plan();

 	@Override
 	public Object act(Object toActOn, Strategy toUse) {
		Strategy recommended = plan();
		String useName = toUse.getClass().getName();
		String recName = recommended.getClass().getName();
		if(!useName.equals(recName)){
			String message = String.format("Planner does not recommend %s.%n", useName);
			if(canOverride){
				message += String.format("Using %s instead", recName);
			}
			System.out.println(message);
		}
		return (!canOverride) ? toUse.evaluate(toActOn) : recommended.evaluate(toActOn);
 	}
 }
