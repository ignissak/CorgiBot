package cz.wake.corgibot.commands.user;

import cz.wake.corgibot.CorgiBot;
import cz.wake.corgibot.commands.ICommand;
import cz.wake.corgibot.commands.CommandType;
import cz.wake.corgibot.commands.Rank;
import cz.wake.corgibot.utils.Constants;
import cz.wake.corgibot.utils.MessageUtils;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Game;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfo implements ICommand {


    @Override
    public void onCommand(User sender, MessageChannel channel, Message message, String[] args, Member member, EventWaiter w, String guildPrefix) {
        String id;
        if (args.length != 1) {
            id = sender.getId();
        } else {
            id = args[0].replaceAll("[^0-9]", "");
        }
        if (id.isEmpty()) {
            MessageUtils.sendAutoDeletedMessage("Musíš použít označení s @!", 10000, channel);
            return;
        }
        User user = CorgiBot.getJda().getUserById(id);
        if (user == null) {
            MessageUtils.sendAutoDeletedMessage("Nelze najít uživatele!", 10000, channel);
            return;
        }
        Member m2 = member.getGuild().getMember(user);

        StringBuilder joinOrder = new StringBuilder();
        List<Member> joins = message.getGuild().getMemberCache().stream().collect(Collectors.toList());
        joins.sort(Comparator.comparing(Member::getJoinDate));
        int index = joins.indexOf(m2);
        index -= 3;
        if (index < 0)
            index = 0;
        joinOrder.append("\n");
        if (joins.get(index).equals(m2))
            joinOrder.append("[").append(joins.get(index).getEffectiveName()).append("]()");
        else
            joinOrder.append(joins.get(index).getEffectiveName());
        for (int i = index + 1; i < index + 7; i++) {
            if (i >= joins.size())
                break;
            Member usr = joins.get(i);
            String name = usr.getEffectiveName();
            if (usr.equals(m2))
                name = "[" + name + "](https://corgibot.xyz/)";
            joinOrder.append(" > ").append(name);
        }



        channel.sendMessage(MessageUtils.getEmbed(sender, member.getGuild().getMember(user).getColor())
                .setThumbnail(user.getEffectiveAvatarUrl())
                .addField("Jméno", user.getName() + "#" + user.getDiscriminator() + " " + getDiscordRank(user), true)
                .addField("ID", user.getId(), true)
                .addField("Status", gameToString(m2.getGame()), true)
                .addField("Pravý jméno", m2.getEffectiveName(), true)
                .addField("Registrován", CorgiBot.getInstance().formatTime(LocalDateTime.from(user.getCreationTime())), true)
                .addField("Připojen", (member.getGuild().getMember(user) == null ? "Tento uživatel nebyl na tomto serveru!." : CorgiBot.getInstance().formatTime(LocalDateTime.from(member.getGuild().getMember(user).getJoinDate()))), true)
                .addField("Pořadí připojení", joinOrder.toString(), false)
                .addField("Online stav", convertStatus(m2.getOnlineStatus()) + " " + m2.getOnlineStatus().name().toLowerCase().replaceAll("_", " "), true)
                .addField("Bot", (user.isBot() ? "Ano" : "Nope"), true)
                .addField("Role", getRoles(m2, member.getGuild()), true).build()).queue();

    }

    @Override
    public String getCommand() {
        return "userinfo";
    }

    @Override
    public String getDescription() {
        return "Získání základních informací o uživateli.";
    }

    @Override
    public String getHelp() {
        return "%userinfo - Informace o sobě\n" +
                "%userinfo @nick - Informace o jiném uživateli";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"ui"};
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public Rank getRank() {
        return Rank.USER;
    }

    private String getRoles(Member user, Guild guid) {
        String roles = "";
        for (Role r : guid.getRoles()) {
            if (user.getRoles().contains(r)) {
                String role = r.getName();
                if (!role.equalsIgnoreCase("@everyone")) {
                    roles += ", `" + role + "`";
                }
            }
        }
        if (roles.equals("")) {
            roles = "Žádné";
        } else {
            roles = roles.substring(2);
        }
        return roles;
    }

    private String getDiscordRank(User user) {
        if (user.isBot()) {
            return Constants.EMOTE_BOT;
        } else if (user.getId().equals("177516608778928129")) { //Wake
            return Constants.EMOTE_PARTNER;
        } else if (user.getId().equals("151332840577957889")) { //Liturkey
            return Constants.EMOTE_HYPESQUAD;
        } else if (user.getId().equals("263736235539955713")) { //_yyySepii
            return Constants.EMOTE_NITRO;
        } else {
            return "";
        }
    }

    public static String convertStatus(OnlineStatus status) {
        switch (status) {
            case ONLINE:
                return "<:online:314899088510418945>";
            case IDLE:
                return "<:away:314900395082252290>";
            case DO_NOT_DISTURB:
                return "<:dnd:314900395556339732>";

            default:
                return "<:offline:314900395430379521>";
        }
    }

    public static String gameToString(Game g) {
        if (g == null) return "no game";

        String gameType = "Playing";

        switch (g.getType().getKey()) {
            case 1:
                gameType = "Streaming";
                break;
            case 2:
                gameType = "Listening to";
                break;
            case 3:
                gameType = "Watching";
        }

        String gameName = g.getName();
        return gameType + " " + gameName;
    }
}
