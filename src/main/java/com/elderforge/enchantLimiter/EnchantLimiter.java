package com.elderforge.enchantLimiter;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class EnchantLimiter extends JavaPlugin {

    private final Map<Enchantment, Integer> allowedEnchants = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadAllowedEnchantments();

        // Register our two event listeners
        Bukkit.getPluginManager().registerEvents(new EnchantPrepareListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EnchantApplyListener(this), this);

        getLogger().info("EnchantLimiter enabled with " + allowedEnchants.size() + " allowed enchants.");
    }

    public Map<Enchantment, Integer> getAllowedEnchants() {
        return allowedEnchants;
    }

    private void loadAllowedEnchantments() {
        for (String key : getConfig().getKeys(false)) {
            Enchantment enchant = Enchantment.getByName(key.toUpperCase());
            if (enchant == null) {
                getLogger().warning("Invalid enchantment in config: " + key + ". Skipping...");
                continue;
            }
            int level = getConfig().getInt(key, 1);
            allowedEnchants.put(enchant, level);
        }
    }
}
