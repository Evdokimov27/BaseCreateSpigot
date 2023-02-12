package ru.evdokimov27;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.ssomar.score.SCore;
import com.ubivashka.vk.bukkit.BukkitVkApiPlugin;
import com.ubivashka.vk.bukkit.events.VKMessageEvent;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.utils.DomainResolvedType;
import com.vk.api.sdk.objects.utils.responses.ResolveScreenNameResponse;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ssomar.score.SCore.NAME;
import static org.bukkit.Bukkit.*;

public class GUIICommand implements Listener {

    private Inventory gui_base;

    Inventory player_head_region;
    Inventory player_head_around;
    LuckPerms luckPerms = LuckPermsProvider.get();
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    World world = BukkitAdapter.adapt(Objects.requireNonNull(getServer().getWorld("world")));
    RegionManager regions = container.get(world);
    private Inventory gui_leveling;
    private Inventory addReg;
    private Location loc;
    public Integer ID_VK;

    int Task;


    private Plugin plugin = basecreate.getPlugin(basecreate.class);

    WorldGuardPlugin wg = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
    TransportClient transportClient = new HttpTransportClient();
    VkApiClient vk = new VkApiClient(transportClient);
    private static final VkApiClient CLIENT = BukkitVkApiPlugin.getPlugin(BukkitVkApiPlugin.class).getVkApiProvider()
            .getVkApiClient();
    private static final GroupActor ACTOR = BukkitVkApiPlugin.getPlugin(BukkitVkApiPlugin.class).getVkApiProvider()
            .getActor();
    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    private final static Random RANDOM = new Random();


    @EventHandler
    public void privateTnt(EntityExplodeEvent e)
    {
        broadcastMessage(e.getLocation()+"");
    }




    public void GlaveRegion(Player p) {

        Server server = plugin.getServer();
        server.getScheduler().runTaskTimerAsynchronously(plugin, (task) -> {
            boolean check = plugin.getConfig().getBoolean(p.getUniqueId() + ".base.glave");
            if (check) {
                summonCircle(p);
                summonCirclePrivate(p);
            } else task.cancel();
        }, 0, 20);

    }


    public void RegionCreate(Player player, String name, BlockVector3 pos1, BlockVector3 pos2) {
        ProtectedRegion region = new ProtectedCuboidRegion(name, pos1, pos2);
        UUID id = player.getUniqueId();
        Location locate = new Location(player.getWorld(), plugin.getConfig().getDouble(id + ".base.coords.x"), plugin.getConfig().getDouble(id + ".base.coords.y"), plugin.getConfig().getDouble(id + ".base.coords.z"));;
        BlockVector3 base1 = BlockVector3.at(locate.getX() - 90, locate.getY() - 90, locate.getZ() - 90);
        BlockVector3 base2 = BlockVector3.at(locate.getX() + 90, locate.getY() + 90, locate.getZ() + 90);
        ProtectedRegion region_protect = new ProtectedCuboidRegion("protect_" + name, base1, base2);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(world);
        assert regions != null;
        regions.addRegion(region);
        regions.addRegion(region_protect);
        region_protect.setFlag(Flags.PASSTHROUGH, StateFlag.State.ALLOW);
        DefaultDomain members = region.getOwners();
        members.addPlayer(player.getUniqueId());
    }

    public void RegionRemove(Player player) {
        RegionManager regions = container.get(world);
        assert regions != null;
        regions.removeRegion(player.getUniqueId().toString());
        regions.removeRegion("protect_" + player.getUniqueId().toString());
    }

    public boolean checkRegion(Player playerCheck, String rgName) {
        ProtectedRegion region = regions.getRegion(rgName);
        assert region != null;
        region.contains(BlockVector3.at(playerCheck.getLocation().getBlockX(), playerCheck.getLocation().getBlockY(), playerCheck.getLocation().getBlockZ()));
        return region.getMembers().contains(playerCheck.getUniqueId());
    }

    public boolean checkRegionLocation(Player location) {
        boolean member = true;
        boolean protect = true;
        ApplicableRegionSet set = Objects.requireNonNull(WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(location.getWorld()))).getApplicableRegions(BlockVector3.at(location.getLocation().getBlockX(), location.getLocation().getBlockY(), location.getLocation().getBlockZ()));
        LocalPlayer localPlayer = wg.wrapPlayer(location);


