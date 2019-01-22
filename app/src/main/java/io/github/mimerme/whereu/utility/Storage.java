package io.github.mimerme.whereu.utility;

import java.util.Iterator;

//Generic file system interfacing that makes it ez
public interface Storage extends Iterable<String> {
    void writeTo(String data);
}
