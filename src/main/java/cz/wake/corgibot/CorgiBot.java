package cz.wake.corgibot;

import cz.wake.corgibot.commands.CommandHandler;
import cz.wake.corgibot.commands.Prefixes;
import cz.wake.corgibot.listener.MainListener;
import cz.wake.corgibot.managers.IgnoredChannels;
import cz.wake.corgibot.runnable.StatusChanger;
import cz.wake.corgibot.sql.SQLManager;
import cz.wake.corgibot.utils.LoadingProperties;
import cz.wake.corgibot.utils.statuses.Checker;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorgiBot {

    private static CorgiBot instance;
    private MainListener events;
    private static JDA jda;
    private CommandHandler ch = new CommandHandler();
    public static final String PREFIX = ".";
    private SQLManager sql;
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MMMM yyyy HH:mm:ss");
    private static String imgflipToken = "";
    public static long startUp;
    private static final Map<String, Logger> LOGGERS;
    public static final Logger LOGGER;
    private static Prefixes prefixes;
    private static IgnoredChannels ignoredChannels;
    private static boolean isBeta;

    static {
        new File("latest.log").delete();
        LOGGERS = new ConcurrentHashMap<>();
        LOGGER = getLog(CorgiBot.class);
    }

    public static void main(String[] args) throws LoginException, RateLimitedException, InterruptedException, IOException {

        bootLogo();

        LoadingProperties config = new LoadingProperties();

        EventWaiter waiter = new EventWaiter();

        startUp = System.currentTimeMillis();

        jda = new JDABuilder(AccountType.BOT)
                .setToken(config.getBotToken())
                .addEventListener(new MainListener(waiter))
                .addEventListener(waiter)
                .setGame(Game.of("Loading..."))
                .buildBlocking();

        (instance = new CorgiBot()).init();
        (instance = new CorgiBot()).initDatabase();

        prefixes = new Prefixes();
        ignoredChannels = new IgnoredChannels();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new Checker(), 10, 60000);
        timer.scheduleAtFixedRate(new StatusChanger(), 10, 120000);

        imgflipToken = config.getImgFlipToken();

        /* NASTAVENI NOVY PROFILOVKY
        jda.getSelfUser().getManager().setAvatar(Icon.from(
                new URL("https://i.imgur.com/N9wftHn.jpg").openStream())).complete();*/
    }

    public static CorgiBot getInstance() {
        return instance;
    }

    public MainListener getEvents() {
        return events;
    }

    public static JDA getJda() {
        return jda;
    }

    public CommandHandler getCommandHandler() {
        return ch;
    }

    public SQLManager getSql() {
        return sql;
    }

    private void init() {
        ch.register();
    }

    private void initDatabase() {
        sql = new SQLManager(this);
    }

    public static long getStartUp(){
        return startUp;
    }

    public String formatTime(LocalDateTime dateTime) {
        return dateTime.getDayOfMonth() + ". " + dateTime.format(timeFormat);
    }

    public String getImgflipToken() {
        return imgflipToken;
    }

    private static Logger getLog(String name) {
        return LOGGERS.computeIfAbsent(name, LoggerFactory::getLogger);
    }

    public static Logger getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }

    public static Prefixes getPrefixes() {
        return prefixes;
    }

    public TextChannel getGuildLogChannel() {
        return getJda().getGuildById("255045073887166475").getTextChannelById("361636711585021953");
    }

    public static String getPrefix(String id) {
        return getPrefixes().get(id);
    }

    public static IgnoredChannels getIgnoredChannels(){
        return ignoredChannels;
    }

    public static Guild getDefaultGuild(){
        return getJda().getGuildById("255045073887166475");
    }


    private static void bootLogo(){
        LOGGER.info("Spousteni bota...");
        LOGGER.info("");
        LOGGER.info("   ______                 _ ");
        LOGGER.info("  / ____/___  _________ _(_)");
        LOGGER.info(" / /   / __ \\/ ___/ __ `/ / ");
        LOGGER.info("/ /___/ /_/ / /  / /_/ / /  ");
        LOGGER.info("\\____/\\____/_/   \\__, /_/   ");
        LOGGER.info("                /____/      ");
        LOGGER.info("");
    }

    public boolean isBeta() {
        return isBeta;
    }
}
