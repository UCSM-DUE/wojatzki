package stanceClassificationTaskA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.tc.api.exception.TextClassificationException;
import util.ComputeSemevalMeasure;

public class StackAblation {

	public static void main(String[] args) throws FileNotFoundException, IOException, NumberFormatException, TextClassificationException {
		ArrayList<String> targets = new ArrayList<String>(
			    Arrays.asList("Atheism","FeministMovement", "ClimateChangeisaRealConcern","HillaryClinton", "LegalizationofAbortion"));
//		 String target="FeministMovement";
//		String target = "ClimateChangeisaRealConcern";
		// String target="LegalizationofAbortion";
		// String target="HillaryClinton";
//		 String target="Atheism";
		
		for(String target: targets){
			
			Map<String,String> feSet2Id2OutcomeFavAgainst=getIdToOutcome(target, true);
//			for(String key: feSet2Id2OutcomeFavAgainst.keySet()){
//				System.out.println(key+ " "+ feSet2Id2OutcomeFavAgainst.get(key));
//			}
			Map<String,String> feSet2Id2OutcomeStanceNone=getIdToOutcome(target, false);
//			for(String key: feSet2Id2OutcomeStanceNone.keySet()){
//				System.out.println(key+ " "+ feSet2Id2OutcomeStanceNone.get(key));
//			}
			
			for(String key: feSet2Id2OutcomeFavAgainst.keySet()){
				for(String key2: feSet2Id2OutcomeStanceNone.keySet()){
					stack(key,key2,feSet2Id2OutcomeFavAgainst.get(key),feSet2Id2OutcomeStanceNone.get(key2),target);
				}
			}
		}

	}

	private static void stack(String FavAgainstKey, String StanceNoneKey, String Id2OutcomeFavAgainst, String Id2OutcomeStanceNone, String target) throws NumberFormatException, UnsupportedEncodingException, IOException, TextClassificationException {
		Map<String, String> correct = readGold("src/main/resources/evaluation/" + target + "/gold/id2homogenizedOutcome.txt");
		Map<String, String> stanceVsNone = readPrediction(Id2OutcomeStanceNone);

		Map<String, String> favorVsAgainst = readPrediction(Id2OutcomeFavAgainst);
		Map<String, String> merged = merge(stanceVsNone, favorVsAgainst);

		File tempId2Outcome = createTempFile(merged, correct);
		System.err.println(target+ ": favor vs against set: "+FavAgainstKey+" stance vs none set: "+  StanceNoneKey);
		double semEvalMeasure=ComputeSemevalMeasure.getSemevalMeasure(tempId2Outcome);
		printFile(target,FavAgainstKey,StanceNoneKey,semEvalMeasure);
	}

