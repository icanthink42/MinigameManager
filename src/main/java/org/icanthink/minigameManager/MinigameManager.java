package org.icanthink.minigameManager;

import org.bukkit.plugin.java.JavaPlugin;
import org.icanthink.minigameManager.commands.HostMinigameCommand;
import org.icanthink.minigameManager.commands.JoinMinigameCommand;
import org.icanthink.minigameManager.commands.ListGamesCommand;
import org.icanthink.minigameManager.commands.StartHostedCommand;
import org.icanthink.minigameManager.commands.StartMinigameCommand;
import org.icanthink.minigameManager.commands.StartServerMinigameCommand;
import org.icanthink.minigameManager.commands.StopGameCommand;
import org.icanthink.minigameManager.commands.GiveCustomItemCommand;
import org.icanthink.minigameManager.commands.SummonCustomMobCommand;
import org.icanthink.minigameManager.commands.TriggerEventCommand;
import org.icanthink.minigameManager.tabcompleters.MinigameTabCompleter;

public final class MinigameManager extends JavaPlugin {

    private StartMinigameCommand startMinigameCommand;
    private HostMinigameCommand hostMinigameCommand;
    private JoinMinigameCommand joinMinigameCommand;
    private StartHostedCommand startHostedCommand;
    private StartServerMinigameCommand startServerMinigameCommand;
    private ListGamesCommand listGamesCommand;
    private StopGameCommand stopGameCommand;
    private GiveCustomItemCommand giveCustomItemCommand;
    private SummonCustomMobCommand summonCustomMobCommand;
    private TriggerEventCommand triggerEventCommand;
    private MinigameTabCompleter tabCompleter;
    public static MinigameManager plugin;

    @Override
    public void onEnable() {
        plugin = this;

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize commands
        startMinigameCommand = new StartMinigameCommand();
        hostMinigameCommand = new HostMinigameCommand();
        joinMinigameCommand = new JoinMinigameCommand();
        startHostedCommand = new StartHostedCommand();
        startServerMinigameCommand = new StartServerMinigameCommand();
        listGamesCommand = new ListGamesCommand();
        stopGameCommand = new StopGameCommand();
        giveCustomItemCommand = new GiveCustomItemCommand();
        summonCustomMobCommand = new SummonCustomMobCommand();
        triggerEventCommand = new TriggerEventCommand();

        // Initialize tab completer
        tabCompleter = new MinigameTabCompleter();

        // Register commands
        getCommand("hostgame").setExecutor(hostMinigameCommand);
        getCommand("joinminigame").setExecutor(joinMinigameCommand);
        getCommand("startgame").setExecutor(startHostedCommand);
        getCommand("startservergame").setExecutor(startServerMinigameCommand);
        getCommand("listgames").setExecutor(listGamesCommand);
        getCommand("stopgame").setExecutor(stopGameCommand);
        getCommand("givecustomitem").setExecutor(giveCustomItemCommand);
        getCommand("summoncustommob").setExecutor(summonCustomMobCommand);
        getCommand("triggerevent").setExecutor(triggerEventCommand);

        // Register tab completers
        getCommand("hostgame").setTabCompleter(tabCompleter);
        getCommand("joinminigame").setTabCompleter(tabCompleter);
        getCommand("startgame").setTabCompleter(tabCompleter);
        getCommand("startservergame").setTabCompleter(tabCompleter);
        getCommand("stopgame").setTabCompleter(tabCompleter);
        getCommand("givecustomitem").setTabCompleter(tabCompleter);
        getCommand("summoncustommob").setTabCompleter(tabCompleter);
        getCommand("triggerevent").setTabCompleter(tabCompleter);

        // Log plugin startup
        getLogger().info("MinigameManager has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save config before disabling
        saveConfig();

        // Plugin shutdown logic
        getLogger().info("MinigameManager has been disabled!");
    }

    /**
     * Get the StartMinigameCommand instance
     *
     * @return The StartMinigameCommand instance
     */
    public StartMinigameCommand getStartMinigameCommand() {
        return startMinigameCommand;
    }

    /**
     * Get the HostMinigameCommand instance
     *
     * @return The HostMinigameCommand instance
     */
    public HostMinigameCommand getHostMinigameCommand() {
        return hostMinigameCommand;
    }

    /**
     * Get the JoinMinigameCommand instance
     *
     * @return The JoinMinigameCommand instance
     */
    public JoinMinigameCommand getJoinMinigameCommand() {
        return joinMinigameCommand;
    }

    /**
     * Get the StartHostedCommand instance
     *
     * @return The StartHostedCommand instance
     */
    public StartHostedCommand getStartHostedCommand() {
        return startHostedCommand;
    }

    /**
     * Get the StartServerMinigameCommand instance
     *
     * @return The StartServerMinigameCommand instance
     */
    public StartServerMinigameCommand getStartServerMinigameCommand() {
        return startServerMinigameCommand;
    }

    /**
     * Get the ListGamesCommand instance
     *
     * @return The ListGamesCommand instance
     */
    public ListGamesCommand getListGamesCommand() {
        return listGamesCommand;
    }

    /**
     * Get the StopGameCommand instance
     *
     * @return The StopGameCommand instance
     */
    public StopGameCommand getStopGameCommand() {
        return stopGameCommand;
    }

    /**
     * Get the GiveCustomItemCommand instance
     *
     * @return The GiveCustomItemCommand instance
     */
    public GiveCustomItemCommand getGiveCustomItemCommand() {
        return giveCustomItemCommand;
    }

    /**
     * Get the SummonCustomMobCommand instance
     *
     * @return The SummonCustomMobCommand instance
     */
    public SummonCustomMobCommand getSummonCustomMobCommand() {
        return summonCustomMobCommand;
    }

    /**
     * Get the TriggerEventCommand instance
     *
     * @return The TriggerEventCommand instance
     */
    public TriggerEventCommand getTriggerEventCommand() {
        return triggerEventCommand;
    }

    /**
     * Get the MinigameTabCompleter instance
     *
     * @return The MinigameTabCompleter instance
     */
    public MinigameTabCompleter getTabCompleter() {
        return tabCompleter;
    }
}
