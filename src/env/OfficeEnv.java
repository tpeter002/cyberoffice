package env;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
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

    public static final int GSize = 20; // grid size
    public static final int GARB  = 8; // garbage code in grid model
    public static final int WALL = 4; // wall code in grid model


    public static final Term load = Literal.parseLiteral("load");
    public static final Term loadpos = Literal.parseLiteral("loadpos");

    private OfficeModel model;
    private OfficeView  view;

    private BackgroundMusic backgroundMusic;
    static Logger logger = Logger.getLogger(OfficeEnv.class.getName());

    @Override
    public void init(String[] args) {
        model = new OfficeModel();
        view  = new OfficeView(model);
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
        
        if (agentName.charAt(0)=='h') {

            if(action.equals(load)){
                Literal routine_element=model.humanAgentModel.getNextRoutineElement(agentName);
                addPercept(agentName, routine_element);
            }
            else if(action.equals(loadpos)){
                Literal hpos=model.humanAgentModel.getPosLiteral(agentName);
                addPercept(agentName, hpos);
            }
            else{
                model.humanAgentModel.executeAction(agentName, action);
            }
            updatePercepts(agentName);
            return true;
        }
        
        if (agentName.equals("mainframe")) {

            if(action.getFunctor().equals("reminder")) {
                String humanName = action.getTerm(0).toString();
                Literal reminder = model.humanAgentModel.getReminder(humanName, action);
                addPercept(humanName, reminder);
            }
            updatePercepts(agentName);
            return true;
        }
        
        if (agentName.equals("light")) {
            
            model.lightModel.executeAction(action);
            updatePercepts(agentName);
            return true;
        }

        return false;
    }

    public void updatePercepts(String agentName) {
        clearPercepts();
        ArrayList<Percept> percepts = model.getNewPercepts(agentName);
        ArrayList<Percept> perceptsToRemove = model.getPerceptsToRemove(agentName);

        // inform mainframe about empty rooms
        for (OfficeModel.ROOM room : OfficeModel.ROOM.values()) {
            if (room != OfficeModel.ROOM.DOORWAY) {
                if (model.roomIsEmpty(room)) {
                    percepts.add(new Percept("mainframe", Literal.parseLiteral("room_empty(" + room.ordinal() + ")")));
                    perceptsToRemove.add(new Percept("mainframe", Literal.parseLiteral("room_not_empty(" + room.ordinal() + ")")));
                } else {
                    percepts.add(new Percept("mainframe", Literal.parseLiteral("room_not_empty(" + room.ordinal() + ")")));
                    perceptsToRemove.add(new Percept("mainframe", Literal.parseLiteral("room_empty(" + room.ordinal() + ")")));
                }
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

        public static int n_human_agents = 5; //fele annyi menedzselhetobb majd max felvisszuk



        private OfficeModel() {
            //vacuumCleanerEnv = new VacuumCleanerEnvironment();   // 1 agent
            super(GSize, GSize, n_human_agents+3);

            // initial location of agents
            try {
                // add walls, initialize rooms
                int yMainWall = (int)(GSize/4);
                int xVacuumDoor = (int)((GSize/4)-2);
                int xPrinterDoor = (int)(GSize/4)*3;


                addWall(0, yMainWall, xVacuumDoor, yMainWall);
                addWall(xVacuumDoor+2, 0, xVacuumDoor+2, yMainWall);
                addWall(xVacuumDoor+3, yMainWall, xPrinterDoor, yMainWall);
                addWall(xPrinterDoor+2, yMainWall, GSize-1, yMainWall);

                add(OfficeEnv.GARB,3, 0);
                add(OfficeEnv.GARB,4, 2);


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
            if (y < (int)(GSize/4) && x < (int)(GSize/4)) {
                return ROOM.VACUUM;
            } else if (y < (int)(GSize/4) && x > (int)(GSize/4)) {
                return ROOM.PRINTER;
            } else if (y > (int)(GSize/4)) {
                return ROOM.HALL;
            }
            else if ((y == (int)((GSize/4))) && ((x == (int)(GSize/4 - 1)) || (x == (int)(GSize/4)*3+1))){
                return ROOM.DOORWAY;
            } 
            else {
                return null;
            }
        }

        public Location getDoorwayPos(ROOM dest, ROOM curr) {
            switch (dest) {
                case VACUUM:
                    switch(curr){
                        case HALL:
                            return new Location((int)(GSize/4)-1, (int)(GSize/4));
                        case PRINTER:
                            return new Location((int)(GSize/4)*3, (int)(GSize/4));
                    }
                case PRINTER:
                    switch(curr){
                        case HALL:
                            return new Location((int)(GSize/4)*3+1, (int)(GSize/4));
                        case VACUUM:
                            return new Location((int)(GSize/4)-1, (int)(GSize/4));
                    }
                case HALL:
                    switch(curr){
                        case PRINTER:
                            return new Location((int)(GSize/4)*3, (int)(GSize/4));
                        case VACUUM:
                            return new Location((int)(GSize/4)-1, (int)(GSize/4));
                    }      
                default:
                    return null;
            }
        }

        //magic numbers
        public boolean isWall(int x, int y) {
            return !isFree(4, x, y);
        }


        public boolean roomIsEmpty(ROOM room) {
            switch (room) {
                case VACUUM:
                    for (int i = 0; i < (int)(GSize/4); i++) {
                        for (int j = 0; j < (int)(GSize/4); j++) {
                            if (cellOccupied(i, j)) {
                                return false;
                            }
                        }
                    }
                    break;
                case PRINTER:
                    for (int i = (int)(GSize/4) + 1; i < GSize; i++) {
                        for (int j = 0; j < (int)(GSize/4); j++) {
                            if (cellOccupied(i, j)) {
                                return false;
                            }
                        }
                    }
                    break;
                case HALL:
                    for (int i = 0; i < GSize; i++) {
                        for (int j = (int)(GSize/4) + 1; j < GSize; j++) {
                            if (cellOccupied(i, j)) {
                                return false;
                            }
                        }
                    }
                // TODO: kell a doorway is?
                    break;

            }
            return true;
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

        public ArrayList<Percept> getNewPercepts(String agentName) {

            ArrayList<Percept> percepts_new = new ArrayList<Percept>();

            if (agentName.equals("printer")) {
                //percepts_new.addAll(printerModel.newPercepts());
            } else if (agentName.equals("vacuumcleaner")) {
                percepts_new.addAll(vacuumCleanerModel.newPercepts());
            } else if (agentName.charAt(0)=='h') {
                //percepts_new.addAll(humanAgentModel.newPercepts());
            } else if (agentName.equals("mainframe")) {
                //percepts_new.addAll(mainframeModel.newPercepts());
            } else if (agentName.equals("light")) {
                //percepts_new.addAll(lightModel.newPercepts());
            }

            return percepts_new;
        }

        public ArrayList<Percept> getPerceptsToRemove(String agentName) {
            ArrayList<Percept> percepts_to_remove = new ArrayList<Percept>();

            if (agentName.equals("printer")) {
                //percepts_to_remove.addAll(printerModel.perceptsToRemove());
            } else if (agentName.equals("vacuumcleaner")) {
                percepts_to_remove.addAll(vacuumCleanerModel.perceptsToRemove());
            } else if (agentName.charAt(0)=='h') {
                //percepts_to_remove.addAll(humanAgentModel.perceptsToRemove());
            } else if (agentName.equals("mainframe")) {
                //percepts_to_remove.addAll(mainframeModel.perceptsToRemove());
            } else if (agentName.equals("light")) {
                //percepts_to_remove.addAll(lightModel.perceptsToRemove());
            }

            return percepts_to_remove;
        }

    }

    class OfficeView extends GridWorldView {

        public OfficeView(OfficeModel model) {
            super(model, "Office World", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
                case OfficeEnv.GARB:
                    g.setColor(new Color(153, 102, 0));
                    g.fillOval(x * cellSizeW + cellSizeW / 4, y * cellSizeH + cellSizeH / 4, cellSizeW / 2, cellSizeH / 2);
                    break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R"+(id+1);

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
            if (id > 1 && id < ((OfficeModel)model).n_human_agents+2) {
                c = Color.red;
                label = "H";
            }
            super.drawAgent(g, x, y, c, id);
            super.drawString(g, x, y, defaultFont, label);
            repaint();
        }
    }
}