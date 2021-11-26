import Classes.Direction;
import Classes.Node;
import Classes.Piece;
import Classes.Turn;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GamePlay {
    public Checkers checkers;

    public Piece[] allPieces;

    public boolean playerTurn = true;

    public boolean gameWon = false;

    public Piece selectedPiece;

    int difficulty = 4;



    public void pieceClicked(Turn turn) {
        //is it the player's turn?
        if(!playerTurn) {
            //TODO: warning that it is not player's turn
            return;
        }
        Piece piece = turn.piece;
        //are they clicking the first button?
        if (selectedPiece == null){
            if(piece.isPlayer() && piece.isActive()) {
                selectedPiece = piece;
                checkers.UpdateColour(piece);
            }
            else{
                //TODO: warning to select a valid player's piece
            }
        }


        //are they clicking the second button?
        if(selectedPiece != null && !piece.isActive()){
            if(ValidateMove(selectedPiece, piece)){
                //move player piece
                piece.setPlayer(true);
                piece.setActive(true);
                checkers.UpdateColour(piece);

                selectedPiece.setPlayer(false);
                selectedPiece.setActive(false);
                ClearSelectedPiece();

                //clear any captured pieces
                turn.capturedPieces.forEach(p -> {
                    p.setActive(false);
                    checkers.UpdateColour(p);
                });

                playerTurn = !playerTurn;
                AITurn();
            }
            else{
                //TODO: warning that this move is invalid
                ClearSelectedPiece();
            }
        }
        else{
            //TODO: warning to select a non-active square
        }
    }

    private void AITurn(){
        //pieces with a player piece adjacent
        List<Piece> highPriority = new ArrayList<>();
        //pieces with an empty space adjacent
        List<Piece> lowPriority = new ArrayList<>();
        for(Piece p : allPieces){
            if(p == null || p.isPlayer() || !p.isActive()){
                continue;
            }
            for(Node n : p.possibleMoves){
                Piece poss = allPieces[n.pieceLocation];
                if(poss.isPlayer() && poss.isActive() && !highPriority.contains(p)){
                    highPriority.add(p);
                }
                if(!poss.isActive() && !lowPriority.contains(p)){
                    lowPriority.add(p);
            }
        }
    }

        //for high - is it blocked?
        //can it move in that direction - look at king
        //would that move make it a king? bump score
        //would that move make it vulnerable? reduce score
        //is this piece about to be taken?

        Turn bestTurn = new Turn();
        for(Piece p : highPriority){
            Turn turn = new Turn();
            turn.score = Minimax(turn, p, difficulty, true);
            bestTurn = turn.score > bestTurn.score ? turn : bestTurn;
            System.out.println("Piece " + p.getLocation() + " has score " + turn.score);
        }
    }

    private int Minimax(Turn turn, Piece piece, int depth, boolean isMin) {
        List<Node> unexplored = piece.possibleMoves.stream()
                .filter(p -> !turn.explored.contains(p.pieceLocation) && (p.direction == Direction.DownLeft || p.direction == Direction.DownRight))
                .collect(Collectors.toList());
        turn.explored.add(piece.getLocation());
        if (depth == 0 || unexplored.isEmpty()){
            return 1;
        }
        if (!isMin){
            int bestValue = -1000;
            for (int i = 0; i < unexplored.size(); i++){
                Node nextNode = unexplored.get(i);
                //System.out.println("Checking MAX " + nextNode.pieceLocation);
                Piece actualPiece = allPieces[nextNode.pieceLocation];
                int eval = Minimax(turn, actualPiece, depth - 1, !isMin);
                bestValue = Math.max(bestValue, eval);


               // actualPiece.button.SetColour(new Color(0, 255, 0));
            }
            return bestValue;
        }
        else{
            int bestValue = 1000;
            for(int i = 0; i < unexplored.size(); i++){
                Node nextNode = unexplored.get(i);
                //System.out.println("Checking MIN " + nextNode.pieceLocation);
                Piece actualPiece = allPieces[nextNode.pieceLocation];
                int eval = Minimax(turn, actualPiece, depth - 1, !isMin);
                bestValue = Math.min(bestValue, eval);

               // actualPiece.button.SetColour(new Color(0, 0, 255));
            }
            return bestValue;
        }

    }

    private boolean ValidateMove(Piece from, Piece to) {
        //TODO: implement method
        //update captured pieces as it iterates
        return true;
    }


    private void ClearSelectedPiece(){
        Piece needsClearing = selectedPiece;
        selectedPiece = null;
        checkers.UpdateColour(needsClearing);
    }
}
