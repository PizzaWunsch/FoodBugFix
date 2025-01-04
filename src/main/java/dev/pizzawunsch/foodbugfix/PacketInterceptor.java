package dev.pizzawunsch.foodbugfix;
import io.netty.channel.*;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;

public class PacketInterceptor {

    public static void initialize() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            injectPlayer(player);
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event) {
                injectPlayer(event.getPlayer());
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                removeInjection(event.getPlayer());
            }
        }, FoodBugFix.getInstance());
    }

    private static void injectPlayer(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ChannelPipeline pipeline = craftPlayer.getHandle().playerConnection.networkManager.channel.pipeline();

        pipeline.addBefore("packet_handler", player.getName() + "_interceptor", new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof Packet) {
                    handleIncomingPacket(player, (Packet<?>) msg);
                }
                super.channelRead(ctx, msg);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof Packet) {
                    handleOutgoingPacket(player, (Packet<?>) msg);
                }
                super.write(ctx, msg, promise);
            }
        });
    }

    private static void removeInjection(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ChannelPipeline pipeline = craftPlayer.getHandle().playerConnection.networkManager.channel.pipeline();

        if (pipeline.get(player.getName() + "_interceptor") != null) {
            pipeline.remove(player.getName() + "_interceptor");
        }
    }

    private static void handleIncomingPacket(Player player, Packet<?> packet) {
        if (packet instanceof PacketPlayOutAnimation) {
            PacketPlayOutAnimation animationPacket = (PacketPlayOutAnimation) packet;

            try {
                Field entityIdField = PacketPlayOutAnimation.class.getDeclaredField("a");
                Field animationIdField = PacketPlayOutAnimation.class.getDeclaredField("b");

                entityIdField.setAccessible(true);
                animationIdField.setAccessible(true);

                int entityId = (int) entityIdField.get(animationPacket);
                int animationId = (int) animationIdField.get(animationPacket);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleOutgoingPacket(Player player, Packet<?> packet) {
        if (packet instanceof PacketPlayOutAnimation) {
            PacketPlayOutAnimation animationPacket = (PacketPlayOutAnimation) packet;

            try {
                Field entityIdField = PacketPlayOutAnimation.class.getDeclaredField("a");
                Field animationIdField = PacketPlayOutAnimation.class.getDeclaredField("b");

                entityIdField.setAccessible(true);
                animationIdField.setAccessible(true);

                int animationId = (int) animationIdField.get(animationPacket);
                if (animationId == 3) {
                    FoodBugFix.getInstance().getDropper().add(player);
                    Bukkit.getScheduler().runTaskLater(FoodBugFix.getInstance(), ()-> {
                        FoodBugFix.getInstance().getDropper().remove(player);
                    },10);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
