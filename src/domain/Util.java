package domain;

import java.util.HashMap;
import java.util.Map;

import domain.State.Checker;

public class Util
{
	public static Map<Byte, String> ITS = new HashMap<Byte, String>();
	static{
		ITS.put((byte)0 , "a7");
		ITS.put((byte)1 , "d7");
		ITS.put((byte)2 , "g7");
		ITS.put((byte)3 , "b6");
		ITS.put((byte)4 , "d6");
		ITS.put((byte)5 , "f6");
		ITS.put((byte)6 , "c5");
		ITS.put((byte)7 , "d5");
		ITS.put((byte)8 , "e5");
		ITS.put((byte)9 , "a4");
		ITS.put((byte)10 , "b4");
		ITS.put((byte)11 , "c4");
		ITS.put((byte)12 , "e4");
		ITS.put((byte)13 , "f4");
		ITS.put((byte)14 , "g4");
		ITS.put((byte)15 , "c3");
		ITS.put((byte)16 , "d3");
		ITS.put((byte)17 , "e3");
		ITS.put((byte)18 , "b2");
		ITS.put((byte)19 , "d2");
		ITS.put((byte)20 , "f2");
		ITS.put((byte)21 , "a1");
		ITS.put((byte)22 , "d1");
		ITS.put((byte)23 , "g1");
	}
	
	public static Map<String, Byte> STI = new HashMap<String, Byte>();
	static{
		STI.put("a7",(byte)0);
		STI.put("d7",(byte)1);
		STI.put("g7",(byte)2);
		STI.put("b6",(byte)3);
		STI.put("d6",(byte)4);
		STI.put("f6",(byte)5);
		STI.put("c5",(byte)6);
		STI.put("d5",(byte)7);
		STI.put("e5",(byte)8);
		STI.put("a4",(byte)9);
		STI.put("b4",(byte)10);
		STI.put("c4",(byte)11);
		STI.put("e4",(byte)12);
		STI.put("f4",(byte)13);
		STI.put("g4",(byte)14);
		STI.put("c3",(byte)15);
		STI.put("d3",(byte)16);
		STI.put("e3",(byte)17);
		STI.put("b2",(byte)18);
		STI.put("d2",(byte)19);
		STI.put("f2",(byte)20);
		STI.put("a1",(byte)21);
		STI.put("d1",(byte)22);
		STI.put("g1",(byte)23);
	}
	
	public static Checker getOpponentChecker(Checker player)
	{
		Checker opponent = player == Checker.WHITE ? Checker.BLACK : Checker.WHITE;
		return opponent;
	}

	public static void setNewStatePhase1(State oldState, State newState, Game game) throws GameException
	{
		for(String s: newState.positions)
		{
			Checker oldC = oldState.getBoard().get(s);
			Checker newC = newState.getBoard().get(s);
	
			if(!oldC.equals(newC))
			{
				if(newC.equals(game.getCurrentPlayer()))
				{
					game.placePieceOfPlayer(STI.get(s), newC);
				}
				if(newC.equals(Checker.EMPTY))
				{
					game.removePiece(STI.get(s), oldC);
				}
			}
		}
	}
	
	public static void setNewStatePhase2(State oldState, State newState, Game game) throws GameException
	{
		byte srcIndex = 0, destIndex = 0;
		for(String s: newState.positions)
		{
			Checker oldC = oldState.getBoard().get(s);
			Checker newC = newState.getBoard().get(s);
		
			if(!oldC.equals(newC))
			{
				if(oldC == getOpponentChecker(game.getCurrentPlayer()))
				{
					game.removePiece(STI.get(s), oldC);
				}
				else if(oldC == Checker.EMPTY)
				{
					destIndex = STI.get(s);
				}
				else if(oldC == game.getCurrentPlayer())
				{
					srcIndex = STI.get(s);
				}
			}
		}
		game.movePieceFromTo(srcIndex, destIndex, game.getCurrentPlayer());
	}
	
}
