import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;

import java.io.*;
import java.util.*;

public class DiffInfo {

    private boolean deletedFile;
    private boolean newFile;
    private String newPath;
    private String oldPath;
    private boolean renamedFile;

    public DiffInfo(boolean deletedFile, boolean newFile, String newPath, String oldPath, boolean renamedFile) {
        this.deletedFile = deletedFile;
        this.newFile = newFile;
        this.newPath = newPath;
        this.oldPath = oldPath;
        this.renamedFile = renamedFile;
    }

    @Override
    public String toString() {
        return "DiffInfo{" +
                "deletedFile=" + deletedFile +
                ", newFile=" + newFile +
                ", newPath='" + newPath +
                ", oldPath='" + oldPath +
                ", renamedFile=" + renamedFile +
                '}';
    }
}

// Скрипт для получения списка изменённых файлов для всех коммитов на всех ветках
public class GetFileDiffsAllBranches {

    // Метод, сопоставляющий каждому человеку имя для сохранения в датасет (в целях анонимизации)
    public static String getUserName(String name) {
        // Тут скрыты фамилии, но при сборе были взяты реальные данные
        List<String> names = List.of("Igor SURNAME", "HSE Apps", "Igor SURNAME2",
                "SURNAME Matvey", "Mikhail", "a SURNAME", "korolevsolutions", "MY_NAME SURNAME", "Mikhail SURNAME",
                "Dmitriy SURNAME", "MY_NAME", "MY_NAME SURNAME", "Andrei SURNAME", "Matvey SURNAME", "Dmitry SURNAME");

        if (names.contains(name)) {
            if (name.equals("Igor SURNAME") || name.equals("Igor SURNAME2")) {
                return "Team Lead";
            } else if (name.equals("HSE Apps")) {
                return "HSE Apps";
            } else if (name.equals("SURNAME Matvey") || name.equals("Matvey SURNAME")) {
                return "iOS Lead";
            } else if (name.equals("Dmitry SURNAME") || name.equals("Dmitriy SURNAME")) {
                return "iOS Middle";
            } else if (name.equals("Andrei SURNAME") || name.equals("korolevsolutions") || name.equals("a SURNAME")) {
                return "iOS Junior (left)";
            } else if (name.equals("Mikhail") || name.equals("Mikhail SURNAME")) {
                return "iOS Middle (left)";
            } else if (name.equals("MY_NAME") || name.equals("MY_NAME SURNAME") || name.equals("MY_NAME SURNAME")) {
                return "iOS Junior";
            }
        }

        return "Developer";
    }

    public void getFileDiffs() {
        var key = "MY_KEY";

        GitLabApi gitLabApi = new GitLabApi("https://g.hse.ru", key);
        List<String> namesForBranches = List.of("Payments", "dev", "feature_rate_app", "master", "new-widgets");
        try {

            OutputStream os = new FileOutputStream("/Users/liuda/Desktop/gitlab/all-branches-commits-diffs.csv");
            os.write(239);
            os.write(187);
            os.write(191);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.append("created at;id;title;message;author;parent;branch;diffs\n");

            for (String branchName: namesForBranches) {
                List<Commit> commits = gitLabApi.getCommitsApi().getCommits("68", branchName,  new GregorianCalendar(2020, Calendar.AUGUST, 4).getTime(),
                        new Date());

                System.out.println(commits.size());
                System.out.println(branchName);

                for (Commit commit: commits) {
                    String output = commit.getCreatedAt() + ";" +  commit.getId() + ";" + commit.getTitle() + ";" +
                            commit.getMessage().replace("\n", "") + ";" + getUserName(commit.getAuthorName()) +
                            ";" + (commit.getParentIds().size() >=1 ? commit.getParentIds().get(0) : "") + ";" +
                            branchName + ";";

                    List<DiffInfo> diffInformation = new ArrayList<>();
                    List<Diff> diffs = gitLabApi.getCommitsApi().getDiff("68", commit.getId());

                    for (Diff diff: diffs) {
                        diffInformation.add(new DiffInfo(diff.getDeletedFile(), diff.getNewFile(), diff.getNewPath(),
                                diff.getOldPath(), diff.getRenamedFile()));
                    }

                    String res = "";

                    if (diffInformation.size() > 0) {
                        for (DiffInfo diff:
                                diffInformation) {
                            res += diff;
                            res += ",";
                        }
                    } else {
                        res += "no diff";
                    }

                    res.replace("\n", "/n");
                    output += res;
                    output += "\n";
                    writer.append(output);
                }
            }

            writer.close();

        } catch (GitLabApiException | IOException e) {
            e.printStackTrace();
        }
    }

}
