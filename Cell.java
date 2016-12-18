public class Cell {
    public int x, y, strength, production;
    public GameMap gameMap;
    public double[] weights = {1.0, 0.0, 0.0, 0.0, 0.0}; //same indices as the enum's "fromInteger"
    public int needs = 0; //how much strength that needs to be added to this cell for it to capture something next frame.
    public Move m;
    public GoalCell gC; //this should be null if the cell is on the border.
    
    public Cell(GameMap gM, Location l) {
        gameMap = gM;
        x = l.x;
        y = l.y;
        Site s = gameMap.getSite(new Location(x, y));
        strength = s.strength;
        production = s.production;
    }
    public Cell(GameMap gM, int x_, int y_) {
        gameMap = gM;
        x = x_;
        y = y_;
        Site s = gameMap.getSite(new Location(x, y));
        strength = s.strength;
        production = s.production;
    }
}