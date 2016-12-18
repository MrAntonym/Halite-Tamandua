import java.util.ArrayList;

public class Tamandua1 {
    //Direction class has been changed, add new version to submission package.
    public static void main(String[] args) throws java.io.IOException {
        InitPackage iPackage = Networking.getInit();
        int myID = iPackage.myID;
        int neutralID = 0;
        GameMap gameMap = iPackage.map;

        Networking.sendInit("Tamandua");
        
        while (true) {
            ArrayList<Cell> cells = new ArrayList<Cell>();
            
            gameMap = Networking.getFrame();
            
            //Create an object for every player-owned cell
            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {
                    Site site = gameMap.getSite(new Location(x, y));
                    if (site.owner == myID) {
                        cells.add(new Cell(gameMap, new Location(x, y)));
                    }
                }
            }
            
            //Add all cells to appropriate lists for neutral borders and enemy borders.
            ArrayList<Cell> nBorderCells = new ArrayList<Cell>();//neutral border
            ArrayList<Cell> eBorderCells = new ArrayList<Cell>();//enemy border
            ArrayList<Cell> toRemove = new ArrayList<Cell>();
            for (Cell c : cells) {
                boolean nBorder = false;
                boolean eBorder = false;
                for (Direction d : Direction.CARDINALS) {
                    if (gameMap.getSite(new Location(c.x, c.y), d).owner == 0) {
                        nBorder = true;
                    } else if (gameMap.getSite(new Location(c.x, c.y), d).owner != myID) {
                        eBorder = true;
                    }
                }
                if (eBorder) {
                    eBorderCells.add(c);
                } else if (nBorder) {
                    nBorderCells.add(c);
                }
            }
            
            //check for any enemy cell that is capturable - cells on the enemy border will never attack a neutral cell in order to avoid losing short-term strength which is more important against an enemy bot.
            for (Cell c : eBorderCells) {
                for (int d = 1; d <= 4; d++) {
                    Site s = gameMap.getSite(new Location(c.x, c.y), Direction.fromInteger(d));
                    if (s.owner != myID && s.owner != 0) {
                        if (s.strength < c.strength) {
                            //THESE NUMBERS AND RATIOS MAY BE CHANGED IN ORDER TO BETTER WEIGH OPTIONS
                            c.weights[d] = 1 + (s.production / (s.strength + 1));
                            toRemove.add(c);
                        }
                    }
                }
            }
            //remove from the list all cells which have decided weights - they won't need to be calculated on their "needs" value.
            for (Cell c : toRemove) {
                eBorderCells.remove(c);
            }
            
            //check for any neutral cells that are capturable.
            for (Cell c : nBorderCells) {
                for (int d = 1; d <= 4; d++) {
                    Site s = gameMap.getSite(new Location(c.x, c.y), Direction.fromInteger(d));
                    if (s.owner != myID) {
                        if (s.strength < c.strength) {
                            //THESE NUMBERS ARE RATIOS MAY BE CHANGED IN ORDER TO BETTER WEIGH OPTIONS
                            c.weights[d] = 1 + (s.production / (s.strength + 1));
                            toRemove.add(c);
                        }
                    }
                }
            }
            //remove from the list all cells which have decided their weights - they won't need to be calculated on their "needs" value.
            for (Cell c : toRemove) {
                nBorderCells.remove(c);
            }
            
            //calculate the needs for the cells in eBorderCells
            for (Cell c : eBorderCells) {
                int minD = 0;
                for (int d = 1; d <= 4; d++) {
                    Site s = gameMap.getSite(new Location(c.x, c.y), Direction.fromInteger(d));
                    if (s.owner != myID && s.owner != 0) {
                        if (minD == 0) {
                            minD = d;
                        } else if (s.strength < gameMap.getSite(new Location(c.x, c.y), Direction.fromInteger(minD)).strength) {
                            minD = d;
                        }
                    }
                }
                c.needs = gameMap.getSite(new Location(c.x, c.y), Direction.fromInteger(minD)).strength - c.strength - c.production;
            }
            
            //calculate the needs for the cells in nBorderCells
            for (Cell c : nBorderCells) {
                int minD = 0;
                for (int d = 1; d <= 4; d++) {
                    Site s = gameMap.getSite(new Location(c.x, c.y), Direction.fromInteger(d));
                    if (s.owner != myID) {
                        if (minD == 0) {
                            minD = d;
                        } else if (s.strength < gameMap.getSite(new Location(c.x, c.y), Direction.fromInteger(minD)).strength) {
                            minD = d;
                        }
                    }
                }
                c.needs = gameMap.getSite(new Location(c.x, c.y), Direction.fromInteger(minD)).strength - c.strength - c.production;
            }
            
            //calculate weights for non-border cells.
            ArrayList<Cell> nonBorder = new ArrayList<Cell>();
            
            ArrayList<Cell> borderCells = new ArrayList<Cell>();
            for (Cell c : nBorderCells) {
                borderCells.add(c);
            }
            for (Cell c : eBorderCells) {
                borderCells.add(c);
            }
            
            for (Cell c : cells) {
                if (!(eBorderCells.contains(c) || nBorderCells.contains(c) || toRemove.contains(c))) {
                    nonBorder.add(c);
                    c.gC = toNearest(c, borderCells, gameMap);
                    //add the strength and production of all the cells which follow this one in its path to the border.
                    /**
                    Temporary measure to test other improvements
                    **/
                    if (c.strength > c.production * 3) {
                        switch (c.gC.d) {
                            case NORTH: c.weights[1] += 2; break;
                            case EAST: c.weights[2] += 2; break;
                            case SOUTH: c.weights[3] += 2; break;
                            case WEST: c.weights[4] += 2; break;
                        }
                    }
                    /**
                    End of temporary measure to test other improvements
                    **/
                }
            }
            
            ArrayList<Move> moves = new ArrayList<Move>();
            for (Cell c : cells) {
                int bestWeight = 0;
                for (int i = 1; i < 5; i++) {
                    if (c.weights[i] > c.weights[bestWeight]) {
                        bestWeight = i;
                    }
                }
                moves.add(new Move(new Location(c.x, c.y), Direction.fromInteger(bestWeight)));
            }
            
            Networking.sendFrame(moves);
        }
    }
    
    public static GoalCell toNearest(Cell c, ArrayList<Cell> border, GameMap gameMap) {
        int x = c.x;
        int y = c.y;
        int closest = 0;
        for (int i = 1; i < border.size(); i++) {
            if (gameMap.getDistance(new Location(x, y), new Location(border.get(i).x, border.get(i).y)) < gameMap.getDistance(new Location(x, y), new Location(border.get(closest).x, border.get(closest).y))) {
                closest = i;
            }
        }
        int x2 = border.get(closest).x;
        int y2 = border.get(closest).y;
        boolean xDirection;
        boolean wrapX = false;
        boolean wrapY = false;
        int dx = Math.abs(x - x2);
        int dy = Math.abs(y - y2);
        if (dx > gameMap.width / 2.0) {
            dx = gameMap.width - dx;
            wrapX = true;
        }
        if (dy > gameMap.height / 2.0) {
            dy = gameMap.height - dy;
            wrapY = true;
        }
        if (dx == dy) {
            if ((int) (Math.random() * 2) == 0) {
                xDirection = true;
            } else {
                xDirection = false;
            }
        } else if (dx > dy) {
            xDirection = true;
        } else {
            xDirection = false;
        }
        if (xDirection && !wrapX) {
            if (x - x2 > 0) {
                GoalCell output = new GoalCell(gameMap, new Location(x2, y2), Direction.WEST);
                return output;
            } else {
                GoalCell output = new GoalCell(gameMap, new Location(x2, y2), Direction.EAST);
                return output;
            }
        } else if (!xDirection && !wrapY) {
            if (y - y2 > 0) {
                GoalCell output = new GoalCell(gameMap, new Location(x2, y2), Direction.NORTH);
                return output;
            } else {
                GoalCell output = new GoalCell(gameMap, new Location(x2, y2), Direction.SOUTH);
                return output;
            }
        } else if (xDirection && wrapX) {
            if (x - x2 > 0) {
                GoalCell output = new GoalCell(gameMap, new Location(x2, y2), Direction.EAST);
                return output;
            } else {
                GoalCell output = new GoalCell(gameMap, new Location(x2, y2), Direction.WEST);
                return output;
            }
        } else {
            if (y - y2 > 0) {
                GoalCell output = new GoalCell(gameMap, new Location(x2, y2), Direction.SOUTH);
                return output;
            } else {
                GoalCell output = new GoalCell(gameMap, new Location(x2, y2), Direction.NORTH);
                return output;
            }
        }
    }
}