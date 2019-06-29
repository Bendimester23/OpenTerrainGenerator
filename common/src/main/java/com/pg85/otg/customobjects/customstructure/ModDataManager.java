package com.pg85.otg.customobjects.customstructure;

import java.util.HashSet;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bo3.bo3function.ModDataFunction;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class ModDataManager
{
	public HashSet<ModDataFunction> modData = new HashSet<ModDataFunction>();
	
    void spawnModData(BO3Config objectConfig, CustomObjectCoordinate coordObject, ChunkCoordinate chunkCoordinate)
    {
    	ModDataFunction[] blockDataInObject = objectConfig.getModData();
    	for(int i = 0; i < blockDataInObject.length; i++)
    	{
    		ModDataFunction newModData = new ModDataFunction();
        	if(coordObject.getRotation() != Rotation.NORTH)
        	{
            	int rotations = 0;
            	// How many counter-clockwise rotations have to be applied?
        		if(coordObject.getRotation() == Rotation.WEST)
        		{
        			rotations = 1;
        		}
        		else if(coordObject.getRotation() == Rotation.SOUTH)
        		{
        			rotations = 2;
        		}
        		else if(coordObject.getRotation() == Rotation.EAST)
        		{
        			rotations = 3;
        		}

                // Apply rotation
            	if(rotations == 0)
            	{
            		newModData.x = blockDataInObject[i].x;
            		newModData.z = blockDataInObject[i].z;
            	}
            	if(rotations == 1)
            	{
            		newModData.x = blockDataInObject[i].z;
            		newModData.z = -blockDataInObject[i].x + 15;
            	}
            	if(rotations == 2)
            	{
            		newModData.x = -blockDataInObject[i].x + 15;
            		newModData.z = -blockDataInObject[i].z + 15;
            	}
            	if(rotations == 3)
            	{
            		newModData.x = -blockDataInObject[i].z + 15;
            		newModData.z = blockDataInObject[i].x;
            	}
            	newModData.y = coordObject.getY() + blockDataInObject[i].y;

            	newModData.x = coordObject.getX() + newModData.x;
            	newModData.z = coordObject.getZ() + newModData.z;

            	newModData.modData = blockDataInObject[i].modData;
            	newModData.modId = blockDataInObject[i].modId;

        		modData.add(newModData);

        		if(!ChunkCoordinate.fromBlockCoords(newModData.x, newModData.z).equals(chunkCoordinate))
        		{
        			throw new RuntimeException(); // TODO: Remove this after testing
        		}
        	} else {

            	newModData.y = coordObject.getY() + blockDataInObject[i].y;

            	newModData.x = coordObject.getX() + blockDataInObject[i].x;
            	newModData.z = coordObject.getZ() + blockDataInObject[i].z;

            	newModData.modData = blockDataInObject[i].modData;
            	newModData.modId = blockDataInObject[i].modId;

        		modData.add(newModData);

        		if(!ChunkCoordinate.fromBlockCoords(newModData.x, newModData.z).equals(chunkCoordinate))
        		{
        			throw new RuntimeException(); // TODO: Remove this after testing
        		}
        	}
    	}
    }
}
