package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;
import jason.environment.grid.Location;
import jason.stdlib.empty;
import env.OfficeEnv.OfficeModel;
import env.OfficeEnv.Percept;

import java.util.ArrayList;

// Human agent environment class
public class VacuumCleanerModel  {
	
	private OfficeModel model;
	private int id;
	private int GSize;

	private Location homePosition;

	private int garbageSpace;
	private int batteryLevel;
	private boolean isBroken;
	private boolean requestedLocation;
	private OfficeModel.ROOM currentRoom;

	private boolean areHumansFriend = true;

	private enum DIRECTION {
		RIGHT,
		LEFT,
		UP,
		DOWN,
	}
	private DIRECTION direction;

	public static final Literal next_slot = Literal.parseLiteral("next_slot");
	public static final Literal slot_has_garbage = Literal.parseLiteral("slot_has_garbage");
	public static final Literal pick_garbage = Literal.parseLiteral("pick_garbage");

	public static final Literal should_go_home = Literal.parseLiteral("should_go_home");
	public static final Literal move_home = Literal.parseLiteral("move_home");
	public static final Literal at_home = Literal.parseLiteral("at_home");

	public static final Literal empty_garbage = Literal.parseLiteral("empty_garbage");
	public static final Literal recharge_battery = Literal.parseLiteral("recharge_battery");

	public static final Literal current_room_empty = Literal.parseLiteral("current_room_empty");
	public static final Literal get_location = Literal.parseLiteral("get_location");
	
	public static final Literal error = Literal.parseLiteral("error");
	public static final Literal fix = Literal.parseLiteral("fix");
	
	Random random = new Random(System.currentTimeMillis());
	private static final double ERROR_PROBABILITY = 0.01;
	
	public VacuumCleanerModel(OfficeModel model, int GSize){
		this.id = 1;
		this.model = model;
		this.homePosition = new Location(0,0);
		this.isBroken = false;
		this.requestedLocation = false;
		this.garbageSpace = 100;
		this.batteryLevel = 100;
		this.GSize = GSize;
		this.direction = DIRECTION.RIGHT;
		initializePositions(GSize);
	}

	public void initializePositions(int GSize){
		model.setAgPos(this.id, 0, 0);
		updateRoom();
	}

	public void updateRoom() {
		this.currentRoom = model.whichRoom(model.getAgPos(this.id).x, model.getAgPos(this.id).y);
	}
	
	public ArrayList<Percept> getNewPercepts() {
        ArrayList<Percept> percepts = new ArrayList<Percept>();

		updateRoom();

		Location vc = model.getAgPos(this.id);
		
		if (model.hasGarbage(vc.x, vc.y)) {
            percepts.add(new Percept(slot_has_garbage));
        }

		if(this.batteryLevel <= 20 || this.garbageSpace <= 20) {
			percepts.add(new Percept(should_go_home));
		}

		if(model.getAgPos(this.id) == homePosition) {
			percepts.add(new Percept(at_home));
		}
		
		if (model.roomIsEmpty(this.currentRoom)) {
			percepts.add(new Percept(current_room_empty));
		}

		if(this.isBroken) {
			percepts.add(new Percept(error));
		}

		if(this.requestedLocation) {
			percepts.add(new Percept(Literal.parseLiteral("location(" + vc.x + ", " + vc.y + ")")));
			this.requestedLocation = false;
		}

		return percepts;
    }

	public ArrayList<Percept> getPerceptsToRemove() {
		ArrayList<Percept> percepts = new ArrayList<Percept>();

		Location vc = model.getAgPos(this.id);

		if (!model.hasGarbage(vc.x, vc.y)) {
            percepts.add(new Percept(slot_has_garbage));
        }

		if(!(this.batteryLevel <= 20 || this.garbageSpace <= 20)){
			percepts.add(new Percept(should_go_home));
		}

		if(model.getAgPos(this.id) != homePosition) {
			percepts.add(new Percept(at_home));
		}
		
		if (!model.roomIsEmpty(this.currentRoom)) {
			percepts.add(new Percept(current_room_empty));
		}

		if(!this.isBroken){
			percepts.add(new Percept(error));
		}

		return percepts;
	}

