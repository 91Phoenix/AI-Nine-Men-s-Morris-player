package domain;

public class Move
{
	static public final int PLACING = 1;
	static public final int MOVING = 2;
	static public final int REMOVING = 3;
	
	public byte srcIndex, destIndex, removePieceOnIndex;
	public final int typeOfMove;
	public int score;
	
	public Move(byte src, byte dest, byte remove, int type) throws GameException 
	{
		if(type != PLACING && type != MOVING && type != REMOVING) 
		{
			throw new GameException(getClass().getName()+" - Invalid Type Of Move");
		}
		this.srcIndex = src;
		this.destIndex = dest;
		this.removePieceOnIndex = remove;
		this.typeOfMove = type;
	}
}
