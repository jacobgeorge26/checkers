import Classes.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerTurn extends TurnHelpers {
    protected Turn turn;

    private List<Turn> possibleMoves;

    public PlayerTurn(UI _ui, Piece[] _allPieces, PieceColour _playerColour, Piece _origin) {
        super(_ui, _allPieces, _playerColour);
        turn = new Turn(_origin);
        isPlayerTurn = true;
        ShowOptions();
    }

    public void ShowOptions(){
        possibleMoves = new ArrayList<Turn>();
        if(turn.origin.isPlayer && turn.origin.isActive) {
            turn.origin.isSelected = true;
            ui.UpdateColour(turn.origin);
            Search(turn.origin, MoveType.Both, possibleMoves, null);
            for(Turn t : possibleMoves){
                t.piece.isOption = true;
            }
            if(possibleMoves.size() == 0){
                ui.ShowMessage("This piece is trapped - no moves possible", Color.orange);
            }
        }
    }

    public List<Turn> Search(Piece piece, MoveType legalMoveType, List<Turn> moves, Turn existingTurn) {
        if(legalMoveType == MoveType.Jump || legalMoveType == MoveType.Both){
            List<Node> jumpMoves = FilterMoves(piece, piece.possibleMoves, MoveType.Jump);
            for(Node nextNode : jumpMoves){
                Turn newTurn = existingTurn == null ? new Turn(turn.origin) : existingTurn.Clone();
                Piece nextPiece = allPieces[nextNode.pieceLocation];
                if(true){
                    Optional<Node> capturedNode = piece.possibleMoves.stream()
                            .filter(p -> p.direction == nextNode.direction).findFirst();
                    Piece capturedPiece = allPieces[capturedNode.get().pieceLocation];
                    if(!newTurn.capturedPieces.contains(capturedPiece)){
                        //update nextPiece
                        nextPiece.isOption = true;
                        ui.UpdateColour(nextPiece);
                        nextPiece.isKing = isKingNow(nextPiece) || piece.isKing;

                        //update list of possible turns
                        moves.add(newTurn);
                        newTurn = moves.get(moves.size() - 1); //get the duplicate
                        newTurn.piece = nextPiece;
                        newTurn.capturedPieces.add(capturedPiece);

                        //continue search
                        Search(nextPiece, MoveType.Jump, moves, newTurn);
                    }

                }
            }
        }
        if(legalMoveType == MoveType.Advance || legalMoveType == MoveType.Both){
            List<Node> advanceMoves = FilterMoves(piece, piece.possibleMoves, MoveType.Advance);
            for (Node nextNode : advanceMoves){
                Piece nextPiece = allPieces[nextNode.pieceLocation];
                if(nextPiece != turn.origin){
                    Turn newTurn = new Turn(turn.origin);
                    newTurn.piece = nextPiece;

                    nextPiece.isKing = isKingNow(nextPiece);

                    moves.add(newTurn);
                    nextPiece.isOption = true;
                    ui.UpdateColour(nextPiece);
                }
            }
        }
        return moves;
    }

    public void RemoveSelection(Piece piece) {
        //clear options
        for(Turn t : possibleMoves){
            t.piece.isOption = false;
            ui.UpdateColour(t.piece);
        }
        //clear selection
        ClearSelectedPiece(turn);
        ClearOptions(possibleMoves);
    }

    public void ChooseMove(Piece piece) {
        List<Turn> matchingTurns = possibleMoves.stream().filter(t -> t.piece.getLocation() == piece.getLocation()).collect(Collectors.toList());
        //no matching turns
        if(matchingTurns.size() == 0){
            ui.ShowMessage("This is not a valid move", Color.orange);
            ClearSelectedPiece(turn);
        }
        else {
            turn = matchingTurns.get(0);
            //if there are multiple matching turns then use the most beneficial one for the player
            for(Turn t : matchingTurns) {
                turn = t.capturedPieces.size() > turn.capturedPieces.size() ? t : turn;
            }
            CompleteTurn(turn);
        }
        ClearOptions(possibleMoves);
    }
}
