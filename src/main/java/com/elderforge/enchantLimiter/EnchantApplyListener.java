package com.elderforge.enchantLimiter;

import com.elderforge.enchantLimiter.EnchantLimiter;
import com.elderforge.enchantLimiter.EnchantPrepareListener;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.util.Map;

public class EnchantApplyListener implements Listener {

    private final EnchantLimiter plugin;

    public EnchantApplyListener(EnchantLimiter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        // The map that Spigot/Paper is about to apply (possibly random extras in vanilla)
        Map<Enchantment, Integer> toAdd = event.getEnchantsToAdd();

        Player player = event.getEnchanter();
        if (player == null) return;

        // Retrieve the stored offers from PrepareItemEnchantEvent
        EnchantmentOffer[] storedOffers = EnchantPrepareListener.OFFERS_MAP.remove(player.getUniqueId());
        if (storedOffers == null || storedOffers.length < 1) {
            // If we have no record, let's not do anything special
            // Or forcibly remove all enchants. But typically it means some mismatch occurred.
            toAdd.clear();
            return;
        }

        // The cost the user actually paid
        int costUsed = event.getExpLevelCost();

        // Let's see which of the stored offers has that cost
        EnchantmentOffer chosenOffer = null;
        for (EnchantmentOffer offer : storedOffers) {
            if (offer == null) continue;
            if (offer.getCost() == costUsed) {
                chosenOffer = offer;
                break;
            }
        }

        // If we didn't find an offer matching costUsed, then none or multiple had the same cost
        // We'll just clear all enchantments
        if (chosenOffer == null) {
            toAdd.clear();
            return;
        }

        // Double check it's actually allowed from config
        Enchantment chosenEnchant = chosenOffer.getEnchantment();
        int chosenLevel = chosenOffer.getEnchantmentLevel();
        if (!plugin.getAllowedEnchants().containsKey(chosenEnchant)) {
            // Not in config? Clear
            toAdd.clear();
            return;
        }

        // Force the final enchant to be ONLY the chosen one
        toAdd.clear();
        toAdd.put(chosenEnchant, chosenLevel);
    }
}
