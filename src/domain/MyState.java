package domain;

import domain.State.Checker;

public class MyState
{
	static public final byte NUM_POSITIONS_OF_BOARD = 24;
	static public final byte NUM_MILL_COMBINATIONS = 16;
	static public final byte NUM_POSITIONS_IN_EACH_MILL = 3;
	
	private Position[] boardPositions;
	private Position[][] millCombinations;
	private boolean[] isNewMill;
	private byte numOfPiecesP1;
	private byte numOfPiecesP2;
	private byte numberOfTotalPiecesPlaced;
	
	public MyState() 
	{
		boardPositions = new Position[NUM_POSITIONS_OF_BOARD];
		isNewMill = new boolean[NUM_MILL_COMBINATIONS];
		for(int i=0; i<NUM_MILL_COMBINATIONS;i++)
			isNewMill[i]=false;
		numOfPiecesP1 = 0;
		numOfPiecesP2 = 0;
		numberOfTotalPiecesPlaced = 0;
		initBoard();
		initMillCombinations();
	}
	
	private void initBoard() 
	{
		for(byte i = 0; i < NUM_POSITIONS_OF_BOARD; i++) 
		{
			boardPositions[i] = new Position(i);
		}
		// outer square
		boardPositions[0].addAdjacentPositionsIndexes((byte)1,(byte)9);
		boardPositions[1].addAdjacentPositionsIndexes((byte)0,(byte)2,(byte)4);
		boardPositions[2].addAdjacentPositionsIndexes((byte)1,(byte)14);
		boardPositions[9].addAdjacentPositionsIndexes((byte)0,(byte)10,(byte)21);
		boardPositions[14].addAdjacentPositionsIndexes((byte)2,(byte)13,(byte)23);
		boardPositions[21].addAdjacentPositionsIndexes((byte)9,(byte)22);
		boardPositions[22].addAdjacentPositionsIndexes((byte)19,(byte)21,(byte)23);
		boardPositions[23].addAdjacentPositionsIndexes((byte)14,(byte)22);
		// middle square
		boardPositions[3].addAdjacentPositionsIndexes((byte)4,(byte)10);
		boardPositions[4].addAdjacentPositionsIndexes((byte)1,(byte)3,(byte)5,(byte)7);
		boardPositions[5].addAdjacentPositionsIndexes((byte)4,(byte)13);
		boardPositions[10].addAdjacentPositionsIndexes((byte)3,(byte)9,(byte)11,(byte)18);
		boardPositions[13].addAdjacentPositionsIndexes((byte)5,(byte)12,(byte)14,(byte)20);
		boardPositions[18].addAdjacentPositionsIndexes((byte)10,(byte)19);
		boardPositions[19].addAdjacentPositionsIndexes((byte)16,(byte)18,(byte)20,(byte)22);
		boardPositions[20].addAdjacentPositionsIndexes((byte)13,(byte)19);
		// inner square
		boardPositions[6].addAdjacentPositionsIndexes((byte)7,(byte)11);
		boardPositions[7].addAdjacentPositionsIndexes((byte)4,(byte)6,(byte)8);
		boardPositions[8].addAdjacentPositionsIndexes((byte)7,(byte)12);
		boardPositions[11].addAdjacentPositionsIndexes((byte)6,(byte)10,(byte)15);
		boardPositions[12].addAdjacentPositionsIndexes((byte)8,(byte)13,(byte)17);
		boardPositions[15].addAdjacentPositionsIndexes((byte)11,(byte)16);
		boardPositions[16].addAdjacentPositionsIndexes((byte)15,(byte)17,(byte)19);
		boardPositions[17].addAdjacentPositionsIndexes((byte)12,(byte)16);
	}
	
	private void initMillCombinations()
	{
		millCombinations = new Position[NUM_MILL_COMBINATIONS][NUM_POSITIONS_IN_EACH_MILL];
		
		//outer square
		millCombinations[0][0] = boardPositions[0];
		millCombinations[0][1] = boardPositions[1];
		millCombinations[0][2] = boardPositions[2];
		millCombinations[1][0] = boardPositions[0];
		millCombinations[1][1] = boardPositions[9];
		millCombinations[1][2] = boardPositions[21];
		millCombinations[2][0] = boardPositions[2];
		millCombinations[2][1] = boardPositions[14];
		millCombinations[2][2] = boardPositions[23];
		millCombinations[3][0] = boardPositions[21];
		millCombinations[3][1] = boardPositions[22];
		millCombinations[3][2] = boardPositions[23];
		//middle square
		millCombinations[4][0] = boardPositions[3];
		millCombinations[4][1] = boardPositions[4];
		millCombinations[4][2] = boardPositions[5];
		millCombinations[5][0] = boardPositions[3];
		millCombinations[5][1] = boardPositions[10];
		millCombinations[5][2] = boardPositions[18];
		millCombinations[6][0] = boardPositions[5];
		millCombinations[6][1] = boardPositions[13];
		millCombinations[6][2] = boardPositions[20];
		millCombinations[7][0] = boardPositions[18];
		millCombinations[7][1] = boardPositions[19];
		millCombinations[7][2] = boardPositions[20];
		//inner square
		millCombinations[8][0] = boardPositions[6];
		millCombinations[8][1] = boardPositions[7];
		millCombinations[8][2] = boardPositions[8];
		millCombinations[9][0] = boardPositions[6];
		millCombinations[9][1] = boardPositions[11];
		millCombinations[9][2] = boardPositions[15];
		millCombinations[10][0] = boardPositions[8];
		millCombinations[10][1] = boardPositions[12];
		millCombinations[10][2] = boardPositions[17];
		millCombinations[11][0] = boardPositions[15];
		millCombinations[11][1] = boardPositions[16];
		millCombinations[11][2] = boardPositions[17];
		//others
		millCombinations[12][0] = boardPositions[1];
		millCombinations[12][1] = boardPositions[4];
		millCombinations[12][2] = boardPositions[7];
		millCombinations[13][0] = boardPositions[9];
		millCombinations[13][1] = boardPositions[10];
		millCombinations[13][2] = boardPositions[11];
		millCombinations[14][0] = boardPositions[12];
		millCombinations[14][1] = boardPositions[13];
		millCombinations[14][2] = boardPositions[14];
		millCombinations[15][0] = boardPositions[16];
		millCombinations[15][1] = boardPositions[19];
		millCombinations[15][2] = boardPositions[22];
	}
	
