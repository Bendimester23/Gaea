package org.polydev.gaea.math;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.polydev.gaea.biome.BiomeGrid;
import org.polydev.gaea.biome.Generator;
import org.polydev.gaea.generation.GenerationPhase;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to abstract away the 16 Interpolators needed to generate a chunk.<br>
 * Contains method to get interpolated noise at a coordinate within the chunk.
 */
public class ChunkInterpolator3 implements ChunkInterpolator {
    private final Interpolator3[][][] interpGrid = new Interpolator3[4][64][4];
    private final Generator[][] gens = new Generator[7][7];
    private final boolean[][] needsBiomeInterp = new boolean[5][5];
    private final double[][][] noiseStorage = new double[7][7][65];
    private final FastNoiseLite noise;
    private final int xOrigin;
    private final int zOrigin;
    private final World w;

    /**
     * Instantiates a 3D ChunkInterpolator at a pair of chunk coordinates, with a BiomeGrid and FastNoiseLite instance.
     *
     * @param chunkX X coordinate of the chunk.
     * @param chunkZ Z coordinate of the chunk.
     * @param grid   BiomeGrid to use for noise fetching.
     * @param noise  FastNoiseLite instance to use.
     */
    public ChunkInterpolator3(World w, int chunkX, int chunkZ, BiomeGrid grid, FastNoiseLite noise) {
        this.xOrigin = chunkX << 4;
        this.zOrigin = chunkZ << 4;
        this.noise = noise;
        this.w = w;


        for(int x = - 1; x < 6; x++) {
            for(int z = - 1; z < 6; z++) {
                gens[x + 1][z + 1] = grid.getBiome(xOrigin + x * 4, zOrigin + z * 4, GenerationPhase.BASE).getGenerator();
            }
        }
        for(int x = 0; x < 5; x++) {
            for(int z = 0; z < 5; z++) {
                needsBiomeInterp[x][z] = compareGens(x+1, z+1);
            }
        }

        storeNoise();

        for(byte x = 0; x < 4; x++) {
            for(byte z = 0; z < 4; z++) {
                for(int y = 0; y < 64; y++) {
                    interpGrid[x][y][z] = new Interpolator3(
                            biomeAvg(x, y, z),
                            biomeAvg(x + 1, y, z),
                            biomeAvg(x, y + 1, z),
                            biomeAvg(x + 1, y + 1, z),
                            biomeAvg(x, y, z + 1),
                            biomeAvg(x + 1, y, z + 1),
                            biomeAvg(x, y + 1, z + 1),
                            biomeAvg(x + 1, y + 1, z + 1));
                }
            }
        }
    }

    private boolean compareGens(int x, int z) {
        Generator comp = gens[x][z];
        if(!comp.equals(gens[x+1][z])) return true;

        if(!comp.equals(gens[x][z+1])) return true;

        if(!comp.equals(gens[x-1][z])) return true;

        if(!comp.equals(gens[x][z-1])) return true;

        if(!comp.equals(gens[x+1][z+1])) return true;

        if(!comp.equals(gens[x-1][z-1])) return true;

        if(!comp.equals(gens[x+1][z-1])) return true;

        return !comp.equals(gens[x - 1][z + 1]);
    }
    private void storeNoise() {
        for(byte x = - 1; x < 6; x++) {
            for(byte z = - 1; z < 6; z++) {
                for(int y = 0; y < 64; y++) {
                    noiseStorage[x + 1][z + 1][y] = gens[x + 1][z + 1].getNoise(noise, w, x * 4 + xOrigin, y * 4, z * 4 + zOrigin);
                }
            }
        }
    }

    private double biomeAvg(int x, int y, int z) {
        if(needsBiomeInterp[x][z]) return (noiseStorage[x + 2][z + 1][y]
                + noiseStorage[x][z + 1][y]
                + noiseStorage[x + 1][z + 2][y]
                + noiseStorage[x + 1][z][y]
                + noiseStorage[x][z][y]
                + noiseStorage[x + 2][z + 2][y]
                + noiseStorage[x + 2][z][y]
                + noiseStorage[x][z + 2][y]
                + noiseStorage[x + 1][z + 1][y]
        ) / 9D;
        else {
            if(gens[x+1][z+1].useMinimalInterpolation()) return noiseStorage[x+1][z+1][y];
            else return (noiseStorage[x + 2][z + 1][y]
                    + noiseStorage[x][z + 1][y]
                    + noiseStorage[x + 1][z + 2][y]
                    + noiseStorage[x + 1][z][y]
                    + noiseStorage[x+1][z+1][y]) / 5D;
        }
    }

    @Override
    public double getNoise(double x, double z) {
        return getNoise(x, 0, z);
    }

    /**
     * Gets the noise at a pair of internal chunk coordinates.
     *
     * @param x The internal X coordinate (0-15).
     * @param z The internal Z coordinate (0-15).
     * @return double - The interpolated noise at the coordinates.
     */
    @Override
    public double getNoise(double x, double y, double z) {
        return interpGrid[((int) x) / 4][((int) y) / 4][((int) z) / 4].trilerp((x % 4) / 4, (y % 4) / 4, (z % 4) / 4);
    }
}
