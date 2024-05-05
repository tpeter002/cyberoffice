package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;

import java.util.ArrayList;

// Human agent environment class
public class VacuumCleanerModel  {
    
    private OfficeModel model;
    private int id;
    private boolean isBroken;
    private boolean isVacuumFull;
    private int batteryLevel;

    Random random = new Random(System.currentTimeMillis());
	private static final double BREAKDOWN_PROBABILITY = 0.01;

    private isRoomEmpty(){

    }
    /*
     * Constructor for the VacuumCleanerModel class
     */
    public VacuumCleanerModel(OfficeModel model, int GSize){
        this.id = 1;
        this.model = model;
        this.isBroken = false;
        this.isVacuumFull = false;
        this.batteryLevel = 100;
        initializePositions(GSize);
    }

    public void executeAction(Structure action){
        //if(action.equals("clean")){
            // Clean the floor
        //    model.removeObject(OfficeEnv.DIRT, model.getAgPos(1).x, model.getAgPos(1).y);
        //}
    }
    /*
     * Initialize starting positions of the vacuum cleaner
     */
    public void initializePositions(int GSize){
        
        model.setAgPos(this.id, 0, 0);
    }
    /*
     * Method to pick up garbage
     */
    public void pick(String garb) {
        // Implement the logic for picking up garbage
        // ...
        if (random.nextDouble() < BREAKDOWN_PROBABILITY) {
            this.isBroken = true;
        }
        // Check if the vacuum cleaner is full
        // ...
        if (isVacuumFull) {
            //model.addPercept(1, Literal.parseLiteral("vacuum_full"));
        }
    }
    public void move_to_room(String room) {
        // Implement the logic for moving to a specific room
        if (this.model.isRoomEmpty(room)) {
            //model.addPercept(1, Literal.parseLiteral("room_empty"));
        }
        // ...
        if (random.nextDouble() < BREAKDOWN_PROBABILITY) {
            this.isBroken = true;
        }
        this.batteryLevel -= 10; // Reduce battery level for each movement
        if (this.batteryLevel <= 20) {
            //model.addPercept(1, Literal.parseLiteral("low_battery"));
        }
    }
    public void repair() {
        System.out.println("Repairing vacuum cleaner...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.isBroken = false;
    }
    public void empty() {
        System.out.println("Emptying vacuum cleaner...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.isVacuumFull = false;
        //model.removePercept(1, Literal.parseLiteral("vacuum_full"));
    }
    public void recharge() {
        System.out.println("Recharging battery...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.batteryLevel = 100;
    }

    public ArrayList<Literal> getPercepts() {
        ArrayList<Literal> percepts = new ArrayList<Literal>();
        return percepts;
    }
}

