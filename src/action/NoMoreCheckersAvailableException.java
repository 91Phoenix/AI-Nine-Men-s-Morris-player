package action;

import domain.State;

public class NoMoreCheckersAvailableException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoMoreCheckersAvailableException(State.Checker checker) {
		super("Checkers of type " + checker.toString() + " are finished!");
	}
}
