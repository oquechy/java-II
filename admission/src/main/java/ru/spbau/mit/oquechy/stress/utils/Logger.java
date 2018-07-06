package ru.spbau.mit.oquechy.stress.utils;

import ru.spbau.mit.oquechy.stress.types.Architecture;
import ru.spbau.mit.oquechy.stress.types.Property;

import java.io.*;

public class Logger implements AutoCloseable {
    private final Writer sortingWriter;
    private final Writer transmittingWriter;
    private final Writer servingWriter;

    private int i = 1;

    public Logger(Architecture architecture, Property property, int iterations, int step, int queries, int length,
                  int clients, int pause) throws IOException {
        String name = architecture + "_" + property + "_";

        sortingWriter = new OutputStreamWriter(new FileOutputStream(name + "sorting"));
        transmittingWriter = new OutputStreamWriter(new FileOutputStream(name + "transmitting"));
        servingWriter = new OutputStreamWriter(new FileOutputStream(name + "serving"));

        String header = "-- iterations: " + iterations + " step: " + step + " queries: " + queries + " length: " +
                length + " clients: " + clients + " delay: " + pause;
        sortingWriter.write(header);
        transmittingWriter.write(header);
        servingWriter.write(header);
    }

    public void close() throws IOException {
        sortingWriter.close();
        transmittingWriter.close();
        servingWriter.close();
    }

    public void add(Statistic statistic) throws IOException {
        sortingWriter.append(",\n           (")
                .append(String.valueOf(i))
                .append(", ")
                .append(String.valueOf(statistic.getSortingTime()))
                .append(")");
        transmittingWriter.append(",\n           (")
                .append(String.valueOf(i))
                .append(", ")
                .append(String.valueOf(statistic.getTransmittingTime()))
                .append(")");
        servingWriter.append(",\n           (")
                .append(String.valueOf(i++))
                .append(", ")
                .append(String.valueOf(statistic.getServingTime()))
                .append(")");
    }
}
