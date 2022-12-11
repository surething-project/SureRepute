package pt.ulisboa.tecnico.surerepute;

import com.google.protobuf.ByteString;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class EncryptedPseudonymsQueue {

  private final Map<String, ByteString> queue = new LinkedHashMap<>();
  private final int limit;

  public EncryptedPseudonymsQueue(int limit) {
    this.limit = limit;
  }

  public synchronized boolean containsKey(String userId) {
    return queue.containsKey(userId);
  }

  public synchronized ByteString get(String userId) {
    if (!queue.containsKey(userId)) return null;
    return queue.get(userId);
  }

  public synchronized void put(String userId, ByteString encryptedPseudonym) {
    if (queue.containsKey(userId)) return;
    if (queue.size() == limit) removeFirst();
    queue.put(userId, encryptedPseudonym);
  }

  public synchronized void removeFirst() {
    Iterator<Map.Entry<String, ByteString>> it = queue.entrySet().iterator();
    if (it.hasNext()) {
      it.next();
      it.remove();
    }
  }
}
