package de.uni_due.ltl.ensemble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.uni_due.ltl.util.Id2OutcomeUtil;
import de.uni_due.ltl.util.TargetSets;

public class SVM_or_LSTM_target extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{

	Map<String,Boolean> set1Occurrences= new HashMap<String,Boolean>();
	Map<String,Boolean> set2Occurrences= new HashMap<String,Boolean>();
	
	public static ArrayList<String> SVM_type = new ArrayList<String>(Arrays.asList(
			"Death Penalty (Debate)",
			"Death Penalty should be enforced more quickly e.g. by minimizing the number of appeals",
			"In certain cases, capital punishment shouldn’t have to be humane but more harsh",
			"The level of certainty that is necessary for Death Penalty is unachievable",
			"Execution helps alleviate the overcrowding of prisons.",
			"It helps the victims’ families achieve closure.",	
			"State-sanctioned killing is wrong (state has not the right).",	
			"The death penalty can produce irreversible miscarriages of justice.",	
			"The death penalty deters crime.",	
			"The death penalty is a financial burden on the state.",
			"Wrongful convictions are irreversible.",
			"Death Penalty should be done by the electric chair",
			"If Death Penalty is allowed, abortion should be legal, too.",
			"Life-long prison should be replaced by Death Penalty"
			));
	
	public static ArrayList<String> LSTM_type = new ArrayList<String>(Arrays.asList(
			"Execution prevents the accused from committing further crimes.",
			"The death penalty should apply as punishment for first-degree murder; an eye for an eye."
			,"Bodies of people sentenced to death should be used to repay society (e.g. medical experiments, organ donation)",	//*
			"Death Penalty for heinous crimes (murder, mass murder, rape, child molestation etc.)"
			,"Death Penalty should be done by gunshot",	
			"Death Penalty should be done by hypoxia",
			"Death Penalty should be done by the electric chair",
			"If Death Penalty is allowed, abortion should be legal, too."	
			));
	
	
	@Override
	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		Set<Feature> featList = new HashSet<Feature>();
		try {
			for (String target1 : TargetSets.targets_Set1) {
				set1Occurrences.put(target1, getOraclePolarity(target, view, target1, true));
			}
			for (String target2 : TargetSets.targets_Set2) {
				set2Occurrences.put(target2, getOraclePolarity(target, view, target2, false));
			}
		}catch (Exception e){
			throw new TextClassificationException(e);
		}
		
		
		boolean svm_type=isSVM_Type();
		boolean lstm_type=isLSTM_Type();
		if(svm_type){
			featList.add(new Feature("svm_Type", 1));
		}else{
			featList.add(new Feature("svm_Type", 0));
		}
		if(lstm_type){
			featList.add(new Feature("lstm_Type", 1));
		}else{
			featList.add(new Feature("lstm_Type", 0));
		}
		
		return featList;
	}

	private boolean isLSTM_Type() {
		boolean lstm_type=false;
		for(String lstmTarget: LSTM_type){
			for(String t: set1Occurrences.keySet()){
				if(lstmTarget.equals(t) &&set1Occurrences.get(t)){
					return true;
				}
			}
			for(String t: set2Occurrences.keySet()){
				if(lstmTarget.equals(t) &&set2Occurrences.get(t)){
					return true;
				}
			}
		}
		return lstm_type;
	}

	private boolean isSVM_Type() {
		boolean svm_type=false;
		for(String svmTarget: SVM_type){
			for(String t: set1Occurrences.keySet()){
				if(svmTarget.equals(t) &&set1Occurrences.get(t)){
					return true;
				}
			}
			for(String t: set2Occurrences.keySet()){
				if(svmTarget.equals(t) &&set2Occurrences.get(t)){
					return true;
				}
			}
		}
		return svm_type;
	}

	private boolean getOraclePolarity(TextClassificationTarget unit, JCas jcas, String targetLabel, boolean targetSet1) throws Exception {
		int polarity=0;
		if(targetSet1){
			polarity= getOraclePolaritySet1(unit,jcas,targetLabel);
		}else{
			polarity= getOraclePolaritySet2(unit,jcas,targetLabel);
		}
		if(polarity==0){
			return false;
		}else{
			return true;
		}
		
		
	}
	
	/**
	 * get oracle polarity for target set1
	 * @param unit
	 * @param jcas
	 * @param targetLabel
	 * @return
	 * @throws Exception
	 */
	private int getOraclePolaritySet1(TextClassificationTarget unit, JCas jcas, String targetLabel) throws Exception {
		for(curated.Explicit_Stance_Set1 subTarget: JCasUtil.selectCovered(jcas, curated.Explicit_Stance_Set1.class,unit)){
			if(targetLabel.equals(subTarget.getTarget())){
				String polarity=subTarget.getPolarity();
				return Id2OutcomeUtil.resolvePolarity(polarity);
			}
		}
		return 0;
	}

	/**
	 * get oracle polarity for target set2
	 * @param unit
	 * @param jcas
	 * @param targetLabel
	 * @return
	 * @throws Exception
	 */
	private int getOraclePolaritySet2(TextClassificationTarget unit, JCas jcas, String targetLabel) throws Exception {
		for(curated.Explicit_Stance_Set2 subTarget: JCasUtil.selectCovered(jcas, curated.Explicit_Stance_Set2.class,unit)){
			if(targetLabel.equals(subTarget.getTarget())){
				String polarity=subTarget.getPolarity();
				return Id2OutcomeUtil.resolvePolarity(polarity);
			}
		}
		return 0;
	}
	
}
