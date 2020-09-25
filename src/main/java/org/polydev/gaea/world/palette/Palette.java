package org.polydev.gaea.world.palette;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.polydev.gaea.math.FastNoise;
import org.polydev.gaea.math.ProbabilityCollection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * A class representation of a "slice" of the world.
 * Used to get a section of blocks, based on the depth at which they are found.
 */
public abstract class Palette<E> {
    private final List<PaletteLayer<E>> pallet = new ArrayList<>();

    /**
     * Constructs a blank palette.
     */
    public Palette() {

    }

    /**
     * Adds a material to the palette, for a number of layers.
     *
     * @param m      - The material to add to the palette.
     * @param layers - The number of layers the material occupies.
     * @return - BlockPalette instance for chaining.
     */
    public Palette<E> add(E m, int layers) {
        pallet.add(new PaletteLayer<>(m, layers + (pallet.size() == 0 ? 0 : pallet.get(pallet.size() - 1).getLayers())));
        return this;
    }

    /**
     * Adds a ProbabilityCollection to the palette, for a number of layers.
     *
     * @param m      - The ProbabilityCollection to add to the palette.
     * @param layers - The number of layers the material occupies.
     * @return - BlockPalette instance for chaining.
     */
    public Palette<E> add(ProbabilityCollection<E> m, int layers) {
        ProbabilityCollection<E> d = new ProbabilityCollection<>();
        Iterator<ProbabilityCollection.ProbabilitySetElement<E>> i = m.iterator();
        while(i.hasNext()) {
            ProbabilityCollection.ProbabilitySetElement<E> e = i.next();
            d.add(e.getObject(), e.getProbability());
        }
        pallet.add(new PaletteLayer<>(d, layers + (pallet.size() == 0 ? 0 : pallet.get(pallet.size() - 1).getLayers())));
        return this;
    }

    /**
     * Fetches a material from the palette, at a given layer.
     *
     * @param layer - The layer at which to fetch the material.
     * @return BlockData - The material fetched.
     */
    public abstract E get(int layer, int x, int z);


    public int getSize() {
        return pallet.get(pallet.size()-1).getLayers();
    }

    public List<PaletteLayer<E>> getLayers() {
        return pallet;
    }

    /**
     * Class representation of a layer of a BlockPalette.
     */
    public static class PaletteLayer<E> {
        private final boolean col;
        private final int layers;
        private ProbabilityCollection<E> collection;
        private E m;

        /**
         * Constructs a PaletteLayer with a ProbabilityCollection of materials and a number of layers.
         *
         * @param type   - The collection of materials to choose from.
         * @param layers - The number of layers.
         */
        public PaletteLayer(ProbabilityCollection<E> type, int layers) {
            this.col = true;
            this.collection = type;
            this.layers = layers;
        }

        /**
         * Constructs a PaletteLayer with a single Material and a number of layers.
         *
         * @param type   - The material to use.
         * @param layers - The number of layers.
         */
        public PaletteLayer(E type, int layers) {
            this.col = false;
            this.m = type;
            this.layers = layers;
        }

        /**
         * Gets the number of layers.
         *
         * @return int - the number of layers.
         */
        public int getLayers() {
            return layers;
        }

        /**
         * Gets a material from the layer.
         *
         * @return Material - the material..
         */
        public E get(Random random) {
            if(col) return this.collection.get(random);
            return m;
        }

        public E get(FastNoise random, int x, int z) {
            if(col) return this.collection.get(random, x, z);
            return m;
        }

        public ProbabilityCollection<E> getCollection() {
            return collection;
        }
    }
}