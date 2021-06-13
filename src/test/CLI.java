package test;

import java.util.ArrayList;

import test.Commands.Command;
import test.Commands.UploadCommand;
import test.Commands.AlgorithmSettingsCommand;
import test.Commands.DetectAnomaliesCommand;
import test.Commands.DisplayResultsCommand;
import test.Commands.UploadAndAnalyzeCommand;
import test.Commands.ExitCommand;
import test.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;
	
	public CLI(DefaultIO dio) {
		this.dio=dio;
		c=new Commands(dio); 
		commands=new ArrayList<>();
		commands.add(c.new UploadCommand());
		commands.add(c.new AlgorithmSettingsCommand());
		commands.add(c.new DetectAnomaliesCommand());
		commands.add(c.new DisplayResultsCommand());
		commands.add(c.new UploadAndAnalyzeCommand());
		commands.add(c.new ExitCommand());
	}
	
	public void start() 
	{
		PrintDescription();
		String input = dio.readText();
		while (true)
		{
			commands.get(Integer.parseInt(input) - 1).execute();
			if (Integer.parseInt(input) == 6)
			{
				break;
			}
			
			PrintDescription();
			input = dio.readText();
		}
	}
	
	public void PrintDescription()
	{
		dio.write("Welcome to the Anomaly Detection Server.\r\n"
				+ "Please choose an option:\r\n");
		for (int i = 0 ; i < commands.size(); i++)
		{	
			dio.write((1 + i) + ". " + commands.get(i).description + "\r\n");
		}
	}
}
