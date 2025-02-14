package de.uni_due.ltl.corpusInspection;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.uni_due.ltl.simpleClassifications.FunctionalPartsAnnotator;
import de.uni_due.ltl.simpleClassifications.SentimentCommentAnnotator;
import de.uni_due.ltl.util.Id2OutcomeUtil;
import de.uni_due.ltl.util.TargetSets;
import io.YouTubeReader;
import preprocessing.CommentText;
import preprocessing.CommentType;
import preprocessing.Users;

public class InspectCorpus {

	public static void main(String[] args) throws Exception {
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(YouTubeReader.class, YouTubeReader.PARAM_SOURCE_LOCATION, "/Users/michael/DKPRO_HOME/youtubeStance/corpus_curated/bin_preprocessed", YouTubeReader.PARAM_LANGUAGE,
				"en", YouTubeReader.PARAM_PATTERNS, "*.bin", YouTubeReader.PARAM_TARGET_LABEL,"DEATH PENALTY", YouTubeReader.PARAM_TARGET_SET,"1");
		AnalysisEngine engine= getPreprocessingEngine();
		inspectExplicitTargetOccurrence(reader,engine);
////		
//		for(String target : TargetSets.targets_Set1){
//			inspectExplicitTarget(engine,target,"1");
//		}
//		
//		for(String target : TargetSets.targets_Set2){
//			inspectExplicitTarget(engine,target,"2");
//		}
//		printVocab(reader,engine);
//		inspectOutcomePerDoc(reader,engine);
//		inspectPolarityNoExplicit(reader,engine);
//		inspectAuthorAndRefereesPerPolarity(reader,engine);
//		inspectAuthorAndReferees(reader,engine);
//		inspectUsersAndCommentType(reader,engine);
//		inspectText(reader,engine);
//		inspectOutcome(reader,engine);
//		AnalysisEngine engineSentiment= getSentimentPreprocessingEngine();
//		inspectSentimemts(reader,engineSentiment);
	}

	private static void inspectExplicitTargetOccurrence(CollectionReaderDescription reader, AnalysisEngine engine) {
		FrequencyDistribution<String> favor = new FrequencyDistribution<>();
		FrequencyDistribution<String> against = new FrequencyDistribution<>();
		FrequencyDistribution<String> none = new FrequencyDistribution<>();
		for (JCas jcas : new JCasIterable(reader)) {
			for (TextClassificationOutcome outcome : JCasUtil.select(jcas, TextClassificationOutcome.class)) {
				int numberOfSubdebates = getNumberOfSubdebates(outcome, jcas);
				if (outcome.getOutcome().equals("FAVOR")) {
					favor.inc(String.valueOf(numberOfSubdebates));
				}
				if (outcome.getOutcome().equals("AGAINST")) {
					against.inc(String.valueOf(numberOfSubdebates));
				}
				if (outcome.getOutcome().equals("NONE")) {
					none.inc(String.valueOf(numberOfSubdebates));
				}
			}
		}
		System.out.println("FAVOR");
		printfd(favor);
		System.out.println("AGAINST");
		printfd(against);
		System.out.println("NONE");
		printfd(none);
	}
		

	private static int getNumberOfSubdebates(TextClassificationOutcome outcome, JCas jcas) {
		int count=0;	
		for (curated.Explicit_Stance_Set1 explicitStance : JCasUtil.selectCovered(curated.Explicit_Stance_Set1.class,
				outcome)) {
			if (!explicitStance.getPolarity().equals("NONE")) {
				count++;
			}
		}
		for (curated.Explicit_Stance_Set2 explicitStance : JCasUtil.selectCovered(curated.Explicit_Stance_Set2.class,
				outcome)) {
			if (!explicitStance.getPolarity().equals("NONE")) {
				count++;
			}
		}
		return count;
	}

	private static void inspectPolarityNoExplicit(CollectionReaderDescription reader, AnalysisEngine engine) {
		FrequencyDistribution<String> fdAll= new FrequencyDistribution<>();
		FrequencyDistribution<String> fdinvers= new FrequencyDistribution<>();
		Map<String,FrequencyDistribution<String>> docId2outcome= new HashMap<>();
		for (JCas jcas : new JCasIterable(reader)) {
			DocumentMetaData metaData= DocumentMetaData.get(jcas);
			String id= metaData.getDocumentId();
			for(TextClassificationOutcome outcome: JCasUtil.select(jcas, TextClassificationOutcome.class)){
				boolean explicitStance=false;
				for(String target : TargetSets.targets_Set1){
					if(contains(outcome,target,"1")){
						explicitStance=true;
					}
				}
				
				for(String target : TargetSets.targets_Set2){
					if(contains(outcome,target,"1")){
						explicitStance=true;
					}
				}
				if(explicitStance){
					fdAll.inc(outcome.getOutcome());
				}
				if(!explicitStance){
					fdinvers.inc(outcome.getOutcome());
				}
			}
		}
		//
		System.out.println("All");

		printfd(fdAll);
		printfd(fdinvers);
		
	}

