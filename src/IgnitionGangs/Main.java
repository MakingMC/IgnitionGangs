package IgnitionGangs;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends JavaPlugin implements Listener{
    public static HashMap<String, String> gang = new HashMap<>();
    public List<String> gangs = new ArrayList<String>();
    String preperm = "Ignition.gang.";
    public static Permission permission = null;
    public static Chat chat = null;

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        setupPermissions();
        gangs.addAll(this.getConfig().getStringList("IgnitionGangs.Gangs"));
        for (Player p: Bukkit.getOnlinePlayers()
             ) {

            gang.put(p.getName(), this.getConfig().getString("IgnitionGangs."+p.getName()+".gang"));
        }


    }

    public void onDisable() {
        this.getConfig().set("IgnitionGangs.Gangs", gangs);

        for (Player p: Bukkit.getOnlinePlayers()
                ) {
            if (gang.get(p.getName()) != null) {
                getConfig().set("IgnitionGangs."+p.getName() + ".gang", gang);
                saveConfig();
            }
            else{
                this.getConfig().set("IgnitionGangs."+p.getName()+".gang", "");
            }
        }
        saveConfig();
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        String name = e.getPlayer().getName();
        if(gang.get(name) !=null) {
            getConfig().set("IgnitionGangs." + p.getName() + ".gang", gang);
            saveConfig();
        }
        else{
            this.getConfig().set("IgnitionGangs." + p.getName() + ".gang", "");
            saveConfig();
        }
        }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        gang.putIfAbsent(p.getName(), this.getConfig().getString("IgnitionGangs."+p.getName()+".gang"));
    }
    @Override
    public boolean onCommand(CommandSender senders, Command command, String label, String[] args) {
        Player sender = (Player) senders;
        if (label.equalsIgnoreCase("gang")) {
            if(args.length > 0) {
                switch (args[0]) {
                    case "list": {
                        if (args.length < 2) {
                            if (gangs.size() >= 1) {
                                sender.sendMessage(String.format("%s%s", ChatColor.GREEN, gangs.toString()));
                            } else {
                                sender.sendMessage(String.format("%sThere are no gangs :/", ChatColor.RED));
                            }
                        }


                    else{
                        sender.sendMessage(String.format("%sPlease just do /gang list", ChatColor.RED));
                    }
                    break;
                }
                    case "leave": {
                        if(args.length <2) {
                            if (gang.get(sender.getName()) != null) {
                                sender.sendMessage(String.format("%sYou have left %s.", ChatColor.GREEN, gang.get(sender.getName())));
                                gang.put(sender.getName(), null);
                            } else {
                                sender.sendMessage(String.format("%sYou are not in a gang", ChatColor.RED));
                            }
                        }
                        else{
                            sender.sendMessage(String.format("%sPlease just do /gang leave", ChatColor.RED));
                        }
                        break;
                    }
                    case "get":{
                        if(gang.get(sender.getName()) != null) {
                            sender.sendMessage(String.format("%s", gang.get(sender.getName())));
                        }
                        if(gang.get(sender.getName()) == null || gang.get(sender.getName()).equals("")) {
                            sender.sendMessage(String.format("%sYou are not in a gang!", ChatColor.RED));
                        }
                        break;
                    }
                    case "invite":{
                        if(gang.get(sender.getName()) != null) {
                            if(sender != Bukkit.getPlayer(args[1])) {
                                if (args.length == 2) {
                                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))) {
                                        sender.sendMessage(String.format("%sYou have invited %s to your gang!", ChatColor.GREEN, args[1]));
                                        getServer().dispatchCommand(getServer().getConsoleSender(), "manuaddp "+ args[1] + " " + preperm + gang.get(sender.getName()));
                                        Bukkit.getPlayer(args[1]).sendMessage(String.format("%sThe gang %s has invited you to join.", ChatColor.AQUA, gang.get(sender.getName())));
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {

                                                getServer().dispatchCommand(getServer().getConsoleSender(), "manudelp "+ args[1] + " " + preperm + gang.get(sender.getName()));
                                                Bukkit.getPlayer(args[1]).sendMessage(String.format("%sInvite from %s has ran out.", ChatColor.RED, gang.get(sender.getName())));

                                            }
                                        }.runTaskTimer(this, 120L, -1L);


                                    } else {
                                        sender.sendMessage(String.format("%s%s is not an online player", ChatColor.RED, args[1]));
                                    }

                                }
                                else {
                                    sender.sendMessage(String.format("%sProper formatting is /gang invite [name]", ChatColor.RED));
                                }
                            }
                            else{
                                sender.sendMessage(String.format("%sYou can not invite yourself.", ChatColor.RED));
                            }
                        }
                        else{
                            sender.sendMessage(String.format("%sYou are not in a gang!", ChatColor.RED));
                        }
                        break;
                    }
                    case "tees":{
                        gang.put(sender.getName(), null);
                        break;
                    }
                    case "create": {
                        if(!gangs.contains(args[1])) {
                            sender.sendMessage(String.format("%sYou have created the gang %s.", ChatColor.GREEN, args[1]));
                            gangs.add(args[1]);
                        }
                        else{
                            sender.sendMessage(String.format("%s%s is already a gang.", ChatColor.RED, args[1]));
                        }
                        break;

                    }
                    case "save":{
                        if(sender.hasPermission("Ignition.save") || sender.isOp()){
                            saveConfig();
                            sender.sendMessage(String.format("%s%sConfig Saved!", ChatColor.GREEN, ChatColor.BOLD));
                        }
                        break;
                    }
                    case "remove": {
                        if(gangs.contains(args[1])) {
                            sender.sendMessage(String.format("%sYou have removed %s as a gang.", ChatColor.GREEN, args[1]));
                            gangs.remove(args[1]);
                        }
                        else {
                            sender.sendMessage(String.format("%s%s is not a registered gang.", ChatColor.RED, args[1]));
                        }
                        break;
                    }
                    case "join": {
                        if(gang.get(sender.getName()) == null) {
                            if (gangs.contains(args[1])) {
                                if(sender.hasPermission(preperm+gang.get(sender.getName()))) {
                                    sender.sendMessage(String.format("%sYou have joined %s.", ChatColor.GREEN, args[1]));
                                    gang.put(sender.getName(), args[1]);
                                }
                                else{
                                    sender.sendMessage("You don't have permission to join that gang.");
                                }
                            }
                            else{
                                sender.sendMessage(String.format("%s%s is not a registered gang.", ChatColor.RED, args[1]));
                            }
                        }
                        else{
                            sender.sendMessage(String.format("%sYou are already in a gang", ChatColor.RED));
                        }

                        break;
                    }

                    default: {
                        sender.sendMessage("Please put in a valid argument.");
                        break;
                    }
                }
            }
            else{
                sender.sendMessage(ChatColor.RED+"/gang <create/remove/join/leave/list> [Gang]");
                sender.sendMessage(ChatColor.RED+"<> = Required  [] = Optional");
            }
        }
        return true;
    }
}