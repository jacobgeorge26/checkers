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
    public UI ui;

    public Piece[] allPieces;

    private Difficulty aiDifficulty;

    private TurnHelpers turnHelpers;

    private PlayerTurn playerTurn;

    private AITurn aiTurn;

    private PieceColour playerColour;

    private boolean isPlayerTurn = false;

    public GamePlay(UI _ui, Piece[] _allPieces, PieceColour _playerColour) {
        ui = _ui;
        allPieces = _allPieces;
        playerColour = _playerColour;
        UpdateDifficulty(Difficulty.Medium);
        if(playerColour == PieceColour.white){
            AI();
        }
        else{
            isPlayerTurn = true;
        }
    }

    public void pieceClicked(Piece piece) {
        //is it the player's turn?
        if(playerTurn != null && !isPlayerTurn) {
            ui.ShowMessage("The AI is thinking", Color.ORANGE);
            return;
        }


        //are they clicking the first button?
        if (playerTurn == null){
            //have they clicked one of their pieces?
            if(!piece.isActive || !piece.isPlayer){
                ui.ShowMessage("Select one of your pieces", Color.red);
            }
            else{
                playerTurn = new PlayerTurn(ui, allPieces, playerColour, piece);
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
        ui.ShowMessage("The AI is thinking...", Color.darkGray);
        aiTurn = new AITurn(ui, allPieces, playerColour);
        aiTurn.MakeMove();
        isPlayerTurn = !isPlayerTurn;
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
