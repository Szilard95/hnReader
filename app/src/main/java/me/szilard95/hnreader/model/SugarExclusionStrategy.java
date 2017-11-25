package me.szilard95.hnreader.model;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class SugarExclusionStrategy implements ExclusionStrategy {
    private Class<?> clazz;

    public SugarExclusionStrategy(Class<?> clazzToExclude) {
        this.clazz = clazzToExclude;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getDeclaringClass().equals(clazz) && f.getName().equals("id");
    }
}
