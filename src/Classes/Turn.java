package Classes;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Turn {

    public Piece piece;

    public Piece origin;

    public List<Piece> capturedPieces = new LinkedList<Piece>();

    public List<Move> changes = new ArrayList<Move>();

    public List<Integer> explored = new ArrayList<>();

    public MoveType moveType = MoveType.Neither;

    public double score = 0;

    public Turn(Piece _origin) {
        origin = _origin;
    }


    public Turn Clone() {
        Turn clone = new Turn(this.origin);
        clone.piece = this.piece;
        clone.capturedPieces = new ArrayList<>();
        this.capturedPieces.forEach(p -> clone.capturedPieces.add(p));
        clone.explored = new ArrayList<>();
        this.explored.forEach(i -> clone.explored.add(i));
        clone.score = this.score;
        return clone;
    }
}
