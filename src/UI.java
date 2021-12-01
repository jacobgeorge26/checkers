import Classes.Board;
import Classes.Difficulty;
import Classes.Piece;
import Components.RoundButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UI implements ActionListener {
    public JPanel rootPanel;
    private JPanel panel;
    private int gridSize;
    private Piece[] pieces;
    protected GamePlay game;
    private JCheckBoxMenuItem[] aiDiffs = new JCheckBoxMenuItem[3];

    public UI() {
        Board board = new Board();
        gridSize = board.getGridSize();
        pieces = new Piece[(gridSize * gridSize/ 2) + 1];
        CreateBoard();
    }

    private void CreateBoard() {
        JFrame frame = new JFrame();
        SetupOptions(frame);
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
                    piece.isActive = true;
                    piece.isPlayer = false;
                }
                else if(index + 1 > (gridSize * gridSize) - (3 * gridSize)){
                    piece.isActive = true;
                    piece.isPlayer = true;
                }
                else{
                    piece.isActive = false;
                    piece.isPlayer = false;
                }
                //setup event handler
                pieceButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        game.pieceClicked(piece);
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

    private void SetupOptions(JFrame frame) {
        JMenuBar optionMenu = new JMenuBar();
        JMenu aiDifficulty = new JMenu("AI Difficulty");
        aiDifficulty.setPreferredSize(new Dimension(100, 20));

        JCheckBoxMenuItem easy = new JCheckBoxMenuItem("Easy");
        easy.addActionListener(this);
        JCheckBoxMenuItem med = new JCheckBoxMenuItem("Medium");
        med.addActionListener(this);
        JCheckBoxMenuItem hard = new JCheckBoxMenuItem("Hard");
        hard.addActionListener(this);
        aiDiffs = new JCheckBoxMenuItem[]{easy, med, hard};
        med.doClick();

        aiDifficulty.add(easy);
        aiDifficulty.add(med);
        aiDifficulty.add(hard);

        optionMenu.add(aiDifficulty);
        frame.setJMenuBar(optionMenu);
    }


    public void UpdateColour(Piece piece) {
        DisplayKingIcon(piece);

        if(piece.isOption)
        {
            piece.button.SetColour(new Color(0, 125, 0));
        }
        else if(piece.isActive && piece.isPlayer && piece.isSelected){
            piece.button.SetColour(new Color(125, 0, 0));
        }
        else if (piece.isActive && piece.isPlayer){
            piece.button.SetColour(new Color(255, 0, 0));
        }
        else if(piece.isActive && !piece.isPlayer){
            piece.button.SetColour(new Color(255, 255, 255));
        }
        else{
            piece.button.SetColour(new Color(0, 0, 0));
        }
    }

    private void DisplayKingIcon(Piece piece) {
        if(piece.isKing){
            try{
                Image img = ImageIO.read(getClass().getResource("king.png"));
                Image newimg = img.getScaledInstance( piece.button.GetSize() / 2, piece.button.GetSize() / 2,  java.awt.Image.SCALE_SMOOTH ) ;
                piece.button.setIcon(new ImageIcon(newimg));
            }
            catch(Exception ex){
                //TODO: error message
            }
        }
        else{
            piece.button.setIcon(null);
        }
    }

    public Piece[] GetPieces(){
        return pieces;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        //AI difficulty options
        if(source == aiDiffs[0]){ //easy
            aiDiffs[0].setState(true);
            aiDiffs[1].setState(false);
            aiDiffs[2].setState(false);
            game.UpdateDifficulty(Difficulty.Easy);
        }
        else if(source == aiDiffs[1]){
            aiDiffs[0].setState(false);
            aiDiffs[1].setState(true);
            aiDiffs[2].setState(false);
        }
        else if(source == aiDiffs[2]){
            aiDiffs[0].setState(false);
            aiDiffs[1].setState(false);
            aiDiffs[2].setState(true);
        }
    }
}



