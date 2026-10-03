package com.example;

import com.google.inject.Provides;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


@Slf4j
@PluginDescriptor(name = "Charge Calculator")
public class ExamplePlugin extends Plugin {

    private static final Pattern TITLE
            = Pattern.compile("How many (charges|scales) (do|would) you (like|wish|want) to (add|apply|use)");
    private static final Pattern MAX
            = Pattern.compile("(\\d+(?:,\\d+)*)\\)");

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ExampleConfig config;

    private ChargeCalc setAmount;

    @Subscribe
    public void onVarClientIntChanged(VarClientIntChanged event) {
        if (event.getIndex() != VarClientID.MESLAYERMODE
                || client.getVarcIntValue(VarClientID.MESLAYERMODE) != 7) {
            return;
        }

        clientThread.invokeLater(()
                -> {
            Widget layer = client.getWidget(InterfaceID.Chatbox.MES_LAYER);
            Widget titleWidget = client.getWidget(InterfaceID.Chatbox.MES_TEXT);
            if (layer == null || titleWidget == null || titleWidget.getText() == null) {
                return;
            }

            String title = titleWidget.getText();
            if (!TITLE.matcher(title).find()) {
                return;
            }

            Matcher m = MAX.matcher(title);
            if (!m.find()) {
                return;
            }

            int amount = Integer.parseInt(m.group(1).replace(",", ""));
            setAmount = new ChargeCalc(layer, client);
            setAmount.showWidget(config, amount);
        });
    }

    @Provides
    ExampleConfig provideConfig(ConfigManager cm) {
        return cm.getConfig(ExampleConfig.class);
    }
}
