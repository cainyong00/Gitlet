package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

public class CommandClass {
    /** CWD. */
    public static final File CWD = new File(".");
    /** gitlet folder. */
    public static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");
    /** commit directory. */
    public static final File COMMIT_DIR = Utils.join(GITLET_FOLDER, "commits");
    /** staging area. */
    public static final File STAGING_AREA
            = Utils.join(GITLET_FOLDER, "staging area");
    /** add folder. */
    public static final File ADD = Utils.join(STAGING_AREA, "add");
    /** remove folder. */
    public static final File REMOVE = Utils.join(STAGING_AREA, "remove");
    /** head folder. */
    public static final File HEAD = Utils.join(GITLET_FOLDER, "head.txt");
    /** branches folder. */
    public static final File BRANCHES = Utils.join(GITLET_FOLDER, "branches");
    /** active branch. */
    public static final File ACTIVE_BRANCH = Utils.join(
            BRANCHES, "active_branch.txt");
    /** master branch. */
    public static final File MASTER = Utils.join(BRANCHES, "master.txt");

    public void init() {
        if (GITLET_FOLDER.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            System.exit(0);
        } else {
            GITLET_FOLDER.mkdirs();
            COMMIT_DIR.mkdir();
            STAGING_AREA.mkdir();
            ADD.mkdir();
            REMOVE.mkdir();
            ACTIVE_BRANCH.mkdir();
            BRANCHES.mkdir();

            try {
                HEAD.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                MASTER.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Commit initialCommit = new Commit();
            String id = initialCommit.getID();
            Utils.writeObject(Utils.join(
                    COMMIT_DIR, id + ".txt"), initialCommit);
            Utils.writeObject(HEAD, initialCommit);
            Utils.writeObject(MASTER, initialCommit);

            Utils.writeObject(ACTIVE_BRANCH, MASTER);
        }
    }

    public void log() {
        Commit headCommit = Utils.readObject(
                new File(GITLET_FOLDER,  "head.txt"), Commit.class);
        String headID = headCommit.getID();
        while (true) {
            System.out.println(headCommit.makeLog());
            if (headCommit.getParents() == null) {
                break;
            }
            headID = headCommit.getParents().get(0);
            headCommit = Utils.readObject(new File(
                    COMMIT_DIR, headID + ".txt"), Commit.class);
        }
    }
    public void add(String filename) {
        File[] addFileList = ADD.listFiles();
        if (addFileList != null) {
            for (File file : addFileList) {
                File newfile = Utils.readObject(file, File.class);
                if (filename.equals(newfile.getName())) {
                    file.delete();
                }
            }
        }
        File file = new File(CWD, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            String sha1 = Utils.sha1(Utils.readContents(file), filename);
            Commit temp = Utils.readObject(
                    new File(GITLET_FOLDER,  "head.txt"), Commit.class);
            String headId = temp.getID();

            File headFile = new File(COMMIT_DIR, headId + ".txt");
            Commit headCommit = Utils.readObject(headFile, Commit.class);
            Blob previousBlob = headCommit.getTrackedFiles().get(filename);
            if ((previousBlob == null) || (!(
                    sha1.equals(previousBlob.getSha1())))) {
                Utils.writeObject(Utils.join(ADD, sha1 + ".txt"), file);
            }
            File[] removeFileList = REMOVE.listFiles();
            for (File temp2 : removeFileList) {
                if (file.equals(Utils.readObject(temp2, File.class))) {
                    File newTemp = new File(REMOVE, temp2.getName());
                    newTemp.delete();
                }
            }
        }
    }
    public void commit(String message) {
        HashMap<String, Blob> temp = new HashMap<>();
        Commit tempC = Utils.readObject(new File(
                GITLET_FOLDER,  "head.txt"), Commit.class);
        String headId = tempC.getID();
        Commit headCommit = Utils.readObject(
                new File(COMMIT_DIR, headId + ".txt"), Commit.class);

        for (String name : headCommit.getTrackedFiles().keySet()) {
            temp.put(name, headCommit.getTrackedFiles().get(name));
        }

        File[] addFileList = ADD.listFiles();
        File[] removeFileList = REMOVE.listFiles();

        if (removeFileList.length == 0 && addFileList.length == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else {
            for (File file : addFileList) {
                File fileContent = Utils.readObject(file, File.class);
                temp.put(fileContent.getName(), new Blob(fileContent));
                Blob test = new Blob(fileContent);
                file.delete();
            }
            for (File file : removeFileList) {
                File fileContent = Utils.readObject(file, File.class);
                temp.remove(fileContent.getName());
                file.delete();
            }

            ArrayList<String> parent = new ArrayList<>();
            parent.add(headId);

            Commit newCommit = new Commit(message, parent, temp);

            String headID = newCommit.getID();
            Utils.writeObject(Utils.join(
                    COMMIT_DIR, headID + ".txt"), newCommit);

            Utils.writeObject(HEAD, newCommit);

            File active = Utils.readObject(ACTIVE_BRANCH, File.class);
            Utils.writeObject(active, newCommit);
        }
    }

    public void checkout(String commitID,
                         String filename, String branchname) {
        if (commitID == null && branchname == null) {
            Commit temp = Utils.readObject(
                    new File(GITLET_FOLDER,  "head.txt"), Commit.class);
            String headID = temp.getID();
            Commit headCommit = Utils.readObject(new File(COMMIT_DIR,
                    headID + ".txt"), Commit.class);
            if (!(headCommit.getTrackedFiles().containsKey(filename))) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            } else {
                Blob headBlob = headCommit.getTrackedFiles().get(filename);
                Utils.writeContents(Utils.join(
                        CWD, filename), Blob.getContent(headBlob));
            }
        } else if (branchname == null) {
            File[] tempList = COMMIT_DIR.listFiles();
            String fullID = " ";
            for (File temp : tempList) {
                if (temp.getName().startsWith(commitID)) {
                    fullID = temp.getName();
                }
            }
            if (fullID.equals(" ")) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            } else {
                Commit tempCommit = Utils.readObject(
                        new File(COMMIT_DIR, fullID), Commit.class);
                if (!(tempCommit.getTrackedFiles().containsKey(filename))) {
                    System.out.println("File does not exist in that commit.");
                    System.exit(0);
                } else {
                    Blob tempBlob = tempCommit.getTrackedFiles().get(filename);
                    Utils.writeContents(
                            Utils.join(CWD, filename),
                            Blob.getContent(tempBlob));
                }
            }
        } else {
            checkout(branchname);
        }
    }

