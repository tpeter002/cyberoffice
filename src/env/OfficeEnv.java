package env;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.Console;
import java.util.Random;
import java.util.logging.Logger;

import env.OfficeEnv.OfficeModel.ROOM;
import models.HumanAgentModel;
import models.PrinterModel;
import models.VacuumCleanerModel;
import models.LightModel;
import models.MainframeModel;

import java.util.ArrayList;
import java.util.HashMap;

// Main environment class
public class OfficeEnv extends Environment {

    public static final int GSize = 20; // grid size
    public static final int GARB = 8; // garbage code in grid model
    public static final int WALL = 4; // wall code in grid model

    private OfficeModel model;
    private OfficeView view;

    static Logger logger = Logger.getLogger(OfficeEnv.class.getName());

    @Override
    public void init(String[] args) {
        model = new OfficeModel();
        view = new OfficeView(model);
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
        } else if (agentName.equals("vacuumcleaner")) {
            model.vacuumCleanerModel.executeAction(action);
            updatePercepts();
            informAgsEnvironmentChanged();
            return true;
        } else if (agentName.equals("human_agent")) {
            model.humanAgentModel.executeAction(action);
            return true;
        } else if (agentName.equals("mainframe")) {
            model.mainframeModel.executeAction(action);
            return true;
        } else if (agentName.charAt(0) == 'l') {
            model.lightModel.executeAction(agentName, action);
            updatePercepts();
            return true;
        }

        return false;
    }

    public void updatePercepts() {
        clearPercepts(); // TODO: do we need to clear percepts?
        ArrayList<Literal> percepts = model.getUpdatedPercepts();
        for (Literal percept : percepts) {
            addPercept(percept);
        }
    }

    public class Percept {
        public String destination;
        public Literal message;

        public Percept(String name, Literal message) {
            this.destination = name;
            this.message = message;
        }

        public void noDestination() {
            destination = null;
        }
    }

    public class OfficeModel extends GridWorldModel {

        private HumanAgentModel humanAgentModel;
        private PrinterModel printerModel;
        private VacuumCleanerModel vacuumCleanerModel;
        private LightModel lightModel;
        private MainframeModel mainframeModel;

        public static int n_human_agents = (int) ((GSize / 4) * (GSize / 4));

        private OfficeModel() {
            // vacuumCleanerEnv = new VacuumCleanerEnvironment(); // 1 agent
            super(GSize, GSize, n_human_agents);

            // initial location of agents
            try {
                // add walls, initialize rooms
                int yMainWall = (int) (GSize / 4);
                int xVacuumDoor = (int) (GSize / 4);
                int xPrinterDoor = (int) (GSize / 4) * 3;

                addWall(0, yMainWall, xVacuumDoor, yMainWall);
                addWall(xVacuumDoor + 2, 0, xVacuumDoor + 2, yMainWall);
                addWall(xVacuumDoor + 3, yMainWall, xPrinterDoor, yMainWall);
                addWall(xPrinterDoor + 2, yMainWall, GSize - 1, yMainWall);
                add(OfficeEnv.GARB, 3, 0);

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
            if (y < (int) (GSize / 4) && x < (int) (GSize / 4)) {
                return ROOM.VACUUM;
            } else if (y < (int) (GSize / 4) && x >= (int) (GSize / 4)) {
                return ROOM.PRINTER;
            } else if (y > (int) (GSize / 4)) {
                return ROOM.HALL;
            } else {
                return null;

            }
        }

        // magic numbers
        public boolean isWall(int x, int y) {
            return !isFree(4, x, y);
        }

        public boolean roomIsEmpty(ROOM room) {
            switch (room) {
                case VACUUM:
                    for (int i = 0; i < (int) (GSize / 4); i++) {
                        for (int j = 0; j < (int) (GSize / 4); j++) {
                            if (cellOccupied(i, j)) {
                                return false;
                            }
                        }
                    }
                    break;
                case PRINTER:
                    for (int i = (int) (GSize / 4); i < GSize; i++) {
                        for (int j = 0; j < (int) (GSize / 4); j++) {
                            if (cellOccupied(i, j)) {
                                return false;
                            }
                        }
                    }
                    break;
                case HALL:
                    for (int i = 0; i < GSize; i++) {
                        for (int j = (int) (GSize / 4); j < GSize; j++) {
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

        public ArrayList<Percept> getUpdatedPercepts() {
            ArrayList<Literal> percepts = new ArrayList<Literal>();
            ArrayList<Percept> perceptsList = new ArrayList<Percept>();
            // extend arraylist with percepts from every model

            percepts.addAll(printerModel.getPercepts());
            for (int i = 0; i < percepts.size(); i++) {
                perceptsList.add(new Percept(null, percepts.get(i)));
            }
            percepts.clear();

            percepts.addAll(vacuumCleanerModel.getPercepts());
            for (int i = 0; i < percepts.size(); i++) {
                perceptsList.add(new Percept(null, percepts.get(i)));
            }
            percepts.clear();

            HashMap<Light, ArrayList<Percept>> perceptsMap = lightModel.getPerceptsMap();
            for (int i = 0; i < 3; i++) {
                percepts.addAll(perceptsMap.get(lightModel.getLight(i)));
                for (int j = 0; j < percepts.size(); j++) {
                    perceptsList.add(new Percept("l" + String.valueOf(i), percepts.get(j)));
                }
                percepts.clear();
            }
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
            if (id > 1 && id < ((OfficeModel) model).n_human_agents) {
                c = Color.red;
                label = "H";
            }
            super.drawAgent(g, x, y, c, id);
            super.drawString(g, x, y, defaultFont, label);
            repaint();
        }

    }
}
