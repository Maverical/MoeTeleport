package cc.carm.plugin.moeteleport;

import cc.carm.plugin.moeteleport.command.BackCommand;
import cc.carm.plugin.moeteleport.command.MoeTeleportCommand;
import cc.carm.plugin.moeteleport.command.completer.HomeNameCompleter;
import cc.carm.plugin.moeteleport.command.completer.PlayerNameCompleter;
import cc.carm.plugin.moeteleport.command.completer.TpRequestCompleter;
import cc.carm.plugin.moeteleport.command.home.DelHomeCommand;
import cc.carm.plugin.moeteleport.command.home.GoHomeCommand;
import cc.carm.plugin.moeteleport.command.home.ListHomeCommand;
import cc.carm.plugin.moeteleport.command.home.SetHomeCommand;
import cc.carm.plugin.moeteleport.command.tpa.TpHandleCommand;
import cc.carm.plugin.moeteleport.command.tpa.TpaCommand;
import cc.carm.plugin.moeteleport.configuration.PluginConfig;
import cc.carm.plugin.moeteleport.listener.UserListener;
import cc.carm.plugin.moeteleport.manager.ConfigManager;
import cc.carm.plugin.moeteleport.manager.RequestManager;
import cc.carm.plugin.moeteleport.manager.UserManager;
import cc.carm.plugin.moeteleport.storage.DataStorage;
import cc.carm.plugin.moeteleport.storage.StorageMethod;
import cc.carm.plugin.moeteleport.util.ColorParser;
import cc.carm.plugin.moeteleport.util.JarResourceUtils;
import cc.carm.plugin.moeteleport.util.SchedulerUtils;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class Main extends JavaPlugin {
    private static Main instance;
    private static SchedulerUtils scheduler;

    private static DataStorage storage;

    private UserManager userManager;
    private RequestManager requestManager;

    @Override
    public void onEnable() {
        instance = this;
        scheduler = new SchedulerUtils(this);
        outputInfo();

        log(getName() + " " + getDescription().getVersion() + " &7开始加载...");
        long startTime = System.currentTimeMillis();

        log("加载配置文件...");
        ConfigManager.initConfig();

        log("初始化存储方式...");
        StorageMethod storageMethod = StorageMethod.read(PluginConfig.STORAGE_METHOD.get());
        log("	正在使用 " + storageMethod.name() + " 进行数据存储");

        storage = storageMethod.createStorage();
        if (!storage.initialize()) {
            error("初始化存储失败，请检查配置文件。");
            storage.shutdown();
            setEnabled(false);
            return; // 初始化失败，不再继续加载
        }


        log("加载用户管理器...");
        this.userManager = new UserManager();
        if (Bukkit.getOnlinePlayers().size() > 0) {
            log("   加载现有用户数据...");
            getUserManager().loadAll();
        }

        log("加载请求管理器...");
        this.requestManager = new RequestManager(this);

        log("注册监听器...");
        regListener(new UserListener());

        log("注册指令...");
        registerCommand("MoeTeleport", new MoeTeleportCommand());

        registerCommand("back", new BackCommand());

        registerCommand("home", new GoHomeCommand(), new HomeNameCompleter());
        registerCommand("delHome", new DelHomeCommand(), new HomeNameCompleter());
        registerCommand("setHome", new SetHomeCommand());
        registerCommand("listHome", new ListHomeCommand());

        registerCommand("tpa", new TpaCommand(), new PlayerNameCompleter());
        registerCommand("tpaHere", new TpaCommand(), new PlayerNameCompleter());
        registerCommand("tpAccept", new TpHandleCommand(), new TpRequestCompleter());
        registerCommand("tpDeny", new TpHandleCommand(), new TpRequestCompleter());

        if (PluginConfig.METRICS.get()) {
            log("启用统计数据...");
            Metrics metrics = new Metrics(this, 14459);
            metrics.addCustomChart(new SimplePie("storage_method", storageMethod::name));
        }

        log("加载完成 ，共耗时 " + (System.currentTimeMillis() - startTime) + " ms 。");
    }

    @Override
    public void onDisable() {
        outputInfo();
        log(getName() + " " + getDescription().getVersion() + " 开始卸载...");
        long startTime = System.currentTimeMillis();

        log("关闭所有请求...");
        getRequestManager().shutdown();

        log("保存用户数据...");
        getUserManager().unloadAll(true);

        log("卸载监听器...");
        Bukkit.getServicesManager().unregisterAll(this);

        log("卸载完成 ，共耗时 " + (System.currentTimeMillis() - startTime) + " ms 。");
    }

    /**
     * 注册监听器
     *
     * @param listener 监听器
     */
    public static void regListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, getInstance());
    }

    public void outputInfo() {
        String[] pluginInfo = JarResourceUtils.readResource(this.getResource("PLUGIN_INFO"));
        if (pluginInfo != null) {
            Arrays.stream(pluginInfo).forEach(Main::log);
        }
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.parse("[" + getInstance().getName() + "] " + message));
    }

    public static void error(String message) {
        log("&4[ERROR] &r" + message);
    }

    public static void debug(String message) {
        if (PluginConfig.DEBUG.get()) {
            log("&e[DEBUG] &r" + message);
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public static SchedulerUtils getScheduler() {
        return scheduler;
    }

    public static void registerCommand(String commandName,
                                       @NotNull CommandExecutor executor) {
        registerCommand(commandName, executor, null);
    }

    public static void registerCommand(String commandName,
                                       @NotNull CommandExecutor executor,
                                       @Nullable TabCompleter tabCompleter) {
        PluginCommand command = Bukkit.getPluginCommand(commandName);
        if (command == null) return;
        command.setExecutor(executor);
        if (tabCompleter != null) command.setTabCompleter(tabCompleter);
    }

    public static DataStorage getStorage() {
        return storage;
    }

    public static UserManager getUserManager() {
        return Main.getInstance().userManager;
    }

    public static RequestManager getRequestManager() {
        return Main.getInstance().requestManager;
    }


}
