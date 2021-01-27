package evidence.qiwi;

import com.google.gson.JsonObject;
import com.qiwi.billpayments.sdk.client.BillPaymentClient;
import com.qiwi.billpayments.sdk.client.BillPaymentClientFactory;
import com.qiwi.billpayments.sdk.model.Bill;
import com.qiwi.billpayments.sdk.model.MoneyAmount;
import com.qiwi.billpayments.sdk.model.in.PaymentInfo;
import com.qiwi.billpayments.sdk.model.out.BillResponse;
import evidence.vk.BillStorage;
import lombok.Data;

import java.io.BufferedReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
public class PaymentHandler {

	private final Logger logger;
	private final String publicKey;
	private final String secretKey;
	private final BillPaymentClient client;

	public PaymentHandler(Logger logger, String publicKey, String secretKey) {
		this.logger = logger;
		this.publicKey = publicKey;
		this.secretKey = secretKey;
		this.client = BillPaymentClientFactory.createDefault(secretKey);
	}

	public String createPaymentForm(double price, UUID uuid, String successUrl) {
		MoneyAmount amount = new MoneyAmount(
				BigDecimal.valueOf(price),
				Currency.getInstance("RUB")
		);
		String billId = uuid.toString();
		return client.createPaymentForm(new PaymentInfo(publicKey, amount, billId, successUrl));
	}

	@Data
	static class BillIdWrapper {
		final UUID billId;
	}

	public BillResponse read(Reader reader) {
		String json = new BufferedReader(reader).lines().collect(Collectors.joining(System.lineSeparator()));
//		BillIdWrapper billIdWrapper = BillStorage.gson.fromJson(json, BillIdWrapper.class);
//		BillResponse billInfo = client.getBillInfo(billIdWrapper.getBillId());
//		billInfo.getStatus().getValue()

		JsonObject obj = BillStorage.gson.fromJson(json, JsonObject.class);
		BillResponse billInfo = BillStorage.gson.fromJson(obj.get("bill"), BillResponse.class);
		billInfo = client.getBillInfo(billInfo.getBillId());
		System.out.println(billInfo);
		return billInfo;
	}

}
