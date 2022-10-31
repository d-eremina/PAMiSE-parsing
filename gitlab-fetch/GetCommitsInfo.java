import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;

import java.io.*;
import java.util.*;

// Скрипт для получения информации о коммитах для одной ветки
public class GetCommitsInfo {
    public void getInfo() {
        var key = "MY_KEY";

        GitLabApi gitLabApi = new GitLabApi("https://g.hse.ru", key);
        List<String> namesForBranches = List.of("Payments", "dev", "feature_rate_app", "master", "new-widgets");

        try {
            // Подставляем имя конкретной ветки в вызов метода
            List<Commit> commits = gitLabApi.getCommitsApi().getCommits("68", "new-widgets",  new GregorianCalendar(2020, Calendar.AUGUST, 4).getTime(),
                    new Date());
            System.out.println(commits.size());

            // Тут скрыты фамилии, но при сборе были взяты реальные данные
            List<String> names = List.of("Igor SURNAME", "HSE Apps", "Igor SURNAME2",
                    "SURNAME Matvey", "Mikhail", "a SURNAME", "korolevsolutions", "MY_NAME SURNAME", "Mikhail SURNAME",
                    "Dmitriy SURNAME", "MY_NAME", "MY_NAME SURNAME2", "Andrei SURNAME", "Matvey SURNAME", "Dmitry SURNAME");

            OutputStream os = new FileOutputStream("/Users/liuda/Desktop/gitlab/branch-new_widgets-commits.csv");
            os.write(239);
            os.write(187);
            os.write(191);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

            for (Commit commit: commits) {
                String output = commit.getCreatedAt() + ";" +  commit.getId() + ";" + commit.getTitle() + ";" + commit.getMessage().replace("\n", "") + ";";

                String name = commit.getAuthorName();
                if (names.contains(name)) {
                    if (name.equals("Igor SURNAME") || name.equals("Igor SURNAME2")) {
                        output += "Team Lead";
                    } else if (name.equals("HSE Apps")) {
                        output += "HSE Apps";
                    } else if (name.equals("SURNAME Matvey") || name.equals("Matvey SURNAME")) {
                        output += "iOS Lead";
                    } else if (name.equals("Dmitry SURNAME") || name.equals("Dmitriy SURNAME")) {
                        output += "iOS Middle";
                    } else if (name.equals("Andrei SURNAME") || name.equals("korolevsolutions") || name.equals("a SURNAME")) {
                        output += "iOS Junior (left)";
                    } else if (name.equals("Mikhail") || name.equals("Mikhail SUNAME")) {
                        output += "iOS Middle (left)";
                    } else if (name.equals("MY_NAME") || name.equals("MY_NAME SURNAME") || name.equals("MY_NAME SURNAME2")) {
                        output += "iOS Junior";
                    }
                } else {
                    output += "Developer";
                }

                if (commit.getParentIds().size() >=1) {
                    output += ";";
                    output += commit.getParentIds().get(0);
                }
                output += "\n";

                writer.append(output);
            }
            writer.close();

        } catch (GitLabApiException | IOException e) {
            e.printStackTrace();
        }
    }

}
