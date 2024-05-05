package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;

import java.util.ArrayList;

// Human agent environment class
public class VacuumCleanerModel  {
    
    private OfficeModel model;
    private int position;
    Random random = new Random(System.currentTimeMillis());

    public VacuumCleanerModel(OfficeModel model, int GSize){
        position = 0;
        this.model = model;
        initializePositions(GSize);
        // Initialize the positions
    }

    public void executeAction(Structure action){
        //if(action.equals("clean")){
            // Clean the floor
        //    model.removeObject(OfficeEnv.DIRT, model.getAgPos(1).x, model.getAgPos(1).y);
        //}
    }

    public void initializePositions(int GSize){
        // Initialize the positions
        model.setAgPos(1, 0, 0);
    }

    public ArrayList<Literal> getPercepts() {
        ArrayList<Literal> percepts = new ArrayList<Literal>();
        return percepts;
    }
}

