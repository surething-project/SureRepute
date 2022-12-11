package pt.ulisboa.tecnico.surerepute.key;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.surerepute.SecurityManager;

@ApplicationScoped
public class PublicKeyService {
  private static final Logger logger = LoggerFactory.getLogger(PublicKeyService.class.getName());

  public byte[] getPublicKey() {
    logger.info("GetPublicKey Received!");
    return SecurityManager.getInstance().getPublicKey().getEncoded();
  }
}
