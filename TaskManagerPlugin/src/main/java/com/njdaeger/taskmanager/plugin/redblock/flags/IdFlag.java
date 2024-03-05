package com.njdaeger.taskmanager.plugin.redblock.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;
import com.njdaeger.taskmanager.plugin.redblock.IRedblockService;
import com.njdaeger.taskmanager.plugin.redblock.RedblockTask;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;

import java.util.function.Predicate;

public class IdFlag extends Flag<RedblockTask> {

    private final Predicate<RedblockTask> filter;
    private final ICacheService cacheService;
    private final IRedblockService redblockService;

    public IdFlag(IRedblockService redblockService, ICacheService cacheService, Predicate<RedblockTask> filter) {
        super(RedblockTask.class, "Redblock ID", "-id <id>", "id");
        this.redblockService = redblockService;
        this.cacheService = cacheService;
        this.filter = filter;
    }

    @Override
    public RedblockTask parse(CommandContext context, String argument) throws PDKCommandException {
        //the taskId is PJ-# where PJ is the project and # is the task key
        if (argument.length() < 3) return null;
        var split = argument.split("-");
        var projectKey = split[0];
        var taskKey = split[1];
        var project = cacheService.getProjectCache().get(projectKey);
        if (project == null) return null;

        return cacheService.getTaskCache().values().stream()
                .filter(t -> t.getProject().equals(project))
                .filter(t -> t.getTaskType().equals(redblockService.getRedblockTaskType()))
                .filter(t -> taskKey.equalsIgnoreCase(String.valueOf(t.getTaskKey())))
                .map(RedblockTask::new)
                .filter(filter)
                .findFirst().orElse(null);
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        var current = context.getCurrent();
        if (current.startsWith("-")) return;
        if (current.contains("-")) {
            var split = current.split("-");
            if (split.length == 2) {
                var project = cacheService.getProjectCache().get(split[0]);
                if (project == null) return;
                var tasks = cacheService.getTaskCache().values().stream()
                        .filter(t -> t.getProject().equals(project))
                        .filter(t -> t.getTaskType().equals(redblockService.getRedblockTaskType()))
                        .map(RedblockTask::new)
                        .filter(filter)
                        .map(t -> project.getProjectPrefix() + "-" + t.getTaskKey())
                        .toList();
                context.completion(tasks.toArray(new String[0]));
            }
        }
        else context.completion(cacheService.getProjectCache().keySet().toArray(new String[0]));
        context.completion();
    }
}
