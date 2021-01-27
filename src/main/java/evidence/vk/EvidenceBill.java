package evidence.vk;

import lombok.Data;

import java.util.UUID;

@Data
public class EvidenceBill {

	private final int peerId;
	private final long messageId;
	private final UUID uuid;
	private final double price;
	private final String link;
	private boolean paid;

}
