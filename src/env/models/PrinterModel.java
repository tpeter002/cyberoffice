package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import jade.util.Logger;
import env.Percept;
import java.util.Queue;
import java.util.LinkedList;
import java.lang.reflect.Array;
import java.util.ArrayList;

import java.util.ArrayList;

// Human agent environment class
public class PrinterModel {

    private OfficeModel model;
    private int position;
    Random random = new Random(System.currentTimeMillis());
    private boolean printerReady = true;
    private boolean printerWorking = false;
    private boolean printerError = false;
    private Queue<String> printQueue = new LinkedList<>();
    Logger logger = Logger.getMyLogger(getClass().getName());
    boolean requestedLocation = false;
    boolean waserroralready = false;

    public PrinterModel(OfficeModel model, int GSize) {
        position = 0;
        this.model = model;
        initializePositions(GSize);
        // Initialize the positions
    }

    public void initializePositions(int GSize) {
        // Initialize the positions
        model.setAgPos(0, GSize - 1, 0);
    }

    public boolean isPrinterReady() {
        return printerReady;
    }

    public boolean isPrinterWorking() {
        return printerWorking;
    }

    public boolean isPrinterError() {
        return printerError;
    }

    public void print() {
        if (printerReady && !printerError && !printerWorking) {
            printerWorking = true;
            String document = printQueue.poll(); // Get the next document from the queue
            if (document != null) {
                // Simulate printing
                try {
                    Thread.sleep(2000); // Printing takes 2 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printerWorking = false;

                if (Math.random() < 0.1) { // 10% chance of error
                    printerError = true;
                    waserroralready=true;
                } else {
                    printerError = false;
                }
            } else {
                printerWorking = false;
            }
        }
    }

    public boolean executeAction(Structure action) {
        if (action.getFunctor().equals("print")) {
            printQueue.offer("placeholder"); // Add the document to the print queue
            print(); // Start printing the next document in the queue
            return true;
        }
        else if (action.getFunctor().equals("get_location")){
            get_location();
        }
        else if (action.getFunctor().equals("gotrepaired")){
            gotRepaired();
        }
        return false;
    }

    public ArrayList<Percept> newPercepts() {
        ArrayList<Percept> newpercepts = new ArrayList<>();
        if (this.requestedLocation) {
            // Need to change the corrdinates if GSize changes
            newpercepts.add(new Percept(Literal.parseLiteral("location(" + (20) + ", " + (0) + ")")));
            System.out.println("Location sent with percept");
            this.requestedLocation = false;
        }
        if(this.printerError){
            newpercepts.add(new Percept(Literal.parseLiteral("printer_error")));
        }
        return newpercepts;
    }
    public ArrayList<Percept> perceptsToRemove()
    {
        ArrayList<Percept> perceptsToRemove = new ArrayList<>();
        if(!this.requestedLocation)
        {
            perceptsToRemove.add(new Percept(Literal.parseLiteral("location(" + (20) + ", " + (0) + ")")));
        }
        if(!this.printerError)
        {
            perceptsToRemove.add(new Percept(Literal.parseLiteral("printer_error")));
        }
        return perceptsToRemove;
    
    }

    public void gotRepaired() {
        this.printerError = false;
    }

    public void get_location() {
        this.requestedLocation = true;
    }

}