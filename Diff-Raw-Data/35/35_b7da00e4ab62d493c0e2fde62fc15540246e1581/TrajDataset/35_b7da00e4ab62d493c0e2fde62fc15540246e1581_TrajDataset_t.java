 //FILE:          TrajDataset.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu, 2/16/08
 //
 // LICENSE:      This file is distributed under the BSD license.
 //               License text is included with the source distribution.
 //
 //               This file is distributed in the hope that it will be useful,
 //               but WITHOUT ANY WARRANTY; without even the implied warranty
 //               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 //
 //               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 //               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES./**
 //
 package edu.uchc.octane;
 
 import ij.IJ;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.Vector;
 
 /**
  * The dataset object that stores the information about all trajectories and particle locations
  */
 public class TrajDataset{
 	
 	Vector<Trajectory> trajectories_;
 	SmNode [][] nodes_; //nodes_[frame][offset]
 	double pixelSize_;
 
 	TrackingModule tm_;
 	DriftCorrectionModule dcm_;
 
 	class Bond implements Comparable<Bond> {
 		int bondTo;
 		double bondLength;
 		@Override
 		public int compareTo(Bond o) {
 			return (int) Math.signum(bondLength - o.bondLength); 
 		}
 	}
 	
 	/**
 	 * Constructor.
 	 */
 	public TrajDataset() {
 		trajectories_ = new Vector<Trajectory>();
 		tm_ = new TrackingModule(this);
 		dcm_ = new DriftCorrectionModule(this);
 		
 		pixelSize_ = GlobalPrefs.defaultPixelSize_;
 	}
 
 	/**
 	 * Returns pixel size in nm.
 	 *
 	 * @return pixel size
 	 */
 	public double getPixelSize() {
 		return pixelSize_;
 	}
 	
 	/**
 	 * Set pixel size in nm 
 	 *
 	 * @param p pixel size in nm
 	 */
 	public void setPixelSize(double p) {
 		pixelSize_ = p;
 	}
 
 	/**
 	 * Returns a trajectory by index.
 	 *
 	 * @param i the index
 	 * @return the trajectory by index
 	 */
 	public Trajectory getTrajectoryByIndex(int i) {
 		return trajectories_.get(i);
 	}
 
 	/**
 	 * Number of trajectories.
 	 *
 	 * @return the size
 	 */
 	public int getSize() {
 		return trajectories_.size();
 	}
 
 	void rebuildNodes() {
 		if (trajectories_ == null || trajectories_.size() == 0) 
 			return;
 
 		ArrayList<ArrayList<SmNode>> framelist = new ArrayList<ArrayList<SmNode>>();
 		for (int i = 0; i < trajectories_.size(); i++) {
 			Trajectory t = trajectories_.get(i);
 			for (int j = 0; j < t.size(); j++) {
 				SmNode n = t.get(j);
 				while (n.frame > framelist.size()) {
 					framelist.add(new ArrayList<SmNode>());
 				}
 				framelist.get(n.frame-1).add(n);
 			}
 		}
 
 		nodes_ = new SmNode[framelist.size()][];
 		for (int i = 0 ; i < nodes_.length; i++) {
 			nodes_[i] = new SmNode[framelist.get(i).size()];
 			framelist.get(i).toArray(nodes_[i]);
 		}
 	}
 
 	/**
 	 * Rebuilt trajectories.
 	 */
 	public void reTrack() {
 		rebuildNodes();
 		doTracking();
 	}
 
 	/**
 	 * Write peak positions to text.
 	 *
 	 * @param file the file
 	 * @throws IOException 
 	 */
 	public void writePositionsToText(File file) throws IOException {
 		rebuildNodes();
 		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
 		for (int i = 0; i < nodes_.length; i ++ ) {
 			for (int j = 0; j < nodes_[i].length; j++) {
 				SmNode s = nodes_[i][j];
 				bw.write(s.toString());
 				bw.write('\n');
 			}
 		}
 		bw.close();
 		nodes_ = null;
 	}
 
 	/**
 	 * Write trajectories to text.
 	 *
 	 * @param w Java writer
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	public void writeTrajectoriesToText(Writer w)throws IOException {
 		for (int i = 0; i < trajectories_.size(); i ++ ) {
 			for (int j = 0; j < trajectories_.get(i).size(); j++) {
 				SmNode s = trajectories_.get(i).get(j);
 				w.append(s.toString());
 				w.append(", " + i + "\n");				
 			}
 		}
 		w.close(); 		
 	}
 
 	/**
 	 * Save dataset to disk.
 	 *
 	 * @param file the file
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	public void saveDataset(File file) throws IOException {
 		ObjectOutputStream out;
 		BufferedOutputStream fs;
 		
 		fs = new BufferedOutputStream(new FileOutputStream(file));
 		out = new ObjectOutputStream(fs);
 		
 		out.writeObject(trajectories_);
 		out.writeDouble(pixelSize_);
 		
 		out.close();
 		fs.close();
 	}
 
 	/**
 	 * Load dataset from disk.
 	 *
 	 * @param file the file
 	 * @return a new dataset
 	 * @throws IOException 
 	 * @throws ClassNotFoundException
 	 */
 	static public TrajDataset loadDataset(File file) throws IOException, ClassNotFoundException {
 		ObjectInputStream in;
 		FileInputStream fs;
 		
 		TrajDataset dataset = new TrajDataset();
 		
 		fs = new FileInputStream(file);
 		in = new ObjectInputStream(fs);
 		
 		Vector<Trajectory> trajectories = (Vector<Trajectory>) in.readObject();
 		dataset.trajectories_ = trajectories;
 
 		try {
 			in.readDouble();
 		} catch (EOFException e) {
 			// do nothing
 		}
 
 		in.close();
 		fs.close();
 		return dataset;
 	}
 
 	/**
 	 * Import dataset from peak position text file.
 	 *
 	 * @param file the file
 	 * @return a new dataset
 	 * @throws IOException
 	 */
 	static public TrajDataset importDatasetFromPositionsText(File file) throws IOException {
 		BufferedReader br;
 		String line;
 		ArrayList<ArrayList<SmNode>> nodes = new ArrayList<ArrayList<SmNode>>(); 
 		br = new BufferedReader(new FileReader(file));
 		while (null != (line = br.readLine())) {
 			if (line.startsWith("#") || line.startsWith("//")) {
 				continue;
 			}
 			if (line.trim().isEmpty()) {
 				continue;
 			}
 			SmNode node = new SmNode(line);
 			while (node.frame > nodes.size()) {
 				nodes.add(new ArrayList<SmNode>());
 			}
 			nodes.get(node.frame-1).add(node);
 		}
 		br.close();
 
 		SmNode[][] n = new SmNode[nodes.size()][];
 		for (int i = 0; i < nodes.size(); i++) {
 			n[i] = new SmNode[nodes.get(i).size()];
 			nodes.get(i).toArray(n[i]);
 		}
 
 		return createDatasetFromNodes(n);
 	}
 
 	/**
 	 * Import dataset from trajectories text file. Will use the traceID if exist or 
 	 * rebuild trajectories if no traceID is found.
 	 * @param file the file
 	 * @return a new dataset
 	 * @throws IOException
 	 */
 	static public TrajDataset importDatasetFromText(File file) throws IOException {
 
 		TrajDataset dataset;
 
 		Trajectory oneTraj = new Trajectory();
 		int cur_cnt = -1;
 		String line;
 		BufferedReader br = new BufferedReader(new FileReader(file));
 
 		dataset = new TrajDataset();
 
 		while (null != (line = br.readLine())) {
 			if (line.startsWith("#") || line.startsWith("//")) {
 				continue;
 			}
 			if (line.trim().isEmpty()) {
 				continue;
			}
			try {
				int c = line.lastIndexOf(',');
				int cnt = Integer.parseInt(line.substring(c + 1).trim());
				if (cur_cnt == cnt - 1) {
					oneTraj = new Trajectory();
					dataset.trajectories_.add(oneTraj);
					cur_cnt = cnt;
				} 
				if (cur_cnt == cnt) {
					SmNode node = new SmNode(line.substring(0, c)); 
					oneTraj.add(node);
					//dataset.nodes_.add(node);
				} else {
					IJ.log("TraceID not countinous. All traces will be rebuilt");
					br.close();
					return importDatasetFromPositionsText(file);
				}
			} catch (java.lang.NumberFormatException e) {
				br.close();
				IJ.log("No trace ID. Treating data as simple coordinates");
 				return importDatasetFromPositionsText(file);
 			}
 		}
 
 		br.close();
 		assert (dataset.trajectories_.size() > 0);	
 
 		return dataset;
 	}
 
 		
 	/**
 	 * Creates the dataset from array of node lists.
 	 *
 	 * @param nodes the 2D array of nodes
 	 * @return a new dataset
 	 */
 	static public TrajDataset createDatasetFromNodes(SmNode[][] nodes) {
 
 		TrajDataset dataset;
 
 		dataset = new TrajDataset();
 		dataset.nodes_ = nodes;
 		dataset.doTracking();
 		
 		return dataset;		
 	}
 
 	protected void doTracking() {
 		trajectories_ = tm_.doTracking();
 	}
 
 	/**
 	 * Maximum frame number in all trajectories.
 	 *
 	 * @return the maximum frame number
 	 */
 	public int getMaximumFrameNumber() {
 		int maxFrameNum = -1;
 		for (int i = 0; i < trajectories_.size(); i++) {
 			Trajectory t = trajectories_.get(i);
 			if (!t.deleted) {
 				int f = t.get(t.size()-1).frame;
 				if (maxFrameNum < f) {
 					maxFrameNum = f;
 				}
 			}
 		}
 
 		return maxFrameNum;
 	}
 	
 	/**
 	 * Import drift data from text file.
 	 *
 	 * @param file the file
 	 * @throws IOException
 	 * @throws OctaneException 
 	 */
 	public void importDriftData(File file) throws IOException{
 		BufferedReader br;
 		String line; 
 		br = new BufferedReader(new FileReader(file));
 		int nFrames = getMaximumFrameNumber();
 		double [] drift_x = new double[nFrames];
 		double [] drift_y = new double[nFrames];
 		double [] drift_z = new double[nFrames];
 		int cnt = 0;
 		while (null != (line = br.readLine())) {
 			if (line.startsWith("#") || line.startsWith("//")) {
 				continue;
 			}
 			if (line.trim().isEmpty()) {
 				continue;
 			}
 			Scanner s = new Scanner(line).useDelimiter("\\s*,\\s*");
 			drift_x[cnt] = s.nextDouble();
 			drift_y[cnt] = s.nextDouble();
 			if (s.hasNextDouble()) {
 				drift_z[cnt] = s.nextDouble();
 			}
 			cnt ++;
 			if (cnt == nFrames) {
 				break;
 			}
 		}
 		br.close();
 		if (cnt != nFrames) {
 			throw new IOException("Error import drift data: wrong format");
 		}
 		
 		try {
 			dcm_.setDriftData(drift_x, drift_y, drift_z);
 		} catch (OctaneException e) {
 			// already checked the size.
 		}
 	}
 
 	public void estimateDrift(int [] selection) {
 		dcm_.calculateDrift(selection);
 	}
 
 	public double [] getDriftX() {
 		return dcm_.getDriftX();
 	}
 	
 	public double [] getDriftY() {
 		return dcm_.getDriftY();
 	}
 
 	public double [] getDriftZ() {
 		return dcm_.getDriftZ();
 	}
 
 	public SmNode correctDrift(SmNode node) throws OctaneException {
 
 			return dcm_.correctDrift(node);
 		
 	}
 	
 }
 
