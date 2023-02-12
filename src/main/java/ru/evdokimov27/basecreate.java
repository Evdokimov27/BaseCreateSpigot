package ru.evdokimov27;

import com.ubivashka.vk.bukkit.BukkitVkApiPlugin;
import com.ubivashka.vk.bukkit.events.VKMessageEvent;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public final class basecreate extends JavaPlugin {


    public static basecreate instance;
    private Storage data;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new GUIICommand(), this);
        saveDefaultConfig();
        data = new Storage("config.yml");
    }


    public static basecreate getInstance() {
        return instance;
    }

    public static Storage getData() {
        return instance.data;
    }

    @Override
    public void onDisable() {
        saveResource("config.yml", false);
    }


}
