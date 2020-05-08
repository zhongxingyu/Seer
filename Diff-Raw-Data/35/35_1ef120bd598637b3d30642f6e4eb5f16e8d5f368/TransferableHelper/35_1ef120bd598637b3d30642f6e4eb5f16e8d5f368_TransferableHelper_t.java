 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.rptools.maptool.client;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 import javax.swing.TransferHandler;
 
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.transferable.FileTransferableHandler;
 import net.rptools.lib.transferable.GroupTokenTransferData;
 import net.rptools.lib.transferable.ImageTransferableHandler;
 import net.rptools.lib.transferable.MapToolTokenTransferData;
 import net.rptools.lib.transferable.TokenTransferData;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.util.PersistenceUtil;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author tcroft
  */
 public class TransferableHelper extends TransferHandler {
 
 	private static final Logger log = Logger.getLogger(TransferableHelper.class);
 
 	// TODO: USE ImageTransferable in rplib
 //	private static final DataFlavor IMAGE_FLAVOR = new DataFlavor("image/x-java-image; class=java.awt.Image", "Image");
 
 	/**
 	 * Takes a drop event and returns an asset
 	 * from it.  returns null if an asset could not be obtained
 	 */
 	public static List<Object> getAsset(Transferable transferable) {
 
 		List<Object> assets = new ArrayList<Object>();
 
 		try {
 			// EXISTING ASSET
 			if (transferable.isDataFlavorSupported(TransferableAsset.dataFlavor)) {
 
 				assets.add(handleTransferableAsset(transferable));
 			} else if (transferable.isDataFlavorSupported(TransferableAssetReference.dataFlavor)) {
 				assets.add(handleTransferableAssetReference(transferable));
 
 				// LOCAL FILESYSTEM
				// Used by OSX when files are dragged form the desktop...
 			} else if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
 				assets = handleFileList(transferable);

				// DIRECT/BROWSER
			} else if (transferable.isDataFlavorSupported(URL_FLAVOR)) {
				assets.add(handleImage(transferable));
 			}
 
 		} catch (Exception e) {
 			System.err.println ("Could not retrieve asset: " + e);
 			e.printStackTrace();
 			return null;
 		}
 
 		if (assets == null || assets.isEmpty()) {
 			return null;
 		}
 
 		for (Object working : assets) {
 			if (working instanceof Asset) {
 				Asset asset = (Asset) working;
 				if (!AssetManager.hasAsset(asset))
 					AssetManager.putAsset(asset);
 				if (!MapTool.getCampaign().containsAsset(asset))
 					MapTool.serverCommand().putAsset(asset);
 			}
 		}
 		return assets;
 	}
 
 	private static Asset handleImage (Transferable transferable) throws IOException, UnsupportedFlavorException {
 		String name = null;
 		BufferedImage image = null;
		DataFlavor[] list = transferable.getTransferDataFlavors();
 		if (transferable.isDataFlavorSupported(URL_FLAVOR)) {
 			try {
				String fname = (String) transferable.getTransferData(URL_FLAVOR);
				log.debug("Transferable " + fname);
				name = FileUtil.getNameWithoutExtension(fname);

 				File file;
 				URL url = new URL(fname);
 				try {
 					URI uri = url.toURI();		// Should replace '%20' sequences and such
 					file = new File(uri);
 				} catch (URISyntaxException e) {
 					file = new File(fname);
 				}
 				if (file.exists()) {
 					log.debug("Reading local file:  " + file);
 					image = ImageIO.read(file);
 				} else {
 					log.debug("Reading remote URL:  " + url);
 					image = ImageIO.read(url);
 				}
 			} catch (Exception e) {
				MapTool.showError("Cannot read URL_FLAVOR", e);
 			}
 		}
 		if (image == null) {
 			log.debug("URL_FLAVOR didn't work; trying ImageTransferableHandler().getTransferObject()");
 			image = (BufferedImage) new ImageTransferableHandler().getTransferObject(transferable);
 		}
 		Asset asset = new Asset(name, ImageUtil.imageToBytes(image));
 		return asset;
 	}
 
 	private static List<Object> handleFileList(Transferable transferable) throws Exception {
 		List<File> list = new FileTransferableHandler().getTransferObject(transferable);
 		List<Object> assets = new ArrayList<Object>();
 		for (File file : list) {
 			// A JFileChooser (at least under Linux) sends a couple empty filenames that need to be ignored.
 			if (!file.getPath().equals("")) {
 				if (Token.isTokenFile(file.getName())) {
 					// Loading the token causes the assets to be added to the AssetManager
 					// so it doesn't need to be added to our List here.  In fact, getAsset()
 					// will strip out anything in the List that isn't an Asset anyway...
 					Token token = PersistenceUtil.loadToken(file);
 					assets.add(token);
 				} else {
 					assets.add(AssetManager.createAsset(file));
 				}
 			}
 		}
 		return assets;
 	}
 
 	private static Asset handleTransferableAssetReference(Transferable transferable) throws Exception {
 
 		return AssetManager.getAsset((MD5Key) transferable.getTransferData(TransferableAssetReference.dataFlavor));
 	}
 
 	private static Asset handleTransferableAsset(Transferable transferable) throws Exception {
 
 		return (Asset) transferable.getTransferData(TransferableAsset.dataFlavor);
 	}
 
 	/**
 	 * Get the tokens from a token list data flavor.
 	 * 
 	 * @param transferable
 	 *            The data that was dropped.
 	 * @return The tokens from the data or <code>null</code> if this isn't the
 	 *         proper data type.
 	 */
 	@SuppressWarnings("unchecked")
 	public static List<Token> getTokens(Transferable transferable) {
 		List<Token> tokens = null;
 		try {
 			Object df = transferable.getTransferData(GroupTokenTransferData.GROUP_TOKEN_LIST_FLAVOR);
 			List<TokenTransferData> tokenMaps = (List<TokenTransferData>) df;
 			tokens = new ArrayList<Token>();
 			for (Object object : tokenMaps) {
 				if (!(object instanceof TokenTransferData))
 					continue;
 				TokenTransferData td = (TokenTransferData) object;
 				if (td.getName() == null || td.getName().trim().length() == 0 || td.getToken() == null)
 					continue;
 				tokens.add(new Token(td));
 			} // endfor
 			if (tokens.size() != tokenMaps.size()) {
 				final String message = "Added " + tokens.size() + " tokens." +
 				"\nThere were " + (tokenMaps.size() - tokens.size()) + " tokens that could not be added " +
 				"because they were missing names or images.";
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						MapTool.showWarning(message);
 					}
 				});
 			} // endif
 		} catch (IOException e) {
 			MapTool.showError("Error during drag/drop operation", e);
 		}
 		catch (UnsupportedFlavorException e) {
 			MapTool.showError("Error during drag/drop operation", e);
 		}
 		return tokens;
 	}
 
 	public static boolean isSupportedAssetFlavor(Transferable transferable) {
 		return transferable.isDataFlavorSupported(TransferableAsset.dataFlavor)
 		|| transferable.isDataFlavorSupported(TransferableAssetReference.dataFlavor)
 		|| transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
 		|| transferable.isDataFlavorSupported(URL_FLAVOR);
 	}
 
 	public static boolean isSupportedTokenFlavor(Transferable transferable) {
 		return transferable.isDataFlavorSupported(GroupTokenTransferData.GROUP_TOKEN_LIST_FLAVOR) ||
 		transferable.isDataFlavorSupported(TransferableToken.dataFlavor);
 	}
 
 	/** URL to an image */
 	private static final DataFlavor URL_FLAVOR = new DataFlavor("text/plain; class=java.lang.String", "Image");
 
 	/**
 	 * Data flavors that this handler will support.
 	 */
 	public static final DataFlavor[] SUPPORTED_FLAVORS = {
 		DataFlavor.javaFileListFlavor,
 		MapToolTokenTransferData.MAP_TOOL_TOKEN_LIST_FLAVOR,
 		GroupTokenTransferData.GROUP_TOKEN_LIST_FLAVOR,
 		TransferableAsset.dataFlavor,
 		TransferableAssetReference.dataFlavor,
 		URL_FLAVOR,
 		TransferableToken.dataFlavor
 	};
 
 	/**
 	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
 	 */
 	@Override
 	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
 		for (int j = 0; j < SUPPORTED_FLAVORS.length; j++) {
 			for (int i = 0; i < transferFlavors.length; i++) {
 				if (SUPPORTED_FLAVORS[j].equals(transferFlavors[i]))
 					return true;
 			} // endfor
 		} // endfor
 		return false;
 	}
 
 	/** The tokens to be loaded onto the renderer when we get a point */
 	List<Token> tokens;
 
 	/** Whether or not each token needs additional configuration (set footprint, guess shape). */
 	List<Boolean> configureTokens;
 
 	/**
 	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
 	 */
 	@Override
 	public boolean importData(JComponent comp, Transferable t) {
 		tokens = null;
 		configureTokens = null;
 		List<Object> assets = getAsset(t);
 		if (assets != null) {
 			tokens = new ArrayList<Token>(assets.size());
 			configureTokens = new ArrayList<Boolean>(assets.size());
 			for (Object working : assets) {
 				if (working instanceof Asset) {
 					Asset asset = (Asset) working;
 					tokens.add(new Token(asset.getName(), asset.getId()));
 					// A token from an image asset needs additional configuration.
 					configureTokens.add(true);
 				} else if (working instanceof Token) {
 					tokens.add(new Token((Token) working));
 					// A token from an .rptok file is already fully configured.
 					configureTokens.add(false);
 				}
 			}
 		} else {
 			if (t.isDataFlavorSupported(TransferableToken.dataFlavor)) {
 				try {
 					// Make a copy so that it gets a new unique GUID
 					tokens = Collections.singletonList(new Token((Token) t.getTransferData(TransferableToken.dataFlavor)));
 					// A token from the Resource Library is already fully configured.
 					configureTokens = Collections.singletonList(new Boolean(false));
 				} catch (UnsupportedFlavorException ufe) {
 					ufe.printStackTrace();
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				}
 			} else {
 
 				tokens = getTokens(t);
 				// Tokens from Init Tool all need to be configured.
 				configureTokens = new ArrayList<Boolean>(tokens.size());
 				for (int i = 0; i < tokens.size(); i++) {
 					configureTokens.add(true);
 				}
 			}
 
 		}
 		return tokens != null;
 	}
 
 	/**
 	 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
 	 */
 	@Override
 	public int getSourceActions(JComponent c) {
 		return NONE;
 	}
 
 	/** @return Getter for tokens */
 	public List<Token> getTokens() {
 		return tokens;
 	}
 
 	/** @param tokens Setter for tokens */
 	public void setTokens(List<Token> tokens) {
 		this.tokens = tokens;
 	}
 
 	/** @return Getter for configureTokens */
 	public List<Boolean> getConfigureTokens() {
 		return configureTokens;
 	}
 
 	/** @param configureTokens Setter for configureTokens */
 	public void setConfigureTokens(List<Boolean> configureTokens) {
 		this.configureTokens = configureTokens;
 	}
 }
