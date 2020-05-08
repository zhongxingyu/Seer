 package com.maton.tools.stiletto.model;
 
 import com.maton.tools.stiletto.model.base.BasePool;
 
 public class SpritePool extends BasePool<Sprite> {
 
 	public static final Sprite EMPTY = new Sprite("_ empty _");
 	
 	protected BundleContext ctx;
 	
 	public SpritePool(BundleContext ctx) {
 		super();
 		this.ctx = ctx;
 	}
 	
 	@Override
 	public Sprite getElement(String name) {
		if (name.equals("< empty >")) {
 			return EMPTY;
 		}
 		
		return super.getElement(name);
 	}
 
 	@Override
 	public Sprite createElement(String name) {
 		return new Sprite(name);
 	}
 	
 }
