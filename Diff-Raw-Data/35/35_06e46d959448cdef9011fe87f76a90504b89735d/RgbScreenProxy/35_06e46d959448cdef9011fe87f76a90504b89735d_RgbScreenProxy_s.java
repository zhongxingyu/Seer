 package ch.bittailor.bt.ui;
 
 import java.awt.Color;
 
 import ch.bittailor.bt.com.IOutputPackage;
 import ch.bittailor.bt.com.IRequestClient;
 
 public class RgbScreenProxy implements IRgbScreen {
 
 	enum Cmd {
 		WIDTH,
 		HEIGHT,
 		SET_PIXEL,
 		FILL,
 		REPAINT
 	};
 
 	private IRequestClient mClient;
 	private int mWidth;
 	private int mHeight;
 
 
 
 	public RgbScreenProxy(IRequestClient iRequestClient) {
 		this.mClient = iRequestClient;
		mWidth = 0;
		mHeight = 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.bittailor.bt.ui.IRgbScreen#width()
 	 */
 	@Override
 	public int width() {
 		if (mWidth == 0) {
 			mClient.out().writeUInt8(Cmd.WIDTH.ordinal()); 
 			mClient.sendQueryRequest();
 			mWidth = mClient.in().readUInt8();
 		}
 		return mWidth;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.bittailor.bt.ui.IRgbScreen#height()
 	 */
 	@Override
 	public int height() {      
 		if (mHeight == 0) {
 			mClient.out().writeUInt8(Cmd.HEIGHT.ordinal()); 
 			mClient.sendQueryRequest();
 			mHeight = mClient.in().readUInt8();
 		}
 		return mHeight;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.bittailor.bt.ui.IRgbScreen#setPixel(int, int, java.awt.Color)
 	 */
 	@Override
 	public void setPixel(int iX, int iY, Color iColor) {
 		mClient.out().writeUInt8(Cmd.SET_PIXEL.ordinal()).writeUInt8(iX).writeUInt8(iY);
 		addColor(mClient.out(),iColor);
 		mClient.sendActionRequest();
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.bittailor.bt.ui.IRgbScreen#fill(java.awt.Color)
 	 */
 	@Override
 	public void fill(Color iColor) {
 		mClient.out().writeUInt8(Cmd.FILL.ordinal());
 		addColor(mClient.out(),iColor);
 		mClient.sendActionRequest();
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.bittailor.bt.ui.IRgbScreen#repaint()
 	 */
 	@Override
 	public void repaint() {
 		mClient.out().writeUInt8(Cmd.REPAINT.ordinal());
 		mClient.sendActionRequest();
 	}
 	
 	private void addColor(IOutputPackage ioOut, Color iColor) {
 		ioOut.writeUInt8(iColor.getRed());
 		ioOut.writeUInt8(iColor.getGreen());
 		ioOut.writeUInt8(iColor.getBlue());
 	}
 
 
 }
