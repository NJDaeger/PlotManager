package com.njdaeger.taskmanager.plugin;

public final class PermissionConstants {

    //region plot command permissions
    public static final String PLOT_INFO_COMMAND = "plotmanager.command.plot.info";
    public static final String PLOT_GOTO_COMMAND = "plotmanager.command.plot.goto";
    public static final String PLOT_LIST_COMMAND = "plotmanager.command.plot.list";
    public static final String PLOT_CREATE_COMMAND = "plotmanager.command.plot.create";
    public static final String PLOT_CLAIM_COMMAND = "plotmanager.command.plot.claims.claim";
    public static final String PLOT_CLAIM_COMMAND_OTHER = "plotmanager.command.plot.claims.claim.other";
    public static final String PLOT_LEAVE_COMMAND = "plotmanager.command.plot.claims.leave";
    public static final String PLOT_LEAVE_COMMAND_OTHER = "plotmanager.command.plot.claims.leave.other";
    public static final String PLOT_ADD_MEMBER_COMMAND = "plotmanager.command.plot.users.addmember";
    public static final String PLOT_ADD_MEMBER_COMMAND_FORCE = "plotmanager.command.plot.users.addmember.force";
    public static final String PLOT_REMOVE_MEMBER_COMMAND = "plotmanager.command.plot.users.removemember";
    public static final String PLOT_REMOVE_MEMBER_COMMAND_FORCE = "plotmanager.command.plot.users.removemember.force";
    public static final String PLOT_MODIFY_ATTRIBUTE_COMMAND = "plotmanager.command.plot.attributes.modify";
    public static final String PLOT_DELETE_ATTRIBUTE_COMMAND = "plotmanager.command.plot.attributes.remove";
    public static final String PLOT_MODIFY_PARENT_COMMAND = "plotmanager.command.plot.parent.modify";
    public static final String PLOT_DELETE_PARENT_COMMAND = "plotmanager.command.plot.parent.remove";
    public static final String PLOT_MODIFY_GROUP_COMMAND = "plotmanager.command.plot.group.modify";
    public static final String PLOT_DELETE_GROUP_COMMAND = "plotmanager.command.plot.group.remove";
    public static final String PLOT_DELETE_COMMAND = "plotmanager.command.plot.delete";
    //endregion

    //region action command permissions

    public static final String USER_START_PLOT_COMMAND = "plotmanager.command.action.plot.start";
    public static final String USER_FINISH_PLOT_COMMAND = "plotmanager.command.action.plot.finish";
    public static final String USER_OPEN_PLOT_COMMAND = "plotmanager.command.action.plot.open";
    public static final String USER_REVIEW_PLOT_COMMAND = "plotmanager.command.action.plot.review";

    //endregion

}
