package CombatLocker;

import CombatLocker.listener.Handler;
import CombatLocker.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Manager manager = new Manager(this);

        manager.getConfig().loadConfig();

        Bukkit.getPluginManager().registerEvents(new Handler(manager), this);
    }

}
