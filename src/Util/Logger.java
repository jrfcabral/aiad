package Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	private static File logFile = new File("logFile.txt");
	private static File statisticsFile = new File("statFile.txt");

	public synchronized static void writeToLog(String toLog) {
		FileOutputStream logStream;
		try {
			logStream = new FileOutputStream(logFile, true);
		} catch (FileNotFoundException e1) {
			System.err.println("LOGGER: Could not log");
			return;
		}

		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date(System.currentTimeMillis()));
		try {
			logStream.write((timeStamp + " - " + toLog + "\n").getBytes());
		} catch (IOException e) {
			System.err.println("LOGGER: Could not write to log.");
		}
	}
	
	public synchronized static void writeAndPrint(String toLog){
		System.out.println(toLog);
		writeToLog(toLog);
	}
	
	public synchronized static void writeToStat(String toLog) {
		FileOutputStream logStream;
		try {
			logStream = new FileOutputStream(statisticsFile, true);
		} catch (FileNotFoundException e1) {
			System.err.println("LOGGER: Could not log");
			return;
		}

		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date(System.currentTimeMillis()));
		try {
			logStream.write((timeStamp + " - " + toLog + "\n").getBytes());
		} catch (IOException e) {
			System.err.println("LOGGER: Could not write to log.");
		}
	}
}