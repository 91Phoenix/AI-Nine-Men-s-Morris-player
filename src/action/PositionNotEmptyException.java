package action;

public class PositionNotEmptyException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PositionNotEmptyException(String position) {
		super("Position " + position + " Not Empty");
	}
}
