 package pt.utl.ist.fenix.tools.file.filters;
 
 import java.io.File;
 import java.util.Collection;
 
 import pt.utl.ist.fenix.tools.file.FileSet;
 import pt.utl.ist.fenix.tools.file.FileSetMetaData;
import sun.net.www.MimeEntry;
 import sun.net.www.MimeTable;
 
 public class SimpleFileSetFilter extends RecursiveFileSetFilter {
 
 	public SimpleFileSetFilter() {
 		super();
 	}
 
 	@Override
 	public void handleFileSetLevel(FileSet leveledFs) throws FileSetFilterException {
 		
 		Collection<File> supposedFiles=leveledFs.getContentFiles();
 		if(supposedFiles!=null && supposedFiles.size()!=0)
 		{
 			File supposedFile=supposedFiles.toArray(new File[0])[0];
 			if(supposedFile.exists() && supposedFile.canRead())
 			{
				MimeEntry findByFileName = MimeTable.getDefaultTable().findByFileName(supposedFile.getName());
				String mimeType = (findByFileName==null) ? "application/octet-stream" :findByFileName.getType(); 
 				leveledFs.addMetaInfo(new FileSetMetaData("format","extent",null,""+supposedFile.length()));
 				leveledFs.addMetaInfo(new FileSetMetaData("format","mimetype",null,mimeType));
 			}
 		}
 	}
 
 }
