package speeddev.info.stickgame;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main
extends JavaPlugin
implements Listener
{
	
	String prefix = "§9[StickGame] ";
	
public void onEnable()
{
  PluginManager pm = getServer().getPluginManager();
  pm.registerEvents(this, this);
  File config = new File(getDataFolder() + File.separator + "config.yml");
  if (!config.exists())
  {
    getLogger().info("Generating config.yml");
    getConfig().createSection("Settings.RespawnTime");
    getConfig().createSection("Locations.Lobby.world");
    getConfig().createSection("Locations.Lobby.x");
    getConfig().createSection("Locations.Lobby.y");
    getConfig().createSection("Locations.Lobby.z");
    getConfig().set("Settings.RespawnTime", Integer.valueOf(4));
    getConfig().set("Locations.Lobby.world", "world");
    getConfig().set("Locations.Lobby.x", Integer.valueOf(100));
    getConfig().set("Locations.Lobby.y", Integer.valueOf(100));
    getConfig().set("Locations.Lobby.z", Integer.valueOf(100));
    getConfig().options().copyDefaults(true);
    saveConfig();
    
    getConfig().options().copyDefaults(true);
    saveConfig();
  }
}

public HashMap<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();
public HashMap<String, ItemStack[]> armour = new HashMap<String, ItemStack[]>();

public void onDisable() {}

public void saveSurvivalInventory(Player p)
{
  ItemStack[] inv = p.getInventory().getContents();
  ItemStack[] arm = p.getInventory().getArmorContents();
  this.inventories.put(p.getName(), inv);
  this.armour.put(p.getName(), arm);
}

public void loadSurvivalInvetory(Player p)
{
  p.getInventory().setContents((ItemStack[])this.inventories.get(p.getName()));
  p.getInventory().setArmorContents((ItemStack[])this.armour.get(p.getName()));
  this.inventories.remove(p.getName());
  this.armour.remove(p.getName());
}

public void joinGame(final Player player)
{
  if (this.ingame.contains(player.getName()))
  {
    player.sendMessage(prefix + ChatColor.RED + "You are already in game!");
  }
  else
  {
    this.ingame.add(player.getName());
    saveSurvivalInventory(player);
    final ItemStack stick = new ItemStack(Material.STICK, 1);
    stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
    ItemMeta stickMeta = stick.getItemMeta();
    stickMeta.setDisplayName(ChatColor.GREEN + "StickGame");
    stick.setItemMeta(stickMeta);
    Random rand = new Random();
    int randomNumber = rand.nextInt(10);
    String spawnwrld = getConfig().getString("Locations.Spawn." + randomNumber + ".world");
    final World spawnworld = Bukkit.getServer().getWorld(spawnwrld);
    final int spawnx = getConfig().getInt("Locations.Spawn." + randomNumber + ".x");
    final int spawny = getConfig().getInt("Locations.Spawn." + randomNumber + ".y");
    final int spawnz = getConfig().getInt("Locations.Spawn." + randomNumber + ".z");
    int lobbyx = getConfig().getInt("Locations.Lobby.x");
    int lobbyy = getConfig().getInt("Locations.Lobby.y");
    int lobbyz = getConfig().getInt("Locations.Lobby.z");
    String lobbywrld = getConfig().getString("Locations.Lobby.world");
    World lobbyworld = Bukkit.getServer().getWorld(lobbywrld);
    player.getInventory().clear();
    player.teleport(new Location(lobbyworld, lobbyx, lobbyy, lobbyz));
    player.setHealth(20.0D);
    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
    {
      public void run()
      {
        if (Main.this.ingame.contains(player.getName()))
        {
          player.teleport(new Location(spawnworld, spawnx, spawny, spawnz));
          player.getInventory().addItem(new ItemStack[] { stick });
        }
      }
    }, getConfig().getInt("Settings.RespawnTime") * 4L);
    
    player.sendMessage(prefix + ChatColor.GREEN + "You have joined stickgame!");
  }
}

@SuppressWarnings("deprecation")
public void leaveGame(Player player)
{
  if (!this.ingame.contains(player.getName()))
  {
    player.sendMessage(prefix + ChatColor.RED + "You are not in a game!");
  }
  else
  {
    int leavex = getConfig().getInt("Locations.Leave.x");
    int leavey = getConfig().getInt("Locations.Leave.y");
    int leavez = getConfig().getInt("Locations.Leave.z");
    String leavewrld = getConfig().getString("Locations.Leave.world");
    World leaveworld = Bukkit.getServer().getWorld(leavewrld);
    this.ingame.remove(player.getName());
    player.getInventory().clear();
    loadSurvivalInvetory(player);
    player.updateInventory();
    player.teleport(new Location(leaveworld, leavex, leavey, leavez));
    player.sendMessage(prefix + ChatColor.RED + "You have left stickgame!");
  }
}

