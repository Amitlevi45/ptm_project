package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class TimeSeries {
	
	public Vector<String> feature_names = new Vector<String>();
	public Vector<Vector<Float>> feature_values = new Vector<Vector<Float>>();

	public TimeSeries(String csvFileName)
	{
		try {
		BufferedReader csvReader = new BufferedReader(new FileReader(csvFileName));
		String row = csvReader.readLine();

		String[] names = row.split(",");
		AddHeader(names);

		while ((row = csvReader.readLine()) != null)
		{
		    String[] data = row.split(",");
		    AddRow(data);
		}
		
		csvReader.close();
		} catch (IOException e) {
			return;
		}
	}
	
	public TimeSeries() {}
	
	public Vector<String> GetFeatureNames()
	{
		return feature_names;
	}
	
	public float GetFeatureAt(String feature_name, int timestamp)
	{
		return feature_values.get(feature_names.indexOf(feature_name)).elementAt(timestamp);
	}
	
	public Vector<Vector<Float>> GetFeatures()
	{
		return feature_values;
	}
	
	public void AddHeader(String[] row)
	{
		for (int i = 0; i < row.length; i++)
		{
			feature_names.add(row[i]);
			feature_values.add(new Vector<Float>());
		}
	}
	
	public void AddRow(String[] row)
	{
		for (int i = 0; i < row.length; i++)
	    {
	    	feature_values.elementAt(i).add(Float.parseFloat(row[i]));
	    }
	}
	
	public long GetTimeFileLength()
	{
		return feature_values.elementAt(0).size();
	}
}
