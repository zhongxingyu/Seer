 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.data;
 
 import jp.ac.osaka_u.ist.sdl.ectec.db.data.BlockType;
 
import org.eclipse.jdt.core.dom.TypeDeclaration;
 
 /**
  * A class that represents methods
  * 
  * @author k-hotta
  * 
  */
public class MethodInfo extends BlockInfo<TypeDeclaration> {
 
 	public MethodInfo(long id, FileInfo ownerFile, CRD crd,
 			RevisionInfo startRevision, RevisionInfo endRevision,
			int startLine, int endLine, int size, TypeDeclaration node) {
 		super(id, ownerFile, crd, startRevision, endRevision, startLine,
 				endLine, size, BlockType.METHOD, node);
 	}
 
 }