	private static boolean contains(TextClassificationOutcome outcome, String explicitTarget, String set) {
		if(set.equals("1")){
			for(curated.Explicit_Stance_Set1 explicitStance: JCasUtil.selectCovered(curated.Explicit_Stance_Set1.class, outcome)){
				if(explicitStance.getTarget().equals(explicitTarget) && !explicitStance.getPolarity().equals("NONE")){
					return true;
				}
			}
		}
		if(set.equals("1")){
			for (curated.Explicit_Stance_Set2 explicitStance : JCasUtil.selectCovered(curated.Explicit_Stance_Set2.class, outcome)) {
				if (explicitStance.getTarget().equals(explicitTarget)&& !explicitStance.getPolarity().equals("NONE")) {
					return true;
				}
			}
		}
		
		
		return false;
	}

	private static void printVocab(CollectionReaderDescription reader, AnalysisEngine engine) throws AnalysisEngineProcessException, IOException {
		FrequencyDistribution<String> words= new FrequencyDistribution<>();
		for (JCas jcas : new JCasIterable(reader)) {
			engine.process(jcas);
			Collection<CommentText> comments=JCasUtil.select(jcas,CommentText.class);
			for (CommentText comment : comments) {
				for(Token t: JCasUtil.selectCovered(Token.class, comment)){
					words.inc(t.getCoveredText().toLowerCase());
				}
			}
		}
		int i=0;
		for(String word: words.getMostFrequentSamples(words.getKeys().size())){
//			System.out.println(word +"\t"+words.getCount(word));
			i++;
			FileUtils.write(new File("src/main/resources/list/vocab"), word+System.lineSeparator(), true);
		}
		System.out.println(i);
	}

	private static void inspectSentimemts(CollectionReaderDescription reader, AnalysisEngine engineSentiment) throws AnalysisEngineProcessException {
		for (JCas jcas : new JCasIterable(reader)) {
			engineSentiment.process(jcas);
			for(TextClassificationOutcome outcome: JCasUtil.select(jcas, TextClassificationOutcome.class)){
//				System.out.println(outcome.getCoveredText()+ " "+outcome.getOutcome());
				System.out.println(JCasUtil.selectCovered(CommentText.class,outcome).iterator().next().getCoveredText());
				for(StanfordSentimentAnnotation sentiment:JCasUtil.selectCovered(StanfordSentimentAnnotation.class,outcome)){
					System.out.println("++ "+sentiment.getVeryPositive()+"\t"+sentiment.getPositive()+"\t"+sentiment.getNeutral()+"\t"+sentiment.getNegative()+"\t"+sentiment.getVeryNegative()+" --");
				}
			}
		}
	}

	private static void inspectExplicitTarget(AnalysisEngine engine, String target, String set) throws Exception {
		System.out.println(target);
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(YouTubeReader.class, YouTubeReader.PARAM_SOURCE_LOCATION, "/Users/michael/DKPRO_HOME/youtubeStance/corpus_curated/bin_preprocessed", YouTubeReader.PARAM_LANGUAGE,
				"en", YouTubeReader.PARAM_PATTERNS, "*.bin", YouTubeReader.PARAM_TARGET_LABEL,target, YouTubeReader.PARAM_TARGET_SET,set);
		
		int i=0;
		int j=0;
		int count =0;
		for (JCas jcas : new JCasIterable(reader)) {
			DocumentMetaData metaData = DocumentMetaData.get(jcas);
			String id = metaData.getDocumentId();
			
			for (TextClassificationOutcome outcome : JCasUtil.select(jcas, TextClassificationOutcome.class)) {
//				int id2OutcomeKey=JCasUtil.selectSingle(jcas, JCasId.class).getId();
				if(!outcome.getOutcome().equals("NONE")){
					count++;
					int polarityInt=Id2OutcomeUtil.resolvePolarity(outcome.getOutcome());
					System.out.println(i+"_"+j+":"+polarityInt);
					FileUtils.write(new File("src/main/resources/explicitStancesLocators/"+set+"/"+target+".txt"), String.valueOf(i)+"_"+String.valueOf(j)+":"+polarityInt+"\n", true);
				}
				j++;
			}
			i++;
		}
		System.out.println(count);
	}

