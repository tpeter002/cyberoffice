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
	private boolean areHumansFriend = true;

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
	public static final Literal recharge = Literal.parseLiteral("recharge(vc)");

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
            }
			else if (action.equals(pg)) {
                pickGarb();
			}
			else if (action.getFunctor().equals("recharge_route")) {
                moveTowards(0,0);
				if(model.getAgPos(this.id).x == 0 && model.getAgPos(this.id).y == 0){
					rechargeBattery();
				}
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	public void moveTowards(int x, int y){
		Location vc = model.getAgPos(this.id);
            if (vc.x < x)
                vc.x++;
            else if (vc.x > x)
                vc.x--;
            if (vc.y < y)
                vc.y++;
            else if (vc.y > y)
                vc.y--;
            model.setAgPos(this.id, vc);
	}
	private void rechargeBattery(){
		Location vc = model.getAgPos(this.id);
		if(vc.x == 0 && vc.y == 0){
			this.batteryLevel = 100;
		}
	}
	/** creates the agents perception based on the MarsModel */
	private ArrayList<Literal> updatePercepts() {
		ArrayList<Literal> percepts = new ArrayList<Literal>();

		updateRoom();

		Location vcLoc = model.getAgPos(this.id);
		//position of the vacuum cleaner
		Literal vcPos = Literal.parseLiteral("pos(vc," + vcLoc.x + "," + vcLoc.y + ")");
		//room which the vacuum cleaner is in
		//Literal vcRoom = Literal.parseLiteral("current_room(" + this.currRoom + ")");
		if (model.hasGarbage(vcLoc.x, vcLoc.y)) {
            percepts.add(gvc);
        }
		if(this.batteryLevel <= 20){
			percepts.add(recharge);
		}
		if(this.isBroken){
			percepts.add(Literal.parseLiteral("broken"));
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
			try{
				Thread.sleep(3000);
			}
			catch(Exception e){
				System.out.println("Error in sleep");
			}
			this.batteryLevel -= 90;
			model.removeGarbage(vc.x, vc.y);
		}
	}

	void cleanInnerRoom() throws Exception {
		Location vc = model.getAgPos(this.id);
		if (this.direction == DIRECTION.RIGHT){
			vc.x++;
			if ( this.GSize<=vc.x || model.isWall(vc.x, vc.y)) {
				this.direction = DIRECTION.LEFT;
				vc.x--;
				vc.y++;
				if(model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))){
					vc.y--;
				}
			}
		}
		else if (this.direction == DIRECTION.LEFT){
			vc.x--;
			if (0 > vc.x || model.isWall(vc.x, vc.y)) {
				this.direction = DIRECTION.RIGHT;
				vc.x++;
				vc.y++;
				if (model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))){
					vc.y--;
				}
			}
		}
		model.setAgPos(this.id, vc);
	}
	public Location avoidHumans(Location vc) throws Exception {
		Location mod = vc;
		if (this.areHumansFriend) {
			if (model.cellOccupied(vc.x, vc.y)) {
				if (this.direction == DIRECTION.RIGHT) {
					if (model.inGrid(vc.x, vc.y + 1) && !model.isWall(vc.x, vc.y + 1) && !model.cellOccupied(vc.x, vc.y + 1)) {
						// Move down
						mod.y++;
						model.setAgPos(this.id, mod);
						// Continue moving right
						mod.x++;
						if (model.inGrid(mod.x, mod.y) && !model.isWall(mod.x, mod.y) && !model.cellOccupied(mod.x, mod.y)) {
							model.setAgPos(this.id, mod);
						}
						// Move back up
						mod.y--;
						model.setAgPos(this.id, mod);
					}
				} else if (this.direction == DIRECTION.LEFT) {
					if (model.inGrid(vc.x, vc.y - 1) && !model.isWall(vc.x, vc.y - 1) && !model.cellOccupied(vc.x, vc.y - 1)) {
						// Move up
						mod.y--;
						model.setAgPos(this.id, mod);
						// Continue moving left
						mod.x--;
						if (model.inGrid(mod.x, mod.y) && !model.isWall(mod.x, mod.y) && !model.cellOccupied(mod.x, mod.y)) {
							model.setAgPos(this.id, mod);
						}
						// Move back down
						mod.y++;
						model.setAgPos(this.id, mod);
					}
				}
			}
		}
		return mod;
	}
	/*
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

