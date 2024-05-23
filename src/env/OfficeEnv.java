package env;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.logging.Logger;

import models.HumanAgentModel;
import models.PrinterModel;
import models.VacuumCleanerModel;
import models.LightModel;
import models.MainframeModel;

import java.util.ArrayList;
import env.Percept;
import env.BackgroundMusic;

// Main environment class
public class OfficeEnv extends Environment {

    public static final int GSize = 21; // grid size
    public static final int GARB  = 16; // garbage code in grid model
    public static final int WALL = 4; // wall code in grid model
    public static final int LACK_OF_LIGHT = 8;
    public static final int AGENT = 2;

    public static final Term load = Literal.parseLiteral("load");
    public static final Term loadpos = Literal.parseLiteral("loadpos");

    private OfficeModel model;
    private OfficeView view;

    private BackgroundMusic backgroundMusic;
    static Logger logger = Logger.getLogger(OfficeEnv.class.getName());

    @Override
    public void init(String[] args) {
        model = new OfficeModel();
        view = new OfficeView(model);
        backgroundMusic = new BackgroundMusic("song.wav");
        backgroundMusic.play();
        model.setView(view);
    }

    @Override
    public boolean executeAction(String agentName, Structure action) {

        if (agentName.equals("printer")) {

            model.printerModel.executeAction(action);
            updatePercepts(agentName);
            return true;
        }

        if (agentName.equals("vacuumcleaner")) {

            model.vacuumCleanerModel.executeAction(action);
            updatePercepts(agentName);
            informAgsEnvironmentChanged();
            return true;
        }

        if (agentName.charAt(0) == 'h') {

            /*
             * if(action.equals(load)){
             * Literal older_element=model.humanAgentModel.getReminder(agentName);
             * Literal
             * routine_element=model.humanAgentModel.getNextRoutineElement(agentName);
             * if (older_element!= null)
             * removePercept(agentName, older_element);
             * addPercept(agentName, routine_element);
             * } else if (action.equals(loadpos)) {
             * Literal hpos = model.humanAgentModel.getPosLiteral(agentName);
             * addPercept(agentName, hpos);
             * } else {
             * model.humanAgentModel.executeAction(agentName, action);
             * }
             */

            model.humanAgentModel.executeAction(agentName, action);
            updatePercepts(agentName);
            return true;
        }

        if (agentName.equals("mainframe")) {

            if (action.getFunctor().equals("reminder")) {
                String humanName = action.getTerm(0).toString();
                Literal reminder = model.humanAgentModel.getReminder(humanName);
                addPercept(humanName, reminder);
            }
            updatePercepts(agentName);
            return true;
        }

        if (agentName.charAt(0) == 'l') {
            model.lightModel.executeAction(agentName, action); // Pass agentName and action
            updatePercepts(agentName);
            return true;
        }

        return false;
    }

    public void updatePercepts(String agentName) {
        clearPercepts();
        ArrayList<Percept> perceptsToRemove = model.getPerceptsToRemove(agentName);
        ArrayList<Percept> perceptsToRemoveByUnif=model.humanAgentModel.getPerceptsToRemoveByUnif();
        ArrayList<Percept> percepts = model.getNewPercepts(agentName);

        // inform mainframe about empty rooms
        for (OfficeModel.ROOM room : OfficeModel.ROOM.values()) {
            if (room != OfficeModel.ROOM.DOORWAY) {
                if (model.roomIsEmpty(room)) {
                    percepts.add(new Percept("mainframe", Literal.parseLiteral("room_empty(" + room.ordinal() + ")")));
                } else {
                    perceptsToRemove
                            .add(new Percept("mainframe", Literal.parseLiteral("room_empty(" + room.ordinal() + ")")));
                }
            }
        }
        // inform agents about percepts to remove
        
        for (Percept percept : perceptsToRemove) {
            if (percept.hasDestination()) {
                removePercept(percept.destination, percept.message);
            } else {
                removePercept(agentName, percept.message);
            }
        }

        for (Percept percept : perceptsToRemoveByUnif) {
            if (percept.hasDestination()) {
                removePerceptsByUnif(percept.destination, percept.message);
            } else {
                removePerceptsByUnif(agentName, percept.message);
            }
        }

        // inform agents about new percepts
        for (Percept percept : percepts) {
            if (percept.hasDestination()) {
                addPercept(percept.destination, percept.message);
            } else {
                addPercept(agentName, percept.message);
            }
        }

        // inform agents about percepts to remove

        for (Percept percept : perceptsToRemove) {
            if (percept.hasDestination()) {
                removePercept(percept.destination, percept.message);
            } else {
                removePercept(agentName, percept.message);
            }
        }

    }

