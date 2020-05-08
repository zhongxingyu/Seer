 	package controllers;
 
 	import java.util.*;
 
 	import play.mvc.*;
 	import play.data.*;
 	import play.*;
 
 	import views.html.*;
 
 	import models.*;
 
 	import com.avaje.ebean.*; //dont think this should be here due to SqlRow
 	import static play.libs.Json.toJson;
 	import static play.libs.Json.*;
 	import java.net.URLDecoder;
 	import java.io.UnsupportedEncodingException;
 
 	/**
 	 * Manage a database of computers
 	 */
 	public class Application extends Controller {
 	    
 	    /**
 	     * This result directly redirect to application home.
 	     */
 	    public static Result GO_HOME = redirect(
 		routes.Application.list2(0, "name", "asc", "", "")
 	    );
 		
 	    public static Result stref(Long id) {
 		System.out.println("testing params value " + id);
 		Streference display = Streference.find.byId(id);
 
 		return ok( 
 				//render(id, display)t
 				stref.ref().render(display)
 				); 
 	    };
 
 	    public static Result proteinsummary(String protein) {
 			
 		List<SqlRow> listSql = null;
 		ArrayList<SqlRow> listSqlArray = new ArrayList<SqlRow>();
 		String proteinName = "";
 		String swissProtName = "";	
 		ArrayList<Biolsource> biolSourceProtein = new ArrayList<Biolsource>();
 		List<Biolsource> biolSourceProteins = Biolsource.findBiolSourceIds(protein);
 		//List<Double> biorefs = new ArrayList<Double>();
 		String accession = "";
 		for(Biolsource biol : biolSourceProteins){
 			System.out.println("test biolsource" + biol.id);
 			proteinName = biol.protein;
 			swissProtName = biol.swiss_prot;
 			Biolsource objectBiolSource = Ebean.find(Biolsource.class, biol.id);
 				biolSourceProtein.add(objectBiolSource);
 
 			listSql = Sourceref.findReferenceSource(biol.id);
 			listSqlArray.addAll(listSql);
 				//biorefs.add(objectBiolSource.reference_id);
 		}
 
 		List<Proteins> proteins = Proteins.findProteins(protein);
 		List<Sites> sites = Sites.findSites(protein);
 		
 		List<String> uniprotDetails = UniprotConnection.EntryRetrievalExample(protein);
 		String sequenceRetrieval = UniprotConnection.EntryRetrievalSequence(protein);
 
 		//might kill the above
 		System.out.println("the accession is " + protein);
 		//List<GsProteinSite> gsProteinSite = GsProteinSite.ProteinRetrieval(protein);
 		List<GsProteinStr2> gsProteinSite = GsProteinStr2.ProteinRetrieval(protein);
 		//List<SitesReferences2> description = SitesReferences2.findSitesReferences(protein);
 		List<SitesReferences> description = SitesReferences.findSites(protein);
 		
 	return ok(
 		proteinsummary.render(proteinName, protein, biolSourceProtein, proteins, uniprotDetails, sites, gsProteinSite, listSqlArray, description, sequenceRetrieval)
 	);
     }
 
     public static Result compositions() {
 	List<Structure> compositionResult = null;
 	 if (request().queryString().size() > 0  ) {
                 Map<String, String[]> params = request().queryString();
                 String[] searchTerms = null;
                 String key = null;
                 for (Map.Entry<String, String[]> entry : params.entrySet() ){
                         key = entry.getKey();
                         searchTerms = entry.getValue();
                 }
                 if(key.contains("comp")) {
                         String out =  Structure.buildComposition(searchTerms);
                         System.out.println("output is " + out);
                         compositionResult = Structure.findComposition(out);
                 }
     	
     	return ok(
 		compositions.render(compositionResult)
 		 );
 	}
 	return ok(compositions.render(compositionResult));
     }
 
     public static Result browse() {
 	List<String> taxonomy  = Taxonomy.findSpecies(); 
 	HashSet taxUnique = Taxonomy.findSpeciesUnique();
 
 	HashSet sourceUnique = Tissue.sourceSummary();
 	HashSet tissueUnique = GlycobaseSource.tissueSummary();
 	sourceUnique.addAll(tissueUnique);
 
 	HashSet proteinUnique = Proteins.proteinSummary();
 	HashSet pertubationUnique = GlycobaseSource.perturbationSummary();
 	proteinUnique.addAll(pertubationUnique);
 
 
 	List<Tissue> foundTissue = null;
 	List<GlycobaseSource> glycobasesource = null;
 	List<GlycobaseSource> glycobaseFindPerturbation = null;
 
         List<Biolsource> biolsource = null;
         List<SqlRow> listSql = null;
 	List<SqlRow> glycobaseSql = null;
 	ArrayList<SqlRow> glycobaseSqlArray = new ArrayList<SqlRow>();
 
 	List<SqlRow> glycobaseSqlTissue = null;
         ArrayList<SqlRow> glycobaseSqlArrayTissue = new ArrayList<SqlRow>();
 
 	ArrayList<SqlRow> listSqlArray = new ArrayList<SqlRow>();
         Taxonomy taxonomyId = null;
 	Proteins proteinId = null;
 	Tissue tissueId = null;
 	ArrayList<Taxonomy> taxonomyList = new ArrayList<Taxonomy>();
 	ArrayList<Proteins> proteinList = new ArrayList<Proteins>();
 	ArrayList<Tissue> tissueList = new ArrayList<Tissue>();
 	List<String> listSql22 = new ArrayList<String>();
 	List<List<String>> listSql2 = new ArrayList<List<String>>();
 	String output = "";
 	String outputtissue = "";
 	String outputprotein = "";
 	List<String> outputlist = new ArrayList<String>();
 	List<String> outputtissuelist = new ArrayList<String>();
 	List<String> outputproteinlist = new ArrayList<String>();
 	int countGlycobase = 0;
 	int countTissueGlycobase = 0;
 	int countProteinGlycobase = 0;
 
         if (request().queryString().size() > 0  ) {
 		String glycobasePerturbationFind = "";
                 Map<String, String[]> params = request().queryString();
                 String[] searchTerms = null;
 		String key = null;
                 for (Map.Entry<String, String[]> entry : params.entrySet() ){
 			key = entry.getKey();
                         searchTerms = entry.getValue();
                 }
 
 		/*if(key.contains("comp")) {
 			String out =  Structure.buildComposition(searchTerms);
 			System.out.println("output is " + out);
 			List<Structure> compositions = Structure.findComposition(out);
 
 			return ok(compositions.render(compositions));
 
 			
 		}*/
 
 		if(key.equals("taxonomy")){
                 for (String queryTaxonomy : searchTerms) {
 		
 			String glycobasePerturbation = queryTaxonomy;
 			output = GlycobaseSource.findGlycobaseTaxonomy(glycobasePerturbation);
 			outputlist.add(output);
 
                         List<Taxonomy> foundTaxonomy  = Taxonomy.findSpeciesTemp(queryTaxonomy);
                         Long taxId = null;
                         for (Taxonomy tax : foundTaxonomy) {
                                 taxId = tax.id;
                                 taxonomyId  = Taxonomy.find.byId(taxId);
 				taxonomyList.add(taxonomyId);
                                 String taxon = taxonomyId.species;
                                 biolsource = Biolsource.findTaxonomyProtein(taxon);
                                 listSql = Biolsource.findTaxonomyProteinSQL(taxon);
 				listSql2 = Biolsource.findTaxonomyProteinString(taxon);
                         }
                 }
 		}
 
 		if(key.equals("protein")) {
 		for (String queryProtein : searchTerms) {
 
 			String glycobasePerturbation = queryProtein;
 			
 			//glycobaseFindPerturbation = GlycobaseSource.findPerturbation(glycobasePerturbation);
 			outputprotein = GlycobaseSource.findPerturbation(glycobasePerturbation);
 			outputproteinlist.add(outputprotein);
 
 			List<Proteins> foundProteins = Proteins.findProteins(queryProtein);
 			Long protId = null;
 			for (Proteins protein : foundProteins){
 				protId = protein.id;
 				System.out.println("found protein " + protein);
 			//}
 			//if (protId > 0 && protId != null ) {
 				proteinId = Proteins.find.byId(protId);	
 				proteinList.add(proteinId);
 			}
 		}
 		}
 
 		if(key.equals("tissue")) {
 		for (String queryTissue : searchTerms) {
 			foundTissue = Tissue.findTissue(queryTissue);
 			//glycobaseSqlTissue = GlycobaseSource.findGlycobaseTissue(queryTissue);
                         //glycobaseSqlArrayTissue.addAll(glycobaseSqlTissue);
 
                         outputtissue = GlycobaseSource.findGlycobaseTissue(queryTissue);
                         outputtissuelist.add(outputtissue);
 
 
 			Long tissId = null;
 			for (Tissue source : foundTissue) {
 				tissId = source.id;
 			}
 			if (tissId > 0 ) {
 				tissueId = Tissue.find.byId(tissId);
 				tissueList.add(tissueId);
 			}
 		}
 		}
 
 	for(String xyz : outputlist) {
 		if(xyz.length() > 10) {countGlycobase++;}
 	}
 
 	for(String xyz : outputtissuelist) {
 		if(xyz.length() > 10) {countTissueGlycobase++;}
 	}
 
 	for(String xyz : outputproteinlist) {
 		if(xyz.length() > 10) {countProteinGlycobase++;}
 	}
 
 
 
 
         return ok(browse.render(taxonomy, taxonomyList, biolsource, listSql2, sourceUnique, proteinUnique, proteinList, tissueList, foundTissue, glycobaseFindPerturbation, glycobaseSqlArray, glycobaseSqlArrayTissue, outputlist, countGlycobase, outputtissuelist, countTissueGlycobase, outputproteinlist, countProteinGlycobase));
         }
 
         return ok(browse.render(taxonomy, taxonomyList, biolsource, listSql2, sourceUnique, proteinUnique, proteinList, tissueList, foundTissue, glycobaseFindPerturbation, glycobaseSqlArray, glycobaseSqlArrayTissue, outputlist, countGlycobase, outputtissuelist, countTissueGlycobase, outputproteinlist, countProteinGlycobase));
     }
 
 
     public static Result structureDetails(Long id) {
 
 	//Structure strDisplay = Structure.find.byId(id);
     List<Structure> strDisplay = Structure.findStructureRef(id);
     ArrayList proteinNames = new ArrayList();
     HashSet proteinNamesUnique = new HashSet();
     ArrayList taxNames = new ArrayList();
     ArrayList taxDivs = new ArrayList();
     HashSet taxNamesUnique = new HashSet();
     ArrayList sourceNames = new ArrayList();
     HashSet sourceNamesUnique = new HashSet();
     ArrayList uniprot = new ArrayList();
     HashSet uniprotUnique = new HashSet();
     ArrayList taxIds = new ArrayList();
     HashSet taxIdsUnique = new HashSet();
     HashSet taxDivsUnique = new HashSet();
 
 
     List<String[]> rowList = new ArrayList<String[]>();
      Map<String, Integer> m = new HashMap<String, Integer>();
 
     //rowList.add(new int[] { 1, 2, 3 });
 	
     if (strDisplay !=null){
     	for (Structure entries : strDisplay){
     		List<Stproteins> stToProtein = entries.stproteins;
 		List<Strtaxonomy> stToTax = entries.strtaxonomy;
 		List<Stsource> stToSource = entries.stsource;
     		if (!stToProtein.isEmpty()) {
     			for (Stproteins stProteinEntry : stToProtein){
     				String proteinName = stProteinEntry.proteins.name;
     				//proteinNamesUnique.add(proteinName);
 				String divprotein = "<a href=\"../proteinsummary/" + stProteinEntry.proteins.name +  "\">" + proteinName + "</a>";
 				//uniprotUnique.add(uniprot);
 				proteinNamesUnique.add(divprotein); 
     			}
     			proteinNames.addAll(proteinNamesUnique);
 			uniprot.addAll(uniprotUnique);
     		}
 
 		if (!stToTax.isEmpty()){
 			for (Strtaxonomy stTaxEntry : stToTax){
 				String taxName = stTaxEntry.taxonomy.species;
 				Long taxId = stTaxEntry.taxonomy.id;
 				taxNamesUnique.add(taxName);
 				taxIdsUnique.add(taxId);
 				String divtax = "<a href=\"../taxonomy/" + stTaxEntry.taxonomy.id + "\">" + taxName + "</a>";
 				taxDivsUnique.add(divtax);
 			}
 			taxNames.addAll(taxNamesUnique);
 			taxIds.addAll(taxIdsUnique);
 			taxDivs.addAll(taxDivsUnique);
 		}
 
 		if (!stToSource.isEmpty()){
 			for (Stsource stSourceEntry : stToSource) {
 				String div = "<a href=\"../taxonomy/" +  stSourceEntry.tissue.id + "\"> > " + stSourceEntry.tissue.div1 + " > " + stSourceEntry.tissue.div2 + " > " + stSourceEntry.tissue.div3 + " > " + stSourceEntry.tissue.div4 + "</a> <br />";
 				sourceNamesUnique.add(div);
 			}
 			
 			sourceNames.addAll(sourceNamesUnique);
 		}
     	}
     }
     
 	//Application str;
 	return ok(
 			structureDetails.render(strDisplay, id, proteinNames, taxNames, sourceNames, rowList, uniprot, taxDivs)
 			
 	);
     };
 
     public static Result refdisplay(Long id) {
     	System.out.println("testingssss params value for reference " + id);
     	//Reference displayReference = null;
     	Reference displayReferencce = Reference.find.byId(id);
 	List<Reference> t = Reference.findJournal(id);
 	List<Method> m = Method.findmethod(id);
 	List<Reference> u = Reference.findRefMethods(id);
 
 	for (Reference r : t ) {
 		List<Sourceref> biol = r.sourceref;		
 	}
 	
 	ArrayList taxsources = new ArrayList();
 	ArrayList proteinsources = new ArrayList();
 	ArrayList protsources = new ArrayList();
 	HashSet hs = new HashSet();
 	HashSet proteinHs = new HashSet();
 	HashSet swissHs = new HashSet();
 	
 	for (Reference taxfind : u){
 		List<Sourceref> source = taxfind.sourceref;
 		for (Sourceref tax : source){
 			hs.add(tax.biolsource.taxonomy);
 			proteinHs.add(tax.biolsource.protein);
 			swissHs.add(tax.biolsource.swiss_prot);
 			//String taxsource = tax.biolsource.taxonomy;
 			//taxsources.add(taxsource); 
 		}
 	}
 	
 	taxsources.addAll(hs);
 	proteinsources.addAll(proteinHs);
 	protsources.addAll(swissHs);
 	
 	
     	//List<Reference> sourcered = Reference.findSourceref(id);	
 	System.out.println("testing value: " + t.size() ) ;
     	return ok( 
     			//refdisplay.render(displayReference)
     			//list2.
     			//refdisplay.ref().render(displayReferencce)
 			refdisplay.render("View selected reference", t, u, taxsources, proteinsources, protsources)
     			); 
     };
     
 
     
     /*
     private static Content render(Reference displayReference) {
 		// TODO Auto-generated method stub
 		return null;
 	} */
 
 	/**
      * Handle default path requests, redirect to computers list
      */
     public static Result index() {
         //return GO_HOME;
 	return ok ( index.render() );
     }
 
     public static Result builder() {
 	return ok (builder.render() );
     }
 
     /*public static Result saySearch(String structure) {
 	try{
 		String result = URLDecoder.decode(structure, "UTF-8");
 		System.out.println("string test; " + result);
 	} catch (UnsupportedEncodingException e) {
         e.printStackTrace();
     }
 	
 		controllers.Search.searchStructure();
 	//return ok (saysearch.render() );
 	return ok ( saySearch.render() );
     }*/
 	
     public static Result ms() {
 	List<SqlRow> results = Lcmucin.groupLcGlycans();
         return ok ( ms.render(results) );
     }
 
     /**
      * Display the taxon search results.
      * @param taxon Search taxon string
     */
     public static Result taxonDetails(Long id){
 	Taxonomy taxonomy  = Taxonomy.find.byId(id);
 	String taxon = taxonomy.species;
         List<Biolsource> biolsource = Biolsource.findTaxonomyProtein(taxon);
 	List<SqlRow> listSql = Biolsource.findTaxonomyProteinSQL(taxon);
 	//return TODO;
 	return ok(
 		taxonDetails.render("Taxonomy Description", taxonomy, biolsource, listSql)
 	);
     }
 
     public static Result taxonsearch(String findTaxon) {
 	List<Taxonomy> foundTaxonomy  = Taxonomy.findSpeciesTemp(findTaxon);
 	Long taxId = null;
 	for (Taxonomy tax : foundTaxonomy) {
 		taxId = tax.id;
 		System.out.println("this is the id: " + taxId);
 	};
 	if (taxId > 0) {
 	Taxonomy taxonomy  = Taxonomy.find.byId(taxId);
         String taxon = taxonomy.species;
 	List<Biolsource> biolsource = Biolsource.findTaxonomyProtein(taxon);
         List<SqlRow> listSql = Biolsource.findTaxonomyProteinSQL(taxon);
 	
         //return TODO;
 	return ok(
                 taxonDetails.render("Taxonomy Description", taxonomy, biolsource, listSql));
 	}
 	else { return TODO;}
     }
 
 
     /**
      * Display the paginated list of computers.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filter Filter applied 
      */
     public static Result list2(int page, String sortBy, String order, String filter, String protein) {
         return ok(
             list.render(
                 Reference.page(page, 10, sortBy, order, filter),
                 sortBy, order, filter
             )
         );
     }
 
     /**
      * Display the paginated list of unicarbreferences.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filter Filter applied on computer names
      */
     public static Result unicarb(int page, String sortBy, String order, String filter) {
         return ok(
             unicarb.render(
                 Unicarbdbreference.page(page, 10, sortBy, order, filter),
                 sortBy, order, filter
             )
         );
     }
 
     public static Result findAllSpecies() {
 	List<String> speciesCollection = Taxonomy.findSpecies();
  	
 	//AString target = "[ ";	
 	String target = "";
 	for(String species : speciesCollection) {
 		//target += "\"" + species + "\", ";
 		target += "replacethis" + species + "replacethis, ";
 	}
 	String endTarget = ""; // " ]";
 	String finalTarget = target.concat(endTarget).toString();
 	System.out.println("final string: " + finalTarget);
 	return ok(
 		species.render("Display Species", speciesCollection, finalTarget)
 	);
     }
 
 
     public static Result tissueSummary(Long id) {
 
 	//	List<Stsource> stsourceTissue = Tissue.tissueStructures(tissueResult);
 		List<Tissue> tissueresult = Tissue.tissuehelp(id);
 		String databaseReference = "";
 		for(Tissue t : tissueresult) {
 			databaseReference = t.div1 + t.div2 + t.div3 + t.div4;
 		}
 
 	return ok(
 		tissuesummary.render("Tissue Summary", databaseReference, tissueresult)
 	);
     } 
     
 }
             
