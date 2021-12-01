import Classes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GamePlay {
    public UI ui;

    public Piece[] allPieces;

    private Difficulty aiDifficulty;

    private TurnHelpers turnHelpers;

    private PlayerTurn playerTurn;

    private AITurn aiTurn;

    public GamePlay(UI _ui, Piece[] _allPieces) {
        ui = _ui;
        allPieces = _allPieces;
    }

    public void pieceClicked(Piece piece) {
        //is it the player's turn?
        if(playerTurn != null && !playerTurn.isPlayerTurn) {
            //TODO: warning that it is not player's turn
            return;
        }

        if(playerTurn == null && (!piece.isActive || !piece.isPlayer)){
            //TODO: error message to select one their pieces
            return;
        }

        //are they clicking the first button?
        if (playerTurn == null){
            playerTurn = new PlayerTurn(ui, allPieces, piece);
        }
        //piece has been clicked again, deselect
        else if(playerTurn.turn.origin != null && piece == playerTurn.turn.origin){
            playerTurn.RemoveSelection(piece);
            playerTurn = null;
        }

        //are they clicking the second button?
        if(playerTurn != null && !piece.isActive){
            playerTurn.ChooseMove(piece);
            playerTurn = null;
            aiTurn = new AITurn(ui, allPieces);
            aiTurn.MakeMove();
        }
        else{
            //TODO: warning to select a non-active square
        }
    }




    //TODO: add other elements to difficulty options
    public void UpdateDifficulty(Difficulty diff) {
        aiDifficulty = diff;
        switch(aiDifficulty){
            case Easy:
                AITurn.aiDepth = 2;
                break;
            case Medium:
                AITurn.aiDepth = 3;
                break;
            case Hard:
                AITurn.aiDepth = 4;
                break;
        }
    }
}
