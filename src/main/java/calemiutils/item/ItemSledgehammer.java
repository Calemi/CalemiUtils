package calemiutils.item;

import calemiutils.CalemiUtils;
import calemiutils.init.InitItems;
import calemiutils.registry.IHasModel;
import calemiutils.util.Location;
import calemiutils.util.VeinScan;
import calemiutils.util.helper.ItemHelper;
import calemiutils.util.helper.SoundHelper;
import calemiutils.util.helper.WorldEditHelper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemSledgehammer extends ItemPickaxe implements IHasModel {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.CONCRETE_POWDER, Blocks.PLANKS, Blocks.BOOKSHELF, Blocks.LOG, Blocks.LOG2, Blocks.CHEST, Blocks.PUMPKIN, Blocks.LIT_PUMPKIN, Blocks.MELON_BLOCK, Blocks.LADDER, Blocks.WOODEN_BUTTON, Blocks.WOODEN_PRESSURE_PLATE);

    private double attackSpeed, attackDamage;

    public ItemSledgehammer(String name, Item.ToolMaterial toolMaterial, double attackSpeed) {
        super(toolMaterial);

        String realName = "sledgehammer_" + name;
        setUnlocalizedName(realName);
        setRegistryName(realName);
        setCreativeTab(CalemiUtils.TAB);
        InitItems.ITEMS.add(this);

        this.attackSpeed = attackSpeed;
        this.attackDamage = 4.0F + toolMaterial.getAttackDamage();
    }

    public float getAttackDamage()
    {
        return toolMaterial.getAttackDamage();
    }
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.BOW;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase e, int timeLeft) {

        EntityPlayer player = (EntityPlayer)e;

        EnumHand hand = EnumHand.OFF_HAND;

        if (ItemStack.areItemStacksEqual(player.getHeldItemMainhand(), stack)) {
            hand = EnumHand.MAIN_HAND;
        }

        if (getMaxItemUseDuration(stack) - timeLeft > 20) {

            player.swingArm(hand);

            Vec3d posVec = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            Vec3d lookVec = player.getLookVec();
            RayTraceResult trace = worldIn.rayTraceBlocks(posVec, posVec.add(lookVec.scale(5)));

            if (trace != null) {

                BlockPos pos = trace.getBlockPos();
                Location location = new Location(worldIn, pos);
                Material mat = location.getBlockMaterial();

                int[] ids = OreDictionary.getOreIDs(new ItemStack(location.getBlock()));

                for (int id : ids) {

                    if (OreDictionary.getOreName(id).contains("log")) {

                        fellTree(player, location);
                        return;
                    }
                }

                excavateRock(worldIn, stack, player, location);
            }
        }
    }

    private void excavateRock(World worldIn, ItemStack stack, EntityPlayer player, Location location) {

        ArrayList<Location> locations = WorldEditHelper.selectCubeFromRadius(location, 1, 1, 1);

        int damage = getDamage(stack);

        for (Location nextLocation : locations) {

            if (damage > getMaxDamage(stack)) {
                return;
            }

            float hardness = nextLocation.getBlockState().getBlockHardness(worldIn, nextLocation.getBlockPos());
            int harvestLevel = nextLocation.getBlock().getHarvestLevel(nextLocation.getBlockState());

            if (hardness >= 0 && hardness <= 50 && toolMaterial.getHarvestLevel() >= harvestLevel) {
                nextLocation.breakBlock(player);
                stack.damageItem(1, player);
                damage++;
            }
        }
    }

    private void fellTree(EntityPlayer player, Location location) {

        IBlockState state = location.getBlockState();

        VeinScan scan = new VeinScan(location, state);

        scan.startScan(10, true);

        for (Location nextLocation : scan.buffer) {

            nextLocation.breakBlock(player);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {

        ItemStack itemstack = playerIn.getHeldItem(handIn);

        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {

        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {

            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", attackDamage, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", attackSpeed, 0));
        }

        return multimap;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return ImmutableSet.of("pickaxe", "axe", "shovel");
    }

    @Override
    public boolean canHarvestBlock(IBlockState block) {
        return EFFECTIVE_ON.contains(block.getBlock()) || super.canHarvestBlock(block);
    }

    public float getDestroySpeed(ItemStack stack, IBlockState state) {

        Material material = state.getMaterial();
        if( material == Material.WOOD && material == Material.PLANTS && material == Material.VINE) {
            return this.efficiency;
        }

        return EFFECTIVE_ON.contains(state.getBlock()) ? this.efficiency : super.getDestroySpeed(stack, state);
    }

    @Override
    public void registerModels() {

        CalemiUtils.proxy.registerItemRenderer(this, 0, "inventory");
    }
}
