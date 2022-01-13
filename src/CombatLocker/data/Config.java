package CombatLocker.data;

import CombatLocker.manager.Manager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {
    private Manager manager;
    private FileConfiguration config;

    public Config(Manager manager) {
        this.manager = manager;
        this.config = manager.getPlugin().getConfig();
    }

    public int combatTime;
    public String combatListType = "BLACKLIST";
    public String commandMessage = "§4Вы не можете использовать команды в бою, осталось %sec%с.";
    public List<String> commands;

    public void loadConfig() {
        combatTime = config.getInt("CombatTime");
        combatListType = config.getString("ListType");
        commandMessage = config.getString("Message");
        commands = config.getStringList("Commands");
    }
}
