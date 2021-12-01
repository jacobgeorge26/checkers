import Classes.*;

import java.util.ArrayList;
import java.util.List;

public class TurnHelpers {
    protected UI ui;

    protected Piece[] allPieces;

    protected boolean isPlayerTurn;

    protected PieceColour playerColour;

    public TurnHelpers(UI _ui, Piece[] _allPieces, PieceColour _playerColour){
        ui = _ui;
        allPieces = _allPieces;
        playerColour = _playerColour;
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

    protected boolean isKingNow(Piece piece) {
        int gridSize = piece.getGridSize();
        if((isPlayerTurn && playerColour == PieceColour.white) || (!isPlayerTurn && playerColour == PieceColour.red)){
            //is the piece in the bottom row?
            return piece.getLocation() > ((gridSize * gridSize) / 2) - (gridSize / 2);
        }
        else {
            //is the piece in the top row?
            return piece.getLocation() <= gridSize / 2;
        }
    }

    protected void CompleteTurn(Turn turn) {
        //move player piece
        turn.piece.isPlayer = isPlayerTurn;
        turn.piece.isActive = true;
        turn.piece.isKing = turn.origin.isKing || turn.piece.isKing;
        ui.UpdateColour(turn.piece);

        turn.origin.isPlayer = false;
        turn.origin.isActive = false;
        turn.origin.isKing = false;

        //clear any captured pieces
        turn.capturedPieces.forEach(p -> {
            p.isActive = false;
            ui.UpdateColour(p);
        });

        //clear selection formatting
        ClearSelectedPiece(turn);

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
            t.piece.isKing = t.piece.isActive ? t.piece.isKing :  false;
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
