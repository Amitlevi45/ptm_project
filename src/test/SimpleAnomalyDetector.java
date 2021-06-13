package test;

import java.util.List;
import java.util.Vector;

class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {
	public Vector<CorrelatedFeatures> features_vector = new Vector<CorrelatedFeatures>();
	public float correlation_threshold = 0.9f;
	public float threshold_multiplier = 1.2f;

	@Override
	public void learnNormal(TimeSeries ts)
	{
		Vector<Vector<Float>> features = ts.GetFeatures();
		for (int i = 0; i < features.size(); i++)
		{
			float max_pearson = 0;
			int max_pearson_index = 0;
			Line max_line_reg = null;
			float max_threshold = 0;
			
			for (int j = i + 1; j < features.size(); j++)
			{
				
				float[] x = new float[features.elementAt(i).size()];
				float[] y = new float[features.elementAt(j).size()];

				for (int k = 0; k < features.elementAt(i).size(); k++)
				{
					x[k] = features.elementAt(i).elementAt(k);
				}
				
				for (int k = 0; k < features.elementAt(j).size(); k++)
				{
					y[k] = features.elementAt(j).elementAt(k);
				}

				float curr_pearson = Math.abs(StatLib.pearson(x,y));
				if (curr_pearson > max_pearson)
				{
					max_threshold = 0;
					max_pearson = curr_pearson;
					max_pearson_index = j;
					Point[] points = new Point[x.length];
					for (int k = 0; k < points.length; k++)
					{
						points[k] = new Point(x[k], y[k]);
					}

					max_line_reg = StatLib.linear_reg(points);
					
					for (int k = 0; k < points.length; k++)
					{
						float curr_threshold = StatLib.dev(points[k], max_line_reg);
						if (curr_threshold > max_threshold)
						{
							max_threshold = curr_threshold;
						}
					}
				}
			}
			
			if (max_pearson >= correlation_threshold)
			{
				CorrelatedFeatures feature = new CorrelatedFeatures(ts.GetFeatureNames().get(i), ts.GetFeatureNames().get(max_pearson_index), max_pearson, max_line_reg, max_threshold * threshold_multiplier);
				features_vector.add(feature);
			}
		}
	}
	
	@Override
	public List<AnomalyReport> detect(TimeSeries ts)
	{
		Vector<AnomalyReport> reports_vector = new Vector<AnomalyReport>();
		for (int cor_index = 0; cor_index < features_vector.size(); cor_index++)
		{
			for (int feature_size = 0; feature_size < ts.GetFeatures().elementAt(0).size(); feature_size++)
			{
				float dev = StatLib.dev(new Point(ts.GetFeatureAt(features_vector.elementAt(cor_index).feature1, feature_size), ts.GetFeatureAt(features_vector.elementAt(cor_index).feature2, feature_size)), features_vector.elementAt(cor_index).lin_reg);
				if (dev > features_vector.elementAt(cor_index).threshold)
				{
					AnomalyReport report = new AnomalyReport(features_vector.elementAt(cor_index).feature1 + "-" + features_vector.elementAt(cor_index).feature2, feature_size + 1);
					reports_vector.add(report);
				}
			}
		}

		return reports_vector;
	}
	
	public List<CorrelatedFeatures> getNormalModel()
	{
		return features_vector;
	}
	
	public float GetCorrelationThreshold()
	{
		return correlation_threshold;
	}
	
	public void SetCorrelationThreshold(float threshold)
	{
		correlation_threshold = threshold;
	}
}
