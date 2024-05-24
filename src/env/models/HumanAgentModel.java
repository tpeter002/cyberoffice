package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import env.OfficeEnv.OfficeModel;
import env.OfficeEnv;

import env.OfficeEnv.OfficeModel.ROOM;
import jason.environment.grid.Location;

import env.Percept;


import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;

// Human agent environment class
public class HumanAgentModel {

    public static final Term load = Literal.parseLiteral("load");
    public static final Term loadpos = Literal.parseLiteral("loadpos");
    public static final Term garbagedrop = Literal.parseLiteral("garbagedrop");

    private OfficeModel model;
    private int id;
    private ArrayList<Human> agents;

    private ArrayList<String[]> routines; 
    private HashMap<String, Integer> load_counters;
    private int n_human_agents;

    static Logger hlogger = Logger.getLogger(HumanAgentModel.class.getName());

    private int GSize;

    

    ArrayList<Percept> percepts_pre = new ArrayList<Percept>();
    ArrayList<Percept> percepts_to_remove = new ArrayList<Percept>();
    ArrayList<Percept> percepts_to_remove_by_unif = new ArrayList<Percept>();

    

    Random random = new Random(System.currentTimeMillis());

    public HumanAgentModel(OfficeModel model, int GSize) {
        agents = new ArrayList<Human>();

        n_human_agents = ((OfficeModel) model).n_human_agents;

        this.id = 2;
        this.GSize = GSize;

        this.model = model;
        routines = readRoutineFromFile("routine.txt");
        initializePositions(GSize);
        load_counters = new HashMap<String, Integer>();

        for (int i = 1; i <= n_human_agents; i++) {
            String hname = "h" + Integer.toString(i);
            load_counters.put(hname, 1);

        }

    }