	private static void inspectOutcomePerDoc(CollectionReaderDescription reader, AnalysisEngine engine) {
		FrequencyDistribution<String> fdAll= new FrequencyDistribution<>();
		Map<String,FrequencyDistribution<String>> docId2outcome= new HashMap<>();
		for (JCas jcas : new JCasIterable(reader)) {
			DocumentMetaData metaData= DocumentMetaData.get(jcas);
			String id= metaData.getDocumentId();
			for(TextClassificationOutcome outcome: JCasUtil.select(jcas, TextClassificationOutcome.class)){
				System.out.println(outcome.getCoveredText()+ " "+outcome.getOutcome());
				fdAll.inc(outcome.getOutcome());
				if(docId2outcome.containsKey(id)){
					docId2outcome.get(id).inc(outcome.getOutcome());
				}else{
					FrequencyDistribution<String> fd= new FrequencyDistribution<>();
					fd.inc(outcome.getOutcome());
					docId2outcome.put(id, fd);
				}
			}
		}
		for(String id:docId2outcome.keySet()){
			System.out.println(id);
			printfd(docId2outcome.get(id));
		}
		//
		System.out.println("All");
		printfd(fdAll);
	}

	private static void inspectOutcome(CollectionReaderDescription reader, AnalysisEngine engine) {
		for (JCas jcas : new JCasIterable(reader)) {
			for(TextClassificationOutcome outcome: JCasUtil.select(jcas, TextClassificationOutcome.class)){
				System.out.println(outcome.getCoveredText()+ " "+outcome.getOutcome());
				List<String> tokens=getList(JCasUtil.selectCovered(jcas,Token.class,outcome));
				System.out.println(StringUtils.join(tokens, " - "));
			}
		}
		
	}

	private static void inspectText(CollectionReaderDescription reader, AnalysisEngine engine) throws AnalysisEngineProcessException {
		int numOfToken=0;
		for (JCas jcas : new JCasIterable(reader)) {
			engine.process(jcas);
			numOfToken+=JCasUtil.select(jcas, Token.class).size();
			for(TextClassificationOutcome outcome: JCasUtil.select(jcas, TextClassificationOutcome.class)){
//				System.out.println(outcome.getCoveredText()+ " "+outcome.getOutcome());
//				System.out.println(JCasUtil.selectCovered(CommentText.class,outcome).iterator().next().getCoveredText());
			}
		}
		System.out.println(numOfToken);
		
	}

	private static void inspectUsersAndCommentType(CollectionReaderDescription reader, AnalysisEngine engine) throws AnalysisEngineProcessException {
		int comment=0;
		int reply=0;
		for (JCas jcas : new JCasIterable(reader)) {
			engine.process(jcas);
			for(TextClassificationOutcome outcome: JCasUtil.select(jcas, TextClassificationOutcome.class)){
				System.out.println(outcome.getCoveredText()+ " "+outcome.getOutcome());
				System.out.println(JCasUtil.selectCovered(Users.class,outcome).iterator().next().getAuthor()+" "+JCasUtil.selectCovered(Users.class,outcome).iterator().next().getReferee());
				System.out.println(JCasUtil.selectCovered(CommentType.class,outcome).iterator().next().getCommentNotReply());
				if(JCasUtil.selectCovered(CommentType.class,outcome).iterator().next().getCommentNotReply()){
					comment++;
				}else{
					reply++;
				}
			}
		}
		System.out.println(comment);
		System.out.println(reply);
		
	}

	private static void inspectAuthorAndReferees(CollectionReaderDescription reader, AnalysisEngine engine) throws AnalysisEngineProcessException {
		/**
		 * users & refereees
		 */
		FrequencyDistribution<String> fd_author= new FrequencyDistribution<>();
		FrequencyDistribution<String> fd_referee= new FrequencyDistribution<>();
		for (JCas jcas : new JCasIterable(reader)) {
			engine.process(jcas);
			for(TextClassificationOutcome outcome: JCasUtil.select(jcas, TextClassificationOutcome.class)){
				fd_author.inc(JCasUtil.selectCovered(Users.class,outcome).iterator().next().getAuthor());
				fd_referee.inc(JCasUtil.selectCovered(Users.class,outcome).iterator().next().getReferee());
			}
		}
		System.out.println("# of authors "+(fd_author.getN()-fd_author.getCount("None")));
		System.out.println("# of referees "+(fd_referee.getN()-fd_referee.getCount("None")));
//		printfd(fd_author);
//		printfd(fd_referee);
		
	}

