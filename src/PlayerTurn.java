import Classes.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerTurn extends TurnHelpers {
    protected Turn turn;

    private List<Turn> possibleMoves;

    public PlayerTurn(UI _ui, Piece[] _allPieces, GamePlay _game, PieceColour _playerColour, Piece _origin) {
        super(_ui, _allPieces, _game, _playerColour);
        turn = new Turn(_origin);
        isPlayerTurn = true;
    }

    protected void ShowOptions(){
        //if forced capture then check for any pieces that need to capture
        if(game.isForcedCapture){
            List<Piece> forcePieces = ForcedCapture(isPlayerTurn);
            if(!forcePieces.isEmpty() && !forcePieces.contains(turn.origin)){
                ui.ShowMessage("Forced capture is turned on and there is a possible capture", Color.orange);
                game.RestartMove(turn.origin);
                return;
            }
        }

        possibleMoves = new ArrayList<Turn>();
        if(turn.origin.info.isPlayer && turn.origin.info.isActive) {
            turn.origin.isSelected = true;
            ui.UpdateColour(turn.origin);
            possibleMoves = Search(turn.origin, turn.origin, MoveType.Both, possibleMoves, null, isPlayerTurn, true);
            for(Turn t : possibleMoves){
                t.piece.isOption = true;
            }
            if(possibleMoves.size() == 0){
                ui.ShowMessage("This piece is trapped - no moves possible", Color.orange);
            }
        }
    }


    public void RemoveSelection(Piece piece) {
        //clear options
        if(possibleMoves != null){
            for(Turn t : possibleMoves){
                t.piece.isOption = false;
                ui.UpdateColour(t.piece);
            }
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
