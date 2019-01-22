package io.github.mimerme.whereu.core;

import java.util.HashMap;
import java.util.HashSet;

import io.github.mimerme.whereu.utility.Storage;

//Lightweight class for use only with the Broadcast Receiver
public class WhitelistLoader {
    private HashSet<String> whitelist = new HashSet<>();

    public WhitelistLoader(Storage storage){
        for(String line : storage){
            whitelist.add(line.split(":")[1]);
        }
    }

    //Check to see if a number is on the whitelist or not
    public boolean valid(String number){
        return whitelist.contains(number);
    }
}
