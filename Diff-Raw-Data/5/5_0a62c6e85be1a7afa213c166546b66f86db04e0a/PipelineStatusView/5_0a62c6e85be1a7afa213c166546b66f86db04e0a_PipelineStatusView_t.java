 package org.remus;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.mpstore.KeyValuePair;
 import org.mpstore.Serializer;
 import org.remus.serverNodes.BaseNode;
 import org.remus.work.RemusApplet;
 
 public class PipelineStatusView implements BaseNode {
 
 	RemusPipeline pipeline;
 	public PipelineStatusView(RemusPipeline pipeline) {
 		this.pipeline = pipeline;
 	}
 
 	@Override
 	public void doDelete(Map params) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void doGet(String name, Map params, String workerID,
 			Serializer serial, OutputStream os) throws FileNotFoundException {
 		
 		if ( name.length() == 0 ) {
 			for ( RemusApplet applet : pipeline.getMembers() ) {
				for ( KeyValuePair kv : applet.getDataStore().listKeyPairs(applet.getPath() + "/@instance", RemusInstance.STATIC_INSTANCE_STR) ) {
 					Map out = new HashMap();
					out.put( kv.getKey() + "/" + applet.getID(), kv.getValue() );	
 					try {
 						os.write( serial.dumps( out ).getBytes() );
 						os.write("\n".getBytes());
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void doPut(String name, String workerID, Serializer serial,
 			InputStream is, OutputStream os) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void doSubmit(String name, String workerID, Serializer serial,
 			InputStream is, OutputStream os) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public BaseNode getChild(String name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
