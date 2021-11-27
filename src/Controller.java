import Classes.Direction;
import Classes.Node;
import Classes.Piece;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Controller {
    private GamePlay game = new GamePlay();
    public static void main(String[] args) {
        new Controller();
    }

    public Controller() {
        //create board - setup pieces
        UI checkers = new UI(game);
        game.ui = checkers;
        game.allPieces = checkers.GetPieces();
        CreateTree();
    }





    private void CreateTree() {
        for(int i = 1; i < game.allPieces.length; i++){
            Piece p = game.allPieces[i];
            int loc = p.getLocation();
            int x = p.getCol(), y = p.getRow(), g = p.getGridSize();
            int[] indexes = y % 2 == 0 ? new int[]{-5, -4, 3, 4} : new int[]{-4, -3, 4, 5};
            if(loc + indexes[0] >= 1 && y > 1 && x > 1) p.possibleMoves.add(new Node(loc + indexes[0], Direction.UpLeft));
            if(loc + indexes[1] >= 1 && y > 1 && x < g) p.possibleMoves.add(new Node(loc + indexes[1], Direction.UpRight));
            if(loc + indexes[2] <= g * g / 2 && y < g && x > 1) p.possibleMoves.add(new Node(loc + indexes[2], Direction.DownLeft));
            if(loc + indexes[3] <= g * g / 2 && y < g && x < g) p.possibleMoves.add(new Node(loc + indexes[3], Direction.DownRight));
        }
    }


}