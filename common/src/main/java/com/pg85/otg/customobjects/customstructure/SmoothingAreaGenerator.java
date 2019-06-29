package com.pg85.otg.customobjects.customstructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.bo3function.BlockFunction;
import com.pg85.otg.customobjects.bo3.bo3function.RandomBlockFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.surface.MesaSurfaceGenerator;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

public class SmoothingAreaGenerator
{   
    Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawnPerDiagonalLineDestination = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();
    Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawnPerLineOrigin = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();    
	
	public void CustomObjectStructureSpawn(Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawn)
	{
		Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawnPerDiagonalLineOrigin = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();
		SmoothingAreasToSpawnPerDiagonalLineDestination.clear();
		SmoothingAreasToSpawnPerLineOrigin.clear();
		Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawnPerLineDestination = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();

		for(Entry<ChunkCoordinate, ArrayList<Object[]>> smoothingArea : SmoothingAreasToSpawn.entrySet())
		{
			ArrayList<Object[]> smoothingAreaLines = smoothingArea.getValue();
			for(Object[] smoothingAreaLine : smoothingAreaLines)
			{
    			int originPointX2 = (Integer)smoothingAreaLine[6];
				int originPointZ2 = (Integer)smoothingAreaLine[8];

				ChunkCoordinate originChunk = ChunkCoordinate.fromBlockCoords(originPointX2, originPointZ2);
            	ArrayList<Object[]> lineInOriginChunkSaved2 = SmoothingAreasToSpawnPerLineOrigin.get(originChunk);
            	if(lineInOriginChunkSaved2 == null)
            	{
                	ArrayList<Object[]> smoothingAreaLines2 = new ArrayList<Object[]>();
                	smoothingAreaLines2.add(smoothingAreaLine);
                	SmoothingAreasToSpawnPerLineOrigin.put(ChunkCoordinate.fromChunkCoords(originPointX2, originPointZ2), smoothingAreaLines2);
                } else {
                	lineInOriginChunkSaved2.add(smoothingAreaLine);
                }

            	int finalDestinationPointX2 = (Integer)smoothingAreaLine[9];
                int finalDestinationPointZ2 = (Integer)smoothingAreaLine[11];

				originChunk = ChunkCoordinate.fromBlockCoords(finalDestinationPointX2, finalDestinationPointZ2);
            	ArrayList<Object[]> lineInOriginChunkSaved3 = SmoothingAreasToSpawnPerLineDestination.get(originChunk);
            	if(lineInOriginChunkSaved3 == null)
            	{
                	ArrayList<Object[]> smoothingAreaLines2 = new ArrayList<Object[]>();
                	smoothingAreaLines2.add(smoothingAreaLine);
                	SmoothingAreasToSpawnPerLineDestination.put(ChunkCoordinate.fromChunkCoords(finalDestinationPointX2, finalDestinationPointZ2), smoothingAreaLines2);
                } else {
                	lineInOriginChunkSaved3.add(smoothingAreaLine);
                }

        		if(smoothingAreaLine.length > 17)
        		{
                	int diagonalLineFinalOriginPointX2 = (Integer)smoothingAreaLine[12];
                    int diagonalLineFinalOriginPointZ2 = (Integer)smoothingAreaLine[14];

                	int diagonalLineFinalDestinationPointX2 = (Integer)smoothingAreaLine[15];
                    int diagonalLineFinalDestinationPointZ2 = (Integer)smoothingAreaLine[17];

                    originChunk = ChunkCoordinate.fromBlockCoords(diagonalLineFinalOriginPointX2, diagonalLineFinalOriginPointZ2);
                    ArrayList<Object[]> lineInOriginChunkSaved4 = SmoothingAreasToSpawnPerDiagonalLineOrigin.get(originChunk);
                    if(lineInOriginChunkSaved4 == null)
                    {
                    	ArrayList<Object[]> smoothingAreaLines2 = new ArrayList<Object[]>();
                    	smoothingAreaLines2.add(smoothingAreaLine);
                    	SmoothingAreasToSpawnPerDiagonalLineOrigin.put(ChunkCoordinate.fromChunkCoords(diagonalLineFinalOriginPointX2, diagonalLineFinalOriginPointZ2), smoothingAreaLines2);
                    } else {
                    	lineInOriginChunkSaved4.add(smoothingAreaLine);
                    }

                    originChunk = ChunkCoordinate.fromBlockCoords(diagonalLineFinalDestinationPointX2, diagonalLineFinalDestinationPointZ2);
                    ArrayList<Object[]> lineInOriginChunkSaved = SmoothingAreasToSpawnPerDiagonalLineDestination.get(originChunk);
                    if(lineInOriginChunkSaved == null)
                    {
                    	ArrayList<Object[]> smoothingAreaLines2 = new ArrayList<Object[]>();
                    	smoothingAreaLines2.add(smoothingAreaLine);
                    	SmoothingAreasToSpawnPerDiagonalLineDestination.put(ChunkCoordinate.fromChunkCoords(diagonalLineFinalDestinationPointX2, diagonalLineFinalDestinationPointZ2), smoothingAreaLines2);
                    } else {
                    	lineInOriginChunkSaved.add(smoothingAreaLine);
                    }
            	}
			}
		}
	}
	
