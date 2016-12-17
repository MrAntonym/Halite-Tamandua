import java.util.ArrayList;

public class SlothBot3 {
    public static void main (String[] args) throws java.io.IOException {
        InitPackage iPackage = Networking.getInit();
        int myID = iPackage.myID;
        GameMap gameMap = iPackage.map;
        
        Networking.sendInit("SlothBot3");
        
        while (true) {
            ArrayList<Move> moves = new ArrayList<Move>();
            
            gameMap = Networking.getFrame();
            
            ArrayList<Location> borderList = findBorder(gameMap, myID);
            
            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {
                    Site site = gameMap.getSite(new Location(x, y));
                    if (site.owner == myID) {
                        moves.add(makeMove(x,y,gameMap, borderList, myID));
                    }
                }
            }
            Networking.sendFrame(moves);
        }
    }
    
    public static ArrayList<Location> findBorder(GameMap gameMap, int myID) {
        ArrayList<Location> borderList = new ArrayList<Location>();
        for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {
                    Site site = gameMap.getSite(new Location(x, y));
                    if (site.owner == myID) {
                        for (Direction d : Direction.CARDINALS) {
                            if (gameMap.getSite(new Location(x, y), d).owner != myID) {
                                borderList.add(new Location(x, y));
                            }
                        }
                    }
                }
            }
        return borderList;
    }
    
    public static Move makeMove(int x, int y, GameMap gameMap, ArrayList<Location> borderList, int myID) {
        //change this factor to influence speed of movement
        int factor = 4;
        
        Location location = new Location(x, y);
        ArrayList<Direction> possibleDirections = new ArrayList<Direction>();
        for (Direction d : Direction.CARDINALS) {
            if (gameMap.getSite(location, d).owner != myID && gameMap.getSite(location, d).strength < gameMap.getSite(location).strength) {
                possibleDirections.add(d);
            }
        }
        if (possibleDirections.size() > 1) {
            return new Move(location, determineImportance(location, possibleDirections, gameMap));
        } else if (possibleDirections.size() == 1) {
            return new Move(location, possibleDirections.get(0));
        }
        
        if (containsLocation(borderList, location)) {
            return new Move(location, Direction.STILL);
        } else {
            if (gameMap.getSite(location).strength > gameMap.getSite(location).production * factor) {
                return new Move(location, toNearest(location, borderList));
            } else {
                return new Move(location, Direction.STILL);
            }
        }
    }
    
    public static boolean containsLocation(ArrayList<Location> list, Location location) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).x == location.x && list.get(i).y == location.y) {
                return true;
            }
        }
        return false;
    }
    
    public static Direction toNearest(Location location, ArrayList<Location> borderList) {
        int x = location.x;
        int y = location.y;
        //Direction.NORTH = y++
        //Direction.SOUTH = y--
        //Direction.EAST = x++
        //Direction.WEST = x--
        int closest = 0;
        for (int i = 1; i < borderList.size(); i++) {
            if (Math.abs(x - borderList.get(i).x) + Math.abs(y - borderList.get(i).y) < Math.abs(x - borderList.get(closest).x) + Math.abs(y - borderList.get(closest).y)) {
                closest = i;
            }
        }
        int x2 = borderList.get(closest).x;
        int y2 = borderList.get(closest).y;
        boolean xDirection;
        if (Math.abs(x - x2) == Math.abs(y - y2)) {
            if ((int) (Math.random() * 2) == 0) {
                xDirection = true;
            } else {
                xDirection = false;
            }
        } else if (Math.abs(x - x2) > Math.abs(y - y2)) {
            xDirection = true;
        } else {
            xDirection = false;
        }
        if (xDirection) {
            if (x - x2 > 0) {
                return Direction.WEST;
            } else {
                return Direction.EAST;
            }
        } else {
            if (y - y2 > 0) {
                return Direction.NORTH;
            } else {
                return Direction.SOUTH;
            }
        }
    }
    
    public static Direction determineImportance(Location location, ArrayList<Direction> directions, GameMap gameMap) {
        int best = 0;
        for (int i = 1; i < directions.size(); i++) {
            if (gameMap.getSite(location, directions.get(best)).strength == 0 && gameMap.getSite(location, directions.get(i)).strength == 0) {
                if (gameMap.getSite(location, directions.get(best)).production < gameMap.getSite(location, directions.get(i)).production) {
                    best = i;
                }
            } else if (gameMap.getSite(location, directions.get(best)).strength == 0) {
                //dodge division by zero
            } else if (gameMap.getSite(location, directions.get(i)).strength == 0) {
                if (gameMap.getSite(location, directions.get(i)).production > 0) {
                    best = i;
                } //else dodge division by zero
            } else if (gameMap.getSite(location, directions.get(best)).production / gameMap.getSite(location, directions.get(best)).strength < gameMap.getSite(location, directions.get(i)).production / gameMap.getSite(location, directions.get(i)).strength) {
                best = i;
            }
        }
        return directions.get(best);
    }
}