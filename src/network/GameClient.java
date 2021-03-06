package network;

import game.BoardSquare;
import game.Game;
import game.Location;
import game.Player;
import game.avatar.Avatar;
import game.objects.interactiveObjects.Item;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.esotericsoftware.kryonet.*;

import network.Packets.*;

/**
 * This class handles the Client Connection of the game and passes interactions 
 * to the server. The Client will have a local copy of the Game object and constantly
 * be updated from the server from other client interactions and update the local
 * copy of the Game object.
 * 
 * @author Prashant Bhikhu
 *
 */
public class GameClient extends Listener {
	private Game game; // The local copy of the game (reducing Large network packet transfers)
	private Client client; // The actual client connection used to connect to the server
	private GameClientListener gameClientListener; // Used to tell the Client's window to update on client refresh
	private Stack<Object> recievedServerReponses = new Stack<Object>(); // Used to get responses from the server when the client is awaiting for server feedback

	/**
	 * Create a new GameServer object (this does not however start the client connection though)
	 * 
	 * @param serverFrame The frame in which console messages are written in to
	 * @throws IOException Thrown when Client cannot connect to a server
	 */
	public GameClient() {
		// Create the client socket with all prerequisites 
	    client = new Client(Network.DEFAULT_BUFFER_SIZE, Network.DEFAULT_BUFFER_SIZE); 
	    Network.register(client);
	    client.addListener(this);
	    client.start();
	}
	
