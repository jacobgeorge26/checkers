import Classes.Direction;
import Classes.Node;
import Classes.Piece;
import Classes.Turn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GamePlay {
    public Checkers checkers;

    public Piece[] allPieces;

    public boolean isPlayerTurn = true;

    public boolean gameWon = false;

    public Turn playerTurn = new Turn();

    private List<Turn> possibleTurns;

    int difficulty = 4;



    public void pieceClicked(Piece piece) {
        //is it the player's turn?
        if(!isPlayerTurn) {
            //TODO: warning that it is not player's turn
            return;
        }
        //are they clicking the first button?
        if (playerTurn.origin == null){
            if(piece.isPlayer() && piece.isActive()) {
                playerTurn.origin = piece;
                piece.isSelected = true;
                checkers.UpdateColour(piece);
                GetPossibleTurns(piece);
            }
            else{
                //TODO: warning to select a valid player's piece
            }
        }


        //are they clicking the second button?
        if(playerTurn.origin != null && !piece.isActive()){
            playerTurn.piece = piece;
            if(ValidateMove(playerTurn.origin, piece)){

                //move player piece
                piece.setPlayer(true);
                piece.setActive(true);
                checkers.UpdateColour(piece);

                playerTurn.origin.setPlayer(false);
                playerTurn.origin.setActive(false);
                ClearSelectedPiece(playerTurn);

                //clear any captured pieces
                playerTurn.capturedPieces.forEach(p -> {
                    p.setActive(false);
                    checkers.UpdateColour(p);
                });

                isPlayerTurn = !isPlayerTurn;
                AITurn();
            }
            else{
                //TODO: warning that this move is invalid
                ClearSelectedPiece(playerTurn);
            }
        }
        else{
            //TODO: warning to select a non-active square
        }
    }

    private void GetPossibleTurns(Piece piece) {
        possibleTurns = new ArrayList<Turn>();
        Turn possTurn = new Turn();
        possTurn.origin = piece;
        Search(piece, possTurn);
    }

    public void Search(Piece p, List<Turn> turn) {
        turn.explored.add(p.getLocation());
        for (Node n : p.possibleMoves) {
            if (IsValidDirection(n, p)) {
                Piece possP = allPieces[n.pieceLocation];
                if (!possP.isActive()) {
                    Search(possP, turn);
                }
                if (possP.isPlayer() != isPlayerTurn && possP.isActive()) {
                    Node[] nextNs = (Node[]) possP.possibleMoves.stream().filter(x -> x.direction == n.direction).toArray();
                    Node nextN = nextNs.length > 0 ? nextNs[0] : null;
                    if (nextN != null) {
                        Piece nextP = allPieces[nextN.pieceLocation];
                        if (!nextP.isActive()) {
                            Search(nextP, turn);
                        }
                    }
                }
            }
        }
    }

    private boolean IsValidDirection(Node n, Piece p){
        if (isPlayerTurn && ((n.direction == Direction.UpLeft || n.direction == Direction.UpRight) || p.isKing)) return true;
        else if(!isPlayerTurn && ((n.direction == Direction.DownLeft || n.direction == Direction.DownRight) || p.isKing)) return true;
        else return false;
    }


    //for high - is it blocked?
    //can it move in that direction - look at king
    //would that move make it a king? bump score
    //would that move make it vulnerable? reduce score
    //is this piece about to be taken?
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

        Turn bestTurn = new Turn();
        for(Piece p : highPriority){
            Turn turn = new Turn();
            turn.score = Minimax(turn, p, difficulty, true);
            bestTurn = turn.score > bestTurn.score ? turn : bestTurn;
            System.out.println("Piece " + p.getLocation() + " has score " + turn.score);
        }
        if(bestTurn.score == 0){
            for(Piece p : lowPriority){
                Turn turn = new Turn();
                turn.score = Minimax(turn, p, difficulty, true);
                bestTurn = turn.score > bestTurn.score ? turn : bestTurn;
                System.out.println("Piece " + p.getLocation() + " has score " + turn.score);
            }
        }
        if(bestTurn.score == 0){
            //TODO: warning - no turns possible - switch player
        }
        CompleteTurn(bestTurn);
    }

    private void CompleteTurn(Turn bestTurn) {

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


    private void ClearSelectedPiece(Turn turn){
        Piece needsClearing = turn.origin;
        needsClearing.isSelected = false;
        turn.origin = null;
        checkers.UpdateColour(needsClearing);
    }
}
