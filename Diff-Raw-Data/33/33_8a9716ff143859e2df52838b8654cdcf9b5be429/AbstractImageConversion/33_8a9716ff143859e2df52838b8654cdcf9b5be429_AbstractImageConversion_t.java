 package org.dawnsci.conversion.converters;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.eclipse.dawnsci.plotting.api.PlotType;
 import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
 import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;
 
 public abstract class AbstractImageConversion extends AbstractConversion {
 
 	int imageCounter = 0;
 	
 	public AbstractImageConversion(IConversionContext context) {
 		super(context);
 	}
	
	protected void iterate(final ILazyDataset         lz, 
            final String               nameFrag,
            final IConversionContext   context) throws Exception {
		
		imageCounter = 0;
		
		super.iterate(lz, nameFrag, context);
	}
 
 	/**
 	 * Please override getExtension() if using getFileName(...)
 	 * @return
 	 */
 	protected final String getFilePath(IDataset slice) {
 		final String sliceFileName = getFileName(slice);
 		
 		final File selectedFile = context.getSelectedConversionFile();
 		final String fileNameFrag = selectedFile!=null ? getFileNameNoExtension(selectedFile)+"/" : "";
 		
 		return context.getOutputPath()+"/"+fileNameFrag+sliceFileName;
 	}
 
 	//private Pattern INDEX_PATTERN = Pattern.compile("(.+index=)(\\d+)\\)");
 	
 	private String getFileName(IDataset slice) {
 		
 		String fileName = slice.getName();
 		if (context.getUserObject()!=null) {
 			ConversionInfoBean bean = (ConversionInfoBean)context.getUserObject();
 			
 			String namePrefix = null;
 			if (bean.getAlternativeNamePrefix()!=null && !"".equals(bean.getAlternativeNamePrefix())) {
 				namePrefix = bean.getAlternativeNamePrefix();
 			}
 			
 			// Used to be: fileName example " data(Dim 0; index=0) "
 			// now contains full slice information ie data (0,:2048,:2048)
 			// so the old method of parsing the data name no longer works
 			if (bean.getSliceIndexFormat()!=null) {
 //				final Matcher matcher = INDEX_PATTERN.matcher(fileName);
 //				if (matcher.matches()) {
 //					final NumberFormat format = new DecimalFormat(bean.getSliceIndexFormat());
 //					namePrefix = namePrefix!=null ? namePrefix :  matcher.group(1);
 //					fileName   = namePrefix+format.format(Integer.parseInt(matcher.group(2)))+")";
 //				}
 				final NumberFormat format = new DecimalFormat(bean.getSliceIndexFormat());
 				fileName   = namePrefix+format.format(imageCounter++);
 				
 			}
 		}
 		fileName = fileName.replace('\\', '_');
 		fileName = fileName.replace('/', '_');
 		fileName = fileName.replace(' ', '_');
 		fileName = fileName.replace('(', '_');
 		fileName = fileName.replaceAll("[^a-zA-Z0-9_]", "");
 		
 		if (context.getFilePaths().size()>1 && context.getSelectedConversionFile()!=null) {
 			final String originalName = getFileNameNoExtension(context.getSelectedConversionFile());
 			fileName  = originalName+"_"+fileName;
 		}
 		
 		return fileName+"."+getExtension();
 	}
 
 	/**
 	 * Please override if using getFileName
 	 * @return
 	 */
 	protected String getExtension() {
 		return "tiff";
 	}
 
 	protected int getDownsampleBin() {
 		if (context.getUserObject()==null) return 1;
         return ((ConversionInfoBean)context.getUserObject()).getDownsampleBin();
 	}
 	protected DownsampleMode getDownsampleMode() {
 		if (context.getUserObject()==null) return DownsampleMode.MAXIMUM;
         return ((ConversionInfoBean)context.getUserObject()).getDownsampleMode();
 	}
 	
 	protected IDataset getDownsampled(IDataset slice) {
 		final String name = slice.getName();
 		if (getDownsampleBin()>1) {
 			final Downsample down = new Downsample(getDownsampleMode(), getDownsampleBin(), getDownsampleBin());
 			slice = down.value(slice).get(0);
 			slice.setName(name);
 		}
 		return slice;
 	}
 	
 	protected Enum getSliceType() {
 		if (context.getUserObject()==null) return null;
         return ((ConversionInfoBean)context.getUserObject()).getSliceType();
 	}
 	protected ImageServiceBean getImageServiceBean() {
 		if (context.getUserObject()==null) return null;
         return ((ConversionInfoBean)context.getUserObject()).getImageServiceBean();
 	}
 	protected boolean isAlwaysShowTitle() {
 		if (context.getUserObject()==null) return false;
         return ((ConversionInfoBean)context.getUserObject()).isAlwaysShowTitle();
 	}
 	
 	/**
 	 * To be used as the user object to convey data about the conversion.
 	 * @author fcp94556
 	 *
 	 */
 	public static final class ConversionInfoBean {
 		
 		private Enum sliceType=PlotType.IMAGE;
 		private int frameRate = 20;
 		private ImageServiceBean imageServiceBean;
 		private int downsampleBin=1;
 		private DownsampleMode downsampleMode=DownsampleMode.MAXIMUM;
 		private String alternativeNamePrefix;
 		private String extension = "tiff";
 		private int    bits      = 33;
 		private String sliceIndexFormat = "#000";
 		private boolean alwaysShowTitle = false;
 		
 		public String getExtension() {
 			return extension;
 		}
 		public void setExtension(String extension) {
 			this.extension = extension;
 		}
 		public int getBits() {
 			return bits;
 		}
 		public void setBits(int bits) {
 			this.bits = bits;
 		}
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime
 					* result
 					+ ((alternativeNamePrefix == null) ? 0
 							: alternativeNamePrefix.hashCode());
 			result = prime * result + (alwaysShowTitle ? 1231 : 1237);
 			result = prime * result + bits;
 			result = prime * result + downsampleBin;
 			result = prime
 					* result
 					+ ((downsampleMode == null) ? 0 : downsampleMode.hashCode());
 			result = prime * result
 					+ ((extension == null) ? 0 : extension.hashCode());
 			result = prime * result + frameRate;
 			result = prime
 					* result
 					+ ((imageServiceBean == null) ? 0 : imageServiceBean
 							.hashCode());
 			result = prime
 					* result
 					+ ((sliceIndexFormat == null) ? 0 : sliceIndexFormat
 							.hashCode());
 			result = prime * result
 					+ ((sliceType == null) ? 0 : sliceType.hashCode());
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
 			ConversionInfoBean other = (ConversionInfoBean) obj;
 			if (alternativeNamePrefix == null) {
 				if (other.alternativeNamePrefix != null)
 					return false;
 			} else if (!alternativeNamePrefix
 					.equals(other.alternativeNamePrefix))
 				return false;
 			if (alwaysShowTitle != other.alwaysShowTitle)
 				return false;
 			if (bits != other.bits)
 				return false;
 			if (downsampleBin != other.downsampleBin)
 				return false;
 			if (downsampleMode != other.downsampleMode)
 				return false;
 			if (extension == null) {
 				if (other.extension != null)
 					return false;
 			} else if (!extension.equals(other.extension))
 				return false;
 			if (frameRate != other.frameRate)
 				return false;
 			if (imageServiceBean == null) {
 				if (other.imageServiceBean != null)
 					return false;
 			} else if (!imageServiceBean.equals(other.imageServiceBean))
 				return false;
 			if (sliceIndexFormat == null) {
 				if (other.sliceIndexFormat != null)
 					return false;
 			} else if (!sliceIndexFormat.equals(other.sliceIndexFormat))
 				return false;
 			if (sliceType == null) {
 				if (other.sliceType != null)
 					return false;
 			} else if (!sliceType.equals(other.sliceType))
 				return false;
 			return true;
 		}
 		public String getAlternativeNamePrefix() {
 			return alternativeNamePrefix;
 		}
 		public void setAlternativeNamePrefix(String alternativeNamePrefix) {
 			this.alternativeNamePrefix = alternativeNamePrefix;
 		}
 		public int getDownsampleBin() {
 			return downsampleBin;
 		}
 		public void setDownsampleBin(int downsampleBin) {
 			this.downsampleBin = downsampleBin;
 		}
 		public DownsampleMode getDownsampleMode() {
 			return downsampleMode;
 		}
 		public void setDownsampleMode(DownsampleMode downsampleMode) {
 			this.downsampleMode = downsampleMode;
 		}
 		public ImageServiceBean getImageServiceBean() {
 			return imageServiceBean;
 		}
 		public void setImageServiceBean(ImageServiceBean imageServiceBean) {
 			this.imageServiceBean = imageServiceBean;
 		}
 		public int getFrameRate() {
 			return frameRate;
 		}
 		public void setFrameRate(int frameRate) {
 			this.frameRate = frameRate;
 		}
 		public String getSliceIndexFormat() {
 			return sliceIndexFormat;
 		}
 		public void setSliceIndexFormat(String sliceIndexFormat) {
 			this.sliceIndexFormat = sliceIndexFormat;
 		}
 		public Enum getSliceType() {
 			return sliceType;
 		}
 		public void setSliceType(Enum sliceType) {
 			this.sliceType = sliceType;
 		}
 		public boolean isAlwaysShowTitle() {
 			return alwaysShowTitle;
 		}
 		public void setAlwaysShowTitle(boolean alwaysShowTitle) {
 			this.alwaysShowTitle = alwaysShowTitle;
 		}
 	}
 
 }
