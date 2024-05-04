package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;

// Human agent environment class
public class HumanAgentModel  {
    
    private OfficeModel model;
    private int[] positions;
    Random random = new Random(System.currentTimeMillis());

    public HumanAgentModel(OfficeModel model, int GSize){
        positions = new int[12];
        this.model = model;
        initializePositions(GSize);
        // Initialize the positions
    }

    public void initializePositions(int GSize){
        // Initialize the positions
                        // add human agents
        for (int i = 2; i < ((OfficeModel)model).n_human_agents; i++) {
            int x = random.nextInt(GSize);
            int y = random.nextInt(GSize);
            while (model.hasObject(1, x, y) || model.hasObject(0, x, y) || model.hasObject(OfficeEnv.WALL, x, y)) {
                x = random.nextInt(GSize);
                y = random.nextInt(GSize);
            }
            model.setAgPos(i, x, y);
        }
    }
}