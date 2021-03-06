 /*
  * Copyright 2012, 2013 Eric Myhre <http://exultant.us>
  *
  * This file is part of mdm <https://github.com/heavenlyhash/mdm/>.
  *
  * mdm is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.mdm;
 
 import java.io.*;
 import net.sourceforge.argparse4j.*;
 import net.sourceforge.argparse4j.impl.*;
 import net.sourceforge.argparse4j.inf.*;
 import org.eclipse.jgit.lib.*;
 import org.eclipse.jgit.storage.file.*;
 import us.exultant.ahs.util.*;
 import us.exultant.mdm.commands.*;
 import us.exultant.mdm.jgit.*;
 
 public class Mdm {
 	public static final String VERSION = "2.10.0";
 
 	public static void main(String[] args) {
 		// apply fixes for questionable jgit behavior
 		SystemReaderFilteringProxy.apply();
 
 		// go
 		MdmExitMessage answer = main(args, null);
 		answer.print(System.err);
 		answer.exit();
 	}
 
 	public static MdmExitMessage main(String[] args, Repository repo) {
 		ArgumentParser parser = ArgumentParsers.newArgumentParser("mdm").version(VERSION);
 
 		parser.addArgument("--version").action(Arguments.version());
 
 		Subparsers subparsers = parser.addSubparsers().dest("subcommand").title("subcommands");
 
 
 		Subparser parser_status = subparsers
 			.addParser("status")
 			.help("list dependencies managed by mdm, and their current status.");
 
 
 		Subparser parser_update = subparsers
 			.addParser("update")
 			.help("pull all dependencies up to date.  Run this after cloning a fresh repo, or pulling or checking out commits that change a dependency.");
 
 
 		Subparser parser_add = subparsers
 			.addParser("add")
 			.help("link a new dependency.");
 		parser_add
 			.addArgument("url")
 			.help("url pointing to an mdm-style releases repository.  Any kind of url that `git clone` understands will fly here too.");
 		parser_add
 			.addArgument("--lib")
 			.help("specifies the directory which shall contain the dependency module.  (default: 'lib')")
 			.setDefault("lib");
 		parser_add
 			.addArgument("--name")
 			.help("the name to give the new dependency module (if not specified, the url of the upstream will be parsed to determine the appropriate name, and if that fails, mdm will prompt you to choose one interactively).  Note that in the future, this dependency will be refered to by its path -- i.e., ${lib}/${name} .");
 		parser_add
 			.addArgument("--version")
 			.help("the version name of the dependency to set up.  If not provided, a mdm will obtain a list of available versions and prompt you to choose one interactively.");
 
 
 		Subparser parser_alter = subparsers
 			.addParser("alter")
 			.help("alter an existing dependency (i.e. switch to a new version).");
 		parser_alter
 			.addArgument("name")
 			.help("the name of the dependency module to operate on.");
 		parser_alter
 			.addArgument("--version")
 			.help("the version name of the dependency to set up.  If not provided, a mdm will obtain a list of available versions and prompt you to choose one interactively.");
 
 
 		Subparser parser_remove = subparsers
 			.addParser("remove")
 			.help("remove an existing dependency.");
 		parser_remove
 			.addArgument("name")
 			.help("the name of the dependency module to operate on.");
 
 
 		Subparser parser_release = subparsers
 			.addParser("release")
 			.help("generates a new release (adding commits to a releases repository; then, if this command was issued from inside a project's repository and the releases repository is a submodule in the canonical location, the new head commit of the master branch of the releases repo will be committed to the project repo along with a release tag).");
 		parser_release
 			.addArgument("--version")
 			.help("specifies the name/number of the release version to be made.  This will be used in naming tags/branches, and also mentioned in commit messages.")
 			.required(true);
 		parser_release
 			.addArgument("--files")
 			.help("specifies the artifact files to commit in the release.  If a directory, all non-hidden contents of the directory will be included.")
 			.required(true);
 		parser_release
 			.addArgument("--repo")
 			.help("specifies a local path to the releases repository for this project.  The new release commits will be added to this repository.  By default, it is assumed that the releases repo of this project is already a submodule in the ./releases/ directory, but using a path like '../projX-releases/' is also reasonable.  (default: 'releases')")
 			.setDefault("releases");
 
 
 		Subparser parser_releaseinit = subparsers
 			.addParser("release-init")
 			.help("set up a releases repository for a new project.  Future invocations of `mdm release` will generate commits into this repository.");
 		parser_releaseinit
 			.addArgument("--name")
 			.help("the name of the project.  If not provided, either the path of the parent project will be parsed to determine the appropriate name, or failing that, mdm will prompt you to choose one interactively.");
 		parser_releaseinit
 			.addArgument("--repo")
 			.help("specifies a path where the releases repository should be created.  If not provided, the default varies depending on if this command is issued from the root of an existing git repo: if so, it is assumed the releases repo should be a submodule in the \"./releases/\" directory; otherwise, the default behavior is to initialize the release repo in the current directory.");
 		parser_releaseinit
 			.addArgument("--use-defaults")
 			.action(Arguments.storeTrue())
 			.help("tell mdm to make its best guess for name based on local folders, and use relative paths for remote urls as necessary.  No interactive prompts for missing parameters.");
 		parser_releaseinit
 			.addArgument("--remote-url")
 			.help("assign a remote url where this repo will be accessible.  If creating this release repo as submodule of an existing project, this will be committed to the superproject's .gitmodules file, and so should be a publicly accessible url.");
 		parser_releaseinit
 			.addArgument("--remote-publish-url")
 			.help("assign a remote url you'll push this repo to when making releases.  This will not be committed to the project; just set in the the release repo's local config (therefore, if not creating this release repo as submodule of an existing project, specifying --remote-url at the same time as this option is useless).");
 
 
 		if (repo == null) try {
			repo = new FileRepositoryBuilder()
				.findGitDir()
				.build();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		Namespace parsedArgs = null;
 		try {
 			parsedArgs = parser.parseArgs(args);
 		} catch (ArgumentParserException e) {
 			parser.handleError(e);
 			System.exit(1);
 		}
 		try {
 			return getCommand(parsedArgs.getString("subcommand"), repo, parsedArgs).call();
 		} catch (Exception e) {
 			return new MdmExitMessage(":'(", "An unexpected error occurred!\n"+X.toString(e));
 		}
 	}
 
 	public static MdmCommand getCommand(String name, Repository repo, Namespace args) {
 		switch (name) {
 			case "status": return new MdmStatusCommand(repo, System.out);
 			case "update": return new MdmUpdateCommand(repo);
 			case "add":    return new MdmAddCommand(repo, args);
 			case "alter":  return new MdmAlterCommand(repo, args);
 			case "remove": return new MdmRemoveCommand(repo, args);
 			case "release": return new MdmReleaseCommand(repo, args);
 			case "release-init": return new MdmReleaseInitCommand(repo, args);
 			default: throw new MajorBug();
 		}
 	}
 }
