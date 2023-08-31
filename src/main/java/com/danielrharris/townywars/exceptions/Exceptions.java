package com.danielrharris.townywars.exceptions;

public class Exceptions
{
	public Exceptions() {
		
	}
	
	@SuppressWarnings("serial")
	public static class AlreadyAtWarException extends Exception{
		public AlreadyAtWarException(String errorMessage) {
	        super(errorMessage);
	    }
	}
    
    @SuppressWarnings("serial")
	public static class NotInWarException extends Exception{
		public NotInWarException(String errorMessage) {
	        super(errorMessage);
	    }
	}
    
    @SuppressWarnings("serial")
	public static class ParticipantNotFoundException extends Exception{
		public ParticipantNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
	
	@SuppressWarnings("serial")
	public static class TownNotFoundException extends Exception{
		public TownNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
	
	@SuppressWarnings("serial")
	public static class TownOrNationNotFoundException extends Exception{
		public TownOrNationNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
	
}