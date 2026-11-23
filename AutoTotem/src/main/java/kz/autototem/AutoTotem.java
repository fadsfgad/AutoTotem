package kz.autototem;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AutoTotem extends JavaPlugin implements Listener {

    // игроки, которые выключили авто-тотем для себя
    private final Set<UUID> disabled = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AutoTotem включён (1.21.4)");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasPermission("autototem.use")) return;
        if (disabled.contains(player.getUniqueId())) return;

        // тотем расходуется после события, поэтому ждём 1 тик
        getServer().getScheduler().runTask(this, () -> equipTotem(player));
    }

    private void equipTotem(Player player) {
        if (!player.isOnline()) return;
        PlayerInventory inv = player.getInventory();

        ItemStack off = inv.getItemInOffHand();
        if (off.getType() == Material.TOTEM_OF_UNDYING) return;
        if (off.getType() != Material.AIR) return; // не трогаем другую вещь в руке

        // сначала ищем в хотбаре и основном инвентаре
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                inv.setItemInOffHand(item.clone());
                inv.setItem(slot, null);
                player.updateInventory();
                return;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        UUID id = player.getUniqueId();
        if (disabled.remove(id)) {
            player.sendMessage("§aАвто-тотем включён.");
        } else {
            disabled.add(id);
            player.sendMessage("§cАвто-тотем выключен.");
        }
        return true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        disabled.remove(e.getPlayer().getUniqueId());
    }
}