	private static void printFile(String target, String favAgainstKey, String stanceNoneKey, double semEvalMeasure) {
		String resultLine=target+ "\t"+ favAgainstKey +"\t"+ stanceNoneKey+"\t"+ String.valueOf(semEvalMeasure);
		try (PrintWriter pw = new PrintWriter(new PrintWriter(new FileOutputStream(new File("src/main/resources/evaluation/ablationresult.txt"), true)))) {
			pw.write(resultLine + "" + System.lineSeparator());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	private static Map<String, String> getIdToOutcome(String target, boolean favVsAgainst) throws FileNotFoundException, IOException {
		String classificationStage="";
		if(favVsAgainst)classificationStage="favorVsAgainst";
		else classificationStage="stanceVsNone";
		
		Map<String, String> feSet2Id2Outcome= new HashMap<String, String>();
		File folder= new File("/Users/michael/DKPRO_HOME/semevalTask6/ablation/"+target+"/"+classificationStage);
		for(File cvFoler: folder.listFiles()){
			if(!cvFoler.isDirectory())continue;
			String id2Outcome=cvFoler.getAbsolutePath()+"/id2homogenizedOutcome.txt";
			File discriminatorsFile= new File(cvFoler.getAbsolutePath()+"/DISCRIMINATORS.txt");
			PropertiesAdapter adapter=new PropertiesAdapter();
			adapter.read(new FileInputStream(discriminatorsFile));
			String feSet= adapter.getMap().get("org.dkpro.tc.core.task.InitTask|featureSet");
			feSet2Id2Outcome.put(feSet, id2Outcome);
		}
		
		return feSet2Id2Outcome;
	}

	private static Map<String, String> readGold(String path)
			throws NumberFormatException, UnsupportedEncodingException, IOException {
		HashMap<String, String> gold = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = "";
		List<String> labelList = null;
		while ((line = br.readLine()) != null) {
			// this needs to happen at the beginning of the loop
			if (line.startsWith("#labels")) {
				labelList = getLabels(line);
			} else if (!line.startsWith("#")) {
				if (labelList == null) {
					br.close();
					throw new IOException("Wrong file format.");
				}
				// line might contain several '=', split at the last one
				int idxMostRightHandEqual = line.lastIndexOf("=");
				String evaluationData = line.substring(idxMostRightHandEqual + 1);
				String id = line.split("=")[0];
				String[] splittedEvaluationData = evaluationData.split(";");
				String[] predictionS = splittedEvaluationData[0].split(",");
				String[] goldS = splittedEvaluationData[1].split(",");

				for (int i = 0; i < predictionS.length; i++) {
					if (goldS[i].equals("1")) {
						gold.put(id, labelList.get(i));
						// System.out.println("gold " + id + " " +
						// labelList.get(i));
					}
				}
			}
		}
		br.close();

		return gold;
	}
	
	public static List<String> getLabels(String line) throws UnsupportedEncodingException {
		String[] numberedClasses = line.split(" ");
		List<String> labels = new ArrayList<String>();

		// filter #labels out and collect labels
		for (int i = 1; i < numberedClasses.length; i++) {
			// split one more time and take just the part with class name
			// e.g. 1=NPg, so take just right site
			String className = numberedClasses[i].split("=")[1];
			labels.add(URLDecoder.decode(className, "UTF-8"));
		}
		return labels;
	}
	private static Map<String, String> readPrediction(String path)
			throws NumberFormatException, UnsupportedEncodingException, IOException {
		HashMap<String, String> prediction = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = "";
		List<String> labelList = null;
		while ((line = br.readLine()) != null) {
			// this needs to happen at the beginning of the loop
			if (line.startsWith("#labels")) {
				labelList = getLabels(line);
			} else if (!line.startsWith("#")) {
				if (labelList == null) {
					br.close();
					throw new IOException("Wrong file format.");
				}
				// line might contain several '=', split at the last one
				int idxMostRightHandEqual = line.lastIndexOf("=");
				String evaluationData = line.substring(idxMostRightHandEqual + 1);
				String id = line.split("=")[0];
				String[] splittedEvaluationData = evaluationData.split(";");
				String[] predictionS = splittedEvaluationData[0].split(",");

				for (int i = 0; i < predictionS.length; i++) {
					if (predictionS[i].equals("1")) {
						// System.out.println("prediction " + id + " " +
						// labelList.get(i));
						prediction.put(id, labelList.get(i));
					}
				}
			}
		}
		br.close();

		return prediction;
	}
	
	private static File createTempFile(Map<String, String> merged, Map<String, String> correct) {
		File temp = null;
		try {
			temp = File.createTempFile("src/main/resources/evaluation/tempId2Outcome", ".tmp");
			BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
			bw.write("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD");
			bw.newLine();
			bw.write("#labels 0=AGAINST 1=FAVOR 2=NONE");
			bw.newLine();
			bw.write("#Thu Nov 26 XX:XX:XX CET 2015");
			bw.newLine();

			for (String gold : correct.keySet()) {
				String toWrite = gold + "=" + toVector(getPredictionById(gold, merged)) + toVector(correct.get(gold))
						+ "-1.0";
				bw.write(toWrite);
				bw.newLine();
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return temp;
	}
	
	private static String toVector(String lable) {
		if (lable.equals("AGAINST"))
			return "1,0,0;";
		else if (lable.equals("FAVOR"))
			return "0,1,0;";
		else if (lable.equals("NONE"))
			return "0,0,1;";
		System.err.println("UNKNOWN LABLE " + lable);
		return lable;
	}

	private static String getPredictionById(String gold, Map<String, String> merged) {
		for (String key : merged.keySet()) {
			if (gold.equals(key))
				return merged.get(key);
		}
		System.err.println(gold + "not found in merged");
		return null;
	}
	private static Map<String, String> merge(Map<String, String> stanceVsNone, Map<String, String> favorVsAgainst) {
		HashMap<String, String> merged = new HashMap<String, String>();
		for (String svn : stanceVsNone.keySet()) {
			if (stanceVsNone.get(svn).equals("NONE")) {
				merged.put(svn, "NONE");
			} else {
				boolean foundInfavorVsAgainst = false;
				for (String fvn : favorVsAgainst.keySet()) {
					if (fvn.equals(svn)) {
						merged.put(fvn, favorVsAgainst.get(fvn));
						foundInfavorVsAgainst = true;
					}
				}
				if (!foundInfavorVsAgainst) {
					System.err.println("UNDECIDED");
				}
			}
		}
		// System.out.println("merged");
		// for(String key: merged.keySet()){
		// System.out.println(key+" "+merged.get(key));
		// }
		return merged;
	}
}
