package org.icanthink.minigameManager.features.mobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.features.items.BrazilStick;
import org.icanthink.minigameManager.features.items.CustomItem;
import org.icanthink.minigameManager.features.items.ShuffleSword;
import org.icanthink.minigameManager.features.items.TeleportRod;
import org.icanthink.minigameManager.features.items.WishItem;
import org.icanthink.minigameManager.features.items.CombatLog;

/**
 * A business-minded Piglin named Mr. Business who sells custom items.
 */
public class BusinessVillager extends CustomMob implements Listener {
    private final List<MerchantRecipe> trades;
    private static final Random random = new Random();
    private static final List<String> WELCOME_MESSAGES = List.of(
        "Today is a good day for business!",
        "The SMP 500 is up today. Lets do business.",
        "I'm a huge fan of reganomics.",
        "I used to be a republican until the tarrifs. I'm a libertarian now.",
        "The problem with socialism is that you eventually run out of other people's money.",
        "The most terrifying words in the English language are 'I'm from the government and I'm here to help.'",
        "From each according to his work, to each according to his money.",
        "Taxes... (grumbles)"
    );

    private static final List<Material> FLOWERS = List.of(
        Material.DANDELION,
        Material.POPPY,
        Material.BLUE_ORCHID,
        Material.ALLIUM,
        Material.AZURE_BLUET,
        Material.RED_TULIP,
        Material.ORANGE_TULIP,
        Material.WHITE_TULIP,
        Material.PINK_TULIP,
        Material.OXEYE_DAISY,
        Material.CORNFLOWER,
        Material.LILY_OF_THE_VALLEY,
        Material.WITHER_ROSE
    );

    public BusinessVillager(Minigame minigame) {
        super(minigame);
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
        this.trades = setupTrades();
    }

    @Override
    protected String getName() {
        return ChatColor.GOLD + "Mr. Business";
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.PIGLIN;
    }

    @Override
    protected void customizeMob(LivingEntity entity) {
        if (!(entity instanceof Piglin piglin)) return;

        // Set piglin properties
        piglin.setCustomNameVisible(true);
        piglin.setInvulnerable(true);
        piglin.setImmuneToZombification(true);
        piglin.setAdult();
        piglin.setCanPickupItems(false);
        piglin.setIsAbleToHunt(false); // Prevent hunting behavior

        // Give them some fancy gold armor
        piglin.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        piglin.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
        piglin.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
        piglin.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));

        // Give them a random flower to hold
        piglin.getEquipment().setItemInMainHand(new ItemStack(FLOWERS.get(random.nextInt(FLOWERS.size()))));
    }

    @Override
    public boolean onTarget(EntityTargetEvent event) {
        // Prevent the piglin from targeting anything
        return true; // Cancel the targeting event
    }

    private List<MerchantRecipe> setupTrades() {
        List<MerchantRecipe> recipes = new ArrayList<>();

        // Add trades with random flower costs
        addRandomFlowerTrade(recipes, BrazilStick.class, 99999);
        addRandomFlowerTrade(recipes, ShuffleSword.class, 1);
        addRandomFlowerTrade(recipes, TeleportRod.class, 1);
        addRandomFlowerTrade(recipes, WishItem.class, 0);
        addRandomFlowerTrade(recipes, CombatLog.class, 1);

        return recipes;
    }

    private void addRandomFlowerTrade(List<MerchantRecipe> recipes, Class<? extends CustomItem> itemClass, int maxUses) {
        try {
            // Create and register the item
            CustomItem item = itemClass.getDeclaredConstructor(Minigame.class).newInstance(minigame);
            minigame.getItemManager().registerItem(item);

            // Create the trade recipe
            ItemStack result = item.createItem();
            MerchantRecipe recipe = new MerchantRecipe(result, maxUses);
            Material flower = FLOWERS.get(random.nextInt(FLOWERS.size()));
            recipe.addIngredient(new ItemStack(flower, 1));
            recipes.add(recipe);
        } catch (Exception e) {
            MinigameManager.plugin.getLogger().warning("Failed to create trade for " + itemClass.getSimpleName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!minigame.isRunning()) return;
        if (!minigame.getPlayers().contains(event.getPlayer())) return;
        if (!(event.getRightClicked() instanceof Piglin piglin)) return;
        if (!isInstance(piglin)) return;

        event.setCancelled(true);

        // Send random welcome message
        String message = WELCOME_MESSAGES.get(random.nextInt(WELCOME_MESSAGES.size()));
        event.getPlayer().sendMessage(ChatColor.GOLD + "[Mr. Business] " + ChatColor.WHITE + message);

        // Open custom trade window
        Merchant merchant = MinigameManager.plugin.getServer().createMerchant(getName());
        merchant.setRecipes(trades);
        event.getPlayer().openMerchant(merchant, true);
    }

    @Override
    protected boolean shouldShowBossBar() {
        return false;
    }

    @Override
    protected boolean shouldShowNametag() {
        return true;
    }
}