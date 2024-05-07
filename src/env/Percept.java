package env;

import jason.asSyntax.*;    


public class Percept {
    public String destination;
    public Literal message;

    public Percept(String name, Literal message) {
        this.destination = name;
        this.message = message;
    }

    public boolean hasDestination() {
        return this.destination != null;
    }

    public boolean equals(Percept other) {
        return this.message.equals(other.message) && this.destination.equals(other.destination);
    }
}