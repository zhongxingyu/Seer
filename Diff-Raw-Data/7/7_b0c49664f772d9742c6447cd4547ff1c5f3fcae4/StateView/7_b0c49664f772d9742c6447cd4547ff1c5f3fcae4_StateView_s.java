 package com.automatonizer.view;
 
 import com.automatonizer.model.State;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.TextMetrics;
 import com.google.gwt.touch.client.Point;
 
 public class StateView {
 
 	private static final int RADIUS = 30;
 	private final State state;
 	private AutomatonCanvas canvas;
 	private Point position;
 
 	public StateView(AutomatonCanvas canvas, State state, double x, double y) {
 		this(canvas, state, new Point(x, y));
 	}
 
 	public StateView(AutomatonCanvas canvas, State state, Point position) {
 		this.canvas = canvas;
 		this.state = state;
 		this.position = position;
 	}
 
 	public void draw() {
 		drawCircle(RADIUS, "black");
 		drawCircle(RADIUS - 2, "white");
 
 		Context2d textCtx = canvas.getTextContext();
 		textCtx.setFillStyle("black");
 		textCtx.setFont("16px Arial");
 		TextMetrics metrics = textCtx.measureText(state.getIdentifier());
 		textCtx.fillText(state.getIdentifier(), getWorldPosition().getX()
 				- metrics.getWidth() / 2, getWorldPosition().getY()
 				+ textCtx.measureText("M").getWidth() / 2);
 	}
 
 	/**
 	 * Based on this SO-answer: http://stackoverflow.com/a/7227057/1501916
 	 */
 	public boolean inRadius(int x, int y) {
		int dx = (int) Math.abs(x - position.getX());
		int dy = (int) Math.abs(y - position.getY());
 
 		if (dx > RADIUS || dy > RADIUS) return false;
 		if (dx + dy <= RADIUS
 				|| Math.pow(dx, 2) + Math.pow(dy, 2) <= Math.pow(RADIUS, 2)) return true;
 
 		return false;
 	}
 
 	public State getState() {
 		return state;
 	}
 
 	public Point getPosition() {
 		return position;
 	}
 
 	public void setPosition(Point position) {
		this.position = position;
 	}
 
 	private Point getWorldPosition() {
 		return position.plus(canvas.offset);
 	}
 
 	private void drawCircle(int radius, String color) {
 		Context2d ctx = canvas.getObjectContext();
 		ctx.beginPath();
 		ctx.setFillStyle(color);
 		ctx.arc(getWorldPosition().getX(), getWorldPosition().getY(), radius,
 				0, 2 * Math.PI);
 		ctx.fill();
 		ctx.closePath();
 	}
 
 }
