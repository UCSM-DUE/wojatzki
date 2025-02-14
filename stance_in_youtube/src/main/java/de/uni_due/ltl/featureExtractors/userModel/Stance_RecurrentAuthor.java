package de.uni_due.ltl.featureExtractors.userModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.generic.Select;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.deeplearning4j.datasets.iterator.ReconstructionDataSetIterator;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.uni_due.ltl.util.Id2OutcomeUtil;
import preprocessing.Users;

/**
 * Feature extractor to extract the last stance (the polarity of the last utterance) of the current author (if present)
 * @author michael
 *
 */
public class Stance_RecurrentAuthor extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{

	public static final String PARAM_USE_ORACLE = "useOracleReccurrentAuthor";
	@ConfigurationParameter(name = PARAM_USE_ORACLE, mandatory = true, defaultValue="false")
	private boolean useOracle;
	
	public static final String PARAM_ID2OUTCOME_FOLDER_PATH = "id2outcomeTargetFolderPath";
	@ConfigurationParameter(name = PARAM_ID2OUTCOME_FOLDER_PATH, mandatory = true)
	private String id2outcomeTargetFolderPath;
	
	
	private Map<String, Integer> debate_id2Outcome;
	private Map<String,Map<String, Integer>> explicitTarget2id2Outcome;
	
	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		debate_id2Outcome= Id2OutcomeUtil.getId2OutcomeMap(id2outcomeTargetFolderPath+"/debateStance"+"/id2homogenizedOutcome.txt");
//		subtarget2id2Outcome_word_ngram= new HashMap<String,Map<String, Integer>>();
//		for (String target : subTargets) {
//		System.out.println(id2outcomeSubTargetFolderPath+"/"+target);
//		subtarget2id2Outcome_word_ngram.put(target, getId2OutcomeMap(id2outcomeSubTargetFolderPath+"/"+target+".txt"));
//		}
		return true;
	}
	
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget unit) throws TextClassificationException {
		Set<Feature> featList = new HashSet<Feature>();
		Users users=JCasUtil.selectCovered(jcas, Users.class,unit).iterator().next();
		String author = users.getAuthor();
//		System.out.println("examine "+unit.getCoveredText());
		try {
			featList.add(new Feature("STANCE_of_AUTHOR_BEFORE", outcomeOfRecurredUser(author, jcas, unit.getAddress())));
		} catch (Exception e) {
			throw new TextClassificationException(e);
		}
		return featList;
	}

	
	/**
	 * check whether the author already commented before and returns the stance polarity
	 * TODO use counts (#of previous occurrences) instead of boolean?
	 * @param author
	 * @param jcas
	 * @param unitAdress
	 * @return
	 * @throws Exception 
	 */
	private int outcomeOfRecurredUser(String author, JCas jcas, int unitAdress) throws Exception {
		
//		//TODO this only checks the first stance of an author in an document; we should do a mojority ?
//		for(TextClassificationTarget unit: JCasUtil.select(jcas, TextClassificationTarget.class)){
//			//break if we reach the same unit (indicated by address) 
//			if(unit.getAddress()==unitAdress){
//				break;
//			}
//			
//			if(JCasUtil.selectCovered(Users.class,unit).iterator().next().getAuthor().equals(author)){
////				System.out.println("found recurrent user in "+unit.getAddress()+" "+unit.getCoveredText());
//				return getClassificationOutcome(unit,jcas);
//			}
//		}
//		//TODO we should distinguish between not occured and NONE
//		return resolvePolarity("NONE");
		int result=0;
		for (TextClassificationTarget unit : JCasUtil.select(jcas, TextClassificationTarget.class)) {
			// break if we reach the same unit (indicated by address)
			if (unit.getAddress() == unitAdress) {
				return result;
			}

			if (JCasUtil.selectCovered(Users.class, unit).iterator().next().getAuthor().equals(author)) {
				//get last previously occured stance
				result=getClassificationOutcome(unit, jcas);
			}
		}
		//TODO should we distinguish between not occured and NONE?
		return result;
	}

	private int getClassificationOutcome(TextClassificationTarget unit, JCas jcas) throws Exception {
		if(useOracle){
			return Id2OutcomeUtil.resolvePolarity(JCasUtil.selectCovered(jcas, curated.Debate_Stance.class,unit).get(0).getPolarity());
		}else{
			String id2OutcomeKey=JCasUtil.selectSingle(jcas, JCasId.class).getId()+"_"+unit.getId();
			if(!debate_id2Outcome.containsKey(id2OutcomeKey)){
				throw new Exception(id2OutcomeKey+" not in id2OutcomeMap");
//				System.err.println(id2OutcomeKey+" not in id2OutcomeMap");
//				return 0;
//				throw new Exception(id2OutcomeKey+" not in id2OutcomeMap");
			}
			return debate_id2Outcome.get(id2OutcomeKey);
		}
	}
	
}
