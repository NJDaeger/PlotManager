package com.njdaeger.plotmanager.servicelibrary.services;

import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.PlotAttribute;
import com.njdaeger.plotmanager.servicelibrary.models.PlotGroup;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.transactional.ITransactionalService;
import com.njdaeger.plotmanager.servicelibrary.PlotBuilder;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface IPlotService extends ITransactionalService {

    /**
     * Creates a draft plot at the given location. no attributes are assigned to this plot, no people can be assigned to this plot
     *
     * @return The plot that was created
     */
    CompletableFuture<Result<PlotBuilder>> createPlotBuilder(UUID creator, Location location);

    /**
     * Gets the plot builder for the given creator
     * @param creator The creator of the plot
     * @return The plot builder that was found, or null if the plot builder was not found.
     */
    CompletableFuture<Result<PlotBuilder>> getPlotBuilder(UUID creator);

    /**
     * Cancels the plot builder and deletes the plot that was being created.
     * @param creator The creator of the plot
     * @return The plot builder that was cancelled, or null if the plot builder was not found.
     */
    CompletableFuture<Result<PlotBuilder>> cancelPlotBuilder(UUID creator);

    /**
     * Finishes the plot builder and creates a plot at the location the builder was started at.
     * @param creator The creator of the plot
     * @return The plot that was created, or null if the plot creation was unsuccessful.
     */
    CompletableFuture<Result<Plot>> finishPlotBuilder(UUID creator);

    /**
     * Gets all plots
     * @return A result with a list of plots if successful, or a result with an empty list if the plot retrieval was unsuccessful.
     */
    CompletableFuture<Result<List<Plot>>> getPlots();

    /**
     * Gets all plots that match the given predicate
     * @param predicate The predicate to match
     * @return A result with a list of plots if successful, or a result with an empty list if the plot retrieval was unsuccessful.
     */
    default CompletableFuture<Result<List<Plot>>> getPlots(Predicate<Plot> predicate) {
        if (predicate == null) return CompletableFuture.completedFuture(Result.bad("Predicate cannot be null."));
        return getPlots().thenApply(result -> {
            if (result.successful()) {
                return Result.good(result.getOrThrow().stream().filter(predicate).toList());
            }
            return result;
        });
    }

    /**
     * Gets a plot by id
     * @param plotId The id of the plot
     * @return A result with the plot if successful, or a result with null if the plot retrieval was unsuccessful.
     */
    CompletableFuture<Result<Plot>> getPlot(int plotId);

    /**
     * Gets a plot by location
     * @param location The location of the plot
     * @return A result with the plot if successful, or a result with null if the plot retrieval was unsuccessful.
     */
    default CompletableFuture<Result<Plot>> getNearestPlot(Location location) {
        if (location == null) return CompletableFuture.completedFuture(Result.bad("Location cannot be null."));
        return getPlots(plot -> plot.getLocation().getWorld().getUID().equals(location.getWorld().getUID())).thenApply(r -> {
            if (r.successful()) {
                var res = r.getOrThrow().stream().min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(location)));
                return res.map(Result::good).orElseGet(() -> Result.bad("No plots found."));
            }
            return Result.bad(r.message());
        });
    }

    /**
     * Sets the location of a plot
     * @param modifiedBy The modifier of the plot
     * @param plotId The id of the plot to update
     * @param newLocation The new location of the plot
     * @return A result with the updated plot if successful, or a result with null if the plot update was unsuccessful.
     */
    CompletableFuture<Result<Plot>> setPlotLocation(UUID modifiedBy, int plotId, Location newLocation);

    /**
     * Sets the parent of a plot
     * @param modifiedBy The modifier of the plot
     * @param plotId The id of the plot to update
     * @param parentId The id of the parent plot
     * @return A result with the updated plot if successful, or a result with null if the plot update was unsuccessful.
     */
    CompletableFuture<Result<Plot>> setPlotParent(UUID modifiedBy, int plotId, int parentId);

    /**
     * Removes the parent of a plot
     * @param modifiedBy The modifier of the plot
     * @param plotId The id of the plot to update
     * @return A result with the updated plot if successful, or a result with null if the plot update was unsuccessful.
     */
    CompletableFuture<Result<Plot>> removePlotParent(UUID modifiedBy, int plotId);

    /**
     * Gets the parent of a plot
     * @param plotId The id of the plot
     * @return A result with the parent plot if successful, or a result with null if the parent plot retrieval was unsuccessful.
     */
    default CompletableFuture<Result<Plot>> getPlotParent(int plotId) {
        return getPlot(plotId).thenApply(r -> {
            if (r.successful()) {
                var plot = r.getOrThrow();
                var parent = plot.getParent();
                if (parent == null) return Result.bad("Plot does not have a parent.");
                return Result.good(parent);
            }
            return Result.bad(r.message());
        });
    }

    /**
     * Sets the plot group of a plot
     *
     * @param modifiedBy The modifier of the plot
     * @param plotId The id of the plot to update
     * @param group The id of the plot group
     * @return A result with the updated plot if successful, or a result with null if the plot update was unsuccessful.
     */
    CompletableFuture<Result<Plot>> setPlotGroup(UUID modifiedBy, int plotId, String group);

    /**
     * Removes the plot group of a plot
     * @param modifiedBy The modifier of the plot
     * @param plotId The id of the plot to update
     * @return A result with the updated plot if successful, or a result with null if the plot update was unsuccessful.
     */
    CompletableFuture<Result<Plot>> removePlotGroup(UUID modifiedBy, int plotId);

    /**
     * Gets the plot group of a plot
     * @param plotId The id of the plot
     * @return A result with the plot group if successful, or a result with null if the plot group retrieval was unsuccessful.
     */
    default CompletableFuture<Result<PlotGroup>> getPlotGroup(int plotId) {
        return getPlot(plotId).thenApply(r -> {
            if (r.successful()) {
                var plot = r.getOrThrow();
                var group = plot.getPlotGroup();
                if (group == null) return Result.bad("Plot does not have a group.");
                return Result.good(group);
            }
            return Result.bad(r.message());
        });
    }

    /**
     * Sets the attribute of a plot
     * @param modifiedBy The modifier of the plot
     * @param plotId The id of the plot to update
     * @param attributeName The name of the attribute
     * @param value The value of the attribute
     * @return A result with the updated plot if successful, or a result with null if the plot update was unsuccessful.
     */
    CompletableFuture<Result<Plot>> setPlotAttribute(UUID modifiedBy, int plotId, String attributeName, String value);

    /**
     * Removes the attribute of a plot
     * @param modifiedBy The modifier of the plot
     * @param plotId The id of the plot to update
     * @param attributeName The name of the attribute
     * @return A result with the updated plot if successful, or a result with null if the plot update was unsuccessful.
     */
    CompletableFuture<Result<Plot>> removePlotAttribute(UUID modifiedBy, int plotId, String attributeName);

    /**
     * Gets all attributes for a plot
     * @param plotId The id of the plot
     * @return A result with a list of attributes if successful, or a result with an empty list if the attribute retrieval was unsuccessful.
     */
    default CompletableFuture<Result<List<PlotAttribute>>> getPlotAttributes(int plotId) {
        return getPlot(plotId).thenApply(r -> {
            if (r.successful()) {
                var plot = r.getOrThrow();
                return Result.good(plot.getAttributes());
            }
            return Result.bad(r.message());
        });
    }

    /**
     * Gets an attribute for a plot
     * @param plotId The id of the plot
     * @param attributeName The name of the attribute
     * @return A result with the attribute if successful, or a result with null if the attribute retrieval was unsuccessful.
     */
    default CompletableFuture<Result<PlotAttribute>> getPlotAttribute(int plotId, String attributeName) {
        if (attributeName == null || attributeName.isBlank()) return CompletableFuture.completedFuture(Result.bad("Attribute name cannot be null or blank."));
        return getPlot(plotId).thenApply(r -> {
            if (r.successful()) {
                var plot = r.getOrThrow();
                var attr = plot.getAttribute(attributeName);
                if (attr == null) return Result.bad("Plot does not have an attribute named " + attributeName + ".");
                return Result.good(attr);
            }
            return Result.bad(r.message());
        });
    }

    /**
     * Adds a user to a plot
     * @param modifiedBy The modifier of the plot
     * @param plotId The id of the plot to update
     * @param userId The id of the user to add
     * @return A result with the updated plot if successful, or a result with null if the plot update was unsuccessful.
     */
    CompletableFuture<Result<Plot>> addPlotUser(UUID modifiedBy, int plotId, UUID userId);

    /**
     * Removes a user from a plot
     * @param modifiedBy The modifier of the plot
     * @param plotId The id of the plot to update
     * @param userId The id of the user to remove
     * @return A result with the updated plot if successful, or a result with null if the plot update was unsuccessful.
     */
    CompletableFuture<Result<Plot>> removePlotUser(UUID modifiedBy, int plotId, UUID userId);

    /**
     * Gets all users for a plot
     * @param plotId The id of the plot
     * @return A result with a list of users if successful, or a result with an empty list if the user retrieval was unsuccessful.
     */
    default CompletableFuture<Result<List<User>>> getPlotUsers(int plotId) {
        return getPlot(plotId).thenApply(r -> {
            if (r.successful()) {
                var plot = r.getOrThrow();
                return Result.good(plot.getUsers());
            }
            return Result.bad(r.message());
        });
    }

    /**
     * Deletes a plot
     * @param deletedBy The deleter of the plot
     * @param plotId The id of the plot to delete
     * @return A result with the deleted plot if successful, or a result with null if the plot deletion was unsuccessful.
     */
    CompletableFuture<Result<Plot>> deletePlot(UUID deletedBy, int plotId);

}
