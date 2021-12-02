import Classes.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GamePlay {
    protected UI ui;

    protected Controller controller;

    protected Piece[] allPieces;

    protected boolean isForcedCapture;

    protected Difficulty aiDifficulty;

    private TurnHelpers turnHelpers;

    private PlayerTurn playerTurn;

    private AITurn aiTurn;

    private PieceColour playerColour;

    private boolean isPlayerTurn = false;

    protected boolean isPaused = false;

    public GamePlay(UI _ui, Piece[] _allPieces, PieceColour _playerColour, boolean _isForcedCapture, Difficulty _aiDifficulty) {
        ui = _ui;
        allPieces = _allPieces;
        playerColour = _playerColour;
        isForcedCapture = _isForcedCapture;
        aiDifficulty = _aiDifficulty;
        if(playerColour == PieceColour.white){
            AI();
        }
        else{
            isPlayerTurn = true;
        }
    }

    public void pieceClicked(Piece piece) {
        //is it the player's turn?
        if((playerTurn != null && !isPlayerTurn) || isPaused) {
            ui.ShowMessage("The AI is thinking...", Color.darkGray);
            return;
        }



        //are they clicking the first button?
        if (playerTurn == null){
            //have they clicked one of their pieces?
            if(!piece.isActive || !piece.isPlayer){
                ui.ShowMessage("Select one of your pieces", Color.red);
            }
            else{
                playerTurn = new PlayerTurn(ui, allPieces, this, playerColour, piece);
                playerTurn.ShowOptions();
            }
        }
        //piece has been clicked again, deselect
        else if(playerTurn.turn.origin != null && piece == playerTurn.turn.origin){
            playerTurn.RemoveSelection(piece);
            playerTurn = null;
        }
        //are they clicking the second button?
        else if(playerTurn != null){
            //have they selected an empty square?
            if(piece.isActive){
                ui.ShowMessage("Select an empty square", Color.red);
            }
            //have they selected one of the valid moves?
            else if(!piece.isOption)
            {
                ui.ShowMessage("That move is not possible. Select a valid move", Color.red);
            }
            //validation passed - complete the move!
            else{
                playerTurn.ChooseMove(piece);
                playerTurn = null;
                isPlayerTurn = !isPlayerTurn;
                AI();
            }
        }
    }


    private void AI(){
        if(!isPaused){
            isPaused = true;
            ui.ShowMessage("The AI is thinking...", Color.darkGray);
            aiTurn = new AITurn(ui, allPieces, playerColour, this);
            aiTurn.MakeMove();
            isPlayerTurn = !isPlayerTurn;
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

    //restart player's move
    protected void RestartMove(Piece piece) {
        playerTurn.RemoveSelection(piece);
        playerTurn = null;
    }
}
