package Classes;

import java.util.Queue;

public class Piece extends Tile{
    public boolean isPlayer() {
        return isPlayer;
    }

    public void setPlayer(boolean player) {
        isPlayer = player;
    }

    protected boolean isPlayer;

    protected boolean isActive = true;

    protected boolean isKing = false;

    public Queue<Piece> TakeSquares;

    public Queue<Piece> MoveSquares;
}
