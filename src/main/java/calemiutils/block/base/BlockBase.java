package calemiutils.block.base;

import calemiutils.CalemiUtils;
import calemiutils.init.InitBlocks;
import calemiutils.init.InitItems;
import calemiutils.registry.IHasModel;
import calemiutils.util.MaterialSound;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class BlockBase extends Block implements IHasModel {

    protected BlockBase(String name, MaterialSound matSound, float hardness, int harvestLevel, float resistance) {

        super(matSound.mat);
        setUnlocalizedName(name);
        setRegistryName(name);

        setSoundType(matSound.sound);
        setHardness(hardness);
        setHarvestLevel("pickaxe", harvestLevel);
        setResistance(resistance);

        if (getRegistryName() != null) {
            InitBlocks.BLOCKS.add(this);
            InitItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
        }
    }

    @Override
    public void registerModels() {

        CalemiUtils.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory");
    }
}
