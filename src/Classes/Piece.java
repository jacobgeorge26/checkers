package Classes;

import Components.RoundButton;

import java.util.*;

public class Piece extends Tile{

    public Info info = new Info(false, false, false);

    public List<Node> possibleMoves = new LinkedList<Node>() {};;

    public boolean isSelected = false;

    public boolean isOption = false;

    public RoundButton button;
}