    /**
     * Adds a smoothing area around the lowest layer of blocks in all BO3's within this branching structure that have smoothRadius set to a value higher than 0.
     * The smooth area is basicly a set of lines, each line being a set of start- and end-point coordinates. Each line starts from a block on the lowest
     * layer of blocks in the BO3 that has no neighbouring block on one of four sides horizontally (taking into account any neighbouring branches that connect seamlessly).
     * The line is drawn starting at that block and then goes outward in the direction where no neighbouring block was found, the length of the line being the smoothRadius.
     * Later, when the BO3 blocks and the smoothing areas are actually being spawned, the y-value of the endpoint is detected (since by then the terrain will have spawned
     * and we'll be able to detect the highest solid block in the landscape that we'll need to smooth to) and the lines of blocks we've plotted are spawned, creating a nice
     * linear slope from the highest solid block in the landscape to the lowest block in the BO3 (the block we started at when drawing the line).
    */
    public Map<ChunkCoordinate, ArrayList<Object[]>> calculateSmoothingAreas(Map<ChunkCoordinate, Stack<CustomObjectCoordinate>> ObjectsToSpawn, CustomObjectCoordinate Start, LocalWorld World)
    {
        // TODO: Don't check neighbouring BO3's with SmoothRadius -1

        // Get all solid blocks on the lowest layer of this BO3 that border an air block or have no neighbouring blocks
        // This may include blocks on the border of this BO3 that are supposed to seamlessly border another BO3, remove those later since they shouldnt be smoothed
        Map<ChunkCoordinate, ArrayList<Object[]>> smoothToBlocksPerChunk = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesn't exactly
        // make the code any easier to read..
        // Object[] { bO3, blockX, blockY, blockZ, smoothInDirection1, smoothInDirection2, smoothInDirection3, smoothInDirection4, smoothRadius }
        ArrayList<Object[]> smoothToBlocks;
        ChunkCoordinate chunkCoord;
        Stack<CustomObjectCoordinate> bO3sInChunk;
        boolean bFoundNeighbour1;
        boolean bFoundNeighbour2;
        boolean bFoundNeighbour3;
        boolean bFoundNeighbour4;
        CustomObjectCoordinate neighbouringBlockCoords;
        int normalizedNeigbouringBlockX;
        int normalizedNeigbouringBlockY;
        int normalizedNeigbouringBlockZ;
        ChunkCoordinate neighbouringBlockChunk;
        ChunkCoordinate searchTarget;
        Stack<CustomObjectCoordinate> bO3sInNeighbouringBlockChunk;
        CustomObjectCoordinate blockToCheckCoords;
        int normalizedBlockToCheckX;
        int normalizedBlockToCheckY;
        int normalizedBlockToCheckZ;

        // Get all BO3's that are a part of this branching structure
        for(Entry<ChunkCoordinate, Stack<CustomObjectCoordinate>> chunkCoordSet : ObjectsToSpawn.entrySet())
        {
            chunkCoord = chunkCoordSet.getKey();
            bO3sInChunk = chunkCoordSet.getValue();
            smoothToBlocks = new ArrayList<Object[]>();

            for(CustomObjectCoordinate objectInChunk : bO3sInChunk)
            {
            	if(objectInChunk.isSpawned)
            	{
            		continue;
            	}

            	BO3 bO3InChunk = ((BO3)objectInChunk.getObject());
            	boolean SmoothStartTop = ((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothStartTop : bO3InChunk.getSettings().smoothStartTop;

                //if((((BO3)Start.getObject(World.getName())).settings.overrideChildSettings && ((BO3)bO3InChunk.getObject(World.getName())).settings.overrideChildSettings && ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius != -1 ? ((BO3)Start.getObject(World.getName())).settings.smoothRadius : ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius) > 0)
                int smoothRadius = ((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothRadius : bO3InChunk.getSettings().smoothRadius;
                if(smoothRadius == -1 || bO3InChunk.getSettings().smoothRadius == -1)
                {
                	smoothRadius = 0;
                }
                if(smoothRadius > 0)
                {
        			Map<ChunkCoordinate, BlockFunction> heightMap = bO3InChunk.getSettings().getHeightMap((BO3)Start.getObject());

                    // if !SmoothStartTop then for each BO3 that has a smoothradius > 0 get the lowest layer of blocks and determine smooth area starting points
                	// if SmoothStartTop then for each BO3 that has a smoothradius > 0 get the highest blocks of the BO3 and determine smooth area starting points

                	for(int x = 0; x <= 15; x ++)
                	{
                		for(int z = 0; z <= 15; z ++)
                		{
                			BlockFunction block = heightMap.get(ChunkCoordinate.fromChunkCoords(x, z));

                			if(block != null)
                			{
                            	//if(1 == 1) { throw new RuntimeException();}

                                bFoundNeighbour1 = false;
                                bFoundNeighbour2 = false;
                                bFoundNeighbour3 = false;
                                bFoundNeighbour4 = false;

                                //Check if any neighbouring blocks are air or non-existent within this BO3
                                if(heightMap.get(ChunkCoordinate.fromChunkCoords(block.x - 1, block.z)) != null)
                                {
                                    bFoundNeighbour1 = true;
                                }
                                if(heightMap.get(ChunkCoordinate.fromChunkCoords(block.x + 1, block.z)) != null)
                                {
                                    bFoundNeighbour2 = true;
                                }
                                if(heightMap.get(ChunkCoordinate.fromChunkCoords(block.x, block.z - 1)) != null)
                                {
                                    bFoundNeighbour3 = true;
                                }
                                if(heightMap.get(ChunkCoordinate.fromChunkCoords(block.x, block.z + 1)) != null)
                                {
                                    bFoundNeighbour4 = true;
                                }

                                // If one of the neighbouring blocks has not been found in this BO3 and the block is on the edge of the BO3
                                // then check for other BO3's that may have blocks that border this block.
                                // If a solid neighbouring block is found then don't smooth in that direction.

                                if(!bFoundNeighbour1 && block.x - 1 < 0)
                                {
                                    // Check if the BO3 contains a block at the location of the neighbouring block
                                    // Normalize the coordinates of the neighbouring block taking into consideration rotation
                                    neighbouringBlockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x - 1, block.y, block.z, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    // Get the chunk that the neighbouring block is in
                                    neighbouringBlockChunk = null;
                                    searchTarget = ChunkCoordinate.fromBlockCoords(normalizedNeigbouringBlockX, normalizedNeigbouringBlockZ);
                                    for(ChunkCoordinate chunkInStructure : ObjectsToSpawn.keySet())
                                    {
                                        // Find the chunk that contains the coordinates were looking for
                                        if(chunkInStructure.getChunkX() == searchTarget.getChunkX() && chunkInStructure.getChunkZ() == searchTarget.getChunkZ())
                                        {
                                            neighbouringBlockChunk = chunkInStructure;
                                            break;
                                        }
                                    }
                                    if(neighbouringBlockChunk != null)
                                    {
                                        // Found the neighbouring chunk
                                        bO3sInNeighbouringBlockChunk = ObjectsToSpawn.get(neighbouringBlockChunk);
                                        if(bO3sInNeighbouringBlockChunk != null)
                                        {
                                            for(CustomObjectCoordinate bO3ToCheck : bO3sInNeighbouringBlockChunk)
                                            {
                                                if(bO3ToCheck != objectInChunk)
                                                {
                                                    // Now find the actual block
                                                	Map<ChunkCoordinate, BlockFunction> neighbouringBO3HeightMap = ((BO3)bO3ToCheck.getObject()).getSettings().getHeightMap((BO3)Start.getObject());

                                                	for(Entry<ChunkCoordinate, BlockFunction> blockToCheckEntry : neighbouringBO3HeightMap.entrySet())
                                                    {
                                                		BlockFunction blockToCheck = blockToCheckEntry.getValue();

                                                        blockToCheckCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(blockToCheck.x, blockToCheck.y, blockToCheck.z, bO3ToCheck.getRotation());
                                                        normalizedBlockToCheckX = blockToCheckCoords.getX() + (bO3ToCheck.getX());
                                                        normalizedBlockToCheckY = blockToCheckCoords.getY() + bO3ToCheck.getY();
                                                        normalizedBlockToCheckZ = blockToCheckCoords.getZ() + (bO3ToCheck.getZ());

                                                        if(normalizedNeigbouringBlockX == normalizedBlockToCheckX && (normalizedNeigbouringBlockY == normalizedBlockToCheckY || SmoothStartTop) && normalizedNeigbouringBlockZ == normalizedBlockToCheckZ)
                                                        {
                                                            // Neighbouring block found
                                                        	if(isMaterialSmoothingAnchor(blockToCheck, bO3ToCheck, Start))
                                                            {
                                                            	bFoundNeighbour1 = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if(bFoundNeighbour1)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(!bFoundNeighbour2 && block.x + 1 > 15)
                                {
                                    // Check if the BO3 contains a block at the location of the neighbouring block
                                    //Normalize the coordinates of the neigbouring block taking into consideration rotation
                                    neighbouringBlockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + 1, block.y, block.z, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    // Get the chunk that the neighbouring block is in
                                    neighbouringBlockChunk = null;
                                    searchTarget = ChunkCoordinate.fromBlockCoords(normalizedNeigbouringBlockX, normalizedNeigbouringBlockZ);
                                    for(ChunkCoordinate chunkInStructure : ObjectsToSpawn.keySet())
                                    {
                                        // Find the chunk that contains the coordinates being looked for
                                        if(chunkInStructure.getChunkX() == searchTarget.getChunkX() && chunkInStructure.getChunkZ() == searchTarget.getChunkZ())
                                        {
                                            neighbouringBlockChunk = chunkInStructure;
                                            break;
                                        }
                                    }
                                    if(neighbouringBlockChunk != null)
                                    {
                                        //found the neighbouring chunk
                                        bO3sInNeighbouringBlockChunk = ObjectsToSpawn.get(neighbouringBlockChunk);
                                        if(bO3sInNeighbouringBlockChunk != null)
                                        {
                                            for(CustomObjectCoordinate bO3ToCheck : bO3sInNeighbouringBlockChunk)
                                            {
                                                if(bO3ToCheck != objectInChunk)
                                                {
                                                    // Now find the actual block
                                                	Map<ChunkCoordinate, BlockFunction> neighbouringBO3HeightMap = ((BO3)bO3ToCheck.getObject()).getSettings().getHeightMap((BO3)Start.getObject());

                                                	for(Entry<ChunkCoordinate, BlockFunction> blockToCheckEntry : neighbouringBO3HeightMap.entrySet())
                                                	{
                                                		BlockFunction blockToCheck = blockToCheckEntry.getValue();

                                                        blockToCheckCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(blockToCheck.x, blockToCheck.y, blockToCheck.z, bO3ToCheck.getRotation());
                                                        normalizedBlockToCheckX = blockToCheckCoords.getX() + (bO3ToCheck.getX());
                                                        normalizedBlockToCheckY = blockToCheckCoords.getY() + bO3ToCheck.getY();
                                                        normalizedBlockToCheckZ = blockToCheckCoords.getZ() + (bO3ToCheck.getZ());

                                                        if(normalizedNeigbouringBlockX == normalizedBlockToCheckX && (normalizedNeigbouringBlockY == normalizedBlockToCheckY || SmoothStartTop) && normalizedNeigbouringBlockZ == normalizedBlockToCheckZ)
                                                        {
                                                            // Neighbouring block found
                                                        	if(isMaterialSmoothingAnchor(blockToCheck, bO3ToCheck, Start))
                                                            {
                                                                bFoundNeighbour2 = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if(bFoundNeighbour2)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(!bFoundNeighbour3 && block.z - 1 < 0)
                                {
                                    // Check if the BO3 contains a block at the location of the neighbouring block
                                    //Normalize the coordinates of the neigbouring block taking into consideration rotation
                                    neighbouringBlockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x, block.y, block.z - 1, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    // Get the chunk that the neighbouring block is in
                                    neighbouringBlockChunk = null;
                                    searchTarget = ChunkCoordinate.fromBlockCoords(normalizedNeigbouringBlockX, normalizedNeigbouringBlockZ);
                                    for(ChunkCoordinate chunkInStructure : ObjectsToSpawn.keySet())
                                    {
                                        // Find the chunk that contains the coordinates being looked for
                                        if(chunkInStructure.getChunkX() == searchTarget.getChunkX() && chunkInStructure.getChunkZ() == searchTarget.getChunkZ())
                                        {
                                            neighbouringBlockChunk = chunkInStructure;
                                            break;
                                        }
                                    }
                                    if(neighbouringBlockChunk != null)
                                    {
                                        //found the neighbouring chunk
                                        bO3sInNeighbouringBlockChunk = ObjectsToSpawn.get(neighbouringBlockChunk);
                                        if(bO3sInNeighbouringBlockChunk != null)
                                        {
                                            for(CustomObjectCoordinate bO3ToCheck : bO3sInNeighbouringBlockChunk)
                                            {
                                                if(bO3ToCheck != objectInChunk)
                                                {
                                                    // Now find the actual block
                                                	Map<ChunkCoordinate, BlockFunction> neighbouringBO3HeightMap = ((BO3)bO3ToCheck.getObject()).getSettings().getHeightMap((BO3)Start.getObject());

                                                	for(Entry<ChunkCoordinate, BlockFunction> blockToCheckEntry : neighbouringBO3HeightMap.entrySet())
                                                    {
                                                		BlockFunction blockToCheck = blockToCheckEntry.getValue();

                                                        blockToCheckCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(blockToCheck.x, blockToCheck.y, blockToCheck.z, bO3ToCheck.getRotation());
                                                        normalizedBlockToCheckX = blockToCheckCoords.getX() + (bO3ToCheck.getX());
                                                        normalizedBlockToCheckY = blockToCheckCoords.getY() + bO3ToCheck.getY();
                                                        normalizedBlockToCheckZ = blockToCheckCoords.getZ() + (bO3ToCheck.getZ());

                                                        if(normalizedNeigbouringBlockX == normalizedBlockToCheckX && (normalizedNeigbouringBlockY == normalizedBlockToCheckY || SmoothStartTop) && normalizedNeigbouringBlockZ == normalizedBlockToCheckZ)
                                                        {
                                                            // Neighbouring block found
                                                        	if(isMaterialSmoothingAnchor(blockToCheck, bO3ToCheck, Start))
                                                            {
                                                            	bFoundNeighbour3 = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if(bFoundNeighbour3)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(!bFoundNeighbour4 && block.z + 1 > 15)
                                {
                                    // Check if the BO3 contains a block at the location of the neighbouring block
                                    // Normalize the coordinates of the neighbouring block taking into consideration rotation
                                    neighbouringBlockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x, block.y, block.z + 1, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    // Get the chunk that the neighbouring block is in
                                    neighbouringBlockChunk = null;
                                    searchTarget = ChunkCoordinate.fromBlockCoords(normalizedNeigbouringBlockX, normalizedNeigbouringBlockZ);
                                    for(ChunkCoordinate chunkInStructure : ObjectsToSpawn.keySet())
                                    {
                                        // Find the chunk that contains the coordinates being looked for
                                        if(chunkInStructure.getChunkX() == searchTarget.getChunkX() && chunkInStructure.getChunkZ() == searchTarget.getChunkZ())
                                        {
                                            neighbouringBlockChunk = chunkInStructure;
                                            break;
                                        }
                                    }
                                    if(neighbouringBlockChunk != null)
                                    {
                                        //found the neighbouring chunk
                                        bO3sInNeighbouringBlockChunk = ObjectsToSpawn.get(neighbouringBlockChunk);
                                        if(bO3sInNeighbouringBlockChunk != null)
                                        {
                                            for(CustomObjectCoordinate bO3ToCheck : bO3sInNeighbouringBlockChunk)
                                            {
                                                if(bO3ToCheck != objectInChunk)
                                                {
                                                    // Now find the actual block
                                                	Map<ChunkCoordinate, BlockFunction> neighbouringBO3HeightMap = ((BO3)bO3ToCheck.getObject()).getSettings().getHeightMap((BO3)Start.getObject());

                                                	for(Entry<ChunkCoordinate, BlockFunction> blockToCheckEntry : neighbouringBO3HeightMap.entrySet())
                                                    {
                                                		BlockFunction blockToCheck = blockToCheckEntry.getValue();

                                                        blockToCheckCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(blockToCheck.x, blockToCheck.y, blockToCheck.z, bO3ToCheck.getRotation());
                                                        normalizedBlockToCheckX = blockToCheckCoords.getX() + (bO3ToCheck.getX());
                                                        normalizedBlockToCheckY = blockToCheckCoords.getY() + bO3ToCheck.getY();
                                                        normalizedBlockToCheckZ = blockToCheckCoords.getZ() + (bO3ToCheck.getZ());

                                                        if(normalizedNeigbouringBlockX == normalizedBlockToCheckX && (normalizedNeigbouringBlockY == normalizedBlockToCheckY || SmoothStartTop) && normalizedNeigbouringBlockZ == normalizedBlockToCheckZ)
                                                        {
                                                            // Neighbouring block found
                                                        	if(isMaterialSmoothingAnchor(blockToCheck, bO3ToCheck, Start))
                                                            {
                                                                bFoundNeighbour4 = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if(bFoundNeighbour4)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Only blocks that have air blocks or no blocks as neighbours should be part of the smoothing area
                                if(!bFoundNeighbour1 || !bFoundNeighbour2 || !bFoundNeighbour3 || !bFoundNeighbour4)
                                {
                                    // The first block of the smoothing area is placed at a 1 block offset in the direction of the smoothing area so that it is not directly underneath or above the origin block
                                    // for outside corner blocks (blocks with no neighbouring block on 2 adjacent sides) that means they will be placed at x AND z offsets of plus or minus one.
                                    // smoothToBlocks is filled with Object[] { bO3, blockX, blockY, blockZ, smoothInDirection1, smoothInDirection2, smoothInDirection3, smoothInDirection4, smoothRadius }
                                    int xOffset = 0;
                                    int yOffset = 0;
                                    int zOffset = 0;

                            		int smoothHeightOffset = ((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothHeightOffset : bO3InChunk.getSettings().smoothHeightOffset;
                                	yOffset += smoothHeightOffset;

                                    // Shorten diagonal line to make circle x = sin(smoothradius)
                                    // 45 degrees == 0.7853981634 radians

                                    //int a = (((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1) * (((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1);
                                    //int b = (((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1) * (((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1);
                                    //int smoothRadiusRectangleCorner = (int)Math.ceil(Math.sqrt(a + b));
                                    //smoothRadiusRectangleCorner = ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1;
                                    //smoothRadiusRectangleCorner = 10;

                                    //test = true;

                                    // Circle / round corners
                                    int smoothRadius1 = bO3InChunk.getSettings().smoothRadius == -1 ? 0 : (((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothRadius : bO3InChunk.getSettings().smoothRadius) - 1;
                                    int smoothRadius2 = bO3InChunk.getSettings().smoothRadius == -1 ? 0 : (int)Math.ceil(((((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothRadius : bO3InChunk.getSettings().smoothRadius) - 1) * Math.sin(0.7853981634));

                                    // Square / square corners
                                    //int smoothRadius1 = ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1;
                                    //int smoothRadius2 = ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1;

                                    int xOffset1 = 0;
                                    int zOffset1 = 0;

                                    if(!bFoundNeighbour1)
                                    {
                                        xOffset = -1;
                                        CustomObjectCoordinate blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset1, objectInChunk.getRotation());

                                        Object[] smoothDirections = rotateSmoothDirections(true, false, false, false, objectInChunk.getRotation());

                                        smoothToBlocks.add(new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1 });

                                        if(!bFoundNeighbour3)
                                        {
                                            zOffset = -1;
                                            blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                            smoothDirections = rotateSmoothDirections(true, false, true, false, objectInChunk.getRotation());

                                            //PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ bO3InChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], 0,smoothRadius2 });
                                            plotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1,smoothRadius2 }, World);
                                        }
                                        if(!bFoundNeighbour4)
                                        {
                                            zOffset = 1;
                                            blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                            smoothDirections = rotateSmoothDirections(true, false, false, true, objectInChunk.getRotation());

                                            //PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ bO3InChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], 0,smoothRadius2 });
                                            plotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1,smoothRadius2 }, World);
                                        }
                                    }

                                    if(!bFoundNeighbour2)
                                    {
                                        xOffset = 1;
                                        CustomObjectCoordinate blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset1, objectInChunk.getRotation());
                                        Object[] smoothDirections = rotateSmoothDirections(false, true, false, false, objectInChunk.getRotation());

                                        smoothToBlocks.add(new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1 });

                                        if(!bFoundNeighbour3)
                                        {
                                            zOffset = -1;
                                            blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                            smoothDirections = rotateSmoothDirections(false, true, true, false, objectInChunk.getRotation());

                                            //PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ bO3InChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], 0, smoothRadius2 });
                                            plotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1, smoothRadius2 }, World);
                                        }
                                        if(!bFoundNeighbour4)
                                        {
                                            zOffset = 1;
                                            blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                            smoothDirections = rotateSmoothDirections(false, true, false, true, objectInChunk.getRotation());

                                            //PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ bO3InChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], 0, smoothRadius2 });
                                            plotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1, smoothRadius2 }, World);
                                        }
                                    }

                                    if(!bFoundNeighbour3)
                                    {
                                        zOffset = -1;
                                        CustomObjectCoordinate blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                        Object[] smoothDirections = rotateSmoothDirections(false, false, true, false, objectInChunk.getRotation());

                                        smoothToBlocks.add(new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1 });
                                    }
                                    if(!bFoundNeighbour4)
                                    {
                                        zOffset = 1;
                                        CustomObjectCoordinate blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                        Object[] smoothDirections = rotateSmoothDirections(false, false, false, true, objectInChunk.getRotation());

                                        smoothToBlocks.add(new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1 });
                                    }
                                }
                			}
                		}
            		}
                }
            }

            if(!smoothToBlocksPerChunk.containsKey(chunkCoord))
            {
            	smoothToBlocksPerChunk.put(chunkCoord, smoothToBlocks);
            } else {
                // only happens in chunks that have horizontal/vertical lines as well as diagonal ones
            	smoothToBlocksPerChunk.get(chunkCoord).addAll(smoothToBlocks);
            }
        }

        return calculateBeginAndEndPointsPerChunk(smoothToBlocksPerChunk);
    }

    Object[] rotateSmoothDirections(Boolean smoothDirection1, Boolean smoothDirection2, Boolean smoothDirection3, Boolean smoothDirection4, Rotation rotation)
    {
    	// smoothDirection1 -1x WEST
    	// smoothDirection2 +1x EAST
    	// smoothDirection3 -1z NORTH
    	// smoothDirection4 +1z SOUTH
		if(rotation == Rotation.NORTH)
		{
			return new Object[] { smoothDirection1, smoothDirection2, smoothDirection3, smoothDirection4 };
		}
		else if(rotation == Rotation.EAST)
		{
			return new Object[] { smoothDirection4, smoothDirection3, smoothDirection1, smoothDirection2 };
		}
		else if(rotation == Rotation.SOUTH)
		{
			return new Object[] { smoothDirection2, smoothDirection1, smoothDirection4, smoothDirection3 };
		} else {
			return new Object[] { smoothDirection3, smoothDirection4, smoothDirection2, smoothDirection1 };
		}
    }

    private boolean isMaterialSmoothingAnchor(BlockFunction blockToCheck, CustomObjectCoordinate bO3ToCheck, CustomObjectCoordinate Start)
    {
		boolean isSmoothAreaAnchor = false;
		if(blockToCheck instanceof RandomBlockFunction)
		{
			for(LocalMaterialData material : ((RandomBlockFunction)blockToCheck).blocks)
			{
				// TODO: Material should never be null, fix the code in RandomBlockFunction.load() that causes this.
				if(material == null)
				{
					continue;
				}
				if(material.isSmoothAreaAnchor(((BO3)Start.getObject()).getSettings().overrideChildSettings && ((BO3)bO3ToCheck.getObject()).getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothStartWood : ((BO3)bO3ToCheck.getObject()).getSettings().smoothStartWood, ((BO3)Start.getObject()).getSettings().SpawnUnderWater))
				{
					isSmoothAreaAnchor = true;
					break;
				}
			}
		}

        // Neighbouring block found
    	if(
			isSmoothAreaAnchor ||
			(!(blockToCheck instanceof RandomBlockFunction) && blockToCheck.material.isSmoothAreaAnchor(((BO3)Start.getObject()).getSettings().overrideChildSettings && ((BO3)bO3ToCheck.getObject()).getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothStartWood : ((BO3)bO3ToCheck.getObject()).getSettings().smoothStartWood, ((BO3)Start.getObject()).getSettings().SpawnUnderWater))
		)
    	{
    		return true;
    	}
    	return false;
    }

    private void plotDiagonalLine(Map<ChunkCoordinate, ArrayList<Object[]>> smoothToBlocksPerChunk, Object[] blockCoordsAndNeighbours, LocalWorld World)
    {
        Map<ChunkCoordinate, ArrayList<Object[]>> smoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesn't exactly
        // make the code any easier to read..

        int normalizedSmoothFinalEndPointBlockX1;
        int normalizedSmoothFinalEndPointBlockY1 = -1;
        int normalizedSmoothFinalEndPointBlockZ1;
        int normalizedSmoothEndPointBlockX;
        int normalizedSmoothEndPointBlockZ;
        ChunkCoordinate destinationChunk;
        int beginPointX;
        int beginPointY;
        int beginPointZ;
        ArrayList<Object[]> beginningAndEndpoints;
        ChunkCoordinate chunkcontainingSmoothArea;
        ArrayList<Object[]> beginAndEndPoints;

        CustomObjectCoordinate bO3 = (CustomObjectCoordinate)blockCoordsAndNeighbours[0];
        int blockX = (Integer)blockCoordsAndNeighbours[1];
        int blockY = (Integer)blockCoordsAndNeighbours[2];
        int blockZ = (Integer)blockCoordsAndNeighbours[3];
        boolean smoothInDirection1 = (Boolean)blockCoordsAndNeighbours[4];
        boolean smoothInDirection2 = (Boolean)blockCoordsAndNeighbours[5];
        boolean smoothInDirection3 = (Boolean)blockCoordsAndNeighbours[6];
        boolean smoothInDirection4 = (Boolean)blockCoordsAndNeighbours[7];
        int smoothRadius = (Integer)blockCoordsAndNeighbours[8];
       	int smoothRadiusDiagonal = (Integer)blockCoordsAndNeighbours[9];

        // Find smooth end point and normalize coord
        // Add each chunk between the smooth-beginning and end points to a list along with the line-segment information (startcoords in chunk, endcoords in chunk, originCoords, finaldestinationCoords)
        // Later when a chunk is being spawned the list is consulted in order to merge all smoothing lines into 1 smoothing area for the chunk.
        // Note: Unfortunately we can only find x and z coordinates for the smoothing lines at this point. In order to find the Y endpoint
        // for a smoothing line we need the landscape to be spawned so that we can find the highest solid block in the landscape.
        // This problem is handled later during spawning, if the Y endpoint for a smoothing line in a chunk is not available when that chunk
        // is being spawned (because the endpoint is in a neighbouring chunk that has not yet been spawned) then all spawning for the chunk is paused until the Y endpoint is available (the neighbouring chunk has spawned).

        // If this block is an outer corner block (it has the smoothInDirection boolean set to true for 2 neighbouring sides)
        if(smoothInDirection1 && smoothInDirection3)// && (smoothRadiusDiagonal > 0 || test))
        {
        	normalizedSmoothFinalEndPointBlockX1 = blockX - smoothRadiusDiagonal + (bO3.getX());
        	normalizedSmoothFinalEndPointBlockZ1 = blockZ - smoothRadiusDiagonal + (bO3.getZ());
            // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas

            beginPointX = blockX + bO3.getX();
            beginPointY = blockY + bO3.getY();
            beginPointZ = blockZ + bO3.getZ();

            // First get all chunks between the beginning- and end-points
            for(int i = 0; i <= smoothRadiusDiagonal; i++)
            {
            	normalizedSmoothEndPointBlockX = blockX - i + (bO3.getX());
                normalizedSmoothEndPointBlockZ = blockZ - i + (bO3.getZ());
                destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

            	ChunkCoordinate nextBlocksChunkCoord = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX - 1, normalizedSmoothEndPointBlockZ - 1);

            	// only store the line once it's traversed an entire chunk or is at smoothRadiusDiagonal
            	if(!destinationChunk.equals(nextBlocksChunkCoord) || i == smoothRadiusDiagonal)
            	{
                    beginningAndEndpoints = new ArrayList<Object[]> ();
                    beginningAndEndpoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });

                    // Check if there are already start and endpoints for this chunk
                    for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                    {
                        chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                        if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                        {
                            beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                            beginAndEndPoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });
                            break;
                        }
                    }
                    smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);

                    // Get the coordinates of the beginning point for the next entry
                    beginPointX = normalizedSmoothEndPointBlockX - 1;
                    beginPointZ = normalizedSmoothEndPointBlockZ - 1;
            	}
            }
        }
        if(smoothInDirection1 && smoothInDirection4)// && (smoothRadiusDiagonal > 0 || test))
        {
        	normalizedSmoothFinalEndPointBlockX1 = blockX - smoothRadiusDiagonal + (bO3.getX());
        	normalizedSmoothFinalEndPointBlockZ1 = blockZ + smoothRadiusDiagonal + (bO3.getZ());
            // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas

            beginPointX = blockX + bO3.getX();
            beginPointY = blockY + bO3.getY();
            beginPointZ = blockZ + bO3.getZ();

            // First get all chunks between the beginning- and end-points
            for(int i = 0; i <= smoothRadiusDiagonal; i++)
            {
            	normalizedSmoothEndPointBlockX = blockX - i + (bO3.getX());
                normalizedSmoothEndPointBlockZ = blockZ + i + (bO3.getZ());
                destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

            	ChunkCoordinate nextBlocksChunkCoord = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX - 1, normalizedSmoothEndPointBlockZ + 1);

            	// only store the line once it's traversed an entire chunk or is at smoothRadiusDiagonal
            	if(!destinationChunk.equals(nextBlocksChunkCoord) || i == smoothRadiusDiagonal)
            	{
                    beginningAndEndpoints = new ArrayList<Object[]> ();
                    beginningAndEndpoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });

                    // Check if there are already start and endpoints for this chunk
                    for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                    {
                        chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                        if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                        {
                            beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                            beginAndEndPoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });
                            break;
                        }
                    }
                    smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);

                    // Get the coordinates of the beginning point for the next entry
                    beginPointX = normalizedSmoothEndPointBlockX - 1;
                    beginPointZ = normalizedSmoothEndPointBlockZ + 1;
            	}
            }
        }
        if(smoothInDirection2 && smoothInDirection3)// && (smoothRadiusDiagonal > 0 || test))
        {
        	normalizedSmoothFinalEndPointBlockX1 = blockX + smoothRadiusDiagonal + (bO3.getX());
        	normalizedSmoothFinalEndPointBlockZ1 = blockZ - smoothRadiusDiagonal + (bO3.getZ());
            // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas

            beginPointX = blockX + bO3.getX();
            beginPointY = blockY + bO3.getY();
            beginPointZ = blockZ + bO3.getZ();

            // First get all chunks between the beginning- and end-points
            for(int i = 0; i <= smoothRadiusDiagonal; i++)
            {
            	normalizedSmoothEndPointBlockX = blockX + i + (bO3.getX());
                normalizedSmoothEndPointBlockZ = blockZ - i + (bO3.getZ());
                destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

            	ChunkCoordinate nextBlocksChunkCoord = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX + 1, normalizedSmoothEndPointBlockZ - 1);

            	// only store the line once it's traversed an entire chunk or is at smoothRadiusDiagonal
            	if(!destinationChunk.equals(nextBlocksChunkCoord) || i == smoothRadiusDiagonal)
            	{
                    beginningAndEndpoints = new ArrayList<Object[]> ();
                    beginningAndEndpoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });

                    // Check if there are already start and endpoints for this chunk
                    for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                    {
                        chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                        if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                        {
                            beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                            beginAndEndPoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });
                            break;
                        }
                    }
                    smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);

                    // Get the coordinates of the beginning point for the next entry
                    beginPointX = normalizedSmoothEndPointBlockX + 1;
                    beginPointZ = normalizedSmoothEndPointBlockZ - 1;
            	}
            }
        }
        if(smoothInDirection2 && smoothInDirection4)// && (smoothRadiusDiagonal > 0 || test))
        {
        	normalizedSmoothFinalEndPointBlockX1 = blockX + smoothRadiusDiagonal + (bO3.getX());
        	normalizedSmoothFinalEndPointBlockZ1 = blockZ + smoothRadiusDiagonal + (bO3.getZ());
            // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas

            beginPointX = blockX + bO3.getX();
            beginPointY = blockY + bO3.getY();
            beginPointZ = blockZ + bO3.getZ();

            // First get all chunks between the beginning- and end-points
            for(int i = 0; i <= smoothRadiusDiagonal; i++)
            {
            	normalizedSmoothEndPointBlockX = blockX + i + (bO3.getX());
                normalizedSmoothEndPointBlockZ = blockZ + i + (bO3.getZ());
                destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

            	ChunkCoordinate nextBlocksChunkCoord = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX + 1, normalizedSmoothEndPointBlockZ + 1);

            	// only store the line once it's traversed an entire chunk or is at smoothRadiusDiagonal
            	if(!destinationChunk.equals(nextBlocksChunkCoord) || i == smoothRadiusDiagonal)
            	{
                    beginningAndEndpoints = new ArrayList<Object[]> ();
                    beginningAndEndpoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });

                    // Check if there are already start and endpoints for this chunk
                    for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                    {
                        chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                        if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                        {
                            beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                            beginAndEndPoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });
                            break;
                        }
                    }
                    smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);

                    // Get the coordinates of the beginning point for the next entry
                    beginPointX = normalizedSmoothEndPointBlockX + 1;
                    beginPointZ = normalizedSmoothEndPointBlockZ + 1;
            	}
            }
        }

        // Now use each block we've just plotted as the start point for a new line

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesnt exactly
        // make the code any easier to read..
        int distanceFromStart;
        BlockFunction beginPoint;
        int originPointX;
        int originPointY;
        int originPointZ;
        int finalDestinationPointX;
        int finalDestinationPointY;
        int finalDestinationPointZ;
        BlockFunction endPoint;
        BlockFunction filler;
        ArrayList<Object[]> smoothToBlocks;

        int diagonalBlockSmoothRadius = 0;
        int diagonalBlockSmoothRadius2 = 0;

        for(Entry<ChunkCoordinate, ArrayList<Object[]>> smoothingAreaInChunk : smoothingAreasToSpawn.entrySet())
        {
	        for(Object[] smoothingBeginAndEndPoints : smoothingAreaInChunk.getValue())
	        {
	            distanceFromStart = 0;

	            beginPoint = new BlockFunction();
	            beginPoint.x = (Integer)smoothingBeginAndEndPoints[0];
	            beginPoint.y = (Integer)smoothingBeginAndEndPoints[1];
	            beginPoint.z = (Integer)smoothingBeginAndEndPoints[2];

                endPoint = new BlockFunction();
                endPoint.x = (Integer)smoothingBeginAndEndPoints[3];
                endPoint.y = (Integer)smoothingBeginAndEndPoints[4];
                endPoint.z = (Integer)smoothingBeginAndEndPoints[5];

	            originPointX = (Integer)smoothingBeginAndEndPoints[6];
	            originPointY = (Integer)smoothingBeginAndEndPoints[7];
	            originPointZ = (Integer)smoothingBeginAndEndPoints[8];

	            finalDestinationPointX = (Integer)smoothingBeginAndEndPoints[9];
	            finalDestinationPointY = (Integer)smoothingBeginAndEndPoints[10];
	            finalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[11];

                diagonalBlockSmoothRadius = smoothRadius;

	        	distanceFromStart = Math.abs(beginPoint.x - originPointX);

	            // Corners call this method for every diagonal block that makes up the corner, every diagonal block spawns
	        	// its own child blocks seperately in x and y directions which creates the shape of the corner
	            for(int i = 0; i <= Math.abs((beginPoint.z) - (endPoint.z)); i++)
	            {
	                filler = new BlockFunction();
	                if(smoothInDirection2)
	                {
	                    filler.x = beginPoint.x + i;
	                }
	                if(smoothInDirection1)
	                {
	                    filler.x = beginPoint.x - i;
	                }
	                if(smoothInDirection4)
	                {
	                    filler.z = beginPoint.z + i;
	                }
	                if(smoothInDirection3)
	                {
	                    filler.z = beginPoint.z - i;
	                }
	                filler.y = beginPoint.y;

	                smoothToBlocks = new ArrayList<Object[]>();
	                bO3 = new CustomObjectCoordinate(World, null, null, null, 0, 0, 0, false, 0, false, false, null);

	        		// While drawing a circle:
	        		// x^2 + y^2 = r^2
	        		// so y^2 = r^2 - x^2

	                // Circle / round corners
	                diagonalBlockSmoothRadius2 = (int)Math.round(Math.sqrt((diagonalBlockSmoothRadius * diagonalBlockSmoothRadius) - ((distanceFromStart + i) * (distanceFromStart + i))) - (distanceFromStart + i));

	                // Square / square corners
	                //diagonalBlockSmoothRadius2 = diagonalBlockSmoothRadius - (distanceFromStart + i);

	                destinationChunk = ChunkCoordinate.fromBlockCoords(beginPoint.x, endPoint.x);

	                //smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, false, false, false, false, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });

	                if(smoothInDirection1)
	                {
	                	smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, true, false, false, false, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });
	                }
	                if(smoothInDirection2)
	                {
	                	smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, false, true, false, false, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });
	                }
	                if(smoothInDirection3)
	                {
	                	smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, false, false, true, false, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });
	                }
	                if(smoothInDirection4)
	                {
	                	smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, false, false, false, true, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });
	                }

