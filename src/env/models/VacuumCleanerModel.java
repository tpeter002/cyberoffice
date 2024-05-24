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

public class VacuumCleanerModel {

	private OfficeModel model;
	private int id;
	private int GSize;

	private OfficeModel.ROOM currentRoom;
	private OfficeModel.ROOM oldRoom;

	private boolean isBroken;
	private boolean requestedLocation;
	private boolean alreadyHaveTask = false;

	private boolean areHumansFriend = true;

	private enum DIRECTION {
		RIGHT,
		LEFT,
		UP,
		DOWN,
	}

	private DIRECTION xDirection;
	private int yOffset = 0;

	public static final Literal next_slot = Literal.parseLiteral("next_slot");
	public static final Literal slot_has_garbage = Literal.parseLiteral("slot_has_garbage");
	public static final Literal pick_garbage = Literal.parseLiteral("pick_garbage");

	public static final Literal people_in_current_room = Literal.parseLiteral("people_in_current_room");
	public static final Literal check_room_empty = Literal.parseLiteral("check_room_empty");

	public static final Literal get_location = Literal.parseLiteral("get_location");
	public static final Literal error = Literal.parseLiteral("error");
	public static final Literal fix = Literal.parseLiteral("fix");

	Random random = new Random(System.currentTimeMillis());
	private static final double ERROR_PROBABILITY = 0.01;

