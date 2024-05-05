package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;

import java.util.ArrayList;


public class MainframeModel  {
    
    private OfficeModel model;
    private int position;
    Random random = new Random(System.currentTimeMillis());

    public MainframeModel(OfficeModel model, int GSize){
        position = 0;
        this.model = model;
        initializePositions(GSize);
        // Initialize the positions
    }

    public void initializePositions(int GSize){
    }

    public ArrayList<Literal> getPercepts() {
        ArrayList<Literal> percepts = new ArrayList<Literal>();
        return percepts;
    }

    public void executeAction(Structure action){
        
    }
}