public class GoalCell extends Cell {
    public Direction d;
    
    public GoalCell(GameMap gM, Location l, Direction d_) {
        super(gM, l);
        d = d_;
    }
}