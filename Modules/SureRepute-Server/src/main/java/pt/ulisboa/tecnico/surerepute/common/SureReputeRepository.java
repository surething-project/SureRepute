package pt.ulisboa.tecnico.surerepute.common;

import org.glassfish.grizzly.utils.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SureReputeRepository {

  private static SureReputeRepository instance;

  public static SureReputeRepository getInstance() {
    if (instance == null) instance = new SureReputeRepository();
    return instance;
  }

  public Pair<Double, Double> hasPseudonym(Connection con, String pseudonym) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("SELECT * FROM pseudonym_score WHERE pseudonym = ?");
    stmt.setString(1, pseudonym);
    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) return null;
    Pair<Double, Double> behavior =
        new Pair<>(rs.getDouble("positive_behavior"), rs.getDouble("negative_behavior"));
    rs.close();
    return behavior;
  }

  public String isFollower(Connection con, String pseudonym) throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("SELECT server_id FROM pseudonym_leader WHERE pseudonym = ?");
    stmt.setString(1, pseudonym);
    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) return "";
    String serverId = rs.getString("server_id");
    rs.close();
    return serverId;
  }

  public List<String> getFollowers(Connection con, String pseudonym) throws SQLException {
    List<String> followers = new ArrayList<>();
    PreparedStatement stmt =
            con.prepareStatement("SELECT server_id from pseudonym_follower WHERE pseudonym = ?");
    stmt.setString(1, pseudonym);
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
      followers.add(rs.getString("server_id"));
    }
    stmt.close();
    return followers;
  }

  public void storePseudonym(Connection con, String pseudonym, Pair<Double, Double> behavior)
      throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement(
            "INSERT INTO pseudonym_score (pseudonym, positive_behavior, negative_behavior) VALUES (?,?,?)");
    stmt.setString(1, pseudonym);
    stmt.setDouble(2, behavior.getFirst());
    stmt.setDouble(3, behavior.getSecond());
    stmt.executeUpdate();
    stmt.close();
  }

  public void storePseudonymFollower(Connection con, String pseudonym, String followerId)
      throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("INSERT INTO pseudonym_follower (pseudonym, server_id) VALUES (?,?)");
    stmt.setString(1, pseudonym);
    stmt.setString(2, followerId);
    stmt.executeUpdate();
    stmt.close();
  }

  public void storePseudonymLeader(Connection con, String pseudonym, String leaderId)
      throws SQLException {
    PreparedStatement stmt =
        con.prepareStatement("INSERT INTO pseudonym_leader (pseudonym, server_id) VALUES (?,?)");
    stmt.setString(1, pseudonym);
    stmt.setString(2, leaderId);
    stmt.executeUpdate();
    stmt.close();
  }

  public void updateBehavior(
          Connection con, String pseudonym, double positiveBehavior, double negativeBehavior)
          throws SQLException {
    PreparedStatement stmt =
            con.prepareStatement(
                    "UPDATE pseudonym_score SET positive_behavior = ?, negative_behavior = ? WHERE pseudonym = ?");
    stmt.setDouble(1, positiveBehavior);
    stmt.setDouble(2, negativeBehavior);
    stmt.setString(3, pseudonym);
    stmt.executeUpdate();
    stmt.close();
  }
}