    public void checkout(String branchname) {
        File[] branchList = BRANCHES.listFiles();
        Commit currHead = Utils.readObject(
                new File(GITLET_FOLDER,  "head.txt"), Commit.class);
        HashMap<String, Blob> currTrackedFiles = currHead.getTrackedFiles();

        boolean branchFound = false;
        branchFound = doCheckout2(branchList,
                branchname, branchFound, currHead, currTrackedFiles);

        if (!branchFound) {
            System.out.println("no such branch exists.");
            System.exit(0);
        }
    }
    public boolean doCheckout2(File[] branchList,
                               String branchname,
                               boolean branchFound,
                               Commit currHead,
                               HashMap<String, Blob> currTrackedFiles) {
        for (File branch : branchList) {
            if (branch.getName().equals(branchname + ".txt")) {
                if (Utils.readObject(ACTIVE_BRANCH,
                        File.class).equals(branch)) {
                    System.out.println(
                            "No need to checkout the current branch.");
                    System.exit(0);
                } else {
                    branchFound = true;
                    boolean found2 = false;
                    Commit headCommit = Utils.readObject(
                            new File(BRANCHES, branchname
                                    + ".txt"), Commit.class);
                    File[] currentFiles = CWD.listFiles();
                    for (File file : currentFiles) {
                        boolean tempFound = doCheckout1(
                                headCommit, file, currHead, found2);
                        found2 = tempFound || found2;
                    }
                    for (Blob file
                            : headCommit.getTrackedFiles().values()) {
                        Utils.writeContents(Utils.join(
                                CWD,
                                file.getFileName()),
                                Blob.getContent(file));
                    }
                    for (Blob file : currTrackedFiles.values()) {
                        boolean found = false;
                        for (Blob file2
                                : headCommit.getTrackedFiles().values()) {
                            if (file.getFileName().equals(
                                    file2.getFileName())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            File tempFile = new File(CWD, file.getFileName());
                            tempFile.delete();
                        }
                    }
                    Utils.writeObject(ACTIVE_BRANCH, branch);
                    Utils.writeObject(Utils.join(
                            BRANCHES, branchname + ".txt"), headCommit);
                    Utils.writeObject(HEAD, headCommit);
                    File[] addFiles = ADD.listFiles();
                    File[] removeFiles = REMOVE.listFiles();
                    for (File delete : addFiles) {
                        delete.delete();
                    }
                    for (File delete : removeFiles) {
                        delete.delete();
                    }
                }
            }
        }
        return branchFound;
    }

    public void rm(String filename) {
        File[] addFileList = ADD.listFiles();
        File file = new File(CWD, filename);
        Commit headCommit = Utils.readObject(
                new File(GITLET_FOLDER,  "head.txt"), Commit.class);
        String headID = headCommit.getID();
        HashMap<String, Blob> headTracked = headCommit.getTrackedFiles();

        boolean addContains = false;
        for (File temp : addFileList) {
            if (Utils.readObject(temp, File.class).getName().equals(filename)) {
                addContains = true;
            }
        }
        if (addContains) {
            for (File add : addFileList) {
                File fileContent = Utils.readObject(add, File.class);
                if (filename.equals(fileContent.getName())) {
                    add.delete();
                }
            }
        } else if (headTracked.containsKey(filename)) {
            for (Blob temp : headCommit.getTrackedFiles().values()) {
                if (filename.equals(temp.getFileName())) {
                    String sha1 = temp.getSha1();
                    Utils.writeObject(Utils.join(REMOVE, sha1 + ".txt"), file);
                    file.delete();
                }
            }
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    public void find(String message) {
        File[] commitList = COMMIT_DIR.listFiles();
        boolean found = false;
        for (File temp : commitList) {
            Commit tempCommit = Utils.readObject(temp, Commit.class);
            if (tempCommit.getMessage().equals(message)) {
                found = true;
                System.out.println(tempCommit.getID());
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message");
        }
    }

    public void globalLog() {
        File[] commitList = COMMIT_DIR.listFiles();
        for (File temp : commitList) {
            Commit tempCommit = Utils.readObject(temp, Commit.class);
            System.out.println(tempCommit.makeLog());
        }
    }

    public void branch(String branchName) {
        File temp = Utils.join(BRANCHES, branchName + ".txt");
        if (temp.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            try {
                temp.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Commit tempC = Utils.readObject(
                    new File(GITLET_FOLDER,  "head.txt"), Commit.class);
            Utils.writeObject(temp, tempC);
        }
    }

    public void status() {
        System.out.println("=== Branches ===");
        File activeBranch = Utils.readObject(ACTIVE_BRANCH, File.class);
        String activeName = activeBranch.getName();

        File[] branchList = BRANCHES.listFiles();
        ArrayList<String> branchNames = new ArrayList<>();
        for (File branch : branchList) {
            if (!(branch.getName().equals("active_branch.txt"))) {
                branchNames.add(branch.getName());
            }
        }
        Collections.sort(branchNames);
        for (String branch : branchNames) {
            if (branch.equals(activeName)) {
                System.out.println("*"
                        + branch.substring(0, activeName.length() - 4));
            } else {
                System.out.println(branch.substring(0, branch.length() - 4));
            }

        }

        System.out.println();

        System.out.println("=== Staged Files ===");
        File[] addList = ADD.listFiles();
        ArrayList<File> added = new ArrayList<>();
        for (File stagedFile : addList) {
            added.add(Utils.readObject(stagedFile, File.class));
        }
        Collections.sort(added);
        for (File tempfile : added) {
            System.out.println(tempfile.getName());
        }

        System.out.println();

        System.out.println("=== Removed Files ===");
        File[] removeList = REMOVE.listFiles();
        ArrayList<File> removed = new ArrayList<>();
        for (File stagedFile : removeList) {
            removed.add(Utils.readObject(stagedFile, File.class));
        }
        Collections.sort(removed);
        for (File tempfile : removed) {
            System.out.println(tempfile.getName());
        }

        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public void reset(String id) {
        File[] tempList = COMMIT_DIR.listFiles();
        String fullID = " ";
        for (File temp : tempList) {
            if (temp.getName().startsWith(id)) {
                fullID = temp.getName();
            }
        }
        File commitFile = new File(COMMIT_DIR, fullID);
        if (fullID.equals(" ")) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit headCommit = Utils.readObject(
                new File(GITLET_FOLDER, "head.txt"), Commit.class);
        Commit givenCommit = Utils.readObject(commitFile, Commit.class);
        File[] currFiles = CWD.listFiles();
        HashMap<String, Blob> headTracked = headCommit.getTrackedFiles();
        HashMap<String, Blob> givenTracked = givenCommit.getTrackedFiles();
        boolean trackedError = false;
        for (File temp : currFiles) {
            for (Blob temp2 : givenTracked.values()) {
                if (temp.getName().equals(temp2.getFileName())) {
                    if (!(Utils.readContentsAsString(
                            temp).equals(temp2.getStringContent()))) {
                        boolean foundInHead = false;
                        for (Blob temp3 : headTracked.values()) {
                            if (temp3.getFileName().equals(temp.getName())) {
                                foundInHead = true;
                                if (!(temp3.getStringContent().equals(
                                        Utils.readContentsAsString(
                                                temp)))) {
                                    trackedError = true;
                                    System.out.println("There is "
                                            + "an untracked file "
                                            + "in the way; "
                                            + "delete it, or add "
                                            + "and commit it first.");
                                    System.exit(0);
                                }
                            }
                        }
                        if (!foundInHead) {
                            trackedError = true;
                            System.out.println(
                                    "There is an untracked "
                                            + "file in the way; "
                                            + "delete it, "
                                            + "or add and "
                                            + "commit it first.");
                            System.exit(0);
                        }
                    }
                }
            }
        }
        if (!trackedError) {
            doReset(headCommit, givenCommit, id);
        }
    }

    public void doReset(Commit headCommit, Commit givenCommit, String id) {
        boolean foundFile = false;
        for (Blob headTemp : headCommit.getTrackedFiles().values()) {
            for (Blob givenTemp
                    : givenCommit.getTrackedFiles().values()) {
                if (givenTemp.getFileName().equals(headTemp.getFileName())) {
                    checkout(id, givenTemp.getFileName(), null);
                    foundFile = true;
                }
            }
            if (!foundFile) {
                headTemp.getSource().delete();
            }
        }
        File currBranch = Utils.readObject(ACTIVE_BRANCH, File.class);
        Utils.writeObject(currBranch, givenCommit);
        Utils.writeObject(HEAD, givenCommit);
        File[] addFiles = ADD.listFiles();
        File[] removeFiles = REMOVE.listFiles();
        for (File delete : addFiles) {
            delete.delete();
        }
        for (File delete : removeFiles) {
            delete.delete();
        }
    }


    public void merge(String branchname) {
        boolean encounterConflict = false;
        File givenBranch = Utils.join(BRANCHES, branchname + ".txt");
        File currentBranch = Utils.readObject(ACTIVE_BRANCH, File.class);
        if ((!givenBranch.exists())) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Commit givenHeadCommit = Utils.readObject(givenBranch, Commit.class);
        Commit currentHeadCommit = Utils.readObject(
                currentBranch, Commit.class);
        Commit splitPoint = findSplitPoint(branchname);
        boolean keepGoing = merge2(branchname, splitPoint);
        if (keepGoing) {
            HashMap<String, Blob> trackedFilesSplitPoint
                    = splitPoint.getTrackedFiles();
            HashMap<String, Blob> trackedFilesCurrent
                    = currentHeadCommit.getTrackedFiles();
            HashMap<String, Blob> trackedFilesGiven
                    = givenHeadCommit.getTrackedFiles();
            ArrayList<Blob> modifiedGiven = new ArrayList<>();
            ArrayList<Blob> removedGiven = new ArrayList<>();
            ArrayList<Blob> modifiedCurrent = new ArrayList<>();
            ArrayList<Blob> removedCurrent = new ArrayList<>();

            doMerge6(trackedFilesSplitPoint, trackedFilesCurrent,
                    modifiedCurrent, removedCurrent,
                    modifiedGiven, removedGiven, trackedFilesGiven);

            boolean tempEncounter = doMerge1(modifiedGiven,
                    modifiedCurrent, removedCurrent,
                    trackedFilesCurrent, givenHeadCommit);
            encounterConflict = (encounterConflict || tempEncounter);
            doMerge2(trackedFilesGiven, trackedFilesSplitPoint,
                    trackedFilesCurrent, givenHeadCommit);
            tempEncounter = doMerge3(trackedFilesCurrent,
                    trackedFilesGiven, trackedFilesSplitPoint);
            encounterConflict = (encounterConflict || tempEncounter);
            doMerge4(trackedFilesSplitPoint, trackedFilesCurrent, removedGiven);
            tempEncounter = doMerge5(modifiedCurrent, removedGiven,
                    encounterConflict);
            encounterConflict = (encounterConflict || tempEncounter);
            commit("Merged " + branchname
                    + " into " + currentBranch.getName().substring(0,
                    currentBranch.getName().length() - 4) + ".", branchname);
            if (encounterConflict) {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }
    public void doMerge6(HashMap<String, Blob> trackedFilesSplitPoint,
                         HashMap<String, Blob> trackedFilesCurrent,
                         ArrayList<Blob> modifiedCurrent,
                         ArrayList<Blob> removedCurrent,
                         ArrayList<Blob> modifiedGiven,
                         ArrayList<Blob> removedGiven,
                         HashMap<String, Blob> trackedFilesGiven) {
        for (Blob file : trackedFilesSplitPoint.values()) {
            boolean exists = false;
            for (Blob file2 : trackedFilesCurrent.values()) {
                if (file.getFileName().equals(file2.getFileName())) {
                    exists = true;
                    if (!(file.getSha1().equals(file2.getSha1()))) {
                        modifiedCurrent.add(file2);
                    }
                }
            }
            if (!exists) {
                removedCurrent.add(file);
            }
        }
        for (Blob file : trackedFilesSplitPoint.values()) {
            boolean exists = false;
            for (Blob file2 : trackedFilesGiven.values()) {
                if (file.getFileName().equals(file2.getFileName())) {
                    exists = true;
                    if (!(file.getSha1().equals(file2.getSha1()))) {
                        modifiedGiven.add(file2);
                    }
                }
            }
            if (!exists) {
                removedGiven.add(file);
            }
        }
    }
    public boolean doMerge5(ArrayList<Blob> modifiedCurrent,
                            ArrayList<Blob> removedGiven,
                            boolean encounterConflict) {
        for (Blob file : modifiedCurrent) {
            for (Blob file2 : removedGiven) {
                if (file.getFileName().equals(file2.getFileName())) {
                    String currentContents = file.getStringContent();
                    String newContent = "<<<<<<< HEAD"
                            + "\n" + currentContents
                            + "=======" + "\n" + ">>>>>>>" + "\n";
                    Utils.writeContents(Utils.join(
                            CWD, file.getFileName()), newContent);
                    add(file.getFileName());
                    encounterConflict = true;
                }
            }
        }
        return encounterConflict;
    }

    public HashMap<Commit, Integer> parents(Commit temp,
                                            ArrayList<Commit> parents,
                            HashMap<Commit, Integer> work, int counter) {
        if (!(parents == null)) {
            for (Commit parent : parents) {
                ArrayList<String> doubleParentsID = parent.getParents();
                ArrayList<Commit> doubleParents = new ArrayList<>();
                if (!(doubleParentsID == null)) {
                    for (String id : doubleParentsID) {
                        Commit temp2 = Utils.readObject(
                                new File(
                                        COMMIT_DIR, id + ".txt"),
                                        Commit.class);
                        doubleParents.add(temp2);
                    }
                }
                if (work.containsKey(parent)) {
                    if (work.get(parent) > counter) {
                        work.put(parent, counter);
                    }
                } else {
                    work.put(parent, counter);
                }
                work = parents(parent, doubleParents, work, counter + 1);
            }
        }
        return work;
    }

    public void rmBranch(String branchName) {
        File currentBranch = Utils.readObject(ACTIVE_BRANCH, File.class);
        if (currentBranch.getName().equals(branchName + ".txt")) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            File[] branchList = BRANCHES.listFiles();
            boolean branchFound = false;
            for (File temp : branchList) {
                if (temp.getName().equals(branchName + ".txt")) {
                    branchFound = true;
                }
            }
            if (!branchFound) {
                System.out.println("A branch with that name does not exist.");
                System.exit(0);
            } else {
                File givenBranch = new File(BRANCHES, branchName + ".txt");
                givenBranch.delete();
            }
        }
    }
    public void commit(String message, String branchname) {
        HashMap<String, Blob> temp = new HashMap<>();
        Commit tempC = Utils.readObject(new File(
                GITLET_FOLDER,  "head.txt"), Commit.class);
        String headID = tempC.getID();
        Commit headCommit = Utils.readObject(
                new File(COMMIT_DIR, headID + ".txt"), Commit.class);

        Commit secondHead = Utils.readObject(
                new File(BRANCHES, branchname + ".txt"), Commit.class);
        String secondHeadID = secondHead.getID();

        for (String name : headCommit.getTrackedFiles().keySet()) {
            temp.put(name, headCommit.getTrackedFiles().get(name));
        }

        File[] addFileList = ADD.listFiles();
        File[] removeFileList = REMOVE.listFiles();

        if (removeFileList.length == 0 && addFileList.length == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else {
            for (File file : addFileList) {
                File fileContent = Utils.readObject(file, File.class);
                temp.put(fileContent.getName(), new Blob(fileContent));
                file.delete();
            }

            for (File file : removeFileList) {
                String fileContent = Utils.readContentsAsString(file);
                temp.remove(fileContent);
                file.delete();
            }

            ArrayList<String> parent = new ArrayList<>();
            parent.add(headID);
            parent.add(secondHeadID);

            Commit newCommit = new Commit(message, parent, temp);

            String headID2 = newCommit.getID();
            Utils.writeObject(Utils.join(
                    COMMIT_DIR, headID2 + ".txt"), newCommit);

            Utils.writeObject(HEAD, newCommit);

            File active = Utils.readObject(ACTIVE_BRANCH, File.class);
            Utils.writeObject(active, newCommit);
        }
    }

    public boolean merge2(String branchname, Commit splitPoint) {
        File[] addList = ADD.listFiles();
        File[] removeList = REMOVE.listFiles();
        File[] cwdFiles = CWD.listFiles();
        File currentBranch = Utils.readObject(ACTIVE_BRANCH, File.class);
        Commit currentHeadCommit = Utils.readObject(
                currentBranch, Commit.class);
        HashMap<String, Blob> currTrackedFiles
                = currentHeadCommit.getTrackedFiles();
        File givenBranch = Utils.join(BRANCHES, branchname + ".txt");
        Commit givenHeadCommit = Utils.readObject(givenBranch, Commit.class);
        HashMap<String, Blob> givenTrackedFiles
                = givenHeadCommit.getTrackedFiles();
        if (currentBranch.getName().equals(givenBranch.getName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
            return false;
        }
        if (addList.length != 0 || removeList.length != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
            return false;
        }
        boolean tempBoolC = merge21(cwdFiles, currTrackedFiles,
                givenTrackedFiles);
        if (!tempBoolC) {
            return false;
        }
        boolean tempBool2 = false;
        HashMap<String, Blob> splitTrackedFiles = splitPoint.getTrackedFiles();


        return merge22(currentBranch, givenBranch, splitPoint,
                givenHeadCommit, currentHeadCommit, branchname);
    }
    public boolean merge21(File[] cwdFiles, HashMap<String,
            Blob> currTrackedFiles, HashMap<String, Blob> givenTrackedFiles) {
        for (Blob temp1 : currTrackedFiles.values()) {
            File tempFile = new File(CWD, temp1.getFileName());
            if (tempFile.exists()) {
                for (File temp2 : cwdFiles) {
                    if (temp1.getFileName().equals(temp2.getName())) {
                        if (!(temp1.getStringContent().equals(
                                Utils.readContentsAsString(temp2)))) {
                            boolean found = false;
                            for (Blob temp3
                                    : givenTrackedFiles.values()) {
                                if (temp1.getFileName(
                                ).equals(temp3.getFileName())) {
                                    found = true;
                                    if (!(Utils.readContentsAsString(
                                            temp2).equals(
                                            temp3.getStringContent()))) {
                                        System.out.println(
                                                "There is an untracked "
                                                        + "file in the way; "
                                                        + "delete it, "
                                                        + "or add and "
                                                        + "commit it first.");
                                        System.exit(0);
                                        return false;
                                    }
                                }
                            }
                            if (!found) {
                                System.out.println(
                                        "There is an untracked "
                                                + "file in the way; "
                                                + "delete it, "
                                                + "or add and "
                                                + "commit it first.");
                                System.exit(0);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        if (!(merge211(givenTrackedFiles, cwdFiles, currTrackedFiles))) {
            return false;
        }
        return true;

    }
    public boolean merge211(HashMap<String, Blob> givenTrackedFiles,
                            File[] cwdFiles,
                            HashMap<String, Blob> currTrackedFiles) {
        boolean found3 = false;
        for (Blob temp : givenTrackedFiles.values()) {
            for (File temp2 : cwdFiles) {
                if (temp.getFileName().equals(temp2.getName())) {
                    if ((!temp.getStringContent().equals(
                            Utils.readContentsAsString(temp2)))) {
                        found3 = false;
                        for (Blob temp3 : currTrackedFiles.values()) {
                            if (temp.getFileName().equals(
                                    temp3.getFileName())) {
                                found3 = true;
                            }
                        }
                        if (!found3) {
                            System.out.println(
                                    "There is an untracked "
                                            + "file in the way; "
                                            + "delete it, "
                                            + "or add and "
                                            + "commit it first.");
                            System.exit(0);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    public boolean merge22(File currentBranch, File givenBranch,
                           Commit splitPoint, Commit givenHeadCommit,
                           Commit currentHeadCommit,
                           String branchname) {
        if (currentBranch.getName().equals(givenBranch.getName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
            return false;
        } else if (splitPoint.getID().equals(givenHeadCommit.getID())) {
            System.out.println(
                    "Given branch is an ancestor of the current branch.");
            System.exit(0);
            return false;
        } else if (splitPoint.getID().equals(currentHeadCommit.getID())) {
            checkout(null, null, branchname);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
            return false;
        } else if (!(givenBranch.exists())) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
            return false;
        } else {
            return true;
        }
    }

    public Commit findSplitPoint(String branchname) {
        File givenBranch = Utils.join(BRANCHES, branchname + ".txt");
        File currentBranch = Utils.readObject(ACTIVE_BRANCH, File.class);

        if ((!givenBranch.exists())) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Commit givenHeadCommit = Utils.readObject(givenBranch, Commit.class);
        Commit currentHeadCommit
                = Utils.readObject(currentBranch, Commit.class);

        ArrayList<String> givenParentsID = givenHeadCommit.getParents();
        ArrayList<String> currentParentsID = currentHeadCommit.getParents();

        ArrayList<Commit> givenParents = new ArrayList<>();
        ArrayList<Commit> currentParents = new ArrayList<>();
        for (String id : givenParentsID) {
            Commit temp = Utils.readObject(
                    new File(COMMIT_DIR, id + ".txt"), Commit.class);
            givenParents.add(temp);
        }
        for (String id : currentParentsID) {
            Commit temp = Utils.readObject(
                    new File(COMMIT_DIR, id + ".txt"), Commit.class);
            currentParents.add(temp);
        }
        HashMap<Commit, Integer> empty1 = new HashMap<>();
        empty1.put(currentHeadCommit, 0);
        HashMap<Commit, Integer> currParentsMap = parents(
                currentHeadCommit, currentParents, empty1, 1);

        HashMap<Commit, Integer> empty2 = new HashMap<>();
        empty2.put(givenHeadCommit, 0);
        HashMap<Commit, Integer> givenParentsMap = parents(
                givenHeadCommit, givenParents, empty2, 1);


        HashMap<Commit, Integer> splitPointMap = new HashMap<>();
        for (Commit tempParent : currParentsMap.keySet()) {
            for (Commit tempParent2 : givenParentsMap.keySet()) {
                if (tempParent.getID().equals(tempParent2.getID())) {
                    splitPointMap.put(
                            tempParent, currParentsMap.get(tempParent));
                }
            }
        }
        int max = Integer.MAX_VALUE;
        Commit splitPoint = new Commit();
        for (Commit tempSplit : splitPointMap.keySet()) {
            if (splitPointMap.get(tempSplit) < max) {
                max = splitPointMap.get(tempSplit);
                splitPoint = tempSplit;
            }
        }
        return splitPoint;
    }
    public boolean doMerge1(ArrayList<Blob> modifiedGiven,
                            ArrayList<Blob> modifiedCurrent,
                            ArrayList<Blob> removedCurrent,
                            HashMap<String, Blob> trackedFilesCurrent,
                            Commit givenHeadCommit) {
        boolean encounterConflict = false;
        for (Blob file : modifiedGiven) {
            boolean found = false;
            boolean removed = false;
            for (Blob file2 : modifiedCurrent) {
                if (file.getFileName().equals(file2.getFileName())) {
                    found = true;
                    if (!(file.getSha1().equals(file2.getSha1()))) {
                        String currentContents = file2.getStringContent();
                        String givenContents = file.getStringContent();
                        String newContent
                                = "<<<<<<< HEAD" + "\n" + currentContents
                                + "=======" + "\n"
                                + givenContents + ">>>>>>>" + "\n";
                        Utils.writeContents(
                                Utils.join(
                                        CWD, file2.getFileName()),
                                newContent);
                        add(file2.getFileName());
                        encounterConflict = true;
                    }
                }
            }
            for (Blob file3 : removedCurrent) {
                if (file.getFileName().equals(file3.getFileName())) {
                    removed = true;
                    String givenContents = file.getStringContent();
                    String newContent = "<<<<<<< HEAD" + "\n"
                            + "=======" + "\n"
                            + givenContents + ">>>>>>>" + "\n";
                    Utils.writeContents(
                            Utils.join(CWD, file3.getFileName()), newContent);
                    add(file3.getFileName());
                    encounterConflict = true;
                }
            }
            if (!found) {
                if (!removed) {
                    for (Blob file3 : trackedFilesCurrent.values()) {
                        if (file3.getFileName().equals(file.getFileName())) {
                            checkout(givenHeadCommit.getID(),
                                    file.getFileName(), null);
                            add(file.getFileName());
                        }
                    }
                }
            }
        }
        return encounterConflict;
    }

    public void doMerge2(HashMap<String, Blob> trackedFilesGiven,
                         HashMap<String, Blob> trackedFilesSplitPoint,
                         HashMap<String, Blob> trackedFilesCurrent,
                         Commit givenHeadCommit) {
        for (Blob file : trackedFilesGiven.values()) {
            boolean foundSplit = false;
            boolean foundCurrent = false;
            for (Blob file2 : trackedFilesSplitPoint.values()) {
                if (file.getFileName().equals(file2.getFileName())) {
                    foundSplit = true;
                }
            }
            for (Blob file3 : trackedFilesCurrent.values()) {
                if (file.getFileName().equals(file3.getFileName())) {
                    foundCurrent = true;
                }
            }
            if (!foundSplit) {
                if (!foundCurrent) {
                    checkout(givenHeadCommit.getID(), file.getFileName(),
                            null);
                    add(file.getFileName());
                }
            }
        }
    }
    public boolean doMerge3(HashMap<String, Blob> trackedFilesCurrent,
                            HashMap<String, Blob> trackedFilesGiven,
                            HashMap<String, Blob> trackedFilesSplitPoint) {
        boolean encounterConflict = false;
        for (Blob file : trackedFilesCurrent.values()) {
            for (Blob file2 : trackedFilesGiven.values()) {
                if (file.getFileName().equals(file2.getFileName())) {
                    boolean existsInSplit = false;
                    for (Blob file3 : trackedFilesSplitPoint.values()) {
                        if (file3.getFileName().equals(file2.getFileName())) {
                            existsInSplit = true;
                        }
                    }
                    if (!existsInSplit) {
                        String currentContents = file.getStringContent();
                        String givenContents = file2.getStringContent();
                        String newContent
                                = "<<<<<<< HEAD"
                                + "\n" + currentContents
                                + "======="
                                + "\n" + givenContents + ">>>>>>>" + "\n";
                        Utils.writeContents(
                                Utils.join(CWD, file.getFileName()),
                                newContent);
                        add(file.getFileName());
                        encounterConflict = true;
                    }
                }
            }
        }
        return encounterConflict;
    }

    public void doMerge4(
            HashMap<String, Blob> trackedFilesSplitPoint,
            HashMap<String,
            Blob> trackedFilesCurrent, ArrayList<Blob> removedGiven) {
        for (Blob file : trackedFilesSplitPoint.values()) {
            for (Blob file2 : trackedFilesCurrent.values()) {
                if (file.getSha1().equals(file2.getSha1())) {
                    for (Blob file3 : removedGiven) {
                        if (file.getFileName().equals(file3.getFileName())) {
                            rm(file.getFileName());
                        }
                    }
                }
            }
        }
    }

    public boolean doCheckout1(Commit headCommit,
                               File file, Commit currHead,
                               boolean found2) {
        for (Blob file2 : headCommit.getTrackedFiles().values()) {
            if (file.getName().equals(file2.getFileName())) {
                if (!(Utils.readContentsAsString(file).equals(file2.
                        getStringContent()))) {
                    for (Blob file3 : currHead.getTrackedFiles().values()) {
                        if (file3.getFileName().equals(file.getName())) {
                            found2 = true;
                            if (!(file3.getStringContent().equals(Utils.
                                    readContentsAsString(file)))) {
                                System.out.println(
                                        "There is an " + "untracked "
                                                + "file in the "
                                                + "way; delete "
                                                + "it, or " + "add and "
                                                + "commit it " + "first.");
                                System.exit(0);
                            }
                        }
                    }
                    if (!found2) {
                        System.out.println(
                                "There is an " + "untracked " + "file in the "
                                        + "way; delete " + "it, or "
                                        + "add and " + "commit it " + "first.");
                        System.exit(0);
                    }
                }
            }
        }
        return found2;
    }
}
