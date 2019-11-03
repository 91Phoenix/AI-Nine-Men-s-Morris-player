package domain;

public class Position {
	
	private boolean isOccupied;
	private int positionIndex;
	private State.Checker playerOccupying;
	private byte[] adjacentPositionsIndexes;
	
	public Position(byte position) 
	{
		isOccupied = false;
		this.positionIndex = position;
		playerOccupying = State.Checker.EMPTY;
	}

	public boolean isOccupied() 
	{
		return isOccupied;
	}
	
	public int getPositionIndex() 
	{
		return positionIndex;
	}
	
	public State.Checker getPlayerOccupyingIt() 
	{
		return playerOccupying;
	}

	public void setAsOccupied(State.Checker player)
	{
		isOccupied = true;
		playerOccupying = player;
	}
	
	/**
	 * Rimuove e ritorna il giocatore presente nella posizione
	 * 
	 */
	public State.Checker setAsUnoccupied() 
	{
		isOccupied = false;
		State.Checker oldPlayer = playerOccupying;
		playerOccupying = State.Checker.EMPTY;
		return oldPlayer;
	}
	
	public void addAdjacentPositionsIndexes(byte pos1, byte pos2)
	{
		adjacentPositionsIndexes = new byte[2];
		adjacentPositionsIndexes[0] = pos1;
		adjacentPositionsIndexes[1] = pos2;
	}
	
	public void addAdjacentPositionsIndexes(byte pos1, byte pos2, byte pos3)
	{
		adjacentPositionsIndexes = new byte[3];
		adjacentPositionsIndexes[0] = pos1;
		adjacentPositionsIndexes[1] = pos2;
		adjacentPositionsIndexes[2] = pos3;
	}
	
	public void addAdjacentPositionsIndexes(byte pos1, byte pos2, byte pos3, byte pos4) {
		adjacentPositionsIndexes = new byte[4];
		adjacentPositionsIndexes[0] = pos1;
		adjacentPositionsIndexes[1] = pos2;
		adjacentPositionsIndexes[2] = pos3;
		adjacentPositionsIndexes[3] = pos4;
	}
	
	public byte[] getAdjacentPositionsIndexes()
	{
		return adjacentPositionsIndexes;
	}
	
	public boolean isAdjacentToThis(int posIndex)
	{
		for(int i = 0; i < adjacentPositionsIndexes.length; i++) 
		{
			if(adjacentPositionsIndexes[i]== posIndex) 
			{
				return true;
			}
		}
		return false;
	}
	
}
