package Classes;

import Components.RoundButton;

import java.util.*;

public class Piece extends Tile{
    public boolean isPlayer;

    public boolean isActive;

    public boolean isKing = false;

    public List<Node> possibleMoves = new LinkedList<Node>() {};;

    public boolean isSelected = false;

    public boolean isOption = false;

    public RoundButton button;
}
