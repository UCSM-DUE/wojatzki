package de.uni_due.subdebateInfluence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.uima.resource.ResourceInitializationException;

import de.uni_due.ltl.util.Id2OutcomeUtil;
import de.unidue.ltl.evaluation.EvaluationData;

public class Filtereable_TcId2OutcomeReader {
	public static EvaluationData<String> read(File id2OutcomeFile) throws ResourceInitializationException{
		EvaluationData<String> evaluation= new EvaluationData<>();
		evaluation=registerId2OutcomePairs(evaluation,id2OutcomeFile);
		return evaluation;
	}
	
	public static EvaluationData<String> read_butExclude(File id2OutcomeFile,List<String> exludeIds) throws ResourceInitializationException{
		EvaluationData<String> evaluation= new EvaluationData<>();
		evaluation=registerId2OutcomePairsBUtExclude(evaluation,id2OutcomeFile,exludeIds);
		return evaluation;
	}
	
	private static EvaluationData<String> registerId2OutcomePairsBUtExclude(EvaluationData<String> evaluation,
			File id2OutcomeFile, List<String> exludeIds) throws ResourceInitializationException {
		List<String> labels=null;
		try (BufferedReader br = new BufferedReader(new FileReader(id2OutcomeFile))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if(line.startsWith("#labels")){
					labels=Id2OutcomeUtil.getLabels(line);
				}
				if (!line.startsWith("#")) {
					String prediction = line.split(";")[0];
					String gold = line.split(";")[1];
					String id= prediction.split("=")[0];
					int indexOfOnePredicted=Id2OutcomeUtil.getIndexOfOne(prediction.split("=")[1]);
					int indexOfOneGold=Id2OutcomeUtil.getIndexOfOne(gold);
					String labelPredicted =labels.get(indexOfOnePredicted);
					String labelGold=labels.get(indexOfOneGold);
					if(!exludeIds.contains(id)){
						evaluation.register(labelGold, labelPredicted);
					}
				}
			}
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		return evaluation;
	}

	public static EvaluationData<String> read_only(File id2OutcomeFile,List<String> includeIds) throws ResourceInitializationException{
		EvaluationData<String> evaluation= new EvaluationData<>();
		evaluation=registerOnlyCertainId2OutcomePairs(evaluation,id2OutcomeFile,includeIds);
		return evaluation;
	}
	
	private static EvaluationData<String> registerOnlyCertainId2OutcomePairs(EvaluationData<String> evaluation,
			File id2OutcomeFile, List<String> includeIds) throws ResourceInitializationException {
		List<String> labels=null;
		try (BufferedReader br = new BufferedReader(new FileReader(id2OutcomeFile))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if(line.startsWith("#labels")){
					labels=Id2OutcomeUtil.getLabels(line);
				}
				if (!line.startsWith("#")) {
					String prediction = line.split(";")[0];
					String gold = line.split(";")[1];
					String id= prediction.split("=")[0];
					int indexOfOnePredicted=Id2OutcomeUtil.getIndexOfOne(prediction.split("=")[1]);
					int indexOfOneGold=Id2OutcomeUtil.getIndexOfOne(gold);
					String labelPredicted =labels.get(indexOfOnePredicted);
					String labelGold=labels.get(indexOfOneGold);
					if(includeIds.contains(id)){
						evaluation.register(labelGold, labelPredicted);
					}
				}
			}
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		return evaluation;
	}

	private static EvaluationData<String> registerId2OutcomePairs(EvaluationData<String> evaluation, File id2OutcomeFile) throws ResourceInitializationException {
		List<String> labels=null;
		try (BufferedReader br = new BufferedReader(new FileReader(id2OutcomeFile))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if(line.startsWith("#labels")){
					labels=Id2OutcomeUtil.getLabels(line);
				}
				if (!line.startsWith("#")) {
					String prediction = line.split(";")[0];
					String gold = line.split(";")[1];
					String id= prediction.split("=")[0];
					int indexOfOnePredicted=Id2OutcomeUtil.getIndexOfOne(prediction.split("=")[1]);
					int indexOfOneGold=Id2OutcomeUtil.getIndexOfOne(gold);
					String labelPredicted =labels.get(indexOfOnePredicted);
					String labelGold=labels.get(indexOfOneGold);
					evaluation.register(labelGold, labelPredicted);
				}
			}
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		return evaluation;
	}
}
