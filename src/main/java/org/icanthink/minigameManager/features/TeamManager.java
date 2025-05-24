package org.icanthink.minigameManager.features;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.icanthink.minigameManager.Minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature that provides team management functionality for minigames.
 */
public class TeamManager extends Feature implements Listener {
    private final Scoreboard scoreboard;
    private final Map<String, Team> teamMap;
    private final Map<Player, String> playerTeams;
    private final String[] teamColors;

    public TeamManager(Minigame minigame, Scoreboard scoreboard, String[] teamColors) {
        super(minigame);
        this.scoreboard = scoreboard;
        this.teamMap = new HashMap<>();
        this.playerTeams = new HashMap<>();
        this.teamColors = teamColors;
        initializeTeams();
    }

    /**
     * Get the scoreboard being used for team management.
     *
     * @return The scoreboard instance
     */
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    /**
     * Get the map of team names to Team objects.
     *
     * @return Map of team names to Team objects
     */
    public Map<String, Team> getTeamMap() {
        return teamMap;
    }

    /**
     * Get the map of players to their team names.
     *
     * @return Map of players to their team names
     */
    public Map<Player, String> getPlayerTeams() {
        return playerTeams;
    }

    /**
     * Get the list of available team colors.
     *
     * @return Array of team color names
     */
    public String[] getTeamColors() {
        return teamColors;
    }

    /**
     * Initialize teams with the given colors.
     * This should be called during minigame setup.
     */
    public void initializeTeams() {
        // Create teams for each color
        for (String color : teamColors) {
            Team team = scoreboard.registerNewTeam(color.toLowerCase());
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);

            // Set team color
            try {
                team.setColor(ChatColor.valueOf(color));
            } catch (IllegalArgumentException e) {
                team.setColor(ChatColor.WHITE);
            }

            teamMap.put(color, team);
        }
    }

    /**
     * Assign players to teams, distributing them evenly.
     *
     * @param players List of players to assign to teams
     */
    public void assignPlayersToTeams(List<Player> players) {
        // Calculate how many teams to use based on player count
        int teamsToUse = Math.min(teamColors.length, Math.max(2, players.size() / 2));
        String[] activeTeams = new String[teamsToUse];

        // Select teams to use
        System.arraycopy(teamColors, 0, activeTeams, 0, teamsToUse);

        // Clear existing team assignments
        for (Team team : teamMap.values()) {
            for (String entry : team.getEntries()) {
                team.removeEntry(entry);
            }
        }
        playerTeams.clear();

        // Assign players to teams
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            String teamName = activeTeams[i % teamsToUse];
            Team team = teamMap.get(teamName);

            addPlayerToTeam(player, teamName);
        }
    }

    /**
     * Add a player to a specific team.
     *
     * @param player The player to add
     * @param teamName The name of the team to add the player to
     */
    public void addPlayerToTeam(Player player, String teamName) {
        Team team = teamMap.get(teamName);
        if (team != null) {
            team.addEntry(player.getName());
            playerTeams.put(player, teamName);

            // Set player name color
            player.setDisplayName(team.getColor() + player.getName() + ChatColor.RESET);
            player.setPlayerListName(team.getColor() + player.getName() + ChatColor.RESET);
        }
    }

    /**
     * Remove a player from their team.
     *
     * @param player The player to remove
     */
    public void removePlayerFromTeam(Player player) {
        String teamName = playerTeams.get(player);
        if (teamName != null) {
            Team team = teamMap.get(teamName);
            if (team != null) {
                team.removeEntry(player.getName());
            }
            playerTeams.remove(player);

            // Reset player name
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
        }
    }

    /**
     * Get the number of living players in each team.
     *
     * @param deadPlayers List of dead players
     * @return Map of team names to their living player count
     */
    public Map<String, Integer> getTeamLivingPlayers(List<Player> deadPlayers) {
        Map<String, Integer> teamLivingPlayers = new HashMap<>();

        // Initialize counts
        for (String teamName : teamMap.keySet()) {
            teamLivingPlayers.put(teamName, 0);
        }

        // Count living players in each team
        for (Map.Entry<Player, String> entry : playerTeams.entrySet()) {
            Player player = entry.getKey();
            String teamName = entry.getValue();

            if (!deadPlayers.contains(player)) {
                int count = teamLivingPlayers.getOrDefault(teamName, 0);
                teamLivingPlayers.put(teamName, count + 1);
            }
        }

        return teamLivingPlayers;
    }

    /**
     * Get a list of teams that still have living players.
     *
     * @param deadPlayers List of dead players
     * @return List of team names with living players
     */
    public List<String> getTeamsWithLivingPlayers(List<Player> deadPlayers) {
        Map<String, Integer> teamLivingPlayers = getTeamLivingPlayers(deadPlayers);
        List<String> teamsAlive = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : teamLivingPlayers.entrySet()) {
            if (entry.getValue() > 0) {
                teamsAlive.add(entry.getKey());
            }
        }

        return teamsAlive;
    }
}