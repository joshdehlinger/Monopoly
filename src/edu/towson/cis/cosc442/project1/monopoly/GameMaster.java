package edu.towson.cis.cosc442.project1.monopoly;

import java.util.ArrayList;
import java.util.Iterator;

public class GameMaster {

	private static GameMaster gameMaster;
	static final public int MAX_PLAYER = 8;
	private Die[] dice;
	private GameBoard gameBoard;
	private MonopolyGUI gui;
	private int initAmountOfMoney;
	private ArrayList<Player> players = new ArrayList<Player>();
	private int turn = 0;
	private int utilDiceRoll;
	private boolean testMode;

	/**
	 * Returns the singleton instance of the GameMaster class.
	 * 
	 * @return the single GameMaster instance
	 */
	public static GameMaster instance() {
		if (gameMaster == null) {
			gameMaster = new GameMaster();
		}
		return gameMaster;
	}

	/**
	 * Initializes a new GameMaster with default initial money and two dice.
	 */
	public GameMaster() {
		initAmountOfMoney = 1500;
		dice = new Die[] { new Die(), new Die() };
	}

	/**
	 * Handles the event when the buy house button is clicked by showing a buy house
	 * dialog for the current player.
	 */
	public void btnBuyHouseClicked() {
		gui.showBuyHouseDialog(getCurrentPlayer());
	}

	/**
	 * Handles the draw card button click by drawing and applying a Community Chest
	 * or Chance card for the current player and updating the GUI buttons
	 * accordingly.
	 * 
	 * @return the drawn Card object
	 */
	public Card btnDrawCardClicked() {
		gui.setDrawCardEnabled(false);
		CardCell cell = (CardCell) getCurrentPlayer().getPosition();
		Card card = null;
		if (cell.getType() == Card.TYPE_CC) {
			card = getGameBoard().drawCCCard();
			card.applyAction();
		} else {
			card = getGameBoard().drawChanceCard();
			card.applyAction();
		}
		gui.setEndTurnEnabled(true);
		return card;
	}

	/**
	 * Handles the end turn button click by performing the current player's position
	 * action, managing bankruptcy, switching turn, and updating the GUI.
	 */
	public void btnEndTurnClicked() {
		setAllButtonEnabled(false);
		getCurrentPlayer().getPosition().playAction();
		if (getCurrentPlayer().isBankrupt()) {
			gui.setBuyHouseEnabled(false);
			gui.setDrawCardEnabled(false);
			gui.setEndTurnEnabled(false);
			gui.setGetOutOfJailEnabled(false);
			gui.setPurchasePropertyEnabled(false);
			gui.setRollDiceEnabled(false);
			gui.setTradeEnabled(getCurrentPlayerIndex(), false);
			updateGUI();
		} else {
			switchTurn();
			updateGUI();
		}
	}

	/**
	 * Processes the get out of jail button click to release the current player from
	 * jail and updates GUI button states based on bankruptcy and jail status.
	 */
	public void btnGetOutOfJailClicked() {
		getCurrentPlayer().getOutOfJail();
		if (getCurrentPlayer().isBankrupt()) {
			gui.setBuyHouseEnabled(false);
			gui.setDrawCardEnabled(false);
			gui.setEndTurnEnabled(false);
			gui.setGetOutOfJailEnabled(false);
			gui.setPurchasePropertyEnabled(false);
			gui.setRollDiceEnabled(false);
			gui.setTradeEnabled(getCurrentPlayerIndex(), false);
		} else {
			gui.setRollDiceEnabled(true);
			gui.setBuyHouseEnabled(getCurrentPlayer().canBuyHouse());
			gui.setGetOutOfJailEnabled(getCurrentPlayer().isInJail());
		}
	}

	/**
	 * Executes the current player's property purchase action, disables the purchase
	 * button, and updates the GUI.
	 */
	public void btnPurchasePropertyClicked() {
		Player player = getCurrentPlayer();
		player.purchase();
		gui.setPurchasePropertyEnabled(false);
		updateGUI();
	}

	/**
	 * Handles the roll dice button click by rolling the dice, moving the current
	 * player accordingly, and displaying the dice roll message.
	 */
	public void btnRollDiceClicked() {
		int[] rolls = rollDice();
		if ((rolls[0] + rolls[1]) > 0) {
			Player player = getCurrentPlayer();
			gui.setRollDiceEnabled(false);
			StringBuffer msg = new StringBuffer();
			msg.append(player.getName())
					.append(", you rolled ")
					.append(rolls[0])
					.append(" and ")
					.append(rolls[1]);
			gui.showMessage(msg.toString());
			movePlayer(player, rolls[0] + rolls[1]);
			gui.setBuyHouseEnabled(false);
		}
	}

	/**
	 * Initiates the trade dialog, processes trade deals, and updates the GUI if the
	 * trade is accepted.
	 */
	public void btnTradeClicked() {
		TradeDialog dialog = gui.openTradeDialog();
		TradeDeal deal = dialog.getTradeDeal();
		if (deal != null) {
			RespondDialog rDialog = gui.openRespondDialog(deal);
			if (rDialog.getResponse()) {
				completeTrade(deal);
				updateGUI();
			}
		}
	}

