package domain;

import domain.State.Checker;

public class Game 
{
	
	static public final byte NUM_PIECES_PER_PLAYER = 9;
	static public final byte PLACING_PHASE = 1;
	static public final byte MOVING_PHASE = 2;
	static public final byte FLYING_PHASE = 3;
	
	static public final byte INVALID_SRC_POS = -1;
	static public final byte UNAVAILABLE_POS = -2;
	static public final byte INVALID_MOVE = -3;
	static public final byte VALID_MOVE = 0;

	static protected final byte MIN_NUM_PIECES = 2;
	
	private MyState myState;
	private byte gamePhase;
	private Checker currentTurnPlayer;
	
	
	public Game(MyState myState, Checker player) 
	{
		this.myState = myState;
		gamePhase = Game.PLACING_PHASE;
		currentTurnPlayer = player;	
	}
	
	public byte getCurrentGamePhase() 
	{
		return gamePhase;
	}
	
	public MyState getMyState()
	{
		return myState;
	}
	
	public Checker getCurrentPlayer()
	{
		return currentTurnPlayer;
	}
	
	public void updateCurrentPlayer()
	{
		currentTurnPlayer = currentTurnPlayer == Checker.WHITE ? Checker.BLACK : Checker.WHITE;
	}
	
	public Checker getPlayerInBoardPosition(byte boardPosition) 
	{
		try 
		{
			return myState.getPosition(boardPosition).getPlayerOccupyingIt();
		} 
		catch (GameException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return Checker.EMPTY;
	}
	
	public boolean positionIsAvailable(byte boardIndex) throws GameException 
	{
        return myState.positionIsAvailable(boardIndex);
	}
	
	public boolean validMove(byte currentPositionIndex, byte nextPositionIndex) throws GameException 
	{
		Position currentPos = myState.getPosition(currentPositionIndex);
		if(currentPos.isAdjacentToThis(nextPositionIndex) && !myState.getPosition(nextPositionIndex).isOccupied()) 
		{
			return true;
		}
		return false;
	}
	
	public byte movePieceFromTo(byte srcIndex, byte destIndex, Checker player) throws GameException
	{
		if(positionHasPieceOfPlayer(srcIndex, player)) 
		{
			if(positionIsAvailable(destIndex))
			{
				if(validMove(srcIndex, destIndex) ||
						(myState.getNumberOfPiecesOfPlayer(player) == Game.MIN_NUM_PIECES + 1)) 
				{
					myState.getPosition(srcIndex).setAsUnoccupied();
					myState.getPosition(destIndex).setAsOccupied(player);
					return Game.VALID_MOVE;
				} 
				else
				{
					return Game.INVALID_MOVE;
				}
			} 
			else 
			{
				return Game.UNAVAILABLE_POS;
			}
		} 
		else
		{
			return Game.INVALID_SRC_POS;
		}
	}
	
	public boolean placePieceOfPlayer(byte boardPosIndex, Checker player) throws GameException 
	{
		if(myState.positionIsAvailable(boardPosIndex)) 
		{
			myState.getPosition(boardPosIndex).setAsOccupied(player);
			myState.incNumPiecesOfPlayer(player);
			if(myState.incNumTotalPiecesPlaced() == (NUM_PIECES_PER_PLAYER * 2)) 
			{
				gamePhase = Game.MOVING_PHASE;
			}
			return true;
		}
		return false;
	}
	
	public boolean madeAMill(byte dest, Checker player) throws GameException 
	{
		int maxNumPlayerPiecesInRow = 0;
		boolean found = false;
		for(byte i = 0; i < MyState.NUM_MILL_COMBINATIONS && !found; i++) 
		{
			Position[] row = myState.getMillCombination(i);
			for(byte j = 0; j < MyState.NUM_POSITIONS_IN_EACH_MILL && !found; j++)
			{
				if(row[j].getPositionIndex() == dest) 
				{
					maxNumPlayerPiecesInRow =
							Math.max(maxNumPlayerPiecesInRow, numPiecesFromPlayerInRow(row, player));
					if(maxNumPlayerPiecesInRow == MyState.NUM_POSITIONS_IN_EACH_MILL)
						found = true;
				}
			}
		}
		return found;
	}

	
	private byte numPiecesFromPlayerInRow(Position[] pos, Checker player)
	{
		byte counter = 0;
		for(int i = 0; i < pos.length; i++)
		{
			if(pos[i].getPlayerOccupyingIt() == player)
			{
				counter++;
			}
		}
		return counter;
	}
	
	public boolean positionHasPieceOfPlayer(byte boardIndex, Checker player) throws GameException 
	{
		return (myState.getPosition(boardIndex).getPlayerOccupyingIt() == player);
	}
	
	public boolean removePiece(byte boardIndex, Checker player) throws GameException 
	{ 
		if(!myState.positionIsAvailable(boardIndex) && positionHasPieceOfPlayer(boardIndex, player)) 
		{
			myState.getPosition(boardIndex).setAsUnoccupied();
			myState.decNumPiecesOfPlayer(player);
			if(gamePhase == Game.MOVING_PHASE && 
					myState.getNumberOfPiecesOfPlayer(player) == (Game.MIN_NUM_PIECES+1)) 
			{
				gamePhase = Game.FLYING_PHASE;
			}
			return true;
		}
		return false;
	}
	
	public boolean isTheGameOver() 
	{
		try 
		{
			if(myState.getNumberOfPiecesOfPlayer(Checker.WHITE) == Game.MIN_NUM_PIECES
					|| myState.getNumberOfPiecesOfPlayer(Checker.BLACK) == Game.MIN_NUM_PIECES) 
			{
				return true;
			}
			else
			{
				boolean p1HasValidMove = false, p2HasValidMove = false;
				Checker player;
				
				// check if each player has at least one valid move
				for(byte i = 0; i < MyState.NUM_POSITIONS_OF_BOARD; i++) 
				{
					Position position = myState.getPosition(i);
					if((player = position.getPlayerOccupyingIt()) != Checker.EMPTY)
					{
						byte[] adjacent = position.getAdjacentPositionsIndexes();
						for(int j = 0; j < adjacent.length; j++) 
						{	
							Position adjacentPos = myState.getPosition(adjacent[j]);
							if(!adjacentPos.isOccupied()) 
							{
								if(!p1HasValidMove)
								{ 
									p1HasValidMove = (player == Checker.WHITE);
								}
								if(!p2HasValidMove) 
								{
									p2HasValidMove = (player == Checker.BLACK);
								}
								break;
							}
						}
					}
					if(p1HasValidMove && p2HasValidMove)
					{
						return false;
					}
				}
			}
		} 
		catch (GameException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return true;
	}

}