	/**
	 * Connects the client socket to a server socket.
	 * 
	 * @param host The host server you want to connect to.
	 * @throws IOException
	 */
	public void connect(InetAddress host) throws IOException {
	    try {
			client.connect(5000, host, Network.DEFAULT_SERVER_PORT_TCP, Network.DEFAULT_SERVER_PORT_UDP);
		} catch (IOException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Checks if the GameClient is connected
	 */
	public boolean isConnected() {
	    return client.isConnected();
	}	
	
	/**
	 * Disconnect the socket connection (but only if its currently connected).
	 */
	public void disconnect() {
	    if(client.isConnected()) {
	    	ClientQuit packet = new ClientQuit();
	    	packet.id = getClientPlayer().getId();
	    	client.sendTCP(packet);
	    	client.close();
	    	game = null;
	    }
	}

	
	/**
	 * @return the list of GameServer servers that the client can join (on whole LAN network)
	 */
	public List<InetAddress> getServerList() {
		return client.discoverHosts(Network.DEFAULT_SERVER_PORT_UDP, Network.DEFAULT_DISCOVERY_TIMEOUT);
	}
	
	@Override
	public void received (Connection connection, Object object) {
		// Determine which the packet is received from the server and deal with them separately

		if(object instanceof ClientNewGame) {
			handleNewGamePacket((ClientNewGame) object);
		}
		else if(object instanceof PlayerUpdateLocationAndDirection) {
			PlayerUpdateLocationAndDirection packet = ((PlayerUpdateLocationAndDirection) object);
			handlePlayerUpdateLocationAndDirection(packet);
		}
		
		else if (object instanceof ClientQuit) {
			ClientQuit packet = (ClientQuit) object;
			game.getPlayers().remove(game.getPlayerByID(packet.id));
			gameClientListener.onGameUpdated();
		}	
		
		else if(object instanceof ServerQuit) {
			this.disconnect();
		}
		
		else if(object instanceof ClientMessage) {
			ClientMessage packet = (ClientMessage) object;
			gameClientListener.onMessageRecieved(packet.playerName, packet.message);
		}
		
		else if(object instanceof ValidateNewPlayerUsername_Response) {
			recievedServerReponses.add(object);
		}
		
		else if(object instanceof ClientOnChoosePlayer_Response) {
			recievedServerReponses.add(object);
		}
		
		else if(object instanceof PlayerUpdate) {
			PlayerUpdate packet = (PlayerUpdate) object;
			Player playerToUpdate = game.getPlayerByID(packet.id);
			
			playerToUpdate.setAttack(packet.newAttack);
			playerToUpdate.setHealth(packet.newHealth);
			playerToUpdate.setPlayerLevel(packet.newPlayerLevel);
			gameClientListener.onGameUpdated();
		}
		
		else if(object instanceof PlayerPickUpItem) {
			PlayerPickUpItem packet = (PlayerPickUpItem) object;
			
			Player playerToUpdate = getGame().getPlayerByID(packet.id);
			playerToUpdate.getInventory().add(packet.item);
			
			BoardSquare sq = game.getRoomByName(packet.location.getRoom().getName()).getBoard().getSquareAt(packet.location.getY(), packet.location.getX());
			sq.setGameObjectOnSquare(null);
			
			//server.sendToAllExceptTCP(packet.id, object);
		}
		
		else if(object instanceof PlayerDropItem) {
			PlayerDropItem packet = (PlayerDropItem) object;
			
			Player playerToUpdate = getGame().getPlayerByID(packet.id);
			playerToUpdate.getInventory().remove(packet.item);
			
			BoardSquare sq = game.getRoomByName(packet.location.getRoom().getName()).getBoard().getSquareAt(packet.location.getY(), packet.location.getX());
			sq.setGameObjectOnSquare(packet.item);
			
			//server.sendToAllExceptTCP(packet.id, object);
		}
	}
	
	/**
	 * Checks whether the username is taken by an already connected player.
	 * 
	 * @param name The username to validate
	 * @return Returns true if the username is valid, otherwise return false
	 */
	public boolean isUsernameAlreadyTaken(String name) {
		// Assembles name in a packet to be sent to the server for verification
		ValidateNewPlayerUsername packet = new ValidateNewPlayerUsername();
		packet.name = name;
		
		client.updateReturnTripTime(); // Update the ping
		client.sendTCP(packet); // Send to packet to the server
		
		// Pause the client for a respectable amount of time (based on ping)
		// so the server has time to send a response
		try {
			Thread.sleep(client.getReturnTripTime() + 100);
		} catch (InterruptedException e) { }
		
		// Assuming a response is received, we get it read the response
		Object object = getRecievedServerReponses().pop();
			
		if(object instanceof ValidateNewPlayerUsername_Response) {
			// Cast the object with the packet response class 
			ValidateNewPlayerUsername_Response packet_recv = (ValidateNewPlayerUsername_Response) object;
			// Return the response from the server which will indicate if the name is valid or not
			return packet_recv.valid;
		}  else {
			 // DEAD CODE - Typically Should NEVER get here due to the nature of TCP (Always will receive packet)
			 throw new RuntimeException("I dont even know how the Client lost the packet!!!");
		}
		
	}
	
	/**
	 * Creates the new player and sends it to the server so the server can add the player
	 * into the global game on the server.
	 * 
	 * @param playerUsername the username the user inputs
	 * @param playerAvatar the avatar the user picks
	 */
	public void sendNewPlayerToServer(String playerUsername, Avatar playerAvatar) {
		// Create the Player with the variables supplied and the connection ID as well
		Player player = new Player(client.getID(), playerUsername, playerAvatar);
		
		// Assemble the newly created player in a packet
		ClientNewPlayer packet = new ClientNewPlayer();
		packet.player = player;
		
		// Finally send packet to server
		client.sendTCP(packet); 
	}
	
	/**
	 * Creates a new packet and gives it parameters based on the current player.
	 * It then sends the packet to the server
	 * 
	 * @param playerName the name corresponding to the current player
	 * @param choosenClientPlayer the player to be loaded
	 */
	public void sendLoadedPlayerToSever(String playerName, Player choosenClientPlayer) {
		//choosenClientPlayer.setName(playerUsername);
		//choosenClientPlayer.setId(client.getID());		
		
		ClientUseExistingPlayer packet = new ClientUseExistingPlayer();
		packet.oldId = choosenClientPlayer.getId();
		packet.oldName = choosenClientPlayer.getName();
		packet.newId = client.getID();
		packet.newName = playerName;
		
		// Finally send packet to server
		client.sendTCP(packet); 
	}
	
	/**
	 * Creates a new packet of the client's message and sends it the server
	 * 
	 * @param playerName name of player who sent the message
	 * @param message the message to be sent
	 */
	public void sendMessage(String playerName, String message) {
		ClientMessage packet = new ClientMessage();
		packet.playerName = playerName;
		packet.message = message;
		client.sendTCP(packet);
	}
	
	/**
	 * Creates a new packet with updated information about the player and sends it to the server
	 */
	public void sendPlayerUpdate() {
		PlayerUpdate packet = new PlayerUpdate();
		Player clientPlayer = getClientPlayer();
		packet.id = client.getID();
		packet.newPlayerLevel = clientPlayer.getPlayerLevel();
		packet.newAttack = clientPlayer.getAttack();
		packet.newHealth = clientPlayer.getHealth();
		
		client.sendTCP(packet);
	}
	
	/**
	 * Sends the Player's new Location and direction to the server, to update other
	 * client's screen.
	 */
	public void sendPlayerMoveUpdateToServer() {
		// Assemble packet containing the player id (player to update), and the location and direction
		PlayerUpdateLocationAndDirection packet = new PlayerUpdateLocationAndDirection();
		Player clientPlayer = getClientPlayer();
		packet.id = clientPlayer.getId();
		packet.newDirection = clientPlayer.getDirection();
		packet.newLocation = clientPlayer.getLocation();
		// Send the packet to the server for processing
		client.sendTCP(packet);
	}
	
	/**
	 * Sends empty packet to server to indicate the program is ready for character selection
	 * 
	 * @return arraylist of saved players
	 */	
	public ArrayList<Player> sendOnClientCharacterSelect() {	
		client.updateReturnTripTime(); // Update the ping
		client.sendTCP(new Packets.ClientOnChoosePlayer());// Send to packet to the server
		
		// Pause the client for a respectable amount of time (based on ping)
		// so the server has time to send a response
		try {
			Thread.sleep(client.getReturnTripTime() + 100);
		} catch (InterruptedException e) { 
		}
		
		// Assuming a response is received, we get it read the response
		Object object = getRecievedServerReponses().pop();
			
		if(object instanceof ClientOnChoosePlayer_Response) {
			// Cast the object with the packet response class 
			ClientOnChoosePlayer_Response packet_recv = (ClientOnChoosePlayer_Response) object;
			// Return the response from the server which will indicate if the name is valid or not
			return packet_recv.savedFilePlayers;
		}  else {
			 // DEAD CODE - Typically Should NEVER get here due to the nature of TCP (Always will receive packet)
			 throw new RuntimeException("I dont even know how the Client lost the packet!!!");
		}		
	}

	/**
	 * Handles the ClientNewGame packet, which will set the game or update the game of the client. 
	 * This is sent when a new player joins generally.
	 * 
	 * @param packet new packet created for a new client
	 */
	private void handleNewGamePacket(ClientNewGame packet) {
		byte[] gameBytes = packet.gameByteArray; // Disassemble the newGame packet and retrieve the btye array of the game
		game = Game.fromByteArray(gameBytes); // convert the btye array into an actual Game object that is usable
		
		// For an unknown reason, Players' Avatars don't load (possibly ImageIcon paths get corrupted) therefore we have
		// to re-assign the avatars to the player so their sprites load.
		
		// Retrieve all avatars
		List<Avatar> avatars = null;
		try {
			avatars = Avatar.getAllAvatars();
		} catch (FileNotFoundException e) {
		}
		
		// Assign each player the same avatar they had before
		for(Player connectedPlayer: game.getPlayers()) {
			// iterate their the all avatars to find the correct one for the specified player
			for(Avatar avatar: avatars) {
				// Compare the names of the avatars to determine if its the required avatar
				if(avatar.getName().equals(connectedPlayer.getAvatar().getName())) {
					connectedPlayer.setAvatar(avatar); 	//Finally set the avatar if its the same as the previously assigned one
				}
			}
		}
		gameClientListener.onGameUpdated(); // Tell external class (GUI) to update due to these changes
	}
	
	/**
	 * Handles changes in player movement from other clients
	 * 
	 * @param packet
	 */
	private void handlePlayerUpdateLocationAndDirection(PlayerUpdateLocationAndDirection packet) {
		// Find the player to update by comparing ID with the one contained in the packet
		for(Player connectedPlayer: game.getPlayers()) {
			if(connectedPlayer.getId() == packet.id) {
				// Update the player's movement locally
				connectedPlayer.setLocation(packet.newLocation);
				connectedPlayer.setDirection(packet.newDirection);
			}
		}	
	}	
	
	
	/**
	 * @return Returns a local copy of the Game world. Useful when drawing to frame or 
	 * handling game logic.
	 */
	public Game getGame() {
		return game;
	}
	
	/**
	 * @return the client player (which is the player the client controls)
	 */
	public Player getClientPlayer() {
		// Gets the client player by iterating through all connected players and find a player with the matching ID
		for(Player connectedPlayer: game.getPlayers()) {
			if(connectedPlayer.getId() == client.getID()) {
				return connectedPlayer;
			}
		}
		return null; // If the game hasn't loaded or client hasn't created their player, null is returned
	}
	
	/**
	 * @return gets responses from the server. Note this does not mean, everything received from
	 * the client will be placed in the stack, only specified response packets.
	 */
	public Stack<Object> getRecievedServerReponses() {
		return recievedServerReponses;
	}

	/**
	 * Used to cause redraws on GUI or tell some other external class that the
	 * game has updated and allows that third party to deal with it in their own
	 * way.
	 */
	public void updateClient() {
		// Only send a signal to the listener if the listener actually exists.
		if (gameClientListener != null) {
			gameClientListener.onGameUpdated();
		}
	}	

	/**
	 * Sets the GameClientListener. This the GameClientListener is used to tell
	 * an external class (usually the GUI) that it needs to be repainted. 
	 * 
	 * @param gameClientListener
	 */
	public void setGameClientListener(GameClientListener gameClientListener) {
		this.gameClientListener = gameClientListener;
	}

	/**
	 * Creates a new packet of item being picked up and sends it the server
	 * 
	 * @param item item being picked up
	 * @param location location of where item is being picked up from
	 * @param id id of current network
	 */
	public void sendPickupItem(Item item, Location location, int id) {
		PlayerPickUpItem packet = new PlayerPickUpItem();
		packet.id = client.getID();
		packet.item = (Item) item;
		packet.location = location;
		
		client.sendTCP(packet);
	}
	
	/**
	 * Creates new packet of item being dropped and sends it to the server
	 * 
	 * @param item being being dropped
	 * @param location of where item is being dropped
	 * @param id id of current network
	 */
	public void sendDropItem(Item item, Location location, int id) {
		PlayerDropItem packet = new PlayerDropItem();
		packet.id = client.getID();
		packet.item = (Item) item;
		packet.location = location;
		
		client.sendTCP(packet);
	}
}

