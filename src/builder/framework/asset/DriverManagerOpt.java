package builder.framework.asset;

public class DriverManagerOpt
{
  private String a;
  private String b;
  private String c;
  private String d;
  private String e;
  private String f;

  public String getIp()
  {
    return this.b;
  }

  public void setIp(String ip) {
    this.b = ip;
  }

  public String getPort() {
    return this.c;
  }

  public void setPort(String port) {
    this.c = port;
  }

  public String getUserName() {
    return this.e;
  }

  public void setUserName(String userName) {
    this.e = userName;
  }

  public String getPassword() {
    return this.f;
  }

  public void setPassword(String password) {
    this.f = password;
  }

  public String getDataBaseType() {
    return this.a;
  }

  public void setDataBaseType(String dataBaseType) {
    this.a = dataBaseType;
  }

  public String getCatalog() {
    return this.d;
  }

  public void setCatalog(String catalog) {
    this.d = catalog;
  }
}