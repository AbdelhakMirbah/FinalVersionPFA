package ma.emsi.fraud.model;

public class EnrichmentResult {
    private boolean isProxy;
    private boolean isEmailLeaked;

    public EnrichmentResult() {
    }

    public EnrichmentResult(boolean isProxy, boolean isEmailLeaked) {
        this.isProxy = isProxy;
        this.isEmailLeaked = isEmailLeaked;
    }

    public boolean isProxy() {
        return isProxy;
    }

    public void setProxy(boolean isProxy) {
        this.isProxy = isProxy;
    }

    public boolean isEmailLeaked() {
        return isEmailLeaked;
    }

    public void setEmailLeaked(boolean isEmailLeaked) {
        this.isEmailLeaked = isEmailLeaked;
    }
}
