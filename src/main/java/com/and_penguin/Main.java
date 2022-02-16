package com.and_penguin;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.mcutils.rand.ChunkRand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static long seed;

    // Looking through a seed randomly
    public static void main(String[] args) {
        System.out.println("Generating a seed...");
        ChunkRand rand = new ChunkRand();
        Random numRand = new Random();
        seed = numRand.nextLong() % (1L << 48);
        while (true) {
            if (filterStructureSeed(seed, rand)) { // If the structure seed matches
                //Loop through all biome seeds
                for (long biomeSeed = 0; biomeSeed < 100; biomeSeed++) {
                    long worldSeed = biomeSeed<<48|seed;
                    if (filterBiomeSeed(worldSeed)) { // If the world seed matches
                        System.out.println(worldSeed + " is a matching seed"); // print out seed
                        return; // Stop searching
                    }
                }
            }
            seed = numRand.nextLong() % (1L << 48); // Chose a new random seed
        }
    }

    // Generates a seed by looking through a file of seeds
    public static void main2(String[] args) {
        System.out.println("Generating a seed...");
        ChunkRand rand = new ChunkRand();
        try {
            Scanner scanner = new Scanner(new File("./seeds.txt"));
            while (scanner.hasNextLong()) {
                seed = scanner.nextLong() % (1L << 48); // Converting the world seed to a structure seed;
                if (filterStructureSeed(seed, rand)) { // If the structure seed matches
                    //Loop through all biome seeds
                    for (long biomeSeed = 0; biomeSeed < 100; biomeSeed++) {
                        long worldSeed = biomeSeed<<48|seed;
                        if (filterBiomeSeed(worldSeed)) {
                            System.out.println(worldSeed + " is a matching seed");
                            return;
                        }
                    }
                }
            }
        }
        catch (FileNotFoundException e) { // If the file isn't found
            System.out.println(e);
        }
    }

    // Checks if the structure seed matches our conditions
    public static boolean filterStructureSeed(long seed, ChunkRand rand) {
        StructureFilter filter = new StructureFilter(seed, rand);
        return filter.filterSeed();
    }

    // Checks if the biome seed matches
    public static boolean filterBiomeSeed(long biomeSeed) { return new BiomeFilter(biomeSeed).filterSeed();
    }
}
