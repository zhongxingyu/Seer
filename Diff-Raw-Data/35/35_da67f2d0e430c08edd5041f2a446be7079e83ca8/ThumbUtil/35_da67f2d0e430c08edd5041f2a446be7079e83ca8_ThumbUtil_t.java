 package org.eweb4j.util;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.coobird.thumbnailator.Thumbnails;
 import net.coobird.thumbnailator.geometry.Positions;
 
 import org.eweb4j.util.FileUtil;
 
 /**
  * 缩略图工具类
  * @author weiwei
  *
  */
 public class ThumbUtil {
 
	private final String W = "width";
	private final String H = "height";
	//原图宽高
	private Map<String,Integer> source = new HashMap<String,Integer>();
	//目标宽高
	private Map<String,Integer> output = new HashMap<String, Integer>();
 	
 	/**
 	 * 注意！当宽度和高度都给定的情况下会进行裁剪。裁剪规则是：先按照比例压缩，然后将多出的部分分两边裁剪。
 	 * @param remoteImageUrl 远程图片URL
 	 * @param outputFormat 希望生成的缩略图格式
 	 * @param failRetryTimes 远程图片下载失败尝试次数
 	 * @param outputWidth 希望生成的缩略图宽度
 	 * @param outputHeight 希望生成的缩略图高度
 	 * @return
 	 * @throws Exception
 	 */
 	public ByteArrayOutputStream generateThumb(String remoteImageUrl, String outputFormat, int failRetryTimes, long sleep, int outputWidth, int outputHeight) throws Exception{
 		if (remoteImageUrl == null || remoteImageUrl.trim().length() == 0)
 			throw new Exception("ImageURL required");
 		
 		if (outputFormat == null || outputFormat.trim().length() == 0)
 			outputFormat = remoteImageUrl.substring(remoteImageUrl.lastIndexOf(".")+1, remoteImageUrl.length());
 		
 		if (outputFormat == null || outputFormat.trim().length() == 0)
 			throw new Exception("can not get the image suffix -> " + remoteImageUrl);
 		
 		if (failRetryTimes <= 0)
 			failRetryTimes = 1;
 		
 		if (outputWidth <= 0 && outputHeight <= 0)
 			throw new Exception("outputWidth and outputHeight must have one");
 		
 		if (outputWidth > 0)
			output.put(W, outputWidth);
 		
 		if (outputHeight > 0)
			output.put(H, outputHeight);
 		
 		BufferedImage bi = null;
 		try {
 			bi = FileUtil.getBufferedImage(remoteImageUrl, failRetryTimes, sleep);
 		}catch(Exception e){
 			throw e;
 		}
 		
 		if (bi == null)
 			throw new Exception("can not get the image from website");
 		
 		//比较W与H，找出小的，记住小的那个
 		int w = bi.getWidth();
 		int h = bi.getHeight();
		source.put(W, w);
		source.put(H, h);
		
 		String min = W;
 		if (h < w)
 			min = H;
		// 如果小值不存在，则小值取给过来的其中一个值
		if (!output.containsKey(min)){
			if (output.containsKey(W))
 				min = W;
 			else
 				min = H;
 		}
 		
 		//算出比例
		double scale = (double)source.get(min)/output.get(min);
 		int sW = new Double(w/scale).intValue();
 		int sH = new Double(h/scale).intValue();
 		
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		
 		//如果给了两个参数，则剪裁
		if (output.containsKey(W) && output.containsKey(H)){
 			//压缩
 			BufferedImage _bi = Thumbnails.of(bi).size(sW, sH).outputFormat(outputFormat).asBufferedImage();
 			//scale必须为 1 的时候图片才不被放大
			Thumbnails.of(_bi).scale(1).sourceRegion(Positions.CENTER, output.get(W), output.get(H)).outputFormat(outputFormat).toOutputStream(os);
 			
 		}else{
 			//压缩
 			Thumbnails.of(bi).size(sW, sH).outputFormat(outputFormat).toOutputStream(os);
 		}
 		
 		return os;
 	}
 	
 	public  void main(String[] args) throws Exception{
 		String format = "jpg";
 		String imageUrl = "http://.zalora.sg/p/chuck-bo-korea-selection-7428-72957-1-zoom.jpg";
 		HashMap<String, Integer> wh = new HashMap<String, Integer>();
 		wh.put(W, 270);
 		wh.put(H, 180);
 		
 		BufferedImage bi = FileUtil.getBufferedImage(imageUrl, 5, 1*1000);
 		if (bi == null)
 			throw new Exception("can not get the image from website");
 		//比较W与H，找出小的，记住小的那个
 		int w = bi.getWidth();
 		int h = bi.getHeight();
 		String min = W;
 		if (h < w)
 			min = H;
 		// 如果小值不存在，则小值取给过来的值
 		if (!wh.containsKey(min)){
 			if (wh.containsKey(W))
 				min = W;
 			else
 				min = H;
 		}
 		
 		//算出比例
 		double scale = (double)w/wh.get(min);
 		int sW = new Double(w/scale).intValue();
 		int sH = new Double(h/scale).intValue();
 		System.out.println(sW+", "+sH);
 		Thumbnails.of(bi).size(sW, sH).outputFormat(format).toFile("d:/fuck2.jpg");
 		//如果给了两个参数，则剪裁
 		if (wh.containsKey(W) && wh.containsKey(H)){
 			//压缩
 			BufferedImage _bi = Thumbnails.of(bi).size(sW, sH).outputFormat(format).asBufferedImage();
 			Thumbnails.of(_bi).scale(1).sourceRegion(Positions.CENTER, wh.get(W), wh.get(H)).outputFormat(format).toFile("d:/fuck1.jpg");
 			
 		}else{
 			//压缩
 			Thumbnails.of(bi).size(sW, sH).outputFormat(format).toFile("d:/fuck2.jpg");
 		}
 		
 	}
 	
 }