	public void executeAction(Structure action){
        try {

            if (action.equals(next_slot)) {
				if (!model.roomIsEmpty(this.currentRoom)) {
					System.out.println("Room is not empty");
					moveTowards(homePosition);
				}
				else if (this.currentRoom==OfficeModel.ROOM.DOORWAY){
					cleanCurrentRoom();
					System.out.println("ajtoban vagyok");
				}
				else{
					cleanCurrentRoom();
				}
            }
			else if (action.equals(pick_garbage)) {
                pickGarbage();
			}
			else if (action.equals(move_home)) {
                moveTowards(homePosition);
			}
			else if (action.equals(empty_garbage)) {
				empty_garbage();
			}
			else if (action.equals(recharge_battery)) {
				recharge_battery();
			}
			else if (action.equals(get_location)) {
				get_location();
			}
			else if (action.equals(fix)) {
				fix();
			}
			
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void moveTowards(Location loc) {
		Location vc = model.getAgPos(this.id);
            if (vc.x < loc.x)
                vc.x++;
            else if (vc.x > loc.x)
                vc.x--;
            if (vc.y < loc.y)
                vc.y++;
            else if (vc.y > loc.y)
                vc.y--;
            model.setAgPos(this.id, vc);
	}

	public void pickGarbage() {
		Location vc = model.getAgPos(this.id);
		this.garbageSpace -= 90;
		this.batteryLevel -= 90;
		model.removeGarbage(vc.x, vc.y);
	}	

	void cleanCurrentRoom() {
		Location vc = model.getAgPos(this.id);
		if (this.direction == DIRECTION.RIGHT){
			vc.x++;
			avoidHumans(vc);
			avoidHumans(vc);
			if (this.GSize<=vc.x || model.isWall(vc.x, vc.y)) {
				this.direction = DIRECTION.LEFT;
				vc.x--;
				vc.y++;
				if(model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))){
					vc.y--;
					avoidHumans(vc);
				}
			}
		}
		else if (this.direction == DIRECTION.LEFT){
			vc.x--;
			avoidHumans(vc);
			avoidHumans(vc);
			if (0 > vc.x || model.isWall(vc.x, vc.y)) {
				this.direction = DIRECTION.RIGHT;
				vc.x++;
				vc.y++;
				if (model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))){
					vc.y--;
					avoidHumans(vc);
				}
			}
		}
		model.setAgPos(this.id, vc);
	}

	public void avoidHumans(Location vc) {
		if (this.areHumansFriend) {
			if (model.cellOccupied(vc.x, vc.y)) {
				System.out.println("Human detected, moving away...");
				if (this.direction == DIRECTION.RIGHT) {
					if (model.inGrid(vc.x, vc.y - 1) && !model.isWall(vc.x, vc.y - 1) && !model.cellOccupied(vc.x, vc.y - 1)) {
						// Move up
						vc.y--;
						model.setAgPos(this.id, vc);
					}
					else if (model.inGrid(vc.x, vc.y + 1) && !model.isWall(vc.x, vc.y + 1) && !model.cellOccupied(vc.x, vc.y + 1)) {
						// Move down
						vc.y++;
						model.setAgPos(this.id, vc);
						
					}
				} else if (this.direction == DIRECTION.LEFT) {
					if (model.inGrid(vc.x, vc.y - 1) && !model.isWall(vc.x, vc.y - 1) && !model.cellOccupied(vc.x, vc.y - 1)) {
						// Move up
						vc.y--;
						model.setAgPos(this.id, vc);
					}
					else if (model.inGrid(vc.x, vc.y + 1) && !model.isWall(vc.x, vc.y + 1) && !model.cellOccupied(vc.x, vc.y + 1)) {
						// Move down
						vc.y++;
						model.setAgPos(this.id, vc);
					}
				}
			}
		}
	}	

	private void empty_garbage(){
		this.garbageSpace = 100;
		System.out.println("Garbage emptied");
	}

	private void recharge_battery(){
		this.batteryLevel = 100;
		System.out.println("Battery recharged");
	}

	public void get_location() {
		this.requestedLocation = true;
	}

	public void fix() {
		this.isBroken = false;
	}	

}
