import Classes.Board;
import Classes.Piece;
import Components.RoundButton;

import javax.swing.*;
import java.awt.*;

public class Checkers {
    public JPanel rootPanel;
    private JPanel panel;
    private int gridSize;
    Piece[] pieces;

    public Checkers() {
        Board board = new Board();
        gridSize = board.getGridSize();
        pieces = new Piece[gridSize * gridSize/ 2];
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
            if (colour) cellPanel.setBackground(new Color(255, 0, 0));
            else cellPanel.setBackground(new Color(0, 0, 0));

            if (((index < gridSize * 3) || (index + 1 > (gridSize * gridSize) - (3 * gridSize))) && !colour) {
                RoundButton pieceButton = new RoundButton();
                if (index < gridSize * 3) pieceButton.SetColour(new Color(255, 255, 255));
                else pieceButton.SetColour(new Color(255, 0, 0));
                cellPanel.add(pieceButton);

                Piece piece = new Piece();
                piece.setLocation((int) Math.ceil((index + 1)/2));
                piece.setPlayer(!(index < gridSize * 3));
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

    public static void main(String[] args) {
        new Checkers();
    }
}



