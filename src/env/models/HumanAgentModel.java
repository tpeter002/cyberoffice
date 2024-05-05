package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;

import java.util.ArrayList;

// Human agent environment class
public class HumanAgentModel  {
    
    private OfficeModel model;
    private ArrayList<Human> agents;
    Random random = new Random(System.currentTimeMillis());

    public HumanAgentModel(OfficeModel model, int GSize){
        agents = new ArrayList<Human>();
        this.model = model;
        initializePositions(GSize);
        // Initialize the positions
    }

    public void initializePositions(int GSize){
        // Initialize the positions
    
        for (int i = 2; i < ((OfficeModel)model).n_human_agents; i++) {
            int x = random.nextInt(GSize);
            int y = random.nextInt(GSize);
            while (model.hasObject(1, x, y) || model.hasObject(0, x, y) || model.hasObject(OfficeEnv.WALL, x, y)) {
                x = random.nextInt(GSize);
                y = random.nextInt(GSize);
            }
            model.setAgPos(i, x, y);

            agents.add(new Human(i, x, y));

        }
    }

    public boolean cellOccupied(int x, int y) {
        for (Human human :  agents) {
            if (human.isOnCell(x, y)) {
                return true;
            }
        }
        return false;
        
    }

    private class Human{
        int x;
        int y;
        int id;


        public Human(int id, int x, int y) {
            this.x = x;
            this.y = y;
            this.id = id;
        }

        public boolean isOnCell(int x, int y) {
            return this.x == x && this.y == y;
        }
    }

    public ArrayList<String> getPercepts() {
        ArrayList<String> percepts = new ArrayList<String>();
        for (Human human: agents) {
            percepts.add("human(" + human.id + "" + human.x + "," + human.y + ")");
        }
        return percepts;
    }

}