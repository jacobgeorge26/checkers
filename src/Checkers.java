import Classes.Board;
import Classes.Piece;
import Classes.Turn;
import Components.RoundButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Checkers {
    public JPanel rootPanel;
    private JPanel panel;
    private int gridSize;
    private Piece[] pieces;
    protected GamePlay game;

    public Checkers(GamePlay _game) {
        game = _game;
        Board board = new Board();
        gridSize = board.getGridSize();
        pieces = new Piece[(gridSize * gridSize/ 2) + 1];
        CreateBoard();
    }

    private void CreateBoard() {
        JFrame frame = new JFrame();
        panel = new JPanel(new GridLayout(gridSize, gridSize));
        boolean colour = true;
        for (int index = 0; index < gridSize * gridSize; index++) {
            //setup panel for grid colour
            JPanel cellPanel = new JPanel();
            if (index % gridSize != 0) colour = !colour;
            if (colour) cellPanel.setBackground(new Color(255, 255, 255));
            else cellPanel.setBackground(new Color(0, 0, 0));

            if(!colour){
                Piece piece = new Piece();
                piece.setLocation((int) Math.ceil(((double)index + 1)/2));

                RoundButton pieceButton = new RoundButton();
                if(index < gridSize * 3)
                {
                    piece.setActive(true);
                    piece.setPlayer(false);
                }
                else if(index + 1 > (gridSize * gridSize) - (3 * gridSize)){
                    piece.setActive(true);
                    piece.setPlayer(true);
                }
                else{
                    piece.setActive(false);
                    piece.setPlayer(false);
                }
                //setup event handler
                pieceButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        game.pieceClicked(new Turn(piece));
                    }
                });

                piece.button = pieceButton;
                UpdateColour(piece);
                cellPanel.add(pieceButton);


                pieces[piece.getLocation()] = piece;

            }
            panel.add(cellPanel);
        }

        panel.setPreferredSize(new Dimension(500, 500));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;

        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
    }

    //TODO: move to GamePlay


    public void UpdateColour(Piece piece) {
        if(piece.isActive() && piece.isPlayer() && game.selectedPiece == piece){
            piece.button.SetColour(new Color(125, 0, 0));
        }
        else if (piece.isActive() && piece.isPlayer()){
            piece.button.SetColour(new Color(255, 0, 0));
        }
        else if(piece.isActive() && !piece.isPlayer()){
            piece.button.SetColour(new Color(255, 255, 255));
        }
        else{
            piece.button.SetColour(new Color(0, 0, 0));
        }
    }

    public Piece[] GetPieces(){
        return pieces;
    }

}



