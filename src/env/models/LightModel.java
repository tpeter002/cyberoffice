package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;
import java.util.ArrayList;
import java.util.Arrays;

public class LightModel  {
    
    private OfficeModel model;

    /*
    * Action that we want the model to react to
    * simple in this case: BDI agent starts operating the lights
    * might need to be more complex in the future
    */
    public static final Term operate = Literal.parseLiteral("operate");

    // we store the states in an array
    private ArrayList<Light> lights = new ArrayList<Light>();


    /*  
    * All models get the OfficeModel object, which stores
    * the overall state of the environment
    * Methods of OfficeModel at this moment:
    *   whichRoom(int x, int y) - returns the room at the given coordinates
    *   roomIsEmpty(ROOM room) - returns true if the room is empty
    *   cellOccupied(int x, int y) - returns true if the cell is occupied
    *   addGarbage(int x, int y) - adds garbage to the cell
    *   removeGarbage(int x, int y) - removes garbage from the cell
    *   hasGarbage(int x, int y) - returns true if the cell has garbage
    */
    public LightModel(OfficeModel model, int GSize){
        this.model = model;

        lights.add(new Light(OfficeModel.ROOM.PRINTER)); 
        lights.add(new Light(OfficeModel.ROOM.VACUUM));
        lights.add(new Light(OfficeModel.ROOM.HALL));
    }

    private class Light {
        /*
        * Light class to keep track of states of lights
        * lights are identified by OfficeModel.ROOM enumerator
        */

        OfficeModel.ROOM room;
        boolean isOn;

        public Light(OfficeModel.ROOM room) {
            this.room = room;
            this.isOn = false;
        }

        public void turnOn() {
            this.isOn = true;
        }

        public void turnOff() {
            this.isOn = false;
        }

        public Literal getPercept() {
            Literal percept = Literal.parseLiteral("light(" + this.room + ")");
            return percept;
        }
    }


    /*
    * Here we change the state of the model of the agent
    * which we will later pass on to the agents as percepts using getPercepts()
    */
    public void executeAction(Structure action) {
        if(action.equals("operate")) {
            for (Light light: lights) {
                if (model.roomIsEmpty(light.room)) {
                    light.turnOn();
                } else {
                    light.turnOff();
        }}
        } else {
            System.out.println("Invalid action");
        }
    }    



    /*
    * Here we infer which percepts to give to the agents
    * based on the state of the model
    */
    public ArrayList<Literal> getPercepts() {
        ArrayList<Literal> percepts = new ArrayList<Literal>();
        for (Light light: lights) {
            if (light.isOn) {
                percepts.add(light.getPercept());
            }
        }
        return percepts;
    }
}