public void voidDeath(final Player player)
{
  final ItemStack stick = new ItemStack(Material.STICK, 1);
  stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
  ItemMeta stickMeta = stick.getItemMeta();
  stickMeta.setDisplayName(ChatColor.GREEN + "StickGame");
  stick.setItemMeta(stickMeta);
  Random rand = new Random();
  int randomNumber = rand.nextInt(10);
  String spawnwrld = getConfig().getString("Locations.Spawn." + randomNumber + ".world");
  final World spawnworld = Bukkit.getServer().getWorld(spawnwrld);
  final int spawnx = getConfig().getInt("Locations.Spawn." + randomNumber + ".x");
  final int spawny = getConfig().getInt("Locations.Spawn." + randomNumber + ".y");
  final int spawnz = getConfig().getInt("Locations.Spawn." + randomNumber + ".z");
  int lobbyx = getConfig().getInt("Locations.Lobby.x");
  int lobbyy = getConfig().getInt("Locations.Lobby.y");
  int lobbyz = getConfig().getInt("Locations.Lobby.z");
  String lobbywrld = getConfig().getString("Locations.Lobby.world");
  World lobbyworld = Bukkit.getServer().getWorld(lobbywrld);
  player.getInventory().clear();
  player.teleport(new Location(lobbyworld, lobbyx, lobbyy, lobbyz));
  player.setHealth(20.0D);
  getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
  {
    public void run()
    {
      if (Main.this.ingame.contains(player.getName()))
      {
        player.teleport(new Location(spawnworld, spawnx, spawny, spawnz));
        player.getInventory().addItem(new ItemStack[] { stick });
        player.setHealth(20.0D);
      }
    }
  }, getConfig().getInt("Settings.RespawnTime") * 4L);
}

ArrayList<String> ingame = new ArrayList<String>();

public static boolean isInt(String s)
{
  try
  {
    Integer.parseInt(s);
  }
  catch (NumberFormatException nfe)
  {
    return false;
  }
  return true;
}

public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
{
  if ((sender instanceof Player))
  {
    Player player = (Player)sender;
    Location loc = player.getLocation();
    String locworld = loc.getWorld().getName();
    int locx = loc.getBlockX();
    int locy = loc.getBlockY();
    int locz = loc.getBlockZ();
    if (commandLabel.equalsIgnoreCase("stickgame")) {
      if (args.length == 0)
      {
        if (player.hasPermission("stickgame.admin"))
        {
          player.sendMessage(prefix + ChatColor.GOLD + "Here are the commands for stickgame:");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame setlobby");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame setspawn 1-10");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame reload");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame join");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame leave");
        }
        else
        {
          player.sendMessage(prefix + ChatColor.GOLD + "Here are the commands for stickgame:");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame join");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame leave");
        }
      }
      else if (args.length >= 1) {
        if ((args[0].equalsIgnoreCase("setlobby")) && (player.hasPermission("stickgame.admin")))
        {
          getConfig().set("Locations.Lobby.world", locworld);
          getConfig().set("Locations.Lobby.x", Integer.valueOf(locx));
          getConfig().set("Locations.Lobby.y", Integer.valueOf(locy));
          getConfig().set("Locations.Lobby.z", Integer.valueOf(locz));
          saveConfig();
          player.sendMessage(prefix + ChatColor.GREEN + "Successfully set the lobby!");
        }
        else if (args[0].equalsIgnoreCase("setspawntime"))
        {
          if (args.length == 1) {
            player.sendMessage(prefix + ChatColor.RED + "Please specify a time in seconds");
          }
          if ((args.length >= 2) && 
            (isInt(args[1])))
          {
            int num = Integer.parseInt(args[1]);
            if (getConfig().contains("Settings.RespawnTime"))
            {
              getConfig().set("Settings.RespawnTime", Integer.valueOf(num));
              saveConfig();
            }
            else
            {
              getConfig().createSection("Settings.RespawnTime");
              getConfig().set("Settings.RespawnTime", Integer.valueOf(num));
              saveConfig();
            }
          }
        }
        else if ((args[0].equalsIgnoreCase("join")) && (player.hasPermission("stickgame.player")))
        {
          joinGame(player);
        }
        else if ((args[0].equalsIgnoreCase("leave")) && (player.hasPermission("stickgame.player")))
        {
          leaveGame(player);
        }
        else if ((args[0].equalsIgnoreCase("setleave")) && (player.hasPermission("stickgame.admin")))
        {
          if (!getConfig().contains("Locations.Leave"))
          {
            getConfig().createSection("Locations.Leave");
            getConfig().set("Locations.Leave.world", locworld);
            getConfig().set("Locations.Leave.x", Integer.valueOf(locx));
            getConfig().set("Locations.Leave.y", Integer.valueOf(locy));
            getConfig().set("Locations.Leave.z", Integer.valueOf(locz));
            saveConfig();
            player.sendMessage(prefix + ChatColor.GOLD + "Leave location set!");
          }
          else if (getConfig().contains("Locations.Leave"))
          {
            getConfig().set("Locations.Leave.world", locworld);
            getConfig().set("Locations.Leave.x", Integer.valueOf(locx));
            getConfig().set("Locations.Leave.y", Integer.valueOf(locy));
            getConfig().set("Locations.Leave.z", Integer.valueOf(locz));
            saveConfig();
            player.sendMessage(prefix + ChatColor.GOLD + "Leave location set!");
          }
        }
        else if ((args[0].equalsIgnoreCase("setspawn")) && (player.hasPermission("stickgame.admin")))
        {
          if (args.length >= 2)
          {
            if (isInt(args[1]))
            {
              int num = Integer.parseInt(args[1]);
              if ((num <= 10) && (num >= 1))
              {
                if (!getConfig().contains("Locations.Spawn." + num))
                {
                  getConfig().createSection("Locations.Spawn." + num);
                  getConfig().set("Locations.Spawn." + num + ".world", locworld);
                  getConfig().set("Locations.Spawn." + num + ".x", Integer.valueOf(locx));
                  getConfig().set("Locations.Spawn." + num + ".y", Integer.valueOf(locy));
                  getConfig().set("Locations.Spawn." + num + ".z", Integer.valueOf(locz));
                  saveConfig();
                  player.sendMessage(prefix + ChatColor.GREEN + "Successfully set spawn " + args[1] + "!");
                }
                else
                {
                  getConfig().set("Locations.Spawn." + num + ".world", locworld);
                  getConfig().set("Locations.Spawn." + num + ".x", Integer.valueOf(locx));
                  getConfig().set("Locations.Spawn." + num + ".y", Integer.valueOf(locy));
                  getConfig().set("Locations.Spawn." + num + ".z", Integer.valueOf(locz));
                  saveConfig();
                  player.sendMessage(prefix + ChatColor.GREEN + "Successfully set spawn " + args[1] + "!");
                }
              }
              else {
                player.sendMessage(prefix + ChatColor.RED + "Please pick an integer between 1 and 10!");
              }
            }
          }
          else {
            player.sendMessage(prefix + ChatColor.RED + "Please pick an integer between 1 and 10!");
          }
        }
        else if ((args[0].equalsIgnoreCase("reload")) && (player.hasPermission("stickgame.admin")))
        {
          reloadConfig();
          player.sendMessage(prefix + ChatColor.RED + "Config reloaded!");
        }
        else
        {
          player.sendMessage(prefix + ChatColor.RED + "The command you sent does not exist!");
          player.sendMessage(prefix + ChatColor.GOLD + "Here are the commands for stickgame:");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame setlobby");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame setspawn 1-10");
          player.sendMessage(prefix + ChatColor.GOLD + "/stickgame reload");
        }
      }
    }
  }
  else if ((sender instanceof ConsoleCommandSender))
  {
    if (args.length == 0)
    {
      sender.sendMessage(prefix + ChatColor.GREEN + "Here are the available commands:");
      sender.sendMessage(prefix + ChatColor.GREEN + "/stickgame reload");
    }
    else if ((args.length >= 1) && 
      (args[0].equalsIgnoreCase("reload")))
    {
      reloadConfig();
      sender.sendMessage(prefix + ChatColor.GREEN + "Config reloaded!");
    }
  }
  return false;
}

