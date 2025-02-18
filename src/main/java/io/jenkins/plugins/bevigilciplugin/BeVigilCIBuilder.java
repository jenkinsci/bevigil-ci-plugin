package io.jenkins.plugins.bevigilciplugin;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;


public class BeVigilCIBuilder extends Builder implements SimpleBuildStep {

    private final Secret apiKey;
    private final String appType;
    private final String appPath;

    private final String packageName;

    private final String scanTimeout;

    private final String severityThreshold;


    @DataBoundConstructor
    public BeVigilCIBuilder(Secret apiKey, String appType, String appPath, String packageName, String scanTimeout, String severityThreshold) {
        this.apiKey = apiKey;
        this.appType = appType;
        this.appPath = appPath;
        this.packageName = packageName;
        this.scanTimeout = scanTimeout;
        this.severityThreshold = severityThreshold;
    }

    public String getAppType() {
        return appType;
    }

    public Secret getApiKey() {return apiKey;}
    public String getAppPath() {return appPath;}

    public String getPackageName() { return packageName; }

    public String getScanTimeout() { return scanTimeout; }

    public String getSeverityThreshold() { return severityThreshold; }

    public Boolean delayRequest() throws InterruptedException {
        Thread.sleep(1000);
        return true;
    }

    private static boolean isChild(Path child, String parentText) {
        Path parent = Paths.get(parentText).toAbsolutePath();
       return child.startsWith(parent);
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        BeVigilCIClient client = new BeVigilCIClient(Messages.BeVigilCIBuilder_BaseUrl(), apiKey.getPlainText());

        try {
            // 1. Get Presigned URL
            GetPresignedUrlResponse presignedUrlResponse = client.getPresignedUrl(appType);
            listener.getLogger().println("Got Presigned URL: " + presignedUrlResponse.url);

            // 2. Upload the APK to the presigned URL
            FilePath absoluteAppPath = new FilePath(workspace, appPath);
            // FilePath absoluteAppPath = new FilePath(launcher.getChannel(), appPath); // Updated to use the agent's local file path

            if (!absoluteAppPath.exists()) {
                throw new Exception("The APK file does not exist at the given path: " + absoluteAppPath.getRemote());
            }
            listener.getLogger().println("Reading APK from path: " + absoluteAppPath.getRemote());

            listener.getLogger().println("Uploading file...");
            client.uploadToPresignedUrl(absoluteAppPath, presignedUrlResponse.url);
            listener.getLogger().println("File uploaded successfully");

            // 3. Submit route and send presigned URL
            SubmitRequest request = new SubmitRequest();
            request.jobID = presignedUrlResponse.jobId;
            request.appType = appType;
            request.uploadUrl = presignedUrlResponse.url;
            request.scanTimeout = Integer.valueOf(scanTimeout);
            request.severityThreshold = severityThreshold;
            request.sourceCI = "jenkins";
            request.packageName = packageName;
            client.submit(request);

            // 4. Poll Status
            listener.getLogger().println("Scanning app for vulnerabilities...");
            String status = "";
            Integer logCount = 0;
            do{
//                listener.getLogger().print("Checking status...");
                StatusResponse statusResponse = client.status(presignedUrlResponse.jobId);
                status = statusResponse.status;

                if(status.equalsIgnoreCase("submitted") && logCount < 1){
                    listener.getLogger().println("Checking status... (Status is " + status + ")");
                    logCount++;
                }
                if(status.equalsIgnoreCase("error")){
                    System.out.println(status);
                    throw new Exception("Scan error");
                }

                System.out.println(status);
            }while (!status.equalsIgnoreCase("completed") && delayRequest());
            listener.getLogger().println("Scan Completed!");

            // 5. Output
            OutputResponse output = client.output(presignedUrlResponse.jobId);
            listener.getLogger().println(output.output);


        } catch (Exception e) {
            listener.getLogger().println("error" + e.getLocalizedMessage());
        }
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public FormValidation doCheckAppPath(@QueryParameter String appPath) {
            if (appPath.strip().length() == 0) {
                return FormValidation.error(Messages.BeVigilCIBuilder_errors_invalidPath());
            }
            try {
                Paths.get(appPath);
            } catch (InvalidPathException ex) {
                return FormValidation.error(Messages.BeVigilCIBuilder_errors_invalidPath());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApiKey(@QueryParameter String apiKey) {
            return FormValidation.ok();
        }

        public FormValidation doCheckScanTimeout(@QueryParameter String scanTimeout){
            int scanInt;
            try{
                scanInt = Integer.parseInt(scanTimeout);
            }catch (NumberFormatException e){
                return FormValidation.error(Messages.BeVigilCIBuilder_errors_scanTimeout());
            }
            if(scanInt < 5 || scanInt > 60){
                return FormValidation.error(Messages.BeVigilCIBuilder_errors_scanTimeout());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckPackageName(@QueryParameter String packageName){
            // String regex = "^([A-Za-z]{1}[A-Za-z\\d_]*\\.)+[A-Za-z][A-Za-z\\d_]*$";

            // if(!packageName.matches(regex)){
            //     return FormValidation.error(FortifyBuilder_errors_packageName());
            // }
            return FormValidation.ok();
        }

        public ListBoxModel doFillAppTypeItems() {
            return new ListBoxModel(
                    new ListBoxModel.Option("iOS", "ios"),
                    new ListBoxModel.Option("Android", "android")
            );
        }

        public ListBoxModel doFillSeverityThresholdItems() {
            return new ListBoxModel(
                    new ListBoxModel.Option("Low", "low"),
                    new ListBoxModel.Option("Medium", "medium"),
                    new ListBoxModel.Option("High", "high")
            );
        }
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.BeVigilCIBuilder_DisplayName();
        }

    }

}
