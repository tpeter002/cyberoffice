package example;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;


public class NyomtatoEnv extends Environment {
    private static final Logger logger = Logger.getLogger(NyomtatoEnv.class.getName());

    private boolean printerReady = true;
    private boolean printerWorking = false;
    private boolean printerError = false;
    private Queue<String> printQueue = new LinkedList<>();

    @Override
    public void init(String[] args) {
        super.init(args);
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
        if (printerReady && !printerError) {
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
                if (Math.random() < 0.1) { // 10% chance of error
                    printerError = true;
                    logger.info("Printer encountered an error!");
                } else {
                    printerError = false;
                }
            } else {
                printerWorking = false;
            }
        } else {
            logger.info("Printer is not ready or has an error!");
        }
    }

    @Override
    public boolean executeAction(String agentName, Structure action) {
        if (action.getFunctor().equals("print")) {
            Literal document = (Literal) action.getTerm(0);
            printQueue.offer(document.toString()); // Add the document to the print queue
            print(); // Start printing the next document in the queue
            return true;
        }
        return false;
    }
}