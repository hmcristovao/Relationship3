package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;

public class Log {
	public static BufferedWriter fileCompleteReport;
	public static BufferedWriter fileShortReport;
	public static BufferedWriter fileConsoleReport;
	public static PrintStream printStreamError;

	public static void initFiles() throws Exception {
		String baseDir = WholeSystem.configTable.getString("baseDirectory")+"\\"+WholeSystem.configTable.getString("testName")+"\\";
		fileCompleteReport = new BufferedWriter(new FileWriter(baseDir+WholeSystem.configTable.getString("nameCompleteReportFile")));
		fileShortReport    = new BufferedWriter(new FileWriter(baseDir+WholeSystem.configTable.getString("nameShortReportFile")));
		fileConsoleReport  = new BufferedWriter(new FileWriter(baseDir+WholeSystem.configTable.getString("nameConsoleReportFile")));
		printStreamError   = new PrintStream(baseDir+WholeSystem.configTable.getString("nameConsoleErrorFile"));
		System.setErr(printStreamError);
	}
	public static void close() throws Exception {
		if(fileCompleteReport != null) fileCompleteReport.close();
		if(fileShortReport != null)    fileShortReport.close();
		if(fileConsoleReport != null)  fileConsoleReport.close();
		if(printStreamError != null)   printStreamError.close();
	}
	public static void outFileCompleteReport(String msgSingle) throws Exception {
		fileCompleteReport.write(Constants.doubleLine);
		fileCompleteReport.write(msgSingle);
		fileCompleteReport.write("\n");
		fileCompleteReport.flush();
	}
	public static void outFileShortReport(String msgSingle) throws Exception {
		fileShortReport.write(Constants.doubleLine);
		fileShortReport.write(msgSingle);
		fileShortReport.write("\n");
		fileShortReport.flush();
	}
	private static void print(String str) {
		System.out.print(str);
		try { 
			fileConsoleReport.write(str); 
			fileConsoleReport.flush();
		}
		catch(Exception e) {
			System.out.println("\nERROR in write of console log file");
		}
	}
	private static void println(String str) {
		System.out.println(str);
		try { 
			fileConsoleReport.write(str);
		    fileConsoleReport.write("\n"); 
			fileConsoleReport.flush();
		}
		catch(Exception e) {
			System.out.println("\nERROR in write of console log file");
		}		
	}
	public static void console(String msg, String value) {
		Log.print(Constants.doubleLine);
		Log.println(msg);
		Log.println("<=>");
		Log.println(value);
		Log.print(Constants.singleLine);
	}
	public static void console(boolean bool) {
		if(bool)
			Log.console("Boolean value: true");
		else
			Log.console("Boolean value: false");
	}
	public static void console(String msg, long value) {
		Log.print(Constants.doubleLine);
		Log.println(msg);
		Log.println("<=>");
		Log.println(String.valueOf(value));
		Log.print(Constants.singleLine);
}
	public static void console(String msg) {
		Log.print(msg);
	}
	public static void console(double value) {
		Log.print("\n" + value + "\n");
	}
	public static void console(String msg, double value) {
		Log.print("\n" + msg + ": " + value + "\n");
	}
	public static void consoleln(String msg, String value) {
		Log.console(msg, value);
		Log.print("\n");
	}
	public static void consoleln(boolean bool) {
		Log.console(bool);
		Log.print("\n");
	}
	public static void consoleln(String msg, long value) {
		Log.console(msg, value);
		Log.print("\n");
	}
	public static void consoleln(String msg) {
		Log.console(msg);
		Log.print("\n");
	}
	public static void consoleln(double value) {
		Log.console(value);
		Log.print("\n");
	}
	public static void consoleln(String msg, double value) {
		Log.console(msg, value);
		Log.print("\n");
	}		
}
