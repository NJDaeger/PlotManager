package com.njdaeger.plotmanager.plugin.commands;

import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.plotmanager.plugin.PermissionConstants;
import com.njdaeger.plotmanager.plugin.commands.wrappers.CommandContextWrapper;
import com.njdaeger.plotmanager.servicelibrary.ColorUtils;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IPlotService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

/**
 * Commands that help with the management of users on plots
 */
public class PlotUserCommands {

    // /plot claim -plot <plotId> [forUser]
    static void claimPlot(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        if (plot.hasAnyActiveUsers()) context.error("This plot is already claimed. Ask to be added by a member.");

        var userToAdd = context.getUUID();
        if (context.hasArgAt(1)) {
            if (!context.hasPermission(PermissionConstants.PLOT_CLAIM_COMMAND_OTHER)) context.noPermission();
            var username = context.argAt(1);
            var user = context.resolveUser(username);
            userToAdd = user.getUserId();
        }

        var res = await(plotService.addPlotUser(context.getUUID(), plot.getId(), userToAdd));

        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("You have claimed plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + plot.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());
    }

    // /plot add -plot <plotId> <user> -force
    static void addPlotMember(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        if (plot.getUser(context.getUUID()) == null) {
            if (!context.hasPermission(PermissionConstants.PLOT_ADD_MEMBER_COMMAND_FORCE)) context.noPermission();
            if (!context.hasFlag("force")) context.error("Failed to add. You are not a member of plot #" + plot.getId() + ".");
        }

        var username = context.argAt(1);
        var user = context.resolveUser(username);

        var res = await(plotService.addPlotUser(context.getUUID(), plot.getId(), user.getUserId()));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("User ").setColor(ColorUtils.REGULAR_TEXT).appendRoot(user.getLastKnownName()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" added to plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + plot.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());

    }

    // /plot remove -plot <plotId> <user> -force
    static void removePlotMember(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        if (plot.getUser(context.getUUID()) == null) {
            if (!context.hasPermission(PermissionConstants.PLOT_REMOVE_MEMBER_COMMAND_FORCE)) context.noPermission();
            if (!context.hasFlag("force")) context.error("Failed to remove. You are not a member of plot #" + plot.getId() + ".");
        }

        var username = context.argAt(1);
        var user = context.resolveUser(username);

        var res = await(plotService.removePlotUser(context.getUUID(), plot.getId(), user.getUserId()));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("User ").setColor(ColorUtils.REGULAR_TEXT).appendRoot(user.getLastKnownName()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" removed from plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + plot.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());
    }

    // /plot leave -plot <plotId> [forUser]
    static void leavePlot(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        var userToRemove = context.getUUID();
        if (context.hasArgAt(1)) {
            if (!context.hasPermission(PermissionConstants.PLOT_LEAVE_COMMAND_OTHER)) context.noPermission();
            var username = context.argAt(1);
            var user = context.resolveUser(username);
            userToRemove = user.getUserId();
        }

        if (plot.getUser(userToRemove) == null) context.error("Failed to leave. User is not a member of plot #" + plot.getId() + ".");

        var res = await(plotService.removePlotUser(context.getUUID(), plot.getId(), userToRemove));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("You have left plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + plot.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());
    }

    // use when a plot is in draft status.
    // moves the plot from Draft -> Plotted
    // /plot open -plot <plotId>
    static void openPlot(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        if (!context.hasPermission(PermissionConstants.USER_OPEN_PLOT_COMMAND)) context.noPermission();
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        var status = plot.getAttributeValueOrDefault("status", null);
        if (status == null) context.error("Failed to open. Plot #" + plot.getId() + " does not have a status attribute.");
        if (!status.equalsIgnoreCase("Draft")) context.error("Failed to open. Plot #" + plot.getId() + " is not in draft status.");
        plotService.setPlotAttribute(context.getUUID(), plot.getId(), "status", "Plotted");
    }

    // use when user has claimed the plot, or is in the plot member list, and the plot is in the status "Plotted"
    // moves the plot from Plotted -> Ongoing
    // /plot start -plot <plotId>
    static void startPlot(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        if (!context.hasPermission(PermissionConstants.USER_START_PLOT_COMMAND)) context.noPermission();
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        if (plot.getUser(context.getUUID()) == null) context.error("Failed to start. You are not a member of plot #" + plot.getId() + ".");

        var status = plot.getAttributeValueOrDefault("status", null);
        if (status == null) context.error("Failed to start. Plot #" + plot.getId() + " does not have a status attribute.");
        if (!status.equalsIgnoreCase("Plotted")) context.error("Failed to start. Plot #" + plot.getId() + " is not in plotted status.");
        plotService.setPlotAttribute(context.getUUID(), plot.getId(), "status", "Ongoing");
    }

    // use when user has claimed the plot, or is in the plot member list, and the plot is in the status "Ongoing"
    // moves the plot from Ongoing -> Review
    // /plot finish -plot <plotId>
    static void completePlot(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        if (!context.hasPermission(PermissionConstants.USER_FINISH_PLOT_COMMAND)) context.noPermission();
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        if (plot.getUser(context.getUUID()) == null) context.error("Failed to complete. You are not a member of plot #" + plot.getId() + ".");

        var status = plot.getAttributeValueOrDefault("status", null);
        if (status == null) context.error("Failed to complete. Plot #" + plot.getId() + " does not have a status attribute.");
        if (!status.equalsIgnoreCase("Ongoing")) context.error("Failed to complete. Plot #" + plot.getId() + " is not in ongoing status.");
        plotService.setPlotAttribute(context.getUUID(), plot.getId(), "status", "Review");
    }

    // use when user has claimed the plot, or is in the plot member list, and the plot is in the status "Review"
    // /plot review -plot <plotId> pass|fail
    // if pass, moves the plot from Review -> Complete
    // if fail, moves the plot from Review -> Ongoing
    static void reviewPlot(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        if (!context.hasPermission(PermissionConstants.USER_REVIEW_PLOT_COMMAND)) context.noPermission();
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        var status = plot.getAttributeValueOrDefault("status", null);
        if (status == null) context.error("Failed to review. Plot #" + plot.getId() + " does not have a status attribute.");
        if (!status.equalsIgnoreCase("Review")) context.error("Failed to review. Plot #" + plot.getId() + " is not in review status.");

        var review = context.argAt(1);
        if (!review.equalsIgnoreCase("pass") && !review.equalsIgnoreCase("fail")) context.error("Failed to review. Invalid review status. Must be pass or fail.");

        plotService.setPlotAttribute(context.getUUID(), plot.getId(), "status", review.equalsIgnoreCase("pass") ? "Complete" : "Ongoing");
    }

}
