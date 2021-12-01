package Classes;

public class Tile extends Board{
    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    protected int location;

    public int getRow() {
        return (int)Math.ceil((double)location / ((double)gridSize / 2));
    }

    public int getCol() {
        double x = (double) location / ((double) gridSize / 2);
        int r = (int)(Math.ceil(x));
        double c = (10 * x - 10 * (r - 1))/10 * 8;
        c = r % 2 == 0 ? c - 1 : c;
        return (int)c;
    }

    protected boolean isTaken = false;
}
