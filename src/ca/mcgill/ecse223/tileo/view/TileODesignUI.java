package ca.mcgill.ecse223.tileo.view;

import ca.mcgill.ecse223.tileo.controller.*;
import ca.mcgill.ecse223.tileo.model.*;
import ca.mcgill.ecse223.tileo.application.TileOApplication;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class TileODesignUI extends javax.swing.JFrame {
	private static final long serialVersionUID = -4784304605398643427L;

	private DesignController currentController;

	private LinkedList<TileUI> tilesButtons;
	private LinkedList<ConnectionUI> connectionButtons;
	private JPanel tilesPanel;

	private enum DesignState {
		BOARD_SIZE, CHANGE_NUMBER_OF_PLAYERS, SELECT_STARTING_POSITION, ADD_TILE, REMOVE_TILE, ADD_CONNECTION, REMOVE_CONNECTION, NONE
	}

	private DesignState designState = DesignState.NONE;

	private int numberOfRows = 0;
	private int numberOfCols = 0;

	/**
	 * Creates new form TileOUGUI
	 */
	public TileODesignUI(Game aGame) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			//Too bad
		}
		
		game = aGame;
		currentController = new DesignController(game);

		//Init layout
		initComponents();
	}

	private int getNumberOfCardsLeft() {
		return 32 - (Integer.valueOf(nbRollDieCard.getText()) + Integer.valueOf(nbRemoveConnectionCard.getText())
				+ Integer.valueOf(nbTeleportCard.getText()) + Integer.valueOf(nbLoseTurnCard.getText())
				+ Integer.valueOf(nbConnectTilesCard.getText()));
	}

	private void showDisabledTiles() {
		for (TileUI button : tilesButtons) {
			if (!button.isVisible()) {
				button.setVisible(true);
			}
		}
	}

	private void hideDisabledTiles() {
		for (TileUI button : tilesButtons) {
			if (button.isSelected()) {
				button.setVisible(false);
			}
		}
	}

	private void showDisabledConnections() {
		for (ConnectionUI button : connectionButtons) {
			if (!button.isVisible()) {
				button.setVisible(true);
			}
		}
	}

	private void hideDisabledConnections() {
		for (ConnectionUI button : connectionButtons) {
			if (button.isSelected()) {
				button.setVisible(false);
			}
		}
	}

	private void changeNumberOfCardsLeft() {
		int nbOfCardsLeft = getNumberOfCardsLeft();

		if (nbOfCardsLeft < 0) {
			cardsLeft.setForeground(new java.awt.Color(255, 0, 0));
			applyChangesButton.setEnabled(false);
		} else {
			cardsLeft.setForeground(new java.awt.Color(0, 0, 0));
			applyChangesButton.setEnabled(true);
		}

		cardsLeft.setText(String.valueOf(nbOfCardsLeft));
	}

	private void changeBoardSize(int m, int n) {
		if (numberOfRows == m && numberOfCols == n)
			return;

		numberOfRows = m;
		numberOfCols = n;

		// Clear
		tilesButtons.clear();
		connectionButtons.clear();
		tilesPanel.removeAll();

		// Create buttons and put in linked list
		for (int i = 0; i < m + (m - 1); i++) {
			for (int j = 0; j < n + (n - 1); j++) {
				// Tile
				if (i % 2 == 0 && j % 2 == 0) {
					TileUI tile = new TileUI();
					tile.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							tileActionPerformed(evt);
						}
					});
					tilesButtons.add(tile);
					currentController.createNormalTile(i/2, j/2);
				}

				// Horizontal connection
				else if (i % 2 == 1 && j % 2 == 0) {
					ConnectionUI conn = new ConnectionUI(ConnectionUI.Type.HORIZONTAL);
					conn.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							connectionActionPerformed(evt);
						}
					});
					connectionButtons.add(conn);
				}

				// Vertical connection
				else if (i % 2 == 0 && j % 2 == 1) {
					ConnectionUI conn = new ConnectionUI(ConnectionUI.Type.VERTICAL);
					conn.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							connectionActionPerformed(evt);
						}
					});
					connectionButtons.add(conn);
				}

			}
		}

		// Create grids
		tilesPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		ListIterator<TileUI> tiles_it = tilesButtons.listIterator();
		ListIterator<ConnectionUI> conn_it = connectionButtons.listIterator();

		for (int row = 0; row < m + (m - 1); row++) {
			for (int col = 0; col < n + (n - 1); col++) {
				c.fill = GridBagConstraints.BOTH;
				c.gridx = row;
				c.gridy = col;

				// Tile
				if (row % 2 == 0 && col % 2 == 0) {
					TileUI next = tiles_it.next();
					tilesPanel.add(next, c);
					next.setPosition(row/2, col/2);
					//TODO : Create tile in game
				}

				// Horizontal connection
				else if (row % 2 == 1 && col % 2 == 0) {
					ConnectionUI next = conn_it.next();
					c.fill = GridBagConstraints.HORIZONTAL;
					tilesPanel.add(next, c);
					next.setPosition(row,col);
				}

				// Vertical connection
				else if (row % 2 == 0 && col % 2 == 1) {
					ConnectionUI next = conn_it.next();
					c.fill = GridBagConstraints.VERTICAL;
					tilesPanel.add(next, c);
					next.setPosition(row,col);
				}

				// Gap
				else {
					JPanel gap = new JPanel();
					gap.setPreferredSize(new java.awt.Dimension(10, 10));
					tilesPanel.add(gap, c);
				}
			}
		}
	}

	private void backupLists() {
		for (TileUI tile : tilesButtons) {
			tile.saveUIState();
		}

		for (ConnectionUI conn : connectionButtons) {
			conn.saveUIState();
		}
	}

	private void restoreLists() {
		for (TileUI tile : tilesButtons) {
			tile.restoreUIState();
		}

		for (ConnectionUI conn : connectionButtons) {
			conn.restoreUIState();
		}
	}

	private void resetUI() {
		restoreLists();

		applyChangesButtonActionPerformed(new java.awt.event.ActionEvent(new Object(), 0, ""));
	}

	//Setup new board
	private void setupBoard() {
		tilesButtons = new LinkedList<TileUI>();
		connectionButtons = new LinkedList<ConnectionUI>();

		tilesPanel = new javax.swing.JPanel();
		tilesPanel.setPreferredSize(new java.awt.Dimension(1130, 680));

		changeBoardSize(Integer.valueOf(horizontalLength.getSelectedItem().toString()), Integer.valueOf(verticalLength.getSelectedItem().toString()));
	}
	
	private void initComponents() {
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jLabel7 = new javax.swing.JLabel();
		jLabel8 = new javax.swing.JLabel();
		jLabel9 = new javax.swing.JLabel();
		jLabel10 = new javax.swing.JLabel();
		applyChangesButton = new javax.swing.JToggleButton();
		chosenPlayer = new javax.swing.JComboBox<>();
		removeTileButton = new javax.swing.JToggleButton();
		tileType = new javax.swing.JComboBox<>();
		addTileButton = new javax.swing.JToggleButton();
		selectPositionButton = new javax.swing.JToggleButton();
		removeConnectionButton = new javax.swing.JToggleButton();
		addConnectionButton = new javax.swing.JToggleButton();
		jLabel11 = new javax.swing.JLabel();
		jLabel12 = new javax.swing.JLabel();
		jLabel13 = new javax.swing.JLabel();
		jLabel14 = new javax.swing.JLabel();
		jLabel15 = new javax.swing.JLabel();
		jLabel16 = new javax.swing.JLabel();
		nbRollDieCard = new javax.swing.JTextField();
		nbRemoveConnectionCard = new javax.swing.JTextField();
		nbTeleportCard = new javax.swing.JTextField();
		nbLoseTurnCard = new javax.swing.JTextField();
		nbConnectTilesCard = new javax.swing.JTextField();
		saveButton = new javax.swing.JButton();
		nbOfPlayers = new javax.swing.JComboBox<>();
		loadButton = new javax.swing.JButton();
		jLabel17 = new javax.swing.JLabel();
		backButton = new javax.swing.JButton();
		cardsLeft = new javax.swing.JLabel();
		horizontalLength = new javax.swing.JComboBox<>();
		verticalLength = new javax.swing.JComboBox<>();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setBackground(new java.awt.Color(204, 255, 255));

		jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
		jLabel1.setText("Board:");

		jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
		jLabel2.setText("Player:");

		jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
		jLabel3.setText("Tile:");

		jLabel4.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
		jLabel4.setText("Connection:");

		jLabel5.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
		jLabel5.setText("Deck:");

		jLabel6.setText("Enter dimensions");

		jLabel7.setText("Enter number of players");

		jLabel8.setText("Select player");

		jLabel9.setText("Change tile type");

		jLabel10.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
		jLabel10.setText("x");

		applyChangesButton.setBackground(new java.awt.Color(0, 204, 0));
		applyChangesButton.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
		applyChangesButton.setForeground(new java.awt.Color(255, 255, 255));
		applyChangesButton.setText("Apply changes");
		applyChangesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyChangesButtonActionPerformed(evt);
				designState = DesignState.NONE;
			}
		});

		chosenPlayer.setBackground(new java.awt.Color(204, 204, 255));
		chosenPlayer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2" }));

		removeTileButton.setBackground(new java.awt.Color(153, 153, 255));
		removeTileButton.setText("Remove Tile");
		removeTileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(removeTileButton.isSelected()) {
					designState = DesignState.REMOVE_TILE;
					tileType.setEnabled(false);
					removeTileButtonChanged();
				} else {
					tileType.setEnabled(true);
					resetUI();
					designState = DesignState.NONE;
				}
			}
		});

		tileType.setModel(
				new javax.swing.DefaultComboBoxModel<>(new String[] { "Regular Tile", "Action Tile", "Win Tile" }));

		addTileButton.setBackground(new java.awt.Color(153, 153, 255));
		addTileButton.setText("Add Tile");
		addTileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(addTileButton.isSelected()) {
					designState = DesignState.ADD_TILE;
					addTileButtonChanged();
				} else {
					resetUI();
					designState = DesignState.NONE;
				}
			}
		});

		selectPositionButton.setBackground(new java.awt.Color(153, 153, 255));
		selectPositionButton.setText("Select Start Position");
		selectPositionButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(selectPositionButton.isSelected()) {
					designState = DesignState.SELECT_STARTING_POSITION;
					selectPositionButtonChanged();
				} else {
					resetUI();
					designState = DesignState.NONE;
				}
			}
		});

		removeConnectionButton.setBackground(new java.awt.Color(153, 153, 255));
		removeConnectionButton.setText("Remove Connection");
		removeConnectionButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(removeConnectionButton.isSelected()) {
					designState = DesignState.REMOVE_CONNECTION;
					removeConnectionButtonChanged();
				} else {
					resetUI();
					designState = DesignState.NONE;
				}
			}
		});

		addConnectionButton.setBackground(new java.awt.Color(153, 153, 255));
		addConnectionButton.setText("Add Connection");
		addConnectionButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(addConnectionButton.isSelected()) {
					designState = DesignState.ADD_CONNECTION;
					addConnectionButtonChanged();
				} else {
					resetUI();
					designState = DesignState.NONE;
				}
			}
		});

		jLabel11.setText("Cards left :");

		jLabel12.setText("Roll Die");

		jLabel13.setText("Remove Connection");

		jLabel14.setText("Teleport");

		jLabel15.setText("Lose Turn");

		jLabel16.setText("Connect Tiles");

		nbRollDieCard.setText("0");
		nbRollDieCard.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nbRollDieCardChanged();
			}
		});
		nbRollDieCard.addFocusListener(new FocusListener() {
			 public void focusGained(FocusEvent e) {
				 
			 }
		      public void focusLost(FocusEvent e) {
		    	  if (Integer.valueOf(nbRollDieCard.getText()) < 0)
		  			nbRollDieCard.setText("0");

		  		changeNumberOfCardsLeft();
		      }
		});

		nbRemoveConnectionCard.setText("0");
		nbRemoveConnectionCard.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nbRemoveConnectionCardChanged();
			}
		});
		nbRemoveConnectionCard.addFocusListener(new FocusListener() {
			 public void focusGained(FocusEvent e) {
				 
			 }
		      public void focusLost(FocusEvent e) {
		    	  if (Integer.valueOf(nbRemoveConnectionCard.getText()) < 0)
		    		  nbRemoveConnectionCard.setText("0");

		  		changeNumberOfCardsLeft();
		      }
		});

		nbTeleportCard.setText("0");
		nbTeleportCard.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nbTeleportCardChanged();
			}
		});
		nbTeleportCard.addFocusListener(new FocusListener() {
			 public void focusGained(FocusEvent e) {
				 
			 }
		      public void focusLost(FocusEvent e) {
		    	  if (Integer.valueOf(nbTeleportCard.getText()) < 0)
		    		  nbTeleportCard.setText("0");

		  		changeNumberOfCardsLeft();
		      }
		});

		nbLoseTurnCard.setText("0");
		nbLoseTurnCard.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nbLoseTurnCardChanged();
			}
		});
		nbLoseTurnCard.addFocusListener(new FocusListener() {
			 public void focusGained(FocusEvent e) {
				 
			 }
		      public void focusLost(FocusEvent e) {
		    	  if (Integer.valueOf(nbLoseTurnCard.getText()) < 0)
		    		  nbLoseTurnCard.setText("0");

		  		changeNumberOfCardsLeft();
		      }
		});

		nbConnectTilesCard.setText("0");
		nbConnectTilesCard.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nbConnectTilesCardChanged();
			}
		});
		nbConnectTilesCard.addFocusListener(new FocusListener() {
			 public void focusGained(FocusEvent e) {
				 
			 }
		      public void focusLost(FocusEvent e) {
		    	  if (Integer.valueOf(nbConnectTilesCard.getText()) < 0)
		    		  nbConnectTilesCard.setText("0");

		  		changeNumberOfCardsLeft();
		      }
		});

		saveButton.setBackground(new java.awt.Color(0, 0, 255));
		saveButton.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
		saveButton.setForeground(new java.awt.Color(255, 255, 255));
		saveButton.setText("Load");
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveButtonActionPerformed(evt);
			}
		});

		nbOfPlayers.setBackground(new java.awt.Color(204, 204, 255));
		nbOfPlayers.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2", "3", "4" }));
		nbOfPlayers.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(Integer.valueOf(String.valueOf(nbOfPlayers.getSelectedItem())) != game.numberOfPlayers()) {
					designState = DesignState.CHANGE_NUMBER_OF_PLAYERS;
					nbOfPlayersChanged();
				} else {
					resetUI();
					designState = DesignState.NONE;
				}
			}
		});

		loadButton.setBackground(new java.awt.Color(0, 0, 255));
		loadButton.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
		loadButton.setForeground(new java.awt.Color(255, 255, 255));
		loadButton.setText("Save");
		loadButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				loadButtonActionPerformed(evt);
			}
		});

		jLabel17.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
		jLabel17.setText("TileO Design Mode");

		backButton.setBackground(new java.awt.Color(255, 0, 0));
		backButton.setFont(new java.awt.Font("Lucida Grande", 3, 13)); // NOI18N
		backButton.setForeground(new java.awt.Color(255, 255, 255));
		backButton.setText("Back");
		backButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				backButtonActionPerformed(evt);
			}
		});

		cardsLeft.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
		cardsLeft.setText("32");

		horizontalLength.setModel(new javax.swing.DefaultComboBoxModel<>(
				new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
		horizontalLength.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				horizontalLengthActionPerformed(evt);
			}
		});
		horizontalLength.setSelectedIndex(8 - 2);

		verticalLength.setModel(new javax.swing.DefaultComboBoxModel<>(
				new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
		verticalLength.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				verticalLengthActionPerformed(evt);
			}
		});
		verticalLength.setSelectedIndex(8 - 2);

		// Window

		setResizable(false);

		// Board
		setupBoard();
		//
		GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
						.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
								.createSequentialGroup().addGroup(layout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
												.createSequentialGroup().addComponent(jLabel11).addGap(6, 6, 6)
												.addComponent(cardsLeft).addGap(52, 52, 52)
												.addComponent(nbRollDieCard, javax.swing.GroupLayout.PREFERRED_SIZE,
														33, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39,
														Short.MAX_VALUE))
										.addGroup(layout.createSequentialGroup().addComponent(jLabel5)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
														javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(jLabel12).addGap(31, 31, 31)))
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(layout.createSequentialGroup().addGap(18, 18, 18)
												.addComponent(jLabel13))
										.addGroup(layout.createSequentialGroup().addGap(57, 57, 57).addComponent(
												nbRemoveConnectionCard, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
												javax.swing.GroupLayout.PREFERRED_SIZE)))
								.addGap(36, 36, 36)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jLabel14)
										.addGroup(layout.createSequentialGroup().addGap(6, 6, 6).addComponent(
												nbTeleportCard, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
												javax.swing.GroupLayout.PREFERRED_SIZE)))
								.addGap(50, 50, 50)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(layout.createSequentialGroup().addGap(6, 6, 6)
												.addComponent(nbLoseTurnCard, javax.swing.GroupLayout.PREFERRED_SIZE,
														33, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGap(87, 87, 87).addComponent(nbConnectTilesCard,
														javax.swing.GroupLayout.PREFERRED_SIZE, 33,
														javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(layout.createSequentialGroup().addComponent(jLabel15)
												.addGap(43, 43, 43).addComponent(jLabel16)))
								.addGap(46, 46, 46))
								.addGroup(layout.createSequentialGroup()
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(jLabel1).addComponent(jLabel2).addComponent(jLabel3))
										.addGap(61, 61, 61)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(layout.createSequentialGroup().addGap(100, 100, 100)
														.addComponent(addConnectionButton,
																javax.swing.GroupLayout.PREFERRED_SIZE, 153,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGroup(layout
																.createParallelGroup(
																		javax.swing.GroupLayout.Alignment.LEADING)
																.addGroup(layout.createSequentialGroup()
																		.addGap(36, 36, 36)
																		.addComponent(removeConnectionButton,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				153,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(0, 0, Short.MAX_VALUE))
																.addGroup(layout.createSequentialGroup()
																		.addGap(18, 18, 18).addComponent(jLabel8)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(chosenPlayer,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18, 18, 18)
																		.addComponent(selectPositionButton,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				191, Short.MAX_VALUE))))
												.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout
														.createSequentialGroup().addComponent(jLabel9)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(tileType, javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(51, 51, 51)
														.addComponent(addTileButton,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(removeTileButton,
																javax.swing.GroupLayout.PREFERRED_SIZE, 153,
																javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGroup(layout.createSequentialGroup().addGroup(layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(layout.createSequentialGroup().addComponent(jLabel6)
																.addGap(61, 61, 61)
																.addComponent(horizontalLength,
																		javax.swing.GroupLayout.PREFERRED_SIZE, 60,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(jLabel10)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(verticalLength,
																		javax.swing.GroupLayout.PREFERRED_SIZE, 60,
																		javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(layout.createSequentialGroup().addComponent(jLabel7)
																.addGap(18, 18, 18).addComponent(nbOfPlayers,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE)))
														.addGap(0, 0, Short.MAX_VALUE))))
								.addGroup(layout.createSequentialGroup().addComponent(jLabel4).addGap(0, 0,
										Short.MAX_VALUE)))
						.addGap(18, 18, 18)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(applyChangesButton, javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
								.addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGap(26, 26, 26))
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addGap(16, 16, 16).addComponent(backButton)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(jLabel17).addGap(376, 376, 376))
						// .addGap(120, 120, 120))
						.addComponent(tilesPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(7, 7, 7)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel17).addComponent(backButton))
						.addGap(18, 18, 18)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel1).addComponent(jLabel6).addComponent(jLabel10).addComponent(
										horizontalLength, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(verticalLength, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(14, 14, 14)
						.addGroup(layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2)
								.addComponent(jLabel7).addComponent(jLabel8)
								.addComponent(chosenPlayer, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(
										selectPositionButton)
								.addComponent(nbOfPlayers, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup().addGap(13, 13, 13)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel3).addComponent(jLabel9)
												.addComponent(tileType, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(addTileButton).addComponent(removeTileButton))
										.addGap(14, 14, 14)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel4).addComponent(removeConnectionButton)
												.addComponent(addConnectionButton)))
								.addGroup(layout.createSequentialGroup()
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(loadButton).addGap(13, 13, 13).addComponent(saveButton)))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
										.addComponent(jLabel5)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel11).addComponent(applyChangesButton)
														.addComponent(cardsLeft)))
								.addGroup(layout
										.createSequentialGroup()
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel12).addComponent(jLabel13).addComponent(jLabel14)
												.addComponent(jLabel15).addComponent(jLabel16))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(nbRollDieCard, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(nbRemoveConnectionCard,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(nbTeleportCard, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(nbLoseTurnCard, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(nbConnectTilesCard,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(tilesPanel,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)));

		pack();
	}// </editor-fold>

	private void applyChangesButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// Change board size
		if (designState == DesignState.BOARD_SIZE) {
			changeBoardSize(Integer.valueOf(horizontalLength.getSelectedItem().toString()),
					Integer.valueOf(verticalLength.getSelectedItem().toString()));
		}

		// Select Player
		if (designState == DesignState.SELECT_STARTING_POSITION) {
			int playerNumber = Integer.valueOf(chosenPlayer.getSelectedItem().toString());

			if (playerNumber == 1) {
				for (JToggleButton button : tilesButtons) {
					if (button.getBackground().equals(new java.awt.Color(240, 10, 10))) {
						button.setBackground(null);
					}
					if (button.isSelected()) {
						button.setBackground(new java.awt.Color(240, 10, 10));
						button.setText("");
						button.setSelected(false);
					}
				}
			}

			if (playerNumber == 2) {
				for (JToggleButton button : tilesButtons) {
					if (button.getBackground().equals(new java.awt.Color(10, 10, 240))) {
						button.setBackground(null);
					}
					if (button.isSelected()) {
						button.setBackground(new java.awt.Color(10, 10, 240));
						button.setText("");
						button.setSelected(false);

					}
				}
			}

			if (playerNumber == 3) {
				for (JToggleButton button : tilesButtons) {
					if (button.getBackground().equals(new java.awt.Color(10, 240, 10))) {
						button.setBackground(null);
					}
					if (button.isSelected()) {
						button.setBackground(new java.awt.Color(10, 240, 10));
						button.setText("");
						button.setSelected(false);
					}
				}
			}

			if (playerNumber == 4) {
				for (JToggleButton button : tilesButtons) {
					if (button.getBackground().equals(new java.awt.Color(240, 240, 10))) {
						button.setBackground(null);
					}
					if (button.isSelected()) {
						button.setBackground(new java.awt.Color(240, 240, 10));
						button.setText("");
						button.setSelected(false);
					}
				}
			}
		}

		// Enable all buttons
		selectPositionButton.setEnabled(true);
		addTileButton.setEnabled(true);
		removeTileButton.setEnabled(true);
		removeConnectionButton.setEnabled(true);
		addConnectionButton.setEnabled(true);
		selectPositionButton.setSelected(false);
		addTileButton.setSelected(false);
		removeTileButton.setSelected(false);
		removeConnectionButton.setSelected(false);
		addConnectionButton.setSelected(false);

		// Remove Tiles
		if (designState == DesignState.REMOVE_TILE) {
			int i = 0;
			for (JToggleButton button : tilesButtons) {
				if (button.isSelected()) {
					button.setVisible(false);
					button.setText("");
					button.setBackground(null);
					currentController.deleteTile((i / numberOfRows) * 2, (i % numberOfRows) * 2);
				}
				i++;
			}
		}

		// Remove Connections
		if (designState == DesignState.REMOVE_CONNECTION) {
			int i = 0;
			for (ConnectionUI button : connectionButtons) {
				if (button.isSelected()) {
					button.setVisible(false);

					currentController.removeConnection(button.getUIX(), button.getUIY());
				}
				i++;
			}

		}

		if (designState == DesignState.ADD_TILE) {
			int i = 0;
			for (JToggleButton tile : tilesButtons) {
				// Add Win Tile
				if (tileType.getSelectedItem().toString().equals("Win Tile")) {
					if (tile.getBackground().equals(Color.pink)) {
						tile.setBackground(null);
						tile.setText("");
					}
					if (tile.isSelected() && tile.isVisible()) {
						tile.setText("W");
						tile.setBackground(Color.pink);
						currentController.createWinTile((i / numberOfRows) * 2, (i % numberOfRows) * 2);
					}
				}

				// Add Action Tile
				if (tileType.getSelectedItem().toString().equals("Action Tile")) {
					if (tile.isSelected() && tile.isVisible()) {
						tile.setText("A");
						tile.setBackground(Color.magenta);
						currentController.createActionTile((i / numberOfRows) * 2, (i % numberOfRows) * 2);
					}
				}
				i++;
			}

			// Reset Tiles
			// Normal Tile
			if (tileType.getSelectedItem().toString().equals("Regular Tile")) {
				hideDisabledTiles();
			}

		}

		// Reset Connections
		if (designState == DesignState.ADD_CONNECTION) {
			hideDisabledConnections();

			int i = 0;
			for (ConnectionUI conn : connectionButtons) {
				if (conn.isVisible()) {
					if ((conn.getUIY() % 2) == 0)
						currentController.connectTiles(conn.getUIX(), conn.getUIY(), true);
					else
						currentController.connectTiles(conn.getUIX(), conn.getUIY(), false);
				}
				i++;
			}
		}

		// Change colors for connections
		for (JToggleButton button : connectionButtons) {
			if (button.isSelected())
				button.setBackground(null);
			else
				button.setBackground(new java.awt.Color(0, 0, 0));
		}

		if (designState == DesignState.ADD_TILE) {
			// Add Action Tile
			if (tileType.getSelectedItem().toString().equals("Action Tile")
					|| tileType.getSelectedItem().toString().equals("Win Tile")) {
				for (JToggleButton tile : tilesButtons) {
					if (tile.isVisible() && tile.isSelected()) {
						tile.setSelected(false);
					}
				}
			}
		}

		// Reset button
		applyChangesButton.setSelected(false);
		tileType.setEnabled(true);
		designState = DesignState.NONE;

		// Repaint GUI
		repaint();
		revalidate();
	}

	private void removeTileButtonChanged() {
			backupLists();

			// Change buttons
			selectPositionButton.setEnabled(false);
			addTileButton.setEnabled(false);
			removeTileButton.setEnabled(true);
			removeConnectionButton.setEnabled(false);
			addConnectionButton.setEnabled(false);
	}

	private void addTileButtonChanged() {
			backupLists();

			// Change buttons
			selectPositionButton.setEnabled(false);
			addTileButton.setEnabled(true);
			removeTileButton.setEnabled(false);
			removeConnectionButton.setEnabled(false);
			addConnectionButton.setEnabled(false);

			if (tileType.getSelectedItem().toString().equals("Regular Tile")) {
				showDisabledTiles();
			}
	}

	private void selectPositionButtonChanged() {
			backupLists();

			// Change buttons
			selectPositionButton.setEnabled(true);
			addTileButton.setEnabled(false);
			removeTileButton.setEnabled(false);
			removeConnectionButton.setEnabled(false);
			addConnectionButton.setEnabled(false);
	}

	private void removeConnectionButtonChanged() {
			backupLists();

			// Change buttons
			selectPositionButton.setEnabled(false);
			addTileButton.setEnabled(false);
			removeTileButton.setEnabled(false);
			removeConnectionButton.setEnabled(true);
			addConnectionButton.setEnabled(false);

			// Change colors for connections
			for (JToggleButton button : connectionButtons) {
				button.setBackground(null);
			}
	}

	private void addConnectionButtonChanged() {
			backupLists();

			// Change buttons
			selectPositionButton.setEnabled(false);
			addTileButton.setEnabled(false);
			removeTileButton.setEnabled(false);
			removeConnectionButton.setEnabled(false);
			addConnectionButton.setEnabled(true);

			showDisabledConnections();
	}

	private void nbRollDieCardChanged() {
		// Change cards left
		if (Integer.valueOf(nbRollDieCard.getText()) < 0)
			nbRollDieCard.setText("0");

		changeNumberOfCardsLeft();
	}

	private void nbRemoveConnectionCardChanged() {
		// Change cards left
		if (Integer.valueOf(nbRemoveConnectionCard.getText()) < 0)
			nbRemoveConnectionCard.setText("0");

		changeNumberOfCardsLeft();
	}

	private void nbTeleportCardChanged() {
		// Change cards left
		if (Integer.valueOf(nbTeleportCard.getText()) < 0)
			nbTeleportCard.setText("0");

		changeNumberOfCardsLeft();
	}

	private void nbLoseTurnCardChanged() {
		// Change cards left
		if (Integer.valueOf(nbLoseTurnCard.getText()) < 0)
			nbLoseTurnCard.setText("0");

		changeNumberOfCardsLeft();
	}

	private void nbConnectTilesCardChanged() {
		// Change cards left
		if (Integer.valueOf(nbConnectTilesCard.getText()) < 0)
			nbConnectTilesCard.setText("0");

		changeNumberOfCardsLeft();
	}

	private void tileActionPerformed(java.awt.event.ActionEvent evt) {
		JToggleButton button = (JToggleButton) evt.getSource();

		if (designState == DesignState.NONE) {
			button.setSelected(false);
			button.setBorderPainted(false);
			button.setFocusPainted(false);
		}

		if ((designState == DesignState.ADD_TILE && (tileType.getSelectedItem().toString().equals("Regular Tile"))
				|| designState == DesignState.REMOVE_CONNECTION || designState == DesignState.ADD_CONNECTION)) {
			if (button.isSelected()) {
				button.setSelected(false);
				button.setBorderPainted(false);
				button.setFocusPainted(false);
			}
		}

		if (designState == DesignState.SELECT_STARTING_POSITION) {
			for (JToggleButton butt : tilesButtons) {
				if (butt.isSelected() && butt != button)
					butt.setSelected(false);
			}
		}

		if (designState == DesignState.ADD_TILE && (tileType.getSelectedItem().toString().equals("Win Tile"))) {
			for (JToggleButton butt : tilesButtons) {
				if (butt.isSelected() && butt != button)
					butt.setSelected(false);
			}
		}

	}

	private void connectionActionPerformed(java.awt.event.ActionEvent evt) {
		if (designState == DesignState.ADD_TILE || designState == DesignState.REMOVE_TILE
				|| designState == DesignState.ADD_CONNECTION) {
			JToggleButton button = (JToggleButton) evt.getSource();
			if (button.isSelected()) {
				button.setSelected(false);
				button.setBorderPainted(false);
				button.setFocusPainted(false);
			}
		}
	}

	private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void nbOfPlayersChanged() {
		String nbOfPlayersChosen = (String) nbOfPlayers.getSelectedItem();

		if (nbOfPlayers.getSelectedItem() == "4") { // Setting value of combo
													// box for choosing a player
													// to defined number of
													// players
			chosenPlayer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4" }));
		} else if (nbOfPlayers.getSelectedItem() == "3") {
			chosenPlayer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3" }));
		} else {
			chosenPlayer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2" }));
		}

		for (JToggleButton button : tilesButtons) {
			if (button.getBackground().equals(new java.awt.Color(240, 10, 10))) {
				button.setBackground(null);
			}
			if (button.getBackground().equals(new java.awt.Color(240, 240, 10))) {
				button.setBackground(null);
			}
			if (button.getBackground().equals(new java.awt.Color(10, 10, 240))) {
				button.setBackground(null);
			}
			if (button.getBackground().equals(new java.awt.Color(240, 240, 10))) {
				button.setBackground(null);
			}
		}

		currentController.changeNumberOfPlayers(Integer.valueOf(nbOfPlayersChosen));
	}

	private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {
		new MainPage().setVisible(true);
		dispose();
	}

	private void horizontalLengthActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void verticalLengthActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	//
	private Game game;
	
	// Variables declaration - do not modify
	private javax.swing.JToggleButton addConnectionButton;
	private javax.swing.JToggleButton addTileButton;
	private javax.swing.JToggleButton applyChangesButton;
	private javax.swing.JButton backButton;
	private javax.swing.JLabel cardsLeft;
	private javax.swing.JComboBox<String> chosenPlayer;
	private javax.swing.JComboBox<String> horizontalLength;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel11;
	private javax.swing.JLabel jLabel12;
	private javax.swing.JLabel jLabel13;
	private javax.swing.JLabel jLabel14;
	private javax.swing.JLabel jLabel15;
	private javax.swing.JLabel jLabel16;
	private javax.swing.JLabel jLabel17;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JButton loadButton;
	private javax.swing.JTextField nbConnectTilesCard;
	private javax.swing.JTextField nbLoseTurnCard;
	private javax.swing.JComboBox<String> nbOfPlayers;
	private javax.swing.JTextField nbRemoveConnectionCard;
	private javax.swing.JTextField nbRollDieCard;
	private javax.swing.JTextField nbTeleportCard;
	private javax.swing.JToggleButton removeConnectionButton;
	private javax.swing.JToggleButton removeTileButton;
	private javax.swing.JButton saveButton;
	private javax.swing.JToggleButton selectPositionButton;
	private javax.swing.JComboBox<String> tileType;
	private javax.swing.JComboBox<String> verticalLength;
	// End of variables declaration
}