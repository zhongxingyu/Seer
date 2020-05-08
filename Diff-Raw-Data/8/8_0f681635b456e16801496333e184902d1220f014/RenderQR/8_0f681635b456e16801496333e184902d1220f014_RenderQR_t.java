 package com.asksteved.noderefqrcode.webui;
 
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.WriterException;
 import com.google.zxing.client.j2se.MatrixToImageWriter;
 import com.google.zxing.common.BitMatrix;
 import com.google.zxing.qrcode.QRCodeWriter;
 import freenet.client.HighLevelSimpleClient;
 import freenet.clients.http.Toadlet;
 import freenet.clients.http.ToadletContext;
 import freenet.clients.http.ToadletContextClosedException;
 import freenet.node.Node;
 import freenet.support.Logger;
 import freenet.support.api.HTTPRequest;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URI;
 
 /**
  * Renders a QR code of the darknet reference. Replies with a PNG of it.
  */
 public class RenderQR extends Toadlet {
 
 	private final Node node;
 	/**
 	 * Width and height of rendered QR code.
 	 */
 	static final int dimension = 500;
 
 	public RenderQR(HighLevelSimpleClient client, Node node) {
 		super(client);
 		this.node = node;
 	}
 	public String path() {
 		return "/qr/render/";
 	}
 
 	public void handleMethodGET(URI uri, HTTPRequest request, ToadletContext ctx)
	       throws ToadletContextClosedException, IOException
 	{
		try {
 		QRCodeWriter qrCodeWriter = new QRCodeWriter();
 		BitMatrix matrix = qrCodeWriter.encode(node.exportDarknetPublicFieldSet().toString(), BarcodeFormat.QR_CODE, dimension, dimension);
 		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		MatrixToImageWriter.writeToStream(matrix, "png", outputStream);
 		writeReply(ctx, 200, "image/png", "OK", outputStream.toByteArray(), 0, outputStream.size());
		} catch (WriterException e) {
			writeInternalError(e, ctx);
		}
 	}
 }
