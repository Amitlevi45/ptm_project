package test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Commands {
	
	// Default IO interface
	public interface DefaultIO{
		public String readText();
		public void write(String text);
		public float readVal();
		public void write(float val);

		// you may add default methods here
	}
	
	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}
	
	public class AnomalyRangeReport extends AnomalyReport {
		public long endStep;
		public AnomalyRangeReport(String description, long startStep, long endStep){
			super(description, startStep);
			this.endStep=endStep;
		}
	}

	class DeviationReport {
		public long start;
		public long finish;
		public DeviationReport(long start, long finish){
			this.start=start;
			this.finish=finish;
		}
	}

	// the shared state of all commands
	private class SharedState{
		// implement here whatever you need
		TimeSeries trainingFile; 
		TimeSeries testingFile;
		SimpleAnomalyDetector anomalyDetector = new SimpleAnomalyDetector();
		List<AnomalyReport> reportsVector;
	}
	
	private SharedState sharedState = new SharedState();

	
	// Command abstract class
	public abstract class Command{
		protected String description;
		
		public Command(String description) {
			this.description=description;
		}
		
		public abstract void execute();
	}
	
	public class UploadCommand extends Command{

		public UploadCommand() {
			super("upload a time series csv file");
		}

		@Override
		public void execute() {
			dio.write("Please upload your local train CSV file.\r\n");
			sharedState.trainingFile = new TimeSeries();

			String training_line = dio.readText();

			String[] training_names = training_line.split(",");
			sharedState.trainingFile.AddHeader(training_names);

			training_line = dio.readText();
			while (!(training_line.equals("done")))
			{
				String[] values = training_line.split(",");
				sharedState.trainingFile.AddRow(values);
				training_line = dio.readText();
			}
			
			dio.write("Upload complete.\r\n");
			
			dio.write("Please upload your local test CSV file.\r\n");
			sharedState.testingFile = new TimeSeries();
			
			String test_line = dio.readText();
			String[] test_names = test_line.split(",");
			sharedState.testingFile.AddHeader(test_names);

			test_line = dio.readText();
			while (!(test_line.equals("done")))
			{
				String[] values = test_line.split(",");
				sharedState.testingFile.AddRow(values);
				test_line = dio.readText();
			}
			
			dio.write("Upload complete.\r\n");
		}		
	}
	
	public class AlgorithmSettingsCommand extends Command{

		public AlgorithmSettingsCommand() {
			super("algorithm settings");
		}

		@Override
		public void execute() {
			dio.write("The current correlation threshold is "  + sharedState.anomalyDetector.GetCorrelationThreshold() + "\r\n"
					+ "Type a new threshold\r\n");
			float new_correlation = Float.parseFloat(dio.readText());
			while (!(new_correlation > 0 && new_correlation < 1))
			{
				dio.write("please choose a value between 0 and 1.\r\n");
				dio.write("The current correlation threshold is "  + sharedState.anomalyDetector.GetCorrelationThreshold() + "\r\n"
						+ "Type a new threshold\r\n");
				new_correlation = Float.parseFloat(dio.readText());
			}
			
			sharedState.anomalyDetector.SetCorrelationThreshold(new_correlation);
		}		
	}
	
	public class DetectAnomaliesCommand extends Command{

		public DetectAnomaliesCommand() {
			super("detect anomalies");
		}

		@Override
		public void execute() {
			sharedState.anomalyDetector.learnNormal(sharedState.trainingFile);
			sharedState.reportsVector = sharedState.anomalyDetector.detect(sharedState.testingFile);
			dio.write("anomaly detection complete.\r\n");
		}		
	}
	
	public class DisplayResultsCommand extends Command{

		public DisplayResultsCommand() {
			super("display results");
		}

		@Override
		public void execute() {
			for (int i = 0; i < sharedState.reportsVector.size(); i++)
			{
				dio.write(sharedState.reportsVector.get(i).timeStep + "\t" + sharedState.reportsVector.get(i).description + "\r\n");
			}
			
			dio.write("Done.\r\n");
		}		
	}
	
	public class UploadAndAnalyzeCommand extends Command{

		public UploadAndAnalyzeCommand() {
			super("upload anomalies and analyze results");
		}

		@Override
		public void execute() {
			dio.write("Please upload your local anomalies file.\r\n");
			Vector<DeviationReport> deviation_reports = new Vector<DeviationReport>();
			String line = dio.readText();
			while (!(line.equals("done")))
			{
				String[] values = line.split(",");
				deviation_reports.add(new DeviationReport(Long.parseLong((values[0])), Long.parseLong((values[1]))));
				line = dio.readText();
			}
						
			Vector<AnomalyRangeReport> anomaly_reports = new Vector<AnomalyRangeReport>();
			if (sharedState.reportsVector.size() > 1)
			{
				String prev_desc = sharedState.reportsVector.get(0).description;
				long prev_timestamp = sharedState.reportsVector.get(0).timeStep;
				AnomalyRangeReport curr_report = new AnomalyRangeReport(prev_desc, prev_timestamp, prev_timestamp);
				for (int i = 1; i < sharedState.reportsVector.size(); i++)
				{
					String cur_desc = sharedState.reportsVector.get(i).description;
					long curr_timestamp = sharedState.reportsVector.get(i).timeStep;
					if (prev_timestamp + 1 == curr_timestamp && prev_desc.equals(cur_desc))
					{
						curr_report.endStep = curr_report.endStep + 1;
					}
					else
					{
						anomaly_reports.add(curr_report);
						curr_report = new AnomalyRangeReport(cur_desc, curr_timestamp, curr_timestamp);
					}
					
					prev_desc = cur_desc;
					prev_timestamp = curr_timestamp;
				}
				
				anomaly_reports.add(curr_report);
			}
						
			float false_positive = 0;
			float true_positive = 0;

			for (int i = 0; i < anomaly_reports.size(); i++)
			{
				boolean is_overlapping = false;
				for (int j = 0; j < deviation_reports.size(); j++)
				{
					if ((anomaly_reports.elementAt(i).timeStep <= deviation_reports.elementAt(j).finish) && 
						(anomaly_reports.elementAt(i).endStep >= deviation_reports.elementAt(j).start))
					{
						is_overlapping = true;
						break;
					}
				}
				
				if (!is_overlapping)
				{
					false_positive++;
				}
			}
						
			for (int i = 0; i < deviation_reports.size(); i++)
			{
				boolean is_overlapping = false;
				for (int j = 0; j < anomaly_reports.size(); j++)
				{
					if ((anomaly_reports.elementAt(j).timeStep <= deviation_reports.elementAt(i).finish) && 
						(anomaly_reports.elementAt(j).endStep >= deviation_reports.elementAt(i).start))
					{
						is_overlapping = true;
						break;
					}
				}
				
				if (is_overlapping)
				{
					true_positive++;
				}
			}
						
			float positive = deviation_reports.size();
			float negative = sharedState.testingFile.GetTimeFileLength();
			for (int i = 0; i < deviation_reports.size(); i++)
	        {
				negative -= (deviation_reports.get(i).finish - deviation_reports.get(i).start + 1);
	        }
			
			
			float true_positive_rate = true_positive / positive;
			float false_positive_rate = false_positive / negative;
			false_positive_rate = ((float) ((int) (false_positive_rate * 1000))) / 1000;
			true_positive_rate = ((float) ((int) (true_positive_rate * 1000))) / 1000;
			
			dio.write("Upload complete.\r\n"
					+ "True Positive Rate: " + true_positive_rate + "\r\n"
					+ "False Positive Rate: "+ false_positive_rate + "\r\n");
		}		
	}
	
	public class ExitCommand extends Command{

		public ExitCommand() {
			super("exit");
		}

		@Override
		public void execute() {
			dio.write("bye\r\n");
		}		
	}
}
