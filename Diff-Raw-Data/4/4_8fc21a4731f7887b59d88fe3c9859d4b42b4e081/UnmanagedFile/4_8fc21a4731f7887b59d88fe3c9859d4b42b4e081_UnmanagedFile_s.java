 package org.magnolialang.resources;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
 import org.eclipse.imp.pdb.facts.visitors.VisitorException;
 import org.rascalmpl.parser.gtd.io.InputConverter;
 import org.rascalmpl.uri.URIUtil;
 
 public class UnmanagedFile implements IManagedFile {
 	protected final File file;
 
 	protected final URI uri;
 
 
 	public UnmanagedFile(File file) throws URISyntaxException {
 		this.file = file;
 		this.uri = URIUtil.createFile(file.getPath());
 	}
 
 
 	@Override
 	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public char[] getContentsCharArray() throws IOException {
 		InputStream stream = getContentsStream();
 		try {
 			char[] cs = InputConverter.toChar(stream, Charset.forName("UTF-8"));
 			return cs;
 		}
 		finally {
 			stream.close();
 		}
 	}
 
 
 	@Override
 	public InputStream getContentsStream() throws IOException {
 		return new FileInputStream(file);
 	}
 
 
 	@Override
 	public String getContentsString() throws IOException {
 		InputStream stream = getContentsStream();
 		try {
 			String string = new String(InputConverter.toChar(stream, Charset.forName("UTF-8")));
 			return string;
 		}
 		finally {
 			stream.close();
 		}
 	}
 
 
 	@Override
 	public long getModificationStamp() {
 		return file.lastModified();
 	}
 
 
 	@Override
 	public IManagedResource getParent() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public Type getType() {
 		return IManagedResource.ResourceType;
 	}
 
 
 	@Override
 	public URI getURI() {
 		return uri;
 	}
 
 
 	@Override
 	public boolean isCodeUnit() {
 		return false;
 	}
 
 
 	@Override
 	public boolean isContainer() {
 		return false;
 	}
 
 
 	@Override
 	public boolean isEqual(IValue other) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public boolean isFile() {
 		return true;
 	}
 
 
 	@Override
 	public boolean isProject() {
 		return false;
 	}
 
 
 	@Override
 	public void onResourceChanged() {
 	}
 
 
 	@Override
 	public boolean setContents(String contents) throws IOException {
 		return false;
 	}
 
 }
