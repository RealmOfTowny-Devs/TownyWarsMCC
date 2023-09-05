package com.danielrharris.townywars.exceptions;

public class Exceptions
{
	public Exceptions() {
		
	}
	
	public static class AlreadyAtWarException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2150678381342797430L;

		public AlreadyAtWarException(String errorMessage) {
	        super(errorMessage);
	    }
	}
    
	public static class NotInWarException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6812768699301300557L;

		public NotInWarException(String errorMessage) {
	        super(errorMessage);
	    }
	}
    
	public static class ParticipantNotFoundException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2738155243795630083L;

		public ParticipantNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
	
	public static class TownNotFoundException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8545375055622714752L;

		public TownNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
	
	public static class TownOrNationNotFoundException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8152914972910723849L;

		public TownOrNationNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
	
}