package test;

public class AnomalyRangeReport extends AnomalyReport {
	public long endStep;
	public AnomalyRangeReport(String description, long startStep, long endStep){
		super(description, startStep);
		this.endStep=endStep;
	}
}
