package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.reporting.FlexTable;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import org.dkpro.tc.ml.ExperimentCrossValidation;

public class CrossValidationReport  extends BatchReportBase
implements Constants{

	Double f_Favor;
	Double f_Against;
	Double semEval =0.0;
	boolean softEvaluation = true;
	boolean individualLabelMeasures = true;
	

    @Override
    public void execute()
        throws Exception
    {
     	
        StorageService store = getContext().getStorageService();

        FlexTable<String> table = FlexTable.forClass(String.class);

        for (TaskContextMetadata subcontext : getSubtasks()) {
            // FIXME this is a hack
            String name = ExperimentCrossValidation.class.getSimpleName();
            // one CV batch (which internally ran numFolds times)
            if (subcontext.getLabel().startsWith(name)) {
                Map<String, String> discriminatorsMap = store.retrieveBinary(subcontext.getId(), Constants.DISCRIMINATORS_KEY_TEMP, new PropertiesAdapter()).getMap();
                
                File fileToEvaluate = store.locateKey(subcontext.getId(), 
                		Constants.TEST_TASK_OUTPUT_KEY + "/" + Constants.SERIALIZED_ID_OUTCOME_KEY);
                
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileToEvaluate));
                Id2Outcome id2Outcome = (Id2Outcome) inputStream.readObject();
                inputStream.close();
                
                EvaluatorBase evaluator = EvaluatorFactory.createEvaluator(id2Outcome, softEvaluation, individualLabelMeasures);
                Map<String, Double> resultTempMap = evaluator.calculateEvaluationMeasures();
                Map<String, String> resultMap = new HashMap<String, String>();
                for (String key : resultTempMap.keySet()) {
                	Double value = resultTempMap.get(key);
					resultMap.put(key, String.valueOf(value));
					if(key.equals("MacroFScore_FAVOR")){
						f_Favor=value;
					}
					if(key.equals("MacroFScore_AGAINST")){
						f_Against=value;
					}
				}

                if(f_Favor!=null && f_Against!=null){
                	resultMap.put("SemEval_Measure", String.valueOf((f_Favor+f_Against)/2));
                    semEval+=(f_Favor+f_Against)/2;
                }
                Map<String, String> values = new HashMap<String, String>();
                values.putAll(discriminatorsMap);
                values.putAll(resultMap);

                table.addRow(subcontext.getLabel(), values);
            }
        }

        /*
         * TODO: make rows to columns 
         * e.g. create a new table and set columns to rows of old table and rows to columns
         * but than must be class FlexTable in this case adapted accordingly: enable setting
         */
        
        getContext().getLoggingService().message(getContextLabel(),
                ReportUtils.getPerformanceOverview(table));
        // Excel cannot cope with more than 255 columns
        if (table.getColumnIds().length <= 255) {
            getContext()
                    .storeBinary(EVAL_FILE_NAME + "_compact" + SUFFIX_EXCEL, table.getExcelWriter());
        }
        getContext().storeBinary(EVAL_FILE_NAME + "_compact" + SUFFIX_CSV, table.getCsvWriter());
        table.setCompact(false);
        // Excel cannot cope with more than 255 columns
        if (table.getColumnIds().length <= 255) {
            getContext().storeBinary(EVAL_FILE_NAME + SUFFIX_EXCEL, table.getExcelWriter());
        }
        getContext().storeBinary(EVAL_FILE_NAME + SUFFIX_CSV, table.getCsvWriter());

        // output the location of the batch evaluation folder
        // otherwise it might be hard for novice users to locate this
        File dummyFolder = store.locateKey(getContext().getId(), "dummy");
        // TODO can we also do this without creating and deleting the dummy folder?
        getContext().getLoggingService().message(getContextLabel(),
                "Storing detailed results in:\n" + dummyFolder.getParent() + "\n");
        dummyFolder.delete();
        if(f_Favor!=null && f_Against!=null){
        	System.out.println("SemEval Measure: "+semEval/getSubtasks().length);
        }
    }

}
