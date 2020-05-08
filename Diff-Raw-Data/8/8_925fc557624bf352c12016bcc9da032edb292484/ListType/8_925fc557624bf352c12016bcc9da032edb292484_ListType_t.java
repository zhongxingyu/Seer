 package com.yuktix.bencode.ds;
 
 import java.util.List;
 import java.util.ListIterator;
 
 public class ListType implements IBencodeType {
 	
 	private List<IBencodeType> list ;
 	
 	public ListType(List<IBencodeType> list) {
 		this.list = list ;
 	}
 
 	public List<IBencodeType> getList() {
 		return list;
 	}
 	
 	public void setList(List<IBencodeType> list) {
 		this.list = list;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("[List:\n");
 		ListIterator<IBencodeType> iter = this.list.listIterator() ;
 		
 		while(iter.hasNext()) {
			sb.append("\t:" );
 			sb.append(iter.next().toString());
 			sb.append("\n");
 		}
 		
 		sb.append("] \n");
 		
 		return sb.toString() ;
 	}
 
 	@Override
 	public String bencode() {
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("l");
 		
 		// @todo list keys ordering?
 		ListIterator<IBencodeType> iter = this.list.listIterator() ;
 		while(iter.hasNext()) {
 			sb.append(iter.next().bencode());
 		}
 		
 		sb.append("e");
 		return sb.toString();
 		
 	}
 	
 }
