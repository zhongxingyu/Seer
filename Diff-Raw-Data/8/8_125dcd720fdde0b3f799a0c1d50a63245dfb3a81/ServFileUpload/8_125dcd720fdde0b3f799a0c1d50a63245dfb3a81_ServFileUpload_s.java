 package com.huhuo.cmsystem.file;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import javax.annotation.Resource;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.springframework.stereotype.Service;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.huhuo.carservicecore.db.GenericBaseExtenseServ;
 import com.huhuo.carservicecore.sys.file.IDaoFileUpload;
 import com.huhuo.carservicecore.sys.file.ModelFileUpload;
 import com.huhuo.cmsystem.constant.Constant;
 import com.huhuo.cmsystem.constant.Constant.Suffix;
 import com.huhuo.integration.algorithm.MD5Utils;
 import com.huhuo.integration.base.IBaseExtenseDao;
 import com.huhuo.integration.exception.ServException;
 import com.huhuo.integration.util.FileUtils;
 import com.huhuo.integration.util.TimeUtils;
 
 @Service("cmsystemServFileUpload")
 public class ServFileUpload extends GenericBaseExtenseServ<ModelFileUpload> implements IServFileUpload {
 
 	@Resource(name = "carservicecoreDaoFileUpload")
 	private IDaoFileUpload<ModelFileUpload> iDaoFileUpload;
 	
 	private static ServletContextEvent sce;
 	
 	/** servlet context **/
 	private static ServletContext ctx;
 	/** file upload cache directory */
 	private static String cachedPath = "file/upload/cached";
 	/** file upload persist directory which can be access by url */
 	private static String webResPath = "file/upload";
 	/** default upload file store directory */
 	private String persistPath = Constant.FILE_UPLOAD_PERSIST_PATH;
 	/** default file separator */
 	private String fileSeparator = File.separator;
 	
 	
 	public static void setSce(ServletContextEvent sce) {
 		ServFileUpload.sce = sce;
 		ctx = ServFileUpload.sce.getServletContext();
 	}
 
 	@Override
 	public IBaseExtenseDao<ModelFileUpload> getDao() {
 		// TODO Auto-generated method stub
 		return iDaoFileUpload;
 	}
 
 	@Override
 	public Class<ModelFileUpload> getModelClazz() {
 		// TODO Auto-generated method stub
 		return ModelFileUpload.class;
 	}
 
 	@Override
 	public ModelFileUpload uploadCacheFile(MultipartFile uploadFile) {
 		// TODO Auto-generated method stub
 		ModelFileUpload ret = new ModelFileUpload();
 		try {
 			byte[] bytes = uploadFile.getBytes();
 			// get md5 value by file's input stream
 			String fileName = MD5Utils.encodeHex(bytes) + FileUtils.DEFFAULT_MARKER + 
 					FileUtils.getSuffix(uploadFile.getOriginalFilename());
 			ret.setName(uploadFile.getOriginalFilename());
 			ret.setPath(cachedPath);
 			ret.setMd5(fileName);
 			StringBuilder sb = new StringBuilder();
 			sb.append(ctx.getRealPath(cachedPath)).append(fileSeparator).append(fileName);
 			File file = new File(sb.toString());
 			if(file.exists()) {
 				throw new ServException("file exist");
 			}
 			logger.info("upload file --> {}", file);
 			FileUtils.writeByteArrayToFile(file, bytes);
 		} catch (IOException e) {
 			logger.warn(ExceptionUtils.getStackTrace(e));
 			throw new ServException(e.getMessage() + " --> get upload file byte[] error", e);
 		}
 		return ret;
 	}
 
 	@Override
 	public ModelFileUpload uploadFile(ModelFileUpload t) {
 		try {
 			File srcFile = new File(ctx.getRealPath(cachedPath + fileSeparator + t.getMd5()));
			if(srcFile.exists()) {
 				// copy file in cached directory to persist directory
 				String firstLevel = TimeUtils.format(new Date(), false);
 				File destDir = new File(ctx.getRealPath(webResPath + fileSeparator + firstLevel));
 				FileUtils.copyFileToDirectory(srcFile, destDir, true);
 				// move file in persist directory
 				destDir = new File(persistPath + fileSeparator + webResPath + fileSeparator + firstLevel);
 				FileUtils.copyFileToDirectory(srcFile, destDir, true);
 				srcFile.delete();
 				String accessPath = webResPath + fileSeparator + firstLevel;
 				// update DB
 				t.setPath(accessPath);
 				String suffix = FileUtils.getSuffix(t.getMd5());
 				Suffix type = Suffix.valueOf(suffix.toUpperCase());
 				t.setType(type.getValue());
 				save(t);
 			}
 		} catch (IOException e) {
 			logger.warn(ExceptionUtils.getStackTrace(e));
 			throw new ServException(e.getMessage() + " --> error while copy file");
 		} catch (IllegalArgumentException e) {
 			logger.warn(ExceptionUtils.getStackTrace(e));
 			throw new ServException(e.getMessage() + " --> unsupported suffix type");
 		}
 		return t;
 	}
 
 	@Override
 	public Boolean save(ModelFileUpload t) {
 		if(t == null)
 			throw new ServException("==> model t can't be null");
 		
 		ModelFileUpload tDB = find(t.getId());
 		if(tDB != null) {
 			String relatePath = tDB.getPath() + fileSeparator + tDB.getMd5();
 			File obsoleteFileInWebapp = new File(ctx.getRealPath(relatePath));
 			boolean status = obsoleteFileInWebapp.delete();
 			File bosoleteFileInPersist = new File(persistPath + fileSeparator + relatePath);
 			boolean status2 = bosoleteFileInPersist.delete();
 			logger.info("==> delete obsolete file in webapp {} --> {}", 
 					status ? "success!" : "failure!", obsoleteFileInWebapp);
 			logger.info("==> delete obsolete file in webapp {} --> {}", 
 					status2 ? "success!" : "failure!", bosoleteFileInPersist);
 			update(t);
 		} else {
 			add(t);
 		}
 		return true;
 	}
 	
 }
