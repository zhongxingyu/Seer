 /**
  * 
  */
 package fhdw.ipscrum.shared.model.userRights;
 
import fhdw.ipscrum.shared.commands.admin.SystemCreateCommand;
 import fhdw.ipscrum.shared.commands.project.ProjectAddSystemCommand;
 import fhdw.ipscrum.shared.commands.project.ProjectChangeNameCommand;
 import fhdw.ipscrum.shared.commands.project.ProjectCreateCommand;
 import fhdw.ipscrum.shared.commands.project.ProjectDeleteCommand;
 import fhdw.ipscrum.shared.commands.project.ProjectRemoveSystemCommand;
 import fhdw.ipscrum.shared.commands.project.ReleaseAddSprintCommand;
 import fhdw.ipscrum.shared.commands.project.ReleaseCreateCommand;
 import fhdw.ipscrum.shared.commands.project.ReleaseDeleteCommand;
 import fhdw.ipscrum.shared.commands.project.ReleaseRemoveSprintCommand;
 import fhdw.ipscrum.shared.commands.project.SprintChangeCommand;
 import fhdw.ipscrum.shared.commands.project.SprintCreateCommand;
 import fhdw.ipscrum.shared.model.Model;
 
 /**
  * handler for project right.
  */
 class ProjectRightHandler extends RightHandler {
 
 	/**
 	 * creates a new project rigth handler.
 	 * 
 	 * @param myRight
 	 *            concrete right.
 	 * @param model
 	 *            the model.
 	 */
 	ProjectRightHandler(final ProjectRight myRight, final Model model) {
 		super(myRight, model);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#
 	 * handleProjectAddSystemCommand
 	 * (fhdw.ipscrum.shared.commands.project.ProjectAddSystemCommand)
 	 */
 	@Override
 	public void handleProjectAddSystemCommand(
 			final ProjectAddSystemCommand projectAddSystemCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#
 	 * handleProjectChangeNameCommand
 	 * (fhdw.ipscrum.shared.commands.project.ProjectChangeNameCommand)
 	 */
 	@Override
 	public void handleProjectChangeNameCommand(
 			final ProjectChangeNameCommand projectChangeNameCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#handleProjectCreateCommand
 	 * (fhdw.ipscrum.shared.commands.project.ProjectCreateCommand)
 	 */
 	@Override
 	public void handleProjectCreateCommand(
 			final ProjectCreateCommand projectCreateCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#handleProjectDeleteCommand
 	 * (fhdw.ipscrum.shared.commands.project.ProjectDeleteCommand)
 	 */
 	@Override
 	public void handleProjectDeleteCommand(
 			final ProjectDeleteCommand projectDeleteCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#
 	 * handleProjectRemoveSystemCommand
 	 * (fhdw.ipscrum.shared.commands.project.ProjectRemoveSystemCommand)
 	 */
 	@Override
 	public void handleProjectRemoveSystemCommand(
 			final ProjectRemoveSystemCommand projectRemoveSystemCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#
 	 * handleReleaseAddSprintCommand
 	 * (fhdw.ipscrum.shared.commands.project.ReleaseAddSprintCommand)
 	 */
 	@Override
 	public void handleReleaseAddSprintCommand(
 			final ReleaseAddSprintCommand releaseAddSprintCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#handleReleaseCreateCommand
 	 * (fhdw.ipscrum.shared.commands.project.ReleaseCreateCommand)
 	 */
 	@Override
 	public void handleReleaseCreateCommand(
 			final ReleaseCreateCommand releaseCreateCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#handleReleaseDeleteCommand
 	 * (fhdw.ipscrum.shared.commands.project.ReleaseDeleteCommand)
 	 */
 	@Override
 	public void handleReleaseDeleteCommand(
 			final ReleaseDeleteCommand releaseDeleteCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#
 	 * handleReleaseRemoveSprintCommand
 	 * (fhdw.ipscrum.shared.commands.project.ReleaseRemoveSprintCommand)
 	 */
 	@Override
 	public void handleReleaseRemoveSprintCommand(
 			final ReleaseRemoveSprintCommand releaseRemoveSprintCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#handleSprintChangeCommand
 	 * (fhdw.ipscrum.shared.commands.project.SprintChangeCommand)
 	 */
 	@Override
 	public void
 			handleSprintChangeCommand(final SprintChangeCommand sprintChangeCommand) {
 		this.allowed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fhdw.ipscrum.shared.commands.visitor.CommandStandardVisitor#handleSprintCreateCommand
 	 * (fhdw.ipscrum.shared.commands.project.SprintCreateCommand)
 	 */
 	@Override
 	public void
 			handleSprintCreateCommand(final SprintCreateCommand sprintCreateCommand) {
 		this.allowed();
 	}
 
	@Override
	public void
			handleSystemCreateCommand(final SystemCreateCommand systemCreateCommand) {
		this.allowed();
	}

 }
