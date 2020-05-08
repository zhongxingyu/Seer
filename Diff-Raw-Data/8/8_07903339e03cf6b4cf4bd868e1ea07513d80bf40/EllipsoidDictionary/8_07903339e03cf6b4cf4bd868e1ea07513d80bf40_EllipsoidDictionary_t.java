 /*
  * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
  * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
  * Meteorological Service of New Zealand
  */
 package com.metservice.gallium.projection;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.metservice.argon.ArgonText;
 
 /**
  * @author roach
  */
 class EllipsoidDictionary {
 
 	private static final EllipsoidDictionary Instance = newInstance();
 
 	private static EllipsoidDictionary newInstance() {
 		final Builder b = new Builder(16);
 		b.add(Ellipsoid.SPHERE);
 		b.add(Ellipsoid.WGS84);
 		b.add(Ellipsoid.newInverseFlattening("NAD83", 6_378_137.0, 298.257222101, "NAD83: GRS 1980 (IUGG, 1980)"));
 		b.add(Ellipsoid.newInverseFlattening("GRS80", 6_378_137.0, 298.257222101, "GRS 1980 (IUGG, 1980)"));
 		b.add(Ellipsoid.newInverseFlattening("WGS72", 6_378_135.0, 298.26, "WGS 72"));
 		b.add(Ellipsoid.newMinor("new_intl", 6_378_157.5, 6_356_772.2, "New International 1967"));
 		b.add(Ellipsoid.newMinor("mod_airy", 6_377_340.189, 6_356_034.446, "Modified Airy"));
 		b.add(Ellipsoid.newMinor("airy", 6_377_563.396, 6_356_256.910, "Airy 1830"));
 		b.add(Ellipsoid.newMinor("australian", 6_378_160.0, 6_356_774.719, "Australian"));
 		return new EllipsoidDictionary(b);
 	}
 
 	public static Ellipsoid findByName(String qcc) {
 		return Instance.findByNameImp(qcc);
 	}
 
 	private Ellipsoid findByNameImp(String qcc) {
 		if (qcc == null || qcc.length() == 0) throw new IllegalArgumentException("string is null or empty");
 		final String oqcctw = ArgonText.oqtw(qcc);
 		if (oqcctw == null) return null;
 		final Ellipsoid oMatch = m_mapNameFull.get(oqcctw);
 		if (oMatch != null) return oMatch;
		return m_mapNameShort.get(oqcctw);
 	}
 
 	private EllipsoidDictionary(Builder b) {
 		assert b != null;
 		m_mapNameFull = b.nf;
		m_mapNameShort = b.ns;
 	}
 
 	private final Map<String, Ellipsoid> m_mapNameFull;
	private final Map<String, Ellipsoid> m_mapNameShort;
 
 	private static class Builder {
 
 		void add(Ellipsoid e) {
 			assert e != null;
 			final DualName n = e.name;
 			nf.put(n.qcctwFullName(), e);
 			if (n.hasDistinctShortName()) {
 				ns.put(n.qcctwShortName(), e);
 			}
 		}
 
 		Builder(int initCap) {
 			nf = new HashMap<>(initCap);
 			ns = new HashMap<>(initCap);
 		}
 		final Map<String, Ellipsoid> nf;
 		final Map<String, Ellipsoid> ns;
 	}
 }