                    if(!smoothToBlocksPerChunk.containsKey(destinationChunk))
                    {
                    	smoothToBlocksPerChunk.put(destinationChunk, smoothToBlocks);
                    } else {
                        // only happens in chunks that have horizontal/vertical lines as well as diagonal ones
                    	smoothToBlocksPerChunk.get(destinationChunk).addAll(smoothToBlocks);
                    }
	            }
	        }
        }
    }

    // We've determined starting points, smooth direction and smooth radius for lines that will form a smoothing area, now find the end point for each line depending on the smoothradius and smooth direction.
    // The lines we plot this way may traverse several chunks so divide them up into segments of one chunk and make a collection of line segments per chunk.
    // For each line-segment store the beginning and endpoints within the chunk as well as the origin coordinate of the line and the final destination coordinate of the line
    // we spawn the line-segments per chunk we can still see what the completed line looks like and how far along that line we are.
    private Map<ChunkCoordinate, ArrayList<Object[]>> calculateBeginAndEndPointsPerChunk(Map<ChunkCoordinate, ArrayList<Object[]>> smoothToBlocksPerChunk)
    {
        Map<ChunkCoordinate, ArrayList<Object[]>> smoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesn't exactly
        // make the code any easier to read..
        CustomObjectCoordinate bO3;
        int blockX;
        int blockY;
        int blockZ;
        boolean smoothInDirection1;
        boolean smoothInDirection2;
        boolean smoothInDirection3;
        boolean smoothInDirection4;
        int smoothRadius;
        int normalizedSmoothFinalEndPointBlockX1;
        int normalizedSmoothFinalEndPointBlockY1;
        int normalizedSmoothFinalEndPointBlockZ1;
        ChunkCoordinate finalDestinationChunk;
        ArrayList<ChunkCoordinate>smoothingAreasToSpawnForThisBlock;
        int normalizedSmoothEndPointBlockX;
        int normalizedSmoothEndPointBlockY;
        int normalizedSmoothEndPointBlockZ;
        ChunkCoordinate destinationChunk;
        boolean bFound;
        int beginPointX;
        int beginPointY;
        int beginPointZ;
        int endPointX;
        int endPointY;
        int endPointZ;
        ArrayList<Object[]> beginningAndEndpoints;
        ChunkCoordinate chunkcontainingSmoothArea;
        ArrayList<Object[]> beginAndEndPoints;

        int originPointX = 0;
		int originPointY = 0;
		int originPointZ = 0;
		int finalDestinationPointX = 0;
		int finalDestinationPointY = 0;
		int finalDestinationPointZ = 0;
        Object[] objectToAdd;

        // Loop through smooth-line starting blocks
        for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkCoordSet : smoothToBlocksPerChunk.entrySet())
        {
            for(Object[] blockCoordsAndNeighbours : chunkCoordSet.getValue())
            {
                bO3 = (CustomObjectCoordinate)blockCoordsAndNeighbours[0];
                blockX = (Integer)blockCoordsAndNeighbours[1];
                blockY = (Integer)blockCoordsAndNeighbours[2];
                blockZ = (Integer)blockCoordsAndNeighbours[3];
                smoothInDirection1 = (Boolean)blockCoordsAndNeighbours[4];
                smoothInDirection2 = (Boolean)blockCoordsAndNeighbours[5];
                smoothInDirection3 = (Boolean)blockCoordsAndNeighbours[6];
                smoothInDirection4 = (Boolean)blockCoordsAndNeighbours[7];
                smoothRadius = (Integer)blockCoordsAndNeighbours[8];

                // used for diagonal line child lines that make up corners
                if(blockCoordsAndNeighbours.length > 14)
                {
	                originPointX = (Integer)blockCoordsAndNeighbours[9];
	        		originPointY = (Integer)blockCoordsAndNeighbours[10];
					originPointZ = (Integer)blockCoordsAndNeighbours[11];
					finalDestinationPointX = (Integer)blockCoordsAndNeighbours[12];
					finalDestinationPointY = (Integer)blockCoordsAndNeighbours[13];
					finalDestinationPointZ = (Integer)blockCoordsAndNeighbours[14];
                }

                // Find smooth end point and normalize coord
                // Add each chunk between the smooth-beginning and end points to a list along with the line-segment information (startcoords in chunk, endcoords in chunk, originCoords, finaldestinationCoords)
                // Later when a chunk is being spawned the list is consulted in order to merge all smoothing lines into 1 smoothing area for the chunk.
                // Note: Unfortunately we can only find x and z coordinates for the smoothing lines at this point. In order to find the Y endpoint
                // for a smoothing line we need the landscape to be spawned so that we can find the highest solid block in the landscape.
                // This problem is handled later during spawning, if the Y endpoint for a smoothing line in a chunk is not available when that chunk
                // is being spawned (because the endpoint is in a neighbouring chunk that has not yet been spawned) then all spawning for the chunk is paused until the Y endpoint is available (the neighbouring chunk has spawned).

                if(smoothRadius == 0 && blockCoordsAndNeighbours.length < 15)
                {
                	//throw new RuntimeException();
                }

                // If this block is a non-outer-corner block (it does not have the smoothInDirection boolean set to true for 2 neighbouring sides)
                if(smoothInDirection1)// && (smoothRadius > 0 || test || blockCoordsAndNeighbours.length > 14))
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX - smoothRadius + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;

                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                    for(int i = 0; i <= smoothRadius; i++)
                    {
                    	normalizedSmoothEndPointBlockX = blockX - i + (bO3.getX());
                        normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                        normalizedSmoothEndPointBlockZ = blockZ + (bO3.getZ());
                        destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                        // Check if we havent handled this chunk yet for the current line
                        bFound = false;
                        for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                        {
                            if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                            {
                                bFound = true;
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                            beginPointX = normalizedSmoothEndPointBlockX;
                            beginPointY = normalizedSmoothEndPointBlockY;
                            beginPointZ = normalizedSmoothEndPointBlockZ;

                            endPointX = normalizedSmoothEndPointBlockX;
                            endPointY = normalizedSmoothEndPointBlockY;
                            endPointZ = normalizedSmoothEndPointBlockZ;

                            if(finalDestinationChunk.getChunkX() != destinationChunk.getChunkX() || finalDestinationChunk.getChunkZ() != destinationChunk.getChunkZ())
                            {
                                // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (0 because we're moving in the - direction)
                                endPointX = destinationChunk.getChunkX() * 16;
                            } else {
                                // Get the endpoint by adding the remaining smoothRadius
                                endPointX = normalizedSmoothEndPointBlockX -= (smoothRadius - i);
                            }

                            beginningAndEndpoints = new ArrayList<Object[]> ();
                            if(blockCoordsAndNeighbours.length > 14)
                            {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                            } else {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                            }

                        	beginningAndEndpoints.add(objectToAdd);

                            // Check if there are already start and endpoints for this chunk
                            for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                            {
                                chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                                if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                                {
                                    bFound = true;
                                    beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                    beginAndEndPoints.add(objectToAdd);
                                    break;
                                }
                            }
                            if(!bFound)
                            {
                                smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                            }
                            smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                        }
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }
                if(smoothInDirection2)// && (smoothRadius > 0 || test || blockCoordsAndNeighbours.length > 14))
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX + smoothRadius + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;
                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                    for(int i = 0; i <= smoothRadius; i++)
                    {
                    	normalizedSmoothEndPointBlockX = blockX + i + (bO3.getX());
                        normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                        normalizedSmoothEndPointBlockZ = blockZ + (bO3.getZ());
                        destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                        // Check if we havent handled this chunk yet for the current line
                        bFound = false;
                        for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                        {
                            if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                            {
                                bFound = true;
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                            beginPointX = normalizedSmoothEndPointBlockX;
                            beginPointY = normalizedSmoothEndPointBlockY;
                            beginPointZ = normalizedSmoothEndPointBlockZ;

                            endPointX = normalizedSmoothEndPointBlockX;
                            endPointY = normalizedSmoothEndPointBlockY;
                            endPointZ = normalizedSmoothEndPointBlockZ;

                            if(finalDestinationChunk.getChunkX() != destinationChunk.getChunkX() || finalDestinationChunk.getChunkZ() != destinationChunk.getChunkZ())
                            {
                                // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (15 because we're moving in the + direction)
                                endPointX = destinationChunk.getChunkX() * 16 + 15;
                            } else {
                                // Get the endpoint by adding the remaining smoothRadius
                                endPointX = normalizedSmoothEndPointBlockX += (smoothRadius - i);
                            }

                            beginningAndEndpoints = new ArrayList<Object[]> ();
                            if(blockCoordsAndNeighbours.length > 14)
                            {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                            } else {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                            }

                        	beginningAndEndpoints.add(objectToAdd);

                            // Check if there are already start and endpoints for this chunk
                            for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                            {
                                chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                                if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                                {
                                    bFound = true;
                                    beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                    beginAndEndPoints.add(objectToAdd);
                                    break;
                                }
                            }
                            if(!bFound)
                            {
                            	smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                            }
                            smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                        }
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }
                if(smoothInDirection3)// && (smoothRadius > 0 || test || blockCoordsAndNeighbours.length > 14))
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ - smoothRadius + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;
                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                    for(int i = 0; i <= smoothRadius; i++)
                    {
                    	normalizedSmoothEndPointBlockX = blockX + (bO3.getX());
                        normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                        normalizedSmoothEndPointBlockZ = blockZ - i + (bO3.getZ());
                        destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                        // Check if we havent handled this chunk yet for the current line
                        bFound = false;
                        for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                        {
                            if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                            {
                                bFound = true;
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                            beginPointX = normalizedSmoothEndPointBlockX;
                            beginPointY = normalizedSmoothEndPointBlockY;
                            beginPointZ = normalizedSmoothEndPointBlockZ;

                            endPointX = normalizedSmoothEndPointBlockX;
                            endPointY = normalizedSmoothEndPointBlockY;
                            endPointZ = normalizedSmoothEndPointBlockZ;

                            if(finalDestinationChunk.getChunkX() != destinationChunk.getChunkX() || finalDestinationChunk.getChunkZ() != destinationChunk.getChunkZ())
                            {
                                // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (0 because we're moving in the - direction)
                                endPointZ = destinationChunk.getChunkZ() * 16;
                            } else {
                                // Get the endpoint by adding the remaining smoothRadius
                                endPointZ = normalizedSmoothEndPointBlockZ -= (smoothRadius - i);
                            }

                            beginningAndEndpoints = new ArrayList<Object[]> ();
                            if(blockCoordsAndNeighbours.length > 14)
                            {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                            } else {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                            }

                        	beginningAndEndpoints.add(objectToAdd);

                            // Check if there are already start and endpoints for this chunk
                            for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                            {
                                chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                                if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                                {
                                    bFound = true;
                                    beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                    beginAndEndPoints.add(objectToAdd);
                                    break;
                                }
                            }
                            if(!bFound)
                            {
                            	smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                            }
                            smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                        }
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }
                if(smoothInDirection4)// && (smoothRadius > 0 || test || blockCoordsAndNeighbours.length > 14))
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ + smoothRadius + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;

                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                    for(int i = 0; i <= smoothRadius; i++)
                    {
                    	normalizedSmoothEndPointBlockX = blockX + (bO3.getX());
                        normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                        normalizedSmoothEndPointBlockZ = blockZ + i + (bO3.getZ());
                        destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                        // Check if we havent handled this chunk yet for the current line
                        bFound = false;
                        for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                        {
                            if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                            {
                                bFound = true;
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                            beginPointX = normalizedSmoothEndPointBlockX;
                            beginPointY = normalizedSmoothEndPointBlockY;
                            beginPointZ = normalizedSmoothEndPointBlockZ;

                            endPointX = normalizedSmoothEndPointBlockX;
                            endPointY = normalizedSmoothEndPointBlockY;
                            endPointZ = normalizedSmoothEndPointBlockZ;

                            if(finalDestinationChunk.getChunkX() != destinationChunk.getChunkX() || finalDestinationChunk.getChunkZ() != destinationChunk.getChunkZ())
                            {
                                // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (15 because we're moving in the + direction)
                                endPointZ = destinationChunk.getChunkZ() * 16 + 15;
                            } else {
                                // Get the endpoint by adding the remaining smoothRadius
                                endPointZ = normalizedSmoothEndPointBlockZ += (smoothRadius - i);
                            }

                            beginningAndEndpoints = new ArrayList<Object[]> ();
                            if(blockCoordsAndNeighbours.length > 14)
                            {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                            } else {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                            }

                        	beginningAndEndpoints.add(objectToAdd);

                            // Check if there are already start and endpoints for this chunk
                            for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                            {
                                chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                                if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                                {
                                    bFound = true;
                                    beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                    beginAndEndPoints.add(objectToAdd);
                                    break;
                                }
                            }
                            if(!bFound)
                            {
                                smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                            }
                            smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                        }
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }

                if(!smoothInDirection1 && !smoothInDirection2 && !smoothInDirection3 && !smoothInDirection4)
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;

                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                	normalizedSmoothEndPointBlockX = blockX + (bO3.getX());
                    normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                    normalizedSmoothEndPointBlockZ = blockZ + (bO3.getZ());
                    destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                    // Check if we havent handled this chunk yet for the current line
                    bFound = false;
                    for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                    {
                        if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                        {
                            bFound = true;
                            break;
                        }
                    }
                    if(!bFound)
                    {
                        // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                        beginPointX = normalizedSmoothEndPointBlockX;
                        beginPointY = normalizedSmoothEndPointBlockY;
                        beginPointZ = normalizedSmoothEndPointBlockZ;

                        endPointX = normalizedSmoothEndPointBlockX;
                        endPointY = normalizedSmoothEndPointBlockY;
                        endPointZ = normalizedSmoothEndPointBlockZ;

                        beginningAndEndpoints = new ArrayList<Object[]> ();
                        if(blockCoordsAndNeighbours.length > 14)
                        {
                        	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                        } else {
                        	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                        }

                        if(normalizedSmoothFinalEndPointBlockY1 != -1)
                        {
                        	throw new RuntimeException();
                        }

                    	beginningAndEndpoints.add(objectToAdd);

                        // Check if there are already start and endpoints for this chunk
                        for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                        {
                            chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                            if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                            {
                                bFound = true;
                                beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                beginAndEndPoints.add(objectToAdd);
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                        }
                        smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }
            }
        }
        return smoothingAreasToSpawn;
    }
    
    /**
     * Merges all the smoothing lines that were plotted earlier into one
     * smoothing area per chunk and then spawns the smoothing area.
     * Returns false if a smoothing area could not be finalised
     * and spawning has to be delayed until other chunks have spawned
     * @param chunkCoordinate
    */
    public boolean spawnSmoothAreas(ChunkCoordinate chunkCoordinate, Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawn, CustomObjectCoordinate Start, LocalWorld World)
    {
        // Get all smoothing areas (lines) that should spawn in this chunk for this branching structure
        Entry<ChunkCoordinate, ArrayList<Object[]>> smoothingAreaInChunk = null;
        for(Entry<ChunkCoordinate, ArrayList<Object[]>> smoothingAreaToSpawn : SmoothingAreasToSpawn.entrySet())
        {
            if(smoothingAreaToSpawn.getKey().getChunkX() == chunkCoordinate.getChunkX() && smoothingAreaToSpawn.getKey().getChunkZ() == chunkCoordinate.getChunkZ())
            {
                smoothingAreaInChunk = smoothingAreaToSpawn;
                break;
            }
        }

        if(smoothingAreaInChunk != null && smoothingAreaInChunk.getValue() != null)
        {
            // Merge all smooth areas (lines) so that in one x + z coordinate there can be a maximum of 2 smoothing area blocks, 1 going up and 1 going down (first pass and second pass)
            ArrayList<Object[]> blocksToSpawn = mergeSmoothingAreas(chunkCoordinate, smoothingAreaInChunk.getValue(), World, Start);

            // blocksToSpawn can be null if a smoothing line's endpoint Y coordinate could not be found. This can happen if
            // the chunk that the endpoint is located in has not yet been spawned. Return false so that the calling method (SpawnForChunk()) knows
            // that it should delay spawning for this chunk and try again later.
            if(blocksToSpawn == null) { return false; }

        	boolean isOnBiomeBorder = false;

        	LocalBiome biome = World.getBiome(chunkCoordinate.getChunkX() * 16, chunkCoordinate.getChunkZ() * 16);
        	LocalBiome biome2 = World.getBiome(chunkCoordinate.getChunkX() * 16 + 15, chunkCoordinate.getChunkZ() * 16);
        	LocalBiome biome3 = World.getBiome(chunkCoordinate.getChunkX() * 16, chunkCoordinate.getChunkZ() * 16 + 15);
        	LocalBiome biome4 = World.getBiome(chunkCoordinate.getChunkX() * 16 + 15, chunkCoordinate.getChunkZ() * 16 + 15);

            if(!(biome == biome2 && biome == biome3 && biome == biome4))
            {
            	isOnBiomeBorder = true;
            }

            BiomeConfig biomeConfig = biome.getBiomeConfig();

            DefaultMaterial surfaceBlockMaterial = biomeConfig.surfaceBlock.toDefaultMaterial();
            byte surfaceBlockMaterialBlockData = biomeConfig.surfaceBlock.getBlockData();
            DefaultMaterial groundBlockMaterial = biomeConfig.groundBlock.toDefaultMaterial();
            byte groundBlockMaterialBlockData = biomeConfig.groundBlock.getBlockData();

            boolean surfaceBlockSet = false;
			if(((BO3)Start.getObject()).getSettings().smoothingSurfaceBlock != null && ((BO3)Start.getObject()).getSettings().smoothingSurfaceBlock.trim().length() > 0)
			{
				try {
					LocalMaterialData material = OTG.readMaterial(((BO3)Start.getObject()).getSettings().smoothingSurfaceBlock);
					surfaceBlockSet = true;
					surfaceBlockMaterial = material.toDefaultMaterial();
					surfaceBlockMaterialBlockData = material.getBlockData();
				}
				catch (InvalidConfigException e)
				{
					e.printStackTrace();
				}
			}
            boolean groundBlockSet = false;
			if(((BO3)Start.getObject()).getSettings().smoothingGroundBlock != null && ((BO3)Start.getObject()).getSettings().smoothingGroundBlock.trim().length() > 0)
			{
				try
				{
					LocalMaterialData material = OTG.readMaterial(((BO3)Start.getObject()).getSettings().smoothingGroundBlock);
					groundBlockSet = true;
					groundBlockMaterial = material.toDefaultMaterial();
					groundBlockMaterialBlockData = material.getBlockData();
				}
				catch (InvalidConfigException e)
				{
					e.printStackTrace();
				}
			}

            if(surfaceBlockMaterial == null || surfaceBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
            {
            	surfaceBlockMaterial = DefaultMaterial.GRASS;
            	surfaceBlockMaterialBlockData = 0;
            }

            if(groundBlockMaterial == null || groundBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
            {
            	groundBlockMaterial = DefaultMaterial.DIRT;
            	groundBlockMaterialBlockData = 0;
            }

            DefaultMaterial replaceAboveMaterial = null;
            byte replaceAboveMaterialBlockData = 0;
            DefaultMaterial replaceBelowMaterial = null;
			if(((BO3)Start.getObject()).getSettings().replaceAbove != null && ((BO3)Start.getObject()).getSettings().replaceAbove.trim().length() > 0)
			{
				try
				{
					LocalMaterialData material = OTG.readMaterial(((BO3)Start.getObject()).getSettings().replaceAbove);
					replaceAboveMaterial = material.toDefaultMaterial();
					replaceAboveMaterialBlockData = material.getBlockData();
				}
				catch (InvalidConfigException e)
				{
					e.printStackTrace();
				}
			}
			if(((BO3)Start.getObject()).getSettings().replaceBelow != null && ((BO3)Start.getObject()).getSettings().replaceBelow.trim().length() > 0)
			{
				try
				{
					LocalMaterialData material = OTG.readMaterial(((BO3)Start.getObject()).getSettings().replaceBelow);
					replaceBelowMaterial = material.toDefaultMaterial();
				}
				catch (InvalidConfigException e)
				{
					e.printStackTrace();
				}
			}

            if(replaceAboveMaterial == null || replaceAboveMaterial == DefaultMaterial.UNKNOWN_BLOCK)
            {
            	replaceAboveMaterial = null;
            	replaceAboveMaterialBlockData = 0;
            }

            if(replaceBelowMaterial == null || replaceBelowMaterial == DefaultMaterial.UNKNOWN_BLOCK)
            {
            	replaceBelowMaterial = null;
            }

            // Declare these here instead of inside for loops to help the GC (good for memory usage)
            // TODO: Find out if this actually makes any noticeable difference, it doesnt exactly
            // make the code any easier to read..
            BlockFunction blockToSpawn;
            boolean goingUp;
            boolean secondPass;
            LocalMaterialData sourceBlockMaterial;
            DefaultMaterial sourceBlockMaterialAbove;
            DefaultMaterial materialToSet = null;
            Byte blockDataToSet = 0;
            boolean bBreak;
            int yStart;
            int yEnd;
            BlockFunction blockToQueueForSpawn = new BlockFunction();


            HashMap<ChunkCoordinate, LocalMaterialData> originalTopBlocks = new HashMap<ChunkCoordinate, LocalMaterialData>();

            // Spawn blocks
            // For each block in the smoothing area replace blocks above and/or below it
            for(Object[] blockItemToSpawn : blocksToSpawn)
            {
                blockToSpawn = (BlockFunction)blockItemToSpawn[0];
                goingUp = (Boolean)blockItemToSpawn[1];
                secondPass =  (Boolean)blockItemToSpawn[3];

                if(blockToSpawn.y > 255)
                {
                	continue; // TODO: prevent this from ever happening!
                }

            	if(!originalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToSpawn.x, blockToSpawn.z)))
            	{
        			int highestBlockY = World.getHighestBlockYAt(blockToSpawn.x, blockToSpawn.z, true, true, false, false);
        			if(highestBlockY > PluginStandardValues.WORLD_DEPTH)
        			{
        				originalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToSpawn.x, blockToSpawn.z), World.getMaterial(blockToSpawn.x, highestBlockY, blockToSpawn.z, true));
        			} else {
        				originalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToSpawn.x, blockToSpawn.z), null);
        			}
            	}

                if(isOnBiomeBorder && (!surfaceBlockSet || !groundBlockSet))
                {
	                biome = World.getBiome(blockToSpawn.x, blockToSpawn.z);
	                biomeConfig = biome.getBiomeConfig();

	                if(!surfaceBlockSet)
	                {
		                surfaceBlockMaterial = biomeConfig.surfaceBlock.toDefaultMaterial();
		                surfaceBlockMaterialBlockData = biomeConfig.surfaceBlock.getBlockData();

		                if(surfaceBlockMaterial == null || surfaceBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
		                {
		                	surfaceBlockMaterial = DefaultMaterial.GRASS;
		                	surfaceBlockMaterialBlockData = 0;
		                }
	                }

	                if(!groundBlockSet)
	                {
		                groundBlockMaterial = biomeConfig.groundBlock.toDefaultMaterial();
		                groundBlockMaterialBlockData = biomeConfig.groundBlock.getBlockData();

		                if(groundBlockMaterial == null || groundBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
		                {
		                	groundBlockMaterial = DefaultMaterial.DIRT;
		                	groundBlockMaterialBlockData = 0;
		                }
	                }
                }

                // If using the biome's surfaceblock then take what was previously the top
                // block and use it's material as the surface block (solves no podzol problem in mega spruce taiga)
                if(
            		!surfaceBlockSet &&
					!(biomeConfig.surfaceAndGroundControl instanceof MesaSurfaceGenerator)
        		)
            	{
        			LocalMaterialData originalSurfaceBlock = originalTopBlocks.get(ChunkCoordinate.fromChunkCoords(blockToSpawn.x, blockToSpawn.z));
        			if(originalSurfaceBlock == null || originalSurfaceBlock.isLiquid() || originalSurfaceBlock.isAir())
        			{
    	                surfaceBlockMaterial = biomeConfig.surfaceBlock.toDefaultMaterial();
    	                surfaceBlockMaterialBlockData = biomeConfig.surfaceBlock.getBlockData();
        			} else {
        				surfaceBlockMaterial = originalSurfaceBlock.toDefaultMaterial();
        				surfaceBlockMaterialBlockData = originalSurfaceBlock.getBlockData();
        			}

                    if(surfaceBlockMaterial == null || surfaceBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
                    {
                    	surfaceBlockMaterial = DefaultMaterial.GRASS;
                    	surfaceBlockMaterialBlockData = 0;
                    }
            	}

                bBreak = false;
                // When going down make a hill for the BO3 to stand on
				if(!goingUp)
				{
					yStart = blockToSpawn.y;
					yEnd = 0;
					for(int y = yStart; y > yEnd; y--)
					{
						if(y >= 255){ continue;}

						sourceBlockMaterial = World.getMaterial(blockToSpawn.x, y, blockToSpawn.z, true);
	                    // When going down don't go lower than the highest solid block
	                    if(sourceBlockMaterial.isSolid() && y < blockToSpawn.y)
	                    {
	                        // Place the current block but abort spawning after that
	                        bBreak = true;
	                    }

	                    if(y == blockToSpawn.y)
	                    {
	                		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, true).toDefaultMaterial();
	                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
	                		{
	                			materialToSet = surfaceBlockMaterial;
	                			blockDataToSet = surfaceBlockMaterialBlockData;
	                		} else {
	                        	materialToSet = groundBlockMaterial;
	                        	blockDataToSet = groundBlockMaterialBlockData;
	                		}
	                    }
	                    else if(y < blockToSpawn.y)
	                    {
	                    	materialToSet = groundBlockMaterial;
	                    	blockDataToSet = groundBlockMaterialBlockData;
	                    } else {
	                    	throw new RuntimeException();
	                    }

	                    if(materialToSet != null && materialToSet != DefaultMaterial.UNKNOWN_BLOCK)
	                    {
	                        blockToQueueForSpawn = new BlockFunction();
	                        blockToQueueForSpawn.x = blockToSpawn.x;
	                        blockToQueueForSpawn.y = y;
	                        blockToQueueForSpawn.z = blockToSpawn.z;
	                        blockToQueueForSpawn.material = OTG.toLocalMaterialData(materialToSet,blockDataToSet);

	                        // Apply mesa blocks if needed
	                        if(
                        		!blockToQueueForSpawn.material.isAir() &&
                        		!blockToQueueForSpawn.material.isLiquid() &&
                        		biomeConfig.surfaceAndGroundControl != null &&
                				biomeConfig.surfaceAndGroundControl instanceof MesaSurfaceGenerator &&
                        		(
                    				(
                						blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeConfig.groundBlock.toDefaultMaterial()) &&
                						blockToQueueForSpawn.material.getBlockData() == biomeConfig.groundBlock.getBlockData()
            						)
            						||
            						(
        								blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeConfig.surfaceBlock.toDefaultMaterial()) &&
        								blockToQueueForSpawn.material.getBlockData() == biomeConfig.surfaceBlock.getBlockData()
    								)
								)
							)
	                        {
            		        	LocalMaterialData customBlockData = biomeConfig.surfaceAndGroundControl.getCustomBlockData(World, biomeConfig, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
            		        	if(customBlockData != null)
            		        	{
            		        		blockToQueueForSpawn.material = customBlockData;
            		        	}
        		        		setBlock(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, World);
	                        } else {
	                        	if (!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
	                        	{
	                        		setBlock(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, World);
	                        	}
	                        }
	                    } else {
	                    	throw new RuntimeException();
	                    }
	                    if(bBreak)
	                    {
	                        break;
	                    }
					}
				}
				else if(goingUp)
				{
	                // ReplaceAbove should really be three option setting:
	                // Clear nothing above BO3
	                // Clear all except water/liquid (below or at water level) and fill up with liquid if below water level
	                // Clear all including water/liquid

					if(replaceAboveMaterial == null)
					{
						continue;
					}

					yStart = World.getHighestBlockYAt(blockToSpawn.x,blockToSpawn.z, true, true, false, false);
					yEnd = 0;
					for(int y = yStart; y >= yEnd; y--)
					{
						if(y >= 255){ continue;}

						sourceBlockMaterial = World.getMaterial(blockToSpawn.x, y, blockToSpawn.z, true);
						DefaultMaterial sourceBlockDefaultMaterial = sourceBlockMaterial.toDefaultMaterial();

                    	materialToSet = replaceAboveMaterial;
                    	blockDataToSet = replaceAboveMaterialBlockData;

	                    if(y < blockToSpawn.y)
                    	{
	                    	if(!sourceBlockMaterial.isLiquid() || (secondPass && !((BO3)Start.getObject()).getSettings().SpawnUnderWater))  // If this is the second pass then the first pass went down and we don't have to make a dam, otherwise we do
	                    	{
                    			break;
                    		}
	                    	else if(((BO3)Start.getObject()).getSettings().SpawnUnderWater)
	                    	{
	                    		materialToSet = replaceAboveMaterial; // Replace liquid with replaceAboveMaterial
	                    		blockDataToSet = replaceAboveMaterialBlockData;
	                    	} else {
	                    		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, true).toDefaultMaterial();
		                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
		                		{
		                			materialToSet = surfaceBlockMaterial;
		                			blockDataToSet = surfaceBlockMaterialBlockData;
		                		} else {
		                        	materialToSet = groundBlockMaterial;
		                        	blockDataToSet = groundBlockMaterialBlockData;
		                		}
	                    	}
	                    }

	                    if(y == blockToSpawn.y)
	                    {
	                    	if(sourceBlockMaterial.isSolid() || (!secondPass && sourceBlockMaterial.isLiquid() && !((BO3)Start.getObject()).getSettings().SpawnUnderWater))
	                    	{
		                		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, true).toDefaultMaterial();
		                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
		                		{
			                		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, true).toDefaultMaterial();
			                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
			                		{
			                			materialToSet = surfaceBlockMaterial;
			                			blockDataToSet = surfaceBlockMaterialBlockData;
			                		} else {
			                        	materialToSet = groundBlockMaterial;
			                        	blockDataToSet = groundBlockMaterialBlockData;
			                		}
		                		} else {
		                        	materialToSet = groundBlockMaterial;
		                        	blockDataToSet = groundBlockMaterialBlockData;
		                		}
	                    	} else {
	                    		if(((BO3)Start.getObject()).getSettings().SpawnUnderWater)
		                    	{
		                    		materialToSet = replaceAboveMaterial; // Replace liquid with replaceAboveMaterial
		                    		blockDataToSet = replaceAboveMaterialBlockData;
	                    		} else {
	                    			// After removing layers of blocks replace the heighest block left with the surfaceBlockMaterial
	                    			if(!sourceBlockMaterial.isLiquid() && !sourceBlockDefaultMaterial.equals(DefaultMaterial.AIR))
	                    			{
	        	                		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, true).toDefaultMaterial();
	        	                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
	        	                		{
	        	                			materialToSet = DefaultMaterial.AIR; // Make sure that canyons/caves etc aren't covered
        		                			blockDataToSet = surfaceBlockMaterialBlockData;
	        	                		} else {
	        	                        	materialToSet = groundBlockMaterial;
	        	                        	blockDataToSet = groundBlockMaterialBlockData;
	        	                		}
	                    				bBreak = true;
	                    			} else {
	                    				break;
	                    			}
		                    	}
	                    	}
	                    }

                    	if(materialToSet.isLiquid() && ((BO3)Start.getObject()).getSettings().SpawnUnderWater && y >= (biomeConfig.useWorldWaterLevel ? World.getConfigs().getWorldConfig().waterLevelMax : biomeConfig.waterLevelMax))
                    	{
                    		materialToSet = DefaultMaterial.AIR;
                    		blockDataToSet = 0;
                    	}

	                    if(materialToSet != null && materialToSet != DefaultMaterial.UNKNOWN_BLOCK)
	                    {
	                        blockToQueueForSpawn = new BlockFunction();
	                        blockToQueueForSpawn.x = blockToSpawn.x;
	                        blockToQueueForSpawn.y = y;
	                        blockToQueueForSpawn.z = blockToSpawn.z;
	                        blockToQueueForSpawn.material = OTG.toLocalMaterialData(materialToSet, blockDataToSet);

	                        // Apply mesa blocks if needed
	                        if(
                        		!blockToQueueForSpawn.material.isAir() &&
                        		!blockToQueueForSpawn.material.isLiquid() &&
                        		biomeConfig.surfaceAndGroundControl != null &&
                				biomeConfig.surfaceAndGroundControl instanceof MesaSurfaceGenerator &&
                        		(
                    				(
                						blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeConfig.groundBlock.toDefaultMaterial()) &&
                						blockToQueueForSpawn.material.getBlockData() == biomeConfig.groundBlock.getBlockData()
            						)
            						||
            						(
        								blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeConfig.surfaceBlock.toDefaultMaterial()) &&
        								blockToQueueForSpawn.material.getBlockData() == biomeConfig.surfaceBlock.getBlockData()
    								)
								)
							)
	                        {
            		        	LocalMaterialData customBlockData = biomeConfig.surfaceAndGroundControl.getCustomBlockData(World, biomeConfig, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
            		        	if(customBlockData != null)
            		        	{
            		        		blockToQueueForSpawn.material = customBlockData;
            		        	}
        		        		setBlock(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, World);
	                        } else {
	                        	if (!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
	                        	{
	                        		setBlock(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, World);
	                        	}
	                        }
	                    } else {
	                    	throw new RuntimeException();
	                    }
	                    if(bBreak)
	                    {
	                        break;
	                    }
					}
				}
            }

            // We'll still be using the chunks that smoothing areas
            // spawn in for chunk based collision detection so keep them
            // but empty them of blocks
            smoothingAreaInChunk.setValue(null);
        }
        return true;
    }

    private void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, LocalWorld World)
    {
	    HashMap<DefaultMaterial,LocalMaterialData> blocksToReplace = World.getConfigs().getWorldConfig().getReplaceBlocksDict();
	    if(blocksToReplace != null && blocksToReplace.size() > 0)
	    {
	    	LocalMaterialData targetBlock = blocksToReplace.get(material.toDefaultMaterial());
	    	if(targetBlock != null)
	    	{
	    		material = targetBlock;
	    	}
	    }
	    World.setBlock(x, y, z, material, metaDataTag, true);
    }

    private ArrayList<Object[]> mergeSmoothingAreas(ChunkCoordinate chunkCoordinate, ArrayList<Object[]> smoothingAreas, LocalWorld World, CustomObjectCoordinate Start)
    {
        ArrayList<Object[]> blocksToSpawn = new ArrayList<Object[]>();

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesnt exactly
        // make the code any easier to read..
        boolean goingUp;
        boolean goingDown;

        boolean diagonalLinegoingUp;
        boolean diagonalLinegoingDown;

        int distanceFromStart;
        BlockFunction beginPoint;
        int originPointX;
        int originPointY;
        int originPointZ;
        int finalDestinationPointX;
        int finalDestinationPointY;
        int finalDestinationPointZ;

        int diagonalLineOriginPointX;
        int diagonalLineoriginPointY;
        int diagonalLineOriginPointZ;
        int diagonalLineFinalDestinationPointX;
        int diagonalLineFinalDestinationPointY;
        int diagonalLineFinalDestinationPointZ;

        LocalMaterialData material;
        boolean dontAdd;
        BlockFunction existingBlock;
        BlockFunction endPoint;
        int surfaceBlockHeight;
        BlockFunction filler;
        ArrayList<Object[]> blocksToRemove;

        BlockFunction[] blockColumn = null;
        BlockFunction[] blockColumn2 = null;
        int prevfinalDestinationPointX = 0;
        int prevfinalDestinationPointZ = 0;
        int prevDiagonalLineFinalDestinationPointX = 0;
        int prevDiagonalLineFinalDestinationPointZ = 0;
        boolean isInitialised = false;

        // Check if all smooth areas have been finalized (endpoint y set) for this chunk
        // if so then merge them down to a single smooth area. Otherwise queue them and
        // spawn them later
        for(Object[] smoothingBeginAndEndPoints : smoothingAreas)
        {
        	beginPoint = new BlockFunction();
            beginPoint.x = (Integer)smoothingBeginAndEndPoints[0];
            beginPoint.y = (Integer)smoothingBeginAndEndPoints[1];
            beginPoint.z = (Integer)smoothingBeginAndEndPoints[2];

            originPointX = (Integer)smoothingBeginAndEndPoints[6];
            originPointY = (Integer)smoothingBeginAndEndPoints[7];
            originPointZ = (Integer)smoothingBeginAndEndPoints[8];

        	finalDestinationPointX = (Integer)smoothingBeginAndEndPoints[9];
            finalDestinationPointY = (Integer)smoothingBeginAndEndPoints[10];
            finalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[11];

            diagonalLineOriginPointX = -1;
            diagonalLineoriginPointY = -1;
            diagonalLineOriginPointZ = -1;
            diagonalLineFinalDestinationPointX = -1;
            diagonalLineFinalDestinationPointY = -1;
            diagonalLineFinalDestinationPointZ = -1;

            // if this line is a child line of a diagonal line
        	if(smoothingBeginAndEndPoints.length > 17)
        	{
	            diagonalLineOriginPointX = (Integer)smoothingBeginAndEndPoints[12];
	            diagonalLineoriginPointY = (Integer)smoothingBeginAndEndPoints[13];
	            diagonalLineOriginPointZ = (Integer)smoothingBeginAndEndPoints[14];
	            diagonalLineFinalDestinationPointX = (Integer)smoothingBeginAndEndPoints[15];
	            diagonalLineFinalDestinationPointY = (Integer)smoothingBeginAndEndPoints[16];
	            diagonalLineFinalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[17];

	            // Line has been marked as do not spawn because it crossed another line
	            if(diagonalLineFinalDestinationPointY == -2)
	            {
	            	continue;
	            }
        	}

            if(!isInitialised || (prevfinalDestinationPointX != finalDestinationPointX || prevfinalDestinationPointZ != finalDestinationPointZ))
            {
               	blockColumn = World.getBlockColumn(finalDestinationPointX, finalDestinationPointZ);

            	prevfinalDestinationPointX = finalDestinationPointX;
            	prevfinalDestinationPointX = finalDestinationPointZ;
            }
            if(smoothingBeginAndEndPoints.length > 17)
            {
	            if(!isInitialised || (prevDiagonalLineFinalDestinationPointX != diagonalLineFinalDestinationPointX || prevDiagonalLineFinalDestinationPointZ != diagonalLineFinalDestinationPointZ))
	            {
            		blockColumn2 = World.getBlockColumn(diagonalLineFinalDestinationPointX, diagonalLineFinalDestinationPointZ);
	            	prevDiagonalLineFinalDestinationPointX = diagonalLineFinalDestinationPointX;
	            	prevDiagonalLineFinalDestinationPointZ = diagonalLineFinalDestinationPointZ;
	            }
            }
            isInitialised = true;

            // This is a line plotted as a child line of a diagonal line of which the endpointy has not yet been determined
            if(originPointY == -1 && smoothingBeginAndEndPoints.length > 17)
            {
	            if(diagonalLineFinalDestinationPointY == -1)
	            {
		            material = null;
		            for(int i = 255; i > -1; i--)
		            {
		                // when going down dont stop at the waterline
		                // when going up stop at the waterline
		            	BlockFunction block = blockColumn2[i];
		            	material = block.material;
		                if(
		                    !material.isAir() &&
		                    (
		                        (block.y <= diagonalLineoriginPointY && !material.isLiquid()) ||
		                        (block.y > diagonalLineoriginPointY && (!((BO3)Start.getObject()).getSettings().SpawnUnderWater || !material.isLiquid()))
		                    )
		                )
		                {
		                	// TODO: get all blocks using this endpoint and set this Y
		                	diagonalLineFinalDestinationPointY = block.y;
		                    break;
		                }
		            }
	            }

	        	if(diagonalLineFinalDestinationPointY == -1)
	        	{
	        		// TODO: Could this be the cause of the mystery bug that places smoothing areas at y 0?
	        		//OTG.log(LogMarker.INFO, "A smoothing area tried to spawn at Y -1 for structure " + Start.BO3Name + ". If you are creating empty chunks intentionally (for a sky-world for instance) you may wish to disable smoothing areas in your BO3s!");
	        		// Something is wrong!
	        		//throw new RuntimeException();
	        		diagonalLineFinalDestinationPointY = 0;
	        	}

	        	//{
        		diagonalLinegoingDown = false;
        		diagonalLinegoingUp = false;
        		if(diagonalLineoriginPointY >= diagonalLineFinalDestinationPointY)
        		{
        			diagonalLinegoingDown = true;
        		}
        		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
        		{
        			diagonalLinegoingUp = true;
        		}

                // Set diagonal-y-endpoint for all other smoothing area lines that are children of this diagonal line.
                ArrayList<Object[]> smoothingAreasToSpawnPerLineDestination = SmoothingAreasToSpawnPerDiagonalLineDestination.get(ChunkCoordinate.fromChunkCoords(diagonalLineFinalDestinationPointX, diagonalLineFinalDestinationPointZ));
                if(smoothingAreasToSpawnPerLineDestination != null)
                {
	                for(Object[] smoothingBeginAndEndPoints2 : smoothingAreasToSpawnPerLineDestination)
	                {
	                	int diagonalLineFinalOriginPointX2 = (Integer)smoothingBeginAndEndPoints2[12];
	                	int diagonalLineFinalOriginPointZ2 = (Integer)smoothingBeginAndEndPoints2[14];

	                    int diagonalLineFinalDestinationPointY2 = (Integer)smoothingBeginAndEndPoints2[16];

	            		if(
            				diagonalLineOriginPointX == diagonalLineFinalOriginPointX2 && diagonalLineOriginPointZ == diagonalLineFinalOriginPointZ2
        				)
	            		{
	            			if(diagonalLineFinalDestinationPointY2 != -2)
	            			{
		            			smoothingBeginAndEndPoints2[16] = diagonalLineFinalDestinationPointY;
		            		}
	        			}
	                }
                }

                if((Integer)smoothingBeginAndEndPoints[16] != -2)
                {
                	smoothingBeginAndEndPoints[16] = diagonalLineFinalDestinationPointY;
                } else {
                	continue;
                }
            }

        	if(finalDestinationPointY == -1)
        	{
	            material = null;
	            for(int i = 255; i > -1; i--)
	            {
	                // when going down dont stop at the waterline
	                // when going up stop at the waterline
	            	BlockFunction block = blockColumn[i];
	            	material = block.material;

	                if(
	                    !material.isAir() &&
	                    (
	                        (
                        		block.y <= (diagonalLineoriginPointY > -1 ? diagonalLineoriginPointY : originPointY) &&
                        		!material.isLiquid()
                    		) ||
	                        (block.y > (diagonalLineoriginPointY > -1 ? diagonalLineoriginPointY : originPointY) && (!((BO3)Start.getObject()).getSettings().SpawnUnderWater || !material.isLiquid()))
	                    )
	                )
	                {
	                	finalDestinationPointY = block.y;

	                	smoothingBeginAndEndPoints[10] = finalDestinationPointY;

    	                // Set y-endpoint for all other smoothing area line-parts that are part of this line

            			ArrayList<Object[]> smoothingAreasForLine = SmoothingAreasToSpawnPerLineOrigin.get(ChunkCoordinate.fromChunkCoords(originPointX, originPointZ));
            			if(smoothingAreasForLine != null)
            			{
		                	for(Object[] smoothingBeginAndEndPoints2 : smoothingAreasForLine)
		                	{
			                	int finalDestinationPointX2 = (Integer)smoothingBeginAndEndPoints2[9];
			                    int finalDestinationPointZ2 = (Integer)smoothingBeginAndEndPoints2[11];

			            		if(finalDestinationPointX == finalDestinationPointX2 && finalDestinationPointZ == finalDestinationPointZ2)
			            		{
		            				smoothingBeginAndEndPoints2[10] = finalDestinationPointY; // - 1; // <-- -1 is a hack because the spawning area endpoints would always spawn 1 block too high
			            		}
	    	                }
            			}
	                    break;
	                }
	            }
        	}

        	// this should no longer be necessary since ForgeWorld has been changed to force chunk
        	// population when height is requested for a block in an unpopulated chunk. TODO: will that work for bukkit too?
            if(finalDestinationPointY == -1)
            {
            	finalDestinationPointY = 0;
            }

            // This is a line plotted as a child line of a diagonal line of which the diagonalendpointy has been determined
            // but the originPointY hasnt
            if((Integer)smoothingBeginAndEndPoints[7] == -1)
            {
	            diagonalLineOriginPointX = (Integer)smoothingBeginAndEndPoints[12];
	            diagonalLineoriginPointY = (Integer)smoothingBeginAndEndPoints[13];
	            diagonalLineFinalDestinationPointX = (Integer)smoothingBeginAndEndPoints[15];
	            diagonalLineFinalDestinationPointY = (Integer)smoothingBeginAndEndPoints[16];

        		originPointY = (int)Math.round(
                    (double)
                    (
                        (double)Math.abs(diagonalLineoriginPointY - diagonalLineFinalDestinationPointY)
                        *
                        (double)((double)Math.abs(diagonalLineOriginPointX - originPointX) / (double)Math.abs(diagonalLineOriginPointX - diagonalLineFinalDestinationPointX))
                    )
                );

        		if(diagonalLineoriginPointY > diagonalLineFinalDestinationPointY)
        		{
        			originPointY = diagonalLineoriginPointY - originPointY;
        		}
        		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
        		{
        			originPointY = diagonalLineoriginPointY + originPointY;
        		} else {
        			originPointY = diagonalLineoriginPointY;
        		}

        		smoothingBeginAndEndPoints[7] = originPointY;
            }
        }

        for(Object[] smoothingBeginAndEndPoints : smoothingAreas)
        {
        	// if this line was set as do not spawn then skip it
        	if(smoothingBeginAndEndPoints.length > 17 && (Integer)smoothingBeginAndEndPoints[16] == -2)
        	{
	            continue;
        	}

            diagonalLinegoingUp = false;
            diagonalLinegoingDown = false;

            goingUp = false;
            goingDown = false;
            distanceFromStart = 0;

            beginPoint = new BlockFunction();
            beginPoint.x = (Integer)smoothingBeginAndEndPoints[0];
            beginPoint.y = (Integer)smoothingBeginAndEndPoints[1];
            beginPoint.z = (Integer)smoothingBeginAndEndPoints[2];

            originPointX = (Integer)smoothingBeginAndEndPoints[6];
            originPointY = (Integer)smoothingBeginAndEndPoints[7];
            originPointZ = (Integer)smoothingBeginAndEndPoints[8];

            finalDestinationPointX = (Integer)smoothingBeginAndEndPoints[9];
            finalDestinationPointY = (Integer)smoothingBeginAndEndPoints[10];
            finalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[11];

            diagonalLineOriginPointX = -1;
            diagonalLineoriginPointY = -1;
            diagonalLineOriginPointZ = -1;
            diagonalLineFinalDestinationPointX = -1;
            diagonalLineFinalDestinationPointY = -1;
            diagonalLineFinalDestinationPointZ = -1;

            if(smoothingBeginAndEndPoints.length > 17)
            {
	            diagonalLineOriginPointX = (Integer)smoothingBeginAndEndPoints[12];
	            diagonalLineoriginPointY = (Integer)smoothingBeginAndEndPoints[13];
	            diagonalLineOriginPointZ = (Integer)smoothingBeginAndEndPoints[14];
	            diagonalLineFinalDestinationPointX = (Integer)smoothingBeginAndEndPoints[15];
	            diagonalLineFinalDestinationPointY = (Integer)smoothingBeginAndEndPoints[16];
	            diagonalLineFinalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[17];
            }

            if(smoothingBeginAndEndPoints.length > 17)
            {
        		if((Integer)smoothingBeginAndEndPoints[13] >= (Integer)smoothingBeginAndEndPoints[16])
        		{
        			diagonalLinegoingDown = true;
        		}
        		else if((Integer)smoothingBeginAndEndPoints[13] < (Integer)smoothingBeginAndEndPoints[16])
        		{
        			diagonalLinegoingUp = true;
        		}
            }

            int highestBlock = -1;

            // TODO: Check if this is really still needed
            // finalDestinationPointY may have been found so the chunk is loaded, however it might still be the wrong coordinate
            // check again, taking into account water and lava and originPointY
            material = null;
            highestBlock = finalDestinationPointY;

            if((prevfinalDestinationPointX != finalDestinationPointX || prevfinalDestinationPointZ != finalDestinationPointZ))
            {
            	blockColumn = World.getBlockColumn(finalDestinationPointX,finalDestinationPointZ);
            	prevfinalDestinationPointX = finalDestinationPointX;
            	prevfinalDestinationPointZ = finalDestinationPointZ;
            }

            for(int i = highestBlock; i > -1; i--)
            {
                // when going down dont stop at the waterline
                // when going up stop at the waterline
            	BlockFunction block = blockColumn[i];
            	material = block.material;
                if(
                    !material.isAir() &&
                    (
                        (block.y <= originPointY && !material.isLiquid()) ||
                        (block.y > originPointY && ((!((BO3)Start.getObject()).getSettings().SpawnUnderWater || !material.isLiquid())))
                    )
                )
                {
                    finalDestinationPointY = block.y;
                    break;
                }
            }

            if(finalDestinationPointY > beginPoint.y)
            {
                goingUp = true;
            }
            if(finalDestinationPointY <= beginPoint.y)
            {
                goingDown = true;
            }

            // Diagonal line child lines can only spawn in the same vertical direction
            // as their parent
    		if(diagonalLinegoingUp && !goingUp)
    		{
    			finalDestinationPointY = originPointY + 75 < 256 ? originPointY + 75 : 255;
                goingUp = true;
                goingDown = false;
    		}
    		else if(diagonalLinegoingDown && !goingDown)
    		{
                goingUp = false;
                goingDown = true;

                int distanceFromOrigin = -1;
                int firstSolidBlock = -1;

                // Since this is the second pass and the first pass went up we'll have to detect
                // the closest suitable block to smooth to without using getHeighestBlock()
                // this means we might accidentally detect a cave beneath the surface as the
                // smooth to point.
                // set a limit of -30 y to reduce the chance that we target a cave underneath the surface
                // if we do hit a cave then it will be used as the base for the dirt ramp we're making,
                // the cave will be filled with the dirt ramp and the dirt ramp may look oddly steep
                // when seen from above. Limiting this to 30 should reduce this effect to acceptable levels?

                // Look for a solid destination block that has air/water/lava above it below the originBlock
                // If we cant find one within range (30 blocks) then use the first solid block without air/water/lava above it

                for(int i = originPointY; i > -1; i--)
                {
                	BlockFunction block = blockColumn[i];
                    distanceFromOrigin = Math.abs(originPointY - block.y);
                    LocalMaterialData materialAbove = blockColumn[i + 1].material;
                    material = blockColumn[block.y].material;
                    if(
                        firstSolidBlock == -1 &&
                        !material.isAir() &&
                        !material.isLiquid()
                    )
                    {
                        firstSolidBlock = block.y;
                    }

                    if(
                        distanceFromOrigin <= 30 &&
                        !material.isAir() &&
                        !material.isLiquid() &&
                        (
                            materialAbove.isAir() ||
                            materialAbove.isLiquid()
                        )
                    )
                    {
                        finalDestinationPointY = block.y;
                        break;
                    }
                    if(distanceFromOrigin > 30 && firstSolidBlock > -1)
                    {
                        finalDestinationPointY = firstSolidBlock;
                        break;
                    }
                }

                // No block found
                if(distanceFromOrigin > 30 && firstSolidBlock == -1)
                {
                    finalDestinationPointY = originPointY;
                }
    		}

            // TODO: Make checks for situations where we can predict that a second pass won't be needed?
            int repeats = 1;

            // Do two passes, one up and one down, for each smoothing begin and endpoint
            // to make both an evenly sloped hole above and a hill below the BO3
            for(int pass2 = 0; pass2 <= repeats; pass2++)
            {
            	//if(pass2 == 1) { break; }

                // If this is a corner then on the second pass move the diagonal line
                if(smoothingBeginAndEndPoints.length > 17 && pass2 == 1)
                {
    	            // Recalculate the diagonal line y endpoint

                    if(diagonalLinegoingDown)
                    {
                        // TODO: replace 75 with... configurable value? or some kinda block-detection routine?
                    	diagonalLineFinalDestinationPointY = diagonalLineoriginPointY + 75 < 256 ? diagonalLineoriginPointY + 75 : 255;
                    	//diagonalLineFinalDestinationPointY = diagonalLineoriginPointY;
                    }
                    else if(diagonalLinegoingUp)// && !goingUp)
                    {
                        int distanceFromOrigin = -1;
                        int firstSolidBlock = -1;
                        // Since this is the second pass and the first pass went up we'll have to detect
                        // the closest suitable block to smooth to without using getHeighestBlock()
                        // this means we might accidentally detect a cave beneath the surface as the
                        // smooth to point.
                        // set a limit of -30 y to reduce the chance that we target a cave underneath the surface
                        // if we do hit a cave then it will be used as the base for the dirt ramp we're making,
                        // the cave will be filled with the dirt ramp and the dirt ramp may look oddly steep
                        // when seen from above. Limiting this to 30 should reduce this effect to acceptable levels?

                        // Look for a solid destination block that has air/water/lava above it below the originBlock
                        // If we cant find one within range (30 blocks) then use the first solid block without air/water/lava above it
                        diagonalLineFinalDestinationPointY = diagonalLineoriginPointY;

        	            if((prevDiagonalLineFinalDestinationPointX != diagonalLineFinalDestinationPointX || prevDiagonalLineFinalDestinationPointZ != diagonalLineFinalDestinationPointZ))
        	            {
        	            	blockColumn2 = World.getBlockColumn(diagonalLineFinalDestinationPointX,diagonalLineFinalDestinationPointZ);
        	            	prevDiagonalLineFinalDestinationPointX = diagonalLineFinalDestinationPointX;
        	            	prevDiagonalLineFinalDestinationPointZ = diagonalLineFinalDestinationPointZ;
        	            }

        	            for(int i = diagonalLineoriginPointY; i > 0; i--)
                        {
                        	BlockFunction block = blockColumn2[i];
                            distanceFromOrigin = Math.abs(diagonalLineoriginPointY - block.y);
                            LocalMaterialData materialAbove = blockColumn2[i + 1].material;
                            material = block.material;
                            if(
                                firstSolidBlock == -1 &&
                                !material.isAir() &&
                                !material.isLiquid()
                            )
                            {
                                firstSolidBlock = block.y;
                            }

                            if(
                                distanceFromOrigin <= 30 &&
                                !material.isAir() &&
                                !material.isLiquid() &&
                                (
                                    materialAbove.isAir() ||
                                    materialAbove.isLiquid()
                                )
                            )
                            {
                            	diagonalLineFinalDestinationPointY = block.y;
                                break;
                            }
                            if(distanceFromOrigin > 30 && firstSolidBlock > -1)
                            {
                            	diagonalLineFinalDestinationPointY = firstSolidBlock;
                                break;
                            }
                        }
                    }

	        		originPointY = (int)Math.ceil(
                        (double)
                        (
                            (double)Math.abs(diagonalLineoriginPointY - diagonalLineFinalDestinationPointY)
                            *
                            (double)((Math.abs(diagonalLineOriginPointX - originPointX)) / (double)Math.abs(diagonalLineOriginPointX - diagonalLineFinalDestinationPointX))
                        )
                    );
	        		if(diagonalLineoriginPointY > diagonalLineFinalDestinationPointY)
	        		{
	        			originPointY = diagonalLineoriginPointY - originPointY;
	        		}
	        		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
	        		{
	        			originPointY = diagonalLineoriginPointY + originPointY;
	        		} else {
	        			originPointY = diagonalLineoriginPointY;
	        		}

	        		// Line has been switched so really this line is going down
	        		if(diagonalLinegoingUp)
	        		{
		                material = null;
		                highestBlock = originPointY;

		                for(int i = highestBlock; i > -1; i--)
		                {
		                    // when going down dont stop at the waterline
		                    // when going up stop at the waterline
		                	BlockFunction block = blockColumn[i];
		                	material = block.material;
		                    if(
		                        !material.isAir() &&
		                        (
		                            (block.y <= originPointY && !material.isLiquid()) ||
		                            (block.y > originPointY && (!((BO3)Start.getObject()).getSettings().SpawnUnderWater || !material.isLiquid()))
		                        )
		                    )
		                    {
		                        finalDestinationPointY = block.y;
		                        break;
		                    }
		                }
                        goingUp = false;
                        goingDown = true;
	        		}
	        		// Line has been switched so really this line is going up
	        		else if(diagonalLinegoingDown)
	        		{
	        			finalDestinationPointY = World.getHighestBlockYAt(finalDestinationPointX, finalDestinationPointZ, true, true, false, true);
                    	if(finalDestinationPointY < diagonalLineoriginPointY)
                    	{
		        			finalDestinationPointY = diagonalLineoriginPointY + 75 < 256 ? diagonalLineoriginPointY + 75 : 255;
                    	}

                        goingUp = true;
                        goingDown = false;
	        		}
                }

                if(pass2 == 1 && smoothingBeginAndEndPoints.length < 18)
                {
                    if(!goingUp)
                    {
                        // TODO: replace 75 with... configurable value? or some kinda block-detection routine?
                        finalDestinationPointY = originPointY + 75 < 256 ? originPointY + 75 : 255;
                        goingUp = true;
                        goingDown = false;
                    }
                    else if(goingUp)
                    {
                        goingUp = false;
                        goingDown = true;

                        int distanceFromOrigin = -1;
                        int firstSolidBlock = -1;
                        // Since this is the second pass and the first pass went up we'll have to detect
                        // the closest suitable block to smooth to without using getHeighestBlock()
                        // this means we might accidentally detect a cave beneath the surface as the
                        // smooth to point.
                        // set a limit of -30 y to reduce the chance that we target a cave underneath the surface
                        // if we do hit a cave then it will be used as the base for the dirt ramp we're making,
                        // the cave will be filled with the dirt ramp and the dirt ramp may look oddly steep
                        // when seen from above. Limiting this to 30 should reduce this effect to acceptable levels?

                        // Look for a solid destination block that has air/water/lava above it below the originBlock
                        // If we cant find one within range (30 blocks) then use the first solid block without air/water/lava above it
                        finalDestinationPointY = originPointY;
                        for(int i = originPointY; i > -1; i--)
                        {
                            BlockFunction block = blockColumn[i];
                            distanceFromOrigin = Math.abs(originPointY - block.y);
                            LocalMaterialData materialAbove = blockColumn[i + 1].material;
                            material = block.material;
                            if(
                                firstSolidBlock == -1 &&
                                !material.isAir() &&
                                !material.isLiquid()
                            )
                            {
                                firstSolidBlock = block.y;
                            }

                            if(
                                distanceFromOrigin <= 30 &&
                                !material.isAir() &&
                                !material.isLiquid() &&
                                (
                                    materialAbove.isAir() ||
                                    materialAbove.isLiquid()
                                )
                            )
                            {
                                finalDestinationPointY = block.y;
                                break;
                            }
                            if(distanceFromOrigin > 30 && firstSolidBlock > -1)
                            {
                                finalDestinationPointY = firstSolidBlock;
                                break;
                            }
                        }

                        // No block found
                        if(distanceFromOrigin > 30 && firstSolidBlock == -1)
                        {
                            finalDestinationPointY = originPointY;
                        }
                    }

                    material = null;
                }

                // Get the coordinates for the last block in this chunk for this line
                endPoint = new BlockFunction();
                endPoint.x = (Integer)smoothingBeginAndEndPoints[3];
                endPoint.y = finalDestinationPointY;
                endPoint.z = (Integer)smoothingBeginAndEndPoints[5];

                // Add to spawn list all the blocks in between the first and last block in this chunk for this line

                if(originPointX != finalDestinationPointX && originPointZ == finalDestinationPointZ)
                {
                	double adjustedOriginPointY = 0;
                	if(smoothingBeginAndEndPoints.length > 17)
                	{
	            		double originPointY2 =
                        (
                            (double)Math.abs(diagonalLineoriginPointY - diagonalLineFinalDestinationPointY)
                            *
                            (double)((double)Math.abs(diagonalLineOriginPointX - originPointX) / (double)Math.abs(diagonalLineOriginPointX - diagonalLineFinalDestinationPointX))
                        );

                		if(diagonalLineoriginPointY > diagonalLineFinalDestinationPointY)
                		{
                			originPointY2 = diagonalLineoriginPointY - originPointY2;
                		}
                		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
                		{
                			originPointY2 = diagonalLineoriginPointY + originPointY2;
                		} else {
                			originPointY2 = diagonalLineoriginPointY;
                		}

                        adjustedOriginPointY =
                        (
                            (double)Math.abs(originPointY2 - finalDestinationPointY)
                            *
                            (double)((double)Math.abs(originPointX - diagonalLineOriginPointX) / (double)Math.abs(originPointX - finalDestinationPointX))
                        );

                        if(originPointY2 > finalDestinationPointY)
                        {
                        	adjustedOriginPointY =  originPointY2 + adjustedOriginPointY;
                        } else {
                        	adjustedOriginPointY =  originPointY2 - adjustedOriginPointY;
                        }
                	}

                    for(int i = 0; i <= Math.abs(beginPoint.x - endPoint.x); i++)
                    {
	    	            //X difference
	    	            distanceFromStart = Math.abs(beginPoint.x - originPointX) + i;

	    	            // (diagonalLineOriginPointX != originPointX) is to ignore any lines of blocks that use the very first block
	    	            // in a diagonal line as an origin point because those lines can be treated like normal (non-corner/diagonal lines)
	    	            if(smoothingBeginAndEndPoints.length > 17 && (diagonalLineOriginPointX != originPointX))
	    	            {
    	    	            //X difference
    	    	            distanceFromStart = Math.abs(beginPoint.x - diagonalLineOriginPointX) + i;

	                        double surfaceBlockHeight2 =
                            (
                                (double)Math.abs(adjustedOriginPointY - finalDestinationPointY)
                                *
                                (double)((double)distanceFromStart / (double)Math.abs(diagonalLineOriginPointX - finalDestinationPointX))
                            );

	                        if(adjustedOriginPointY > finalDestinationPointY)
	                        {
	                            // Moving down
	                        	surfaceBlockHeight = (int)Math.round(adjustedOriginPointY - surfaceBlockHeight2);
	                        } else {
	                        	surfaceBlockHeight = (int)Math.round(adjustedOriginPointY + surfaceBlockHeight2);
	                        }
	    	            } else {
	                        //surfaceBlockHeight = (int)Math.ceil(
	    	            	surfaceBlockHeight = (int)Math.round(
	                            (double)
	                            (
	                                (double)Math.abs(originPointY - finalDestinationPointY)
	                                *
	                                (double)((double)distanceFromStart / (double)Math.abs(originPointX - finalDestinationPointX))
	                            )
	                        );

	                        if(originPointY > finalDestinationPointY)
	                        {
	                            // Moving down
	                            surfaceBlockHeight = originPointY - surfaceBlockHeight;
	                        } else {
	                            surfaceBlockHeight = originPointY + surfaceBlockHeight;
	                        }
	    	            }

                        filler = new BlockFunction();
                        if(originPointX < finalDestinationPointX)
                        {
                            filler.x = beginPoint.x + i;
                            filler.y = surfaceBlockHeight;
                            filler.z = beginPoint.z;
                        }
                        if(originPointX > finalDestinationPointX)
                        {
                            filler.x = beginPoint.x - i;
                            filler.y = surfaceBlockHeight;
                            filler.z = beginPoint.z;
                        }

                        // For each block to spawn find out if it is above or below a smooth-area beginning point
                        // if it is above a smooth-area beginning point and this line is going up then don't spawn the block
                        // and abort spawning for this line of blocks
                        // this is done to make sure that smoothing-areas going down can cover lower-lying smooth areas
                        // but lower-lying smooth-areas going up do not replace higher smoothing areas going down
                        boolean abort = false;
                        // get smoothing blocks
                        for(Object[] smoothingBeginAndEndPoints2 : smoothingAreas)
                        {
                        	// TODO: Find out if this doesnt skip the block at
                        	// diagonal line index 0, it shouldnt!
                        	if(smoothingBeginAndEndPoints2.length < 18)
                        	{
	                        	int originPointX2 = (Integer)smoothingBeginAndEndPoints2[6];
	                        	int originPointZ2 = (Integer)smoothingBeginAndEndPoints2[8];

	                            if((originPointX2 != filler.x || originPointZ2 != filler.z) || (originPointX == originPointX2 && originPointZ == originPointZ2))
	                            {
	                                continue;
	                            }

	                            if(goingUp)
	                            {
	                                abort = true;
	                                break;
	                            }
                        	}
                        }

                        if(abort)
                        {
                            break;
                        }

                        blocksToRemove = new ArrayList<Object[]>();
                        dontAdd = false;
                        for(Object[] existingBlockItem : blocksToSpawn)
                        {
                            existingBlock = (BlockFunction)existingBlockItem[0];

                            //Don't always override higher blocks when going down, instead do a second pass going up
                            if (existingBlock.x == filler.x && existingBlock.z == filler.z)
                            {
                                // When this block is lower than existingblock and this block is going up and existingblock is going up
                                if (filler.y < existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                                // When this block is higher than or equal to existingblock and this block is going up and existingblock is going up
                                else if (filler.y >= existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                                {
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is lower than existingblock and this block is not going up and existingblock is going up
                                else if (filler.y < existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    // since goingDown does not remove higher blocks allow both blocks
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                                else if (filler.y > existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                                else if (filler.y == existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    //Allow both
                                }

                                // When this block is lower than existingblock and this block is going up and existingblock is not going up
                                if (filler.y < existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    //if the other block is higher and smoothing downwards then let it cover (smother) any smooth area below it (namely this one)
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is higher than or equal to existingblock and this block is going up and existingblock is not going up
                                else if (filler.y >= existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    // if this block is above another block that is going down and this block is going up then both are allowed
                                }
                                // When this block is lower than existingblock and this block is not going up and existingblock is not going up
                                else if (filler.y < existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is not going up
                                else if (filler.y >= existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                            }
                        }

                        if(!dontAdd)
                        {
                        	/*
	                        // Use this to debug smoothing areas, this shows origin points!
	                        if(filler.x == originPointX && filler.z == originPointZ)
	                        {
	            	    		try {
	            					World.setBlock(originPointX, originPointY, originPointZ,OTG.readMaterial("STONE"), null, true);
	            				} catch (InvalidConfigException e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}
	                        }
	                        */

	                        if(blocksToRemove.size() > 0)
	                        {
	                            for(Object[] blockToRemove : blocksToRemove)
	                            {
	                                blocksToSpawn.remove(blockToRemove);
	                            }
	                        }

                            blocksToSpawn.add(new Object[] { filler, goingUp, goingDown, pass2 == 1 });
                        }
                    }
                }
                if(originPointX == finalDestinationPointX && originPointZ != finalDestinationPointZ)
                {
                	double adjustedOriginPointY = 0;
                	if(smoothingBeginAndEndPoints.length > 17)
                	{
	            		double originPointY2 =
                        (
                            (double)Math.abs(diagonalLineoriginPointY - diagonalLineFinalDestinationPointY)
                            *
                            (double)((double)Math.abs(diagonalLineOriginPointX - originPointX) / (double)Math.abs(diagonalLineOriginPointX - diagonalLineFinalDestinationPointX))
                        );

                		if(diagonalLineoriginPointY > diagonalLineFinalDestinationPointY)
                		{
                			originPointY2 = diagonalLineoriginPointY - originPointY2;
                		}
                		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
                		{
                			originPointY2 = diagonalLineoriginPointY + originPointY2;
                		} else {
                			originPointY2 = diagonalLineoriginPointY;
                		}

                        adjustedOriginPointY =
                        (
                            (double)Math.abs(originPointY2 - finalDestinationPointY)
                            *
                            (double)((double)Math.abs(originPointZ - diagonalLineOriginPointZ) / (double)Math.abs(originPointZ - finalDestinationPointZ))
                        );

                        if(originPointY2 > finalDestinationPointY)
                        {
                        	adjustedOriginPointY =  originPointY2 + adjustedOriginPointY;
                        } else {
                        	adjustedOriginPointY =  originPointY2 - adjustedOriginPointY;
                        }
                	}

                    for(int i = 0; i <= Math.abs(beginPoint.z - endPoint.z); i++)
                    {
	    	            //Z difference
	    	            distanceFromStart = Math.abs(beginPoint.z - originPointZ) + i;

	    	            // (diagonalLineOriginPointZ != originPointZ) is to ignore any lines of blocks that use the very first block
	    	            // in a diagonal line as an origin point because those lines can be treated like normal (non-corner/diagonal lines)
	    	            if(smoothingBeginAndEndPoints.length > 17 && (diagonalLineOriginPointZ != originPointZ))
	    	            {
    	    	            //Z difference
    	    	            distanceFromStart = Math.abs(beginPoint.z - diagonalLineOriginPointZ) + i;

	                        double surfaceBlockHeight2 =
                            (
                                (double)Math.abs(adjustedOriginPointY - finalDestinationPointY)
                                *
                                (double)((double)distanceFromStart / (double)Math.abs(diagonalLineOriginPointZ - finalDestinationPointZ))
                            );

	                        if(adjustedOriginPointY > finalDestinationPointY)
	                        {
	                            // Moving down
	                        	surfaceBlockHeight = (int)Math.round(adjustedOriginPointY - surfaceBlockHeight2);
	                        } else {
	                        	surfaceBlockHeight = (int)Math.round(adjustedOriginPointY + surfaceBlockHeight2);
	                        }
	    	            } else {
                    		surfaceBlockHeight = (int)Math.round(
	                            (double)
	                            (
	                                (double)Math.abs(originPointY - finalDestinationPointY)
	                                *
	                                (double)((double)distanceFromStart / (double)Math.abs(originPointZ - finalDestinationPointZ))
	                            )
	                        );

	                        if(originPointY > finalDestinationPointY)
	                        {
	                            // Moving down
	                            surfaceBlockHeight = originPointY - surfaceBlockHeight;
	                        } else {
	                            surfaceBlockHeight = originPointY + surfaceBlockHeight;
	                        }
	    	            }

                        filler = new BlockFunction();
                        if(originPointZ < finalDestinationPointZ)
                        {
                            filler.x = beginPoint.x;
                            filler.y = surfaceBlockHeight;
                            filler.z = beginPoint.z + i;
                        }
                        if(originPointZ > finalDestinationPointZ)
                        {
                            filler.x = beginPoint.x;
                            filler.y = surfaceBlockHeight;
                            filler.z = beginPoint.z - i;
                        }

                        // For each block to spawn find out if it is above or below a smooth-area beginning point
                        // if it is above a smooth-area beginning point and this line is going up then don't spawn the block
                        // and abort spawning for this line of blocks
                        // this is done to make sure that smoothing-areas going down can cover lower-lying smooth areas
                        // but lower-lying smooth-areas going up do not replace higher smoothing areas going down
                        boolean abort = false;
                        // get smoothing blocks
                        for(Object[] smoothingBeginAndEndPoints2 : smoothingAreas)
                        {
                        	// TODO: Find out if this doesnt skip the block at
                        	// diagonal line index 0, it shouldnt!
                        	if(smoothingBeginAndEndPoints2.length < 18)
                        	{
	                        	// TODO: Even diagonal block child line smooth origin points are included
	                        	// here, find out if that doesn't cause bugs..
	                        	int originPointX2 = (Integer)smoothingBeginAndEndPoints2[6];
	                        	int originPointZ2 = (Integer)smoothingBeginAndEndPoints2[8];

	                            if((originPointX2 != filler.x || originPointZ2 != filler.z) || (originPointX == originPointX2 && originPointZ == originPointZ2))
	                            {
	                                continue;
	                            }

	                            if(goingUp)
	                            {
	                                abort = true;
	                                break;
	                            }
                        	}
                        }

                        if(abort)
                        {
                            break;
                        }

                        dontAdd = false;
                        blocksToRemove = new ArrayList<Object[]>();
                        for(Object[] existingBlockItem : blocksToSpawn)
                        {
                            existingBlock = (BlockFunction)existingBlockItem[0];
                            //Don't always override higher blocks when going down, instead do a second pass going up
                            if (existingBlock.x == filler.x && existingBlock.z == filler.z)
                            {
                                // When this block is lower than existingblock and this block is going up and existingblock is going up
                                if (filler.y < existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                                // When this block is higher than or equal to existingblock and this block is going up and existingblock is going up
                                else if (filler.y >= existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                                {
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is lower than existingblock and this block is not going up and existingblock is going up
                                else if (filler.y < existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    // since goingDown does not remove higher blocks allow both blocks
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                                else if (filler.y > existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                                else if (filler.y == existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    //Allow both
                                }

                                // When this block is lower than existingblock and this block is going up and existingblock is not going up
                                if (filler.y < existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    //if the other block is higher and smoothing downwards then let it cover (smother) any smooth area below it (namely this one)
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is higher than or equal to existingblock and this block is going up and existingblock is not going up
                                else if (filler.y >= existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    // if this block is above another block that is going down and this block is going up then both are allowed
                                }
                                // When this block is lower than existingblock and this block is not going up and existingblock is not going up
                                else if (filler.y < existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is not going up
                                else if (filler.y >= existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                            }
                        }

                        if(!dontAdd)
                        {
                        	/*
	                        // Use this to debug smoothing areas, this shows origin points!
	                        if(filler.x == originPointX && filler.z == originPointZ)
	                        {
	            	    		try {
	            					World.setBlock(originPointX, originPointY, originPointZ,OTG.readMaterial("STONE"), null, true);
	            				} catch (InvalidConfigException e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}
	                        }
                        	*/

	                        if(blocksToRemove.size() > 0)
	                        {
	                            for(Object[] blockToRemove : blocksToRemove)
	                            {
	                                blocksToSpawn.remove(blockToRemove);
	                            }
	                        }

                            blocksToSpawn.add(new Object[] { filler, goingUp, goingDown, pass2 == 1 });
                        }
                    }
                }

                if(originPointX == finalDestinationPointX && originPointZ == finalDestinationPointZ)
                {
                    filler = new BlockFunction();
                    filler.x = finalDestinationPointX;
                    filler.y = finalDestinationPointY;
                    filler.z = finalDestinationPointZ;

                    if(!goingUp && !goingDown)
                    {
                    	goingDown = true;
                    }

                    // For each block to spawn find out if it is above or below a smooth-area beginning point
                    // if it is above a smooth-area beginning point and this line is going up then don't spawn the block
                    // and abort spawning for this line of blocks
                    // this is done to make sure that smoothing-areas going down can cover lower-lying smooth areas
                    // but lower-lying smooth-areas going up do not replace higher smoothing areas going down
                    boolean abort = false;
                    // get smoothing blocks
                    for(Object[] smoothingBeginAndEndPoints2 : smoothingAreas)
                    {
                    	// TODO: Find out if this doesnt skip the block at
                    	// diagonal line index 0, it shouldnt!
                    	if(smoothingBeginAndEndPoints2.length < 18)
                    	{
                        	int originPointX2 = (Integer)smoothingBeginAndEndPoints2[6];
                        	int originPointZ2 = (Integer)smoothingBeginAndEndPoints2[8];

                            if((originPointX2 != filler.x || originPointZ2 != filler.z) || (originPointX == originPointX2 && originPointZ == originPointZ2))
                            {
                                continue;
                            }

                            if(goingUp)
                            {
                                abort = true;
                                break;
                            }
                    	}
                    }

                    if(abort)
                    {
                    	break;
                    }

                    dontAdd = false;
                    blocksToRemove = new ArrayList<Object[]>();
                    for(Object[] existingBlockItem : blocksToSpawn)
                    {
                        existingBlock = (BlockFunction)existingBlockItem[0];
                        //Don't always override higher blocks when going down, instead do a second pass going up
                        if (existingBlock.x == filler.x && existingBlock.z == filler.z)
                        {
                            // When this block is lower than existingblock and this block is going up and existingblock is going up
                            if (filler.y < existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                            {
                                blocksToRemove.add(existingBlockItem);
                            }
                            // When this block is higher than or equal to existingblock and this block is going up and existingblock is going up
                            else if (filler.y >= existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                            {
                                dontAdd = true;
                                break;
                            }
                            // When this block is lower than existingblock and this block is not going up and existingblock is going up
                            else if (filler.y < existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                            {
                                // since goingDown does not remove higher blocks allow both blocks
                            }
                            // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                            else if (filler.y > existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                            {
                                blocksToRemove.add(existingBlockItem);
                            }
                            // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                            else if (filler.y == existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                            {
                                //Allow both
                            }

                            // When this block is lower than existingblock and this block is going up and existingblock is not going up
                            if (filler.y < existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                            {
                                //if the other block is higher and smoothing downwards then let it cover (smother) any smooth area below it (namely this one)
                                dontAdd = true;
                                break;
                            }
                            // When this block is higher than or equal to existingblock and this block is going up and existingblock is not going up
                            else if (filler.y >= existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                            {
                                // if this block is above another block that is going down and this block is going up then both are allowed
                            }
                            // When this block is lower than existingblock and this block is not going up and existingblock is not going up
                            else if (filler.y < existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                            {
                                dontAdd = true;
                                break;
                            }
                            // When this block is higher than or equal to existingblock and this block is not going up and existingblock is not going up
                            else if (filler.y >= existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                            {
                                blocksToRemove.add(existingBlockItem);
                            }
                        }
                    }

                    if(!dontAdd)
                    {
                    	/*
                        // Use this to debug smoothing areas, this shows origin points!
                        if(filler.x == originPointX && filler.z == originPointZ)
                        {
            	    		try {
            					World.setBlock(originPointX, originPointY, originPointZ,OTG.readMaterial("STONE"), null, true);
            				} catch (InvalidConfigException e) {
            					// TODO Auto-generated catch block
            					e.printStackTrace();
            				}
                        }
                    	*/

                        if(blocksToRemove.size() > 0)
                        {
                            for(Object[] blockToRemove : blocksToRemove)
                            {
                                blocksToSpawn.remove(blockToRemove);
                            }
                        }

                        blocksToSpawn.add(new Object[] { filler, goingUp, goingDown, pass2 == 1 });
                    }
                }
            }
        }
        return blocksToSpawn;
    }    
}
