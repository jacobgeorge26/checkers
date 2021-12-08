package Classes;

public class Move {
    public int pieceLocation;

    public Info before;

    public Info after;

    public Move(Piece p) {
        before = new Info(p.info.isPlayer, p.info.isActive, p.info.isKing);
        pieceLocation = p.getLocation();
    }
}
