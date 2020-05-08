 /*
   Copyright (c) 2012 Malte Mader <mader@zbh.uni-hamburg.de>
   Copyright (c) 2012 Center for Bioinformatics, University of Hamburg
 
   Permission to use, copy, modify, and distribute this software for any
   purpose with or without fee is hereby granted, provided that the above
   copyright notice and this permission notice appear in all copies.
 
   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
 
 package extended;
 
 import com.sun.jna.Pointer;
 import com.sun.jna.ptr.PointerByReference;
 
 import gtnative.GT;
 import annotationsketch.FeatureIndex;
 import core.Array;
 import core.GTerror;
 import core.GTerrorJava;
 import core.Range;
 
 public class AnnoDBEnsembl extends AnnoDBSchema {
 
 	public AnnoDBEnsembl() {
 		
 		super.adb_ptr = GT.INSTANCE.gt_anno_db_ensembl_new();
 	}
 
 	/**
 	 * Fetch all segments for a given range.
 	 * 
 	 * @param fis The FeatureIndex.
 	 * @param seqid The chromosome.
 	 * @param qryRange The start and end positions.
 	 * @return An Array containing segments as FeatureNodes.
 	 * @throws GTerrorJava
 	 */
 	public Array getFeaturesForRange(FeatureIndex fis, String seqid, Range qryRange) throws GTerrorJava{
 		
 		Array results = new Array(Pointer.SIZE);
 		
 		GTerror err = new GTerror();
 		
 		GT.INSTANCE.gt_feature_index_ensembl_get_features_for_range(fis.to_ptr(), results.to_ptr(), seqid, qryRange, err.to_ptr());
 		
 		if(err.is_set()){
 			throw new GTerrorJava(err.get_err());
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * Fetches a a gene for the given gene name.
 	 * 
 	 * @param gfi The FeatureIndex.
 	 * @param geneName The gene name.
 	 * @return A Gene as FeatureNode.
	 * @throws GTerrorJava
 	 */
 	public FeatureNode getFeatureForGeneName(FeatureIndex gfi, String geneName) throws GTerrorJava{
 		
 		GTerror err = new GTerror();
 		
 		PointerByReference gn_ptr = new PointerByReference();
 		
 		GT.INSTANCE.gt_feature_index_ensembl_get_feature_for_gene_name(gfi.to_ptr(), gn_ptr, geneName, err.to_ptr());
 		
 		if(err.is_set()){
 			throw new GTerrorJava(err.get_err());
 		}
 		
 		return new FeatureNode(gn_ptr.getValue());
 	}
 	
 	/**
 	 * Fetches a gene for an Ensembl stable ID.
 	 * 
 	 * @param gfi The FeatureIndex.
 	 * @param stableId The given Ensembl stable ID.
 	 * @return A Gene as FeatureNode.
 	 * @throws GTerrorJava
 	 */
 	public FeatureNode getFeatureForStableId(FeatureIndex gfi, String stableId) throws GTerrorJava{
 		
 		GTerror err = new GTerror();
 		
 		PointerByReference gn_ptr = new PointerByReference();
 		
 		GT.INSTANCE.gt_feature_index_ensembl_get_feature_for_stable_id(gfi.to_ptr(), gn_ptr, stableId, err.to_ptr());
 		
 		if(err.is_set()){
 			throw new GTerrorJava(err.get_err());
 		}
 		
 		return new FeatureNode(gn_ptr.getValue());
 	}
 	
 	/**
 	 * Fetches the Range of a given karyoband.
 	 * 
 	 * @param gfi The FeatureIndex.
 	 * @param chr The chromosome.
 	 * @param band The band.
 	 * @return The range of the karyoband.
 	 * @throws GTerrorJava
 	 */
 	public Range getRangeForKaryoband(FeatureIndex gfi, String chr, String band) throws GTerrorJava{
 		
 		GTerror err = new GTerror();
 		Range r = new Range();
 		GT.INSTANCE.gt_feature_index_ensembl_get_range_for_karyoband(gfi.to_ptr(), r, chr, band, err.to_ptr());
 		
 		if(err.is_set()){
 			throw new GTerrorJava(err.get_err());
 		}
 		
 		return r;
 	}
 	
 	/**
 	 * Fetches karyobands that overlap with a given range.
 	 * 
 	 * @param gfi The FeatureIndex.
 	 * @param seqid The chromosome.
 	 * @param qryRange The given Range.
 	 * @return An array of karyobands as FeatureNode.
 	 * @throws GTerrorJava
 	 */
 	public Array getKaryobandFeaturesForRange(FeatureIndex gfi, String seqid, Range qryRange) throws GTerrorJava{
 		
 		Array results = new Array(Pointer.SIZE);
 		
 		GTerror err = new GTerror();
 		
 		GT.INSTANCE.gt_feature_index_ensembl_get_karyoband_features_for_range(gfi.to_ptr(), results.to_ptr(), seqid,
 				qryRange, err.to_ptr());
 		
 		if(err.is_set()){
 			throw new GTerrorJava(err.get_err());
 		}
 		
 		return results;
 	}
 }
