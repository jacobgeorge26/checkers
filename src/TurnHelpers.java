import Classes.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TurnHelpers {
    protected UI ui;

    protected Piece[] allPieces;

    protected GamePlay game;

    protected boolean isPlayerTurn;

    protected PieceColour playerColour;

    public TurnHelpers(UI _ui, Piece[] _allPieces, GamePlay _game, PieceColour _playerColour){
        ui = _ui;
        allPieces = _allPieces;
        game = _game;
        playerColour = _playerColour;
    }

    protected List<Piece> GetPriorityPieces(Priority priority) {
        List<Piece> priorityPieces = new ArrayList<>();
        for(Piece p : allPieces){
            if(p == null || p.isPlayer != isPlayerTurn || !p.isActive){
                continue;
            }
            for(Node n : p.possibleMoves){
                Piece poss = allPieces[n.pieceLocation];
                //pieces with a player piece adjacent
                if(poss.isPlayer != isPlayerTurn && poss.isActive && !priorityPieces.contains(p) && priority == Priority.High){
                    priorityPieces.add(p);
                }
                //pieces with an empty space adjacent
                if(!poss.isActive && !priorityPieces.contains(p) && priority == Priority.Low){
                    priorityPieces.add(p);
                }
            }
        }
        return priorityPieces;
    }

    protected List<Node> FilterMoves(Piece currentPiece, List<Node> possibleMoves, MoveType moveType) {
        List<Node> filteredMoves = new ArrayList<Node>();
        for (Node possN : possibleMoves) {
            if (IsValidDirection(possN, currentPiece)) {
                Piece possP = allPieces[possN.pieceLocation];
                if ((moveType == MoveType.Advance || moveType == MoveType.Both) && !possP.isActive) {
                    filteredMoves.add(possN);
                }
                if ((moveType == MoveType.Jump || moveType == MoveType.Both) && possP.isPlayer != currentPiece.isPlayer && possP.isActive) {
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
        if((p.isPlayer && playerColour == PieceColour.red) || (!p.isPlayer && playerColour == PieceColour.white) ){
            //look up
            return ((n.direction == Direction.UpLeft || n.direction == Direction.UpRight) || p.isKing);
        }
        else if((p.isPlayer && playerColour == PieceColour.white) || (!p.isPlayer && playerColour == PieceColour.red)){
            //look down
            return ((n.direction == Direction.DownLeft || n.direction == Direction.DownRight) || p.isKing);
        }
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
        IsGameWon();

        isPlayerTurn = !isPlayerTurn;
    }

    protected void ClearSelectedPiece(Turn turn){
        Piece needsClearing = turn.origin;
        needsClearing.isSelected = false;
        turn.origin = null;
        ui.UpdateColour(needsClearing);
    }

    protected void ClearOptions(List<Turn> possibleMoves){
        if(possibleMoves != null){
            for(Turn t : possibleMoves){
                t.piece.isOption = false;
                t.piece.isKing = t.piece.isActive ? t.piece.isKing :  false;
                ui.UpdateColour(t.piece);
            }
        }
    }

    private void IsGameWon() {
        //it's my turn
        //are all their pieces now captured?
        //if they have any pieces left, are all of them trapped?
        boolean thisPlayerTrapped = true, otherPlayerTrapped = true, otherPlayerCaptured = true;

        for(Piece p : allPieces){
            if(p == null || !p.isActive){
                continue;
            }
            else if(p.isActive && p.isPlayer == isPlayerTurn){
                //are all my pieces trapped?
                //if so, game over and I've lost
                if(!FilterMoves(p, p.possibleMoves, MoveType.Both).isEmpty()){
                    thisPlayerTrapped = false;
                }
            }
            else if(p.isActive && p.isPlayer != isPlayerTurn){
                //not all their pieces are captured
                otherPlayerCaptured = false;
                //are all their pieces trapped?
                //if so, game over and I've won
                if(!FilterMoves(p, p.possibleMoves, MoveType.Both).isEmpty()){
                    otherPlayerTrapped = false;
                }
            }
        }
        if(thisPlayerTrapped){
            isPlayerTurn = !isPlayerTurn;
            GameOver("All pieces are trapped");
        }
        else if(otherPlayerCaptured){
            GameOver("All pieces are captured");
        }
        else if(otherPlayerTrapped){
            GameOver("All pieces are trapped");
        }

    }

    protected void GameOver(String message){
        game.isPaused = true;
        message = isPlayerTurn ? "Congratulations! You won! "  + message: "The AI won! " + message;
        ui.GameOverDialog(message);
    }
}
