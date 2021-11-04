package Classes;

public class Tile extends Board{
    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    protected int location;

    public int index = (int) Math.floor(location / gridSize);

    protected boolean isTaken = false;
}
