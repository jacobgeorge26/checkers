import Classes.Direction;
import Classes.Node;
import Classes.Piece;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GamePlay {
    private Piece[] pieces;
    boolean playerTurn = false;
    List<Integer> explored = new ArrayList<>();
//    private List<Turn> compHistory;
//    private List<Turn> humanHistory;
//    private int[] successorScores;
//    private int count = 0;


    public static void main(String[] args) {
        new GamePlay();
    }

    public GamePlay() {
        //create board - setup pieces
        Checkers checkers = new Checkers();
        pieces = checkers.GetPieces();
        CreateTree();
        Minimax(pieces[1], 4, true);
    }

    private int Minimax(Piece piece, int depth, boolean isMin) {
        List<Node> unexplored = piece.possibleMoves.stream()
                .filter(p -> !explored.contains(p.pieceLocation) && (p.direction == Direction.DownLeft || p.direction == Direction.DownRight))
                .collect(Collectors.toList());
        explored.add(piece.getLocation());
        if (depth == 0 || unexplored.isEmpty()){
            return 1;
        }
        if (!isMin){
            int bestValue = -1000;
            for (int i = 0; i < unexplored.size(); i++){
                Node nextNode = unexplored.get(i);
                System.out.println("Checking MAX " + nextNode.pieceLocation);
                Piece actualPiece = pieces[nextNode.pieceLocation];
                int eval = Minimax(actualPiece, depth - 1, !isMin);
                bestValue = Math.max(bestValue, eval);


                actualPiece.button.SetColour(new Color(0, 255, 0));
            }
            return bestValue;
        }
        else{
            int bestValue = 1000;
            for(int i = 0; i < unexplored.size(); i++){
                Node nextNode = unexplored.get(i);
                System.out.println("Checking MIN " + nextNode.pieceLocation);
                Piece actualPiece = pieces[nextNode.pieceLocation];
                int eval = Minimax(actualPiece, depth - 1, !isMin);
                bestValue = Math.min(bestValue, eval);

                actualPiece.button.SetColour(new Color(0, 0, 255));
            }
            return bestValue;
        }

    }

    private void CreateTree() {
        for(int i = 1; i < pieces.length; i++){
            Piece p = pieces[i];
            int loc = p.getLocation();
            int x = p.getCol(), y = p.getRow(), g = p.getGridSize();
            int[] indexes = y % 2 == 0 ? new int[]{-5, -4, 3, 4} : new int[]{-4, -3, 4, 5};
            if(loc + indexes[0] >= 1 && y > 1 && x > 1) p.possibleMoves.add(new Node(loc + indexes[0], Direction.UpLeft));
            if(loc + indexes[1] >= 1 && y > 1 && x < g) p.possibleMoves.add(new Node(loc + indexes[1], Direction.UpRight));
            if(loc + indexes[2] <= g * g / 2 && y < g && x > 1) p.possibleMoves.add(new Node(loc + indexes[2], Direction.DownLeft));
            if(loc + indexes[3] <= g * g / 2 && y < g && x < g) p.possibleMoves.add(new Node(loc + indexes[3], Direction.DownRight));
        }
    }


}