    @Override
    public void stop() {
        backgroundMusic.stop();
        super.stop();
    }

    public class OfficeModel extends GridWorldModel {

        private HumanAgentModel humanAgentModel;
        private PrinterModel printerModel;
        private VacuumCleanerModel vacuumCleanerModel;
        private LightModel lightModel;
        private MainframeModel mainframeModel;

        public static int n_human_agents = 4; // fele annyi menedzselhetobb majd max felvisszuk

        public int yMainWall = (int) (GSize / 4);
        public int xVacuumDoor = (int) ((GSize / 4) - 2);
        public int xPrinterDoor = (int) (GSize / 4) * 3 + 1;
        public int xMainWall = xVacuumDoor + 2;

        private OfficeModel() {
            // vacuumCleanerEnv = new VacuumCleanerEnvironment(); // 1 agent
            super(GSize, GSize, n_human_agents + 3);

            // initial location of agents
            try {
                // add walls, initialize rooms

                // HORIZONTAL WALLS
                addWall(0, yMainWall, xVacuumDoor - 1, yMainWall);
                addWall(xVacuumDoor + 1, yMainWall, xPrinterDoor - 1, yMainWall);
                addWall(xPrinterDoor + 1, yMainWall, GSize - 1, yMainWall);

                // VERTICAL WALL(S)
                addWall(xMainWall, 0, xMainWall, 0);
                addWall(xMainWall, 2, xMainWall, yMainWall);

                add(OfficeEnv.GARB, 3, 0);
                add(OfficeEnv.GARB, 4, 2);

                // add mainframe
                mainframeModel = new MainframeModel(this, GSize);
                // setup ligtning
                lightModel = new LightModel(this, GSize);
                // add printer
                printerModel = new PrinterModel(this, GSize);
                // add vacuum cleaner
                vacuumCleanerModel = new VacuumCleanerModel(this, GSize);
                // add human agents
                humanAgentModel = new HumanAgentModel(this, GSize);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public enum ROOM {
            HALL,
            PRINTER,
            VACUUM,
            DOORWAY,
        }

        // Ide nem kéne az a +1 !!!
        public ROOM whichRoom(int x, int y) {
            if (y < yMainWall && x < xMainWall) {
                return ROOM.VACUUM;
            } else if (y < yMainWall && x > xMainWall) {
                return ROOM.PRINTER;
            } else if (y > yMainWall) {
                return ROOM.HALL;
            } else if ((y == yMainWall) && ((x == xVacuumDoor) || (x == xPrinterDoor))) {
                return ROOM.DOORWAY;
            } else if ((x == xMainWall) && (y == 1)) {
                return ROOM.DOORWAY;
            } else {
                return null;
            }
        }

        // Doorway locations
        public Location VACUUM_HALL_DOOR = new Location(xVacuumDoor, yMainWall);
        public Location PRINTER_HALL_DOOR = new Location(xPrinterDoor, yMainWall);
        public Location VACUUM_PRINTER_DOOR = new Location(xMainWall, 1);

        // Start/End locations
        public Location VACUUM_START = new Location(0, 0);
        public Location VACUUM_END = new Location(xMainWall - 1, yMainWall - 1);
        public Location PRINTER_START = new Location(xMainWall + 1, 0);
        public Location PRINTER_END = new Location(GSize - 1, yMainWall - 1);
        public Location HALL_START = new Location(0, yMainWall + 1);
        public Location HALL_END = new Location(GSize - 1, GSize - 1);

        public Location getDoorwayPos(ROOM curr, ROOM dest) {
            switch (curr) {
                case VACUUM:
                    switch (dest) {
                        case HALL:
                            return VACUUM_HALL_DOOR;
                        case PRINTER:
                            return VACUUM_PRINTER_DOOR;
                    }
                case PRINTER:
                    switch (dest) {
                        case HALL:
                            return PRINTER_HALL_DOOR;
                        case VACUUM:
                            return VACUUM_PRINTER_DOOR;
                    }
                case HALL:
                    switch (dest) {
                        case PRINTER:
                            return PRINTER_HALL_DOOR;
                        case VACUUM:
                            return VACUUM_HALL_DOOR;
                    }
                default:
                    return null;
            }
        }

        public Location getRoomStartPos(ROOM curr) {
            switch (curr) {
                case VACUUM:
                    return VACUUM_START;
                case PRINTER:
                    return PRINTER_START;
                case HALL:
                    return HALL_START;
                default:
                    return null;
            }
        }

        public Location getRoomEndPos(ROOM curr) {
            switch (curr) {
                case VACUUM:
                    return VACUUM_END;
                case PRINTER:
                    return PRINTER_END;
                case HALL:
                    return HALL_END;
                default:
                    return null;
            }
        }

        @Override
        public boolean isFree(int x, int y) {
            return super.isFree(x, y) || (
                hasObject(LACK_OF_LIGHT, x, y) && (
                    !hasObject(AGENT, x, y) 
                    && !hasObject(WALL, x, y)
                    && !hasObject(GARB, x, y)
                )
            );
        }


        public boolean roomIsEmpty(ROOM room) {
            switch (room) {
                case VACUUM:
                    for (int x = 0; x < xMainWall; x++) {
                        for (int y = 0; y < yMainWall; y++) {
                            if (cellOccupied(x, y)) {
                                return false;
                            }
                        }
                    }
                    break;
                case PRINTER:
                    for (int x = xMainWall + 1; x < GSize; x++) {
                        for (int y = 0; y < yMainWall; y++) {
                            if (cellOccupied(x, y)) {
                                return false;
                            }
                        }
                    }
                    break;
                case HALL:
                    for (int x = 0; x < GSize; x++) {
                        for (int y = yMainWall + 1; y < GSize; y++) {
                            if (cellOccupied(x, y)) {
                                return false;
                            }
                        }
                    }
                    break;
            }
            return true;
        }

        // magic numbers
        public boolean isWall(int x, int y) {
            return !isFree(OfficeEnv.WALL, x, y); 
        }

        public boolean cellOccupied(int x, int y) {
            return humanAgentModel.cellOccupied(x, y);
        }


        public void addGarbage(int x, int y) {
            add(OfficeEnv.GARB, x, y);
        }

        public void removeGarbage(int x, int y) {
            remove(OfficeEnv.GARB, x, y);
        }

        public boolean hasGarbage(int x, int y) {
            return hasObject(OfficeEnv.GARB, x, y);
        }

        public int getData(int x, int y) {
            return data[x][y];
        }

        public boolean isLightBrokenInLocation(int x, int y) {
            ROOM room = whichRoom(x, y);
            return lightModel.isLightBrokenInRoom(room);
        }

        public void turnOffLight(ROOM room) {
            switch (room) {
                case VACUUM:
                    for (int x = 0; x < xMainWall; x++) {
                        for (int y = 0; y < yMainWall; y++) {
                            add(LACK_OF_LIGHT, x, y);
                        }
                    }
                    break;
                case PRINTER:
                    for (int x = xMainWall + 1; x < GSize; x++) {
                        for (int y = 0; y < yMainWall; y++) {
                            add(LACK_OF_LIGHT, x, y);
                        }
                    }
                    break;
                case HALL:
                    for (int x = 0; x < GSize; x++) {
                        for (int y = yMainWall + 1; y < GSize; y++) {
                            add(LACK_OF_LIGHT, x, y);
                        }
                    }
                    break;
            }
        }

        public void turnOnLight(ROOM room) {
            switch (room) {
                case VACUUM:
                    for (int x = 0; x < xMainWall; x++) {
                        for (int y = 0; y < yMainWall; y++) {
                            remove(LACK_OF_LIGHT, x, y);
                        }
                    }
                    break;
                case PRINTER:
                    for (int x = xMainWall + 1; x < GSize; x++) {
                        for (int y = 0; y < yMainWall; y++) {
                            remove(LACK_OF_LIGHT, x, y);
                        }
                    }
                    break;
                case HALL:
                    for (int x = 0; x < GSize; x++) {
                        for (int y = yMainWall + 1; y < GSize; y++) {
                            remove(LACK_OF_LIGHT, x, y);
                        }
                    }
                    break;
            }
        }

        public ArrayList<Percept> getNewPercepts(String agentName) {

            ArrayList<Percept> percepts_new = new ArrayList<Percept>();

            if (agentName.equals("printer")) {
                percepts_new.addAll(printerModel.newPercepts());
            } else if (agentName.equals("vacuumcleaner")) {
                percepts_new.addAll(vacuumCleanerModel.newPercepts());
            } else if (agentName.charAt(0) == 'h') {
                percepts_new.addAll(humanAgentModel.newPercepts());
            } else if (agentName.equals("mainframe")) {
                // percepts_new.addAll(mainframeModel.newPercepts());
            } else if (agentName.charAt(0) == 'l') {
                percepts_new.addAll(lightModel.newPercepts());
            }
            return percepts_new;
        }

        public ArrayList<Percept> getPerceptsToRemove(String agentName) {
            ArrayList<Percept> percepts_to_remove = new ArrayList<Percept>();
            if (agentName.equals("printer")) {
                percepts_to_remove.addAll(printerModel.perceptsToRemove());
            } else if (agentName.equals("vacuumcleaner")) {
                percepts_to_remove.addAll(vacuumCleanerModel.perceptsToRemove());
            } else if (agentName.charAt(0) == 'h') {
                percepts_to_remove.addAll(humanAgentModel.perceptsToRemove());
            } else if (agentName.equals("mainframe")) {
                // percepts_to_remove.addAll(mainframeModel.perceptsToRemove());
            } else if (agentName.charAt(0) == 'l') {
                percepts_to_remove.addAll(lightModel.perceptsToRemove());
            }
            return percepts_to_remove;
        }
    }

    class OfficeView extends GridWorldView {

        OfficeModel omodel;

        public OfficeView(OfficeModel model) {
            super(model, "Office World", 600);
            omodel = model;
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
        }


        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
                case OfficeEnv.LACK_OF_LIGHT:
                    // if broken
                    if (omodel.isLightBrokenInLocation(x, y)) {
                        g.setColor(Color.ORANGE);
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                    
                    g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
                    //g.setColor(Color.BLACK);
                    //g.drawRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
                    break;
                case OfficeEnv.GARB:
                    g.setColor(new Color(153, 102, 0));
                    g.fillOval(x * cellSizeW + cellSizeW / 4, y * cellSizeH + cellSizeH / 4, cellSizeW / 2,
                            cellSizeH / 2);
                    break;
            }
        }


        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R" + (id + 1);

            // draw printer
            if (id == 0) {
                c = Color.green;
                label = "P";
            }

            // draw vacuum cleaner
            if (id == 1) {
                c = Color.blue;
                label = "V";
            }

            // draw human agents
            if (id > 1 && id < ((OfficeModel) model).n_human_agents + 2) {
                c = Color.red;
                label = "H";
            }
            super.drawAgent(g, x, y, c, id);
            //super.drawString(g, x, y, defaultFont, label);
            repaint();
        }
    }
}