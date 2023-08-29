package com.danielrharris.townywars.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Utils{
	
	public static ArrayList<?> setToList(Set<?> set){
		ArrayList list = new ArrayList();
		if(!set.equals(null) && !set.isEmpty()){
			for(Object sb : set)
	    	{
	    		list.add(sb);
	    	}
		} 	
    	return list;
	}
	
	public static Set<?> listToSet(List<?> list){
		HashSet set = new HashSet();
		if(!list.equals(null) && !list.isEmpty())
		{
	    	for(Object sb : list)
	    	{
	    		set.add(sb);
	    	}
		}
    	return set;
	}
	
	public static boolean isOtherAttachable(Material mat){
		if(getOtherAttachablesUp().contains(mat) || getOtherAttachablesSide().contains(mat))
			return true;
		return false;
	}
	
	
	public static ArrayList<Entity> getNearbyEntities(Block block, double distance){
		double radius = distance;
		List<Entity> entities = block.getLocation().getWorld().getEntities();
		ArrayList<Entity> near = new ArrayList<Entity>();
		for(Entity e : entities) {
		    if(e.getLocation().distance(block.getLocation()) <= radius){
		    	near.add(e);
		    }
		}
		return near;
	}
	
	public static ArrayList<Block> getNearbyLiquids(Block block){
		ArrayList<Block> blocks = new ArrayList<Block>();
		for(int i = -2; i <= 2; i++){
			Location l = block.getLocation();
			l.add(i, i, i);
			Block check = l.getBlock();
			if(check.getType().equals(Material.WATER)){
				blocks.add(check);
			}
		}
		return blocks;
	}
	
	//make this a list in a seperate config file for easy/user updatabililty
	private static List<Material> getOtherAttachablesUp(){
		List<Material> PHYSICS_UP = new ArrayList<Material>();
		PHYSICS_UP.add(Material.ACACIA_DOOR);
		PHYSICS_UP.add(Material.ACACIA_DOOR);
        PHYSICS_UP.add(Material.ACACIA_PRESSURE_PLATE);
        PHYSICS_UP.add(Material.ACACIA_SAPLING);
        PHYSICS_UP.add(Material.ACACIA_SIGN);
        PHYSICS_UP.add(Material.ACTIVATOR_RAIL);
        PHYSICS_UP.add(Material.ALLIUM);
        PHYSICS_UP.add(Material.ATTACHED_MELON_STEM);
        PHYSICS_UP.add(Material.ATTACHED_PUMPKIN_STEM);
        PHYSICS_UP.add(Material.AZURE_BLUET);
        PHYSICS_UP.add(Material.BAMBOO_SAPLING);
        PHYSICS_UP.add(Material.BEETROOTS);
        PHYSICS_UP.add(Material.BELL);
        PHYSICS_UP.add(Material.BIRCH_DOOR);
        PHYSICS_UP.add(Material.BIRCH_PRESSURE_PLATE);
        PHYSICS_UP.add(Material.BIRCH_SAPLING);
        PHYSICS_UP.add(Material.BIRCH_SIGN);
        PHYSICS_UP.add(Material.BLACK_BANNER);
        PHYSICS_UP.add(Material.BLACK_CARPET);
        PHYSICS_UP.add(Material.BLUE_BANNER);
        PHYSICS_UP.add(Material.BLUE_CARPET);
        PHYSICS_UP.add(Material.BLUE_ORCHID);
        PHYSICS_UP.add(Material.BROWN_BANNER);
        PHYSICS_UP.add(Material.BROWN_CARPET);
        PHYSICS_UP.add(Material.BROWN_MUSHROOM);
        PHYSICS_UP.add(Material.CAKE);
        PHYSICS_UP.add(Material.CARROTS);
        PHYSICS_UP.add(Material.COMPARATOR);
        PHYSICS_UP.add(Material.CORNFLOWER);
        PHYSICS_UP.add(Material.CYAN_BANNER);
        PHYSICS_UP.add(Material.CYAN_CARPET);
        PHYSICS_UP.add(Material.DANDELION);
        PHYSICS_UP.add(Material.DARK_OAK_DOOR);
        PHYSICS_UP.add(Material.DARK_OAK_PRESSURE_PLATE);
        PHYSICS_UP.add(Material.DARK_OAK_SAPLING);
        PHYSICS_UP.add(Material.DARK_OAK_SIGN);
        PHYSICS_UP.add(Material.DEAD_BUSH);
        PHYSICS_UP.add(Material.DETECTOR_RAIL);
        PHYSICS_UP.add(Material.FERN);
        PHYSICS_UP.add(Material.FIRE);
        PHYSICS_UP.add(Material.GRASS);
        PHYSICS_UP.add(Material.GRAY_BANNER);
        PHYSICS_UP.add(Material.GRAY_CARPET);
        PHYSICS_UP.add(Material.GREEN_BANNER);
        PHYSICS_UP.add(Material.GREEN_CARPET);
        PHYSICS_UP.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        PHYSICS_UP.add(Material.IRON_DOOR);
        PHYSICS_UP.add(Material.JUNGLE_DOOR);
        PHYSICS_UP.add(Material.JUNGLE_PRESSURE_PLATE);
        PHYSICS_UP.add(Material.JUNGLE_SAPLING);
        PHYSICS_UP.add(Material.JUNGLE_SIGN);
        PHYSICS_UP.add(Material.LANTERN);
        PHYSICS_UP.add(Material.LARGE_FERN);
        PHYSICS_UP.add(Material.LIGHT_BLUE_BANNER);
        PHYSICS_UP.add(Material.LIGHT_BLUE_CARPET);
        PHYSICS_UP.add(Material.LIGHT_GRAY_BANNER);
        PHYSICS_UP.add(Material.LIGHT_GRAY_CARPET);
        PHYSICS_UP.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        PHYSICS_UP.add(Material.LILAC);
        PHYSICS_UP.add(Material.LILY_OF_THE_VALLEY);
        PHYSICS_UP.add(Material.LILY_PAD);
        PHYSICS_UP.add(Material.LIME_BANNER);
        PHYSICS_UP.add(Material.LIME_CARPET);
        PHYSICS_UP.add(Material.MAGENTA_BANNER);
        PHYSICS_UP.add(Material.MAGENTA_CARPET);
        PHYSICS_UP.add(Material.MELON_STEM);
        PHYSICS_UP.add(Material.NETHER_PORTAL);
        PHYSICS_UP.add(Material.NETHER_WART);
        PHYSICS_UP.add(Material.OAK_DOOR);
        PHYSICS_UP.add(Material.OAK_PRESSURE_PLATE);
        PHYSICS_UP.add(Material.OAK_SAPLING);
        PHYSICS_UP.add(Material.OAK_SIGN);
        PHYSICS_UP.add(Material.ORANGE_BANNER);
        PHYSICS_UP.add(Material.ORANGE_CARPET);
        PHYSICS_UP.add(Material.ORANGE_TULIP);
        PHYSICS_UP.add(Material.OXEYE_DAISY);
        PHYSICS_UP.add(Material.PEONY);
        PHYSICS_UP.add(Material.PINK_BANNER);
        PHYSICS_UP.add(Material.PINK_CARPET);
        PHYSICS_UP.add(Material.PINK_TULIP);
        PHYSICS_UP.add(Material.POPPY);
        PHYSICS_UP.add(Material.POTATOES);
        PHYSICS_UP.add(Material.POWERED_RAIL);
        PHYSICS_UP.add(Material.PUMPKIN_STEM);
        PHYSICS_UP.add(Material.PURPLE_BANNER);
        PHYSICS_UP.add(Material.PURPLE_CARPET);
        PHYSICS_UP.add(Material.RAIL);
        PHYSICS_UP.add(Material.REDSTONE_TORCH);
        PHYSICS_UP.add(Material.REDSTONE_WIRE);
        PHYSICS_UP.add(Material.RED_BANNER);
        PHYSICS_UP.add(Material.RED_CARPET);
        PHYSICS_UP.add(Material.RED_MUSHROOM);
        PHYSICS_UP.add(Material.RED_TULIP);
        PHYSICS_UP.add(Material.REPEATER);
        PHYSICS_UP.add(Material.ROSE_BUSH);
        PHYSICS_UP.add(Material.SNOW);
        PHYSICS_UP.add(Material.SPRUCE_DOOR);
        PHYSICS_UP.add(Material.SPRUCE_PRESSURE_PLATE);
        PHYSICS_UP.add(Material.SPRUCE_SAPLING);
        PHYSICS_UP.add(Material.SPRUCE_SIGN);
        PHYSICS_UP.add(Material.STONE_PRESSURE_PLATE);
        PHYSICS_UP.add(Material.SUNFLOWER);
        PHYSICS_UP.add(Material.SWEET_BERRY_BUSH);
        PHYSICS_UP.add(Material.TALL_GRASS);
        PHYSICS_UP.add(Material.TORCH);
        PHYSICS_UP.add(Material.SAND);
        PHYSICS_UP.add(Material.RED_SAND);
        PHYSICS_UP.add(Material.ANVIL);
        PHYSICS_UP.add(Material.GRAVEL);
        PHYSICS_UP.add(Material.WHEAT);
        PHYSICS_UP.add(Material.WHITE_BANNER);
        PHYSICS_UP.add(Material.WHITE_CARPET);
        PHYSICS_UP.add(Material.WHITE_TULIP);
        PHYSICS_UP.add(Material.WITHER_ROSE);
        PHYSICS_UP.add(Material.YELLOW_BANNER);
        PHYSICS_UP.add(Material.YELLOW_CARPET);
        return PHYSICS_UP;
	}
	
	//make this a list in a seperate config file for easy/user updatabililty
	private static List<Material> getOtherAttachablesSide(){
		List<Material> PHYSICS_SIDE = new ArrayList<Material>();
        PHYSICS_SIDE.add(Material.LADDER);
        PHYSICS_SIDE.add(Material.OAK_BUTTON);
        PHYSICS_SIDE.add(Material.SPRUCE_BUTTON);
        PHYSICS_SIDE.add(Material.ACACIA_BUTTON);
        PHYSICS_SIDE.add(Material.BIRCH_BUTTON);
        PHYSICS_SIDE.add(Material.JUNGLE_BUTTON);
        PHYSICS_SIDE.add(Material.DARK_OAK_BUTTON);
        PHYSICS_SIDE.add(Material.OAK_SIGN);
        PHYSICS_SIDE.add(Material.SPRUCE_SIGN);
        PHYSICS_SIDE.add(Material.ACACIA_SIGN);
        PHYSICS_SIDE.add(Material.BIRCH_SIGN);
        PHYSICS_SIDE.add(Material.JUNGLE_SIGN);
        PHYSICS_SIDE.add(Material.DARK_OAK_SIGN);
        PHYSICS_SIDE.add(Material.SPRUCE_TRAPDOOR);
        PHYSICS_SIDE.add(Material.ACACIA_TRAPDOOR);
        PHYSICS_SIDE.add(Material.BIRCH_TRAPDOOR);
        PHYSICS_SIDE.add(Material.JUNGLE_TRAPDOOR);
            PHYSICS_SIDE.add(Material.DARK_OAK_TRAPDOOR);
        PHYSICS_SIDE.add(Material.STONE_BUTTON);
        PHYSICS_SIDE.add(Material.TORCH);
        PHYSICS_SIDE.add(Material.REDSTONE_TORCH);
        PHYSICS_SIDE.add(Material.LEVER);
        PHYSICS_SIDE.add(Material.TRIPWIRE);
        return PHYSICS_SIDE;
	}
}