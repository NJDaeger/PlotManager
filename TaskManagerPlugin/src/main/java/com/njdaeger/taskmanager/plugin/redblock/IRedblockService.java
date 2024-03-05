package com.njdaeger.taskmanager.plugin.redblock;

import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.models.Task;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface IRedblockService {

    TaskType getRedblockTaskType();

    CompletableFuture<Result<RedblockTask>> createRedblock(Project project, UUID createdBy, Location location, UUID assignedTo, String minRank, String description);

    CompletableFuture<Result<RedblockTask>> deleteRedblock(Project project, UUID deletedBy, int taskKey);

    CompletableFuture<Result<RedblockTask>> updateRedblock(Project project, UUID modifiedBy, int taskKey, UUID assignedTo, String minRank, String description);

    CompletableFuture<Result<RedblockTask>> approveRedblock(Project project, UUID approvedBy, int taskKey);

    CompletableFuture<Result<RedblockTask>> denyRedblock(Project project, UUID deniedBy, int taskKey);

    CompletableFuture<Result<RedblockTask>> completeRedblock(Project project, UUID completedBy, int taskKey);

    RedblockTask getNearestRedblock(List<RedblockTask> redblockTasks, Location location, int searchRadius);

}
