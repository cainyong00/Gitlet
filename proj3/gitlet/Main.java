package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Heeyong Chung
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        CommandClass command = new CommandClass();
        String message1 = "A Gitlet version-control"
                + "system already exists in the " + "current directory.";
        main2(command, message1, args);
    }
    public static void main2(CommandClass command,
                             String message1, String... args) {
        switch (args[0]) { case "init": if (Utils.join(".gitlet").exists()) {
                System.out.println(message1);
                System.exit(0);
                break;
            } else {
                command.init();
                break;
            }
        case "add": if (!Utils.join(args[1]).exists()) {
                System.out.println("File does not exist.");
            } else {
                command.add(args[1]);
            }
            break;
        case "commit": if (args.length != 2
                || args[1].isEmpty() || args[1].isBlank()) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            } else {
                command.commit(args[1]);
            }
            break;
        case "log": command.log();
            break;
        case "checkout": if (args.length == 3) {
                command.checkout(null, args[2], null);
            } else if (args.length == 4) {
                if (!(args[2].equals("--"))) {
                    System.out.println("Incorrect operands.");
                } else {
                    command.checkout(args[1], args[3], null);
                }
            } else if (args.length == 2) {
                command.checkout(null, null, args[1]);
            }
                break;
        case  "rm": command.rm(args[1]);
                break;
        case "find": command.find(args[1]);
                break;
        case "branch": command.branch(args[1]);
                break;
        case "status": if (!(Utils.join(".gitlet").exists())) {
                System.out.println(
                    "Not in an initialized Gitlet directory.");
                System.exit(0);
            }
                command.status();
                break;
        case "reset": command.reset(args[1]); break;
        case "merge": command.merge(args[1]); break;
        case "global-log": command.globalLog(); break;
        case "rm-branch": command.rmBranch(args[1]); break;
        default: System.out.println("No command with that name exists.");
        }
    }
}