	/**
	 * Completes a trade between the current player and a seller based on the
	 * specified trade deal.
	 * 
	 * @param deal the TradeDeal object containing trade details
	 */
	public void completeTrade(TradeDeal deal) {
		Player seller = getPlayer(deal.getPlayerIndex());
		Cell property = gameBoard.queryCell(deal.getPropertyName());
		seller.sellProperty(property, deal.getAmount());
		getCurrentPlayer().buyProperty(property, deal.getAmount());
	}

	/**
	 * Draws a Community Chest card from the game board.
	 * 
	 * @return the drawn Community Chest card
	 */
	public Card drawCCCard() {
		return gameBoard.drawCCCard();
	}

	/**
	 * Draws a Chance card from the game board.
	 * 
	 * @return the drawn Chance card
	 */
	public Card drawChanceCard() {
		return gameBoard.drawChanceCard();
	}

	/**
	 * Gets the player whose turn it currently is.
	 * 
	 * @return the current Player object
	 */
	public Player getCurrentPlayer() {
		return getPlayer(turn);
	}

	/**
	 * Gets the index of the current player in the players list.
	 * 
	 * @return the current player's index
	 */
	public int getCurrentPlayerIndex() {
		return turn;
	}

	/**
	 * Returns the game board instance used in the game.
	 * 
	 * @return the GameBoard object
	 */
	public GameBoard getGameBoard() {
		return gameBoard;
	}

	/**
	 * Returns the GUI interface used for the game.
	 * 
	 * @return the MonopolyGUI object
	 */
	public MonopolyGUI getGUI() {
		return gui;
	}

	/**
	 * Gets the initial amount of money assigned to each player at game start.
	 * 
	 * @return the initial money amount
	 */
	public int getInitAmountOfMoney() {
		return initAmountOfMoney;
	}

	/**
	 * Returns the total number of players currently in the game.
	 * 
	 * @return the number of players
	 */
	public int getNumberOfPlayers() {
		return players.size();
	}

	/**
	 * Returns the count of players available to sell properties excluding the
	 * current player.
	 * 
	 * @return the number of sellers (players other than current)
	 */
	public int getNumberOfSellers() {
		return players.size() - 1;
	}

	/**
	 * Retrieves the player at the specified index.
	 * 
	 * @param index the index of the player to retrieve
	 * @return the Player object at the given index
	 */
	public Player getPlayer(int index) {
		return (Player) players.get(index);
	}

	/**
	 * Finds the index of the specified player in the players list.
	 * 
	 * @param player the Player object whose index is searched
	 * @return the index of the specified player, or -1 if not found
	 */
	public int getPlayerIndex(Player player) {
		return players.indexOf(player);
	}

	/**
	 * Gets a list of all players except the current player, representing sellers.
	 * 
	 * @return a list of Player objects excluding the current player
	 */
	public ArrayList<Player> getSellerList() {
		ArrayList<Player> sellers = new ArrayList<Player>();
		for (Iterator<Player> iter = players.iterator(); iter.hasNext();) {
			Player player = iter.next();
			if (player != getCurrentPlayer())
				sellers.add(player);
		}
		return sellers;
	}

	/**
	 * Returns the current turn index indicating which player's turn it is.
	 * 
	 * @return the current turn index
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * Retrieves the last utility dice roll value.
	 * 
	 * @return the utility dice roll integer value
	 */
	public int getUtilDiceRoll() {
		return this.utilDiceRoll;
	}

	/**
	 * Moves the player at the specified index forward by the given dice value and
	 * updates the game state and GUI accordingly.
	 * 
	 * @param playerIndex the index of the player to move
	 * @param diceValue   the number of spaces to move the player
	 */
	public void movePlayer(int playerIndex, int diceValue) {
		Player player = (Player) players.get(playerIndex);
		movePlayer(player, diceValue);
	}

	/**
	 * Moves the specified player forward by the given dice value, handles passing
	 * Go and updates the GUI.
	 * 
	 * @param player    the Player to move
	 * @param diceValue the number of spaces to move the player
	 */
	public void movePlayer(Player player, int diceValue) {
		Cell currentPosition = player.getPosition();
		int positionIndex = gameBoard.queryCellIndex(currentPosition.getName());
		int newIndex = (positionIndex + diceValue) % gameBoard.getCellNumber();
		if (newIndex <= positionIndex || diceValue > gameBoard.getCellNumber()) {
			player.setMoney(player.getMoney() + 200);
		}
		player.setPosition(gameBoard.getCell(newIndex));
		gui.movePlayer(getPlayerIndex(player), positionIndex, newIndex);
		playerMoved(player);
		updateGUI();
	}