	private static void inspectAuthorAndRefereesPerPolarity(CollectionReaderDescription reader, AnalysisEngine engine) throws AnalysisEngineProcessException {
		/**
		 * users & refereees
		 */
		FrequencyDistribution<String> fd_author_FAVOR= new FrequencyDistribution<>();
		FrequencyDistribution<String> fd_referee_FAVOR= new FrequencyDistribution<>();
		FrequencyDistribution<String> fd_author_AGAINST= new FrequencyDistribution<>();
		FrequencyDistribution<String> fd_referee_AGAINST= new FrequencyDistribution<>();
		FrequencyDistribution<String> fd_author_NONE= new FrequencyDistribution<>();
		FrequencyDistribution<String> fd_referee_NONE= new FrequencyDistribution<>();
		for (JCas jcas : new JCasIterable(reader)) {
			engine.process(jcas);
			for(TextClassificationOutcome outcome: JCasUtil.select(jcas, TextClassificationOutcome.class)){
				if(outcome.getOutcome().equals("FAVOR")){
					fd_author_FAVOR.inc(JCasUtil.selectCovered(Users.class,outcome).iterator().next().getAuthor());
					fd_referee_FAVOR.inc(JCasUtil.selectCovered(Users.class,outcome).iterator().next().getReferee());
					}
				if(outcome.getOutcome().equals("AGAINST")){
					fd_author_AGAINST.inc(JCasUtil.selectCovered(Users.class,outcome).iterator().next().getAuthor());
					fd_referee_AGAINST.inc(JCasUtil.selectCovered(Users.class,outcome).iterator().next().getReferee());
					}
				if(outcome.getOutcome().equals("NONE")){
					fd_author_NONE.inc(JCasUtil.selectCovered(Users.class,outcome).iterator().next().getAuthor());
					fd_referee_NONE.inc(JCasUtil.selectCovered(Users.class,outcome).iterator().next().getReferee());
					}
				}
			}
		System.out.println("--- FAVOR ---");
		System.out.println(fd_author_FAVOR.getN()-fd_author_FAVOR.getCount("Unknown"));
		System.out.println(fd_referee_FAVOR.getN()-fd_referee_FAVOR.getCount("None"));
		System.out.println(((double)fd_referee_FAVOR.getN()-fd_referee_FAVOR.getCount("None"))/821);
		System.out.println("--- AGAINST ---");
		System.out.println(fd_author_AGAINST.getN()-fd_author_AGAINST.getCount("Unknown"));
		System.out.println(fd_referee_AGAINST.getN()-fd_referee_AGAINST.getCount("None"));
		System.out.println(((double)fd_referee_AGAINST.getN()-fd_referee_AGAINST.getCount("None"))/821);
		System.out.println("--- NONE ---");
		System.out.println(fd_author_NONE.getN()-fd_author_NONE.getCount("Unknown"));
		System.out.println(fd_referee_NONE.getN()-fd_referee_NONE.getCount("None"));
		System.out.println(((double)fd_referee_NONE.getN()-fd_referee_NONE.getCount("None"))/821);
		
	}

	private static void printfd(FrequencyDistribution<String> fd) {
		for(String sample: fd.getMostFrequentSamples(fd.getKeys().size())){
			System.out.println(sample+" "+fd.getCount(sample));
		}
		
	}

	private static List<String> getList(List<Token> selectCovered) {
		List<String> result= new ArrayList<>();
		for(Token token: selectCovered){
			result.add(token.getCoveredText());
		}
		return result;
	}
	
	private static AnalysisEngine getPreprocessingEngine() {
		AggregateBuilder builder = new AggregateBuilder();
		AnalysisEngine engine = null;
		try {
			builder.add(createEngineDescription(
					createEngineDescription(FunctionalPartsAnnotator.class)
					));
			engine = builder.createAggregate();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}
		return engine;
	}

	
	private static AnalysisEngine getSentimentPreprocessingEngine() {
		AggregateBuilder builder = new AggregateBuilder();
		AnalysisEngine engine = null;
		try {
			builder.add(createEngineDescription(
					createEngineDescription(FunctionalPartsAnnotator.class),
					createEngineDescription(SentimentCommentAnnotator.class)
					));
			engine = builder.createAggregate();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}
		return engine;
	}

}
