package activesupport.jenkins;

import activesupport.jenkins.exceptions.JenkinsBuildFailed;
import activesupport.MissingRequiredArgument;
import activesupport.system.Properties;
import activesupport.system.out.Output;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.helper.Range;
import com.offbytwo.jenkins.model.*;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class Jenkins {

    public enum Job {
        NI_EXPORT("Batch_data-gov-NI-export"),
        BATCH_PROCESS_QUEQUE("Batch_Process_Queue");

        private final String name;

        Job(@NotNull String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static void trigger(@NotNull Job job, @NotNull HashMap<String, String> params) throws Exception {
        String username = Jenkins.username();
        String password = Jenkins.password();

        JenkinsServer batchProcessJobs = new JenkinsServer(new URI("http://olcsci.shd.ci.nonprod.dvsa.aws:8080/"), username, password);
        batchProcessJobs.enableJob(job.toString());
        JobWithDetails details = batchProcessJobs.getJob(job.toString());
        int lastSuccessfulBuild = details.getNextBuildNumber();
        details.build(params);
        List<Build> builds = details.getAllBuilds(Range.build().from(lastSuccessfulBuild).build());

        for (Build build : builds) {
            BuildWithDetails buildInfo = build.details();

            while (buildInfo.isBuilding()) {
                if (buildInfo.getResult() == BuildResult.SUCCESS) {
                    break;
                } else if (buildInfo.getResult() == BuildResult.FAILURE) {
                    throw new JenkinsBuildFailed();
                } else {
                    throw new Exception(Output.printColoredLog("[ERROR] Jenkins job was not successfully completed"));
                }
            }
        }
    }

    public static String username() throws MissingRequiredArgument {
        if (Properties.get("JENKINS_USERNAME") != null) {
            return Properties.get("JENKINS_USERNAME");
        } else {
            throw new MissingRequiredArgument("[ERROR] JENKINS_USERNAME should be passed in during runtime "
                    + "or should be specified in your system variables. This variable should hold the value of "
                    + "you Jenkins username"
            );
        }
    }

    private static String password() throws MissingRequiredArgument {
        if (Properties.get("JENKINS_PASSWORD") != null) {
            return Properties.get("JENKINS_PASSWORD");
        } else {
            throw new MissingRequiredArgument("[ERROR] JENKINS_PASSWORD should be passed in during runtime "
                    + "or should be specified in your system variables. This variable should hold the value of "
                    + "you Jenkins password"
            );
        }
    }
}
