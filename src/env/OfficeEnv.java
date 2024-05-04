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

// Main environment class
public class OfficeEnv extends Environment {

    public static final int GSize = 20; // grid size
    public static final int GARB  = 16; // garbage code in grid model
    public static final int WALL = 255; // wall code in grid model

    private OfficeModel model;
    private OfficeView  view;

    static Logger logger = Logger.getLogger(OfficeEnv.class.getName());

    @Override
    public void init(String[] args) {
        model = new OfficeModel();
        view  = new OfficeView(model);
        model.setView(view);
        //updatePercepts();
    }

    //@Override
    //public boolean executeAction(String agentName, Structure action) {
        //if (agentName.equals("vacuum_cleaner")) {
        //    return vacuumCleanerEnv.executeAction(agentName, action);
        //} else if (agentName.equals("human_agent")) {
        //    return humanAgentEnv.executeAction(agentName, action);
        //}
        // ... handle actions for other agents
    //}

    //@Override
    //public void updatePercepts() {
        //vacuumCleanerEnv.updatePercepts();
        //humanAgentEnv.updatePercepts();
        // ... update percepts for other agent-specific environments

        // Collect percepts from all agent-specific environments
        // and update the overall environment state
        // ...
    //}


    public class OfficeModel extends GridWorldModel {

        /*
        draws the grid, places the items
        */

        Random random = new Random(System.currentTimeMillis());

        private HumanAgentModel humanAgentModel;
        private PrinterModel printerModel;
        private VacuumCleanerModel vacuumCleanerModel;
        private LightModel lightModel;
        private MainframeModel mainframeModel;

        public static int n_human_agents = (int)((GSize/4) * (GSize/4));

        private OfficeModel() {
            //vacuumCleanerEnv = new VacuumCleanerEnvironment();   // 1 agent
            super(GSize, GSize, n_human_agents);

            // initial location of agents
            try {
                // add walls, initialize rooms
                int yMainWall = (int)(GSize/4);
                int xVacuumDoor = (int)(GSize/4);
                int xPrinterDoor = (int)(GSize/4)*3;

                for (int i = 0; i < GSize; i++) {
                    if (i != xPrinterDoor && i != xVacuumDoor) {
                        add(WALL, i, yMainWall);
                    }
                    if (i == xVacuumDoor + 1) {
                        for (int j = 0; j < yMainWall; j++) {
                            add(WALL, i, j);
                        }
                    }
                }

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

        private enum ROOM {
            HALL,
            PRINTER,
            VACUUM,
        }

        public ROOM whichRoom(int x, int y) {
            if (y < (int)(GSize/4) && x < (int)(GSize/4)) {
                return ROOM.VACUUM;
            } else if (y < (int)(GSize/4) && x >= (int)(GSize/4)) {
                return ROOM.PRINTER;
            } else if (y > (int)(GSize/4)) {
                return ROOM.HALL;
            } else {
                return null;
            }
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
                case OfficeEnv.WALL:
                    drawGarb(g, x, y);
                    break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R"+(id+1);

            // draw wall
            if (id == -1) {
                c = Color.black;
                label = "";
                super.drawObstacle(g, x, y);
            }

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
            if (id > 1 && id < ((OfficeModel)model).n_human_agents) {
                c = Color.red;
                label = "H";
            }

            super.drawString(g, x, y, defaultFont, label);
            repaint();
        }

        public void drawGarb(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.black);
            super.drawString(g, x, y, defaultFont, "X");
        }

    }



}
