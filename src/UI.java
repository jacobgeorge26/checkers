import Classes.Board;
import Classes.Difficulty;
import Classes.Piece;
import Classes.PieceColour;
import Components.RoundButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UI implements ActionListener {
    private JFrame frame;
    public JPanel rootPanel;
    private int gridSize;
    private Piece[] pieces;
    protected Controller controller;
    protected PieceColour playerColour;
    private JCheckBoxMenuItem[] aiDiffs = new JCheckBoxMenuItem[3];
    private JMenu reset;
    private JTextField messageBox;

    public UI(PieceColour _playerColour, Controller _controller) {
        playerColour = _playerColour;
        controller = _controller;
        Board board = new Board();
        gridSize = board.getGridSize();
        pieces = new Piece[(gridSize * gridSize/ 2) + 1];
        CreateBoard();
    }

    private void CreateBoard() {
        //setup main layout
        frame = new JFrame();
        SetupOptions(frame);
        rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.PAGE_AXIS));
        SetupMessageBox();
        rootPanel.add(messageBox);

        //setup board
        JPanel panel = new JPanel(new GridLayout(gridSize, gridSize));
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
                    piece.isPlayer = playerColour == PieceColour.white;
                }
                else if(index + 1 > (gridSize * gridSize) - (3 * gridSize)){
                    piece.isActive = true;
                    piece.isPlayer = playerColour == PieceColour.red;
                }
                else{
                    piece.isActive = false;
                    piece.isPlayer = false;
                }
                //setup event handler
                pieceButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.ClickPiece(piece);
                    }
                });

                piece.button = pieceButton;
                UpdateColour(piece);
                cellPanel.add(pieceButton);
                cellPanel.setAlignmentY(SwingConstants.CENTER);


                pieces[piece.getLocation()] = piece;

            }
            panel.add(cellPanel);
        }

        //piece it all together
        panel.setPreferredSize(new Dimension(500, 500));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;

        frame.setContentPane(rootPanel);
        rootPanel.add(panel);

        frame.pack();
        frame.setVisible(true);
    }

    private void SetupMessageBox() {
        messageBox = new JFormattedTextField();
        messageBox.setEnabled(false);
        messageBox.setFont(new Font("Arial", Font.PLAIN, 18));
        messageBox.setHorizontalAlignment(SwingConstants.LEFT);
        ShowMessage("Good luck!", Color.darkGray);
    }

    private void SetupOptions(JFrame frame) {
        JMenuBar optionMenu = new JMenuBar();

        //AI difficulty options
        JMenu aiDifficulty = new JMenu("AI Difficulty");
        aiDifficulty.setPreferredSize(new Dimension(100, 30));

        JCheckBoxMenuItem easy = new JCheckBoxMenuItem("Easy");
        easy.addActionListener(this);
        JCheckBoxMenuItem med = new JCheckBoxMenuItem("Medium");
        med.addActionListener(this);
        JCheckBoxMenuItem hard = new JCheckBoxMenuItem("Hard");
        hard.addActionListener(this);
        aiDiffs = new JCheckBoxMenuItem[]{easy, med, hard};
        aiDifficulty.add(easy);
        aiDifficulty.add(med);
        aiDifficulty.add(hard);

        //Reset game option
        reset = new JMenu("Reset");
        reset.setPreferredSize(new Dimension(100, 30));
        JMenuItem resetGame = new JMenuItem("Reset Game");
        resetGame.addActionListener(this);
        reset.add(resetGame);
        JMenuItem resetColour = new JMenuItem("Switch Colour");
        resetColour.addActionListener(this);
        reset.add(resetColour);


        optionMenu.add(reset);
        optionMenu.add(aiDifficulty);
        frame.setJMenuBar(optionMenu);
    }

    public void ShowMessage(String message, Color boxColor){
        messageBox.setText(message);
        messageBox.setBorder(new LineBorder(boxColor));
        Timer timer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messageBox.setText("");
                messageBox.setBorder(new LineBorder(Color.darkGray));
            }
        });
        timer.start();
    }


    public void UpdateColour(Piece piece) {
        DisplayKingIcon(piece);

        if(piece.isOption)
        {
            piece.button.SetColour(new Color(0, 125, 0));
        }
        else if(piece.isActive && piece.isPlayer && piece.isSelected){
            if(playerColour == PieceColour.red){
                piece.button.SetColour(new Color(125, 0, 0));
            }
            else{
                piece.button.SetColour(new Color(128, 128, 128));
            }
        }
        else if (piece.isActive){
            if((piece.isPlayer && playerColour == PieceColour.red) || (!piece.isPlayer && playerColour == PieceColour.white)){
                piece.button.SetColour(new Color(255, 0, 0));
            }
            else{
                piece.button.SetColour(new Color(255, 255, 255));
            }

        }
        else{
            piece.button.SetColour(new Color(0, 0, 0));
        }
    }

    private void DisplayKingIcon(Piece piece) {
        if(piece.isKing && !piece.isOption){
            try{
                Image img = ImageIO.read(getClass().getResource("king.png"));
                Image newimg = img.getScaledInstance( piece.button.GetSize() / 2, piece.button.GetSize() / 2,  java.awt.Image.SCALE_SMOOTH ) ;
                piece.button.setIcon(new ImageIcon(newimg));
            }
            catch(Exception ex){
                ShowMessage("There has been an error in UI/DisplayKingIcon. There was an issue displaying the king icon in piece " + piece.getLocation(), Color.red);
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
        if(source == aiDiffs[0]){
            //easy AI
            aiDiffs[0].setState(true);
            aiDiffs[1].setState(false);
            aiDiffs[2].setState(false);
            controller.UpdateDifficulty(Difficulty.Easy);
        }
        else if(source == aiDiffs[1]){
            //medium AI
            aiDiffs[0].setState(false);
            aiDiffs[1].setState(true);
            aiDiffs[2].setState(false);
            controller.UpdateDifficulty(Difficulty.Medium);
        }
        else if(source == aiDiffs[2]){
            //hard AI
            aiDiffs[0].setState(false);
            aiDiffs[1].setState(false);
            aiDiffs[2].setState(true);
            controller.UpdateDifficulty(Difficulty.Hard);
        }
        //Reset options
        else if(source == reset.getItem(0)){
            //reset game
            int n = JOptionPane.showConfirmDialog(frame,
                    "You will lose any progress you have made.", "Reset Game?",
                    JOptionPane.YES_NO_OPTION);
            if(n == 0){
                controller.ResetGame(playerColour);
            }
        }
        else if(source == reset.getItem(1)){
            //reset game as other colour
            int n = JOptionPane.showConfirmDialog(frame,
                    "You will lose any progress you have made.", "Switch Colour?",
                    JOptionPane.YES_NO_OPTION);
            if(n == 0){
                //swap colour
                playerColour = playerColour == PieceColour.red ? PieceColour.white : PieceColour.red;
                controller.ResetGame(playerColour);
            }
        }
    }

    protected JFrame GetFrame() {
        return frame;
    }
}