        if (set.size() > 0) {
            for (ProtectedRegion rg : set.getRegions()) {
                String str = rg.getId();
                Pattern p = Pattern.compile(".*prot(?=ect_).*");
                Matcher m = p.matcher(rg.getId());
                if (m.matches()) {
                    if (rg.getId().toString().equals(rg.getId())) {
                        member = true;
                    }
                    else member = false;
                } else {
                    if (rg.isMember(localPlayer)) {
                        protect = true;

                    } else protect = false;
                }
            }
        }
        return member == protect;
    }

    public boolean protectRegion(Player location) {
        boolean protect = false;
        ApplicableRegionSet set = Objects.requireNonNull(WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(location.getWorld()))).getApplicableRegions(BlockVector3.at(location.getLocation().getBlockX(), location.getLocation().getBlockY(), location.getLocation().getBlockZ()));

        if (set.size() > 0) {
            for (ProtectedRegion rg : set.getRegions()) {
                Pattern p = Pattern.compile(".*prot(?=ect_).*");
                Matcher m = p.matcher(rg.getId());
                if (m.matches()) {
                    if (rg.getId().equals(rg.getId())) {
                        protect = true;
                    }
                }

            }
        }
        return protect;
    }






    public void addRegion(Player player, Player name) {
        ProtectedRegion region = regions.getRegion(player.getUniqueId().toString());
        assert region != null;
        DefaultDomain members = region.getMembers();
        members.addPlayer(UUID.fromString(name.getUniqueId().toString()));
    }

    public void removeRegion(Player player, Player add) {
        ProtectedRegion region = regions.getRegion(player.getUniqueId().toString());
        assert region != null;
        DefaultDomain members = region.getMembers();
        members.removePlayer(UUID.fromString(add.getUniqueId().toString()));
    }

    public static List<Player> getPlayers(Location loc, double radius) {
        List<Player> players = new ArrayList<>();
        Collection<Entity> nearby = Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, radius, radius, radius);
        for (Entity e : nearby) {
            if (e.getType() == EntityType.PLAYER) {
                players.add((Player) e);
            }
        }
        return players;
    }

    public int index(Player p) {
        int index = 0;
        switch (plugin.getConfig().getInt(p.getUniqueId() + ".base.level")) {
            case 1: {
                index = 10;
                break;
            }
            case 2: {
                index = 15;
                break;
            }
            case 3: {
                index = 20;
                break;
            }
            case 4: {
                index = 30;
                break;
            }
            case 5: {
                index = 45;
                break;
            }
        }
        return index;
    }

    public void summonCircle(Player p) {
        Location location = new Location(p.getWorld(), plugin.getConfig().getInt(p.getUniqueId() + ".base.coords.x"), plugin.getConfig().getInt(p.getUniqueId() + ".base.coords.y"), plugin.getConfig().getInt(p.getUniqueId() + ".base.coords.z"));
        for (int d = 0; d <= 360; d += 1) { // 360 - заполненность формы
            Location particleLoc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
            particleLoc.setX(location.getX()+1 + Math.cos(d) * index(p));
            particleLoc.setZ(location.getZ()+1 + Math.sin(d) * index(p));
            Objects.requireNonNull(location.getWorld()).spawnParticle(Particle.REDSTONE, particleLoc, 20, new Particle.DustOptions(Color.RED, 1)); // size - размер и частота мигания, count - кол-во частиц
        }
    }
    public void summonCirclePrivate(Player p) {
        Location location = new Location(p.getWorld(), plugin.getConfig().getInt(p.getUniqueId() + ".base.coords.x"), plugin.getConfig().getInt(p.getUniqueId() + ".base.coords.y"), plugin.getConfig().getInt(p.getUniqueId() + ".base.coords.z"));
        for (int d = 0; d <= 360; d += 1) { // 360 - заполненность формы
            for (int x = 0; x <= 20; x += 1) {
                Location particleLoc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
                particleLoc.setX(location.getX() + 1 + Math.cos(d) * 90);
                particleLoc.setY(location.getY() + x);
                particleLoc.setZ(location.getZ() + 1 + Math.sin(d) * 90);
                Objects.requireNonNull(location.getWorld()).spawnParticle(Particle.REDSTONE, particleLoc, 20, new Particle.DustOptions(Color.BLUE, 1)); // size - размер и частота мигания, count - кол-во частиц
            }
        }
    }


    public void openPlayerAround(Player player) {
        player_head_around = Bukkit.createInventory(player, 27, ChatColor.RED + "Игроки рядом");
        ArrayList<Player> list = (ArrayList<Player>) getPlayers(player.getLocation(), 10);
        for (int i = 0; i < list.size(); i++) {
            if (player != list.get(i).getPlayer()) {
                if (!checkRegion(list.get(i), player.getUniqueId().toString())) {
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
                    ItemMeta meta = playerHead.getItemMeta();
                    assert meta != null;
                    meta.setDisplayName(list.get(i).getDisplayName());
                    playerHead.setItemMeta(meta);
                    player_head_around.addItem(playerHead);
                }
            }


        }
        ItemStack exit = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = exit.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.DARK_RED + "Назад");
        exit.setItemMeta(meta);
        player_head_around.setItem(26, exit);
        player.openInventory(player_head_around);
    }

    public void openPlayerInRegion(Player player) {
        player_head_region = Bukkit.createInventory(player, 27, ChatColor.RED + "Игроки в регионе");
        ArrayList<Player> list = (ArrayList<Player>) getPlayers(player.getLocation(), 10);
        for (Player value : list) {
            if (player != value.getPlayer()) {
                if (checkRegion(value, player.getUniqueId().toString())) {
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
                    ItemMeta meta = playerHead.getItemMeta();
                    assert meta != null;
                    meta.setDisplayName(value.getDisplayName());
                    playerHead.setItemMeta(meta);
                    player_head_region.addItem(playerHead);
                }
            }

        }
        ItemStack exit = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = exit.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.DARK_RED + "Назад");
        exit.setItemMeta(meta);
        player_head_region.setItem(26, exit);
        player.openInventory(player_head_region);
    }


    public void addPermission(Player player, String permission) {
        if (provider != null) {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            LuckPerms api = provider.getProvider();
            // Add the permission
            user.data().add(Node.builder(permission).build());
            // Now we need to save changes.
            api.getUserManager().saveUser(user);
        }
    }

    public void removePermission(Player player, String permission) {
        if (provider != null) {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            LuckPerms api = provider.getProvider();
            // Add the permission
            user.data().remove(Node.builder(permission).build());

            // Now we need to save changes.
            api.getUserManager().saveUser(user);
        }
    }

    public boolean hasPermission(Player player, String permission) {
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

    }

    @EventHandler
    public void onExit(PlayerQuitEvent e) {
        boolean glave = plugin.getConfig().getBoolean(e.getPlayer().getUniqueId() + ".base.glave");
        GlaveRegion(e.getPlayer());
        glave = false;
        plugin.getConfig().set(e.getPlayer().getUniqueId() + ".base.glave", glave);
        plugin.saveConfig();
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§7Прокачка");
        meta.setCustomModelData(5);
        meta.setLore(Arrays.asList("§8» Нажмите чтобы открыть меню улучшения персонажа"));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        p.getInventory().setItem(17, item);


        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("° " + (int) p.getHealth() + "/" + (int) p.getMaxHealth() + "   ∞ " + PlaceholderAPI.setPlaceholders(p, "%radioactiveBlocks_radLvl%") + " ‣ " + PlaceholderAPI.setPlaceholders(p, "%mycommand_playerdata_lvl_zavis%")).create()), 0L, 20L);

    }
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Iterator<ItemStack> dropIterator = e.getDrops().iterator();
        while (dropIterator.hasNext()) {
            if (dropIterator.next().getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                dropIterator.remove();
            }
        }
    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e)
    {
        Player p = e.getPlayer();
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§7Прокачка");
        meta.setCustomModelData(5);
        meta.setLore(Arrays.asList("§8» Нажмите чтобы открыть меню улучшения персонажа"));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        p.getInventory().setItem(17, item);


        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("° " + (int) p.getHealth() + "/" + (int) p.getMaxHealth() + "   ∞ " + PlaceholderAPI.setPlaceholders(p, "%radioactiveBlocks_radLvl%") + " ‣ " + PlaceholderAPI.setPlaceholders(p, "%mycommand_playerdata_lvl_zavis%")).create()), 0L, 20L);

    }

    public static boolean hasExecutableBlocks = false;
    Plugin executableBlocks = null;

    {
        SCore.plugin.getServer().getLogger().info("[" + NAME + "] ExecutableBlocks hooked !");
        hasExecutableBlocks = true;
    }



    public void openGUI(Player p) {
        boolean glave = plugin.getConfig().getBoolean(p.getUniqueId() + ".base.glave");
        List<String> lore = new ArrayList<>();
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        if (!glave) {
            item = new ItemStack(Material.REDSTONE_TORCH);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Показать зону региона");
            lore.add(ChatColor.GREEN + "Нажмите, чтобы увидеть границы региона");

        } else {
            item = new ItemStack(Material.TORCH);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Убрать зону региона");
            lore.add(ChatColor.GREEN + "Нажмите, чтобы отключить видимость границ региона");

        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        gui_base.setItem(17, item);
        lore.clear();
        {
            item = new ItemStack(Material.BARRIER);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Удалить игрока из региона");
            lore.add(ChatColor.GREEN + "Нажмите, чтобы убрать игроков из региона!");
            meta.setLore(lore);

            item.setItemMeta(meta);
            gui_base.setItem(7, item);
        }
        lore.clear();
        {
            item = new ItemStack(Material.DIAMOND);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Добавить игрока в регион");
            lore.add(ChatColor.GREEN + "Нажмите, чтобы добавить игроков поблизасти в приват");
            meta.setLore(lore);

            item.setItemMeta(meta);
            gui_base.setItem(8, item);
        }
        lore.clear();
//////////////////////////// base lvl 1

        if (plugin.getConfig().getInt(p.getUniqueId() + ".base.level") == 1) {
            item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "База 2ур.");
            if (CheckItems(p, Material.STONE, 3, 5)) {
                lore.add(ChatColor.GREEN + "x5 Веревка");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x5 Веревка");
                meta.setLore(lore);

            }
            if (CheckItems(p, Material.IRON_INGOT, 3, 10)) {
                lore.add(ChatColor.GREEN + "x10 Железо");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x10 Железо");
                meta.setLore(lore);

            }
            item.setItemMeta(meta);
            gui_base.setItem(26, item);
        }
        lore.clear();
///////////////////////////////// base lvl 2
        if (plugin.getConfig().getInt(p.getUniqueId() + ".base.level") == 2) {
            item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "База 2ур.");
            if (CheckItems(p, Material.STONE, 3, 5)) {
                lore.add(ChatColor.GREEN + "x5 Веревка");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x5 Веревка");
                meta.setLore(lore);

            }
            if (CheckItems(p, Material.IRON_INGOT, 3, 10)) {
                lore.add(ChatColor.GREEN + "x10 Железо");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x10 Железо");
                meta.setLore(lore);

            }
            item.setItemMeta(meta);
            gui_base.setItem(26, item);
        }
        lore.clear();
