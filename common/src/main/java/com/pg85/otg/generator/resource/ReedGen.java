package com.pg85.otg.generator.resource;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

public class ReedGen extends Resource
{
    private final int maxAltitude;
    private final int minAltitude;
    private final MaterialSet sourceBlocks;

    public ReedGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(6, args);

        material = readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), PluginStandardValues.WORLD_DEPTH,
                PluginStandardValues.WORLD_HEIGHT);
        maxAltitude = readInt(args.get(4), minAltitude,
                PluginStandardValues.WORLD_HEIGHT);
        sourceBlocks = readMaterials(args, 5);
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final ReedGen compare = (ReedGen) other;
        return this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude
               && (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                   : this.sourceBlocks.equals(compare.sourceBlocks));
    }

    @Override
    public int getPriority()
    {
        return -34;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + super.hashCode();
        hash = 23 * hash + this.minAltitude;
        hash = 23 * hash + this.maxAltitude;
        hash = 23 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "Reed(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = world.getHighestBlockYAt(x, z);
        if (y > maxAltitude || y < minAltitude || (!world.getMaterial(x - 1, y - 1, z, false).isLiquid() && !world.getMaterial(x + 1, y - 1, z, false).isLiquid() && !world.getMaterial(x, y - 1, z - 1, false).isLiquid() && !world.getMaterial(x, y - 1, z + 1, false).isLiquid()))
        {
            return;
        }
        if (!sourceBlocks.contains(world.getMaterial(x, y - 1, z, false)))
        {
            return;
        }

        int n = 1 + rand.nextInt(2);
        for (int i1 = 0; i1 < n; i1++)
            world.setBlock(x, y + i1, z, material, null, false);
    }
    
}