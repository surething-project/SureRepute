package pt.ulisboa.tecnico.surerepute.identityprovider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IdentityProviderRepository {

  private static IdentityProviderRepository instance;

  public static IdentityProviderRepository getInstance() {
    if (instance == null) instance = new IdentityProviderRepository();
    return instance;
  }

  public String getPseudonym(Connection con, String userId) throws SQLException {
    PreparedStatement stmt = con.prepareStatement("SELECT * FROM pseudonym WHERE user_id=?");
    stmt.setString(1, userId);
    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) {
      rs.close();
      return "";
    }
    String pseudonym = rs.getString("pseudonym");
    rs.close();
    return pseudonym;
  }

  public void storePseudonym(Connection con, String userId, String pseudonym) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("INSERT INTO pseudonym (user_id, pseudonym) VALUES (?,?)");
    stmt.setString(1, userId);
    stmt.setString(2, pseudonym);
    stmt.executeUpdate();
  }
}
