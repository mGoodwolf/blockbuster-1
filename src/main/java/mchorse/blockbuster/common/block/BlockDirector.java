package mchorse.blockbuster.common.block;

import java.util.List;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.aperture.CameraHandler;
import mchorse.blockbuster.common.GuiHandler;
import mchorse.blockbuster.common.item.ItemPlayback;
import mchorse.blockbuster.common.item.ItemRegister;
import mchorse.blockbuster.common.tileentity.TileEntityDirector;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.common.director.PacketDirectorCast;
import mchorse.blockbuster.utils.L10n;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Director block
 *
 * This block is the one that responsible for machinimas creation.
 */
public class BlockDirector extends Block implements ITileEntityProvider
{
    /**
     * The only property of director block (playing state) 
     */
    public static final PropertyBool PLAYING = PropertyBool.create("playing");

    public BlockDirector()
    {
        super(Material.ROCK);
        this.setDefaultState(this.getDefaultState().withProperty(PLAYING, false));
        this.setCreativeTab(Blockbuster.blockbusterTab);
        this.setBlockUnbreakable();
        this.setResistance(6000000.0F);
        this.setRegistryName("director");
        this.setUnlocalizedName("blockbuster.director");
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
        tooltip.add(I18n.format("blockbuster.info.director_block"));
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return true;
    }

    /* States */

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(PLAYING) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(PLAYING, meta == 1);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {PLAYING});
    }

    /* Redstone */

    @Override
    public boolean canProvidePower(IBlockState state)
    {
        return true;
    }

    /**
     * Power west side of the block while block is playing and power east side
     * of the block while isn't playback actors.
     */
    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        boolean isPlaying = blockState.getValue(PLAYING);

        return (isPlaying && side == EnumFacing.WEST) || (!isPlaying && side == EnumFacing.EAST) ? 15 : 0;
    }

    /* Player interaction */

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack item = playerIn.getHeldItemMainhand();

        if (item != null && this.handleItem(item, worldIn, pos, playerIn))
        {
            return true;
        }

        if (!worldIn.isRemote)
        {
            this.displayCast(playerIn, worldIn, pos);
        }

        return true;
    }

    protected boolean handleItem(ItemStack item, World world, BlockPos pos, EntityPlayer player)
    {
        return this.handlePlaybackItem(item, world, pos, player) || this.handleRegisterItem(item, world, pos, player);
    }

    private boolean handleRegisterItem(ItemStack item, World world, BlockPos pos, EntityPlayer player)
    {
        if (!(item.getItem() instanceof ItemRegister))
        {
            return false;
        }

        if (world.isRemote)
        {
            return true;
        }

        ((ItemRegister) item.getItem()).registerStack(item, pos);
        L10n.success(player, "director.attached_device");

        return true;
    }

    /**
     * Attach recording item to current director block
     */
    protected boolean handlePlaybackItem(ItemStack item, World world, BlockPos pos, EntityPlayer player)
    {
        if (!(item.getItem() instanceof ItemPlayback))
        {
            return false;
        }

        ItemPlayback.saveBlockPos("Dir", item, pos);

        if (CameraHandler.isApertureLoaded())
        {
            GuiHandler.open(player, GuiHandler.PLAYBACK, 0, 0, 0);
        }

        return true;
    }

    protected void displayCast(EntityPlayer player, World worldIn, BlockPos pos)
    {
        TileEntityDirector tile = (TileEntityDirector) worldIn.getTileEntity(pos);
        Dispatcher.sendTo(new PacketDirectorCast(tile.getPos(), tile.replays), (EntityPlayerMP) player);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityDirector();
    }
}