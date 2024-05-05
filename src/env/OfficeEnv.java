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
    public boolean executeAction(String agentName, Structure action) {
      //  if (agentName.equals("vacuum_cleaner")) {
        //    return vacuumCleanerEnv.executeAction(agentName, action);
        //} else if (agentName.equals("human_agent")) {
          //  return humanAgentEnv.executeAction(agentName, action);
        //}
         if (agentName.equals("printer")) {
            PrinterModel env = new PrinterModel(this.model, GSize);
            return env.executeAction(agentName, action);
        }
        return false;
    }

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
        
        // ... other agent-specific environment classes

        public static final int MErr = 2; // max error in pick garb
        int nerr; // number of tries of pick garb
        boolean r1HasGarb = false; // whether r1 is carrying garbage or no


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

        void nextSlot() throws Exception {
            Location r1 = getAgPos(0);
            r1.x++;
            if (r1.x == getWidth()) {
                r1.x = 0;
                r1.y++;
            }
            // finished searching the whole grid
            if (r1.y == getHeight()) {
                return;
            }
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
        }

        void moveTowards(int x, int y) throws Exception {
            Location r1 = getAgPos(0);
            if (r1.x < x)
                r1.x++;
            else if (r1.x > x)
                r1.x--;
            if (r1.y < y)
                r1.y++;
            else if (r1.y > y)
                r1.y--;
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
        }

        void pickGarb() {
            // r1 location has garbage
            if (model.hasObject(GARB, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARB, getAgPos(0));
                    nerr = 0;
                    r1HasGarb = true;
                } else {
                    nerr++;
                }
            }
        }

        void dropGarb() {
            if (r1HasGarb) {
                r1HasGarb = false;
                add(GARB, getAgPos(0));
            }
        }
        void burnGarb() {
            // r2 location has garbage
            if (model.hasObject(GARB, getAgPos(1))) {
                remove(GARB, getAgPos(1));
            }
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
                //case OfficeEnv.GARB:
                //    drawGarb(g, x, y);
                //    break;
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
