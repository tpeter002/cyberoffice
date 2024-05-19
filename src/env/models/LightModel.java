package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;
import env.OfficeEnv.Percept;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Array;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class LightModel {

    private OfficeModel model;

    /*
     * Action that we want the model to react to
     * simple in this case: BDI agent starts operating the lights
     * might need to be more complex in the future
     */

    // we store the states in an array
    private ArrayList<Light> lights = new ArrayList<Light>();
    private ArrayList<Percept> newLightsPercepts = new ArrayList<Percept>();
    private ArrayList<Percept> removeLightsPercepts = new ArrayList<Percept>();

    /*
     * All models get the OfficeModel object, which stores
     * the overall state of the environment
     * Methods of OfficeModel at this moment:
     * whichRoom(int x, int y) - returns the room at the given coordinates
     * roomIsEmpty(ROOM room) - returns true if the room is empty
     * cellOccupied(int x, int y) - returns true if the cell is occupied
     * addGarbage(int x, int y) - adds garbage to the cell
     * removeGarbage(int x, int y) - removes garbage from the cell
     * hasGarbage(int x, int y) - returns true if the cell has garbage
     */
    public LightModel(OfficeModel model, int GSize) {
        this.model = model;
        lights.add(new Light(OfficeModel.ROOM.VACUUM)); // jobb felso szoba
        lights.add(new Light(OfficeModel.ROOM.HALL)); // bal felso szoba
        lights.add(new Light(OfficeModel.ROOM.PRINTER)); // also nagy szoba
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            light.set_id(i);
            if (i == 0) {
                // model.setAgPos(i, 0, 1);
                light.setLocation(0, 1);
            } else if (i == 1) {
                // model.setAgPos(i, 0, GSize - 1);
                light.setLocation(0, GSize - 1);
            } else if (i == 2) {
                // model.setAgPos(i, GSize - 1, 0);
                light.setLocation(GSize - 1, 0);
            }
        }
    }

    public Light getLight(int i) {
        return lights.get(i);
    }

    /*
     * Here we change the state of the model of the agent
     * which we will later pass on to the agents as percepts using getPercepts()
     */

    /*
     * Here we infer which percepts to give to the agents
     * based on the state of the model
     */

    public void executeAction(String agentName, Structure action) {
        Light light;
        if (agentName.equals("l1")) {
            light = this.getLight(0);
        } else if (agentName.equals("l2")) {
            light = this.getLight(1);
        } else if (agentName.equals("l3")) {
            light = this.getLight(2);
        } else {
            System.err.println("Light not found");
            return;
        }
        light.executeAction(action);
        return;
    }

    public ArrayList<Percept> newPercepts() {
        ArrayList<Percept> percepts = new ArrayList<Percept>();
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            percepts.addAll(light.newPercept());
        }
        return percepts;
    }

    public ArrayList<Percept> perceptsToRemove() {
        ArrayList<Percept> percepts = new ArrayList<Percept>();
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            percepts.addAll(light.perceptsToRemove(light.newPercept()));
        }
        return percepts;
    }

    public ArrayList<Percept> getPercepts(String agentName) {
        Light light;
        if (agentName.equals("l1")) {
            System.out.println("l1");
            light = this.getLight(0);
        } else if (agentName.equals("l2")) {
            System.out.println("l2");
            light = this.getLight(1);
        } else if (agentName.equals("l3")) {
            System.out.println("l3");
            light = this.getLight(2);
        } else {
            System.err.println("Light not found");
            return null;
        }

        return light.newPercept();
    }

    private class Light {
        /*
         * Light class to keep track of states of lights
         * lights are identified by OfficeModel.ROOM enumerator
         */
        int _id;
        int x;
        int y;
        OfficeModel.ROOM room;
        boolean isOn;
        int untilBreakDown;
        private boolean isBroken;
        private ArrayList<Percept> newPercepts = new ArrayList<Percept>();
        private ArrayList<Percept> removePercepts = new ArrayList<Percept>();

        public static final Term turnOn = Literal.parseLiteral("turn_light_on");
        public static final Term turnOff = Literal.parseLiteral("turn_light_off");
        public static final Term repair = Literal.parseLiteral("repair_light");
        public static final Term broken = Literal.parseLiteral("light_broken");
        public static final Term getLocation = Literal.parseLiteral("get_location");

        public Light(OfficeModel.ROOM room) {
            this.room = room;
            this.isOn = false;
            this.untilBreakDown = 10;
            this.isBroken = false;
            this.newPercepts = new ArrayList<Percept>();
            this.removePercepts = new ArrayList<Percept>();
        }

        public void turnOn() {
            this.untilBreakDown--;
            if (this.untilBreakDown <= 0) {
                this.isBroken = true;
                this.isOn = false;
                System.err.println("Light in room " + this.room + " is broken");
                newPercepts.add(new Percept("l" + String.valueOf(_id), Literal.parseLiteral("light_broken")));
                removePercepts.add(new Percept("l" + String.valueOf(_id), Literal.parseLiteral("light_on")));
            } else {
                this.isOn = true;
                newPercepts.add(new Percept("l" + String.valueOf(_id), Literal.parseLiteral("light_on")));
                removePercepts.add(new Percept("l" + String.valueOf(_id), Literal.parseLiteral("light_off")));
            }
        }

        public void turnOff() {
            this.isOn = false;
            newPercepts.add(new Percept("l" + String.valueOf(_id), Literal.parseLiteral("light_off")));
            removePercepts.add(new Percept("l" + String.valueOf(_id), Literal.parseLiteral("light_on")));
        }

        public void repair() {
            this.isBroken = false;
            this.untilBreakDown = 10;
            newPercepts.add(new Percept("l" + String.valueOf(_id), Literal.parseLiteral("light_repaired")));
            removePercepts.add(new Percept("l" + String.valueOf(_id), Literal.parseLiteral("light_broken")));
        }

        public ArrayList<Percept> newPercept() {
            return newPercepts;
        }

        public ArrayList<Percept> perceptsToRemove(ArrayList<Percept> percepts) {
            return removePercepts;
        }

        public void executeAction(Structure action) {
            if (action.equals(turnOn)) {
                this.turnOn();
            } else if (action.equals(turnOff)) {
                this.turnOff();
            } else if (action.equals(repair)) {
                this.repair();
            } else if (action.equals(getLocation)) {
                newPercepts.add(new Percept("l" + String.valueOf(_id),
                        Literal.parseLiteral("location(" + this.x + "," + this.y + ")")));
            }
        }

        public void set_id(int id) {
            this._id = id;
        }

        public void setLocation(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }
}
