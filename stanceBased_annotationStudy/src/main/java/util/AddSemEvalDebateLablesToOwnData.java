package util;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTokenizer;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import io.PlainTaskATweetReader;
import io.StanceReader_AddsOriginal;

public class AddSemEvalDebateLablesToOwnData {

	public static void main(String[] args) throws IOException, ResourceInitializationException, UIMAException {
		String baseDir = DkproContext.getContext().getWorkspace().getAbsolutePath();
		AddSemEvalDebateLablesToOwnData pipelineTaskA= new AddSemEvalDebateLablesToOwnData();
		pipelineTaskA.run(baseDir);
	}

	private void run(String baseDir) throws ResourceInitializationException, UIMAException, IOException {
		SimplePipeline.runPipeline(
				CollectionReaderFactory.createReader(
						StanceReader_AddsOriginal.class,
						StanceReader_AddsOriginal.PARAM_SOURCE_LOCATION, baseDir + "/semevalTask6/annotationStudy/curatedTweets/Atheism/all", StanceReader_AddsOriginal.PARAM_LANGUAGE,
						"en", StanceReader_AddsOriginal.PARAM_PATTERNS, "*.bin", StanceReader_AddsOriginal.PARAM_TARGET_LABEL,"ATHEISM")
				,
				AnalysisEngineFactory.createEngineDescription(createEngineDescription(XmiWriter.class,XmiWriter.PARAM_TARGET_LOCATION, baseDir + "/semevalTask6/annotationStudy/originalDebateStanceLabels", XmiWriter.PARAM_OVERWRITE,true),
				AnalysisEngineFactory.createEngineDescription(createEngineDescription(BinaryCasWriter.class,BinaryCasWriter.PARAM_TARGET_LOCATION, baseDir + "/semevalTask6/annotationStudy/originalDebateStanceLabels/bin", BinaryCasWriter.PARAM_OVERWRITE,true)))
		);
	}

}
