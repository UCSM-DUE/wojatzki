package featureExtractors;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.DocumentFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import types.NegationAnnotation;

public class SimpleNegationDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor{

	@Override
	public Set<Feature> extract(JCas jcas) throws TextClassificationException {
		Set<Feature> features= new HashSet<Feature>();
		
		int numOfNegation=0;
		for(NegationAnnotation negationCand: JCasUtil.select(jcas, NegationAnnotation.class)){
			if(negationCand.getIsNegation())numOfNegation++;
		}
		features.add(new Feature("NUMBER_OF_NEGATIONS", numOfNegation));
		return features;
	}

}