	public VacuumCleanerModel(OfficeModel model, int GSize) {
		this.id = 1;
		this.model = model;
		this.isBroken = false;
		this.requestedLocation = false;
		this.xDirection = DIRECTION.RIGHT;
		this.GSize = GSize;
		initializePositions(GSize);
	}
	/* Adds the new percepts
	 * We update our java level location based on the environment
	 * The percepts are:
	 * 		at_room - the current room
	 * 		at_room_start - the start of the current room
	 * 		at_room_end - the end of the current room
	 * 		slot_has_garbage - if the current location has garbage
	 * 		people_in_current_room - if there are people in the current room
	 * 		error - if the vacuum cleaner is broken
	 * 		location - the location of the vacuum cleaner
	 */
	public ArrayList<Percept> newPercepts() {
		ArrayList<Percept> percepts = new ArrayList<Percept>();

		Location vc = model.getAgPos(this.id);
		updateCurrentRoom();
		percepts.add(new Percept(Literal.parseLiteral("at_room(" + this.currentRoom.ordinal() + ")")));

		if (this.currentRoom != OfficeModel.ROOM.DOORWAY) {
			if (model.getRoomStartPos(this.currentRoom).x == vc.x
					&& model.getRoomStartPos(this.currentRoom).y == vc.y) {
				percepts.add(new Percept(Literal.parseLiteral("at_room_start(" + this.currentRoom.ordinal() + ")")));
			}
			if (model.getRoomEndPos(this.currentRoom).x == vc.x && model.getRoomEndPos(this.currentRoom).y == vc.y) {
				percepts.add(new Percept(Literal.parseLiteral("at_room_end(" + this.currentRoom.ordinal() + ")")));
			}
		}
		if (model.hasGarbage(vc.x, vc.y)) {
			percepts.add(new Percept(slot_has_garbage));
		}
		if (!model.roomIsEmpty(this.currentRoom)) {
			percepts.add(new Percept(people_in_current_room));
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
	/* Removes the old percepts, most of the logic is based on states changing, thus percepts need to be removed */
	public ArrayList<Percept> perceptsToRemove() {
		ArrayList<Percept> percepts = new ArrayList<Percept>();

		Location vc = model.getAgPos(this.id);
		if (this.oldRoom != this.currentRoom) {
			percepts.add(new Percept(Literal.parseLiteral("at_room(" + this.oldRoom.ordinal() + ")")));
			percepts.add(new Percept(Literal.parseLiteral("at_room_start(" + this.oldRoom.ordinal() + ")")));
			percepts.add(new Percept(Literal.parseLiteral("at_room_end(" + this.oldRoom.ordinal() + ")")));
		}
		if (this.currentRoom != OfficeModel.ROOM.DOORWAY) {
			if (model.getRoomStartPos(this.currentRoom).x != vc.x
					&& model.getRoomStartPos(this.currentRoom).y != vc.y) {
				percepts.add(new Percept(Literal.parseLiteral("at_room_start(" + this.currentRoom.ordinal() + ")")));
			}
			if (model.getRoomEndPos(this.currentRoom).x != vc.x && model.getRoomEndPos(this.currentRoom).y != vc.y) {
				percepts.add(new Percept(Literal.parseLiteral("at_room_end(" + this.currentRoom.ordinal() + ")")));
			}
		}
		if (!model.hasGarbage(vc.x, vc.y)) {
			percepts.add(new Percept(slot_has_garbage));
		}
		if (model.roomIsEmpty(this.currentRoom)) {
			percepts.add(new Percept(people_in_current_room));
		}

		if (!this.isBroken) {
			percepts.add(new Percept(error));
		}
		return percepts;
	}
	/* Executes the action of the agent
	 * @param action - the action to be executed
	 * The actions are:
	 * next_slot - cleans the current room
	 * go_to - moves to a specific room
	 * go_to_start - moves to the start of a specific room
	 * pick_garbage - picks garbage from the current location
	 * get_location - requests the location of the vacuum cleaner
	 * fix - fixes the vacuum cleaner
	 * check_room_empty - checks if the room is empty
	 */
	public void executeAction(Structure action) {
		try {
			if (action.equals(next_slot)) {
				cleanCurrentRoom();
			} else if (action.getFunctor().equals("go_to")) {
				int x = ((int) ((NumberTerm) action.getTerm(0)).solve());
				OfficeModel.ROOM dest = OfficeModel.ROOM.values()[x];
				if (this.currentRoom == OfficeModel.ROOM.DOORWAY) {
					moveTowards(model.getRoomStartPos(dest));
				} else {
					moveTowards(model.getDoorwayPos(this.currentRoom, dest));
				}
			} else if (action.getFunctor().equals("go_to_start")) {
				int x = ((int) ((NumberTerm) action.getTerm(0)).solve());
				moveTowards(model.getRoomStartPos(OfficeModel.ROOM.values()[x]));
			} else if (action.equals(pick_garbage)) {
				pickGarbage();
			} else if (action.equals(get_location)) {
				get_location();
			} else if (action.equals(fix)) {
				fix();
			} else if (action.equals(check_room_empty)) {
				// this is effectively an active wait so the percepts of the agent get updated
			} else {
				System.out.println("Action not implemented: " + action);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/* Initializes the vacuum cleaner position and rooms*/
	private void initializePositions(int GSize) {
		model.setAgPos(this.id, 0, 0);
		updateCurrentRoom();
		this.oldRoom = this.currentRoom;
	}
	/* Internal function to update location and reset the Y offset */
	private void updateCurrentRoom() {
		OfficeModel.ROOM newRoom = model.whichRoom(model.getAgPos(this.id).x, model.getAgPos(this.id).y);
		if (this.currentRoom != newRoom) {
			this.oldRoom = this.currentRoom;
		}
		if (this.currentRoom == OfficeModel.ROOM.DOORWAY) {
			yOffset = 0;
		}
		this.currentRoom = newRoom;
	}
	/* Picks garbage from the current location
	 * Sometimes breaks the vacuum cleaner
	 */
	public void pickGarbage() {
		Location vc = model.getAgPos(this.id);
		model.removeGarbage(vc.x, vc.y);
		// sometimes error when picking garbage
		if (ERROR_PROBABILITY > random.nextDouble()) {
			this.isBroken = true;
		}
	}
	/* Requests the location of the vacuum cleaner */
	public void get_location() {
		this.requestedLocation = true;
	}
	/* Fixes java level broken state */
	public void fix() {
		this.isBroken = false;
	}
	/* Avoids obstacles on the map by checking the desired location to move to and shifts
	 * to a location where it is possible to move 
	 * When encountering an obstacle adds a Y offset to the location to move to
	 * to be able to keep the horizontal direction
	 * @param vc - the desired location to move to
	 * @return vc - the new location shifted location
	 */
	public Location avoidObstacle(Location vc) {
		if (this.areHumansFriend) {
			if ((!(model.isFree(vc.x, vc.y)) && !(model.hasGarbage(vc.x, vc.y)))) {
				if (model.isFree(vc.x, vc.y + 1)) {
					this.yOffset += 1;
					vc.y++;
				} else if (model.isFree(vc.x + 1, vc.y))
					vc.x++;
				else if (model.isFree(vc.x, vc.y - 1)) {
					vc.y--;
					this.yOffset -= 1;
				} else if (model.isFree(vc.x - 1, vc.y))
					vc.x--;
				else if (model.isFree(vc.x + 1, vc.y + 1)) {
					vc.x++;
					vc.y++;
					this.yOffset += 1;
				} else if (model.isFree(vc.x - 1, vc.y - 1)) {
					vc.x--;
					vc.y--;
					this.yOffset -= 1;
				} else if (model.isFree(vc.x + 1, vc.y - 1)) {
					vc.x++;
					vc.y--;
					this.yOffset -= 1;
				} else if (model.isFree(vc.x - 1, vc.y + 1)) {
					vc.x--;
					vc.y++;
					this.yOffset += 1;
				} else {
					this.isBroken = true; // break instantly if no way to avoid obstacle
					System.out.println("Error avoiding obstacle, someone might get hurt.");
				}
			}
		}
		return vc;
	}
	/* Moves the vacuum cleaner towards a location */
	public void moveTowards(Location loc) {

		Location vc = model.getAgPos(this.id);
		Location next = vc;

		int dx = Math.abs(loc.x - vc.x);
		int dy = Math.abs(loc.y - vc.y);

		if (dx >= dy) {
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
		next = avoidObstacle(next);
		model.setAgPos(this.id, next);
	}
	/* Cleans the current room by going to the right until it's not possible anymore
	 * then switches direction and goes to the left until it's not possible anymore
	 * then calls avoidObstacle to not go through objects on the map
	 */
	public void cleanCurrentRoom() {
		Location vc = model.getAgPos(this.id);
		if (yOffset != 0) {
			switchXDirection();
			this.yOffset = 0;
		}
		if (this.xDirection == DIRECTION.RIGHT) {
			vc.x++;
			if (this.GSize <= vc.x || model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))
					|| (model.whichRoom(vc.x, vc.y) != this.currentRoom)) {
				this.xDirection = DIRECTION.LEFT;
				vc.x--;
				vc.y++;
			}
		} else if (this.xDirection == DIRECTION.LEFT) {
			vc.x--;
			if (0 > vc.x || model.isWall(vc.x, vc.y) || !(model.inGrid(vc.x, vc.y))
					|| (model.whichRoom(vc.x, vc.y) != this.currentRoom)) {
				this.xDirection = DIRECTION.RIGHT;
				vc.x++;
				vc.y++;
			}
		}
		vc = avoidObstacle(vc);
		model.setAgPos(this.id, vc);
	}
	/* Switches Y direction */
	private void switchXDirection() {
		if (this.xDirection == DIRECTION.RIGHT) {
			this.xDirection = DIRECTION.LEFT;
		} else {
			this.xDirection = DIRECTION.RIGHT;
		}
	}
}
