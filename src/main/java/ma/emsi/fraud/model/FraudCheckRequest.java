package ma.emsi.fraud.model;

public class FraudCheckRequest {
    private double amount;
    private double oldBalance;
    private double newBalance;
    private String ip;
    private String email;

    public FraudCheckRequest() {
    }

    public FraudCheckRequest(double amount, double oldBalance, double newBalance, String ip, String email) {
        this.amount = amount;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.ip = ip;
        this.email = email;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getOldBalance() {
        return oldBalance;
    }

    public void setOldBalance(double oldBalance) {
        this.oldBalance = oldBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(double newBalance) {
        this.newBalance = newBalance;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
