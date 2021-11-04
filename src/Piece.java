import java.util.Queue;

public class Piece extends Tile{
    protected boolean isPlayer;

    protected boolean isActive = true;

    protected boolean isKing = false;

    public Queue<Piece> TakeSquares;

    public Queue<Piece> MoveSquares;
}
