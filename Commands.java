import MicrowavedBunny.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Timer;
import java.util.TimerTask;


public class Commands extends ListenerAdapter {

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event)
    {
        JDA jda = event.getJDA();
        final Guild guild = event.getGuild();
        final AudioManager audioManager = event.getGuild().getAudioManager();
        final VoiceChannel channel = event.getChannelJoined();
        final TextChannel textChannel = guild.getDefaultChannel();
        audioManager.setSelfDeafened(true);
        if (!audioManager.isConnected()) {
            audioManager.openAudioConnection(channel);
        }

        PlayerManager.getInstance()
                .loadAndPlay(textChannel, "https://www.youtube.com/watch?v=Ks5cWd5STT0&ab_channel=Jovstarki");

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                audioManager.closeAudioConnection();
                timer.cancel();
            }
        }, 3500);

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        JDA jda = event.getJDA();

        //Event specific information
        User author = event.getAuthor();                //The user that sent the message
        Message message = event.getMessage();           //The message that was received.
        MessageChannel channel = event.getChannel();    //This is the MessageChannel that the message was sent to.

        String msg = message.getContentDisplay();            //converts message to string for console output and comparisons


        if (event.isFromType(ChannelType.TEXT))         //If this message was sent to a Guild TextChannel
        {
            Guild guild = event.getGuild();             //The Guild that this message was sent in.
            TextChannel textChannel = event.getTextChannel(); //The TextChannel that this message was sent to.
            Member member = event.getMember();          //This Member that sent the message.

            String name;
            if (message.isWebhookMessage())
            {
                name = author.getName();                //If this is a Webhook message, then there is no Member associated
            }                                           // with the User, thus we default to the author for name.
            else
            {
                name = member.getEffectiveName();       //This will either use the Member's nickname if they have one,
            }                                           // otherwise it will default to their username. (User#getName())

            //print in console the Guild,channel,user,and message
            System.out.printf("(%s)[%s]<%s>: %s\n", guild.getName(), textChannel.getName(), name, msg);
        }
        else if (event.isFromType(ChannelType.PRIVATE)) //If this message was sent to a PrivateChannel
        {
            PrivateChannel privateChannel = event.getPrivateChannel();

            //print in console the channel,user,and message
            System.out.printf("[PRIV]<%s>: %s\n", author.getName(), msg);
        }

        //start of commands
        if (msg.equals(">info")) {
            //create embed
            EmbedBuilder info = new EmbedBuilder();
            info.setTitle("Disboard");
            info.setDescription("Completely useless bot for playing sound bytes in voice chat!");
            info.setColor(0xf45642);
            //info.setAuthor("Jacob Davidson", "https://discordapp.com/users/509285002278862879", "https://cdn.discordapp.com/avatars/509285002278862879/bb8983c79329d18835e3ded893136f18.png");
            //info.setFooter("Created by @MicrowavedBunny#1368", event.getMember().getUser().getAvatarUrl());

            //type and send
            channel.sendTyping().queue();
            channel.sendMessage("Info").setEmbeds(info.build()).queue();
            info.clear();
        }
        else if (msg.equals(">join")){
            final Member member = event.getMember();
            final Member self = event.getGuild().getMember(event.getJDA().getSelfUser());

            final GuildVoiceState selfVoiceState = self.getVoiceState();

            if (selfVoiceState.inVoiceChannel()) {
                channel.sendMessage("I'm already in a voice channel").queue();
                return;
            }

            final GuildVoiceState memberVoiceState = member.getVoiceState();

            if (!memberVoiceState.inVoiceChannel()) {
                channel.sendMessage("You need to be in a voice channel for this command to work").queue();
                return;
            }

            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = memberVoiceState.getChannel();

            audioManager.setSelfDeafened(true);
            audioManager.openAudioConnection(memberChannel);
            channel.sendMessageFormat("Connecting to `\uD83D\uDD0A %s`", memberChannel.getName()).queue();

        }
        else if (msg.equals(">leave")){
           final Member member = event.getMember();
           final Member self = event.getGuild().getMember(event.getJDA().getSelfUser());
           final GuildVoiceState selfVoiceState = self.getVoiceState();
           final GuildVoiceState memberVoiceState = member.getVoiceState();
           final AudioManager audioManager = event.getGuild().getAudioManager();
           final VoiceChannel memberChannel = memberVoiceState.getChannel();
           audioManager.closeAudioConnection();

            if (!selfVoiceState.inVoiceChannel()) {
                channel.sendMessage("I'm not in a voice channel").queue();
                return;
            }else {
                channel.sendMessageFormat("Leaving `\uD83D\uDD0A %s`", memberChannel.getName()).queue();
            }
        }
        else if (msg.equals(">play")) {

            final TextChannel textChannel = event.getTextChannel();
            final Member self = event.getGuild().getMember(event.getJDA().getSelfUser());
            final GuildVoiceState selfVoiceState = self.getVoiceState();

            if (!selfVoiceState.inVoiceChannel()) {
                channel.sendMessage("I need to be in a voice channel for this to work").queue();
                return;
            }

            final Member member = event.getMember();
            final GuildVoiceState memberVoiceState = member.getVoiceState();

            if (!memberVoiceState.inVoiceChannel()) {
                channel.sendMessage("You need to be in a voice channel for this command to work").queue();
                return;
            }

            if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
                channel.sendMessage("You need to be in the same voice channel as me for this to work").queue();
                return;
            }
            PlayerManager.getInstance()
                    .loadAndPlay(textChannel, "https://www.youtube.com/watch?v=Ks5cWd5STT0&ab_channel=Jovstarki");
        }

        /*else if (msg.equals(">roles")){
            Role yellow = event.getGuild().getRoleById("800289850934034432");
            event.getGuild().addRoleToMember(event.getMember().getUser().getId(), yellow).queue();
            Role red = event.getGuild().getRoleById("613277519495299073");
            event.getGuild().addRoleToMember(event.getMember().getUser().getId(), red).queue();
        }*/
    }
}