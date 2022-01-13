package CombatLocker.listener;

import CombatLocker.manager.Manager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Handler implements Listener {

    private Manager manager;
    public Handler(Manager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if(manager.getCombat().isInCombat(p)) {
            p.sendMessage(ChatColor.RED + "Вы вышли из боя, за что были автоматически убиты!");
            p.setHealth(0F);

            manager.getCombat().cancelCombat(p, true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if(manager.getCombat().isInCombat(p))
            manager.getCombat().cancelCombat(p, false);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        manager.getCombat().inCombat.put(p.getUniqueId(), false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerCommandExecute(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage().replace("/", "");
        Player p = e.getPlayer();

        if(manager.getCombat().isInCombat(p)) {
            String listType = manager.getConfig().combatListType;
            long secondsLeft = ((manager.getCombat().timeInCombat.get(p.getUniqueId()) / 1000) + manager.getConfig().combatTime) - (System.currentTimeMillis() / 1000);
            String replacedMessage = manager.getConfig().commandMessage.replace("%sec%", String.valueOf(secondsLeft));

            switch (listType) {
                case "ALL":  // если список ALL, просто не даем любые кмд
                    p.sendMessage(replacedMessage);
                    e.setCancelled(true);
                    break;

                case "WHITELIST":  // Если вайтлист, даем вводить только указанные кмд
                    if (!manager.getConfig().commands.contains(cmd)) {
                        p.sendMessage(replacedMessage);
                        e.setCancelled(true);
                    }
                    break;

                case "BLACKLIST": default:  // Если блеклист или любая другая хрень, то не даем вводить только указанные
                    if (manager.getConfig().commands.contains(cmd)) {
                        p.sendMessage(replacedMessage);
                        e.setCancelled(true);
                    }
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerHit(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {

            Player damager = (Player) e.getDamager();
            Player victim = (Player) e.getEntity();

            if(damager.getGameMode() == GameMode.CREATIVE) return;

            manager.getCombat().startCombat(damager);
            manager.getCombat().startCombat(victim);

        }

        else if(e.getDamager() instanceof Arrow && e.getEntity() instanceof Player) {
            Arrow arrow = (Arrow) e.getDamager();

            if(!(arrow.getShooter() instanceof Player)) return;
            Player damager = (Player) arrow.getShooter();
            Player victim = (Player) e.getEntity();

            if(damager.getGameMode() == GameMode.CREATIVE) return;

            manager.getCombat().startCombat(damager);
            manager.getCombat().startCombat(victim);
        }
    }

}
