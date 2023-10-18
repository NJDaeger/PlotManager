package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class ConfigValidationService implements Runnable {

    private final IServiceTransaction transaction;
    private final IConfigService configService;
    private final IPluginLogger logger;

    public ConfigValidationService(IServiceTransaction transaction, IConfigService configService, IPluginLogger logger) {
        this.transaction = transaction;
        this.configService = configService;
        this.logger = logger;
    }

    @Override
    public void run() {
        logger.info("Validating configuration...");
        validateCustomTypes();
        validateRequiredAttributes();
        validateRequiredAttributeDefaults();
        try {
            transaction.close();
        } catch (Exception e) {
            logger.exception(e);
        }
        logger.info("Configuration validated.");
    }

    private void validateCustomTypes() {
        logger.debug("Validating custom attribute types...");
        var typeNames = configService.getSection("attributes.types").getKeys(false);
        if (typeNames == null || typeNames.isEmpty()) return;
        for (var name : typeNames) {
            if (configService.getDefaultAttributeTypes().stream().anyMatch(t -> t.getName().equalsIgnoreCase(name))) {
                throw new RuntimeException("Attribute type " + name + " is already defined as a default type.");
            }
            if (typeNames.stream().filter(n -> n.equalsIgnoreCase(name)).count() > 1) {
                throw new RuntimeException("Attribute type " + name + " is already defined as a custom type.");
            }
        }
    }

    private void validateRequiredAttributes() {
        logger.debug("Validating required plot attributes...");
        var attributes = await(transaction.getService(IAttributeService.class).getAttributes()).getOrThrow();
        var required = configService.getStringList("plots.required-attributes");
        for (var requiredAttribute : required) {
            if (attributes.stream().noneMatch(a -> a.getName().equalsIgnoreCase(requiredAttribute))) {
                throw new RuntimeException("Required attribute " + requiredAttribute + " is not a valid attribute. Please create the attribute before defining it as a required attribute.");
            }
        }
    }

    private void validateRequiredAttributeDefaults() {
        logger.debug("Validating required plot attribute defaults...");
        var attributes = await(transaction.getService(IAttributeService.class).getAttributes()).getOrThrow();
        var requiredDefaults = configService.getStringList("plots.required-attributes-defaults");
        var required = configService.getStringList("plots.required-attributes");
        for (var attributeDefault : requiredDefaults) {
            var attributeName = attributeDefault.split(":")[0];
            var attributeDefValue = attributeDefault.split(":")[1];

            //does the attribute found here exist?
            var attribute = attributes.stream().filter(a -> a.getName().equalsIgnoreCase(attributeName)).findFirst();
            if (attribute.isEmpty()) {
                throw new RuntimeException("Required attribute default " + attributeDefault + " is not valid because the attribute " + attributeName + " is not a valid attribute.");
            }

            //ok it exists, but is it a required attribute?
            if (required.stream().noneMatch(a -> a.equalsIgnoreCase(attributeName))) {
                throw new RuntimeException("Required attribute default " + attributeDefault + " is not valid because the attribute " + attributeName + " is not a required attribute.");
            }

            //ok it exists and is required, but does the type exist?
            var attributeType = configService.getAttributeType(attribute.get().getType());
            if (attributeType == null) {
                throw new RuntimeException("Required attribute default " + attributeDefault + " is not valid because the attribute " + attributeName + " does not have a valid type.");
            }

            //ok it exists, is required, and has a valid type, but is the default value valid?
            if (attributeType.isValidValue(attributeDefValue)) {
                throw new RuntimeException("Required attribute default " + attributeDefault + " is not valid because the attribute " + attributeName + " does not have a valid default value.");
            }

        }
    }
}
