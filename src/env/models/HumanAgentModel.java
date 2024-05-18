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

    public static final Term load = Literal.parseLiteral("loadroutine");

    private OfficeModel model;
    private int id;
    private ArrayList<Human> agents;

    private ArrayList<String[]> routines; 
    private HashMap<String, Integer> load_counters;
    private int n_human_agents;
    private Location vacuum_hall_doorway;
    private Location printer_hall_doorway;

    static Logger hlogger = Logger.getLogger(HumanAgentModel.class.getName());

    private int GSize;

    

    ArrayList<Percept> percepts_pre = new ArrayList<Percept>();

    

    Random random = new Random(System.currentTimeMillis());

    public HumanAgentModel(OfficeModel model, int GSize) {
        agents = new ArrayList<Human>();

        n_human_agents = ((OfficeModel) model).n_human_agents;

        this.id = 2;

        this.model = model;
        routines = readRoutineFromFile("routine.txt");
        initializePositions(GSize);
        load_counters = new HashMap<String, Integer>();

        for (int i = 1; i <= n_human_agents; i++) {
            String hname = "h" + Integer.toString(i);
            load_counters.put(hname, 0);

        }
        this.GSize = GSize;

        vacuum_hall_doorway = new Location(4, 5);

        printer_hall_doorway = new Location(16, 5);
    }

    public void initializePositions(int GSize) {
        // Initialize the positions

        int h_id = n_human_agents + 2;

        for (int i = 2; i < h_id; i++) {

            int x = random.nextInt(GSize);
            int y = random.nextInt(GSize);
            while (model.hasObject(1, x, y) || model.hasObject(0, x, y) || model.hasObject(OfficeEnv.WALL, x, y)) {
                x = random.nextInt(GSize);
                y = random.nextInt(GSize);
            }
            model.setAgPos(i, x, y);
            hlogger.info(Integer.toString(i));

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


    public ArrayList<Percept> getNewPercepts() {
        ArrayList<Percept> percepts = new ArrayList<Percept>();

        percepts.addAll(percepts_pre);
        percepts_pre.clear();

        // for (Human human: agents) {
        // percepts.add("human(" + human.id + "" + human.x + "," + human.y + ")");
        // }

        return percepts;
    }

    public Literal getNextRoutineElement(String agentName) {
        int load_counter = load_counters.get(agentName);
        Literal result = null;

        if (load_counter == 0) {
            result = getPosLiteral(agentName);
        } else {
            for (String[] agentRoutine : routines) {
                if (agentRoutine.length > load_counter && agentRoutine[0].equals(agentName)) {
                    String element = agentRoutine[load_counter];
                    result = Literal.parseLiteral(element);
                }


            }
        }

        load_counters.put(agentName, load_counter + 1);
        return result;
    }

    //ez igazabol getnextroutine element csak loadcounter inkrementalas nelkul(meg mas agentname megszerzes ugye), nem tom meglehetne e oldani hogy ez nalad legyen kicsit szivas lenne sztem, max officeenvbe is lehetne load counter i guess es akk te is elerned
    public Literal getReminder(String humanName, Structure action){
        hlogger.info(humanName);
        int load_counter = load_counters.get(humanName);

        for (String[] agentRoutine : routines) {
            if (agentRoutine.length > load_counter && agentRoutine[0].equals(humanName)) {
                String element = agentRoutine[load_counter];
                return Literal.parseLiteral(element);

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
            if (action.getFunctor().equals("moveto")) {
                moveto(agentName, action);
            } else if(action.getFunctor().equals("garbagedrop")){
                dropGarbage(agentName);

            } 

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dropGarbage(String agentName){
        Location spot=model.getAgPos(getID(agentName));
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
        hlogger.info("getbyidfail");
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
        //Human agent = getHumanByID(agentid);

        Location loc = model.getAgPos(agentid); // 4, 5
        int x = (int) ((NumberTerm) action.getTerm(0)).solve(); //19
        int y = (int) ((NumberTerm) action.getTerm(1)).solve(); //0
        int newX = loc.x; 

        int newY = loc.y;
        ROOM targetRoom = model.whichRoom(x, y);
        ROOM currentRoom = model.whichRoom(loc.x, loc.y);


        
        if (targetRoom != currentRoom) {

            if ((targetRoom == ROOM.VACUUM && currentRoom == ROOM.HALL)
                    || (targetRoom == ROOM.HALL && currentRoom == ROOM.VACUUM)
                    || (targetRoom == ROOM.PRINTER && currentRoom == ROOM.VACUUM)) {
                x = vacuum_hall_doorway.x;
                y = vacuum_hall_doorway.y;
            } else if ((targetRoom == ROOM.VACUUM && currentRoom == ROOM.PRINTER)
                    || (targetRoom == ROOM.PRINTER && currentRoom == ROOM.HALL)
                    || (targetRoom == ROOM.HALL && currentRoom == ROOM.PRINTER)) {
                x = printer_hall_doorway.x;
                y = printer_hall_doorway.y;

            }else if((currentRoom==ROOM.DOORWAY && targetRoom==ROOM.HALL)){
                x=loc.x;
                y=loc.y+1;
            }
            //doorwayen at csak egyenesen lehet menni pont mint a valosagban!
            else if((currentRoom==ROOM.DOORWAY && (targetRoom==ROOM.VACUUM || targetRoom==ROOM.PRINTER))){
                x=loc.x;
                y=loc.y-1;
            }

        }

        if (loc.x < x) 
            newX = loc.x + 1; //5

        else if (loc.x > x)
            newX = loc.x - 1;
        if (loc.y < y)
            newY = loc.y + 1;
        else if (loc.y > y)
            newY = loc.y - 1;

        if (canStep(newX, newY)) {
            loc.x = newX;
            loc.y = newY;
            updateLoc(agentid, loc);
        } else if (canStep(newX, loc.y)) {
            loc.x = newX;
            updateLoc(agentid, loc);
        } else if (canStep(loc.x, newY)) {
            loc.y = newY;
            updateLoc(agentid, loc);
        } else if (canStep(newX, loc.y+1)) {
            loc.x = newX;
            loc.y=loc.y+1;
            updateLoc(agentid, loc);
        }
        else if (canStep(newX, loc.y-1)) {
            loc.x = newX;
            loc.y=loc.y-1;
            updateLoc(agentid, loc);
        }
        else if (canStep(loc.x+1, newY)) {
            loc.x = loc.x+1;
            loc.y=newY;
            updateLoc(agentid, loc);
        }
        else if (canStep(loc.x-1, newY)) {
            loc.x = loc.x-1;
            loc.y=newY;
            updateLoc(agentid, loc);
        }
        

    } 


       

  /*
    public void executeAction(String agentName, Structure action){

        if(action.equals(load)){
            Literal routine_element=getNextRoutineElement(agentName);
            percepts_pre.add(new Percept(agentName, routine_element));
        }
        */



   

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