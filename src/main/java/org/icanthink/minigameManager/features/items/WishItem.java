package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.utils.ChatGPTClient;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.EventHandler;

import java.util.Arrays;
import java.util.List;

public class WishItem extends CustomItem {
    private final ChatGPTClient chatGPTClient;

    public WishItem(Minigame minigame) {
        super(minigame);
        this.chatGPTClient = new ChatGPTClient(MinigameManager.plugin.getConfig());
    }

    @Override
    protected String getName() {
        return ChatColor.GOLD + "Magic Wish";
    }

    @Override
    protected Material getMaterial() {
        return Material.SUNFLOWER;
    }

    @Override
    protected List<String> getLore() {
        return Arrays.asList(
            ChatColor.YELLOW + "Right-click to make a wish!",
            ChatColor.GRAY + "Tell me your wish and I'll",
            ChatColor.GRAY + "make it come true with commands..."
        );
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (!isInstance(event.getItem())) return;

        Player player = event.getPlayer();
        player.sendMessage(ChatColor.GOLD + "* What is your wish? Type it in chat!");

        // Set up a chat listener for the next message
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onPlayerChat(org.bukkit.event.player.AsyncPlayerChatEvent chatEvent) {
                if (chatEvent.getPlayer() != player) return;

                chatEvent.setCancelled(true);
                String wish = chatEvent.getMessage();

                // Unregister this listener
                org.bukkit.event.HandlerList.unregisterAll(this);

                player.sendMessage(ChatColor.GOLD + "* Processing your wish: " + ChatColor.WHITE + wish);

                // Create the prompt for ChatGPT
                String prompt = String.format(
                    "You are an evil Minecraft command generator. The player %s has made the following wish: '%s'. " +
                    "The player is at position %d, %d, %d." +
                    "Respond ONLY with Minecraft commands (one per line) that would help fulfill this wish. " +
                    "Use only vanilla Minecraft commands. Each command should be executable by an operator. " +
                    "Keep the response under 5 commands. Don't include any explanations, just the commands. " +
                    "Fulfill the wish as best as possible, but try to make the effects of the wish harmful to the player. " +
                    "Do not give the player anything too overpowered. The wish should always do more bad than good. " +
                    "Don't just give random bad effects, make the harmful effects a result of the wish.",
                    player.getName(),
                    wish,
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(),
                    player.getLocation().getBlockZ()
                );

                // Send to ChatGPT asynchronously
                chatGPTClient.sendMessage(prompt).thenAccept(response -> {
                    player.sendMessage(ChatColor.GOLD + "* Your wish is my command!");

                    // Execute each command
                    for (String command : response.split("\n")) {
                        MinigameManager.plugin.getServer().getScheduler().runTask(
                            MinigameManager.plugin,
                            () -> {
                                // Get the player's current world at time of execution
                                World currentWorld = player.getWorld();
                                // Execute command in the player's current world
                                MinigameManager.plugin.getServer().dispatchCommand(
                                    MinigameManager.plugin.getServer().getConsoleSender(),
                                    command.trim().startsWith("/") ?
                                        String.format("execute in %s run %s", currentWorld.getName(), command.trim().substring(1)) :
                                        String.format("execute in %s run %s", currentWorld.getName(), command.trim())
                                );
                            }
                        );
                    }
                }).exceptionally(e -> {
                    player.sendMessage(ChatColor.RED + "* Sorry, I couldn't process your wish right now!");
                    e.printStackTrace();
                    return null;
                });
            }
        }, MinigameManager.plugin);

        event.setCancelled(true);
    }
}