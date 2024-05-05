package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;

import java.util.ArrayList;

// Human agent environment class
public class PrinterModel  {
    
    private OfficeModel model;
    private int position;
    Random random = new Random(System.currentTimeMillis());

    public PrinterModel(OfficeModel model, int GSize){
        position = 0;
        this.model = model;
        initializePositions(GSize);
        // Initialize the positions
    }

    public void initializePositions(int GSize){
        // Initialize the positions
        model.setAgPos(0, GSize-1, 0);
    }

    public ArrayList<Literal> getPercepts() {
        ArrayList<Literal> percepts = new ArrayList<Literal>(); 
        return percepts;
    }

    public void executeAction(Structure action){
        
    }

    
}