 package com.yu.server.network;
 
 import java.util.ArrayList;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class ServerInputBuffer {
 	private int bufferID;
 	private ArrayList<String> buffer = null;
 
	
	/*
	 * 缓存可以容忍的最大队列长度
	 */
	//TODO  缓存可以容忍的最大队列长度
 	private int tolerantCapacity;
 	
 	private Lock lock = new ReentrantLock();
 
 	private Condition notEmpty = lock.newCondition();
 
 	public ServerInputBuffer(int bufferID) {
 		this.bufferID = bufferID;
 		buffer = new ArrayList<String>();
 		this.tolerantCapacity = 1;
 		
 	}
 
 	public int getSize(){
 		return buffer.size();
 	}
 	
 	public void add(String s) {
 		lock.lock();
 		//若达到最大容忍队列长度，清空队列
 		if(buffer.size() >= this.tolerantCapacity){
 			buffer.clear();
 			System.out.println("ServerInputBuffer[" + this.bufferID + "] cleared once");
 		}
 		buffer.add(s);
 		// 唤醒
 		notEmpty.signal();
 		lock.unlock();
 		//TODO delete
 		//System.out.println(this.bufferID + "--ServerInputBuffer::"+buffer.size());
 	}
 
 	public String getThenRemove() {
 		String s = null;
 		lock.lock();
 		try {
 			while (buffer.isEmpty()) {
 //				System.out.println("Wait for ServerInputBuffer's notEmpty condition!");
 				// 等待
 				notEmpty.await();
 			}
 			s = buffer.get(0);
 			buffer.remove(0);
 		} catch (InterruptedException ex) {
 			ex.printStackTrace();
 		} finally {
 			lock.unlock();
 			return s;
 		}
 	}
 
 	public boolean isEmpty() {
 		return buffer.isEmpty();
 	}
 
 	public boolean remove() {
 		if (buffer.isEmpty())
 			return false;
 		else {
 			buffer.remove(0);
 			return true;
 		}
 	}
 
 	public String getCommand() {
 		if (buffer.isEmpty())
 			return null;
 		else
 			return buffer.get(0);
 	}
 	
 	public void setTolerantCapacity(int i ){
 		this.tolerantCapacity = i;
 	}
 	
 	public int getTolerantCapacity(){
 		return this.tolerantCapacity;
 	}
 }
