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

    public LightModel(OfficeModel model, int GSize) {
        this.model = model;
        lights.add(new Light(OfficeModel.ROOM.VACUUM));
        lights.add(new Light(OfficeModel.ROOM.HALL));
        lights.add(new Light(OfficeModel.ROOM.PRINTER));

        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            light.set_id((i + 1));
            if (i == 0) {
                // printer room
                light.setLocation(2, 2);
                light.setUntilBreakDown(1);
            } else if (i == 1) {
                // hall
                light.setLocation(10, 10);
            } else if (i == 2) {
                // vacuum room
                light.setLocation(2, 12);
            }
            System.out.println("[l" + light._id + "] Location: " + light.x + ", " + light.y + " Room: " + light.room);
        }
    }

    private void pollRoomStates() {
        for (Light light : lights) {
            light.poolRoomState();
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
        // System.out.println("executeAction:" + action + " " + agentName + " in room: "
        // + light.getRoom());
        light.executeAction(action);
    }

    public Light getLight(int i) {
        return lights.get(i);
    }

    public ArrayList<Percept> newPercepts() {
        ArrayList<Percept> percepts = new ArrayList<>();
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            for (Percept p : light.newPercept()) {
                percepts.add(new Percept("l" + light._id, p.message));
            }
            light.newPercepts.clear();
        }
        return percepts;
    }

    public ArrayList<Percept> perceptsToRemove() {
        ArrayList<Percept> percepts = new ArrayList<>();
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            for (Percept p : light.perceptsToRemove()) {
                percepts.add(new Percept("l" + light._id, p.message));
            }
            light.removePercepts.clear();
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
        private boolean prevoiusState;

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
            this.prevoiusState = true;
            this.newPercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_off")));
        }

        public void poolRoomState() {
            boolean currentState = model.roomIsEmpty(room);
            if (!currentState && prevoiusState) {
                turnOn();
            } else if (currentState && !prevoiusState) {
                turnOff();
            }
            prevoiusState = currentState;
        }

        public void turnOn() {
            this.untilBreakDown--;
            if (this.untilBreakDown <= 0) {
                this.isBroken = true;
                this.isOn = false;
                newPercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_broken")));
                removePercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_off")));
                System.out.println("[l" + this._id + "] " + "pooling room states:" + this.room
                        + "Person detected in room: ---------------------------------- turnOn() called but failed, light is broken");
            } else {
                System.out.println("[l" + this._id + "] " + "pooling room states:" + this.room
                        + "Person detected in room: ---------------------------------- turnOn() called");
                this.isOn = true;
                newPercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_on")));
                removePercepts.add(new Percept("l" + _id, Literal.parseLiteral("light_off")));
            }
        }

        public void turnOff() {
            System.out.println("[l" + this._id + "] " + "pooling room states:" + this.room
                    + " Person left the Room:---------------------------------- turnOff() called");
            this.isOn = false;
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
            System.out.println("áááááááááááááá" + action);

            if (action.equals(repair)) {
                this.repair();
            } else if (action.equals(getLocation)) {
                newPercepts.add(new Percept("l" + this._id,
                        Literal.parseLiteral("location(" + this.x + ", " + this.y + ")")));
            } else if (action.equals(operate)) {
                this.poolRoomState();
            }
        }

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

        public void setUntilBreakDown(int untilBreakDown) {
            this.untilBreakDown = untilBreakDown;
        }
    }
}
