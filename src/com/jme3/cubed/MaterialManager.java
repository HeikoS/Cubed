package com.jme3.cubed;

import java.util.HashMap;

/**
 *
 * @author Carl
 * TODO: Needs a complete re-write, shouldn't store 256 static types
 */
public class MaterialManager {
    
    private static MaterialManager instance;
    public static MaterialManager getInstance() {
        if (instance == null) {
            instance = new MaterialManager();
        }
        return instance;
    }
    private final HashMap<Class<? extends Block>, BlockType> BLOCK_TYPES = new HashMap<>();
    private final BlockType[] TYPES_BLOCKS = new BlockType[256];
    private byte nextBlockType = 1;
    
    private MaterialManager() { }

    public void register(Class<? extends Block> blockClass, BlockSkin skin){
        BlockType blockType = new BlockType(nextBlockType, skin);
        BLOCK_TYPES.put(blockClass, blockType);
        TYPES_BLOCKS[nextBlockType] = blockType;
        nextBlockType++;
    }
    
    public BlockType getType(Class<? extends Block> blockClass){
        return BLOCK_TYPES.get(blockClass);
    }
    
    public BlockType getType(byte type){
        return TYPES_BLOCKS[type];
    }
}
