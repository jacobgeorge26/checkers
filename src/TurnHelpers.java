import Classes.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    protected List<Piece> ForcedCapture(boolean isPlayer) {
        List<Piece> possibleJumps = GetPriorityPieces(Priority.High, isPlayer);
        List<Piece> forcePieces = new ArrayList<Piece>();
        int score = 0;
        //go through each potential jump to see if it is a viable move
        for(Piece p : possibleJumps){
            if(!FilterMoves(p, p.possibleMoves, MoveType.Jump).isEmpty()){
                List<Turn> possibleMoves = new ArrayList<Turn>();
                possibleMoves = Search(p, p, MoveType.Jump, possibleMoves, null, isPlayer, false);
                for(Turn t : possibleMoves){
                    if(t.capturedPieces.size() > score){
                        forcePieces = new ArrayList<Piece>(){};
                        forcePieces.add(t.origin);
                        score = t.capturedPieces.size();
                    }
                    else if(t.capturedPieces.size() == score){
                        forcePieces.add(t.origin);
                    }
                }
            }
        }
        return forcePieces;
    }

    protected List<Piece> GetPriorityPieces(Priority priority, boolean isPlayer) {
        List<Piece> priorityPieces = new ArrayList<>();
        for(Piece p : allPieces){
            if(p == null || p.info.isPlayer != isPlayer || !p.info.isActive){
                continue;
            }
            for(Node n : p.possibleMoves){
                Piece poss = allPieces[n.pieceLocation];
                //pieces with a player piece adjacent
                if(poss.info.isPlayer != isPlayer && poss.info.isActive && !priorityPieces.contains(p) && priority != Priority.Low){
                    priorityPieces.add(p);
                }
                //pieces with an empty space adjacent
                if(!poss.info.isActive && !priorityPieces.contains(p) && priority != Priority.High){
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
                if ((moveType == MoveType.Advance || moveType == MoveType.Both) && !possP.info.isActive) {
                    filteredMoves.add(possN);
                }
                if ((moveType == MoveType.Jump || moveType == MoveType.Both) && possP.info.isPlayer != currentPiece.info.isPlayer && possP.info.isActive) {
                    Object[] nextNs = possP.possibleMoves.stream().filter(x -> x.direction == possN.direction).toArray();
                    Node nextN = nextNs.length > 0 ? (Node) nextNs[0] : null;
                    if (nextN != null) {
                        Piece nextP = allPieces[nextN.pieceLocation];
                        if (!nextP.info.isActive) {
                            filteredMoves.add(nextN);
                        }
                    }
                }
            }
        }
        return filteredMoves;
    }

    public List<Turn> Search(Piece origin, Piece piece, MoveType legalMoveType, List<Turn> moves, Turn existingTurn, boolean isPlayer, boolean updateColour) {
        if(legalMoveType == MoveType.Jump || legalMoveType == MoveType.Both){
            List<Node> jumpMoves = FilterMoves(piece, piece.possibleMoves, MoveType.Jump);
            for(Node nextNode : jumpMoves){
                Turn newTurn = existingTurn == null ? new Turn(origin) : existingTurn.Clone();
                Piece nextPiece = allPieces[nextNode.pieceLocation];
                Optional<Node> capturedNode = piece.possibleMoves.stream()
                        .filter(p -> p.direction == nextNode.direction).findFirst();
                Piece capturedPiece = allPieces[capturedNode.get().pieceLocation];
                if(!newTurn.capturedPieces.contains(capturedPiece)){
                    //update nextPiece IF this is not an exploratory exercise
                    if(updateColour){
                        nextPiece.isOption = true;
                        ui.UpdateColour(nextPiece);
                        nextPiece.info.isKing = isKingNow(nextPiece) || piece.info.isKing || capturedPiece.info.isKing;
                    }

                    //update list of possible turns
                    newTurn.moveType = MoveType.Jump;
                    moves.add(newTurn);
                    newTurn = moves.get(moves.size() - 1); //get the duplicate
                    newTurn.piece = nextPiece;
                    newTurn.capturedPieces.add(capturedPiece);

                    //continue search
                    //i don't remember why i wrap this in the isPLayer/!isPlayer
                    //but when I remove it something breaks
                    nextPiece.info.isPlayer = isPlayer;
                    Search(origin, nextPiece, MoveType.Jump, moves, newTurn, isPlayer, updateColour);
                    nextPiece.info.isPlayer = !isPlayer;
                }
            }
        }
        //if we're looking for advance moves and there are no jump moves available - forced capture
        if((legalMoveType == MoveType.Advance || legalMoveType == MoveType.Both) && moves.isEmpty()){
            List<Node> advanceMoves = FilterMoves(piece, piece.possibleMoves, MoveType.Advance);
            for (Node nextNode : advanceMoves){
                Piece nextPiece = allPieces[nextNode.pieceLocation];
                if(nextPiece != origin){
                    Turn newTurn = new Turn(origin);
                    newTurn.piece = nextPiece;

                    nextPiece.info.isKing = isKingNow(nextPiece);

                    newTurn.moveType = MoveType.Advance;
                    moves.add(newTurn);
                    if(updateColour){
                        nextPiece.isOption = true;
                        ui.UpdateColour(nextPiece);
                    }
                }
            }
        }
        return moves;
    }

    private boolean IsValidDirection(Node n, Piece p){
        if((p.info.isPlayer && playerColour == PieceColour.red) || (!p.info.isPlayer && playerColour == PieceColour.white) ){
            //look up
            return ((n.direction == Direction.UpLeft || n.direction == Direction.UpRight) || p.info.isKing);
        }
        else if((p.info.isPlayer && playerColour == PieceColour.white) || (!p.info.isPlayer && playerColour == PieceColour.red)){
            //look down
            return ((n.direction == Direction.DownLeft || n.direction == Direction.DownRight) || p.info.isKing);
        }
        else return false;
    }

    protected List<Move> DoMove(Turn turn, boolean playerMove) {
        List<Move> changes = new ArrayList<Move>();
        if(turn.moveType == MoveType.Advance){
            //create move for fromPiece
            Move m = new Move(turn.origin);
            m.after = new Info(false, false, false);
            turn.origin.info = m.after;
            changes.add(m);
            //create move for toPiece
            Move n = new Move(turn.piece);
            n.after = new Info(playerMove, true, turn.origin.info.isKing);
            turn.piece.info = n.after;
            changes.add(n);
        }
        else if(turn.moveType == MoveType.Jump){
            //create move for fromPiece
            Move m = new Move(turn.origin);
            m.after = new Info(false, false, false);
            turn.origin.info = m.after;
            changes.add(m);
            //create move for toPiece
            Move n = new Move(turn.piece);
            n.after = new Info(playerMove, true, turn.piece.info.isKing);
            turn.piece.info = n.after;
            changes.add(n);
            //create move for captured piece
            for(Piece p : turn.capturedPieces){
                Move c = new Move(p);
                c.after = new Info(false, false, false);
                p.info = c.after;
                changes.add(c);
            }
        }
        else{
            //TODO: error - should be advance or jump
        }
        return changes;
    }

    protected void UndoMove(Turn turn) {
        while(!turn.changes.isEmpty()){
            Move m = turn.changes.remove(0);
            Piece piece = allPieces[m.pieceLocation];
            piece.info = m.before;
        }

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
        turn.piece.info.isPlayer = isPlayerTurn;
        turn.piece.info.isActive = true;
        turn.piece.info.isKing = turn.origin.info.isKing || turn.piece.info.isKing;
        ui.UpdateColour(turn.piece);

        turn.origin.info.isPlayer = false;
        turn.origin.info.isActive = false;
        turn.origin.info.isKing = false;

        //clear any captured pieces
        turn.capturedPieces.forEach(p -> {
            p.info.isActive = false;
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
                t.piece.info.isKing = t.piece.info.isActive ? t.piece.info.isKing :  false;
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
            if(p == null || !p.info.isActive){
                continue;
            }
            else if(p.info.isActive && p.info.isPlayer == isPlayerTurn){
                //are all my pieces trapped?
                //if so, game over and I've lost
                if(!FilterMoves(p, p.possibleMoves, MoveType.Both).isEmpty()){
                    thisPlayerTrapped = false;
                }
            }
            else if(p.info.isActive && p.info.isPlayer != isPlayerTurn){
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

    protected boolean InDanger(Piece piece) {
        for(Node adjacentNode : piece.possibleMoves){
            Piece playerPiece = allPieces[adjacentNode.pieceLocation];
            if(playerPiece.info.isPlayer != piece.info.isPlayer && playerPiece.info.isActive){
                //this piece is the opposite player and is active - possible threat
                //get the node that moves the player piece to the passed in piece
                List<Node> moveNodes = playerPiece.possibleMoves.stream().filter(n -> n.pieceLocation == piece.getLocation()).collect(Collectors.toList());
                if(moveNodes.isEmpty()){
                    //TODO: error
                    continue;
                }
                //get the piece that the player piece would move to if it takes the passed in piece
                List<Node> nextNodes = piece.possibleMoves.stream().filter(n -> n.direction == moveNodes.get(0).direction).collect(Collectors.toList());
                if(nextNodes.isEmpty()){
                    //piece is at the edge and the player piece can't take it
                    continue;
                }
                Piece oppositePiece = allPieces[nextNodes.get(0).pieceLocation];
                if(!oppositePiece.info.isActive){
                    return true;
                }

            }
        }
        return false;
    }

    protected void GameOver(String message){
        game.isPaused = true;
        message = isPlayerTurn ? "Congratulations! You won! "  + message: "The AI won! " + message;
        ui.GameOverDialog(message);
    }
}