///////////////////////////////// base lvl 3
        if (plugin.getConfig().getInt(p.getUniqueId() + ".base.level") == 3) {
            item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "База 3ур.");
            if (CheckItems(p, Material.STONE, 3, 5)) {
                lore.add(ChatColor.GREEN + "x5 Веревка");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x5 Веревка");
                meta.setLore(lore);

            }
            if (CheckItems(p, Material.IRON_INGOT, 3, 10)) {
                lore.add(ChatColor.GREEN + "x10 Железо");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x10 Железо");
                meta.setLore(lore);

            }
            item.setItemMeta(meta);
            gui_base.setItem(26, item);
        }
        lore.clear();
///////////////////////////////// base lvl 4
        if (plugin.getConfig().getInt(p.getUniqueId() + ".base.level") == 4) {
            item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "База 4ур.");
            if (CheckItems(p, Material.STONE, 3, 5)) {
                lore.add(ChatColor.GREEN + "x5 Веревка");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x5 Веревка");
                meta.setLore(lore);

            }
            if (CheckItems(p, Material.IRON_INGOT, 3, 10)) {
                lore.add(ChatColor.GREEN + "x10 Железо");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x10 Железо");
                meta.setLore(lore);

            }
            item.setItemMeta(meta);
            gui_base.setItem(26, item);
        }
        lore.clear();
///////////////////////////////// base lvl 2
        if (plugin.getConfig().getInt(p.getUniqueId() + ".base.level") == 5) {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "База МАКС ур.");
            item.setItemMeta(meta);
            gui_base.setItem(26, item);
        }
        lore.clear();
//////////////////////////////// 0

        if (!plugin.getConfig().getBoolean(p.getUniqueId() + ".base.upgrade.armor_workbench")) {
            item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Верстак для брони");
            if (CheckItems(p, Material.STONE, 3, 5)) {
                lore.add(ChatColor.GREEN + "x5 Веревка");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x5 Веревка");
                meta.setLore(lore);

            }
            if (CheckItems(p, Material.IRON_INGOT, 3, 10)) {
                lore.add(ChatColor.GREEN + "x10 Железо");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x10 Железо");
                meta.setLore(lore);

            }
            item.setItemMeta(meta);
            gui_base.setItem(0, item);
        } else {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Верстак для брони");
            lore.add(ChatColor.RED + "Нажмите, чтобы открыть");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_base.setItem(0, item);
        }

        lore.clear();
//////// //////////////////// 1

        if (!plugin.getConfig().getBoolean(p.getUniqueId() + ".base.upgrade.weapon_workbench")) {
            item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Верстак для оружия");
            if (CheckItems(p, Material.STONE, 3, 5)) {
                lore.add(ChatColor.GREEN + "x5 Веревка");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x5 Веревка");
                meta.setLore(lore);

            }
            if (CheckItems(p, Material.IRON_INGOT, 3, 10)) {
                lore.add(ChatColor.GREEN + "x10 Железо");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x10 Железо");
                meta.setLore(lore);

            }
            item.setItemMeta(meta);
            gui_base.setItem(1, item);
        } else {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Верстак для оружия");
            lore.add(ChatColor.RED + "Нажмите, чтобы открыть");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_base.setItem(1, item);
        }

        lore.clear();
