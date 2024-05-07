package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;
import jason.environment.grid.Location;
import jason.stdlib.empty;
import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;
import env.Percept;

import java.time.OffsetTime;
import java.util.ArrayList;

// Human agent environment class
public class VacuumCleanerModel {

	private OfficeModel model;
	private int id;
	private int GSize;

	private Location homePosition;
	private Location whereToCleanNow;
	private Location firstDoor;

	private int garbageSpace;
	private int batteryLevel;
	private boolean isBroken;
	private boolean requestedLocation;
	private Location lastMove;
	private OfficeModel.ROOM currentRoom;
	private OfficeModel.ROOM firstRoom;
	private OfficeModel.ROOM roomToClean;
	int[] lastMoveArray = new int[2];

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
	public static final Literal at_room_to_clean = Literal.parseLiteral("at_room_to_clean");

	public static final Literal empty_garbage = Literal.parseLiteral("empty_garbage");
	public static final Literal recharge_battery = Literal.parseLiteral("recharge_battery");

	public static final Literal current_room_has_people = Literal.parseLiteral("current_room_has_people");
	public static final Literal get_location = Literal.parseLiteral("get_location");

	public static final Literal error = Literal.parseLiteral("error");
	public static final Literal fix = Literal.parseLiteral("fix");

	Random random = new Random(System.currentTimeMillis());
	private static final double ERROR_PROBABILITY = 0.00;

	public VacuumCleanerModel(OfficeModel model, int GSize) {
		this.id = 1;
		this.model = model;
		this.homePosition = new Location(0, 0);
		this.isBroken = false;
		this.requestedLocation = false;
		this.garbageSpace = 100;
		this.batteryLevel = 100;
		this.lastMove = new Location(0, 0);
		this.GSize = GSize;
		this.direction = DIRECTION.RIGHT;
		initializePositions(GSize);
	}

	public void initializePositions(int GSize) {
		model.setAgPos(this.id, 0, 0);
		updateRoom();
	}

	public void updateRoom() {
		this.currentRoom = model.whichRoom(model.getAgPos(this.id).x, model.getAgPos(this.id).y);
	}

	public ArrayList<Percept> newPercepts() {
		ArrayList<Percept> percepts = new ArrayList<Percept>();

		updateRoom();

		Location vc = model.getAgPos(this.id);

		if (model.hasGarbage(vc.x, vc.y)) {
			percepts.add(new Percept(slot_has_garbage));
		}

		if (this.batteryLevel <= 20 || this.garbageSpace <= 20) {
			percepts.add(new Percept(should_go_home));
		}

		if (model.getAgPos(this.id).equals(homePosition)) {
			percepts.add(new Percept(at_home));
		}
		if (model.getAgPos(this.id).equals(this.whereToCleanNow) && this.roomToClean != null) {
				percepts.add(new Percept(at_room_to_clean));
				this.roomToClean = null;
		}
		if(model.getAgPos(this.id).equals(this.firstDoor)) {
			System.out.println("elertem az elso ajtot");
			this.firstDoor = null;
		}

		if (!model.roomIsEmpty(this.currentRoom)) {
			percepts.add(new Percept(current_room_has_people));
		}

		if (this.isBroken) {
			percepts.add(new Percept(error));
		}

		if (this.requestedLocation) {
			percepts.add(new Percept(Literal.parseLiteral("location(" + vc.x + ", " + vc.y + ")")));
			this.requestedLocation = false;
		}

		return percepts;
	}

	public ArrayList<Percept> perceptsToRemove() {
		ArrayList<Percept> percepts = new ArrayList<Percept>();

		Location vc = model.getAgPos(this.id);

		if (!model.hasGarbage(vc.x, vc.y)) {
			percepts.add(new Percept(slot_has_garbage));
		}

		if (!(this.batteryLevel <= 20 || this.garbageSpace <= 20)) {
			percepts.add(new Percept(should_go_home));
		}

		if (!model.getAgPos(this.id).equals(homePosition)) {
			percepts.add(new Percept(at_home));
		}

		if (model.roomIsEmpty(this.currentRoom)) {
			percepts.add(new Percept(current_room_has_people));
		}

		if (!this.isBroken) {
			percepts.add(new Percept(error));
		}

		return percepts;
	}

