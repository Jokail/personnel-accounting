package org.viti.demo;

public class Person {
    private String name;
    private boolean arrived;

    public Person(String name, boolean arrived) {
        this.name = name;
        this.arrived = arrived;
    }

    public String getName() {
        return name;
    }

    public boolean isArrived() {
        return arrived;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    public void setName(String name) {
        this.name = name;
    }
}
