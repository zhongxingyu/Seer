 package yapto.swing;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import yapto.picturebank.IPicture;
 import yapto.picturebank.IPictureBank;
 import yapto.picturebank.IPictureBrowser;
 import yapto.picturebank.PictureBankList;
 import yapto.picturebank.PictureBrowserChangedEvent;
 import yapto.picturebank.PictureChangedEvent;
 
 import com.google.common.eventbus.Subscribe;
 
 /**
  * Component used to display a picture.
  * 
  * @author benobiwan
  * 
  */
 public final class PictureDisplayComponent extends JScrollPane
 {
 	/**
 	 * serialVersionUID for Serialization.
 	 */
 	private static final long serialVersionUID = 6664591741974259719L;
 
 	/**
 	 * Logger object.
 	 */
 	protected static transient final Logger LOGGER = LoggerFactory
 			.getLogger(PictureDisplayComponent.class);
 
 	/**
 	 * The {@link DisplayPane} used to display the picture.
 	 */
 	private final DisplayPane _displayPane;
 
 	/**
 	 * Creates a new {@link PictureDisplayComponent}.
 	 * 
 	 * @param bankList
 	 *            the {@link PictureBankList} used to load the
 	 *            {@link IPictureBank} used as source for the {@link IPicture}.
 	 */
 	public PictureDisplayComponent(final PictureBankList bankList)
 	{
 		super();
 		_displayPane = new DisplayPane(bankList);
 		setViewportView(_displayPane);
 	}
 
 	/**
 	 * Load the picture to display.
 	 * 
 	 * @throws IOException
 	 *             if an error occurs during reading.
 	 */
 	public void loadPicture() throws IOException
 	{
 		_displayPane.loadPicture();
 	}
 
 	/**
 	 * Change the zoomType of the picture.
 	 * 
 	 * @param zoomType
 	 *            the new zoomType.
 	 * @param zoomScale
 	 *            the scale associated with the zoom type. Required for
 	 *            WINDOW_PERCENTAGE and PICTURE_PERCENTAGE zoom type.
 	 * @param zoomDimension
 	 *            the {@link Dimension} associated with the zoom type. Required
 	 *            for SPECIFIC_SIZE zoom type.
 	 */
 	public void changePictureZoomType(final PictureZoomType zoomType,
 			final double zoomScale, final Dimension zoomDimension)
 	{
 		_displayPane.changePictureZoomType(zoomType, zoomScale, zoomDimension);
 	}
 
 	/**
 	 * Method called when the selected picture has changed.
 	 * 
 	 * @param ev
 	 *            the event signaling the change of the picture.
 	 */
 	@Subscribe
 	public void handlePictureChanged(
 			@SuppressWarnings("unused") final PictureChangedEvent ev)
 	{
 		try
 		{
 			_displayPane.loadPicture();
 		}
 		catch (final IOException ex)
 		{
 			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
 					JOptionPane.ERROR_MESSAGE);
 			LOGGER.error(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * {@link JComponent} that actually displays the picture.
 	 * 
 	 * @author benobiwan
 	 */
 	private final class DisplayPane extends JComponent
 	{
 		/**
 		 * serialVersionUID for Serialization.
 		 */
 		private static final long serialVersionUID = 7055585500143733186L;
 
 		/**
 		 * The picture to display on this component.
 		 */
 		private BufferedImage _img;
 
 		/**
 		 * The configured type of zoom used to display the picture.
 		 */
 		private PictureZoomType _zoomType = PictureZoomType.WINDOW_DIMENSION;
 
 		/**
 		 * the scale associated with the zoom type. Required for
 		 * WINDOW_PERCENTAGE and PICTURE_PERCENTAGE zoom type.
 		 */
 		private double _zoomScale;
 
 		/**
 		 * the {@link Dimension} associated with the zoom type. Required for
 		 * SPECIFIC_SIZE zoom type.
 		 */
 		private Dimension _zoomDimension;
 
 		/**
 		 * The {@link PictureBankList} used to load the {@link IPictureBank}
 		 * used as source for the {@link IPicture}.
 		 */
 		private final PictureBankList _bankList;
 
 		/**
 		 * The {@link IPictureBrowser} used to display picture on this
 		 * {@link PictureDisplayComponent}.
 		 */
 		private IPictureBrowser<? extends IPicture> _pictureBrowser;
 
 		/**
 		 * Lock protecting access to the {@link IPictureBrowser}.
 		 */
 		private final Object _lockBrowser = new Object();
 
 		/**
 		 * Memory of the last size used to scale the image.
 		 */
 		private Dimension _size;
 
 		/**
 		 * Memory of the last scale factor used to scale the image.
 		 */
 		private double _dScaleFactor;
 
 		/**
 		 * The AffineTransform used to scale the image.
 		 */
 		private AffineTransform _transform;
 
 		/**
 		 * Creates a new {@link DisplayPane}.
 		 * 
 		 * @param bankList
 		 *            the {@link PictureBankList} used to load the
 		 *            {@link IPictureBank} used as source for the
 		 *            {@link IPicture}.
 		 */
 		public DisplayPane(final PictureBankList bankList)
 		{
 			_bankList = bankList;
 			changePictureBrowser();
 			_bankList.register(this);
 		}
 
 		/**
 		 * Load the picture to display.
 		 * 
 		 * @throws IOException
 		 *             if an error occurs during reading.
 		 */
 		public void loadPicture() throws IOException
 		{
 			IPicture pic;
 			synchronized (_lockBrowser)
 			{
 				pic = _pictureBrowser.getCurrentPicture();
 			}
 			if (pic != null)
 			{
 				_img = pic.getImageData();
 				_transform = null;
 				switch (_zoomType)
 				{
 				case REAL_SIZE:
 					setPreferredSize(new Dimension(_img.getWidth(),
 							_img.getHeight()));
 					break;
 				case WINDOW_DIMENSION:
 					setPreferredSize(PictureDisplayComponent.this.getSize());
 					break;
 				case SCALE_DOWN_TO_WINDOW:
 					setPreferredSize(PictureDisplayComponent.this.getSize());
 					break;
 				case PICTURE_PERCENTAGE:
 					// TODO PICTURE_PERCENTAGE size implementation
 					break;
 				case SPECIFIC_SIZE:
 					setPreferredSize(_zoomDimension);
 					break;
 				case WINDOW_PERCENTAGE:
 					// TODO WINDOW_PERCENTAGE size implementation
 					break;
 				default:
 					break;
 				}
 				repaint();
 			}
 		}
 
 		@Override
 		public void paint(final Graphics g)
 		{
 			final Graphics2D g2 = (Graphics2D) g;
 			switch (_zoomType)
 			{
 			case REAL_SIZE:
 				g.drawImage(_img, 0, 0, null);
 				break;
 			case WINDOW_DIMENSION:
 				changeTransform(PictureDisplayComponent.this.getSize());
 				g2.drawImage(_img, _transform, null);
 				break;
 			case SCALE_DOWN_TO_WINDOW:
 				if (_img.getWidth() > PictureDisplayComponent.this.getWidth()
 						|| _img.getHeight() > PictureDisplayComponent.this
 								.getHeight())
 				{
 					changeTransform(PictureDisplayComponent.this.getSize());
 					g2.drawImage(_img, _transform, null);
 				}
 				else
 				{
 					g.drawImage(_img, 0, 0, null);
 				}
 				break;
 			case PICTURE_PERCENTAGE:
 				changeTransform(_zoomScale);
 				g2.drawImage(_img, _transform, null);
 				break;
 			case SPECIFIC_SIZE:
 				changeTransform(_zoomDimension);
 				g2.drawImage(_img, _transform, null);
 				break;
 			case WINDOW_PERCENTAGE:
 				// TODO : WINDOW_PERCENTAGE size implementation
 				break;
 			default:
 				break;
 			}
 		}
 
 		/**
 		 * Change the AffineTransform to fit the specified size.
 		 * 
 		 * @param size
 		 *            the size to match.
 		 */
 		private void changeTransform(final Dimension size)
 		{
 			if (_size == null || _transform == null || !_size.equals(size))
 			{
 				_size = size;
 				final double dScaleFactor = Math.min(
 						size.getWidth() / _img.getWidth(), size.getHeight()
 								/ _img.getHeight());
 				changeTransform(dScaleFactor);
 			}
 		}
 
 		/**
 		 * Change the AffineTransform to fit the specified scale factor.
 		 * 
 		 * @param dScaleFactor
 		 *            the scale factor to match.
 		 */
 		private void changeTransform(final double dScaleFactor)
 		{
 			if (dScaleFactor != _dScaleFactor || _transform == null
 					&& dScaleFactor > 0)
 			{
 				_dScaleFactor = dScaleFactor;
 				_transform = AffineTransform.getScaleInstance(_dScaleFactor,
 						_dScaleFactor);
 			}
 		}
 
 		/**
 		 * Change the zoomType of the picture.
 		 * 
 		 * @param zoomType
 		 *            the new zoomType.
 		 * @param zoomScale
 		 *            the scale associated with the zoom type. Required for
 		 *            WINDOW_PERCENTAGE and PICTURE_PERCENTAGE zoom type.
 		 * @param zoomDimension
 		 *            the {@link Dimension} associated with the zoom type.
 		 *            Required for SPECIFIC_SIZE zoom type.
 		 */
 		public void changePictureZoomType(final PictureZoomType zoomType,
 				final double zoomScale, final Dimension zoomDimension)
 		{
 			switch (zoomType)
 			{
 			case REAL_SIZE:
 				_zoomScale = 0;
 				_zoomDimension = null;
 				break;
 			case WINDOW_DIMENSION:
 				_zoomScale = 0;
 				_zoomDimension = null;
 				break;
 			case SCALE_DOWN_TO_WINDOW:
 				_zoomScale = 0;
 				_zoomDimension = null;
 				break;
 			case SPECIFIC_SIZE:
 				if (zoomDimension == null)
 				{
 					throw new IllegalArgumentException(
 							"zoomDimension value can't be null for SPECIFIC_SIZE zoom type");
 				}
 				_zoomScale = 0;
 				_zoomDimension = zoomDimension;
 				break;
 			case PICTURE_PERCENTAGE:
 				if (zoomScale <= 0)
 				{
 					throw new IllegalArgumentException(
 							"Illegal scale value for PICTURE_PERCENTAGE zoom type : "
 									+ zoomScale);
 				}
 				_zoomScale = zoomScale;
 				_zoomDimension = null;
 				break;
 			case WINDOW_PERCENTAGE:
 				if (zoomScale <= 0)
 				{
 					throw new IllegalArgumentException(
 							"Illegal scale value for WINDOW_PERCENTAGE zoom type : "
 									+ zoomScale);
 				}
 				_zoomScale = zoomScale;
 				_zoomDimension = null;
 				break;
 			default:
 				break;
 			}
 			_zoomType = zoomType;
 		}
 
 		/**
 		 * Handle a {@link PictureBrowserChangedEvent} by changing the current
 		 * {@link IPictureBrowser}.
 		 * 
 		 * @param ev
 		 *            the event to handle.
 		 */
 		@Subscribe
 		public void handlePictureBrowserChangedEvent(
 				@SuppressWarnings("unused") final PictureBrowserChangedEvent ev)
 		{
 			changePictureBrowser();
 		}
 
 		/**
 		 * Change the {@link IPictureBrowser}.
 		 */
 		public void changePictureBrowser()
 		{
 			synchronized (_lockBrowser)
 			{
 				if (_pictureBrowser != null)
 				{
					_pictureBrowser.unRegister(PictureDisplayComponent.this);
 				}
 				_pictureBrowser = _bankList.getLastSelectPictureBrowser();
 				if (_pictureBrowser != null)
 				{
					_pictureBrowser.register(PictureDisplayComponent.this);
 				}
 			}
 		}
 	}
 }
