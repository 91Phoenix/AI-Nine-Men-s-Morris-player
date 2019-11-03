package client;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import action.Action;
import action.Phase1Action;
import action.Phase2Action;
import action.PhaseFinalAction;
import domain.Game;
import domain.GameException;
import domain.Move;
import domain.MyState;
import domain.State;
import domain.State.Checker;
import domain.Util;
import engine.TCPMulino;


public class MulinoClient
{
	
	private State.Checker player;
	private Socket playerSocket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public MulinoClient(State.Checker player) throws UnknownHostException, IOException 
	{
		this.player = player;
		int port = 0;
		switch (player) {
			case WHITE:
				port = TCPMulino.whiteSocket;
				break;
			case BLACK:
				port = TCPMulino.blackSocket;
				break;
			default:
				System.exit(5);
		}
		playerSocket = new Socket("localhost", port);
		out = new ObjectOutputStream(playerSocket.getOutputStream());
		in = new ObjectInputStream(new BufferedInputStream(playerSocket.getInputStream()));
	}
	
	
	public void write(Action action) throws IOException, ClassNotFoundException 
	{
		out.writeObject(action);
	}
	
	public State read() throws ClassNotFoundException, IOException 
	{
		return (State) in.readObject();
	}
	
	public State.Checker getPlayer() { return player; }
	public void setPlayer(State.Checker player) { this.player = player; }
	
	public static void main(String[] args) throws UnknownHostException, IOException, 
			ClassNotFoundException, GameException 
	{
		State.Checker player;
	
		if (args.length==0) 
		{
			System.out.println("You must specify which player you are (Wthie or Black)!");
			System.exit(-1);
		}
		System.out.println("Selected client: " + args[0]);
		
		
		if ("White".equals(args[0]))
			player = State.Checker.WHITE;
		else
			player = State.Checker.BLACK;
		
		State currentState = null;
		MyState myState = new MyState(); // stato compatibile con l'IA 
		int depth = 8;
		IAPlayer IA = new IAPlayer(player, Game.NUM_PIECES_PER_PLAYER, depth); // IA
		Game game = new Game(myState, player); // gestione della partita
		
		MulinoClient client = new MulinoClient(player);
		System.out.println("You are player " + client.getPlayer().toString() + "!");
		System.out.println("Current state:");
		
		currentState = client.read();
		System.out.println(currentState.toString());
		if(player == Checker.WHITE)
		{
			while (game.getCurrentGamePhase() == Game.PLACING_PHASE) 
			{
				System.out.println("Player " + client.getPlayer().toString() + ", do your move: ");
				
				Phase1Action action = getPhase1Action(player, IA, game); //piazzamento propria pedina
				client.write(action);
				
				currentState = client.read();
				System.out.println("Effect of your move: ");
				System.out.println(currentState.toString());
				game.updateCurrentPlayer();
				
				System.out.println("Waiting for your opponent move... ");
				State newState = client.read(); //stato con il movimento avversario
				Util.setNewStatePhase1(currentState, newState, game); // aggiornamento stato
				System.out.println("Your Opponent did his move, and the result is: ");
				System.out.println(currentState.toString());
				game.updateCurrentPlayer();
			}
			System.out.println("FASE 1 TERMINATA");
			while(!game.isTheGameOver())
			{	
				System.out.println("Player " + client.getPlayer().toString() + ", do your move: ");
				
				Action action = getPhase2Action(client.getPlayer(), IA, game);
				client.write(action);
				
				currentState = client.read();
				System.out.println("Effect of your move: ");
				System.out.println(currentState.toString());
				game.updateCurrentPlayer();
				
				System.out.println("Waiting for your opponent move... ");
				State newState = client.read();
				Util.setNewStatePhase2(currentState, newState, game); // aggiornamento stato
				System.out.println("Your Opponent did his move, and the result is: ");
				System.out.println(currentState.toString());
				game.updateCurrentPlayer();
			}
		}
		else
		{
			while (game.getCurrentGamePhase() == Game.PLACING_PHASE) 
			{
				game.updateCurrentPlayer();
				System.out.println("Waiting for your opponent move... ");
				State newState = client.read(); //stato con il movimento avversario
				Util.setNewStatePhase1(currentState, newState, game); // aggiornamento stato
				System.out.println("Your Opponent did his move, and the result is: ");
				System.out.println(newState.toString());
				
				game.updateCurrentPlayer();		
				System.out.println("Player " + client.getPlayer().toString() + ", do your move: ");
				
				Phase1Action action = getPhase1Action(player, IA, game); //piazzamento propria pedina
				client.write(action);
				
				currentState = client.read();
				System.out.println("Effect of your move: ");
				System.out.println(currentState.toString());		
			}
			System.out.println("FASE 1 TERMINATA");
			while(!game.isTheGameOver())
			{	
				game.updateCurrentPlayer();
				System.out.println("Waiting for your opponent move... ");
				State newState = client.read();
				Util.setNewStatePhase2(currentState, newState, game); // aggiornamento stato
				System.out.println("Your Opponent did his move, and the result is: ");
				System.out.println(newState.toString());
				
				game.updateCurrentPlayer();
				System.out.println("Player " + client.getPlayer().toString() + ", do your move: ");
				
				Action action = getPhase2Action(client.getPlayer(), IA, game);
				client.write(action);
				
				currentState = client.read();
				System.out.println("Effect of your move: ");
				System.out.println(currentState.toString());
			}
		}
	}
	
	private static Phase1Action getPhase1Action(State.Checker player, IAPlayer IA, Game game)
			throws GameException, ClassNotFoundException 
	{
		Phase1Action action = new Phase1Action();
		byte boardIndex = IA.getIndexToPlacePiece(game);
		String actionString = Util.ITS.get(boardIndex);
		if(game.placePieceOfPlayer(boardIndex, player))
		{
			if(game.madeAMill(boardIndex, player))
			{
				boardIndex = IA.getIndexToRemovePieceOfOpponent();
				if(game.removePiece(boardIndex, Util.getOpponentChecker(player)))
				{
					String toRemove = Util.ITS.get(boardIndex);
					action.setRemoveOpponentChecker(toRemove.substring(0,2));
				}
			}
		}

		action.setPutPosition(actionString.substring(0, 2));
		return action;
	}

	private static Action getPhase2Action(State.Checker player, IAPlayer IA, Game game)
			throws GameException, ClassNotFoundException 
	{
		Action action = null;
		byte boardIndex;
		
		if(game.getCurrentGamePhase() == Game.FLYING_PHASE)
			action = new PhaseFinalAction();
		else
			action = new Phase2Action();
		
		Move m = IA.getPieceMove(game.getMyState(), game.getCurrentGamePhase());
		action.setFrom(Util.ITS.get(m.srcIndex));
		action.setTo(Util.ITS.get(m.destIndex));
		
		if((game.movePieceFromTo(m.srcIndex,m.destIndex, player)) == Game.VALID_MOVE)
		{
			if(game.madeAMill(m.destIndex, player))
			{
				boardIndex = m.removePieceOnIndex;
				action.setRemoveOpponentChecker(Util.ITS.get(m.removePieceOnIndex));
				
				game.removePiece(boardIndex, Util.getOpponentChecker(player));

			}
		}
		return action;
	}

	
}
