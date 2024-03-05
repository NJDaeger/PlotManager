package com.njdaeger.taskmanager.plugin.redblock;

import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.taskmanager.plugin.ITaskManagerPlugin;
import com.njdaeger.taskmanager.plugin.commands.wrappers.CommandContextWrapper;
import com.njdaeger.taskmanager.servicelibrary.ColorUtils;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.models.Task;
import com.njdaeger.taskmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.taskmanager.servicelibrary.services.ITaskService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.UUID;

import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class RedblockCommands {

    private final ITaskManagerPlugin plugin;
    private final IRedblockService rbService;

    public RedblockCommands(ITaskManagerPlugin plugin, IRedblockService rbService) {
        this.plugin = plugin;
        this.rbService = rbService;
    }

    private void create(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        UUID assignedTo = context.getFlag("assign");
        String minRank = context.getFlag("rank");
        Project project = context.getFlag("project");

        if (project == null)
            context.error(context.hasFlag("project") ? "No project found with the provided name." : "No default project defined or found for redblock creation.");

        rbService.createRedblock(project, context.getUUID(), context.getLocation().getBlock().getLocation(), assignedTo, minRank, context.joinArgs()).thenApply(result -> {
            if (!result.successful()) {
                try {
                    context.error(result.message());
                } catch (PDKCommandException ignored) {
                }
                return null;
            }
            context.send(ColorUtils.HIGHLIGHT_TEXT + "[Redblock] " + ColorUtils.REGULAR_TEXT + "Redblock created with id " + ColorUtils.HIGHLIGHT_TEXT + project.getProjectPrefix() + "-" + rbService.getRedblockTaskType().getTaskTypePrefix() + "-" + result.getOrThrow().getTaskKey());
            return null;
        });
    }

    private void delete(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        Project project = context.getFlag("project");
        if (project == null)
            context.error(context.hasFlag("project") ? "No project found with the provided name." : "No default project defined or found for redblock deletion.");

        var taskService = transaction.getService(ITaskService.class);
        var tasks = await(taskService.getTasksOfProjectAndType(project, rbService.getRedblockTaskType())).get();

        if (tasks == null || tasks.isEmpty()) {
            context.error("No redblocks found in the provided project.");
            return;
        }
        var filteredRedblocks = tasks.stream().map(RedblockTask::new).filter(rb -> rb.isPending() || rb.isIncomplete()).toList();

        RedblockTask task = context.hasFlag("id") ? context.getFlag("id") : rbService.getNearestRedblock(filteredRedblocks, context.getLocation().getBlock().getLocation(), 10);
        if (task == null) {
            context.error(ChatColor.RED + "No pending or incomplete redblock found in a 10 block radius with the given ID.");
            return;
        }
        rbService.deleteRedblock(project, context.getUUID(), task.getTaskKey()).thenApply(result -> {
            if (!result.successful()) {
                try {
                    context.error(result.message());
                } catch (PDKCommandException ignored) {
                }
                return null;
            }
            context.send(ColorUtils.HIGHLIGHT_TEXT + "[Redblock] " + ColorUtils.REGULAR_TEXT + "Redblock deleted with id " + ColorUtils.HIGHLIGHT_TEXT + project.getProjectPrefix() + "-" + rbService.getRedblockTaskType().getTaskTypePrefix() + "-" + task.getTaskKey());
            return null;
        });
    }

    private void edit(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        Project project = context.getFlag("project");
        if (project == null)
            context.error(context.hasFlag("project") ? "No project found with the provided name." : "No default project defined or found for redblock editing.");


    }

}
