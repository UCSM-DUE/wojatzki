//package annotators.stackedAnnotators;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.uima.UIMAFramework;
//import org.apache.uima.UimaContext;
//import org.apache.uima.analysis_engine.AnalysisEngine;
//import org.apache.uima.analysis_engine.AnalysisEngineDescription;
//import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
//import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
//import org.apache.uima.fit.factory.AnalysisEngineFactory;
//import org.apache.uima.fit.util.JCasUtil;
//import org.apache.uima.jcas.JCas;
//import org.apache.uima.resource.ExternalResourceDescription;
//import org.apache.uima.resource.ResourceInitializationException;
//import org.dkpro.tc.api.type.TextClassificationOutcome;
//import org.dkpro.tc.core.Constants;
//import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
//import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
//import org.dkpro.tc.core.util.SaveModelUtils;
//import org.dkpro.tc.fstore.simple.DenseFeatureStore;
//import org.dkpro.tc.ml.uima.TcAnnotator;
//
//import predictedTypes.ClassifiedSubTarget;
//import predictedTypes.NgramClassification;
//
//public class Stacked_NgramClassification extends JCasAnnotator_ImplBase{
//	
//	private ArrayList<String> ngramVariants = new ArrayList<String>(Arrays.asList("ATHEISM_char", "ATHEISM_word"));
//	
//	private Map<String, AnalysisEngine> subTargetToModel;
//	private TCMachineLearningAdapter mlAdapter;
//	private String learningMode = Constants.LM_SINGLE_LABEL;
//	private String featureMode = Constants.FM_DOCUMENT;
//	
//	@Override
//	public void initialize(UimaContext context) throws ResourceInitializationException {
//		super.initialize(context);
//
//		subTargetToModel = new HashMap<String, AnalysisEngine>();
//		// System.out.println(tcModelLocations);
//		for (String subTarget : ngramVariants) {
//			File modelFile=new File("src/main/resources/models/"+subTarget);
//			try {
//				mlAdapter = SaveModelUtils.initMachineLearningAdapter(modelFile);
//				List<Object> parameters = SaveModelUtils.initParameters(modelFile);
//				List<ExternalResourceDescription> featureExtractors = SaveModelUtils.loadExternalResourceDescriptionOfFeatures(modelFile,context);
//
//				AnalysisEngineDescription connector = getSaveModelConnector(parameters, modelFile.getAbsolutePath(),
//						mlAdapter.getDataWriterClass().toString(), learningMode, featureMode,
//						DenseFeatureStore.class.getName(), featureExtractors.toArray(new String[0]),context);
//
//				AnalysisEngine engine = UIMAFramework.produceAnalysisEngine(connector,
//						SaveModelUtils.getModelFeatureAwareResourceManager(modelFile), null);
//				subTargetToModel.put(subTarget, engine);
//
//			} catch (Exception e) {
//				throw new ResourceInitializationException(e);
//			}
//		}
//	}
//	
//	@Override
//	public void process(JCas jcas) throws AnalysisEngineProcessException {
//		for (String subTarget : ngramVariants) {
//			String goldOutcome = "";
//			
//				goldOutcome = JCasUtil.selectSingle(jcas, TextClassificationOutcome.class).getOutcome();
//				JCasUtil.selectSingle(jcas, TextClassificationOutcome.class).removeFromIndexes();
//				TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
//				outcome.setOutcome("");
//				outcome.addToIndexes();
//				
//				try {
//					subTargetToModel.get(subTarget).process(jcas);
//				} catch (Exception e) {
//					throw new AnalysisEngineProcessException(e);
//				}
////				 annotate
//				NgramClassification annotation = new NgramClassification(jcas);
//				
//				annotation.setClassificationOutcome(JCasUtil.selectSingle(jcas, TextClassificationOutcome.class).getOutcome());
//				annotation.setVariant(subTarget);
//				annotation.setBegin(0);
//				annotation.setEnd(jcas.getDocumentText().length());
//				annotation.addToIndexes();
//
//				// drop outcome from stacked classification
//				JCasUtil.selectSingle(jcas, TextClassificationOutcome.class).removeFromIndexes();
//				outcome = new TextClassificationOutcome(jcas);
//				//reset the gold annotation
//				outcome.setOutcome(goldOutcome);
//				outcome.addToIndexes();
//			
//		}
//	}
//
//	/**
//	 * @param featureExtractorClassNames
//	 * @param context 
//	 * @return A fully configured feature extractor connector
//	 * @throws ResourceInitializationException
//	 */
//	private AnalysisEngineDescription getSaveModelConnector(List<Object> parameters, String outputPath,
//			String dataWriter, String learningMode, String featureMode, String featureStore,
//			String... featureExtractorClassNames, UimaContext context) throws ResourceInitializationException {
//		// convert parameters to string as external resources only take string
//		// parameters
//		List<Object> convertedParameters = SaveModelUtils.convertParameters(parameters);
//
//		List<ExternalResourceDescription> extractorResources = SaveModelUtils.loadExternalResourceDescriptionOfFeatures(outputPath, context)
//
//		// add the rest of the necessary parameters with the correct types
//		parameters.addAll(Arrays.asList(TcAnnotator.PARAM_TC_MODEL_LOCATION, outputPath,
//				ModelSerialization_ImplBase.PARAM_OUTPUT_DIRECTORY, outputPath,
//				ModelSerialization_ImplBase.PARAM_DATA_WRITER_CLASS, dataWriter,
//				ModelSerialization_ImplBase.PARAM_LEARNING_MODE, learningMode,
//				ModelSerialization_ImplBase.PARAM_FEATURE_EXTRACTORS, extractorResources,
//				ModelSerialization_ImplBase.PARAM_FEATURE_FILTERS, null, ModelSerialization_ImplBase.PARAM_IS_TESTING,
//				true, ModelSerialization_ImplBase.PARAM_FEATURE_MODE, featureMode,
//				ModelSerialization_ImplBase.PARAM_FEATURE_STORE_CLASS, featureStore));
//
//		return AnalysisEngineFactory.createEngineDescription(mlAdapter.getLoadModelConnectorClass(),
//				parameters.toArray());
//	}
//}
