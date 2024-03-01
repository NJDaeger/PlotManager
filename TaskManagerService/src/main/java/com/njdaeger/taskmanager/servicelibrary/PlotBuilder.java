package com.njdaeger.taskmanager.servicelibrary;

public class PlotBuilder {

//    private final Location location;
//    private List<PlotAttribute> attributes;
//    private PlotGroup plotGroup;
//    private List<User> users;
//    private Plot parent;
//
//    public PlotBuilder(Location location) {
//        this.location = location;
//
//        this.attributes = new ArrayList<>();
//        this.users = new ArrayList<>();
//    }
//
//    public PlotBuilder copyAttributes(Plot ref) {
//        ref.getAttributes().forEach(pa -> addAttribute(pa.getAttribute(), pa.getValue()));
//        return this;
//    }
//
//    public PlotBuilder addAttribute(String attribute, String value) {
//        attributes.removeIf(pa -> pa.getAttribute().equalsIgnoreCase(attribute));
//        if (value != null) attributes.add(new PlotAttribute(attribute, value));
//        return this;
//    }
//
//    public List<PlotAttribute> getAttributes() {
//        return attributes;
//    }
//
//    public PlotAttribute getAttribute(String attribute) {
//        return attributes.stream().filter(pa -> pa.getAttribute().equalsIgnoreCase(attribute)).findFirst().orElse(null);
//    }
//
//    public boolean hasAttribute(String attribute) {
//        return attributes.stream().anyMatch(pa -> pa.getAttribute().equalsIgnoreCase(attribute));
//    }
//
//    public PlotBuilder setPlotGroup(PlotGroup plotGroup) {
//        this.plotGroup = plotGroup;
//        return this;
//    }
//
//    public PlotGroup getPlotGroup() {
//        return plotGroup;
//    }
//
//    public PlotBuilder setParent(Plot parent) {
//        this.parent = parent;
//        return this;
//    }
//
//    public Plot getParent() {
//        return parent;
//    }
//
//    public Location getLocation() {
//        return location;
//    }
//
//    public List<PlotBuilderListItem> getBuilderListItems(List<String> requiredAttributes, IAttributeService attributeService, IConfigService configService) {
//        int locX = location.getBlockX();
//        int locY = location.getBlockY();
//        int locZ = location.getBlockZ();
//        String locWorld = location.getWorld().getName();
//
//        var listItems = new ArrayList<PlotBuilderListItem>();
//        listItems.add(new PlotBuilderListItem("Location",
//                Text.of(String.valueOf(locX)).setColor(ColorUtils.GRAYED_TEXT)
//                        .appendRoot(", ").setColor(ColorUtils.REGULAR_TEXT)
//                        .appendRoot(String.valueOf(locY)).setColor(ColorUtils.GRAYED_TEXT)
//                        .appendRoot(", ").setColor(ColorUtils.REGULAR_TEXT)
//                        .appendRoot(String.valueOf(locZ)).setColor(ColorUtils.GRAYED_TEXT)
//                        .appendRoot(", ").setColor(ColorUtils.REGULAR_TEXT)
//                        .appendRoot(locWorld).setColor(ColorUtils.GRAYED_TEXT),
//                false));
//        listItems.add(new PlotBuilderListItem("PlotGroup",
//                Text.of(plotGroup == null ? "none" : plotGroup.getName()).setColor(ColorUtils.HIGHLIGHT_TEXT)
//                        .appendRoot(" ")
//                        .appendRoot(plotGroup == null ? "[select]" : "[remove]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
//                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to " + (plotGroup == null ? "select a plot group" : "remove the plot group")).setColor(ColorUtils.REGULAR_TEXT))
//                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit -session group " + (plotGroup == null ? "select" : "remove"))),
//                false));
//        listItems.add(new PlotBuilderListItem("Parent",
//                Text.of(parent == null ? "none" : parent.getLocation().getBlockX() + ", " + parent.getLocation().getBlockY() + ", " + parent.getLocation().getBlockZ()).setColor(ColorUtils.HIGHLIGHT_TEXT)
//                        .appendRoot(" ")
//                        .appendRoot(parent == null ? "[select]" : "[remove]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
//                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to " + (parent == null ? "select a parent plot" : "remove the parent plot")).setColor(ColorUtils.REGULAR_TEXT))
//                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit -session parent " + (parent == null ? "select" : "remove"))),
//                false));
//
//        for (var req : requiredAttributes) {
//            Text.Section value;
//            if (hasAttribute(req)) {
//                var attribute = getAttribute(req);
//                value = Text.of(attribute.getValue()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" ")
//                        .appendRoot("[change]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
//                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to change the value.").setColor(ColorUtils.REGULAR_TEXT));
//
//                var type = await(attributeService.getAttribute(attribute.getAttribute())).getOrThrow().getType();
//
//                if (!configService.getAttributeType(type).getValues().isEmpty()) value.setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit -session attribute " + req + " select"));
//                else value.setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot edit -session attribute " + req + " set "));
//            }
//            else {
//                value = Text.of("[set]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
//                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to set the value.").setColor(ColorUtils.REGULAR_TEXT))
//                        .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot edit -session attribute " + req + " set "));
//            }
//            listItems.add(new PlotBuilderListItem(niceName(req), value, true));
//        }
//
//        for (var attr : attributes) {
//            if (requiredAttributes.contains(attr.getAttribute())) continue; //skip required attributes, we already did those
//            var value = Text.of(attr.getValue()).setColor(ColorUtils.HIGHLIGHT_TEXT)
//                    .appendRoot(" ")
//                    .appendRoot("[change]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
//                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to change the value.").setColor(ColorUtils.REGULAR_TEXT));
//
//            var type = await(attributeService.getAttribute(attr.getAttribute())).getOrThrow().getType();
//
//            if (!configService.getAttributeType(type).getValues().isEmpty()) value.setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit -session attribute " + attr + " select"));
//            else value.setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot edit -session attribute " + attr + " set "));
//            listItems.add(new PlotBuilderListItem(niceName(attr.getAttribute()), value, false));
//        }
//
//        var addAdditionalValue = Text.of("[Add Additional Attributes...]").setUnderlined(true).setItalic(true).setColor(ColorUtils.ACTION_TEXT)
//                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to add additional attributes.").setColor(ColorUtils.REGULAR_TEXT))
//                .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot edit -session attribute " ));
//        listItems.add(new PlotBuilderListItem("", addAdditionalValue, false));
//
//        return listItems;
//    }
//
//    private static String niceName(String name) {
//        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
//    }
//
//    public static class PlotBuilderListItem {
//
//        private final String itemName;
//        private final Text.Section value;
//        private final boolean isRequired;
//
//        public PlotBuilderListItem(String itemName, Text.Section value, boolean isRequired) {
//            this.itemName = itemName;
//            this.value = value;
//            this.isRequired = isRequired;
//        }
//
//        public String getItemName() {
//            return itemName;
//        }
//
//        public Text.Section getValue() {
//            return value;
//        }
//
//        public boolean isRequired() {
//            return isRequired;
//        }
//
//    }

}
