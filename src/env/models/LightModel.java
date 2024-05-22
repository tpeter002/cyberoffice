package models;

import jason.asSyntax.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import env.OfficeEnv;
import env.OfficeEnv.OfficeModel;
import env.Percept;

public class LightModel {

    private OfficeModel model;
    private ArrayList<Light> lights = new ArrayList<>();
    private Map<OfficeModel.ROOM, Boolean> roomStates = new HashMap<>();

    public LightModel(OfficeModel model, int GSize) {
        this.model = model;
        lights.add(new Light(OfficeModel.ROOM.VACUUM));
        lights.add(new Light(OfficeModel.ROOM.HALL));
        lights.add(new Light(OfficeModel.ROOM.PRINTER));

        for (OfficeModel.ROOM room : OfficeModel.ROOM.values()) {
            roomStates.put(room, true);
        }

        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            light.set_id(i);
            if (i == 0) {
                light.setLocation(0, 1);
            } else if (i == 1) {
                light.setLocation(0, GSize - 1);
            } else if (i == 2) {
                light.setLocation(GSize - 1, 0);
            }
        }
    }

    private void pollRoomStates() {
        for (OfficeModel.ROOM room : OfficeModel.ROOM.values()) {
            boolean currentState = model.roomIsEmpty(room);
            boolean previousState = roomStates.get(room);
            // System.out.println("Room: " + room + " Current: " + currentState + "
            // Previous: " + previousState);

            if (!currentState && previousState) {
                for (Light light : lights) {
                    if (light.getRoom() == room) {
                        System.out.println(
                                "pooling room states:" + light.getRoom()
                                        + "Person detected in room: ---------------------------------- turnOn() called");
                        light.turnOn();

                    }
                }
            } else if (currentState && !previousState) {
                for (Light light : lights) {
                    if (light.getRoom() == room) {
                        light.turnOff();
                        System.out.println(
                                "pooling room states:" + light.getRoom()
                                        + " Person left the Room:---------------------------------- turnOff() called");
                    }
                }
            }

            roomStates.put(room, currentState);
        }
    }

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
        System.out
                .println("executeAction:" + action + "  " + agentName + " in room: " + light.getRoom());
        light.executeAction(action);
    }

    public Light getLight(int i) {
        return lights.get(i);
    }

    public ArrayList<Percept> newPercepts() {
        pollRoomStates();
        ArrayList<Percept> percepts = new ArrayList<>();
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            percepts.addAll(light.newPercept());
        }
        return percepts;
    }

    public ArrayList<Percept> perceptsToRemove() {
        // pollRoomStates();
        ArrayList<Percept> percepts = new ArrayList<>();
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            percepts.addAll(light.perceptsToRemove());
        }
        return percepts;
    }

    private class Light {
        int _id;
        int x;
        int y;
        OfficeModel.ROOM room;
        boolean isOn;
        int untilBreakDown;
        private boolean isBroken;
        private ArrayList<Percept> newPercepts = new ArrayList<>();
        private ArrayList<Percept> removePercepts = new ArrayList<>();
        private boolean isPersonInRoom = false;

        public static final Term turnOn = Literal.parseLiteral("turn_light_on");
        public static final Term turnOff = Literal.parseLiteral("turn_light_off");
        public static final Term operate = Literal.parseLiteral("operate");
        public static final Term repair = Literal.parseLiteral("repair_light");
        public static final Term broken = Literal.parseLiteral("light_broken");
        public static final Term getLocation = Literal.parseLiteral("get_location");

        public Light(OfficeModel.ROOM room) {
            this.room = room;
            this.isOn = false;
            this.untilBreakDown = 10;
            this.isBroken = false;
            this.newPercepts = new ArrayList<>();
            this.removePercepts = new ArrayList<>();
        }

        public void turnOn() {
            this.untilBreakDown--;
            if (this.untilBreakDown <= 0) {
                System.out.println("------------------------------------------Until Breakdown: " + this.untilBreakDown);
                this.isBroken = true;
                this.isOn = false;
                newPercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_broken")));
                removePercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_on")));
            } else {
                this.isOn = true;
                System.out.println("Light in room " + this.room + " is on");
                newPercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_on")));
                removePercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_off")));
            }
        }

        public void turnOff() {
            this.isOn = false;
            System.out.println("Light in room " + this.room + " is off");
            newPercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_off")));
            removePercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_on")));
        }

        public void repair() {
            this.isBroken = false;
            this.untilBreakDown = 10;
            System.out.println("Light in room " + this.room + " is repaired");
            newPercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_repaired")));
            removePercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_broken")));
        }

        public ArrayList<Percept> newPercept() {
            return newPercepts;
        }

        public ArrayList<Percept> perceptsToRemove() {
            return removePercepts;
        }

        public void executeAction(Structure action) {
            System.out.println("[l" + this._id + "] Light in room " + this.room + " is On: " + this.isOn);
            if (isBroken) {
                System.out.println("Light in room " + this.room + " is broken");
                return;
            }
            if (action.equals(turnOn)) {
                this.turnOn();
            } else if (action.equals(turnOff)) {
                this.turnOff();
            } else if (action.equals(repair)) {
                this.repair();
            } else if (action.equals(getLocation)) {
                newPercepts
                        .add(new Percept("l" + _id, Literal.parseLiteral("location(" + this.x + "," + this.y + ")")));
            } // else if (action.equals(operate)) {
              // pollRoomStates();
              // }
        }

        // public void pollRoomStates() {
        // System.out.println("Polling room states");
        // boolean currentState = model.roomIsEmpty(room);
        // if (isPersonInRoom && !currentState) {
        // turnOn();
        // } else if (!isPersonInRoom && currentState) {
        // turnOff();
        // }
        // isPersonInRoom = currentState;
        // }

        public void set_id(int id) {
            this._id = id;
        }

        public void setLocation(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public OfficeModel.ROOM getRoom() {
            return room;
        }

        public int getId() {
            return _id;
        }
    }
}
