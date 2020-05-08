 package net.cheney.cocktail.resource.file;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import net.cheney.cocktail.message.Request.Method;
 import net.cheney.cocktail.resource.ApplicationResource;
 import net.cheney.cocktail.resource.CollectionResource;
 import net.cheney.cocktail.resource.Resource;
 import net.cheney.snax.model.Element;
 import net.cheney.snax.model.QName;
 
 import org.apache.commons.io.FileUtils;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterables;
 
 public class FileResource extends ApplicationResource {
 
 	private final File file;
 	private final FileResourceProvidor providor;
 
 	FileResource(FileResourceProvidor providor, File file) {
 		this.providor = providor;
 		this.file = file;
 	}
 
 	public boolean exists() {
 		return file().exists();
 	}
 
 	public Date lastModified() {
 		return new Date(file().lastModified());
 	}
 
 	public String etag() {
 		return String.format("\"%d\"", file().lastModified()); // "2134123412"
 	}
 
 	public ByteBuffer entity() throws IOException {
 		FileChannel fc = channel();
 		try {
 			return fc.map(MapMode.READ_ONLY, 0, fc.size());
 		} finally {
 			fc.close();
 		}
 	}
 
 	public boolean mkcol() {
 		return file().mkdir();
 	}
 
 	public FileResource parent() {
 		return new FileResource(providor, file().getParentFile());
 	}
 
 	public void put(ByteBuffer entity) throws IOException {
 		FileChannel fc = new FileOutputStream(file()).getChannel();
 		fc.write(entity);
 		fc.close();
 	}
 
 	public boolean delete() {
 		return (isCollection() ? deleteDirectory() : deleteFile());
 	}
 
 	private boolean deleteDirectory() {
 		try {
 			FileUtils.deleteDirectory(file());
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	private boolean deleteFile() {
 		return file().delete();
 	}
 
 	public boolean isCollection() {
 		return file().isDirectory();
 	}
 
 	public File file() {
 		return file;
 	}
 
 	public List<Resource> members() {
 		File[] files = file.listFiles();
 		List<Resource> children = new ArrayList<Resource>();
 		for(File c : (files == null ? new File[0] : files )) {
 			children.add(new FileResource(providor, c));
 		}
 		return children;
 	}
 
 	public void copyTo(final Resource destination) throws IOException {
 		File dest = ((FileResource)destination).file;
 		File source = this.file;
 		
 		if(source.isDirectory()) {
 			if(dest.isDirectory()) {
 				FileUtils.copyDirectoryToDirectory(source, dest);
 			} else {
 				dest.delete();
 				FileUtils.copyDirectory(source, dest);
 			}
 		} else {
 			if(dest.isDirectory()) {
 				FileUtils.copyFileToDirectory(source, dest);
 			} else {
 				FileUtils.copyFile(source, dest);
 			}
 		}
 	}
 	
 	public void moveTo(final Resource destination) throws IOException {
 		File dest = ((FileResource)destination).file;
 		File source = this.file;
 		
 		if(source.isDirectory()) {
 			if(dest.isDirectory()) {
 				FileUtils.copyDirectoryToDirectory(source, dest);
 				FileUtils.deleteDirectory(source);
 			} else {
 				dest.delete();
 				FileUtils.copyDirectory(source, dest);
 				FileUtils.deleteDirectory(source);
 			}
 		} else {
 			if(dest.isDirectory()) {
 				FileUtils.copyFileToDirectory(source, dest);
 				source.delete();
 			} else {
 				FileUtils.copyFile(source, dest);
 				source.delete();
 			}
 		}
 	}
 	
 	public long size() {
 		return file.length();
 	}
 	
 	public String displayName() {
 		return file.getName();
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		return (o instanceof FileResource && ((FileResource)o).file.equals(this.file));
 	}
 	
 	@Override
 	public int hashCode() {
 		return file.hashCode();
 	}
 
 	@Override
 	public Collection<Method> supportedMethods() {
 		return Arrays.asList(Method.values());
 	}
 
 	@Override
 	public Collection<ComplianceClass> davOptions() {
 		return Arrays.asList(new ComplianceClass[] { ComplianceClass.LEVEL_1, ComplianceClass.LEVEL_2 });
 	}
 
 	public FileResourceProvidor providor() {
 		return providor;
 	}
 
 //	@Override
 	
 	public FileChannel channel() throws IOException {
 		return new FileInputStream(file()).getChannel();
 	}
 
 	@Override
 	public String name() {
 		return file.getName();
 	}
 
 	@Override
 	public boolean isLocked() {
 		return false;
 	}
 
 	@Override
 	public long contentLength() {
 		return file.length();
 	}
 
 	@Override
 	public Resource create(String name, ByteBuffer buffer) throws IOException {
 		File f = new File(file, name);
 		FileOutputStream fos = new FileOutputStream(f);
 		FileChannel fc = fos.getChannel();
		fc.write(buffer);
 		return new FileResource(providor, f);
 	}
 
 	@Override
 	public Resource child(String name) {
 		return new FileResource(providor, new File(file, name));
 	}
 
 	@Override
 	public Iterable<Resource> children() {
 		return Iterables.transform(filterHidden(childFiles()), new Function<File, Resource>() {
 
 			@Override
 			public Resource apply(File file) {
 				return new FileResource(providor, file);
 			}
 		});
 	}
 
 	private Iterable<File> childFiles() {
 		return Arrays.asList(file.listFiles());
 	}
 
 	private Iterable<File> filterHidden(Iterable<File> files) {
 		return files;
 	}
 
 	@Override
 	public boolean moveTo(CollectionResource destination) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Element property(QName name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Iterable<Element> properties() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean makeCollection(String name) {
 		return new File(file, name).mkdir();
 	}
 
 	@Override
 	public void lock() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void unlock() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public ByteBuffer body() throws IOException {
 		FileChannel channel = channel();
		ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, size());
 		channel.close();
 		return buffer;
 	}
 
 }
