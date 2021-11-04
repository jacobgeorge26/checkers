import Classes.Piece;

public class GamePlay {
    private Piece[] pieces;

    public static void main(String[] args) {
        new GamePlay();
    }

    public GamePlay() {
        //create board - setup pieces
        Checkers checkers = new Checkers();
        pieces = checkers.GetPieces();
    }


}
