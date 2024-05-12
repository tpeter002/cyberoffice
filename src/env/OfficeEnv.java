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


// Main environment class
public class OfficeEnv extends Environment {

    public static final int GSize = 20; // grid size

    public static final int GARB  = 8; // garbage code in grid model
    public static final int WALL = 4; // wall code in grid model



    public static final Term load = Literal.parseLiteral("load");
    public static final Term loadpos = Literal.parseLiteral("loadpos");

    private OfficeModel model;
    private OfficeView  view;

    static Logger logger = Logger.getLogger(OfficeEnv.class.getName());

    @Override
    public void init(String[] args) {
        model = new OfficeModel();
        view  = new OfficeView(model);
        model.setView(view);
        updatePercepts();
    }

    @Override
    public boolean executeAction(String agentName, Structure action) {

        // TODO: literals may be needed for agent names

        if (agentName.equals("printer")) {
            model.printerModel.executeAction(action);
            updatePercepts();
            return true;
        } else if (agentName.equals("vacuum_cleaner")) {
            model.vacuumCleanerModel.executeAction(action);
            updatePercepts();
            informAgsEnvironmentChanged();
            return true;
        } else if (agentName.charAt(0)=='h') {
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
            return true;
        } else if (agentName.equals("mainframe")) {
            model.mainframeModel.executeAction(action);
            return true;
        } else if (agentName.equals("light")) {
            model.lightModel.executeAction(action);
            return true;
        }
        return false;
    }



    public void updatePercepts() {
        clearPercepts();    // TODO: do we need to clear percepts?
        ArrayList<Literal> percepts = model.getUpdatedPercepts();
        for (Literal percept : percepts) {
            addPercept(percept);
        }
    }




    public class OfficeModel extends GridWorldModel {

        private HumanAgentModel humanAgentModel;
        private PrinterModel printerModel;
        private VacuumCleanerModel vacuumCleanerModel;
        private LightModel lightModel;
        private MainframeModel mainframeModel;

        public static int n_human_agents =5; //fele annyi menedzselhetobb majd max felvisszuk



        private OfficeModel() {
            //vacuumCleanerEnv = new VacuumCleanerEnvironment();   // 1 agent
            super(GSize, GSize, n_human_agents+4);

            // initial location of agents
            try {
                // add walls, initialize rooms
                int yMainWall = (int)(GSize/4);
                int xVacuumDoor = (int)(GSize/4);
                int xPrinterDoor = (int)(GSize/4)*3;


                addWall(0, yMainWall, xVacuumDoor, yMainWall);
                addWall(xVacuumDoor+2, 0, xVacuumDoor+2, yMainWall);
                addWall(xVacuumDoor+3, yMainWall, xPrinterDoor, yMainWall);
                addWall(xPrinterDoor+2, yMainWall, GSize-1, yMainWall);
                add(OfficeEnv.GARB,3, 0);


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
        }

        public ROOM whichRoom(int x, int y) {
            if (y < (int)(GSize/4) && x <= (int)(GSize/4)+1) {
                return ROOM.VACUUM;
            } else if (y < (int)(GSize/4) && x > (int)(GSize/4)) {
                return ROOM.PRINTER;
            } else if (y > (int)(GSize/4)) {
                return ROOM.HALL;
            } else {
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
                    for (int i = (int)(GSize/4); i < GSize; i++) {
                        for (int j = 0; j < (int)(GSize/4); j++) {
                            if (cellOccupied(i, j)) {
                                return false;
                            }
                        }
                    }
                    break;
                case HALL:
                    for (int i = 0; i < GSize; i++) {
                        for (int j = (int)(GSize/4); j < GSize; j++) {
                            if (cellOccupied(i, j)) {
                                return false;
                            }
                        }
                    }
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

        public ArrayList<Literal> getUpdatedPercepts() {
            ArrayList<Literal> percepts = new ArrayList<Literal>();
            // extend arraylist with percepts from every model
            percepts.addAll(printerModel.getPercepts());
            percepts.addAll(vacuumCleanerModel.getPercepts());
            return percepts;
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
                    g.setColor(Color.RED);
                    super.drawObstacle(g, x, y);
                    break;
               case OfficeEnv.WALL:
                    g.setColor(Color.PINK);
                    super.drawObstacle(g, x, y);
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
