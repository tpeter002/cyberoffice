package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;


import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;


// Human agent environment class
public class HumanAgentModel  {
    
    public static final Term load = Literal.parseLiteral("loadroutine");
    private OfficeModel model;
    private ArrayList<Human> agents;

    private ArrayList<String[]> routines;
    private HashMap<String, Integer> load_counters;

    static Logger hlogger = Logger.getLogger(HumanAgentModel.class.getName());
    

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


        public Human(int id, int x, int y) {
            this.x = x;
            this.y = y;
            this.id = id;
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
        if(action.equals(load)) {
       }   
       else{
            hlogger.info("executeActionfail");
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