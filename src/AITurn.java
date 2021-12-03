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
        List<Piece> potentialTurns = GetPriorityPieces(Priority.Both, isPlayerTurn);
        for(Piece p : potentialTurns){
            List<Turn> pTurns = new ArrayList<Turn>();
            pTurns = Search(p, p, MoveType.Both, pTurns, null, isPlayerTurn, false);
            pTurns.forEach(t -> allTurns.add(t));
        }

        Turn bestTurn = null;
        for(Turn turn : allTurns){
            turn.score = Minimax(turn, aiDepth, true,-1000, 1000);
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


    private int Minimax(Turn turn, int depth, boolean isMaximising, int alpha, int beta) {
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

    private int Min(Turn turn, int depth, int alpha, int beta) {
        int value = 10000;
        List<Move> moves = DoMove(turn, false);
        moves.forEach(m -> turn.changes.add(m));

        //update score
        //--5 for being vulnerable
        //--10 for being captured

        List<Turn> nextTurns = new ArrayList<>();
        List<Piece> potentialTurns = GetPriorityPieces(Priority.Both, isPlayerTurn);
        for(Piece p : potentialTurns){
            List<Turn> pTurns = new ArrayList<Turn>();
            pTurns = Search(p, p, MoveType.Both, pTurns, null, isPlayerTurn, false);
            pTurns.forEach(t -> nextTurns.add(t));
        }
        for(Turn nextTurn : nextTurns){
            int eval = turn.score + Minimax(nextTurn, aiDepth - 1, true,alpha, beta);
            value = Math.min(value, eval);
            beta = Math.min(beta, value);
            if(beta <= alpha){
                break;
            }
            System.out.println("Layer " + aiDepth + ";  Piece " + nextTurn.origin.getLocation() + ";   Score " + nextTurn.score);
        }

        //undo move otherwise it'd shuffle the board
        UndoMove(turn);
        return value;

    }

    private int Max(Turn turn, int depth, int alpha, int beta) {
        int value = -10000;
        List<Move> moves = DoMove(turn, false);
        moves.forEach(m -> turn.changes.add(m));

        //update score
        //++5 for moving forward
        turn.score += turn.moveType == MoveType.Advance ? 5 : 0;
        //++10 for each
        turn.score += turn.capturedPieces.size() * 10;
        //++5 for capturing a king
        boolean capturedKing = turn.capturedPieces.stream().filter(p -> p.info.isKing).collect(Collectors.toList()).size() > 0;
        turn.score += capturedKing ? 5 : 0;
        //++5 for becoming a king
        turn.score += turn.origin.info.isKing != turn.piece.info.isKing && turn.piece.info.isKing ? 5 : 0;

        if(turn.origin.getLocation() == 9 && turn.piece.getLocation() == 14){
            System.out.println("here");
        }

        List<Turn> nextTurns = new ArrayList<>();
        List<Piece> potentialTurns = GetPriorityPieces(Priority.Both, !isPlayerTurn);
        for(Piece p : potentialTurns){
            List<Turn> pTurns = new ArrayList<Turn>();
            pTurns = Search(p, p, MoveType.Both, pTurns, null, !isPlayerTurn, false);
            pTurns.forEach(t -> nextTurns.add(t));
        }
        for(Turn nextTurn : nextTurns){
            int eval = turn.score + Minimax(nextTurn, aiDepth - 1, false,alpha, beta);
            value = Math.max(value, eval);
            alpha = Math.max(alpha, value);
            if(alpha >= beta){
                break;
            }
            System.out.println("Layer " + aiDepth + ";  Piece " + nextTurn.origin.getLocation() + ";   Score " + nextTurn.score);
        }

        //undo move otherwise it'd shuffle the board
        UndoMove(turn);
        return value;
    }


}
