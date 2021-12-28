package net.azisaba.afnw.randomteleporter;

import java.util.List;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

import static org.bukkit.Bukkit.*;

public class Teleporter implements Listener, CommandExecutor {
    private final RandomTeleporter plugin;
    private final Configuration config;
    private final SecureRandom rnd;

    public Teleporter(RandomTeleporter plugin, Configuration config) {
        this.plugin = plugin;
        this.config = config;
        this.rnd = new SecureRandom();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("givertp")) return false;
        Player p;
        if (args.length == 0) p = (Player)sender;
        else p = getPlayer(args[0]);

        if (p == null) {
            sender.sendMessage(ChatColor.RED + "[RandomTP] 指定されたプレイヤーは存在しません．");
            return false;
        }

        if (p.getInventory().firstEmpty() == -1) {
            sender.sendMessage(ChatColor.RED + "[RandomTP] 指定されたプレイヤーのインベントリがいっぱいです．\n" +
                                               "           このスクリーンショットを運営にご提示ください．");
            return false;
        }

        ItemStack item = new ItemStack(Material.POTION, 1);
        ItemMeta im = item.getItemMeta();
        if (im == null) {
            sender.sendMessage(ChatColor.RED + "[RandomTP] 不明なエラーです．このスクリーンショットを運営にご提示ください．");
            return false;
        }
        im.setDisplayName("Random Teleporter");
        im.setLore(List.of("これを飲むとランダムな位置にテレポートします","一度使うと消えます"));
        im.setCustomModelData(100);
        item.setItemMeta(im);
        p.getInventory().addItem(item);
        p.sendMessage(ChatColor.YELLOW + "[RandomTP] Random Teleporter を入手しました！");
        return true;
    }

    @EventHandler
    public void onDrink (PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.POTION) return;
        if (event.getItem().getItemMeta() == null) return;
        if (event.getItem().getItemMeta().getCustomModelData() != 100) return;
        if (event.getPlayer().getWorld() != this.config.getWorld()) {
            event.getPlayer().sendMessage(ChatColor.RED + "[RandomTP] ここでは使えません．");
            event.setCancelled(true);
            return;
        }
        randomTp(event.getPlayer());
    }

    private void randomTp(Player p) {
        if (p.getWorld() != this.config.getWorld()) {
            p.sendMessage(ChatColor.RED + "[RandomTP] 転移に失敗しました．ここでは使えません．");
            Bukkit.getServer().getLogger().info(ChatColor.YELLOW + "randomTp()によるコマンド実行がされます．");
            Bukkit.dispatchCommand(getServer().getConsoleSender(),"/givertp "+p.getName());
            return;
        }

        LOOPS:
        for (int i=0; i<10; i++) {
            Location tpLoc = new Location(
                    this.config.getWorld(),
                    this.config.getXMax() * 2 * this.rnd.nextFloat() - this.config.getXMax(),
                    100 * this.rnd.nextFloat() + 50,
                    this.config.getZMax() * 2 * this.rnd.nextFloat() - this.config.getZMax()
            );

            // そのチャンク内のy=50~150が全てAIRかどうかを判定する
            Chunk tpChunk = tpLoc.getChunk();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 50; y < 150; y++) {
                        if (!tpChunk.getBlock(x, y, z).isEmpty()) {
                            Bukkit.getServer().getLogger().info(ChatColor.YELLOW + p.getName() + "によるテレポート失敗" + i + "回目");
                            continue LOOPS;
                        }
                    }
                }
            }

            Location blockLoc = tpLoc.clone();
            blockLoc.add(0,-1,0);
            if (!blockLoc.getBlock().isEmpty()) continue;

            p.sendMessage(ChatColor.YELLOW + "[RandomTP] " + this.config.getDelaySec() + "秒後にランダムテレポートします．");
            Bukkit.getScheduler().runTaskLater(this.plugin, ()->{
                this.config.getWorld().getBlockAt(blockLoc).setType(Material.BEDROCK);
                p.teleport(tpLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                p.sendMessage(ChatColor.YELLOW + "[RandomTP] テレポートに成功しました！");
            }, 20 * this.config.getDelaySec());
            return;
        }

        p.sendMessage(ChatColor.RED + "[RandomTP] 転移に失敗しました．アイテムをお返しいたします．");
        Bukkit.getServer().getLogger().info(ChatColor.YELLOW + "randomTp()によるコマンド実行がされます．");
        Bukkit.dispatchCommand(getServer().getConsoleSender(),"givertp "+p.getName());
    }
}