@EventHandler
public void onInteract(PlayerInteractEvent Event)
{
  Player player = Event.getPlayer();
  Block block = Event.getClickedBlock();
  Action action = Event.getAction();
  if (((block.getState() instanceof Sign)) && (
    (action == Action.RIGHT_CLICK_BLOCK) || (action == Action.LEFT_CLICK_BLOCK)))
  {
    Sign sign = (Sign)block.getState();
    String line1 = sign.getLine(0);
    String line2 = sign.getLine(1);
    if ((line1.equalsIgnoreCase("[stickgame]")) && (player.hasPermission("stickgame.player"))) {
      if (line2.equalsIgnoreCase("join")) {
        joinGame(player);
      } else if (line2.equalsIgnoreCase("leave")) {
        leaveGame(player);
      }
    }
  }
}

@EventHandler(priority=EventPriority.HIGHEST)
public void onDamage(EntityDamageEvent event)
{
  if ((event.getEntity() instanceof Player))
  {
    Player player = (Player)event.getEntity();
    if (this.ingame.contains(player.getName())) {
      if (event.getCause() == EntityDamageEvent.DamageCause.VOID)
      {
        voidDeath(player);
        event.setCancelled(true);
      }
      else
      {
        event.setDamage(0.0D);
      }
    }
  }
}

@EventHandler
public void onQuit(PlayerQuitEvent event)
{
  if (this.ingame.contains(event.getPlayer().getName()))
  {
    event.getPlayer().getInventory().clear();
    this.ingame.remove(event.getPlayer().getName());
    loadSurvivalInvetory(event.getPlayer());
  }
}

@EventHandler
public void onHunger(FoodLevelChangeEvent event)
{
  if ((event.getEntity() instanceof Player))
  {
    Player player = (Player)event.getEntity();
    if (this.ingame.contains(player.getName())) {
      event.setCancelled(true);
    }
  }
}

@EventHandler
public void onDestroy(BlockBreakEvent event)
{
  if ((!event.getPlayer().isOp()) && (this.ingame.contains(event.getPlayer().getName()))) {
    event.setCancelled(true);
  }
}

@EventHandler
public void onDrop(PlayerDropItemEvent event)
{
  if (this.ingame.contains(event.getPlayer().getName())) {
    event.setCancelled(true);
  }
}
}
