package CombatLocker.manager;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class CombatManager {

    private Manager manager;

    CombatManager(Manager manager) {
        this.manager = manager;
    }

    public HashMap<UUID, Boolean> inCombat = new HashMap<>();
    public HashMap<UUID, Long> timeInCombat = new HashMap<>();

    private HashMap<UUID, Integer> combatCancelTask = new HashMap<>();

    public boolean isInCombat(Player p) {
        return inCombat.getOrDefault(p.getUniqueId(), false);
    }

    public void cancelCombat(Player p, boolean removeInCombat) {
        if(removeInCombat) inCombat.remove(p.getUniqueId()); // первое при ливе с сервера, второе при смерти
        else inCombat.put(p.getUniqueId(), false);

        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "Вы вышли из боя"));

        if(combatCancelTask.containsKey(p.getUniqueId())) {
            int task = combatCancelTask.get(p.getUniqueId());
            Bukkit.getScheduler().cancelTask(task);
        }
    }

    public void startCombat(Player p) {
        int duration = manager.getConfig().combatTime*20;

        p.setFlying(false);
        p.setWalkSpeed(0.2F);

        timeInCombat.put(p.getUniqueId(), System.currentTimeMillis());

        if(!inCombat.containsKey(p.getUniqueId()) || !inCombat.get(p.getUniqueId())) { // если игрок был вне боя, просто уведомляем его, если в бою - перезначаем таймер
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "Вы вошли в бой!"));
            inCombat.put(p.getUniqueId(), true);

            BossBar bar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_10);
            bar.setVisible(true);

            new BukkitRunnable() { // даем босбар
                public void run() {
                    long secondsLeft = ((timeInCombat.get(p.getUniqueId()) / 1000) + manager.getConfig().combatTime) - (System.currentTimeMillis() / 1000);
                    String text = ChatColor.RED + "" + ChatColor.BOLD + "Вы в бою! (" + secondsLeft + "с)";

                    int maxProgress = manager.getConfig().combatTime;
                    double progressPerTick = 1.00 / maxProgress;

                    double progress = secondsLeft * progressPerTick;
                    if(progress > 1.00 || progress < 0) progress = 1.00;

                    bar.setTitle(text);
                    bar.setProgress(progress);
                    bar.addPlayer(p);

                    if(!inCombat.containsKey(p.getUniqueId()) || !inCombat.get(p.getUniqueId()) || !p.isOnline()) {
                        bar.removePlayer(p);
                        cancel();
                    }
                }
            }.runTaskTimer(manager.getPlugin(), 0, 5);

        } else {
            if(combatCancelTask.containsKey(p.getUniqueId())) {
                int task = combatCancelTask.get(p.getUniqueId());
                Bukkit.getScheduler().cancelTask(task);
            }
        }

        int i = Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), () -> {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "Вы вышли из боя"));
            inCombat.put(p.getUniqueId(), false);
            combatCancelTask.remove(p.getUniqueId()); // по окончанию файта удаляем таск из мапы
        }, duration);

        combatCancelTask.put(p.getUniqueId(), i);
    }
}