	public byte getNumOfPiecesP1() 
	{
		return numOfPiecesP1;
	}

	public byte getNumOfPiecesP2() 
	{
		return numOfPiecesP2;
	}

	public int getNumTotalPiecesPlaced() 
	{
		return numberOfTotalPiecesPlaced;
	}
	
	public Position getPosition(byte posIndex) throws GameException
	{
		if(posIndex >= 0 && posIndex < NUM_POSITIONS_OF_BOARD) {
			return boardPositions[posIndex];
		} else 
		{
			throw new GameException(""+getClass().getName()+" - Invalid Board Position Index: "+posIndex);
		}
	}
	
	public boolean positionIsAvailable(byte posIndex) throws GameException 
	{
		if(posIndex >= 0 && posIndex < NUM_POSITIONS_OF_BOARD)
		{
			return !boardPositions[posIndex].isOccupied();
		}
		else
		{
			throw new GameException(""+getClass().getName()+" - Invalid Board Position Index: "+posIndex);
		}
	}
	
	public void setPositionAsPlayer(byte posIndex, Checker player) throws GameException
	{
		if(posIndex >= 0 && posIndex < NUM_POSITIONS_OF_BOARD)
		{
			if(player == Checker.WHITE || player == Checker.BLACK) 
			{
				boardPositions[posIndex].setAsOccupied(player);
			} 
			else
			{
				throw new GameException(""+getClass().getName()+" - Invalid Player Token: "+player);
			}
		} else {
			throw new GameException(""+getClass().getName()+" - Invalid Board Position Index: "+posIndex);
		}
	}

	public byte incNumTotalPiecesPlaced() 
	{
		return ++numberOfTotalPiecesPlaced;
	}
	
	public byte incNumPiecesOfPlayer(Checker player) throws GameException 
	{
		if(player == Checker.WHITE)
		{
			return ++numOfPiecesP1;
		} 
		else if (player == Checker.BLACK) 
		{
			return ++numOfPiecesP2;
		} 
		else
		{
			throw new GameException(""+getClass().getName()+" - Invalid Player Token: "+player);
		}
	}

	public byte decNumPiecesOfPlayer(Checker player) throws GameException 
	{
		if(player == Checker.WHITE) 
		{
			return --numOfPiecesP1;
		}
		else if (player == Checker.BLACK) 
		{
			return --numOfPiecesP2;
		} 
		else
		{
			throw new GameException(""+getClass().getName()+" - Invalid Player Token: "+player);
		}
	}
	
	public byte getNumberOfPiecesOfPlayer(Checker player) throws GameException 
	{
		if(player == Checker.WHITE)
		{
			return numOfPiecesP1;
		} 
		else if (player == Checker.BLACK) 
		{
			return numOfPiecesP2;
		}
		else 
		{
			throw new GameException(""+getClass().getName()+" - Invalid Player Token: "+player);
		}
	}
	 
	public Position[] getMillCombination(byte index) throws GameException 
	{
		if(index >= 0 && index < NUM_MILL_COMBINATIONS) 
		{
			return millCombinations[index];
		} 
		else
		{
			throw new GameException(""+getClass().getName()+" - Invalid Mill Combination Index: "+index);
		}
	}
	
	public boolean[] getIsNewMill()
	{
		return isNewMill;
	}
	
	public void printBoard() {
		System.out.println(showPos(0)+" - - - - - "+showPos(1)+" - - - - - "+showPos(2));
		System.out.println("|           |           |");
		System.out.println("|     "+showPos(3)+" - - "+showPos(4)+" - - "+showPos(5)+"     |");
		System.out.println("|     |     |     |     |");
		System.out.println("|     | "+showPos(6)+" - "+showPos(7)+" - "+showPos(8)+" |     |" );
		System.out.println("|     | |       | |     |");
		System.out.println(showPos(9)+" - - "+showPos(10)+"-"+showPos(11)+"       "+showPos(12)+"-"+showPos(13)+" - - "+showPos(14));
		System.out.println("|     | |       | |     |");
		System.out.println("|     | "+showPos(15)+" - "+showPos(16)+" - "+showPos(17)+" |     |" );
		System.out.println("|     |     |     |     |");
		System.out.println("|     "+showPos(18)+" - - "+showPos(19)+" - - "+showPos(20)+"     |");
		System.out.println("|           |           |");
		System.out.println(showPos(21)+" - - - - - "+showPos(22)+" - - - - - "+showPos(23));
	}
	
	private String showPos(int i)
	{
		switch (boardPositions[i].getPlayerOccupyingIt()) {
		case WHITE:
			return "X";
		case BLACK:
			return "O";
		case EMPTY:
			return "*";
		default:
			return null;
		}
	}
	
}
