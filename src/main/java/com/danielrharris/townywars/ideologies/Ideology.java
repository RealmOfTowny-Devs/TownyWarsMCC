package com.danielrharris.townywars.ideologies;

public enum Ideology{

    ECONOMIC("Economic"), RELIGIOUS("Religious") , MILITARISTIC("Militaristic");

    private String name;

    Ideology(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
