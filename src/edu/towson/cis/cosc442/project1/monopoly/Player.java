package edu.towson.cis.cosc442.project1.monopoly;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class Player {
	// the key of colorGroups is the name of the color group.
	private Hashtable<String, Integer> colorGroups = new Hashtable<String, Integer>();
	private boolean inJail;
	private int money;
	private String name;

	private Cell position;
	private ArrayList<PropertyCell> properties = new ArrayList<PropertyCell>();
	private ArrayList<Cell> railroads = new ArrayList<Cell>();
	private ArrayList<Cell> utilities = new ArrayList<Cell>();

	public Player() {
		GameBoard gb = GameMaster.instance().getGameBoard();
		inJail = false;
		if (gb != null) {
			position = gb.queryCell("Go");
		}
	}

	/**
	 * @param property
	 * @param amount
	 */
	public void buyProperty(Cell property, int amount) {
		property.setTheOwner(this);
		if (property instanceof PropertyCell) {
			PropertyCell cell = (PropertyCell) property;
			properties.add(cell);
			colorGroups.put(
					cell.getColorGroup(),
					new Integer(getPropertyNumberForColor(cell.getColorGroup()) + 1));
		}
		if (property instanceof RailRoadCell) {
			railroads.add(property);
			colorGroups.put(
					RailRoadCell.COLOR_GROUP,
					new Integer(getPropertyNumberForColor(RailRoadCell.COLOR_GROUP) + 1));
		}
		if (property instanceof UtilityCell) {
			utilities.add(property);
			colorGroups.put(
					UtilityCell.COLOR_GROUP,
					new Integer(getPropertyNumberForColor(UtilityCell.COLOR_GROUP) + 1));
		}
		setMoney(getMoney() - amount);
	}

	/**
	 * @return boolean
	 */
	public boolean canBuyHouse() {
		return (getMonopolies().length != 0);
	}

	/**
	 * @param property
	 * @return boolean
	 */
	public boolean checkProperty(String property) {
		for (int i = 0; i < properties.size(); i++) {
			Cell cell = (Cell) properties.get(i);
			if (cell.getName().equals(property)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * @param player
	 */
	public void exchangeProperty(Player player) {
		for (int i = 0; i < getPropertyNumber(); i++) {
			PropertyCell cell = getProperty(i);
			cell.setTheOwner(player);
			if (player == null) {
				cell.setAvailable(true);
				cell.setNumHouses(0);
			} else {
				player.properties.add(cell);
				colorGroups.put(
						cell.getColorGroup(),
						new Integer(getPropertyNumberForColor(cell.getColorGroup()) + 1));
			}
		}
		properties.clear();
	}

	/**
	 * @return Cell[]
	 */
	public Cell[] getAllProperties() {
		ArrayList<Cell> list = new ArrayList<Cell>();
		list.addAll(properties);
		list.addAll(utilities);
		list.addAll(railroads);
		return (Cell[]) list.toArray(new Cell[list.size()]);
	}

	/**
	 * @return int
	 */
	public int getMoney() {
		return this.money;
	}

	/**
	 * @return String[]
	 */
	public String[] getMonopolies() {
		ArrayList<String> monopolies = new ArrayList<String>();
		Enumeration<String> colors = colorGroups.keys();
		while (colors.hasMoreElements()) {
			String color = (String) colors.nextElement();
			if (!(color.equals(RailRoadCell.COLOR_GROUP)) && !(color.equals(UtilityCell.COLOR_GROUP))) {
				Integer num = (Integer) colorGroups.get(color);
				GameBoard gameBoard = GameMaster.instance().getGameBoard();
				if (num.intValue() == gameBoard.getPropertyNumberForColor(color)) {
					monopolies.add(color);
				}
			}
		}
		return (String[]) monopolies.toArray(new String[monopolies.size()]);
	}

	/**
	 * @return String
	 */
	public String getName() {
		return name;
	}

	public void getOutOfJail() {
		money -= JailCell.BAIL;
		if (isBankrupt()) {
			money = 0;
			exchangeProperty(null);
		}
		inJail = false;
		GameMaster.instance().updateGUI();
	}

	/**
	 * @return Cell
	 */
	public Cell getPosition() {
		return this.position;
	}

	/**
	 * @param index
	 * @return PropertyCell
	 */
	public PropertyCell getProperty(int index) {
		return (PropertyCell) properties.get(index);
	}

	/**
	 * @return int
	 */
	public int getPropertyNumber() {
		return properties.size();
	}

	/**
	 * @param name
	 * @return int
	 */
	private int getPropertyNumberForColor(String name) {
		Integer number = (Integer) colorGroups.get(name);
		if (number != null) {
			return number.intValue();
		}
		return 0;
	}

	/**
	 * @return boolean
	 */
	public boolean isBankrupt() {
		return money <= 0;
	}

	/**
	 * @return boolean
	 */
	public boolean isInJail() {
		return inJail;
	}

	/**
	 * @return int
	 */
	public int numberOfRR() {
		return getPropertyNumberForColor(RailRoadCell.COLOR_GROUP);
	}

	/**
	 * @return int
	 */
	public int numberOfUtil() {
		return getPropertyNumberForColor(UtilityCell.COLOR_GROUP);
	}

	/**
	 * @param owner
	 * @param rentValue
	 */
	public void payRentTo(Player owner, int rentValue) {
		if (money < rentValue) {
			owner.money += money;
			money -= rentValue;
		} else {
			money -= rentValue;
			owner.money += rentValue;
		}
		if (isBankrupt()) {
			money = 0;
			exchangeProperty(owner);
		}
	}

	public void purchase() {
		if (getPosition().isAvailable()) {
			Cell c = getPosition();
			c.setAvailable(false);
			if (c instanceof PropertyCell) {
				PropertyCell cell = (PropertyCell) c;
				purchaseProperty(cell);
			}
			if (c instanceof RailRoadCell) {
				RailRoadCell cell = (RailRoadCell) c;
				purchaseRailRoad(cell);
			}
			if (c instanceof UtilityCell) {
				UtilityCell cell = (UtilityCell) c;
				purchaseUtility(cell);
			}
		}
	}

	/**
	 * @param selectedMonopoly
	 * @param houses
	 */
	public void purchaseHouse(String selectedMonopoly, int houses) {
		GameBoard gb = GameMaster.instance().getGameBoard();
		PropertyCell[] cells = gb.getPropertiesInMonopoly(selectedMonopoly);
		if ((money >= (cells.length * (cells[0].getHousePrice() * houses)))) {
			for (int i = 0; i < cells.length; i++) {
				int newNumber = cells[i].getNumHouses() + houses;
				if (newNumber <= 5) {
					cells[i].setNumHouses(newNumber);
					this.setMoney(money - (cells[i].getHousePrice() * houses));
					GameMaster.instance().updateGUI();
				}
			}
		}
	}

	/**
	 * @param cell
	 */
	private void purchaseProperty(PropertyCell cell) {
		buyProperty(cell, cell.getPrice());
	}

	/**
	 * @param cell
	 */
	private void purchaseRailRoad(RailRoadCell cell) {
		buyProperty(cell, cell.getPrice());
	}

	/**
	 * @param cell
	 */
	private void purchaseUtility(UtilityCell cell) {
		buyProperty(cell, cell.getPrice());
	}

	/**
	 * @param property
	 * @param amount
	 */
	public void sellProperty(Cell property, int amount) {
		property.setTheOwner(null);
		if (property instanceof PropertyCell) {
			properties.remove(property);
		}
		if (property instanceof RailRoadCell) {
			railroads.remove(property);
		}
		if (property instanceof UtilityCell) {
			utilities.remove(property);
		}
		setMoney(getMoney() + amount);
	}

	/**
	 * @param inJail
	 */
	public void setInJail(boolean inJail) {
		this.inJail = inJail;
	}

	/**
	 * @param money
	 */
	public void setMoney(int money) {
		this.money = money;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param newPosition
	 */
	public void setPosition(Cell newPosition) {
		this.position = newPosition;
	}

	/**
	 * @return String
	 */
	public String toString() {
		return name;
	}

	public void resetProperty() {
		properties = new ArrayList<PropertyCell>();
		railroads = new ArrayList<Cell>();
		utilities = new ArrayList<Cell>();
	}
}
