package mchorse.blockbuster.commands;

import java.util.List;

import mchorse.blockbuster.common.tileentity.AbstractTileEntityDirector;
import mchorse.blockbuster.utils.BlockPos;
import mchorse.blockbuster.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Command /director
 *
 * This command is responsible for playing or stopping director block.
 *
 * This command is unified version of previous existed commands CommandStopDirector
 * and CommandPlayDirector. These unifications were made to avoid duplicate
 * code, and less command memorizing.
 */
public class CommandDirector extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "director";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "blockbuster.commands.director";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length < 4)
        {
            throw new WrongUsageException(this.getCommandUsage(null));
        }

        String action = args[0];
        BlockPos pos = new BlockPos(CommandBase.parseInt(sender, args[1]), CommandBase.parseInt(sender, args[2]), CommandBase.parseInt(sender, args[3]));
        AbstractTileEntityDirector director = this.getDirector(sender.getEntityWorld(), pos);

        if (director == null)
        {
            L10n.error(sender, "director.no_director", pos.getX(), pos.getY(), pos.getZ());
            return;
        }

        String play = "director.play";
        String stop = "director.stop";

        if (action.equals("play"))
        {
            if (director.isPlaying())
            {
                L10n.error(sender, "director.playing", args[1], args[2], args[3]);
                return;
            }

            director.startPlayback();
            L10n.success(sender, play, args[1], args[2], args[3]);
        }
        else if (action.equals("stop"))
        {
            if (!director.isPlaying())
            {
                L10n.error(sender, "director.stopped", args[1], args[2], args[3]);
                return;
            }

            director.stopPlayback();
            L10n.success(sender, stop, args[1], args[2], args[3]);
        }
        else if (action.equals("toggle"))
        {
            boolean isPlaying = director.togglePlayback();
            L10n.success(sender, isPlaying ? play : stop, args[1], args[2], args[3]);
        }
    }

    /**
     * Get abstract director from block pos
     */
    protected AbstractTileEntityDirector getDirector(World world, BlockPos pos)
    {
        TileEntity entity = world.getTileEntity(pos.x, pos.y, pos.z);

        if (entity instanceof AbstractTileEntityDirector)
        {
            return (AbstractTileEntityDirector) entity;
        }

        return null;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "play", "stop", "toggle");
        }

        return super.addTabCompletionOptions(sender, args);
    }
}