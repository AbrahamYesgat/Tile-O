/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.25.0-9e8af9e modeling language!*/

package ca.mcgill.ecse223.tileo.model;

import java.util.*;

import ca.mcgill.ecse223.tileo.application.TileOApplication;

import java.io.Serializable;

// line 3 "../../../../../main.ump"
public class TileO implements Serializable {
	private static final long serialVersionUID = -1402467009912279401L;
	// ------------------------
	// MEMBER VARIABLES
	// ------------------------

	// TileO Associations
	private List<Game> games;
	private Game currentGame;

	// ------------------------
	// CONSTRUCTOR
	// ------------------------

	public TileO() {
		games = new ArrayList<Game>();
	}

	// ------------------------
	// INTERFACE
	// ------------------------

	public Game getGame(int index) {
		Game aGame = games.get(index);
		return aGame;
	}

	public List<Game> getGames() {
		List<Game> newGames = Collections.unmodifiableList(games);
		return newGames;
	}

	public int numberOfGames() {
		int number = games.size();
		return number;
	}

	public boolean hasGames() {
		boolean has = games.size() > 0;
		return has;
	}

	public int indexOfGame(Game aGame) {
		int index = games.indexOf(aGame);
		return index;
	}
	
	public Game getGameByName(String gameName) {
		for(Game game : games) {
			if(game.getGameName().equals(gameName))
				return game;
		}
		
		return null;
	}

	public Game getCurrentGame() {
		return currentGame;
	}

	public boolean hasCurrentGame() {
		boolean has = currentGame != null;
		return has;
	}

	public static int minimumNumberOfGames() {
		return 0;
	}

	public boolean addGame(Game aGame) {
		boolean wasAdded = false;
		if (games.contains(aGame)) {
			return false;
		}
		
		games.add(aGame);
		
		wasAdded = true;
		return wasAdded;
	}

	public boolean removeGame(Game aGame) {
		boolean wasRemoved = false;
		// Unable to remove aGame, as it must always have a tileO
		if (games.contains(aGame)) {
			games.remove(aGame);
			wasRemoved = true;
		}
		return wasRemoved;
	}

	public boolean addGameAt(Game aGame, int index) {
		boolean wasAdded = false;
		if (addGame(aGame)) {
			if (index < 0) {
				index = 0;
			}
			if (index > numberOfGames()) {
				index = numberOfGames() - 1;
			}
			games.remove(aGame);
			games.add(index, aGame);
			wasAdded = true;
		}
		return wasAdded;
	}

	public boolean addOrMoveGameAt(Game aGame, int index) {
		boolean wasAdded = false;
		if (games.contains(aGame)) {
			if (index < 0) {
				index = 0;
			}
			if (index > numberOfGames()) {
				index = numberOfGames() - 1;
			}
			games.remove(aGame);
			games.add(index, aGame);
			wasAdded = true;
		} else {
			wasAdded = addGameAt(aGame, index);
		}
		return wasAdded;
	}

	public boolean setCurrentGame(Game aNewCurrentGame) {
		boolean wasSet = false;
		currentGame = aNewCurrentGame;
		wasSet = true;
		return wasSet;
	}

	public void delete() {
		games.clear();

		currentGame = null;
	}

}