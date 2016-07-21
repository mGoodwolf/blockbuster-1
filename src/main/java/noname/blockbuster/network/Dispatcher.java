package noname.blockbuster.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import noname.blockbuster.Blockbuster;
import noname.blockbuster.network.client.ClientHandlerCameraProfile;
import noname.blockbuster.network.client.ClientHandlerCameraState;
import noname.blockbuster.network.client.ClientHandlerDirectorCast;
import noname.blockbuster.network.client.ClientHandlerDirectorMapCast;
import noname.blockbuster.network.client.ClientHandlerModifyActor;
import noname.blockbuster.network.client.ClientHandlerPlayerRecording;
import noname.blockbuster.network.common.PacketCameraProfile;
import noname.blockbuster.network.common.PacketCameraState;
import noname.blockbuster.network.common.PacketLoadCameraProfile;
import noname.blockbuster.network.common.PacketModifyActor;
import noname.blockbuster.network.common.PacketPlayerRecording;
import noname.blockbuster.network.common.director.PacketDirectorCast;
import noname.blockbuster.network.common.director.PacketDirectorMapAdd;
import noname.blockbuster.network.common.director.PacketDirectorMapCast;
import noname.blockbuster.network.common.director.PacketDirectorMapEdit;
import noname.blockbuster.network.common.director.PacketDirectorMapRemove;
import noname.blockbuster.network.common.director.PacketDirectorMapReset;
import noname.blockbuster.network.common.director.PacketDirectorRemove;
import noname.blockbuster.network.common.director.PacketDirectorRequestCast;
import noname.blockbuster.network.common.director.PacketDirectorReset;
import noname.blockbuster.network.server.ServerHandlerCameraProfile;
import noname.blockbuster.network.server.ServerHandlerDirectorMapAdd;
import noname.blockbuster.network.server.ServerHandlerDirectorMapEdit;
import noname.blockbuster.network.server.ServerHandlerDirectorMapRemove;
import noname.blockbuster.network.server.ServerHandlerDirectorMapReset;
import noname.blockbuster.network.server.ServerHandlerDirectorRemove;
import noname.blockbuster.network.server.ServerHandlerDirectorRequestCast;
import noname.blockbuster.network.server.ServerHandlerDirectorReset;
import noname.blockbuster.network.server.ServerHandlerLoadCameraProfile;
import noname.blockbuster.network.server.ServerHandlerModifyActor;

/**
 * Network dispatcher
 *
 * @author Ernio (Ernest Sadowski)
 */
public class Dispatcher
{
    private static final SimpleNetworkWrapper DISPATCHER = NetworkRegistry.INSTANCE.newSimpleChannel(Blockbuster.MODID);
    private static byte PACKET_ID;

    public static SimpleNetworkWrapper get()
    {
        return DISPATCHER;
    }

    public static void updateTrackers(Entity entity, IMessage message)
    {
        EntityTracker et = ((WorldServer) entity.worldObj).getEntityTracker();

        for (EntityPlayer player : et.getTrackingPlayers(entity))
        {
            DISPATCHER.sendTo(message, (EntityPlayerMP) player);
        }
    }

    public static void sendTo(IMessage message, EntityPlayerMP player)
    {
        DISPATCHER.sendTo(message, player);
    }

    public static void sendToServer(IMessage message)
    {
        DISPATCHER.sendToServer(message);
    }

    /**
     * Register all the networking messages and message handlers
     */
    public static void register()
    {
        /* Update actor properties */
        register(PacketModifyActor.class, ClientHandlerModifyActor.class, Side.CLIENT);
        register(PacketModifyActor.class, ServerHandlerModifyActor.class, Side.SERVER);

        /* Show up recording label when player starts recording */
        register(PacketPlayerRecording.class, ClientHandlerPlayerRecording.class, Side.CLIENT);

        /* Director block management messages */
        register(PacketDirectorCast.class, ClientHandlerDirectorCast.class, Side.CLIENT);

        register(PacketDirectorRequestCast.class, ServerHandlerDirectorRequestCast.class, Side.SERVER);
        register(PacketDirectorReset.class, ServerHandlerDirectorReset.class, Side.SERVER);
        register(PacketDirectorRemove.class, ServerHandlerDirectorRemove.class, Side.SERVER);

        /* Director block map management messages */
        register(PacketDirectorMapCast.class, ClientHandlerDirectorMapCast.class, Side.CLIENT);

        register(PacketDirectorMapAdd.class, ServerHandlerDirectorMapAdd.class, Side.SERVER);
        register(PacketDirectorMapEdit.class, ServerHandlerDirectorMapEdit.class, Side.SERVER);
        register(PacketDirectorMapReset.class, ServerHandlerDirectorMapReset.class, Side.SERVER);
        register(PacketDirectorMapRemove.class, ServerHandlerDirectorMapRemove.class, Side.SERVER);

        /* Camera management */
        register(PacketCameraProfile.class, ClientHandlerCameraProfile.class, Side.CLIENT);
        register(PacketCameraProfile.class, ServerHandlerCameraProfile.class, Side.SERVER);
        register(PacketCameraState.class, ClientHandlerCameraState.class, Side.CLIENT);
        register(PacketLoadCameraProfile.class, ServerHandlerLoadCameraProfile.class, Side.SERVER);
    }

    private static <REQ extends IMessage, REPLY extends IMessage> void register(Class<REQ> message, Class<? extends IMessageHandler<REQ, REPLY>> handler, Side side)
    {
        DISPATCHER.registerMessage(handler, message, PACKET_ID++, side);
    }
}