 package edu.uw.cs.lil.tiny.tempeval;
 
 import edu.uw.cs.lil.tiny.ccg.categories.ICategoryServices;
 import edu.uw.cs.lil.tiny.data.IDataCollection;
 import edu.uw.cs.lil.tiny.data.ILabeledDataItem;
 import edu.uw.cs.lil.tiny.data.sentence.Sentence;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicLanguageServices;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
 import edu.uw.cs.lil.tiny.mr.lambda.Ontology;
 import edu.uw.cs.lil.tiny.mr.lambda.ccg.LogicalExpressionCategoryServices;
 import edu.uw.cs.lil.tiny.mr.language.type.TypeRepository;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.AbstractCKYParser;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.CKYBinaryParsingRule;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.multi.MultiCKYParser;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.single.CKYParser;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.single.CKYUnaryParsingRule;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.FactoredLexicon;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.Lexeme;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.LexemeFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.LexicalTemplateFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.scorers.LexemeCooccurrenceScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.LexicalFeatureSet;
//import edu.uw.cs.lil.tiny.parser.ccg.features.basic.LexicalFeatureSetBuilder;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.LogicalExpressionCoordinationFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.LogicalExpressionTypeFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.ExpLengthLexicalEntryScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.ScalingScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.SkippingSensitiveLexicalEntryScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.UniformScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.ILexicon;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry;
//import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry.EntryOrigin;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.Lexicon;
 import edu.uw.cs.lil.tiny.parser.ccg.model.Model;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.RuleSetBuilder;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.BackwardApplication;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.BackwardComposition;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.ForwardApplication;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.ForwardComposition;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.skipping.BackwardSkippingRule;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.skipping.ForwardSkippingRule;
 import edu.uw.cs.lil.tiny.utils.concurrency.TinyExecutorService;
 import edu.uw.cs.lil.tiny.utils.string.StubStringFilter;
 import edu.uw.cs.utils.composites.Pair;
 import edu.uw.cs.utils.log.ILogger;
 import edu.uw.cs.utils.log.Log;
 import edu.uw.cs.utils.log.LogLevel;
 import edu.uw.cs.utils.log.Logger;
 import edu.uw.cs.utils.log.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 public class TempEval2Dev {
 	private static final ILogger LOG = LoggerFactory.create(TempEval2Dev.class);
 
 	public static void main(String[] args) {
 		boolean testingDataset = false;
 		
 		Logger.DEFAULT_LOG = new Log(System.out);
 		Logger.setSkipPrefix(true);
 		LogLevel.INFO.set();
 
 		//long startTime = System.currentTimeMillis();
 
 		// Relative path for data directories
 		String datasetDir = "./data/dataset/";
 		String resourcesDir = "./data/resources/";
 
 		
 		// Init the logical expression type system
 		LogicLanguageServices.setInstance(new LogicLanguageServices(
 				new TypeRepository(
 						new File(resourcesDir + "tempeval.types.txt")), "i"));
 
 		
		// TODO: Check the constructors for this. There is a second one, that takes two booleans.
		final ICategoryServices<LogicalExpression> categoryServices = new LogicalExpressionCategoryServices();
 		
 
 		// Load the ontology
 		List<File> ontologyFiles = new LinkedList<File>();
 		ontologyFiles.add(new File(resourcesDir + "tempeval.predicates.txt"));
 		ontologyFiles.add(new File(resourcesDir + "tempeval.constants.txt"));
 		try {
 			// Ontology is currently not used, so we are just reading it, not
 			// storing
 			new Ontology(ontologyFiles);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		
 		// When running on testing dataset, testingDataset = true
 		String dataLoc;
 		if (testingDataset)
 			dataLoc = "tempeval3.testing.txt";
 		else
 			dataLoc = "tempeval3.timebank.txt";
 			//dataLoc = "tempeval.dataset.corrected.txt";
 		IDataCollection<? extends ILabeledDataItem<Pair<String[], Pair<Sentence, TemporalSentence>>, Pair<String, String>>> train = TemporalSentenceDataset
 				.read(new File(datasetDir + dataLoc),
 						new StubStringFilter(), true);
 		IDataCollection<? extends ILabeledDataItem<Pair<String[], Pair<Sentence, TemporalSentence>>, Pair<String, String>>> test = TemporalSentenceDataset
 				.read(new File(datasetDir + dataLoc),
 						new StubStringFilter(), true);
 		LOG.info("Train Size: " + train.size());
 		LOG.info("Test Size: " + test.size());
 
 		// Below is the code from Geo880DevParameterEstimation. I am replacing this with code from Geo880Dev.
 		/*
 		// Init the lexicon
 		final ILexicon<LogicalExpression> lexicon = new Lexicon<LogicalExpression>();
 		lexicon.addEntriesFromFile(
 				new File(
 						resourcesDir + "tempeval.lexicon.txt"),
 				new StubStringFilter(), categoryServices,
 				EntryOrigin.FIXED_DOMAIN);
 
 		LOG.info("Size of lexicon: %d", lexicon.size());
 
 		LOG.info("Size of lexicon: %d", lexicon.size());
 
 		final LexicalFeatureSet<LogicalExpression> lexPhi = new LexicalFeatureSetBuilder<LogicalExpression>()
 				.setInitialFixedScorer(
 						new ExpLengthLexicalEntryScorer<LogicalExpression>(
 								10.0, 1.1))
 				.setInitialScorer(
 						new SkippingSensitiveLexicalEntryScorer<LogicalExpression>(
 								categoryServices,
 								-1.0,
 								new UniformScorer<LexicalEntry<LogicalExpression>>(
 										0.0))).build();
 
 		// Create the model
 		final Model<Sentence, LogicalExpression> model = new Model.Builder<Sentence, LogicalExpression>()
 				.addParseFeatureSet(
 						new LogicalExpressionCoordinationFeatureSet<Sentence>())
 				.addParseFeatureSet(
 						new LogicalExpressionTypeFeatureSet<Sentence>())
 				.addLexicalFeatureSet(lexPhi)
 				.setLexicon(new Lexicon<LogicalExpression>()).build();
 		*/
 		
 		// Init the lexicon
 				// final Lexicon<LogicalExpression> fixed = new
 				// Lexicon<LogicalExpression>();
 		final ILexicon<LogicalExpression> fixedInput = new Lexicon<LogicalExpression>();
 		fixedInput.addEntriesFromFile(new File(resourcesDir
 				+ "tempeval.lexicon.txt"), new StubStringFilter(),
				categoryServices, LexicalEntry.Origin.FIXED_DOMAIN);
 		
 		final ILexicon<LogicalExpression> fixed = new Lexicon<LogicalExpression>();
 		
 		// factor the fixed lexical entries
 		//for (final LexicalEntry<LogicalExpression> lex : fixedInput
 		//		.toCollection()) {
 		//	fixed.add(FactoredLexicon.factor(lex));
 		//}
 		//System.out.println(fixed);
 		
 		// try two at factoring the fixed lexical entries:
 		for (final LexicalEntry<LogicalExpression> lex : fixedInput.toCollection()){
 		    fixed.add(lex);
 		}
 		
		final LexicalFeatureSet<Sentence, LogicalExpression> lexPhi = new LexicalFeatureSet.Builder<Sentence,LogicalExpression>()
 				.setInitialFixedScorer(
 						new ExpLengthLexicalEntryScorer<LogicalExpression>(
 								10.0, 1.1))
 				.setInitialScorer(
 						new SkippingSensitiveLexicalEntryScorer<LogicalExpression>(
 								categoryServices,
 								-1.0,
 								new UniformScorer<LexicalEntry<LogicalExpression>>(
 										0.0)))
 				// .setInitialWeightScorer(gizaScores)
 				.build();
 				
 				// Create the lexeme feature set
 				//final LexemeCooccurrenceScorer gizaScores;
 				// final DecoderHelper<LogicalExpression> decoderHelper = new
 				// DecoderHelper<LogicalExpression>(
 				// categoryServices);
 				//try {
 				//	gizaScores = new LexemeCooccurrenceScorer(new File(resourcesDir
 				//			+ "/geo600.dev.giza_probs"));
 				//} catch (final IOException e) {
 				//	System.err.println(e);
 				//	throw new RuntimeException(e);
 				//}
 				//final LexemeFeatureSet lexemeFeats = new LexemeFeatureSet.Builder()
 				//		.setInitialFixedScorer(new UniformScorer<Lexeme>(0.0))
 				//		.setInitialScorer(new ScalingScorer<Lexeme>(10.0, gizaScores))
 				//		.build();
 				
 				// This was used for initializing the factored lexicon
 				final LexicalTemplateFeatureSet templateFeats = new LexicalTemplateFeatureSet.Builder()
 						.setScale(0.1)
 						// .setInitialWeightScorer(new LexicalSyntaxPenaltyScorer(-0.1))
 						.build();
 				
 				// Create the entire feature collection
 				// Adjusted to move away from a factored lexicon
 				final Model<Sentence, LogicalExpression> model = new Model.Builder<Sentence, LogicalExpression>()
 						.addParseFeatureSet(
								new LogicalExpressionCoordinationFeatureSet<Sentence>(true, true, true))
 						.addParseFeatureSet(
 								new LogicalExpressionTypeFeatureSet<Sentence>())
 						.addLexicalFeatureSet(lexPhi)//.addLexicalFeatureSet(lexemeFeats)
 						.addLexicalFeatureSet(templateFeats)
 						.setLexicon(new Lexicon<LogicalExpression>()).build();
 				
 		
 		// Initialize lexical features. This is not "natural" for every lexical
 		// feature set, only for this one, so it's done here and not on all
 		// lexical feature sets.
 		model.addFixedLexicalEntries(fixed.toCollection());
 
 		// Parsing rules
 		final RuleSetBuilder<LogicalExpression> ruleSetBuilder = new RuleSetBuilder<LogicalExpression>();
 
 		// Binary rules
 		ruleSetBuilder.add(new ForwardComposition<LogicalExpression>(
 				categoryServices));
 		ruleSetBuilder.add(new BackwardComposition<LogicalExpression>(
 				categoryServices));
 		ruleSetBuilder.add(new ForwardApplication<LogicalExpression>(
 				categoryServices));
 		ruleSetBuilder.add(new BackwardApplication<LogicalExpression>(
 				categoryServices));
 
 		// Executor for multi-threading
 		final TinyExecutorService executor = new TinyExecutorService(Runtime
 				.getRuntime().availableProcessors());
 
 		LOG.info("Using %d threads", Runtime.getRuntime().availableProcessors());
 
 		// Create the parser -- support empty rule
 		final AbstractCKYParser<LogicalExpression> parser = new CKYParser.Builder<LogicalExpression>(
 				categoryServices)//, executor)
 				.addBinaryParseRule(
 						new CKYBinaryParsingRule<LogicalExpression>(
 								ruleSetBuilder.build()))
 				.addBinaryParseRule(
 						new CKYBinaryParsingRule<LogicalExpression>(
 								new ForwardSkippingRule<LogicalExpression>(
 										categoryServices)))
 				.addBinaryParseRule(
 						new CKYBinaryParsingRule<LogicalExpression>(
 								new BackwardSkippingRule<LogicalExpression>(
 										categoryServices)))
 				.addUnaryParseRule(new CKYUnaryParsingRule<LogicalExpression>(new SentenceCompilation(categoryServices), true))
 				.setMaxNumberOfCellsInSpan(100).build();
 
 		TemporalTesterSmall tester = TemporalTesterSmall.build(test, parser);
 
 		// Make new training set from old training set. Should include logical
 		// expressions for phrases.
 		// Do ^^^ in tester! Or maybe not, because I need it for the learner...
 
 		// final ILearner<Sentence, LogicalExpression> learner = new
 		// SimplePerceptron<Sentence, LogicalExpression>(
 		// 20, train, parser);
 
 		// learner.train(model);
 
 		// Within this tester, I should go through each example and use the
 		// visitor on each logical expression!
 		tester.test(model);
 
 		//LOG.info("Total runtime %.4f seconds", Double.valueOf(System
 		//		.currentTimeMillis() - startTime / 1000.0D));
 	}
 }
