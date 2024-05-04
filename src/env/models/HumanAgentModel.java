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

    void moveTowards(int agentId, int targetX, int targetY) throws Exception {
        int currentX = model.getAgPos(agentId).x;
        int currentY = model.getAgPos(agentId).y;
    
        // Calculate the direction to move towards the target position
        int dx = Integer.compare(targetX, currentX);
        int dy = Integer.compare(targetY, currentY);
    
        // Check if the agent has already reached the target position
        if (dx == 0 && dy == 0) {
            return;
        }
    
        // Calculate the new position after moving towards the target
        int newX = currentX + dx;
        int newY = currentY + dy;
    
        // Check if the new position is within the grid boundaries
        if (newX >= 0 && newX < model.getWidth() && newY >= 0 && newY < model.getHeight()) {
            // Check if the new position is not occupied by a wall or another agent
            if (!model.hasObject(OfficeEnv.WALL, newX, newY) && !model.hasObject(0, newX, newY)) {
                // Move the agent to the new position
                model.setAgPos(agentId, newX, newY);
            }
        }
    }


}