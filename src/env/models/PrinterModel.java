package models;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.Random;

import env.OfficeEnv.OfficeModel;
import jade.util.Logger;
import env.OfficeEnv;
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
    ArrayList<Literal> percepts = new ArrayList<>();
    Logger logger = Logger.getMyLogger(getClass().getName());

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

                // Randomly set printer error
                if (Math.random() < 0.5) { // 10% chance of error
                    printerError = true;
                    percepts.add(Literal.parseLiteral("printer_error"));
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
        return false;
    }

    public ArrayList<Literal> getPercepts() {
        return percepts;
    }

}