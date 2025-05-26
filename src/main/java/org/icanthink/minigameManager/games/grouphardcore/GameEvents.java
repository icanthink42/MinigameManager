package org.icanthink.minigameManager.games.grouphardcore;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.icanthink.minigameManager.features.items.BrazilStick;
import org.icanthink.minigameManager.features.items.CursedPumpkinItem;
import org.icanthink.minigameManager.features.items.Shoes;
import org.icanthink.minigameManager.features.mobs.InvincibleZombie;
import org.icanthink.minigameManager.features.mobs.BusinessVillager;
import org.icanthink.minigameManager.features.mobs.SpecialDog;
import org.icanthink.minigameManager.MinigameManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains all possible random events for GroupHardcore.
 */
public class GameEvents implements Listener {
    private static final Random random = new Random();
    private static final Map<GroupHardcore, Boolean> zombieEventTriggered = new WeakHashMap<>();
    private static final Map<GroupHardcore, Boolean> businessEventTriggered = new WeakHashMap<>();
    private static final Map<String, Set<UUID>> usedDogLinks = new WeakHashMap<>();

    static {
        // Register this class as a listener
        Bukkit.getPluginManager().registerEvents(new GameEvents(), MinigameManager.plugin);
    }

    public static final List<Consumer<GroupHardcore>> EVENTS = Arrays.asList(
        game -> {
            List<Player> players = new ArrayList<>(game.getPlayers());
            if (!players.isEmpty()) {
                Player p = players.get(random.nextInt(players.size()));
                game.getItemManager().giveItem(p, BrazilStick.class);
                p.sendMessage(ChatColor.GOLD + "You received a Brazil Stick!");
                for (Player other : game.getPlayers()) {
                    if (other != p) {
                        other.sendMessage(ChatColor.GOLD + p.getName() + " received a Brazil Stick!");
                    }
                }
            }
        },
        game -> {
            List<Player> players = new ArrayList<>(game.getPlayers());
            if (!players.isEmpty()) {
                Player p = players.get(random.nextInt(players.size()));
                game.getItemManager().giveItem(p, CursedPumpkinItem.class);
                p.sendMessage(ChatColor.GOLD + "You received a Cursed Pumpkin!");
                for (Player other : game.getPlayers()) {
                    if (other != p) {
                        other.sendMessage(ChatColor.GOLD + p.getName() + " received a Cursed Pumpkin!");
                    }
                }
            }
        },
        game -> {
            List<Player> players = new ArrayList<>(game.getPlayers());
            if (!players.isEmpty()) {
                Player p = players.get(random.nextInt(players.size()));
                game.getItemManager().giveItem(p, Shoes.class);
                p.sendMessage(ChatColor.GOLD + "You received Pretty Good Quality Shoes!");
                for (Player other : game.getPlayers()) {
                    if (other != p) {
                        other.sendMessage(ChatColor.GOLD + p.getName() + " received Pretty Good Quality Shoes!");
                    }
                }
            }
        },
        game -> {
            // Check if zombie event has already been triggered for this game
            if (zombieEventTriggered.getOrDefault(game, false)) {
                return;
            }

            // Only trigger if it's night time (time > 13000 and < 23000)
            World world = game.getWorld();
            long time = world.getTime();
            if (time < 13000 || time > 23000) {
                return;
            }

            List<Player> players = new ArrayList<>(game.getPlayers());
            if (!players.isEmpty()) {
                Player target = players.get(random.nextInt(players.size()));
                Location spawnLoc = target.getLocation().add(random.nextInt(20) - 10, 0, random.nextInt(20) - 10);

                // Find safe Y coordinate
                spawnLoc.setY(world.getHighestBlockYAt(spawnLoc));

                // Spawn the zombie using CustomMobManager
                game.getMobManager().spawnMob(spawnLoc, InvincibleZombie.class);

                // Mark zombie event as triggered for this game
                zombieEventTriggered.put(game, true);
            }
        },
        game -> {
            // Check if business event has already been triggered for this game
            if (businessEventTriggered.getOrDefault(game, false)) {
                return;
            }

            List<Player> players = new ArrayList<>(game.getPlayers());
            if (!players.isEmpty()) {
                Player target = players.get(random.nextInt(players.size()));
                game.getMobManager().spawnMob(target.getLocation(), BusinessVillager.class);
                for (Player p : game.getPlayers()) {
                    p.sendMessage(ChatColor.GOLD + "Mr. Business has arrived!");
                }
                // Mark business event as triggered for this game
                businessEventTriggered.put(game, true);
            }
        },
        game -> {
            // Generate a unique ID for this dog link
            String linkId = UUID.randomUUID().toString();
            usedDogLinks.put(linkId, new HashSet<>());

            // Create clickable text component
            TextComponent message = new TextComponent(ChatColor.GREEN + "Click here for a free dog!");
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claimdog " + linkId));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to get your special dog!").create()));

            // Send to all players
            for (Player p : game.getPlayers()) {
                p.spigot().sendMessage(message);
            }

            // Register command handler if not already registered
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onCommand(PlayerCommandPreprocessEvent event) {
                    if (event.getMessage().startsWith("/claimdog " + linkId)) {
                        event.setCancelled(true);
                        Player player = event.getPlayer();

                        // Check if player has already used this link
                        Set<UUID> usedPlayers = usedDogLinks.get(linkId);
                        if (usedPlayers != null && !usedPlayers.contains(player.getUniqueId())) {
                            // Spawn the special dog
                            game.getMobManager().spawnMob(player.getLocation(), SpecialDog.class);
                            usedPlayers.add(player.getUniqueId());
                        } else {
                            player.sendMessage(ChatColor.RED + "This dog link has already been used!");
                        }
                    }
                }
            }, MinigameManager.plugin);
        }
    );
}