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

    public List<Integer> explored = new ArrayList<>();

    public int score = 0;

    public Turn(){

    }

}
