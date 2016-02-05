package com.danielrharris.townywars;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;



public class KillRecord {
	
	// set up the date conversion spec and the character set for file writing
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd zzz HH:mm:ss");
	private static final Charset utf8 = StandardCharsets.UTF_8;
	
	private static final String deathsFile="deaths.txt";
	
    // takes in information about the death that just happened and writes it to a file
	public int writeKillRecord(long deathTime, String playerName, String killerName, String damageCause, String deathMessage){
	
		// convert the time in milliseconds to a date and then convert it to a string in a useful format (have to tack on the milliseconds)
		// format example: 2014-08-29 EDT 10:05:25:756
		Date deathDate = new Date(deathTime);
	    String deathDateString = format.format(deathDate)+":"+deathTime%1000;
		
	    // prepare the death record string that will be written to file
		List<String> deathRecord = Arrays.asList(deathDateString+": "+playerName+" died to "+killerName+" via "+damageCause+"; '"+deathMessage+"'");
		
		
		// append the death record to the specified file
		try {	
				Files.write(Paths.get(deathsFile), deathRecord, utf8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
		// some kind of error occurred . . . .
		catch (IOException e) {
			e.printStackTrace();
			return 1;
		}
		// all good!
		return 0;
	}
	
}