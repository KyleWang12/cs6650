package constants;

public class DatabaseConstants {
  public static final String host = "172.31.31.21";
  public static final String[] replicaHosts = {"172.31.24.207", "172.31.29.203", "172.31.26.118"};
  public static final int port = 6379;
  public static final String password = "wyhadmin";
  public static final String replicaPass = "wyhadminreplica";
  public static final int timeout = 5000;
}
