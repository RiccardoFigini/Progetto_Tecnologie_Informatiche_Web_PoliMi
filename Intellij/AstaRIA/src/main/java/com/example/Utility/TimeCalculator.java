package com.example.Utility;

import java.sql.Timestamp;
import java.time.Duration;

public class TimeCalculator
{

	/** Calcola il tempo rimanente partendo dalla data Start fino alla data End
	 *  restituendo una stringa indicante i giorni, le ore e i minuti di differenza
	 *  Se la data End viene prima di Start allora restituisce Time elapsed*/
	public static String calculateRemainingTime(Timestamp start, Timestamp end)
	{

		Duration duration = Duration.between(start.toInstant(), end.toInstant());
	    long seconds = duration.getSeconds();

		if(seconds>0)
		{
			long days = seconds / (24 * 60 * 60);
			seconds -= days * (24 * 60 * 60);
			long hours = seconds / (60 * 60);
			seconds -= hours * (60 * 60);
			long minutes = seconds / 60;

			/** Se si vogliono vedere anche i secondi di differenza togli i commenti*/
			/*
			seconds -= minutes * 60;
			 */
			return days + " days, " + hours + " hours, " + minutes + " minutes"/* +  + " seconds"*/;
		}
		else
			return "Time elapsed";
	}
	
}
