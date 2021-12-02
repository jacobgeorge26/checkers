import Classes.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AITurn extends TurnHelpers{

    protected static int aiDepth;

    public AITurn(UI _ui, Piece[] _allPieces, PieceColour _playerColour, GamePlay _game) {
        super(_ui, _allPieces, _game, _playerColour);
        isPlayerTurn = false;
    }

    public void MakeMove(){
        //start with any potential jump moves
        List<Piece> highPriority = GetPriorityPieces(Priority.High);
        Turn bestTurn = null;
        for(Piece p : highPriority){
            Turn turn = new Turn(p);
            //TODO: forced capture is missing possible jumps
            turn.score = Minimax(turn, p, aiDepth, false, MoveType.Both);
            bestTurn = bestTurn == null || turn.score > bestTurn.score ? turn : bestTurn;
            System.out.println("Piece " + p.getLocation() + " has score " + turn.score);
        }
        if(bestTurn == null || bestTurn.score < 2){
            //no jump moves possible, get low priority moves
            List<Piece> lowPriority = GetPriorityPieces(Priority.Low);
            for(Piece p : lowPriority){
                Turn turn = new Turn(p);
                turn.score = Minimax(turn, p, aiDepth, true, MoveType.Both);
                bestTurn = bestTurn == null || turn.score > bestTurn.score ? turn : bestTurn;
                System.out.println("Piece " + p.getLocation() + " has score " + turn.score);
            }
        }
        if(bestTurn == null || bestTurn.score == 0){
            isPlayerTurn = !isPlayerTurn;
            GameOver("All pieces are trapped");
        }
        else{
            System.out.println("MOVING piece " + bestTurn.origin.getLocation() + " to piece " + bestTurn.piece.getLocation());
            Turn finalBestTurn = bestTurn;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            CompleteTurn(finalBestTurn);
                            game.isPaused = false;
                        }
                    },
                    1500
            );

        }
    }


    private int Minimax(Turn turn, Piece piece, int depth, boolean isMin, MoveType moveType) {
        //if allowed, get possible advancing moves
        List<Node> unexploredA = (moveType == MoveType.Advance || moveType == MoveType.Both)
                ? FilterMoves(piece, piece.possibleMoves, MoveType.Advance) : new ArrayList<Node>();
        //remove those already explored - for advance only as jumps can return to the same place
        unexploredA.removeIf(n -> turn.explored.contains(n.pieceLocation));
        //if allowed, get possible jumping moves
        List<Node> unexploredJ = (moveType == MoveType.Jump || moveType == MoveType.Both)
                ? FilterMoves(piece, piece.possibleMoves, MoveType.Jump) : new ArrayList<Node>();

        turn.explored.add(piece.getLocation());
        if (depth == 0 || (unexploredA.isEmpty() && unexploredJ.isEmpty())){
            turn.piece = piece;
            return turn.capturedPieces.isEmpty()
                    ? moveType == MoveType.Both ? 0 : 1
                    : 1 + turn.capturedPieces.size() + turn.score;
        }

        int bestValue;
        if (isMin || unexploredJ.isEmpty()){
            bestValue = -1000;
            for (int i = 0; i < unexploredA.size(); i++){
                Piece nextPiece = allPieces[unexploredA.get(i).pieceLocation];

                boolean becomesKing = isKingNow(nextPiece);
                turn.score += becomesKing && piece.isKing ? 1 : 0; //add to score if this move would make it a king
                nextPiece.isKing = becomesKing;

                int eval = Minimax(turn, nextPiece, depth - 1, !isMin, MoveType.Neither);
                bestValue = Math.max(bestValue, eval);
            }
        }
        else if(!isMin || unexploredA.isEmpty()){
            bestValue = 1000;
            for(int i = 0; i < unexploredJ.size(); i++){
                Node nextNode = unexploredJ.get(i);
                Piece nextPiece = allPieces[nextNode.pieceLocation];

                Optional<Node> capturedNode = piece.possibleMoves.stream()
                        .filter(p -> p.direction == nextNode.direction).findFirst();
                if(!capturedNode.isPresent()){
                    ui.ShowMessage("There has been an error in AITurn/Minimax/JumpingMoves. The captured piece cannot be found.", Color.red);
                }
                else{
                    Piece capturedPiece = allPieces[capturedNode.get().pieceLocation];
                    //would this move capture a king?
                    boolean becomesKing = capturedPiece.isKing;
                    //bonus point if so, taking away a player's king
                    turn.score += becomesKing ? 1 : 0;
                    //would this move take the piece to the edge and make it a king
                    becomesKing = isKingNow(nextPiece) || becomesKing;
                    //bonus point if this move turns a normal piece into a king
                    turn.score += becomesKing && !piece.isKing ? 1 : 0;
                    nextPiece.isKing = becomesKing;

                    turn.capturedPieces.add(capturedPiece);
                    int eval = Minimax(turn, nextPiece, depth - 1, !isMin, MoveType.Jump);
                    bestValue = Math.min(bestValue, eval);
                }
            }
        }
        else{
            //this should never be reached
            ui.ShowMessage("There has been an error in AITurn/MiniMax. No valid moves were found and this situation was not handled correctly", Color.red);
            return 0;
        }
        return bestValue;
    }

}
