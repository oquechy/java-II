package ru.spbau.mit.oquechy.stress.utils;

import lombok.Getter;

public class Statistic {
    @Getter
    private double sortingTime;
    @Getter
    private double transmittingTime;
    @Getter
    private double servingTime;

    public Statistic(long sortingTime, long transmittingTime, long servingTime, int queries) {
        this.sortingTime = sortingTime / (1. * queries);
        this.transmittingTime = transmittingTime / (1. * queries);
        this.servingTime = servingTime / (1. * queries);
    }
}