    public void initializePositions(int GSize) {
        // Initialize the positions

        int h_id = n_human_agents + this.id;

        for (int i = 2; i < h_id; i++) {

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
        for (Human human : agents) {
            if (human.isOnCell(x, y)) {
                return true;
            }
        }
        return false;
    }

    private class Human {
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


    public ArrayList<Percept> newPercepts() {
        ArrayList<Percept> percepts = new ArrayList<Percept>();
        percepts.addAll(percepts_pre);
        percepts_pre.clear();
        return percepts;
    }

    public ArrayList<Percept> perceptsToRemove() {
        ArrayList<Percept> percepts = new ArrayList<Percept>();
        percepts.addAll(percepts_to_remove);
        percepts_to_remove.clear();
        return percepts;
    }

    public ArrayList<Percept> getPerceptsToRemoveByUnif(){
        ArrayList<Percept> percepts = new ArrayList<Percept>();
        percepts.addAll(percepts_to_remove_by_unif);
        percepts_to_remove_by_unif.clear();
        return percepts;
    }

    public Literal getNextRoutineElement(String agentName) {
        int load_counter = load_counters.get(agentName);
        Literal result = null;


        for (String[] agentRoutine : routines) {
            if (agentRoutine.length > load_counter && agentRoutine[0].equals(agentName)) {
                String element = agentRoutine[load_counter];
                result = Literal.parseLiteral(element);

            }
        }
    
        load_counters.put(agentName, load_counter + 1);
        return result;
    }


    public Literal getReminder(String humanName){
        
        int load_counter = load_counters.get(humanName) - 1;
        if(load_counter>0){

        for (String[] agentRoutine : routines) {
            if (agentRoutine.length > load_counter && agentRoutine[0].equals(humanName)) {
                String element = agentRoutine[load_counter];
                return Literal.parseLiteral(element);
            }
        }
    }
        return null;
    }



    public Literal getPosLiteral(String agentName) {
        int id = getID(agentName);
        Location hLoc = model.getAgPos(id);
        Literal result = Literal.parseLiteral("pos(" + hLoc.x + "," + hLoc.y + ")");
        return result;
    }

    


    public void executeAction(String agentName, Structure action) {
        try {
            if(action.equals(load)){
                Literal older_element=getReminder(agentName);
                Literal routine_element=getNextRoutineElement(agentName);
                if (older_element!= null){
                    Percept removed=new Percept(agentName, older_element);
                    percepts_to_remove.add(removed);
                }
                Percept new_element=new Percept(agentName, routine_element); 
                percepts_pre.add(new_element);
            }
            else if(action.equals(loadpos)){
                loadPosition(agentName);
                
            }
            else if (action.getFunctor().equals("moveto")) {
                moveto(agentName, action);
            } 
            else if(action.equals(garbagedrop)){
                dropGarbage(agentName);
            } else if(action.getFunctor().equals("clear")){
                addToRemovePercept(agentName, action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPosition(String agentName){
        int id = getID(agentName);
        Location hLoc = model.getAgPos(id);
        Literal hpos=getPosLiteral(agentName);
        Percept new_loc=new Percept(agentName, hpos);
        Percept old_adj=new Percept(agentName, Literal.parseLiteral("adjacent(_,_)[source(_)]"));
        Percept old_pos=new Percept(agentName, Literal.parseLiteral("pos(_,_)[source(_)]"));
        percepts_to_remove_by_unif.add(old_adj);
        percepts_to_remove_by_unif.add(old_pos);
        percepts_pre.add(new_loc);
        int adjX=hLoc.x;
        int adjY=hLoc.y;
        if(hLoc.x+1<GSize){
            adjX=hLoc.x+1;
            adjY=hLoc.y;
            Percept new_adj=new Percept(agentName, Literal.parseLiteral( "adjacent(" + adjX + "," + adjY + ")" ));
            percepts_pre.add(new_adj);
        }
        if(hLoc.x-1>=0){
            adjX=hLoc.x-1;
            adjY=hLoc.y;
            Percept new_adj=new Percept(agentName, Literal.parseLiteral( "adjacent(" + adjX + "," + adjY + ")" ));
            percepts_pre.add(new_adj);
        }
        if(hLoc.y+1<GSize){
            adjX=hLoc.x;
            adjY=hLoc.y+1;
            Percept new_adj=new Percept(agentName, Literal.parseLiteral( "adjacent(" + adjX + "," + adjY + ")" ));
            percepts_pre.add(new_adj);
        }
        if(hLoc.y-1>=0){
            adjX=hLoc.x;
            adjY=hLoc.y-1;
            Percept new_adj=new Percept(agentName, Literal.parseLiteral( "adjacent(" + adjX + "," + adjY + ")" ));
            percepts_pre.add(new_adj);
        }
        
    }
    public void addToRemovePercept(String agentName, Structure action){
        String perceptToClear = action.getTerm(0).toString();
        Literal perceptToClearLit=Literal.parseLiteral(perceptToClear);
        Percept removed=new Percept(agentName, perceptToClearLit);
        percepts_to_remove_by_unif.add(removed);
    }

    public void dropGarbage(String agentName){
        Location spot=model.getAgPos(getID(agentName));
        if(model.whichRoom(spot.x, spot.y)!=ROOM.DOORWAY)
            model.addGarbage(spot.x, spot.y);
    }

    public int getID(String agentName) {
        int value = Integer.parseInt(agentName.replaceAll("[^0-9]", "")) + 1;
        return value;
    }

    public Human getHumanByID(int id) {
        for (Human h : agents) {
            if (h.id == id) {
                return h;
            }
        }
        return null;
    }

    public void updateLoc(int id, Location loc) {
        model.setAgPos(id, loc);
        for (Human h : agents) {
            if (h.id == id) {
                h.x = loc.x;
                h.y = loc.y;
            }
        }
    }

    public boolean canStep(int x, int y){
        Location vacuumpos=model.getAgPos(1);
        if(vacuumpos.x==x && vacuumpos.y==y)
            return false;

        if(model.isFree(x, y)){
            return true;
        }
        else if(model.hasGarbage(x, y) && !cellOccupied(x, y)){
            return true;
        }

        else{
            return false;
        }

    }

    public void moveto(String agentName, Structure action) throws Exception {
        int agentid = getID(agentName);
        Location loc = model.getAgPos(agentid); 
        int x = (int) ((NumberTerm) action.getTerm(0)).solve(); 
        int y = (int) ((NumberTerm) action.getTerm(1)).solve(); 
        int newX = loc.x; 
        int newY = loc.y;

        ROOM targetRoom = model.whichRoom(x, y);
        ROOM currentRoom = model.whichRoom(loc.x, loc.y);

        if (targetRoom != currentRoom && targetRoom!=ROOM.DOORWAY && currentRoom!=ROOM.DOORWAY) {
            Location doorway=model.getDoorwayPos(currentRoom, targetRoom);
            x=doorway.x;
            y=doorway.y;


        }
        boolean xChanged=false;
        boolean yChanged=false;

        if (loc.x < x) {
            newX = loc.x + 1; 
            xChanged=true;
        }
        else if (loc.x > x){
            newX = loc.x - 1;
            xChanged=true;
        }
        if (loc.y < y){
            newY = loc.y + 1; 
            yChanged=true;
        }
        else if (loc.y > y){
            newY = loc.y - 1;
            yChanged=true;
        }
        //egyenesen oda
        if (canStep(newX, newY)) {
            loc.x = newX;
            loc.y = newY; 
        } else if (xChanged && canStep(newX, loc.y)) {
            loc.x = newX;
        } else if (yChanged && canStep(loc.x, newY)) {
            loc.y = newY;      
        } else if (xChanged && canStep(newX, loc.y+1)) {
            loc.x = newX;
            loc.y=loc.y+1;
        }
        else if (xChanged && canStep(newX, loc.y-1)) {
            loc.x = newX;
            loc.y=loc.y-1;
        }
        else if (yChanged && canStep(loc.x+1, newY)) {
            loc.x = loc.x+1;
            loc.y=newY;  
        }
        else if (yChanged && canStep(loc.x-1, newY)) {
            loc.x = loc.x-1;
            loc.y=newY;
        }
        updateLoc(agentid, loc);
        

    } 


    private static ArrayList<String[]> readRoutineFromFile(String filename) {
        ArrayList<String[]> routine = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            ArrayList<String> currentRoutine = new ArrayList<>();
            while ((line = br.readLine()) != null) {

                if (!line.trim().isEmpty()) {

                    if (line.trim().equals(";")) {

                        if (!currentRoutine.isEmpty()) {
                            routine.add(currentRoutine.toArray(new String[0]));
                            currentRoutine.clear();

                        }
                    } else {
                        currentRoutine.add(line.trim());
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String[] routine_per_agent : routine) {
            hlogger.info(routine_per_agent[0] + " agent's routine: ");
            for (int i = 1; i < routine_per_agent.length; i++) {
                hlogger.info(routine_per_agent[i]);
            }
        }

        return routine;
    }

    public ArrayList<String[]> getRoutines() {
        return routines;

    }
}