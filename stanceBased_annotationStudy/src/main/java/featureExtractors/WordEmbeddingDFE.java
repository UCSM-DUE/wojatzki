package featureExtractors;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class WordEmbeddingDFE extends FeatureExtractorResource_ImplBase implements FeatureExtractor {
	
	public static final String PARAM_WORDEMBEDDINGLOCATION = "embeddingsLocation";
	@ConfigurationParameter(name = PARAM_WORDEMBEDDINGLOCATION, mandatory = true)
	private String embeddingsLocation;
	
	private WordEmbeddingLexicon lexicon;

	
	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
			throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		System.out.println("use embedding "+embeddingsLocation);
			try {
				lexicon = new WordEmbeddingLexicon(embeddingsLocation);
			} catch (Exception e) {
				throw new ResourceInitializationException(e);
			}
		return true;
	}

	private Set<Feature> createFeatures(List<Float> averagedVector) {
		Set<Feature> featList = new HashSet<Feature>();
		for(int i=0; i< averagedVector.size(); i++){
			featList.add(new Feature("embeddingDimension_"+i, averagedVector.get(i)));
		}
		return featList;
	}

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) throws TextClassificationException {
		WordEmbeddingHelper helper=new WordEmbeddingHelper(this.lexicon);
		Set<String> embeddingCandidates= new HashSet<String>();
		
		//TODO filtering for content words?
		for(Token t: JCasUtil.select(jcas, Token.class)){
			String lowerCase = t.getCoveredText().toLowerCase();
			embeddingCandidates.add(lowerCase);
		}
		List<Float> averagedSentenceVector= helper.getAveragedSentenceVector(embeddingCandidates);
		System.out.println(jcas.getDocumentText()+" "+averagedSentenceVector);
		Set<Feature> featList = createFeatures(averagedSentenceVector);
		return featList;
	}

	

}
