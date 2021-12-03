import Classes.*;
import javax.swing.*;
import java.awt.event.WindowEvent;

public class Controller {
    private UI ui;
    private GamePlay game;
    public static void main(String[] args) {
        new Controller(PieceColour.red, true, Difficulty.Easy);
    }
    private PieceColour defaultPlayerColour;

    public Controller(PieceColour _playerColour, boolean isForcedCapture, Difficulty aiDifficulty) {
        defaultPlayerColour = _playerColour;
        //create board - setup pieces
        //give game controller to UI (to create bridge to invoke event methods)
        ui = new UI(defaultPlayerColour, this);
        //create tree for game controller to search
        Piece[] allPieces = ui.GetPieces();;
        CreateTree(allPieces);
        //create game
        game = new GamePlay(ui, allPieces, defaultPlayerColour, isForcedCapture, aiDifficulty);
        ui.InitialiseDifficulty(aiDifficulty);
        ui.InitialiseCapture(isForcedCapture);
    }





    private void CreateTree(Piece[] pieces) {
        for(int i = 1; i < pieces.length; i++){
            Piece p = pieces[i];
            int loc = p.getLocation();
            int x = p.getCol(), y = p.getRow(), g = p.getGridSize();
            int[] indexes = y % 2 == 0 ? new int[]{-5, -4, 3, 4} : new int[]{-4, -3, 4, 5};
            if(loc + indexes[0] >= 1 && y > 1 && x > 1) p.possibleMoves.add(new Node(loc + indexes[0], Direction.UpLeft));
            if(loc + indexes[1] >= 1 && y > 1 && x < g) p.possibleMoves.add(new Node(loc + indexes[1], Direction.UpRight));
            if(loc + indexes[2] <= g * g / 2 && y < g && x > 1) p.possibleMoves.add(new Node(loc + indexes[2], Direction.DownLeft));
            if(loc + indexes[3] <= g * g / 2 && y < g && x < g) p.possibleMoves.add(new Node(loc + indexes[3], Direction.DownRight));
        }
    }


    protected void ClickPiece(Piece piece) {
        game.pieceClicked(piece);
    }

    public void UpdateDifficulty(Difficulty diff) {
        game.UpdateDifficulty(diff);
    }

    public void ResetGame(PieceColour playerColour) {
        new Controller(playerColour, game.isForcedCapture, game.aiDifficulty);
        JFrame currentFrame = ui.GetFrame();
        currentFrame.dispatchEvent(new WindowEvent(currentFrame, WindowEvent.WINDOW_CLOSING));
    }

    public void ToggleForceCapture(boolean isForcedCapture) {
        game.isForcedCapture = isForcedCapture;
    }

    public void CloseGame() {
        JFrame currentFrame = ui.GetFrame();
        currentFrame.dispatchEvent(new WindowEvent(currentFrame, WindowEvent.WINDOW_CLOSING));
    }
}
