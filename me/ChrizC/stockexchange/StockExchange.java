package me.ChrizC.stockexchange;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.*;
import org.bukkit.event.*;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import com.nijikokun.register.payment.Method;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class StockExchange extends JavaPlugin {
    
    /**
     * To-do list:
     * TODO Figure out how to work "buying out" markets.
     * TODO Add buy/sales charges.
     **/
    
    public static PermissionHandler permissionHandler;
    protected final SEConfig config = new SEConfig(this);
    private final SEDateHandler dateHandler = new SEDateHandler(this);
    private final SEBackupHandler backup = new SEBackupHandler(this, config, dateHandler);
    protected final SEFileHandler fileHandler = new SEFileHandler(this, config);
    private final SEHelper helper = new SEHelper(this);
    private final SEMarketHandler marketHandler = new SEMarketHandler(this, config);
    private final SEPluginListener pluginListener = new SEPluginListener(this);
    private final SECommandListener cmdHandler = new SECommandListener(this, marketHandler, config, fileHandler, helper, backup);
    private final SEUpdater updater = new SEUpdater(this, config);
    public Method Method = null;
    
    protected static Set<StockExchangeListener> listeners = new HashSet<StockExchangeListener>();
    
    static HashMap<String, Double> market = new HashMap<String, Double>();
    static HashMap<String, Integer> stockOwnership = new HashMap<String, Integer>();
    
    @Override
    public void onDisable() {
        fileHandler.saveMarket();
        fileHandler.saveOwnership();
        if (config.backup == true) {
            backup.backup();
        }
        System.out.println("[StockExchange] disabled.");
    }
    
    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
        config.doConfig();
        updater.update(true);
        cmdHandler.setupCommands();
        setupPermissions();
        fileHandler.loadMarket();
        fileHandler.loadOwnership();
        config.configStocks();
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[StockExchange] version v" + pdfFile.getVersion() + " is enabled.");
        if (pdfFile.getVersion().contains("dev")) {
            System.out.println("[StockExchange] Warning: you are using a DEVELOPMENT build.");
            System.out.println("[StockExchange] Warning: Be sure to back up your worlds, and report all bugs to the thread.");
        }
    }
    
    private void setupPermissions() {
      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

      if (this.permissionHandler == null) {
            if (permissionsPlugin != null) {
                this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
                System.out.println("[StockExchange] hooked into Permissions.");
            } else {
                System.out.println("[StockExchange] Permissions not found, defaulting to ops.txt");
            }
        }
    }  
    
    public boolean checkPermissions(String node, Player player, boolean opcheck) {
        if (this.permissionHandler != null) {
            return this.permissionHandler.has(player, node);
        } else {
            if (opcheck == true) {
                return player.isOp();
            } else {
                return false;
            }
        }
    }

}
