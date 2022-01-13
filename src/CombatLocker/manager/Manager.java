package CombatLocker.manager;

import CombatLocker.Main;
import CombatLocker.data.Config;
import org.bukkit.plugin.Plugin;

public class Manager {
    private Main plugin;
    private CombatManager combat;
    private Config config;

    public Manager(Main main) {
        this.plugin = main;
        this.combat = new CombatManager(this);
        this.config = new Config(this);
    }

    public CombatManager getCombat() { return combat; }

    public Plugin getPlugin() {
        return plugin;
    }

    public Config getConfig() { return config; }
}
