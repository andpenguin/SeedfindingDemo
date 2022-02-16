package com.and_penguin;

import kaptainwutax.biomeutils.source.EndBiomeSource;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.featureutils.structure.generator.structure.EndCityGenerator;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.util.pos.RPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.terrain.EndTerrainGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class StructureFilter {
    private long seed;
    private ChunkRand rand;
    public static final MCVersion VERSION = MCVersion.v1_16_1; // Version of the game
    public static DesertPyramid temple = new DesertPyramid(VERSION); // Desert pyramid template
    public static RuinedPortal portal = new RuinedPortal(Dimension.OVERWORLD, VERSION); //Dimension specific example
    public static EndCity endCity = new EndCity(VERSION);
    public static BastionRemnant bastion = new BastionRemnant(VERSION);
    public static Fortress fortress = new Fortress(VERSION);
    private CPos city;
    public static final double MAX_DIST = 100.0D * 100.0D; // In blocks
    public static final double NETHER_DIST = 10.0D * 10.0D; // In chunks

    public StructureFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
    }

    // Desert Temple in the 0,0 region within 100 blocks of spawn
    public boolean filterSeed() {
        // Find where a Desert Temple is in a certain region on the seed
        CPos templeLoc = temple.getInRegion(seed, 0, 0, rand);
        //return templeLoc.distanceTo(new CPos(0,0), DistanceMetric.EUCLIDEAN_SQ) < MAX_DIST; // Works for any CPos
        if (templeLoc.toBlockPos().getMagnitudeSq() < MAX_DIST) { // Checks temple's block magnitude ^2 compared to the maximum distance\
            Storage.templeCoords = templeLoc;
            System.out.println(templeLoc.toBlockPos());
            return true;
        }
        return false;
    }

    /**
     * Finds the block position of the first gateway
     * @param structureSeed the structure seed to be checked
     * @return the Block position of the gateway
     */
    public static BPos firstGateway(long structureSeed) {
        ArrayList<Integer> gateways = new ArrayList<>();
        for (int i = 0; i < 20; i++) gateways.add(i);
        Collections.shuffle(gateways, new Random(structureSeed));
        double angle = 2.0 * (-1*Math.PI + 0.15707963267948966 * (gateways.remove(gateways.size() - 1)));
        int gateway_x = (int)(1000.0 * Math.cos(angle));
        int gateway_z = (int)(1000.0 * Math.sin(angle));
        return new BPos(gateway_x, 0, gateway_z);
    }

    // Looks for fastions
    public boolean filterNether() {
        Storage.bastionCoords = null;
        CPos[] bastionLocs = new CPos[4];
        CPos[] fortressLocs = new CPos[4];
        // Loop through the primary quadrants
        for (int x = -1; x < 1; x++) {
            for (int z = -1; z < 1; z++) {
                bastionLocs[(x+1) + (z+1)*2] = bastion.getInRegion(seed, x, z, rand);
                fortressLocs[(x+1) + (z+1)*2] = fortress.getInRegion(seed, x, z, rand);
            }
        }

        //Check if fastions are present
        for (CPos fortress: fortressLocs) {
            for (CPos bastion: bastionLocs) {
                if (fortress != null && bastion != null && fortress.getMagnitudeSq() < NETHER_DIST && // Within dist of 0,0
                    fortress.distanceTo(bastion, DistanceMetric.EUCLIDEAN_SQ) < NETHER_DIST) { // Within dist of the bastion
                    Storage.bastionCoords = bastion; // fastion found
                    return true;
                }
            }
        }
        return false;
    }

    // Checks if bastion is not covered up by basalt
    public boolean filterNetherBiomes() {
        NetherBiomeSource source = new NetherBiomeSource(VERSION, seed);
        return bastion.canSpawn(Storage.bastionCoords, source);
    }

    //Finds if there is a close end city structure
    public boolean filterEnd() {
        BPos gateway = firstGateway(seed);
        RPos region = gateway.toRegionPos(20 << 4);
        city = endCity.getInRegion(seed, region.getX(), region.getZ(), rand);
        return city.toBlockPos().distanceTo(gateway, DistanceMetric.EUCLIDEAN_SQ) < MAX_DIST;
    }

    //Filter End Biomes for city containing ship
    public boolean filterEndBiomes() {
        EndBiomeSource source = new EndBiomeSource(VERSION, seed);
        EndTerrainGenerator gen = new EndTerrainGenerator(source);

        if (!endCity.canSpawn(city.getX(), city.getZ(), source)) return false; // Can spawn
        if (!endCity.canGenerate(city.getX(), city.getZ(), gen)) return false; // Can generate

        EndCityGenerator generator = new EndCityGenerator(VERSION);
        generator.generate(gen, city); // Load an end city

        return generator.hasShip(); // Does it have a ship
    }

}
