import Classes.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AITurn extends TurnHelpers{

    protected static int aiDepth;

    public AITurn(UI _ui, Piece[] _allPieces, PieceColour _playerColour, GamePlay _game) {
        super(_ui, _allPieces, _game, _playerColour);
        isPlayerTurn = false;
    }

    public void MakeMove(){
        //get all possible turns
        List<Turn> allTurns = new ArrayList<>();
        //if forced capture is on then only look at the pieces that have a capture available
        List<Piece> forcePieces = game.isForcedCapture ? ForcedCapture(isPlayerTurn) : new ArrayList<>();
        //only look at pieces that have a potential move - less expensive
        List<Piece> potentialTurns = forcePieces.isEmpty() ? GetPriorityPieces(Priority.Both, isPlayerTurn) : forcePieces;
        for(Piece p : potentialTurns){
            List<Turn> pTurns = new ArrayList<Turn>();
            pTurns = Search(p, p, MoveType.Both, pTurns, null, isPlayerTurn, false);
            pTurns.forEach(t -> allTurns.add(t));
        }

        //for each potential turn, run MINIMAX to explore its score. Run the best scoring move
        Turn bestTurn = null;
        for(Turn turn : allTurns){
            turn.score = Minimax(turn, aiDepth, true,Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            bestTurn = bestTurn == null || turn.score > bestTurn.score ? turn : bestTurn;
            System.out.println("Piece " + turn.origin.getLocation() + " has score " + turn.score);
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


    private double Minimax(Turn turn, int depth, boolean isMaximising, double alpha, double beta) {
        //TERMINAL NODE
        if (depth == 0) {
            return turn.score;
        }
        //MAX
        if (isMaximising) {
            return Max(turn, depth, alpha, beta);
        }
        //MIN
        else if (!isMaximising) {
            return Min(turn, depth, alpha, beta);
        }
        else{
            return 0;
            //TODO: error - shouldn't reach this point
        }
    }

    private double Min(Turn turn, int depth, double alpha, double beta) {
        double value = Double.POSITIVE_INFINITY;
        List<Move> moves = DoMove(turn, false);
        moves.forEach(m -> turn.changes.add(m));

        //update score
        //--10 for each captured piece
        turn.score -= turn.capturedPieces.size() * 10;
        //--5 for each captured king
        turn.score -= turn.capturedPieces.stream().filter(p -> p.info.isKing).collect(Collectors.toList()).size() * 5;
        //--5 for becoming a king
        turn.score -= turn.origin.info.isKing != turn.piece.info.isKing && turn.piece.info.isKing ? 5 : 0;

        List<Turn> nextTurns = new ArrayList<>();
        //if forced capture is on then only look at the pieces that have a capture available
        List<Piece> forcePieces = game.isForcedCapture ? ForcedCapture(!isPlayerTurn) : new ArrayList<>();
        //only look at pieces that have a potential move - less expensive
        List<Piece> potentialTurns = forcePieces.isEmpty() ? GetPriorityPieces(Priority.Both, !isPlayerTurn) : forcePieces;
        for(Piece p : potentialTurns){
            List<Turn> pTurns = new ArrayList<Turn>();
            pTurns = Search(p, p, MoveType.Both, pTurns, null, !isPlayerTurn, false);
            pTurns.forEach(t -> nextTurns.add(t));
        }
        for(Turn nextTurn : nextTurns){
            //get the score for this branch (depending on depth it may branch in the next search too - will return best branch)
            double eval = turn.score + Minimax(nextTurn, depth - 1, true, alpha, beta);
            //return the move that the player would make - as it advantages them the most
            value = Math.min(value, eval);
            //alpha-beta pruning
            //if this branch is already known to disadvantage the player then don't bother looking at the rest of it
            beta = Math.min(beta, value);
            if(beta <= alpha){
                break;
            }
        }

        //undo move otherwise it'd shuffle the board
        UndoMove(turn);
        return value;

    }

    private double Max(Turn turn, int depth, double alpha, double beta) {
        double value = Double.NEGATIVE_INFINITY;
        List<Move> moves = DoMove(turn, false);
        moves.forEach(m -> turn.changes.add(m));

        //update score
        //++5 for moving forward
        turn.score += turn.moveType == MoveType.Advance ? 5 : 0;
        //++10 for each captured piece
        turn.score += turn.capturedPieces.size() * 10;
        //++5 for each captured king
        turn.score += turn.capturedPieces.stream().filter(p -> p.info.isKing).collect(Collectors.toList()).size() * 5;
        //++5 for becoming a king
        turn.score += turn.origin.info.isKing != turn.piece.info.isKing && turn.piece.info.isKing ? 5 : 0;

        List<Turn> nextTurns = new ArrayList<>();
        //if forced capture is on then only look at the pieces that have a capture available
        List<Piece> forcePieces = game.isForcedCapture ? ForcedCapture(isPlayerTurn) : new ArrayList<>();
        //only look at pieces that have a potential move - less expensive
        List<Piece> potentialTurns = forcePieces.isEmpty() ? GetPriorityPieces(Priority.Both, isPlayerTurn) : forcePieces;
        for(Piece p : potentialTurns){
            List<Turn> pTurns = new ArrayList<Turn>();
            pTurns = Search(p, p, MoveType.Both, pTurns, null, isPlayerTurn, false);
            pTurns.forEach(t -> nextTurns.add(t));
        }
        for(Turn nextTurn : nextTurns){
            //get the score for this branch (depending on depth it may branch in the next search too - will return best branch)
            double eval = turn.score + Minimax(nextTurn, depth - 1, false,alpha, beta);
            //return the move that the AI would make - as it advantages them the most
            value = Math.max(value, eval);
            //alpha-beta pruning
            //if this branch is already known to disadvantage the AI then don't bother looking at the rest of it
            alpha = Math.max(alpha, value);
            if(alpha >= beta){
                break;
            }
        }
        //undo move otherwise it'd shuffle the board
        UndoMove(turn);
        return value;
    }


}
