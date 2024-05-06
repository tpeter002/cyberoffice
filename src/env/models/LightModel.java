package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;
import java.util.ArrayList;
import java.util.Arrays;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;

public class LightModel {

    private OfficeModel model;

    /*
     * Action that we want the model to react to
     * simple in this case: BDI agent starts operating the lights
     * might need to be more complex in the future
     */

    // we store the states in an array
    private ArrayList<Light> lights = new ArrayList<Light>();

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

        lights.add(new Light(OfficeModel.ROOM.PRINTER));
        lights.add(new Light(OfficeModel.ROOM.VACUUM));
        lights.add(new Light(OfficeModel.ROOM.HALL));
    }

    public Light getLight(int i) {
        return lights.get(i);
    }

    private class Light {
        /*
         * Light class to keep track of states of lights
         * lights are identified by OfficeModel.ROOM enumerator
         */

        OfficeModel.ROOM room;
        boolean isOn;
        int untilBreakDown;
        private boolean isBroken;

        public static final Term turnOn = Literal.parseLiteral("turn_light_on(location)");
        public static final Term turnOff = Literal.parseLiteral("turn_light_off(location)");

        public Light(OfficeModel.ROOM room) {
            this.room = room;
            this.isOn = false;
            this.untilBreakDown = 10;
            this.isBroken = false;
        }

        public void turnOn() {
            this.untilBreakDown--;
            if (this.untilBreakDown <= 0) {
                this.isBroken = true;
                this.isOn = false;
                System.err.println("Light in room " + this.room + " is broken");
            } else {
                this.isOn = true;
            }
        }

        public void turnOff() {
            this.isOn = false;
        }

        public void repair() {
            this.isBroken = false;
            this.untilBreakDown = 10;
        }

        // public Literal getPercept() {
        // String status = this.isOn ? "on" : "off";
        // return Literal.parseLiteral("light(" + this.room + "," + status + ")");
        // }

        public Literal getPercept() {
            Literal percept = Literal.parseLiteral("is_light_on");
            System.err.println("IsOn: " + this.isOn);
            return percept;
        }

        public void executeAction(Structure action) {
            if (action.equals(turnOn)) {
                this.turnOn();
            } else if (action.equals(turnOff)) {
                this.turnOff();
            }
        }
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

    public ArrayList<Literal> getPercepts() {
        ArrayList<Literal> percepts = new ArrayList<Literal>();
        for (Light light : lights) {
            if (light.isOn) {
                percepts.add(light.getPercept());

            }
        }
        return percepts;
    }
}
