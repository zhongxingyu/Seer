 package org.gladstoneinstitutes.customgraphicsui.internal.util;
 
 import java.awt.GridBagConstraints;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * Uses the builder pattern for filling fields of a {@link GridBagConstraints}.
  */
 public class EasyGBC extends GridBagConstraints {
 	final Map<String,Integer> anchors = new HashMap<String,Integer>(); // this should be populated staticly, but the GBC's constants are not available at compile-time
 	
 	public EasyGBC() {
 		anchors.put("n", super.NORTH);
 		anchors.put("nw", super.NORTHWEST);
 		anchors.put("w", super.WEST);
 		anchors.put("s", super.SOUTH);
 		anchors.put("e", super.EAST);
 		reset();
 	}
 
 	public EasyGBC reset() {
 		gridx = 0;			gridy = 0;
 		gridwidth = 1;		gridheight = 1;
 		weightx = 0.0;		weighty = 0.0;
 		fill = GridBagConstraints.NONE;
 		insets.set(0, 0, 0, 0);
 		return this;
 	}
 
 	public EasyGBC noExpand() {
 		weightx = 0.0;
 		weighty = 0.0;
 		fill = GridBagConstraints.NONE;
 		return this;
 	}
 
 	public EasyGBC expandH() {
 		weightx = 1.0;
 		weighty = 0.0;
 		fill = GridBagConstraints.HORIZONTAL;
 		return this;
 	}
 
 	public EasyGBC expandV() {
 		weightx = 0.0;
 		weighty = 1.0;
 		fill = GridBagConstraints.VERTICAL;
 		return this;
 	}
 
 	public EasyGBC expandHV() {
 		weightx = 1.0;
 		weighty = 1.0;
 		fill = GridBagConstraints.BOTH;
 		return this;
 	}
 
 	public EasyGBC expand(double weightx, double weighty) {
 		super.weightx = weightx;
 		super.weighty = weighty;
 		fill = GridBagConstraints.BOTH;
 		return this;
 	}
 
 	public EasyGBC right() {
 		gridx++;
 		return this;
 	}
 
 	public EasyGBC down() {
 		gridx = 0;
 		gridy++;
 		return this;
 	}
 
 	public EasyGBC position(int x, int y) {
 		gridx = x;
 		gridy = y;
 		return this;
 	}
 
 	public EasyGBC noSpan() {
 		gridwidth = 1;
 		gridheight = 1;
 		return this;
 	}
 
 	public EasyGBC spanH(int n) {
 		gridwidth = n;
 		gridheight = 1;
 		return this;
 	}
 
 	public EasyGBC spanV(int n) {
 		gridwidth = 1;
 		gridheight = n;
 		return this;
 	}
 
 	public EasyGBC insets(int t, int l, int b, int r) {
 		insets.set(t, l, b, r);
 		return this;
 	}
 
 	public EasyGBC noInsets() {
 		insets.set(0, 0, 0, 0);
 		return this;
 	}
 
 	public EasyGBC anchor(String str) {
 		anchor = anchors.get(str);
 		return this;
 	}
 }
