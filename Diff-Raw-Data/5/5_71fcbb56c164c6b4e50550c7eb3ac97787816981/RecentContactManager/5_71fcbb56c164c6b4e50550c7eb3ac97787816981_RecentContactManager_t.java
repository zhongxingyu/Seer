 package com.lorent.vovo.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.swing.DefaultListModel;
 
 import com.lorent.vovo.ui.RecentContactListPanel;
 
 
 public class RecentContactManager {
 	
 	private static final int AVAILABLE_COUNT=30;
 	private static final int CONTACT_SIZE=100;
 	private static final Charset charset=Charset.forName("utf-8");
 	private RandomAccessFile r;
 	private int availableMask;
 	private int size;
 	private long indexSeed;
 	private TreeMap<RecentContact,RecentContact> result;
 	private HashMap<RecentContact,RecentContact> quickCache;
 	private RecentContactListPanel panel;
 	
 	private static RecentContactManager instance;
 	
 	public static RecentContactManager newInstance(
 			RecentContactListPanel p) {
 		instance=new RecentContactManager(p);
 		return instance;
 	}
 	
 	public static RecentContactManager getInstance(){
 		return instance;
 	}
 	
 	private RecentContactManager(RecentContactListPanel panel) {
 		this.panel = panel;
 	}
 
 	public Set<RecentContact> init() throws IOException{
 		File path = new File(System.getProperty("user.dir")+File.separator+"users");
 		if(!path.exists()){
 			path.mkdirs();
 		}
 		String userName;
 		try{
 			userName = DataUtil.getUserName();
 		}catch(Error e){
 			userName=null;
 		}
 		File file = new File(path,(userName==null?"admin":userName)+".rc");
 		if(file.exists()){
 			r = new RandomAccessFile(file, "rwd");
 		}else{
 			r = new RandomAccessFile(file, "rwd");
 			r.writeInt(0);//size
 			r.writeLong(0);
 			byte[] bytes = getSampleContact();
 			for(int i=0;i<AVAILABLE_COUNT;i++){
 				r.writeInt(RecentContactInfo.INVALID.mask);
 				r.write(bytes);
 				r.writeLong(0L);
 			}
 			r.seek(0L);
 		}
 		availableMask = r.readInt();
 		int mask=1;
 		for(int i=0;i<30;i++){
 			if(((mask<<i)&availableMask)==0){
 				size++;
 			}
 		}
 		indexSeed = r.readLong();
 		return getAll();
 	}
 
 	private byte[] getSampleContact() {
 		byte[] data=new byte[CONTACT_SIZE];
 		Arrays.fill(data, (byte)20);
 		return data;
 	}
 	
 	private long getRealPosition(int p){
 		return 12+p*(12+CONTACT_SIZE);
 	}
 	
 	private AvailablePositionResult findAvailablePosition(){
 		int mask=1;
 		for(int i=0;i<AVAILABLE_COUNT;i++){
 			if(((mask<<i)&availableMask)==0){
 				AvailablePositionResult result = new AvailablePositionResult();
 				result.position=getRealPosition(i);
 				result.mask=mask<<i;
 				return result;
 			}
 		}
 		return null;
 	}
 	
 	class AvailablePositionResult{
 		long position;
 		int mask;
 	}
 	
 	class AvailablePositionIterator implements Iterator<Long>{
 		int i;
 		@Override
 		public boolean hasNext() {
 			int mask=1;
 			for(;i<AVAILABLE_COUNT;i++){
 				if(((mask<<i)&availableMask)!=0){
 					return true;
 				}
 			}
 			return false;
 		}
 
 		@Override
 		public Long next() {
 			return getRealPosition(i++);
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 		
 	}
 	
 	public Set<RecentContact> getAll() throws IOException{
 		result = new TreeMap<RecentContact,RecentContact>();
 		quickCache = new HashMap<RecentContact,RecentContact>();
 		r.seek(12L);
 		byte[] _contact=new byte[CONTACT_SIZE];
 		AvailablePositionIterator iterator=new AvailablePositionIterator();
 		while(iterator.hasNext()){
 			RecentContact rc = new RecentContact();
 			rc.position=iterator.next();
 			r.seek(rc.position);
 			rc.info=r.readInt();
 			r.read(_contact);
 			rc.contact=new String(new String(_contact,charset).trim());
 			rc.index=r.readLong();
 			result.put(rc,rc);
 			quickCache.put(rc, rc);
 		}
 		return result.keySet();
 	}
 
 	public void insertFriendChat(String lcc) throws IOException{
 		RecentContact rc = new RecentContact(RecentContactInfo.FRIEND.mask,lcc);
 		insert(rc);
 	}
 	
 	public void insertGroupChat(String room) throws IOException{
 		RecentContact rc = new RecentContact(RecentContactInfo.GROUP.mask,room);
 		insert(rc);
 	}
 	
 	public void insert(RecentContact c) throws IOException{
 		if(!result.isEmpty()&&c.equals(result.lastKey()))
 			return;
 		RecentContact rc = quickCache.remove(c);
 		DefaultListModel model = panel.getModel();
 		if(rc!=null){
 			result.remove(rc);
 			model.removeElement(rc);
 			model.add(0, rc);
 			rc.index=++indexSeed;
 			put(rc);
 			saveIndexSeed();
 			r.seek(rc.position+4+CONTACT_SIZE);
 			r.writeLong(indexSeed);
 		}else{
 			AvailablePositionResult freePosition=findAvailablePosition();
 			if(freePosition!=null){
 				c.index=++indexSeed;
 				model.add(0, c);
 				put(c);
 				writeRecentContact(freePosition.position,c);
 				r.seek(0);
 				++size;
 				r.writeInt(availableMask|=freePosition.mask);
 				r.writeLong(indexSeed);
 			}else{
 				c.index=++indexSeed;
 				rc = result.firstKey();
 				put(c);
 				quickCache.remove(rc);
 				result.remove(rc);
 				model.removeElement(rc);
 				model.add(0, c);
 				writeRecentContact(rc.position,c);
 				saveIndexSeed();
 			}
 		}
 	}
 	
 	private void put(RecentContact c){
 		result.put(c, c);
 		quickCache.put(c, c);
 	}
 	
 	public void removeFriendChat(String lcc) throws IOException{
 		RecentContact rc = new RecentContact(RecentContactInfo.FRIEND.mask,lcc);
 		remove(rc);
 	}
 	
 	public void removeGroupChat(String room) throws IOException{
 		RecentContact rc = new RecentContact(RecentContactInfo.GROUP.mask,room);
 		remove(rc);
 	}
 	
 	public void remove(RecentContact c) throws IOException{
 		RecentContact rc = quickCache.remove(c);
 		if(rc==null){
 			return;
 		}
		panel.getModel().removeElement(rc);
		result.remove(rc);
 		--size;
 		availableMask&=~(1<<(rc.position-12)/112);
 		r.seek(0);
 		r.writeInt(availableMask);
 		r.seek(rc.position);
 		r.writeInt(RecentContactInfo.INVALID.mask);
 	}
 	
 	private void writeRecentContact(long position,RecentContact c) throws IOException{
 		c.position=position;
 		r.seek(position);
 		r.writeInt(c.info);
 		byte[] temp=new byte[CONTACT_SIZE];
 		byte[] bytes = c.contact.getBytes(charset);
 		if(CONTACT_SIZE>bytes.length){
 			System.arraycopy(bytes, 0, temp, 0, bytes.length);
 			Arrays.fill(temp, bytes.length,CONTACT_SIZE,(byte)0);
 		}else{
 			System.arraycopy(bytes, 0, temp, 0, CONTACT_SIZE);
 		}
 		r.write(temp);
 		r.writeLong(c.index);
 	}
 	
 	private void saveIndexSeed() throws IOException{
 		r.seek(4);
 		r.writeLong(indexSeed);
 	}
 	
 	public static class RecentContact implements Comparable<RecentContact>{
 		int info;
 		String contact;
 		long index;
 		long position;
 		
 		private RecentContact(){}
 		
 		public RecentContact(int info, String contact) {
 			this.info = info;
 			this.contact = contact;
 		}
 
 		public int getInfo() {
 			return info;
 		}
 
 		public String getContact() {
 			return contact;
 		}
 
 		public long getIndex() {
 			return index;
 		}
 
 		public long getPosition() {
 			return position;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((contact == null) ? 0 : contact.hashCode());
 			result = prime * result + info;
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			RecentContact other = (RecentContact) obj;
 			if (contact == null) {
 				if (other.contact != null)
 					return false;
 			} else if (!contact.equals(other.contact))
 				return false;
 			if (info != other.info)
 				return false;
 			return true;
 		}
 
 		@Override
 		public int compareTo(RecentContact o) {
 			if(this.equals(o))
 				return 0;
 			else if(index<o.index)
 				return -1;
 			else if(index>o.index)
 				return 1;
 			else 
 				return 0;
 		}
 
 		@Override
 		public String toString() {
 			return "RecentContact [info=" + info + ", contact=" + contact
 					+ ", index=" + index + ", position=" + position + "]";
 		}
 
 	}
 	
 	public static enum RecentContactInfo{
 		INVALID,FRIEND,GROUP;
 		public final int mask;
 		
 		private RecentContactInfo() {
 			mask=1<<ordinal();
 		}
 	}
 
 }
