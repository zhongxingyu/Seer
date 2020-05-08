 package com.eleventhhour.towerdefense;
 
 import java.util.ArrayList;
 
 public abstract class ObjectPool {
 
	  private static final int DEFAULTSIZE = 32;
 	  private int size;
 	  protected ArrayList<Object> thePool;
 
 	  public ObjectPool() {
 	    setSize(ObjectPool.DEFAULTSIZE);
 	  }
 
 	  public ObjectPool(int size) {
 	    setSize(size);
 	  }
 
 	  private void setSize(int size) {
 	    this.size = size;
 	    this.thePool = new ArrayList<Object>();
 	    fill();
 	  }
 
 	  protected Object allocate() {
 	    Object result = this.thePool.remove(this.thePool.size() - 1);
 	    return result;
 	  }
 
 	  protected abstract void fill();
 
 	  public int getAllocatedCount() {
 	    return this.size - this.thePool.size();
 	  }
 
 	  protected ArrayList<Object> getPool() {
 	    return this.thePool;
 	  }
 
 	  protected int getSize() {
 	    return this.size;
 	  }
 
 	  public void release(Object paramObject) {
 	    this.thePool.add(paramObject);
 	  }
 
 	  public abstract void reset();
 	  
 }
