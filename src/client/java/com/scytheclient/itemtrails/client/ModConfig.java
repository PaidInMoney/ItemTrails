package com.scytheclient.itemtrails.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

@Config(name = "itemtrails")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public Map<String, TrailConfig> trails = new HashMap<>();

    public ModConfig() {
        // Default values
        trails.put("minecraft:wind_charge", new TrailConfig(true, "minecraft:cloud"));
        trails.put("minecraft:snowball", new TrailConfig(true, "minecraft:snowflake"));
        trails.put("minecraft:arrow", new TrailConfig(true, "minecraft:crit"));
        trails.put("minecraft:ender_pearl", new TrailConfig(true, "minecraft:portal"));
        trails.put("minecraft:egg", new TrailConfig(true, "minecraft:item_snowball"));
        trails.put("minecraft:potion", new TrailConfig(true, "minecraft:effect"));
        trails.put("minecraft:experience_bottle", new TrailConfig(true, "minecraft:enchant"));
        trails.put("minecraft:trident", new TrailConfig(true, "minecraft:bubble"));
        trails.put("minecraft:spectral_arrow", new TrailConfig(true, "minecraft:instant_effect"));
        trails.put("minecraft:firework_rocket", new TrailConfig(true, "minecraft:firework"));
        trails.put("minecraft:small_fireball", new TrailConfig(true, "minecraft:flame"));
        trails.put("minecraft:dragon_fireball", new TrailConfig(true, "minecraft:dragon_breath"));
        trails.put("minecraft:wither_skull", new TrailConfig(true, "minecraft:smoke"));
        trails.put("minecraft:shulker_bullet", new TrailConfig(true, "minecraft:end_rod"));
        trails.put("minecraft:llama_spit", new TrailConfig(true, "minecraft:spit"));
        trails.put("minecraft:fishing_bobber", new TrailConfig(true, "minecraft:bubble"));
    }

    public static class TrailConfig {
        public boolean enabled;
        public String particleId;

        public TrailConfig() {
            this.enabled = true;
            this.particleId = "minecraft:cloud";
        }

        public TrailConfig(boolean enabled, String particleId) {
            this.enabled = enabled;
            this.particleId = particleId;
        }
    }
}
