package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;
import jason.environment.grid.Location;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;

import java.util.ArrayList;

// Human agent environment class
public class VacuumCleanerModel  {
	
	private OfficeModel model;
	private int id;
	private int GSize;
	private boolean isBroken;
	private boolean isVacuumFull;
	private int batteryLevel;
	private OfficeModel.ROOM currRoom;

	private enum DIRECTION {
		RIGHT,
		LEFT,
		UP,
		DOWN,
	}
	private DIRECTION direction;

	public static final Term    ns = Literal.parseLiteral("next(slot)");
	public static final Term    pg = Literal.parseLiteral("pick(garb)");
	public static final Literal gvc = Literal.parseLiteral("garbage(vc)");

	Random random = new Random(System.currentTimeMillis());
	private static final double BREAKDOWN_PROBABILITY = 0.01;
	/*
	 * Constructor for the VacuumCleanerModel class
	 */
	public VacuumCleanerModel(OfficeModel model, int GSize){
		this.id = 1;
		this.model = model;
		this.isBroken = false;
		this.isVacuumFull = false;
		this.batteryLevel = 100;
		this.GSize = GSize;
		this.direction = DIRECTION.RIGHT;
		initializePositions(GSize);
	}
	//maybe change this to x,y coordinates and just go for the trash
	private boolean updateRoomView() {
		updateRoom();
		for(int x=0; x<this.GSize;x++){
			for(int y=0; y<this.GSize;y++){
				if(this.currRoom.equals(model.whichRoom(x, y))){
					System.out.println("Found the room which we are in");
					return true;
				}
			}
		}
		return false;
	}

	public void executeAction(Structure action){
        try {
            if (action.equals(ns)) {
                cleanInnerRoom();
            } else if (action.getFunctor().equals("move_towards")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
				if (this.currRoom.equals(model.whichRoom(x, y))) {
					System.out.println("Moving towards the trash");
                	moveTowards(x,y);
				}
				else {
					System.out.println("Can't see trash in this room");
				}
            } else if (action.equals(pg)) {
                pickGarb();
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	/** creates the agents perception based on the MarsModel */
	private ArrayList<Literal> updatePercepts() {
		ArrayList<Literal> percepts = new ArrayList<Literal>();

		updateRoom();
		percepts.clear();

		Location vcLoc = model.getAgPos(this.id);
		//position of the vacuum cleaner
		Literal vcPos = Literal.parseLiteral("pos(vc," + vcLoc.x + "," + vcLoc.y + ")");
		//room which the vacuum cleaner is in
		//Literal vcRoom = Literal.parseLiteral("current_room(" + this.currRoom + ")");
		if (model.hasGarbage(vcLoc.x, vcLoc.y)) {
            percepts.add(gvc);
        }
		percepts.add(vcPos);

		return percepts;
	}
	
	/*
	 * Method to update the current room of the vacuum cleaner
	 */
	public void updateRoom() {
		try{
			this.currRoom = model.whichRoom(model.getAgPos(this.id).x, model.getAgPos(this.id).y);
		} catch (Exception e) {
			System.out.println("Curr room couldn't be initalized: " + e);
		}
	}
	/*
	 * Initialize starting positions of the vacuum cleaner
	 */
	public void initializePositions(int GSize){
		model.setAgPos(this.id, 0, 0);
		updateRoom();
	}
	public void pickGarb() {
		Location vc = model.getAgPos(this.id);
		if (model.hasGarbage(vc.x, vc.y)) {
			System.out.println("Destorying da garbage");
			model.removeGarbage(vc.x, vc.y);
		}
	}

	public void moveTowards(int x, int y) throws Exception {
		Location r1 = model.getAgPos(this.id);
		if (r1.x < x)
			r1.x++;
		else if (r1.x > x)
			r1.x--;
		if (r1.y < y)
			r1.y++;
		else if (r1.y > y)
			r1.y--;
		model.setAgPos(this.id, r1);
	}
	void cleanInnerRoom() throws Exception {
		Location vc = model.getAgPos(this.id);
		if (this.direction == DIRECTION.RIGHT)
			vc.x++;
			if ( this.GSize<vc.x || model.isWall(vc.x, vc.y)) {
				this.direction = DIRECTION.LEFT;
				vc.x--;
				vc.y++;
				if(model.isWall(vc.x, vc.y)){
					vc.y--;
					System.out.println("Can't move in this direction");
				}
			}
		else if (this.GSize>vc.x || this.direction == DIRECTION.LEFT)
			vc.x--;
			if (model.isWall(vc.x, vc.y)) {
				this.direction = DIRECTION.RIGHT;
				vc.x++;
				vc.y++;
				if(model.isWall(vc.x, vc.y)){
					vc.y--;
					System.out.println("Can't move in this direction");
				}
			}
		System.out.println("Moving to the next slot:");
		System.out.println("x: " + vc.x + ", y: " + vc.y);
		model.setAgPos(this.id, vc);
	}
	/*
	 * Method to pick up garbage
	 
	public void pick(String garb) {
		// Implement the logic for picking up garbage
		if (this.model.hasGarbage(OfficeEnv.GARB, getAgPos(self.id))) {
			remove(OfficeEnv.GARB, getAgPos(self.id));
		}
		if (random.nextDouble() < BREAKDOWN_PROBABILITY) {
			this.isBroken = true;
		}
		// Check if the vacuum cleaner is full
		// ...
		if (isVacuumFull) {
			//model.addPercept(1, Literal.parseLiteral("vacuum_full"));
		}
	}*/
	public void move_to_room(String room) {
		// Implement the logic for moving to a specific room
		//if (this.model.isRoomEmpty(room)) {
			//model.addPercept(1, Literal.parseLiteral("room_empty"));
		//}
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
        return updatePercepts();
    }
}

