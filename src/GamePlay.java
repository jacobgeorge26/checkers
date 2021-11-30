import Classes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GamePlay {
    public UI ui;

    private Difficulty aiDifficulty;

    public Piece[] allPieces;

    public boolean isPlayerTurn = true;

    public boolean gameWon = false;

    public Turn playerTurn = new Turn();

    private List<Piece> possibleMoves;

    private int aiDepth = 3;



    public void pieceClicked(Piece piece) {
        //SetupTest();
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
                ui.UpdateColour(piece);
                possibleMoves = new ArrayList<Piece>();
                Search(piece, piece, possibleMoves, MoveType.Both, isPlayerTurn);
                for(Piece p : possibleMoves){
                    p.isOption = true;
                }
            }
            else{
                //TODO: warning to select a valid player's piece
            }
        }
        else if(playerTurn.origin != null && piece == playerTurn.origin){
            //piece has been clicked again, deselect
            //clear options
            for(Piece p : possibleMoves){
                p.isOption = false;
                ui.UpdateColour(p);
            }
            //clear selection
            ClearSelectedPiece(playerTurn);
        }


        //are they clicking the second button?
        if(playerTurn.origin != null && !piece.isActive()){
            playerTurn.piece = piece;
            if(possibleMoves.contains(playerTurn.piece)){
                CompleteTurn(playerTurn);
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

    public void SetupTest() {
        allPieces[7].setActive(false);
        ui.UpdateColour(allPieces[7]);
        allPieces[18].setPlayer(false);
        allPieces[18].setActive(true);
        ui.UpdateColour(allPieces[18]);
    }

    //TODO: what if it reaches the edge and becomes a king halfway through?
    public void Search(Piece source, Piece piece, List<Piece> moves, MoveType legalMoveType, boolean updateColour) {
        if(legalMoveType == MoveType.Neither && piece != source){
            moves.add(piece);
            piece.isOption = true;
            if(updateColour){
                ui.UpdateColour(piece);
            }
            return;
        }

        if(legalMoveType == MoveType.Jump || legalMoveType == MoveType.Both){
            List<Node> jumpMoves = FilterMoves(piece, piece.possibleMoves, MoveType.Jump);
            for(Node n : jumpMoves){
                Piece p = allPieces[n.pieceLocation];
                if(!moves.contains(p) && p != source){
                    moves.add(p);
                    p.isOption = true;
                    if(updateColour){
                        ui.UpdateColour(p);
                    }
                    Search(source, p, moves, MoveType.Jump, updateColour);
                }
            }
        }
        if(legalMoveType == MoveType.Advance || legalMoveType == MoveType.Both){
            List<Node> advanceMoves = FilterMoves(piece, piece.possibleMoves, MoveType.Advance);
            for (Node n : advanceMoves){
                Piece p = allPieces[n.pieceLocation];
                if(!moves.contains(p) && p != source){
                    moves.add(p);
                    p.isOption = true;
                    if(updateColour){
                        ui.UpdateColour(p);
                    }
                    Search(source, p, moves, MoveType.Neither, updateColour);
                }
            }
        }
    }

    private List<Node> FilterMoves(Piece currentPiece, List<Node> possibleMoves, MoveType moveType) {
        List<Node> filteredMoves = new ArrayList<Node>();
        for (Node possN : possibleMoves) {
            if (IsValidDirection(possN, currentPiece)) {
                Piece possP = allPieces[possN.pieceLocation];
                if ((moveType == MoveType.Advance || moveType == MoveType.Both) && !possP.isActive()) {
                    filteredMoves.add(possN);
                }
                if ((moveType == MoveType.Jump || moveType == MoveType.Both) && possP.isPlayer() != isPlayerTurn && possP.isActive()) {
                    Object[] nextNs = possP.possibleMoves.stream().filter(x -> x.direction == possN.direction).toArray();
                    Node nextN = nextNs.length > 0 ? (Node) nextNs[0] : null;
                    if (nextN != null) {
                        Piece nextP = allPieces[nextN.pieceLocation];
                        if (!nextP.isActive()) {
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
            turn.origin = p;
            turn.score = Minimax(turn, p, aiDepth, false, MoveType.Both);
            bestTurn = turn.score > bestTurn.score ? turn : bestTurn;
            System.out.println("Piece " + p.getLocation() + " has score " + turn.score);
        }
        if(bestTurn.score < 2){
            for(Piece p : lowPriority){
                Turn turn = new Turn();
                turn.origin = p;
                turn.score = Minimax(turn, p, aiDepth, false, MoveType.Both);
                bestTurn = turn.score > bestTurn.score ? turn : bestTurn;
                System.out.println("Piece " + p.getLocation() + " has score " + turn.score);
            }
        }
        if(bestTurn.score == 0){
            //TODO: warning - no turns possible - switch player
        }

        System.out.println("MOVING piece " + bestTurn.origin.getLocation() + " to piece " + bestTurn.piece.getLocation());
        CompleteTurn(bestTurn);
    }

    private void CompleteTurn(Turn turn) {
        //move player piece
        turn.piece.setPlayer(isPlayerTurn);
        turn.piece.setActive(true);
        ui.UpdateColour(turn.piece);

        turn.origin.setPlayer(false);
        turn.origin.setActive(false);
        ClearSelectedPiece(turn);

        //clear any captured pieces
        turn.capturedPieces.forEach(p -> {
            p.setActive(false);
            ui.UpdateColour(p);
        });

        isPlayerTurn = !isPlayerTurn;
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
                    : turn.capturedPieces.size();
        }

        int bestValue;
        if (isMin || unexploredJ.isEmpty()){
            bestValue = -1000;
            for (int i = 0; i < unexploredA.size(); i++){
                Piece nextPiece = allPieces[unexploredA.get(i).pieceLocation];
                int eval = Minimax(turn, nextPiece, depth - 1, !isMin, MoveType.Neither);
                bestValue = Math.max(bestValue, eval);
            }
        }
        else{
            bestValue = 1000;
            for(int i = 0; i < unexploredJ.size(); i++){
                Node nextNode = unexploredJ.get(i);
                Piece nextPiece = allPieces[nextNode.pieceLocation];
                Optional<Node> capturedNode = piece.possibleMoves.stream()
                        .filter(p -> p.direction == nextNode.direction).findFirst();
                if(!capturedNode.isPresent()){
                    //TODO: error
                }
                else{
                    turn.capturedPieces.add(allPieces[capturedNode.get().pieceLocation]);
                    int eval = Minimax(turn, nextPiece, depth - 1, !isMin, MoveType.Jump);
                    bestValue = Math.min(bestValue, eval);
                }
            }
        }
        return bestValue;

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
        ui.UpdateColour(needsClearing);

        for(Piece p : possibleMoves){
            p.isOption = false;
            ui.UpdateColour(p);
        }
    }

    //TODO: add other elements to difficulty options
    public void UpdateDifficulty(Difficulty diff) {
        aiDifficulty = diff;
        switch(aiDifficulty){
            case Easy:
                aiDepth = 2;
                break;
            case Medium:
                aiDepth = 3;
                break;
            case Hard:
                aiDepth = 4;
                break;
        }
    }
}
