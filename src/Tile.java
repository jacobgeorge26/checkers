public class Tile extends Board{
    protected int location;

    protected int index = (int) Math.floor(location / gridSize);

    protected boolean isTaken = false;
}
