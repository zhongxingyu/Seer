 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import loss.leastSquare;
 import cern.colt.matrix.*;
 import cern.colt.matrix.linalg.*;
 import cern.colt.list.*;
 
 public class MRP {
 
 	// sparse index
 	public class sIdx {
 		int sId;
 		double weight;
 
 		public sIdx(int sId, double weight) {
 			super();
 			this.sId = sId;
 			this.weight = weight;
 		}
 	}
 
 	private Blas blas = SeqBlas.seqBlas;
 	private Algebra algebra = new Algebra();
 
 	public static final double smooth = 0.01;
 
 	int dimGF;
 	int dimGA;
 	int dimLatent;
 	int dimFeatureF;
 	int dimFeatureA;
 	int dimBiFeatureF;
 	int dimBiFeatureA;
 
 	MRPPara para = null;
 
 	DoubleMatrix2D H = null;
 	DoubleMatrix2D U = null;
 	DoubleMatrix2D V = null;
 	DoubleMatrix2D Z = null;
 	DoubleMatrix2D W = null;
 	DoubleMatrix2D M = null;
 
	double[][] HH = null;
	double[][] YH = null;
	double[][] FM = null;

 	leastSquare l2 = leastSquare.getInstance();
 
 	List<DoubleMatrix1D> GF = null;
 	List<DoubleMatrix1D> GA = null;
 	List<DoubleMatrix1D> featureF = null;
 	List<DoubleMatrix1D> featureA = null;
 
 	List<DoubleMatrix1D> reverseGF = null;
 	List<DoubleMatrix1D> reverseGA = null;
 	List<DoubleMatrix1D> reverseFeatureF = null;
 
 	Map<String, Integer> userMap = null;
 	Map<String, Integer> attrMap = null;
 	Map<String, Integer> userFMap = null;
 	Map<String, Integer> attrFMap = null;
 
 	private static int userCount = 0;
 	private static int attrCount = 0;
 	private static int userFCount = 0;
 	private static int attrFCount = 0;
 
 	UserIndex useridx = null;
 	AttrIndex attridx = null;
 	UserFIndex userfidx = null;
 	AttrFIndex attrfidx = null;
 
 	/**
 	 * An updater for Mahalanobis Distance
 	 */
 	public void updateMD(DoubleMatrix1D ls, int curIdx, List<DoubleMatrix1D> data1, List<DoubleMatrix1D> data2,
 			Predict pp, double lambda1, double lambda2, DoubleMatrix2D freeVar, double lr) {
 		IntArrayList idx = new IntArrayList();
 		DoubleArrayList val = new DoubleArrayList();
 		ls.getNonZeros(idx, val);
 		int len = idx.size();
 		for (int i = 0; i < len; i++) {
 			int ssId = idx.get(i);
 			double sweight = val.get(i);
 			DoubleMatrix1D xi = data1.get(curIdx);
 			DoubleMatrix1D xii = data2.get(ssId);
 			double fakeVal = pp.predict(curIdx, ssId);
 			DoubleMatrix2D d1 = DoubleFactory2D.dense.make(xi.size(), xii.size());
 			outdot(xi, xii, d1);
 			blas.dscal(lambda1 * l2.getPartialDerivation(sweight, fakeVal), d1);
 			DoubleMatrix2D d2 = d1.like();
 			blas.daxpy(lambda2 * 0.5 / algebra.normF(freeVar), freeVar, d2);
 			blas.daxpy(1.0, d2, d1);
 			blas.daxpy(-1 * lr, d1, freeVar);
 		}
 	}
 
 	/**
 	 * An updater for Hidden Vector
 	 */
 	public void updateHV(DoubleMatrix1D ls, int curIdx, Predict pp, double lambda1, double lambda2,
 			DoubleMatrix2D freeVar, DoubleMatrix2D fixVar, double lr, boolean reverse) {
 		IntArrayList idx = new IntArrayList();
 		DoubleArrayList val = new DoubleArrayList();
 		ls.getNonZeros(idx, val);
 		int len = idx.size();
 		for (int i = 0; i < len; i++) {
 			int ssId = idx.get(i);
 			double sweight = val.get(i);
 			DoubleMatrix1D d1 = DoubleFactory1D.dense.make(dimLatent);
 			double fakeVal = 0.0;
 			if (!reverse) {
 				fakeVal = pp.predict(curIdx, ssId);
 			} else {
 				fakeVal = pp.predict(ssId, curIdx);
 			}
 			blas.daxpy(lambda1 * l2.getPartialDerivation(sweight, fakeVal), fixVar.viewRow(ssId), d1);
 			assert (algebra.norm2(freeVar.viewRow(curIdx)) != 0);
 			DoubleMatrix1D d2 = d1.like();
 			blas.daxpy(lambda2 / algebra.norm2(freeVar.viewRow(curIdx)), freeVar.viewRow(curIdx), d2);
 			blas.daxpy(1.0, d2, d1);
 			blas.daxpy(-1 * lr, d1, d2);
 			freeVar.viewRow(curIdx).assign(d2);
 		}
 	}
 
 	public void init(int dgf, int dga, int dl, int dff, int dfa, int dbff, int dbfa) {
 		this.para = new MRPPara();
 		this.dimBiFeatureA = dbfa;
 		this.dimBiFeatureF = dbff;
 		this.dimFeatureA = dfa;
 		this.dimFeatureF = dff;
 		this.dimGA = dga;
 		this.dimGF = dgf;
 		this.dimLatent = dl;
 		this.H = DoubleFactory2D.dense.random(dimGF, dimLatent);
 		this.U = DoubleFactory2D.dense.random(dimGF, dimLatent);
 		this.V = DoubleFactory2D.dense.random(dimGF, dimLatent);
 		this.Z = DoubleFactory2D.dense.random(dimGA, dimLatent);
 		this.W = DoubleFactory2D.dense.random(dimFeatureF, dimFeatureF);
 		this.M = DoubleFactory2D.dense.random(dimFeatureF, dimFeatureA);
 		this.GF = new ArrayList<DoubleMatrix1D>();
 		this.GA = new ArrayList<DoubleMatrix1D>();
 		this.featureF = new ArrayList<DoubleMatrix1D>();
 		this.featureA = new ArrayList<DoubleMatrix1D>();
 		this.reverseGF = new ArrayList<DoubleMatrix1D>();
 		this.reverseGA = new ArrayList<DoubleMatrix1D>();
 		this.reverseFeatureF = new ArrayList<DoubleMatrix1D>();
 
 		for (int i = 0; i < dimGF; i++) {
 			this.GF.add(DoubleFactory1D.sparse.make(dimGF));
 			this.GA.add(DoubleFactory1D.sparse.make(dimGA));
 			this.featureF.add(DoubleFactory1D.sparse.make(dimFeatureF));
 
 			this.reverseGF.add(DoubleFactory1D.sparse.make(dimGF));
 		}
 
 		for (int i = 0; i < dimGA; i++) {
 			this.featureA.add(DoubleFactory1D.sparse.make(dimFeatureA));
 
 			this.reverseGA.add(DoubleFactory1D.sparse.make(dimGF));
 		}
 
 		for (int i = 0; i < dimFeatureF; i++) {
 			this.reverseFeatureF.add(DoubleFactory1D.sparse.make(dimGF));
 		}
 
 		this.userMap = new HashMap<String, Integer>();
 		this.attrMap = new HashMap<String, Integer>();
 		this.userFMap = new HashMap<String, Integer>();
 		this.attrFMap = new HashMap<String, Integer>();
 
 		useridx = new UserIndex();
 		attridx = new AttrIndex();
 		userfidx = new UserFIndex();
 		attrfidx = new AttrFIndex();
 	}
 
 	/*
 	 * We can init them using random function But local classifier is a better
 	 * choice.
 	 */
 	public void initParameterAndLatentFactor() {
 		/* Just nothing, because we can init it when call constructor */
 	}
 
 	public interface IIndex {
 		public int Index(String str);
 	}
 
 	public class UserIndex implements IIndex {
 		public int Index(String str) {
 			if (userMap.containsKey(str)) {
 				return userMap.get(str);
 			} else {
 				userMap.put(str, MRP.userCount);
 				return MRP.userCount++;
 			}
 		}
 	}
 
 	public class UserFIndex implements IIndex {
 		public int Index(String str) {
 			if (userFMap.containsKey(str)) {
 				return userFMap.get(str);
 			} else {
 				userFMap.put(str, MRP.userFCount);
 				return MRP.userFCount++;
 			}
 		}
 	}
 
 	public class AttrIndex implements IIndex {
 		public int Index(String str) {
 			if (attrMap.containsKey(str)) {
 				return attrMap.get(str);
 			} else {
 				attrMap.put(str, MRP.attrCount);
 				return MRP.attrCount++;
 			}
 		}
 	}
 
 	public class AttrFIndex implements IIndex {
 		public int Index(String str) {
 			if (attrFMap.containsKey(str)) {
 				return attrFMap.get(str);
 			} else {
 				attrFMap.put(str, MRP.attrFCount);
 				return MRP.attrFCount++;
 			}
 		}
 	}
 
 	private void readData(String f, List<DoubleMatrix1D> container, IIndex idxP, IIndex idxM,
 			List<DoubleMatrix1D> reverseContainer) throws NumberFormatException, IOException {
 		BufferedReader reader = null;
 		FileInputStream file = new FileInputStream(new File(f));
 		System.out.println("read " + f);
 		reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
 		String tempString = null;
 
 		int cnt = 0;
 		while ((tempString = reader.readLine()) != null) {
 			cnt++;
 			if (cnt % 1000 == 0) {
 				System.out.println("read");
 			}
 			String[] strArray = tempString.split(",");
 			Integer primaryId = idxP.Index(strArray[0]);
 			Integer minorId = idxM.Index(strArray[1]);
 			double weight = Double.parseDouble(strArray[2]);
 			if (weight > 10000) {
 				System.out.println("shit!");
 			}
 			container.get(primaryId).setQuick(minorId, weight);
 			if (reverseContainer != null) {
 				reverseContainer.get(minorId).setQuick(primaryId, weight);
 			}
 		}
 		reader.close();
 	}
 
 	interface Predict {
 		public double predict(int i, int j);
 	}
 
 	class PredictFriendship implements Predict {
 		public double predict(int i, int j) {
 			double part1 = blas.ddot(H.viewRow(i), V.viewRow(j));
 			DoubleMatrix1D y = DoubleFactory1D.dense.make(dimFeatureF);
 			blas.dgemv(false, 1.0, W, featureF.get(j), 0, y);
 			double part2 = blas.ddot(featureF.get(i), y);
 			return part1 + part2;
 		}
 	}
 
 	class PredictAttribute implements Predict {
 		public double predict(int i, int j) {
 			double part1 = blas.ddot(H.viewRow(i), Z.viewRow(j));
 			DoubleMatrix1D y = DoubleFactory1D.dense.make(dimFeatureF);
 			blas.dgemv(false, 1.0, M, featureA.get(j), 0, y);
 			double part2 = blas.ddot(featureF.get(i), y);
 			return part1 + part2;
 		}
 	}
 
 	class PredictFeatureF implements Predict {
 		public double predict(int i, int j) {
 			return blas.ddot(H.viewRow(i), U.viewRow(j));
 		}
 	}
 
 	PredictFriendship pf = new PredictFriendship();
 	PredictAttribute pa = new PredictAttribute();
 	PredictFeatureF pff = new PredictFeatureF();
 
 	public interface updateMethod {
 		public void update(DoubleMatrix1D ls, int curIdx);
 
 		public void updateH(DoubleMatrix1D ls, int curIdx);
 
 		public void updateU(DoubleMatrix1D ls, int curIdx);
 
 		public void updateV(DoubleMatrix1D ls, int curIdx);
 
 		public void updateZ(DoubleMatrix1D ls, int curIdx);
 
 		public void updateW(DoubleMatrix1D ls, int curIdx);
 
 		public void updateM(DoubleMatrix1D ls, int curIdx);
 	}
 
 	public void outdot(DoubleMatrix1D left, DoubleMatrix1D right, DoubleMatrix2D retMat) {
 		DoubleMatrix1D tmpMat = DoubleFactory1D.dense.make(right.size());
 		for (int i = 0; i < left.size(); i++) {
 			blas.daxpy(left.getQuick(i), right, tmpMat);
 			retMat.viewRow(i).assign(tmpMat);
 		}
 	}
 
 	updateGF ugf = new updateGF();
 
 	/* update H V W */
 	class updateGF implements updateMethod {
 
 		@Override
 		public void updateH(DoubleMatrix1D ls, int curIdx) {
 			updateHV(ls, curIdx, pf, para.lambdaF, para.lambdaRH, H, V, para.lr, false);
 		}
 
 		@Override
 		public void updateU(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void updateV(DoubleMatrix1D ls, int curIdx) {
 			updateHV(ls, curIdx, pf, para.lambdaF, para.lambdaRV, V, H, para.lr, true);
 		}
 
 		@Override
 		public void updateZ(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void updateW(DoubleMatrix1D ls, int curIdx) {
 			updateMD(ls, curIdx, featureF, featureF, pf, para.lambdaF, para.lambdaRW, W, para.lr);
 		}
 
 		@Override
 		public void updateM(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void update(DoubleMatrix1D ls, int curIdx) {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 
 	updateGA uga = new updateGA();
 
 	/* update H Z M */
 	class updateGA implements updateMethod {
 
 		@Override
 		public void updateH(DoubleMatrix1D ls, int curIdx) {
 			updateHV(ls, curIdx, pa, para.lambdaA, para.lambdaRH, H, Z, para.lr, false);
 		}
 
 		@Override
 		public void updateU(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void updateV(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		public void updateZVectorwise(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void updateZ(DoubleMatrix1D ls, int curIdx) {
 			updateHV(ls, curIdx, pa, para.lambdaA, para.lambdaRZ, Z, H, para.lr, true);
 		}
 
 		@Override
 		public void updateW(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void updateM(DoubleMatrix1D ls, int curIdx) {
 			updateMD(ls, curIdx, featureF, featureA, pa, para.lambdaF, para.lambdaRM, M, para.lr);
 		}
 
 		@Override
 		public void update(DoubleMatrix1D ls, int curIdx) {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 
 	updateFF uff = new updateFF();
 
 	/* update H U */
 	class updateFF implements updateMethod {
 		public void update(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void updateH(DoubleMatrix1D ls, int curIdx) {
 			updateHV(ls, curIdx, pff, para.lambdaN, para.lambdaRH, H, U, para.lr, false);
 		}
 
 		@Override
 		public void updateU(DoubleMatrix1D ls, int curIdx) {
 			updateHV(ls, curIdx, pff, para.lambdaN, para.lambdaRU, U, H, para.lr, true);
 		}
 
 		@Override
 		public void updateV(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void updateZ(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void updateW(DoubleMatrix1D ls, int curIdx) {
 		}
 
 		@Override
 		public void updateM(DoubleMatrix1D ls, int curIdx) {
 		}
 	}
 
 	public List<Integer> shuffleIndex(int n) {
 		List<Integer> retArray = new ArrayList<Integer>();
 		for (int i = 0; i < n; i++) {
 			retArray.add(i, i);
 		}
 		Collections.shuffle(retArray);
 		return retArray;
 	}
 
 	public void updateU2convergence() {
 		for (int t = 0; t < MRPPara.T; t++) {
 			List<Integer> shuffle = shuffleIndex(this.reverseFeatureF.size());
 			for (int i = 0; i < this.reverseFeatureF.size(); i++) {
 				int index = shuffle.get(i);
 				uff.updateU(this.reverseFeatureF.get(index), index);
 			}
 		}
 	}
 
 	public void updateV2convergence() {
 		for (int t = 0; t < MRPPara.T; t++) {
 			List<Integer> shuffle = shuffleIndex(this.reverseGF.size());
 			for (int i = 0; i < this.reverseGF.size(); i++) {
 				int index = shuffle.get(i);
 				ugf.updateV(this.reverseGF.get(index), index);
 			}
 		}
 	}
 
 	public void updateH2convergence() {
 		for (int t = 0; t < MRPPara.T; t++) {
 			List<Integer> shuffle = shuffleIndex(this.GF.size());
 			for (int i = 0; i < this.GF.size(); i++) {
 				int index = shuffle.get(i);
 				ugf.updateH(this.GF.get(index), index);
 			}
 			shuffle = shuffleIndex(this.GA.size());
 			for (int i = 0; i < this.GA.size(); i++) {
 				int index = shuffle.get(i);
 				uga.updateH(this.GA.get(index), index);
 			}
 			shuffle = shuffleIndex(this.featureF.size());
 			for (int i = 0; i < this.featureF.size(); i++) {
 				int index = shuffle.get(i);
 				uff.updateH(this.featureF.get(index), index);
 			}
 		}
 
 	}
 
 	public void updateW2convergence() {
 		for (int t = 0; t < MRPPara.T; t++) {
 			List<Integer> shuffle = shuffleIndex(this.GF.size());
 			for (int i = 0; i < this.GF.size(); i++) {
 				int index = shuffle.get(i);
 				ugf.updateW(this.GF.get(index), index);
 			}
 		}
 	}
 
 	public void updateM2convergence() {
 		for (int t = 0; t < MRPPara.T; t++) {
 			List<Integer> shuffle = shuffleIndex(this.GA.size());
 			for (int i = 0; i < this.GA.size(); i++) {
 				int index = shuffle.get(i);
 				uga.updateM(this.GA.get(index), index);
 			}
 		}
 	}
 
 	public void updateZ2convergence() {
 		for (int t = 0; t < MRPPara.T; t++) {
 			List<Integer> shuffle = shuffleIndex(this.reverseGA.size());
 			for (int i = 0; i < this.reverseGA.size(); i++) {
 				int index = shuffle.get(i);
 				uga.updateZ(this.reverseGA.get(index), index);
 			}
 		}
 	}
 
 	public double err(Predict pp, List<DoubleMatrix1D> data, int size) {
 		double err = 0.0;
 		IntArrayList denseIdx = new IntArrayList();
 		DoubleArrayList denseData = new DoubleArrayList();
 		for (int i = 0; i < data.size(); i++) {
 			data.get(i).getNonZeros(denseIdx, denseData);
 			for (int j = 0; j < denseIdx.size(); j++) {
 				int idx = denseIdx.get(j);
 				double realVal = denseData.get(j);
 				double fakeVal = pp.predict(i, idx);
 				double diff = realVal - fakeVal;
 				err += diff * diff;
 			}
 		}
 		return err / size;
 	}
 
 	public double errA() {
 		return err(pa, GA, dimGA * dimGF);
 	}
 
 	public double errF() {
 		return err(pf, GF, dimGF * dimGF);
 	}
 
 	public double errFF() {
 		return err(pff, featureF, dimGF * dimFeatureF);
 	}
 
 	public double error() {
 		double errF = this.errF();
 		double errA = this.errA();
 		double errFF = this.errFF();
 		double err = para.lambdaF * errF + para.lambdaA * errA + para.lambdaN * errFF;
 		return err;
 	}
 
 	public void train(int T) {
 		double err = 0.0;
 		for (int t = 0; t < T; t++) {
 			System.out.println("U2");
 			updateU2convergence();
 			System.out.println("V2");
 			updateV2convergence();
 			System.out.println("Z2");
 			updateZ2convergence();
 			System.out.println("H2");
 			updateH2convergence();
 			System.out.println("M2");
 			updateM2convergence();
 			System.out.println("W2");
 			updateW2convergence();
 			err = error();
 			System.out.println("" + err);
 			if (err < 0.01) {
 				break;
 			}
 		}
 	}
 
 	public void readFromText(String fGA, String fGF, String fFF, String fFA) throws NumberFormatException, IOException {
 		readData(fGA, GA, useridx, attridx, reverseGA);
 		readData(fGF, GF, useridx, useridx, reverseGF);
 		readData(fFF, featureF, useridx, userfidx, reverseFeatureF);
 		readData(fFA, featureA, attridx, attrfidx, null);
 	}
 
 	public void saveParas(String fName, int m, int n, DoubleMatrix2D matrix) throws IOException {
 		File outf = new File(fName);
 		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outf), "utf-8"));
 		double tmp = 0.0;
 		for (int i = 0; i < m; i++) {
 			for (int j = 0; j < n - 1; j++) {
 				tmp = matrix.get(i, j) > 1e-4 ? matrix.get(i, j) : 0.0;
 				writer.write(tmp + ",");
 			}
 			tmp = matrix.get(i, n - 1) > 1e-4 ? matrix.get(i, n - 1) : 0.0;
 			writer.write(tmp + "\n");
 		}
 		writer.close();
 	}
 
 	public void saveAll() throws IOException {
 		this.saveParas("matrixU", dimGF, dimLatent, this.U);
 		this.saveParas("matrixV", dimGF, dimLatent, this.V);
 		this.saveParas("matrixH", dimGF, dimLatent, this.H);
 		this.saveParas("matrixZ", dimGA, dimLatent, this.Z);
 		this.saveParas("matrixW", dimFeatureF, dimFeatureF, this.W);
 		this.saveParas("matrixM", dimFeatureF, dimFeatureA, this.M);
 	}
 
 	public double MAE(double i, double j) {
 		double retval = 0.0;
 		retval = Math.abs(i - j);
 		return retval;
 	}
 
 	public double evaluationFriendship(String fe, int lineCnt) throws NumberFormatException, IOException {
 		double retval = 0.0;
 		List<DoubleMatrix1D> le = new ArrayList<DoubleMatrix1D>(lineCnt);
 		this.readData(fe, le, this.useridx, this.useridx, null);
 		int cnt = 0;
 		for (int i = 0; i < lineCnt; i++) {
 			for (int j = 0; j < le.get(i).size(); j++) {
 				double realVal = le.get(i).get(j);
 				double fakeVal = pf.predict(i, j);
 				retval += this.MAE(realVal, fakeVal);
 				cnt += 1;
 			}
 		}
 		return retval / cnt;
 	}
 
 	public double evaluationAttribute(String fe, int lineCnt) throws NumberFormatException, IOException {
 		double retval = 0.0;
 		List<DoubleMatrix1D> le = new ArrayList<DoubleMatrix1D>(lineCnt);
 		this.readData(fe, le, this.useridx, this.attridx, null);
 		for (int i = 0; i < lineCnt; i++) {
 			for (int j = 0; j < le.get(i).size(); j++) {
 				double realVal = le.get(i).get(j);
 				double fakeVal = pa.predict(i, j);
 				retval += this.MAE(realVal, fakeVal);
 			}
 		}
 		return retval;
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 * @throws NumberFormatException
 	 */
 
 	public static void main(String[] args) throws NumberFormatException, IOException {
 
 		MRP test = new MRP();
 		test.init(45683, 1002, 5, 14, 7, 1, 1);
 		test.readFromText("final-data/graph-attribute-number.txt", "final-data/graph-friendship-number.txt",
 				"final-data/feature-user-number.txt", "final-data/feature-attribute-number.txt");
 		test.train(1);
 		test.saveAll();
 		System.out.println("last error:\t" + test.error());
 		// System.out.println("evaluation:\t" +
 		// test.evaluationAttribute("C:\\Users\\xusenyin\\Desktop\\weibo_sample\\ga_a0.1",
 		// 617));
 	}
 }
