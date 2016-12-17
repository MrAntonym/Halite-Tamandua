import java.util.ArrayList;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class SlothBot {
    public static void main(String[] args) throws java.io.IOException {
        InitPackage iPackage = Networking.getInit();
        int myID = iPackage.myID;
        GameMap gameMap = iPackage.map;

        Networking.sendInit("SlothJavaBot");
        
        int frame = 0;
        
        Writer writer = new Writer("slothLog.txt");
        
        //fix the problem where it refuses to move

        while(true) {
            frame++;
            
            ArrayList<Move> moves = new ArrayList<Move>();
            ArrayList<Location> owned = new ArrayList<Location>();
            ArrayList<Location> border = new ArrayList<Location>();
            ArrayList<Location> ownedBorder = new ArrayList<Location>();

            gameMap = Networking.getFrame();

            //adds all important locations to lists
            for(int y = 0; y < gameMap.height; y++) {
                for(int x = 0; x < gameMap.width; x++) {
                    Site site = gameMap.getSite(new Location(x, y));
                    if(site.owner == myID) {
                        owned.add(new Location(x, y));
                        writer.write(frame + " (" + x + "," + y + ") was added to owned");
                    } else {
                        if(gameMap.getSite(new Location(x, y), Direction.NORTH).owner != myID) {
                            ownedBorder.add(new Location(x, y));
                            border.add(gameMap.getLocation(new Location(x, y), Direction.NORTH));
                            writer.write(frame + " (" + x + "," + y + ") was added to ownedBorder");
                        } else if (gameMap.getSite(new Location(x, y), Direction.EAST).owner != myID) {
                            ownedBorder.add(new Location(x, y));
                            border.add(gameMap.getLocation(new Location(x, y), Direction.EAST));
                            writer.write(frame + " (" + x + "," + y + ") was added to ownedBorder");
                        } else if (gameMap.getSite(new Location(x, y), Direction.SOUTH).owner != myID) {
                            ownedBorder.add(new Location(x, y));
                            border.add(gameMap.getLocation(new Location(x, y), Direction.SOUTH));
                            writer.write(frame + " (" + x + "," + y + ") was added to ownedBorder");
                        } else if (gameMap.getSite(new Location(x, y), Direction.WEST).owner != myID) {
                            ownedBorder.add(new Location(x, y));
                            border.add(gameMap.getLocation(new Location(x, y), Direction.WEST));
                            writer.write(frame + " (" + x + "," + y + ") was added to ownedBorder");
                        }
                    }
                }
            }
            
            //removes the ownedBorder locations from the owned list
            for (int i = 0; i < ownedBorder.size(); i++) {
                for (int j = owned.size() - 1; j > -1; j--) {
                    if (owned.get(j).x == ownedBorder.get(i).x && owned.get(j).y == ownedBorder.get(i).y) {
                        owned.remove(j);
                        writer.write(frame + " (" + ownedBorder.get(i).x + "," + ownedBorder.get(i).y + ") was removed from owned");
                    }
                }
            }
            
            //moves any ownedBorder units which can take a border piece
            for (int i = 0; i < ownedBorder.size(); i++) {
                Site site = gameMap.getSite(ownedBorder.get(i));
                if (site.strength > gameMap.getSite(ownedBorder.get(i), Direction.NORTH).strength) {
                    moves.add(new Move(ownedBorder.get(i), Direction.NORTH));
                } else if(site.strength > gameMap.getSite(ownedBorder.get(i), Direction.EAST).strength) {
                    moves.add(new Move(ownedBorder.get(i), Direction.EAST));
                } else if(site.strength > gameMap.getSite(ownedBorder.get(i), Direction.SOUTH).strength) {
                    moves.add(new Move(ownedBorder.get(i), Direction.SOUTH));
                } else if(site.strength > gameMap.getSite(ownedBorder.get(i), Direction.WEST).strength) {
                    moves.add(new Move(ownedBorder.get(i), Direction.WEST));
                } else {
                    moves.add(new Move(ownedBorder.get(i), Direction.STILL));
                }
            }
            
            /** moves the remaining units (all in owned) the STILL direction --
            This should be changed in the future so that they are moved to the border as they are needed or as they fill up.
            **/
            for (int i = 0; i < owned.size(); i++) {
                moves.add(new Move(owned.get(i), Direction.STILL));
            }
            
            Networking.sendFrame(moves);
        }
    }
    
    private class Writer {
        private String path;
        private boolean append = true;
        
        public Writer(String file_path) {
            path = file_path;
        }
        
        public void write(String text) throws IOException {
            FileWriter myWriter = new FileWriter(path, append);
            PrintWriter printWrite = new PrintWriter(myWriter);
            
            printWrite.printf("%s" + "%n", text);
            printWrite.close();
        }
    }
}
