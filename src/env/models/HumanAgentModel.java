package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;
import jason.environment.grid.Location;


import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;


// Human agent environment class
public class HumanAgentModel  {
    
    
    private OfficeModel model;
    private ArrayList<Human> agents;

    private ArrayList<String[]> routines;
    private HashMap<String, Integer> load_counters;

    static Logger hlogger = Logger.getLogger(HumanAgentModel.class.getName());

    private enum DIRECTION {
		RIGHT,
		LEFT,
		UP,
		DOWN,
	}
    

    Random random = new Random(System.currentTimeMillis());

    public HumanAgentModel(OfficeModel model, int GSize){
        agents = new ArrayList<Human>();
        this.model = model;
        routines=readRoutineFromFile("routine.txt");
        initializePositions(GSize);
        load_counters=new HashMap<String, Integer>();
        for (int i=1; i<=10; i++){ //itt n_human agents kene cant be asked
            String hname="h"+Integer.toString(i);
            load_counters.put(hname, 1);
        }
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
        DIRECTION direction;



        public Human(int id, int x, int y) {
            this.x = x;
            this.y = y;
            this.id = id;
            this.direction=RIGHT;
        }

        public boolean isOnCell(int x, int y) {
            return this.x == x && this.y == y;
        }

    }

    public ArrayList<Literal> getPercepts() {
        ArrayList<Literal> percepts = new ArrayList<Literal>();
        //for (Human human: agents) {
        //    percepts.add("human(" + human.id + "" + human.x + "," + human.y + ")");
        //}


        return percepts;
    }

    public Literal getNextRoutineElement(String agentName){
        int load_counter=load_counters.get(agentName);
        Literal result=null;
        for (String[] agentRoutine : routines) {
            if (agentRoutine.length > load_counter && agentRoutine[0].equals(agentName)) {
                    String element = agentRoutine[load_counter];
                    result=Literal.parseLiteral(element);
            }
        }
        
        load_counters.put(agentName, load_counter+1);
        return result;
    }


    public void executeAction(String agentName, Structure action){
        try{
            if(action.getFunctor().equals("moveto")) {
                moveto(agentName, action);
            }   
            else {
                    hlogger.info("executeActionfail " + agentName);
            }
        }catch(Exception e){
            e.printStackTrace();
        }


    }

    public int getID(String agentName){
        int value=Integer.parseInt(agentName.replaceAll("[^0-9]", "")) + 2;
        return value;
    }

    public int getHumanByID(int id){
        for (Human h : model.agents){
            if(h.id==id){
                return h;
            }
        }
    }

    

    public void moveto(String agentName, Structure action) throws Exception{
        int agentid=getID(agentName);
        Human agent=getHumanByID(agentid);
        Location loc=model.getAgPos(agentid);
        hlogger.info("moveba bejut");
        int x = (int)((NumberTerm)action.getTerm(0)).solve();
        int y = (int)((NumberTerm)action.getTerm(1)).solve();
        int newX=loc.x;
        int newY=loc.y;
            if (loc.x < x)
                newX=loc.x+1;
            else if (loc.x > x)
                newX=loc.x-1;
            if (loc.y < y) 
                newY=loc.y+1;
            else if (loc.y > y)
                newY=loc.y-1;

            if(!model.isWall(newX, newY) && !model.cellOccupied(newX, newY)){
                loc.x=newX;
                loc.y=newY;
                model.setAgPos(agentid, loc);
            }
            else if(!model.isWall(newX, loc.y)){
                model.setAgPos(agentid, loc);
            }
    }

    private static ArrayList<String[]> readRoutineFromFile(String filename) {
        ArrayList<String[]> routine = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            ArrayList<String> currentRoutine = new ArrayList<>(); 
            while ((line = br.readLine()) != null) {
                
                if (!line.trim().isEmpty()) { 
                    
                    if(line.trim().equals(";")){ 
                        
                        if (!currentRoutine.isEmpty()) {
                            routine.add(currentRoutine.toArray(new String[0]));
                            currentRoutine.clear();
                    
                        }
                    }
                    else{
                        currentRoutine.add(line.trim());
                    }
                } 
                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String[] routine_per_agent : routine){
            hlogger.info(routine_per_agent[0] + " agent's routine: ");
            for (int i=1; i<routine_per_agent.length; i++){
                hlogger.info(routine_per_agent[i]);
            }
        }

        return routine;
    }

    public ArrayList<String[]> getRoutines(){
        return routines;

    }

}