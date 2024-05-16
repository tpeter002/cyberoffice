package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;

import java.util.ArrayList;

// Human agent environment class
public class MainframeModel  {
	
	private OfficeModel model;
	Random random = new Random(System.currentTimeMillis());

	ArrayList<Literal> percepts = new ArrayList<>();

	public MainframeModel(OfficeModel model, int GSize){
		this.model = model;
	}

	public ArrayList<Literal> getPercepts() {
		return percepts;
	}
}