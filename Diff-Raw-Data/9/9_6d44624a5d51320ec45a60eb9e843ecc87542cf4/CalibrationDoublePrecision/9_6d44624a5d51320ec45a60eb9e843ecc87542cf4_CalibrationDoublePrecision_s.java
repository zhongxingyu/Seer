 package com.example.ivcam.demo;
 
 public class CalibrationDoublePrecision {
 	public class Parameters {
 		public double[] kc = new double[9]; // Camera intrinsic [3x3]
 		public double[] distc = new double[5]; // Camera forward distortion [1x5]
 		public double[] invdistc = new double[5]; // Camera inverse distortion
 												// [1x5]
 		public double width = 0; // Camera width
 		public double height = 0; // Camera height
 		public double zmax = 200; // Maximum Z range. [0,65535] in the Z image is
 									// mapped to [0,Zmax]
 		public double[] pp = new double[12];// ??? [3x4] : important [3x4]
 											// required (can't be used with
 											// [2x4]
 		public double[] kp = new double[9]; // [3x3]
 		public double[] rp = new double[9]; // [3x3]
 		public double[] tp = new double[3];
 		public double[] distp = new double[5]; // [1x5]
 		public double[] invdistp = new double[5]; // [1x5]
 		public double[] pt = new double[12];// [3x4]
 		public double[] distt = new double[5]; // [1x5]
 	};
 
 	public class Point {
 		public double x;
 		public double y;
 
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
 		}
 	};
 
 	public Parameters params;
 	public int m_zWidth; // equals to params.Width but of type int
 	public Boolean m_initialized;
 	public Point[] m_unprojCoeffs;
 	public int m_unprojAllocSize;
 
 	public CalibrationDoublePrecision(double[] paramData, int nParams) {
 		m_initialized = false;
 		m_unprojCoeffs = null;
 		m_unprojAllocSize = 0;
 		buildParameters(paramData, nParams);
 	}
 
 	public void clear() {
 		m_initialized = false;
 	}
 
 	public double getZMax() {
 		return params.zmax;
 	};
 
 	public double[] getParameters() {
 		if (m_initialized)
 			return params.kc;
 		else
 			return null;
 	}
 
 	static public int nParamters() {
 		return 82;
 	}
 
 	public Boolean buildParameters(double[] paramData, int nParams) {
 		double[] ps = new double[128];
 		for (int i = 0; i < nParams; ++i) {
 			ps[i] = paramData[i];
 		}
 		return buildParametersAux(ps, nParams);
 	}
 
 	public Boolean buildParametersAux(double[] paramData, int nParams) {
 		final int kParamsCount = 61;
 
 		if (nParams < kParamsCount)
 			return false;
 		params = new Parameters();
 		int paramIdx = 0;
 		int i = 0;
 		for (i = 0; i != 9; i++) {
 			params.kc[i] = paramData[paramIdx++];
 		}
 		for (i = 0; i != 5; i++) {
 			params.distc[i] = paramData[paramIdx++];
 		}
 		for (i = 0; i != 5; i++) {
 			params.invdistc[i] = paramData[paramIdx++];
 		}
 		params.width = paramData[paramIdx++];
 		m_zWidth = (int) params.width;
 		params.height = paramData[paramIdx++];
 		params.zmax = paramData[paramIdx++];
 
 		for (i = 0; i != 12; i++) {
 			params.pp[i] = paramData[paramIdx++];
 		}
 		for (i = 0; i != 9; i++) {
 			params.kp[i] = paramData[paramIdx++];
 		}
 		for (i = 0; i != 9; i++) {
 			params.rp[i] = paramData[paramIdx++];
 		}
 		for (i = 0; i != 3; i++) {
 			params.tp[i] = paramData[paramIdx++];
 		}
 		for (i = 0; i != 5; i++) {
 			params.distp[i] = paramData[paramIdx++];
 		}
 		for (i = 0; i != 5; i++) {
 			params.invdistp[i] = paramData[paramIdx++];
 		}
 		for (i = 0; i != 12; i++) {
 			params.pt[i] = paramData[paramIdx++];
 		}
 		for (i = 0; i != 5; i++) {
 			params.distt[i] = paramData[paramIdx++];
 		}
 		m_initialized = true;
 
 		// precompute pixel unproject coefficients
 
 		int w = (int) params.width;
 		int h = (int) params.height;
 		int sizeToAlloc = w * h;
 		if (sizeToAlloc == m_unprojAllocSize) {
 			Boolean sameParams = true;
 			int k = nParamters();
 			double[] params = getParameters();
 			for (i = 0; i != k; ++i) {
 				if (params[i] != paramData[i]) {
 					sameParams = false;
 					break;
 				}
 			}
 			if (sameParams)
 				return true; // no need to compute pixel coefficients
 		} else {
 			m_unprojCoeffs = null;
 			m_unprojCoeffs = new Point[sizeToAlloc];
 			m_unprojAllocSize = sizeToAlloc;
 		}
 
 		for (int y = 0; y != h; ++y)
 			for (int x = 0; x != w; ++x)
 				m_unprojCoeffs[y * w + x] = computeUnprojectCoeffs(x, y);
 		return true;
 	}
 
 	public void buildUV(double x, double y, double z, double[] u, double[] v) {
 		// x, y, z: must be unprojected coordinates
 		// u, v are in the range [0, 1]
 		double[] pt = params.pt;
 		double D = (0.5f) / (pt[2] * x + pt[5] * y + pt[8] * z + pt[11]); // 0.5
 																			// ->
 																			// 1.0
 		u[0] = (pt[0] * x + pt[3] * y + pt[6] * z + pt[9]) * D + (0.5f);
 		v[0] = (pt[1] * x + pt[4] * y + pt[7] * z + pt[10]) * D + (0.5f);
 	}
 
 	public void project(double x, double y, double z, double[] u, double[] v,
 			double[] d) {
 		double xc = x / z;
 		double yc = y / z;
 
 		double r2 = xc * xc + yc * yc;
 		double r2d = ((1.0f) + params.distc[0] * r2 + params.distc[1] * r2 * r2 + params.distc[4]
 				* r2 * r2 * r2);
 		double xcd = xc * r2d + (2.0f) * params.distc[2] * xc * yc
 				+ params.distc[3] * (r2 + (2.0f) * xc * xc);
 		double ycd = yc * r2d + (2.0f) * params.distc[3] * xc * yc
 				+ params.distc[2] * (r2 + (2.0f) * yc * yc);
 		xcd = xcd * params.kc[0] + params.kc[6];
 		ycd = ycd * params.kc[4] + params.kc[7];
 
 		u[0] = ((xcd + 1) * params.width * 0.5f);
 		v[0] = ((ycd + 1) * params.height * 0.5f);
 		d[0] = z / params.zmax * (65535);
 	}
 
 	public Point computeUnprojectCoeffs(int _u, int _v) {
 		double u = ((double) _u) / params.width * 2 - 1;
 		double v = ((double) _v) / params.height * 2 - 1;
 
 		// Distort camera coordinates
 		double xc = (u - params.kc[6]) / params.kc[0];
 		double yc = (v - params.kc[7]) / params.kc[4];
 		double r2 = xc * xc + yc * yc;
 		double r2c = ((1.0f) + params.invdistc[0] * r2 + params.invdistc[1] * r2
 				* r2 + params.invdistc[4] * r2 * r2 * r2);
 		double xcd = xc * r2c + (2.0f) * params.invdistc[2] * xc * yc
 				+ params.invdistc[3] * (r2 + (2.0f) * xc * xc);
 		double ycd = yc * r2c + (2.0f) * params.invdistc[3] * xc * yc
 				+ params.invdistc[2] * (r2 + (2.0f) * yc * yc);
 		xcd = xcd * params.kc[0] + params.kc[6];
 		ycd = ycd * params.kc[4] + params.kc[7];
 
 		// Unnormalized camera rays
 		double dx = params.kc[4] * xcd - params.kc[4] * params.kc[6];
 		double dy = params.kc[0] * ycd - params.kc[0] * params.kc[7];
 		double dz = params.kc[0] * params.kc[4];
 
 		double x = dx / dz;
 		double y = dy / dz;
		return new Point((int) x, (int) y);
 	}
 
 	public void unproject(int u, int v, int _d, double[] x, double[] y, double[] z) {
 		double d = ((double) _d) / (65535) * params.zmax;
 		Point p = m_unprojCoeffs[v * m_zWidth + u];
 
 		x[0] = p.x * d;
 		y[0] = p.y * d;
 		z[0] = d;
 	}
 
 }
