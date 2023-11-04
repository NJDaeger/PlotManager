package com.njdaeger.plotmanager.servicelibrary;

import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.models.PlotAttribute;
import com.njdaeger.plotmanager.servicelibrary.models.PlotGroup;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class PlotChatListItems {

    public static List<PlotChatListItem> getPlotUserList(Plot plot) {
        return plot.getUsers().stream().map((u) -> {
            if (u.isDeleted()) return new PlotChatListItem("", Text.of(u.getUser().getLastKnownName()).setColor(ColorUtils.GRAYED_TEXT)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("User is no longer a plot member.").setColor(ColorUtils.REGULAR_TEXT)), false);
            else return new PlotChatListItem("", Text.of(u.getUser().getLastKnownName()).setColor(ColorUtils.REGULAR_TEXT), false);
        }).toList();
    }

    public static List<PlotChatListItem> getListItemsForPlot(List<String> requiredAttributes, IAttributeService attributeService, IConfigService configService, Plot plot, boolean readonly) {
        var listItems = new ArrayList<PlotChatListItem>();
        listItems.add(getLocationLine(plot.getLocation()));
        //listItems.add(getPlotGroupLine(plot.getPlotGroup(), readonly, false));
        listItems.add(getParentLine(plot.getParent(), readonly, false));
        listItems.addAll(getPlotAttributeLines(attributeService, configService, requiredAttributes, plot.getAttributes(), readonly, false, plot.getId()));
        return listItems;
    }

    public static List<PlotChatListItem> getListItemsForBuilder(List<String> requiredAttributes, IAttributeService attributeService, IConfigService configService, PlotBuilder builder) {
        var listItems = new ArrayList<PlotChatListItem>();
        listItems.add(getLocationLine(builder.getLocation()));
        //listItems.add(getPlotGroupLine(builder.getPlotGroup(), false, true));
        listItems.add(getParentLine(builder.getParent(), false, true));
        listItems.addAll(getPlotAttributeLines(attributeService, configService, requiredAttributes, builder.getAttributes(), false, true, -1));
        var addAdditionalValue = Text.of("[Add Additional Attributes...]").setUnderlined(true).setItalic(true).setColor(ColorUtils.ACTION_TEXT)
                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to add additional attributes.").setColor(ColorUtils.REGULAR_TEXT))
                .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit -session attribute " ));
        listItems.add(new PlotChatListItem("", addAdditionalValue, false));

        return listItems;
    }

    private static PlotChatListItem getLocationLine(Location location) {
        int locX = location.getBlockX();
        int locY = location.getBlockY();
        int locZ = location.getBlockZ();
        String locWorld = location.getWorld().getName();

        return new PlotChatListItem("Location",
                Text.of(String.valueOf(locX)).setColor(ColorUtils.GRAYED_TEXT)
                        .appendRoot(", ").setColor(ColorUtils.REGULAR_TEXT)
                        .appendRoot(String.valueOf(locY)).setColor(ColorUtils.GRAYED_TEXT)
                        .appendRoot(", ").setColor(ColorUtils.REGULAR_TEXT)
                        .appendRoot(String.valueOf(locZ)).setColor(ColorUtils.GRAYED_TEXT)
                        .appendRoot(", ").setColor(ColorUtils.REGULAR_TEXT)
                        .appendRoot(locWorld).setColor(ColorUtils.GRAYED_TEXT),
                false);
    }

    private static PlotChatListItem getPlotGroupLine(PlotGroup group, boolean readonly, boolean builderSession) {
        var value = Text.of(group == null ? "none" : group.getName()).setColor(readonly ? ColorUtils.GRAYED_TEXT : ColorUtils.HIGHLIGHT_TEXT);
        if (!readonly) {
            value.appendRoot(" ");
            value.appendRoot(group == null ? "[select]" : "[remove]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to " + (group == null ? "select a plot group" : "remove the plot group")).setColor(ColorUtils.REGULAR_TEXT))
                    .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit group " + (group == null ? "select" : "remove") + (builderSession ? " -session" : "")));
        }
        return new PlotChatListItem("PlotGroup", value, false);
    }

    private static PlotChatListItem getParentLine(Plot parent, boolean readonly, boolean builderSession) {
        var value = Text.of(parent == null ? "none" : parent.getLocation().getBlockX() + ", " + parent.getLocation().getBlockY() + ", " + parent.getLocation().getBlockZ()).setColor(readonly ? ColorUtils.GRAYED_TEXT : ColorUtils.HIGHLIGHT_TEXT);
        if (!readonly) {
            value.appendRoot(" ");
            value.appendRoot(parent == null ? "[select]" : "[remove]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to " + (parent == null ? "select a parent plot" : "remove the parent plot")).setColor(ColorUtils.REGULAR_TEXT))
                    .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit parent " + (parent == null ? "select" : "remove") + (builderSession ? " -session" : "")));
        }
        return new PlotChatListItem("Parent", value, false);
    }

    private static List<PlotChatListItem> getPlotAttributeLines(IAttributeService attributeService, IConfigService configService, List<String> required, List<PlotAttribute> attributes, boolean readonly, boolean builderSession, int plotId) {
        var listItems = new ArrayList<PlotChatListItem>();
        for (var req : required) {
            var plotAttribute = attributes.stream().filter(a -> a.getAttribute().equalsIgnoreCase(req)).findFirst().orElse(null);
            var attribute = await(attributeService.getAttribute(req));
            var type = configService.getAttributeType(attribute.getOrThrow().getType());
            Text.Section value;

            if (readonly) {
                value = Text.of(plotAttribute == null ? "none" : plotAttribute.getValue()).setColor(ColorUtils.GRAYED_TEXT);
            } else {
                if (plotAttribute != null) {
                    value = Text.of(plotAttribute.getValue()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" ")
                            .appendRoot("[change]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to change the value.").setColor(ColorUtils.REGULAR_TEXT));

                    if (!type.getValues().isEmpty()) value.setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit " + (builderSession ? "-session" : "-plot " + plotId) + " attribute " + req + " select"));
                    else value.setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot edit " + (builderSession ? "-session" : "-plot " + plotId) + " attribute " + req + " set "));
                }
                else {
                    if (type.getValues().isEmpty()) {
                        value = Text.of("[set]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to set the value.").setColor(ColorUtils.REGULAR_TEXT))
                                .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot edit " + (builderSession ? "-session" : "-plot " + plotId) + " attribute " + req + " set "));
                    } else {
                        value = Text.of("[select]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to select the value.").setColor(ColorUtils.REGULAR_TEXT))
                                .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit " + (builderSession ? "-session" : "-plot " + plotId) + " attribute " + req + " select "));
                    }
                }
            }
            listItems.add(new PlotChatListItem(niceName(req), value, true));
        }

        for (var plotAttr : attributes) {
            if (required.contains(plotAttr.getAttribute())) continue;
            var attribute = await(attributeService.getAttribute(plotAttr.getAttribute()));
            var type = configService.getAttributeType(attribute.getOrThrow().getType());
            Text.Section value;

            if (readonly) {
                value = Text.of(plotAttr.getValue()).setColor(ColorUtils.HIGHLIGHT_TEXT);
            } else {
                if (type.getValues().isEmpty()) {
                    value = Text.of(plotAttr.getValue()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" ")
                            .appendRoot("[change]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to change the value.").setColor(ColorUtils.REGULAR_TEXT))
                            .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot edit " + (builderSession ? "-session" : "-plot " + plotId) + " attribute " + plotAttr.getAttribute() + " set "));
                } else {
                    value = Text.of(plotAttr.getValue()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" ")
                            .appendRoot("[change]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to change the value.").setColor(ColorUtils.REGULAR_TEXT))
                            .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit " + (builderSession ? "-session" : "-plot " + plotId) + " attribute " + plotAttr.getAttribute() + " select "));
                }
            }
            listItems.add(new PlotChatListItem(niceName(plotAttr.getAttribute()), value, false));
        }

        return listItems;
    }

    private static String niceName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public static class PlotChatListItem {

        private final String name;
        private final Text.Section value;
        private final boolean isRequired;

        public PlotChatListItem(String name, Text.Section value, boolean isRequired) {
            this.name = name;
            this.value = value;
            this.isRequired = isRequired;
        }

        public String getName() {
            return name;
        }

        public Text.Section getValue() {
            return value;
        }

        public boolean isRequired() {
            return isRequired;
        }

    }

}
