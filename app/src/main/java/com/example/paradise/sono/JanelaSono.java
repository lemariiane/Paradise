package com.example.paradise.sono;

public class JanelaSono {
    private final long minMin; // mínimo em minutos
    private final long maxMin; // máximo em minutos

    public JanelaSono(long minMin, long maxMin) {
        this.minMin = minMin;
        this.maxMin = maxMin;
    }

    public long getMinMin() { return minMin; }
    public long getMaxMin() { return maxMin; }

    // Timestamps do intervalo a partir de quando o bebê acordou
    public long getInicioJanelaMs(long acordouEm) {
        return acordouEm + (minMin * 60 * 1000L);
    }

    public long getFimJanelaMs(long acordouEm) {
        return acordouEm + (maxMin * 60 * 1000L);
    }
}