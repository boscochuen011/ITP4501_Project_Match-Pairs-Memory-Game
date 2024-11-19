package com.example.matchpairsmemorygame;

// Class representing a player in the match-pairs memory game
class Player {
    private String name; // Name of the player
    private int moves;   // Number of moves made by the player
    private int rank;    // Rank of the player

    // Constructor initializing the player's name and moves
    public Player(String name, int moves) {
        this.name = name;
        this.moves = moves;
    }

    // Getter for the player's moves
    public int getMoves() {
        return moves;
    }

    // Getter for the player's rank
    public int getRank() {
        return rank;
    }

    // Getter for the player's name
    public String getName() {
        return name;
    }

    // Setter for the player's rank
    public void setRank(int rank) {
        this.rank = rank;
    }

    // Overrides the toString method to return a string representation of the player object
    @Override
    public String toString() {
        return "Rank " + rank + ", " + name + ", " + moves + " moves";
    }
}