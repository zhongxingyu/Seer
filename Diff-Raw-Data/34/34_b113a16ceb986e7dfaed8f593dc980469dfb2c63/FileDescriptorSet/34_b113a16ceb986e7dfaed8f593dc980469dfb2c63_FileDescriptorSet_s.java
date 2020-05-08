 package org.lttng.flightbox.model;
 
 import java.util.HashMap;
 import java.util.Set;
 
 public class FileDescriptorSet {
 
 	private final IdMap<SocketInet> sockets;
 	private final IdMap<RegularFile> regFiles;
 	private final IdMap<FileDescriptor> fds;
 	private final HashMap<Integer, FileDescriptor> latests;
 	private final HashMap<IPv4Con, SocketInet> con2sock;
 	
 	public FileDescriptorSet() {
 		FileDescriptorIdProvider<SocketInet> provSock = new FileDescriptorIdProvider<SocketInet>();
 		sockets = new IdMap<SocketInet>();
 		sockets.setProvider(provSock);
 		
 		FileDescriptorIdProvider<RegularFile> provReg = new FileDescriptorIdProvider<RegularFile>();
 		regFiles = new IdMap<RegularFile>();
 		regFiles.setProvider(provReg);
 		
 		FileDescriptorIdProvider<FileDescriptor> provFd = new FileDescriptorIdProvider<FileDescriptor>();
 		fds = new IdMap<FileDescriptor>();
 		fds.setProvider(provFd);
 		
 		con2sock = new HashMap<IPv4Con, SocketInet>();
 		latests = new HashMap<Integer, FileDescriptor>();
 	}
 	
 	/* if a duplicate exists (fd,starTime), the fd is *not* added
 	 * and no error is thrown. This is acceptable on per-process basis
 	 * because no such duplicate can exists for a single process */
 	public <T extends FileDescriptor> void add(T fd) {
 		if (fd instanceof RegularFile) {
 			regFiles.add((RegularFile) fd);
 		} else if (fd instanceof SocketInet) {
 			SocketInet sock = (SocketInet) fd;
 			sockets.add(sock);
 			con2sock.put(sock.getIp(), sock);
 		}
 		fds.add(fd);
 		latests.put(fd.getFd(), fd);
 	}
 	
 	public <T extends FileDescriptor> void addAll(Set<T> set) {
 		for (T obj: set) {
 			add(obj);
 		}
 	}
 	
 	public <T extends FileDescriptor> void remove(T fd) {
 		if (fd instanceof RegularFile) {
 			regFiles.remove((RegularFile) fd);
 		} else if (fd instanceof SocketInet) {
 			SocketInet sock = (SocketInet) fd;
 			sockets.add(sock);
 			con2sock.remove(sock.getIp());
 		}
 		fds.remove(fd);
 		latests.remove(fd.getFd());
 	}
 	
 	public int size() {
 		return fds.size();
 	}
 	
 	public int historySize() {
 		return fds.historySize();
 	}
 	
 	public String toString() {
 		return fds.toString();
 	}
 	
 	public SocketInet findSocketByIp(IPv4Con con) {
 		return con2sock.get(con);
 	}
 	public SocketInet findSocketByPointer(long ptr) {
 		for (SocketInet sock: con2sock.values()) {
 			if (sock.getPointer() == ptr)
 				return sock;
 		}
 		return null;
 	}
 	public FileDescriptor getLatest(int fd) {
 		return latests.get(fd);
 	}
 
 	public HashMap<Integer, FileDescriptor> getCurrent() {
 		return latests;
 	}
 }