//////// ////////////////////////////// 2

        if (!plugin.getConfig().getBoolean(p.getUniqueId() + ".base.upgrade.medic_workbench")) {
            item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Верстак для медицины");
            if (CheckItems(p, Material.STONE, 3, 5)) {
                lore.add(ChatColor.GREEN + "x5 Веревка");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x5 Веревка");
                meta.setLore(lore);

            }
            if (CheckItems(p, Material.IRON_INGOT, 3, 10)) {
                lore.add(ChatColor.GREEN + "x10 Железо");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x10 Железо");
                meta.setLore(lore);

            }
            item.setItemMeta(meta);
            gui_base.setItem(2, item);
        } else {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Верстак для медицины");
            lore.add(ChatColor.RED + "Нажмите, чтобы открыть");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_base.setItem(2, item);
        }

        lore.clear();
//////// //////////////////////////////// 3

        if (!plugin.getConfig().getBoolean(p.getUniqueId() + ".base.upgrade.component_workbench")) {
            item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Верстак для компонентов");
            if (CheckItems(p, Material.STONE, 3, 5)) {
                lore.add(ChatColor.GREEN + "x5 Веревка");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x5 Веревка");
                meta.setLore(lore);

            }
            if (CheckItems(p, Material.IRON_INGOT, 3, 10)) {
                lore.add(ChatColor.GREEN + "x10 Железо");
                meta.setLore(lore);

            } else {
                lore.add(ChatColor.RED + "x10 Железо");
                meta.setLore(lore);

            }
            item.setItemMeta(meta);
            gui_base.setItem(3, item);
        } else {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Верстак для компонентов");
            lore.add(ChatColor.RED + "Нажмите, чтобы открыть");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_base.setItem(3, item);
        }
        lore.clear();

        p.openInventory(gui_base);


    }

    public static void removeInventoryItems(PlayerInventory inv, Material type, int model, int amount) {
        for (ItemStack is : inv.getContents()) {
            if (is != null && is.hasItemMeta() && Objects.requireNonNull(is.getItemMeta()).hasCustomModelData()) {
                if (is.getType() == type && is.getItemMeta().getCustomModelData() == model) {
                    int newamount = is.getAmount() - amount;
                    if (newamount > 0) {
                        is.setAmount(newamount);
                        break;
                    } else {
                        inv.remove(is);
                        amount = -newamount;
                        if (amount == 0) break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void guiRemoveRegion(InventoryClickEvent e) {
        if (!e.getInventory().equals(player_head_region)) {
            return;
        }
        UUID id;
        e.setCancelled(true);
        id = e.getWhoClicked().getUniqueId();
        Player p = (Player) e.getWhoClicked();

        if (Objects.requireNonNull(e.getCurrentItem()).getType() == Material.PLAYER_HEAD) {
            removeRegion(Objects.requireNonNull(((Player) e.getWhoClicked()).getPlayer()), Objects.requireNonNull(Bukkit.getPlayer(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName())));
            ((Player) e.getWhoClicked()).getPlayer().sendMessage("Игрок " + e.getCurrentItem().getItemMeta().getDisplayName() + " убран из в региоа!");
            openPlayerInRegion(p);
            e.setCancelled(true);
        }
        if (e.getCurrentItem().getType() == Material.BARRIER) openGUI(p);


    }

    @EventHandler
    public void guiAddRegion(InventoryClickEvent e) {
        UUID id;
        if (!e.getInventory().equals(player_head_around)) {
            return;
        }
        e.setCancelled(true);
        id = e.getWhoClicked().getUniqueId();
        Player p = (Player) e.getWhoClicked();

        if (Objects.requireNonNull(e.getCurrentItem()).getType() == Material.PLAYER_HEAD) {


            addRegion(Objects.requireNonNull(((Player) e.getWhoClicked()).getPlayer()), Objects.requireNonNull(Bukkit.getPlayer(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName())));
            ((Player) e.getWhoClicked()).getPlayer().sendMessage("Игрок " + e.getCurrentItem().getItemMeta().getDisplayName() + " добавлен в регион!");
            openPlayerAround(p);
            e.setCancelled(true);
        }
        if (e.getCurrentItem().getType() == Material.BARRIER) openGUI(p);


    }


    @EventHandler
    public void guiClickEvent(InventoryClickEvent e) {
        if (!e.getInventory().equals(gui_base)) {
            return;
        }
        boolean glave = plugin.getConfig().getBoolean(e.getWhoClicked().getUniqueId() + ".base.glave");
        UUID id;
        e.setCancelled(true);
        id = e.getWhoClicked().getUniqueId();
        Player p = (Player) e.getWhoClicked();
        switch (e.getSlot()) {
            case 0: {
                if (!plugin.getConfig().getBoolean(p.getUniqueId() + ".base.upgrade.armor_workbench")) {
                    if (CheckItems(p, Material.IRON_INGOT, 3, 10) && CheckItems(p, Material.STONE, 3, 5)) {
                        plugin.getConfig().set(id + ".base.upgrade.armor_workbench", true);
                        plugin.saveConfig();
                        p.sendMessage(ChatColor.GREEN + "Верстак для брони построен!");
                        removeInventoryItems(p.getInventory(), Material.STONE, 3, 5);
                        removeInventoryItems(p.getInventory(), Material.IRON_INGOT, 3, 10);
                        openGUI(p);
                    } else p.sendMessage(ChatColor.RED + "Для постройки не хватает ресурсов!");
                } else {
                    p.closeInventory();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ecraft opencategory " + Objects.requireNonNull(((Player) e.getWhoClicked()).getPlayer()).getName() + " armors");
                }
                break;
            }
            case 1: {
                if (!plugin.getConfig().getBoolean(p.getUniqueId() + ".base.upgrade.weapon_workbench")) {
                    if (CheckItems(p, Material.IRON_INGOT, 3, 10) && CheckItems(p, Material.STONE, 3, 5)) {
                        plugin.getConfig().set(id + ".base.upgrade.weapon_workbench", true);
                        plugin.saveConfig();
                        p.sendMessage(ChatColor.GREEN + "Оружейный верстак построен!");
                        removeInventoryItems(p.getInventory(), Material.STONE, 3, 5);
                        removeInventoryItems(p.getInventory(), Material.IRON_INGOT, 3, 10);
                        openGUI(p);
                    } else p.sendMessage(ChatColor.RED + "Для постройки не хватает ресурсов!");
                } else {
                    p.closeInventory();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ecraft opencategory " + Objects.requireNonNull(((Player) e.getWhoClicked()).getPlayer()).getName() + " weapons");
                }
                break;
            }
            case 2: {
                if (!plugin.getConfig().getBoolean(p.getUniqueId() + ".base.upgrade.medic_workbench")) {
                    if (CheckItems(p, Material.IRON_INGOT, 3, 10) && CheckItems(p, Material.STONE, 3, 5)) {
                        plugin.getConfig().set(id + ".base.upgrade.medic_workbench", true);
                        plugin.saveConfig();
                        p.sendMessage(ChatColor.GREEN + "Медецинский верстак построен!");
                        removeInventoryItems(p.getInventory(), Material.STONE, 3, 5);
                        removeInventoryItems(p.getInventory(), Material.IRON_INGOT, 3, 10);
                        openGUI(p);
                    } else p.sendMessage(ChatColor.RED + "Для постройки не хватает ресурсов!");
                } else {
                    p.closeInventory();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ecraft opencategory " + Objects.requireNonNull(((Player) e.getWhoClicked()).getPlayer()).getName() + " meds");
                }
                break;
            }
            case 3: {
                if (!plugin.getConfig().getBoolean(p.getUniqueId() + ".base.upgrade.component_workbench")) {
                    if (CheckItems(p, Material.IRON_INGOT, 3, 10) && CheckItems(p, Material.STONE, 3, 5)) {
                        plugin.getConfig().set(id + ".base.upgrade.component_workbench", true);
                        plugin.saveConfig();
                        p.sendMessage(ChatColor.GREEN + "Верстак для копмонентов построен!");
                        removeInventoryItems(p.getInventory(), Material.STONE, 3, 5);
                        removeInventoryItems(p.getInventory(), Material.IRON_INGOT, 3, 10);
                        openGUI(p);
                    } else p.sendMessage(ChatColor.RED + "Для постройки не хватает ресурсов!");
                } else {
                    p.closeInventory();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ecraft opencategory " + Objects.requireNonNull(((Player) e.getWhoClicked()).getPlayer()).getName() + " component");
                }
                break;
            }
            case 7: {
                openPlayerInRegion(p);
                break;
            }
            case 8: {
                openPlayerAround(p);
                break;
            }
            case 17: {
                glave = !glave;
                plugin.getConfig().set(e.getWhoClicked().getUniqueId() + ".base.glave", glave);
                plugin.saveConfig();
                if (glave) {
                    GlaveRegion(((Player) e.getWhoClicked()).getPlayer());
                } else {
                    GlaveRegion((Player) e.getWhoClicked());
                }
                openGUI(p);
                break;
            }
            case 26: {
                if (plugin.getConfig().getInt(p.getUniqueId() + ".base.level") == 1) {
                    if (CheckItems(p, Material.IRON_INGOT, 3, 10) && CheckItems(p, Material.STONE, 3, 5)) {
                        plugin.getConfig().set(p.getUniqueId() + ".base.level", 2);
                        plugin.saveConfig();
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eb place base_2 " + plugin.getConfig().getInt(id + ".base.coords.x") + " " + plugin.getConfig().getInt(id + ".base.coords.y") + " " + plugin.getConfig().getInt(id + ".base.coords.z") + " world");
                        p.sendMessage(ChatColor.GREEN + "База улучшена до 2 уровня!");
                        removeInventoryItems(p.getInventory(), Material.STONE, 3, 5);
                        removeInventoryItems(p.getInventory(), Material.IRON_INGOT, 3, 10);
                        Location locate = new Location(e.getWhoClicked().getWorld(), plugin.getConfig().getDouble(id + ".base.coords.x"), plugin.getConfig().getDouble(id + ".base.coords.y"), plugin.getConfig().getDouble(id + ".base.coords.z"));;
                        RegionCreate((Player) e.getWhoClicked(),
                                e.getWhoClicked().getUniqueId().toString(),
                                BlockVector3.at(locate.getBlockX() - index(p), locate.getBlockY() - index(p), locate.getBlockZ() - index(p)),
                                BlockVector3.at(locate.getBlockX() + index(p), locate.getBlockY() + index(p), locate.getBlockZ() + index(p)));
                        openGUI(p);
                    } else p.sendMessage(ChatColor.RED + "Для постройки не хватает ресурсов!");
                    break;
                }

                if (plugin.getConfig().getInt(p.getUniqueId() + ".base.level") == 2) {
                    if (CheckItems(p, Material.IRON_INGOT, 3, 10) && CheckItems(p, Material.STONE, 3, 5)) {
                        plugin.getConfig().set(p.getUniqueId() + ".base.level", 3);
                        plugin.saveConfig();
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eb place base_3 " + plugin.getConfig().getInt(id + ".base.coords.x") + " " + plugin.getConfig().getInt(id + ".base.coords.y") + " " + plugin.getConfig().getInt(id + ".base.coords.z") + " world");
                        p.sendMessage(ChatColor.GREEN + "База улучшена до 3 уровня!");
                        removeInventoryItems(p.getInventory(), Material.STONE, 3, 5);
                        removeInventoryItems(p.getInventory(), Material.IRON_INGOT, 3, 10);
                        Location locate = new Location(e.getWhoClicked().getWorld(), plugin.getConfig().getDouble(id + ".base.coords.x"), plugin.getConfig().getDouble(id + ".base.coords.y"), plugin.getConfig().getDouble(id + ".base.coords.z"));
                        summonCircle(p);
                        summonCirclePrivate(p);
                        RegionCreate((Player) e.getWhoClicked(),
                                e.getWhoClicked().getUniqueId().toString(),
                                BlockVector3.at(locate.getBlockX() - index(p), locate.getBlockY() - index(p), locate.getBlockZ() - index(p)),
                                BlockVector3.at(locate.getBlockX() + index(p), locate.getBlockY() + index(p), locate.getBlockZ() + index(p)));
                        openGUI(p);
                    } else p.sendMessage(ChatColor.RED + "Для постройки не хватает ресурсов!");
                    break;
                }

                    if (plugin.getConfig().getInt(p.getUniqueId() + ".base.level") == 3) {
                        if (CheckItems(p, Material.IRON_INGOT, 3, 10) && CheckItems(p, Material.STONE, 3, 5)) {
                            plugin.getConfig().set(p.getUniqueId() + ".base.level", 4);
                            plugin.saveConfig();
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eb place base_4 " + plugin.getConfig().getInt(id + ".base.coords.x") + " " + plugin.getConfig().getInt(id + ".base.coords.y") + " " + plugin.getConfig().getInt(id + ".base.coords.z") + " world");
                            p.sendMessage(ChatColor.GREEN + "База улучшена до 4 уровня!");
                            removeInventoryItems(p.getInventory(), Material.STONE, 3, 5);
                            removeInventoryItems(p.getInventory(), Material.IRON_INGOT, 3, 10);
                            Location locate = new Location(e.getWhoClicked().getWorld(), plugin.getConfig().getDouble(id + ".base.coords.x"), plugin.getConfig().getDouble(id + ".base.coords.y"), plugin.getConfig().getDouble(id + ".base.coords.z"));;
                            RegionCreate((Player) e.getWhoClicked(),
                                    e.getWhoClicked().getUniqueId().toString(),
                                    BlockVector3.at(locate.getBlockX() - index(p), locate.getBlockY() - index(p), locate.getBlockZ() - index(p)),
                                    BlockVector3.at(locate.getBlockX() + index(p), locate.getBlockY() + index(p), locate.getBlockZ() + index(p)));
                            openGUI(p);
                        } else p.sendMessage(ChatColor.RED + "Для постройки не хватает ресурсов!");
                        break;
                    }

                    if (plugin.getConfig().getInt(p.getUniqueId() + ".base.level") == 4) {
                        if (CheckItems(p, Material.IRON_INGOT, 3, 10) && CheckItems(p, Material.STONE, 3, 5)) {
                            plugin.getConfig().set(p.getUniqueId() + ".base.level", 5);
                            plugin.saveConfig();
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eb place base_5 " + plugin.getConfig().getInt(id + ".base.coords.x") + " " + plugin.getConfig().getInt(id + ".base.coords.y") + " " + plugin.getConfig().getInt(id + ".base.coords.z") + " world");
                            p.sendMessage(ChatColor.GREEN + "База улучшена до 5 уровня!");
                            removeInventoryItems(p.getInventory(), Material.STONE, 3, 5);
                            removeInventoryItems(p.getInventory(), Material.IRON_INGOT, 3, 10);
                            Location locate = new Location(e.getWhoClicked().getWorld(), plugin.getConfig().getDouble(id + ".base.coords.x"), plugin.getConfig().getDouble(id + ".base.coords.y"), plugin.getConfig().getDouble(id + ".base.coords.z"));;
                            RegionCreate((Player) e.getWhoClicked(),
                                    e.getWhoClicked().getUniqueId().toString(),
                                    BlockVector3.at(locate.getBlockX() - index(p), locate.getBlockY() - index(p), locate.getBlockZ() - index(p)),
                                    BlockVector3.at(locate.getBlockX() + index(p), locate.getBlockY() + index(p), locate.getBlockZ() + index(p)));
                            openGUI(p);
                        } else p.sendMessage(ChatColor.RED + "Для постройки не хватает ресурсов!");
                        break;
                    }

                    break;
                }
            }
        }



    public boolean CheckItems(Player p, Material item, int model, int amount) {
        boolean result = false;
        for (int i = 0; i < 36; i++) {
            if (p.getInventory().getItem(i) != null) {
                if (Objects.requireNonNull(p.getInventory().getItem(i)).getType() == item) {
                    if (Objects.requireNonNull(p.getInventory().getItem(i)).hasItemMeta()) {
                        if (Objects.requireNonNull(p.getInventory().getItem(i).getItemMeta()).hasCustomModelData() && Objects.requireNonNull(p.getInventory().getItem(i).getItemMeta()).getCustomModelData() == model) {
                            if (Objects.requireNonNull(p.getInventory().getItem(i)).getAmount() >= amount) {
                                result = true;
                            }
                        }
                    }
                }
            }
        }
        return result;

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void openGuiEvent(PlayerInteractEvent e) {

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && (Objects.requireNonNull(e.getClickedBlock()).getType() == Material.CAMPFIRE || e.getAction() == Action.RIGHT_CLICK_BLOCK && (Objects.requireNonNull(e.getClickedBlock()).getType() == Material.SOUL_CAMPFIRE || e.getAction() == Action.RIGHT_CLICK_BLOCK && (Objects.requireNonNull(e.getClickedBlock()).getType() == Material.BELL || e.getClickedBlock().getType() == Material.LECTERN || e.getClickedBlock().getType() == Material.GRINDSTONE)))) {
            if (checkRegionLocation(e.getPlayer())) {
                e.setCancelled(true);
                openGUI(e.getPlayer());
            } else {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "Вы не можете взаимодействовать с убежищем в чужом регионе!");
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void BasePlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        UUID id;
        if (e.getBlockPlaced().getType() == Material.CAMPFIRE) {
            if(!protectRegion(p)){
                if (plugin.getConfig().getBoolean(e.getPlayer().getUniqueId() + ".base.build")) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "У вас уже имеется база!");
                } else {
                    id = e.getPlayer().getUniqueId();
                    plugin.getConfig().set(id + ".owner", e.getPlayer().getName());
                    plugin.getConfig().set(id + ".base.build", true);
                    plugin.getConfig().set(id + ".base.coords.x", e.getBlockPlaced().getLocation().getBlockX());
                    plugin.getConfig().set(id + ".base.coords.y", e.getBlockPlaced().getLocation().getBlockY());
                    plugin.getConfig().set(id + ".base.coords.z", e.getBlockPlaced().getLocation().getBlockZ());
                    plugin.getConfig().set(id + ".base.level", 1);
                    Location locate = e.getBlockPlaced().getLocation();

                    RegionCreate(e.getPlayer(),
                            e.getPlayer().getUniqueId().toString(),
                            BlockVector3.at(locate.getBlockX() - index(p), locate.getBlockY() - index(p), locate.getBlockZ() - index(p)),
                            BlockVector3.at(locate.getBlockX() + index(p), locate.getBlockY() + index(p), locate.getBlockZ() + index(p)));
                    addRegion(e.getPlayer(), e.getPlayer());
                    plugin.saveConfig();
                }
            }
            else
            {
              p.sendMessage(ChatColor.RED+"Вы не можете устанавливать свое убежище рядом с чужим!");
              e.setCancelled(true);
            }
        }


        if (e.getBlockPlaced().getType() == Material.SOUL_CAMPFIRE) {
            if(!protectRegion(p)){
                if (plugin.getConfig().getBoolean(e.getPlayer().getUniqueId() + ".base.build")) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "У вас уже имеется база!");
                } else {
                    id = e.getPlayer().getUniqueId();
                    plugin.getConfig().set(id + ".owner", e.getPlayer().getName());
                    plugin.getConfig().set(id + ".base.build", true);
                    plugin.getConfig().set(id + ".base.coords.x", e.getBlockPlaced().getLocation().getBlockX());
                    plugin.getConfig().set(id + ".base.coords.y", e.getBlockPlaced().getLocation().getBlockY());
                    plugin.getConfig().set(id + ".base.coords.z", e.getBlockPlaced().getLocation().getBlockZ());
                    plugin.getConfig().set(id + ".base.level", 2);
                    Location locate = e.getBlockPlaced().getLocation();

                    RegionCreate(e.getPlayer(),
                            e.getPlayer().getUniqueId().toString(),
                            BlockVector3.at(locate.getBlockX() - index(p), locate.getBlockY() - index(p), locate.getBlockZ() - index(p)),
                            BlockVector3.at(locate.getBlockX() + index(p), locate.getBlockY() + index(p), locate.getBlockZ() + index(p)));
                    addRegion(e.getPlayer(), e.getPlayer());
                    plugin.saveConfig();
                }
            }
            else
            {
                p.sendMessage(ChatColor.RED+"Вы не можете устанавливать свое убежище рядом с чужим!");
                e.setCancelled(true);
            }
        }

        if (e.getBlockPlaced().getType() == Material.GRINDSTONE) {
            if(!protectRegion(p)){
                if (plugin.getConfig().getBoolean(e.getPlayer().getUniqueId() + ".base.build")) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "У вас уже имеется база!");
                } else {
                    id = e.getPlayer().getUniqueId();
                    plugin.getConfig().set(id + ".owner", e.getPlayer().getName());
                    plugin.getConfig().set(id + ".base.build", true);
                    plugin.getConfig().set(id + ".base.coords.x", e.getBlockPlaced().getLocation().getBlockX());
                    plugin.getConfig().set(id + ".base.coords.y", e.getBlockPlaced().getLocation().getBlockY());
                    plugin.getConfig().set(id + ".base.coords.z", e.getBlockPlaced().getLocation().getBlockZ());
                    plugin.getConfig().set(id + ".base.level", 3);
                    Location locate = e.getBlockPlaced().getLocation();

                    RegionCreate(e.getPlayer(),
                            e.getPlayer().getUniqueId().toString(),
                            BlockVector3.at(locate.getBlockX() - index(p), locate.getBlockY() - index(p), locate.getBlockZ() - index(p)),
                            BlockVector3.at(locate.getBlockX() + index(p), locate.getBlockY() + index(p), locate.getBlockZ() + index(p)));
                    addRegion(e.getPlayer(), e.getPlayer());
                    plugin.saveConfig();
                }
            }
            else
            {
                p.sendMessage(ChatColor.RED+"Вы не можете устанавливать свое убежище рядом с чужим!");
                e.setCancelled(true);
            }
        }

        if (e.getBlockPlaced().getType() == Material.BELL) {
            if(!protectRegion(p)){
                if (plugin.getConfig().getBoolean(e.getPlayer().getUniqueId() + ".base.build")) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "У вас уже имеется база!");
                } else {
                    id = e.getPlayer().getUniqueId();
                    plugin.getConfig().set(id + ".owner", e.getPlayer().getName());
                    plugin.getConfig().set(id + ".base.build", true);
                    plugin.getConfig().set(id + ".base.coords.x", e.getBlockPlaced().getLocation().getBlockX());
                    plugin.getConfig().set(id + ".base.coords.y", e.getBlockPlaced().getLocation().getBlockY());
                    plugin.getConfig().set(id + ".base.coords.z", e.getBlockPlaced().getLocation().getBlockZ());
                    plugin.getConfig().set(id + ".base.level", 4);
                    Location locate = e.getBlockPlaced().getLocation();

                    RegionCreate(e.getPlayer(),
                            e.getPlayer().getUniqueId().toString(),
                            BlockVector3.at(locate.getBlockX() - index(p), locate.getBlockY() - index(p), locate.getBlockZ() - index(p)),
                            BlockVector3.at(locate.getBlockX() + index(p), locate.getBlockY() + index(p), locate.getBlockZ() + index(p)));
                    addRegion(e.getPlayer(), e.getPlayer());
                    plugin.saveConfig();
                }
            }
            else
            {
                p.sendMessage(ChatColor.RED+"Вы не можете устанавливать свое убежище рядом с чужим!");
                e.setCancelled(true);
            }
        }

        if (e.getBlockPlaced().getType() == Material.LECTERN) {
            if(!protectRegion(p)){
                if (plugin.getConfig().getBoolean(e.getPlayer().getUniqueId() + ".base.build")) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "У вас уже имеется база!");
                } else {
                    id = e.getPlayer().getUniqueId();
                    plugin.getConfig().set(id + ".owner", e.getPlayer().getName());
                    plugin.getConfig().set(id + ".base.build", true);
                    plugin.getConfig().set(id + ".base.coords.x", e.getBlockPlaced().getLocation().getBlockX());
                    plugin.getConfig().set(id + ".base.coords.y", e.getBlockPlaced().getLocation().getBlockY());
                    plugin.getConfig().set(id + ".base.coords.z", e.getBlockPlaced().getLocation().getBlockZ());
                    plugin.getConfig().set(id + ".base.level", 5);
                    Location locate = e.getBlockPlaced().getLocation();

                    RegionCreate(e.getPlayer(),
                            e.getPlayer().getUniqueId().toString(),
                            BlockVector3.at(locate.getBlockX() - index(p), locate.getBlockY() - index(p), locate.getBlockZ() - index(p)),
                            BlockVector3.at(locate.getBlockX() + index(p), locate.getBlockY() + index(p), locate.getBlockZ() + index(p)));
                    addRegion(e.getPlayer(), e.getPlayer());
                    plugin.saveConfig();
                }
            }
            else
            {
                p.sendMessage(ChatColor.RED+"Вы не можете устанавливать свое убежище рядом с чужим!");
                e.setCancelled(true);
            }
        }

    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void BaseDestry(BlockBreakEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        if (e.getBlock().getType() == Material.CAMPFIRE || e.getBlock().getType() == Material.SOUL_CAMPFIRE || e.getBlock().getType() == Material.BELL || e.getBlock().getType() == Material.LECTERN || e.getBlock().getType() == Material.GRINDSTONE) {
            if (checkRegionLocation(e.getPlayer())) {
                if (e.getBlock().getLocation().getX() == plugin.getConfig().getInt(id + ".base.coords.x", e.getBlock().getLocation().getBlockX())) {
                    if (e.getBlock().getLocation().getY() == plugin.getConfig().getInt(id + ".base.coords.y", e.getBlock().getLocation().getBlockY())) {
                        if (e.getBlock().getLocation().getZ() == plugin.getConfig().getInt(id + ".base.coords.z", e.getBlock().getLocation().getBlockZ())) {
                            id = e.getPlayer().getUniqueId();
                            plugin.getConfig().set(id + ".owner", e.getPlayer().getName());
                            plugin.getConfig().set(id + ".base.build", false);

                            plugin.getConfig().set(id + ".base.glave", false);
                            plugin.saveConfig();
                            GlaveRegion(e.getPlayer());
                            RegionRemove(e.getPlayer());
                            RegionRemove(e.getPlayer());
                        }
                    }
                }
            } else {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "Вы не можете ломать блоки в чужом регионе!");
            }
        }
    }


    @EventHandler
    public void InventoryLVL(InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == 17) {
            if (Objects.requireNonNull(e.getCurrentItem().getItemMeta()).hasCustomModelData() && e.getCurrentItem().getItemMeta().getCustomModelData() == 5) {
                p.closeInventory();
                openGUILVL((Player) e.getWhoClicked());
            }
            e.setCancelled(true);
        }
    }

    public void openGUILVL(Player p) {
        gui_leveling = Bukkit.createInventory(null, 18, "Прокачка");


        //////////////////////////////////
        if (!hasPermission(p, "morehearts.group.default.ignore")) {
            ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Здоровье 1ур.");
            List<String> lore = new ArrayList<>();
            lore.add("Нажмите для прокачки");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_leveling.setItem(1, item);
        }
        if (hasPermission(p, "morehearts.group.legenda")) {
            ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Здоровье 2ур.");
            List<String> lore = new ArrayList<>();
            lore.add("Нажмите для прокачки");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_leveling.setItem(1, item);
        }

        if (hasPermission(p, "morehearts.group.ultra")) {
            ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Здоровье MAX ур.");
            item.setItemMeta(meta);
            gui_leveling.setItem(1, item);
        }
        //////////////////////////////////

        if (!hasPermission(p, "damagemodifier.group.lvl0.ignore")) {
            ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Урон 1ур.");
            List<String> lore = new ArrayList<>();
            lore.add("Нажмите для прокачки");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_leveling.setItem(2, item);
        }
        if (hasPermission(p, "damagemodifier.group.lvl1")) {
            ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Урон 2ур.");
            List<String> lore = new ArrayList<>();
            lore.add("Нажмите для прокачки");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_leveling.setItem(2, item);
        }
        if (hasPermission(p, "damagemodifier.group.lvl2")) {
            ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Урон 3ур.");
            List<String> lore = new ArrayList<>();
            lore.add("Нажмите для прокачки");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_leveling.setItem(2, item);
        }
        if (hasPermission(p, "damagemodifier.group.lvl3")) {
            ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Урон 4ур.");
            List<String> lore = new ArrayList<>();
            lore.add("Нажмите для прокачки");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui_leveling.setItem(2, item);
        }
        if (hasPermission(p, "damagemodifier.group.lvl4")) {
            ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Урон MAX ур.");
            item.setItemMeta(meta);
            gui_leveling.setItem(2, item);
        }


        p.openInventory(gui_leveling);


    }

    @EventHandler
    public void guiClickEventLVL(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (!e.getInventory().equals(gui_leveling)) {
            return;
        }
        e.setCancelled(true);
        {
            switch (e.getSlot()) {
                case 0: {
                    if (!hasPermission(p, "eb.block.*")) {
                        addPermission(p, "eb.block.*");
                        break;
                    } else {
                        removePermission(p, "eb.block.*");
                    }
                }
                case 1: {

                    if (!hasPermission(p, "morehearts.group.default.ignore")) {
                        addPermission(p, "morehearts.group.legenda");
                        addPermission(p, "morehearts.group.default.ignore");
                        break;
                    }
                    if (hasPermission(p, "morehearts.group.legenda")) {
                        addPermission(p, "morehearts.group.ultra");
                        removePermission(p, "morehearts.group.legenda");
                        break;
                    }
                }
                case 2: {
                    if (!hasPermission(p, "damagemodifier.group.lvl0.ignore")) {

                        addPermission(p, "damagemodifier.group.lvl0.ignore");
                        addPermission(p, "damagemodifier.group.lvl1");
                        break;
                    }
                    if (!hasPermission(p, "damagemodifier.group.lvl1.ignore")) {
                        addPermission(p, "damagemodifier.group.lvl1.ignore");
                        addPermission(p, "damagemodifier.group.lvl2");
                        removePermission(p, "damagemodifier.group.lvl1");
                        break;
                    }
                    if (!hasPermission(p, "damagemodifier.group.lvl2.ignore")) {
                        addPermission(p, "damagemodifier.group.lvl2.ignore");
                        addPermission(p, "damagemodifier.group.lvl3");
                        removePermission(p, "damagemodifier.group.lvl2");
                        break;
                    }
                    if (!hasPermission(p, "damagemodifier.group.lvl3.ignore")) {
                        addPermission(p, "damagemodifier.group.lvl3.ignore");
                        addPermission(p, "damagemodifier.group.lvl4");
                        break;
                    }

                }
            }

            openGUILVL(p);
        }
    }
}

