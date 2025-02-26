package com.elderforge.enchantLimiter;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnchantPrepareListener implements Listener {

    // Store the three "preview" offers for each player
    // Key = Player UUID, Value = The three offers that were displayed
    public static final Map<UUID, EnchantmentOffer[]> OFFERS_MAP = new HashMap<>();

    private final EnchantLimiter plugin;

    public EnchantPrepareListener(EnchantLimiter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        Player player = event.getEnchanter();
        if (player == null) return;

        Map<Enchantment, Integer> allowed = plugin.getAllowedEnchants();

        // We'll fill up to 3 possible offers (one per enchanting table slot)
        List<EnchantmentOffer> validOffers = new ArrayList<>();

        for (Map.Entry<Enchantment, Integer> entry : allowed.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();

            // Check if the item can accept this enchant
            if (enchant.canEnchantItem(item)) {
                int slotIndex = validOffers.size();
                int cost = event.getExpLevelCostsOffered()[slotIndex];
                // cost is typically something like [1, 2, 3], or might vary

                EnchantmentOffer offer = new EnchantmentOffer(enchant, level, cost);
                validOffers.add(offer);

                if (validOffers.size() >= 3) {
                    break; // We only have 3 slots
                }
            }
        }

        // Overwrite the random offers
        EnchantmentOffer[] offers = event.getOffers(); // length = 3
        for (int i = 0; i < offers.length; i++) {
            if (i < validOffers.size()) {
                offers[i] = validOffers.get(i);
            } else {
                offers[i] = null;
            }
        }

        // Store these offers so we know which ones were displayed
        // We'll retrieve them in EnchantItemEvent
        OFFERS_MAP.put(player.getUniqueId(), offers.clone());
    }
}
