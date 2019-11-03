package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import domain.Game;
import domain.GameException;
import domain.Move;
import domain.MyState;
import domain.Position;
import domain.Util;
import domain.State.Checker;

public class IAPlayer 
{
	private Checker playerToken;
	private int depth;
	private Checker opponentPlayer;
	private Move currentBestMove;
	public int bestScore = 0;
	static final int maxScore = 1000000;
	
	public IAPlayer(Checker player, byte numPiecesPerPlayer,int depth) throws GameException
	{
		if(player != Checker.WHITE && player != Checker.BLACK)
		{
			throw new GameException(""+getClass().getName()+" - Invalid Player Token: "+player);
		}
		else 
		{
			playerToken = player;
		}
		if(depth < 1)
		{
			throw new GameException(""+getClass().getName()+" - Invalid Minimax Player Depth");
		}
		this.depth = depth;
		opponentPlayer = Util.getOpponentChecker(player);
	}
	
	public byte getIndexToPlacePiece(Game game)
	{
		MyState myState = game.getMyState();
		try 
		{
			List<Move> moves = generateMoves(myState, playerToken, Move.PLACING);
			for(Move move : moves)
			{
				applyMove(move, playerToken, myState, Move.PLACING);
				move.score += alphaBeta(opponentPlayer, myState, depth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
				undoMove(move, playerToken, myState, Move.PLACING);
			}
			
			Collections.sort(moves, new HeuristicComparatorMax());

			// Recupero le mosse che hanno stesso score
			List<Move> bestMoves = new ArrayList<Move>();
			int bestScore = moves.get(0).score;
			bestMoves.add(moves.get(0));
			for(int i = 1; i < moves.size(); i++) 
			{
				if(moves.get(i).score == bestScore) 
				{
					bestMoves.add(moves.get(i));
				}
				else
				{
					break;
				}
			}
			Random rand = new Random();
			currentBestMove = bestMoves.get(rand.nextInt(bestMoves.size()));
			return (byte) currentBestMove.destIndex;
		} 
		catch (GameException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return -1;
	}

	public Move getPieceMove(MyState myState, int gamePhase) throws GameException 
	{
		List<Move> moves = generateMoves(myState, playerToken, getGamePhase(myState, playerToken)); // sorted already
		for(Move move : moves)
		{
			applyMove(move, playerToken, myState, Move.MOVING);
			move.score += alphaBeta(opponentPlayer, myState, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
			undoMove(move, playerToken, myState, Move.MOVING);
		}
		Collections.sort(moves, new HeuristicComparatorMax());
		
		// Recupero le mosse che hanno stesso score
		List<Move> bestMoves = new ArrayList<Move>();
		int bestScore = moves.get(0).score;
		bestMoves.add(moves.get(0));
		for(int i = 1; i < moves.size(); i++) 
		{
			if(moves.get(i).score == bestScore)
			{
				bestMoves.add(moves.get(i));
			}
			else
			{
				break;
			}
		}
		Random rand = new Random();
		currentBestMove = bestMoves.get(rand.nextInt(bestMoves.size()));
		return currentBestMove;
	}
	
	// contare i pezzi inseriti prima del ciclo
	// nella terza fase considera solo i movimenti adiacenti alle pedine presenti
	
	private  List<Move> generateMoves(MyState myState, Checker player, int gamePhase) throws GameException
	{
		List<Move> moves = new ArrayList<Move>();
		Position position, adjacentPos;

		try
		{
			if(gamePhase == Game.PLACING_PHASE) 
			{
				for(byte i = 0; i < MyState.NUM_POSITIONS_OF_BOARD; i++) 
				{ // Search for empty cells and add to the List
					Move move = new Move((byte)-7, (byte)-1, (byte)-1, Move.PLACING);
					if(!(position = myState.getPosition(i)).isOccupied())	
					{
						position.setAsOccupied(player);
						move.destIndex = i;
						checkMove(myState, player, moves, move);
						position.setAsUnoccupied();
					}
				}
			}
			else if (gamePhase == Game.MOVING_PHASE)
			{
				for(byte i = 0; i < MyState.NUM_POSITIONS_OF_BOARD; i++)
				{
					if((position = myState.getPosition(i)).getPlayerOccupyingIt() == player)
					{ // for each piece of the player
						byte[] adjacent = position.getAdjacentPositionsIndexes();

						for(int j = 0; j < adjacent.length; j++) 
						{ // check valid moves to adjacent positions
							Move move = new Move(i, (byte)-1, (byte)-1, Move.MOVING);
							adjacentPos = myState.getPosition(adjacent[j]);

							if(!adjacentPos.isOccupied()) 
							{
								adjacentPos.setAsOccupied(player);
								move.destIndex = adjacent[j];
								position.setAsUnoccupied();
								checkMove(myState, player, moves, move);
								position.setAsOccupied(player);
								adjacentPos.setAsUnoccupied();
							}
						}
					}
				}
			} 
			else if (gamePhase == Game.FLYING_PHASE) 
			{
				List<Byte> freeSpaces = new ArrayList<Byte>();
				List<Byte> playerSpaces = new ArrayList<Byte>();

				for(byte i = 0; i < MyState.NUM_POSITIONS_OF_BOARD; i++) 
				{
					if((position = myState.getPosition(i)).getPlayerOccupyingIt() == player) 
					{
						playerSpaces.add(i);
					}
					else if(!position.isOccupied()) 
					{
						boolean found = false;
						for(byte index : position.getAdjacentPositionsIndexes())
						{
							if(myState.getPosition(index).getPlayerOccupyingIt() != Checker.EMPTY)
							{
								found = true;
								break;
							}
						}
						if(found)
							freeSpaces.add(i);
					}
				}

				// for every piece the player has on the board
				for(int n = 0; n < playerSpaces.size(); n++) 
				{
					Position srcPos =  myState.getPosition(playerSpaces.get(n));
					srcPos.setAsUnoccupied();

					// each empty space is a valid move
					for(int j = 0; j < freeSpaces.size(); j++) 
					{
						Move move = new Move((byte)srcPos.getPositionIndex(), (byte)-1, (byte)-1, Move.MOVING);
						Position destPos = myState.getPosition(freeSpaces.get(j));
						destPos.setAsOccupied(player);
						move.destIndex = freeSpaces.get(j);
						checkMove(myState, player, moves, move);
						destPos.setAsUnoccupied();
					}
					srcPos.setAsOccupied(player);
				}
			}
		} 
		catch (GameException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		if(depth > 3)
		{
			for(Move move : moves)
			{
				Checker removedPlayer = Checker.EMPTY;
				position = myState.getPosition(move.destIndex);

				// Try this move for the current player
				position.setAsOccupied(player);

				if(gamePhase == Game.PLACING_PHASE) 
				{
					myState.incNumPiecesOfPlayer(player);
				}
				else
				{
					myState.getPosition(move.srcIndex).setAsUnoccupied();
				}

				if(move.removePieceOnIndex != -1) // this move removed a piece from opponent
				{ 
					Position removed = myState.getPosition(move.removePieceOnIndex);
					removedPlayer = removed.getPlayerOccupyingIt();
					removed.setAsUnoccupied();
					myState.decNumPiecesOfPlayer(removedPlayer);
				}

				move.score = evaluate(myState, gamePhase);

				// Undo move
				position.setAsUnoccupied();

				if(gamePhase == Game.PLACING_PHASE)
				{
					myState.decNumPiecesOfPlayer(player);
				}
				else
				{
					myState.getPosition(move.srcIndex).setAsOccupied(player);
				}

				if(move.removePieceOnIndex != -1) {
					myState.getPosition(move.removePieceOnIndex).setAsOccupied(removedPlayer);
					myState.incNumPiecesOfPlayer(removedPlayer);
				}
			}

			if(player == playerToken) {
				Collections.sort(moves, new HeuristicComparatorMax());
			} else {
				Collections.sort(moves, new HeuristicComparatorMin());
			}
		}

		//numberOfMoves += moves.size();
		return moves;
	}	

	private int alphaBeta(Checker player, MyState myState, int depth, int alpha, int beta) {

		int gameOver;
		List<Move> childMoves;

		try 
		{
			int gamePhase = getGamePhase(myState, player);

			if (depth==0)
			{ 
				return evaluate(myState, gamePhase);
			} 
			else if((gameOver = checkGameOver(myState)) != 0) 
			{ 
				return gameOver;
			}
			else if((childMoves = generateMoves(myState, player, gamePhase)).isEmpty()) 
			{
				if(player == playerToken) 
				{
					return -maxScore;
				} 
				else
				{
					return maxScore;
				}
			}  
			else
			{
				for (Move move : childMoves) 
				{
					applyMove(move, player, myState, gamePhase);
					if (player == playerToken) // maximizing player
					{  
						alpha = Math.max(alpha, alphaBeta(opponentPlayer, myState, depth - 1, alpha, beta));

						if (beta <= alpha)
						{
							undoMove(move, player, myState, gamePhase);
							break; 
						}
					} 
					else //  minimizing player
					{  
						beta = Math.min(beta, alphaBeta(playerToken, myState, depth - 1, alpha, beta));
						if (beta <= alpha) {
							undoMove(move, player, myState, gamePhase);
							break; 
						}
					}
					undoMove(move, player, myState, gamePhase);
				}

				if(player == playerToken)
				{
					return alpha;
				} 
				else
				{
					return beta;
				}
			}
		} 
		catch (GameException e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return -1;
	}
	
	private int evaluate(MyState myState, int gamePhase) throws GameException 
	{
		int score = 0;
		int R1_numPlayerMills = 0, R1_numOppMills = 0;
		int R2_numPlayerTwoPieceConf = 0, R2_numOppTwoPieceConf = 0;

		for(byte i = 0; i < MyState.NUM_POSITIONS_OF_BOARD; i++) 
		{
			int playerPieces = 0, emptyCells = 0, opponentPieces = 0;
			if(i <= MyState.NUM_MILL_COMBINATIONS - 1)
			{
				try 
				{
					Position[] row = myState.getMillCombination(i);
					for(int j = 0; j < MyState.NUM_POSITIONS_IN_EACH_MILL; j++) 
					{
						if(row[j].getPlayerOccupyingIt() == playerToken)
						{
							playerPieces++;
						}
						else if(row[j].getPlayerOccupyingIt() == Checker.EMPTY)
						{
							emptyCells++;
						}
						else
						{ 
							opponentPieces++;
						}
					}
				} 
				catch (GameException e)
				{
					e.printStackTrace();
				}
				if(playerPieces == 3 && myState.getIsNewMill()[i]) 
				{
					R1_numPlayerMills++;	
				} 
				else if(playerPieces == 2 && emptyCells == 1)
				{
					R2_numPlayerTwoPieceConf++;
				}
				else if(playerPieces == 1 && emptyCells == 2) 
				{
					score += 1;
				}
				else if(opponentPieces == 3 && myState.getIsNewMill()[i]) 
				{
					R1_numOppMills++;
					
				}
				else if(opponentPieces == 2 && emptyCells == 1)
				{
					R2_numOppTwoPieceConf++;
				} 
				else if(opponentPieces == 1 && emptyCells == 2)
				{
					score += -1;
				}
			}
			Checker playerInPos = myState.getPosition(i).getPlayerOccupyingIt();
			if(i == 4 || i == 10 || i == 13 || i == 19)
			{
				if(playerInPos == playerToken) 
				{
					score += 2;
				} 
				else if(playerInPos != Checker.EMPTY) 
				{
					score -= 2;
				}
			} 
			else if(i == 1 || i == 9 || i == 14 || i == 22 
						|| i == 7 || i == 11 || i == 12 || i == 16) 
			{
				if(playerInPos == playerToken) 
				{
					score += 1;
				}
				else if(playerInPos != Checker.EMPTY) 
				{
					score -= 1;
				}
			}
		}

		int coef1,coef2,coef3;
		// number of mills
		if(gamePhase == Game.PLACING_PHASE) 
		{
			coef1 = 80; 
			coef2 = 10; 
			coef3 = 12;
			
			score += coef1*R1_numPlayerMills;
			score -= coef1*R1_numOppMills;
			score += coef2*myState.getNumberOfPiecesOfPlayer(playerToken);
			score -= coef2*myState.getNumberOfPiecesOfPlayer(opponentPlayer);
			score += coef3*R2_numPlayerTwoPieceConf;
			score -= coef3*R2_numOppTwoPieceConf;
		} 
		else if(gamePhase == Game.MOVING_PHASE)
		{
			coef1 = 120; 
			coef2 = 8;
			coef3 = 10;
			
			score += coef1*R1_numPlayerMills;
			score -= coef1*R1_numOppMills;
			score += coef2*myState.getNumberOfPiecesOfPlayer(playerToken);
			score -= coef2*myState.getNumberOfPiecesOfPlayer(opponentPlayer);
			score += coef3*R2_numPlayerTwoPieceConf;
			score -= coef3*R2_numOppTwoPieceConf;
		} 
		else
		{
			coef1 = 180; 
			coef2 = 6;
			coef3 = 10;
			
			score += coef1*R1_numPlayerMills;
			score -= coef1*R1_numOppMills;
			score += coef2*myState.getNumberOfPiecesOfPlayer(playerToken);
			score -= coef2*myState.getNumberOfPiecesOfPlayer(opponentPlayer);
			score += coef3*R2_numPlayerTwoPieceConf;
			score -= coef3*R2_numOppTwoPieceConf;
		}
		//gameBoard.printBoard();
		//System.out.println("score " + score + " Numero MyMill: " + R1_numPlayerMills + " NumOppMill: " +
		//                       R1_numOppMills + " numero My2Conf: " + R2_numPlayerTwoPieceConf +
		//                        	" numero Opp2Conf: " + R2_numOppTwoPieceConf);
		return score;
	}
	
	private void checkMove(MyState myState, Checker player, List<Move> moves, Move move) throws GameException 
	{
		boolean madeMill = false;
		Checker opponent = Util.getOpponentChecker(player);
		for(byte i = 0; i < MyState.NUM_MILL_COMBINATIONS; i++) //check if piece made a mill
		{ 
			int playerPieces = 0; 
			boolean selectedPiece = false;
			Position[] row = myState.getMillCombination(i);
			for(byte j = 0; j < MyState.NUM_POSITIONS_IN_EACH_MILL; j++)
			{

				if(row[j].getPlayerOccupyingIt() == player) 
				{
					playerPieces++;
				}
				if(row[j].getPositionIndex() == move.destIndex)
				{
					selectedPiece = true;
				}
			}

			if(playerPieces == 3 && selectedPiece)  // made a mill - select piece to remove
			{
				madeMill = true;
				for(byte l = 0; l < MyState.NUM_POSITIONS_OF_BOARD; l++) 
				{
					Move removingMove = new Move(move.srcIndex, move.destIndex, move.destIndex, move.typeOfMove);
					Position pos = myState.getPosition(l);

					if(pos.getPlayerOccupyingIt() != player && 
							pos.getPlayerOccupyingIt() != Checker.EMPTY &&
								(!madeAMill(l, opponent, myState) || isAllMill(opponent, myState)) )
					{
						removingMove.removePieceOnIndex = l;
						// add a move for each piece that can be removed, 
						//this way it will check what's the best one to remove
						moves.add(removingMove);
					}
				}
			}
			selectedPiece = false;					
		}

		if(!madeMill)
		{ // don't add repeated moves
			moves.add(move);
		}
		else
		{
			madeMill = false;
		}
	}
	
	private void applyMove(Move move, Checker player, MyState myState, int gamePhase) throws GameException
	{	
		// Try this move for the current player
		byte destIndex = move.destIndex;
		Position position = myState.getPosition(destIndex);
		position.setAsOccupied(player);
		
		if(gamePhase == Game.PLACING_PHASE)
		{
			myState.incNumPiecesOfPlayer(player);
		} 
		else
		{
			myState.getPosition(move.srcIndex).setAsUnoccupied();
		}

		if(move.removePieceOnIndex != -1) // this move removed a piece from opponent
		{ 
			Position removed = myState.getPosition(move.removePieceOnIndex);
			removed.setAsUnoccupied();
			myState.decNumPiecesOfPlayer(Util.getOpponentChecker(player));
			for(byte i=0; i<MyState.NUM_MILL_COMBINATIONS;i++)
			{
				Position[] row = myState.getMillCombination(i);
				if(row[0].getPositionIndex() == destIndex ||
						row[1].getPositionIndex() == destIndex ||
							row[2].getPositionIndex() == destIndex)
				{
					if(row[0].getPlayerOccupyingIt() == player &&
						row[1].getPlayerOccupyingIt() == player &&
							row[2].getPlayerOccupyingIt() == player)
					{
						myState.getIsNewMill()[i] = true;
						break;
					}
				}
			}
		}
	}
	
	private void undoMove(Move move, Checker player, MyState myState, int gamePhase) throws GameException 
	{
		Position position = myState.getPosition(move.destIndex);
		position.setAsUnoccupied();

		if(gamePhase == Game.PLACING_PHASE) 
		{
			myState.decNumPiecesOfPlayer(player);
		} 
		else 
		{
			myState.getPosition(move.srcIndex).setAsOccupied(player);
		}

		if(move.removePieceOnIndex != -1) 
		{
			Checker opp = Util.getOpponentChecker(player);
			myState.getPosition(move.removePieceOnIndex).setAsOccupied(opp);
			myState.incNumPiecesOfPlayer(opp);
		}
	}

	public byte getIndexToRemovePieceOfOpponent()
	{
		return currentBestMove.removePieceOnIndex;
	}
	
	public int getGamePhase(MyState myState, Checker player) 
	{
		int gamePhase = Game.PLACING_PHASE;
		try 
		{
			if(myState.getNumTotalPiecesPlaced() == (Game.NUM_PIECES_PER_PLAYER * 2))
			{
				gamePhase = Game.MOVING_PHASE;
				if(myState.getNumberOfPiecesOfPlayer(player) <= 3)
				{
					gamePhase = Game.FLYING_PHASE;
				}
			}
		} 
		catch (GameException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return gamePhase;		
	}
	
	private int checkGameOver(MyState myState) 
	{
		if(myState.getNumTotalPiecesPlaced() == (Game.NUM_PIECES_PER_PLAYER * 2))
		{
			try 
			{
				if(myState.getNumberOfPiecesOfPlayer(playerToken) <= 2) 
				{
					return -maxScore;
				}
				else if(myState.getNumberOfPiecesOfPlayer(opponentPlayer) <= 2)
				{
					return maxScore;
				}
				else
				{
					return 0;
				}
			} 
			catch (GameException e) 
			{
				e.printStackTrace();

			}
		}
		return 0;
	}
	
	private boolean madeAMill(byte dest, Checker player, MyState myState) throws GameException 
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
		
	
	
	
	
	private class HeuristicComparatorMax implements Comparator<Move>
	{

		@Override
		public int compare(Move t, Move t1) {
			return t1.score - t.score;
		}
	}
	

	private class HeuristicComparatorMin implements Comparator<Move> 
	{

		@Override
		public int compare(Move t, Move t1) {
			return t.score - t1.score;
		}
	}

	private boolean isAllMill(Checker player, MyState myState) throws GameException
	{
		boolean result = true;
		for(byte i = 0; i < MyState.NUM_POSITIONS_OF_BOARD; i++)
		{
			if(myState.getPosition(i).getPlayerOccupyingIt() == player)
			{
				if(!madeAMill(i, player, myState))
				{
					result = false;
					break;
				}
			}
		}
		return result;
	}








}
