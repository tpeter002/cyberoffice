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
	private WHEREINROOM whereInRoom;
	int[] lastMoveArray = new int[2];

	private boolean areHumansFriend = true;

	private enum DIRECTION {
		RIGHT,
		LEFT,
		UP,
		DOWN,
	}

	private DIRECTION xDirection;
	private DIRECTION yDirection;

	public static final Literal next_slot = Literal.parseLiteral("next_slot");
	public static final Literal slot_has_garbage = Literal.parseLiteral("slot_has_garbage");
	public static final Literal pick_garbage = Literal.parseLiteral("pick_garbage");

	public static final Literal should_go_home = Literal.parseLiteral("should_go_home");
	public static final Literal move_home = Literal.parseLiteral("move_home");
	public static final Literal at_home = Literal.parseLiteral("at_home");
	public static final Literal at_room_to_clean = Literal.parseLiteral("at_room_to_clean");

	public static final Literal empty_garbage = Literal.parseLiteral("empty_garbage");
	public static final Literal recharge_battery = Literal.parseLiteral("recharge_battery");
	public static final Literal enter_room = Literal.parseLiteral("enter_room");

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
		this.xDirection = DIRECTION.RIGHT;
		initializePositions(GSize);
	}

	public void initializePositions(int GSize) {
		model.setAgPos(this.id, 0, 0);
		this.yDirection = DIRECTION.DOWN;
		updateRoom();
		updateWhereInRoom(model.getAgPos(this.id));
	}

	public void updateRoom() {
		this.currentRoom = model.whichRoom(model.getAgPos(this.id).x, model.getAgPos(this.id).y);
	}

	public ArrayList<Percept> newPercepts() {
		ArrayList<Percept> percepts = new ArrayList<Percept>();

		Location vc = model.getAgPos(this.id);

		updateRoom();
		updateWhereInRoom(vc);

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
		}
		if (model.getAgPos(this.id).equals(this.firstDoor)) {
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
				} else {
					switch (this.yDirection) {
						case DOWN:
							cleanCurrentRoomDown();
							break;
						case UP:
							cleanCurrentRoomUp();
							break;
						default:
							System.out.println("DEFAULT" + this.whereInRoom);
							moveTowards(homePosition);
							break;
					}
				}
			} else if (action.getFunctor().equals("go_to")) {
				int x = ((int) ((NumberTerm) action.getTerm(0)).solve());
				goTowardSelectedRoom(OfficeModel.ROOM.values()[x]);
			} else if (action.equals(pick_garbage)) {
				pickGarbage();
			} else if (action.equals(move_home)) {
				if (this.currentRoom != model.whichRoom(homePosition.x, homePosition.y)) {
					goTowardSelectedRoom(OfficeModel.ROOM.HALL);
				} else {
					moveTowards(homePosition);
				}
			} else if (action.equals(empty_garbage)) {
				empty_garbage();
			} else if (action.equals(recharge_battery)) {
				recharge_battery();
			} else if (action.equals(get_location)) {
				get_location();
			} else if (action.equals(fix)) {
				fix();
			} else if (action.equals(enter_room)) {
				enter_room();
			} else {
				System.out.println("Action not implemented: " + action);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void goTowardSelectedRoom(OfficeModel.ROOM room) {
		this.roomToClean = room;
		Location vc = model.getAgPos(this.id);
		Location next = vc;
		Location door = model.getDoorwayPos(room, model.whichRoom(vc.x, vc.y));
		if (door == null) {
			door = model.getDoorwayPos(room, OfficeModel.ROOM.HALL);
		}

		if ((model.whichRoom(vc.x, vc.y) == OfficeModel.ROOM.VACUUM && room == OfficeModel.ROOM.PRINTER
				|| model.whichRoom(vc.x, vc.y) == OfficeModel.ROOM.PRINTER && room == OfficeModel.ROOM.VACUUM)
				&& this.firstDoor == null) {
			this.firstDoor = door;
			this.firstRoom = model.whichRoom(vc.x, vc.y);
		}
		if (this.whereToCleanNow == null && this.firstDoor == null) {
			whereToCleanNow = door;
		}

		if (this.firstDoor != null) {
			door = firstDoor;
		}
		if (vc.x < door.x) {
			if (!model.isWall(vc.x + 1, vc.y)) {
				next.x = vc.x + 1;
				lastMoveArray[0] = 1;
				lastMoveArray[1] = 0;
			} else {
				if (lastMoveArray[1] == 1) {
					next.y = vc.y + 1;
					lastMoveArray[1] = +1;
				} else {
					next.y = vc.y - 1;
					lastMoveArray[0] = -1;
				}
			}
		}

		if (vc.x > door.x && !model.isWall(vc.x - 1, vc.y)) {
			next.x = vc.x - 1;
			lastMoveArray[1] = 0;
		}

		if (vc.y < door.y && !model.isWall(vc.x, vc.y + 1) && lastMoveArray[1] != -1) {
			next.y = vc.y + 1;
			lastMoveArray[1] = 1;
		}

		if (vc.y > door.y && !model.isWall(vc.x, vc.y - 1) && lastMoveArray[1] != 1) {
			lastMoveArray[1] = -1;
			next.y = vc.y - 1;
		}
		next = avoidObstacle(next);

		model.setAgPos(this.id, next);
	}

	public enum WHEREINROOM {
		UPPEREDGE,
		LOWEREDGE,
		LEFTEDGE,
		RIGHTEDGE,
		UPPERRIGHTCORNER,
		UPPERLEFTCORNER,
		LOWERRIGHTCORNER,
		LOWERLEFTCORNER,
		MIDDLE
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

		model.setAgPos(this.id, next);

	}

	public void enter_room() {
		Location vc = model.getAgPos(this.id);
		if (model.whichRoom(vc.x, vc.y - 1) == this.roomToClean) {
			this.yDirection = DIRECTION.UP;
			this.roomToClean = null;
		} else if (model.whichRoom(vc.x, vc.y + 1) == this.roomToClean) {
			this.yDirection = DIRECTION.DOWN;
			this.roomToClean = null;
		}
	}

	public void updateWhereInRoom(Location vc) {
		if ((model.isWall(vc.x + 1, vc.y) || !(model.inGrid(vc.x + 1, vc.y)))
				&& (model.isWall(vc.x, vc.y - 1) || !(model.inGrid(vc.x, vc.y - 1)))) {
			this.whereInRoom = WHEREINROOM.UPPERRIGHTCORNER;
		} else if ((model.isWall(vc.x - 1, vc.y) || !(model.inGrid(vc.x - 1, vc.y)))
				&& (model.isWall(vc.x, vc.y - 1) || !(model.inGrid(vc.x, vc.y - 1)))) {
			this.whereInRoom = WHEREINROOM.UPPERLEFTCORNER;
		} else if ((model.isWall(vc.x + 1, vc.y) || !(model.inGrid(vc.x + 1, vc.y)))
				&& (model.isWall(vc.x, vc.y + 1) || !(model.inGrid(vc.x, vc.y + 1)))) {
			this.whereInRoom = WHEREINROOM.LOWERRIGHTCORNER;
		} else if ((model.isWall(vc.x - 1, vc.y) || !(model.inGrid(vc.x - 1, vc.y)))
				&& (model.isWall(vc.x, vc.y + 1) || !(model.inGrid(vc.x, vc.y + 1)))) {
			this.whereInRoom = WHEREINROOM.LOWERLEFTCORNER;
		} else if (!(model.inGrid(vc.x, vc.y - 1)) || model.isWall(vc.x, vc.y - 1)) {
			this.whereInRoom = WHEREINROOM.UPPEREDGE;
		} else if (!(model.inGrid(vc.x, vc.y + 1)) || model.isWall(vc.x, vc.y + 1)) {
			this.whereInRoom = WHEREINROOM.LOWEREDGE;
		} else if (!(model.inGrid(vc.x + 1, vc.y)) || model.isWall(vc.x + 1, vc.y)) {
			this.whereInRoom = WHEREINROOM.RIGHTEDGE;
		} else if (!(model.inGrid(vc.x - 1, vc.y)) || model.isWall(vc.x - 1, vc.y)) {
			this.whereInRoom = WHEREINROOM.LEFTEDGE;
		} else {
			this.whereInRoom = WHEREINROOM.MIDDLE;
		}
	}

	public void pickGarbage() {
		Location vc = model.getAgPos(this.id);
		this.garbageSpace -= 5;
		this.batteryLevel -= 5;
		model.removeGarbage(vc.x, vc.y);
	}

	void cleanCurrentRoomDown() {
		Location vc = model.getAgPos(this.id);
		if (this.xDirection == DIRECTION.RIGHT) {
			vc.x++;
			if (this.GSize <= vc.x || model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))) {
				this.xDirection = DIRECTION.LEFT;
				vc.x--;
				vc.y++;
			}
		} else if (this.xDirection == DIRECTION.LEFT) {
			vc.x--;
			if (0 > vc.x || model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))) {
				this.xDirection = DIRECTION.RIGHT;
				vc.x++;
				vc.y++;
			}
		}
		vc = avoidObstacle(vc);
		updateRoom();
		updateWhereInRoom(vc);
		if (this.whereInRoom == WHEREINROOM.LOWERLEFTCORNER || this.whereInRoom == WHEREINROOM.LOWERRIGHTCORNER) {
			switchYdirection();
		}
		// randomly break
		if (random.nextDouble() < ERROR_PROBABILITY) {
			this.isBroken = true;
		}
		model.setAgPos(this.id, vc);
	}

	void cleanCurrentRoomUp() {
		Location vc = model.getAgPos(this.id);
		if (this.xDirection == DIRECTION.RIGHT) {
			vc.x++;
			if (this.GSize <= vc.x || model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))) {
				this.xDirection = DIRECTION.LEFT;
				vc.x--;
				vc.y--;
			}
		} else if (this.xDirection == DIRECTION.LEFT) {
			vc.x--;
			if (0 > vc.x || model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))) {
				this.xDirection = DIRECTION.RIGHT;
				vc.x++;
				vc.y--;
			}
		}
		vc = avoidObstacle(vc);
		updateRoom();
		updateWhereInRoom(vc);
		if (this.whereInRoom == WHEREINROOM.UPPERRIGHTCORNER || this.whereInRoom == WHEREINROOM.UPPERLEFTCORNER) {
			switchYdirection();
		}
		// randomly break
		if (random.nextDouble() < ERROR_PROBABILITY) {
			this.isBroken = true;
		}
		model.setAgPos(this.id, vc);
	}

	public void switchYdirection() {
		Location vc = model.getAgPos(this.id);
		switch (this.whereInRoom) {
			case UPPERLEFTCORNER:
				this.yDirection = DIRECTION.DOWN;
				break;
			case UPPERRIGHTCORNER:
				this.yDirection = DIRECTION.DOWN;
				break;
			case LOWERLEFTCORNER:
				this.yDirection = DIRECTION.UP;
				break;
			case LOWERRIGHTCORNER:
				this.yDirection = DIRECTION.UP;
				break;
			default:
				break;
		}
	}

	// The object code of CLEAN which is not defined in the agent program is 0.
	// There is a possibility that we need to add this to isFree
	public Location avoidObstacle(Location vc) {
		if (this.areHumansFriend) {
			if ((!(model.isFree(vc.x, vc.y)) && !(model.hasGarbage(vc.x, vc.y))) || (model.cellOccupied(vc.x, vc.y))) {
				if (model.isFree(vc.x, vc.y + 1) && !(model.cellOccupied(vc.x, vc.y + 1)))
					vc.y++;
				else if (model.isFree(vc.x + 1, vc.y) && !(model.cellOccupied(vc.x + 1, vc.y)))
					vc.x++;
				else if (model.isFree(vc.x, vc.y - 1) && !(model.cellOccupied(vc.x, vc.y - 1)))
					vc.y--;
				else if (model.isFree(vc.x - 1, vc.y) && !(model.cellOccupied(vc.x - 1, vc.y)))
					vc.x--;
				else {
					System.out.println("Error avoiding obstacle, someone might get hurt.");
				}
			}
		}
		return vc;
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
