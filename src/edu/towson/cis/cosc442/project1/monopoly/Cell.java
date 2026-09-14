package edu.towson.cis.cosc442.project1.monopoly;

public abstract class Cell {
	private boolean available = true;
	private String name;
	protected Player theOwner;

	/**
	 * Returns the name of the cell.
	 * 
	 * @return the name of the cell
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the player who owns the cell.
	 * 
	 * @return the player who owns the cell, or null if unowned
	 */
	public Player getTheOwner() {
		return theOwner;
	}

	/**
	 * Returns the price of the cell, defaulting to 0.
	 * 
	 * @return the price of the cell
	 */
	public int getPrice() {
		return 0;
	}

	/**
	 * Indicates whether the cell is currently available.
	 * 
	 * @return true if the cell is available; false otherwise
	 */
	public boolean isAvailable() {
		return available;
	}

	/**
	 * Defines the action that occurs when the cell is played.
	 */
	public abstract void playAction();

	/**
	 * Sets the availability status of the cell.
	 * 
	 * @param available the new availability status to set
	 */
	public void setAvailable(boolean available) {
		this.available = available;
	}

	/**
	 * Sets the name of the cell.
	 * 
	 * @param name the new name to assign to the cell
	 */
	void setName(String name) {
		this.name = name;
	}

	/**
	 * Assigns the owner of the cell to a specified player.
	 * 
	 * @param owner the player to set as the owner
	 */
	public void setTheOwner(Player owner) {
		this.theOwner = owner;
	}

	/**
	 * Returns the string representation of the cell, which is its name.
	 * 
	 * @return the name of the cell as a string
	 */
	public String toString() {
		return name;
	}
}
