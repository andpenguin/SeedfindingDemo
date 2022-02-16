package com.and_penguin;

import kaptainwutax.biomeutils.source.OverworldBiomeSource;

public class BiomeFilter {
    private long seed;

    public BiomeFilter(long seed) { // Creates a Biome Filter
        this.seed = seed;
    }

    // Checks if the saved temple can spawn
    public boolean filterSeed() {
        OverworldBiomeSource source = new OverworldBiomeSource(StructureFilter.VERSION, seed); // Find biomes in a seed
        if (StructureFilter.temple.canSpawn(Storage.templeCoords, source)) { // If it can spawn
            return true;
        }
        return false;
    }
}
