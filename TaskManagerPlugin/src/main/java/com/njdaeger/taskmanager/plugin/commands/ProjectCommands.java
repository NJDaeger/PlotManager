package com.njdaeger.taskmanager.plugin.commands;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import com.njdaeger.taskmanager.plugin.ITaskManagerPlugin;
import com.njdaeger.taskmanager.plugin.commands.flags.PageFlag;
import com.njdaeger.taskmanager.plugin.commands.help.HelpChatListItem;
import com.njdaeger.taskmanager.plugin.commands.wrappers.CommandBuilderWrapper;
import com.njdaeger.taskmanager.plugin.commands.wrappers.CommandContextWrapper;
import com.njdaeger.taskmanager.servicelibrary.ColorUtils;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.taskmanager.servicelibrary.services.IProjectService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;

import java.util.ArrayList;
import java.util.List;

import static com.njdaeger.taskmanager.dataaccess.Util.await;
import static com.njdaeger.taskmanager.plugin.Paginators.helpMenuPaginator;

public class ProjectCommands {

    private final ICacheService cacheService;
    private final List<HelpChatListItem> commandList;
    private final ChatPaginator<Project, CommandContextWrapper> projectListPaginator;

    public ProjectCommands(ITaskManagerPlugin plugin, ICacheService cacheService) {
        this.cacheService = cacheService;

        this.commandList = new ArrayList<>() {{
            add(new HelpChatListItem("list", "List all projects.",
                    new HelpChatListItem("-page <page>", "The page number to view."))
            );
            add(new HelpChatListItem("create", "Create a new project.",
                    new HelpChatListItem("<name>", "The name of the project."),
                    new HelpChatListItem("<prefix>", "The prefix of the project."),
                    new HelpChatListItem("<description...>", "The description of the project."))
            );
            add(new HelpChatListItem("delete", "Delete a project.",
                    new HelpChatListItem("<prefix>", "The prefix of the project."))
            );
            add(new HelpChatListItem("edit", "Edit a project.",
                    new HelpChatListItem("<prefix>", "The prefix of the project."),
                    new HelpChatListItem("<name|prefix|description>", "The field to edit."),
                    new HelpChatListItem("<new value...>", "The new value for the field."))
            );
            add(new HelpChatListItem("help", "View help for a specific command.",
                    new HelpChatListItem("-page <page>", "The page number to view."))
            );
        }};

        CommandBuilderWrapper.of("project")
                .executor(this::projectCommands)
                .completer(this::projectCommandsTabComplete)
                .description("Manage projects.")
                .usage("'/project help' for usage information.")
                .min(1)
                .flag(new PageFlag(ctx ->
                        ctx.argAt(0).equalsIgnoreCase("list") ||
                        ctx.argAt(0).equalsIgnoreCase("help")))
                .permissions("taskmanager.manage.projects")
                .build()
                .register(plugin);




        this.projectListPaginator = ChatPaginator.<Project, CommandContextWrapper>builder((proj, ctx) ->
            Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true)
                .appendRoot(proj.getProjectPrefix()).setColor(ColorUtils.HIGHLIGHT_TEXT)
                .appendRoot(" - ").setColor(ColorUtils.REGULAR_TEXT)
                .appendRoot(proj.getProjectName()).setColor(ColorUtils.HIGHLIGHT_TEXT).setItalic(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(proj.getProjectDescription()).setColor(ColorUtils.REGULAR_TEXT)))
            .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
            .addComponent(new PageNavigationComponent<>(
                    (ctx, res, pg) -> "/project " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                    (ctx, res, pg) -> "/project " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                    (ctx, res, pg) -> "/project " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                    (ctx, res, pg) -> "/project " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
            ), ComponentPosition.BOTTOM_CENTER)
            .addComponent(Text.of("Project List").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
            .setGrayColor(ColorUtils.REGULAR_TEXT)
            .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
            .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
            .build();
    }

    private void projectCommandsTabComplete(TabContext context) {
        context.completionAt(0, "help", "list", "create", "delete", "edit");
        if (context.argAt(0).equalsIgnoreCase("edit")) {
            context.completionAt(1, cacheService.getProjectCache().keySet().toArray(new String[0]));
            context.completionAt(2, "name", "prefix", "description");
        }
        if (context.argAt(0).equalsIgnoreCase("delete")) {
            context.completionAt(1, cacheService.getProjectCache().keySet().toArray(new String[0]));
        }
    }

