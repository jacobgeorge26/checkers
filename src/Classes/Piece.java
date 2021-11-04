package Classes;

import Components.RoundButton;

import javax.swing.*;
import java.util.Queue;

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

    protected boolean isKing = false;

    public Queue<Piece> TakeSquares;

    public Queue<Piece> MoveSquares;

    public RoundButton button;
}
