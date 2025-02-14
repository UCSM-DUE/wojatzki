package de.uni.due.ltl.interactiveStance.io;

import java.io.Serializable;

import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.uni.due.ltl.interactiveStance.util.EvaluationScenarioUtil;

public class EvaluationScenario implements Serializable, Cloneable{

	private EvaluationDataSet trainData;
	private EvaluationDataSet testData;
	private String target;
	private String mode;
	
	
	public EvaluationScenario(String target, String experimentalMode, boolean useBinCas) throws Exception {
		
	   if(!EvaluationScenarioUtil.targetIsValid(target)){
		   throw new Exception(target + " is not a valid target");
	   }
	   this.setTarget(target);
	   
	   this.mode=experimentalMode;
		
	   
	   String baseDir = DkproContext.getContext().getWorkspace().getAbsolutePath();
	   System.out.println("DKPRO_HOME: " + baseDir);

	   this.trainData = new EvaluationDataSet( baseDir+"/interactiveStance/trainSet/targets/"+target,useBinCas);
	   this.testData = new EvaluationDataSet( baseDir+"/interactiveStance/testSet/targets/"+target,useBinCas);

	}


	public EvaluationDataSet getTrainData() {
		return trainData;
	}


	public EvaluationDataSet getTestData() {
		return testData;
	}


	public String getTarget() {
		return target;
	}


	public void setTarget(String target) {
		this.target = target;
	}


	public String getMode() {
		return mode;
	}


	public void setMode(String mode) {
		this.mode = mode;
	}

	
}