    private void projectCommands(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var sub = context.argAtOrThrow(0, "Invalid subcommand.");
        switch (sub) {
            case "list" -> listProjects(transaction, context);
            case "create" -> createProject(transaction, context);
            case "delete" -> deleteProject(transaction, context);
            case "edit" -> editProject(transaction, context);
            case "help" -> helpProject(transaction, context);
            default -> context.error("Invalid subcommand.");
        }
    }

    private void listProjects(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var projectService = transaction.getService(IProjectService.class);
        int page = context.getFlag("page", 1);
        var projects = await(projectService.getProjects());
        if (projects.successful()) {
            var gen = projectListPaginator.generatePage(context, projects.getOrThrow(), page);
            if (gen != null) gen.sendTo(context.getSender());
            else context.error("No projects found.");
        } else context.error(projects.message());
    }

    private void createProject(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var projectService = transaction.getService(IProjectService.class);
        var name = context.argAtOrThrow(1, "No project name provided.");
        var prefix = context.argAtOrThrow(2, "No project prefix provided.");
        var desc = context.joinArgs(3);
        if (desc == null || desc.isBlank()) context.error("No project description provided.");
        var result = await(projectService.createProject(context.getUUID(), name, desc, prefix));
        if (result.successful())
            context.send(Text.of("[TaskManager] ")
                    .setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot("Project ")
                    .setColor(ColorUtils.REGULAR_TEXT)
                    .appendRoot(result.getOrThrow().getProjectName()).setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot(" has been created.").setColor(ColorUtils.REGULAR_TEXT));
        else context.error(result.message());
    }

    private void deleteProject(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var projectService = transaction.getService(IProjectService.class);
        var prefix = context.argAtOrThrow(1, "No project prefix provided.");
        var project = await(projectService.getProjectByPrefix(prefix));
        if (!project.successful()) context.error(project.message());

        var result = await(projectService.deleteProject(context.getUUID(), project.getOrThrow().getProjectId()));
        if (result.successful())
            context.send(Text.of("[TaskManager] ")
                    .setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot("Project ")
                    .setColor(ColorUtils.REGULAR_TEXT)
                    .appendRoot(result.getOrThrow().getProjectName()).setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot(" has been deleted.").setColor(ColorUtils.REGULAR_TEXT));
        else context.error(result.message());
    }

    private void editProject(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var projectService = transaction.getService(IProjectService.class);
        var prefix = context.argAtOrThrow(1, "No project prefix provided.");
        var project = await(projectService.getProjectByPrefix(prefix));
        if (!project.successful()) context.error(project.message());
        var field = context.argAtOrThrow(2, "No field to edit provided.").toLowerCase();
        String value = switch (field) {
            case "name" -> context.argAtOrThrow(3, "No new name provided.");
            case "prefix" -> context.argAtOrThrow(3, "No new prefix provided.");
            case "description" -> context.joinArgs(3);
            default -> null;
        };

        if (value == null || value.isBlank()) context.error("No new value provided.");

        var result = await(projectService.updateProject(context.getUUID(), project.getOrThrow().getProjectId(),
                field.equals("name") ? value : null,
                field.equals("prefix") ? value : null,
                field.equals("description") ? value : null));

        if (result.successful())
            context.send(Text.of("[TaskManager] ")
                    .setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot("Project ")
                    .setColor(ColorUtils.REGULAR_TEXT)
                    .appendRoot(result.getOrThrow().getProjectName()).setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot(" has been edited.").setColor(ColorUtils.REGULAR_TEXT));
        else context.error(result.message());
    }

    private void helpProject(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var page = context.getFlag("page", 1);
        var path = context.joinArgs(1).trim().split(" ");
        var currentList = commandList;
        if (!path[0].isBlank()) {
            for (String cmd : path) {
                if (currentList.isEmpty()) break;
                currentList = currentList.stream().filter(c -> c.getCommand().equalsIgnoreCase(cmd)).findFirst().map(HelpChatListItem::getSubCommands).orElse(List.of());
            }
        }

        var res = helpMenuPaginator.generatePage(context, currentList, page);
        if (res == null) context.error("No data to show.");
        else res.sendTo(context.getSender());
    }

}