	public void executeAction(Structure action) {
		try {

			if (action.equals(next_slot)) {
				if (!model.roomIsEmpty(this.currentRoom)) {
					System.out.println("Room is not empty");
					moveTowards(homePosition);
				} else if (this.currentRoom == OfficeModel.ROOM.DOORWAY) {
					cleanCurrentRoom();
					System.out.println("ajtoban vagyok");
				} else {
					cleanCurrentRoom();
				}
			} else if (action.getFunctor().equals("go_to")) {
				int x = ((int) ((NumberTerm) action.getTerm(0)).solve());
				System.out.println("funktoros geci");
				System.out.println(x);
				goTowardSelectedRoom(OfficeModel.ROOM.values()[x]);
			} else if (action.equals(pick_garbage)) {
				pickGarbage();
			} else if (action.equals(move_home)) {
				moveTowards(homePosition);
			} else if (action.equals(empty_garbage)) {
				empty_garbage();
			} else if (action.equals(recharge_battery)) {
				recharge_battery();
			} else if (action.equals(get_location)) {
				get_location();
			} else if (action.equals(fix)) {
				fix();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void goTowardSelectedRoom(OfficeModel.ROOM room) {
		System.out.println("bejutottam a go towardba");
		this.roomToClean = room;
		Location vc = model.getAgPos(this.id);
		Location next = vc;
		Location door = model.getDoorwayPos(room, model.whichRoom(vc.x, vc.y));
		if(door == null) {
			System.out.println("beallitottam a " + room + "es " + OfficeModel.ROOM.HALL + " ajtot"	);
			door = model.getDoorwayPos(room, OfficeModel.ROOM.HALL);
		}
		
		if((model.whichRoom(vc.x, vc.y) == OfficeModel.ROOM.VACUUM && room == OfficeModel.ROOM.PRINTER || model.whichRoom(vc.x, vc.y) == OfficeModel.ROOM.PRINTER && room == OfficeModel.ROOM.VACUUM) && this.firstDoor == null)
		 {
			this.firstDoor = door;
			this.firstRoom = model.whichRoom(vc.x, vc.y);
			System.out.println("elso ajto beallitva");
		}
		if(this.whereToCleanNow == null && this.firstDoor == null) {
			System.out.println("beallitottam a where to cleannowt");
			whereToCleanNow = door;
			System.out.println("itt vagyok: " + vc.x + " " + vc.y + "es ide megyek: " + door.x + " " + door.y);
		}
		
		//this.whereToCleanNow = door;
		if(this.firstDoor != null) {
			System.out.println("door beallitva firstdoorra");
			door = firstDoor;
		}
		if (vc.x < door.x )
		{
			if(!model.isWall(vc.x + 1, vc.y))
			{
				next.x = vc.x + 1;
				lastMoveArray[0] = 1;
				lastMoveArray[1] = 0;
			}
			else
			{
				if(lastMoveArray[1] == 1)
				{
					next.y = vc.y  +1;
					lastMoveArray[1] = + 1;
				}
				else
				{
					next.y = vc.y - 1;
					lastMoveArray[0] = -1;
				}
			}
		}
			
		if (vc.x > door.x && !model.isWall(vc.x - 1, vc.y))
		{
			next.x = vc.x - 1;
			lastMoveArray[1] = 0;
		}
	
		if (vc.y < door.y && !model.isWall(vc.x, vc.y+1) && lastMoveArray[1] != -1)
		{
			next.y = vc.y + 1;
			lastMoveArray[1] = 1;	
		}
		
		if (vc.y > door.y && !model.isWall(vc.x, vc.y-1) && lastMoveArray[1] != 1) 
		{
			lastMoveArray[1] = -1;
			next.y = vc.y - 1;
		}

		model.setAgPos(this.id, next);
	}

	public void moveRight() {
		Location vc = model.getAgPos(this.id);
		vc.x++;
		model.setAgPos(this.id, vc);
	}

	public void moveLeft() {
		Location vc = model.getAgPos(this.id);
		vc.x--;
		model.setAgPos(this.id, vc);
	}

	public void moveUp() {
		Location vc = model.getAgPos(this.id);
		vc.y++;
		model.setAgPos(this.id, vc);
	}

	public void moveDown() {
		Location vc = model.getAgPos(this.id);
		vc.y--;
		model.setAgPos(this.id, vc);
	}

	public void moveTowards(Location loc) {
		Location vc = model.getAgPos(this.id);
		Location next = vc;
		// boolean inSameRoom = model.whichRoom(vc.x, vc.y) == model.whichRoom(loc.x,
		// loc.y);
		// boolean inDoorway = this.currentRoom == OfficeModel.ROOM.DOORWAY;
		// if (!inSameRoom) {
		// Location door = model.getDoorwayPos(model.whichRoom(loc.x, loc.y));
		// loc.x = door.x;
		// loc.y = door.y;
		// }
		// if (inDoorway) {
		// loc.x = lastMove.x + vc.x;
		// loc.y = lastMove.y + vc.y;
		// }

		int dx = Math.abs(loc.x - vc.x);
		int dy = Math.abs(loc.y - vc.y);

		if (dx > dy) {
			if (vc.x < loc.x)
				next.x = vc.x + 1;
			if (vc.x > loc.x)
				next.x = vc.x - 1;
		} else {
			if (vc.y < loc.y)
				next.y = vc.y + 1;
			if (vc.y > loc.y)
				next.y = vc.y - 1;
		}

		if (model.isWall(next.x, vc.y)) {
			next.x = vc.x;
		}
		if (model.isWall(vc.x, next.y)) {
			next.y = vc.y;
		}

		// this.lastMove.x = next.x - vc.x;
		// this.lastMove.y = next.y - vc.y;

		model.setAgPos(this.id, next);

	}

	public void pickGarbage() {
		Location vc = model.getAgPos(this.id);
		this.garbageSpace -= 5;
		this.batteryLevel -= 5;
		model.removeGarbage(vc.x, vc.y);
	}

	void cleanCurrentRoom() {
		Location vc = model.getAgPos(this.id);
		if (this.direction == DIRECTION.RIGHT) {
			vc.x++;
			avoidHumans(vc);
			avoidHumans(vc);
			if (this.GSize <= vc.x || model.isWall(vc.x, vc.y)) {
				this.direction = DIRECTION.LEFT;
				vc.x--;
				vc.y++;
				if (model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))) {
					vc.y--;
					avoidHumans(vc);
				}
			}
		} else if (this.direction == DIRECTION.LEFT) {
			vc.x--;
			avoidHumans(vc);
			avoidHumans(vc);
			if (0 > vc.x || model.isWall(vc.x, vc.y)) {
				this.direction = DIRECTION.RIGHT;
				vc.x++;
				vc.y++;
				if (model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))) {
					vc.y--;
					avoidHumans(vc);
				}
			}
		}
		updateRoom();
		// randomly break
		if (random.nextDouble() < ERROR_PROBABILITY) {
			this.isBroken = true;
		}
		model.setAgPos(this.id, vc);
	}

	public void avoidHumans(Location vc) {
		if (this.areHumansFriend) {
			if (model.cellOccupied(vc.x, vc.y)) {
				System.out.println("Human detected, moving away...");
				if (this.direction == DIRECTION.RIGHT) {
					if (model.inGrid(vc.x, vc.y - 1) && !model.isWall(vc.x, vc.y - 1)
							&& !model.cellOccupied(vc.x, vc.y - 1)) {
						// Move up
						vc.y--;
						model.setAgPos(this.id, vc);
					} else if (model.inGrid(vc.x, vc.y + 1) && !model.isWall(vc.x, vc.y + 1)
							&& !model.cellOccupied(vc.x, vc.y + 1)) {
						// Move down
						vc.y++;
						model.setAgPos(this.id, vc);

					}
				} else if (this.direction == DIRECTION.LEFT) {
					if (model.inGrid(vc.x, vc.y - 1) && !model.isWall(vc.x, vc.y - 1)
							&& !model.cellOccupied(vc.x, vc.y - 1)) {
						// Move up
						vc.y--;
						model.setAgPos(this.id, vc);
					} else if (model.inGrid(vc.x, vc.y + 1) && !model.isWall(vc.x, vc.y + 1)
							&& !model.cellOccupied(vc.x, vc.y + 1)) {
						// Move down
						vc.y++;
						model.setAgPos(this.id, vc);
					}
				}
			}
		}
	}

	private void empty_garbage() {
		this.garbageSpace = 100;
	}

	private void recharge_battery() {
		this.batteryLevel = 100;
	}

	public void get_location() {
		this.requestedLocation = true;
	}

	public void fix() {
		this.isBroken = false;
	}

}