	/**
	 * Performs game logic and GUI updates after a player has moved to a new
	 * position.
	 * 
	 * @param player the player who has just moved
	 */
	public void playerMoved(Player player) {
		Cell cell = player.getPosition();
		int playerIndex = getPlayerIndex(player);
		if (cell instanceof CardCell) {
			gui.setDrawCardEnabled(true);
		} else {
			if (cell.isAvailable()) {
				int price = cell.getPrice();
				if (price <= player.getMoney() && price > 0) {
					gui.enablePurchaseBtn(playerIndex);
				}
			}
			gui.enableEndTurnBtn(playerIndex);
		}
		gui.setTradeEnabled(turn, false);
	}

	/**
	 * Resets all players to the starting position and clears game board cards,
	 * preparing for a new game.
	 */
	public void reset() {
		for (int i = 0; i < getNumberOfPlayers(); i++) {
			Player player = (Player) players.get(i);
			player.setPosition(gameBoard.getCell(0));
		}
		if (gameBoard != null)
			gameBoard.removeCards();
		turn = 0;
	}

	/**
	 * Rolls two dice and returns their results, using test mode dice roll if
	 * enabled.
	 * 
	 * @return an integer array containing two dice roll results
	 */
	public int[] rollDice() {
		if (testMode) {
			return gui.getDiceRoll();
		} else {
			return new int[] {
					dice[0].getRoll(),
					dice[1].getRoll()
			};
		}
	}

	/**
	 * Sends the specified player directly to jail and updates their jail status and
	 * GUI position.
	 * 
	 * @param player the Player to send to jail
	 */
	public void sendToJail(Player player) {
		int oldPosition = gameBoard.queryCellIndex(getCurrentPlayer().getPosition().getName());
		player.setPosition(gameBoard.queryCell("Jail"));
		player.setInJail(true);
		int jailIndex = gameBoard.queryCellIndex("Jail");
		gui.movePlayer(
				getPlayerIndex(player),
				oldPosition,
				jailIndex);
	}

	/**
	 * Enables or disables all main game control buttons in the GUI based on the
	 * provided flag.
	 * 
	 * @param enabled true to enable buttons, false to disable
	 */
	private void setAllButtonEnabled(boolean enabled) {
		gui.setRollDiceEnabled(enabled);
		gui.setPurchasePropertyEnabled(enabled);
		gui.setEndTurnEnabled(enabled);
		gui.setTradeEnabled(turn, enabled);
		gui.setBuyHouseEnabled(enabled);
		gui.setDrawCardEnabled(enabled);
		gui.setGetOutOfJailEnabled(enabled);
	}

	/**
	 * Sets the game board to be used for this game session.
	 * 
	 * @param board the GameBoard instance to set
	 */
	public void setGameBoard(GameBoard board) {
		this.gameBoard = board;
	}

	/**
	 * Assigns the Monopoly GUI interface to be used by the game master.
	 * 
	 * @param gui the MonopolyGUI instance to set
	 */
	public void setGUI(MonopolyGUI gui) {
		this.gui = gui;
	}

	/**
	 * Sets the initial money amount each player receives at the game start.
	 * 
	 * @param money the amount of money to assign to each player initially
	 */
	public void setInitAmountOfMoney(int money) {
		this.initAmountOfMoney = money;
	}

	/**
	 * Sets the number of players for the game and initializes their money to the
	 * starting amount.
	 * 
	 * @param number the number of players to initialize
	 */
	public void setNumberOfPlayers(int number) {
		players.clear();
		for (int i = 0; i < number; i++) {
			Player player = new Player();
			player.setMoney(initAmountOfMoney);
			players.add(player);
		}
	}

	/**
	 * Sets the utility dice roll value used for specific game mechanics.
	 * 
	 * @param diceRoll the utility dice roll value to set
	 */
	public void setUtilDiceRoll(int diceRoll) {
		this.utilDiceRoll = diceRoll;
	}

	/**
	 * Starts the game by initializing the GUI and enabling the first player's turn
	 * and trade options.
	 */
	public void startGame() {
		gui.startGame();
		gui.enablePlayerTurn(0);
		gui.setTradeEnabled(0, true);
	}

	/**
	 * Advances the game turn to the next player and updates GUI controls based on
	 * jail status and buying ability.
	 */
	public void switchTurn() {
		turn = (turn + 1) % getNumberOfPlayers();
		if (!getCurrentPlayer().isInJail()) {
			gui.enablePlayerTurn(turn);
			gui.setBuyHouseEnabled(getCurrentPlayer().canBuyHouse());
			gui.setTradeEnabled(turn, true);
		} else {
			gui.setGetOutOfJailEnabled(true);
		}
	}

	/**
	 * Refreshes the game GUI to reflect the current game state.
	 */
	public void updateGUI() {
		gui.update();
	}

	/**
	 * Prompts the GUI to show the utility dice roll and stores the result for game
	 * use.
	 */
	public void utilRollDice() {
		this.utilDiceRoll = gui.showUtilDiceRoll();
	}

	/**
	 * Enables or disables test mode, affecting how dice rolls are generated during
	 * the game.
	 * 
	 * @param b true to enable test mode, false to disable
	 */
	public void setTestMode(boolean b) {
		testMode = b;
	}
}
