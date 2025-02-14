package de.uni.due.ltl.interactiveStance.experimentLogging;

public abstract class LoggingEvent {

	private ExperimentLogging logging;
	
	
	public LoggingEvent(ExperimentLogging logging) {
		this.logging = logging;
	}
	
	public boolean persist(boolean finalEvent){
		if(finalEvent){
			logging.persistExperiment(this);
		}else{
			logging.persistEvent(this);
		}
		
		return true;
	}
	
	protected abstract String eventToString();


}
