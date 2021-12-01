package Classes;

import Components.RoundButton;

import java.util.*;

public class Piece extends Tile{
    public boolean isPlayer() {
        return isPlayer;
    }

    public void setPlayer(boolean player) {
        isPlayer = player;
    }

    protected boolean isPlayer;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    protected boolean isActive;

    public boolean isKing = false;

    public List<Node> possibleMoves = new LinkedList<Node>() {};;

    public boolean isSelected = false;

    public boolean isOption = false;

    public RoundButton button;
}
