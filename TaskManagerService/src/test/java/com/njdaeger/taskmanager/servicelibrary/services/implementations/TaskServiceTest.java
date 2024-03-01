package com.njdaeger.taskmanager.servicelibrary.services.implementations;

import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.taskmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.taskmanager.servicelibrary.services.IProjectService;
import com.njdaeger.taskmanager.servicelibrary.services.ITaskTypeService;
import com.njdaeger.taskmanager.servicelibrary.services.IUserService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class TaskServiceTest {

    private static IServiceTransaction getServiceTransaction() {
        return Mockito.mock(IServiceTransaction.class);
//        when(mock.getUnitOfWork()).thenReturn()
    }

    @Test
    void getTasks() {
        var transaction = Mockito.mock(IServiceTransaction.class);
        var userService = Mockito.mock(IUserService.class);
        var projectService = Mockito.mock(IProjectService.class);
        var taskTypeService = Mockito.mock(ITaskTypeService.class);
        var attributeService = Mockito.mock(IAttributeService.class);
        var cacheService = Mockito.mock(ICacheService.class);
        var logger = Mockito.mock(IPluginLogger.class);

//        when()

        var service = new TaskService(transaction, userService, projectService, taskTypeService, attributeService, cacheService, logger);

    }

    @Test
    void getTaskById() {
    }

    @Test
    void getTasksOfProject() {
    }

    @Test
    void getTasksOfProjectAndType() {
    }
}