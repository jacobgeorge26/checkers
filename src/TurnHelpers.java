import Classes.*;

import java.util.ArrayList;
import java.util.List;

public class TurnHelpers {
    protected UI ui;

    protected Piece[] allPieces;

    protected boolean isPlayerTurn;

    public TurnHelpers(){
    }

    protected List<Node> FilterMoves(Piece currentPiece, List<Node> possibleMoves, MoveType moveType) {
        List<Node> filteredMoves = new ArrayList<Node>();
        for (Node possN : possibleMoves) {
            if (IsValidDirection(possN, currentPiece)) {
                Piece possP = allPieces[possN.pieceLocation];
                if ((moveType == MoveType.Advance || moveType == MoveType.Both) && !possP.isActive) {
                    filteredMoves.add(possN);
                }
                if ((moveType == MoveType.Jump || moveType == MoveType.Both) && possP.isPlayer != isPlayerTurn && possP.isActive) {
                    Object[] nextNs = possP.possibleMoves.stream().filter(x -> x.direction == possN.direction).toArray();
                    Node nextN = nextNs.length > 0 ? (Node) nextNs[0] : null;
                    if (nextN != null) {
                        Piece nextP = allPieces[nextN.pieceLocation];
                        if (!nextP.isActive) {
                            filteredMoves.add(nextN);
                        }
                    }
                }
            }
        }
        return filteredMoves;
    }

    private boolean IsValidDirection(Node n, Piece p){
        if (isPlayerTurn && ((n.direction == Direction.UpLeft || n.direction == Direction.UpRight) || p.isKing)) return true;
        else if(!isPlayerTurn && ((n.direction == Direction.DownLeft || n.direction == Direction.DownRight) || p.isKing)) return true;
        else return false;
    }

    protected void CompleteTurn(Turn turn) {
        //move player piece
        turn.piece.isPlayer = isPlayerTurn;
        turn.piece.isActive = true;
        ui.UpdateColour(turn.piece);

        turn.origin.isPlayer = false;
        turn.origin.isActive = false;
        ClearSelectedPiece(turn);

        //clear any captured pieces
        turn.capturedPieces.forEach(p -> {
            p.isActive = false;
            ui.UpdateColour(p);
        });

        boolean isWon = IsGameWon();
        if(isWon){
            String message = isPlayerTurn ? " the player. Well done!" : " the AI. Good try!";
            System.out.println("The winner is " + message);
            //TODO: reset game?
        }
        isPlayerTurn = !isPlayerTurn;
    }

    protected void ClearSelectedPiece(Turn turn){
        Piece needsClearing = turn.origin;
        needsClearing.isSelected = false;
        turn.origin = null;
        ui.UpdateColour(needsClearing);
    }

    protected void ClearOptions(List<Turn> possibleMoves){
        for(Turn t : possibleMoves){
            t.piece.isOption = false;
            ui.UpdateColour(t.piece);
        }
    }

    private boolean IsGameWon() {
        boolean isWon = true;
        for(Piece p : allPieces){
            if(p == null){
                continue;
            }
            isWon = !p.isActive && p.isPlayer != isPlayerTurn;
            if(!isWon) return false;
        }
        return isWon;
    }
}
